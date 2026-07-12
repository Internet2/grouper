---
title: "Grouper custom template via GSH role mapper"
space: Grouper
pageId: 28548048
version: 10
lastUpdated: 2026-07-01T05:45:28.114Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548048/Grouper+custom+template+via+GSH+role+mapper
---

There are groups in Banner, groups in [Outsystems](https://www.outsystems.com/) (development platform). There are many environments of Banner, and a few Outsystems environments. A utility is needed for a functional analyst to map Banner groups to be added to Outsystems groups for certain environments. It should only affect groups being uploaded (i.e. there should be a Financial Aid spreadsheet that shouldnt unassign the Student Records groups). Groups assigned from another environment should be unassigned (for the groups in spreadsheet).

## Environments

Banner groups loaded from Banner env database (Banner is system of record)

| Environment | Example group name |
| --- | --- |
| Dev | penn:isc:ait:apps:ngss:environments:dev:banner:ngssDevSecrUserGroups:ngssDevSecr_UP_AR_ACCOUNTING_G |
| Fun | penn:isc:ait:apps:ngss:environments:fun:banner:ngssFunSecrUserGroups:ngssFunSecr_UP_AR_ALL_T_OBJECTS_TEMP_G |
| Trx | penn:isc:ait:apps:ngss:environments:trx:banner:ngssTrxSecrUserGroups:ngssTrxSecr_UP_AR_CALL_CENTER_G |

Outsystems groups (Grouper is system of record)

| Environment | Example group name |
| --- | --- |
| Dev | penn:isc:ait:apps:outsystems:groups:dev:PennsWay:PennsWayAdmin |
| Test | penn:isc:ait:apps:outsystems:groups:test:PFAudit:PFAuditAdmin |
| Prod | penn:isc:ait:apps:outsystems:groups:prod:EmailForward:BillingAdmin |

## Role mapping spreadsheet

Banner (top group) has the group name as the suffix after Secr_, outsystems (along left) needs the group extension and the parent folder name. The group names can be generated from Grouper via query. X means the top group should be a member of the left group. Blank means the top group should not be a member of the left group.

## Upload CSV to template UI

Upload a CSV file, the CSV looks like this

## Setup the template

Config

```
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.displayErrorOutput = true
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.folderShowOnDescendants = certainFolderAndDescendants
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.folderShowType = certainFolder
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.folderUuidToShow = 122373d3760545ce88d455483fbe9d50
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.groupUuidCanRun = 9195195b7e5144cf9cea74821fc2bd66
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.gshTemplate = //
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.0.description = Banner environment to use groups from
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.0.dropdownCsvValue = dev, fun, trx
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.0.dropdownValueFormat = csv
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.0.formElementType = dropdown
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.0.label = Banner environment
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.0.name = gsh_input_bannerEnvironment
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.0.required = true
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.1.description = Outsystems environment to assign groups to
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.1.dropdownCsvValue = dev, test, prod
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.1.dropdownValueFormat = csv
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.1.formElementType = dropdown
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.1.label = Outsystems environment
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.1.name = gsh_input_outsystemsEnvironment
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.1.required = true
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.2.defaultValue = true
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.2.description = If a report should be shown and no changes made
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.2.label = Readonly
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.2.name = gsh_input_readonly
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.2.type = boolean
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.3.description = CSV of grid of Banner groups that should be included in Outsystems groups.  Enter in $thenewline$ for newlines
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.3.label = CSV of assignments
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.3.maxLength = 20000
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.3.name = gsh_input_csv
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.3.required = true
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.input.3.validationType = none
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.moreActionsLabel = NGSS Banner groups to Outsystems
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.numberOfInputs = 4
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.runAsType = GrouperSystem
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.runGshInTransaction = false
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.securityRunType = specifiedGroup
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.showInMoreActions = true
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.showOnFolders = true
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.templateDescription = Map Banner groups to Outsystems.  See a report or make assignments
grouperGshTemplate.ngssSecurityMapBannerGroupsToOutsystems.templateName = NGSS Banner groups to Outsystems

```

Template script

```
//
////uncomment to compile in eclipse (and last line)
//// these are standard imports, can be commented out in script but needed in eclipse
//import java.io.StringReader;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.TreeMap;
//import java.util.TreeSet;
//
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

//import org.apache.commons.lang3.StringUtils;
//
//import edu.internet2.middleware.grouper.Group;
//import edu.internet2.middleware.grouper.GroupFinder;
//import edu.internet2.middleware.grouper.GrouperSession;
//import edu.internet2.middleware.grouper.SubjectFinder;
//import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
//import edu.internet2.middleware.grouper.misc.GrouperStartup;
//import edu.internet2.middleware.grouper.util.GrouperUtil;
//import edu.internet2.middleware.grouperClient.collections.MultiKey;
//import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
//import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
//import edu.internet2.middleware.subject.Subject;
//
//public class Test10 {
//
//  public static void main(String[] args) {
//    
//    GrouperStartup.startup();
//    
//    //input of project name, alphanumeric, start with lower.
//    String gsh_input_bannerEnvironment = "dev";
//    String gsh_input_outsystemsEnvironment = "dev";
//    Boolean gsh_input_readonly = false;
//    String gsh_input_csv = "Group name,UP_FA_SFS_SYSTEM_SUPPORT_G,UP_FA_SFS_SENIOR_MGMT_G,UP_FA_SFS_MANAGEMENT_G,UP_FA_COUNSELORS_G,UP_FA_SRFS_ACCOUNTING_MGR_G,UP_FA_ONE_STOP_G,UP_FA_DATA_MANAGEMENT_G,UP_FA_CASHIER_G,UP_FA_SCHOOL_G,UP_FA_SCHOOL_NA_G$thenewline$PFAthLia:PFAthLiaAdmin,X,X, ,,,,,,,$thenewline$PFAthLia:PFAthLiaAllowed,, ,,,,,,,,$thenewline$PFAudit:PFAuditAdmin,X,X,,,,,,,,$thenewline$PFAudit:PFAuditAllowed,X,,X,X,,,,,,$thenewline$PFAwardConfig:PFAwardConfigAdmin,X,X,,,,,,,,$thenewline$PFAwardConfig:PFAwardConfigAllowed,X, ,X,,,,,,,$thenewline$PFCline:PFClineAllowed,,X,X,X,,, ,X,,$thenewline$PFDocUp:PFDocUpAdmin,X,X,,,,,X,,,$thenewline$PFDocUp:PFDocUpAllowed,,, , ,,,,,,$thenewline$PFDocUp:PFDocUpProxy,X,X,X,X,,X,X,,X,X$thenewline$PFFaar:PFFaarAdmin,X,X,,,,,,,,$thenewline$PFFaar:PFFaarAllowed,X,X,X,X,,X,,,,$thenewline$PFFaar:PFFaarProxy,X,X,X,X,,,,,X,$thenewline$PFFisap:PFFisapAdmin,X, ,, , ,,,,,$thenewline$PFFisap:PFFisapAllowed,,X,,,X,,,,,$thenewline$PFFundsMgmt:PFFundsMgmtAdmin,X,X, ,, ,,,,, $thenewline$PFFundsMgmt:PFFundsMgmtAllowed,,X,X,,X,,,,,$thenewline$PFGloPro:PFGloProAdmin,X,,X,,,,,,,$thenewline$PFGloPro:PFGloProAllowed,,X,X,X,,,,,,$thenewline$PFIRule:PFIRuleAdmin,X,X,,,,,X,,,$thenewline$PFIRule:PFIRuleAllowed,X,, ,,,,X,,,$thenewline$PFIRule:PFIRuleTesting,X,X,X,,,,X,,,$thenewline$PFLoan:PFLoanAdmin,X,X,,,,,,,,$thenewline$PFLoan:PFLoanAllowed,,, ,X,,X,X, ,,$thenewline$PFLoan:PFLoanStaff,,,X,X,,,,X,,$thenewline$PFPimt:PFPimtAdmin,X,X,,,,,,,,$thenewline$PFPimt:PFPimtAllowed,,, ,,,,,, ,X$thenewline$PFPimt:PFPimtProxyUser,X,X, , ,,,,,,$thenewline$PFPimt:PFPimtUnrestricted, ,X,X,X,,,,,,$thenewline$PFProf:PFProfAdmin,X,X,X,,,,,,,$thenewline$PFPudd:PFPuddAllowed,X,X,,,,,,,,$thenewline$PFPudd:PFPuddTesting,X,X,X,X,,,,,,$thenewline$PFTax:PFTaxAdmin,X,X,X,,,, ,,,$thenewline$PFTax:PFTaxAllowed,,,X,X,,,X,,,$thenewline$PFVet:PFVetAdmin,X,X,,,,,,,,$thenewline$PFVet:PFVetAllowed,,,, ,,X,,,,$thenewline$PGFeeder:Admin,X,,,,,,,,,$thenewline$PGFeeder:DownloadUser,X,,,,,,,,,$thenewline$PGFeeder:ProxyUser,X,,,,,,,,,$thenewline$PGFeeder:SourceAdmin,X,,,,,,,,,$thenewline$PGFeeder:UploadAdmin,X,,,,,,,,,$thenewline$PGFeeder:UploadApprover,,X,X,,,,,, ,$thenewline$PGFeeder:UploadUser,,X,X,,,,,,X,$thenewline$PGFeeder:UserAdmin,X,,,,,,,,,$thenewline$PGJob:PGJobAdmin,X,,,,,,,,,$thenewline$PGJob:PGJobAllowed,X,X,X,,,,,,,$thenewline$PGJob:PGJobAllowed_preCheck, ,,,,,,,,,$thenewline$PGJob:PGJobLog,X,X,X,,,,,,,$thenewline$PGMatch:PGMatchAllowed,X,X,X,X,,,X,,,$thenewline$PGMatch:PGMatchPIDOver, ,X,,,,,,,,$thenewline$PGMatch:PGMatchTesting,X,X,X,X,,,X,,,$thenewline$PGOwn:PGOwnAllowed,X,,,,,,,,,$thenewline$PGOwn:PGOwnTesting,X,,,,,,,,,$thenewline$PGStat:PGStatAllowed,X,,,,,,,,,$thenewline$PSDesc:PSDescAdmin,X,,,,,,,,,$thenewline$PGSerDisco:PGSerDiscoAllowed,,,,,,,,,,$thenewline$";
//
//    GrouperSession gsh_builtin_grouperSession = GrouperSession.startRootSession();
//    Subject gsh_builtin_subject = SubjectFinder.findByIdentifierAndSource("mchyzer", "pennperson", true);
//    GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();

    gsh_input_csv = GrouperUtil.replace(gsh_input_csv, '$' + "thenewline" + '$', "\n");
    
    List<String> bannerGroupNamesFromCsv = new ArrayList<String>();
    List<String> outsystemsGroupNamesFromCsv = new ArrayList<String>();
    Map<String, List<String>> outsystemGroupNameToBannerMemberNameFromCsv = new HashMap<String, List<String>>();
    Set<MultiKey> outsystemGroupExtBannerMemberExtFromCsv = new HashSet<MultiKey>();

    StringReader stringReader = null;
    CSVParser csvParser = null;

    try {
      stringReader = new StringReader(gsh_input_csv);

      //convert from CSV to 
      csvParser = new CSVParser(stringReader, CSVFormat.DEFAULT);

      //note, the first row is the title
      List<CSVRecord> csvEntries = csvParser.getRecords();
      
      int lineNumber = 1;

      for (CSVRecord csvRecord : csvEntries) {
        // header is the list of Banner group names
        if (lineNumber == 1) {
          
          // go across top row and skip first cell, and add all to Banner group names
          for (int i=1; i<csvRecord.size(); i++) {
            bannerGroupNamesFromCsv.add(csvRecord.get(i).toLowerCase());
          }
          
        } else {
          
          String outsystemsGroupName = csvRecord.get(0).toLowerCase();

          // make sure valid csv
          if (csvRecord.size() != bannerGroupNamesFromCsv.size()+1) {
            gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_csv",
                "Error: Number of cells in row " + lineNumber + " is not equal to the number of cells in the first line: " + (bannerGroupNamesFromCsv.size()+1));
            gsh_builtin_gshTemplateOutput.assignIsError(true);
            GrouperUtil.gshReturn();
          }

          if (outsystemsGroupNamesFromCsv.contains(outsystemsGroupName)) {
            gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_csv",
                "Error: Two rows have same outsystems group name: " + outsystemsGroupName);
            gsh_builtin_gshTemplateOutput.assignIsError(true);
            GrouperUtil.gshReturn();
          }
          
          outsystemsGroupNamesFromCsv.add(outsystemsGroupName);
          
          List<String> bannerGroupNamesInThisOutsystemGroup = new ArrayList<String>();
          outsystemGroupNameToBannerMemberNameFromCsv.put(outsystemsGroupName, bannerGroupNamesInThisOutsystemGroup);

          // go through the row and see where there is an X and add the Banner group name to the list of outsystems group names
          for (int i=1; i<csvRecord.size(); i++) {
            String cellValue = StringUtils.trim(csvRecord.get(i));
            boolean isX = StringUtils.equalsIgnoreCase(cellValue, "x");
            if (!StringUtils.isBlank(cellValue) && !isX) {
              gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_csv",
                  "Error: line: " + lineNumber + " column: " + i + " does not have a valid value which is blank or X: '" + cellValue+ "'");
              gsh_builtin_gshTemplateOutput.assignIsError(true);
              GrouperUtil.gshReturn();
            }
            
            if (isX) {
              String bannerGroupName = bannerGroupNamesFromCsv.get(i-1);
              bannerGroupNamesInThisOutsystemGroup.add(bannerGroupName);
              outsystemGroupExtBannerMemberExtFromCsv.add(new MultiKey(outsystemsGroupName, bannerGroupName));
            }
          }
          
        }
        lineNumber++;
      }
      
    } catch (Exception e) {
      throw new RuntimeException("Error parsing CSV", e);
      
    } finally {
      GrouperUtil.closeQuietly(csvParser);
      GrouperUtil.closeQuietly(stringReader);
    }

    // get available outsystems groups
    List<Object[]> outsystemsGroups = new GcDbAccess().sql("select gg.name, gg.id from grouper_groups gg where gg.name like ?").addBindVar("penn:isc:ait:apps:outsystems:groups:" + gsh_input_outsystemsEnvironment + ":%").selectList(Object[].class);
    Map<String, String> outsystemsGroupExtensionToId = new TreeMap<String, String>();
    Map<String, String> outsystemsGroupIdToExtension = new TreeMap<String, String>();
    for (Object[] outsystemsExtensionAndId : outsystemsGroups) {
      String nameLower = ((String)outsystemsExtensionAndId[0]).toLowerCase();
      String extensionLower = GrouperUtil.prefixOrSuffix(nameLower, "penn:isc:ait:apps:outsystems:groups:" + gsh_input_outsystemsEnvironment + ":".toLowerCase(), false);
      if (outsystemsGroupExtensionToId.containsKey(extensionLower)) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_csv",
            "Error: Two outsystems groups have the same extension: " + extensionLower);
        gsh_builtin_gshTemplateOutput.assignIsError(true);
        GrouperUtil.gshReturn();
      }
      outsystemsGroupExtensionToId.put(extensionLower, (String)outsystemsExtensionAndId[1]);
      outsystemsGroupIdToExtension.put((String)outsystemsExtensionAndId[1], extensionLower);
    }

    // make sure outsystems groups exist
    for (String outsystemsGroupName : outsystemsGroupNamesFromCsv) {
      if (!outsystemsGroupExtensionToId.containsKey(outsystemsGroupName)) {
//        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_csv",
//            "Error: Outsystems group name doesnt exist in grouper env: " + outsystemsGroupName);
//        gsh_builtin_gshTemplateOutput.assignIsError(true);
//        GrouperUtil.gshReturn();
        gsh_builtin_gshTemplateOutput.addOutputLine("error", "Error: Outsystems group does not exist! '" + outsystemsGroupName + "'");
      }
    }

    // get available Banner groups
    String bannerGroupPrefix = "penn:isc:ait:apps:ngss:environments:" + gsh_input_bannerEnvironment + ":banner:ngss" + StringUtils.capitalize(gsh_input_bannerEnvironment) + "SecrUserGroups:ngss" + StringUtils.capitalize(gsh_input_bannerEnvironment) + "Secr_";
    List<Object[]> bannerGroups = new GcDbAccess().sql("select gg.extension, gg.id from grouper_groups gg where gg.name like ?").addBindVar(bannerGroupPrefix + "%").selectList(Object[].class);
    Map<String, String> bannerGroupExtensionToId = new TreeMap<String, String>();
    for (Object[] bannerExtensionAndId : bannerGroups) {
      String extension = (String)bannerExtensionAndId[0];
      extension = GrouperUtil.prefixOrSuffix(extension, "Secr_", false);
      String extensionLower = extension.toLowerCase();
      if (bannerGroupExtensionToId.containsKey(extensionLower)) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_csv",
            "Error: Two Banner groups have the same extension: " + extensionLower);
        gsh_builtin_gshTemplateOutput.assignIsError(true);
        GrouperUtil.gshReturn();
      }
      bannerGroupExtensionToId.put(extensionLower, (String)bannerExtensionAndId[1]);
    }

    // make sure Banner groups exist
    for (String bannerGroupName : bannerGroupNamesFromCsv) {
      if (!bannerGroupExtensionToId.containsKey(bannerGroupName)) {
//        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_csv",
//            "Error: Banner group name doesnt exist in env: " + bannerGroupName);
//        gsh_builtin_gshTemplateOutput.assignIsError(true);
//        GrouperUtil.gshReturn();
        gsh_builtin_gshTemplateOutput.addOutputLine("error", "Error: Banner group does not exist! '" + bannerGroupName + "'");

      }
    }
    
    // lets only deal with outsystems groups in the spreadsheet
    outsystemsGroupExtensionToId.keySet().retainAll(outsystemsGroupNamesFromCsv);
    
    // lets only deal with Banner groups in the spreadsheet
    bannerGroupExtensionToId.keySet().retainAll(bannerGroupNamesFromCsv);
    
    if (outsystemsGroupIdToExtension.size() == 0) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_csv",
          "Error: Cannot find any Outsystems groups");
      gsh_builtin_gshTemplateOutput.assignIsError(true);
      GrouperUtil.gshReturn();
    }
    // see which Banner groups are used in which outsystems groups in grouper (all envs, with the valid Banner group suffix)
    GcDbAccess gcDbAccess = new GcDbAccess().sql("select gmav.owner_group_id, gg.name from grouper_memberships_all_v gmav, grouper_groups gg, grouper_members gm, grouper_fields gf where gmav.member_id = gm.id and gm.subject_source = 'g:gsa' and gg.id = gm.subject_id and gmav.owner_group_id in (" + GrouperClientUtils.appendQuestions(outsystemsGroupIdToExtension.size()) + ") and gmav.mship_type = 'immediate' and gmav.field_id = gf.id and gf.name = 'members' ");
    for (String outsystemsGroupName : outsystemsGroupIdToExtension.keySet()) {
      gcDbAccess.addBindVar(outsystemsGroupName);
    }
    List<Object[]> groupIdAndSubjectGroupNames = gcDbAccess.selectList(Object[].class); 
    
    Set<MultiKey> ownerGroupIdAndMemberGroupNameWrongEnv = new HashSet<MultiKey>();
    Set<MultiKey> outsystemGroupExtBannerMemberExtFromGrouper = new HashSet<MultiKey>();

    for (Object[] groupIdAndSubjectGroupName : groupIdAndSubjectGroupNames) {
      String ownerGroupId = (String)groupIdAndSubjectGroupName[0];
      String ownerGroupExtension = outsystemsGroupIdToExtension.get(ownerGroupId);
      if (StringUtils.isBlank(ownerGroupExtension)) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_csv",
            "Error: Cant find group id: " + ownerGroupId);
        gsh_builtin_gshTemplateOutput.assignIsError(true);
        GrouperUtil.gshReturn();
        
      }
      String memberGroupName = (String)groupIdAndSubjectGroupName[1];
      // penn:isc:ait:apps:ngss:environments:fun:banner:ngssFunSecrUserGroups:ngssFunSecr_UP_AR_ACCOUNTING_G
      // member group name needs to be like this
      if (!memberGroupName.startsWith("penn:isc:ait:apps:ngss:environments:") || !memberGroupName.contains("Secr_")) {
        continue;
      }
      // make sure this is one of the groups we are tracking
      String groupSuffix = GrouperUtil.prefixOrSuffix(memberGroupName, "Secr_", false).toLowerCase();
      if (!bannerGroupNamesFromCsv.contains(groupSuffix)) {
        continue;
      }
      
      // see if it is in the right env
      if (memberGroupName.startsWith("penn:isc:ait:apps:ngss:environments:" + gsh_input_bannerEnvironment + ":")) {
        
        outsystemGroupExtBannerMemberExtFromGrouper.add(new MultiKey(ownerGroupExtension, groupSuffix));
        
      } else {
        ownerGroupIdAndMemberGroupNameWrongEnv.add(new MultiKey(ownerGroupId, memberGroupName));
      }
      
    }

    Set<MultiKey> outsystemGroupExtBannerMemberExtToAdds = new HashSet<MultiKey>(outsystemGroupExtBannerMemberExtFromCsv);
    outsystemGroupExtBannerMemberExtToAdds.removeAll(outsystemGroupExtBannerMemberExtFromGrouper);
    
    Set<MultiKey> outsystemGroupExtBannerMemberExtToRemoves = new HashSet<MultiKey>(outsystemGroupExtBannerMemberExtFromGrouper);
    outsystemGroupExtBannerMemberExtToRemoves.removeAll(outsystemGroupExtBannerMemberExtFromCsv);
    
    if (ownerGroupIdAndMemberGroupNameWrongEnv.size() == 0 && outsystemGroupExtBannerMemberExtToAdds.size() == 0 && outsystemGroupExtBannerMemberExtToRemoves.size() == 0) {
      gsh_builtin_gshTemplateOutput.addOutputLine("success", "Success: envs are in sync");
    }
    if (gsh_input_readonly) {
      for (MultiKey ownerGroupIdMemberGroupName : ownerGroupIdAndMemberGroupNameWrongEnv) {
        String outsystemsExtension = outsystemsGroupIdToExtension.get((String)ownerGroupIdMemberGroupName.getKey(0));
        gsh_builtin_gshTemplateOutput.addOutputLine("info", "Remove from '" + outsystemsExtension + "' wrong env group: '" + (String)ownerGroupIdMemberGroupName.getKey(1) + "'");
      }
      for (MultiKey outsystemGroupExtBannerMemberExtToRemove : outsystemGroupExtBannerMemberExtToRemoves) {
        String outsystemsExtension = (String)outsystemGroupExtBannerMemberExtToRemove.getKey(0);
        gsh_builtin_gshTemplateOutput.addOutputLine("info", "Remove from '" + outsystemsExtension + "' Banner group: '" + (String)outsystemGroupExtBannerMemberExtToRemove.getKey(1) + "'");
      }
      for (MultiKey outsystemGroupExtBannerMemberExtToAdd : outsystemGroupExtBannerMemberExtToAdds) {
        String outsystemsExtension = (String)outsystemGroupExtBannerMemberExtToAdd.getKey(0);
        gsh_builtin_gshTemplateOutput.addOutputLine("info", "Add to '" + outsystemsExtension + "' Banner group: '" + (String)outsystemGroupExtBannerMemberExtToAdd.getKey(1) + "'");
      }
    } else {
      for (MultiKey ownerGroupIdMemberGroupName : ownerGroupIdAndMemberGroupNameWrongEnv) {
        String outsystemsGroupId = (String)ownerGroupIdMemberGroupName.getKey(0);
        String outsystemsExtension = outsystemsGroupIdToExtension.get(outsystemsGroupId);
        Group outsystemsGroup = GroupFinder.findByUuid(outsystemsGroupId, true);
        String bannerGroupName = (String)ownerGroupIdMemberGroupName.getKey(1);
        Group bannerGroup = GroupFinder.findByName(bannerGroupName, true);
        outsystemsGroup.deleteMember(bannerGroup.toSubject());
        gsh_builtin_gshTemplateOutput.addOutputLine("success", "Success: removed from '" + outsystemsExtension + "' wrong env group: '" + bannerGroupName + "'");
      }
      for (MultiKey outsystemGroupExtBannerMemberExtToRemove : outsystemGroupExtBannerMemberExtToRemoves) {
        String outsystemsExtension = (String)outsystemGroupExtBannerMemberExtToRemove.getKey(0);
        String outsystemsGroupId = outsystemsGroupExtensionToId.get(outsystemsExtension);
        Group outsystemsGroup = GroupFinder.findByUuid(outsystemsGroupId, true);
        String bannerGroupExtension = (String)outsystemGroupExtBannerMemberExtToRemove.getKey(1);
        String bannerGroupId = bannerGroupExtensionToId.get(bannerGroupExtension);
        Group bannerGroup = GroupFinder.findByUuid(bannerGroupId, true);
        outsystemsGroup.deleteMember(bannerGroup.toSubject());
        gsh_builtin_gshTemplateOutput.addOutputLine("success", "Success: removed from '" + outsystemsExtension + "' Banner group: '" + bannerGroupExtension + "'");
      }
      for (MultiKey outsystemGroupExtBannerMemberExtToAdd : outsystemGroupExtBannerMemberExtToAdds) {
        String outsystemsExtension = (String)outsystemGroupExtBannerMemberExtToAdd.getKey(0);
        String bannerGroupExtension = (String)outsystemGroupExtBannerMemberExtToAdd.getKey(1);
        String outsystemsGroupId = outsystemsGroupExtensionToId.get(outsystemsExtension);
        if (StringUtils.isBlank(outsystemsGroupId)) {
          // doesnt exist, already reported, continue
          gsh_builtin_gshTemplateOutput.addOutputLine("error", "Error: skipping add to '" + outsystemsExtension + "' Banner group: '" + bannerGroupExtension + "' (Outsystems group doesnt exist)");
          continue;
        }
        Group outsystemsGroup = GroupFinder.findByUuid(outsystemsGroupId, true);
        String bannerGroupId = bannerGroupExtensionToId.get(bannerGroupExtension);
        if (StringUtils.isBlank(bannerGroupId)) {
          // doesnt exist, already reported, continue
          gsh_builtin_gshTemplateOutput.addOutputLine("error", "Error: skipping add to '" + outsystemsExtension + "' Banner group: '" + bannerGroupExtension + "' (Banner group doesnt exist)");
          continue;
        }
        Group bannerGroup = GroupFinder.findByUuid(bannerGroupId, true);
        outsystemsGroup.addMember(bannerGroup.toSubject());
        gsh_builtin_gshTemplateOutput.addOutputLine("success", "Success: added to '" + outsystemsExtension + "' Banner group: '" + bannerGroupExtension + "'");
      }
      
    }
    
//  }
//
//}
```
