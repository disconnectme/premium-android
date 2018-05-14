package pro.disconnect.me.feeds;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import pro.disconnect.me.R;
import pro.disconnect.me.SplashActivity;
import pro.disconnect.me.comms.CommsEngine;
import pro.disconnect.me.comms.models.Post;
import pro.disconnect.me.comms.utils.Resource;
import pro.disconnect.me.feeds.news.NewsRecyclerViewAdapter;

import java.util.List;

/**
 * A fragment representing a list of posts.
 */
public abstract class PostsFragment extends Fragment implements OnPostInteractionListener, SwipeRefreshLayout.OnRefreshListener {
    protected PostsViewModel viewModel;
    protected RecyclerView.Adapter mAdapter;
    protected SwipeRefreshLayout mSwipeContainer;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PostsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.list);
        Context context = view.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = createAdapter();
        recyclerView.setAdapter(mAdapter);

        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeContainer.setOnRefreshListener(this);

        mSwipeContainer.setRefreshing(true);

        return view;
    }

    abstract protected RecyclerView.Adapter createAdapter();

    public void onPostLink(int aId, String aUrl){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(aUrl));
        startActivity(browserIntent);

        CommsEngine.getInstance(getContext()).markPostAsSeen(aId);
    }

    public void refreshComplete(){
        mSwipeContainer.setRefreshing(false);
    }
}
