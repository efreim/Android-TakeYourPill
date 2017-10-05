package pl.balazinski.jakub.takeyourpill.presentation.views;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.utilities.Constants;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;


public class HorizontalScrollViewItem extends RelativeLayout implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private CheckBox mCheckBox;
    private ImageView mImageView;
    private TextView mTextView;

    private Context mContext;
    private boolean wasClicked = false;
    private OutputProvider mOutputProvider;
    private String mImagePath;
    private String mText;
    private Long mPillId;

    public HorizontalScrollViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public HorizontalScrollViewItem(Context context, String imagePath, String text, Long pillId) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_horizontal_scroll_item, this, true);
        this.mContext = context;
        this.mImagePath = imagePath;
        this.mText = text;
        this.mPillId = pillId;
        setOnClickListener(this);
        mOutputProvider = new OutputProvider(context);

        setContent();
        setView();
    }

    private void setView() {
        if (Constants.VERSION >= Build.VERSION_CODES.M) {
            setBackground(getResources().getDrawable(R.drawable.ripple_effect, mContext.getTheme()));
        } else {
            setBackground(getResources().getDrawable(R.drawable.ripple_effect));
        }
        Glide.with(mImageView.getContext())
                .load(Uri.parse(mImagePath))
                .fitCenter()
                .into(mImageView);
        mTextView.setText(mText);
        setClicked();
    }

    private void setContent() {
        RelativeLayout relativeLayout = (RelativeLayout) getChildAt(0);
        mImageView = (ImageView) relativeLayout.getChildAt(0);
        mTextView = (TextView) relativeLayout.getChildAt(1);
        mCheckBox = (CheckBox) relativeLayout.getChildAt(2);
    }

    private void setClicked() {
        if (!wasClicked) {
            mCheckBox.setChecked(false);
            mTextView.setTextColor(Color.BLACK);
        } else {
            mCheckBox.setChecked(true);
            mTextView.setTextColor(Color.BLACK);
        }
    }

    @Override
    public void onClick(View v) {
        wasClicked = (!wasClicked);
        setClicked();
        //mOutputProvider.displayLog(TAG, "Clicked");
    }

    public void setClick() {
        wasClicked = (!wasClicked);
        setClicked();
        //mOutputProvider.displayLog(TAG, "Clicked");
    }

    public boolean isChecked() {
        return (mCheckBox.isChecked());
    }

    public void setTextColorWhite() {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            mTextView.setTextColor(ContextCompat.getColor(mContext, R.color.alarm_list_item_text));
        } else {
            mTextView.setTextColor(mContext.getResources().getColor(R.color.alarm_list_item_text));
        }
    }

    public String getText() {
        return mText;
    }

    public Long getPillId() {
        return mPillId;
    }

    public void setCheckboxGone() {
        mCheckBox.setVisibility(GONE);
    }

    public void setImageGone() {
        mImageView.setVisibility(GONE);
    }
}
