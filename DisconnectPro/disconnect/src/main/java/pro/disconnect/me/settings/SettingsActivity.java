package pro.disconnect.me.settings;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Room;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.strongswan.android.data.VpnProfile;
import org.strongswan.android.data.VpnProfileDataSource;

import java.util.List;

import pro.disconnect.me.R;
import pro.disconnect.me.comms.database.DisconnectDatabase;
import pro.disconnect.me.comms.database.TrackersDao;
import pro.disconnect.me.feeds.PostsViewModel;

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.OnSettingsListFragmentInteractionListener {
    private static final String ACCOUNT_URL = "https://secure-wifi-api.disconnect.me/v1/account?username=%s&device=ios";
    private ProgressBar mBusySpinner;
    private SettingsViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
   //     actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Settings");
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.dark_purple_disconnect));

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // Add fragment one with tag name.
            fragmentTransaction.add(R.id.content_fragment, SettingsFragment.newInstance(SettingsContent.MAIN_MENU), "Fragment One");
            fragmentTransaction.commit();
        }

        mBusySpinner = (ProgressBar)findViewById(R.id.progress_bar);
        mBusySpinner.getIndeterminateDrawable().setColorFilter(Color.GRAY, android.graphics.PorterDuff.Mode.MULTIPLY);

        mViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        mViewModel.getTrackersPurged().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if ( aBoolean != null ){
                    if ( aBoolean ){
                        mBusySpinner.setVisibility( View.GONE );
                        Toast toast = Toast.makeText(getBaseContext(), R.string.tracker_database_purged, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();
                    } else {
                        mBusySpinner.setVisibility( View.VISIBLE );
                    }
                }
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        mBusySpinner.setVisibility(View.GONE);
                    }
                });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(SettingsContent.SettingItem item) {
        if (item.mUrl != null) {
            // Launch browser
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.mUrl));
            startActivity(intent);
        } else {

            int resId = item.mContentResId;
            switch (resId) {
                case R.string.setting_info: {
                    switchFragment(SettingsFragment.newInstance(SettingsContent.INFO_MENU));
                }
                break;
                case R.string.settings_reporting: {
                    switchFragment(SettingsFragment.newInstance(SettingsContent.PRIVACY_MENU));
                }
                break;
                case R.string.settings_notification: {

                }
                break;
                case R.string.settings_account: {
                    // Get username
                    // Get VPNProfile
                    VpnProfileDataSource dataSource = new VpnProfileDataSource(getApplicationContext());
                    dataSource.open();
                    List<VpnProfile> profiles = dataSource.getAllVpnProfiles();
                    dataSource.close();
                    if (profiles.size() > 0) {
                        VpnProfile vpnProfile = profiles.get(0);
                        String username = vpnProfile.getUsername();
                        String accountUrl = String.format(ACCOUNT_URL, username);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(accountUrl));
                        startActivity(intent);
                    }
                }
                break;
                case R.string.settings_restore: {

                }
                break;
                case R.string.setting_purge_database:{
                    mViewModel.deleteTrackers(this);
                }
                break;
                default: {

                }
            }
        }
    }

    private void switchFragment(Fragment aFragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_fragment, aFragment, "Fragment Two");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
