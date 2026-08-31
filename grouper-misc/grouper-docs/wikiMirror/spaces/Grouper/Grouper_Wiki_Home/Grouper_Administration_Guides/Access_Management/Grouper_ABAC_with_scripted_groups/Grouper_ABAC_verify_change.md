---
title: "Grouper ABAC verify change"
space: Grouper
pageId: 28549610
version: 5
lastUpdated: 2026-07-01T05:41:40.294Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549610/Grouper+ABAC+verify+change
---

If an ABAC row is in use in scripted groups, a GSH template like this can help assure that changes will not negatively affect existing groups.

Each row based on a query should have a current view and a next version view. Edit the new view with the proposed changes and the new and old counts can be tested and compared with the current membership count.

For each type of row, have a drop down item. Each category of scripted group can be modeled in the GSH template so the current count, before query, and after query are tested.

## Find groups that use a given row

Note, substitute in the "etc" folder location, in this case it is just "etc". The row alias in this case is "affiliation".

```
select
  group_name,value_string,
  (select gscg.membership_size
  from grouper_sql_cache_group gscg, grouper_fields gf, grouper_groups gg
  where gscg.group_internal_id = gg.internal_id and gf.name = 'members'
    and gf.internal_id = gscg.field_internal_id and gg.name = gaaagv.group_name) as membership_size
from grouper_aval_asn_asn_group_v gaaagv
where gaaagv.attribute_def_name_name2 = 'etc:attribute:abacJexlScript:grouperJexlScriptJexlScript'
  and lower(gaaagv.value_string) like '%affiliation%' and lower(gaaagv.value_string) like '%hasrow%'
```

## Example GSH template to test ABAC changes

GSH template source:

```
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class Test139abacChangeHelper extends GshTemplateV2 {

  private static final Log LOG = GrouperUtil.getLog(GshTemplateV2.class);

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
      GshTemplateV2output gshTemplateV2output) {

    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();

    // find groups in sql
    //  select group_name, value_string,
    //  (select gscg.membership_size from grouper_sql_cache_group gscg, grouper_fields gf, grouper_groups gg
    //  where gscg.group_internal_id = gg.internal_id and gf.name = 'members' and gf.internal_id = gscg.field_internal_id
    //  and gg.name = gaaagv.group_name) as membership_size
    //  from grouper_aval_asn_asn_group_v gaaagv 
    //  where gaaagv.attribute_def_name_name2 = 'penn:etc:attribute:abacJexlScript:grouperJexlScriptJexlScript'
    //  and lower(gaaagv.value_string) like '%affiliation%' and lower(gaaagv.value_string) like '%hasrow%';
    
    // stay on same page
    gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("NONE");

    // get input
    String gsh_input_abacType = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_abacType");

    // validate input must be pursual or section
    if (!StringUtils.equals("pursual", gsh_input_abacType) && !StringUtils.equals("section", gsh_input_abacType)
        && !StringUtils.equals("affiliation", gsh_input_abacType)) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_abacType", "Only 'affiliation', 'pursual' and 'section' are supported");
      gsh_builtin_gshTemplateOutput.assignIsError(true);
      return;
    }

    // if section
    if (StringUtils.equals("pursual", gsh_input_abacType)) {
      // define test cases
      List<String> groupNames = new ArrayList<String>();
      groupNames.add("penn:wharton:community:whartonHS");
      groupNames.add("penn:wharton:community:whartonUGROneYearGrads");
      groupNames.add("penn:wharton:community:whartonUGROneMonthGrads");
      groupNames.add("penn:sas:query:student:psych:undergrad-psych");

      List<String> sqlsOldView = new ArrayList<String>();
      sqlsOldView.add("""
        SELECT count(DISTINCT pursual_penn_id)
        FROM AUTHZ_ABAC_PURSUAL_V aapv
        WHERE pursual_student_level = 'HS' AND pursual_has_pennkey = 'T'
          AND pursual_division = 'WU'
          AND (pursual_exit_action = 'null' or pursual_exit_date_in_future = 'T')
          AND pursual_exp_grad_date_in_future = 'T'
      """);
      sqlsOldView.add("""
        SELECT count(DISTINCT pursual_penn_id)
        FROM AUTHZ_ABAC_PURSUAL_V aapv
        WHERE pursual_student_level != 'HS' AND pursual_has_pennkey = 'T'
          AND pursual_division = 'WU'
          AND (( pursual_has_exit_term = 'T'
                 AND pursual_exit_action = 'E'
                 AND pursual_exp_grad_date_future_months >= -13)
              OR (pursual_has_graduation_term = 'T'
                 AND pursual_graduation_date_future_months >= -13 ))
      """);
      sqlsOldView.add("""
        SELECT count(DISTINCT pursual_penn_id)
        FROM AUTHZ_ABAC_PURSUAL_V aapv
        WHERE pursual_student_level != 'HS' AND pursual_has_pennkey = 'T'
          AND pursual_division = 'WU'
          AND (( pursual_has_exit_term = 'T'
                 AND pursual_exit_action = 'E'
                 AND pursual_exp_grad_date_future_months >= -1)
              OR (pursual_has_graduation_term = 'T'
                 AND pursual_graduation_date_future_months >= -1 ))
      """);
      sqlsOldView.add("""
          SELECT count(DISTINCT pursual_penn_id) FROM AUTHZ_ABAC_PURSUAL_V aapv 
          WHERE pursual_major_minor = 'PSYC' 
            and pursual_major_minor_type = 'MAJOR' 
            and pursual_division = 'AU'
            and (pursual_exit_action = 'null' or pursual_exit_date_in_future = 'T')
            and pursual_grad_date_null_or_future = 'T'
            and pursual_last_degree_term_now_or_fut = 'T'
        """);

      List<String> sqlsNewView = new ArrayList<String>();
      for (String oldSql : sqlsOldView) {
        sqlsNewView.add(oldSql.replace("AUTHZ_ABAC_PURSUAL_V", "AUTHZ_ABAC_PURSUAL2_V"));
      }

      // get counts from main abac views
      List<List<String>> viewNamesList = new ArrayList<>(); 
      viewNamesList.add((List<String>)(Object)GrouperUtil.toList(GrouperUtil.toList("AUTHZ_ABAC_PURSUAL_V", "AUTHZ_ABAC_PURSUAL2_V")));

      for (List<String> viewNames : viewNamesList) {
        String viewOld = viewNames.get(0);
        String viewNew = viewNames.get(1);

        Long oldCount = new GcDbAccess().connectionName("snowflakeProdAuthzadm").sql("select count(*) from " + viewOld).select(Long.class);
        Long newCount = new GcDbAccess().connectionName("snowflakeProdAuthzadm").sql("select count(*) from " + viewNew).select(Long.class);

        gsh_builtin_gshTemplateOutput.addOutputLine("View: " + viewOld + " (new: " + viewNew + ")");
        gsh_builtin_gshTemplateOutput.addOutputLine(" - Old view result: " + oldCount);
        gsh_builtin_gshTemplateOutput.addOutputLine(" - New view result: " + newCount);
        
      }
      
      // iterate through the groups and print results
      for (int i = 0; i < groupNames.size(); i++) {
        String groupName = groupNames.get(i);

        long groupMemberCount = new GcDbAccess().sql("""
          select count(distinct gm.subject_id)
          from grouper_memberships_lw_v gmlv, grouper_members gm
          where gmlv.group_name = ?
            and gmlv.list_name = 'members'
            and gmlv.subject_id = gm.subject_id
        """).addBindVar(groupName).select(long.class);

        Long oldCount = new GcDbAccess().connectionName("snowflakeProdAuthzadm").sql(sqlsOldView.get(i)).select(Long.class);

        Long newCount = new GcDbAccess().connectionName("snowflakeProdAuthzadm").sql(sqlsNewView.get(i)).select(Long.class);

        gsh_builtin_gshTemplateOutput.addOutputLine("Group: " + groupName);
        gsh_builtin_gshTemplateOutput.addOutputLine(" - Group members: " + groupMemberCount);
        gsh_builtin_gshTemplateOutput.addOutputLine(" - Old view result: " + oldCount);
        gsh_builtin_gshTemplateOutput.addOutputLine(" - New view result: " + newCount);
      }
      
    }
    // if section
    if (StringUtils.equals("section", gsh_input_abacType)) {
      // define test cases
      List<String> groupNames = new ArrayList<String>();
      groupNames.add("penn:wharton:community:whartonCurrentInstructors");
      groupNames.add("penn:wharton:community:whartonEnrolledUGRCourses");
      groupNames.add("penn:wharton:community:whartonEnrolledEMBACourses");
//      groupNames.add("test:nursing:community:students-at-faginhall-non-nursing");

      List<String> sqlsOldView = new ArrayList<String>();
      sqlsOldView.add("""
        SELECT count(DISTINCT section_penn_id)
        FROM AUTHZ_ABAC_SECTION_PERSON_V aapv
        WHERE section_role = 'instructor' AND section_has_pennkey = 'T'
          AND section_school = 'W'
          AND (section_current_term = 'T' or section_future_term = 'T')
      """);
      sqlsOldView.add("""
        SELECT count(DISTINCT section_penn_id)
        FROM AUTHZ_ABAC_SECTION_PERSON_V aapv
        WHERE section_role = 'student' AND section_has_pennkey = 'T'
          AND section_meeting_currently = 'T'
          AND (section_related_divisions like '%w-null%')
      """);
      sqlsOldView.add("""
        SELECT count(DISTINCT section_penn_id)
        FROM AUTHZ_ABAC_SECTION_PERSON_V aapv
        WHERE section_role = 'student' AND section_has_pennkey = 'T'
          AND section_meeting_currently = 'T'
          AND section_primary_division = 'WX'
      """);
//      sqlsOldView.add("""
//        """);

      List<String> sqlsNewView = new ArrayList<String>();
      for (String oldSql : sqlsOldView) {
        sqlsNewView.add(oldSql.replace("AUTHZ_ABAC_SECTION_V", "AUTHZ_ABAC_SECTION2_V"));
      }

      // get counts from main abac views
      List<List<String>> viewNamesList = new ArrayList<>(); 
      viewNamesList.add((List<String>)(Object)GrouperUtil.toList(GrouperUtil.toList("AUTHZ_ABAC_SECTION_V", "AUTHZ_ABAC_SECTION2_V")));
      viewNamesList.add((List<String>)(Object)GrouperUtil.toList(GrouperUtil.toList("AUTHZ_ABAC_SECTION_INSTRUCTOR_V", "AUTHZ_ABAC_SECTION_INSTRUCTOR2_V")));
      viewNamesList.add((List<String>)(Object)GrouperUtil.toList(GrouperUtil.toList("AUTHZ_ABAC_SECTION_STUDENT_V", "AUTHZ_ABAC_SECTION_STUDENT2_V")));
      viewNamesList.add((List<String>)(Object)GrouperUtil.toList(GrouperUtil.toList("AUTHZ_ABAC_SECTION_PERSON_V", "AUTHZ_ABAC_SECTION_PERSON2_V")));

      for (List<String> viewNames : viewNamesList) {
        String viewOld = viewNames.get(0);
        String viewNew = viewNames.get(1);

        Long oldCount = new GcDbAccess().connectionName("snowflakeProdAuthzadm").sql("select count(*) from " + viewOld).select(Long.class);
        Long newCount = new GcDbAccess().connectionName("snowflakeProdAuthzadm").sql("select count(*) from " + viewNew).select(Long.class);

        gsh_builtin_gshTemplateOutput.addOutputLine("View: " + viewOld + " (new: " + viewNew + ")");
        gsh_builtin_gshTemplateOutput.addOutputLine(" - Old view result: " + oldCount);
        gsh_builtin_gshTemplateOutput.addOutputLine(" - New view result: " + newCount);
        
      }
      
      // iterate through the groups and print results
      for (int i = 0; i < groupNames.size(); i++) {
        String groupName = groupNames.get(i);

        long groupMemberCount = new GcDbAccess().sql("""
          select count(distinct gm.subject_id)
          from grouper_memberships_lw_v gmlv, grouper_members gm
          where gmlv.group_name = ?
            and gmlv.list_name = 'members'
            and gmlv.subject_id = gm.subject_id
        """).addBindVar(groupName).select(long.class);

        Long oldCount = new GcDbAccess().connectionName("snowflakeProdAuthzadm").sql(sqlsOldView.get(i)).select(Long.class);

        Long newCount = new GcDbAccess().connectionName("snowflakeProdAuthzadm").sql(sqlsNewView.get(i)).select(Long.class);

        gsh_builtin_gshTemplateOutput.addOutputLine("Group: " + groupName);
        gsh_builtin_gshTemplateOutput.addOutputLine(" - Group members: " + groupMemberCount);
        gsh_builtin_gshTemplateOutput.addOutputLine(" - Old view result: " + oldCount);
        gsh_builtin_gshTemplateOutput.addOutputLine(" - New view result: " + newCount);
      }
      
    }

    // if affiliation type
    if (StringUtils.equals("affiliation", gsh_input_abacType)) {
      List<String> groupNames = new ArrayList<String>();
      groupNames.add("penn:evp:hr:apps:hrimNotifications:hrWorkforceWithPennkey");
      groupNames.add("penn:wharton:community:whartonSponsoredPCOM");
      groupNames.add("penn:med:apps:personFinderGroups:policy:autoProvisionPsomDocusign");

      List<String> sqlsOldView = new ArrayList<String>();
      sqlsOldView.add("""
        SELECT count(DISTINCT aaav.PENN_ID)
        FROM AUTHZ_ABAC_AFFILIATION_V aaav
        WHERE aaav.AFFILIATION_HAS_PENNKEY = 'T'
        AND aaav.affiliation_center = '92'
      """);
      sqlsOldView.add("""
        SELECT count(DISTINCT PENN_ID)
        FROM AUTHZ_ABAC_AFFILIATION_V
        WHERE affiliation_has_pennkey = 'T'
        AND affiliation_center = '07'
        AND affiliation_source IN ('PERSUPLOAD', 'WEB')
      """);
      sqlsOldView.add("""
        SELECT count(DISTINCT PENN_ID)
        FROM AUTHZ_ABAC_AFFILIATION_V
        WHERE affiliation_center = '40'
        AND affiliation_has_pennkey = 'T'
        AND affiliation_code IN ('FAC', 'STAF', 'TEMP')
      """);

      List<String> sqlsNewView = new ArrayList<String>();
      for (String oldSql : sqlsOldView) {
        sqlsNewView.add(oldSql.replace("AUTHZ_ABAC_AFFILIATION_V", "AUTHZ_ABAC_AFFILIATION2_V"));
      }

      List<List<String>> viewNamesList = new ArrayList<>();
      viewNamesList.add((List<String>)(Object)GrouperUtil.toList(GrouperUtil.toList("AUTHZ_ABAC_AFFILIATION_V", "AUTHZ_ABAC_AFFILIATION2_V")));

      for (List<String> viewNames : viewNamesList) {
        String viewOld = viewNames.get(0);
        String viewNew = viewNames.get(1);

        Long oldCount = new GcDbAccess().connectionName("pennCommunity").sql("select count(*) from " + viewOld).select(Long.class);
        Long newCount = new GcDbAccess().connectionName("pennCommunity").sql("select count(*) from " + viewNew).select(Long.class);

        gsh_builtin_gshTemplateOutput.addOutputLine("View: " + viewOld + " (new: " + viewNew + ")");
        gsh_builtin_gshTemplateOutput.addOutputLine(" - Old view result: " + oldCount);
        gsh_builtin_gshTemplateOutput.addOutputLine(" - New view result: " + newCount);
      }

      for (int i = 0; i < groupNames.size(); i++) {
        String groupName = groupNames.get(i);

        long groupMemberCount = new GcDbAccess().sql("""
          select count(distinct gm.subject_id)
          from grouper_memberships_lw_v gmlv, grouper_members gm
          where gmlv.group_name = ?
            and gmlv.list_name = 'members'
            and gmlv.subject_id = gm.subject_id
        """).addBindVar(groupName).select(long.class);

        Long oldCount = new GcDbAccess().connectionName("pennCommunity").sql(sqlsOldView.get(i)).select(Long.class);
        Long newCount = new GcDbAccess().connectionName("pennCommunity").sql(sqlsNewView.get(i)).select(Long.class);

        gsh_builtin_gshTemplateOutput.addOutputLine("Group: " + groupName);
        gsh_builtin_gshTemplateOutput.addOutputLine(" - Group members: " + groupMemberCount);
        gsh_builtin_gshTemplateOutput.addOutputLine(" - Old view result: " + oldCount);
        gsh_builtin_gshTemplateOutput.addOutputLine(" - New view result: " + newCount);
      }
    }

    gsh_builtin_gshTemplateOutput.addOutputLine("Success: ABAC comparison completed");
  }
}

```

GSH template config

GSH template config (minus source)

```
grouperGshTemplate.abacChangeHelper.defaultRunButtonFolderUuidOrName = penn\u003Aetc\u003Atemplates\u003AabacChangeHelper
grouperGshTemplate.abacChangeHelper.displayErrorOutput = true
grouperGshTemplate.abacChangeHelper.folderShowOnDescendants = certainFolders
grouperGshTemplate.abacChangeHelper.folderShowType = certainFolders
grouperGshTemplate.abacChangeHelper.folderUuidToShow = penn\u003Aetc\u003Atemplates\u003AabacChangeHelper
grouperGshTemplate.abacChangeHelper.gshTemplate = //
grouperGshTemplate.abacChangeHelper.input.0.description = ABAC type
grouperGshTemplate.abacChangeHelper.input.0.dropdownCsvValue = affiliation, pursual, section
grouperGshTemplate.abacChangeHelper.input.0.formElementType = dropdown
grouperGshTemplate.abacChangeHelper.input.0.label = ABAC type
grouperGshTemplate.abacChangeHelper.input.0.name = gsh_input_abacType
grouperGshTemplate.abacChangeHelper.input.0.required = true
grouperGshTemplate.abacChangeHelper.moreActionsLabel = ABAC change helper
grouperGshTemplate.abacChangeHelper.numberOfInputs = 1
grouperGshTemplate.abacChangeHelper.runAsType = GrouperSystem
grouperGshTemplate.abacChangeHelper.runButtonGroupOrFolder = folder
grouperGshTemplate.abacChangeHelper.runGshInTransaction = false
grouperGshTemplate.abacChangeHelper.securityRunType = wheel
grouperGshTemplate.abacChangeHelper.showInMoreActions = true
grouperGshTemplate.abacChangeHelper.showOnFolders = true
grouperGshTemplate.abacChangeHelper.templateDescription = ABAC change helper
grouperGshTemplate.abacChangeHelper.templateName = ABAC change helper
grouperGshTemplate.abacChangeHelper.templateType = gsh
grouperGshTemplate.abacChangeHelper.templateVersion = V2

```

## GSH template output

## Make a change to the new view

Add columns/rows to the new view. In this case we are also adding rows since previously only active degree pursuals were represented, but now for student rows, we would like to see the inactive rows as well for alums. See the new result where existing groups do not change but the view has changed
