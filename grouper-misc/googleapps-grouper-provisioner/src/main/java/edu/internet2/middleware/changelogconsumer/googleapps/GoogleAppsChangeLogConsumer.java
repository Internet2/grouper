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

package edu.internet2.middleware.changelogconsumer.googleapps;

import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.User;
import edu.internet2.middleware.changelogconsumer.googleapps.cache.GoogleCacheManager;
import edu.internet2.middleware.changelogconsumer.googleapps.utils.GoogleAppsSyncProperties;
import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.changeLog.*;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;
import java.io.IOException;
import java.util.*;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A {@link ChangeLogConsumer} which provisions via Google Apps API.
 *
 * @author John Gasper, Unicon
 **/
public class GoogleAppsChangeLogConsumer extends ChangeLogConsumerBase {

    /** Maps change log entry category and action (change log type) to methods. */
    enum EventType {

        /** Process the add attribute assign value change log entry type. */
        attributeAssign__addAttributeAssign {
            /** {@inheritDoc} */
            public void process(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
                consumer.processAttributeAssignAdd(consumer, changeLogEntry);
            }
        },

        /** Process the delete attribute assign value change log entry type. */
        attributeAssign__deleteAttributeAssign {
            /** {@inheritDoc} */
            public void process(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
                consumer.processAttributeAssignDelete(consumer, changeLogEntry);
            }
        },

        /** Process the add group change log entry type. */
        group__addGroup {
            /** {@inheritDoc} */
            public void process(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
                consumer.processGroupAdd(consumer, changeLogEntry);
            }
        },

        /** Process the delete group change log entry type. */
        group__deleteGroup {
            /** {@inheritDoc} */
            public void process(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
                consumer.processGroupDelete(consumer, changeLogEntry);
            }
        },

        /** Process the update group change log entry type. */
        group__updateGroup {
            /** {@inheritDoc} */
            public void process(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
                consumer.processGroupUpdate(consumer, changeLogEntry);
            }
        },

        /** Process the add membership change log entry type. */
        membership__addMembership {
            /** {@inheritDoc} */
            public void process(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
                consumer.processMembershipAdd(consumer, changeLogEntry);
            }
        },

        /** Process the delete membership change log entry type. */
        membership__deleteMembership {
            /** {@inheritDoc} */
            public void process(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
                consumer.processMembershipDelete(consumer, changeLogEntry);
            }
        },

        privilege__addPrivilege {
            public void process(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
                consumer.processPrivilegeAdd(consumer, changeLogEntry);
            }
        },

        privilege__deletePrivilege {
            public void process(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
                consumer.processPrivilegeDelete(consumer, changeLogEntry);
            }
        },

        privilege__updatePrivilege {
            public void process(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
                consumer.processPrivilegeUpdate(consumer, changeLogEntry);
            }
        },

        /** Process the delete stem change log entry type. */
        stem__deleteStem {
            /** {@inheritDoc} */
            public void process(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception {
                consumer.processStemDelete(consumer, changeLogEntry);
            }
        },
        ;

        /**
         * Process the change log entry.
         *
         * @param consumer the google change log consumer
         * @param changeLogEntry the change log entry
         * @throws Exception if any error occurs
         */
        public abstract void process(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) throws Exception;
    }

    private static final Logger LOG = LoggerFactory.getLogger(GoogleAppsFullSync.class);

    /** The change log consumer name from the processor metadata. */
    private String consumerName;
    private AttributeDefName syncAttribute;
    private GoogleGrouperConnector connector;


    public GoogleAppsChangeLogConsumer() {
        LOG.trace("Google Apps Consumer - new instances starting up");

        connector = new GoogleGrouperConnector();
    }

    /** {@inheritDoc} */
    @Override
    public long processChangeLogEntries(final List<ChangeLogEntry> changeLogEntryList,
                                        ChangeLogProcessorMetadata changeLogProcessorMetadata) {

        LOG.debug("Google Apps Consumer - waking up");

        // the change log sequence number to return
        long sequenceNumber = -1;

        // initialize this consumer's consumerName from the change log metadata
        if (consumerName == null) {
            consumerName = changeLogProcessorMetadata.getConsumerName();
            LOG.trace("Google Apps Consumer '{}' - Setting name.", consumerName);
        }

        GoogleAppsSyncProperties properties = new GoogleAppsSyncProperties(consumerName);

        try {
            connector.initialize(consumerName, properties);

            if (properties.getprefillGoogleCachesForConsumer()) {
                connector.populateGoogleCache();
            }

        } catch (Exception e) {
            LOG.error("Google Apps Consumer '{}' - This consumer failed to initialize: {}", consumerName, e.getMessage());
            return changeLogEntryList.get(0).getSequenceNumber() - 1;
        }

        GrouperSession grouperSession = null;
        try {

            grouperSession = GrouperSession.startRootSession();
            syncAttribute = connector.getGoogleSyncAttribute();
            connector.cacheSyncedGroupsAndStems();

            // time context processing
            final StopWatch stopWatch = new StopWatch();

            // the last change log sequence number processed
            String lastContextId = null;

            LOG.debug("Google Apps Consumer '{}' - Processing change log entry list size '{}'", consumerName, changeLogEntryList.size());

            // process each change log entry
            for (ChangeLogEntry changeLogEntry : changeLogEntryList) {

                // return the current change log sequence number
                sequenceNumber = changeLogEntry.getSequenceNumber();

                // if full sync is running, return the previous sequence number to process this entry on the next run
                boolean isFullSyncRunning = GoogleAppsFullSync.isFullSyncRunning(consumerName);

                if (isFullSyncRunning) {
                    LOG.info("Google Apps Consumer '{}' - Full sync is running, returning sequence number '{}'", consumerName,
                            sequenceNumber - 1);
                    return sequenceNumber - 1;
                }

                // if first run, start the stop watch and store the last sequence number
                if (lastContextId == null) {
                    stopWatch.start();
                    lastContextId = changeLogEntry.getContextId();
                }

                // whether or not an exception was thrown during processing of the change log entry
                boolean errorOccurred = false;

                try {
                    // process the change log entry
                    processChangeLogEntry(changeLogEntry);

                } catch (Exception e) {
                    errorOccurred = true;
                    String message =
                            "Google Apps Consumer '" + consumerName + "' - An error occurred processing sequence number " + sequenceNumber;
                    LOG.error(message, e);
                    changeLogProcessorMetadata.registerProblem(e, message, sequenceNumber);
                    changeLogProcessorMetadata.setHadProblem(true);
                    changeLogProcessorMetadata.setRecordException(e);
                    changeLogProcessorMetadata.setRecordExceptionSequence(sequenceNumber);
                }

                // if the change log context id has changed, log and restart stop watch
                if (!lastContextId.equals(changeLogEntry.getContextId())) {
                    stopWatch.stop();
                    LOG.debug("Google Apps Consumer '{}' - Processed change log context '{}' Elapsed time {}", new Object[] {consumerName,
                            lastContextId, stopWatch,});
                    stopWatch.reset();
                    stopWatch.start();
                }

                lastContextId = changeLogEntry.getContextId();

                // if an error occurs and retry on error is true, return the current sequence number minus 1
                /* Whether or not to retry a change log entry if an error occurs. */
                boolean retryOnError = properties.isRetryOnError();
                if (errorOccurred && retryOnError) {
                    sequenceNumber--;
                    break;
                }
            }

            // stop the timer and log
            stopWatch.stop();
            LOG.debug("Google Apps Consumer '{}' - Processed change log context '{}' Elapsed time {}", new Object[] {consumerName,
                    lastContextId, stopWatch,});

        } finally {
            GrouperSession.stopQuietly(grouperSession);
        }

        if (sequenceNumber == -1) {
            LOG.error("Google Apps Consumer '" + consumerName + "' - Unable to process any records.");
            throw new RuntimeException("Google Apps Consumer '" + consumerName + "' - Unable to process any records.");
        }

        LOG.debug("Google Apps Consumer '{}' - Finished processing change log entries. Last sequence number '{}'", consumerName,
                sequenceNumber);

        // return the sequence number
        return sequenceNumber;
    }

    /**
     * Call the method of the {@link EventType} enum which matches the {@link ChangeLogEntry} category and action (the
     * change log type).
     *
     * @param changeLogEntry the change log entry
     * @throws Exception if an error occurs processing the change log entry
     */
    protected void processChangeLogEntry(ChangeLogEntry changeLogEntry) throws Exception {
        try {
            // find the method to run via the enum
            final String enumKey = changeLogEntry.getChangeLogType().getChangeLogCategory() + "__"
                    + changeLogEntry.getChangeLogType().getActionName();

            final EventType eventType = EventType.valueOf(enumKey);

            if (eventType == null) {
                LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Unsupported category and action.", consumerName,
                        toString(changeLogEntry));
            } else {
                // process the change log event
                LOG.info("Google Apps Consumer '{}' - Change log entry '{}'", consumerName, toStringDeep(changeLogEntry));
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                eventType.process(this, changeLogEntry);

                stopWatch.stop();
                LOG.info("Google Apps Consumer '{}' - Change log entry '{}' Finished processing. Elapsed time {}",
                        new Object[] {consumerName, toString(changeLogEntry), stopWatch,});

            }

        } catch (IllegalArgumentException e) {
            LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Unsupported category and action.", consumerName,
                    toString(changeLogEntry));
        }
    }

    /**
     * Add an attribute.
     *
     * @param consumer the change log consumer
     * @param changeLogEntry the change log entry
     */
    protected void processAttributeAssignAdd(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) {

        LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Processing add attribute assign value.", consumerName,
                toString(changeLogEntry));

        final String attributeDefNameId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameId);
        final String assignType = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.assignType);
        final String ownerId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId1);

        if (syncAttribute.getId().equalsIgnoreCase(attributeDefNameId)) {

            if (AttributeAssignType.valueOf(assignType) == AttributeAssignType.group) {
                final edu.internet2.middleware.grouper.Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), ownerId, false);

                try {
                    connector.createGooGroupIfNecessary(group);
                } catch (IOException e) {
                    LOG.error("Google Apps Consumer '{}' - Change log entry '{}' Error processing group add: {}", new Object[] {consumerName, toString(changeLogEntry), e});
                }

            } else if (AttributeAssignType.valueOf(assignType) == AttributeAssignType.stem) {
                final Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), ownerId, false);
                final Set<edu.internet2.middleware.grouper.Group> groups = stem.getChildGroups(Scope.SUB);

                for (edu.internet2.middleware.grouper.Group group : groups) {
                    try {
                        connector.createGooGroupIfNecessary(group);
                    } catch (IOException e) {
                        LOG.error("Google Apps Consumer '{}' - Change log entry '{}' Error processing group add, continuing: {}", new Object[] {consumerName, toString(changeLogEntry), e});
                    }
                }
            }
        }
    }

    /**
     * Delete an attribute.
     *
     * @param consumer the change log consumer
     * @param changeLogEntry the change log entry
     */
    protected void processAttributeAssignDelete(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry)  {

        LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Processing delete attribute assign value.", consumerName,
                toString(changeLogEntry));

        final String attributeDefNameId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameId);
        final String assignType = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.assignType);
        final String ownerId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.ownerId1);

        if (syncAttribute.getId().equalsIgnoreCase(attributeDefNameId)) {

            if (AttributeAssignType.valueOf(assignType) == AttributeAssignType.group) {
                final edu.internet2.middleware.grouper.Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), ownerId, false);

                try {
                    connector.deleteGooGroup(group);
                } catch (IOException e) {
                    LOG.error("Google Apps Consumer '{}' - Change log entry '{}' Error processing group add: {}", new Object[] {consumerName, toString(changeLogEntry), e});
                }

            } else if (AttributeAssignType.valueOf(assignType) == AttributeAssignType.stem) {
                final Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), ownerId, false);
                final Set<edu.internet2.middleware.grouper.Group> groups = stem.getChildGroups(Scope.SUB);

                for (edu.internet2.middleware.grouper.Group group : groups) {
                    try {
                        connector.deleteGooGroup(group);
                    } catch (IOException e) {
                        LOG.error("Google Apps Consumer '{}' - Change log entry '{}' Error processing group add, continuing: {}", new Object[] {consumerName, toString(changeLogEntry), e});
                    }
                }
            }
        }
    }

    /**
     * Add a group.
     *
     * @param consumer the change log consumer
     * @param changeLogEntry the change log entry
     */
    protected void processGroupAdd(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) {

        LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Processing group add.", consumerName, toString(changeLogEntry));

        final String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.name);
        final edu.internet2.middleware.grouper.Group group = connector.fetchGrouperGroup(groupName);

        if (!connector.shouldSyncGroup(group)) {
            LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Skipping group add, nothing to do cause the group is not flagged or is gone.", consumerName,
                    toString(changeLogEntry));
            return;
        }

        try {
            connector.createGooGroupIfNecessary(group);
        } catch (IOException e) {
            LOG.error("Google Apps Consumer '{}' - Change log entry '{}' Error processing group add: {}",  new Object[] {consumerName, toString(changeLogEntry), e});
        }

    }

    /**
     * Delete a group.
     *
     * @param consumer the change log consumer
     * @param changeLogEntry the change log entry
     */
    protected void processGroupDelete(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) {

        LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Processing group delete.", consumerName, toString(changeLogEntry));

        final String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_DELETE.name);
        final edu.internet2.middleware.grouper.Group grouperGroup = connector.fetchGrouperGroup(groupName);

        if (!connector.shouldSyncGroup(grouperGroup)) {
            LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Skipping group delete, nothing to do cause the group is not flagged or is gone.", consumerName,
                    toString(changeLogEntry));
            return;
        }

        try {
            connector.deleteGooGroup(grouperGroup);
        } catch (IOException e) {
            LOG.error("Google Apps Consumer '{}' - Change log entry '{}' Error processing group delete: {}", new Object[] {consumerName, toString(changeLogEntry), e.getMessage()});
        }
    }

    /**
     * Update a group.
     *
     * @param consumer the change log consumer
     * @param changeLogEntry the change log entry
     */
    protected void processGroupUpdate(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) {

        LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Processing group update.", consumerName, toString(changeLogEntry));

        final String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.name);
        final String propertyChanged = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyChanged);
        final String propertyOldValue = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyOldValue);
        final String propertyNewValue = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyNewValue);

        Group group;
        edu.internet2.middleware.grouper.Group grouperGroup;

        try {
            grouperGroup = connector.fetchGrouperGroup(groupName);
            if (!connector.shouldSyncGroup(grouperGroup)) {
                LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Skipping group update, nothing to do cause the group is not flagged or is gone.", consumerName,
                        toString(changeLogEntry));
                return;
            }

            //Group moves are a bit different than just a property change, let's take care of it now.
            if (propertyChanged.equalsIgnoreCase("name")) {
                String oldAddress = connector.getAddressFormatter().qualifyGroupAddress(propertyOldValue);
                String newAddress = connector.getAddressFormatter().qualifyGroupAddress(propertyNewValue);

                group = connector.fetchGooGroup(oldAddress);

                if (group != null) {
                    group.setEmail(newAddress);

                    if (group.getAliases() == null) {
                        group.setAliases(new ArrayList<String>(1));
                    }

                    group.getAliases().add(oldAddress);

                    connector.getSyncedGroupsAndStems().remove(groupName);
                    GoogleCacheManager.googleGroups().remove(oldAddress);
                    GoogleCacheManager.googleGroups().put(connector.updateGooGroup(oldAddress, group));
                }

                return;
            }

            group = connector.fetchGooGroup(connector.getAddressFormatter().qualifyGroupAddress(groupName));

            if (propertyChanged.equalsIgnoreCase("displayExtension")) {
                group.setName(propertyNewValue);

            } else if (propertyChanged.equalsIgnoreCase("description")) {
                group.setDescription(propertyNewValue);

            } else {
                LOG.warn("Google Apps Consumer '{}' - Change log entry '{}' Unmapped group property updated {}.",
                        new Object[] {consumerName, toString(changeLogEntry), propertyChanged});
            }

            GoogleCacheManager.googleGroups().put(connector.updateGooGroup(connector.getAddressFormatter().qualifyGroupAddress(groupName), group));

        } catch (IOException e) {
            LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Error processing group update.", consumerName, toString(changeLogEntry));
        }
    }

    /**
     * Add a membership.
     *
     * @param consumer the change log consumer
     * @param changeLogEntry the change log entry
     */
    protected void processMembershipAdd(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) {

        LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Processing membership add.", consumerName,
                toString(changeLogEntry));

        final String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName);
        final String memberId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.memberId);
        final edu.internet2.middleware.grouper.Group grouperGroup = connector.fetchGrouperGroup(groupName);
        final Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, false);

        if (!connector.shouldSyncGroup(grouperGroup)) {
            LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Skipping membership add, nothing to do cause the group is not flagged or is gone.", consumerName,
                    toString(changeLogEntry));
            return;
        }

        final String subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId);
        final String sourceId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId);
        final Subject lookupSubject = connector.fetchGrouperSubject(sourceId, subjectId);
        final SubjectType subjectType = lookupSubject.getType();

        try {
            Group group = connector.fetchGooGroup(connector.getAddressFormatter().qualifyGroupAddress(groupName));
            if (group == null) {
                connector.createGooGroupIfNecessary(grouperGroup);
                group = connector.fetchGooGroup(connector.getAddressFormatter().qualifyGroupAddress(groupName));
            }

            //For nested groups, ChangeLogEvents fire when the group is added, and also for each indirect user added,
            //so we only need to handle PERSON events.
            if (subjectType == SubjectTypeEnum.PERSON) {
                User user = connector.fetchGooUser(connector.getAddressFormatter().qualifySubjectAddress(subjectId));
                if (user == null) {
                    user = connector.createGooUser(lookupSubject);
                }

                if (user != null) {
                    connector.createGooMember(group, user, connector.determineRole(member, grouperGroup));
                }
            }

        } catch (IOException e) {
            LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Error processing membership add failed: {}", new Object[] {consumerName,
                    toString(changeLogEntry), e});
        }
    }

    /**
     * Delete a membership entry.
     *
     * @param consumer the change log consumer
     * @param changeLogEntry the change log entry
     */
    protected void processMembershipDelete(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) {

        LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Processing membership delete.", consumerName,
                toString(changeLogEntry));

        final String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName);
        final edu.internet2.middleware.grouper.Group grouperGroup = connector.fetchGrouperGroup(groupName);

        if (!connector.shouldSyncGroup(grouperGroup)) {
            LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Skipping membership delete, nothing to do cause the group is not flagged or is gone.", consumerName,
                    toString(changeLogEntry));
            return;
        }

        final String subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId);
        final String sourceId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId);
        final Subject lookupSubject = connector.fetchGrouperSubject(sourceId, subjectId);
        final SubjectType subjectType = lookupSubject.getType();

        //For nested groups, ChangeLogEvents fire when the group is removed, and also for each indirect user added,
        //so we only need to handle PERSON events.
        if (subjectType == SubjectTypeEnum.PERSON) {
            try {
                connector.removeGooMembership(groupName, lookupSubject);
            } catch (IOException e) {
                LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Error processing membership delete: {}", new Object[]{consumerName,
                        toString(changeLogEntry), e});
            }
        }
    }

    protected void processPrivilegeAdd(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) {
        final String ROLE = "MEMBER"; //Other types are ADMIN and OWNER. Neither makes sense for managed groups.

        LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Processing privilege add.", consumerName,
                toString(changeLogEntry));

        final String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName);
        final String privilegeName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName);
        final String memberId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.memberId);

        final edu.internet2.middleware.grouper.Group grouperGroup = connector.fetchGrouperGroup(groupName);
        final Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, false);

        if (!connector.shouldSyncGroup(grouperGroup)) {
            LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Skipping privilege add, nothing to do cause the group is not flagged or is gone.", consumerName,
                    toString(changeLogEntry));

            return;
        }

        if (member.getSubjectType() == SubjectTypeEnum.PERSON) {
            try {
                connector.createGooMember(grouperGroup, member.getSubject(), connector.determineRole(member, grouperGroup));
            } catch (IOException e) {
                LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Error processing privilege add: {}", new Object[]{consumerName,
                        toString(changeLogEntry), e});
            }
        }

    }

    /**
     * Update a privilege entry.
     *
     * @param consumer the change log consumer
     * @param changeLogEntry the change log entry
     */
    protected void processPrivilegeUpdate(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) {

        LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Processing privilege update.", consumerName,
                toString(changeLogEntry));

        final String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_UPDATE.ownerName);
        final String privilegeName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_UPDATE.privilegeName);
        final String memberId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_UPDATE.id);

        final edu.internet2.middleware.grouper.Group grouperGroup = connector.fetchGrouperGroup(groupName);
        final Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, false);

        if (!connector.shouldSyncGroup(grouperGroup)) {
            LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Skipping privilege update, nothing to do cause the group is not flagged or is gone.", consumerName,
                    toString(changeLogEntry));

            return;
        }

        if (member.getSubjectType() == SubjectTypeEnum.PERSON) {
            try {
                connector.updateGooMember(grouperGroup, member.getSubject(), connector.determineRole(member, grouperGroup));
            } catch (IOException e) {
                LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Error processing privilege update: {}", new Object[]{consumerName,
                        toString(changeLogEntry), e});
            }
        }

    }

    /**
     * Delete a privilege entry.
     *
     * @param consumer the change log consumer
     * @param changeLogEntry the change log entry
     */
    protected void processPrivilegeDelete(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) {

        LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Processing privilege delete.", consumerName,
                toString(changeLogEntry));

        final String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName);
        final String privilegeName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName);
        final String memberId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.memberId);

        final edu.internet2.middleware.grouper.Group grouperGroup = connector.fetchGrouperGroup(groupName);
        final Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, false);

        if (!connector.shouldSyncGroup(grouperGroup)) {
            LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Skipping privilege delete, nothing to do cause the group is not flagged or is gone", consumerName,
                    toString(changeLogEntry));

            return;
        }

        if (member.getSubjectType() == SubjectTypeEnum.PERSON) {
            try {
                if (grouperGroup.hasMember(member.getSubject())) {
                    connector.updateGooMember(grouperGroup, member.getSubject(), connector.determineRole(member, grouperGroup));
                } else {
                    connector.removeGooMembership(grouperGroup.getName(), member.getSubject());
                }

            } catch (IOException e) {
                LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Error processing privilege delete: {}",
                        new Object[]{consumerName, toString(changeLogEntry), e});
            }
        }

    }

    /**
     * Delete a stem, but we generally don't care since the stem has to be empty before it can be deleted.
     *
     * @param consumer the change log consumer
     * @param changeLogEntry the change log entry
     */
    protected void processStemDelete(GoogleAppsChangeLogConsumer consumer, ChangeLogEntry changeLogEntry) {

        LOG.debug("Google Apps Consumer '{}' - Change log entry '{}' Processing stem delete.", consumerName, toString(changeLogEntry));

        final String stemName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_DELETE.name);

        connector.getSyncedGroupsAndStems().remove(stemName);
    }

    /**
     * Gets a simple string representation of the change log entry.
     *
     * @param changeLogEntry the change log entry
     * @return the simple string representation of the change log entry
     */
    private static String toString(ChangeLogEntry changeLogEntry) {
        final ToStringBuilder toStringBuilder = new ToStringBuilder(changeLogEntry, ToStringStyle.SHORT_PREFIX_STYLE);
        toStringBuilder.append("timestamp", changeLogEntry.getCreatedOn())
                .append("sequence", changeLogEntry.getSequenceNumber())
                .append("category", changeLogEntry.getChangeLogType().getChangeLogCategory())
                .append("actionName", changeLogEntry.getChangeLogType().getActionName())
                .append("contextId", changeLogEntry.getContextId());
        return toStringBuilder.toString();
    }

    /**
     * Gets a deep string representation of the change log entry.
     *
     * @param changeLogEntry the change log entry
     * @return the deep string representation of the change log entry
     */
    private static String toStringDeep(ChangeLogEntry changeLogEntry) {
        final ToStringBuilder toStringBuilder = new ToStringBuilder(changeLogEntry, ToStringStyle.SHORT_PREFIX_STYLE);
        toStringBuilder.append("timestamp", changeLogEntry.getCreatedOn())
                .append("sequence", changeLogEntry.getSequenceNumber())
                .append("category", changeLogEntry.getChangeLogType().getChangeLogCategory())
                .append("actionName", changeLogEntry.getChangeLogType().getActionName())
                .append("contextId", changeLogEntry.getContextId());

        final ChangeLogType changeLogType = changeLogEntry.getChangeLogType();

        for (String label : changeLogType.labels()) {
            toStringBuilder.append(label, changeLogEntry.retrieveValueForLabel(label));
        }

        return toStringBuilder.toString();
    }

}
