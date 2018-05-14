package pro.disconnect.me.trackers;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import pro.disconnect.me.comms.database.DisconnectDatabase;
import pro.disconnect.me.comms.database.TrackersDao;
import pro.disconnect.me.comms.models.Tracker;
import pro.disconnect.me.comms.utils.Resource;

import static android.os.AsyncTask.Status.FINISHED;

public class StatsViewModel extends ViewModel {
    public static final int DAY = 0;
    public static final int WEEK = 1;
    public static final int MONTH = 2;
    public static final int ALL = 3;

    private static final int[] sGroupBySeconds = new int[]{14400, 86400, 518400, 86400};
    private static final int[] sNumOfBuckets = new int[]{6, 7, 5, 5};

    private MutableLiveData<List<TrackerBucket>> mBuckets = new MutableLiveData<>();
    private DisconnectDatabase mDatabase;
    private AsyncBucketFetcher mAsyncTask;

    public LiveData<List<TrackerBucket>> getBuckets(){
        return mBuckets;
    }

    public void init(Context aContext ){
        final Context appContext = aContext.getApplicationContext();
        mDatabase = Room.databaseBuilder(appContext,
                DisconnectDatabase.class, "disconnect-database").build();
    }

    public void getBucket(int aType) {
        if ( mAsyncTask != null && mAsyncTask.getStatus() != FINISHED){
            mAsyncTask.cancel(true);
        }

        mAsyncTask = new AsyncBucketFetcher(mDatabase, mBuckets);
        mAsyncTask.execute(aType);
    }

    private static class AsyncBucketFetcher extends AsyncTask<Integer, Void, List<TrackerBucket>> {
        private DisconnectDatabase mDatabase;
        private MutableLiveData<List<TrackerBucket>> mBuckets;

        public AsyncBucketFetcher(DisconnectDatabase aDatabase, MutableLiveData<List<TrackerBucket>> aBuckets ){
            mDatabase = aDatabase;
            mBuckets = aBuckets;
        }

        @Override
        protected List<TrackerBucket> doInBackground(Integer... integers) {
            int type = integers[0];

            TrackersDao dao = mDatabase.trackersDao();

            ArrayList<TrackerBucket> buckets = new ArrayList<>();

            long currentTime = System.currentTimeMillis();
            int numberOfBuckets = sNumOfBuckets[type];
            int bucketLength = sGroupBySeconds[type] * 1000; // Convert to milliseconds
            for ( int index = 0; index < numberOfBuckets; index++){
                int count = dao.getTrackersBetween(currentTime - bucketLength, currentTime);
                buckets.add(0, new TrackerBucket(currentTime, count));
                currentTime = currentTime - bucketLength;
            }

            return buckets;
        }

        @Override
        protected void onPostExecute(List<TrackerBucket> aBuckets) {
            super.onPostExecute(aBuckets);
            mBuckets.setValue(aBuckets);
        }
    }
}
