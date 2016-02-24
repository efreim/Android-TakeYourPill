package pl.balazinski.jakub.takeyourpill.presentation;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.AlarmListAdapter;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.PillListAdapter;

/**
 * Created by Kuba on 15.02.2016.
 */
public class OutputProvider {

    private Context context;

    public OutputProvider(Context context){
        this.context = context;
    }

    public void displayShortToast(String toastText){
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
    }

    public void displayLongToast(String toastText){
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
    }

    public void displayPopupMenu(PillListAdapter.ViewHolder holder, View v, int menuResource){
        PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.inflate(menuResource);
        popupMenu.setOnMenuItemClickListener(holder);
        popupMenu.show();
    }

    public void displayPopupMenu(AlarmListAdapter.ViewHolder holder, View v, int menuResource){
        PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.inflate(menuResource);
        popupMenu.setOnMenuItemClickListener(holder);
        popupMenu.show();
    }

    public void displayLog(String where, String whaaat){
        Log.i(where,whaaat);
    }
}
