// Grouper action: 1.2 Place a marker on a group
// Target outcome: add the group and all its effective memberships (direct and indirect)
// Test 1.2.1:
// 1) Set up folder and a group with memberships, and no syncAttribute marks
// 2) Mark group with syncAttribute
// Outcome:
// 1) group and its memberships added to the target
print("Test 1.2.1 Place a marker on a group, add the group and all its effective memberships to the target");
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

//
print("wait for grouper_debug.log: changeLog.consumer.print add group testFolder:parentFolder:group1 and memberships");
print("end of Test 1.2.1");
print("hit return to teardown test");
in.readLine();

// Test 1.2.1 teardown
delGroup(group1Name);
delStem(parentFolderName);
delStem(testFolderName);
print("end of Test 1.2.1 teardown");

