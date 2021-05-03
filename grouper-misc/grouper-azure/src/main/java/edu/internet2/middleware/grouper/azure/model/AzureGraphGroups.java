package edu.internet2.middleware.grouper.azure.model;

import com.squareup.moshi.Json;

import java.util.List;

public class AzureGraphGroups {
    @Json(name = "@odata.context") public final String context;
    @Json(name = "value") public final List<AzureGraphGroup> groups;

    public AzureGraphGroups(String context, List<AzureGraphGroup> groups) {
        this.context = context;
        this.groups = groups;
    }
}
