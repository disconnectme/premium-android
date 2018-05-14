package pro.disconnect.me.settings;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pro.disconnect.me.R;
import pro.disconnect.me.settings.SettingsFragment.OnSettingsListFragmentInteractionListener;
import pro.disconnect.me.settings.SettingsContent.SettingItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link SettingItem} and makes a call to the
 * specified {@link OnSettingsListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class SettingRecyclerViewAdapter extends RecyclerView.Adapter<SettingRecyclerViewAdapter.ViewHolder> {

    private final List<SettingItem> mValues;
    private final OnSettingsListFragmentInteractionListener mListener;

    public SettingRecyclerViewAdapter(List<SettingItem> items, OnSettingsListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_setting, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        SettingItem item = mValues.get(position);
        holder.mItem = item;
        if (item.mIconResId > 0) {
            holder.mIcon.setImageResource(item.mIconResId);
            holder.mIcon.setVisibility(View.VISIBLE);
        } else {
            holder.mIcon.setVisibility(View.GONE);
        }
        holder.mContentView.setText(item.mContentResId);
        holder.mMoreIcon.setVisibility(item.mHasMore ? View.VISIBLE : View.INVISIBLE);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mIcon;
        public final TextView mContentView;
        public final ImageView mMoreIcon;
        public SettingItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIcon = (ImageView) view.findViewById(R.id.icon);
            mContentView = (TextView) view.findViewById(R.id.content);
            mMoreIcon = (ImageView) view.findViewById(R.id.more);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
