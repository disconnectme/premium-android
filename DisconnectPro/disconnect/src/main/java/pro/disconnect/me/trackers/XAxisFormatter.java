package pro.disconnect.me.trackers;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class XAxisFormatter implements IAxisValueFormatter {

    private long mStartDate;
    private long mEndDate;
    private SimpleDateFormat simpleDateFormat;
    private int mCount;

    public XAxisFormatter(long aStartDate, long aEndDate, int count) {
        mStartDate = aStartDate;
        mEndDate = aEndDate;
        mCount = count;

        if ( mEndDate - mStartDate <= 24 * 60 * 60 * 1000) {
            simpleDateFormat = new SimpleDateFormat("h:mm a");
        } else {
            simpleDateFormat = new SimpleDateFormat("MM/dd");
        }
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        long timeStamp = mStartDate + (long)((value / mCount) * ( mEndDate - mStartDate));
        Date date = new Date(timeStamp);
        String formattedValue = simpleDateFormat.format(date);
        return formattedValue;
    }
}