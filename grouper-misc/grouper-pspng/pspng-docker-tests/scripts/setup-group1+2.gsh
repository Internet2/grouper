// 1) no groups are provisioned

msg="Creating testFolder:parentFolder:group[12] and adding ann, bill and bob";
print("STARTING: " + msg);
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

print("DONE: " + msg);
