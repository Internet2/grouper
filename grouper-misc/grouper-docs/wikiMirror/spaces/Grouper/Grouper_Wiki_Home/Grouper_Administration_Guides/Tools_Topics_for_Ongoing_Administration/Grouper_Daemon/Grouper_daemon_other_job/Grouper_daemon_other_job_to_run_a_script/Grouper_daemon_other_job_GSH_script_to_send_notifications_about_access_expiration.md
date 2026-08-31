---
title: "Grouper daemon \"other job\" GSH script to send notifications about access expiration"
space: Grouper
pageId: 28560427
version: 6
lastUpdated: 2026-07-01T05:35:35.670Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560427/Grouper+daemon+other+job+GSH+script+to+send+notifications+about+access+expiration
---

Note: this is an example for postgres, convert to oracle or mysql if that is what you are using.

## Problem

When a user changes affiliation at the university (e.g., a faculty member retires), their Microsoft 365 license category changes. Rather than immediately removing their current license, users are placed into a "hold" group (e.g., `M365-A5-Faculty-90Day-HOLD`) with a 90-day membership expiration. During this grace period they retain their current license and services.

The requirement is to send the user email notifications at specific milestones during the grace period:

- **Day 0** (90 days remaining): Initial notice that their license will transition
- **Day 30** (60 days remaining): Second reminder
- **Day 60** (30 days remaining): Third reminder
- **Day 89** (1 day remaining): Final notice with instructions to save files before the transition

Each email must include the specific expiration date (not just "X days remaining"), and duplicate emails must never be sent for the same milestone.

## How the Solution Works

The solution is a Grouper GSH script daemon that runs on a schedule (e.g., daily). It uses a single SQL query against Grouper's point-in-time (PIT) membership view (`grouper_pit_mship_group_lw_v`) to find all current members of the hold group and classify them into notification buckets based on how many days they have been in the group:

| Days in Group | Email Type | Meaning |
| --- | --- | --- |
| 0 - 5 | 90day | Just added, 90 days remain |
| 30 - 35 | 60day | 30 days in, 60 days remain |
| 60 - 65 | 30day | 60 days in, 30 days remain |
| 88 - 89 | 1day | About to expire, 1 day remains |

Note, there is a range there in case the daemon is down for a couple days it will catch up.

Each bucket has a multi-day window so that if the daemon misses a run (e.g., server downtime), members are still caught.

A custom PostgreSQL table (`m365_notification_log`) tracks which notifications have already been sent. The SQL query joins against this table to exclude members who have already received a given email type. The table's primary key is `(subject_id, email_type)`, which prevents duplicates at the database level.

For each member returned by the query, the daemon:

1. Resolves the subject to get their name and email address
2. Calculates the expiration date from the PIT start time + 90 days
3. Builds an HTML email tailored to the milestone (the final notice has a different tone)
4. Sends the email via `GrouperEmail`
5. Inserts a record into `m365_notification_log`

The daemon logs counts per email type to the Grouper loader log, visible in the admin UI under daemon job history.

## How to Create a Script Daemon

- **Create the notification log table** — Run the DDL and COMMENT ON statements in the Grouper PostgreSQL database (see the comments at the top of the script source code).

- **Add the daemon in the Grouper UI** — Go to **Miscellaneous > Daemon jobs**, click **Daemon actions > Add daemon**, and fill out the form:

| Field | Value |
| --- | --- |
| Config ID | `m365GracePeriodNotifications` |
| Daemon type | Script daemon |
| Quartz cron | `23 47 7 * * ?` (daily at 7:47:23 AM — use a random minute and second so daemon jobs are staggered and don't all fire at the same time) |
| Script type | gsh |
| File type | script |
| Script source | (paste the full script source code into the textarea) |

- **Monitor** — After each run, check the daemon job log in **Miscellaneous > Daemon jobs**. The job message will show counts like `totalNotificationsToSend`, `emailsSent90day`, `emailsSent60day`, `emailsSent30day`, `emailsSent1day`, and `totalEmailsSent`. Any subjects that could not be resolved or had no email address will also be logged.

## Source code

```
import java.sql.Timestamp;
import java.util.*;

import org.apache.commons.lang3.*;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.app.loader.*;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.*;
import edu.internet2.middleware.grouperClient.jdbc.*;
import edu.internet2.middleware.subject.*;

// ============================================================================
// M365 License Grace Period Notification Daemon
// ============================================================================
//
// Overview:
//   When a user changes affiliation (e.g., a faculty member retires), they are
//   placed into a "hold" group with a 90-day membership expiration. During that
//   grace period, they retain their current M365 license. This daemon sends
//   reminder emails at specific milestones leading up to the expiration date:
//     - 90day: the day they are added to the hold group (0-5 days in group)
//     - 60day: 30 days after being added, 60 days remain (30-35 days in group)
//     - 30day: 60 days after being added, 30 days remain (60-65 days in group)
//     - 1day:  89 days after being added, 1 day remains (88-90 days in group)
//
//   Each milestone has a multi-day window so that if the daemon is down for
//   a few days or misses a run, we still catch members in that window.
//
// Group: test:M365-A5-Faculty-90Day-HOLD
//   Members are added with a 90-day membership expiration.
//   Send notification emails at: 90 days out (day added), 60 days out, 30 days out, and 1 day out.
//
// ============================================================================
// DDL for notification tracking table (run this in PostgreSQL before first use):
// ============================================================================
//
// CREATE TABLE m365_notification_log (
//   subject_id VARCHAR(256) NOT NULL,
//   email_type VARCHAR(16) NOT NULL,
//   to_email_address VARCHAR(256) NOT NULL,
//   email_body TEXT,
//   sent_timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
//   PRIMARY KEY (subject_id, email_type)
// );
//
// COMMENT ON TABLE m365_notification_log IS 'Tracks M365 license grace period notification emails sent to users so we do not send duplicate notifications of the same type';
// COMMENT ON COLUMN m365_notification_log.subject_id IS 'Grouper subject ID of the member';
// COMMENT ON COLUMN m365_notification_log.email_type IS 'Notification milestone: 90day, 60day, 30day, 1day';
// COMMENT ON COLUMN m365_notification_log.to_email_address IS 'Email address the notification was sent to';
// COMMENT ON COLUMN m365_notification_log.email_body IS 'The actual email body sent to the user';
// COMMENT ON COLUMN m365_notification_log.sent_timestamp IS 'When the email was sent';

public class Test169m365LicenseGracePeriodNotifications {

  // CHANGE THIS
  private static final String subjectSourceId = "jdbc";

  public static void main(String[] args) {

    // The fully qualified name of the hold group in Grouper.
    // Members of this group are in their 90-day grace period.
    String groupName = "test:M365-A5-Faculty-90Day-HOLD";

    // The grace period length in days. Members are given 90 days before
    // their license transitions.
    int gracePeriodDays = 90;

    // OtherJobScript provides integration with Grouper's daemon/loader framework.
    // retrieveHib3GrouperLoaderLogNotNull() returns the loader log object for this
    // daemon run, which is used to track counts (inserts, updates, deletes) and
    // store a job message that appears in the Grouper UI under "Daemon jobs".
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = OtherJobScript.retrieveHib3GrouperLoaderLogNotNull();

    // debugMap is a LinkedHashMap so that entries appear in insertion order.
    // It accumulates key statistics and diagnostic info throughout the run.
    // At the end, it is serialized to a string and stored as the job message
    // in the loader log, making it visible in the Grouper admin UI.
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    GrouperSession grouperSession = null;

    try {

      // Start a root session so we have full privileges to read memberships
      // and group data. This is standard for daemon scripts.
      grouperSession = GrouperSession.startRootSession();

      // =====================================================================
      // Step 1: Find all members who need ANY notification
      // =====================================================================
      //
      // Instead of running separate queries per milestone, we use a single
      // query that returns each member along with their email_type bucket.
      // The query uses CASE/WHEN to classify members into buckets based on
      // how many days they have been in the group:
      //
      //   Days in group  |  email_type  |  Meaning
      //   ---------------+--------------+----------------------------------
      //   0 - 5          |  90day       |  Just added, 90 days remain
      //   30 - 35        |  60day       |  30 days in, 60 days remain
      //   60 - 65        |  30day       |  60 days in, 30 days remain
      //   88 - 90        |  1day        |  About to expire, 1 day remains
      //
      // Each bucket has a multi-day window so that if the daemon misses a
      // run (e.g., downtime), we still catch members in that window.
      //
      // We use the point-in-time (PIT) lightweight membership view
      // (grouper_pit_mship_group_lw_v) which has subject_id, group_name,
      // and the_start_time in microseconds since epoch.
      //
      // Key conditions:
      //   - the_end_time is null: membership is still active
      //   - field_name = 'members': only actual memberships (not privileges)
      //   - NOT IN m365_notification_log: haven't already sent this email type
      //   - CASE determines which bucket, returns NULL if not in any window
      //     (those rows are filtered out by the outer query)
      //
      // We use max(the_start_time) and GROUP BY because a member could have
      // been removed and re-added; we want the most recent addition date.
      //
      // The inner query computes days_in_group from the max start time.
      // The outer query applies the CASE/WHEN bucketing logic.
      //
      // NOTE: We compute the current time in SQL using
      // extract(epoch from now()) * 1000000 to get microseconds since epoch.
      // This avoids passing a Java long as a bind variable, which can cause
      // a PostgreSQL "integer out of range" error because the JDBC driver
      // may bind it as a 32-bit integer. The division constant
      // (1000000 * 60 * 60 * 24 = 86400000000) is written as a literal
      // bigint (86400000000) to avoid integer overflow in PostgreSQL.
      //

      String sqlNotifications = """
select subject_id, the_start_time, email_type
from (
  select inner_q.subject_id, inner_q.the_start_time,
    case
      when inner_q.days_in_group between 0 and 5
        and inner_q.subject_id not in (select mnl.subject_id from m365_notification_log mnl where mnl.email_type = '90day')
        then '90day'
      when inner_q.days_in_group between 30 and 35
        and inner_q.subject_id not in (select mnl.subject_id from m365_notification_log mnl where mnl.email_type = '60day')
        then '60day'
      when inner_q.days_in_group between 60 and 65
        and inner_q.subject_id not in (select mnl.subject_id from m365_notification_log mnl where mnl.email_type = '30day')
        then '30day'
      when inner_q.days_in_group between 88 and 90
        and inner_q.subject_id not in (select mnl.subject_id from m365_notification_log mnl where mnl.email_type = '1day')
        then '1day'
      else null
    end as email_type
  from (
    select gpm.subject_id, max(gpm.the_start_time) as the_start_time,
      (cast(extract(epoch from now()) * 1000000 as bigint) - max(gpm.the_start_time)) / cast(86400000000 as bigint) as days_in_group
    from grouper_pit_mship_group_lw_v gpm
    where gpm.group_name = ?
    and gpm.field_name = 'members'
    and gpm.the_end_time is null
    group by gpm.subject_id
  ) inner_q
) outer_q
where outer_q.email_type is not null
""";

      // Execute the query. Bind variable:
      //   1st ? = group name
      // Each row returns: [subject_id (String), the_start_time (Long), email_type (String)]
      List<Object[]> notificationRows = new GcDbAccess().sql(sqlNotifications).addBindVar(groupName).selectList(Object[].class);

      // Log the total number of notifications to send
      debugMap.put("totalNotificationsToSend", notificationRows.size());

      // =====================================================================
      // Step 2: Loop through results, send emails, and log them
      // =====================================================================
      //
      // For each row we:
      //   1. Resolve the Grouper subject to get their name and email address
      //   2. Calculate the expiration date (start date + 90 days)
      //   3. Determine the reminder number and days remaining from the bucket
      //   4. Build an HTML email tailored to the specific milestone
      //   5. Send the email using GrouperEmail
      //   6. Insert a record into m365_notification_log so we don't re-send
      //

      // Track counts per email type for the daemon job log
      int emailsSent90day = 0;
      int emailsSent60day = 0;
      int emailsSent30day = 0;
      int emailsSent1day = 0;

      // Prepare the insert SQL once, reuse for each notification
      String insertSql = """
insert into m365_notification_log (subject_id, email_type, to_email_address, email_body) values (?, ?, ?, ?)
""";

      // Date formatter for user-friendly dates (e.g., "04/30/2026")
      java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy");

      for (Object[] row : notificationRows) {

        String subjectId = (String) row[0];

        // the_start_time is in microseconds since epoch. Convert to millis
        // by dividing by 1000, then calculate the expiration date by adding
        // the grace period (90 days).
        long startTimeMicros = ((Number) row[1]).longValue();
        long startTimeMillis = startTimeMicros / 1000L;
        long expirationMillis = startTimeMillis + ((long) gracePeriodDays * 24 * 60 * 60 * 1000);

        // Format the expiration date for display in the email
        String expirationDate = sdf.format(new java.util.Date(expirationMillis));

        // The email_type bucket determined by the SQL query
        String emailType = (String) row[2];

        // Resolve the subject in Grouper's subject system. This gives us access
        // to the member's name, email, and other attributes.
        // The second parameter "pennperson" is the subject source ID.
        // The third parameter "false" means don't throw an exception if not found.
        Subject subject = SubjectFinder.findByIdAndSource(subjectId, subjectSourceId, false);

        // If the subject can't be found (e.g., deleted account), skip them
        if (subject == null) {
          debugMap.put("subjectNotFound_" + subjectId, true);
          continue;
        }

        // Use GrouperEmail's static helper to look up the email address
        // from the subject's attributes (configured in subject.properties).
        String emailAddress = GrouperEmail.retrieveEmailAddress(subject);

        // If no email address is available, skip this member
        if (StringUtils.isBlank(emailAddress)) {
          debugMap.put("noEmail_" + subjectId, true);
          continue;
        }

        // Get the member's display name for the email greeting
        String displayName = subject.getName();

        // ---------------------------------------------------------------
        // Determine the reminder number and days remaining based on bucket.
        // This tailors each email so the member knows which reminder it is
        // and exactly how many days they have left.
        // ---------------------------------------------------------------
        String reminderNumber = null;
        String daysRemaining = null;

        if ("90day".equals(emailType)) {
          reminderNumber = "1st";
          daysRemaining = "90";
        } else if ("60day".equals(emailType)) {
          reminderNumber = "2nd";
          daysRemaining = "60";
        } else if ("30day".equals(emailType)) {
          reminderNumber = "3rd";
          daysRemaining = "30";
        } else if ("1day".equals(emailType)) {
          reminderNumber = "final";
          daysRemaining = "1";
        }

        // ---------------------------------------------------------------
        // Build the HTML email body, tailored per milestone.
        // The 1day (final) notice has a different tone since the transition
        // is imminent — it tells them the transition is happening tomorrow
        // and includes information about their new license.
        // ---------------------------------------------------------------
        String emailBody = null;

        // NOTE: In Groovy/GSH, string concatenation with "+" at the start of
        // a line will fail. So we build the email body using StringBuilder
        // and append() calls on separate lines instead.

        StringBuilder emailBodyBuilder = new StringBuilder();

        if ("1day".equals(emailType)) {
          // Final notice — transition is imminent
          emailBodyBuilder.append("<html><body>");
          emailBodyBuilder.append("<p>Hello ").append(StringUtils.defaultString(displayName)).append(",</p>");
          emailBodyBuilder.append("<p>This is your <strong>final reminder</strong> that your current Faculty A5 ");
          emailBodyBuilder.append("Microsoft 365 license and your access to associated Microsoft applications ");
          emailBodyBuilder.append("will end on <strong>").append(expirationDate).append("</strong>.</p>");
          emailBodyBuilder.append("<p>After that date, your account will be transitioned to a Retiree license. ");
          emailBodyBuilder.append("Please ensure you have saved any important files or data before the transition.</p>");
          emailBodyBuilder.append("<p>If you need additional information or have questions or concerns, ");
          emailBodyBuilder.append("please contact us.</p>");
          emailBodyBuilder.append("<p>Thank you.</p>");
          emailBodyBuilder.append("</body></html>");
        } else {
          // Standard reminder (1st, 2nd, or 3rd)
          emailBodyBuilder.append("<html><body>");
          emailBodyBuilder.append("<p>Hello ").append(StringUtils.defaultString(displayName)).append(",</p>");
          emailBodyBuilder.append("<p>This is your <strong>").append(reminderNumber).append(" reminder</strong> that your current Faculty A5 ");
          emailBodyBuilder.append("Microsoft 365 license and your access to associated Microsoft applications ");
          emailBodyBuilder.append("will end on <strong>").append(expirationDate).append("</strong> ");
          emailBodyBuilder.append("(").append(daysRemaining).append(" days from now).</p>");
          emailBodyBuilder.append("<p>After that date, your account will be transitioned to a Retiree license.</p>");
          emailBodyBuilder.append("<p>If you need additional information or have questions or concerns, ");
          emailBodyBuilder.append("please contact us.</p>");
          emailBodyBuilder.append("<p>Thank you.</p>");
          emailBodyBuilder.append("</body></html>");
        }

        emailBody = emailBodyBuilder.toString();

        // Send the email using GrouperEmail. This uses the Grouper mail
        // configuration (SMTP server, from address, etc.) defined in
        // grouper.properties.
        GrouperEmail grouperEmail = new GrouperEmail();
        grouperEmail.addEmailAddressToSendTo(emailAddress);
        grouperEmail.setSubject("Microsoft 365 License Transition Notice - Action Required");
        grouperEmail.setBody(emailBody);
        grouperEmail.send();

        // Insert a record into the notification log table so we don't send
        // this same notification type again on the next daemon run.
        // We store the email address and body for audit/troubleshooting purposes.
        new GcDbAccess().sql(insertSql).addBindVar(subjectId).addBindVar(emailType).addBindVar(emailAddress).addBindVar(emailBody).executeSql();

        // Increment the appropriate counter for the daemon job log
        if ("90day".equals(emailType)) {
          emailsSent90day++;
        } else if ("60day".equals(emailType)) {
          emailsSent60day++;
        } else if ("30day".equals(emailType)) {
          emailsSent30day++;
        } else if ("1day".equals(emailType)) {
          emailsSent1day++;
        }
      }

      // Log counts per email type to the debugMap so administrators can see
      // exactly how many of each notification type were sent in this run.
      debugMap.put("emailsSent90day", emailsSent90day);
      debugMap.put("emailsSent60day", emailsSent60day);
      debugMap.put("emailsSent30day", emailsSent30day);
      debugMap.put("emailsSent1day", emailsSent1day);

      // Record total emails sent in the loader log insert count.
      // This shows up in the Grouper UI daemon job summary.
      int totalEmailsSent = emailsSent90day + emailsSent60day + emailsSent30day + emailsSent1day;
      hib3GrouperLoaderLog.addInsertCount(totalEmailsSent);
      debugMap.put("totalEmailsSent", totalEmailsSent);

    } catch (RuntimeException re) {
      // If an exception occurs, capture the full stack trace in the debugMap
      // so it will be visible in the Grouper admin UI daemon job log.
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
      // Always stop the Grouper session, even if an exception occurred.
      // stopQuietly() won't throw if the session is null.
      GrouperSession.stopQuietly(grouperSession);

      // Serialize the debugMap to a readable string and store it as the
      // job message in the loader log. This is what administrators see
      // in the Grouper UI when they view this daemon's run history.
      String debugMapForLog = GrouperUtil.toStringForLog(debugMap);
      hib3GrouperLoaderLog.setJobMessage(debugMapForLog);

      // If OtherJobScript.retrieveFromThreadLocal() is null, it means we are
      // running this script locally (not inside the Grouper daemon framework),
      // e.g., for testing. In that case, print the debug info and exit.
      // When running as a real daemon job, this block is skipped.
      if (OtherJobScript.retrieveFromThreadLocal() == null) {
        System.out.println(debugMapForLog);
        System.exit(0);
      }
    }
  }

}

// This commented-out line is required by the Grouper daemon framework.
// It is the entry point that the daemon scheduler uses to invoke this script.
//Test169m365LicenseGracePeriodNotifications.main(null);
```
