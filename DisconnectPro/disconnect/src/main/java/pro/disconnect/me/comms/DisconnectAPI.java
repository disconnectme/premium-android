package pro.disconnect.me.comms;

import android.arch.lifecycle.LiveData;

import java.util.Map;

import pro.disconnect.me.comms.models.AccountUpgrade;
import pro.disconnect.me.comms.models.NewUser;
import pro.disconnect.me.comms.models.NewsItems;
import pro.disconnect.me.comms.models.Status;
import pro.disconnect.me.comms.models.TrackerDescriptions;
import pro.disconnect.me.comms.models.Trackers;
import pro.disconnect.me.comms.utils.ApiResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * API for getting data from Disconnect
 */
public interface DisconnectAPI {
    class EmptyRequest {
        public static final EmptyRequest INSTANCE = new EmptyRequest();
    }

    @POST("/v1/provision?tag=pro_android")
    Call<NewUser> newUser(@Body EmptyRequest user);

    @FormUrlEncoded
    @POST("/v1/activateDevice?device=android")
//    @POST("/v1/status")
    Call<Status> activateDevice(@Field("username") String uname,
                                @Field("pass") String aPassword,
                                @Field("packageName") String aPackageName,
                                @Field("subscriptionId") String aSubscriptionId,
                                @Field("token") String token,
                                @Field("json") String json);

    @FormUrlEncoded
    @POST("/v1/status")
    Call<Status> updateStatus(@Field("username") String uname);

    @FormUrlEncoded
    @POST("/v1/upgrade")
    Call<AccountUpgrade> applyUpgradeCode(@Field("username") String uname, @Field("featureCode") String featureCode);

    @GET("mobile/feedback/recent")
    LiveData<ApiResponse<Trackers>> getTrackersSince(@Query("pass") String aPassword, @Query("user") String aUser, @Query("after") long aTimestamp );

    @GET("trackerDescriptions_EN.json")
    Call<TrackerDescriptions> getTrackersDescriptions();

    @GET("ghost/posts")
    LiveData<ApiResponse<NewsItems>> getNewsItemsSince(@Query("filter") String aFilter, @Query("published_at") String aPublishedAt);
}
