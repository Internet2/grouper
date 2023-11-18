import edu.internet2.middleware.grouper.app.provisioning.*;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;


ProvisioningEntity grouperProvisioningEntity = new ProvisioningEntity();
grouperProvisioningEntity.setName("TestName");
grouperProvisioningEntity.setId("abc123");
grouperProvisioningEntity.setSubjectId("abc123");
grouperProvisioningEntity.setIdIndex(1234567L);
grouperProvisioningEntity.setDescription("test description");

ProvisioningEntity grouperTargetEntity = new ProvisioningEntity();

GcGrouperSyncMember gcGrouperSyncMember = new GcGrouperSyncMember();
gcGrouperSyncMember.setMemberId("abc123");
gcGrouperSyncMember.setSubjectId("abc123");
gcGrouperSyncMember.setEntityAttributeValueCache0("cacheValue0");

ProvisioningEntityWrapper provisioningEntityWrapper = new ProvisioningEntityWrapper();
provisioningEntityWrapper.setGrouperProvisioningEntity(grouperProvisioningEntity);
provisioningEntityWrapper.setGrouperTargetEntity(grouperTargetEntity);
provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);


Map<String, Object> elVariableMap = new HashMap<String, Object>();
elVariableMap.put("grouperProvisioningEntity", grouperProvisioningEntity);
elVariableMap.put("provisioningEntityWrapper", provisioningEntityWrapper);
elVariableMap.put("grouperTargetEntity", grouperTargetEntity);
elVariableMap.put("gcGrouperSyncMember", gcGrouperSyncMember);

