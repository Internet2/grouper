---
title: "Grouper custom template via GSH impersonate testing helper"
space: Grouper
pageId: 28548123
version: 4
lastUpdated: 2026-07-01T05:45:15.682Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548123/Grouper+custom+template+via+GSH+impersonate+testing+helper
---

This template helps test a feed from IDM to Banner. This is an example of running queries.

## Sync data task

Copy data from one database to another so it can easily be joined/queried/tested

## Print state of person on both sides of integration

## Change a netId

## Find example random records

Note, this joins data on both sides so the "syncData" option must have been run recently

## Configuration

## Script

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

//uncomment to compile in eclipse (and last line)
//public class Test13 {

  /**
   * select data from penncomm based on one input
   * @param pennId
   * @param pennKey
   * @param bannerGuid
   * @return pennid, pennkey, and banner guid
   */
  public static Object[] selectPersonFromPennComm(String pennId, String pennKey, String bannerGuid) {
    // what kind of identifier
    // if pennid
    // select m.penn_id, m.kerberos_principal pennkey, m.BANNER_GUID from comadmin.member m
    String pennCommSql = "select m.char_penn_id, m.kerberos_principal pennkey, m.BANNER_GUID from comadmin.member m where ";
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName("pennCommunityTest");
    
    boolean selectFromPennComm = false;
    if (!StringUtils.isBlank(pennId)) {
      pennCommSql += " m.penn_id = ?";
      gcDbAccess.addBindVar(pennId);
      selectFromPennComm = true;
    } else if (!StringUtils.isBlank(pennKey)) {
      pennCommSql += " m.kerberos_principal = ?";
      gcDbAccess.addBindVar(pennKey);
      selectFromPennComm = true;
    } else if (!StringUtils.isBlank(bannerGuid)) {
      pennCommSql += " m.BANNER_GUID = ?";
      gcDbAccess.addBindVar(bannerGuid);
      selectFromPennComm = true;
    }
    
    String pennCommPennId = null;
    String pennCommPennKey = null;
    String pennCommBannerGuid = null;
    Object[] row = null;
    if (selectFromPennComm) {
      row = gcDbAccess.sql(pennCommSql).select(Object[].class);
    }
    return row;
  }

  /**
   * select data from banner based on one input
   * @param pidm
   * @param pennId
   * @param pennKey
   * @param bannerGuid
   * @return pennid, pennkey, and banner guid
   */
  public static Object[] selectPersonFromBanner(String dbLinkPennantStudent, Integer pidm, String pennId, String pennKey, String bannerGuid) {

    String bannerSql = "select S.SPRIDEN_PIDM , S.SPRIDEN_ID, G.GOBUMAP_UDC_ID, GID.GORADID_ADDITIONAL_ID, ";
    bannerSql += " case when exists (SELECT 1 FROM SGBSTDN@" + dbLinkPennantStudent + " WHERE SGBSTDN_PIDM = SPRIDEN_PIDM) then 'T' else 'F' end as student ";
    bannerSql += " from spriden@" + dbLinkPennantStudent + " s, GOBUMAP@" + dbLinkPennantStudent + " g, GORADID@" + dbLinkPennantStudent + " gid ";
    bannerSql += " where SPRIDEN_CHANGE_IND is null and spriden_id is not null and S.SPRIDEN_PIDM = G.GOBUMAP_PIDM(+) ";
    bannerSql += " and S.SPRIDEN_PIDM = GID.GORADID_PIDM(+) and GID.GORADID_ADID_CODE(+) = 'PKEY' and REGEXP_LIKE(s.spriden_id, '^[0-9]{8}" + '$' + "') ";
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName("pennCommunityTest");
    boolean selectFromBanner = false;
    if (pidm != null) {
      
      bannerSql += "and S.SPRIDEN_PIDM = ? ";
      gcDbAccess.addBindVar(pidm);
      selectFromBanner = true;
    } else if (!StringUtils.isBlank(pennId)) {
      bannerSql += "and S.SPRIDEN_ID = ? ";
      gcDbAccess.addBindVar(pennId);
      selectFromBanner = true;
      
    } else if (!StringUtils.isBlank(pennKey)) {
  
      bannerSql += "and GID.GORADID_ADDITIONAL_ID = ? ";
      gcDbAccess.addBindVar(pennKey);
      selectFromBanner = true;
    
    } else if (!StringUtils.isBlank(bannerGuid)) {
  
      bannerSql += "and G.GOBUMAP_UDC_ID = ? ";
      gcDbAccess.addBindVar(bannerGuid);
      selectFromBanner = true;
  
    }
    Object[] row = null;
    if (selectFromBanner) {
      row = gcDbAccess.sql(bannerSql).select(Object[].class);
    }
    return row;
  }
  
  public static void printPerson(GshTemplateOutput gsh_builtin_gshTemplateOutput,
      String pennCommPennId, String pennCommPennKey, String pennCommBannerGuid,
      Integer bannerPidm, String bannerPennId, String bannerPennKey,
      String bannerBannerGuid, Boolean bannerIsStudent) {
    GcDbAccess gcDbAccess;
    // print results
    gsh_builtin_gshTemplateOutput.addOutputLine("Penn community: Penn ID: " + pennCommPennId + ", PennKey: " + pennCommPennKey + ", Banner GUID: " + pennCommBannerGuid);
    gsh_builtin_gshTemplateOutput.addOutputLine("Banner: Penn ID: " + bannerPennId + ", PennKey: " + bannerPennKey + ", Banner GUID: " + bannerBannerGuid + ", pidm: " + bannerPidm + ", student? " + bannerIsStudent);

    boolean pennIdMatch = StringUtils.equals(StringUtils.trimToEmpty(pennCommPennId), StringUtils.trimToEmpty(bannerPennId));
    boolean pennKeyMatch = StringUtils.equals(StringUtils.trimToEmpty(pennCommPennKey), StringUtils.trimToEmpty(bannerPennKey));
    boolean bannerGuidMatch = StringUtils.equals(StringUtils.trimToEmpty(pennCommBannerGuid), StringUtils.trimToEmpty(bannerBannerGuid));
    
    gsh_builtin_gshTemplateOutput.addOutputLine("Identity (overall) match: " + (pennIdMatch && pennKeyMatch && bannerGuidMatch) + ", Penn ID match: " + pennIdMatch + ", PennKey match: " + pennKeyMatch + ", Banner GUID match: " + bannerGuidMatch);

    // get emails
    if (!StringUtils.isBlank(pennCommPennId)) {
      gcDbAccess = new GcDbAccess().connectionName("pennCommunityTest");
      List<Object[]> rows = gcDbAccess.sql("select PENN_ID, EMAIL_ADDRESS, SOURCE, IS_PREFERRED, LAST_UPDATED from comadmin.TEMP_NGSS_STU_ID_FULL_V_emails where penn_id = ? order by 3, 2").addBindVar(pennCommPennId).selectList(Object[].class);
      for (Object[] theRow : rows) {
        gsh_builtin_gshTemplateOutput.addOutputLine("Penn community email: Penn ID: " + GrouperUtil.stringValue(theRow[0]) + ", email: " + GrouperUtil.stringValue(theRow[1]) + ", source: " + GrouperUtil.stringValue(theRow[2]) + ", pref: " + GrouperUtil.stringValue(theRow[3]) + ", last updated: " + GrouperUtil.stringValue(theRow[4]));
      }
    }

    // get emails
    if (bannerPidm != null) {
      gcDbAccess = new GcDbAccess().connectionName("pennCommunityTest");
      String bannerEmailSql = "select goremal_pidm as pidm, s.spriden_id as char_penn_id, ";
      bannerEmailSql += "REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE( ";
      bannerEmailSql += "REGEXP_REPLACE(REGEXP_REPLACE(REGEXP_REPLACE(goremal_email_address, '^(.*)\\.XXX\\.XX\\.[0-9]+" + '$' + "','\\1'), '^(.*)\\.XX\\.[0-9]+" + '$' + "','\\1') ";
      bannerEmailSql += ", '^(.*)\\.YYY\\.XXX" + '$' + "','\\1'), '^(.*)\\.yyy\\.xxx" + '$' + "','\\1'), '^(.*)\\.XXX" + '$' + "','\\1'), '^(.*)\\.XXXX" + '$' + "','\\1'), '^(.*)XXX" + '$' + "','\\1') as email_Address_real, ";
      bannerEmailSql += "goremal_email_address as email_Address_obfuscated, ";
      bannerEmailSql += "goremal_emal_code as emal_code, goremal_preferred_ind as preferred, goremal_status_ind as status ";
      bannerEmailSql += "from goremal@PENNANT_STUDENT_BANfun.WORLD, spriden@PENNANT_STUDENT_BANfun.WORLD s where goremal_pidm = spriden_pidm and S.SPRIDEN_CHANGE_IND is null ";
      bannerEmailSql += "and REGEXP_LIKE(s.spriden_id, '^[0-9]{8}" + '$' + "') and goremal_pidm = ?";
      List<Object[]> rows = gcDbAccess.sql(bannerEmailSql).addBindVar(bannerPidm).selectList(Object[].class);
      for (Object[] theRow : rows) {
        gsh_builtin_gshTemplateOutput.addOutputLine("Banner email: Pidm: " + GrouperUtil.stringValue(theRow[0]) + ", email: " + GrouperUtil.stringValue(theRow[1]) + ", email obfuscated: " + GrouperUtil.stringValue(theRow[2]) + ", email code: " + GrouperUtil.stringValue(theRow[3]) + ", pref: " + GrouperUtil.stringValue(theRow[4]) + ", status: " + GrouperUtil.stringValue(theRow[5]));
      }
    }
  }
  
  public static void displayPerson(GshTemplateOutput gsh_builtin_gshTemplateOutput,
      Map<String, String> envToDbLink, String gsh_input_env, String gsh_input_pennId,
      String gsh_input_bannerGuid, String gsh_input_pennKey, Integer gsh_input_pidm) {
    String dbLink = envToDbLink.get(gsh_input_env);
    String dbLinkPennantStudent = dbLink.toLowerCase().replace("authzadm", "pennant_student");
    
    //  String gsh_input_env = "fot";
    //  String gsh_input_pennId = "10021368";
    //  String gsh_input_bannerGuid = "abc123";
    //  Integer gsh_input_pidm = 1234;
    //  String gsh_input_pennKey = "mchyzer";

    // what kind of identifier
    // if pennid
    // select m.penn_id, m.kerberos_principal pennkey, m.BANNER_GUID from comadmin.member m
    String pennCommSql = "select m.char_penn_id, m.kerberos_principal pennkey, m.BANNER_GUID from comadmin.member m where ";
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName("pennCommunityTest");
    
    Object[] row = selectPersonFromPennComm(gsh_input_pennId, gsh_input_pennKey, gsh_input_bannerGuid);
    
    String pennCommPennId = null;
    String pennCommPennKey = null;
    String pennCommBannerGuid = null;
    boolean selectFromPennComm = false;
    if (row != null) {
      
      selectFromPennComm = true;
      pennCommPennId = GrouperUtil.stringValue(row[0]);
      pennCommPennKey = GrouperUtil.stringValue(row[1]);
      pennCommBannerGuid = GrouperUtil.stringValue(row[2]);
      
    }

    
    // get the banner side
    Integer bannerPidm = null;
    String bannerPennId = null;
    String bannerPennKey = null;
    String bannerBannerGuid = null;
    Boolean bannerIsStudent = null;
    row = selectPersonFromBanner(dbLinkPennantStudent, gsh_input_pidm, gsh_input_pennId, gsh_input_pennKey, gsh_input_bannerGuid);
    boolean selectFromBanner = row != null;

    if (!selectFromBanner) {
      row = selectPersonFromBanner(dbLinkPennantStudent, null, pennCommPennId, pennCommPennKey, pennCommBannerGuid);
      selectFromBanner = row != null;
    }
    
    if (selectFromBanner) {
      // S.SPRIDEN_PIDM , S.SPRIDEN_ID, G.GOBUMAP_UDC_ID, GID.GORADID_ADDITIONAL_ID
      bannerPidm = GrouperUtil.intObjectValue(row[0], false);
      bannerPennId = GrouperUtil.stringValue(row[1]);
      bannerBannerGuid = GrouperUtil.stringValue(row[2]);
      bannerPennKey = GrouperUtil.stringValue(row[3]);
      bannerIsStudent = GrouperUtil.booleanValue(row[4]);
    }
    
    printPerson(gsh_builtin_gshTemplateOutput, pennCommPennId, pennCommPennKey,
        pennCommBannerGuid, bannerPidm, bannerPennId, bannerPennKey, bannerBannerGuid,
        bannerIsStudent);
  }

  public static void findRandomPennId(GshTemplateOutput gsh_builtin_gshTemplateOutput,
      String gsh_input_env, Map<String, String> envToDbLink, String sql) {
    //lets see how many
    int count = new GcDbAccess().connectionName("pennCommunityTest").sql("select count(1) from " + sql).select(int.class);        
    
    if (count == 0) {
      gsh_builtin_gshTemplateOutput.addOutputLine("No records found!");
    } else {
      int rowNum = Math.max(1, (int)(Math.random() * (double)count));
      String pennId = new GcDbAccess().connectionName("pennCommunityTest").sql("select penn_id from " + sql + " where row_number = ?").addBindVar(rowNum).select(String.class);        
      displayPerson(gsh_builtin_gshTemplateOutput, envToDbLink, gsh_input_env,  pennId, null, null, null);

    }
  }

  // public static void main(String[] args) {
    
   // GrouperStartup.startup();
    
   // //syncData, changePennKey, displayState, findPerson
   // String gsh_input_action = "displayState";
   // String gsh_input_env = "fot";
   // String gsh_input_pennId = "10021368";
   // String gsh_input_bannerGuid = "abc123";
   // String gsh_input_oldPennKey = "mchyzer";
   // String gsh_input_newPennKey = "mchyzer1";
   // Integer gsh_input_pidm = 1234;
   // String gsh_input_pennKey = "mchyzer";
    // personNotInBanner, personInBannerWithoutPennkey,  personInBannerWithPennkey, personInBannerIsStudent, personInBannerNotStudent

   // String gsh_input_dataType = "personNotInBanner";
    
   // GrouperSession gsh_builtin_grouperSession = GrouperSession.startRootSession();
   // Subject gsh_builtin_subject = SubjectFinder.findByIdentifierAndSource("mchyzer", "pennperson", true);
   // GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();
   
    // create table TEMP_banner_STU_ID_FULL as select * from TEMP_NGSS_STU_ID_FULL_v where 1=0;

    // CREATE TABLE TEMP_BANNER_STU_ID_FULL ( PENN_ID NUMBER(8) NOT NULL, KERBEROS_PRINCIPAL VARCHAR2(24 CHAR), BANNER_GUID VARCHAR2(225 CHAR) NOT NULL, LAST_UPDATE DATE );

    // grant select on TEMP_BANNER_STU_ID_FULL to ngss_readonly with grant option;
    
    // create table TEMP_banner_STU_ID_FULL_EMAILS as select * from TEMP_NGSS_STU_ID_FULL_V_EMAILS where 1=0;

    // CREATE TABLE TEMP_BANNER_STU_ID_FULL_EMAILS ( PENN_ID NUMBER ), EMAIL_ADDRESS VARCHAR2(100 CHAR), SOURCE VARCHAR2(25 CHAR), IS_PREFERRED VARCHAR2(1 CHAR), LAST_UPDATED TIMESTAMP(6) );

    // grant select on TEMP_BANNER_STU_ID_FULL_EMAILS to ngss_readonly with grant option;

    gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("NONE");
   
    // 1. convert action to boolean
    boolean actionSyncData = StringUtils.equals(gsh_input_action, "syncData");
    boolean actionChangePennKey = StringUtils.equals(gsh_input_action, "changePennKey");
    boolean actionDisplayState = StringUtils.equals(gsh_input_action, "displayState");
    boolean actionFindPerson = StringUtils.equals(gsh_input_action, "findPerson");

    // 2. must have an action we expect
    if (!actionSyncData && !actionChangePennKey && !actionDisplayState && !actionFindPerson) {

      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_action",
          "Error: Action cannot be found '" + gsh_input_action + "'!");

    }

    if (actionChangePennKey && StringUtils.isBlank(gsh_input_pennId)) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_pennId",
          "Error: Penn ID is required when changing PennKey!");
    
    }

    if (actionChangePennKey && StringUtils.isBlank(gsh_input_bannerGuid)) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_bannerGuid",
          "Error: Banner GUID is required when changing PennKey!");
    }

    if (actionChangePennKey && StringUtils.isBlank(gsh_input_newPennKey) && StringUtils.isBlank(gsh_input_oldPennKey)) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_newPennKey",
          "Error: New PennKey or old PennKey is required to change PennKey");
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_oldPennKey",
          "Error: New PennKey or old PennKey is required to change PennKey");
    }

    if (actionDisplayState) {
      
      // we need one identifier
      int identifierCount = 0;
      identifierCount += !StringUtils.isBlank(gsh_input_pennId) ? 1 : 0;
      identifierCount += !StringUtils.isBlank(gsh_input_pennKey) ? 1 : 0;
      identifierCount += gsh_input_pidm != null ? 1 : 0;
      identifierCount += !StringUtils.isBlank(gsh_input_bannerGuid) ? 1 : 0;
      
      if (identifierCount != 1) {
        gsh_builtin_gshTemplateOutput.addValidationLine(
            "Error: Enter one and only one identifier: " + identifierCount + ", " + gsh_input_pennId + ", " + gsh_input_pennKey + ", " + gsh_input_pidm + ", " + gsh_input_bannerGuid);
      }
      
    }
    
    
    // 3. get enabled envs, put envs in a map of strings from env to dblink
    Map<String, String> envToDbLink = new HashMap<String, String>();

    RETRIEVE_ENABLED: {
      // retrieve all enabled envs from database
      List<Object[]> rows = new GcDbAccess().connectionName("pennCommunityTest").sql("select env, db_link from penn_ngss_sec_comp where enabled = 'T'").selectList(Object[].class);
  
      for (Object[] row : rows) {
        envToDbLink.put((String)row[0], (String)row[1]);
      }
    }
    
    // 4. validate the env is enabled and in the env table
    if ((actionSyncData || actionDisplayState || actionFindPerson) && !envToDbLink.containsKey(gsh_input_env)) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_env",
          "Error: invalid env '" + gsh_input_env + "' is not an enabled env: " + GrouperUtil.join(envToDbLink.keySet().iterator(), ", "));
    }

    // if anything not valid, stop
    if (GrouperUtil.length(gsh_builtin_gshTemplateOutput.getValidationLines()) > 0) {
      gsh_builtin_gshTemplateOutput.assignIsError(true);
      GrouperUtil.gshReturn();
    }

    if (actionSyncData) {
      String dbLink = envToDbLink.get(gsh_input_env);
      // run some queries
      int rows = new GcDbAccess().connectionName("pennCommunityTest").sql("delete from TEMP_BANNER_STU_ID_FULL@" + dbLink).executeSql();
      
      gsh_builtin_gshTemplateOutput.addOutputLine("Deleted " + rows + " from authzadm.TEMP_BANNER_STU_ID_FULL@" + dbLink);
      
      
      String theSql = "insert into TEMP_BANNER_STU_ID_FULL@" + dbLink + " (PENN_ID, KERBEROS_PRINCIPAL, BANNER_GUID, LAST_UPDATE) ";
      theSql += "(select char_PENN_ID, KERBEROS_PRINCIPAL, BANNER_GUID, LAST_UPDATE from comadmin.TEMP_NGSS_STU_ID_FULL_V)";
      rows = new GcDbAccess().connectionName("pennCommunityTest").sql(theSql).executeSql();

      gsh_builtin_gshTemplateOutput.addOutputLine("Inserted " + rows + " into authzadm.TEMP_BANNER_STU_ID_FULL@" + dbLink);

      rows = new GcDbAccess().connectionName("pennCommunityTest").sql("delete from TEMP_BANNER_STU_ID_FULL_EMAILS@" + dbLink).executeSql();
      
      gsh_builtin_gshTemplateOutput.addOutputLine("Deleted " + rows + " into authzadm.TEMP_BANNER_STU_ID_FULL_EMAILS@" + dbLink);

      theSql = "insert into TEMP_BANNER_STU_ID_FULL_EMAILS@" + dbLink + " (PENN_ID, EMAIL_ADDRESS, SOURCE, IS_PREFERRED, LAST_UPDATED) ";
      theSql += "(select char_PENN_ID, EMAIL_ADDRESS, SOURCE, IS_PREFERRED, LAST_UPDATED from comadmin.TEMP_NGSS_STU_ID_FULL_V_emails)";
      rows = new GcDbAccess().connectionName("pennCommunityTest").sql(theSql).executeSql();

      gsh_builtin_gshTemplateOutput.addOutputLine("Inserted " + rows + " into authzadm.TEMP_BANNER_STU_ID_FULL_EMAILS@" + dbLink);

    } else if (actionChangePennKey) {

      String sql = "update comadmin.member set KERBEROS_PRINCIPAL ";
      if (StringUtils.isBlank(gsh_input_newPennKey)) {
        sql += " = null "; 
      } else {
        sql += " = ? ";
      }
      sql += " where CHAR_PENN_ID = ? and BANNER_GUID = ? and kerberos_principal ";
      if (StringUtils.isBlank(gsh_input_oldPennKey)) {
        sql += " is null "; 
      } else {
        sql += " = ? ";
      }
      
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName("pennCommunityTest").sql(sql);
      
      if (!StringUtils.isBlank(gsh_input_newPennKey)) {
        gcDbAccess.addBindVar(gsh_input_newPennKey);
      }
      
      gcDbAccess.addBindVar(gsh_input_pennId).addBindVar(gsh_input_bannerGuid);

      if (!StringUtils.isBlank(gsh_input_oldPennKey)) {
        gcDbAccess.addBindVar(gsh_input_oldPennKey);
      }

      int rows = gcDbAccess.executeSql();

      if (rows == 1) {
        gsh_builtin_gshTemplateOutput.addOutputLine("Updated PennKey successfully!");
      } else if (rows == 0) {
        gsh_builtin_gshTemplateOutput.addOutputLine("error", "Could not match on inputs, did not update PennKey!");
      } else {
        gsh_builtin_gshTemplateOutput.addOutputLine("error", "" + rows + " were updated, that is a big problem which should not happen!");
      }

    } else if (actionDisplayState) {

      displayPerson(gsh_builtin_gshTemplateOutput, envToDbLink, gsh_input_env, gsh_input_pennId, gsh_input_bannerGuid, gsh_input_pennKey, gsh_input_pidm);

      
    } else if (actionFindPerson) {
      
      String dbLink = envToDbLink.get(gsh_input_env);
      String dbLinkPennantStudent = dbLink.toLowerCase().replace("authzadm", "pennant_student");
      
      //personNotInBanner, personInBannerWithoutPennkey,  personInBannerWithPennkey, personInBannerIsStudent, personInBannerNotStudent
      if (StringUtils.equals(gsh_input_dataType, "personNotInBanner")) {
        
        String sql = " ( select penn_id, row_number() over (order by penn_id) as row_number from authzadm.TEMP_BANNER_STU_ID_FULL@" + dbLinkPennantStudent + " p ";
        sql += " where not exists (SELECT 1 FROM spriden@" + dbLinkPennantStudent + " s where s.spriden_id = p.penn_id) ) ";
        
        findRandomPennId(gsh_builtin_gshTemplateOutput, gsh_input_env, envToDbLink, sql);
      } else if (StringUtils.equals(gsh_input_dataType, "personInBannerWithoutPennkey")) {
          
        String sql = " ( select penn_id, row_number() over (order by penn_id) as row_number from authzadm.TEMP_BANNER_STU_ID_FULL@" + dbLinkPennantStudent + " p , ";
        sql += " spriden@" + dbLinkPennantStudent + " s where s.spriden_id = p.penn_id and not exists (SELECT 1 FROM gobumap@" + dbLinkPennantStudent + " g where g.gobumap_pidm = s.spriden_pidm)  ) ";
        
        findRandomPennId(gsh_builtin_gshTemplateOutput, gsh_input_env, envToDbLink, sql);
        
      } else if (StringUtils.equals(gsh_input_dataType, "personInBannerWithPennkey")) {
        
        String sql = " ( select penn_id, row_number() over (order by penn_id) as row_number from authzadm.TEMP_BANNER_STU_ID_FULL@" + dbLinkPennantStudent + " p , ";
        sql += " spriden@" + dbLinkPennantStudent + " s where s.spriden_id = p.penn_id and exists (SELECT 1 FROM gobumap@" + dbLinkPennantStudent + " g where g.gobumap_pidm = s.spriden_pidm)  ) ";
        
        findRandomPennId(gsh_builtin_gshTemplateOutput, gsh_input_env, envToDbLink, sql);
        
      } else if (StringUtils.equals(gsh_input_dataType, "personInBannerIsStudent")) {
        
        String sql = " ( select penn_id, row_number() over (order by penn_id) as row_number from authzadm.TEMP_BANNER_STU_ID_FULL@" + dbLinkPennantStudent + " p , ";
        sql += " spriden@" + dbLinkPennantStudent + " s where s.spriden_id = p.penn_id and exists (SELECT 1 FROM SGBSTDN@" + dbLinkPennantStudent + " WHERE SGBSTDN_PIDM = s.SPRIDEN_PIDM)  ) ";
        
        findRandomPennId(gsh_builtin_gshTemplateOutput, gsh_input_env, envToDbLink, sql);

      } else if (StringUtils.equals(gsh_input_dataType, "personInBannerNotStudent")) {
        
        String sql = " ( select penn_id, row_number() over (order by penn_id) as row_number from authzadm.TEMP_BANNER_STU_ID_FULL@" + dbLinkPennantStudent + " p , ";
        sql += " spriden@" + dbLinkPennantStudent + " s where s.spriden_id = p.penn_id and not exists (SELECT 1 FROM SGBSTDN@" + dbLinkPennantStudent + " WHERE SGBSTDN_PIDM = s.SPRIDEN_PIDM)  ) ";
        
        findRandomPennId(gsh_builtin_gshTemplateOutput, gsh_input_env, envToDbLink, sql);

      }
    }
    
    // done!
    gsh_builtin_gshTemplateOutput.addOutputLine("Finished running NGSS E201 template!");

  // Uncomment to run in java
  
//  }

 
//}
```

## Config without script

```
grouperGshTemplate.ngssE201Testing.displayErrorOutput = true
grouperGshTemplate.ngssE201Testing.folderShowOnDescendants = certainFolderAndDescendants
grouperGshTemplate.ngssE201Testing.folderShowType = certainFolder
grouperGshTemplate.ngssE201Testing.folderUuidToShow = 2f91557345254c9e9c2b744336791be9
grouperGshTemplate.ngssE201Testing.groupUuidCanRun = fb9238d5bf564bb1b603852545e45ff4
grouperGshTemplate.ngssE201Testing.input.0.description = Select the action you want
grouperGshTemplate.ngssE201Testing.input.0.dropdownCsvValue = syncData, changePennKey, displayState, findPerson
grouperGshTemplate.ngssE201Testing.input.0.dropdownValueFormat = csv
grouperGshTemplate.ngssE201Testing.input.0.formElementType = dropdown
grouperGshTemplate.ngssE201Testing.input.0.label = Action
grouperGshTemplate.ngssE201Testing.input.0.name = gsh_input_action
grouperGshTemplate.ngssE201Testing.input.0.required = true
grouperGshTemplate.ngssE201Testing.input.1.description = Pick the Banner environment to operate on
grouperGshTemplate.ngssE201Testing.input.1.dropdownCsvValue = fot
grouperGshTemplate.ngssE201Testing.input.1.dropdownValueFormat = csv
grouperGshTemplate.ngssE201Testing.input.1.formElementType = dropdown
grouperGshTemplate.ngssE201Testing.input.1.label = Environment
grouperGshTemplate.ngssE201Testing.input.1.name = gsh_input_env
grouperGshTemplate.ngssE201Testing.input.1.required = true
grouperGshTemplate.ngssE201Testing.input.1.showEl = ${gsh_input_action == 'syncData' || gsh_input_action == 'displayState' || gsh_input_action=='findPerson'}
grouperGshTemplate.ngssE201Testing.input.2.description = Numeric 8 digit Penn ID
grouperGshTemplate.ngssE201Testing.input.2.label = Penn ID
grouperGshTemplate.ngssE201Testing.input.2.maxLength = 8
grouperGshTemplate.ngssE201Testing.input.2.name = gsh_input_pennId
grouperGshTemplate.ngssE201Testing.input.2.showEl = ${gsh_input_action == 'changePennKey' || gsh_input_action == 'displayState' }
grouperGshTemplate.ngssE201Testing.input.2.validationMessage = 8 digit numeric
grouperGshTemplate.ngssE201Testing.input.2.validationRegex = ^[0-9]{8}$
grouperGshTemplate.ngssE201Testing.input.2.validationType = regex
grouperGshTemplate.ngssE201Testing.input.3.description = Banner GUID
grouperGshTemplate.ngssE201Testing.input.3.label = Banner GUID
grouperGshTemplate.ngssE201Testing.input.3.maxLength = 40
grouperGshTemplate.ngssE201Testing.input.3.name = gsh_input_bannerGuid
grouperGshTemplate.ngssE201Testing.input.3.showEl = ${gsh_input_action == 'changePennKey' || gsh_input_action == 'displayState'}
grouperGshTemplate.ngssE201Testing.input.3.validationBuiltin = alphaNumeric
grouperGshTemplate.ngssE201Testing.input.3.validationType = builtin
grouperGshTemplate.ngssE201Testing.input.4.description = The previous value of this user's PennKey or blank if doesn't have one
grouperGshTemplate.ngssE201Testing.input.4.label = Old PennKey
grouperGshTemplate.ngssE201Testing.input.4.maxLength = 8
grouperGshTemplate.ngssE201Testing.input.4.name = gsh_input_oldPennKey
grouperGshTemplate.ngssE201Testing.input.4.showEl = ${gsh_input_action == 'changePennKey'}
grouperGshTemplate.ngssE201Testing.input.4.validationBuiltin = alphaNumeric
grouperGshTemplate.ngssE201Testing.input.4.validationType = builtin
grouperGshTemplate.ngssE201Testing.input.5.description = The new value of the person's PennKey of blank to remove it
grouperGshTemplate.ngssE201Testing.input.5.label = New PennKey
grouperGshTemplate.ngssE201Testing.input.5.maxLength = 8
grouperGshTemplate.ngssE201Testing.input.5.name = gsh_input_newPennKey
grouperGshTemplate.ngssE201Testing.input.5.showEl = ${gsh_input_action == 'changePennKey'}
grouperGshTemplate.ngssE201Testing.input.5.validationBuiltin = alphaNumeric
grouperGshTemplate.ngssE201Testing.input.5.validationType = builtin
grouperGshTemplate.ngssE201Testing.input.6.description = Banner PIDM for user
grouperGshTemplate.ngssE201Testing.input.6.label = PIDM
grouperGshTemplate.ngssE201Testing.input.6.name = gsh_input_pidm
grouperGshTemplate.ngssE201Testing.input.6.showEl = ${gsh_input_action == 'displayState'}
grouperGshTemplate.ngssE201Testing.input.6.type = integer
grouperGshTemplate.ngssE201Testing.input.6.validationRegex = ^[0-9]{0,8}$
grouperGshTemplate.ngssE201Testing.input.6.validationType = regex
grouperGshTemplate.ngssE201Testing.input.7.description = What data do you want to find
grouperGshTemplate.ngssE201Testing.input.7.dropdownCsvValue = personNotInBanner, personInBannerWithoutPennkey,  personInBannerWithPennkey, personInBannerIsStudent, personInBannerNotStudent
grouperGshTemplate.ngssE201Testing.input.7.dropdownValueFormat = csv
grouperGshTemplate.ngssE201Testing.input.7.formElementType = dropdown
grouperGshTemplate.ngssE201Testing.input.7.label = Data type
grouperGshTemplate.ngssE201Testing.input.7.name = gsh_input_dataType
grouperGshTemplate.ngssE201Testing.input.7.required = true
grouperGshTemplate.ngssE201Testing.input.7.showEl = ${gsh_input_action == 'findPerson'}
grouperGshTemplate.ngssE201Testing.input.8.description = Enter the alphanumeric PennKey, e.g. jsmith
grouperGshTemplate.ngssE201Testing.input.8.label = PennKey
grouperGshTemplate.ngssE201Testing.input.8.maxLength = 8
grouperGshTemplate.ngssE201Testing.input.8.name = gsh_input_pennKey
grouperGshTemplate.ngssE201Testing.input.8.showEl = ${gsh_input_action == 'displayState'}
grouperGshTemplate.ngssE201Testing.input.8.validationBuiltin = alphaNumeric
grouperGshTemplate.ngssE201Testing.input.8.validationType = builtin
grouperGshTemplate.ngssE201Testing.moreActionsLabel = NGSS E201 testing
grouperGshTemplate.ngssE201Testing.numberOfInputs = 9
grouperGshTemplate.ngssE201Testing.runAsType = GrouperSystem
grouperGshTemplate.ngssE201Testing.securityRunType = specifiedGroup
grouperGshTemplate.ngssE201Testing.showInMoreActions = true
grouperGshTemplate.ngssE201Testing.showOnFolders = true
grouperGshTemplate.ngssE201Testing.templateDescription = When testing E201 these are certain utilities
grouperGshTemplate.ngssE201Testing.templateName = NGSS E201 testing

```
