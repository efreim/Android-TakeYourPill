package pl.balazinski.jakub.takeyourpill.presentation.views;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.ButterKnife;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;

/**
 * Created by Kuba on 26.02.2016.
 */
public class HorizontalScrollViewItem extends LinearLayout implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private CheckBox checkBox;
    // @Bind(R.id.circle_image_horizontal_item)
    private ImageView imageView;

    //  @Bind(R.id.pill_name_horizontal_item)
    private TextView textView;
    private RelativeLayout relativeLayout;

    private Context context;
    private boolean wasClicked = false;
    private OutputProvider outputProvider;
    private String imagePath;
    private String text;
    private Long pillId;

    public HorizontalScrollViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public HorizontalScrollViewItem(Context context, String imagePath, String text, Long pillId) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.horizontal_scroll_view_item, this, true);
        this.context = context;
        ButterKnife.bind(this);
        setOnClickListener(this);
        this.imagePath = imagePath;
        this.text = text;
        this.pillId = pillId;
        outputProvider = new OutputProvider(context);
        relativeLayout = (RelativeLayout) getChildAt(0);
        imageView = (ImageView) relativeLayout.getChildAt(0);
        textView = (TextView) relativeLayout.getChildAt(1);
        checkBox = (CheckBox) relativeLayout.getChildAt(2);
        setContent();
    }

   /* @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(height, height);

        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width;
        setMeasuredDimension(size, size);

    }*/


    private void setContent() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setBackground(getResources().getDrawable(R.drawable.ripple_effect, context.getTheme()));
        } else {
            setBackground(getResources().getDrawable(R.drawable.ripple_effect));
        }
        Glide.with(imageView.getContext())
                .load(Uri.parse(imagePath))
                .fitCenter()
                .into(imageView);
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

    public Long getPillId() {
        return pillId;
    }

    public void setCheckboxGone(){
        checkBox.setVisibility(GONE);
    }
}
