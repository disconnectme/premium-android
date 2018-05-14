package pro.disconnect.me.comms.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AccountUpgrade {
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
    @SerializedName("deactivates_on")
    @Expose
    private Object deactivatesOn;
    @SerializedName("partner_provision")
    @Expose
    private PartnerProvision partnerProvision;

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

    public Object getDeactivatesOn() {
        return deactivatesOn;
    }

    public void setDeactivatesOn(Object deactivatesOn) {
        this.deactivatesOn = deactivatesOn;
    }

    public PartnerProvision getPartnerProvision() {
        return partnerProvision;
    }

    public void setPartnerProvision(PartnerProvision partnerProvision) {
        this.partnerProvision = partnerProvision;
    }

}