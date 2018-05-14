package pro.disconnect.me.settings;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.List;

import pro.disconnect.me.R;
import pro.disconnect.me.comms.CommsEngine;
import pro.disconnect.me.comms.models.Post;
import pro.disconnect.me.comms.utils.Resource;
import pro.disconnect.me.feeds.PostsViewModel;
import pro.disconnect.me.feeds.alerts.AlertsRecyclerViewAdapter;
import pro.disconnect.me.settings.SettingsContent.SettingItem;

import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnSettingsListFragmentInteractionListener}
 * interface.
 */
public class SettingsFragment extends Fragment {
    private OnSettingsListFragmentInteractionListener mListener;

    private static final String ARG_SCREEN = "screen";

    private String mScreen;
    private ProgressBar mBusySpinner;

    public static SettingsFragment newInstance(String aScreen) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SCREEN, aScreen);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mScreen= getArguments().getString(ARG_SCREEN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            List<SettingItem> items = SettingsContent.SETTINGS_ITEMS.get(mScreen);

            recyclerView.setAdapter(new SettingRecyclerViewAdapter(items, mListener));
            DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), VERTICAL);
            itemDecor.setDrawable(getResources().getDrawable(R.drawable.white_divider));
            recyclerView.addItemDecoration(itemDecor);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingsListFragmentInteractionListener) {
            mListener = (OnSettingsListFragmentInteractionListener) context;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnSettingsListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(SettingItem item);
    }
}
