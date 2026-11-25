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

import android.content.Context;
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

import app.morphe.extension.shared.ResourceType;
import app.morphe.extension.shared.ResourceUtils;
import app.morphe.extension.shared.Utils;

/**
 * Opens a dialog showing the patches version and official NexFusion links.
 */
@SuppressWarnings({"unused", "deprecation"})
public class MorpheAboutPreference extends Preference {

    static class WebLink {
        private static final Map<String, String> webLinkNameReplacements = new HashMap<>() {
            {
                var websiteStringKey = "morphe_settings_about_links_website";
                if (ResourceUtils.getIdentifier(ResourceType.STRING, websiteStringKey) != 0) {
                    put("website", websiteStringKey);
                    put("donate", "morphe_settings_about_links_donate");
                    put("translations", "morphe_settings_about_links_translations");
                }
            }
        };

        final boolean preferred;
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

    static final String CREDITS_LINK_PLACEHOLDER_URL = "https://morphe.software/credits/";
    static final String SUPPORTERS_LINK_PLACEHOLDER_URL = "https://morphe.software/supporters/";
    static final String MANAGER_LINK_PLACEHOLDER_URL = "https://morphe.software/manager/";

    private static String useNonBreakingHyphens(String text) {
        return text.replace("-", "&#8209;");
    }

    private static void appendTile(StringBuilder html, String url, String title, String icon, boolean wide) {
        html.append("<a href=\"").append(AboutDialogStyle.escapeHtml(url))
                .append(wide ? "\" class=\"tile wide\">" : "\" class=\"tile\">")
                .append(icon)
                .append("<span class=\"tile-title\">").append(title).append("</span>")
                .append("</a>");
    }

    protected String getString(String key, Object... args) {
        return str(key, args);
    }

    private static String getAppPatchesVersion() {
        for (String className : new String[]{
                "io.github.a092devs.nexfusion.patches.BuildConfig",
                "io.github.a092devs.nexfusion.BuildConfig",
                "io.github.nexfusion.patches.BuildConfig",
                "io.github.nexfusion.BuildConfig"
        }) {
            try {
                Class<?> clazz = Class.forName(className, false, MorpheAboutPreference.class.getClassLoader());
                Object version = clazz.getField("VERSION_NAME").get(null);
                if (version instanceof String s && !s.isEmpty()) {
                    return s;
                }
            } catch (Throwable ignored) {
            }
        }

        String releaseVer = Utils.getPatchesReleaseVersion();
        if (releaseVer != null && !releaseVer.isEmpty()) {
            return releaseVer;
        }

        return "Unknown";
    }

    private String createDialogHtml() {
        StringBuilder html = new StringBuilder(AboutDialogStyle.documentStart(getContext()))
                .append(AboutDialogStyle.pageStart(true));

        html.append("<div class=\"dialog-header\"><div class=\"about-header\">");

        html.append("<div class=\"app-logo\">").append(AboutDialogStyle.LOGO).append("</div>");

        html.append("<div><div class=\"app-name\">NexFusion</div><div class=\"app-component\">")
                .append("Modular Patches")
                .append("</div></div></div>");

        String appPatchesVersion = getAppPatchesVersion();

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
        appendTile(html, "https://t.me/nexfusion", "Telegram Channel", AboutDialogStyle.linkIcon("https://t.me/nexfusion"), false);

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
