package pro.disconnect.me.settings;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;

import pro.disconnect.me.comms.database.DisconnectDatabase;
import pro.disconnect.me.comms.database.TrackersDao;

public class SettingsViewModel extends ViewModel {
    private MutableLiveData<Boolean> mTrackerPurged = new MutableLiveData<>();

    public LiveData<Boolean> getTrackersPurged() {
        return mTrackerPurged;
    }

    public void deleteTrackers(Context aContext){
        final Context appContext = aContext.getApplicationContext();
        mTrackerPurged.setValue(false);

        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                DisconnectDatabase database = Room.databaseBuilder(appContext,
                        DisconnectDatabase.class, "disconnect-database").build();
                TrackersDao dao = database.trackersDao();
                dao.deleteTrackers();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mTrackerPurged.setValue(true);
            }
        }.execute();
    }

}