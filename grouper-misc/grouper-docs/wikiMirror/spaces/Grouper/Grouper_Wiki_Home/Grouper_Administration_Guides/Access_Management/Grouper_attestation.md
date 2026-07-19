---
title: "Grouper attestation"
space: Grouper
pageId: 28545015
version: 64
lastUpdated: 2026-07-01T05:47:50.447Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545015/Grouper+attestation
---

Grouper attestation means marking a group or folder so that those with READ & UPDATE privileges must review the membership list periodically. This is useful in ad hoc groups where deprovisioning is not automatic. Subjects with READ & UPDATE privileges will be reminded by email to review the memberships. After reviewing the memberships, the group owner will click a button on the group indicating that it has been reviewed.

**Child pages:**

**Outline:**

## Privileges

If you are a Grouper admin or if you have UPDATE or ADMIN privileges on a group or stem, you can edit attestation on that entity.

If you can edit attestation or if you have READ on a group or stem, you can READ the attestation on that entity.

NOTE: you don't need privileges on the attributes that configure the attestation.

To run the attestation daemon you need to be a Grouper admin.

## Configuration

Set this in grouper.properties

```
#put the URL which will be used e.g. in emails to users. include the webappname at the end, and nothing after that.
#e.g. https://server.school.edu/grouper/
grouper.ui.url = http://localhost:8088/grouper/

#smtp server is a domain name or dns name. set to "testing" if you want to log instead of send (e.g. for testing)
mail.smtp.server = localhost

#this is the default email address where mail from grouper will come from
mail.smtp.from.address = noreply@example.com

# OPTIONAL FOR ATTESTATION, WILL BE BLANK IN PROD
#this is the subject prefix of emails, which will help differentiate prod vs test vs dev etc
mail.smtp.subject.prefix = DEV:

```

Note, might want to leave these as defaults. grouper.properties

```
#########################################
## Attestation
#########################################

#default value of attestation days until recertify. Every group/folder can define their own days until recertify value and if they don't provide, use the following one.
attestation.default.daysUntilRecertify = 180

#number of groups shown in the body of attestation email
attestation.email.group.count = 100

#attestation reminder email subject
attestation.reminder.email.subject = You have $objectCount$ groups that require attestation

#attestation reminder email body (links and groups are added dynamically)
attestation.reminder.email.body = You need to attest the memberships of the following groups. Review the memberships of each group and click: More actions -> Attestation -> Members of this group have been reviewed
attestation.reminder.email.body.greaterThan100 = There are $remaining$ more groups to be attested.

```

Configure in grouper-loader.properties (these are the defaults)

```
#####################################
## Atttestation Job
#####################################
otherJob.attestationDaemon.class = edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob
otherJob.attestationDaemon.quartzCron = 0 0 1 * * ?

```

## Attestation Attributes

Attestation is implemented using attributes. After configuring and enabling attestation, `attestationDef` and `attestationValueDef` attribute definitions will be added to the system as shown in the screenshots.

### attestationDef

### attestationValueDef

## Attestation Daemon

The attestation daemon runs daily (by default). This daemon sends reminder emails to people configured in attestationEmailAddresses attribute; if there is no email address in that attribute, it picks up the emails from subject source email property of admins (subjects with READ and UPDATE privileges) for the group. If no emails are found, the job logs an error and moves on to the next entity. Note that the job doesn't send multiple emails to the same person on the same day even if you configure the cron to run the job multiple times on the same day.

The `attestationDaysBeforeToRemind` attribute controls how many days before the current attestation expires that reminder emails are sent.

Run daemon from UI: (you would only do this occasionally or for testing). There is a menu item for Grouper admins to be able to kick off the daemon:

Run daemon from GSH:

```
loaderRunOneJob("OTHER_JOB_attestationDaemon");

```

## Attest a group as reviewed

When a group needs it memberships reviewed (either initially, or when the attestation period has elapsed), you can attest the group on the membership screen, or on the group's attestation screen

On the membership screen you will see a note and a button:

If you are on the attestation screen, you will see a menu item Attestation actions → Attest group as reviewed

## Clear last reviewed date

If you want to mark a group to be reviewed again, you can clear its last reviewed date. While on the attestation screen for a group, click "Attestation actions → Clear last reviewed date".

## View folder attestation

If a group inherits its attestation settings from an ancestor folder, there is a link from the group attestation menu: Attestation actions → View folder attestation.

## View all attestable groups

If you are in the folder or group "Attestation actions" menu, you can "View all attestable groups". This will go to the global [view all attestable groups screen](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547727/Grouper+attestation+global+groups+that+need+attestation), that need attestation

## Emails

The attestation daemon runs daily (via cron) and looks for groups which have not been attested, grouped by email address. The daemon sends each user an email with a list of groups (or folders) that require attestation.

If there are more than 100 (configured in grouper.properties) attestations for a given user, the email will only contain links to 100 groups/folders, but will also say there are XXXX others.

Example:

school:groupA needs attestation: emails to: [jsmith@example.com](mailto:jsmith@example.com), and [bgreen@example.com](mailto:bgreen@example.com)

school:groupB needs attestation: emails to: [jsmith@example.com](mailto:jsmith@example.com), and [kwilson@example.com](mailto:kwilson@example.com)

send 3 emails:

FIRST EMAIL: To [jsmith@example.com](mailto:jsmith@example.com)

**Subject**: you have 2 grouper groups that require attestation.

**Body**:

You need to attest the memberships of the following groups:

1. 1. school:groupA (cc’d [bgreen@example.com](mailto:bgreen@example.com))

<link to membership>

1. 2. school:groupB (cc’d [kwilson@example.com](mailto:kwilson@example.com))

<link to membership>

SECOND EMAIL: To [bgreen@example.com](mailto:bgreen@example.com)

**Subject**: you have 1 grouper groups that require attestation.

**Body**:

You need to attest the memberships of the following groups:

1. 1. school:groupA (cc’d [jsmith@example.com](mailto:jsmith@example.com))

<link to membership>

THIRD EMAIL: To [kwilson@example.com](mailto:kwilson@example.com)

**Subject**: you have 1 grouper groups that require attestation.

**Body**:

You need to attest the memberships of the following groups:

1. 1. school:groupB (cc’d [jsmith@example.com](mailto:jsmith@example.com))

<link to membership>

## Future scope

- - If attestation is not done in a certain amount of time, disable the memberships or group somehow

## See Also

[Presentation from Duke on Paranoid IAM, page 20](https://meetings.internet2.edu/media/medialibrary/2019/12/05/20191211-mckee-paranoidiam_1.pdf)
