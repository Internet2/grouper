---
title: "Grouper Role and Permission Management"
space: Grouper
pageId: 28544259
version: 54
lastUpdated: 2026-07-12T15:26:16.139Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544259/Grouper+Role+and+Permission+Management
---

## Diagrams

Note: there are 4 levels of hierarchies in Grouper permissions.

1. Indirect group membership to have a role
2. Permissions that imply other permissions (so you can assign one permissions and get a lot of rights)
3. Actions that imply other actions (e.g. admin implies all other actions)
4. Role inheritance so a role can inherit all permissions from another role, and add some more

Here are examples and diagrams of Grouper permissions

## When to use Grouper Permissions

1. When the application can support permissions being provisioned to it
2. Helps if your application has a specific and probably custom UI to assign permissions
  
  1. E.g. imagine assigning permissions on Confluence pages outside of the Confluence app? Might be difficult to use
  2. Grouper has a permissions UI but it is generic
3. Grouper permissions do not provision with PSPNG. You need to provision permissions to the application using Grouper WS or SQL or Java
4. Grouper permissions will tell you real time when assignments change (for real time provisioning), but it only indicates that a role has changed somehow
  
  1. If you are doing a change log consumer or messaging, you need to get that indication and do a full sync of permissions for that role

## Role and Permission Management as of v2.0 and above

Grouper has the capability to manage external applications' roles and permissions, and can function as a central permission management system.

Note that "privilege" is interchangeable with "permission", but Grouper already has documents about internal Grouper privileges on Groups / folders / etc. so the word "permission" is used here.

- Roles can be stored in Grouper. Roles can be assigned to subjects or groups.
- External application permission objects can be stored in Grouper.
- Permissions can be assigned to roles or to subjects since they are modeled as types of attributes on role memberships or roles.
- Roles can be configured to imply other roles. For example a Senior Loan Administrator is a Loan Administrator, plus a few more security grants. Roles can be connected like a directed graph of role inheritance
- Permissions can grouped into permissionSets. E.g. if an organization hierarchy was represented as permissions, then the higher level organizations can imply the lower level ones. Note this does not have to be a hierarchy
- Permission assignments have an optional "action" qualifier. This is a free form string which is configured per permission definition. E.g. a user can READ certain orgs, and WRITE certain other orgs.
- Permission actions can imply other actions. e.g. Having ADMIN on a permission resource implies being able to READ or WRITE it. Note, there are no built in actions (though a default "assign" exists if none specified). So the actions and action inheritance needs to be defined
- Permission assignments can be [ALLOWED or DISALLOWED](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547660/Grouper+permissions+allow+and+disallow). With all the inheritance (permission resource, role, action, memberships), if a permission is allowed to a wide population, then it can be narrowed with a disallow. For example, someone could be assigned to READ all orgs in the University in the payroll system, except for the user's own org.
- [Permission limits](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548474/Grouper+permission+limits) can be assigned to direct permission assignments. The limit can have a value or type string, numeric, date, etc. The limit has logic associated with it to use the optional value and context from the caller to decide if the permission is allowed or not. There are [built-in limits](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28673891/Permission+limit+builtin+implementations) for value (e.g. allowed to approve if value less then 50000), time of day (only allowed during business hours), ip address, etc.
- All permissions operations are exposed through the Grouper Lite UI
- Videos:
  
  - [Demo of permissions](http://www.youtube.com/watch?v=E2-fGjE2obQ)
  - [Attribute definitions](http://www.youtube.com/watch?v=EYH_T86IJ9k) (definition holds security, name, actions for all permission names associated)
  - [Attribute definition privileges](http://www.youtube.com/watch?v=Qqg7aVJ-SkE) (attribute definition privileges control who can list the permissions, or assign them)
  - [Permission name hierarchies](http://www.youtube.com/watch?v=w0z4AA3p1aQ)
  - [Role editor](http://www.youtube.com/watch?v=IVObg36d4Qs)
  - [Directed graph visualization](http://www.youtube.com/watch?v=DzBvOteaXJM)
  - Permissions demos of allow/deny
    
    - [Common setup](http://www.youtube.com/watch?v=axvbV7-Bhbc)
    - [Role assignment vs individual assignment](http://www.youtube.com/watch?v=gqooNOjVioQ)
    - [Role assignment vs individual assignment up the hierarchy](http://www.youtube.com/watch?v=tVivglVIudw)
    - [Role assignment vs individual assignment up the hierarchy2](http://www.youtube.com/watch?v=aPRzX5gQTHM)
    - [Action directed graph priority](http://www.youtube.com/watch?v=G8R7XUAaG1M)
    - [Various role assignments](http://www.youtube.com/watch?v=0oLzYvMFCPw)
    - [Role inheritance](http://www.youtube.com/watch?v=qcH14ada-5k)
    - [Directed graph priority](http://www.youtube.com/watch?v=0YUftOqeWtA)
    - [Directed graph priority with tie](http://www.youtube.com/watch?v=I9Sp92rzoWI)
    - [Resource directed graph tie with different actions](http://www.youtube.com/watch?v=FU57u3pIA2E)
  - [Permission limits UI](http://www.youtube.com/watch?v=06l381Myjxg)

See also the [Overview of Access Management Features](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544689/Access+Management+Features+Overview) page for guidelines of when to use rules, roles, permission limits, and enabled / disabled dates.

### GSH commands

Sample

```
import edu.internet2.middleware.grouper.permissions.*;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;

GrouperSession grouperSession = GrouperSession.startRootSession();

Group test = new GroupFinder().addGroupName("test:test").findGroup();
AttributeDefName perm = AttributeDefNameFinder.findByName("test:permName", true);

test.getPermissionRoleDelegate().assignRolePermission(perm);
Subject subject = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
test.getPermissionRoleDelegate().assignSubjectRolePermission(perm, subject);

for (PermissionEntry permissionEntry : new PermissionFinder().assignPermissionType(PermissionType.role).assignImmediateOnly(true).addRole("test:test").findPermissions()) {      System.out.println(permissionEntry.getAttributeDefNameName());    }

```

Create a role

```
gsh 30% userSharerRole = rolesStem.addChildRole("userSharer", "userSharer");

```

Add a member to a role (in this case a group)

```
gsh 38% userSharerRole.addMember(studentsGroup.toSubject());

```

Create a permission definition

```
gsh 51% resourcesDef = resourcesStem.addChildAttributeDef("secureShareWebResources", AttributeDefType.perm);

```

Add one permissions resource name to another (permissionSet)

```
gsh 63% receiveSetResource.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(splashResource);

```

Assign a permission to a role

```
gsh 70% userSharerRole.getPermissionRoleDelegate().assignRolePermission(sendSetResource);

```

Assign a permission to a member in a role

```
gsh 73% adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(adminEmailButtonResource, schleindMember);

```

Get the permission assignments (not necessarily active or allowed), assigned to a role, immediate, based on role name, print these out

```
gsh 123% for (PermissionEntry permissionEntry : new PermissionFinder().assignPermissionType(edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType.role).assignImmediateOnly(true).addRole("a:b").findPermissions()) {      System.out.println(permissionEntry.getAttributeDefNameName());    }
    for (PermissionEntry permissionEntry : new PermissionFinder().assignPermissionType(edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType.role).assignImmediateOnly(true).addRole("a:b").findPermissions()) {      System.out.println(permissionEntry.getAttributeDefNameName());    }

```

Get the permission assignments (not necessarily active or allowed), assigned to a role, immediate, based on permission name, print these out

```
for (PermissionEntry permissionEntry : new PermissionFinder().assignPermissionType(edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType.role).assignImmediateOnly(true).addPermissionName("a:b").findPermissions()) {      System.out.println(permissionEntry.getRoleName());    }

```

sdf

### SQL interface

The view for permissions is grouper_perms_all_v. Note, results here need to be processed is allow/disallow is used, also you should take into account if the records are active or not

get all attributes assigned to a role, assuming direct assignment (unassignable)

```
SELECT GPAV.ATTRIBUTE_DEF_NAME_NAME
  FROM grouper_perms_all_v gpav
 WHERE     GPAV.ROLE_NAME = 'a:b'
       AND gpav.permission_type = 'role'
       AND GPAV.ROLE_SET_DEPTH = 0
       AND GPAV.ATTR_ASSIGN_ACTION_SET_DEPTH = 0
       AND GPAV.ATTR_DEF_NAME_SET_DEPTH = 0
       AND GPAV.MEMBERSHIP_DEPTH = 0

```

get all roles that are assigned a given attribute, assuming direct assignment (unassignable)

```
SELECT GPAV.role_name
  FROM grouper_perms_all_v gpav
 WHERE     GPAV.ATTRIBUTE_DEF_NAME_NAME = 'a:b'
       AND gpav.permission_type = 'role'
       AND GPAV.ROLE_SET_DEPTH = 0
       AND GPAV.ATTR_ASSIGN_ACTION_SET_DEPTH = 0
       AND GPAV.ATTR_DEF_NAME_SET_DEPTH = 0
       AND GPAV.MEMBERSHIP_DEPTH = 0

```

#### See also

[Access Management Features Overview](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544689/Access+Management+Features+Overview)

[Grouper New Template Wizard](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545142/Template+wizard)

[Training Slides, pages 31-38](https://spaces.at.internet2.edu/download/attachments/14517786/GrouperTraining-Apereo_part3.pdf?version=1&modificationDate=1370270516490)
