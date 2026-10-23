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

import io.github.nexalloy.BuildConfig;

/**
 * Opens a dialog showing official links for NexAlloyNext.
 */
@SuppressWarnings({"unused", "deprecation"})
public class MorpheAboutPreference extends Preference {

    static class WebLink {
        final boolean preferred;
        final String name;
        @Nullable
        final String subText;
        final String url;

        WebLink(JSONObject json) throws JSONException {
            this(json.optBoolean("preferred", false),
                    json.getString("name"),
                    null,
                    json.getString("url")
            );
        }

        WebLink(String name, @Nullable String subText, String url) {
            this(false, name, subText, url);
        }

        WebLink(boolean preferred, String name, @Nullable String subText, String url) {
            this.preferred = preferred;
            this.name = name;
            this.subText = subText;
            this.url = url;
        }

        @NonNull
        @Override
        public String toString() {
            return "WebLink{" +
                    "name='" + name + '\'' +
                    ", subText='" + subText + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    public static void showVancedAsPastContributor(boolean includeVanced) {
    }

    // Dummy url
    static final String CREDITS_LINK_PLACEHOLDER_URL = "https://github.com/a092devs/NexAlloyNext";

    static final WebLink CREDITS_LINK = new WebLink("GitHub", "NexAlloyNext", CREDITS_LINK_PLACEHOLDER_URL);

    private static String useNonBreakingHyphens(String text) {
        return text.replace("-", "&#8209;");
    }

    protected String getString(String key, Object... args) {
        return str(key, args);
    }

    private String createDialogHtml() {
        StringBuilder html = new StringBuilder(AboutDialogStyle.documentStart());

        // Header section.
        html.append("<div class=\"dialog-header\">");
        html.append("<div class=\"app-name\">NexAlloyNext</div>");
        html.append(String.format("""
                    <div class="info-card">
                        <p>Version <i>%s</i></p>
                    </div>
                    """,
                useNonBreakingHyphens(BuildConfig.VERSION_NAME)
        ));
        html.append("</div>"); // end .dialog-header

        // Links section.
        html.append("<div class=\"section\">")
                .append(AboutDialogStyle.sectionTitle(getString("morphe_settings_about_links_header")))
                .append("<div class=\"settings-group\">");

        var myLinks = new WebLink[]{
                new WebLink("GitHub", "NexAlloyNext", "https://github.com/a092devs/NexAlloyNext"),
                new WebLink("Developer", "@a092devs", "https://github.com/a092devs"),
                new WebLink("Telegram", "@a092devs", "https://t.me/a092devs"),
        };

        for (WebLink link : myLinks) {
            html.append("<a href=\"").append(link.url).append("\" class=\"settings-item\">")
                    .append("<span class=\"item-icon\">")
                    .append(AboutDialogStyle.linkIcon(link.url))
                    .append("</span>")
                    .append("<div class=\"item-text\"><div class=\"item-title\">")
                    .append(link.name)
                    .append("</div>");
            if (link.subText != null) {
                html.append("<div class=\"item-subtitle\">")
                        .append(link.subText)
                        .append("</div>");
            }
            html.append("</div>")
                    .append(AboutDialogStyle.chevron())
                    .append("</a>");
        }

        html.append("</div>"); // end .settings-group
        html.append("</div>"); // end .section

        html.append(AboutDialogStyle.DOCUMENT_END);

        return html.toString();
    }

    {
        setOnPreferenceClickListener(pref -> {
            new AboutWebViewDialog(pref.getContext(), createDialogHtml()).show();
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
