---
title: "Grouper daily email notification - affiliation expiration report"
space: Grouper
pageId: 28555651
version: 2
lastUpdated: 2026-07-01T05:37:42.779Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555651/Grouper+daily+email+notification+-+affiliation+expiration+report
---

When active affiliations expire, employees of the student system team in the IT dept lose access to collaboration tools and systems. Normally their BA will extend their job on time, but if it doesn't happen, access is cut, and it takes a while to resume. This notification in actuality is sent to end users, but in this example we show what can be sent to a BA

Grouper was configured to tell the BA that members of the student team has an affiliation that is about to expire, and give them a couple days notice, and let people opt out of the notifications if they like.

Make a query from the IDM, of whose affiliation is about to expire minus a few days. Turn this into a view. Note, one of the columns is subject_id, and one of the columns is 3 days before expiration to give extra time.

Setup a group of eligible people (people on the student team). Have a composite so people can opt out

Setup a group of people who have received the emails.

Look at membership attribute values to get last sent date

Email

Subject template

```
NGSS affiliation expiration report, ${size(listOfRecordMaps)} issues
```

Email template in text editor

```
Hello ${subject_name},

There are ${size(listOfRecordMaps)} people on the NGSS team whose affiliation is about to expire.  Their access will end then.  Please address this issue soon if they should have access after the date.

$$ for (var recordMap : listOfRecordMaps) {
Affil expires: ${recordMap.get('column_needed_by_date')} for ${recordMap.get('subject_description')}
$$}

If you would like to stop receiving these emails please go here and click More actions - > Join group:
https://...uper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=f3e16cbe0e6342a993312e6f0ea186f6

Thanks,
Chris Hyzer
ps. Slack or teams or skype me with issues.
```

Email template for config (replace \n with __NEWLINE__)

```
Hello ${subject_name},__NEWLINE____NEWLINE__There are ${size(listOfRecordMaps)} people on the NGSS team whose affiliation is about to expire.  Their access will end then.  Please address this issue soon if they should have access after the date.__NEWLINE____NEWLINE__$$ for (var recordMap : listOfRecordMaps) {__NEWLINE__Affil expires: ${recordMap.get('column_needed_by_date')} for ${recordMap.get('subject_description')}__NEWLINE__$$}__NEWLINE____NEWLINE__If you would like to stop receiving these emails please go here and click More actions - > Join group:__NEWLINE__https://...uper/grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=f3e16cbe0e6342a993312e6f0ea186f6__NEWLINE____NEWLINE__Thanks,__NEWLINE__Chris Hyzer__NEWLINE__ps. Slack or teams or skype me with issues.
```

Setup the notification job in grouper
