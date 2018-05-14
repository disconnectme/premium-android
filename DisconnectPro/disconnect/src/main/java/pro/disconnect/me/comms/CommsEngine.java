package pro.disconnect.me.comms;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.strongswan.android.data.VpnProfile;
import org.strongswan.android.data.VpnProfileDataSource;
import org.strongswan.android.data.VpnType;
import org.strongswan.android.logic.TrustedCertificateManager;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.Cache;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import pro.disconnect.me.R;
import pro.disconnect.me.billing.BillingManager;
import pro.disconnect.me.comms.database.DisconnectDatabase;
import pro.disconnect.me.comms.database.PostsDao;
import pro.disconnect.me.comms.database.TrackersDao;
import pro.disconnect.me.comms.models.AccountUpgrade;
import pro.disconnect.me.comms.models.NewUser;
import pro.disconnect.me.comms.models.NewsItems;
import pro.disconnect.me.comms.models.PartnerProvision;
import pro.disconnect.me.comms.models.Post;
import pro.disconnect.me.comms.models.Status;
import pro.disconnect.me.comms.models.Tracker;
import pro.disconnect.me.comms.models.TrackerDescriptions;
import pro.disconnect.me.comms.models.Trackers;
import pro.disconnect.me.comms.utils.ApiResponse;
import pro.disconnect.me.comms.utils.LiveDataCallAdapterFactory;
import pro.disconnect.me.comms.utils.NetworkBoundResource;
import pro.disconnect.me.comms.utils.Resource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;
import static com.android.billingclient.api.BillingClient.BillingResponse.SERVICE_UNAVAILABLE;


public class CommsEngine implements BillingManager.BillingUpdatesListener {
    private static final String TAG = "CommsEngine";

    private static final String ACCOUNT_BASE_URL = "https://secure-wifi-api.disconnect.me/";
    private static final String DATA_BASE_URL = "https://cct-updates.disconnect.me/";
    private static final String DESCRIPTION_BASE_URL = "https://s3.amazonaws.com/cct-updates/";
    private static final String ACCOUNT_URL_PATTERN = "secure-wifi-api.disconnect.me/";
    private static final String ACCOUNT_URL_CERT_PIN = "sha256/7FNXE6XOo4E1A/RHTePS/QYMXWx/IrPRXDWnWF6Ro9c=";

    private static final int UPGRADE_QUANTITY = 1048576;

    public static final String TRACKER_TIME_STAMP = "tracker_time_stamp";
    public static final String UPGRADED = "upgraded";

    public static final String POST_NEWS = "news";
    public static final String POST_ALERTS = "alerts";
    public static final String POST_TIPS = "tips";

    private static final int cacheSize = 1 * 1024 * 1024; // 1 MB

    private static CommsEngine sCommsEngine;
    private Retrofit mAccountRetroFit;
    private Retrofit mDataRetroFit;
    private Retrofit mDescriptionRetroFit;

    private Context mApplicationContext;
    private ServerLocations mServerLocations;

    private Map<String, String> mTrackerDescriptions;

    private boolean mUpgraded;
    private VpnProfile mVpnProfile;
    private Executor mExecutor;

    private DisconnectDatabase mDatabase;

    private BillingManager mBillingManager;
    private List<SkuDetails> mSkuDetailsList;

    private SharedPreferences mSharedPreferences;
    private HashMap<String, String> mLastPostMap = new HashMap<>();

    private MutableLiveData<Resource<VpnProfile>> mVpnProfileResource = new MutableLiveData<>();

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }

    private MainThreadExecutor mMainThreadExecutor;

    public static CommsEngine getInstance(Context aContext) {
        if (sCommsEngine == null) {

            sCommsEngine = new CommsEngine(aContext);
        }
        return sCommsEngine;
    }

    public CommsEngine(Context aContext){
        mApplicationContext = aContext.getApplicationContext();
        mServerLocations = ServerLocations.getInstance(mApplicationContext);

        mDatabase = Room.databaseBuilder(mApplicationContext,
                DisconnectDatabase.class, "disconnect-database").build();


        // Get VPNProfile
        VpnProfileDataSource dataSource = new VpnProfileDataSource(mApplicationContext);
        dataSource.open();
        List<VpnProfile> profiles = dataSource.getAllVpnProfiles();
        dataSource.close();
        if ( profiles.size() > 0 ) {
            mVpnProfile = profiles.get(0);
        }

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpClientBuilder.addInterceptor(interceptor);

        mExecutor = Executors.newSingleThreadExecutor();
        mMainThreadExecutor = new MainThreadExecutor();

        CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add(ACCOUNT_URL_PATTERN, ACCOUNT_URL_CERT_PIN)
                .build();

        mAccountRetroFit = new Retrofit.Builder()
                .client(okHttpClientBuilder.certificatePinner(certificatePinner).build())
                .baseUrl(ACCOUNT_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .callbackExecutor(mExecutor)
                .build();

        mDataRetroFit = new Retrofit.Builder()
                .client(okHttpClientBuilder.build())
                .baseUrl(DATA_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .callbackExecutor(mExecutor)
                .build();

        // Cache file download responses
        Cache cache = new Cache(mApplicationContext.getCacheDir(), cacheSize);
        OkHttpClient.Builder fileDownloadClientBuilder = new OkHttpClient.Builder()
                .cache(cache);

        mDescriptionRetroFit = new Retrofit.Builder()
                .client(fileDownloadClientBuilder.build())
                .baseUrl(DESCRIPTION_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .callbackExecutor(mExecutor)
                .build();

        // Check ca crt has been added to store
        TrustedCertificateManager certman = TrustedCertificateManager.getInstance().load();
        Hashtable<String, X509Certificate> certificates = certman.getCACertificates(TrustedCertificateManager.TrustedCertificateSource.LOCAL);
        if ( certificates.size() == 0 ){
            importCaCert();
        }

        mSharedPreferences = mApplicationContext.getSharedPreferences("disconnect", MODE_PRIVATE );

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final PostsDao postsDao = mDatabase.postsDao();
                mLastPostMap.put(CommsEngine.POST_NEWS, postsDao.getTimestampLastPost(CommsEngine.POST_NEWS));
                mLastPostMap.put(CommsEngine.POST_ALERTS, postsDao.getTimestampLastPost(CommsEngine.POST_ALERTS));
                mLastPostMap.put(CommsEngine.POST_TIPS, postsDao.getTimestampLastPost(CommsEngine.POST_TIPS));
            }
        });
    }

    public LiveData<Resource<VpnProfile>> start(){
        if ( mVpnProfile == null ){
            newUser();
        } else {
            updateStatus(mVpnProfile.getUsername());
        }

        downloadTrackerDescriptions();

        return mVpnProfileResource;
    }

    private void newUser() {
        DisconnectAPI api = mAccountRetroFit.create(DisconnectAPI.class);
        Call<NewUser> newUserCall = api.newUser(DisconnectAPI.EmptyRequest.INSTANCE);

                newUserCall.enqueue(new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) {
                        if (response != null && response.body() != null) {
                            NewUser newUser = (NewUser) response.body();

                            // Create vpnProfile
                            VpnProfile profile = new VpnProfile();
                            profile.setSplitTunneling(VpnProfile.SPLIT_TUNNELING_BLOCK_IPV6);
                            profile.setUsername(newUser.getUsername());
                            profile.setPassword(newUser.getPassword());

                            profile.setVpnType(VpnType.IKEV2_EAP);

                            String defaultServer = mServerLocations.getDefaultServerByLocale(mApplicationContext.getResources());

                            profile.setGateway(defaultServer);
                            profile.setName("Disconnect Premium");
                            profile.setRemoteId(String.format("CN=%s", defaultServer));
                            profile.setCertificateAlias(null);

                            VpnProfileDataSource dataSource = new VpnProfileDataSource(mApplicationContext);
                            dataSource.open();
                            dataSource.insertProfile(profile);
                            dataSource.close();

                            mVpnProfile = profile;

                            updateStatus(newUser.getUsername());
                        }
                    }

                    @Override
                    public void onFailure(Call call, Throwable t) {
                        final String message = t.getLocalizedMessage();
                        mMainThreadExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                mVpnProfileResource.setValue(Resource.error(message, mVpnProfile));
                            }
                        });
                    }
                });
    }

    private void updateStatus(String aUsername) {
        DisconnectAPI api = mAccountRetroFit.create(DisconnectAPI.class);
        Call<Status> updateStatusCall = api.updateStatus(aUsername);
        updateStatusCall.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response != null && response.body() != null) {
                    Status status = (Status) response.body();
                    if ( status.getQuantity() >= UPGRADE_QUANTITY){
                        mUpgraded = true;
                    } else {
                        mUpgraded = false;
                    }

                    mSharedPreferences.edit().putBoolean(UPGRADED, mUpgraded).commit();
                }

                    mMainThreadExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if ( !mUpgraded ){
                                // Check subscriptions
                                checkForSubscriptions();
                            } else {
                                mVpnProfileResource.setValue(Resource.success(mVpnProfile));
                            }
                        }
                    });
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                final String message = t.getLocalizedMessage();
                mMainThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mVpnProfileResource.setValue(Resource.error(message, mVpnProfile));
                    }
                });
        }
        });
    }

    private void importCaCert(){
        // Parse file
        X509Certificate certificate = null;
        try
        {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            AssetManager am = mApplicationContext.getAssets();
            InputStream in = am.open("ca.crt");
            certificate = (X509Certificate)factory.generateCertificate(in);
			/* we don't check whether it's actually a CA certificate or not */

            KeyStore store = KeyStore.getInstance("LocalCertificateStore");
            store.load(null, null);
            store.setCertificateEntry(null, certificate);
            TrustedCertificateManager.getInstance().reset();
            }
        catch (Exception e)
            {
                e.printStackTrace();
            }
    }



    public LiveData<Resource<List<Tracker>>> getBlockedTrackers(long since) {
        return new NetworkBoundResource<List<Tracker>,Trackers>(mExecutor, mMainThreadExecutor) {
            @Override
            protected void saveCallResult(@NonNull Trackers aTrackers) {
                final TrackersDao trackersDao = mDatabase.trackersDao();
                trackersDao.save(aTrackers.getDomains());

                long latestTimeStamp = trackersDao.getTimestampLastTracker();
                if (latestTimeStamp > 1){
                    mSharedPreferences.edit()
                            .putLong(TRACKER_TIME_STAMP, latestTimeStamp + 1)
                            .commit();
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Tracker> data) {
                return true;
            }

            @NonNull
            @Override
            protected LiveData<List<Tracker>> loadFromDb() {
                final TrackersDao trackersDao = mDatabase.trackersDao();
                LiveData<List<Tracker>> trackers = trackersDao.load();
                return trackers;
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<Trackers>> createCall() {
                DisconnectAPI api = mDataRetroFit.create(DisconnectAPI.class);
                long lastTrackerTimeStamp = mSharedPreferences.getLong(TRACKER_TIME_STAMP, 0);
                return api.getTrackersSince(mVpnProfile.getPassword(), mVpnProfile.getUsername(), lastTrackerTimeStamp);
              //  return api.getTrackersSince("ghost-busters-gand", "disconnectmobile7787",  mLastTrackerTimeStamp);
            }
        }.asLiveData();
    }

    public void downloadTrackerDescriptions(){
        DisconnectAPI api = mDescriptionRetroFit.create(DisconnectAPI.class);
        Call<TrackerDescriptions> updateStatusCall = api.getTrackersDescriptions();
        updateStatusCall.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response != null && response.body() != null) {
                    TrackerDescriptions trackerDescriptions = (TrackerDescriptions) response.body();
                    mTrackerDescriptions = trackerDescriptions.getDescriptions();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {

            }
        });
    }

    public String getTrackerDescription(String aKey){
        String description = null;
        if ( mTrackerDescriptions != null){
            String[] components = aKey.split("\\.");
            int length = components.length;
            if ( length > 2 ) {
                String key = String.format("%s.%s", components[length-2], components[length-1]);
                description = mTrackerDescriptions.get(key);
            }
        }

        return description;
    }

    public LiveData<Resource<List<Post>>> getNewsItems(final String aSourceType) {
        return new NetworkBoundResource<List<Post>,NewsItems>(mExecutor, mMainThreadExecutor) {
            @Override
            protected void saveCallResult(@NonNull NewsItems aPosts) {
                final PostsDao postsDao = mDatabase.postsDao();

                List<Post> posts = aPosts.getPosts();

                // TODO Is there a more elegant way of setting the type?
                for (Post post : posts ){
                    post.setSourceType(aSourceType);
                }

                postsDao.save(posts);

                mLastPostMap.put(aSourceType, postsDao.getTimestampLastPost(aSourceType));
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Post> data) {
                return true;
            }

            @NonNull
            @Override
            protected LiveData<List<Post>> loadFromDb() {
                final PostsDao postsDao = mDatabase.postsDao();
                return postsDao.load(aSourceType);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<NewsItems>> createCall() {
                DisconnectAPI api = mDataRetroFit.create(DisconnectAPI.class);
                String lastPost = mLastPostMap.get(aSourceType);
                return api.getNewsItemsSince("tag:" + aSourceType, lastPost);
            }
        }.asLiveData();
    }

    public void markPostAsSeen(final int aId ){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.postsDao().markAsSeen(aId);
            }
        });
    }

    public LiveData<Resource<String>> applyUpgradeCode(final String aFeatureCode){
        final MutableLiveData<Resource<String>> serverResponse = new MutableLiveData<>();
        serverResponse.setValue(Resource.loading(""));

        DisconnectAPI api = mAccountRetroFit.create(DisconnectAPI.class);
        Call<AccountUpgrade> upgradeCall = api.applyUpgradeCode(mVpnProfile.getUsername(), aFeatureCode);
        upgradeCall.enqueue(new Callback<AccountUpgrade>() {
            @Override
            public void onResponse(Call<AccountUpgrade> call, Response<AccountUpgrade> response) {
                String message = "";
                if (response != null && response.body() != null) {
                    AccountUpgrade upgrade = (AccountUpgrade) response.body();
                    PartnerProvision provision = upgrade.getPartnerProvision();
                    if ( provision != null ){
                        message = provision.getStatusText();
                    }
                }

                final String messageText = message;
                mMainThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        serverResponse.setValue(Resource.success(messageText));
                    }
                });
            }

            @Override
            public void onFailure(Call<AccountUpgrade> call, Throwable t) {
                final String message = t.getLocalizedMessage();
                mMainThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        serverResponse.setValue(Resource.error(message, ""));
                    }
                });
            }
        });
        return serverResponse;
    }

    private void checkForSubscriptions(){
        if ( mBillingManager == null ) {
            mBillingManager = new BillingManager(mApplicationContext, this);
        } else {
            mBillingManager.queryPurchases();
        }
    }

    public MutableLiveData<Resource<VpnProfile>> initiatePurchaseFlow(Activity aActivity, String aProductId){
        mBillingManager.initiatePurchaseFlow(aActivity,aProductId,BillingClient.SkuType.SUBS );
        mVpnProfileResource.setValue(Resource.loading(mVpnProfile));
        return mVpnProfileResource;
    }

    // From BillingUpdatesListener
    @Override
    public void onBillingClientSetupFinished() {
    }

    @Override
    public void onConsumeFinished(String token, @BillingClient.BillingResponse int result){
    }

    @Override
    public void onPurchasesUpdated(List<Purchase> purchases){
        // TODO check expiration time/date of purchases
        if ( purchases.size() > 0 ){
            // Send subscription information to server
            mVpnProfileResource.setValue(Resource.loading(mVpnProfile));
            uploadPurchaseDetails(purchases);
        } else {
            mVpnProfileResource.setValue(Resource.success(mVpnProfile));
        }
    }

    @Override
    public void onPurchasesFailure(int aFailureCode, boolean aShowError) {
        String errorMessage = "";
        if ( aShowError ){
            switch ( aFailureCode ){
                case SERVICE_UNAVAILABLE:{
                    errorMessage = mApplicationContext.getString(R.string.error_purchasing_network_down);
                }
                break;
                default:{
                    errorMessage = mApplicationContext.getString(R.string.error_purchasing_subscription);
                }
            }
        }

        Log.w(TAG, "Purchases failed: "+errorMessage);

        final String message = errorMessage;
        mMainThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mVpnProfileResource.setValue(Resource.error(message, mVpnProfile));
            }
        });
    }

    public void uploadPurchaseDetails(List<Purchase> aPurchases){
        // Assume first purchase is active
        Purchase purchase = aPurchases.get(0);

        String packageName = purchase.getPackageName();
        String subscriptionId = purchase.getSku();
        String token = purchase.getPurchaseToken();
        String json = purchase.getOriginalJson();

        DisconnectAPI api = mAccountRetroFit.create(DisconnectAPI.class);
        Call<Status> upgradeCall = api.activateDevice(mVpnProfile.getUsername(),
                mVpnProfile.getPassword(), packageName, subscriptionId, token, json);
        upgradeCall.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.w(TAG, "upload purchase details");
                if (response != null && response.body() != null) {
                    Status status = (Status) response.body();
                    if ( status.getQuantity() >= UPGRADE_QUANTITY){
                        Log.w(TAG, "upload purchase details == Upgraded");
                        mUpgraded = true;
                    } else {
                        Log.w(TAG, "upload purchase details == Not upgraded");
                        mUpgraded = false;
                    }
                }

                mMainThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mSharedPreferences.edit().putBoolean(UPGRADED, mUpgraded).commit();
                        mVpnProfileResource.setValue(Resource.success(mVpnProfile));
                    }
                });
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                final String message = t.getLocalizedMessage();
                mMainThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.w(TAG, "upload purchase details == ERROR: " + message);
                        mVpnProfileResource.setValue(Resource.error(message, mVpnProfile));
                    }
                });
            }
        });
    }

    public LiveData<Resource<List<SkuDetails>>> fetchProductInformation(){
        final MutableLiveData<Resource<List<SkuDetails>>> serverResponse = new MutableLiveData<>();
        serverResponse.setValue(Resource.loading(mSkuDetailsList));

        List productIds = Arrays.asList(mApplicationContext.getResources().getStringArray(R.array.product_ids));
        mBillingManager.querySkuDetailsAsync(BillingClient.SkuType.SUBS,productIds, new SkuDetailsResponseListener(){

            @Override
            public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                if (responseCode != BillingClient.BillingResponse.OK) {
                    serverResponse.setValue(Resource.error(mApplicationContext.getResources().getString(R.string.sku_details_missing),mSkuDetailsList));
                } else if(responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                    mSkuDetailsList = skuDetailsList;
                    serverResponse.setValue(Resource.success(mSkuDetailsList));
                }
            }
        });

        return serverResponse;
    }
}
