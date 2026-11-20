---
title: "Grouper Training - Use cases - Lesson 09: VPN access control part 2"
space: Grouper
pageId: 28544298
version: 29
lastUpdated: 2026-11-18T20:22:05.157Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544298/Grouper+Training+-+Use+cases+-+Lesson+09+VPN+access+control+part+2
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

In this lesson, anything you type is in a box with a copy button on the right. Buttons, menus, and labels to click or select are in double quotes.

## Hands on

### Translate natural language policy to digital policy

The natural language policy is “Faculty, staff, and some exceptions (students, contractors, etc)”.

#### Create the vpn application

1. In the left menu, click "Browse folders", click "Root", then click the folder "app".
2. Click "Folder actions" → "Run template".
3. "Template to run": select "Application".
4. "Key":
  
  
  ```
  vpn
  ```
5. "Description":
  
  
  ```
  VPN access policies
  ```
6. Click "Next". Review the folders and groups the template will create, then click "Submit".

#### Create the vpn_authorized policy group

1. Go to the folder "app:vpn:service:policy" (Browse folders → Root → app → vpn → service → policy).
2. Click "Folder actions" → "Run template".
3. "Template to run": select "Policy group".
4. "Key":
  
  
  ```
  vpn_authorized
  ```
5. Click "Next". Review the groups the template will create, then click "Submit".

#### Add faculty and staff to the allow group

1. Go to the group "app:vpn:service:policy:vpn_authorized_allow".
2. Click "Add members".
3. In "Member name or ID", paste this, pick "All Faculty/Staff" from the list, and click "Add":
  
  
  ```
  ref:role:all_facstaff
  ```

### Create provisioner

1. In the left menu, click "Miscellaneous", then click "Provisioning".
2. Click "Provisioning actions" → "Add provisioner".
3. "Configuration id":
  
  
  ```
  groupOfNames2
  ```
4. "Provisioner type": select "Ldap".
5. "Start with": select "LDAP 'start with'".
6. "External system config id": select "demo".
7. "Ldap pattern": select "groupOfNames".
8. "Group DN type": select "flat".
9. "User attributes type": select "core".
10. "Group search base DN":
  
  
  ```
  ou=groups,dc=internet2,dc=edu
  ```
11. "RDN groups attribute":
  
  
  ```
  cn
  ```
12. "RDN value for groups": select "name".
13. "Membership attribute name for groups":
  
  
  ```
  member
  ```
14. "Membership attribute default value":
  
  
  ```
  <emptyString>
  ```
15. "Object classes for groups": select "groupOfNames".
16. "Other group ldap attributes":
  
  
  ```
  description
  ```
17. "Entity search base DN":
  
  
  ```
  ou=people,dc=internet2,dc=edu
  ```
18. "Select all entities at once during full sync": select "True".
19. "Matching search attribute name for entities":
  
  
  ```
  uid
  ```
20. "Matching search attribute value for entities": select "subjectIdentifier0".
21. "Object classes for entities":
  
  
  ```
  eduPerson
  ```
22. Click "Submit" to continue to the full form.
23. Click "Submit" to save the provisioner.

In this provisioner, a group syncs to a record in LDAP in the ou=groups tree. The cn value is the full group name. The member attribute is multi-valued and holds the LDAP DNs of the group’s members.

### Provision vpn_authorized to OpenLDAP

1. Go to the group "app:vpn:service:policy:vpn_authorized".
2. Click "Group actions" → "Provisioning".
3. Click "Provisioning actions" → "Edit provisioning settings".
4. "Target name": select "groupOfNames2".
5. "Provision": select "Yes".
6. Click "Save".

### Enable and run the full sync provisioning job

A full sync daemon job was created when the provisioner was added. It needs to be enabled.

1. In the left menu, click "Miscellaneous", then click "Daemon jobs".
2. Type this in the filter box and click "Apply filter":
  
  
  ```
  OTHER_JOB_provisioner_full_groupOfNames2
  ```
3. Click the job name to go to its detail page.
4. Click "Job actions" → "Enable job".
5. Click "Job actions" → "Run job now".

### Verify results

1. Open phpLDAPadmin and log in:
  
  
  ```
  https://localhost:8443/phpldapadmin
  ```
2. Go to ou=groups and review your new Grouper managed vpn access control group.

In real life you would then investigate exceptions, add them to the ad hoc group as needed, and open a service ticket to have the network team switch the VPN config to use vpn_authorized.

Some important goals have been accomplished:

- Automatic provisioning/deprovisioning of VPN access for faculty and staff
- Natural language policy - clear and visible
- Exceptions management

This is a huge improvement! However, we are still dealing with tickets to add and remove subjects (well at least to add!) to the ad hoc group. There is no way to distinguish different exceptions, and it is not clear who is responsible for lifecycle and attestation.

### Enable the incremental job

1. In the left menu, click "Miscellaneous", then click "Daemon jobs".
2. Type this in the filter box and click "Apply filter":
  
  
  ```
  CHANGE_LOG_consumer_provisioner_incremental_groupOfNames2
  ```
3. Click the job name, then click "Job actions" → "Enable job". You do not need to run it yet.

### Implement distributed exception management

Each policy exception is represented by an application specific access control list (ACL). vpn_consultants will be managed by the IAM team. Management of vpn_wri250 will be delegated to a course instructor.

#### Create the ACL groups

1. Go to the folder "app:vpn:service:policy" (Browse folders → Root → app → vpn → service → policy).
2. Click "Folder actions" → "Create new group".
3. "Group name":
  
  
  ```
  vpn_consultants
  ```
4. Click "Save". You land on the new group page "app:vpn:service:policy:vpn_consultants".
5. Go to the folder "app:vpn:service:policy" (Browse folders → Root → app → vpn → service → policy).
6. Click "Folder actions" → "Create new group".
7. "Group name":
  
  
  ```
  vpn_wri250
  ```
8. Click "Save". You land on the new group page "app:vpn:service:policy:vpn_wri250".

#### Add the ACLs to the allow group

1. Go to the group "app:vpn:service:policy:vpn_authorized_allow".
2. Click "Add members" → "Import".
3. Scroll down to "Add these members" and change the radio button to "Copy/paste a list of member IDs".
4. Paste this whole list into the text area, then click "Submit":
  
  
  ```
  app:vpn:service:policy:vpn_consultants
  app:vpn:service:policy:vpn_wri250
  ```

#### Delegate vpn_wri250 to the instructors

Professor Jenkins (kjenkins) runs a special project for course WRI250 that includes online resources that can only be accessed from the VPN. The professor should be able to control who has VPN access for the project. To delegate management of vpn_wri250, create a security group and grant it privileges.

1. Go to the folder "app:vpn:security" (Browse folders → Root → app → vpn → security).
2. Click "Folder actions" → "Create new group".
3. "Group name":
  
  
  ```
  vpn_wri250_mgr
  ```
4. Click "Save". You land on the new group page "app:vpn:security:vpn_wri250_mgr".
5. Click "Add members".
6. In "Member name or ID", paste this, pick "instructor" from the list, and click "Add":
  
  
  ```
  basis:sis:course:wri:wri250:instructor
  ```

Grant the security group UPDATE and READ on vpn_wri250:

1. Go to the group "app:vpn:service:policy:vpn_wri250".
2. Click the "Privileges" tab, then click "Add members".
3. In "Member name or ID", paste this and pick "vpn_wri250_mgr" from the list:
  
  
  ```
  app:vpn:security:vpn_wri250_mgr
  ```
4. "Assign these privileges": check "UPDATE" and "READ".
5. Click "Add", then review the privileges on vpn_wri250.

#### Manage the ACL as the instructor

1. Open a private / incognito browser window and go to:
  
  
  ```
  https://localhost:8443/grouper
  ```
2. Log in with username:
  
  
  ```
  kjenkins
  ```
3. and password:
  
  
  ```
  password
  ```
4. Go to the group "app:vpn:service:policy:vpn_wri250". You can add and remove members of this ACL.
5. Click "Add members".
6. In "Member name or ID", paste this, pick the match from the list, and click "Add":
  
  
  ```
  mwest
  ```

#### Trace the new membership

1. Switch back to your banderson browser window.
2. Go to the group "app:vpn:service:policy:vpn_authorized".
3. On the "Members" tab, type this in the "Member name" filter box and click "Apply filter":
  
  
  ```
  mwest
  ```
4. On the row for mwest, click "Actions" → "Trace membership".

As banderson, check whether the incremental job provisioned the new user:

1. In the left menu, click "Miscellaneous", then click "Daemon jobs".
2. Type this in the filter box and click "Apply filter":
  
  
  ```
  CHANGE_LOG_consumer_provisioner_incremental_groupOfNames2
  ```

### Implement additional policy constraints

#### Global deny

It is the IAM team’s responsibility to make sure that VPN access is granted to the correct subjects. Putting some limits in place can help make sure improper access is not granted. Attestation makes sure that access which was granted in the past is still appropriate.

The ref:iam:global_deny reference group represents a broad cohort of subjects that should not be granted access. Subjects that fall into this category may include:

- Termed with cause
- Deceased
- Other reasons

ref:iam:global_deny was automatically added to vpn_authorized_deny.

#### WRI250 attestation

As banderson, add a 30 day attestation requirement to vpn_wri250:

1. Go to the group "app:vpn:service:policy:vpn_wri250".
2. Click "Group actions" → "Attestation".
3. Click "Attestation actions" → "Edit attestation settings".
4. "Attestation": select "Yes, does have attestation directly assigned".
5. "Use default recertify days": select "No, use custom recertify days".
6. "Days until recertify":
  
  
  ```
  30
  ```
7. Click "Save".

As kjenkins, review attestations: in the left menu, click "Miscellaneous", then click "Attestation".

The daemon that sends attestation emails generally runs once at night. Force it to run so you can see the email. As banderson:

1. In the left menu, click "Miscellaneous", then click "Daemon jobs".
2. Type this in the filter box and click "Apply filter":
  
  
  ```
  OTHER_JOB_attestationDaemon
  ```
3. Click the job name, then click "Job actions" → "Run job now".
4. Open the mail server (also linked from the GTE jump page):
  
  
  ```
  https://localhost:8443/mail/
  ```

#### Consultants

Consultant exceptions should expire automatically after 180 days. There are 2 techniques to do this in Grouper. The first is to edit the membership end date after you add a subject to a group. The second, and more reliable, is a rule that runs every time a subject is added and sets the membership end date. Let’s implement the second approach.

1. Go to the group "app:vpn:service:policy:vpn_consultants".
2. Click "Group actions" → "Rules".
3. Click "Rules actions" → "Add rule".
4. "Rule pattern": select "Add disabled date on membership".
5. "Expire after X days":
  
  
  ```
  180
  ```
6. Click "Save".
7. Click "Add members".
8. In "Member name or ID", paste this, pick "Ricardo Johnson" from the list, and click "Add":
  
  
  ```
  rjohnso5
  ```
9. On the "Members" tab, click "Advanced".
10. "Enabled / disabled": select "Enabled / disabled status", then click "Apply filter". Review the enabled/disabled status of the membership.

### Answering audit questions - does jadams3 have access to VPN? When?

The CISO is working on an investigation and wants to know if NetID jadams3 has access to the VPN now or had it in the past 90 days.

1. Go to the group "app:vpn:service:policy:vpn_authorized".
2. On the "Members" tab, type this in the "Member name" filter box and click "Apply filter":
  
  
  ```
  jadams3
  ```
3. On the row for jadams3, click "Actions" → "Trace membership".

Joseph currently has access since he is staff. The point in time (PIT) tables know if he has had access in the last 90 days. To see this, on the "Members" tab click "Advanced", set "Point in time audit" to "Yes, show audit memberships", and click "Apply filter". This shows his earliest access date.
