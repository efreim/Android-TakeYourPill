package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.domain.AlarmReceiver;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.FragmentViewPagerAdapter;
import pl.balazinski.jakub.takeyourpill.presentation.fragments.AlarmListFragment;
import pl.balazinski.jakub.takeyourpill.presentation.fragments.PillListFragment;


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        outputProvider = new OutputProvider(this);
        Intent intent = getIntent();
        if (intent != null) {
            Long pillID = intent.getLongExtra("pillID", -1);
            Long alarmID = intent.getLongExtra("alarmID", -1);
            outputProvider.displayLog(TAG,"pillID == " + String.valueOf(pillID));
            outputProvider.displayLog(TAG, "alarmID == " + String.valueOf(alarmID));
            if (((pillID != -1) && (alarmID != -1)) || ((pillID == -1) && (alarmID != -1))) {
                displayDialog(pillID, alarmID);
            }
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
        }
        tabLayout.setupWithViewPager(viewPager);
    }

    private void createFragments(){
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
                alertDialog.show();
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
        FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(alarmFragment, "Alarms");
        adapter.addFragment(pillFragment, "Pills");
        viewPager.setAdapter(adapter);
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
                                outputProvider.displayShortToast("To be done");
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.find_pharmacy_drawer:
                                intent = new Intent(getApplicationContext(), MapsActivity.class);
                                startActivity(intent);
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.about_drawer:
                                outputProvider.displayShortToast("To be done");
                                mDrawerLayout.closeDrawers();
                                break;
                            default:
                                return true;
                        }
                        return true;
                    }
                });
    }

    private void displayDialog(final Long pillId, final Long alarmID) {
        final AlarmReceiver alarmReceiver = new AlarmReceiver();
        Pill pill = null;
        if(pillId!=null) {
            pill = DatabaseRepository.getPillByID(this, pillId);
            outputProvider.displayLog(TAG, "pillId is not null : " + pillId);
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("Take your pill!");
        alertDialogBuilder.setMessage("Did you take your pill?");
        // set positive button: Yes message
        final Pill finalPill = pill;

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                AlarmReceiver.stopRingtone();
                alarmReceiver.cancelAlarm(getApplicationContext(), alarmID);
                if (finalPill != null) {
                    int pillRemaining = finalPill.getPillsRemaining();
                    int pillDosage = finalPill.getDosage();
                    if (pillRemaining != -1 && pillDosage != -1) {
                        finalPill.setPillsRemaining(pillRemaining - pillDosage);
                        DatabaseHelper.getInstance(getApplicationContext()).getPillDao().update(finalPill);
                        alarmFragment.refreshList();
                    }
                } else
                    outputProvider.displayShortToast("No pill added to alarm");
            }
        });
        // set negative button: No message
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                AlarmReceiver.stopRingtone();
                // cancel the alert box and put a Toast to the user
                if (finalPill != null) {
                    alarmReceiver.cancelAlarm(getApplicationContext(), alarmID);
                    alarmFragment.refreshList();
                }
                dialog.cancel();
                outputProvider.displayShortToast("You chose a negative answer");
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        // show alert
        alertDialog.show();
    }

}
