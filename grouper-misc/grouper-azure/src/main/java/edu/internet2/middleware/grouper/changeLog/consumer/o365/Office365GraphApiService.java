package edu.internet2.middleware.grouper.changeLog.consumer.o365;

import edu.internet2.middleware.grouper.azure.model.*;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface Office365GraphApiService {
    @POST("groups")
    Call<AzureGraphGroup> createGroup(@Body AzureGraphGroup group);

    @DELETE("groups/{id}")
    Call<ResponseBody> deleteGroup(@Path("id") String groupId);

    @GET("groups/{id}")
    Call<AzureGraphGroup> getGroup(@Path("id") String groupId);

    @GET("groups")
    Call<AzureGraphGroups> getGroups(@QueryMap Map<String, String> options);

    @POST("groups/{groupId}/members/$ref")
    Call<ResponseBody> addGroupMember(@Path("groupId") String groupId, @Body AzureGraphDataIdContainer member);

    @GET("groups/{groupId}/members/")
    Call<AzureGraphGroupMembers> getGroupMembers(@Path("groupId") String groupId);
    
    @GET("users/{upn}")
    Call<AzureGraphUser> getUserByUPN(@Path("upn") String upn);

    @GET("users")
    Call<AzureGraphUsers> getUsers();

    // DELETE https://graph.microsoft.com/v1.0/groups/47e94099-daf6-4036-96c4-62b1593b38a5/members/0041a4a4-0ead-4fde-b3be-5e8968eaa2f4/$ref
    @DELETE("groups/{groupId}/members/{userId}/$ref")
    Call<ResponseBody> removeGroupMember(@Path("groupId") String groupId, @Path("userId") String userId);
}
