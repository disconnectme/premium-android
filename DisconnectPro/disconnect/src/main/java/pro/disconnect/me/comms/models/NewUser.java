package pro.disconnect.me.comms.models;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

public class NewUser {

    @SerializedName("account_type")
    @Expose
    private String accountType;
    @SerializedName("quantity")
    @Expose
    private String quantity;
    @SerializedName("consumed")
    @Expose
    private String consumed;
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
    @SerializedName("password")
    @Expose
    private String password;

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getConsumed() {
        return consumed;
    }

    public void setConsumed(String consumed) {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


