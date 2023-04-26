import edu.internet2.middleware.grouper.app.provisioning.*;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;


ProvisioningGroup grouperProvisioningGroup = new ProvisioningGroup();
ProvisioningGroup grouperTargetGroup = new ProvisioningGroup();

grouperTargetGroup.assignAttributeValue("group_name", "test:testGroup");

ProvisioningEntity grouperProvisioningEntity = new ProvisioningEntity();
ProvisioningEntity grouperTargetEntity = new ProvisioningEntity();

grouperTargetEntity.assignAttributeValue("subject_id", "12345678");


ProvisioningMembership grouperProvisioningMembership = new ProvisioningMembership();
grouperProvisioningMembership.setProvisioningEntity(grouperProvisioningEntity);
grouperProvisioningMembership.setProvisioningEntityId("abc123");
grouperProvisioningMembership.setProvisioningGroup(grouperProvisioningGroup);
grouperProvisioningMembership.setProvisioningGroupId("xyz234");


ProvisioningMembership grouperTargetMembership = new ProvisioningMembership();

GcGrouperSyncGroup gcGrouperSyncGroup = new GcGrouperSyncGroup();
gcGrouperSyncGroup.setGroupId("abc123");
gcGrouperSyncGroup.setGroupIdIndex(1234567L);
gcGrouperSyncGroup.setGroupName("test:testGroup");
gcGrouperSyncGroup.setGroupAttributeValueCache0("cacheValue0");

GcGrouperSyncMember gcGrouperSyncMember = new GcGrouperSyncMember();
gcGrouperSyncMember.setMemberId("abc123");
gcGrouperSyncMember.setSubjectId("abc123");
gcGrouperSyncMember.setEntityAttributeValueCache0("cacheValue0");

GcGrouperSyncMembership gcGrouperSyncMembership = new GcGrouperSyncMembership();
gcGrouperSyncMembership.setGrouperSyncGroup(gcGrouperSyncGroup);
gcGrouperSyncMembership.setGrouperSyncMember(gcGrouperSyncMember);
gcGrouperSyncMembership.setGrouperSyncGroupId("abc123");
gcGrouperSyncMembership.setGrouperSyncMemberId("abc123");

ProvisioningGroupWrapper provisioningGroupWrapper = new ProvisioningGroupWrapper();
provisioningGroupWrapper.setGrouperProvisioningGroup(grouperProvisioningGroup);
provisioningGroupWrapper.setGrouperTargetGroup(grouperTargetGroup);
provisioningGroupWrapper.setGcGrouperSyncGroup(gcGrouperSyncGroup);

ProvisioningEntityWrapper provisioningEntityWrapper = new ProvisioningEntityWrapper();
provisioningEntityWrapper.setGrouperProvisioningEntity(grouperProvisioningEntity);
provisioningEntityWrapper.setGrouperTargetEntity(grouperTargetEntity);
provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);

ProvisioningMembershipWrapper provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
provisioningMembershipWrapper.setGrouperProvisioningMembership(grouperProvisioningMembership);
provisioningMembershipWrapper.setGrouperTargetMembership(grouperTargetMembership);
provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);

Map<String, Object> elVariableMap = new HashMap<String, Object>();
elVariableMap.put("grouperProvisioningGroup", grouperProvisioningGroup);
elVariableMap.put("provisioningGroupWrapper", provisioningGroupWrapper);
elVariableMap.put("grouperTargetGroup", grouperTargetGroup);
elVariableMap.put("gcGrouperSyncGroup", gcGrouperSyncGroup);
elVariableMap.put("grouperProvisioningEntity", grouperProvisioningEntity);
elVariableMap.put("provisioningEntityWrapper", provisioningEntityWrapper);
elVariableMap.put("grouperTargetEntity", grouperTargetEntity);
elVariableMap.put("gcGrouperSyncMember", gcGrouperSyncMember);
elVariableMap.put("grouperProvisioningMembership", grouperProvisioningMembership);
elVariableMap.put("provisioningMembershipWrapper", provisioningMembershipWrapper);
elVariableMap.put("grouperTargetMembership", grouperTargetMembership);
elVariableMap.put("gcGrouperSyncMembership", gcGrouperSyncMembership);

