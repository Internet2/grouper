package edu.internet2.middleware.grouper.pspng;

import edu.internet2.middleware.subject.Subject;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This class doesn't do any provisioning, but just prints the methods being invoked.
 * This is useful to make PSPNG go through its motions or to use it to identify what methods
 * are called as events occur.
 */
public class VoidProvisioner extends Provisioner<ProvisionerConfiguration, TargetSystemUser, TargetSystemGroup> {
    VoidProvisioner(String provisionerName, ProvisionerConfiguration config) {
        super(provisionerName, config);
    }

    @Override
    protected void addMembership(GrouperGroupInfo grouperGroupInfo, TargetSystemGroup tsGroup, Subject subject, TargetSystemUser tsUser) throws PspException {

    }

    @Override
    protected void deleteMembership(GrouperGroupInfo grouperGroupInfo, TargetSystemGroup tsGroup, Subject subject, TargetSystemUser tsUser) throws PspException {

    }

    @Override
    protected TargetSystemGroup createGroup(GrouperGroupInfo grouperGroup, Collection initialMembers) throws PspException {
        return null;
    }

    @Override
    protected void deleteGroup(GrouperGroupInfo grouperGroupInfo, TargetSystemGroup tsGroup) throws PspException {

    }

    @Override
    protected Map<Subject, TargetSystemUser> fetchTargetSystemUsers(Collection personSubjects) throws PspException {
        return null;
    }

    @Override
    protected Map fetchTargetSystemGroups(Collection grouperGroups) throws PspException {
        return null;
    }

    @Override
    protected void doFullSync_cleanupExtraGroups(Set groupsForThisProvisioner, Map tsGroups, JobStatistics stats) throws PspException {

    }

    @Override
    protected void doFullSync(GrouperGroupInfo grouperGroupInfo, TargetSystemGroup tsGroup, Set correctSubjects, Map tsUserMap, Set correctTSUsers, JobStatistics stats) throws PspException {

    }
}
