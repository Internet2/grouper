---
title: "Grouper custom template via GSH report compare Banner security envs"
space: Grouper
pageId: 28548763
version: 5
lastUpdated: 2026-07-01T05:43:33.070Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548763/Grouper+custom+template+via+GSH+report+compare+Banner+security+envs
---

This [GSH template](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544720/Grouper+custom+template+via+GSH) compares Banner security among various environments

## Run the template

People in a certain group can run the template

Fill out the form

It will email you an HTML fixed-width report

| `Group report comparing to``'dev'``:`   ```- fot : missing groups: UP_FA_BATCH_USER_G, UP_FA_DIRECTOR_G`   ```- fot : extra groups: UP_AR_TEMP_BATCH_JOBS_G`   ```- sit1 : missing groups: UP_FA_BATCH_USER_G`   ```- sit2 : extra groups: UP_AR_TEMP_BATCH_JOBS_G`       `Membership report (``for` `groups in both envs) comparing to``'dev'``:`   ```- fot : UP_AR_ACCOUNTING_G : missing loginids: JSMITH`   ```- fot : UP_ST_SR_MGMT_G : extra loginids: QWRITE`   ```- sit1 : UP_AR_CALL_CENTER_G : missing loginids: ASMITH`   ```- sit1 : groups with correct mships : UP_AR_LOANS_G`   ```- sit2 : UP_AR_ACCOUNTING_G : missing loginids: JSMITH`       `Class report comparing to``'dev'``:`   ```- fot : missing object classes: UP_FA_BATCH_JOBS, UP_GN_JOBS_C`   ```- fot : extra object classes: UP_AR_BATCH_JOBS_C, UP_BCM_C`   ```- sit1 : missing object classes: UP_FAJOBS_REPORTS_C`   ```- sit2 : missing object classes: BAN_FINAID_C`       `Group/``class` `report (``for` `groups in both envs) comparing to``'dev'``:`   ```- fot : UP_AR_SA_MGMT_G : extra classes: UP_FA_SRQUERY_C, UP_AR_STANDARD_QUERY_C`   ```- fot : UP_AR_TEMP_BATCH_JOBS_G : missing classes: UP_AR_BATCH_JOBS_C`   ```- fot : groups with correct classes : UP_AR_ACCOUNTING_G, UP_AR_CALL_CENTER_G`   ```- sit1 : UP_FA_DIRECTOR_TEMP_G : extra classes: BAN_FINAID_C, UP_AR_BATCH_JOBS_C`   ```- sit1 : groups with correct classes : UP_AR_ACCOUNTING_G, UP_AR_CALL_CENTER_G`   ```- sit2 : UP_FA_COUNSELORS_G : extra classes: UP_FA_ADMISSIONS_VIEWING_C, UP_FA_BUDGET_MAINT_C`       `Object report comparing to``'dev'``:`   ```- fot : missing objects: GYCLADDR, GYCLEMRG`   ```- fot : extra objects: DA_BULK_API_USER, PF5000J`   ```- sit1 : missing objects: API_ADM_APPLICATION_INFLUENCE, API_APT_ASSESSMENT_SOURCES`   ```- sit1 : extra objects: API-JOBSUB-PRINT, API_FUND_CATEGORIES, BAN_AUDIT_USAGE`   ```- sit2 : missing objects: API_ADM_APPLICATION_INFLUENCE, API_APT_ASSESSMENT_SOURCES`   ```- sit2 : extra objects: PF5000J, TYRMTFX`       `Class/objectRole report (``for` `classes in both envs) comparing to``'dev'``:`   ```- fot : BAN_FINAID_C : extra objects: RERIM4E___M`   ```- fot : UP_AR_BATCH_JOBS_C : missing objects: GLBDATA___M, GLOLETT___M`   ```- fot : classes with correct object/role: UP_AR_ACCOUNTING_JOBS_RPTS_C, UP_AR_ACCT_DETL_MAINT_C`   ```- sit1 : BAN_ILP_API_C : extra objects: API_ACADEMIC_PERIODS___M, API_CROSSLISTED_SECTIONS___M`   ```- sit1 : BAN_STUDENT_C : extra objects: API_REGISTRATION___M, API_REGSTATUS___M`   ```- sit1 : classes with correct object/role: BAN_FINAID_C, UP_AR_ACCOUNTING_JOBS_RPTS_C`   ```- sit2 : BAN_GENERAL_C : extra objects: BANIAM2___M, COMMMGR_ROLE_OBJECT___CONNECT, EXTENDED_QUERY___M`   ```- sit2 : BAN_STUDENT_API_C : missing objects: API_ACADEMIC_CATALOGS___M, API_ACADEMIC_LEVELS___M`   ```- sit2 : classes with correct object/role: UP_AR_ACCOUNTING_JOBS_RPTS_C, UP_AR_ACCT_DETL_MAINT_C` |
| --- |

## Configure the template

## GSH script

```
import java.util.*;
import edu.internet2.middleware.grouper.cfg.*;
import edu.internet2.middleware.grouper.misc.*;
import edu.internet2.middleware.grouper.rules.*;
import edu.internet2.middleware.grouper.util.*;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouper.app.attestation.*;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.attr.assign.*;
import edu.internet2.middleware.grouper.attr.finder.*;
import edu.internet2.middleware.grouper.attr.value.*;
import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.subject.*;
import org.apache.commons.lang3.*;

////uncomment to compile in eclipse (and last line)
//public class Test3 {
//
//  public Test3() {
//  }
//
//  public static void main(String[] args) {
//    
//    GrouperStartup.startup();
//    
//    //input of project name, alphanumeric, start with lower.
//    String gsh_input_env = "sit1";
//    String gsh_input_compareEnvs = null; //"dev,sit2";
//    boolean gsh_input_compareGroups = true;
//    boolean gsh_input_compareMships = true;
//    boolean gsh_input_compareClasses = true;
//    boolean gsh_input_compareGroupClass = true;
//    boolean gsh_input_compareObjects = true;
//    boolean gsh_input_compareClassObject = true;
//    
//    GrouperSession gsh_builtin_grouperSession = GrouperSession.startRootSession();
//    Subject gsh_builtin_subject = SubjectFinder.findByIdentifierAndSource("mchyzer", "pennperson", true);
//    GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();
    
    // 1. build email as we do tasks
    StringBuilder emailBody = new StringBuilder("<pre>");
    Set<String> emailTos = new HashSet<String>();

    // 2. email to the user who performed this so they can support it
    String emailOfUserRunningTemplate = GrouperEmail.retrieveEmailAddress(gsh_builtin_subject);
    if (!StringUtils.isBlank(emailOfUserRunningTemplate)) {
      emailTos.add(emailOfUserRunningTemplate);
    } else {
      gsh_builtin_gshTemplateOutput.addValidationLine(
          "Error: Cannot find your email address.  Please edit it in the directory and try again an hour later");
    }

    // put envs in a map of strings from env to dblink
    Map<String, String> envToDbLink = new HashMap<String, String>();

    RETRIEVE_ENABLED: {
      // retrieve all enabled envs from database
      List<Object[]> rows = new GcDbAccess().connectionName("pennCommunity").sql("select env, db_link from penn_ngss_sec_comp where enabled = 'T'").selectList(Object[].class);
  
      for (Object[] row : rows) {
        envToDbLink.put((String)row[0], (String)row[1]);
      }
    }
    
    //validate the env is enabled and in the env table
    if (!envToDbLink.containsKey(gsh_input_env)) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_env",
          "Error: invalid env '" + gsh_input_env + "' is not an enabled env: " + GrouperUtil.join(envToDbLink.keySet().iterator(), ", "));
    }

    // are we only doing certain envs?
    if (!StringUtils.isBlank(gsh_input_compareEnvs) && !StringUtils.equals("null", gsh_input_compareEnvs)) {
      Set<String> compareEnvsSet = GrouperUtil.splitTrimToSet(gsh_input_compareEnvs, ",");
      for (String compareEnv : compareEnvsSet) {
        if (!envToDbLink.containsKey(compareEnv)) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_compareEnvs",
              "Error: invalid env '" + compareEnv + "' is not an enabled env: " + GrouperUtil.join(envToDbLink.keySet().iterator(), ", "));
        }
      }
      
      compareEnvsSet.add(gsh_input_env);
      envToDbLink.keySet().retainAll(compareEnvsSet);
    }

    // if anything not valid, stop
    if (GrouperUtil.length(gsh_builtin_gshTemplateOutput.getValidationLines()) > 0) {
      gsh_builtin_gshTemplateOutput.assignIsError(true);
      GrouperUtil.gshReturn();
    }
    
    // get the groups in all envs
    Map<String, Set<String>> envToGroups = new HashMap<String, Set<String>>();
    GET_GROUPS: {
      for (String env : envToDbLink.keySet()) {
        
        Set<String> groupNamesInEnv = new TreeSet<String>(new GcDbAccess().connectionName("pennCommunity")
            .sql("select distinct group_name from authz_ngss_sec_" + env + "_grp_v").selectList(String.class));
        
        envToGroups.put(env, groupNamesInEnv);
      }
      
      if (gsh_input_compareGroups) {
        emailBody.append("Group report comparing to '" + gsh_input_env + "':\n");
        
        // compare groups
        for (String env : new TreeSet<String>(envToDbLink.keySet())) {
          if (StringUtils.equals(env, gsh_input_env)) {
            continue;
          }
          
          Set<String> groupsInSelectedEnv = envToGroups.get(gsh_input_env);
          Set<String> groupsInCurrentEnv = envToGroups.get(env);
    
          // missing groups
          Set<String> groupsMissing = new TreeSet<String>(groupsInSelectedEnv);
          groupsMissing.removeAll(groupsInCurrentEnv);
          
          // extra groups
          Set<String> groupsExtra = new TreeSet<String>(groupsInCurrentEnv);
          groupsExtra.removeAll(groupsInSelectedEnv);
          
          // report in the email
          if (groupsMissing.size() == 0 && groupsExtra.size() == 0) {
            emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": has the same groups\n");
          } else {
            if (groupsMissing.size() > 0) {
              emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": missing groups: " + GrouperUtil.join(groupsMissing.iterator(), ", ") + "\n");
            }
            if (groupsExtra.size() > 0) {
              emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": extra groups:   " + GrouperUtil.join(groupsExtra.iterator(), ", ") + "\n");
            }
          }
          
        }
      }
    }
    
    if (gsh_input_compareMships) {
      // get the memberships in all envs
      Map<String, Map<String, Set<String>>> envToGroupToLoginid = new HashMap<String, Map<String, Set<String>>>();
      for (String env : envToDbLink.keySet()) {

        // map of groups to members for this env
        Map<String, Set<String>> groupToLoginidsForThisEnv = new HashMap<String, Set<String>>();
        envToGroupToLoginid.put(env, groupToLoginidsForThisEnv);
        
        // init the maps from the query above
        Set<String> allGroupsInEnv = envToGroups.get(env);
        if (GrouperUtil.length(allGroupsInEnv) == 0) {
          // there are no groups in this env...
          continue;
        }

        List<Object[]> rows = new GcDbAccess().connectionName("pennCommunity")
            .sql("select distinct group_name, loginid from authz_ngss_sec_" + env + "_mem_v").selectList(Object[].class);

        // loop through membership data
        for (Object[] row : rows) {
          
          String groupName = (String)row[0];
          String loginid = (String)row[1];
          
          Set<String> loginids = groupToLoginidsForThisEnv.get(groupName);
          if (loginids == null) {
            // init the list if not there
            loginids = new TreeSet<String>();
            groupToLoginidsForThisEnv.put(groupName, loginids);
          }
          
          loginids.add(loginid);
          
        }
      
      }
      
      emailBody.append("\nMembership report (for groups in both envs) comparing to '" + gsh_input_env + "':\n");
      
      // compare memberships
      for (String env : new TreeSet<String>(envToDbLink.keySet())) {
        if (StringUtils.equals(env, gsh_input_env)) {
          continue;
        }
        
        Map<String, Set<String>> membershipsInSelectedEnv = envToGroupToLoginid.get(gsh_input_env);
        Map<String, Set<String>> membershipsInCurrentEnv = envToGroupToLoginid.get(env);

        Set<String> groupsCorrect = new TreeSet<String>();

        // loop through groups in current env
        for (String groupName : new TreeSet<String>(membershipsInCurrentEnv.keySet())) {
          
          // skip this if its an extra group
          if (!envToGroups.get(env).contains(groupName)) {
            continue;
          }
          
          Set<String> loginIdsInSelectedEnv = GrouperUtil.nonNull((Set)membershipsInSelectedEnv.get(groupName));
          Set<String> loginIdsInCurrentEnv = GrouperUtil.nonNull((Set)membershipsInCurrentEnv.get(groupName));
          
          // missing loginids for group
          Set<String> loginidsMissing = new TreeSet<String>(loginIdsInSelectedEnv);
          loginidsMissing.removeAll(loginIdsInCurrentEnv);
          
          // extra groups
          Set<String> loginidsExtra = new TreeSet<String>(loginIdsInCurrentEnv);
          loginidsExtra.removeAll(loginIdsInSelectedEnv);
          
          // report in the email
          if (loginidsMissing.size() == 0 && loginidsExtra.size() == 0) {
            groupsCorrect.add(groupName);
          } else {
            if (loginidsMissing.size() > 0) {
              emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": " + StringUtils.rightPad(groupName, 32) + ": missing loginids: " + GrouperUtil.join(loginidsMissing.iterator(), ", ") + "\n");
            }
            if (loginidsExtra.size() > 0) {
              emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": " + StringUtils.rightPad(groupName, 32) + ": extra loginids:   " + GrouperUtil.join(loginidsExtra.iterator(), ", ") + "\n");
            }
          }
          
        }
        
        if (groupsCorrect.size() > 0) {
          emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": " + StringUtils.rightPad("groups with correct mships", 32) + ": " + GrouperUtil.join(groupsCorrect.iterator(), ", ") + "\n");
        }
        
      }
    }
    
    // get the classes in all envs
    Map<String, Set<String>> envToClass = new HashMap<String, Set<String>>();
    GET_CLASSES: {
      for (String env : envToDbLink.keySet()) {
        
        Set<String> groupNamesInEnv = new TreeSet<String>(new GcDbAccess().connectionName("pennCommunity")
            .sql("select distinct class_name from authz_ngss_sec_" + env + "_cls_v").selectList(String.class));
        
        envToClass.put(env, groupNamesInEnv);
      }
      
      if (gsh_input_compareClasses) {
        emailBody.append("\nClass report comparing to '" + gsh_input_env + "':\n");
        
        // compare classes
        for (String env : new TreeSet<String>(envToDbLink.keySet())) {
          if (StringUtils.equals(env, gsh_input_env)) {
            continue;
          }
          
          Set<String> classesInSelectedEnv = envToClass.get(gsh_input_env);
          Set<String> classesInCurrentEnv = envToClass.get(env);
    
          // missing classes
          Set<String> classesMissing = new TreeSet<String>(classesInSelectedEnv);
          classesMissing.removeAll(classesInCurrentEnv);
          
          // extra classes
          Set<String> classesExtra = new TreeSet<String>(classesInCurrentEnv);
          classesExtra.removeAll(classesInSelectedEnv);
          
          // report in the email
          if (classesMissing.size() == 0 && classesExtra.size() == 0) {
            emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": has the same object classes\n");
          } else {
            if (classesMissing.size() > 0) {
              emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": missing object classes: " + GrouperUtil.join(classesMissing.iterator(), ", ") + "\n");
            }
            if (classesExtra.size() > 0) {
              emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": extra object classes:   " + GrouperUtil.join(classesExtra.iterator(), ", ") + "\n");
            }
          }
        }
      }
    }

    if (gsh_input_compareGroupClass) {
      // get the groupClass in all envs
      Map<String, Map<String, Set<String>>> envToGroupToClass = new HashMap<String, Map<String, Set<String>>>();
      for (String env : envToDbLink.keySet()) {

        // map of groups to class for this env
        Map<String, Set<String>> groupToClassForThisEnv = new HashMap<String, Set<String>>();
        envToGroupToClass.put(env, groupToClassForThisEnv);
        
        // init the maps from the query above
        Set<String> allGroupInEnv = envToGroups.get(env);
        if (GrouperUtil.length(allGroupInEnv) == 0) {
          // there are no groups in this env...
          continue;
        }

        List<Object[]> rows = new GcDbAccess().connectionName("pennCommunity")
            .sql("select distinct group_name, class_name from authz_ngss_sec_" + env + "_grc_v").selectList(Object[].class);

        // loop through groupclass data
        for (Object[] row : rows) {
          
          String groupName = (String)row[0];
          String className = (String)row[1];
          
          Set<String> classes = groupToClassForThisEnv.get(groupName);
          if (classes == null) {
            // init the list if not there
            classes = new TreeSet<String>();
            groupToClassForThisEnv.put(groupName, classes);
          }
          
          classes.add(className);
          
        }
      
      }
      
      emailBody.append("\nGroup/class report (for groups in both envs) comparing to '" + gsh_input_env + "':\n");
      
      // compare groupClass
      for (String env : new TreeSet<String>(envToDbLink.keySet())) {
        if (StringUtils.equals(env, gsh_input_env)) {
          continue;
        }
        
        Map<String, Set<String>> groupClassInSelectedEnv = envToGroupToClass.get(gsh_input_env);
        Map<String, Set<String>> groupClassInCurrentEnv = envToGroupToClass.get(env);

        Set<String> groupsCorrect = new TreeSet<String>();

        // loop through groups in current env
        for (String groupName : new TreeSet<String>(groupClassInCurrentEnv.keySet())) {
          
          // skip this if its an extra group
          if (!envToGroups.get(env).contains(groupName)) {
            continue;
          }
          
          Set<String> classesInSelectedEnv = GrouperUtil.nonNull((Set)groupClassInSelectedEnv.get(groupName));
          Set<String> classesInCurrentEnv = GrouperUtil.nonNull((Set)groupClassInCurrentEnv.get(groupName));
          
          // missing classes for group
          Set<String> classesMissing = new TreeSet<String>(classesInSelectedEnv);
          classesMissing.removeAll(classesInCurrentEnv);
          
          // extra classes
          Set<String> classesExtra = new TreeSet<String>(classesInCurrentEnv);
          classesExtra.removeAll(classesInSelectedEnv);
          
          // report in the email
          if (classesMissing.size() == 0 && classesExtra.size() == 0) {
            groupsCorrect.add(groupName);
          } else {
            if (classesMissing.size() > 0) {
              emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": " + StringUtils.rightPad(groupName, 32) + ": missing classes: " + GrouperUtil.join(classesMissing.iterator(), ", ") + "\n");
            }
            if (classesExtra.size() > 0) {
              emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": " + StringUtils.rightPad(groupName, 32) + ": extra classes:   " + GrouperUtil.join(classesExtra.iterator(), ", ") + "\n");
            }
          }
          
        }
        
        if (groupsCorrect.size() > 0) {
          emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": " + StringUtils.rightPad("groups with correct classes", 32) + ": " + GrouperUtil.join(groupsCorrect.iterator(), ", ") + "\n");
        }
        
      }
    }
    // get the objects in all envs
    Map<String, Set<String>> envToObjects = new HashMap<String, Set<String>>();
    GET_OBJECTS: {
      for (String env : envToDbLink.keySet()) {
        
        Set<String> objectNamesInEnv = new TreeSet<String>(new GcDbAccess().connectionName("pennCommunity")
            .sql("select distinct object_name from authz_ngss_sec_" + env + "_obj_v").selectList(String.class));
        
        envToObjects.put(env, objectNamesInEnv);
      }
      
      if (gsh_input_compareObjects) {
        emailBody.append("\nObject report comparing to '" + gsh_input_env + "':\n");
        
        // compare objects
        for (String env : new TreeSet<String>(envToDbLink.keySet())) {
          if (StringUtils.equals(env, gsh_input_env)) {
            continue;
          }
          
          Set<String> objectsInSelectedEnv = envToObjects.get(gsh_input_env);
          Set<String> objectsInCurrentEnv = envToObjects.get(env);
    
          // missing objects
          Set<String> objectsMissing = new TreeSet<String>(objectsInSelectedEnv);
          objectsMissing.removeAll(objectsInCurrentEnv);
          
          // extra objects
          Set<String> objectsExtra = new TreeSet<String>(objectsInCurrentEnv);
          objectsExtra.removeAll(objectsInSelectedEnv);
          
          // report in the email
          if (objectsMissing.size() == 0 && objectsExtra.size() == 0) {
            emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": has the same objects\n");
          } else {
            if (objectsMissing.size() > 0) {
              emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": missing objects: " + GrouperUtil.join(objectsMissing.iterator(), ", ") + "\n");
            }
            if (objectsExtra.size() > 0) {
              emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": extra objects:   " + GrouperUtil.join(objectsExtra.iterator(), ", ") + "\n");
            }
          }
          
        }
      }
    }
    
    if (gsh_input_compareClassObject) {
      // get the classObject in all envs
      Map<String, Map<String, Set<String>>> envToClassToObject = new HashMap<String, Map<String, Set<String>>>();
      for (String env : envToDbLink.keySet()) {

        // map of class to object for this env
        Map<String, Set<String>> classToObjectForThisEnv = new HashMap<String, Set<String>>();
        envToClassToObject.put(env, classToObjectForThisEnv);
        
        // init the maps from the query above
        Set<String> allClassInEnv = envToClass.get(env);
        if (GrouperUtil.length(allClassInEnv) == 0) {
          // there are no classes in this env...
          continue;
        }

        List<Object[]> rows = new GcDbAccess().connectionName("pennCommunity")
            .sql("select distinct class_name, object_role from authz_ngss_sec_" + env + "_clo_v").selectList(Object[].class);

        // loop through classobject data
        for (Object[] row : rows) {
          
          String className = (String)row[0];
          String objectRole = (String)row[1];
          
          Set<String> objectRoles = classToObjectForThisEnv.get(className);
          if (objectRoles == null) {
            // init the list if not there
            objectRoles = new TreeSet<String>();
            classToObjectForThisEnv.put(className, objectRoles);
          }
          
          objectRoles.add(objectRole);
          
        }
      
      }
      
      emailBody.append("\nClass/objectRole report (for classes in both envs) comparing to '" + gsh_input_env + "':\n");
      
      // compare groupClass
      for (String env : new TreeSet<String>(envToDbLink.keySet())) {
        if (StringUtils.equals(env, gsh_input_env)) {
          continue;
        }
        
        Map<String, Set<String>> classObjectInSelectedEnv = envToClassToObject.get(gsh_input_env);
        Map<String, Set<String>> classObjectInCurrentEnv = envToClassToObject.get(env);

        Set<String> classesCorrect = new TreeSet<String>();

        // loop through groups in current env
        for (String className : new TreeSet<String>(classObjectInCurrentEnv.keySet())) {
          
          // skip this if its an extra class
          if (!envToClass.get(env).contains(className)) {
            continue;
          }
          
          Set<String> objectRolesInSelectedEnv = GrouperUtil.nonNull((Set)classObjectInSelectedEnv.get(className));
          Set<String> objectRolesInCurrentEnv = GrouperUtil.nonNull((Set)classObjectInCurrentEnv.get(className));
          
          // missing objects for class
          Set<String> objectRolesMissing = new TreeSet<String>(objectRolesInSelectedEnv);
          objectRolesMissing.removeAll(objectRolesInCurrentEnv);
          
          // extra classes
          Set<String> objectRolesExtra = new TreeSet<String>(objectRolesInCurrentEnv);
          objectRolesExtra.removeAll(objectRolesInSelectedEnv);
          
          // report in the email
          if (objectRolesMissing.size() == 0 && objectRolesExtra.size() == 0) {
            classesCorrect.add(className);
          } else {
            if (objectRolesMissing.size() > 0) {
              emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": " + StringUtils.rightPad(className, 32) + ": missing objects: " + GrouperUtil.join(objectRolesMissing.iterator(), ", ") + "\n");
            }
            if (objectRolesExtra.size() > 0) {
              emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": " + StringUtils.rightPad(className, 32) + ": extra objects:   " + GrouperUtil.join(objectRolesExtra.iterator(), ", ") + "\n");
            }
          }
          
        }
        
        if (classesCorrect.size() > 0) {
          emailBody.append(" - " + StringUtils.rightPad(env, 6) + ": " + StringUtils.rightPad("classes with correct object/role", 32) + ":                  " + GrouperUtil.join(classesCorrect.iterator(), ", ") + "\n");
        }
        
      }
    }

    emailBody.append("</pre>");

    // send email
    String emailTo = StringUtils.join(emailTos.iterator(), ",");
    new GrouperEmail().setTo(emailTo).setSubject("NGSS security report for env " + gsh_input_env).setBody(emailBody.toString()).send();
    gsh_builtin_gshTemplateOutput.addOutputLine("Sent email report to: " + emailTo);
    
    // done!
    gsh_builtin_gshTemplateOutput.addOutputLine("Finished running NGSS security report for env: " + gsh_input_env);

//  // Uncomment to run in java
//  System.out.println(emailBody);
//  
//  }
//
//}
```

## Make the views / dblinks

This is beyond the scope of this wiki at this time, but it will be updated sometime soon.

There is a table of envs with the dblinks

There is a view for each env for each type of comparison that uses the db link. This will be automated in the future with another template to add/remove/disable/enabled the envs
