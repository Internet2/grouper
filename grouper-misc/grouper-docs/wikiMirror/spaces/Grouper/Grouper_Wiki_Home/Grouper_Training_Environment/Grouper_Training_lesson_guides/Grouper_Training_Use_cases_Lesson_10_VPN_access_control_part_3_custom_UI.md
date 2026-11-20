---
title: "Grouper Training - Use cases - Lesson 10: VPN access control part 3 - custom UI"
space: Grouper
pageId: 28545541
version: 18
lastUpdated: 2026-11-19T18:25:50.018Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545541/Grouper+Training+-+Use+cases+-+Lesson+10+VPN+access+control+part+3+-+custom+UI
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

In this lesson, anything you type is in a box with a copy button on the right. Buttons, menus, and labels to click or select are in double quotes.

## Links

[Grouper Custom UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549064/Grouper+Custom+UI)

[Grouper Custom UI example consent to T&C](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554873/Grouper+Custom+UI+example+consent+to+T+C)

## Hands on

### Create reference group for acknowledged users

#### Create the folder ref:certs

1. In the left menu, click "Browse folders", click "Root", then click the folder "ref".
2. Click "Folder actions" → "Create new folder".
3. "Folder name":
  
  
  ```
  Certifications
  ```
4. "Folder ID": check "Edit the ID", then type:
  
  
  ```
  certs
  ```
5. "Description":
  
  
  ```
  Groups of users with specific acknowledgements, certifications, training completions, etc.
  ```
6. Click "Save". You are now on the folder "ref:certs".

#### Create the group ref:certs:network_aup_acknowledged

1. On the folder "ref:certs", click "Folder actions" → "Create new group".
2. "Group name":
  
  
  ```
  Network Acceptable Use Policy (AUP) acknowledged
  ```
3. "Group ID": check "Edit the ID", then type:
  
  
  ```
  network_aup_acknowledged
  ```
4. "Description":
  
  
  ```
  Users who have attested they have read and accepted the AUP document at http://example.edu/aup
  ```
5. "Assign privileges to everyone": check "OPTIN".
6. Click "Save". You are now on the group "ref:certs:network_aup_acknowledged".

#### Add the ref type

1. On the group "ref:certs:network_aup_acknowledged", click "Group actions" → "Types".
2. Click "Type actions" → "Edit type settings".
3. "Type": select "Yes, has direct type configuration".
4. "Type name": select "ref".
5. Click "Save".

### Create rule to expire memberships after a year

1. Go to the group "ref:certs:network_aup_acknowledged".
2. Click "Group actions" → "Rules".
3. Click "Rules actions" → "Add rule".
4. "Rule pattern": select "Add disabled date on membership".
5. "Expire after X days":
  
  
  ```
  365
  ```
6. Click "Save".

### Create rule to email before a membership expires

1. Go to the group "ref:certs:network_aup_acknowledged".
2. Click "Group actions" → "Rules".
3. Click "Rules actions" → "Add rule".
4. "Rule pattern": select "Send email due to disabled date".
5. "Minimum days in the future":
  
  
  ```
  0
  ```
6. "Maximum days in the future":
  
  
  ```
  7
  ```
7. "Email addresses":
  
  
  ```
  ${safeSubject.emailAddress}
  ```
8. "Email subject":
  
  
  ```
  Please re-acknowledge the Networking Acceptable Use Policy
  ```
9. "Email body":
  
  
  ```
  You will lose access to VPN networking on ${ruleElUtils.formatDate(membershipDisabledTimestamp, 'yyyy/MM/dd')}, unless you read and acknowledge the latest AUP. Please go to https://localhost:8443/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=${groupId} to access the form.
  ```
10. Click "Save".

### Create custom UI (skip this)

This shows how the custom UI is built in the UI. In class, import the configuration in the next section instead.

Miscellaneous > Custom UI > Add Custom UI

- Config id: network_aup_acknowledge
- Custom UI configuration: CustomUiConfiguration
- Group UUID or name: ref:certs:network_aup_acknowledged

Number of queries: 3

1)

- User query type: grouper
- Field names: members
- Group name: ref:certs:network_aup_acknowledged
- Label: Already in group
- Variable to assign: cu_isEnrolled
- Variable type: boolean

2)

- User query type: sql
- Bind variable 0: ${[group.id](http://group.id)}
- Bind variable 0 type: string
- Bind variable 1: ${[subject.id](http://subject.id)}
- Bind variable 1 type: string
- Config id: grouper
- Group name: ref:certs:network_aup_acknowledged
- Label: create time
- Query: select to_char(to_timestamp(membership_create_time/1e3), 'MM/DD/YYYY HH24:MI') AS membership_create_time from grouper_memberships_all_v v join grouper_members s on v.member_id = [s.id](http://s.id) where owner_group_id = ? and s.subject_id = ? and s.subject_source = 'eduLDAP' and mship_type = 'immediate' and immediate_mship_enabled = 'T'
- Variable to assign: cu_mshipCreateTime
- Variable type: string

3)

- User query type: sql
- Bind variable 0: ${[group.id](http://group.id)}
- Bind variable 0 type: string
- Bind variable 1: ${[subject.id](http://subject.id)}
- Bind variable 1 type: string
- Config id: grouper
- Group name: ref:certs:network_aup_acknowledged
- Label: expiration time
- Query: select to_char(to_timestamp(immediate_mship_disabled_time/1e3), 'MM/DD/YYYY HH24:MI') AS immediate_mship_disabled_time from grouper_memberships_all_v v join grouper_members s on v.member_id = [s.id](http://s.id) where owner_group_id = ? and s.subject_id = ? and s.subject_source = 'eduLDAP' and mship_type = 'immediate' and immediate_mship_enabled = 'T'
- Variable to assign: cu_mshipExpireTime
- Variable type: string

Number of text configs: 9

1) Logo

[https://localhost:8443/demo/customUi/aup/networking-logo.png](https://localhost:8443/demo/customUi/aup/networking-logo.png)

2) helpLink

```
<a href="https://localhost:8443/demo/customUi/aup/aup.html">Acceptable Use Policy</a>

```

3) header

```
<h1>Acknowledgement of acceptable use policy</h1>

```

4) instructions1

```
<p>In order to access the campus network, you need to agree to our <a href="https://localhost:8443/demo/customUi/aup/aup.html">acceptable use policy</a>. By clicking the Acknowledge button, you affirm you have read and agree to the network Acceptable Use Policy.</p>

```

5) enrollButtonShow

6) enrollmentLabel

```
Text: Click Acknowledge to affirm acceptance of the policy

```

7) enrollButtonText

```
Acknowledge

```

8) instructions1

Text:

```
<p>You have already acknowledged the AUP, on ${cu_isEnrolled ? cu_mshipCreateTime : 'Never'}. Your enrollment will expire on ${cu_isEnrolled ? cu_mshipExpireTime : 'Never'}</p>

```

Script:

```
${cu_isEnrolled}

```

9) gshScript

Text

```
def membership = MembershipFinder.findImmediateMembership(grouperSession, group, subject, true)
membership.disabledTime = new Timestamp((long)(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)))
membership.update()

```

Script:

```
${cu_grouperEnroll}

```

### Import custom UI config and review

1. In the left menu, click "Miscellaneous", then click "Configure".
2. Click "Config actions" → "Import config file".
3. "Import configuration with": select "Copy/paste configuration entries".
4. "Configuration file type": select "grouper.properties".
5. "Configuration file contents": paste this:
  
  
  ```
  grouperCustomUI.network_aup_acknowledge.cuQuery.0.fieldNames = members
  grouperCustomUI.network_aup_acknowledge.cuQuery.0.groupName = ref\u003Acerts\u003Anetwork_aup_acknowledged
  grouperCustomUI.network_aup_acknowledge.cuQuery.0.label = Already in group
  grouperCustomUI.network_aup_acknowledge.cuQuery.0.userQueryType = grouper
  grouperCustomUI.network_aup_acknowledge.cuQuery.0.variableToAssign = cu_isEnrolled
  grouperCustomUI.network_aup_acknowledge.cuQuery.0.variableType = boolean
  grouperCustomUI.network_aup_acknowledge.cuQuery.1.bindVar0 = \u0024{group.id}
  grouperCustomUI.network_aup_acknowledge.cuQuery.1.bindVar0Type = string
  grouperCustomUI.network_aup_acknowledge.cuQuery.1.bindVar1 = \u0024{subject.id}
  grouperCustomUI.network_aup_acknowledge.cuQuery.1.bindVar1Type = string
  grouperCustomUI.network_aup_acknowledge.cuQuery.1.configId = grouper
  grouperCustomUI.network_aup_acknowledge.cuQuery.1.groupName = ref\u003Acerts\u003Anetwork_aup_acknowledged
  grouperCustomUI.network_aup_acknowledge.cuQuery.1.label = create time
  grouperCustomUI.network_aup_acknowledge.cuQuery.1.query = select to_char(to_timestamp(membership_create_time/1e3), 'MM/DD/YYYY HH24\u003AMI') AS membership_create_time from grouper_memberships_all_v v join grouper_members s on v.member_id = s.id where owner_group_id = ? and s.subject_id = ? and s.subject_source = 'eduLDAP' and mship_type = 'immediate' and immediate_mship_enabled = 'T'
  grouperCustomUI.network_aup_acknowledge.cuQuery.1.userQueryType = sql
  grouperCustomUI.network_aup_acknowledge.cuQuery.1.variableToAssign = cu_mshipCreateTime
  grouperCustomUI.network_aup_acknowledge.cuQuery.1.variableType = string
  grouperCustomUI.network_aup_acknowledge.cuQuery.2.bindVar0 = \u0024{group.id}
  grouperCustomUI.network_aup_acknowledge.cuQuery.2.bindVar0Type = string
  grouperCustomUI.network_aup_acknowledge.cuQuery.2.bindVar1 = \u0024{subject.id}
  grouperCustomUI.network_aup_acknowledge.cuQuery.2.bindVar1Type = string
  grouperCustomUI.network_aup_acknowledge.cuQuery.2.configId = grouper
  grouperCustomUI.network_aup_acknowledge.cuQuery.2.groupName = ref\u003Acerts\u003Anetwork_aup_acknowledged
  grouperCustomUI.network_aup_acknowledge.cuQuery.2.label = expiration time
  grouperCustomUI.network_aup_acknowledge.cuQuery.2.query = select to_char(to_timestamp(immediate_mship_disabled_time/1e3), 'MM/DD/YYYY HH24\u003AMI') AS immediate_mship_disabled_time from grouper_memberships_all_v v join grouper_members s on v.member_id = s.id where owner_group_id = ? and s.subject_id = ? and s.subject_source = 'eduLDAP' and mship_type = 'immediate' and immediate_mship_enabled = 'T'
  grouperCustomUI.network_aup_acknowledge.cuQuery.2.userQueryType = sql
  grouperCustomUI.network_aup_acknowledge.cuQuery.2.variableToAssign = cu_mshipExpireTime
  grouperCustomUI.network_aup_acknowledge.cuQuery.2.variableType = string
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.0.index = 10
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.0.text = https\u003A//localhost\u003A8443/demo/customUi/aup/networking-logo.png
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.0.textType = logo
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.1.index = 10
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.1.text = <a href="https\u003A//localhost\u003A8443/demo/customUi/aup/aup.html">Acceptable Use Policy</a>
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.1.textType = helpLink
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.2.index = 10
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.2.text = <h1>Acknowledgement of acceptable use policy</h1>
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.2.textType = header
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.3.index = 10
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.3.text = <p>In order to access the campus network, you need to agree to our <a href="https\u003A//localhost\u003A8443/demo/customUi/aup/aup.html">acceptable use policy</a>. By clicking the Acknowledge button, you affirm you have read and agree to the network Acceptable Use Policy.</p>
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.3.textType = instructions1
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.4.index = 10
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.4.textBoolean = true
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.4.textType = enrollButtonShow
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.5.index = 10
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.5.text = Click Acknowledge to affirm acceptance of the policy
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.5.textType = enrollmentLabel
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.6.index = 10
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.6.text = Acknowledge
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.6.textType = enrollButtonText
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.7.index = 20
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.7.script = \u0024{cu_isEnrolled}
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.7.text = <p>You have already acknowledged the AUP, on \u0024{cu_isEnrolled ? cu_mshipCreateTime \u003A 'Never'}. Your enrollment will expire on \u0024{cu_isEnrolled ? cu_mshipExpireTime \u003A 'Never'}</p>
  grouperCustomUI.network_aup_acknowledge.cuTextConfig.7.textType = instructions1
  grouperCustomUI.network_aup_acknowledge.groupUUIDOrName = ref\u003Acerts\u003Anetwork_aup_acknowledged
  grouperCustomUI.network_aup_acknowledge.numberOfQueries = 3
  grouperCustomUI.network_aup_acknowledge.numberOfTextConfigs = 8
  
  ```
6. Click "Submit".
7. In the left menu, click "Miscellaneous", then click "Custom UI (admin)" and review the custom UI "network_aup_acknowledge".

### Acknowledge the AUP as another user

Log in as dgilmore, accept the AUP in the custom UI, then check that the membership ends a year from today.

1. Open a private browser window and go to:
  
  
  ```
  https://localhost:8443/grouper
  ```
2. Log in with username:
  
  
  ```
  dgilmore
  ```
  
  and password:
  
  
  ```
  password
  ```
3. In the left menu, click "Browse folders", click "Root", click the folder "ref", click the folder "certs", then click the group "Network Acceptable Use Policy (AUP) acknowledged". dgilmore can see the group because everyone has OPTIN.
4. Click "Group actions" → "Custom UI".
5. Click "Acknowledge". The page now says you acknowledged the AUP today, and that your enrollment expires a year from today.
6. Close the private browser window.
7. In your main browser window (logged in as the admin), go to the group "ref:certs:network_aup_acknowledged".
8. On the "Members" tab, click "Advanced".
9. "Enabled / disabled": select "Enabled / disabled status".
10. Click "Apply filter".
11. Find dgilmore in the list. The "Disabled date" column shows a date one year from today. This is when the membership expires unless dgilmore acknowledges the AUP again.

### Simulate expiration in 2 days

This adds jsmith to the group with a membership that expires in 2 days, then runs the rules daemon so the reminder email is sent.

1. In the terminal on your VM, start GSH:
  
  
  ```
  gte-gsh
  ```
2. Paste this script:
  
  
  ```groovy
  import edu.internet2.middleware.grouper.app.loader.GrouperLoader
  import java.sql.Timestamp
  
  def group = GroupFinder.findByName("ref:certs:network_aup_acknowledged", true)
  def subject = SubjectFinder.findByIdentifier("jsmith", true)
  group.addMember(subject, false)
  def membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), group, subject, true)
  membership.disabledTime = new Timestamp((long)(System.currentTimeMillis() + (2L * 24 * 60 * 60 * 1000)))
  membership.update()
  
  GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_rules")
  
  ```
3. When you see “loader ran successfully: Ran rules daemon”, quit GSH:
  
  
  ```
  :quit
  ```

### Check email

1. Open the mail server in your browser and look for the “Please re-acknowledge the Networking Acceptable Use Policy” email:
  
  
  ```
  https://localhost:8443/mail/
  ```
