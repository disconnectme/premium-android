package pro.disconnect.me;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.strongswan.android.data.VpnProfile;

import pro.disconnect.me.comms.utils.Resource;

public class SplashActivity extends AppCompatActivity {
    private StartUpViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mViewModel = ViewModelProviders.of(this).get(StartUpViewModel.class);
        mViewModel.init(this);
        mViewModel.getVpnProfile().observe(this, new Observer<Resource<VpnProfile>>() {
            @Override
            public void onChanged(@Nullable Resource<VpnProfile> vpnProfileResource) {
                if ( vpnProfileResource.data == null ){
                    // Display error message
                    ErrorDialogFragment fragment = ErrorDialogFragment.newInstance();
                    fragment.show(getSupportFragmentManager(), "dialog");
                } else {
                    Intent incomingIntent = getIntent();
                    Uri uri = incomingIntent.getData();

                    Intent intent = new Intent(SplashActivity.this, TabbedActivity.class);
                    intent.setData(uri); // Pass on launch intent
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public static class ErrorDialogFragment extends DialogFragment {

        public static ErrorDialogFragment newInstance() {
            ErrorDialogFragment frag = new ErrorDialogFragment();
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.start_up_error_message)
                    .setPositiveButton(R.string.okay,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((SplashActivity)getActivity()).finish();
                                }
                            }
                    )
                    .create();
        }
    }
}
