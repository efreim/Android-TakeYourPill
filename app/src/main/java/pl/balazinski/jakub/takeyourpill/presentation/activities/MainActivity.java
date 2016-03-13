package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.database.Pill;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.FragmentViewPagerAdapter;
import pl.balazinski.jakub.takeyourpill.presentation.fragments.AlarmListFragment;
import pl.balazinski.jakub.takeyourpill.presentation.fragments.PillListFragment;
import pl.balazinski.jakub.takeyourpill.utilities.AlarmReceiver;


/**
 * Main class of the project
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private AlarmListFragment alarmFragment;
    private PillListFragment pillFragment;
    private OutputProvider outputProvider;

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
    @Bind(R.id.fab)
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        outputProvider = new OutputProvider(this);
        Intent intent = getIntent();
        if (intent != null) {
            Long alarmID = intent.getLongExtra("alarmID", -1);
            outputProvider.displayLog(TAG, "alarmID == " + String.valueOf(alarmID));
          /*  if (alarmID != -1) {
                displayDialog(alarmID);
            }*/
        }

        createFragments();

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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

        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));
        } else {
            button.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
        }
        button.setText("Add alarm");
    }

    private void createFragments() {
        Bundle alarmBundle = new Bundle();
        alarmBundle.putInt(Constants.ALARM_FRAGMENT, Constants.ALARM_FRAGMENT_VALUE);
        alarmFragment = new AlarmListFragment();
        alarmFragment.setArguments(alarmBundle);

        Bundle pillBundle = new Bundle();
        pillBundle.putInt(Constants.PILL_FRAGMENT, Constants.PILL_FRAGMENT_VALUE);
        pillFragment = new PillListFragment();
        pillFragment.setArguments(pillBundle);
    }


    @OnClick(R.id.fab)
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
     * Drawer panel left on action bar
     *
     * @param item Open drawer item
     * @return If true open drawer
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        outputProvider.displayLog(TAG, "(options) item id = " + String.valueOf(item.getItemId()));
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.delete_database:
                outputProvider.displayLog(TAG, "(delete db) item id = " + String.valueOf(R.id.delete_database));

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle("Delete database");
                alertDialogBuilder.setMessage("Deleting database will remove all of your pills and alarms!\nAre you sure?")
                        .setCancelable(false)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //// TODO: 2016-02-09 deactivate all alarms before deleting database!!
                                DatabaseRepository.deleteWholeDatabase(MainActivity.this);
                                alarmFragment.refreshList();
                                pillFragment.refreshList();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
                break;
            case R.id.action_settings:
                startActivity(new Intent(getApplicationContext(), AppPreferences.class));
                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up viewpager for fragments
     *
     * @param viewPager layout component
     */
    private void setupViewPager(ViewPager viewPager) {
        final FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(alarmFragment, "Alarms");
        adapter.addFragment(pillFragment, "Pills");
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    button.setText("Add alarm");
                    alarmFragment.refreshList();
                } else {
                    button.setText("Add pill");
                    pillFragment.refreshList();
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
                        Intent intent = null;
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
                                startActivity(new Intent(getApplicationContext(), AppPreferences.class));
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
