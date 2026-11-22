/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * Original hard forked code:
 * https://github.com/ReVanced/revanced-patches/commit/724e6d61b2ecd868c1a9a37d465a688e83a74799
 *
 * See the included NOTICE file for GPLv3 Section 7 terms that apply to Morphe contributions.
 */

package app.morphe.extension.shared.settings.preference.about;

import static app.morphe.extension.shared.StringRef.str;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import app.morphe.extension.shared.Logger;
import app.morphe.extension.shared.ResourceType;
import app.morphe.extension.shared.ResourceUtils;
import app.morphe.extension.shared.Utils;

/**
 * Opens a dialog showing the patches version, the official links and the people behind Morphe.
 */
@SuppressWarnings({"unused", "deprecation"})
public class MorpheAboutPreference extends Preference {

    static class WebLink {
        /**
         * Localized name replacements for links.
         */
        private static final Map<String, String> webLinkNameReplacements = new HashMap<>() {
            {
                // Handle no string resources available, and use the original untranslated tet.
                var websiteStringKey = "morphe_settings_about_links_website";
                if (ResourceUtils.getIdentifier(ResourceType.STRING, websiteStringKey) != 0) {
                    put("website", websiteStringKey);
                    put("donate", "morphe_settings_about_links_donate");
                    put("translations", "morphe_settings_about_links_translations");
                }
            }
        };

        final boolean preferred;
        /**
         * Whether the link asks for a donation, which leads the other links on a row of its own.
         */
        final boolean donation;
        final String name;
        final String url;

        WebLink(JSONObject json, boolean donation) throws JSONException {
            this(json.getBoolean("preferred"), donation, json.getString("name"), json.getString("url"));
        }

        WebLink(String name, String url) {
            this(true, false, name, url);
        }

        private WebLink(boolean preferred, boolean donation, String name, String url) {
            this.preferred = preferred;
            this.donation = donation;
            String localizedNameKey = webLinkNameReplacements.get(name.toLowerCase(Locale.US));
            this.name = (localizedNameKey != null) ? str(localizedNameKey) : name;
            this.url = url;
        }

        @NonNull
        @Override
        public String toString() {
            return "WebLink{" +
                    "preferred=" + preferred +
                    ", donation=" + donation +
                    ", name='" + name + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    public static void showVancedAsPastContributor(boolean includeVanced) {
        MorpheCreditsDialog.showVancedAsPastContributor = includeVanced;
    }

    // Dummy urls. Tapping one opens the list of people it stands for rather than a page.
    static final String CREDITS_LINK_PLACEHOLDER_URL = "https://morphe.software/credits/";
    static final String SUPPORTERS_LINK_PLACEHOLDER_URL = "https://morphe.software/supporters/";
    // Opens Morphe itself, to patch the app again.
    static final String MANAGER_LINK_PLACEHOLDER_URL = "https://morphe.software/manager/";

    private static String useNonBreakingHyphens(String text) {
        // Replace any dashes with non-breaking dashes, so the English text 'pre-release'
        // and the dev release number does not break and cover two lines.
        return text.replace("-", "&#8209;"); // #8209 = non-breaking hyphen.
    }

    private static void appendTile(StringBuilder html, String url, String title, String icon, boolean wide) {
        html.append("<a href=\"").append(AboutDialogStyle.escapeHtml(url))
                .append(wide ? "\" class=\"tile wide\">" : "\" class=\"tile\">")
                .append(icon)
                .append("<span class=\"tile-title\">").append(title).append("</span>")
                .append("</a>");
    }

    /**
     * Apps that do not support bundling resources must override this.
     *
     * @return A localized string to display for the key.
     */
    protected String getString(String key, Object... args) {
        return str(key, args);
    }

    private String createDialogHtml() {
        StringBuilder html = new StringBuilder(AboutDialogStyle.documentStart(getContext()))
                .append(AboutDialogStyle.pageStart(true));

        html.append("<div class=\"dialog-header\"><div class=\"about-header\">");

        html.append("<div class=\"app-logo\">").append(AboutDialogStyle.LOGO).append("</div>");

        html.append("<div><div class=\"app-name\">NexFusion</div><div class=\"app-component\">")
                .append("Modular Patches")
                .append("</div></div></div>");

        String appPatchesVersion = "5.0.1";
        try {
            Class<?> clazz = Class.forName("io.github.a092devs.nexfusion.BuildConfig");
            appPatchesVersion = (String) clazz.getField("VERSION_NAME").get(null);
        } catch (Throwable ignored) {
            try {
                Class<?> clazz = Class.forName("io.github.a092devs.nexfusion.patches.BuildConfig");
                appPatchesVersion = (String) clazz.getField("VERSION_NAME").get(null);
            } catch (Throwable ignored2) {
                appPatchesVersion = Utils.getPatchesReleaseVersion();
            }
        }
        if (appPatchesVersion == null || appPatchesVersion.isEmpty()) {
            appPatchesVersion = "5.0.1";
        }

        html.append("<div class=\"version-card\"><div class=\"version-label-row\"><span class=\"version-label\">")
                .append(getString("morphe_settings_about_version"))
                .append("</span>");
        html.append("</div><div class=\"version\">")
                .append(useNonBreakingHyphens(AboutDialogStyle.isolateLtr(appPatchesVersion)))
                .append("</div>");

        html.append("</div></div>"); // end .version-card and .dialog-header

        html.append("<div class=\"section\"><div class=\"group-label\">")
                .append(getString("morphe_settings_about_links_header"))
                .append("</div><div class=\"tiles\">");

        appendTile(html, "https://github.com/a092devs/NexFusion", "GitHub", AboutDialogStyle.linkIcon("https://github.com/a092devs/NexFusion"), false);
        appendTile(html, "https://github.com/a092devs", "Developer (@a092devs)", AboutDialogStyle.linkIcon("https://github.com/a092devs"), false);
        appendTile(html, "https://t.me/a092devs", "Telegram (@a092devs)", AboutDialogStyle.linkIcon("https://t.me/a092devs"), false);

        html.append("</div></div>")
                .append(AboutDialogStyle.pageEnd(getString("morphe_settings_about_close"), null))
                .append(AboutDialogStyle.DOCUMENT_END);

        return html.toString();
    }

    private String createDialogHtml(List<WebLink> aboutLinks, @Nullable String currentVersion) {
        return createDialogHtml();
    }

    {
        setOnPreferenceClickListener(pref -> {
            new AboutWebViewDialog(getContext(), createDialogHtml(), false).show();
            return false;
        });
    }



    public MorpheAboutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public MorpheAboutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public MorpheAboutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MorpheAboutPreference(Context context) {
        super(context);
    }
}
