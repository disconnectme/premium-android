package pro.disconnect.me.billing;


import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import com.android.billingclient.api.SkuDetails;

import org.strongswan.android.data.VpnProfile;

import java.util.List;

import pro.disconnect.me.comms.CommsEngine;
import pro.disconnect.me.comms.models.Post;
import pro.disconnect.me.comms.utils.Resource;

public class SkusViewModel extends ViewModel {
    private LiveData<Resource<List<SkuDetails>>> mSkuDetails;
    private LiveData<Resource<VpnProfile>> mVpnProfile;
    private CommsEngine mCommsEngine;

    public LiveData<Resource<List<SkuDetails>>> getSkuDetails() {
        return mSkuDetails;
    }
    public LiveData<Resource<VpnProfile>> getPurchaseResource() {
        return mVpnProfile;
    }

    public void init(Context aContext, String aSourceType){
        if ( mCommsEngine == null ) {
            mCommsEngine = CommsEngine.getInstance(aContext);
            mSkuDetails = mCommsEngine.fetchProductInformation();
        }
    }

    public LiveData<Resource<VpnProfile>> startPurchaseFlow(Activity aActivity, String aProductId){
        mVpnProfile = mCommsEngine.initiatePurchaseFlow(aActivity, aProductId);
        return mVpnProfile;
    }
}

