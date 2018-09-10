print("Test 2.0.5: Move an indirectly marked group that has memberships to an unmarked folder");
// 1) Set up folder with syncAttribute mark
// 2) Set up indirectly marked group with membership outside of unmarked folder
// 3) Move indirectly marked group to unmarked folder (or subfolder)
// Outcome:
// 1) Remove group at target, since it is no longer marked

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

print("add group and membership to marked folder");
group1Name = parentFolderName + ":group1";
group1 = new GroupSave(gs).assignName(group1Name).assignGroupNameToEdit(group1Name).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignCreateParentStemsIfNotExist(true).save();
bob = "banderson";
ann = "agasper";
bill = "bbrown705";
addMember(group1Name, bob);
addMember(group1Name, ann);
addMember(group1Name, bill);

print("wait for group_debug.log: changeLog.consumer.print add subject Bill Brown to group testFolder:parentFolder:group1.");
print("hit return to continue");
in.readLine();

print("move indirectly marked group1 to unmarked folder, expect group remove at the target.");
group1.move(testFolder);

print("wait for grouper_debug.log:");
print("  hangeLog.consumer.print processed groupUpdate for group move. group testFolder:group1 is no longer marked so calling removeMovedGroup for old group testFolder:parentFolder:group1.");
print("end of Test 2.0.5");
print("hit return to teardown test");
in.readLine();

// Test 2.0.5 teardown
delGroup(group1.getName());
delStem(parentFolder.getName());
delStem(testFolderName);
print("end of Test 2.0.5 teardown");
