print("Test 2.0.2: Move group that has memberships to a folder that is already marked");
// 1) Set up folder with syncAttribute mark
// 2) Set up group with membership outside of marked folder
// 3) Move group to marked folder (or subfolder)
// Outcome:
// 1) Group and its membership (in case of moved with membership) added to target

BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
gs = GrouperSession.startRootSession();

print("add test folder and parent folder");
testFolderName = "testFolder"
testFolder = addStem("", testFolderName, testFolderName);
parentFolderExtension = "parentFolder";
parentFolderName = testFolderName + ":" + parentFolderExtension;
parentFolder = addStem(testFolder.getName(), parentFolderExtension, parentFolderExtension);

print("add syncAttribute mark to parent folder");
syncAttr = AttributeDefNameFinder.findByName("etc:attribute:changeLogConsumer:printSync", true);
parentFolder.getAttributeDelegate().addAttribute(syncAttr);

print("add group and membership outside of marked folder");
group1Name = testFolderName + ":group1";
group1 = new GroupSave(gs).assignName(group1Name).assignGroupNameToEdit(group1Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
bob = "banderson";
ann = "agasper";
bill = "bbrown705";
addMember(group1Name, bob);
addMember(group1Name, ann);
addMember(group1Name, bill);

print("wait for group_debug.log: changeLog.consumer.print skipping addMembership for subject Bill Brown since group testFolder:group1 is not marked for sync");
print("hit return to continue");
in.readLine();

print("move group1 to marked folder, expect to add group and membership to target via renameGroup.");
group1.move(parentFolder);

print("wait for grouper_debug.log: ");
print("  changeLog.consumer.print processed groupUpdate for group move. group testFolder:parentFolder:group1 is marked so calling renameGroup for old group testFolder:group1.");
print("end of Test 2.0.2");
print("hit return to teardown test");
in.readLine();

// Test 2.0.2 teardown
delGroup(group1.getName());
delStem(parentFolder.getName());
delStem(testFolderName);
print("end of Test 2.0.2 teardown");
