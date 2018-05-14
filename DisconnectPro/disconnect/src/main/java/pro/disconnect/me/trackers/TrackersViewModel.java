package pro.disconnect.me.trackers;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import java.util.List;

import pro.disconnect.me.comms.CommsEngine;
import pro.disconnect.me.comms.utils.Resource;
import pro.disconnect.me.comms.models.Tracker;

public class TrackersViewModel  extends ViewModel {
    private LiveData<Resource<List<Tracker>>> mTrackers;
    private CommsEngine mCommsEngine;

    public LiveData<Resource<List<Tracker>>> getTrackers() {
        return mTrackers;
    }

    public void init(Context aContext){
        if ( mCommsEngine == null ){
            mCommsEngine = CommsEngine.getInstance(aContext);
            mTrackers = mCommsEngine.getBlockedTrackers(0);
        }
    }
}

