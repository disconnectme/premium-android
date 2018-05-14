package pro.disconnect.me.feeds.tips;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.List;

import pro.disconnect.me.R;
import pro.disconnect.me.comms.CommsEngine;
import pro.disconnect.me.comms.models.Post;
import pro.disconnect.me.comms.utils.Resource;
import pro.disconnect.me.feeds.PostsFragment;
import pro.disconnect.me.feeds.PostsViewModel;
import pro.disconnect.me.feeds.TimeAgo;

public class TipsFragment extends PostsFragment {
    public static final String TIPS_VIDEO_LINKS = "https://s3.us-east-2.amazonaws.com/premium-app-content/Videos/";

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(PostsViewModel.class);
        viewModel.init(getContext(), CommsEngine.POST_TIPS);

        viewModel.getPosts().observe(this, mPostsObserver);
    }

    private Observer<Resource<List<Post>>> mPostsObserver = new Observer<Resource<List<Post>>>() {
        @Override
        public void onChanged(@Nullable Resource<List<Post>> posts) {
            if ( posts.status.equals(Resource.Status.ERROR)){
                // Display error message
                Toast.makeText(getContext(), R.string.posts_error_message, Toast.LENGTH_LONG).show();
            }

            // Update UI
            TipsRecyclerViewAdapter adapter = (TipsRecyclerViewAdapter)mAdapter;
            adapter.setPosts(posts.data);
            adapter.notifyDataSetChanged();

            refreshComplete();
        }
    };

    protected RecyclerView.Adapter createAdapter(){
        RequestManager glideRequestManager = Glide.with(this);
        TimeAgo timeAgo = new TimeAgo(getResources());
        TipsRecyclerViewAdapter adapter = new TipsRecyclerViewAdapter(glideRequestManager, this, timeAgo);
        return adapter;
    }

    public void onPostLink(int aId, String aUrl){
        if ( TIPS_VIDEO_LINKS.equals(aUrl)) {
            Intent openVideo = new Intent(Intent.ACTION_VIEW);
            openVideo.setDataAndType(Uri.parse(aUrl), "video/*");
            startActivity(openVideo);

            CommsEngine.getInstance(getContext()).markPostAsSeen(aId);
        } else {
            super.onPostLink(aId, aUrl);
        }
    }

    @Override
    public void onRefresh() {
        if ( viewModel.getPosts().getValue().status != Resource.Status.LOADING ){
            viewModel.refresh(CommsEngine.POST_TIPS);
            viewModel.getPosts().observe(this, mPostsObserver );
        }
    }
}
