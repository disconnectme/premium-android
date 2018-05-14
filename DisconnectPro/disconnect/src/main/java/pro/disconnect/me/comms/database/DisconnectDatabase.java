package pro.disconnect.me.comms.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import pro.disconnect.me.comms.models.Post;
import pro.disconnect.me.comms.models.Tracker;

@Database(entities = {Tracker.class, Post.class}, version = 1)
@TypeConverters({DateConverter.class})
public abstract class DisconnectDatabase extends RoomDatabase {
    public abstract TrackersDao trackersDao();
    public abstract PostsDao postsDao();
}

