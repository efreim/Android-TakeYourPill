package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.FragmentViewPagerAdapter;
import pl.balazinski.jakub.takeyourpill.presentation.fragments.IntervalAlarmFragment;
import pl.balazinski.jakub.takeyourpill.presentation.fragments.RepeatingAlarmFragment;
import pl.balazinski.jakub.takeyourpill.presentation.fragments.SingleAlarmFragment;

/**
 * Activity that holds add alarm fragments
 */
public class AlarmActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    /**
     * State activity is entered in
     */
    public enum State {
        NEW, EDIT
    }

    /**
     * Setting up components for activity
     */
    @Bind(R.id.toolbarPill)
    Toolbar toolbar;
    @Bind(R.id.viewpager)
    ViewPager viewPager;
    @Bind(R.id.tabs)
    TabLayout tabLayout;

    @Bind(R.id.add_alarm)
    Button addAlarm;

    private State mState;
    private Alarm mAlarm;
    private IntervalAlarmFragment mIntervalFragment;
    private RepeatingAlarmFragment mRepeatableFragment;
    private SingleAlarmFragment mSingleFragment;
    private OutputProvider mOutputProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        mOutputProvider = new OutputProvider(this);

        createFragments();
        setupContent(extras);
        setupView();

    }

    private void createFragments() {
        mRepeatableFragment = new RepeatingAlarmFragment();
        mIntervalFragment = new IntervalAlarmFragment();
        mSingleFragment = new SingleAlarmFragment();
    }

    private void setupContent(Bundle extras) {
        /**
         * If extras are empty mState is new otherwise
         * mState is edit and edited pill must be loaded
         * from database.
         */
        if (extras == null) {
            mState = AlarmActivity.State.NEW;
            addAlarm.setText(getString(R.string.add_alarm));
        } else {
            addAlarm.setText(getString(R.string.edit_alarm));
            mState = AlarmActivity.State.EDIT;
            Long id = extras.getLong(Constants.EXTRA_LONG_ID);

            mAlarm = DatabaseRepository.getAlarmById(this, id);
            Bundle bundle = new Bundle();
            bundle.putLong(Constants.EXTRA_LONG_ID, id);


            if (mAlarm == null)
                mOutputProvider.displayShortToast(getString(R.string.error_loading_pills));
            else {
                if (mAlarm.isRepeatable()) {
                    mRepeatableFragment.setArguments(bundle);
                } else if (mAlarm.isInterval()) {
                    mIntervalFragment.setArguments(bundle);
                } else if (mAlarm.isSingle()) {
                    mSingleFragment.setArguments(bundle);
                }
            }

        }
    }

    private void setupView() {
        /**
         * Setting up notification bar color:
         */
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.notification_bar));

        /**
         * Setting up toolbar
         */
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /**
         * Setting up viewpager
         */
        if (viewPager != null) {
            setupViewPager(viewPager);
            tabLayout.setupWithViewPager(viewPager);
        }


        if (Constants.VERSION >= Build.VERSION_CODES.M) {
            addAlarm.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));

        } else {
            addAlarm.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
        }
    }


    /**
     * Sets up viewpager for fragments
     *
     * @param viewPager layout component
     */
    private void setupViewPager(ViewPager viewPager) {
        final FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(getSupportFragmentManager());
        if (mState == State.NEW) {
            adapter.addFragment(mRepeatableFragment, getString(R.string.repeatable));
            adapter.addFragment(mIntervalFragment, getString(R.string.interval));
            adapter.addFragment(mSingleFragment, getString(R.string.single));
        } else if (mState == State.EDIT) {
            if (mAlarm != null) {
                if (mAlarm.isRepeatable()) {
                    adapter.addFragment(mRepeatableFragment, getString(R.string.repeatable));
                } else if (mAlarm.isInterval()) {
                    adapter.addFragment(mIntervalFragment, getString(R.string.interval));
                } else if (mAlarm.isSingle()) {
                    adapter.addFragment(mSingleFragment, getString(R.string.single));
                }
            }
        }
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @OnClick(R.id.add_alarm)
    public void addAlarmButton(View v) {
        boolean isOk = false;
        if(mState == State.NEW) {
            if (viewPager.getCurrentItem() == 0) {
                isOk = mRepeatableFragment.addAlarm(mState);
            } else if (viewPager.getCurrentItem() == 1) {
                isOk = mIntervalFragment.addAlarm(mState);
            } else if (viewPager.getCurrentItem() == 2) {
                isOk = mSingleFragment.addAlarm(mState);
            }
        }else if(mState == State.EDIT){
            if (mAlarm.isRepeatable()) {
                isOk = mRepeatableFragment.addAlarm(mState);
            } else if (mAlarm.isInterval()) {
                isOk = mIntervalFragment.addAlarm(mState);
            } else if (mAlarm.isSingle()) {
                isOk = mSingleFragment.addAlarm(mState);
            }
        }

        if (isOk)
            finish();
    }


    //TODO Check if this works without this
  /*@Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            getFragmentManager().popBackStack();
        }
    }*/

}
