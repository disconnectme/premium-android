package pro.disconnect.me.trackers;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pro.disconnect.me.R;
import pro.disconnect.me.comms.models.Tracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Tracker} and makes a call to the
 * specified {@link OnTrackerListInteractionListener}.
 */
public class TrackerRecyclerViewAdapter extends RecyclerView.Adapter<TrackerRecyclerViewAdapter.ViewHolder> {
    public interface OnTrackerListInteractionListener {
        void onTrackerInteraction(Tracker item);
    }

    private List<Tracker> mValues;
    private final OnTrackerListInteractionListener mListener;

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("MM/dd @ hh:mm a");

    public TrackerRecyclerViewAdapter(OnTrackerListInteractionListener listener) {
        mListener = listener;
    }

    public void setTrackers(List<Tracker> aValues){
        mValues = aValues;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_tracker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.bind(mValues.get(position));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onTrackerInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues == null ? 0 : mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mTimeView;
        public final ImageView mStatusIcon;
        public Tracker mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mTimeView = (TextView) view.findViewById(R.id.time);
            mStatusIcon = (ImageView)view.findViewById(R.id.status);
        }

        public void bind(Tracker aTracker){
            mItem = aTracker;
            mTitleView.setText(aTracker.getDomain());

            mTimeView.setText(mDateFormat.format(aTracker.getDateTimeStamp()));
            mStatusIcon.setSelected(aTracker.getBlocked() == "");
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTimeView.getText() + "'";
        }
    }
}
