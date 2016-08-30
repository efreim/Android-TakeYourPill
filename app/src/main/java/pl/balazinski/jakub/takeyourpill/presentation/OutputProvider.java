package pl.balazinski.jakub.takeyourpill.presentation;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import pl.balazinski.jakub.takeyourpill.presentation.adapters.AlarmListAdapter;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.PillListAdapter;


public class OutputProvider {

    private Context mContext;

    public OutputProvider(Context context) {
        this.mContext = context;
    }

    public void displayShortToast(String toastText) {
        Toast.makeText(mContext, toastText, Toast.LENGTH_SHORT).show();
    }

    public void displayLongToast(String toastText) {
        Toast.makeText(mContext, toastText, Toast.LENGTH_LONG).show();
    }

    public void displayPopupMenu(PillListAdapter.ViewHolder holder, View v, int menuResource) {
        PopupMenu popupMenu = new PopupMenu(mContext, v);
        popupMenu.inflate(menuResource);
        popupMenu.setOnMenuItemClickListener(holder);
        popupMenu.show();
    }

    public void displayPopupMenu(AlarmListAdapter.ViewHolder holder, View v, int menuResource) {
        PopupMenu popupMenu = new PopupMenu(mContext, v, Gravity.CENTER);
        popupMenu.inflate(menuResource);
        popupMenu.setOnMenuItemClickListener(holder);
        popupMenu.show();
    }

    /*public void displayLog(String where, String whaaat) {
        Log.i(where, whaaat);
    }

    public void displayDebugLog(String where, String whaaat) {
        Log.i(where, whaaat);
    }*/

}
