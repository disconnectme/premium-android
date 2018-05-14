package pro.disconnect.me;

import android.app.Service;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;

import org.strongswan.android.data.VpnProfile;
import org.strongswan.android.data.VpnProfileDataSource;
import org.strongswan.android.logic.VpnStateService;
import org.strongswan.android.ui.MainActivity;

import java.util.List;

import pro.disconnect.me.billing.BillingManager;
import pro.disconnect.me.billing.SubsItem;
import pro.disconnect.me.billing.UpgradeFragment;
import pro.disconnect.me.comms.CommsEngine;
import pro.disconnect.me.comms.utils.Resource;
import pro.disconnect.me.feeds.alerts.AlertsFragment;
import pro.disconnect.me.feeds.news.NewsFragment;
import pro.disconnect.me.feeds.tips.TipsFragment;
import pro.disconnect.me.settings.SettingsActivity;

public class TabbedActivity extends MainActivity implements VpnStateService.VpnStateListener,
        UpgradeFragment.OnSubscriptionFragmentListener, ProtectionFragment.ProtectionFragmentListener {
    private static final String DISCONNECT_SCHEME = "disconnectme";

    private RadioGroup mBottomNavRadioGroup;
    private VpnProfile mProfile;
    private CommsEngine mEngine;
    private TextView mTitleView;
    private RadioButton mProtectionIcon;
    private View mBusyView;

    private UpgradeViewModel mUpgradeViewModel;
    private SharedPreferences mSharedPreferences;
    private UpgradeFragment mUpgradeFragment;
    private boolean mUpgradeFragmentShowing = false;

    private VpnStateService mService;
    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mService = ((VpnStateService.LocalBinder)service).getService();
            mService.registerListener(TabbedActivity.this);
            stateChanged();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mSharedPreferences = getSharedPreferences("disconnect", MODE_PRIVATE );

        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowHomeEnabled(true);
        bar.setIcon(R.drawable.d_logo);
        View view = getLayoutInflater().inflate(R.layout.actionbar_title, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);

        mTitleView = (TextView) view.findViewById(R.id.actionbar_title);

        getSupportActionBar().setCustomView(view,params);
        getSupportActionBar().setDisplayShowCustomEnabled(true); //show custom title
        getSupportActionBar().setDisplayShowTitleEnabled(false); //hide the default title

        // Get VPNProfile
        VpnProfileDataSource dataSource = new VpnProfileDataSource(TabbedActivity.this);
        dataSource.open();
        List<VpnProfile> profiles = dataSource.getAllVpnProfiles();
        mProfile = profiles.get(0);
        dataSource.close();

        mBusyView = findViewById(R.id.busy_container);
        ProgressBar spinner = (ProgressBar)findViewById(R.id.progress_bar);
        spinner.getIndeterminateDrawable().setColorFilter(Color.GRAY, android.graphics.PorterDuff.Mode.MULTIPLY);

        mBottomNavRadioGroup = (RadioGroup) findViewById(R.id.bottom_navigation);
        mBottomNavRadioGroup.setOnCheckedChangeListener(mOnBottomNavGroupCheckChangeListener);
        mBottomNavRadioGroup.check(R.id.navigation_protection);
        mProtectionIcon = findViewById(R.id.navigation_protection);
        switchToProtectionFragment();

        Context context = getApplicationContext();
        context.bindService(new Intent(context, VpnStateService.class),
                mServiceConnection, Service.BIND_AUTO_CREATE);

        Intent intent = getIntent();
        if ( intent != null && intent.getData() != null ){
            handleIntent(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        return true;
    }

    private RadioGroup.OnCheckedChangeListener mOnBottomNavGroupCheckChangeListener
            = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch ( checkedId ){
                case R.id.navigation_news:
                    switchToNewsFragment();
                    break;
                case R.id.navigation_alerts:
                    switchToAlertsFragment();
                    break;
                case R.id.navigation_learn:
                    switchToLearnFragment();
                    break;
                case R.id.navigation_protection:
                    switchToProtectionFragment();
                    break;
            }
        }
    };

    public void switchToNewsFragment() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new NewsFragment()).commitAllowingStateLoss();
        mTitleView.setText(R.string.news_title);
    }

    public void switchToAlertsFragment() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new AlertsFragment()).commitAllowingStateLoss();
        mTitleView.setText(R.string.alerts_title);
    }

    public void switchToLearnFragment() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new TipsFragment()).commitAllowingStateLoss();
        mTitleView.setText(R.string.learn_title);
    }

    public void switchToProtectionFragment() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new ProtectionFragment()).commitAllowingStateLoss();
        mTitleView.setText(R.string.protection_title);

        if ( !mSharedPreferences.getBoolean(CommsEngine.UPGRADED, false)){
            // Show upgrade panel
            showUpgradeFragment();
        }
    }

    public void showUpgradeFragment() {
        FragmentManager manager = getSupportFragmentManager();
        if (!mUpgradeFragmentShowing) {
            if ( mUpgradeFragment == null ){
                mUpgradeFragment = new UpgradeFragment();
            }

            FragmentTransaction transaction = manager.beginTransaction();
            mUpgradeFragment.show(transaction, "txn_tag");
            mUpgradeFragmentShowing = true;
        }
    }

    public void startVpn(){
        startVpnProfile(mProfile, true);
    }

    public VpnProfile getProfile(){
        return mProfile;
    }

    @Override
    public void stateChanged() {
        VpnStateService.State state = mService.getState();
        Drawable top;
        if ( state == VpnStateService.State.CONNECTED ){
            top = getResources().getDrawable(R.drawable.icn_protection_blocking);
        } else {
            top = getResources().getDrawable(R.drawable.icn_protection_off);
        }

        mProtectionIcon.setCompoundDrawablesWithIntrinsicBounds(null, top , null, null);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (mService != null)
        {
            mService.registerListener(this);
            stateChanged();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mService != null)
        {
            mService.unregisterListener(this);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mService != null)
        {
            getApplicationContext().unbindService(mServiceConnection);
        }
    }

    private void handleIntent(Intent aIntent){
        Uri uri = aIntent.getData();
        String scheme = uri.getScheme();
        if ( DISCONNECT_SCHEME.equals(scheme)){
            String code = uri.getQueryParameter("code");

            mUpgradeViewModel = ViewModelProviders.of(this).get(UpgradeViewModel.class);
            mUpgradeViewModel.applyUpgradeCode(this, code).observe(this, new Observer<Resource<String>>() {
                @Override
                public void onChanged(@Nullable Resource<String> serverResponse) {
                    switch ( serverResponse.status){
                        case LOADING:{
                            mBusyView.setVisibility(View.VISIBLE);
                        }
                        break;
                        case SUCCESS:{
                            mBusyView.setVisibility(View.GONE);
                            Toast.makeText(TabbedActivity.this, serverResponse.data, Toast.LENGTH_LONG).show();
                        }
                        break;
                        case ERROR:{
                            mBusyView.setVisibility(View.GONE);
                            Toast.makeText(TabbedActivity.this, R.string.upgrade_error_message, Toast.LENGTH_LONG).show();
                        }

                    }
                }
            });

        }
    }


    @Override
    public void onSubscriptionFragmentDismissed() {
        mUpgradeFragmentShowing = false;
    }

    @Override
    public boolean isUpgraded() {
        if ( !mSharedPreferences.getBoolean(CommsEngine.UPGRADED, false)){
            // Show upgrade panel
            showUpgradeFragment();
            return false;
        }

        return true;
    }
}
