print("Test 4.1.3 Membership delete by grouper effective membership (via sub groups or group math)");
// 1) Test 4.0.3
// 2) Remove subgroup from marked group
// Outcome:
// 1) Indirect memberships due to subgroup removed from target
// GSH:
// Test 4.1.3

BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

gs = GrouperSession.startRootSession();
bob = "banderson";
ann = "agasper";
bill = "bbrown705";
testFolderName = "testFolder"

print("add group1 and membership to parent folder");
parentFolderName = testFolderName + ":parentFolder";
group1Name = parentFolderName + ":group1";
group1 = new GroupSave(gs).assignName(group1Name).assignGroupNameToEdit(group1Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
addMember(group1Name, bob);
addMember(group1Name, ann);

group2Name = parentFolderName + ":group2";
group2 = new GroupSave(gs).assignName(group2Name).assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
addMember(group2Name, bill);

print("add syncAttribute mark to group1");
syncAttr = AttributeDefNameFinder.findByName("etc:attribute:changeLogConsumer:printSync", true);
group1.getAttributeDelegate().addAttribute(syncAttr);

print("wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships");
print("hit return to continue");
in.readLine();

print("add group2 to group1, expecting to add group2 memberships to target as effective members of group1");
addMember(group1Name, group2Name);

print("wait for grouper_debug.log: changeLog.consumer.print add subject Bill Brown to group testFolder:parentFolder:group1.");
print("hit return to continue");
in.readLine();

print("remove sub group, expect remove all the indirect memberships due to group2");
delMember(group1Name, group2Name);

print("wait for grouper_debug.log: changeLog.consumer.print remove subject Bill Brown from group testFolder:parentFolder:group1.");
print("end of Test 4.1.3");
print("hit return to teardown test");
in.readLine();

// Test 4.1.3 teardown
delGroup(group1Name);
delGroup(group2Name);
delStem(parentFolderName);
delStem(testFolderName);
print("end of Test 4.1.3 teardown");


