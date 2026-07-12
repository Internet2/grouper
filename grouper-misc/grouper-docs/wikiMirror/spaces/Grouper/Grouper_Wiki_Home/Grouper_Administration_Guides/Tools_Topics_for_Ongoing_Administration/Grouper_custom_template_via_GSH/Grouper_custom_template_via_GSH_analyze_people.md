---
title: "Grouper custom template via GSH analyze people"
space: Grouper
pageId: 28549765
version: 4
lastUpdated: 2026-07-01T05:41:16.501Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549765/Grouper+custom+template+via+GSH+analyze+people
---

A common use case is an app owner has a list of people or emails and they want to analyze those to see if they are still working at the institution, which organization they are from, etc

## Input

You can input (comma separated, semi-colon separated, newline separated, space separate)

- PennIDs (subject ids)
- PennKeys (subject identifier)
- EPPNs (pennkey@institution.edu) (subject identifier)
- email addresses (looked up in our email registry, not a Grouper subject identifier)

## Output

A CSV will be emailed to the user of the screen.

Sample email

## Data fields

| Column name | Sample data | Documentation |
| --- | --- | --- |
| inputted_id | mchyzer | Whatever was inputted, the PennID, PennKey, EPPN, email address |
| matched | T\|F | If this input matches an identity (note, it could be a non person role account) |
| person | T\|F\|<blank> | T means its a person, F means its a role account or principal, blank means unmatched |
| penn_id | 12345678\|<blank> | Penn ID or blank if not a matched person |
| pennkey | jsmith | This is the PennKey if they have one |
| email | [jsmith@whatever.upenn.edu](mailto:jsmith@whatever.upenn.edu) | Preferred email in directory |
| eppn | [jsmith@upenn.edu](mailto:jsmith@upenn.edu) | [pennkey@upenn.edu](mailto:pennkey@upenn.edu) |
| primary_affiliation | STAF | Primary affiliation (e.g. full time staff is preferable to guest) |
| center | 02 | Center code from the payroll system. Note it is possible that there is an org without a center if the user is not at Penn anymore |
| center_name | Wharton School | Human readable label for the center |
| org | 1234 | Org code from the payroll system |
| org_name | English Department | Human readable label for org |
| school | L | School code from student system. Note: faculty are listed as center from the payroll system |
| school_name | Law School | Human readable label for school |
| division | AB | Division code inside the school (e.g. undergrad which which graduate part of the school) |
| division_name | Law Docorate | Human readable label for division |
| active_affiliations | ALUM,CTSY | List of all active affiliations for the user |
| is_workforce | T\|F\|<blank> | If matched person is in the [workforce](https://penno365.sharepoint.com/teams/PennGroups/SitePages/referenceGroups/PennGroups-reference-groups---workforce.aspx) |
| is_member | T\|F\|<blank> | If matched person is a [member of Penn](https://penno365.sharepoint.com/teams/PennGroups/SitePages/referenceGroups/PennGroups-reference-groups---member-of-Penn.aspx) |
| is_affiliate | T\|F\|<blank> | If matched person is an [affiliate](https://penno365.sharepoint.com/teams/PennGroups/SitePages/referenceGroups/PennGroups-reference-groups---affiliate.aspx) |
| name | John Smith | First and last name as displayed in PennGroups |
| description | Chris Hyzer (mchyzer, 10021368) (active) Staff - Isc-applications & Information Services - Application Architect (also: Alumni) | Description as displayed in PennGroups |

## Access

People who have access to this screen (in our example):

- Grouper support team (system administrators)
- Reference group readers (power users)
- People in this group: [analyzePeopleTemplateRunners](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Main.searchSubmit&searchQuery2=analyzePeopleTemplateRunners) (note: users must be in the workforce and will be automatically deprovisioned)

## Configuration

```
import java.io.File;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateRuntime;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.app.reports.GrouperCsvReportJob;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncFromData;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

public class Test104analyzePeople extends GshTemplateV2 {

  public static class TheState {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    public Set<String> groupHasPennids(String groupName, Collection<String> pennids) {
      int batchSize = 1000;
      List<String> pennidsList = new ArrayList<String>(pennids);
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(pennidsList, batchSize, false);
      Set<String> pennIdsExistTotal = new HashSet<String>();
      
      // go through in batches
      for (int i=0;i<numberOfBatches;i++) {
        List<String> pennidsBatch = GrouperUtil.batchList(pennidsList, batchSize, i);
        List<String> pennIdsExist = new GcDbAccess().sql(
            "select subject_id from grouper_memberships_lw_v gmlv where " +
            " list_name = 'members' and subject_source = 'pennperson' " + 
            " and group_name = ? and subject_id in (" + 
            GrouperClientUtils.appendQuestions(pennidsBatch.size()) + ")").
            addBindVar(groupName).addBindVars(pennidsBatch).selectList(String.class);
        
        pennIdsExistTotal.addAll(pennIdsExist);
        
      }
    
      return pennIdsExistTotal;
    }

  }
  
  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {

    Subject currentUserSubject = gshTemplateV2input.getGsh_builtin_subject();
    
    TheState theState = new TheState();

    // csv inputted
    String gsh_input_people = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_people");
    
    if (StringUtils.isBlank(gsh_input_people)) {
      gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addValidationLine(gsh_input_people, "People is a required field!");
    }
    
    gsh_input_people = StringUtils.replace(gsh_input_people, ",", " ");
    gsh_input_people = StringUtils.replace(gsh_input_people, ";", " ");
    gsh_input_people = StringUtils.replace(gsh_input_people, "\n", " ");
    gsh_input_people = StringUtils.replace(gsh_input_people, "\t", " ");
    gsh_input_people = StringUtils.replace(gsh_input_people, "\r", " ");
    Set<String> peopleInputSetOrig = GrouperUtil.splitTrimToSet(gsh_input_people, " ");
    Set<String> peopleInputSet = new LinkedHashSet<>();
    
    for (String peopleInput : peopleInputSetOrig) {
      if (!StringUtils.isBlank(peopleInput)) {
        peopleInputSet.add(peopleInput.toLowerCase());
      }
    }
    
    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();

    int batchSize = 1000;
    List<String> unmatchedPeopleList = new ArrayList<String>(peopleInputSet);

    List<String> unmatchedPeopleListCurrent = new ArrayList<String>(unmatchedPeopleList);
    Map<String, String> peopleInputToPennid = new HashMap<String, String>();
    Set<String> peopleInputNonPerson = new HashSet<String>();

    int numberOfBatches = GrouperUtil.batchNumberOfBatches(unmatchedPeopleListCurrent, batchSize, false);

    for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
      
      List<String> peopleBatch = GrouperUtil.batchList(unmatchedPeopleListCurrent, batchSize, batchIndex);
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName("pennCommunity").sql(
          "SELECT char_penn_id FROM pcdadmin.computed_person where char_penn_id in (" + GrouperClientUtils.appendQuestions(peopleBatch.size()) + ")");
      for (String peopleId : peopleBatch) {
        gcDbAccess.addBindVar(peopleId);
      }
      List<String> peoplePennids = gcDbAccess.selectList(String.class);
      for (String peoplePennid : peoplePennids) {
        peopleInputToPennid.put(peoplePennid, peoplePennid);
        unmatchedPeopleList.remove(peoplePennid);
      }
    }

    unmatchedPeopleListCurrent = new ArrayList<String>(unmatchedPeopleList);
    numberOfBatches = GrouperUtil.batchNumberOfBatches(unmatchedPeopleListCurrent, batchSize, false);

    for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
      
      List<String> peopleBatch = GrouperUtil.batchList(unmatchedPeopleListCurrent, batchSize, batchIndex);
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName("pennCommunity").sql(
          "SELECT kerberos_principal, char_penn_id FROM pcdadmin.computed_person where kerberos_principal in (" + GrouperClientUtils.appendQuestions(peopleBatch.size()) + ")");
      for (String peopleId : peopleBatch) {
        gcDbAccess.addBindVar(peopleId);
      }
      List<Object[]> peoplePennkeyToPennids = gcDbAccess.selectList(Object[].class);
      for (Object[] peoplePennkeyToPennid : peoplePennkeyToPennids) {
        String pennkey = (String)peoplePennkeyToPennid[0];
        String pennid = (String)peoplePennkeyToPennid[1];
        peopleInputToPennid.put(pennkey, pennid);
        unmatchedPeopleList.remove(pennkey);
      }
    }

    unmatchedPeopleListCurrent = new ArrayList<String>(unmatchedPeopleList);
    numberOfBatches = GrouperUtil.batchNumberOfBatches(unmatchedPeopleListCurrent, batchSize, false);

    for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
      
      List<String> peopleBatch = GrouperUtil.batchList(unmatchedPeopleListCurrent, batchSize, batchIndex);
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName("pennCommunity").sql(
          "SELECT distinct email, penn_id FROM diradmin.EMAIL_TO_PERSON_COMPOSITE where email in (" + GrouperClientUtils.appendQuestions(peopleBatch.size()) + ")");
      for (String peopleId : peopleBatch) {
        gcDbAccess.addBindVar(peopleId);
      }
      List<Object[]> peoplePennkeyToPennids = gcDbAccess.selectList(Object[].class);
      for (Object[] peoplePennkeyToPennid : peoplePennkeyToPennids) {
        String email = (String)peoplePennkeyToPennid[0];
        String pennid = GrouperUtil.stringValue(peoplePennkeyToPennid[1]);
        peopleInputToPennid.put(email, pennid);
        unmatchedPeopleList.remove(email);
      }
    }
    
    unmatchedPeopleListCurrent = new ArrayList<String>(unmatchedPeopleList);
    numberOfBatches = GrouperUtil.batchNumberOfBatches(unmatchedPeopleListCurrent, batchSize, false);

    for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
      
      List<String> peopleBatch = GrouperUtil.batchList(unmatchedPeopleListCurrent, batchSize, batchIndex);
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName("pennCommunity").sql(
          "SELECT distinct email FROM diradmin.EMAIL_NON_PERSON_COMPOSITE where email in (" + GrouperClientUtils.appendQuestions(peopleBatch.size()) + ")");
      for (String peopleId : peopleBatch) {
        gcDbAccess.addBindVar(peopleId);
      }
      List<Object[]> peoplePennkeyToPennids = gcDbAccess.selectList(Object[].class);
      for (Object[] peoplePennkeyToPennid : peoplePennkeyToPennids) {
        String email = (String)peoplePennkeyToPennid[0];
        peopleInputNonPerson.add(email);
        unmatchedPeopleList.remove(email);
      }
    }
    
    unmatchedPeopleListCurrent = new ArrayList<String>(unmatchedPeopleList);
    numberOfBatches = GrouperUtil.batchNumberOfBatches(unmatchedPeopleListCurrent, batchSize, false);

    for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
      
      List<String> peopleBatch = GrouperUtil.batchList(unmatchedPeopleListCurrent, batchSize, batchIndex);
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName("pennCommunity").sql(
          "SELECT penn_eppn, char_penn_id FROM pcdadmin.computed_person where penn_eppn in (" + GrouperClientUtils.appendQuestions(peopleBatch.size()) + ")");
      for (String peopleId : peopleBatch) {
        gcDbAccess.addBindVar(peopleId);
      }
      List<Object[]> peoplePennkeyToPennids = gcDbAccess.selectList(Object[].class);
      for (Object[] peoplePennkeyToPennid : peoplePennkeyToPennids) {
        String eppn = (String)peoplePennkeyToPennid[0];
        String pennid = (String)peoplePennkeyToPennid[1];
        peopleInputToPennid.put(eppn, pennid);
        unmatchedPeopleList.remove(eppn);
      }
    }
    
    Map<String, Object[]> pennidToData = new HashMap<>();
    List<String> pennids = new ArrayList<String>(peopleInputToPennid.values());
    
    numberOfBatches = GrouperUtil.batchNumberOfBatches(pennids, batchSize, false);

    for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
      
      List<String> peopleBatch = GrouperUtil.batchList(pennids, batchSize, batchIndex);
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName("pennCommunity").sql(
          "SELECT char_penn_id, kerberos_principal, DIRECTORY_PRIM_CENT_AFFIL_CODE, org_center_or_override, nvl(home_org, sponsor_org), "
          + " srs_school_or_override, srs_division, all_active_affiliations, penn_eppn as eppn,"
          + " ( select c.CENTER_NAME from PACMAN.CENTER c where CENTER_CODE = org_center_or_override ) as center_description, "
          + " ( select o.ORG_DISPLAY_NAME from ORG_LIST_V o where ORG_NAME =  nvl(home_org, sponsor_org) ) as org_name, "
          + " ( select d.SCHOOL_DESC from DIRADMIN.DIR_SCHOOL d where SCHOOL_CODE = srs_school_or_override ) as school_name, "
          + " ( select d.DIVISION_DESC from DIRADMIN.DIR_DIVISION d where DIVISION = srs_division ) as div_name "
          + " FROM pcdadmin.computed_person where char_penn_id in (" + GrouperClientUtils.appendQuestions(peopleBatch.size()) + ")");
      for (String peopleId : peopleBatch) {
        gcDbAccess.addBindVar(peopleId);
      }
      List<Object[]> peopleDatas = gcDbAccess.selectList(Object[].class);
      for (Object[] peopleData : peopleDatas) {
        String pennid = (String)peopleData[0];
        pennidToData.put(pennid, peopleData);
      }
    }
    
    // resolve all the subjects
    Map<String, Subject> pennIdToSubject = SubjectFinder.findByIds(pennids, "pennperson");

    List<String[]> reportData = new ArrayList<String[]>();
    List<String> columnNamesUsers = GrouperUtil.toList("inputted_id", "matched", "person", "penn_id", "pennkey", "email", "eppn",
        "primary_affiliation", "center", "center_name", "org", "org_name", "school", "school_name", "division", "division_name", 
        "active_affiliations", "is_workforce", "is_member", "is_affiliate", "name", "description");
    
    Set<String> workforcePennids = theState.groupHasPennids("penn:community:employeeOrContractorIncludingUphs", pennids);
    Set<String> memberPennids = theState.groupHasPennids("penn:community:activeNonAlumniWithPennname", pennids);
    Set<String> affiliatePennids = theState.groupHasPennids("penn:community:affiliateMember", pennids);

    for (String peopleId : peopleInputSet) {
      
      List<String> rowData = new ArrayList<String>();
      rowData.add(peopleId);
      
      boolean matched = !unmatchedPeopleList.contains(peopleId);
      rowData.add(matched ? "T" : "F");
      
      boolean person = matched && peopleInputToPennid.containsKey(peopleId);
      String pennid = peopleInputToPennid.get(peopleId);
      
      rowData.add(person ? "T" : "F");
      
      rowData.add(person ? pennid : "");

      Object[] data = !StringUtils.isBlank(pennid) ? pennidToData.get(pennid) : null;

      // char_penn_id, kerberos_principal, DIRECTORY_PRIM_CENT_AFFIL_CODE, org_center_or_override, nvl(home_org, sponsor_org), 
      // srs_school_or_override, srs_division, all_active_affiliations, eppn
      String pennkey = data == null ? "" : (String)data[1];
      rowData.add(pennkey);
      String affiliation = data == null ? "" : StringUtils.defaultString((String)data[2]);
      String center = data == null ? "" : StringUtils.defaultString((String)data[3]);
      String org = data == null ? "" : StringUtils.defaultString((String)data[4]);
      String school = data == null ? "" : StringUtils.defaultString((String)data[5]);
      String division = data == null ? "" : StringUtils.defaultString((String)data[6]);
      String allAfiliations = data == null ? "" : StringUtils.defaultString((String)data[7]);
      String eppn = data == null ? "" : StringUtils.defaultString((String)data[8]);
      String centerName = data == null ? "" : StringUtils.defaultString((String)data[9]);
      String orgName = data == null ? "" : StringUtils.defaultString((String)data[10]);
      String schoolName = data == null ? "" : StringUtils.defaultString((String)data[11]);
      String divisionName = data == null ? "" : StringUtils.defaultString((String)data[12]);
      
      Subject subject = !StringUtils.isBlank(pennid) ? pennIdToSubject.get(pennid) : null;
      String name = subject == null ? "" : subject.getName();
      String email = subject == null ? "" : StringUtils.defaultString(GrouperEmail.retrieveEmailAddress(subject));
      String description = subject == null ? "" : StringUtils.defaultString(subject.getDescription());

      rowData.add(email);
      rowData.add(eppn);
      rowData.add(affiliation);
      rowData.add(center);
      rowData.add(centerName);
      rowData.add(org);
      rowData.add(orgName);
      rowData.add(school);
      rowData.add(schoolName);
      rowData.add(division);
      rowData.add(divisionName);
      rowData.add(allAfiliations);
      rowData.add(pennid == null ? "" : (workforcePennids.contains(pennid) ? "T" : "F"));
      rowData.add(pennid == null ? "" : (memberPennids.contains(pennid) ? "T" : "F"));
      rowData.add(pennid == null ? "" : (affiliatePennids.contains(pennid) ? "T" : "F"));
      rowData.add(name);
      rowData.add(description);

      String[] row = GrouperUtil.toArray(rowData, String.class);
      reportData.add(row);
      
    }
    
    
    String timestampToFileString = GrouperUtil.timestampToFileString(new Date());
    String fileName = GrouperUtil.tmpDir(true) + "peopleAnalyze_" + timestampToFileString + ".csv";

    File file = GrouperCsvReportJob.createCsv(fileName, columnNamesUsers, reportData);

    new GrouperEmail().addEmailAddressToSendTo(GrouperEmailUtils.getEmail(currentUserSubject)).setSubject("People analyze report " + timestampToFileString).
      setBody("<html>Attached is person report<br /><br><a href=\"https://penno365.sharepoint.com/teams/PennGroups/SitePages/features/PennGroups-features---analyzePeople.aspx\">Documentation wiki</a><br /><br />"
          + "inputted_id: what was inputted in the screen<br />"
          + "matched: if this matches a person or non person<br />"
          + "person: if this matched id is a person, F for non person (service account or list serv etc)<br />"
          + "eppn: pennkey@upenn.edu<br />"
          + "is_workforce: if the user is a fac/staf/serv/temp/ctwk/uphs/etc<br />"
          + "is_member: if user is student or workforce or other strong relation to Penn<br />"
          + "is_affiliate: if user is member or weak relationship to Penn</html>").addAttachment(file).send();
    
    file.delete();

    gsh_builtin_gshTemplateOutput.addOutputLine("People analysis complete.  You will receive an email with the results.");

    
  }
  
  public static void main(String[] args) {
    
    GrouperStartup.startup();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Subject subject = SubjectFinder.findByIdAndSource("10021368", "pennperson", true);

        Test104analyzePeople test104analyzePeople = new Test104analyzePeople();
        GshTemplateV2input gshTemplateV2input = new GshTemplateV2input();
        gshTemplateV2input.setGsh_builtin_subject(subject);
        GshTemplateRuntime gshTemplateRuntime = new GshTemplateRuntime();
        gshTemplateRuntime.setTemplateConfigId("analyzePeople");
        gshTemplateV2input.setGsh_builtin_gshTemplateRuntime(gshTemplateRuntime);
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_people", "mchyzer, kwilso");

        GshTemplateV2output gshTemplateV2output = new GshTemplateV2output();
        
        test104analyzePeople.gshRunLogic(gshTemplateV2input, gshTemplateV2output);
        
        return null;
      }
    });
    System.exit(0);

  }

}

```
