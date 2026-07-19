---
title: "Nightly email reminder when training will run out"
space: Grouper
pageId: 28544549
version: 7
lastUpdated: 2026-07-01T05:48:26.087Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544549/Nightly+email+reminder+when+training+will+run+out
---

We would like an email to go out to people when training will run out. When there is 2 weeks remaining, people should get a nightly email reminder.

The LMS should do this, but if people on the project team are not in the curriculum in the LMS, they fall through the cracks. But Grouper know when people will expire.

This does not require Java, or restarting any services. This will be a nightly GSH daemon.

## Data of when people expire

First we need a query for the right population, and get the date training will query, but that in a view.

The LMS data is in another DB as the Grouper data, so we need two views and a DB link

```
create view AUTHZ_KL_FERPA_EXPIRE_V as
     SELECT s.penn_id,
            s.completion_date + 362 as needed_by_date -- few days less than a year so they have some notice
       FROM dwlms.LM_CURRENT_TRAINING_STATUS s, dwlms.LM_USER u
      WHERE     s.penn_id = u.penn_id
            AND s.completed = 'Y'
            and s.item_id = 'UP.87024.ITEM.FERPA001'
            and s.completion_date is not null 
            and s.completion_date < SYSDATE - (362-14) -- two weeks before you need notice
            ;

```

## Population of people to get the email

Population of team

penn:isc:ait:apps:ngss:team:ngssTeamAllBeforeCheckingStatus

Note, lets allow them to opt out of the notifications. Here is an opt in group. Note, allow optin/optout for ngssTeamAllBeforeCheckingStatus

penn:isc:ait:apps:ngss:team:ngssTeamNotificationsIgnoreForStatusWarnings

Make a composite which is the policy group that identifies people eligible for status warning notifications

penn:isc:ait:apps:ngss:team:ngssTeamNotificationsEligibleForStatusWarnings

## Lets not email more than once a day

Make this group: penn:isc:ait:apps:ngss:team:ngssTeamNotificationsLastSent

People here have a membership attribute with string that is yyyy/mm/dd of last time sent

Make this attribute: penn:isc:ait:apps:ngss:attributes:ngssTeamNotificationsLastSent (on memberships, string)

## Test view for myself

```
CREATE OR REPLACE FORCE VIEW AUTHZ_NGSS_FERPA_NEEDED_V
(
   PENN_ID,
   NEEDED_BY_DATE
)
   BEQUEATH DEFINER
AS
   SELECT '10021368' AS penn_id, '2020/01/23' AS needed_by_date FROM DUAL;
```

## Externalize some text

Externalized text: ngss.notification.ferpa.body

```
Dear ${yourName},$newline$Your yearly FERPA training will expire on ${theDate} at which point you will lose access to NGSS software that you need to work.  Please take the training asap.$newline$$newline$https://mylms/ngssFerpa$newline$$newline$Thanks!$newline$Chris Hyzer$newline$slack/teams/skype me if you have issues.
```

## GSH script

```
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    String date = new java.text.SimpleDateFormat("yyyy/MM/dd").format(new Date());
    List pennIdsSentToday = new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().sql("select subject_id from grouper_aval_asn_efmship_v gaaev where group_name = 'penn:isc:ait:apps:ngss:team:ngssTeamNotificationsLastSent' and attribute_def_name_name = 'penn:isc:ait:apps:ngss:attributes:ngssTeamNotificationsLastSent' and value_string = ?").addBindVar(date).selectList(String.class);
        
    List results = new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().connectionName("pennCommunity").sql("select penn_id, needed_by_date from authz_ngss_ferpa_needed_v").selectList(Object[].class);

    RuntimeException re = null;
    for (Object[] result : (List<Object[]>)results) {

      edu.internet2.middleware.grouper.app.loader.OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addTotalCount(1);
      
      try {

        String pennid = (String)result[0];
        if (pennIdsSentToday.contains(pennid)) {
          continue;
        }
        String neededByDate = (String)result[1];
        Subject subject = SubjectFinder.findByIdAndSource(pennid, "pennperson", true);
        if (subject == null) {
          continue;
        }
        String email = subject.getAttributeValue("email");
        if (GrouperUtil.isBlank(email)) {
          continue;
        }
        
        edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer.assignThreadLocalVariable("yourName", subject.getName());
        edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer.assignThreadLocalVariable("theDate", neededByDate);
        
        new GrouperEmail().setTo(email).setSubject(edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer.textOrNull("ngss.notification.ferpa.subject")).setBody(edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer.textOrNull("ngss.notification.ferpa.body")).send();

        edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer.resetThreadLocalVariableMap();

        Group group = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:ngss:team:ngssTeamNotificationsLastSent", true);
        group.addMember(subject, false);
        Member member = MemberFinder.findBySubject(grouperSession, subject, true);
        group.getAttributeValueDelegateEffMship(member).assignValue("penn:isc:ait:apps:ngss:attributes:ngssTeamNotificationsLastSent", date);

        edu.internet2.middleware.grouper.app.loader.OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addUpdateCount(1);

      } catch (RuntimeException e) {

        e.printStackTrace();
        re = e;

        edu.internet2.middleware.grouper.app.loader.OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().incrementUnresolvableSubjectCount();
      }
    }
    if (re != null) {
      throw re;
    }
```

We will search and replace the newlines to be $newline$

```
otherJob.ngssNotificationFerpaExpire.class = edu.internet2.middleware.grouper.app.loader.OtherJobScript
otherJob.ngssNotificationFerpaExpire.quartzCron = 0 0 6 * * ?
otherJob.ngssNotificationFerpaExpire.scriptType = gsh
otherJob.ngssNotificationFerpaExpire.scriptSource = GrouperSession grouperSession = GrouperSession.startRootSession();$newline$    String date = new java.text.SimpleDateFormat("yyyy/MM/dd").format(new Date());$newline$    List pennIdsSentToday = new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().sql("select subject_id from grouper_aval_asn_efmship_v gaaev where group_name = 'penn:isc:ait:apps:ngss:team:ngssTeamNotificationsLastSent' and attribute_def_name_name = 'penn:isc:ait:apps:ngss:attributes:ngssTeamNotificationsLastSent' and value_string = ?").addBindVar(date).selectList(String.class);$newline$    List results = new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().connectionName("pennCommunity").sql("select penn_id, needed_by_date from authz_ngss_ferpa_needed_v").selectList(Object[].class);$newline$    RuntimeException re = null;$newline$    for (Object[] result : (List<Object[]>)results) {$newline$      edu.internet2.middleware.grouper.app.loader.OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addTotalCount(1);$newline$      try {$newline$        String pennid = (String)result[0];$newline$        if (pennIdsSentToday.contains(pennid)) {$newline$          continue;$newline$        }$newline$        String neededByDate = (String)result[1];$newline$        Subject subject = SubjectFinder.findByIdAndSource(pennid, "pennperson", true);$newline$        if (subject == null) {$newline$          continue;$newline$        }$newline$        String email = subject.getAttributeValue("email");$newline$        if (GrouperUtil.isBlank(email)) {$newline$          continue;$newline$        }        $newline$        edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer.assignThreadLocalVariable("yourName", subject.getName());$newline$        edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer.assignThreadLocalVariable("theDate", neededByDate);        $newline$        new GrouperEmail().setTo(email).setFrom("noreply@example.com").setSubject(edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer.textOrNull("ngss.notification.ferpa.subject")).setBody(edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer.textOrNull("ngss.notification.ferpa.body")).send();$newline$        edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer.resetThreadLocalVariableMap();$newline$        Group group = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:ngss:team:ngssTeamNotificationsLastSent", true);$newline$        group.addMember(subject, false);$newline$        Member member = MemberFinder.findBySubject(grouperSession, subject, true);$newline$        group.getAttributeValueDelegateEffMship(member).assignValue("penn:isc:ait:apps:ngss:attributes:ngssTeamNotificationsLastSent", date);$newline$        edu.internet2.middleware.grouper.app.loader.OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addUpdateCount(1);$newline$      } catch (RuntimeException e) {$newline$        e.printStackTrace();$newline$        re = e;$newline$        edu.internet2.middleware.grouper.app.loader.OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().incrementUnresolvableSubjectCount();$newline$      }$newline$    }$newline$    if (re != null) {$newline$  throw re; $newline$   }
```
