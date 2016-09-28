GrouperSession grouperSession = GrouperSession.startRootSession();
Group group = new GroupSave(grouperSession).assignName("box:groups:someGroup").save();
Subject subject = SubjectFinder.findById("GrouperSystem");
group.deleteMember(subject, false);
GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");
