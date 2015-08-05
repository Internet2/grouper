/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.changelogconsumer.googleapps.utils;

import com.google.api.services.groupssettings.model.Groups;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects all the various properties and makes them available to the consumer and fullSync.
 *
 * @author John Gasper, Unicon
 */
public class GoogleAppsSyncProperties {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleAppsSyncProperties.class);
    private static final String PARAMETER_NAMESPACE = "changeLog.consumer.";

    private String serviceAccountPKCS12FilePath;
    private String serviceAccountEmail;
    private String serviceImpersonationUser;

    private String googleDomain;

    private String groupIdentifierExpression;
    private String subjectIdentifierExpression;

    /** how long should the Google caches be valid */
    private int googleUserCacheValidity;
    private int googleGroupCacheValidity;

    /** should the Google caches be pre-filled at start-up to take advantage of bath queries */
    private boolean prefillGoogleCachesForConsumer;
    private boolean prefillGoogleCachesForFullSync;

    private boolean retryOnError;

    /** Whether or not to provision users. */
    private boolean provisionUsers;

    /** Whether or not to de-provision users. */
    private boolean deprovisionUsers;

    /** Whether to not use "split" to parse name or the subject API is used to get the name, see subjectGivenNameField and subjectSurnameField */
    private boolean simpleSubjectNaming;

    /** The givenName field to lookup with the Subject API */
    private String subjectGivenNameField;

    /** The surname field to lookup with the Subject API */
    private String subjectSurnameField;

    /** should the provisioned users be in the GAL*/
    private Groups defaultGroupSettings = new Groups();

    /** should the provisioned users be in the GAL*/
    private boolean includeUserInGlobalAddressList;

    /** What to do with deleted Groups: archive, delete, ignore (default) */
    private String handleDeletedGroup;

    /** Which type of privilege becomes an owner: none (default), admin, update, both */
    private String whoCanManage;

    private String googleGroupFilter;

    private boolean ignoreExtraGoogleMembers;

    /** Newly deleted objects aren't always removed ASAP, nor are newly created/updated object ready immediately */
    private int recentlyManipulatedQueueSize;
    private int recentlyManipulatedQueueDelay;

    public GoogleAppsSyncProperties(String consumerName) {
        final String qualifiedParameterNamespace = PARAMETER_NAMESPACE + consumerName + ".";

        LOG.debug("Google Apps Consumer - Setting properties for {} consumer/provisioner.", consumerName);

        serviceAccountPKCS12FilePath =
                GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(qualifiedParameterNamespace + "serviceAccountPKCS12FilePath");
        LOG.debug("Google Apps Consumer - Setting Google serviceAccountPKCS12FilePath to {}", serviceAccountPKCS12FilePath);

        serviceAccountEmail =
                GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(qualifiedParameterNamespace + "serviceAccountEmail");
        LOG.debug("Google Apps Consumer - Setting Google serviceAccountEmail on error to {}", serviceAccountEmail);

        serviceImpersonationUser =
                GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(qualifiedParameterNamespace + "serviceImpersonationUser");
        LOG.debug("Google Apps Consumer - Setting Google serviceImpersonationUser to {}", serviceImpersonationUser);

        googleDomain =
                GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(qualifiedParameterNamespace + "domain");
        LOG.debug("Google Apps Consumer - Setting Google domain to {}", googleDomain);

        groupIdentifierExpression =
                GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(qualifiedParameterNamespace + "groupIdentifierExpression");
        LOG.debug("Google Apps Consumer - Setting groupIdentifierExpression to {}", groupIdentifierExpression);

        subjectIdentifierExpression =
                GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(qualifiedParameterNamespace + "subjectIdentifierExpression");
        LOG.debug("Google Apps Consumer - Setting subjectIdentifierExpression to {}", subjectIdentifierExpression);

        provisionUsers =
                GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(qualifiedParameterNamespace + "provisionUsers", false);
        LOG.debug("Google Apps Consumer - Setting provisionUser to {}", provisionUsers);

        deprovisionUsers =
                GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(qualifiedParameterNamespace + "deprovisionUsers", false);
        LOG.debug("Google Apps Consumer - Setting deprovisionUser to {}", deprovisionUsers);

        includeUserInGlobalAddressList =
                GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(qualifiedParameterNamespace + "includeUserInGlobalAddressList", true);
        LOG.debug("Google Apps Consumer - Setting includeUserInGlobalAddressList to {}", includeUserInGlobalAddressList);

        simpleSubjectNaming =
                GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(qualifiedParameterNamespace + "simpleSubjectNaming", true);
        LOG.debug("Google Apps Consumer - Setting simpleSubjectNaming to {}", simpleSubjectNaming);

        subjectGivenNameField =
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "subjectGivenNameField", "givenName");
        LOG.debug("Google Apps Consumer - Setting subjectGivenNameField to {}", subjectGivenNameField);

        subjectSurnameField =
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "subjectSurnameField" ,"sn");
        LOG.debug("Google Apps Consumer - Setting subjectSurnameField to {}", subjectSurnameField);

        googleUserCacheValidity =
                GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "googleUserCacheValidityPeriod", 30);
        LOG.debug("Google Apps Consumer - Setting googleUserCacheValidityPeriod to {}", googleUserCacheValidity);

        googleGroupCacheValidity =
                GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "googleGroupCacheValidityPeriod", 30);
        LOG.debug("Google Apps Consumer - Setting googleGroupCacheValidityPeriod to {}", googleGroupCacheValidity);

        prefillGoogleCachesForConsumer = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(PARAMETER_NAMESPACE + "prefillGoogleCachesForConsumer", false);
        LOG.debug("Google Apps Consumer - Setting prefillGoogleCachesForConsumer to {}", prefillGoogleCachesForConsumer);

        prefillGoogleCachesForFullSync = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(PARAMETER_NAMESPACE + "prefillGoogleCachesForFullSync", false);
        LOG.debug("Google Apps Consumer - Setting prefillGoogleCachesForFullSync to {}", prefillGoogleCachesForFullSync);

        handleDeletedGroup =
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "handleDeletedGroup", "ignore");
        LOG.debug("Google Apps Consumer - Setting handleDeletedGroup to {}", handleDeletedGroup);

        whoCanManage =
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "whoCanManage", "none");
        LOG.debug("Google Apps Consumer - Setting whoCanManage to {}", whoCanManage);

        recentlyManipulatedQueueSize =
                GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "recentlyManipulatedQueueSize", 5);
        LOG.debug("Google Apps Consumer - Setting recentlyManipulatedQueueSize to {}", recentlyManipulatedQueueSize);

        recentlyManipulatedQueueDelay =
                GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "recentlyManipulatedQueueDelay", 2);
        LOG.debug("Google Apps Consumer - Setting recentlyManipulatedQueueDelay to {}", recentlyManipulatedQueueDelay);


        defaultGroupSettings.setWhoCanViewMembership(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "whoCanViewMembership", "ALL_IN_DOMAIN_CAN_VIEW"));
        LOG.debug("Google Apps Consumer - Setting whoCanViewMembership to {}", defaultGroupSettings.getWhoCanViewMembership());

        defaultGroupSettings.setWhoCanInvite(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "whoCanInvite", "ALL_MANAGERS_CAN_INVITE"));
        LOG.debug("Google Apps Consumer - Setting whoCanInvite to {}", defaultGroupSettings.getWhoCanInvite());

        defaultGroupSettings.setAllowExternalMembers(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "allowExternalMembers", "false"));
        LOG.debug("Google Apps Consumer - Setting allowExternalMembers to {}", defaultGroupSettings.getAllowExternalMembers());

        defaultGroupSettings.setWhoCanPostMessage(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "whoCanPostMessage", "ALL_IN_DOMAIN_CAN_POST"));
        LOG.debug("Google Apps Consumer - Setting whoCanPostMessage to {}", defaultGroupSettings.getWhoCanPostMessage());

        defaultGroupSettings.setAllowWebPosting(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "allowWebPosting", "true"));
        LOG.debug("Google Apps Consumer - Setting allowWebPosting to {}", defaultGroupSettings.getAllowWebPosting());

        defaultGroupSettings.setPrimaryLanguage(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "primaryLanguage", "en"));
        LOG.debug("Google Apps Consumer - Setting primaryLanguage to {}", defaultGroupSettings.getPrimaryLanguage());

        defaultGroupSettings.setMaxMessageBytes(
                GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "maxMessageBytes", 10240));
        LOG.debug("Google Apps Consumer - Setting maxMessageBytes to {}", defaultGroupSettings.getMaxMessageBytes());

        defaultGroupSettings.setIsArchived(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "isArchived", "true"));
        LOG.debug("Google Apps Consumer - Setting isArchived to {}", defaultGroupSettings.getIsArchived());

        defaultGroupSettings.setMessageModerationLevel(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "messageModerationLevel", "MODERATE_NONE"));
        LOG.debug("Google Apps Consumer - Setting messageModerationLevel to {}", defaultGroupSettings.getMessageModerationLevel());

        defaultGroupSettings.setSpamModerationLevel(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "spamModerationLevel", "ALLOW"));
        LOG.debug("Google Apps Consumer - Setting spamModerationLevel to {}", defaultGroupSettings.getSpamModerationLevel());

        defaultGroupSettings.setReplyTo(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "replyTo", "REPLY_TO_IGNORE"));
        LOG.debug("Google Apps Consumer - Setting replyTo to {}", defaultGroupSettings.getReplyTo());

        defaultGroupSettings.setCustomReplyTo(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "customReplyTo", ""));
        LOG.debug("Google Apps Consumer - Setting customReplyTo to {}", defaultGroupSettings.getCustomReplyTo());

        defaultGroupSettings.setSendMessageDenyNotification(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "sendMessageDenyNotification", "true"));
        LOG.debug("Google Apps Consumer - Setting sendMessageDenyNotification to {}", defaultGroupSettings.getSendMessageDenyNotification());

        defaultGroupSettings.setDefaultMessageDenyNotificationText(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "defaultMessageDenyNotificationText", "Your message has been denied."));
        LOG.debug("Google Apps Consumer - Setting defaultMessageDenyNotificationText to {}", defaultGroupSettings.getDefaultMessageDenyNotificationText());

        defaultGroupSettings.setShowInGroupDirectory(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "showInGroupDirectory", "false"));
        LOG.debug("Google Apps Consumer - Setting showInGroupDirectory to {}", defaultGroupSettings.getShowInGroupDirectory());

        defaultGroupSettings.setAllowGoogleCommunication(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "allowGoogleCommunication", "false"));
        LOG.debug("Google Apps Consumer - Setting allowGoogleCommunication to {}", defaultGroupSettings.getAllowGoogleCommunication());

        defaultGroupSettings.setMembersCanPostAsTheGroup(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "membersCanPostAsTheGroup", "false"));
        LOG.debug("Google Apps Consumer - Setting membersCanPostAsTheGroup to {}", defaultGroupSettings.getMembersCanPostAsTheGroup());

        defaultGroupSettings.setMessageDisplayFont(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "messageDisplayFont", "DEFAULT_FONT"));
        LOG.debug("Google Apps Consumer - Setting messageDisplayFont to {}", defaultGroupSettings.getMessageDisplayFont());

        defaultGroupSettings.setIncludeInGlobalAddressList(
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "includeInGlobalAddressList", "true"));
        LOG.debug("Google Apps Consumer - Setting includeInGlobalAddressList to {}", defaultGroupSettings.getIncludeInGlobalAddressList());

        retryOnError = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(PARAMETER_NAMESPACE + "retryOnError", false);
        LOG.debug("Google Apps Consumer - Setting retryOnError to {}", retryOnError);

        googleGroupFilter = GrouperLoaderConfig.retrieveConfig().propertyValueString(PARAMETER_NAMESPACE + "googleGroupFilter", ".*");
        LOG.debug("Google Apps Consumer - Setting googleGroupFilter to {}", googleGroupFilter);

        ignoreExtraGoogleMembers = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(PARAMETER_NAMESPACE + "ignoreExtraGoogleMembers", true);
        LOG.debug("Google Apps Consumer - Setting ignoreExtraGoogleMembers to {}", ignoreExtraGoogleMembers);

    }

    public boolean isRetryOnError() {
        return retryOnError;
    }

    public String getHandleDeletedGroup() {
        return handleDeletedGroup;
    }

    public boolean shouldIncludeUserInGlobalAddressList() {
        return includeUserInGlobalAddressList;
    }

    public Groups getDefaultGroupSettings() {
        return defaultGroupSettings;
    }

    public String getSubjectSurnameField() {
        return subjectSurnameField;
    }

    public String getSubjectGivenNameField() {
        return subjectGivenNameField;
    }

    public boolean useSimpleSubjectNaming() {
        return simpleSubjectNaming;
    }

    public boolean shouldDeprovisionUsers() {
        return deprovisionUsers;
    }

    public boolean shouldProvisionUsers() {
        return provisionUsers;
    }

    public int getGoogleGroupCacheValidity() {
        return googleGroupCacheValidity;
    }

    public int getGoogleUserCacheValidity() {
        return googleUserCacheValidity;
    }

    public boolean getprefillGoogleCachesForConsumer() {
        return prefillGoogleCachesForConsumer;
    }

    public boolean getprefillGoogleCachesForFullSync() {
        return prefillGoogleCachesForFullSync;
    }

    public String getSubjectIdentifierExpression() {
        return subjectIdentifierExpression;
    }

    public String getGroupIdentifierExpression() {
        return groupIdentifierExpression;
    }

    public String getGoogleDomain() {
        return googleDomain;
    }

    public String getServiceImpersonationUser() {
        return serviceImpersonationUser;
    }

    public String getServiceAccountEmail() {
        return serviceAccountEmail;
    }

    public String getServiceAccountPKCS12FilePath() {
        return serviceAccountPKCS12FilePath;
    }

    public String getWhoCanManage() {
        return whoCanManage;
    }

    public String getGoogleGroupFilter() { return googleGroupFilter; }

    public boolean shouldIgnoreExtraGoogleMembers() { return ignoreExtraGoogleMembers; }

    public int getRecentlyManipulatedQueueSize() {
        return recentlyManipulatedQueueSize;
    }

    public int getRecentlyManipulatedQueueDelay() {
        return recentlyManipulatedQueueDelay;
    }
}
