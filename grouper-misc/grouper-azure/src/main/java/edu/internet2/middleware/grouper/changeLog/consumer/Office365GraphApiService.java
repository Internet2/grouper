package edu.internet2.middleware.grouper.changeLog.consumer;

import edu.internet2.middleware.grouper.changeLog.consumer.model.Group;
import edu.internet2.middleware.grouper.changeLog.consumer.model.GroupsOdata;
import edu.internet2.middleware.grouper.changeLog.consumer.model.OdataIdContainer;
import edu.internet2.middleware.grouper.changeLog.consumer.model.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface Office365GraphApiService {
    @POST("groups")
    Call<Group> createGroup(@Body Group group);

    @DELETE("groups/{id}")
    Call<ResponseBody> deleteGroup(@Path("id") String groupId);

    @GET("groups")
    Call<Group> getGroup(@QueryMap Map<String, String> options);

    @GET("groups")
    Call<GroupsOdata> getGroups(@QueryMap Map<String, String> options);

    @POST("groups/{groupId}/members/$ref")
    Call<ResponseBody> addGroupMember(@Path("groupId") String groupId, @Body OdataIdContainer member);

    @GET("users/{upn}")
    Call<User> getUserByUPN(@Path("upn") String upn);

    // DELETE https://graph.microsoft.com/v1.0/groups/47e94099-daf6-4036-96c4-62b1593b38a5/members/0041a4a4-0ead-4fde-b3be-5e8968eaa2f4/$ref
    @DELETE("groups/{groupId}/members/{userId}/$ref")
    Call<ResponseBody> removeGroupMember(@Path("groupId") String groupId, @Path("userId") String userId);
}
