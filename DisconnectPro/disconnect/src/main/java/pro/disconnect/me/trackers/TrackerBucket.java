package pro.disconnect.me.trackers;

import java.util.Date;

public class TrackerBucket {
    public Date mDate;
    public int mCount;

    public TrackerBucket(long aTimeStamp, int aCount){
        mDate = new Date(aTimeStamp);
        mCount = aCount;
    }
}
