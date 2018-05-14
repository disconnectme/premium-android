package pro.disconnect.me.feeds;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import java.util.List;

import pro.disconnect.me.comms.CommsEngine;
import pro.disconnect.me.comms.models.Post;
import pro.disconnect.me.comms.models.Tracker;
import pro.disconnect.me.comms.utils.Resource;

public class PostsViewModel extends ViewModel {
    private LiveData<Resource<List<Post>>> mPosts;
    private CommsEngine mCommsEngine;

    public LiveData<Resource<List<Post>>> getPosts() {
        return mPosts;
    }

    public void init(Context aContext, String aSourceType){
        if ( mCommsEngine == null ) {
            mCommsEngine = CommsEngine.getInstance(aContext);
            mPosts = mCommsEngine.getNewsItems(aSourceType);
        }
    }

    public void refresh(String aSourceType){
        mPosts = mCommsEngine.getNewsItems(aSourceType);
    }
}

