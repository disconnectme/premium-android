package pro.disconnect.me.trackers;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import pro.disconnect.me.R;
import pro.disconnect.me.comms.CommsEngine;
import pro.disconnect.me.comms.models.Tracker;

import static pro.disconnect.me.comms.CommsEngine.getInstance;

public class IndividualTrackerFragment extends Fragment {
    private static final String ARG_TIMESTAMP = "tracker_timestamp";
    private static final String ARG_DOMAIN = "tracker_domain";
    private static final String ARG_STATUS = "tracker_status";

    private Date mTimestamp;
    private String mDomain;
    private String mStatus;

    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MM.dd.yy @ hh:mm a");

    public IndividualTrackerFragment() {
        // Required empty public constructor
    }

    public static IndividualTrackerFragment newInstance(Tracker aTracker) {
        IndividualTrackerFragment fragment = new IndividualTrackerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DOMAIN, aTracker.getDomain());
        args.putLong(ARG_TIMESTAMP, aTracker.getDateTimeStamp().getTime());
        args.putString(ARG_STATUS, aTracker.getBlocked() );
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDomain = getArguments().getString(ARG_DOMAIN);
            long timeStamp = getArguments().getLong(ARG_TIMESTAMP);
            mTimestamp = new Date(timeStamp);
            mStatus = getArguments().getString(ARG_STATUS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_individual_tracker, container, false);

        TextView domainView = view.findViewById(R.id.domain);
        TextView timestampView = view.findViewById(R.id.timestamp);
        TextView descriptionView = view.findViewById(R.id.description);

        domainView.setText(mDomain);
        timestampView.setText(getString(R.string.blocked_on,mSimpleDateFormat.format(mTimestamp)));

        String description = CommsEngine.getInstance(getContext()).getTrackerDescription(mDomain);
        if ( description != null ){
            descriptionView.setText(description);
        } else {
            descriptionView.setText(R.string.default_tracker_description);
        }

        timestampView.setSelected(mStatus.equals("true"));

        ImageView backButton = view.findViewById(R.id.circular_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go back
                getActivity().getSupportFragmentManager().beginTransaction().remove(IndividualTrackerFragment.this).commit();
            }
        });

        return view;
    }
}
