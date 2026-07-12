---
title: "Grouper Training - Use cases - Lesson 09: VPN access control part 2"
space: Grouper
pageId: 28544298
version: 27
lastUpdated: 2026-07-12T15:26:17.091Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544298/Grouper+Training+-+Use+cases+-+Lesson+09+VPN+access+control+part+2
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

## Hands On

### Translate natural language policy to digital policy

The natural language policy is "Faculty, staff, and some exceptions (students, contractors, etc)"

- Use the application template and the policy group template to create a new `vpn` application folder
- Create a policy group called `vpn_authorized`
- Add the *All Faculty/Staff* group to *vpn_authorized_allow*

### Create provisioner

Go to Miscellaneous > Provisioning and add a new provisioner

- Configuration Id: `groupOfNames2`
- Provisioner type: Ldap
- Start with: LDAP 'start with'

- External system config id: demo
- Ldap pattern: groupOfNames

- Group DN type: flat
- User attributes type: core
- Group search base DN: `ou=groups,dc=internet2,dc=edu`
- RDN groups attribute: `cn`
- RDN value for groups: name
- Membership attribute name for groups: `member`
- Membership attribute default value: `<emptyString>`
- Object classes for groups: groupOfNames
- Other group ldap attributes: `description`
- Entity search base DN: `ou=people,dc=internet2,dc=edu`
- Select all entities at once during full sync: True
- Matching search attribute name for entities: `uid`
- Matching search attribute value for entities: subjectIdentifier0
- Object classes for entities: `eduPerson`

Click Submit to continue to the full form.

Click Submit to complete the form

In this provisioner, a group will sync to a record in LDAP in the *ou=Groups* tree. The *cn* value will be the full group name. The *member* attribute will be multi-valued, containing the LDAP Dn values for the groups' members

### Provision vpn_authorized to OpenLDAP

- Configure provisioning on *app:vpn:service:policy:vpn_authorized*

### Enable and run full sync provisioning job

A full sync daemon job was created when the provisioner was added. It needs to be enabled

- Go to Miscellaneous->Daemon jobs
- Filter for job name `OTHER_JOB_provisioner_full_groupOfNames2`
- Click on name to go to detail page
- Enable job
- Run job

### Verify results

- Log in to [https://localhost:8443/phpldapadmin](https://localhost:8443/phpldapadmin) and navigate to *ou=groups*. Review your new Grouper managed vpn access control group!
- Investigate exceptions and add to the ad-hoc group as needed
- *Open a service ticket to have the network team switch the VPN config to use vpn_authorized.

Some important goals have been accomplished:

- Automatic provisioning/deprovisioning of VPN access for faculty and staff.
- Natural language policy - clear and visible.
- Exceptions management

This is a huge improvement! However, we are still dealing with tickets to add and remove subjects (well at least to add!) to the ad-hoc group. There is no way to distinguish different exceptions, and it is not clear who is responsible for lifecycle and attestation.

### Enable incremental job

Locate daemon job CHANGE_LOG_consumer_provisioner_incremental_groupOfNames2 and enable it. No need to run it yet

### Implement distributed exception management

Each policy exception is represented by an application specific access control lists (ACL).

- Create `app:vpn:service:policy:vpn_consultants`. This ACL will be managed by the IAM team.
- Create `app:vpn:service:policy:vpn_wri250`. Management of this ACL will be delegated to a course instructor.
- Add each of these ACLs to *vpn_authorized_allow*

Professor Jenkins (`kjenkins`) runs a special project for course WRI250 that includes various online resources that can only be accessed from the VPN. The professor should be able to control who is allowed to have VPN access for the purpose of accessing his project’s resources.

We already created an access control list (ACL) *app:vpn:service:policy:vpn_wri250* to represent subjects that will access resources related to the special project. In order to delegate management of this ACL to the course instructors, we must create a security group and grant it appropriate permissions:

- Create `app:vpn:security:vpn_wri250_mgr`
- Add the instructors for WRI250 to this security group (hint: there is a basis group for this)
- Grant *vpn_wri250_mgr* UPDATE and READ to *vpn_wri250*
- Review the privileges on *vpn_wri250*
- In a private browser window, log in to [http://localhost:8443/grouper](http://localhost:8443/grouper) with username `kjenkins` and password `password`. You should be able to add and remove members from the *vpn_wri250* ACL.
- Add student `mwest` to *vpn_wri250*
- Switch back to `banderson`. Find `mwest` in *vpn_authorized* and trace membership

As banderson, find the provisioner incremental job and see whether the new user got provisioned.

### Implement additional policy constraints

#### Global deny

It is the IAM team’s responsibility to make sure that VPN access is granted to the correct subjects. Putting some limits in place can help make sure improper access is not granted. Attestation makes sure that access which was granted in the past is still appropriate.

The ref:iam:global_deny reference group represents a broad cohort of subjects that should not be granted access. Subjects that fall into this category may include:

- Termed with cause
- Deceased
- Other reasons

ref:iam:global_deny was automatically added to the vpn_authorized_deny

#### WRI250

- As banderson, add 30 day attestation requirements to the *vpn_wri250* ACL. (vpn_wri250 -> More actions -> Attestation -> Attestation actions -> Edit attestation settings…)
- As `kjenkins`, review attestations (Miscellaneous -> Attestation)

The daemon to send attestation emails generally runs once at night. We can force it to run so we can see the email.

- As banderson, run daemon job OTHER_JOB_attestationDaemon
- Click on the Jump page link for the [mail server](https://localhost:8443/mail/)

#### Consultants

Consultant exceptions should expire automatically after 180 days. There are 2 techniques to accomplish this in Grouper. The first is to simply edit the membership end date after you have added a subject to a group. The second, and more reliable, is to have a rule that runs every time a subject is added which automatically sets the membership end date. Let’s implement the second approach.

In the vpn_consultants group, go to Group actions > Rules

Add rule "Add disabled date on membership"

Expire after X days: 180

Add Ricardo Johnson (`rjohnso5`) to *vpn_consultants*

Review Enabled/Disabled status - Membership -> Filter for: -> Advanced -> Enabled/disabled

### Answering Audit Questions - Does "jadams3" have access to VPN? When?

The CISO is working on a investigation and wants to know if this particular NetID "jadams3" has access to the VPN now or in the past 90 days?

- Navigate to *app:vpn:service:policy:vpn_authorized*
- Search for `jadams3` and trace membership.

Joseph currently has access since he is staff. The Point-In-Time (PIT) tables know if he’s had access in the last 90 days. These can be access using the advanced membership filter. This shows his earliest access date.
