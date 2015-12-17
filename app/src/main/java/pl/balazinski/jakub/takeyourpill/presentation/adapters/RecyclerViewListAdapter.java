package pl.balazinski.jakub.takeyourpill.presentation.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import pl.balazinski.jakub.takeyourpill.manager.PillManager;
import pl.balazinski.jakub.takeyourpill.presentation.activities.PillActivity;
import pl.balazinski.jakub.takeyourpill.presentation.activities.PillDetailActivity;
import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.R;

/**
 * Created by Kuba on 08.12.2015.
 */
public class RecyclerViewListAdapter
        extends RecyclerView.Adapter<RecyclerViewListAdapter.ViewHolder> {

    private static RecyclerViewListAdapter mInstance = null;
    private TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    private ArrayList<Pill> pillArrayList;

    public static RecyclerViewListAdapter getInstance(Context context){
        if(mInstance == null){
            mInstance = new RecyclerViewListAdapter(context);
        }
        return mInstance;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Pill pill;

        public final View mView;
        public final ImageView mImageView;
        public final TextView mTextView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.avatar);
            mTextView = (TextView) view.findViewById(android.R.id.text1);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText();
        }
    }


    public RecyclerViewListAdapter(Context context) {
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        pillArrayList = PillManager.getInstance().getPillList();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        view.setBackgroundResource(mBackground);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.pill = pillArrayList.get(position);
        holder.mTextView.setText(holder.pill.getName());
        Log.i("onBindViewHolder", String.valueOf(position));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("onClick", String.valueOf(position));
                if (position != -1) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, PillDetailActivity.class);
                    intent.putExtra("pos", position);
                    context.startActivity(intent);
                }
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if(position != -1){
                    Context context = v.getContext();
                    Intent intent = new Intent(context, PillActivity.class);
                    intent.putExtra("pos", position);
                    context.startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        Glide.with(holder.mImageView.getContext())
                .load(pillArrayList.get(position).getPhoto())
                .fitCenter()
                .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return pillArrayList.size();
    }
}