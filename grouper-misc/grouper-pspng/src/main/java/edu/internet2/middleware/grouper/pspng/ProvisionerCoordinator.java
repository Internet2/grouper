package edu.internet2.middleware.grouper.pspng;

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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class coordinates the efforts of a provsioner's full-sync and incremental-sync
 * processes. One of these classes is created by the ProvisionerFactory for each named provisioner
 * and then shared among the incremental and full-sync provisioners.
 *
 * The goal of this class is to prevent the full- and incremental-provisioners of a target
 * from processing the same group at the same time. It will also eventually serve as the
 * way to skip incremental events that were created before the last full-sync occurred.
 */
public class ProvisionerCoordinator {
    private final static Logger LOG = LoggerFactory.getLogger(ProvisionerCoordinator.class);
    private final static int COORDINATION_TIMEOUT_SECS = 300;
    private final static int COORDINATION_UPDATE_SECS = 10;

    final String provisionerName;

    ConcurrentMap<GrouperGroupInfo, ProvisioningStatus> group2ProvisioningStatus
            = new ConcurrentHashMap<>();

    private class ProvisioningStatus {
        final GrouperGroupInfo group;
        Date lastFullSyncStart;
        Date lastSuccessfulFullSyncStart;
        boolean isBeingFullSynced = false;

        Date lastIncrementalProvisioningStart;
        boolean isBeingIncrementallyProvisioned = false;

        ProvisioningStatus(GrouperGroupInfo group) {
            this.group = group;
        }

        void startFullSyncWhenNoIncrementalIsUnderway() {
            // We'll give up after COORDINATION_TIME_SECS
            long giveUpTimeMillis = System.currentTimeMillis() + 1000L * COORDINATION_TIMEOUT_SECS;

            while ( System.currentTimeMillis() < giveUpTimeMillis ) {
                synchronized (this) {
                    if (isBeingIncrementallyProvisioned) {
                        LOG.warn("{}: Cannot start FullSync of {}. Incremental provisioning underway since {}. We'll give up and move ahead anyway in {} seconds.",
                                new Object[] {provisionerName, group, lastIncrementalProvisioningStart, (giveUpTimeMillis-System.currentTimeMillis())/1000});
                        try {
                            this.wait(COORDINATION_UPDATE_SECS*1000);
                        }
                        catch (InterruptedException e) {
                            // nada
                        }
                    }
                    else {
                        lastFullSyncStart = new Date();
                        isBeingFullSynced = true;
                        return;
                    }
                }
            }
            LOG.warn("{}: Giving up on coordination efforts between full and incremental provisioning");
            lastFullSyncStart = new Date();
            isBeingFullSynced = true;
        }

        synchronized void stopFullSync( boolean fullSyncWasSuccessful) {
            if (fullSyncWasSuccessful) {
                lastSuccessfulFullSyncStart = lastFullSyncStart;
            }
            isBeingFullSynced = false;
            this.notify();
        }

        synchronized void stopFullSync() {
            stopFullSync(false);
        }

        void startIncrementalProvisioningWhenNoFullSyncIsUnderway() {
            // We'll give up after COORDINATION_TIME_SECS
            long giveUpTimeMillis = System.currentTimeMillis() + 1000L * COORDINATION_TIMEOUT_SECS;

            while ( System.currentTimeMillis() < giveUpTimeMillis ) {
                synchronized (this) {
                    if (isBeingFullSynced) {
                        LOG.warn("{}: Cannot start Incremental Provisioning of {}. FullSync underway since {}. We'll give up and move ahead anyway in {} seconds",
                                new Object[] {provisionerName, group, lastFullSyncStart, (giveUpTimeMillis-System.currentTimeMillis())/1000});
                        try {
                            this.wait(1000L*COORDINATION_UPDATE_SECS);
                        }
                        catch (InterruptedException e) {
                            // nada
                        }
                    }
                    else {
                        lastIncrementalProvisioningStart = new Date();
                        isBeingIncrementallyProvisioned = true;
                        return;
                    }
                }
            }
            LOG.warn("{}: Giving up on coordination efforts between full and incremental provisioning");
            lastIncrementalProvisioningStart = new Date();
            isBeingIncrementallyProvisioned = true;
        }

        synchronized void stopIncrementalProvisioning() {
            isBeingIncrementallyProvisioned = false;
            this.notify();
        }
    }


    /**
     * Create a ProvisionerCoordinator
     * @param provisionerName
     */
    public ProvisionerCoordinator(String provisionerName) {
        this.provisionerName = provisionerName;
    }


    /**
     * Get the provisionerStatus object for this group.
     * @param group
     * @return
     */
    private ProvisioningStatus get(GrouperGroupInfo group) {

        // Create the ProvisionerStatus if one doesn't exist already
        if ( !group2ProvisioningStatus.containsKey(group) ) {
            group2ProvisioningStatus.putIfAbsent(group, new ProvisioningStatus(group));
        }
        ProvisioningStatus result = group2ProvisioningStatus.get(group);;

        return result;
    }


    /**
     * Used by the Full-Sync provisioners to wait and then lock the group from being incrementally provisioned.
     * @param group
     */
    public void startFullSyncIfNoIncrementalIsUnderway(GrouperGroupInfo group) {
        get(group).startFullSyncWhenNoIncrementalIsUnderway();
    }

    public void stopFullSync( GrouperGroupInfo group, boolean fullSyncWasSuccessful) {
        get(group).stopFullSync(fullSyncWasSuccessful);
    }


    public void stopFullSync( GrouperGroupInfo group) {
        get(group).stopFullSync();
    }


    public Date getLastSuccessfulFullSyncTime( GrouperGroupInfo group) {
        return get(group).lastSuccessfulFullSyncStart;
    }


    /**
     * Used by the incremental provisioners to wait and then lock the group from full-sync operations
     * @param group
     */
    public void startIncrementalProvisioningIfNoFullSyncIsUnderway(GrouperGroupInfo group) {
        get(group).startIncrementalProvisioningWhenNoFullSyncIsUnderway();
    }

    public void stopIncrementalProvisioning(GrouperGroupInfo group) {
        get(group).stopIncrementalProvisioning();
    }
}
