package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.FragmentViewPagerAdapter;
import pl.balazinski.jakub.takeyourpill.presentation.fragments.AlarmListFragment;
import pl.balazinski.jakub.takeyourpill.presentation.fragments.PillListFragment;


/**
 * Main class of the project
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    //Setting up components
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @Bind(R.id.nav_view)
    NavigationView navigationView;
    @Bind(R.id.viewpager)
    ViewPager viewPager;
    @Bind(R.id.tabs)
    TabLayout tabLayout;
    //@Bind(R.id.fab)
    //Button button;
    @Bind(R.id.toolbar_main_add_button)
    ImageButton toolbarAddButton;
    private AlarmListFragment mAlarmFragment;
    private PillListFragment mPillFragment;
    private OutputProvider mOutputProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        mOutputProvider = new OutputProvider(this);

        createFragments();
        setupContent(extras);
        setupView();


    }


    private void createFragments() {
        Bundle alarmBundle = new Bundle();
        alarmBundle.putInt(Constants.ALARM_FRAGMENT, Constants.ALARM_FRAGMENT_VALUE);
        mAlarmFragment = new AlarmListFragment();
        mAlarmFragment.setArguments(alarmBundle);

        Bundle pillBundle = new Bundle();
        pillBundle.putInt(Constants.PILL_FRAGMENT, Constants.PILL_FRAGMENT_VALUE);
        mPillFragment = new PillListFragment();
        mPillFragment.setArguments(pillBundle);
    }

    private void setupContent(Bundle extras) {
        if (extras != null) {
            Long alarmID = extras.getLong(Constants.EXTRA_LONG_ALARM_ID);
            //mOutputProvider.displayLog(TAG, "alarmID == " + String.valueOf(alarmID));
        }
    }

    private void setupView() {
        //Setting up toolbar
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
        if (viewPager != null) {
            setupViewPager(viewPager);
            tabLayout.setupWithViewPager(viewPager);
        }

        /*if (Constants.VERSION >= Build.VERSION_CODES.M) {
            button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));
        } else {
            button.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
        }
        button.setText(getString(R.string.add_alarm));*/
        if (Constants.VERSION >= Build.VERSION_CODES.M) {
            toolbarAddButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_add_white_36dp));

        } else {
            toolbarAddButton.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.ic_add_white_36dp));
        }
    }


    @OnClick(R.id.toolbar_main_add_button)
    public void onClick(View v) {
        if (viewPager != null) {
            if (viewPager.getCurrentItem() == 0) {
                Intent intent = new Intent(getApplicationContext(), AlarmActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getApplicationContext(), AddPillChooserActivity.class);
                startActivity(intent);
            }
        }
    }

    /**
     * Options panel right on action bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    /**
     * Toolbar drawer and options items
     *
     * @param item id of clicked item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_delete_database:

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle(getString(R.string.dialog_delete_database_title));
                alertDialogBuilder.setMessage(getString(R.string.dialog_delete_database_message))
                        .setCancelable(false)
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: 2016-02-09 deactivate all alarms before deleting database!!
                                DatabaseRepository.deleteWholeDatabase(MainActivity.this);
                                mAlarmFragment.refreshList();
                                mPillFragment.refreshList();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
                break;
            case R.id.action_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up viewpager for fragments
     * @param viewPager layout component
     */
    private void setupViewPager(ViewPager viewPager) {
        final FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(mAlarmFragment, getString(R.string.alarms));
        adapter.addFragment(mPillFragment, getString(R.string.pills));
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    //button.setText(getString(R.string.add_alarm));
                    mAlarmFragment.refreshList();
                } else {
                    //button.setText(getString(R.string.add_pill));
                    mPillFragment.refreshList();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * Sets up drawer
     *
     * @param navigationView layout component
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        Intent intent;
                        switch (menuItem.getItemId()) {
                            case R.id.add_alarm_drawer:
                                intent = new Intent(getApplicationContext(), AlarmActivity.class);
                                startActivity(intent);
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.add_pill_drawer:
                                intent = new Intent(getApplicationContext(), AddPillChooserActivity.class);
                                startActivity(intent);
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.settings_drawer:
                                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.find_pharmacy_drawer:
                                intent = new Intent(getApplicationContext(), MapsActivity.class);
                                startActivity(intent);
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.about_drawer:
                                intent = new Intent(getApplicationContext(), AboutActivity.class);
                                startActivity(intent);
                                mDrawerLayout.closeDrawers();
                                break;
                            default:
                                return true;
                        }
                        return true;
                    }
                });
    }

}
