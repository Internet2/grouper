package edu.internet2.middleware.grouper.azure.model;

import com.squareup.moshi.Json;

public class AzureGraphDataIdContainer {
    @Json(name = "@odata.id") public final String id;

    public AzureGraphDataIdContainer(String id) {
        this.id = id;
    }
}
