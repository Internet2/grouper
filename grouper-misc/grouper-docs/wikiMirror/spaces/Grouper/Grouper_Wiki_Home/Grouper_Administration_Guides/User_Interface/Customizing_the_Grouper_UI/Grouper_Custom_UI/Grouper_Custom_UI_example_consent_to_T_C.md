---
title: "Grouper Custom UI example consent to T&C"
space: Grouper
pageId: 28554873
version: 7
lastUpdated: 2026-07-01T05:39:29.670Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554873/Grouper+Custom+UI+example+consent+to+T+C
---

## Requirements

The Covid scheduling SaaS application (Accommodate) needs a Terms and Conditions acceptance from users before scheduling Covid tests. The data should be captured and fed to warehouse for reporting. Note, its possible to use a deep link to get around the terms, but thats ok. The T&C page originally was only going to show once per user, but then that was changed to show each time the user goes to the application. Any netid holder should be able to use the application

## Screenshot

## Data reporting

View was created in postgres grouper with users and dates they agreed with terms:

| `create or replace`   `view penn_covid_accept_terms_v as`   `select`   ```subject_id as penn_id,`   ```to_timestamp(value_integer /``1000``) as terms_accepted_timestamp`   `from`   ```grouper_aval_asn_mship_v gaamv`   `where`   ```gaamv.group_id =``'3f70d4f1214f40f899cca9d707ddb9df'`   ```and source_id =``'pennperson'`   ```and gaamv.attribute_def_name_name =``'penn:isc:ait:apps:covidScheduling:service:attributes:covidAcceptTerms'`   ```and subject_id is not``null`   ```and value_integer is not``null` |
| --- |

Data looks like

Create a table in oracle warehouse and index by penn_id

| `CREATE TABLE AUTHZADM.penn_covid_accept_terms`   `(`   ```penn_id VARCHAR2(``20` `CHAR) NOT NULL,`   ```terms_accepted_timestamp TIMESTAMP(``6``) NOT NULL`   `);`       `ALTER TABLE penn_covid_accept_terms ADD (`   ```CONSTRAINT penn_covid_accept_terms_PK`   ```PRIMARY KEY`   ```(penn_id, terms_accepted_timestamp)`   ```ENABLE VALIDATE);`           `CREATE INDEX AUTHZADM.PENN_COVID_AC_TRM_pkey_idx ON AUTHZADM.PENN_COVID_ACCEPT_TERMS`   `(PENN_ID);`       `--NOTE: grant to user that SELECTs``for` `snowflake` |
| --- |

Sync this data over a few times a day

grouper.client.properties

| `grouperClient.syncTable.covidTerms.databaseFrom=grouper`   `grouperClient.syncTable.covidTerms.databaseTo=warehouse`   `grouperClient.syncTable.covidTerms.tableFrom=penn_covid_accept_terms_v`   `grouperClient.syncTable.covidTerms.tableTo=penn_covid_accept_terms`   `grouperClient.syncTable.covidTerms.columns=penn_id, terms_accepted_timestamp`   `grouperClient.syncTable.covidTerms.primaryKeyColumns=penn_id, terms_accepted_timestamp` |
| --- |

grouper-loader.properties

| `otherJob.covidTerms.quartzCron=``51` `56` `10``,``14``,``23` `* * ?`   `otherJob.covidTerms.``class``=edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob`   `otherJob.covidTerms.syncType=fullSyncFull`   `otherJob.covidTerms.grouperClientTableSyncConfigKey=covidTerms` |
| --- |

See it work

## Grouper design

1. Grouper "custom ui" that anyone can use
2. Show the terms, same for all users
3. On click, run a GSH script that adds an attribute date to the membership of the user in the group
4. Those attribute values can be retrieved via view → table sync to warehouse

## See T&C acceptances in Grouper

Members of this group have accepted the terms

[https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=3f70d4f1214f40f899cca9d707ddb9df](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=3f70d4f1214f40f899cca9d707ddb9df)

See attributes on the membership to get the specifics

## Setup attributes

We need an attribute on an immediate membership that has a timestamp value type, and supports multi-valued assignments. We only need one attribute name

## Allow everyone to use the screen

Note, the "opt in" just means they can hit the button. Being a member of the group doesnt mean anything if someone uses the UI to literally opt in to the group

## Grouper "custom ui" configuration externalized text

This is configured in Grouper database configs in the UI: [grouper.text.en.us](http://grouper.text.en.us).properties

| `penn_covid_header = <h1>``2020` `Fall Semester: Who Must Be Tested</h1><ul><li>Students, faculty, postdoctoral trainees, blah blah blah``Houses.</li><li>All residents blah blah blah sing (CHAS)</li></ul>`           `penn_covid_enrollmentLabel = <h1>Consent to Testing</h1>The University of Pennsylvania is asking members of blah blah blah``related to COVID-``19``;</li><li>To identify patterns of infection blah blah blah``with respect to my test results.</li></ul>`       `penn_covid_enrollmentButtonText = I Agree` |
| --- |

## Grouper "custom ui" JSON configuration for text

There is externalized text and JSON attributes on the group used for custom ui

| `# no help link`   `{``"endIfMatches"``:``true``,``"customUiTextType"``:``"helpLink"``,``"index"``:``0``,``"text"``:``" "``}`       `#``this` `is the logo we put in the container`   `{``"endIfMatches"``:``true``,``"customUiTextType"``:``"logo"``,``"index"``:``0``,``"text"``:``"../../grouperExternal/public/penn/images/pennCares.png"``}`       `# header text (who must get tested)`   `{``"endIfMatches"``:``true``,``"customUiTextType"``:``"header"``,``"index"``:``10``,``"defaultText"``:``true``,``"text"``:``"${textContainer.text['penn_covid_header']}"``}`       `# body text (terms and conditions)`   `{``"endIfMatches"``:``true``,``"customUiTextType"``:``"enrollmentLabel"``,``"index"``:``0``,``"defaultText"``:``true``,``"text"``:``"${textContainer.text['penn_covid_enrollmentLabel']}"``}`       `# button text`   `{``"endIfMatches"``:``true``,``"customUiTextType"``:``"enrollButtonText"``,``"index"``:``0``,``"defaultText"``:``true``,``"text"``:``"${textContainer.text['penn_covid_enrollmentButtonText']}"``}`       `# show the button irrespective of membership in group (since``if` `in group they still need to press button). The unenroll button isnt really used.`   `{``"endIfMatches"``:``true``,``"customUiTextType"``:``"unenrollButtonShow"``,``"index"``:``0``,``"defaultText"``:``true``,``"text"``:``"false"``}`   `{``"endIfMatches"``:``true``,``"customUiTextType"``:``"enrollButtonShow"``,``"index"``:``0``,``"defaultText"``:``true``,``"text"``:``"true"``}` |
| --- |

## Add logo into container

Start with the logo, put in the Grouper container in: slashRoot\opt\grouper\grouperWebapp\grouperExternal\public\penn\images\pennCares.png

## Grouper "custom ui" configuration for redirect

This JSON tells the UI to redirect to the app after the user agrees to terms

| `{``"endIfMatches"``:``true``,``"customUiTextType"``:``"redirectToUrl"``,``"index"``:``0``,``"text"``:``"[https://upen...ty.com/](https://upenn-covid19-scheduling-accommodate.symplicity.com/)"``,``"script"``:``"${ cu_joinGroupButtonPressed }"``}` |
| --- |

## Add the "agree" date to the attribute value when button pressed

This GSH script will make sure the user is in the group, and a new attribute value is assigned to that membership

| `{``"endIfMatches"``:``true``,``"customUiTextType"``:``"gshScript"``,``"index"``:``0``,``"text"``:``"${textContainer.text['penn_covid_gsh']}"``,``"script"``:``"${ cu_joinGroupButtonPressed }"``}`           `Externalized text:`           `penn_covid_gsh = group.addMember(subject,``false``);$newline$Membership membership = MembershipFinder.findImmediateMembership(grouperSession, group, subject,``true``);$newline$membership.getAttributeValueDelegate().addValueTimestamp(``"penn:isc:ait:apps:covidScheduling:service:attributes:covidAcceptTerms"``,``new` `java.sql.Timestamp(System.currentTimeMillis()));`           `Note, you put $newline$ in between lines so it is in one line in externalized text, so the GSH is actually:`           `group.addMember(subject,``false``);`   `Membership membership = MembershipFinder.findImmediateMembership(grouperSession, group, subject,``true``);`   `membership.getAttributeValueDelegate().addValueTimestamp(``"penn:isc:ait:apps:covidScheduling:service:attributes:covidAcceptTerms"``,``new` `java.sql.Timestamp(System.currentTimeMillis()));` |
| --- |
