package edu.internet2.middleware.grouper.changeLog.consumer.o365.model;

import com.squareup.moshi.Json;

import java.util.List;

public class GroupsOdata {
    @Json(name = "@odata.context") public final String context;
    @Json(name = "value") public final List<Group> groups;

    public GroupsOdata(String context, List<Group> groups) {
        this.context = context;
        this.groups = groups;
    }
}
