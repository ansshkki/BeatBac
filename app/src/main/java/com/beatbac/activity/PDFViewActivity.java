package com.beatbac.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.res.ResourcesCompat;

import com.beatbac.AppUtils;
import com.beatbac.Data;
import com.beatbac.R;
import com.beatbac.model.Exam;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;
import java.util.Locale;

public class PDFViewActivity extends AppCompatActivity {

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                mHideHandler.removeCallbacks(mHideRunnable);
                mHideHandler.postDelayed(mHideRunnable, AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private PDFView pdfView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @Override
        public void run() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                pdfView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            } else {
                pdfView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }
    };
    private String title;
    private String folder;
    private String file;
    private Exam exam;
    private ProgressBar progressBar;
    private Toolbar mToolbar;
    private View mControlsView;
    private final Runnable mShowPart2Runnable = () -> {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            showActionBar();
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = this::hide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfview);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration configuration = getResources().getConfiguration();
            configuration.setLayoutDirection(new Locale("ar"));
            configuration.setLocale(new Locale("ar"));
            getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        }

        if (getIntent().getExtras() != null) {
            title = getIntent().getExtras().getString(getString(R.string.key_name));
            folder = getIntent().getExtras().getString(getString(R.string.key_folder));
            file = getIntent().getExtras().getString(getString(R.string.key_file));
        }
        if (file != null)
            exam = Data.getPdfExams(file, getSharedPreferences(
                    getString(R.string.preference_filename), MODE_PRIVATE)
                    .getString(getString(R.string.key_section_preference), getString(R.string.scientific))
                    .equals(getString(R.string.scientific)) ? Data.SCIENTIFIC : Data.LITERARY);

        mToolbar = findViewById(R.id.pdf_toolbar);
        mToolbar.setTitle("\u200e" + title);
        setSupportActionBar(mToolbar);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preference_filename), MODE_PRIVATE);
        if (!preferences.getBoolean(getString(R.string.key_opened_pdf), false)) {
            if (Data.getIndex(file) != -1) {
                mToolbar.inflateMenu(R.menu.pdf_view_menu);
                //noinspection ConstantConditions
                TapTargetView.showFor(this, TapTarget.forToolbarMenuItem(mToolbar, R.id.action_solution, "سلم التصحيح", "للحصول على سلم هذه الدورة")
                                .outerCircleColor(R.color.colorPrimary)
                                .targetCircleColor(R.color.colorAccent)
                                .transparentTarget(false)
                                .textTypeface(ResourcesCompat.getFont(this, R.font.cairo))
                                .textColor(android.R.color.white));
                preferences.edit().putBoolean(getString(R.string.key_opened_pdf), true).apply();
            }
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        pdfView = findViewById(R.id.fullscreen_content);
        progressBar = findViewById(R.id.pdf_progress_bar);
        final TextView error = findViewById(R.id.error_loading);

        pdfView.setMinZoom(0.5f);
        pdfView.setMidZoom(1f);
        pdfView.setMaxZoom(2f);

        pdfView.fromFile(new File(AppUtils.getRootDirPath(this) + "/" + folder + "/" + file + ".pdf"))
                .onLoad(nbPages -> progressBar.setVisibility(View.GONE))
                .onError(t -> {
                    error.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Log.e("PDFViewActivity", "Error: " + t.getMessage());
                })
                .spacing(16)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();

        pdfView.setOnClickListener(view -> toggle());

        findViewById(R.id.pdf_toolbar).setOnTouchListener(mDelayHideTouchListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pdf_view_menu, menu);
        if (Data.getIndex(file) == -1)
            menu.getItem(1).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_exams:
                AppUtils.showDialog(this, exam, Data.EXAM);
                return true;
            case R.id.action_solution:
                String solutionFile = Data.getSolution(file,
                        Data.getIndex(file),
                        getSharedPreferences(getString(R.string.preference_filename), MODE_PRIVATE)
                                .getString(getString(R.string.key_section_preference), getString(R.string.scientific))
                                .equals(getString(R.string.scientific)) ? Data.SCIENTIFIC : Data.LITERARY);
                if (AppUtils.isFileExist(this, getString(R.string.exams_folder), solutionFile))
                    AppUtils.startPDF(this, getString(R.string.exams_folder), title,
                            Data.getSolution(file,
                                    Data.getIndex(file),
                                    getSharedPreferences(getString(R.string.preference_filename), MODE_PRIVATE)
                                            .getString(getString(R.string.key_section_preference), getString(R.string.scientific))
                                            .equals(getString(R.string.scientific)) ? Data.SCIENTIFIC : Data.LITERARY));
                else if (AppUtils.isConnected(this))
                    AppUtils.downloadExam(this, exam, Data.getIndex(file));
                else
                    AppUtils.showSnackbar(R.string.snackbar_no_connection_error_message, this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Data.getIndex(file) != -1)
            NavUtils.navigateUpFromSameTask(this);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            hideActionBar();
        }
        mVisible = false;

        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        pdfView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    protected void hideActionBar() {
        final ActionBar ab = getSupportActionBar();
        if (ab != null && ab.isShowing()) {
            if (mToolbar != null) {
                mToolbar.animate().translationY(-112).setDuration(150)
                        .withEndAction(ab::hide).start();
            } else {
                ab.hide();
            }
        }
        mControlsView.animate().translationY(-112).setDuration(150)
                .withEndAction(() -> mControlsView.setVisibility(View.GONE)).start();
    }

    protected void showActionBar() {
        ActionBar ab = getSupportActionBar();
        if (ab != null && !ab.isShowing()) {
            ab.show();
            if (mToolbar != null) {
                mToolbar.animate().translationY(0).setDuration(150).start();
            }
        }
        mControlsView.setVisibility(View.VISIBLE);
        mControlsView.animate().translationY(0).setDuration(150).start();
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
    }
}
