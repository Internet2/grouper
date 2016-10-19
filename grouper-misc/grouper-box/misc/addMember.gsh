GrouperSession grouperSession = GrouperSession.startRootSession();
Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("box:groups:someGroup").save();
Subject subject = SubjectFinder.findById("GrouperSystem");
group.addMember(subject, false);
loaderRunOneJob("CHANGE_LOG_changeLogTempToChangeLog");
GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
