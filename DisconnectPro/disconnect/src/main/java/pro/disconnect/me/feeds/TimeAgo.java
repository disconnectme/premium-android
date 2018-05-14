package pro.disconnect.me.feeds;


import android.content.res.Resources;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import pro.disconnect.me.R;

public class TimeAgo {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private final SimpleDateFormat mDateFormat;
    private final Resources mResources;
    
    public TimeAgo(Resources aResources){
        mResources = aResources;
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public String fromDateString(String aDateString) throws ParseException {
        long time = mDateFormat.parse(aDateString).getTime();
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return mResources.getString(R.string.just_now);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return mResources.getQuantityString(R.plurals.minutes_ago, 1, 1);
        } else if (diff < 50 * MINUTE_MILLIS) {
            int quantity = (int)(diff / MINUTE_MILLIS);
            return mResources.getQuantityString(R.plurals.minutes_ago, quantity, quantity);
        } else if (diff < 90 * MINUTE_MILLIS) {
            return mResources.getQuantityString(R.plurals.hours_ago, 1, 1);
        } else if (diff < 24 * HOUR_MILLIS) {
            int quantity = (int)(diff /  HOUR_MILLIS);
            return mResources.getQuantityString(R.plurals.hours_ago, quantity, quantity);
        } else if (diff < 48 * HOUR_MILLIS) {
            return mResources.getString(R.string.yesterday);
        } else {
            int quantity = (int)(diff / DAY_MILLIS );
            return mResources.getQuantityString(R.plurals.days_ago,quantity, quantity);
        }
    }
}
