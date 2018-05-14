package pro.disconnect.me;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import org.strongswan.android.data.VpnProfile;

import pro.disconnect.me.comms.CommsEngine;

import pro.disconnect.me.comms.utils.Resource;

public class StartUpViewModel extends ViewModel {
    private LiveData<Resource<VpnProfile>> mVpnProfile;
    private CommsEngine mCommsEngine;

    public LiveData<Resource<VpnProfile>> getVpnProfile() {
        return mVpnProfile;
    }

    public void init(Context aContext) {
        if (mCommsEngine == null) {
            mCommsEngine = CommsEngine.getInstance(aContext);
            mVpnProfile = mCommsEngine.start();
        }
    }
}