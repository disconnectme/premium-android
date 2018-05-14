package pro.disconnect.me.comms.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CountryContinent {

    @SerializedName("country")
    private String country;
    @SerializedName("continent")
    private String continent;

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}


