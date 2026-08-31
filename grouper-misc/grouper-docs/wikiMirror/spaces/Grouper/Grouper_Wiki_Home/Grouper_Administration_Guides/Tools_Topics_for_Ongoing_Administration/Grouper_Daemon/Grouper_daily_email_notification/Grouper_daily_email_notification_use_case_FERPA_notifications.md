---
title: "Grouper daily email notification use case - FERPA notifications"
space: Grouper
pageId: 28555646
version: 3
lastUpdated: 2026-07-01T05:37:43.795Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555646/Grouper+daily+email+notification+use+case+-+FERPA+notifications
---

When yearly FERPA training expires, employees of the student system team in the IT dept lose access to collaboration tools and systems. The LMS tells them then they need the training only if someone enrolled them in the curriculum, and the date it tells them is one year from the last time they took it, but they need it a couple days prior so data can flow through to all the systems. People send those emails to junk too, there is no way to suppress them.

Grouper was configured to tell members of the student team their FERPA will expire, and give them a couple days notice, and let them opt out of the notifications if they like.

Make a query from the LMS (via the data warehouse), of whose training will expire, and the date minus a few days. Turn this into a view. Note, one of the columns is subject_id

Setup a group of eligible people (people on the student team). Have a composite so people can opt out

Setup a group of people who have received the emails.

Look at membership attribute values to get last sent date

Configure the templates

Subject template

| `Important! FERPA training expires ${column_needed_by_date}` |
| --- |

Email template in text editor

| `Dear ${subject_name},`       `Your FERPA training is about to expire and then access to NGSS resources will be revoked. Please take the FERPA training again now or at least before ${column_needed_by_date}.`       `https:``//.../...Ferpa`       `If you would like to stop receiving these emails please go here and click More actions - > Join group:`   `https:``//gro...er/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=f3e16cbe0e6342a993312e6f0ea186f6`       `Thanks,`   `Chris Hyzer`   `ps. Slack or teams or skype me with issues.` |
| --- |

Email template for config (replace \n with __NEWLINE__)

| `Dear ${subject_name}, __NEWLINE____NEWLINE__Your FERPA training is about to expire and then access to NGSS resources will be revoked. Please take the FERPA training again now or at least before ${column_needed_by_date}.__NEWLINE____NEWLINE__https:``//.../...Ferpa__NEWLINE____NEWLINE__If you would like to stop receiving these emails please go here and click More actions - > Join group:__NEWLINE__[https://gro...er/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=f3e16cbe0e6342a993312e6f0ea186f6__NEWLINE____NEWLINE__Thanks,__NEWLINE__Chris](https://gro...er/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=f3e16cbe0e6342a993312e6f0ea186f6__NEWLINE____NEWLINE__Thanks,__NEWLINE__Chris) Hyzer__NEWLINE__ps. Slack or teams or skype me with issues.` |
| --- |

Setup the notification job in grouper
