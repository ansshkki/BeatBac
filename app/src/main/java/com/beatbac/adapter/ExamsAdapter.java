package com.beatbac.adapter;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beatbac.AppUtils;
import com.beatbac.Data;
import com.beatbac.R;
import com.beatbac.model.Exam;

import java.util.ArrayList;
import java.util.List;

public class ExamsAdapter extends RecyclerView.Adapter<ExamsAdapter.ViewHolder> implements Filterable {

    private Activity context;
    private List<Exam> examArrayList;
    private List<Exam> orig;

    public ExamsAdapter(Activity context, ArrayList<Exam> exams) {
        this.context = context;
        this.examArrayList = exams;
        this.orig = new ArrayList<>(exams);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.exam_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Exam exam = examArrayList.get(position);
        holder.setDetails(exam);

        holder.itemView.setOnClickListener(view -> AppUtils.showDialog(context, exam, Data.EXAM));
        holder.examQuestions.setOnClickListener(view -> AppUtils.showDialog(context, exam, Data.EXAM));
        holder.examSolution.setOnClickListener(view -> AppUtils.showDialog(context, exam, Data.SOLUTION));

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
        return examArrayList.size();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Exam> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(orig);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (Exam exam : orig) {
                        if (exam.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(exam);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                examArrayList.clear();
                //noinspection unchecked
                examArrayList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, section;
        ImageView icon;
        Button examQuestions;
        Button examSolution;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.exam_subject);
            section = itemView.findViewById(R.id.exam_section);
            icon = itemView.findViewById(R.id.exam_subject_icon);
            examQuestions = itemView.findViewById(R.id.exam_questions);
            examSolution = itemView.findViewById(R.id.exam_solutions);
        }

        void setDetails(Exam exam) {
            name.setText(exam.getName());
            section.setText(exam.getSection());
            icon.setImageResource(exam.getIcon());
        }

    }

}
