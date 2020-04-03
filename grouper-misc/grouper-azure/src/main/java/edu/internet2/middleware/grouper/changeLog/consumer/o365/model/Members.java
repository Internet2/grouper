package edu.internet2.middleware.grouper.changeLog.consumer.o365.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Members {
    @SerializedName("@odata.context")
    public final String context;
    @SerializedName("value") public final List<MemberUser> users;

    public Members(String context, List<MemberUser> users) {
        this.context = context;
        this.users = users;
    }

    @Override
    public String toString() {
        return "Members{" +
                "context='" + context + '\'' +
                ", users=" + users +
                '}';
    }
}
