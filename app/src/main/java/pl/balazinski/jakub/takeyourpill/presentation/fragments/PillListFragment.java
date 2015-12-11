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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

import pl.balazinski.jakub.takeyourpill.domain.PillManager;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.RecyclerViewListAdapter;
import pl.balazinski.jakub.takeyourpill.R;

public class PillListFragment extends Fragment {

    public RecyclerViewListAdapter listAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView) inflater.inflate(
                R.layout.fragment_cheese_list, container, false);

        Log.i("FRAGMENT", "onCreateView");
        setupRecyclerView(rv);
        return rv;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        listAdapter = new RecyclerViewListAdapter(getActivity());
        recyclerView.setAdapter(listAdapter);
        recyclerView.invalidateItemDecorations();
        recyclerView.invalidate();
        listAdapter.notifyDataSetChanged();
    }



    //public void updateList(){
    //    listAdapter.notifyDataSetChanged();
    //}

}
