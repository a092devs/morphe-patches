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
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
import app.morphe.extension.shared.ui.CustomDialog;

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

    public static Drawable getNexFusionIcon(Context context) {
        try {
            int size = (int) (48 * context.getResources().getDisplayMetrics().density);
            if (size <= 0) size = 96;
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            float scale = size / 108f;
            canvas.scale(scale, scale);

            Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeCap(Paint.Cap.ROUND);
            strokePaint.setStrokeJoin(Paint.Join.ROUND);

            // 1. Outer Chamfered Silicon Die Bevel Shadow (#1E293B)
            fillPaint.setColor(0xFF1E293B);
            strokePaint.setColor(0xFF334155);
            strokePaint.setStrokeWidth(1.5f);
            Path dieShadow = new Path();
            dieShadow.moveTo(54, 18);
            dieShadow.lineTo(84, 35);
            dieShadow.lineTo(84, 73);
            dieShadow.lineTo(54, 90);
            dieShadow.lineTo(24, 73);
            dieShadow.lineTo(24, 35);
            dieShadow.close();
            canvas.drawPath(dieShadow, fillPaint);
            canvas.drawPath(dieShadow, strokePaint);

            // 2. Outer Die Front Bevel Plate (#334155)
            fillPaint.setColor(0xFF334155);
            Path diePlate = new Path();
            diePlate.moveTo(54, 19);
            diePlate.lineTo(83, 35);
            diePlate.lineTo(83, 73);
            diePlate.lineTo(54, 89);
            diePlate.lineTo(25, 73);
            diePlate.lineTo(25, 35);
            diePlate.close();
            canvas.drawPath(diePlate, fillPaint);

            // 3. Recessed Silicon Die Cavity (#090D18)
            fillPaint.setColor(0xFF090D18);
            Path cavity = new Path();
            cavity.moveTo(54, 23);
            cavity.lineTo(79, 37);
            cavity.lineTo(79, 71);
            cavity.lineTo(54, 85);
            cavity.lineTo(29, 71);
            cavity.lineTo(29, 37);
            cavity.close();
            canvas.drawPath(cavity, fillPaint);

            // 4. Cavity Inner Edge Accent Ring (#1E293B)
            strokePaint.setColor(0xFF1E293B);
            strokePaint.setStrokeWidth(1.2f);
            Path innerRing = new Path();
            innerRing.moveTo(54, 25);
            innerRing.lineTo(77, 38);
            innerRing.lineTo(77, 70);
            innerRing.lineTo(54, 83);
            innerRing.lineTo(31, 70);
            innerRing.lineTo(31, 38);
            innerRing.close();
            canvas.drawPath(innerRing, strokePaint);

            // 5. Vertical Alignment Guides (#0284C7)
            strokePaint.setColor(0xFF0284C7);
            strokePaint.setStrokeWidth(1.6f);
            canvas.drawLine(54, 24, 54, 30, strokePaint);
            canvas.drawLine(54, 78, 54, 84, strokePaint);

            // 6. Monogram N - Left Pillar Bevel (#1E40AF)
            fillPaint.setColor(0xFF1E40AF);
            canvas.drawRect(37, 38, 41, 70, fillPaint);

            // 7. Monogram N - Left Pillar Main (#0284C7)
            fillPaint.setColor(0xFF0284C7);
            canvas.drawRect(41, 38, 47, 70, fillPaint);

            // 8. Monogram N - Diagonal Core Bridge (#06B6D4 and #38BDF8)
            fillPaint.setColor(0xFF06B6D4);
            Path diag1 = new Path();
            diag1.moveTo(47, 38);
            diag1.lineTo(61, 59);
            diag1.lineTo(61, 49);
            diag1.lineTo(47, 38);
            diag1.close();
            canvas.drawPath(diag1, fillPaint);

            fillPaint.setColor(0xFF38BDF8);
            Path diag2 = new Path();
            diag2.moveTo(47, 49);
            diag2.lineTo(61, 70);
            diag2.lineTo(61, 59);
            diag2.lineTo(47, 38);
            diag2.close();
            canvas.drawPath(diag2, fillPaint);

            // Specular Ridge On Diagonal (#BAE6FD)
            strokePaint.setColor(0xFFBAE6FD);
            strokePaint.setStrokeWidth(1.2f);
            canvas.drawLine(47, 38, 61, 70, strokePaint);

            // 9. Monogram N - Right Pillar Main (#0284C7)
            fillPaint.setColor(0xFF0284C7);
            canvas.drawRect(61, 38, 67, 70, fillPaint);

            // 10. Monogram N - Right Pillar Bevel (#2563EB)
            fillPaint.setColor(0xFF2563EB);
            canvas.drawRect(67, 38, 71, 70, fillPaint);

            // 11. Contact Pads (#FFFFFF and #38BDF8)
            fillPaint.setColor(0xFFFFFFFF);
            canvas.drawRect(35, 36, 39, 40, fillPaint);
            canvas.drawRect(69, 68, 73, 72, fillPaint);
            fillPaint.setColor(0xFF38BDF8);
            canvas.drawRect(69, 36, 73, 40, fillPaint);
            canvas.drawRect(35, 68, 39, 72, fillPaint);

            // 12. Link Pins (#38BDF8)
            strokePaint.setColor(0xFF38BDF8);
            strokePaint.setStrokeWidth(1.2f);
            canvas.drawLine(31, 54, 35, 54, strokePaint);
            canvas.drawLine(73, 54, 77, 54, strokePaint);

            return new BitmapDrawable(context.getResources(), bitmap);
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        Drawable icon = getNexFusionIcon(getContext());
        if (icon != null) {
            setIcon(icon);
        }
    }

    private void showAboutPopup(Context context) {
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

        String msg = "Modular Patches\nVersion: " + appPatchesVersion + "\nDeveloper: @a092devs";

        Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                context,
                "NexFusion",
                msg,
                null,
                "Close",
                () -> {},
                null,
                "GitHub",
                () -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/a092devs/NexFusion"));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (Throwable ignored) {}
                },
                false,
                true
        );

        Dialog dialog = dialogPair.first;
        LinearLayout mainLayout = dialogPair.second;

        // Add NexFusion Icon above title
        Drawable icon = getNexFusionIcon(context);
        if (icon != null) {
            ImageView iconView = new ImageView(context);
            iconView.setImageDrawable(icon);
            int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, context.getResources().getDisplayMetrics());
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(size, size);
            iconParams.gravity = Gravity.CENTER_HORIZONTAL;
            iconParams.setMargins(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, context.getResources().getDisplayMetrics()));
            iconView.setLayoutParams(iconParams);
            mainLayout.addView(iconView, 0);
        }

        // Add Telegram button in buttonContainer alongside GitHub
        if (mainLayout.getChildCount() > 0) {
            View lastView = mainLayout.getChildAt(mainLayout.getChildCount() - 1);
            if (lastView instanceof LinearLayout) {
                LinearLayout buttonContainer = (LinearLayout) lastView;
                Button telegramButton = CustomDialog.createButton(context, dialog, "Telegram", () -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/a092devs"));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (Throwable ignored) {}
                }, false, false);
                buttonContainer.addView(telegramButton, 0);
            }
        }

        dialog.show();
    }

    {
        setOnPreferenceClickListener(pref -> {
            showAboutPopup(pref.getContext());
            return true;
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
