package pl.balazinski.jakub.takeyourpill.presentation.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.PillRepository;
import pl.balazinski.jakub.takeyourpill.presentation.activities.PillActivity;
import pl.balazinski.jakub.takeyourpill.presentation.activities.PillDetailActivity;
import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.R;

/**
 * Adapter for lists in fragments
 * Nothing to explain :D
 */
public class RecyclerViewListAdapter
        extends RecyclerView.Adapter<RecyclerViewListAdapter.ViewHolder> {

    private int mBackground;
    private Context context;
    private List<Pill> pills;

    public RecyclerViewListAdapter(Context context, List<Pill> list) {
        this.context = context;
        TypedValue mTypedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        pills = list;
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


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        view.setBackgroundResource(mBackground);
        return new ViewHolder(view);
    }

    public Pill getItem(int position) {
        return PillRepository.getAllPills(context).get(position);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.pill = getItem(position);
        holder.mTextView.setText(holder.pill.getName());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position != -1) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, PillDetailActivity.class);
                    intent.putExtra(Constants.EXTRA_INT, position);
                    context.startActivity(intent);
                }
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (position != -1) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, PillActivity.class);
                    intent.putExtra(Constants.EXTRA_INT, position);
                    context.startActivity(intent);
                    return true;
                }
                return false;
            }
        });

            Glide.with(holder.mImageView.getContext())
                    .load(Uri.parse(getItem(position).getPhoto()))
                    .fitCenter()
                    .into(holder.mImageView);

    }

    @Override
    public int getItemCount() {
        return PillRepository.getAllPills(context).size();
    }
}