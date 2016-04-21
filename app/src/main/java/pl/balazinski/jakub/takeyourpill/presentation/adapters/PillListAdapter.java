package pl.balazinski.jakub.takeyourpill.presentation.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.database.Pill;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.activities.PillActivity;
import pl.balazinski.jakub.takeyourpill.presentation.activities.PillDetailActivity;


public class PillListAdapter
        extends RecyclerView.Adapter<PillListAdapter.ViewHolder> {

    private int mBackground;
    private Context mContext;
    private PillListRefreshListener mRefreshListener;

    public PillListAdapter(Context context) {
        this.mContext = context;
        TypedValue mTypedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
    }

    public void setListRefreshListener(PillListRefreshListener l) {
        this.mRefreshListener = l;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_pill_item, parent, false);
        view.setBackgroundResource(mBackground);
        return new ViewHolder(view, this);
    }

    public Pill getItem(int position) {
        Pill pill = DatabaseRepository.getAllPills(mContext).get(position);
        if (pill != null)
            return pill;
        else
            return null;

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.pill = getItem(position);
        holder.mTextView.setText(holder.pill.getName());

        if (Constants.VERSION >= Build.VERSION_CODES.M) {
            holder.pillItem.setBackground(ContextCompat.getDrawable(mContext, R.drawable.alarm_list_item_inactive_background));
        } else {
            holder.pillItem.setBackground(mContext.getResources().getDrawable(R.drawable.alarm_list_item_inactive_background));
        }

        Glide.with(holder.mImageView.getContext())
                .load(Uri.parse(getItem(position).getPhoto()))
                .fitCenter()
                .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return DatabaseRepository.getAllPills(mContext).size();
    }


    /**
     * Interface implemented in AlarmListFragment in order to refresh list after deleting item from.
     */
    public interface PillListRefreshListener {
        void onListRefresh();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener, View.OnLongClickListener, View.OnClickListener {
        public final View mView;
        public final ImageView mImageView;
        public final TextView mTextView;
        public final RelativeLayout pillItem;
        public Pill pill;
        private PillListAdapter mAdapter;
        private OutputProvider mOutputProvider;
        private Context mContext;

        public ViewHolder(View view, PillListAdapter adapter) {
            super(view);
            mContext = view.getContext();
            mOutputProvider = new OutputProvider(mContext);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.avatar);
            mTextView = (TextView) view.findViewById(android.R.id.text1);
            pillItem = (RelativeLayout) view.findViewById(R.id.pill_item);
            mView.setOnClickListener(this);
            mView.setOnLongClickListener(this);
            this.mAdapter = adapter;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.edit_pill:
                    Intent intent = new Intent(mContext, PillActivity.class);
                    intent.putExtra(Constants.EXTRA_LONG_ID, pill.getId());
                    mContext.startActivity(intent);
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
            mOutputProvider.displayPopupMenu(this, v, R.menu.pill_context_menu);
            return false;
        }

        public void pillDeleter() {
            DatabaseRepository.deletePill(mContext, pill);
            mView.invalidate();
            mAdapter.mRefreshListener.onListRefresh();
            mOutputProvider.displayShortToast(mContext.getString(R.string.alarm_deleted));
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, PillDetailActivity.class);
            intent.putExtra(Constants.EXTRA_LONG_ID, pill.getId());
            mContext.startActivity(intent);
        }
    }

}