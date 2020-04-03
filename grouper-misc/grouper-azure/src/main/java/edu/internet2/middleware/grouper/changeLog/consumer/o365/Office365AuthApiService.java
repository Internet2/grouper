package edu.internet2.middleware.grouper.changeLog.consumer.o365;

import edu.internet2.middleware.grouper.changeLog.consumer.o365.model.OAuthTokenInfo;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Office365AuthApiService {
    @FormUrlEncoded
    @POST("oauth2/token")
    Call<OAuthTokenInfo> getOauth2Token(
            @Field("grant_type") String grantType,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret,
            @Field("scope") String scope,
            @Field("resource") String resource
    );
}
