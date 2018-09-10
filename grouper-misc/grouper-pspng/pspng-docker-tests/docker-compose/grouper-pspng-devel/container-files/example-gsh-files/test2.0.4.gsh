print("Test 2.0.4: Move marked group that has memberships to a folder that is also marked");
// 1) Set up folder with syncAttribute mark
// 2) Set up marked group with membership outside of marked folder
// 3) Move marked group to marked folder (or subfolder)
// Outcome:
// 1) Group rename at the target

BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
gs = GrouperSession.startRootSession();

print("add test folder and marked parent folder");
testFolderName = "testFolder"
testFolder = addStem("", testFolderName, testFolderName);
parentFolderExtension = "parentFolder";
parentFolderName = testFolderName + ":" + parentFolderExtension;
parentFolder = addStem(testFolder.getName(), parentFolderExtension, parentFolderExtension);

print("add syncAttribute mark to parent folder");
syncAttr = AttributeDefNameFinder.findByName("etc:attribute:changeLogConsumer:printSync", true);
parentFolder.getAttributeDelegate().addAttribute(syncAttr);

print("add marked group and membership outside of marked folder");
group1Name = testFolderName + ":group1";
group1 = new GroupSave(gs).assignName(group1Name).assignGroupNameToEdit(group1Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
group1.getAttributeDelegate().addAttribute(syncAttr);
bob = "banderson";
ann = "agasper";
bill = "bbrown705";
addMember(group1Name, bob);
addMember(group1Name, ann);
addMember(group1Name, bill);

print("wait for group_debug.log: changeLog.consumer.print add subject Bill Brown to group testFolder:group1.");
print("hit return to continue");
in.readLine();

print("move marked group1 to marked folder, expect group rename at the target.");
group1.move(parentFolder);

print("wait for grouper_debug.log:");
print("  changeLog.consumer.print processed groupUpdate for group move. group testFolder:parentFolder:group1 is marked so calling renameGroup for old group testFolder:group1.");

print("end of Test 2.0.4");
print("hit return to teardown test");
in.readLine();

// Test 2.0.4 teardown
delGroup(group1.getName());
delStem(parentFolder.getName());
delStem(testFolderName);
print("end of Test 2.0.4 teardown");
