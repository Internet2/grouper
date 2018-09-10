print("Grouper action: 2.0 Add indirectly marked group (i.e. add a group under a folder that is already marked)");
print("Target outcome: add the group");
print("Test 2.0.1: Add group to a folder that is already marked");
// 1) Set up folder with syncAttribute mark
// 2) Add (TODO what about move?) group to folder (or subfolder)
// Outcome:
// 1) Group and its membership (in case of moved with membership) added to target
//GSH:
// Test 2.0.1
BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
gs = GrouperSession.startRootSession();

print("add test folder and parent folder");
testFolderName = "testFolder"
addStem("", testFolderName, testFolderName);
parentFolderExtension = "parentFolder";
parentFolderName = testFolderName + ":" + parentFolderExtension;
addStem(testFolderName, parentFolderExtension, parentFolderExtension);

print("add syncAttribute mark to parent folder");
syncAttr = AttributeDefNameFinder.findByName("etc:attribute:changeLogConsumer:printSync", true);
parentFolder = StemFinder.findByName(gs, parentFolderName, true);
parentFolder.getAttributeDelegate().addAttribute(syncAttr);

print("wait for grouper_debug.log: changeLog.consumer.print dispatching change log event attributeAssign_addAttributeAssign for change log...");
print("hit return to continue");
in.readLine();

print("add group1 to marked folder, expecting to be added to target");
group1Name = parentFolderName + ":group1";
group1 = new GroupSave(gs).assignName(group1Name).assignGroupNameToEdit(group1Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();

print("wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1.");
print("end of Test 2.0.1");
print("hit return to teardown test");
in.readLine();

// Test 2.0.1 teardown
delGroup(group1Name);
delStem(parentFolderName);
delStem(testFolderName);
print("end of Test 2.0.1 teardown");
