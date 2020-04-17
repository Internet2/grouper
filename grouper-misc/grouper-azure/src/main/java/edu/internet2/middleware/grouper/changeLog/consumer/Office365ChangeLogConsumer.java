package edu.internet2.middleware.grouper.changeLog.consumer;


import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.consumer.o365.GraphApiClient;
import edu.internet2.middleware.grouper.changeLog.consumer.o365.model.User;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.subject.Subject;
import okhttp3.logging.HttpLoggingInterceptor;
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
    private final GraphApiClient apiClient;

    private String token = null;
    private final String clientId;
    private final String clientSecret;
    private final String tenantId;
    private final String scope;
    private final String idAttribute;
    private final String domain;
    private final String groupJexl;

    private final String proxyType;
    private final String proxyHost;
    private final Integer proxyPort;

    private final GrouperSession grouperSession;

    private final JexlEngine jexlEngine = new JexlEngine();

    public Office365ChangeLogConsumer() {
        // TODO: this.getConsumerName() isn't working for some reason. track down
        String name = this.getConsumerName() != null ? this.getConsumerName() : "o365";
        this.clientId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(CONFIG_PREFIX + name + ".clientId");
        this.clientSecret = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(CONFIG_PREFIX + name + ".clientSecret");
        this.tenantId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(CONFIG_PREFIX + name + ".tenantId");
        this.scope = GrouperLoaderConfig.retrieveConfig().propertyValueString(CONFIG_PREFIX + name + ".scope", "https://graph.microsoft.com/.default");
        this.idAttribute = GrouperLoaderConfig.retrieveConfig().propertyValueString(CONFIG_PREFIX + name + ".idAttribute", DEFAULT_ID_ATTRIBUTE);
        this.domain = GrouperLoaderConfig.retrieveConfig().propertyValueString(CONFIG_PREFIX + name + ".domain", this.tenantId);
        this.groupJexl = GrouperLoaderConfig.retrieveConfig().propertyValueString(CONFIG_PREFIX + name + ".groupJexl");

        this.proxyType = GrouperLoaderConfig.retrieveConfig().propertyValueString(CONFIG_PREFIX + name + ".proxyType");

        if (this.proxyType != null) {
            this.proxyHost = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(CONFIG_PREFIX + name + ".proxyHost");
            this.proxyPort = GrouperLoaderConfig.retrieveConfig().propertyValueIntRequired(CONFIG_PREFIX + name + ".proxyPort");
        } else {
            proxyHost = null;
            proxyPort = null;
        }

        this.grouperSession = GrouperSession.startRootSession();
        this.apiClient = new GraphApiClient(clientId, clientSecret, tenantId, scope, proxyType, proxyHost, proxyPort);
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
                        false,
                        group.getUuid(),
                        true,
                        new ArrayList<String>(),
                        group.getId()
                );

        // todo capture exception
        AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("etc:attribute:office365:o365Id", false);
        group.getAttributeDelegate().assignAttribute(attributeDefName);
        group.getAttributeValueDelegate().assignValue("etc:attribute:office365:o365Id", ((edu.internet2.middleware.grouper.changeLog.consumer.o365.model.Group) response.body()).id);
    }

    // TODO: find out how to induce and implement (if necessary)
    @Override
    protected void removeGroup(Group group, ChangeLogEntry changeLogEntry) {
        logger.debug("removing group " + group);
        String id = group.getAttributeValueDelegate().retrieveValuesString("etc:attribute:office365:o365Id").get(0);
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

        String groupId = group.getAttributeValueDelegate().retrieveValueString("etc:attribute:office365:o365Id");
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
            String groupId = group.getAttributeValueDelegate().retrieveValueString("etc:attribute:office365:o365Id");
            apiClient.removeUserFromGroupInMS(groupId, user.id);
        } catch (IOException e) {
            logger.error(e);
        }
    }
}
