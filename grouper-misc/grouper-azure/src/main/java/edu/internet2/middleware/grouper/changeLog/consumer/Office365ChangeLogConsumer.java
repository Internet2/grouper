package edu.internet2.middleware.grouper.changeLog.consumer;


import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.azure.AzureGroupType;
import edu.internet2.middleware.grouper.azure.AzureVisibility;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.consumer.o365.GraphApiClient;
import edu.internet2.middleware.grouper.azure.model.User;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignValueQuery;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignValueView;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeDefNameFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
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
    private String upnAttribute;
    private String groupJexl;
    private String mailNicknameJexl;
    private String descriptionJexl;
    private String subjectJexl;

    private GrouperSession grouperSession;

    public void initialize(ChangeLogProcessorMetadata changeLogProcessorMetadata) {
        String name = changeLogProcessorMetadata.getConsumerName();
        GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();

        String clientId = config.propertyValueStringRequired(CONFIG_PREFIX + name + ".clientId");
        String clientSecret = config.propertyValueStringRequired(CONFIG_PREFIX + name + ".clientSecret");
        this.tenantId = config.propertyValueStringRequired(CONFIG_PREFIX + name + ".tenantId");
        String scope = config.propertyValueString(CONFIG_PREFIX + name + ".scope", "https://graph.microsoft.com/.default");
        this.idAttribute = config.propertyValueString(CONFIG_PREFIX + name + ".idAttribute", DEFAULT_ID_ATTRIBUTE);
        this.domain = config.propertyValueString(CONFIG_PREFIX + name + ".domain", this.tenantId);
        this.upnAttribute = config.propertyValueString(CONFIG_PREFIX + name + ".upnAttribute");
        this.groupJexl = config.propertyValueString(CONFIG_PREFIX + name + ".groupJexl");
        this.mailNicknameJexl = config.propertyValueString(CONFIG_PREFIX + name + ".mailNicknameJexl");
        this.descriptionJexl = config.propertyValueString(CONFIG_PREFIX + name + ".descriptionJexl");
        this.subjectJexl = config.propertyValueString(CONFIG_PREFIX + name + ".subjectJexl");

        AzureGroupType groupType;
        String groupTypeString = config.propertyValueString(CONFIG_PREFIX + name + ".groupType", AzureGroupType.Security.name());
        try {
            groupType = AzureGroupType.valueOf(groupTypeString);
        } catch (IllegalArgumentException e) {
            groupType = AzureGroupType.Security;
            logger.error("consumer " + this.getConsumerName() + ": Invalid option for property " + CONFIG_PREFIX + name + ".groupType: " + groupTypeString + " - reverting to type " + groupType.name());
        }

        AzureVisibility visibility = null;
        String visibilityString = config.propertyValueString(CONFIG_PREFIX + name + ".visibility");
        if (visibilityString != null) {
            if (groupType == AzureGroupType.Unified) {
                try {
                    // discrepancy in graph api -- documented as Hiddenmembership but object returns HiddenMembership.
                    // The enum is fixed, but the older loader property was using Hiddenmembership. Map the old value
                    // so existing configs don't break
                    if ("Hiddenmembership".equals(visibilityString)) {
                        visibilityString = "HiddenMembership";
                        logger.warn("For " + CONFIG_PREFIX + name + ".visibility, legacy value Hiddenmembership was remapped to HiddenMembership");
                    }
                    visibility = AzureVisibility.valueOf(visibilityString);
                } catch (IllegalArgumentException e) {
                    visibility = AzureVisibility.Public;
                    logger.error("consumer " + this.getConsumerName() + ": Invalid option for property " + CONFIG_PREFIX + name + ".visibility: " + visibilityString + " - reverting to type " + visibility.name());
                }
            } else {
                logger.error("consumer " + this.getConsumerName() + ": Property " + CONFIG_PREFIX + name + ".visibility is only valid for Unified group type -- ignoring");
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
    }

    @Override
    public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
                                        ChangeLogProcessorMetadata changeLogProcessorMetadata) {
        initialize(changeLogProcessorMetadata);
        return super.processChangeLogEntries(changeLogEntryList, changeLogProcessorMetadata);
    }

    public String evaluateGroupJexlExpression(Group group, String expressionString, String defaultIfExpressionNull) {
        if (expressionString == null) {
            return defaultIfExpressionNull;
        }

        Expression expression = new JexlEngine().createExpression(expressionString);
        MapContext context = new MapContext();
        context.set("group", group);
        context.set("consumerName", this.getConsumerName());
        context.set("tenantId", this.tenantId);
        context.set("domain", this.domain);
        context.set("idAttribute", this.idAttribute);
        String result = (String)expression.evaluate(context);
        logger.trace("evaluated group jexl -> [" + result + "]");
        return result;
    }

    public String evaluateSubjectJexlExpression(Subject subject, String expressionString) {
        if (expressionString == null) {
            return null;
        }

        Expression expression = new JexlEngine().createExpression(expressionString);
        MapContext context = new MapContext();
        context.set("subject", subject);
        context.set("subjectIdValue", subject.getAttributeValue(this.idAttribute));
        context.set("subjectUpnValue", this.upnAttribute == null ? null : subject.getAttributeValue(this.upnAttribute));
        context.set("consumerName", this.getConsumerName());
        context.set("tenantId", this.tenantId);
        context.set("domain", this.domain);
        context.set("idAttribute", this.idAttribute);
        String result = (String)expression.evaluate(context);
        logger.trace("evaluated subject jexl -> [" + result + "]");
        return result;
    }

    public String getUserPrincipalName(Subject subject) {
        String userPrincipalName = null;
        String method = "N/A";

        if (this.upnAttribute != null) {
            userPrincipalName = subject.getAttributeValue(this.upnAttribute);
            method = "upnAttribute";
        }

        if (GrouperUtil.isBlank(userPrincipalName) && this.subjectJexl != null) {
            userPrincipalName = evaluateSubjectJexlExpression(subject, subjectJexl);
            method = "subjectJexl";
        }

        if (GrouperUtil.isBlank(userPrincipalName)) {
            String id = subject.getAttributeValue(this.idAttribute);
            if (!GrouperUtil.isBlank(id)) {
                userPrincipalName = subject.getAttributeValue(this.idAttribute).trim() + "@" + this.domain;
                method = "default (idAttribute)";
            }
        }

        if (GrouperUtil.isBlank(userPrincipalName)) {
            logger.error("consumer " + this.getConsumerName() + " unable to determine principal name for subject " + subject);
            throw new RuntimeException("Failed to calculate principal name for subject " + subject);
        }

        logger.debug("consumer " + this.getConsumerName() + ": calculated subject principal by method " + method + " as " + userPrincipalName);

        return userPrincipalName;
    }

    @Override
    protected void addGroup(Group group, ChangeLogEntry changeLogEntry) {
        logger.info("consumer " + this.getConsumerName() + ": Creating group " + group);

        /*
Request
        {
        "description":"907f8d9543ce46c9a89df743c5994660",
        "displayName":"app:test:grp-2607:provision:groupTypes:o365Sync:group2",
        "groupTypes":[],
        "mailEnabled":false,
        "mailNickname":"907f8d9543ce46c9a89df743c5994660",
        "securityEnabled":true
        }

Response
        // TODO: currently uses the mailNickname as an ID. need to fix this
        {
        "@odata.context":"https://graph.microsoft.com/v1.0/$metadata#groups/$entity",
        "id":"6a461966-e0cb-4d01-adca-2fa3cd40a1a1",
        "createdDateTime":"2020-04-16T20:31:01Z",
        "description":"907f8d9543ce46c9a89df743c5994660",
        "displayName":"app:test:grp-2607:provision:groupTypes:o365Sync:group2",
        "groupTypes":[],
        "isAssignableToRole":null,
        "mail":null,
        "mailEnabled":false,
        "mailNickname":"907f8d9543ce46c9a89df743c5994660",
        "onPremisesDomainName":null,
        "onPremisesLastSyncDateTime":null,
        "onPremisesNetBiosName":null,
        "onPremisesSamAccountName":null,
        "onPremisesSecurityIdentifier":null,
        "onPremisesSyncEnabled":null,
        "preferredDataLocation":null,
        "proxyAddresses":[],
        "renewedDateTime":"2020-04-16T20:31:01Z",
        "resourceBehaviorOptions":[],
        "resourceProvisioningOptions":[],
        "securityEnabled":true,
        "securityIdentifier":"S-1-12-1-1782978918-1291968715-2737818285-2711699661",
        "visibility":null,
        "onPremisesProvisioningErrors":[]
        }
         */

        String displayName = evaluateGroupJexlExpression(group, this.groupJexl, group.getName());
        logger.debug("consumer " + this.getConsumerName() + ": calculated displayName as " + displayName);

        String mailNickname = evaluateGroupJexlExpression(group, this.mailNicknameJexl, group.getUuid());
        logger.debug("consumer " + this.getConsumerName() + ": calculated mailNickname as " + mailNickname);

        String description = evaluateGroupJexlExpression(group, this.descriptionJexl, group.getId());
        logger.debug("consumer " + this.getConsumerName() + ": calculated description as " + description);

        edu.internet2.middleware.grouper.azure.model.Group azureGroup = apiClient.addGroup(
                        displayName,
                        mailNickname,
                        description
                );

        // todo capture exception
        AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(GROUP_ID_ATTRIBUTE_NAME, false);
        group.getAttributeDelegate().assignAttribute(attributeDefName);
        group.getAttributeValueDelegate().assignValue(GROUP_ID_ATTRIBUTE_NAME, azureGroup.id);
    }

    // TODO: find out how to induce and implement (if necessary)
    @Override
    protected void removeGroup(Group group, ChangeLogEntry changeLogEntry) {
        logger.info("consumer " + this.getConsumerName() + ": Removing group " + group);
        String id = group.getAttributeValueDelegate().retrieveValuesString(GROUP_ID_ATTRIBUTE_NAME).get(0);
        logger.debug("removing azure groupId: " + id);
    }

    @Override
    protected void removeDeletedGroup(PITGroup pitGroup, ChangeLogEntry changeLogEntry) {
        logger.info("consumer " + this.getConsumerName() + ": Removing deleted group " + pitGroup.getName() + " from Azure");

        String azureGroupId = null;

        // looking for syncAttribute assignment in the PIT
        try {
            Set<PITAttributeDefName> pitGroupIdAttributes = PITAttributeDefNameFinder
                    .findByName(GROUP_ID_ATTRIBUTE_NAME, false, true);
            if (pitGroupIdAttributes.isEmpty()) {
                throw new RuntimeException("Could not find PITAttributeDefName " + GROUP_ID_ATTRIBUTE_NAME);
            }
            PITAttributeDefName pitGroupIdAttribute = pitGroupIdAttributes.iterator().next();

            Set<PITAttributeAssignValueView> attrAssignValues = new PITAttributeAssignValueQuery()
                    .setAttributeDefNameId(pitGroupIdAttribute.getSourceId()).setOwnerGroupId(pitGroup.getSourceId()).execute();

            if (!attrAssignValues.isEmpty()) {
                azureGroupId = attrAssignValues.iterator().next().getValueString();
            } else {
                throw new RuntimeException("no attribute value for " + GROUP_ID_ATTRIBUTE_NAME);
            }
        } catch (Exception e) {
            logger.error("consumer " + this.getConsumerName() + ": Failed to obtain Azure group id from attributes", e);
            return;
        }

        logger.debug("PITGroup " + pitGroup.getName() + " attribute " + GROUP_ID_ATTRIBUTE_NAME + " = " + azureGroupId);
        apiClient.removeGroup(azureGroupId);
    }

    @Override
    protected void addMembership(Subject subject, Group group, ChangeLogEntry changeLogEntry) {
        logger.info("consumer " + this.getConsumerName() + ": Adding " + subject + " to " + group);

        String groupId = group.getAttributeValueDelegate().retrieveValueString(GROUP_ID_ATTRIBUTE_NAME);
        logger.debug("azure groupId: " + groupId);

        String userPrincipalName = getUserPrincipalName(subject);

        try {

            apiClient.addMemberToMS(groupId, userPrincipalName);
        } catch (Exception e) {
            //todo do more to handle this gracefully
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void removeMembership(Subject subject, Group group, ChangeLogEntry changeLogEntry) {
        logger.info("consumer " + this.getConsumerName() + ": Removing " + subject + " from " + group);
        try {
            User user = apiClient.lookupMSUser(getUserPrincipalName(subject));
            String groupId = group.getAttributeValueDelegate().retrieveValueString(GROUP_ID_ATTRIBUTE_NAME);
            apiClient.removeUserFromGroupInMS(groupId, user.id);
        } catch (IOException e) {
            logger.error(e);
        }
    }
}
