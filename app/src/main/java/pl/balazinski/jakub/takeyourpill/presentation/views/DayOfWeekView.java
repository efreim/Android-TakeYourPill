package pl.balazinski.jakub.takeyourpill.presentation.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;

public class DayOfWeekView extends RelativeLayout implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private CheckBox mCheckBox;
    private TextView mTextView;

    private Context mContext;
    private boolean wasClicked = false;
    private OutputProvider mOutputProvider;
    private String mText;
    private int mId;

    public DayOfWeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public DayOfWeekView(Context context, int id, String text) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_day_of_week, this, true);
        this.mContext = context;
        this.mText = text;
        this.mId = id;

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
        mTextView.setText(mText);
        setClicked();
    }

    private void setContent() {
        RelativeLayout relativeLayout = (RelativeLayout) getChildAt(0);
        mTextView = (TextView) relativeLayout.getChildAt(0);
        mCheckBox = (CheckBox) relativeLayout.getChildAt(1);
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

    public String getText() {
        return mText;
    }

    public int getId() {
        return mId;
    }

    public void setCheckboxGone() {
        mCheckBox.setVisibility(GONE);
    }
}