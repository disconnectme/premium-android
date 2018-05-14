package pro.disconnect.me.comms.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

@Entity
public class Tracker {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @SerializedName("domain")
    @Expose
    private String domain;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("blocked")
    @Expose
    private String blocked;

    private Date dateTimeStamp;

    public int getId() {return id; }

    public void setId(int id) {this.id = id; }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        if ( dateTimeStamp == null ){
            long longDate = Long.parseLong(timestamp.split("\\.")[0]);
            dateTimeStamp = new Date(longDate);
        }
    }

    public String getBlocked() {
        return blocked;
    }

    public void setBlocked(String blocked) {
        this.blocked = blocked;
    }

    public Date getDateTimeStamp(){
        if ( dateTimeStamp == null ){
            long longDate = Long.parseLong(timestamp.split("\\.")[0]);
            dateTimeStamp = new Date(longDate);
        }
        return dateTimeStamp;
    }

    public void setDateTimeStamp(Date aDate){
        dateTimeStamp = aDate;
    }

}