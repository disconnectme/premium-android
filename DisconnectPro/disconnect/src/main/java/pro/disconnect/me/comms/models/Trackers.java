package pro.disconnect.me.comms.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Trackers {
    @SerializedName("domains")
    @Expose
    private List<Tracker> domains = null;

    public List<Tracker> getDomains() {
        return domains;
    }

    public void setDomains(List<Tracker> domains) {
        this.domains = domains;
    }
}
