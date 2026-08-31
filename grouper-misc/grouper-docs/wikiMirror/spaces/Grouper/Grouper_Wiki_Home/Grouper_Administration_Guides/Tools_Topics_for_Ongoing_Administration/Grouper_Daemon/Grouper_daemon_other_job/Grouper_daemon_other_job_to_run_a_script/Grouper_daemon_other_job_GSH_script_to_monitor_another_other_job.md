---
title: "Grouper daemon \"other job\" GSH script to monitor another \"other job\""
space: Grouper
pageId: 28560354
version: 4
lastUpdated: 2026-07-01T05:35:43.721Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560354/Grouper+daemon+other+job+GSH+script+to+monitor+another+other+job
---

Its best to monitor jobs from something other than the job engine running the job. e.g. nagios. If the grouper daemon is down, the job you are monitoring will not run and the job that monitors it will not run, so you wont be notified.

Here is an example, it will email a group of sysadmins, but if you want to hard code email addresses you can do that too.

```
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.j2ee.status.DiagnosticLoaderJobTest;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;

//public class Test31DaemonStatus {

//  public static void main(String[] args) {
    
    String otherJobName = "OTHER_JOB_attestationDaemon";
    String groupToEmailTo = "a:b:c";
    String subject = "Error in attestation job";
    
    DiagnosticLoaderJobTest diagnosticLoaderJobTest = new DiagnosticLoaderJobTest(otherJobName, GrouperLoaderType.OTHER_JOB);
    String message = null;
    boolean success = false;
    try {
       success = (Boolean)GrouperUtil.callMethod(diagnosticLoaderJobTest, "doTask");
       if (success) {
        message = diagnosticLoaderJobTest.retrieveSuccessText().toString();
      } else {
        message = diagnosticLoaderJobTest.retrieveFailureText().toString();
      }
    } catch (Exception e) {
      message = GrouperUtil.getFullStackTrace(e);
    }

    OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setJobMessage(message);
    if (!success) {
      new GrouperEmail().addGroupNameToSendTo(groupToEmailTo, true).setSubject(subject).setBody(message).send();
    }
    
  // }

// }
```

Email

```
java.lang.RuntimeException: Cant find a success in job OTHER_JOB_attestationDaemon, expecting one in the last 3120 minutes
	at edu.internet2.middleware.grouper.j2ee.status.DiagnosticLoaderJobTest.doTask(DiagnosticLoaderJobTest.java:111)
	at edu.internet2.middleware.grouper.j2ee.status.DiagnosticLoaderJobTest$doTask.call(Unknown Source)

```
