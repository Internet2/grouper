print("Test 1.3.2: Remove mark from group that has an indirect mark from a parent folder");
// 1) Test 1.2.1
// 2) Mark parent folder (adding an indirect syncAttribute mark)
// 3) Remove the direct marker from the group
// Outcome:
// 1) Group is *not* removed from target, since it has an indirect mark from parent folder

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

print("add syncAttribute mark to parent folder");
parentFolder = StemFinder.findByName(gs, parentFolderName, true);
parentFolder.getAttributeDelegate().addAttribute(syncAttr);

print("wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships");
print("hit return to continue");
in.readLine();

print("remove syncAttribute mark from group, expecting indirect mark from parent folder to override removeGroup.");
group1.getAttributeDelegate().removeAttribute(syncAttr);

print("wait for grouper_debug.log:");
print("  changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for group testFolder:parentFolder:group1, but still marked by a parent folder.");
print("end of Test 1.3.2");
print("hit return to teardown test");
in.readLine();

// Test 1.3.2 teardown
delGroup(group1Name);
delStem(parentFolderName);
delStem(testFolderName);
print("end of Test 1.3.2 teardown");

