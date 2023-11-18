import edu.internet2.middleware.grouper.app.provisioning.*;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;

ProvisioningGroup grouperProvisioningGroup = new ProvisioningGroup();
grouperProvisioningGroup.setName("applications:departmentOfArtsAndSciences:commonResources:servicesOfSchool:policyGroups:groupsForActiveDirectory:historyDepartmentProfessors");
grouperProvisioningGroup.setId("abc123");
grouperProvisioningGroup.setIdIndex(1234567L);

ProvisioningGroup grouperTargetGroup = new ProvisioningGroup();

GcGrouperSyncGroup gcGrouperSyncGroup = new GcGrouperSyncGroup();

ProvisioningGroupWrapper provisioningGroupWrapper = new ProvisioningGroupWrapper();
provisioningGroupWrapper.setGrouperProvisioningGroup(grouperProvisioningGroup);
provisioningGroupWrapper.setGrouperTargetGroup(grouperTargetGroup);
provisioningGroupWrapper.setGcGrouperSyncGroup(gcGrouperSyncGroup);

Map<String, Object> elVariableMap = new HashMap<String, Object>();
elVariableMap.put("grouperProvisioningGroup", grouperProvisioningGroup);
elVariableMap.put("provisioningGroupWrapper", provisioningGroupWrapper);
elVariableMap.put("grouperTargetGroup", grouperTargetGroup);
elVariableMap.put("gcGrouperSyncGroup", gcGrouperSyncGroup);

