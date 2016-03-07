package pl.balazinski.jakub.takeyourpill.presentation.views;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.ButterKnife;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;

/**
 * Created by Kuba on 02.03.2016.
 */
public class DayOfWeekView extends RelativeLayout implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private CheckBox checkBox;

    private TextView textView;

    private Context context;
    private boolean wasClicked = false;
    private OutputProvider outputProvider;
    private String text;
    private int id;

    public DayOfWeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public DayOfWeekView(Context context, int id, String text) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.day_of_week_view, this, true);
        this.context = context;
        ButterKnife.bind(this);
        setOnClickListener(this);
        this.text = text;
        this.id = id;
        outputProvider = new OutputProvider(context);
        RelativeLayout relativeLayout = (RelativeLayout) getChildAt(0);
        textView = (TextView) relativeLayout.getChildAt(0);
        checkBox = (CheckBox) relativeLayout.getChildAt(1);
        setContent();
    }



    private void setContent() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setBackground(getResources().getDrawable(R.drawable.ripple_effect, context.getTheme()));
        } else {
            setBackground(getResources().getDrawable(R.drawable.ripple_effect));
        }
        textView.setText(text);
        setClicked();
    }

    private void setClicked() {
        if (!wasClicked) {
            checkBox.setChecked(false);
            textView.setTextColor(Color.BLACK);
        } else {
            checkBox.setChecked(true);
            textView.setTextColor(Color.BLACK);
        }
    }

    @Override
    public void onClick(View v) {
        wasClicked = (!wasClicked);
        setClicked();
        outputProvider.displayLog(TAG, "Clicked");
    }

    public void setClick(){
        wasClicked = (!wasClicked);
        setClicked();
        outputProvider.displayLog(TAG, "Clicked");
    }

    public boolean isChecked(){
        return (checkBox.isChecked());
    }

    public String getText() {
        return text;
    }

    public int getId(){
        return id;
    }

    public void setCheckboxGone(){
        checkBox.setVisibility(GONE);
    }
}