package com.beatbac.adapter;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.beatbac.AppUtils;
import com.beatbac.R;
import com.beatbac.model.Book;
import com.downloader.PRDownloader;
import com.downloader.Status;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> implements Filterable {

    private Activity context;
    private List<Book> bookArrayList;
    private List<Book> orig;

    public BookAdapter(Activity context, ArrayList<Book> books) {
        this.context = context;
        this.bookArrayList = books;
        this.orig = new ArrayList<>(books);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.book_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Book book = bookArrayList.get(position);
        holder.setDetails(book);

        SharedPreferences preferences = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (holder.downloadPercent.getText().toString().equals("")
                && PRDownloader.getStatus(preferences.getInt(context.getString(R.string.key_download_id), 0)).equals(Status.RUNNING)
                && preferences.getString(context.getString(R.string.key_book_name), "").equals(book.getFile())) {
            holder.downloadPercent.setText(R.string.downloading_notification);
            holder.downloadProgressBar.setIndeterminate(true);
        }

        if (PRDownloader.getStatus(preferences.getInt(context.getString(R.string.key_download_id), 0)).equals(Status.RUNNING)
                && preferences.getString(context.getString(R.string.key_book_name), "").equals(book.getFile())) {
            holder.download.setVisibility(View.GONE);
            holder.downloadCancel.setVisibility(View.VISIBLE);
            holder.downloadPercent.setVisibility(View.VISIBLE);
            holder.downloadProgressBar.setVisibility(View.VISIBLE);
        } else {
            holder.download.setVisibility(View.VISIBLE);
            holder.downloadCancel.setVisibility(View.GONE);
            holder.downloadPercent.setVisibility(View.GONE);
            holder.downloadProgressBar.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(view -> {
            if (AppUtils.isFileExist(context, context.getString(R.string.books_folder), book.getFile()))
                AppUtils.startPDF(context, context.getString(R.string.books_folder), book.getName(), book.getFile());
        });
        holder.download.setOnClickListener(view -> {
            if (AppUtils.isFileExist(context, context.getString(R.string.books_folder), book.getFile()))
                AppUtils.startPDF(context, context.getString(R.string.books_folder), book.getName(), book.getFile());
            else {
                if (AppUtils.isConnected(context)) {
                    if (preferences.getInt(context.getString(R.string.key_download_id), 0) == 0) {
                        editor.putInt(context.getString(R.string.key_download_id), AppUtils.downloadBook(context, holder, book));
                        editor.putString(context.getString(R.string.key_book_name), book.getFile());
                        editor.apply();
                    } else
                        AppUtils.showSnackbar(R.string.snackbar_another_is_downloading, context);
                } else
                    AppUtils.showSnackbar(R.string.snackbar_no_connection_error_message, context);
            }
        });
        holder.downloadCancel.setOnClickListener(view -> {
            PRDownloader.cancel(preferences.getInt(context.getString(R.string.key_download_id), 0));
            AppUtils.finishDownload(holder);
            editor.remove(context.getString(R.string.key_download_id));
            editor.remove(context.getString(R.string.key_book_name));
            editor.apply();
        });
        holder.itemView.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ObjectAnimator downAnimator = ObjectAnimator.ofFloat(view, "translationZ", 16);
                    downAnimator.setDuration(200);
                    downAnimator.setInterpolator(new DecelerateInterpolator());
                    downAnimator.start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    ObjectAnimator upAnimator = ObjectAnimator.ofFloat(view, "translationZ", 0);
                    upAnimator.setDuration(200);
                    upAnimator.setInterpolator(new AccelerateInterpolator());
                    upAnimator.start();
                    break;
            }
            return false;
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return bookArrayList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Book> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(orig);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (Book book : orig) {
                        if (book.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(book);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                bookArrayList.clear();
                //noinspection unchecked
                bookArrayList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public MaterialButton download;
        public ProgressBar downloadProgressBar;
        public TextView downloadPercent;
        public Button downloadCancel;
        TextView name, section;
        ImageView img;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.book_name);
            section = itemView.findViewById(R.id.book_section);
            img = itemView.findViewById(R.id.book_img);
            download = itemView.findViewById(R.id.download);
            downloadProgressBar = itemView.findViewById(R.id.download_progressBar);
            downloadPercent = itemView.findViewById(R.id.progress_percentage);
            downloadCancel = itemView.findViewById(R.id.download_close);
        }

        void setDetails(Book book) {
            name.setText(book.getName());
            section.setText(book.getSection());
            img.setImageResource(book.getImg());
            if (AppUtils.isFileExist(context, context.getString(R.string.books_folder), book.getFile())) {
                download.setText(R.string.open);
                download.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_open));
            } else {
                download.setText(R.string.download);
                download.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_download));
            }
        }

    }

}
