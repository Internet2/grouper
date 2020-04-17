package edu.internet2.middleware.grouper.changeLog.consumer.o365.model;

import java.util.Collection;

public class Group {
    public enum Visibility {Public, Private, Hiddenmembership}

    public final String id;
    public final String displayName;
    public final boolean mailEnabled;
    public final String mailNickname;
    public final boolean securityEnabled;
    public final Collection<String> groupTypes;
    public final String description;
    public final Visibility visibility;

    public Group(String id, String displayName, boolean mailEnabled, String mailNickname, boolean securityEnabled, Collection<String> groupTypes, String description, Visibility visibility) {
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
