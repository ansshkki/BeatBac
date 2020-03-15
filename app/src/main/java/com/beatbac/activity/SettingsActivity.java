package com.beatbac.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.beatbac.R;
import com.google.android.material.textview.MaterialTextView;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SettingsTheme);
        setContentView(R.layout.activity_settings);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration configuration = getResources().getConfiguration();
            configuration.setLayoutDirection(new Locale("ar"));
            configuration.setLocale(new Locale("ar"));
            getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference aboutApp = findPreference(getString(R.string.key_about_app));
            if (aboutApp != null && getContext() != null) {
                aboutApp.setOnPreferenceClickListener(preference -> {
                    Dialog dialog = new Dialog(getContext(), R.style.AboutDialog);
                    dialog.setContentView(R.layout.about_dialog);
                    Toolbar toolbar = dialog.findViewById(R.id.about_toolbar);
                    toolbar.setNavigationOnClickListener(v -> dialog.dismiss());
                    View sendFeedback = dialog.findViewById(R.id.send_feedback);
                    sendFeedback.setOnClickListener(v -> {
                        Intent s = new Intent(Intent.ACTION_SENDTO);
                        s.setData(Uri.parse("mailto:"));
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getContext().getString(R.string.beatbac_email)});
                        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_subject));
                        String msg = "\n\n\n_________________\nSystem information:" + "\nManufacturer: " + Build.MANUFACTURER + "\nBrand: " + Build.BRAND
                                + "\nModel: " + Build.MODEL + "\nAndroid API: " + Build.VERSION.SDK_INT + "\nDevice: " + Build.DEVICE;
                        intent.putExtra(Intent.EXTRA_TEXT, msg);
                        intent.setSelector(s);
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), getString(R.string.about_not_found_email), Toast.LENGTH_SHORT).show();
                        }
                    });
                    MaterialTextView emailAnas = dialog.findViewById(R.id.anas_email);
                    emailAnas.setOnClickListener(v -> {
                        Intent s = new Intent(Intent.ACTION_SENDTO);
                        s.setData(Uri.parse("mailto:"));
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getContext().getString(R.string.anas_email)});
                        intent.setSelector(s);
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), getString(R.string.about_not_found_email), Toast.LENGTH_SHORT).show();
                        }
                    });
                    MaterialTextView telegramAnas = dialog.findViewById(R.id.anas_telegram);
                    telegramAnas.setOnClickListener(v -> {
                        Intent telegram = new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/ansshkki"));
                        startActivity(telegram);
                    });
                    MaterialTextView emailTamim = dialog.findViewById(R.id.tamim_email);
                    emailTamim.setOnClickListener(v -> {
                        Intent s = new Intent(Intent.ACTION_SENDTO);
                        s.setData(Uri.parse("mailto:"));
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getContext().getString(R.string.tamim_email)});
                        intent.setSelector(s);
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), getString(R.string.about_not_found_email), Toast.LENGTH_SHORT).show();
                        }
                    });
                    MaterialTextView telegramTamim = dialog.findViewById(R.id.tamim_telegram);
                    telegramTamim.setOnClickListener(v -> {
                        Intent telegram = new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/tamimkabbani"));
                        startActivity(telegram);
                    });
                    dialog.show();
                    return true;
                });

            }
        }
    }
}