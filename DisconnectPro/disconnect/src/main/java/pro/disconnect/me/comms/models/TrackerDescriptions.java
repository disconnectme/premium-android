package pro.disconnect.me.comms.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class TrackerDescriptions {
    @SerializedName("descriptions")
    @Expose
    private Map<String, String> descriptions = null;

    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Map<String, String> descriptions) {
        this.descriptions = descriptions;
    }
}
