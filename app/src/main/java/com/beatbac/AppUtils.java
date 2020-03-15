package com.beatbac;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.beatbac.activity.MainActivity;
import com.beatbac.activity.PDFViewActivity;
import com.beatbac.adapter.BookAdapter;
import com.beatbac.model.Book;
import com.beatbac.model.Exam;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

public class AppUtils {

    private final static int NOTIFICATION_ID = 1;

    @SuppressLint("SetTextI18n")
    public static int downloadBook(Activity context, @NonNull BookAdapter.ViewHolder holder, Book book) {
        holder.download.setVisibility(View.GONE);
        holder.downloadCancel.setVisibility(View.VISIBLE);
        holder.downloadPercent.setVisibility(View.VISIBLE);
        holder.downloadPercent.setText(R.string.downloading);
        holder.downloadProgressBar.setVisibility(View.VISIBLE);
        holder.downloadProgressBar.setIndeterminate(true);

        SharedPreferences preferences = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.channel_id));
        builder.setContentTitle(context.getString(R.string.downloading))
                .setContentText(book.getName())
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        builder.setProgress(0, 0, true);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        return PRDownloader.download(book.getUrl(), getRootDirPath(context) + context.getString(R.string.books_folder), book.getFile() + ".pdf")
                .build()
                .setOnStartOrResumeListener(() -> holder.downloadProgressBar.setIndeterminate(false))
                .setOnProgressListener(progress -> {
                    long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                    holder.downloadPercent.setText((int) progressPercent + "%");
                    holder.downloadProgressBar.setProgress((int) progressPercent);
                    builder.setContentTitle(book.getName())
                            .setContentText(getProgressDisplayLine(progress.currentBytes, progress.totalBytes))
                            .setSubText((int) progressPercent + "%");
                    builder.setProgress(100, (int) progressPercent, false);
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                })
                .setOnCancelListener(() -> {
                    finishDownload(holder);
                    builder.setContentTitle(context.getString(R.string.download_canceled))
                            .setContentText(book.getName())
                            .setSubText(null)
                            .setProgress(0, 0, false)
                            .setSmallIcon(R.drawable.ic_cancel)
                            .setAutoCancel(true)
                            .setOngoing(false);
                    new Handler().postDelayed(() -> notificationManager.notify(NOTIFICATION_ID, builder.build()), 1000);
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        startPDF(context, context.getString(R.string.books_folder), book.getName(), book.getFile());
                        finishDownload(holder);
                        holder.download.setText(R.string.open);
                        holder.download.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_open));
                        builder.setContentTitle(context.getString(R.string.download_completed))
                                .setContentText(book.getName())
                                .setSubText(null)
                                .setSmallIcon(R.drawable.ic_done)
                                .setProgress(0, 0, false)
                                .setAutoCancel(true)
                                .setOngoing(false);
                        editor.remove(context.getString(R.string.key_download_id));
                        editor.remove(context.getString(R.string.key_book_name));
                        editor.apply();
                        new Handler().postDelayed(() -> notificationManager.notify(NOTIFICATION_ID, builder.build()), 1000);
                    }

                    @Override
                    public void onError(Error error) {
                        PRDownloader.cancelAll();
                        if (error.isConnectionError())
                            showSnackbar(R.string.snackbar_connection_error_message, context);
                        else if (error.isServerError())
                            showSnackbar(R.string.snackbar_server_error_message, context);
                        else
                            showSnackbar(R.string.error_message, context);

                        finishDownload(holder);

                        builder.setContentTitle(context.getString(R.string.download_error))
                                .setContentText(book.getName())
                                .setSubText(null)
                                .setProgress(0, 0, false)
                                .setSmallIcon(R.drawable.ic_error)
                                .setAutoCancel(true)
                                .setOngoing(false);
                        editor.remove(context.getString(R.string.key_download_id));
                        editor.remove(context.getString(R.string.key_book_name));
                        editor.apply();
                        new Handler().postDelayed(() -> notificationManager.notify(NOTIFICATION_ID, builder.build()), 1000);
                    }
                });
    }

    public static void showDialog(Activity context, Exam exam, @SessionType int session) {
        new AlertDialog.Builder(context, R.style.AppDialog)
                .setTitle(context.getString(R.string.years_title))
                .setSingleChoiceItems(getExistYears(exam, session, context), -1, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    if (AppUtils.isFileExist(context, context.getString(R.string.exams_folder), exam.getFile(i + session * 8)))
                        AppUtils.startPDF(context, context.getString(R.string.exams_folder), exam.getName(), exam.getFile(i + session * 8));
                    else {
                        if (AppUtils.isConnected(context))
                            AppUtils.downloadExam(context, exam, i + session * 8);
                        else
                            AppUtils.showSnackbar(R.string.snackbar_no_connection_error_message, context);
                    }
                })
                .setNeutralButton(context.getString(R.string.cancel), null)
                .show();
    }

    public static void downloadExam(Activity context, Exam exam, int i) {
        if (exam.getUrl(i) == null) {
            showSnackbar(R.string.snackbar_file_not_exist, context);
            return;
        }

        ProgressDialog dialog = new ProgressDialog(context, R.style.AppDialog);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.downloading);
        dialog.setMax(100);
        dialog.setIndeterminate(true);
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, context.getResources().getText(R.string.cancel), (dialogInterface, i1) -> {
            PRDownloader.cancelAll();
            dialogInterface.dismiss();
        });
        dialog.show();

        PRDownloader.download(exam.getUrl(i), getRootDirPath(context) + context.getString(R.string.exams_folder), exam.getFile(i) + ".pdf")
                .build()
                .setOnProgressListener(progress -> {
                    long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                    dialog.setIndeterminate(false);
                    dialog.setProgress((int) progressPercent);
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        startPDF(context, context.getString(R.string.exams_folder), exam.getName(), exam.getFile(i));
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(Error error) {
                        dialog.dismiss();
                        if (error.isConnectionError())
                            showSnackbar(R.string.snackbar_connection_error_message, context);
                        else if (error.isServerError())
                            showSnackbar(R.string.snackbar_server_error_message, context);
                        else
                            showSnackbar(R.string.error_message, context);
                    }
                });
    }

    public static void finishDownload(@NonNull BookAdapter.ViewHolder holder) {
        holder.download.setVisibility(View.VISIBLE);
        holder.downloadCancel.setVisibility(View.GONE);
        holder.downloadPercent.setVisibility(View.GONE);
        holder.downloadPercent.setText("");
        holder.downloadProgressBar.setVisibility(View.GONE);
        holder.downloadProgressBar.setIndeterminate(false);
    }

    public static void startPDF(Context context, String folder, String name, String file) {
        Intent intent = new Intent(context, PDFViewActivity.class);
        intent.putExtra(context.getString(R.string.key_folder), folder);
        intent.putExtra(context.getString(R.string.key_name), name);
        intent.putExtra(context.getString(R.string.key_file), file);
        context.startActivity(intent);
    }

    public static boolean isFileExist(Activity context, String folder, String file) {
        return new File(getRootDirPath(context) + folder + "/" + file + ".pdf").exists();
    }

    private static String[] getExistYears(Exam exam, @SessionType int session, Activity context) {
        String[] years = context.getResources().getStringArray(R.array.years);
        for (int i = 0; i < years.length; i++) {
            if (exam.getUrl(i + session * 8) != null) {
                if (!isFileExist(context, context.getString(R.string.exams_folder), exam.getFile(i + session * 8)))
                    years[i] += "  \uD83D\uDCE5";
            } else {
                years[i] += "  ✘";
            }
        }
        return years;
    }

    public static void showSnackbar(@StringRes int message, Activity context) {
        final Snackbar snackbar = Snackbar.make(context.findViewById(R.id.main_container), message, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.snackbar_ok, view -> snackbar.dismiss());
        if (context instanceof MainActivity) {
            snackbar.setAnchorView(R.id.nav_view);
        }
        snackbar.show();
    }

    public static int getScreenWidthDp(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) (displayMetrics.widthPixels / displayMetrics.density);
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static String getRootDirPath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = ContextCompat.getExternalFilesDirs(context.getApplicationContext(),
                    null)[0];
            return file.getAbsolutePath() + "/";
        } else {
            return context.getApplicationContext().getFilesDir().getAbsolutePath() + "/";
        }
    }

    private static String getProgressDisplayLine(long currentBytes, long totalBytes) {
        return getBytesToMBString(currentBytes) + " / " + getBytesToMBString(totalBytes);
    }

    private static String getBytesToMBString(long bytes) {
        return String.format(Locale.ENGLISH, "%.2f MB", bytes / (1024.00 * 1024.00));
    }

    @IntDef(value = {Data.EXAM, Data.SOLUTION})
    @Retention(RetentionPolicy.SOURCE)
    private @interface SessionType {
    }

    @IntDef(value = {Data.ALL, Data.SCIENTIFIC, Data.LITERARY})
    @Retention(RetentionPolicy.SOURCE)
    @interface Section {
    }

}