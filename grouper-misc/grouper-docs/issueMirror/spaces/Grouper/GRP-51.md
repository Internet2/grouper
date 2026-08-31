---
key: GRP-51
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-51
type: Bug
status: Resolved
resolution: Completed
priority: Minor
reporter: Tom Zeller <tzeller@example.com>
assignee: Tom Zeller <tzeller@example.com>
created: 2007-10-10T15:41:38.266+0000
updated: 2011-03-02T16:48:58.754+0000
resolved: 2011-03-02T16:48:58.669+0000
components: [API]
fixVersions: [HEAD]
labels: []
links: []
---

# GRP-51  GrouperDAOException when threads 'simultaneously' call MemberFinder.internal_findOrCreateBySubject

More than one thread calling

 MemberFinder.findBySubject();

may result in

GrouperDAOException: Could not execute JDBC batch update at edu.internet2.middleware.grouper.internal.dao.hibernate.HibernateMemberDAO.create(HibernateMemberDAO.java:76)
        at edu.internet2.middleware.grouper.MemberFinder.internal_findOrCreateBySubject(MemberFinder.java:139)
        at edu.internet2.middleware.grouper.MemberFinder.internal_findBySubject(MemberFinder.java:123)
        at edu.internet2.middleware.grouper.MemberFinder.findBySubject(MemberFinder.java:53)
...
JDBCExceptionReporter:58 - ERROR: duplicate key violates unique constraint "grouper_members_subject_id_key"

Our provisioning software uses one thread per provisioned resource, of which we have around 10. When evaluating group membership for the first time for a subject, we've seen this exception. Our workaround is along the lines of

Member member;
try {
 member = MemberFinder.findBySubject(s, SubjectFinder.findById());
} catch (GrouperDAOException e) {
 // try again
  member = MemberFinder.findBySubject(s, SubjectFinder.findById());
}

## Comments

### tzeller@example.com - 2011-02-16T17:46:32.372+0000

I should check logs to verify that this issue is resolved and then close this issue.

### tzeller@example.com - 2011-03-02T16:48:58.711+0000

Issue will be fixed by hibernate logging.