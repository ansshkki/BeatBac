package com.beatbac.activity;


import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.beatbac.R;
import com.beatbac.fragment.BooksFragment;
import com.beatbac.fragment.ExamsFragment;
import com.beatbac.fragment.SummariesFragment;
import com.downloader.PRDownloader;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.oshi.libsearchtoolbar.SearchAnimationToolbar;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SearchAnimationToolbar.OnSearchQueryChangedListener {

    Fragment fragment = null;
    SearchAnimationToolbar toolbar;
    BottomNavigationView navView;
    //View container;
    SharedPreferences preferences;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_exams:
                if (!(fragment instanceof ExamsFragment)) {
                    fragment = new ExamsFragment();
                    toolbar.setTitle(getResources().getString(R.string.title_exams));
                    item.setIcon(R.drawable.ic_exams_selected);
                    navView.getMenu().getItem(1).setIcon(R.drawable.ic_books);
                    navView.getMenu().getItem(2).setIcon(R.drawable.ic_summaries);
                }
                break;
            case R.id.navigation_books:
                if (!(fragment instanceof BooksFragment)) {
                    fragment = new BooksFragment();
                    toolbar.setTitle(getResources().getString(R.string.title_books));
                    navView.getMenu().getItem(0).setIcon(R.drawable.ic_exams);
                    item.setIcon(R.drawable.ic_books_selected);
                    navView.getMenu().getItem(2).setIcon(R.drawable.ic_summaries);
                }
                break;
            case R.id.navigation_summaries:
                if (!(fragment instanceof SummariesFragment)) {
                    fragment = new SummariesFragment();
                    toolbar.setTitle(getResources().getString(R.string.title_summaries));
                    navView.getMenu().getItem(0).setIcon(R.drawable.ic_exams);
                    navView.getMenu().getItem(1).setIcon(R.drawable.ic_books);
                    item.setIcon(R.drawable.ic_summaries_selected);
                }
                break;
        }
        toolbar.getSearchToolbar().collapseActionView();
        return loadFragment(fragment);

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        if (itemId == R.id.action_search) {
            toolbar.onSearchIconClick();
            return true;
        } else if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (itemId == R.id.action_about) {
            startDialogAbout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(getString(R.string.preference_filename), MODE_PRIVATE);
        if (!preferences.getBoolean(getString(R.string.key_opened), false)) {
            firstLaunchScreen();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration configuration = getResources().getConfiguration();
            configuration.setLayoutDirection(new Locale("ar"));
            configuration.setLocale(new Locale("ar"));
            getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        }

        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setSupportActionBar(MainActivity.this);
        toolbar.setOnSearchQueryChangedListener(this);
        toolbar.getSearchToolbar().setCollapseIcon(R.drawable.ic_arrow_back);

        //loading the default fragment
        fragment = new ExamsFragment();
        loadFragment(fragment);
        toolbar.setTitle(getResources().getString(R.string.title_exams));
        navView.getMenu().getItem(0).setIcon(R.drawable.ic_exams_selected);

        PRDownloader.initialize(this);
        getPreferences(MODE_PRIVATE).edit().remove(getString(R.string.key_download_id)).apply();
        createNotificationChannel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, fragment instanceof BooksFragment ? new BooksFragment() : new ExamsFragment())
                .commit();
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim)
                    .replace(R.id.main_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {

        boolean handledByToolbar = toolbar.onBackPressed();

        if (!handledByToolbar) {
            super.onBackPressed();
        }
    }

    @Override
    public void onSearchExpanded() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
        }
        //container = findViewById(R.id.container);
        //container.animate().translationY(toolbar.getHeight()).setDuration(250).start();
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //    container.animate().translationZ(9).setDuration(250).start();
        //}
        //navView.animate().translationY(toolbar.getHeight()).setDuration(250).start();
    }

    @Override
    public void onSearchCollapsed() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));
            } else {
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.lollipop_status_bar));
            }
        }
        //container = findViewById(R.id.container);
        //container.animate().translationY(0).setDuration(250).start();
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //    container.animate().translationZ(0).setDuration(250).start();
        //}
        //navView.animate().translationY(0).setDuration(250).start();
        //Chip chip = findViewById(R.id.chip_all);
        //chip.setChecked(true);
    }

    @Override
    public void onSearchQueryChanged(String query) {
        SearchView searchView = findViewById(R.id.search);
        searchView.setQuery(query, false);
    }

    @Override
    public void onSearchSubmitted(String query) {
    }

    private void startDialogAbout() {
        Dialog dialog = new Dialog(this, R.style.AboutDialog);
        dialog.setContentView(R.layout.about_dialog);
        Toolbar toolbar = dialog.findViewById(R.id.about_toolbar);
        toolbar.setNavigationOnClickListener(v -> dialog.dismiss());
        View sendFeedback = dialog.findViewById(R.id.send_feedback);
        sendFeedback.setOnClickListener(v -> {
            Intent s = new Intent(Intent.ACTION_SENDTO);
            s.setData(Uri.parse("mailto:"));
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.beatbac_email)});
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_subject));
            String msg = "\n\n\n_________________\nSystem information:" + "\nManufacturer: " + Build.MANUFACTURER + "\nBrand: " + Build.BRAND
                    + "\nModel: " + Build.MODEL + "\nAndroid API: " + Build.VERSION.SDK_INT + "\nDevice: " + Build.DEVICE;
            intent.putExtra(Intent.EXTRA_TEXT, msg);
            intent.setSelector(s);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.about_not_found_email), Toast.LENGTH_SHORT).show();
            }
        });
        MaterialTextView emailAnas = dialog.findViewById(R.id.anas_email);
        emailAnas.setOnClickListener(v -> {
            Intent s = new Intent(Intent.ACTION_SENDTO);
            s.setData(Uri.parse("mailto:"));
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.anas_email)});
            intent.setSelector(s);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.about_not_found_email), Toast.LENGTH_SHORT).show();
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
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.tamim_email)});
            intent.setSelector(s);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.about_not_found_email), Toast.LENGTH_SHORT).show();
            }
        });
        MaterialTextView telegramTamim = dialog.findViewById(R.id.tamim_telegram);
        telegramTamim.setOnClickListener(v -> {
            Intent telegram = new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/tamimkabbani"));
            startActivity(telegram);
        });
        dialog.show();
    }

    private void firstLaunchScreen() {
        SharedPreferences.Editor editor = preferences.edit();

        Dialog dialog = new Dialog(this, R.style.DialogFullscreen);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_first_open);

        AutoCompleteTextView sectionSpinner = dialog.findViewById(R.id.spinner_section);
        ArrayAdapter<String> sectionAdapter =
                new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.spinner_section));
        sectionSpinner.setAdapter(sectionAdapter);

        AutoCompleteTextView religionSpinner = dialog.findViewById(R.id.spinner_religion);
        ArrayAdapter<String> religionAdapter =
                new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.spinner_religion));
        religionSpinner.setAdapter(religionAdapter);

        AutoCompleteTextView languageSpinner = dialog.findViewById(R.id.spinner_language);
        ArrayAdapter<String> languageAdapter =
                new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.spinner_language));
        languageSpinner.setAdapter(languageAdapter);

        MaterialButton button = dialog.findViewById(R.id.continue_settings);
        button.setOnClickListener(v -> {
            if (sectionSpinner.getText().toString().isEmpty() || religionSpinner.getText().toString().isEmpty()
                    || languageSpinner.getText().toString().isEmpty())
                Snackbar.make(dialog.findViewById(R.id.settings_container), R.string.empty_spinners_message, BaseTransientBottomBar.LENGTH_SHORT).show();
            else {
                editor.putString(getString(R.string.key_section_preference), sectionSpinner.getText().toString());
                editor.putString(getString(R.string.key_religion_preference), religionSpinner.getText().toString());
                editor.putString(getString(R.string.key_language_preference), languageSpinner.getText().toString());
                editor.putBoolean(getString(R.string.key_opened), true);
                editor.apply();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_container, new ExamsFragment())
                        .commit();
                new Handler().postDelayed(dialog::dismiss, 100);
            }
        });

        dialog.show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Download files";
            String description = "Progress bar that is shown when the app is downloading a file such as a book.";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("com.beatbac.channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration configuration = getResources().getConfiguration();
            configuration.setLayoutDirection(new Locale("ar"));
            configuration.setLocale(new Locale("ar"));
            getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        }
        getSupportFragmentManager()
                .beginTransaction()
                .detach(fragment)
                .attach(fragment)
                .commit();
    }
}
