package com.beatbac.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beatbac.AppUtils;
import com.beatbac.Data;
import com.beatbac.R;
import com.beatbac.adapter.ExamsAdapter;
import com.beatbac.model.Exam;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExamsFragment extends Fragment {

    private SearchView searchView;
    private String query;
    private ExamsAdapter adapter;
    private SharedPreferences preferences;

    public ExamsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() != null)
            preferences = getContext().getSharedPreferences(getString(R.string.preference_filename), Context.MODE_PRIVATE);

        String section = preferences.getString(getString(R.string.key_section_preference), getString(R.string.scientific));
        ArrayList<Exam> arrayList = new ArrayList<>(Data.getExams(section.equals(getString(R.string.scientific)) ? Data.SCIENTIFIC : Data.LITERARY,
                preferences.getString(getString(R.string.key_language_preference), getString(R.string.french)),
                preferences.getString(getString(R.string.key_religion_preference), getString(R.string.islamic))));
        final RecyclerView recyclerView = view.findViewById(R.id.list);
        adapter = new ExamsAdapter(getActivity(), arrayList);
        recyclerView.setAdapter(adapter);

        if (AppUtils.getScreenWidthDp(Objects.requireNonNull(getActivity())) >= 1200) {
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
            recyclerView.setLayoutManager(gridLayoutManager);
        } else if (AppUtils.getScreenWidthDp(getActivity()) >= 800) {
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
            recyclerView.setLayoutManager(gridLayoutManager);
        } else {
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(linearLayoutManager);
        }
        searchView = view.findViewById(R.id.search);
        query = "";
        /*ChipGroup filter = view.findViewById(R.id.group_chip);

        filter.setOnCheckedChangeListener((group, checkedId) -> {
            arrayList.clear();
            switch (checkedId) {
                case R.id.chip_all:
                    arrayList.addAll(Data.getExams(Data.ALL));
                    break;
                case R.id.chip_scientific:
                    arrayList.addAll(Data.getExams(Data.SCIENTIFIC));
                    break;
                case R.id.chip_literary:
                    arrayList.addAll(Data.getExams(Data.LITERARY));
                    break;
                default:
                    Chip chip = view.findViewById(R.id.chip_all);
                    chip.setChecked(true);
                    break;
            }
            adapter = new ExamsAdapter(getActivity(), arrayList);
            recyclerView.setAdapter(adapter);
            searchView.setQuery(query + " ", false);
        });
*/
        setupSearchView();

    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                query = s;
                adapter.getFilter().filter(query);
                return true;
            }
        });
    }

}
