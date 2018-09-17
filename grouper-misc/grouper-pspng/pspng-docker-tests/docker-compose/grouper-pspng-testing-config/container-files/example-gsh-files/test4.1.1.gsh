print("Grouper action: 4.1 Membership delete on a marked group (directly or indirect marked)");
print("Target outcome: remove membership");
print("Test 4.1.1: Membership delete to directly marked group");
// 1) Test 4.0.1
// 2) Remove member from marked group
// Outcome:
// 1) membership removed from target
// GSH:
// Test 4.1.1

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

print("add syncAttribute mark to group1");
syncAttr = AttributeDefNameFinder.findByName("etc:attribute:changeLogConsumer:printSync", true);
group1.getAttributeDelegate().addAttribute(syncAttr);

print("add new membership, expect to add to target");
addMember(group1Name, bill);

print("wait for grouper_debug.log: changeLog.consumer.print add subject Bill Brown to group testFolder:parentFolder:group1.");
print("hit return to continue");
in.readLine();

print("remove bill from marked group, expect to remove bill from target");
delMember(group1Name, bill);

print("wait for grouper_debug.log: changeLog.consumer.print remove subject Bill Brown from group testFolder:parentFolder:group1.");
print("end of Test 4.1.1");
print("hit return to teardown test");
in.readLine();

// Test 4.1.1 teardown
delGroup(group1Name);
delStem(parentFolderName);
delStem(testFolderName);
print("end of Test 4.1.1 teardown");
