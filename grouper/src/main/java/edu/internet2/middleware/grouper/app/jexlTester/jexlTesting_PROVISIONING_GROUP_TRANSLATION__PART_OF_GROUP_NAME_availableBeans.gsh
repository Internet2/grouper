import edu.internet2.middleware.grouper.app.provisioning.*;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;


ProvisioningGroup grouperProvisioningGroup = new ProvisioningGroup();
grouperProvisioningGroup.setName("app:MyApplication:service:policy:Users");
grouperProvisioningGroup.setId("abc123");
grouperProvisioningGroup.setIdIndex(1234567L);
grouperProvisioningGroup.setDisplayName("Test:Test group");
grouperProvisioningGroup.assignAttributeValue("md_M365", "test");

ProvisioningGroup grouperTargetGroup = new ProvisioningGroup();

GcGrouperSyncGroup gcGrouperSyncGroup = new GcGrouperSyncGroup();
gcGrouperSyncGroup.setGroupId("abc123");
gcGrouperSyncGroup.setGroupIdIndex(1234567L);
gcGrouperSyncGroup.setGroupName("test:testGroup");
gcGrouperSyncGroup.setGroupAttributeValueCache0("cacheValue0");

ProvisioningGroupWrapper provisioningGroupWrapper = new ProvisioningGroupWrapper();
provisioningGroupWrapper.setGrouperProvisioningGroup(grouperProvisioningGroup);
provisioningGroupWrapper.setGrouperTargetGroup(grouperTargetGroup);
provisioningGroupWrapper.setGcGrouperSyncGroup(gcGrouperSyncGroup);

Map<String, Object> elVariableMap = new HashMap<String, Object>();
elVariableMap.put("grouperProvisioningGroup", grouperProvisioningGroup);
elVariableMap.put("provisioningGroupWrapper", provisioningGroupWrapper);
elVariableMap.put("grouperTargetGroup", grouperTargetGroup);
elVariableMap.put("gcGrouperSyncGroup", gcGrouperSyncGroup);

