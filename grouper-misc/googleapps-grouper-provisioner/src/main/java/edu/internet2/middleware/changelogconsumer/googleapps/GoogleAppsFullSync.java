/*******************************************************************************
 * Copyright 2015 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.changelogconsumer.googleapps;

import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.User;
import edu.internet2.middleware.changelogconsumer.googleapps.cache.GoogleCacheManager;
import edu.internet2.middleware.changelogconsumer.googleapps.utils.ComparableGroupItem;
import edu.internet2.middleware.changelogconsumer.googleapps.utils.ComparableMemberItem;
import edu.internet2.middleware.changelogconsumer.googleapps.utils.GoogleAppsSyncProperties;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initiates a GoogleAppsFullSync from command-line
 *
 * @author John Gasper, Unicon
 */
public class GoogleAppsFullSync {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleAppsFullSync.class);

    /** "Boolean" used to delay change log processing when a full sync is running. */
    private static final HashMap<String, String> fullSyncIsRunning = new HashMap<String, String>();
    private static final Object fullSyncIsRunningLock = new Object();

    private GoogleGrouperConnector connector;
    private String consumerName;
    private GoogleAppsSyncProperties properties;

    public GoogleAppsFullSync(String consumerName) {
        this.consumerName = consumerName;
    }

    public static void main(String[] args) {
        if (args.length == 0 ) {
            System.console().printf("Google Change Log Consumer Name must be provided\n");
            System.console().printf("*nix: googleAppsFullSync.sh consumerName [--dry-run]\n");
            System.console().printf("Windows: googleAppsFullSync.bat consumerName [--dry-run]\n");

            System.exit(-1);
        }

        try {
            GoogleAppsFullSync googleAppsFullSync = new GoogleAppsFullSync(args[0]);
            googleAppsFullSync.process(args.length > 1 && args[1].equalsIgnoreCase("--dry-run"));

        } catch (Exception e) {
            System.console().printf(e.toString() + ": \n");
            e.printStackTrace();
        }

        System.exit(0);
    }

    public static boolean isFullSyncRunning(String consumerName) {
        synchronized (fullSyncIsRunningLock) {
            return fullSyncIsRunning.get(consumerName) != null && Boolean.valueOf(fullSyncIsRunning.get(consumerName));
        }
    }

    /**
     * Runs a fullSync.
     * @param dryRun indicates that this is dryRun
     */
    public void process(boolean dryRun) {

        synchronized (fullSyncIsRunningLock) {
            fullSyncIsRunning.put(consumerName, Boolean.toString(true));
        }

        connector = new GoogleGrouperConnector();

        //Start with a clean cache
        GoogleCacheManager.googleGroups().clear();
        GoogleCacheManager.googleUsers().clear();

        properties = new GoogleAppsSyncProperties(consumerName);

        Pattern googleGroupFilter = Pattern.compile(properties.getGoogleGroupFilter());

        try {
            connector.initialize(consumerName, properties);

            if (properties.getprefillGoogleCachesForFullSync()) {
                connector.populateGoogleCache();
            }

        } catch (GeneralSecurityException e) {
            LOG.error("Google Apps Consume '{}' Full Sync - This consumer failed to initialize: {}", consumerName, e.getMessage());
        } catch (IOException e) {
            LOG.error("Google Apps Consume '{}' Full Sync - This consumer failed to initialize: {}", consumerName, e.getMessage());
        }

        GrouperSession grouperSession = null;

        try {
            grouperSession = GrouperSession.startRootSession();
            connector.getGoogleSyncAttribute();
            connector.cacheSyncedGroupsAndStems(true);

            // time context processing
            final StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            //Populate a normalized list (google naming) of Grouper groups
            ArrayList<ComparableGroupItem> grouperGroups = new ArrayList<ComparableGroupItem>();
            for (String groupKey : connector.getSyncedGroupsAndStems().keySet()) {
                if (connector.getSyncedGroupsAndStems().get(groupKey).equalsIgnoreCase("yes")) {
                    edu.internet2.middleware.grouper.Group group = connector.fetchGrouperGroup(groupKey);

                    if (group != null) {
                        grouperGroups.add(new ComparableGroupItem(connector.getAddressFormatter().qualifyGroupAddress(group.getName()), group));
                    }
                }
            }

            //Populate a comparable list of Google groups
            ArrayList<ComparableGroupItem> googleGroups = new ArrayList<ComparableGroupItem>();
            for (String groupName : GoogleCacheManager.googleGroups().getKeySet()) {
                if (googleGroupFilter.matcher(groupName.replace("@" + properties.getGoogleDomain(), "")).find()) {
                    googleGroups.add(new ComparableGroupItem(groupName));
                    LOG.debug("Google Apps Consumer '{}' Full Sync - {} group matches group filter: included", consumerName, groupName);
                } else {
                    LOG.debug("Google Apps Consumer '{}' Full Sync - {} group does not match group filter: ignored", consumerName, groupName);
                }
            }

            //Get our sets
            Collection<ComparableGroupItem> extraGroups = CollectionUtils.subtract(googleGroups, grouperGroups);
            processExtraGroups(dryRun, extraGroups);

            Collection<ComparableGroupItem> missingGroups = CollectionUtils.subtract(grouperGroups, googleGroups);
            processMissingGroups(dryRun, missingGroups);

            Collection<ComparableGroupItem> matchedGroups = CollectionUtils.intersection(grouperGroups, googleGroups);
            processMatchedGroups(dryRun, matchedGroups);

            // stop the timer and log
            stopWatch.stop();
            LOG.debug("Google Apps Consumer '{}' Full Sync - Processed, Elapsed time {}", new Object[] {consumerName, stopWatch});

        } finally {
            GrouperSession.stopQuietly(grouperSession);

            synchronized (fullSyncIsRunningLock) {
                fullSyncIsRunning.put(consumerName, Boolean.toString(true));
            }
        }

    }

    private void processMatchedGroups(boolean dryRun, Collection<ComparableGroupItem> matchedGroups) {
        for (ComparableGroupItem item : matchedGroups) {
            LOG.info("Google Apps Consumer '{}' Full Sync - examining matched group: {} ({})", new Object[]{consumerName, item.getGrouperGroup().getName(), item});

            Group gooGroup = null;
            try {
                gooGroup = connector.fetchGooGroup(item.getName());
            } catch (IOException e) {
                LOG.error("Google Apps Consume '{}' Full Sync - Error fetching matched group ({}): {}", new Object[]{consumerName, item.getName(), e.getMessage()});
            }
            boolean updated = false;

            if (gooGroup == null) {
                LOG.error("Google Apps Consume '{}' Full Sync - Error fetching matched group ({}); it disappeared during processing.", new Object[]{consumerName, item.getName()});
            } else {

                if (!item.getGrouperGroup().getDescription().equalsIgnoreCase(gooGroup.getDescription())) {
                    if (!dryRun) {
                        gooGroup.setDescription(item.getGrouperGroup().getDescription());
                        updated = true;
                    }
                }

                if (!item.getGrouperGroup().getDisplayExtension().equalsIgnoreCase(gooGroup.getName())) {
                    if (!dryRun) {
                        gooGroup.setName(item.getGrouperGroup().getDisplayExtension());
                        updated = true;
                    }
                }

                if (updated) {
                    try {
                        connector.updateGooGroup(item.getName(), gooGroup);
                    } catch (IOException e) {
                        LOG.error("Google Apps Consume '{}' Full Sync - Error updating matched group ({}): {}", new Object[]{consumerName, item.getName(), e.getMessage()});
                    }
                }
                
                try {
                  connector.unarchiveGooGroupIfNecessary(gooGroup);
                } catch (IOException e) {
                  LOG.error("Google Apps Consume '{}' Full Sync - Error checking archive status for matched group ({}): {}", new Object[]{consumerName, item.getName(), e.getMessage()});
                }

                //Retrieve Membership
                ArrayList<ComparableMemberItem> grouperMembers = new ArrayList<ComparableMemberItem>();
                Set<edu.internet2.middleware.grouper.Member> members = new LinkedHashSet<edu.internet2.middleware.grouper.Member>();
                members.addAll(item.getGrouperGroup().getMembers());
                for (Subject subj : item.getGrouperGroup().getUpdaters()) {
                  members.add(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj, false));
                }
                for (Subject subj : item.getGrouperGroup().getAdmins()) {
                  members.add(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj, false));
                }
                for (edu.internet2.middleware.grouper.Member member : members) {
                    if (member.getSubjectType() == SubjectTypeEnum.PERSON) {
                      String role = connector.determineRole(member, item.getGrouperGroup());
                      if (role != null) {
                        grouperMembers.add(new ComparableMemberItem(connector.getAddressFormatter().qualifySubjectAddress(member.getSubjectId()), member));
                      }
                    }
                }

                ArrayList<ComparableMemberItem> googleMembers = new ArrayList<ComparableMemberItem>();
                List<Member> memberList = null;

                try {
                    memberList = connector.getGooMembership(item.getName());
                } catch (IOException e) {
                    LOG.error("Google Apps Consume '{}' Full Sync - Error fetching membership list for group({}): {}", new Object[]{consumerName, item.getName(), e.getMessage()});
                }

                if (memberList == null) {
                    LOG.error("Google Apps Consume '{}' Full Sync - Error fetching membership list for group ({}); it's null", new Object[]{consumerName, item.getName()});

                } else {
                    for (Member member : memberList) {
                        googleMembers.add(new ComparableMemberItem(member.getEmail()));
                    }

                    Collection<ComparableMemberItem> extraMembers = CollectionUtils.subtract(googleMembers, grouperMembers);
                    if (!properties.shouldIgnoreExtraGoogleMembers()) {
                        processExtraGroupMembers(item, extraMembers, dryRun);
                    }

                    Collection<ComparableMemberItem> missingMembers = CollectionUtils.subtract(grouperMembers, googleMembers);
                    processMissingGroupMembers(item, missingMembers, gooGroup, dryRun);

                    Collection<ComparableMemberItem> matchedMembers = CollectionUtils.intersection(grouperMembers, googleMembers);
                    processMatchedGroupMembers(item, matchedMembers, dryRun);
                }
            }
        }
    }

    private void processMatchedGroupMembers(ComparableGroupItem group, Collection<ComparableMemberItem> matchedMembers, boolean dryRun) {
        for (ComparableMemberItem member : matchedMembers) {
            if (!dryRun) {
                edu.internet2.middleware.grouper.Member grouperMember = member.getGrouperMember();

                try {
                    connector.updateGooMember(group.getGrouperGroup(), grouperMember.getSubject(), connector.determineRole(grouperMember, group.getGrouperGroup()));
                } catch (IOException e) {
                    LOG.error("Google Apps Consume '{}' Full Sync - Error updating existing user ({}) from existing group ({}): {}", new Object[]{consumerName, member.getEmail(), group.getName(), e.getMessage()});
                }
            }
        }
    }

    private void processMissingGroupMembers(ComparableGroupItem group, Collection<ComparableMemberItem> missingMembers, Group gooGroup, boolean dryRun) {
        for (ComparableMemberItem member : missingMembers) {
            LOG.info("Google Apps Consume '{}' Full Sync - Creating missing user/member ({}) from extra group ({}).", new Object[]{consumerName, member.getEmail(), group.getName()});
            if (!dryRun) {
                Subject subject = connector.fetchGrouperSubject(member.getGrouperMember().getSubjectSourceId(), member.getGrouperMember().getSubjectId());
                if (subject == null) {
                    continue;
                }
                User user = connector.fetchGooUser(member.getEmail());

                if (user == null) {
                    try {
                        user = connector.createGooUser(subject);
                    } catch (IOException e) {
                        LOG.error("Google Apps Consume '{}' Full Sync - Error creating missing user ({}) from extra group ({}): {}", new Object[]{consumerName, member.getEmail(), group.getName(), e.getMessage()});
                    }
                }

                if (user != null) {
                    try {
                        connector.createGooMember(gooGroup, user, connector.determineRole(member.getGrouperMember(), group.getGrouperGroup()));
                    } catch (IOException e) {
                        LOG.error("Google Apps Consume '{}' Full Sync - Error creating missing member ({}) from extra group ({}): {}", new Object[]{consumerName, member.getEmail(), group.getName(), e.getMessage()});
                    }
                }
            }
        }
    }

    private void processExtraGroupMembers(ComparableGroupItem group, Collection<ComparableMemberItem> extraMembers, boolean dryRun) {
        for (ComparableMemberItem member : extraMembers) {
            LOG.info("Google Apps Consume '{}' Full Sync - Removing extra member ({}) from matched group ({})", new Object[]{consumerName, member.getEmail(), group.getName()});
            if (!dryRun) {
                try {
                    connector.removeGooMembership(group.getName(), member.getEmail());
                } catch (IOException e) {
                    LOG.warn("Google Apps Consume '{}' - Error removing membership ({}) from Google Group ({}): {}", new Object[]{consumerName, member.getEmail(), group.getName(), e.getMessage()});
                }
            }
        }
    }

    private void processMissingGroups(boolean dryRun, Collection<ComparableGroupItem> missingGroups) {
        for (ComparableGroupItem item : missingGroups) {
            LOG.info("Google Apps Consumer '{}' Full Sync - adding missing Google group: {} ({})", new Object[] {consumerName, item.getGrouperGroup().getName(), item});

            if (!dryRun) {
                try {
                    connector.createGooGroupIfNecessary(item.getGrouperGroup());
                } catch (IOException e) {
                    LOG.error("Google Apps Consume '{}' Full Sync - Error adding missing group ({}): {}", new Object[]{consumerName, item.getName(), e.getMessage()});
                }
            }
        }
    }

    private void processExtraGroups(boolean dryRun, Collection<ComparableGroupItem> extraGroups) {
        for (ComparableGroupItem item : extraGroups) {
          if (!properties.shouldIgnoreExtraGoogleGroups()) {
            LOG.info("Google Apps Consumer '{}' Full Sync - removing extra Google group: {}", consumerName, item);

            if (!dryRun) {
                try {
                    connector.deleteGooGroupByEmail(item.getName());
                } catch (IOException e) {
                    LOG.error("Google Apps Consume '{}' Full Sync - Error removing extra group ({}): {}", new Object[]{consumerName, item.getName(), e.getMessage()});
                }
            }
          } else {
            LOG.info("Google Apps Consumer '{}' Full Sync - ignoring extra Google group: {}", consumerName, item);
          }
        }
    }

}
