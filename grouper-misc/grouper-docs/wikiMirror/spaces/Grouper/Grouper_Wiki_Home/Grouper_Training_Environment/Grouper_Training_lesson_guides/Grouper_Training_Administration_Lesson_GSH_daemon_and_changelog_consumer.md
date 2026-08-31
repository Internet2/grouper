---
title: "Grouper Training - Administration - Lesson: GSH daemon and changelog consumer"
space: Grouper
pageId: 28544919
version: 3
lastUpdated: 2026-07-12T15:26:32.441Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544919/Grouper+Training+-+Administration+-+Lesson+GSH+daemon+and+changelog+consumer
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

## Script daemon

Miscellaneous > Daemon jobs > add daemon

- Config Id: DoSomethingImportant
- Daemon type: Script daemon
- Quartz cron: 0 38 6 * * ? 2099
- Script type: gsh
- File type: script
- Script source: paste in from below

```groovy
import edu.internet2.middleware.grouper.app.loader.OtherJobScript
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog

Hib3GrouperLoaderLog hib3GrouperLoaderLog = OtherJobScript.retrieveHib3GrouperLoaderLogNotNull()

hib3GrouperLoaderLog.appendJobMessage("${new Date()} *** START ***\n")

try {
    hib3GrouperLoaderLog.appendJobMessage("${new Date()} Did some work\n")
    hib3GrouperLoaderLog.addTotalCount(1)

    hib3GrouperLoaderLog.status = "SUCCESS"
} catch (Exception e) {
    hib3GrouperLoaderLog.status = "ERROR"
    hib3GrouperLoaderLog.appendJobMessage(e.message)
}

hib3GrouperLoaderLog.appendJobMessage("${new Date()} *** END ***")

```

## GSH changelog daemon

Miscellaneous > Daemon jobs > add daemon

- Config id: AddPresidentTooChangelogConsumer
- Daemon type: Change log GSH script
- File type: script
- Script: paste in from below

```groovy
import edu.internet2.middleware.grouper.*
import edu.internet2.middleware.subject.Subject
import edu.internet2.middleware.grouper.changeLog.esb.consumer.*

long lastSequenceProcessed = -1

for (EsbEventContainer esbEventContainer : gsh_builtin_esbEventContainers) {
    EsbEvent esbEvent = esbEventContainer.getEsbEvent()

    def action_name =  esbEvent.eventType
    def subjectId = esbEvent.subjectId
    def sourceId = esbEvent.sourceId
    def groupName = esbEvent.groupName

    Subject subj = null
    if (action_name == 'MEMBERSHIP_ADD' && groupName.toLowerCase().contains('president')) {
        Group group = GroupFinder.findByName(groupName, true)
        if (subj == null) {
            subj = SubjectFinder.findByIdentifier("cmoore", true)
        }

        boolean notAlreadyExisted = group.addMember(subj, exceptionIfAlreadyMember=false)
        if (notAlreadyExisted) {
            gsh_builtin_hib3GrouperLoaderLog.addInsertCount(1)
        }

        gsh_builtin_hib3GrouperLoaderLog.appendJobMessage("Sequence: ${esbEventContainer.sequenceNumber}, Added ${subj.description} to ${groupName}\n")
        lastSequenceProcessed = esbEventContainer.getSequenceNumber()
    } else {
        lastSequenceProcessed = esbEventContainer.getSequenceNumber()
    }
}
return lastSequenceProcessed 
```

Create a test group "presidentGroup", add a user, then watch the daemon job or kick it off manually.

Optionally, disable or delete the job at the end of the lesson.
