package pro.disconnect.me.billing;

import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.SkuDetails;

import org.strongswan.android.data.VpnProfile;

import pro.disconnect.me.R;
import pro.disconnect.me.TabbedActivity;
import pro.disconnect.me.comms.CommsEngine;
import pro.disconnect.me.comms.utils.Resource;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;

/**
 * A fragment representing a list of subscriptions.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnSubscriptionFragmentListener}
 * interface.
 */
public class UpgradeFragment extends DialogFragment {
    public interface OnSubscriptionFragmentListener {
        void onSubscriptionFragmentDismissed();
    }

    public interface OnSubscriptionAdapterListener {
        void onSubscriptionListener(SubsItem item);
    }

    private OnSubscriptionFragmentListener mListener;
    private SkusViewModel mViewModel;
    private SubscriptionRecyclerViewAdapter mAdapter;
    private List<SubsItem> mSubsItems;


    private View mBusyView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UpgradeFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
            d.getWindow().getAttributes().windowAnimations = R.style.UpDownAnimation;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.full_screen);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upgrade, container, false);

        // Set the adapter
        final RecyclerView recyclerView = view.findViewById(R.id.list);
        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Create sub entries
        mSubsItems = SubsItem.getSubsList(getContext());
        mAdapter = new SubscriptionRecyclerViewAdapter(mAdapterListener);
        mAdapter.setSkuDetails(mSubsItems);
        recyclerView.setAdapter(mAdapter);

        View closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        TextView termsAndConditions = view.findViewById(R.id.terms_and_conditions);
        termsAndConditions.setMovementMethod(LinkMovementMethod.getInstance());

        mBusyView = view.findViewById(R.id.busy_container);
        ProgressBar spinner = (ProgressBar)view.findViewById(R.id.progress_bar);
        spinner.getIndeterminateDrawable().setColorFilter(Color.GRAY, android.graphics.PorterDuff.Mode.MULTIPLY);

        view.findViewById(R.id.cta_image).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                v.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(SkusViewModel.class);
        mViewModel.init(getContext(), CommsEngine.POST_NEWS);

        mViewModel.getSkuDetails().observe(this, new Observer<Resource<List<SkuDetails>>>() {
            @Override
            public void onChanged(@Nullable Resource<List<SkuDetails>> listResource) {
                // Match prices with subs items
                if (listResource.status == Resource.Status.ERROR ){
                    Toast.makeText(getContext(), "Unable to retrieve pricing information. Please check your internet connection", Toast.LENGTH_LONG).show();
                } else if ( listResource.status == Resource.Status.SUCCESS){
                    if ( listResource.data.size() == 0 ){
                        Toast.makeText(getContext(), "Unable to retrieve pricing information. Please check your internet connection", Toast.LENGTH_LONG).show();
                    } else {
                        // Find format for currency
                        SkuDetails firstSku = listResource.data.get(0);
                        String currencyCode = firstSku.getPriceCurrencyCode();
                        Currency currency = Currency.getInstance(currencyCode);
                        NumberFormat nf = NumberFormat.getCurrencyInstance();
                        nf.setCurrency(currency);

                        // Attempt to match prices
                        for (SkuDetails sku : listResource.data) {
                            String id = sku.getSku();
                            for (SubsItem subsItem : mSubsItems) {
                                if (subsItem.mId.equals(id)) {
                                    subsItem.mFormattedPrice = sku.getPrice();
                                    float unitPrice = sku.getPriceAmountMicros() / subsItem.mNumberMonths;
                                    subsItem.mPrice = unitPrice / 1000000;
                                    String monthlyPrice = nf.format(subsItem.mPrice);
                                    subsItem.mMonthlyPrice = String.format("%s / month", monthlyPrice);
                                }
                            }
                        }

                        mAdapter.setSkuDetails(mSubsItems);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSubscriptionFragmentListener) {
            mListener = (OnSubscriptionFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSubscriptionFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if ( mListener != null ){
            mListener.onSubscriptionFragmentDismissed();
        }
    }

    private OnSubscriptionAdapterListener mAdapterListener = new OnSubscriptionAdapterListener() {

        @Override
        public void onSubscriptionListener(SubsItem item) {
            // An item has been selected for purchase
            LiveData<Resource<VpnProfile>> liveData = mViewModel.startPurchaseFlow(getActivity(), item.mId);
            liveData.observe(UpgradeFragment.this, new Observer<Resource<VpnProfile>>() {
                @Override
                public void onChanged(@Nullable Resource<VpnProfile> vpnProfileResource) {
                    switch (vpnProfileResource.status){
                        case SUCCESS:{
                            mBusyView.setVisibility(View.GONE);
                            mListener.onSubscriptionFragmentDismissed();
                            dismiss();
                        }
                        break;
                        case ERROR:{
                            mBusyView.setVisibility(View.GONE);
                            if ( !"".equals(vpnProfileResource.message) ) {
                                Toast.makeText(getContext(), vpnProfileResource.message, Toast.LENGTH_LONG).show();
                            }
                        }
                        break;
                        case LOADING:{
                            mBusyView.setVisibility(View.VISIBLE);
                        }
                        break;
                    }
                }
            });
        }
    };
}
