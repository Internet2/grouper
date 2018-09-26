// Test 1.1.2: Marked folder with marked subfolders
// 1) Test 1.0.1
// 2) Mark subfolder with syncAttribute
// 3) Remove syncAttribute from parent folder
// Outcome:
// 1) all groups within parent folder structure removed from target, expect those within marked subfolder
print("Test 1.1.2 Removing mark from parent folder with subfolders and groups, with a mark on a subfolder");
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

print("add syncAttribute mark to parent folder");
syncAttr = AttributeDefNameFinder.findByName("etc:attribute:changeLogConsumer:printSync", true);
parentFolder = StemFinder.findByName(gs, parentFolderName, true);
parentFolder.getAttributeDelegate().addAttribute(syncAttr);

print("add syncAttribute mark to subfolder");
subFolder = StemFinder.findByName(gs, subFolderName, true);
subFolder.getAttributeDelegate().addAttribute(syncAttr);

print("wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships");
print("hit return to continue");
in.readLine();

print("removed syncAttribute mark from parent folder, expecting subfolder mark to override removeGroup.");
parentFolder.getAttributeDelegate().removeAttribute(syncAttr);

//
print("wait for group_debug.log:");
print("  changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, no other mark found for group testFolder:parentFolder:group1 so calling removeGroup");
print("  changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for folder testFolder:parentFolder, found mark for group testFolder:parentFolder:subFolder:group2 so nothing to do.");
print("end of Test 1.1.2");
print("hit return to teardown test");
in.readLine();


// Test 1.1.2 teardown
delGroup(group2Name);
delStem(subFolderName);
delGroup(group1Name);
delStem(parentFolderName);
delStem(testFolderName);
print("end of Test 1.1.2 teardown");

