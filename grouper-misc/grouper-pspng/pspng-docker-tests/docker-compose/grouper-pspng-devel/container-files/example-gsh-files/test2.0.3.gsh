print("Test 2.0.3: Move unmarked group that has memberships to a folder that is also not marked");
// 1) Set up folder without syncAttribute mark
// 2) Set up group with membership outside of marked folder
// 3) Move group to unmarked folder (or subfolder)
// Outcome:
// 1) Nothing to do since group still unmarked

BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
gs = GrouperSession.startRootSession();

print("add test folder and parent folder");
testFolderName = "testFolder"
testFolder = addStem("", testFolderName, testFolderName);
parentFolderExtension = "parentFolder";
parentFolderName = testFolderName + ":" + parentFolderExtension;
parentFolder = addStem(testFolder.getName(), parentFolderExtension, parentFolderExtension);

print("add unmarked group and membership outside of marked folder");
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

print("move group1 to unmarked folder, expect nothing to do since still unmarked.");
group1.move(parentFolder);

print("wait for grouper_debug.log:");
print("  changeLog.consumer.print processed groupUpdate for group testFolder:parentFolder:group1 move. Couldn't find parent folder for old group testFolder:group1, or it was not previously marked, no-op in either case.");

print("end of Test 2.0.3");
print("hit return to teardown test");
in.readLine();

// Test 2.0.3 teardown
delGroup(group1.getName());
delStem(parentFolder.getName());
delStem(testFolderName);
print("end of Test 2.0.3 teardown");
