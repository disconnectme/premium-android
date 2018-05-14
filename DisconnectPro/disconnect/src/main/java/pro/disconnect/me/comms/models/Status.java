package pro.disconnect.me.comms.models;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Status {
    @SerializedName("account_type")
    @Expose
    private String accountType;
    @SerializedName("quantity")
    @Expose
    private int quantity;
    @SerializedName("consumed")
    @Expose
    private float consumed;
    @SerializedName("usage_calculated_at")
    @Expose
    private String usageCalculatedAt;
    @SerializedName("expires_on")
    @Expose
    private String expiresOn;
    @SerializedName("renews")
    @Expose
    private boolean renews;
    @SerializedName("renewed_at")
    @Expose
    private String renewedAt;
    @SerializedName("feedback_enabled")
    @Expose
    private boolean feedbackEnabled;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("server_info")
    @Expose
    private ServerInfo serverInfo;
    @SerializedName("deactivates_on")
    @Expose
    private String deactivatesOn;
    @SerializedName("apns_token")
    @Expose
    private String apnsToken;

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getConsumed() {
        return consumed;
    }

    public void setConsumed(float consumed) {
        this.consumed = consumed;
    }

    public String getUsageCalculatedAt() {
        return usageCalculatedAt;
    }

    public void setUsageCalculatedAt(String usageCalculatedAt) {
        this.usageCalculatedAt = usageCalculatedAt;
    }

    public String getExpiresOn() {
        return expiresOn;
    }

    public void setExpiresOn(String expiresOn) {
        this.expiresOn = expiresOn;
    }

    public boolean isRenews() {
        return renews;
    }

    public void setRenews(boolean renews) {
        this.renews = renews;
    }

    public String getRenewedAt() {
        return renewedAt;
    }

    public void setRenewedAt(String renewedAt) {
        this.renewedAt = renewedAt;
    }

    public boolean isFeedbackEnabled() {
        return feedbackEnabled;
    }

    public void setFeedbackEnabled(boolean feedbackEnabled) {
        this.feedbackEnabled = feedbackEnabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public String getDeactivatesOn() {
        return deactivatesOn;
    }

    public void setDeactivatesOn(String deactivatesOn) {
        this.deactivatesOn = deactivatesOn;
    }

    public String getApnsToken() {
        return apnsToken;
    }

    public void setApnsToken(String apnsToken) {
        this.apnsToken = apnsToken;
    }

}