// Grouper action: 1.0 Place a marker on a folder
// Target outcome: add all the groups under that folder and any subfolder, and all the group memberships
// Test 1.0.1: Marking a parent folder
// 1) setup folder structure with groups, sub folders, and groups in sub folders
// 2) place syncAttribute marker on parent folder
// Outcome:
// 1) all groups within folder structure added to the target

print("Test 1.0.1 Marking a parent folder");
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

print("wait for grouper_debug.log: changeLog.consumer.print skipping addMembership for subject Bill Brown since group testFolder:parentFolder:subFolder:group2 is not marked for sync");
print("hit return to continue");
in.readLine();

// add syncAttribute mark to parent folder
syncAttr = AttributeDefNameFinder.findByName("etc:attribute:changeLogConsumer:printSync", true);
parentFolder = StemFinder.findByName(gs, parentFolderName, true);
parentFolder.getAttributeDelegate().addAttribute(syncAttr);

print("added syncAttribute to parent folder")
print("wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships");
print("wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:subFolder:group2 and memberships");
print("end of Test 1.0.1 Marking a parent folder");
print("hit return to teardown test");
in.readLine();

print("Test 1.0.1 teardown");
delGroup(group2Name);
delStem(subFolderName);
delGroup(group1Name);
delStem(parentFolderName);
delStem(testFolderName);
print("end of Test 1.0.1 teardown");
