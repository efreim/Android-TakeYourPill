package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
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

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.domain.PillManager;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.FragAdapter;
import pl.balazinski.jakub.takeyourpill.presentation.adapters.RecyclerViewListAdapter;
import pl.balazinski.jakub.takeyourpill.presentation.fragments.PillListFragment;
import pl.balazinski.jakub.takeyourpill.R;


public class MainActivity extends AppCompatActivity {

    private PillListFragment activeFragment;
    private PillListFragment inactiveFragment;

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

        if (savedInstanceState == null) {
            activeFragment = new PillListFragment();
            inactiveFragment = new PillListFragment();
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
        if (viewPager != null) {
            setupViewPager(viewPager);
        }
        tabLayout.setupWithViewPager(viewPager);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Main", "ON_RESUME");
        if (activeFragment != null) {
            if (activeFragment.listAdapter != null)
                activeFragment.listAdapter.notifyDataSetChanged();
        }
        if (inactiveFragment != null) {
            if (inactiveFragment.listAdapter != null)
                inactiveFragment.listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Main", "ON_START");
        if (activeFragment != null) {
            if (activeFragment.listAdapter != null)
                activeFragment.listAdapter.notifyDataSetChanged();
        }
        if (inactiveFragment != null) {
            if (inactiveFragment.listAdapter != null)
                inactiveFragment.listAdapter.notifyDataSetChanged();
        }
    }

    @OnClick(R.id.fab)
    public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), PillActivity.class);
        startActivity(intent);
        if (activeFragment != null) {
            if (activeFragment.listAdapter != null)
                activeFragment.listAdapter.notifyDataSetChanged();
        }
        if (inactiveFragment != null) {
            if (inactiveFragment.listAdapter != null)
                inactiveFragment.listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        FragAdapter adapter = new FragAdapter(getSupportFragmentManager());
        adapter.addFragment(activeFragment, "Active");
        adapter.addFragment(inactiveFragment, "Inactive");
        viewPager.setAdapter(adapter);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

}
