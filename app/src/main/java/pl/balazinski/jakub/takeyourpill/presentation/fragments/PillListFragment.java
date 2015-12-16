/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.balazinski.jakub.takeyourpill.presentation.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.manager.PillManager;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.RecyclerViewListAdapter;

public class PillListFragment extends Fragment {

    private RecyclerViewListAdapter listAdapter;
    private RecyclerView rv;

    //@Bind(R.id.fab)

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rv = (RecyclerView) inflater.inflate(
                R.layout.fragment_pill_list, container, false);
        FloatingActionButton button = (FloatingActionButton) getView().findViewById(R.id.fab);
        setupRecyclerView(rv);
        return rv;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Toast.makeText(getContext(), "test", Toast.LENGTH_SHORT).show();
        super.onConfigurationChanged(newConfig);
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        listAdapter = RecyclerViewListAdapter.getInstance(getActivity());
        recyclerView.setAdapter(listAdapter);
        //ButterKnife.bind(rv);
        //button.attachToRecyclerView(recyclerView);
        PillManager.getInstance().setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }

    public void updateList() {
        if (PillManager.getInstance().getAdapter() != null)
            PillManager.getInstance().getAdapter().notifyDataSetChanged();
    }

}
