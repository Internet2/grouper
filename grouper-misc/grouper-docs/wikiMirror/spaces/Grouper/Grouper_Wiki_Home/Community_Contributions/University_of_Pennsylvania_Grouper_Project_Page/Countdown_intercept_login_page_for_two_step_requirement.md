---
title: "Countdown intercept login page for two-step requirement"
space: Grouper
pageId: 28544159
version: 1
lastUpdated: 2018-10-15T18:48:19.692Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544159/Countdown+intercept+login+page+for+two-step+requirement
---

- Penn has enrolled staff in two-step
- We want to enroll faculty and students in the near future
- Schools will have various requirement dates for certain departments before the main deadline
- Whatever they are comfortable with, time-wise, when someone is going to be required, they should see a warning in Shibboleth once a day (or session) when they login.

## Example countdown

## High level design

- We will have 9 days in advance of warning
- People who are enrolled should not see the warning
- Grouper will use a custom attribute which has a value of the date the group will be required to enroll
- Grouper will use that attribute value in a loader job to load people into the countdown groups if they are not enrolled
- When they are overdue, they will be added to a group which is required

## Process

- School opens a ticket with IT to require a group of faculty to be required on a certain date
- Two step admin creates a group in the two-step folder, and assigned an attribute with that date (e.g. 2018/10/23)
- If there are reference groups to add, or a loader job to configure, then make the members
- When that date is 9 days away, people not enrolled in two step start getting emails

## Grouper design

Create the attribute definition and attribute name. Note this is 2.3.

Attribute definition

Attribute assignment

Grouper design PDF

## Shibboleth design

Shibboleth design PDF
