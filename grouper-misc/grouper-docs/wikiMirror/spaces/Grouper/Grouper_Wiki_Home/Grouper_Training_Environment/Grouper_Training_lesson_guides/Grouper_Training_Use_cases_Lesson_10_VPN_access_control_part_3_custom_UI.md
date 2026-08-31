---
title: "Grouper Training - Use cases - Lesson 10: VPN access control part 3 - custom UI"
space: Grouper
pageId: 28545541
version: 16
lastUpdated: 2026-08-13T18:39:02.297Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545541/Grouper+Training+-+Use+cases+-+Lesson+10+VPN+access+control+part+3+-+custom+UI
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

## Links

[Grouper Custom UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549064/Grouper+Custom+UI)

[Grouper Custom UI example consent to T&C](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554873/Grouper+Custom+UI+example+consent+to+T+C)

## Hands On

### Create reference group for acknowledge users

Create folder under ref:

- Folder name: Certifications
- Folder ID: certs
- Description: Groups of users with specific acknowledgements, certifications, training completions, etc.

Create group under ref:certs

- Group name: Network Acceptable Use Policy (AUP) acknowledged
- Group ID: network_aup_acknowledged
- Description: Users who have attested they have read and accepted the AUP document at http://example.edu/aup
- Object types: ref

Grant OPTIN to EveryEntity

### Create rule

- Add disabled date on membership
- Expire after X days: 365

### Create rule on group

Send email due to disabled date

- Minimum: 0
- Maximum: 7
- Email addresses: ${safeSubject.emailAddress}
- Subject: Please re-acknowledge the Networking Acceptable Use Policy
- body
  
  
  ```
  You will lose access to VPN networking on ${ruleElUtils.formatDate(membershipDisabledTimestamp, 'yyyy/MM/dd')}, unless you read and acknowledge the latest AUP. Please go to https://localhost:8443/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=${groupId} to access the form.
  
  ```

### Create custom UI (SKIP THIS)

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

### Import Custom UI config and review

Full script (if you did not do the configuration above). After creating group, you can import this into grouper.properties

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

### Simulate expiration in 2 days

In terminal, in ec-2, type

```
[student@ip-172-31-21-17 ~]$ gte-gsh
```

Paste this script below:

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

Quit GSH

```
===> loader ran successfully: Ran rules daemon, changed 0 records, there were 0 errors.
groovy:000> :quit

```

### Check email

[https://localhost:8443/mail/](https://localhost:8443/mail/)
