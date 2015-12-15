package pl.balazinski.jakub.takeyourpill.manager;


import java.util.ArrayList;

import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.RecyclerViewListAdapter;

/**
 * Created by Kuba on 08.12.2015.
 */
public class PillManager {
    private static PillManager mInstance = null;


    private ArrayList<Pill> pillList;
    private RecyclerViewListAdapter adapter;

    public PillManager() {
        pillList = new ArrayList<>();
    }

    public static PillManager getInstance(){
        if(mInstance == null){
            mInstance = new PillManager();
        }
        return mInstance;
    }

    public RecyclerViewListAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(RecyclerViewListAdapter adapter) {
        this.adapter = adapter;
    }

    public ArrayList<Pill> getPillList() {
        return pillList;
    }

    public void addPill(Pill pill) {
        pillList.add(pill);
    }

    public void removePill(Pill pill) {
        pillList.remove(pill);
    }

    public Pill getPill(int position) {
        return pillList.get(position);
    }

    public int getSize(){
        return pillList.size();
    }

}
