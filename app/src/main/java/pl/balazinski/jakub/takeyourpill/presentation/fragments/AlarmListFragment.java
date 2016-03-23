package pl.balazinski.jakub.takeyourpill.presentation.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.AlarmListAdapter;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.AlarmListAdapter.AlarmListRefreshListener;


public class AlarmListFragment extends Fragment implements AlarmListRefreshListener {

    private final String TAG = getClass().getSimpleName();

    private AlarmListAdapter mAlarmListAdapter;
    private OutputProvider mOutputProvider;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView) inflater.inflate(R.layout.fragment_list, container, false);
        mOutputProvider = new OutputProvider(getContext());
        setupRecyclerView(rv);
        return rv;
    }


    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        mAlarmListAdapter = new AlarmListAdapter(getActivity());
        mAlarmListAdapter.setListRefreshListener(this);
        recyclerView.setAdapter(mAlarmListAdapter);
        refreshList();
    }


    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    public void refreshList() {
        if (mAlarmListAdapter != null) {
            mAlarmListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onListRefresh(){
      /*  Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {*/
        try {
            refreshList();

        }catch (Exception e){
            mOutputProvider.displayDebugLog(TAG, e.toString());
        }
      /*      }
        };
        handler.post(r);*/
    }
}