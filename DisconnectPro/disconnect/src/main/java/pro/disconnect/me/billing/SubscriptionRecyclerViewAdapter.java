package pro.disconnect.me.billing;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pro.disconnect.me.R;

import java.util.List;


public class SubscriptionRecyclerViewAdapter extends RecyclerView.Adapter<SubscriptionRecyclerViewAdapter.ViewHolder> {

    private List<SubsItem> mValues;
    private final UpgradeFragment.OnSubscriptionAdapterListener mListener;

    public SubscriptionRecyclerViewAdapter(UpgradeFragment.OnSubscriptionAdapterListener listener) {
        mListener = listener;
    }

    public void setSkuDetails(List<SubsItem> aValues){
        mValues = aValues;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_subscription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mDescriptionView.setText(mValues.get(position).mDescription);
        holder.mPriceView.setVisibility(holder.mItem.mSaving ? View.VISIBLE : View.GONE);

        if ( true || holder.mItem.mPrice > 0 ) {
            holder.mMonthlyPriceView.setText(holder.mItem.mMonthlyPrice);
            holder.mPriceView.setText(holder.mItem.mFormattedPrice);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        // Notify the active callbacks interface that an item has been selected.
                        mListener.onSubscriptionListener(holder.mItem);
                    }
                }
            });
        } else {
            holder.mMonthlyPriceView.setText("--");
            holder.mPriceView.setText("test");
            holder.mView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return mValues == null ? 0 : mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mDescriptionView;
        public final TextView mPriceView;
        public final TextView mMonthlyPriceView;
        public SubsItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDescriptionView = (TextView) view.findViewById(R.id.description);
            mMonthlyPriceView = (TextView) view.findViewById(R.id.price_per_month);
            mPriceView = (TextView) view.findViewById(R.id.price);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDescriptionView.getText() + "'";
        }
    }
}
