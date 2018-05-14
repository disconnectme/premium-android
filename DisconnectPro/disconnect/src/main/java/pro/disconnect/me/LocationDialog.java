package pro.disconnect.me;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.widget.TextView;

import org.strongswan.android.data.VpnProfile;
import org.strongswan.android.data.VpnProfileDataSource;

import pro.disconnect.me.comms.ServerLocations;

/**
 * Created by Peter Mullarkey on 26/03/2018.
 */

public class LocationDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        TextView title = new TextView(getContext());
        title.setText("Location\nChange your preferred location");
        title.setGravity(Gravity.CENTER);

        builder.setCustomTitle(title);

        String[] locationList = ServerLocations.getInstance(getContext()).getLocationList();


        builder.setItems(locationList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Change location.
                String serverAddress = ServerLocations.getInstance(getContext()).getServerAddressByPosition(which);
                TabbedActivity activity = (TabbedActivity) getActivity();
                VpnProfile profile = activity.getProfile();
                profile.setGateway(serverAddress);
                profile.setRemoteId(String.format("CN=%s",serverAddress));

                VpnProfileDataSource dataSource = new VpnProfileDataSource(getContext());
                dataSource.open();
                dataSource.updateVpnProfile(profile);
                dataSource.close();

                // Refresh calling fragment
                getTargetFragment().onActivityResult(getTargetRequestCode(), 0, null);
            }
        }
        ).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
