---
key: GRP-38
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-38
type: Bug
status: Resolved
resolution: Fixed
priority: Minor
reporter: James Cramton <jcramton@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-09-20T16:19:22.535+0000
updated: 2007-12-07T16:51:46.116+0000
resolved: 2007-10-17T15:00:48.902+0000
components: [UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-38  Do not list nameless groups for which no View privilege is granted

In Brown's Grouper 1.2.0 instance, we have modified the default group privileges granted to GrouperAll, thinking that by default, we do not want users to see that a group exists unless explicitly granted View privilege. These are the pertinent settings from our grouper.properties file:

# If set to _true_, the ALL subject will be granted that privilege on
# each new group that is created.  
groups.create.grant.all.admin         = false
groups.create.grant.all.optin         = false
groups.create.grant.all.optout        = false
groups.create.grant.all.read          = false # (was true)
groups.create.grant.all.update        = false
groups.create.grant.all.view          = false # (was true)


So as I understand it, this should prevent anyone from having any privileges on any group, unless they have privileges explicitly set upon group creation--either in the MACE Grouper UI, or through our provisioning program.

But our typical MACE Grouper user's subject summary page looks like this:

is a member of : []
is a member of : []
is a member of : []
is a member of : []
is a member of : []
is a member of : [COURSE:TEST:0001:2007-Fall:S01: Learner Students ]


The nameless lines are various demographic groups, including EAB:EMPLOYEE:ONCAMPUS. We created a command line script to evaluate the explicit privileges set on a group. This script shows that only members of the ADMIN:COMMUNITY group have View privilege on EAB:EMPLOYEE:ONCAMPUS. The ADMIN:COMMUNITY group is empty, and was created only to support the ACL. This setup is why I can't view the name of the group, but I would expect Grouper to not fetch the group, if I don't have view privilege on that group. 

Conversely, the 2nd example below shows the group info for the test course group listed on my Subject Summary page. Presumably, I can only see this name because I have the View privilege inherited from the Admin privilege I have as a member of ADMIN:COURSE. 

Conclusion: Grouper's UI is heeding the View privilege correctly, but the fetch logic is fetching groups for which I do not have View privilege. I would expect Grouper to only fetch groups that I can view.


Group: EAB:EMPLOYEE:ONCAMPUS
  Person members: (10720)
    <snip>
  No group members
  Group types: (2)
    base
    provisioned
  ACLs:
    admin:
      GrouperSystem
    view:
      ADMIN:COMMUNITY
  Creation and modification:
    createSource = ""
    createSubjectName = "GrouperSystem"
    createTime = "Wed Aug 22 17:14:05 EDT 2007"
    modifySource = ""
    modifySubjectName = "GrouperSystem"
    modifyTime = "Tue Sep 11 03:34:31 EDT 2007"
  Attributes:
    description = "Base group for EMPLOYEE.ONCAMPUS"
    displayExtension = "ONCAMPUS"
    displayName = "EAB:EMPLOYEE:ONCAMPUS"
    extension = "ONCAMPUS"
    name = "EAB:EMPLOYEE:ONCAMPUS"
    provisionLastUpdate = "20070911032245"
    provisionSource = "EAB.EMPLOYEE.ONCAMPUS"




Group: COURSE:TEST:0001:2007-Fall:S01:Student
  Person members: (3)
     <snip>
  No group members
  Group types: (1)
    base
  ACLs:
    admin:
      ADMIN:COURSE
      GrouperSystem
    read:
      COURSE:TEST:0001:2007-Fall:S01:Administrator
      SERVICE:BULK_MAIL
      SERVICE:WEBAUTH
    update:
      COURSE:TEST:0001:2007-Fall:S01:Administrator
  Creation and modification:
    createSource = ""
    createSubjectName = "GrouperSystem"
    createTime = "Thu Sep 06 11:12:43 EDT 2007"
    modifySource = ""
    modifySubjectName = "GrouperSystem"
    modifyTime = "Thu Sep 06 11:14:15 EDT 2007"
  Attributes:
    description = "Students for TEST0001 S01 2007-Fall"
    displayExtension = " Learner Students "
    displayName = "COURSE:TEST:0001:2007-Fall:S01: Learner Students "
    extension = "Student"
    name = "COURSE:TEST:0001:2007-Fall:S01:Student"




## Comments

### James Cramton - 2007-09-20T16:33:14.169+0000

Screenshot showing an individual subject summary. The missing group names are demographic groups for which my user has not been granted view permission. They should not show at all, rather than list a nameless group.

### Gary Brown - 2007-09-26T10:43:18.981+0000

There are several things going on here:

1) The code which checks whether the subject associated with GrouperSession can view a membership.

try {
        if ( FieldType.ACCESS.equals( ms.getList().getType() ) ) {
          dispatch( s, ms.getGroup(), s.getSubject(), ms.getList().getReadPriv() );
        }
        mships.add(ms);
      }
      catch (Exception e) {
        ErrorLog.error( PrivilegeHelper.class, "canViewMemberships: " + e.getMessage() );
      }

is not working properly. I don't think that the if statement is necessary (for groups - not sure if used for stems) - it doesn't resolve to true so all memberships are returned regardless of whether the Subject has READ privilege.

2) Once the UI has a Membership it calls Membership.getGroup - which does no privilege check  -you ought to have READ to have the membership

3) I'm not sure that Group.getAttribute is respecting the read_privilege field in the grouper_fields table

4) There may be some other UI things going on which lead to the [] you are seeing, however, we can look at those if they still arise after 1) is fixed.

### Gary Brown - 2007-10-17T15:00:48.836+0000

I've removed the condition in the 1.2.1 PrivilegeHelper class. This ensured the correct result for me, but it would be good if Brown could verify that it fixes their problem.

### James Cramton - 2007-12-07T16:51:44.913+0000

I can confirm that 1.2.1 resolved this display issue at Brown.

## Attachments
- edu.brown.subjectSummaryNoGroupNames.jpg (187940 bytes) - by James Cramton on 2007-09-20T16:33:14.110+0000