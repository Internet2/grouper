---
title: "GrouperShell (gsh) Grouper session (GrouperSession)"
space: Grouper
pageId: 28548715
version: 5
lastUpdated: 2026-07-01T05:43:42.909Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548715/GrouperShell+gsh+Grouper+session+GrouperSession
---

A GrouperSession indicates in the environment which user to run commands as. Sometimes the session is passed into methods, and other times it is just retrieved from the currently running thread implicitly.

One session should be open at a time. Sessions should be closed after opening them. If you need a temporary root session, that is possible.

In GSH templates, the session will be automatically opened and closed for you.

By default, in GSH, a root session is created for scripts. Create a root session:

```
GrouperSession grouperSession = GrouperSession.startRootSession();

... run code ...

GrouperSession.stopQuietly(grouperSession);
```

Start a session as a user (note, to find a subject you should use a root session)

```
GrouperSession grouperSession = GrouperSession.startRootSession();
Subject subject = SubjectFinder.findByIdAndSource("someSourceId", "someSubjectId", true); // true means fail if unresolvable
GrouperSession.stopQuietly(grouperSession);

grouperSession = GrouperSession.start(subject);

... run code ...

GrouperSession.stopQuietly(grouperSession);
```

If you are running a non root session, you can stop and start a root and start back. Or there is an anonymous inner class way

```
    grouperSession = GrouperSession.start(subject);

    ... do things as non root ...

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        ... do things as root ...        

        return null;
      }
    });

    ... do things as original non root user

    GrouperSession.stopQuietly(grouperSession);

```

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/GrouperSession.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/GrouperSession.html)
