package pro.disconnect.me.comms.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServerInfo {

    @SerializedName("ca_file")
    @Expose
    private String caFile;
    @SerializedName("ovpn")
    @Expose
    private String ovpn;
    @SerializedName("servers")
    @Expose
    private List<String> servers = null;

    public String getCaFile() {
        return caFile;
    }

    public void setCaFile(String caFile) {
        this.caFile = caFile;
    }

    public String getOvpn() {
        return ovpn;
    }

    public void setOvpn(String ovpn) {
        this.ovpn = ovpn;
    }

    public List<String> getServers() {
        return servers;
    }

    public void setServers(List<String> servers) {
        this.servers = servers;
    }
}
