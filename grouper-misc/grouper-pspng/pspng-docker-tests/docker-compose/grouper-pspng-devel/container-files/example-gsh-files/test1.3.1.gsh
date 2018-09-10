// Grouper action: 1.3 Remove a marker from a group
// Target outcome: remove the group (and implicitly all the memberships), unless otherwise marked by a parent folder
// Test 1.3.1: Remove marker from a group that doesn't have parent folders marked
// 1) Test 1.2.1
// 2) Remove marker from the group
// Outcome:
// 1) group removed from target
print("Test 1.3.1: Remove marker from a group that doesn't have parent folders marked");
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
addMember(group1Name, bill);

print("add syncAttribute mark to group1");
syncAttr = AttributeDefNameFinder.findByName("etc:attribute:changeLogConsumer:printSync", true);
group1.getAttributeDelegate().addAttribute(syncAttr);

print("wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships");
print("hit return to continue");
in.readLine();

print("remove syncAttribute mark form group1");
group1.getAttributeDelegate().removeAttribute(syncAttr);

print("wait for grouper_debug.log:")
print("  changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for group testFolder:parentFolder:group1, no other mark found so calling removeGroup");
print("end of Test 1.3.1");
print("hit return to teardown test");
in.readLine();

// Test 1.3.1 teardown
delGroup(group1Name);
delStem(parentFolderName);
delStem(testFolderName);
print("end of Test 1.3.1 teardown");

