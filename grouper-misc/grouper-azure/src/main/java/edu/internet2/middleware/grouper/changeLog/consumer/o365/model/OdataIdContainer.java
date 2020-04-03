package edu.internet2.middleware.grouper.changeLog.consumer.o365.model;

import com.squareup.moshi.Json;

public class OdataIdContainer {
    @Json(name = "@odata.id") public final String id;

    public OdataIdContainer(String id) {
        this.id = id;
    }
}
