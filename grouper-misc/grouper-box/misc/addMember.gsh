GrouperSession grouperSession = GrouperSession.startRootSession();
Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("box:groups:someGroup").save();
Subject subject = SubjectFinder.findById("mchyzerGoogle");
group.addMember(subject, false);
GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
