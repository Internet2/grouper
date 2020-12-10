package edu.internet2.middleware.grouper.azure.model;

import edu.internet2.middleware.grouper.azure.AzureVisibility;

import java.util.Collection;

public class AzureGraphGroup {

    public final String id;
    public final String displayName;
    public final boolean mailEnabled;
    public final String mailNickname;
    public final boolean securityEnabled;
    public final Collection<String> groupTypes;
    public final String description;
    public final AzureVisibility visibility;

    public AzureGraphGroup(String id, String displayName, boolean mailEnabled, String mailNickname, boolean securityEnabled, Collection<String> groupTypes, String description, AzureVisibility visibility) {
        this.id = id;
        this.displayName = displayName;
        this.mailEnabled = mailEnabled;
        this.mailNickname = mailNickname;
        this.securityEnabled = securityEnabled;
        this.groupTypes = groupTypes;
        this.description = description;
        this.visibility = visibility;
    }
}
