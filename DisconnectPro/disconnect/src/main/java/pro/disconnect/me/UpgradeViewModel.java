package pro.disconnect.me;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;


import pro.disconnect.me.comms.CommsEngine;
import pro.disconnect.me.comms.utils.Resource;

public class UpgradeViewModel extends ViewModel{
    private LiveData<Resource<String>> mUpgradeStatus;
    private CommsEngine mCommsEngine;

    public LiveData<Resource<String>> applyUpgradeCode(Context aContext, String aUpgradeCode){
        mCommsEngine = CommsEngine.getInstance(aContext);
        return mCommsEngine.applyUpgradeCode(aUpgradeCode);
    }
}
