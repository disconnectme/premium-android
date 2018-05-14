package pro.disconnect.me.billing;


import android.content.Context;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

import pro.disconnect.me.R;

public class SubsItem {
    public String mDescription;
    public String mId;
    public boolean mSaving;
    public float mPrice;
    public String mFormattedPrice;
    public String mMonthlyPrice;
    public int mNumberMonths;

    private static ArrayList<SubsItem> mSubsItemsArray;

    public SubsItem(Resources aResources, int aValue, int aIdResId, boolean aSaving){
        mDescription = aResources.getQuantityString(R.plurals.months, aValue, aValue);
        mId = aResources.getString(aIdResId);
        mSaving = aSaving;
        mNumberMonths = aValue;
    }

    public static List<SubsItem> getSubsList(Context aContext){
        Resources res = aContext.getResources();
        if ( mSubsItemsArray == null ){
            mSubsItemsArray = new ArrayList<>();

            mSubsItemsArray.add(new SubsItem(res,12, R.string.product_id_1, true ));
            mSubsItemsArray.add(new SubsItem(res,6 , R.string.product_id_2, true));
            mSubsItemsArray.add(new SubsItem(res, 3, R.string.product_id_3, true ));
            mSubsItemsArray.add(new SubsItem(res, 1, R.string.product_id_4, false ));
        }

        return mSubsItemsArray;
    }
}
