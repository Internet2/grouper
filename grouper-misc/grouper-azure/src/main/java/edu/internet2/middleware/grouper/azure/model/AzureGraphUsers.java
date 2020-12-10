package edu.internet2.middleware.grouper.azure.model;

import com.squareup.moshi.Json;

import java.util.List;

public class AzureGraphUsers {
    @Json(name = "@odata.context") public final String context;
    @Json(name = "value") public final List<AzureGraphUser> users;

    public AzureGraphUsers(String context, List<AzureGraphUser> users) {
        this.context = context;
        this.users = users;
    }
}
