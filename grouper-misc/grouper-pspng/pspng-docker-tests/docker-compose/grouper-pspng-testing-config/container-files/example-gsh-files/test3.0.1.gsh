print("Grouper action: 3.0 Delete a directly marked group");
print("Target outcome: remove the group");
print("Test 3.0.1: delete a directly marked group, with no other parent folder marks");
// 1) Test 1.2.1
// 2) Delete group
// Outcome:
// 1) group removed from target
// GSH:
// Test 3.0.1

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

print("Delete the group, expecting it to be removed from the target");
delGroup(group1Name)

print("wait for grouper_debug.log");
print("  changeLog.consumer.print processed deleteAttributeAssign etc:attribute:changeLogConsumer:printSync for deleted group testFolder:parentFolder:group1, calling removeDeletedGroup");
print("end of Test 3.0.1");
print("hit return to teardown test");
in.readLine();

// Test 3.0.1 teardown
delStem(parentFolderName);
delStem(testFolderName);
print("end of Test 3.0.1 teardown");


