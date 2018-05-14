package pro.disconnect.me.comms.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

@Entity
public class Post {

    @PrimaryKey
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("uuid")
    @Expose
    private String uuid;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("markdown")
    @Expose
    private String markdown;

    @SerializedName("image")
    @Expose
    private String image;

    @SerializedName("language")
    @Expose
    private String language;

    @SerializedName("published_at")
    @Expose
    private String publishedAt;

    @SerializedName("source_type")
    @Expose
    protected String sourceType;

    @SerializedName("seen")
    @Expose
    private boolean seen;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType){
        this.sourceType = sourceType;
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen){
        this.seen = seen;
    }
}
