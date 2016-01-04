package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.PillRepository;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.FragAdapter;
import pl.balazinski.jakub.takeyourpill.presentation.fragments.PillListFragment;
import pl.balazinski.jakub.takeyourpill.R;


/**
 * Main class of the project
 */
public class MainActivity extends AppCompatActivity {

    private PillListFragment alarmFragment;
    private PillListFragment pillFragment;

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

        Bundle alarmBundle = new Bundle();
        alarmBundle.putInt(Constants.ALARM_FRAGMENT, Constants.ALARM_FRAGMENT_VALUE);
        alarmFragment = new PillListFragment();
        alarmFragment.setArguments(alarmBundle);

        Bundle pillBundle = new Bundle();
        pillBundle.putInt(Constants.PILL_FRAGMENT, Constants.PILL_FRAGMENT_VALUE);
        pillFragment = new PillListFragment();
        pillFragment.setArguments(pillBundle);

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


    @OnClick(R.id.fab)
    public void onClick(View v) {
        if (viewPager != null) {
            if (viewPager.getCurrentItem() == 0)
                Toast.makeText(getApplicationContext(), "Add alarm", Toast.LENGTH_SHORT).show();
            else {
                Intent intent = new Intent(getApplicationContext(), PillActivity.class);
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
        Log.i("ITEM", String.valueOf(item.getItemId()));

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.delete_database:
                Log.i("ITEM", String.valueOf(R.id.delete_database));

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
                                PillRepository.deleteDatabase(MainActivity.this);
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
        FragAdapter adapter = new FragAdapter(getSupportFragmentManager());
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
                        switch (menuItem.getItemId()) {
                            case R.id.nav_one:
                                Toast.makeText(getApplicationContext(), "First item", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.nav_two:
                                Toast.makeText(getApplicationContext(), "Second item", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.nav_three:
                                Toast.makeText(getApplicationContext(), "Third item", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.nav_four:
                                Toast.makeText(getApplicationContext(), "Fourth item", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.find_pharmacy:
                                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
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
