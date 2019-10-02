package edu.internet2.middleware.grouperDuo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import com.duosecurity.client.Http;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderScheduleType;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Synchronize
 */
@DisallowConcurrentExecution
public class GrouperDuoAdminFullRefresh extends OtherJobBase {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDuoAdminFullRefresh.class);

  public static final String GROUPER_DUO_ADMIN_FULL_REFRESH = "CHANGE_LOG_grouperDuoAdminFullRefresh";

  /**
   *
   */
  public static void fullRefreshLogic() {
    OtherJobInput otherJobInput = new OtherJobInput();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    otherJobInput.setGrouperSession(grouperSession);
    Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouploaderLog);
    try {
      fullRefreshLogic(otherJobInput);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  public static void fullRefreshLogic(OtherJobInput otherJobInput) {
    LOG.info("Starting GrouperDuo Administrator Full Refresh...");
    GrouperSession grouperSession = otherJobInput.getGrouperSession();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "fullRefreshLogic");

    //lets enter a log entry so it shows up as error in the db
    Hib3GrouperLoaderLog hib3GrouploaderLog = otherJobInput.getHib3GrouperLoaderLog();
    hib3GrouploaderLog.setHost(GrouperUtil.hostname());
    hib3GrouploaderLog.setJobName(GROUPER_DUO_ADMIN_FULL_REFRESH);
    hib3GrouploaderLog.setJobScheduleType(GrouperLoaderScheduleType.CRON.name());
    hib3GrouploaderLog.setJobType(GrouperLoaderType.MAINTENANCE.name());

    hib3GrouploaderLog.setStartedTime(new Timestamp(System.currentTimeMillis()));

    Map<String, GrouperDuoAdministrator> administratorMap = GrouperDuoCommands
        .retrieveAdminAccounts();
    ArrayList<GrouperDuoAdministrator> administrators = new ArrayList<GrouperDuoAdministrator>(
        administratorMap.values());
    HashSet<Member> grouperMembers = retrieveMembersFromAdminGroups(grouperSession);
    LOG.debug(String.format("Fetched %d administrator accounts, and %d grouper members.",
        administrators.size(), grouperMembers.size()));

    // Sync the state of all Grouper users managed by the admin sync
    for (Member member : grouperMembers) {
      GrouperDuoAdministrator administrator;
      try {
        administrator = GrouperDuoUtils.fetchOrCreateGrouperDuoAdministrator(
            member,
            true,
            administratorMap
            );
      } catch (Exception e) {
        LOG.error("Error fetchOrCreating Duo Administrator for Member " + member.getName() + "("
            + member.getId() + ")" + ", " + e.getMessage(), e);
        if (GrouperDuoUtils.configEmailRecipientsGroupName().length() > 0) {
          String body = "Failed to create an administrator object during an Administrator Full Sync operation.\nCheck the logs for a stack trace.\n\n";
          String subject = "Error Creating an Administrator in Duo.";

          try {
            Subject s = member.getSubject();
            LOG.error(String.format("Subject Id: %s, Subject Source: %s", s.getId(), s.getSource()));

            body += "\n\nSubject Information:\n";
            body += String.format("Subject Id: %s \nSubject Source: %n", s.getId(), s.getSource());
            body += GrouperDuoUtils.getSubjectAttributesForEmail(s);
          } catch (SubjectNotFoundException subjectNotFoundException) {
            LOG.error(e);
            body += "\nAdditionally, there was a SubjectNotFoundException thrown while handling this exception.";
            body += "\n\nMember Information:\n";
            body += "Member Id: " + member.getId() + "\n";
            body += "Member Name: " + member.getName() + "\n";
          }

          GrouperDuoUtils.sendEmailToGroupMembers(
              GroupFinder.findByName(grouperSession,
                  GrouperDuoUtils.configEmailRecipientsGroupName(), false),
              subject,
              body
              );
        }

        continue;
      }

      if (administrator == null) {
        LOG.error("Failed to fetch or create Administrator for grouper user " + member.getName());
        continue;
      }

      LOG.debug(String.format("Syncing Grouper Member %s with Duo Admin %s <%s>", member.getName(),
          administrator.getAdminId(), administrator.getEmail()));

      // Skip and remove Admins with a role outside of the configured roles to manage
      if (!GrouperDuoUtils.manageableAdminRoles().contains(administrator.getRole())) {
        LOG.debug("Skipping admin " + administrator.toString() + " due to unmanaged admin role.");
        administrators.remove(administrator);
        continue;
      }

      // Sync the Member and Administrator objects
      try {
        GrouperDuoUtils.synchronizeMemberAndDuoAdministrator(grouperSession, member, administrator);
      } catch (SubjectNotFoundException e) {
        // If the subject cannot be found, then we should skip processing them. They will be marked as an
        // unmanaged administrator and action may be taken against the unmanaged account. If the 
        // subject becomes available later, the account can be reprovisioned.
        continue;
      }

      // Remove the administrator from the administrators list (So we know that any remaining accounts are unmanaged by GMS)
      administrators.remove(administrator);
    }

    // Determine the fate of any remaining Duo administrator objects...
    for (GrouperDuoAdministrator administrator : administrators) {
      if (!GrouperDuoUtils.manageableAdminRoles().contains(administrator.getRole())) {
        LOG.debug("Skipping admin " + administrator.toString() + " due to unmanaged admin role.");
        continue;
      }

      LOG.debug(String.format("Found unmanaged Duo administrator account... ID: %s, Email: %s",
          administrator.getAdminId(), administrator.getEmail()));
      if (GrouperDuoUtils.isDisableUnknownAdminAccountsEnabled() && administrator.isActive()) {
        LOG.warn(String
            .format(
                "Disabling unmanaged administrator account. ID: %s, Email: %s, Name: %s, Last Login: %d",
                administrator.getAdminId(), administrator.getEmail(), administrator.getName(),
                administrator.getLastLogin()));

        try {
          Http request = GrouperDuoCommands.startAdminUpdateRequest(administrator);
          GrouperDuoCommands.updateAdminStatus(request, false);
          GrouperDuoCommands.updateAdminRole(request, "Read-only");
          GrouperDuoCommands.executeAdminUpdateRequest(administrator, request);
        } catch (Exception e) {
          LOG.error("Error while disabling an unmanaged administrator account...", e);
        }
      }

      if (GrouperDuoUtils.isDeleteUnknownAdminAccountsEnabled() && !administrator.isActive()) {
        long secondsSinceLastLogin = Math.round(System.currentTimeMillis() / 1000)
            - administrator.getLastLogin();

        if (secondsSinceLastLogin > GrouperDuoUtils.deleteUnknownAdminAccountsAfterSeconds()) {
          LOG.warn(String
              .format(
                  "Deleting unmanaged administrator account. ID: %s, Email: %s, Name: %s, Last Login: %d",
                  administrator.getAdminId(), administrator.getEmail(), administrator.getName(),
                  administrator.getLastLogin()));

          try {
            GrouperDuoCommands.deleteAdminAccount(administrator.getAdminId());
          } catch (Exception e) {
            LOG.error("Failed to delete Duo Administrator account: " + administrator.getAdminId(),
                e);
          }
        }

      }

    }

    //lets enter a log entry so it shows up as error in the db
    hib3GrouploaderLog.setJobMessage(GrouperUtil.mapToString(debugMap));
    hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
    hib3GrouploaderLog.store();

    LOG.info("Finished GrouperDuo Admin Full Refresh.");
  }

  private static HashSet<Member> retrieveMembersFromAdminGroups(GrouperSession session) {
    String folderName = GrouperDuoUtils.configFolderForDuoAdmins();
    folderName = folderName.substring(0, folderName.length() - 1);

    Stem roleStem = StemFinder.findByName(session, folderName, true);
    HashSet<Member> members = new HashSet<Member>();

    for (Object object : roleStem.getChildGroups()) {
      Group group = (Group) object;

      members.addAll(group.getMembers());
    }

    return members;
  }

  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    OtherJobOutput otherJobOutput = new OtherJobOutput();
    fullRefreshLogic(otherJobInput);

    return otherJobOutput;
  }
}
