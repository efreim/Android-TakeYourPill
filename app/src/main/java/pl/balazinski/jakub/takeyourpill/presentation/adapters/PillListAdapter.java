package pl.balazinski.jakub.takeyourpill.presentation.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.activities.PillActivity;
import pl.balazinski.jakub.takeyourpill.presentation.activities.PillDetailActivity;

/**
 * Adapter for lists in fragments
 * Nothing to explain :D
 */
public class PillListAdapter
        extends RecyclerView.Adapter<PillListAdapter.ViewHolder> {

    private int mBackground;
    private Context context;
    private PillListRefreshListener refreshListener;
    private ViewHolder viewHolder;

    public PillListAdapter(Context context) {
        this.context = context;
        TypedValue mTypedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
    }

    /**
     * Interface implemented in AlarmListFragment in order to refresh list after deleting item from.
     */
    public interface PillListRefreshListener {
        void onListRefresh();
    }

    public void setListRefreshListener(PillListRefreshListener l) {
        this.refreshListener = l;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pill_list_item, parent, false);
        view.setBackgroundResource(mBackground);
        viewHolder = new ViewHolder(view,this);
        return viewHolder;
    }

    public Pill getItem(int position) {
        Pill pill = DatabaseRepository.getAllPills(context).get(position);
        if (pill != null)
            return pill;
        else
            return null;

    }



    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.pill = getItem(position);
        holder.mTextView.setText(holder.pill.getName());

        Glide.with(holder.mImageView.getContext())
                .load(Uri.parse(getItem(position).getPhoto()))
                .fitCenter()
                .into(holder.mImageView);

    //    animate(holder);
    }

    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, R.anim.bounce_interpolator);
        viewHolder.itemView.setAnimation(animAnticipateOvershoot);
    }

    @Override
    public int getItemCount() {
        return DatabaseRepository.getAllPills(context).size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener, View.OnLongClickListener, View.OnClickListener{
        public Pill pill;
        public final View mView;
        public final ImageView mImageView;
        public final TextView mTextView;
        private PillListAdapter adapter;
        private OutputProvider outputProvider;
        private Context context;

        public ViewHolder(View view, PillListAdapter adapter) {
            super(view);
            context = view.getContext();
            outputProvider = new OutputProvider(context);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.avatar);
            mTextView = (TextView) view.findViewById(android.R.id.text1);
            mView.setOnClickListener(this);
            mView.setOnLongClickListener(this);
            this.adapter = adapter;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.edit_pill:
                    Intent intent = new Intent(context, PillActivity.class);
                    intent.putExtra(Constants.EXTRA_LONG_ID, pill.getId());
                    context.startActivity(intent);
                    break;
                case R.id.delete_pill:
                    pillDeleter();
                    break;
                default:
                    break;
            }
            return true;
        }

        @Override
        public boolean onLongClick(View v) {
            outputProvider.displayPopupMenu(this, v, R.menu.pill_context_menu);
            return false;
        }

        public void pillDeleter() {
            DatabaseRepository.deletePill(context, pill);
            mView.invalidate();
            adapter.refreshListener.onListRefresh();
            outputProvider.displayShortToast("Alarm deleted!");
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, PillDetailActivity.class);
            intent.putExtra(Constants.EXTRA_LONG_ID, pill.getId());
            context.startActivity(intent);
        }
    }




}