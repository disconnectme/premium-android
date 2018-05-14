package pro.disconnect.me.comms.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PartnerProvision {

    @SerializedName("status")
    @Expose
    private int status;
    @SerializedName("status_text")
    @Expose
    private String statusText;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

}
