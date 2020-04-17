package edu.internet2.middleware.grouper.changeLog.consumer;


import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.consumer.o365.GraphApiClient;
import edu.internet2.middleware.grouper.changeLog.consumer.o365.model.Group.Visibility;
import edu.internet2.middleware.grouper.changeLog.consumer.o365.model.User;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.subject.Subject;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Created by jj on 5/30/16.
 */
public class Office365ChangeLogConsumer extends ChangeLogConsumerBaseImpl {
    private static final Logger logger = Logger.getLogger(Office365ChangeLogConsumer.class);
    private static final String CONFIG_PREFIX = "changeLog.consumer.";
    private static final String DEFAULT_ID_ATTRIBUTE = "uid";
    public static final String GROUP_ID_ATTRIBUTE_NAME = "etc:attribute:office365:o365Id";

    private GraphApiClient apiClient;
    private String tenantId;
    private String idAttribute;
    private String domain;
    private String groupJexl;

    private GrouperSession grouperSession;

    private final JexlEngine jexlEngine = new JexlEngine();

    public enum AzureGroupType {Security, Unified, MailEnabled, MailEnabledSecurity}

    @Override
    public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
                                        ChangeLogProcessorMetadata changeLogProcessorMetadata) {
        String name = changeLogProcessorMetadata.getConsumerName();
        GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();

        String clientId = config.propertyValueStringRequired(CONFIG_PREFIX + name + ".clientId");
        String clientSecret = config.propertyValueStringRequired(CONFIG_PREFIX + name + ".clientSecret");
        this.tenantId = config.propertyValueStringRequired(CONFIG_PREFIX + name + ".tenantId");
        String scope = config.propertyValueString(CONFIG_PREFIX + name + ".scope", "https://graph.microsoft.com/.default");
        this.idAttribute = config.propertyValueString(CONFIG_PREFIX + name + ".idAttribute", DEFAULT_ID_ATTRIBUTE);
        this.domain = config.propertyValueString(CONFIG_PREFIX + name + ".domain", this.tenantId);
        this.groupJexl = config.propertyValueString(CONFIG_PREFIX + name + ".groupJexl");

        AzureGroupType groupType;
        String groupTypeString = config.propertyValueString(CONFIG_PREFIX + name + ".groupType", AzureGroupType.Security.name());
        try {
            groupType = AzureGroupType.valueOf(groupTypeString);
        } catch (IllegalArgumentException e) {
            groupType = AzureGroupType.Security;
            logger.error("Invalid option for property " + CONFIG_PREFIX + name + ".groupType: " + groupTypeString + " - reverting to type " + groupType.name());
        }

        Visibility visibility = null;
        String visibilityString = config.propertyValueString(CONFIG_PREFIX + name + ".visibility");
        if (visibilityString != null) {
            if (groupType == AzureGroupType.Unified) {
                try {
                    visibility = Visibility.valueOf(visibilityString);
                } catch (IllegalArgumentException e) {
                    visibility = Visibility.Public;
                    logger.error("Invalid option for property " + CONFIG_PREFIX + name + ".visibility: " + visibilityString + " - reverting to type " + visibility.name());
                }
            } else {
                logger.error("Property " + CONFIG_PREFIX + name + ".visibility is only valid for Unified group type -- ignoring");
            }
        }

        String proxyType = config.propertyValueString(CONFIG_PREFIX + name + ".proxyType");
        String proxyHost;
        Integer proxyPort;
        if (proxyType != null) {
            proxyHost = config.propertyValueStringRequired(CONFIG_PREFIX + name + ".proxyHost");
            proxyPort = config.propertyValueIntRequired(CONFIG_PREFIX + name + ".proxyPort");
        } else {
            proxyHost = null;
            proxyPort = null;
        }

        this.grouperSession = GrouperSession.startRootSession();
        this.apiClient = new GraphApiClient(clientId, clientSecret, tenantId, scope, groupType, visibility, proxyType, proxyHost, proxyPort);

        return super.processChangeLogEntries(changeLogEntryList, changeLogProcessorMetadata);
    }

    private String getJexlGroupName(Object group) {
        String finalName;
        if (this.groupJexl == null) {
            if (group instanceof PITGroup) {
                finalName = ((PITGroup) group).getName();
            } else {
                // assume a Group
                finalName = ((Group) group).getName();
            }
        } else {
            Expression expression = this.jexlEngine.createExpression(this.groupJexl);
            MapContext context = new MapContext();
            context.set("group", group);
            finalName = (String)expression.evaluate(context);
        }
        return finalName;
    }

    @Override
    protected void addGroup(Group group, ChangeLogEntry changeLogEntry) {
        logger.debug("Creating group " + group);
        // TODO: currently uses the mailNickname as an ID. need to fix this
        /*
        {
        "id": "faccfbe2-3270-4db6-9d61-cf95feae9faf",
        "createdDateTime": "2016-06-03T01:09:51Z",
        "description": null,
        "displayName": "test",
        "groupTypes": [],
        "mail": null,
        "mailEnabled": false,
        "mailNickname": "test",
        "onPremisesLastSyncDateTime": null,
        "onPremisesSecurityIdentifier": null,
        "onPremisesSyncEnabled": null,
        "proxyAddresses": [],
        "renewedDateTime": "2016-06-03T01:09:51Z",
        "securityEnabled": true,
        "visibility": null
        }
         */

        retrofit2.Response response = apiClient.addGroup(
                        this.getJexlGroupName(group),
                        group.getUuid(),
                        group.getId()
                );

        // todo capture exception
        AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(GROUP_ID_ATTRIBUTE_NAME, false);
        group.getAttributeDelegate().assignAttribute(attributeDefName);
        group.getAttributeValueDelegate().assignValue(GROUP_ID_ATTRIBUTE_NAME, ((edu.internet2.middleware.grouper.changeLog.consumer.o365.model.Group) response.body()).id);
    }

    // TODO: find out how to induce and implement (if necessary)
    @Override
    protected void removeGroup(Group group, ChangeLogEntry changeLogEntry) {
        logger.debug("removing group " + group);
        String id = group.getAttributeValueDelegate().retrieveValuesString(GROUP_ID_ATTRIBUTE_NAME).get(0);
        logger.debug("removing id: " + id);
    }

    @Override
    protected void removeDeletedGroup(PITGroup pitGroup, ChangeLogEntry changeLogEntry) {
        logger.debug("removing group " + pitGroup + ": " + pitGroup.getId());
        try {
            String finalName = this.getJexlGroupName(pitGroup);
            Map options = new TreeMap<>();
            //TODO: fix this
            options.put("$filter", "displayName eq '" + finalName + "'");
            apiClient.removeGroup(options);
        } catch (Exception e) {
            logger.error(e);
            //todo do more to handle this gracefully
        }
    }

    @Override
    protected void addMembership(Subject subject, Group group, ChangeLogEntry changeLogEntry) {
        logger.debug("adding " + subject + " to " + group);

        String groupId = group.getAttributeValueDelegate().retrieveValueString(GROUP_ID_ATTRIBUTE_NAME);
        logger.debug("groupId: " + groupId);

        String userPrincipalName = subject.getAttributeValue(this.idAttribute) + "@" + this.domain;

        try {

            apiClient.addMemberToMS(groupId, userPrincipalName);
        } catch (Exception e) {
            //todo do more to handle this gracefully
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void removeMembership(Subject subject, Group group, ChangeLogEntry changeLogEntry) {
        logger.debug("removing " + subject + " from " + group);
        try {
            User user = apiClient.lookupMSUser(subject.getAttributeValue(this.idAttribute).trim() + "@" + tenantId);
            String groupId = group.getAttributeValueDelegate().retrieveValueString(GROUP_ID_ATTRIBUTE_NAME);
            apiClient.removeUserFromGroupInMS(groupId, user.id);
        } catch (IOException e) {
            logger.error(e);
        }
    }
}
