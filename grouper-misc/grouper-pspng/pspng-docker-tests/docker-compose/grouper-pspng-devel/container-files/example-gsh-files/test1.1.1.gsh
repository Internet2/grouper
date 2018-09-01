// Grouper action: 1.1 Remove a marker from a folder
// Target outcome: remove groups under that folder and any subfolder (and implicitly all the memberships), unless otherwise marked from a parent folder or has a direct assignment
// Test 1.1.1: Removing mark from parent folder with subfolders and groups (and no other marks)
// 1) Test 1.0.1
// 2) Remove syncAttribute marker from parent folder
// Outcome:
// 1) all groups within folder structure removed from target
// GSH:
print("Test 1.1.1 Removing mark from parent folder with subfolders and groups (and no other marks)");
BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
gs = GrouperSession.startRootSession();
bob = "banderson";
ann = "agasper";
bill = "bbrown705";
testFolderName = "testFolder"

// add group1 and membership to parent folder
parentFolderName = testFolderName + ":parentFolder";
group1Name = parentFolderName + ":group1";
group1 = new GroupSave(gs).assignName(group1Name).assignGroupNameToEdit(group1Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
addMember(group1Name, bob);
addMember(group1Name, ann);
addMember(group1Name, bill);

// add group2 and membership to subfolder
subFolderName = parentFolderName + ":subFolder"
group2Name = subFolderName + ":group2";
group2 = new GroupSave(gs).assignName(group2Name).assignGroupNameToEdit(group2Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
addMember(group2Name, bob);
addMember(group2Name, ann);
addMember(group2Name, bill);

// add syncAttribute mark to parent folder
syncAttr = AttributeDefNameFinder.findByName("etc:attribute:changeLogConsumer:printSync", true);
parentFolder = StemFinder.findByName(gs, parentFolderName, true);
parentFolder.getAttributeDelegate().addAttribute(syncAttr);

print("add syncAttribute mark to parent folder");
print("wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships.");
print("wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships.");
print("hit return to continue");
in.readLine();

// remove syncAttribute mark
parentFolder.getAttributeDelegate().removeAttribute(syncAttr);

print("removed syncAttribute mark");
print("wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
print("wait for group_debug.log: changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:subFolder:group2 so calling removeGroup");
print("end of Test 1.1.1");
print("hit return to teardown test");
in.readLine();

// Test 1.1.1 teardown
delGroup(group2Name);
delStem(subFolderName);
delGroup(group1Name);
delStem(parentFolderName);
delStem(testFolderName);
print("end of Test 1.1.1 teardown");
