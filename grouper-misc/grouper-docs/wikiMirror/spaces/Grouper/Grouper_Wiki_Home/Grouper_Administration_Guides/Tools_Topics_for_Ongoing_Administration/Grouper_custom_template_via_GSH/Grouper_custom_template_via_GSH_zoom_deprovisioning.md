---
title: "Grouper custom template via GSH zoom deprovisioning"
space: Grouper
pageId: 28548913
version: 3
lastUpdated: 2026-07-01T05:43:14.316Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548913/Grouper+custom+template+via+GSH+zoom+deprovisioning
---

## Screenshots

See [this page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549445/Penn+Zoom+Deprovisioning)

## Configuration

```
grouperGshTemplate.zoomDeprovisioning.displayErrorOutput = true
grouperGshTemplate.zoomDeprovisioning.folderShowOnDescendants = certainFolderAndDescendants
grouperGshTemplate.zoomDeprovisioning.folderShowType = certainFolder
grouperGshTemplate.zoomDeprovisioning.folderUuidToShow = 319a44d13d754dfea820571dd2709147
grouperGshTemplate.zoomDeprovisioning.groupUuidCanRun = b05d4ff9946c4e6aa3f33c6e71dcca48
grouperGshTemplate.zoomDeprovisioning.input.0.description = Pick what you want to do.  Manage non humans to remove false positives.  Manage email links to link non-eppn emails up with their identities.  The reports have the CSVs.  Reports are scheduled to run weekly so you should generally run them before viewing them to get most recent data.
grouperGshTemplate.zoomDeprovisioning.input.0.dropdownCsvValue = runReports, viewReports, addNonHumans, removeNonHumans, addEmailMappings, removeEmailMappings
grouperGshTemplate.zoomDeprovisioning.input.0.dropdownValueFormat = csv
grouperGshTemplate.zoomDeprovisioning.input.0.formElementType = dropdown
grouperGshTemplate.zoomDeprovisioning.input.0.label = Action
grouperGshTemplate.zoomDeprovisioning.input.0.name = gsh_input_action
grouperGshTemplate.zoomDeprovisioning.input.0.required = true
grouperGshTemplate.zoomDeprovisioning.input.1.description = Enter in emails, one on each line, no delimiters
grouperGshTemplate.zoomDeprovisioning.input.1.formElementType = textarea
grouperGshTemplate.zoomDeprovisioning.input.1.label = Emails
grouperGshTemplate.zoomDeprovisioning.input.1.maxLength = 100000
grouperGshTemplate.zoomDeprovisioning.input.1.name = gsh_input_emails
grouperGshTemplate.zoomDeprovisioning.input.1.required = true
grouperGshTemplate.zoomDeprovisioning.input.1.showEl = ${gsh_input_action == 'addNonHumans' || gsh_input_action == 'removeNonHumans' || gsh_input_action == 'removeEmailMappings' }
grouperGshTemplate.zoomDeprovisioning.input.1.validationType = none
grouperGshTemplate.zoomDeprovisioning.input.2.description = Enter in CSV with no headers.  First col is email address, second col is penn id or pennkey.
grouperGshTemplate.zoomDeprovisioning.input.2.formElementType = textarea
grouperGshTemplate.zoomDeprovisioning.input.2.label = Email link
grouperGshTemplate.zoomDeprovisioning.input.2.maxLength = 100000
grouperGshTemplate.zoomDeprovisioning.input.2.name = gsh_input_emailLink
grouperGshTemplate.zoomDeprovisioning.input.2.showEl = ${gsh_input_action == 'addEmailMappings'}
grouperGshTemplate.zoomDeprovisioning.input.2.validationType = none
grouperGshTemplate.zoomDeprovisioning.moreActionsLabel = Zoom deprovisioning
grouperGshTemplate.zoomDeprovisioning.numberOfInputs = 3
grouperGshTemplate.zoomDeprovisioning.runAsType = GrouperSystem
grouperGshTemplate.zoomDeprovisioning.securityRunType = specifiedGroup
grouperGshTemplate.zoomDeprovisioning.showInMoreActions = true
grouperGshTemplate.zoomDeprovisioning.showOnFolders = true
grouperGshTemplate.zoomDeprovisioning.templateDescription = Manage deprovisioning of zoom licensed accounts
grouperGshTemplate.zoomDeprovisioning.templateName = Zoom deprovisioning

```

GSH script

```
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.apache.commons.lang3.StringUtils;
//
//import edu.internet2.middleware.grouper.GrouperSession;
//import edu.internet2.middleware.grouper.Member;
//import edu.internet2.middleware.grouper.MemberFinder;
//import edu.internet2.middleware.grouper.Stem;
//import edu.internet2.middleware.grouper.StemFinder;
//import edu.internet2.middleware.grouper.SubjectFinder;
//import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
//import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
//import edu.internet2.middleware.grouper.misc.GrouperStartup;
//import edu.internet2.middleware.grouper.util.GrouperUtil;
//import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
//import edu.internet2.middleware.subject.Subject;
//
////uncomment to compile in eclipse (and last line)
//public class Test11 {
//
//  public Test11() {
//  }
//
//  public static void main(String[] args) {
//    
//    GrouperStartup.startup();
//        
//    GrouperSession gsh_builtin_grouperSession = GrouperSession.startRootSession();
//    Subject gsh_builtin_subject = SubjectFinder.findByIdentifierAndSource("mchyzer", "pennperson", true);
//    GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();
//    String gsh_builtin_ownerStemName = "penn:isc:ait:apps:zoom:service:ref:zoomDeprovisioning";
//    
//    String gsh_input_action = "removeEmailMappings"; //  runReports, viewReports, addNonHumans, removeNonHumans, addEmailMappings, removeEmailMappings
//    String gsh_input_emails = "brymes@example.com"; //     gsh_input_emails += "0095a7722511b8e55f72e557efbf3c4a1dccf7ae@example.com\n";
//    String gsh_input_emailLink = ""; //     gsh_input_emailLink += "brymes@example.com, brymes\n";

    // check valid action (should never be invalid since drop down)
    
    boolean actionRunReports = StringUtils.equals(gsh_input_action, "runReports");
    boolean actionViewReports = StringUtils.equals(gsh_input_action, "viewReports");
    boolean actionAddNonHumans = StringUtils.equals(gsh_input_action, "addNonHumans");
    boolean actionRemoveNonHumans = StringUtils.equals(gsh_input_action, "removeNonHumans");
    boolean actionAddEmailMappings = StringUtils.equals(gsh_input_action, "addEmailMappings");
    boolean actionRemoveEmailMappings = StringUtils.equals(gsh_input_action, "removeEmailMappings");
    if (!actionRunReports && !actionViewReports && !actionAddNonHumans && !actionViewReports && !actionRemoveNonHumans && !actionAddEmailMappings && !actionRemoveEmailMappings) {

      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_action",
          "Error: Action cannot be found '" + gsh_input_action + "'!");

    }

    // stay on screen
    if (!actionViewReports) {
      gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("NONE");
    }

    
    // make sure emails are valid
    Set<String> emails = null;
    if (actionAddNonHumans || actionRemoveNonHumans || actionRemoveEmailMappings) {
      gsh_input_emails = StringUtils.replace(gsh_input_emails, "\r\n", "\n");
      gsh_input_emails = StringUtils.replace(gsh_input_emails, "\r", "\n");
      emails = GrouperUtil.splitTrimToSet(gsh_input_emails, "\n");
      emails.remove("");
      if (emails.size() == 0) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_emails", 
            "Error: Enter some emails");
      }
      for (String email : emails) {
        if (email.contains(" ") || email.contains(",")) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_emails", 
              "Error: Invalid email '" + email + "'");
        }
        if (!email.contains("@")) {
          gsh_builtin_gshTemplateOutput.addOutputLine("Note this email '" + email + "' does not contain an @ sign");
        }
      }
    }

    Map<String, String> emailLinks = null; // email, charPennId
    if (actionAddEmailMappings) {
      emailLinks = new HashMap<String, String>();
      gsh_input_emailLink = StringUtils.replace(gsh_input_emailLink, "\r\n", "\n");
      gsh_input_emailLink = StringUtils.replace(gsh_input_emailLink, "\r", "\n");
      Set<String> emailLinkLines = GrouperUtil.splitTrimToSet(gsh_input_emailLink, "\n");
      emailLinkLines.remove("");
      if (emailLinkLines.size() == 0) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_emailLink", 
            "Error: Enter some comma separated emails and (pennkeys or pennids), e.g. jsmith@example.com, jsmith -or- jsmith@example.com, 12345678");
      }
      for (String emailLinkLine : emailLinkLines) {
        if (!emailLinkLine.contains(",")) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_emailLink", 
              "Error: Invalid email link '" + emailLinkLine + "', should be: email@address comma pennkeyOrPennid");
          continue;
        }
        
        String email = StringUtils.trim(GrouperUtil.prefixOrSuffix(emailLinkLine, ",", true));
        String pennkeyOrPennid = StringUtils.trim(GrouperUtil.prefixOrSuffix(emailLinkLine, ",", false));
        
        if (email.contains(" ") || email.contains(",")) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_emailLink", 
              "Error: Invalid email '" + email + "'");
        } else if (!email.contains("@")) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_emailLink", "Note this email '" + email + "' does not contain an @ sign");
        } else {
          
          Subject subject = SubjectFinder.findByIdOrIdentifierAndSource(pennkeyOrPennid, "pennperson", false);
          if (subject == null) {
            gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_emailLink", "Person not found: '" + pennkeyOrPennid + "'");
            continue;
          }
          emailLinks.put(email, subject.getId());
        }
      }
      if (emailLinks.size() == 0) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_emailLink", 
            "Error: No email links found");
      }

    }
    
    
    
    // if anything not valid, stop
    if (GrouperUtil.length(gsh_builtin_gshTemplateOutput.getValidationLines()) > 0) {
      gsh_builtin_gshTemplateOutput.assignIsError(true);
      GrouperUtil.gshReturn();
    }

    java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
    
    GrouperUtil.sleep(3000);

    if (actionAddNonHumans || actionRemoveNonHumans) {

      // get emails from database
      Set<String> emailsLowerInDatabase = new HashSet<String>(new GcDbAccess().sql("select lower(email) from penn_zoom_non_human").selectList(String.class));
      
      GcDbAccess gcDbAccess = new GcDbAccess().batchSize(200);
      List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
      gcDbAccess.batchBindVars(batchBindVars);
      
      Member builtInMember = MemberFinder.findBySubject(gsh_builtin_grouperSession, gsh_builtin_subject, true);
      
      if (actionAddNonHumans) {
        gcDbAccess.sql("insert into penn_zoom_non_human (email, modifier_id, modify_time) values (?, ?, ?)");
      } else if (actionRemoveNonHumans) {
        gcDbAccess.sql("delete from penn_zoom_non_human where lower(email) = ?");
      }
      
      long modifyTime = System.currentTimeMillis();
      
      int countChanged = 0;
      int countUnchanged = 0;
      
      for (String email : emails) {
        
        boolean emailInDatabase = emailsLowerInDatabase.contains(email.toLowerCase());

        if (actionAddNonHumans) {
          
          if (emailInDatabase) {
            countUnchanged++;
          } else {
            countChanged++;
            batchBindVars.add(GrouperUtil.toListObject(email, builtInMember.getId(), modifyTime));
          }
        } else if (actionRemoveNonHumans) {
          if (emailInDatabase) {
            countChanged++;
            batchBindVars.add(GrouperUtil.toListObject(email));
          } else {
            countUnchanged++;
          }
          
        }
        
      }

      if (countChanged > 0) {
        gcDbAccess.executeBatchSql();
        gsh_builtin_gshTemplateOutput.addOutputLine("Made " + countChanged + " changes");
      }
      if (countUnchanged > 0) {
        gsh_builtin_gshTemplateOutput.addOutputLine(countUnchanged + " records did not need changes");
      }
      
    }
    
    if (actionAddEmailMappings || actionRemoveEmailMappings) {

      // get emails from database
      List<Object[]> emailsLowerInDatabase = new GcDbAccess().connectionName("pennCommunity").sql("select lower(lower_email_address), char_penn_id from person_source_email_adhoc").selectList(Object[].class);
      
      Map<String, String> emailLowerToPennIdInDatabase = new HashMap<String, String>();
      
      for (Object[] row : GrouperUtil.nonNull(emailsLowerInDatabase)) {
        emailLowerToPennIdInDatabase.put((String)row[0], (String)row[1]);
      }
      
      GcDbAccess gcDbAccessAdd = new GcDbAccess().connectionName("pennCommunity").batchSize(200);
      List<List<Object>> batchBindVarsAdd = new ArrayList<List<Object>>();
      gcDbAccessAdd.batchBindVars(batchBindVarsAdd);
      gcDbAccessAdd.sql("insert into person_source_email_adhoc (lower_email_address, char_penn_id) values (?, ?)");
      
      GcDbAccess gcDbAccessDelete = new GcDbAccess().connectionName("pennCommunity").batchSize(200);
      List<List<Object>> batchBindVarsDelete = new ArrayList<List<Object>>();
      gcDbAccessDelete.batchBindVars(batchBindVarsDelete);
      gcDbAccessDelete.sql("delete from person_source_email_adhoc where lower(lower_email_address) = ?");
      
      int countChanged = 0;
      int countUnchanged = 0;
      
      if (actionAddEmailMappings) {
        for (String email : emailLinks.keySet()) {

          String pennId = emailLinks.get(email);

          boolean emailInDatabase = emailLowerToPennIdInDatabase.containsKey(email.toLowerCase());

          if (emailInDatabase) {
            
            String pennIdInDatabase = emailLowerToPennIdInDatabase.get(email.toLowerCase());
            if (StringUtils.equals(pennIdInDatabase, pennId)) {
              countUnchanged++;
              continue;
            } else {
              batchBindVarsDelete.add(GrouperUtil.toListObject(email.toLowerCase()));
            }
          }
          batchBindVarsAdd.add(GrouperUtil.toListObject(email.toLowerCase(), pennId));
          countChanged++;
        }
      }
      if (actionRemoveEmailMappings) {
        for (String email: emails) {
          boolean emailInDatabase = emailLowerToPennIdInDatabase.containsKey(email.toLowerCase());
          if (emailInDatabase) {
            countChanged++;
            batchBindVarsDelete.add(GrouperUtil.toListObject(email));
          } else {
            countUnchanged++;
          }
        }
      }

      if (countChanged > 0) {
        if (batchBindVarsDelete.size() > 0) {
          gcDbAccessDelete.executeBatchSql();
        }
        if (batchBindVarsAdd.size() > 0) {
          gcDbAccessAdd.executeBatchSql();
        }
        gsh_builtin_gshTemplateOutput.addOutputLine("Made " + countChanged + " changes");
      }
      if (countUnchanged > 0) {
        gsh_builtin_gshTemplateOutput.addOutputLine(countUnchanged + " records did not need changes");
      }
      gsh_builtin_gshTemplateOutput.addOutputLine("Running daemons...");
      List<String> jobNames = GrouperUtil.toList("OTHER_JOB_personSourceEmailLookup", "OTHER_JOB_personSourceEmailLookup2", "OTHER_JOB_personSourceEmailCopyFull");
      for (String jobName : jobNames) {
        GrouperLoader.runOnceByJobName(gsh_builtin_grouperSession, jobName, true);
      }
      boolean[] successes = new boolean[jobNames.size()];
      boolean overallSuccess = false;
      OUTER: for (int i=0;i<12;i++) {
        GrouperUtil.sleep(5000);
        for (int j=0;j<jobNames.size();j++) {
          if (!successes[j]) {
            successes[j] = 1 <= new GcDbAccess().sql("select count(1) from grouper_loader_log where job_name = '" + jobNames.get(j) + "' and status = 'SUCCESS' and started_time > ?").addBindVar(now).select(int.class);
          }
        }
        for (boolean success : successes) {
          if (!success) {
            continue OUTER;
          }
        }
        overallSuccess = true;
      }
      if (!overallSuccess) {
        gsh_builtin_gshTemplateOutput.addOutputLine("error", "Could not successfully run daemons...");
      }
    }
    if (actionRunReports) {
      
      
      gsh_builtin_gshTemplateOutput.addOutputLine("Running loader daemon which takes a few minutes...");
      
      GrouperLoader.runOnceByJobName(gsh_builtin_grouperSession, "OTHER_JOB_pennZoomLoader", true);
      
      for (int i=0;i<40;i++) {
        GrouperUtil.sleep(10000);
        if (1 <= new GcDbAccess().sql("select count(1) from grouper_loader_log where job_name = 'OTHER_JOB_pennZoomLoader' and status = 'SUCCESS' and started_time > ?").addBindVar(now).select(int.class)) {
          break;
        }
      }
      
      List<String> jobNames = GrouperUtil.toList("grouper_report_319a44d13d754dfea820571dd2709147_4659596c6b804041bb2b86a9aa2ecfb8",
          "grouper_report_319a44d13d754dfea820571dd2709147_6f406147cca7464d9dc75805c8d9b3bf",
          "grouper_report_319a44d13d754dfea820571dd2709147_8bca4a2525d4403baede757b10db2f5a", 
          "grouper_report_319a44d13d754dfea820571dd2709147_9ba8ca3a5f2b490088b2b008a4ed2bcd");
      for (String jobName : jobNames) {
        GrouperLoader.runOnceByJobName(gsh_builtin_grouperSession, jobName, true);
      }
      boolean[] successes = new boolean[jobNames.size()];

      boolean overallSuccess = false;
      OUTER: for (int i=0;i<12;i++) {
        GrouperUtil.sleep(5000);
        for (int j=0;j<jobNames.size();j++) {
          if (!successes[j]) {
            successes[j] = 1 <= new GcDbAccess().sql("select count(1) from grouper_loader_log where job_name = '" + jobNames.get(j) + "' and status = 'SUCCESS' and started_time > ?").addBindVar(now).select(int.class);
          }
        }
        for (boolean success : successes) {
          if (!success) {
            continue OUTER;
          }
        }
        overallSuccess = true;
      }
      if (!overallSuccess) {
        gsh_builtin_gshTemplateOutput.addOutputLine("error", "Could not successfully run daemons...");
      }

    }
    if (actionViewReports) {
      Stem stem = StemFinder.findByName(gsh_builtin_grouperSession, gsh_builtin_ownerStemName, true);
      gsh_builtin_gshTemplateOutput.addOutputLine("success", "Please wait while reports are retrieved");
      gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("operation=UiV2GrouperReport.viewReportConfigsOnFolder&stemId=" + stem.getId());
    }
    // done!
    gsh_builtin_gshTemplateOutput.addOutputLine("Finished running zoom deprovisioning template!");

//    }
//  
//  }
```
