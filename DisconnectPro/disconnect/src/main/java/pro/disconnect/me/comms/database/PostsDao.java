package pro.disconnect.me.comms.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import pro.disconnect.me.comms.models.Post;
import pro.disconnect.me.comms.models.Tracker;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface PostsDao {
    @Insert(onConflict = IGNORE)
    void save(List<Post> posts);

    @Query("SELECT * FROM post WHERE sourceType = :sourcetype ORDER BY publishedAt DESC" )
    LiveData<List<Post>> load(String sourcetype);

    @Query("UPDATE post SET seen=1 WHERE id = :id")
    void markAsSeen(int id);

    @Query("SELECT publishedAt FROM post WHERE sourceType = :sourcetype ORDER BY publishedAt DESC LIMIT 1")
    String getTimestampLastPost(String sourcetype);
}
