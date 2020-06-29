package com.sharebounds.sharebounds.main;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.sharebounds.sharebounds.AppSettings;
import com.sharebounds.sharebounds.R;

class ShareUtils {

    private static final String MARKET_URI = "market://details?id=";
    private static final String PLAY_URI = "https://play.google.com/store/apps/details?id=";

    static void startShareIntent(final Context context, String text) {
        startShareIntent(context, text, false);
    }

    static void startShareIntent(final Context context, String text, boolean settings) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        if (!settings) {
            text = context.getString(R.string.share_app_name) + text;
        }

        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_title)));
    }

    static void startRateIntent(final Context context) {
        String packageName = context.getPackageName();

        Uri uri = Uri.parse(MARKET_URI + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        if (Build.VERSION.SDK_INT >= 21)
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        else goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(PLAY_URI + packageName)));
        }
    }

    static void showRateDialog(final Context context) {
        int theme = AppSettings.getInstance(context.getApplicationContext()).getTheme();
        final int textColor;
        switch (theme) {
            case 1:
                textColor = context.getResources().getColor(R.color.colorDarkAccent);
                break;
            default:
                textColor = context.getResources().getColor(R.color.colorAccent);
        }

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.rate_title))
                .setMessage(context.getString(R.string.rate_body))
                .setPositiveButton(context.getString(R.string.rate_button_pos),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                       startRateIntent(context);
                    }
                })
                .setNeutralButton(context.getString(R.string.rate_button_later), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AppSettings.getInstance(context.getApplicationContext()).newRateDialogCount(1);
                    }
                })
                .setNegativeButton(context.getString(R.string.rate_button_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AppSettings.getInstance(context.getApplicationContext()).newRateDialogCount(-1);
                    }
                }).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(textColor);
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(textColor);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(textColor);
            }
        });
        dialog.show();
    }
}
