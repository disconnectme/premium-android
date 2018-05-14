package pro.disconnect.me.comms;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.os.ConfigurationCompat;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;

import pro.disconnect.me.R;
import pro.disconnect.me.comms.models.CountryContinent;

/**
 * Created by Peter Mullarkey on 26/03/2018.
 */

public class ServerLocations {
    private String[] mLocations;
    private String[] mServerAddresses;
    private Configuration mConfiguration;

    private static ServerLocations sInstance;

    public static ServerLocations getInstance(Context aContext){
        if ( sInstance == null ){
            sInstance = new ServerLocations(aContext.getResources());
        }

        return  sInstance;
    }

    private ServerLocations(Resources aResources){
        mLocations = aResources.getStringArray(R.array.locations);
        mServerAddresses = aResources.getStringArray(R.array.server_addresses);
        mConfiguration = aResources.getConfiguration();
    }

    public String[] getLocationList(){
        return mLocations;
    }


    public String getServerAddressByPosition(int aPosition){
        return mServerAddresses[aPosition];
    }

    public String getLocationByServerAddress(String aServerAddress){
        String location = "";
        int count = mServerAddresses.length;
        for (int index = 0; index < count; index++){
            String serverAddress = mServerAddresses[index];
            if ( aServerAddress.equals(serverAddress)){
                location = mLocations[index];
                break;
            }
        }
        return location;
    }

    public String getDefaultServerByLocale(Resources aResources){
        // Get the region for the current locale.
        Locale current = ConfigurationCompat.getLocales(mConfiguration).get(0);

        InputStream raw =  aResources.openRawResource(R.raw.country_continent);
        Reader rd = new BufferedReader(new InputStreamReader(raw));
        Gson gson = new Gson();
        CountryContinent[] countryContinents = gson.fromJson(rd, CountryContinent[].class);
        String country = current.getCountry();
        String continentCode = "NA";
        for ( CountryContinent countryContinent : countryContinents){
            if (countryContinent.getCountry().equals(country) ){
                continentCode = countryContinent.getContinent();
                break;
            }
        }

        // Lookup default server for region
        String server = "";
        String[] defaultServers = aResources.getStringArray(R.array.default_server_addresses);
        for (String defaultServer : defaultServers ){
            String[] split = defaultServer.split("\\|");
            if ( split[0].equals(continentCode)){
                server = split[1];
            }
        }

        return server;
    }
}
