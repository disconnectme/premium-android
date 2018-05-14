package pro.disconnect.me.trackers;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import pro.disconnect.me.R;
import pro.disconnect.me.comms.utils.Resource;
import pro.disconnect.me.comms.models.Tracker;

/**
 * A fragment representing a list of Items.
 * interface.
 */
public class TrackerFragment extends DialogFragment implements TrackerRecyclerViewAdapter.OnTrackerListInteractionListener {
    private static final double DATA_CONSTANT = 0.107;
    private static final double TIME_CONSTANT = 0.179;
    private static final int NUM_BUCKETS = 4;

    private TextView mBlockedTextView;
    private TextView mTimeTextView;
    private TextView mDataTextView;

    private static final int[] bucketIds = new int[]{R.id.day_bucket, R.id.week_bucket, R.id.month_bucket, R.id.all_bucket};
    private ArrayList<TextView> mBuckets = new ArrayList<>();

    private StatsViewModel mStatsViewModel;

    private TrackersViewModel viewModel;
    private TrackerRecyclerViewAdapter mAdapter;
    private View mListHolder;

    private LineChart mLineChart;

    private int mBucketType = StatsViewModel.DAY;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TrackerFragment() {
    }

    @SuppressWarnings("unused")
    public static TrackerFragment newInstance() {
        TrackerFragment fragment = new TrackerFragment();
        return fragment;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(TrackersViewModel.class);
        viewModel.init(getContext());

        viewModel.getTrackers().observe(this, new Observer<Resource<List<Tracker>>>() {
            @Override
            public void onChanged(@Nullable Resource<List<Tracker>> trackers) {
                // Update UI
                mAdapter.setTrackers(trackers.data);
                mAdapter.notifyDataSetChanged();
                mStatsViewModel.getBucket(mBucketType);
            }
        });

        mStatsViewModel = ViewModelProviders.of(this).get(StatsViewModel.class);
        mStatsViewModel.init(getContext());

        mStatsViewModel.getBuckets().observe(this, new Observer<List<TrackerBucket>>() {
            @Override
            public void onChanged(@Nullable List<TrackerBucket> aBuckets) {
                int total_trackers = 0;

                List<Entry> entries = new ArrayList<Entry>();
                int index = 0;
                for (TrackerBucket bucket : aBuckets ){
                    total_trackers += bucket.mCount;
                    entries.add(new Entry(index++, bucket.mCount));
                }

                double total_time = total_trackers * TIME_CONSTANT;
                double total_data = total_trackers * DATA_CONSTANT;

                mBlockedTextView.setText(String.format("%d", total_trackers ));
                mTimeTextView.setText(String.format("%.1f", total_time ));
                mDataTextView.setText(String.format("%.1f", total_data ));

                LineDataSet dataSet = new LineDataSet(entries, "");
                dataSet.setDrawFilled(true);
                int disconnectGreen = getResources().getColor(R.color.dark_green_disconnect);
                dataSet.setFillColor(disconnectGreen);
                dataSet.setColor(disconnectGreen);
                dataSet.setCircleColor(disconnectGreen);
                dataSet.setCircleColorHole(disconnectGreen);

                LineData lineData = new LineData(dataSet);
                mLineChart.setData(lineData);
                mLineChart.getAxisLeft().setDrawGridLines(false);
                mLineChart.getAxisLeft().setEnabled(false);
                mLineChart.getAxisRight().setDrawGridLines(false);
                mLineChart.getAxisRight().setEnabled(false);
                mLineChart.getDescription().setEnabled(false);
                mLineChart.setExtraOffsets(20, 0, 20, 0);

                XAxis xAxis = mLineChart.getXAxis();
                xAxis.setDrawGridLines(false);
                xAxis.setValueFormatter(new XAxisFormatter(aBuckets.get(0).mDate.getTime(),aBuckets.get(aBuckets.size()-1).mDate.getTime(), aBuckets.size() - 1 ));
                xAxis.setLabelCount(aBuckets.size() - 1);

                mLineChart.invalidate();
            }
        });

        mStatsViewModel.getBucket(mBucketType);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracker_list, container, false);
        mAdapter = new TrackerRecyclerViewAdapter(this);

        RecyclerView recyclerView = view.findViewById(R.id.list);
        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mAdapter);

        mListHolder = view.findViewById(R.id.list_holder);

        mBlockedTextView = view.findViewById(R.id.blocked);
        mTimeTextView = view.findViewById(R.id.time);
        mDataTextView = view.findViewById(R.id.data);

        // Style chart
        mLineChart  = (LineChart) view.findViewById(R.id.chart);
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0);

        mLineChart.getLegend().setEnabled(false);

        for ( int resId : bucketIds){
            TextView bucket = view.findViewById(resId);
            bucket.setOnClickListener(mBucketListener);
            mBuckets.add(bucket);
        }

        mBuckets.get(0).setSelected(true);

        return view;
    }

    private View.OnClickListener mBucketListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int selectedBucketId = view.getId();
            for ( int index = 0; index < NUM_BUCKETS; index++){
                TextView bucket = mBuckets.get(index);
                if (bucket.getId() == selectedBucketId ){
                    bucket.setSelected(true);
                    mStatsViewModel.getBucket(index);
                } else {
                    bucket.setSelected(false);
                }
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void onTrackerInteraction(Tracker item){
        IndividualTrackerFragment fragment = IndividualTrackerFragment.newInstance(item);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.individual_tracker, fragment);
        fragmentTransaction.commit();
    }

}
