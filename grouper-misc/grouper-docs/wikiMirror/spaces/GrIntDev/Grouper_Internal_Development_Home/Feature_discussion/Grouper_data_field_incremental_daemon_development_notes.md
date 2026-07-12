---
title: "Grouper data field incremental daemon development notes"
space: GrIntDev
pageId: 48793060
version: 8
lastUpdated: 2026-07-12T07:01:20.942Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793060/Grouper+data+field+incremental+daemon+development+notes
---

## Current design

Have an incremental data that makes sure dependencies are tracked for groups, data fields, and data rows

GrouperLoaderJexlScriptIncremental

## Previous idea

This is a development item for v5.

This effort is intended to use best practices from the provisioning framework in the data providers.

1. Have an overall class (similar to GrouperProvisioner)
2. Have some base classes for responsibilities and allow the data providers to override them
  
  
  ```
    private GrouperProvisioningConfiguration grouperProvisioningConfiguration = null;
  
    /**
     * return the class of the DAO for this provisioner
     */
    protected abstract Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass();
  
    /**
     * returns the subclass of Data Access Object for this provisioner
     * @return the DAO
     */
    public GrouperProvisioningConfiguration retrieveGrouperProvisioningConfiguration() {
      if (this.grouperProvisioningConfiguration == null) {
        Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass = this.grouperProvisioningConfigurationClass();
        this.grouperProvisioningConfiguration = GrouperUtil.newInstance(grouperProvisioningConfigurationClass);
        this.grouperProvisioningConfiguration.setGrouperProvisioner(this);
      }
      return this.grouperProvisioningConfiguration;
      
    }
  
  
  ```
3. Make configuration class for sql and ldap and a generic configuration class (superclass)
4. Note, we do not need capabilities and behaviors at this time since there will be a lot fewer data providers than provisioners
5. Have a logic class (subclassable) that has a full and incremental method (similar to provisioning), and each will list methods in the workflow (e.g. retrieve from source, target, compare, make changes, etc). If there are opportunities for re-use do that
6. Have fields for gcGrouperSync, job, heartbeat, etc (if not already there)
7. Have a bean similar to GrouperProvisioningData which keeps all the data. That could be indexed or there could be something similar to GrouperProvisioningDataIndex
8. Have something similar to a DAO which runs operations to the target (CRUD)
9. Have something similar to GrouperProvisioningObjectLog(Type) which can optionally print out a report of a run
10. Integrate with grouper messaging (similar to provisioning incremental) so that certain records can be refreshed
11. Integrate with other types of messaging optionally?
12. Integrate with SQL change log (for any type of target?)
13. Assume everything is a "recalc" (could be recalc for user or recalc for user with a field/row)
14. Filter messages or change log for events that happened before the start of the last successful full sync
15. Convert from incremental to full for certain thresholds
16. Is incremental at the attribute level so not all queries need to run?
17. Batch incremental queries
18. Add in failsafes like provisioning
19. Attribute changes should go to the temp/real change log (so it can be used by abac, etc).
