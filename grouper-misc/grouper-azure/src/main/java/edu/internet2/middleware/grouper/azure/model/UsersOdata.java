package edu.internet2.middleware.grouper.azure.model;

import com.squareup.moshi.Json;

import java.util.List;

public class UsersOdata {
    @Json(name = "@odata.context") public final String context;
    @Json(name = "value") public final List<User> users;

    public UsersOdata(String context, List<User> users) {
        this.context = context;
        this.users = users;
    }
}
