package edu.internet2.middleware.grouper.azure.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AzureGraphGroupMembers {
    @SerializedName("@odata.context")
    public final String context;
    @SerializedName("value") public final List<AzureGraphGroupMember> users;

    public AzureGraphGroupMembers(String context, List<AzureGraphGroupMember> users) {
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
