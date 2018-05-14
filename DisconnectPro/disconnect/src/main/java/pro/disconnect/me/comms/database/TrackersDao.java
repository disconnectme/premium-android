package pro.disconnect.me.comms.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import pro.disconnect.me.comms.models.Tracker;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface TrackersDao {
    @Insert(onConflict = REPLACE)
    void save(List<Tracker> trackers);

    @Query("SELECT * FROM tracker ORDER BY dateTimeStamp" )
    LiveData<List<Tracker>> load();

    @Query("DELETE FROM tracker")
    void deleteTrackers();

   @Query("SELECT dateTimeStamp FROM tracker ORDER BY dateTimeStamp DESC LIMIT 1")
   long getTimestampLastTracker();

   @Query("SELECT count(*) FROM tracker WHERE dateTimeStamp BETWEEN :start AND :end")
   int getTrackersBetween(long start, long end);
}
