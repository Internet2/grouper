---
title: "Subject Identifier column in member table"
space: Grouper
pageId: 28548814
version: 6
lastUpdated: 2026-07-12T15:26:58.448Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548814/Subject+Identifier+column+in+member+table
---

As of Grouper 2.3, a new column has been added to the grouper_members table called subject_identifier0. This allows Grouper to store one subject identifier for each subject to reduce the number of subject lookups during certain operations. See the [Subject sources](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544786/Subject+sources) document for definitions and examples of subject identifiers.

You can store any subject attribute in this column using the sources.xml configuration. Here's an example:

```
    <!-- subject identifier to store in grouper's member table -->
    <init-param>
      <param-name>subjectIdentifierAttribute0</param-name>
      <param-value>uid</param-value>
    </init-param>
```

### Sync

To initially populate the column after you have configured it and if you make a change to this configuration again in the future, you should sync the attribute. For subjects that are people, you can use USDU:

```
gsh 0% // run USDU to resolve all the subjects with type=person
gsh 1% GrouperSession.startRootSession()
gsh 2% subject=SubjectFinder.findById("GrouperSystem")
subject: id='GrouperSystem' type='application' source='g:isa' name='GrouperSysAdmin'
gsh 3% session=GrouperSession.start(subject)
edu.internet2.middleware.grouper.GrouperSession: 8106bdad683d43f88bf24c8e683f6162,'GrouperSystem','application'
gsh 4% usdu()
usdu completed successfully

```

For subjects that are groups, you can run the following line using [GSH](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh):

```
gsh 0% GrouperSession.startRootSession()
gsh 1% for (String g : HibernateSession.byHqlStatic().createQuery("select uuid from Group").listSet(String.class)) { subj = SubjectFinder.findByIdAndSource(g, "g:gsa", true); GrouperDAOFactory.getFactory().getMember().findBySubject(subj).updateMemberAttributes(subj, true); }

```
