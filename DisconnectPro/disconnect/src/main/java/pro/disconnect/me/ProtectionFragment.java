package pro.disconnect.me;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.airbnb.lottie.LottieAnimationView;

import org.strongswan.android.data.VpnProfile;
import org.strongswan.android.logic.VpnStateService;
import org.strongswan.android.logic.imc.ImcState;
import org.strongswan.android.logic.imc.RemediationInstruction;
import org.strongswan.android.ui.LogActivity;
import org.strongswan.android.ui.RemediationInstructionsActivity;
import org.strongswan.android.ui.RemediationInstructionsFragment;

import java.util.ArrayList;
import java.util.List;

import pro.disconnect.me.comms.ServerLocations;
import pro.disconnect.me.trackers.TrackerFragment;

import static org.strongswan.android.logic.VpnStateService.State.DISABLED;

public class ProtectionFragment extends Fragment implements VpnStateService.VpnStateListener {
    public interface ProtectionFragmentListener {
        public boolean isUpgraded();
    }

    private static final String KEY_ERROR_CONNECTION_ID = "error_connection_id";
    private static final String KEY_DISMISSED_CONNECTION_ID = "dismissed_connection_id";

    private Button mLocationButton;
    private ImageView mStatsButton;
    private LottieAnimationView mActionButton;

    private ImageView mParticleCircle;
    private ImageView mNodesBackground;
    private ImageView mCircularBackground;

    private ServerLocations mServerLocations;

    private AlertDialog mErrorDialog;

    private boolean mReconnect = false;
    private int mShortAnimationDuration;

    private ProtectionFragmentListener mListener;

    private long mErrorConnectionID;
    private long mDismissedConnectionID;
    private VpnStateService mService;
    private ProgressBar mSpinner;

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
            mService.registerListener(ProtectionFragment.this);
            updateView();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

		/* bind to the service only seems to work from the ApplicationContext */
        Context context = getActivity().getApplicationContext();
        context.bindService(new Intent(context, VpnStateService.class),
                mServiceConnection, Service.BIND_AUTO_CREATE);

        mErrorConnectionID = 0;
        mDismissedConnectionID = 0;
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_ERROR_CONNECTION_ID))
        {
            mErrorConnectionID = (Long)savedInstanceState.getSerializable(KEY_ERROR_CONNECTION_ID);
            mDismissedConnectionID = (Long)savedInstanceState.getSerializable(KEY_DISMISSED_CONNECTION_ID);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putSerializable(KEY_ERROR_CONNECTION_ID, mErrorConnectionID);
        outState.putSerializable(KEY_DISMISSED_CONNECTION_ID, mDismissedConnectionID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.protection_layout, null);

        mActionButton = view.findViewById(R.id.connection_animation_view);
        mActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (mListener.isUpgraded()) {
                    if (mService != null) {
                        VpnStateService.State state = mService.getState();
                        if (state == VpnStateService.State.CONNECTING ||
                                state == VpnStateService.State.CONNECTED) {
                            mService.disconnect();
                        } else {
                            // Connect VPN
                            TabbedActivity activity = (TabbedActivity) getActivity();
                            activity.startVpn();
                        }
                    }
                }
            }
        });


        mLocationButton = view.findViewById(R.id.location_button);
        mLocationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mListener.isUpgraded()) {
                    DialogFragment newFragment = new LocationDialog();
                    newFragment.setTargetFragment(ProtectionFragment.this, 0);
                    newFragment.show(getFragmentManager(), "location");
                }
            }
        });

        mStatsButton = view.findViewById(R.id.stats_button);
        mStatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener.isUpgraded()) {
                    DialogFragment newFragment = new TrackerFragment();
                    newFragment.show(getFragmentManager(), "trackers");
                }
            }
        });

        mServerLocations = ServerLocations.getInstance(getContext());

        mParticleCircle = view.findViewById(R.id.particle_circle);
        mNodesBackground = view.findViewById(R.id.nodes_background);
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_longAnimTime);
        mCircularBackground = view.findViewById(R.id.circular_background);


        mSpinner = (ProgressBar)view.findViewById(R.id.progressBar1);
        mSpinner.getIndeterminateDrawable().setColorFilter(Color.GRAY, android.graphics.PorterDuff.Mode.MULTIPLY);
        mSpinner.setIndeterminate(true);
        mSpinner.setVisibility(View.INVISIBLE);

        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (mService != null)
        {
            mService.registerListener(this);
            updateView();
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
        hideErrorDialog();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mService != null)
        {
            getActivity().getApplicationContext().unbindService(mServiceConnection);
        }
    }

    @Override
    public void stateChanged()
    {
        if ( mService != null && mReconnect && mService.getState() == DISABLED){
            mReconnect = false;
            TabbedActivity activity = (TabbedActivity) getActivity();
            activity.startVpn();
        }

        updateView();
    }

    public void updateView() {
        long connectionID = mService.getConnectionID();
        VpnStateService.State state = mService.getState();
        VpnStateService.ErrorState error = mService.getErrorState();
        ImcState imcState = mService.getImcState();
        String location = "";

        if (getActivity() == null) {
            return;
        }

        TabbedActivity activity = (TabbedActivity) getActivity();
        VpnProfile profile = activity.getProfile();
        if (profile != null ) {
            location = mServerLocations.getLocationByServerAddress(profile.getGateway());
            mLocationButton.setText(location);
        }

        if (reportError(connectionID, location, error, imcState)) {
            return;
        }

        switch (state) {
            case DISABLED:
                connectionAnimation(false);
                mLocationButton.setSelected(false);
                mSpinner.setVisibility(View.INVISIBLE);
                break;
            case CONNECTING:
                mSpinner.setVisibility(View.VISIBLE);
                break;
            case CONNECTED:
                connectionAnimation(true);
                mLocationButton.setSelected(true);
                mSpinner.setVisibility(View.INVISIBLE);
                break;
            case DISCONNECTING:
                mSpinner.setVisibility(View.VISIBLE);
                break;
                default:
                    mSpinner.setVisibility(View.INVISIBLE);


        }
    }


    private void connectionAnimation(final boolean aConnect) {
        View firstView;
        View secondView;
        float start;
        float end;
        if ( aConnect ){
            firstView = mNodesBackground;
            secondView = mParticleCircle;
            start = 0f;
            end = 1f;
        } else {
            secondView = mNodesBackground;
            firstView = mParticleCircle;
            start = 1f;
            end = 0f;
        }

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        secondView.setAlpha(0f);
        secondView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        secondView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity.
        final View fFirstView = firstView;
        firstView.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fFirstView.setVisibility(View.INVISIBLE);
                        mCircularBackground.setSelected(!aConnect);
                    }
                });

        if (mActionButton.getProgress() != end ) {
            ValueAnimator animator = ValueAnimator.ofFloat(start, end);
            animator.setDuration(mShortAnimationDuration);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float value = (float) valueAnimator.getAnimatedValue();
                    mActionButton.setProgress(value);
                }
            });
            animator.start();
        }
    }

    private boolean reportError(long connectionID, String name, VpnStateService.ErrorState error, ImcState imcState)
    {
        if (connectionID > mDismissedConnectionID)
        {	/* report error if it hasn't been dismissed yet */
            mErrorConnectionID = connectionID;
        }
        else
        {	/* ignore all other errors */
            error = VpnStateService.ErrorState.NO_ERROR;
        }
        if (error == VpnStateService.ErrorState.NO_ERROR)
        {
            hideErrorDialog();
            return false;
        }
        else if (mErrorDialog != null)
        {	/* we already show the dialog */
            return true;
        }

        switch (error)
        {
            case AUTH_FAILED:
                if (imcState == ImcState.BLOCK)
                {
                    showErrorDialog(org.strongswan.android.R.string.error_assessment_failed);
                }
                else
                {
                    showErrorDialog(org.strongswan.android.R.string.error_auth_failed);
                }
                break;
            case PEER_AUTH_FAILED:
                showErrorDialog(org.strongswan.android.R.string.error_peer_auth_failed);
                break;
            case LOOKUP_FAILED:
                showErrorDialog(org.strongswan.android.R.string.error_lookup_failed);
                break;
            case UNREACHABLE:
                showErrorDialog(org.strongswan.android.R.string.error_unreachable);
                break;
            default:
                showErrorDialog(org.strongswan.android.R.string.error_generic);
                break;
        }
        return true;
    }

    private void hideErrorDialog()
    {
        if (mErrorDialog != null)
        {
            mErrorDialog.dismiss();
            mErrorDialog = null;
        }
    }

    private void clearError()
    {
        if (mService != null)
        {
            mService.disconnect();
        }
        mDismissedConnectionID = mErrorConnectionID;
        updateView();
    }

    private void showErrorDialog(int textid)
    {
        final List<RemediationInstruction> instructions = mService.getRemediationInstructions();
        final boolean show_instructions = mService.getImcState() == ImcState.BLOCK && !instructions.isEmpty();
        int text = show_instructions ? org.strongswan.android.R.string.show_remediation_instructions : org.strongswan.android.R.string.show_log;

        mErrorDialog = new AlertDialog.Builder(getActivity())
                .setMessage(getString(org.strongswan.android.R.string.error_introduction) + " " + getString(textid))
                .setCancelable(false)
                .setNeutralButton(text, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        clearError();
                        dialog.dismiss();
                        Intent intent;
                        if (show_instructions)
                        {
                            intent = new Intent(getActivity(), RemediationInstructionsActivity.class);
                            intent.putParcelableArrayListExtra(RemediationInstructionsFragment.EXTRA_REMEDIATION_INSTRUCTIONS,
                                    new ArrayList<RemediationInstruction>(instructions));
                        }
                        else
                        {
                            intent = new Intent(getActivity(), LogActivity.class);
                        }
                        startActivity(intent);
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        clearError();
                        dialog.dismiss();
                    }
                }).create();
        mErrorDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                mErrorDialog = null;
            }
        });
        mErrorDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateLocation();
    }

    private void updateLocation(){
        updateView();

        if (mService != null){
            VpnStateService.State state = mService.getState();
            if (state == VpnStateService.State.CONNECTING ||
                    state == VpnStateService.State.CONNECTED) {
                mService.disconnect();

                // Reconnect
                mReconnect = true;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProtectionFragmentListener) {
            mListener = (ProtectionFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ProtectionFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
