---
title: "Grouper custom template via GSH analyze groups for orgs"
space: Grouper
pageId: 28549782
version: 5
lastUpdated: 2026-07-01T05:41:15.470Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549782/Grouper+custom+template+via+GSH+analyze+groups+for+orgs
---

Groups which are manual might be able to leverage automatic orgs or supervisory orgs for membership. The result might allow more access than needed, but has the advantage that when people switch orgs or leave Penn they will be deprovisioned. Also, when new people start working for an org or a supervisor, they do not need to request access to the service, which reduces tickets.

Example: a group has three people. There is a supervisory org with 4 people that includes two of those people.

Security situation with original setup: one of the people in the group switches jobs at Penn, and uses their access from their previous job. In the new setup when they change jobs they will be automatically removed from the policy group.

Security situation with new setup: one of the 2 employees in the supervisory org which did not need access uses their access inappropriately. If the supervisory org has more members than are absolutely needed for the policy then that is a risk as well.

This screen in Grouper will analyze a group and see which orgs, supervisory orgs (direct reports), or inherited supervisory orgs (direct and indirect reports) overlap with the members of the inputted group. Note not all inherited supervisory orgs are loaded into PennGroups. If you want some orgs (4 digit) to have inherited supervisory orgs open a ticket.

The first part of the output shows the membership count and how many people are not at Penn anymore.

Orgs which have at least one person in the policy group, and at least 5% of the org is in the policy group. ie if there are 5 people in the org in the policy group, but 1000 people in the org, it will not be listed since that is less than 5%.

Supervisory orgs (name might be outdated) which have at least one person in the policy group, and at least 10% of org is in the policy group. ie if there are 5 people in the org in the policy group, but 1000 people in the org, it will not be listed since that is less than 10%.

If there are inherited supervisory orgs (note not all in PennGroups, request if you need some), which have indirect reports, at least one member, and at least 10%). ie if there are 5 people in the org in the policy group, but 1000 people in the org, it will not be listed since that is less than 10%.

Lets adjust this group as an example.

First lets export the current members, click export.

No one should be in the group who is not in the workforce, so filter members for people who do not work here and remove them

Apply a membership requirement to the group so that only workforce people (and other groups) can be members. Invalid members will be vetoed, or as members leave the University, they will be automatically removed from the group

Based on the output we will add 9014 advancement analytics since all 7 people in the org have access

Then filter the group for indirect members (i.e. in that org), and remove them since they are now indirect

We can do the same process with some inherited supervisory orgs, and then direct supervisory orgs.

We end up with a group that is mostly automated (and people will lose access from the automated parts when they change departments), and manual where it must be. Anyone who leaves the University will be removed from the group whether they are automated or manual.

## Configuration

```
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateRuntime;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

public class Test103analyzeGroupForOrgsAndSupervisors extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
      GshTemplateV2output gshTemplateV2output) {
    
    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();

    String groupName = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_groupName");
    
    if (StringUtils.isBlank(groupName)) {
      groupName = gshTemplateV2input.getGsh_builtin_ownerGroupName();
    }
    
    List<Group> groups = new ArrayList<Group>();
    
    Group theGroup = GroupFinder.findByName(groupName, false);
    if (theGroup != null) {
      groups.add(theGroup);
    } else {
      Stem stem = StemFinder.findByName(groupName, false);
      if (stem == null) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_groupName", "Folder/group not found!");
        return;
      }

      groups.addAll(new GroupFinder().assignParentStemId(stem.getId()).assignStemScope(Scope.ONE).findGroups());
      
    }

    if (groups.size() == 0) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_groupName", "Group(s) not found!");
      return;
    }
    if (groups.size() > 200) {
      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_groupName", "Too many groups found, cannot be more than 200");
      return;
    }

    for (Group group : groups) {
      
      String groupDisplayExtension = group.getDisplayExtension();
          
      // penn:community:employee:org:5148:5148_personorg
      // penn:community:employee:org:USCH:USCH_rolluporg
      
      String query = """
        select 
        (select count(1) from grouper_memberships_lw_v gmlv where gmlv.group_id = gg.id 
        and gmlv.subject_source = 'pennperson' and gmlv.list_name = 'members') mship_count,
        (select count(1) from grouper_memberships_lw_v gmlv where gmlv.group_id = gg.id 
        and gmlv.subject_source = 'pennperson' and gmlv.list_name = 'members'
        and exists (select 1 from grouper_memberships_lw_v gmlv2 where gmlv2.group_name = 'penn:community:employeeOrContractorIncludingUphs' 
        and gmlv2.subject_source = 'pennperson' and gmlv2.list_name = 'members' and gmlv2.member_id = gmlv.member_id)
        ) workforce_count,
        (select count(1) from grouper_memberships_lw_v gmlv where gmlv.group_id = gg.id 
        and gmlv.subject_source = 'pennperson' and gmlv.list_name = 'members'
        and exists (select 1 from grouper_memberships_lw_v gmlv2 where gmlv2.group_name = 'penn:community:activeNonAlumniWithPennname'
        and gmlv2.subject_source = 'pennperson' and gmlv2.list_name = 'members' and gmlv2.member_id = gmlv.member_id)
        ) member_count,
        (select count(1) from grouper_memberships_all_v gmav, grouper_fields gf, grouper_members gm
         where gmav.owner_group_id = gg.id
         and gmav.field_id = gf.id
         and gmav.member_id = gm.id
         and gm.subject_source = 'pennperson'
         and gf.name = 'members'
         and gmav.mship_type = 'immediate') mship_direct_count,
         (select count(1) from grouper_memberships_all_v gmav, grouper_fields gf, grouper_members gm
         where gmav.owner_group_id = gg.id
         and gmav.field_id = gf.id
         and gmav.member_id = gm.id
         and gm.subject_source = 'pennperson'
         and gf.name = 'members'
         and gmav.mship_type = 'immediate'
         and exists (select 1 from grouper_memberships_lw_v gmlv2 where gmlv2.group_name = 'penn:community:employeeOrContractorIncludingUphs' 
         and gmlv2.subject_source = 'pennperson' and gmlv2.list_name = 'members' and gmlv2.member_id = gmav.member_id)) workforce_direct_count,
         (select count(1) from grouper_memberships_all_v gmav, grouper_fields gf, grouper_members gm
         where gmav.owner_group_id = gg.id
         and gmav.field_id = gf.id
         and gmav.member_id = gm.id
         and gm.subject_source = 'pennperson'
         and gf.name = 'members'
         and gmav.mship_type = 'immediate'
         and exists (select 1 from grouper_memberships_lw_v gmlv2 where gmlv2.group_name = 'penn:community:activeNonAlumniWithPennname'
         and gmlv2.subject_source = 'pennperson' and gmlv2.list_name = 'members' and gmlv2.member_id = gmav.member_id)
        ) member_direct_count
        from grouper_groups gg where gg.name = ?

          """;
      
      Object[] mshipCountWorkforceCountMemberCount = new GcDbAccess().sql(query).addBindVar(group.getName()).select(Object[].class);
  
      int mshipCount = GrouperUtil.intValue(mshipCountWorkforceCountMemberCount[0]);
      int workforceCount = GrouperUtil.intValue(mshipCountWorkforceCountMemberCount[1]);
      int memberCount = GrouperUtil.intValue(mshipCountWorkforceCountMemberCount[2]);
      int mshipDirectCount = GrouperUtil.intValue(mshipCountWorkforceCountMemberCount[3]);
      int workforceDirectCount = GrouperUtil.intValue(mshipCountWorkforceCountMemberCount[4]);
      int memberDirectCount = GrouperUtil.intValue(mshipCountWorkforceCountMemberCount[5]);
      gsh_builtin_gshTemplateOutput.addOutputLine(groupDisplayExtension + " has " + mshipCount + " members (" + mshipDirectCount + " direct), " 
        + workforceCount + " people in workforce (" + workforceDirectCount + " direct), " + memberCount 
        + " people are 'Members of Penn' (" + memberDirectCount + " direct) (e.g. faculty/students/staff) of Penn.");
      
      query = """
        select 
        (select count(1) from grouper_memberships_lw_v gmlv3 where gmlv3.group_id = gg.id 
        and gmlv3.subject_source = 'pennperson' and gmlv3.list_name = 'members') mship_org_count,
        (select count(1) from grouper_memberships_lw_v gmlv4 
        where gmlv4.group_name = ?
        and gmlv4.subject_source = 'pennperson' and gmlv4.list_name = 'members'
        and exists (select 1 from grouper_memberships_lw_v gmlv5 where gmlv5.group_id = gg.id 
        and gmlv5.member_id = gmlv4.member_id  and gmlv5.list_name = 'members')
        ) vpn_mship_count, display_extension,
        extension, id,
        (select count(1) from grouper_memberships_all_v gmav, grouper_fields gf, grouper_members gm, grouper_groups gg2
         where gmav.owner_group_id = gg2.id
         and gg2.name = ?
         and gmav.field_id = gf.id
         and gmav.member_id = gm.id
         and gm.subject_source = 'pennperson'
         and gf.name = 'members'
         and gmav.mship_type = 'immediate'
        and exists (select 1 from grouper_memberships_lw_v gmlv5 where gmlv5.group_id = gg.id 
        and gmlv5.member_id = gmav.member_id  and gmlv5.list_name = 'members')
        ) vpn_mship_direct_count
        from grouper_groups gg where name like 'penn:community:employee:org:%'
        and exists (select 1 from grouper_memberships_lw_v gmlv where gmlv.group_name = gg.name 
        and gmlv.group_name like 'penn:community:employee:org:%'
        and gmlv.subject_source = 'pennperson' and gmlv.list_name = 'members'
        and gmlv.member_id in (select gmlv2.member_id from grouper_memberships_lw_v gmlv2
        where gmlv2.subject_source = 'pennperson' and gmlv2.list_name = 'members' 
        and gmlv2.group_name = ?)
        )
        order by 2
          """;
      List<Object[]> mshipOrgCountMshipGroupCountDisplayExtensionExtensions = new GcDbAccess().sql(query).addBindVar(group.getName()).
          addBindVar(group.getName()).addBindVar(group.getName()).selectList(Object[].class);
      boolean foundOrg = false;
      gsh_builtin_gshTemplateOutput.addOutputLine("Orgs are displayed if there is more than one org member in " + groupDisplayExtension + ", and at least 5% matches.");
  
      for (Object[] mshipOrgCountMshipGroupCountDisplayExtensionExtension : mshipOrgCountMshipGroupCountDisplayExtensionExtensions) {
        int mshipOrgCount = GrouperUtil.intValue(mshipOrgCountMshipGroupCountDisplayExtensionExtension[0]);
        int mshipGroupCount = GrouperUtil.intValue(mshipOrgCountMshipGroupCountDisplayExtensionExtension[1]);
        String displayExtension = (String)mshipOrgCountMshipGroupCountDisplayExtensionExtension[2];
        String extension = (String)mshipOrgCountMshipGroupCountDisplayExtensionExtension[3];
        String id = (String)mshipOrgCountMshipGroupCountDisplayExtensionExtension[4];
        int mshipGroupDirectCount = GrouperUtil.intValue(mshipOrgCountMshipGroupCountDisplayExtensionExtension[5]);
          
        if (mshipGroupCount < 2) {
          continue;
        }
        if (!extension.endsWith("_personorg") && !extension.endsWith("_rolluporg")) {
          continue;
        }
        if (mshipGroupCount * 20 < mshipOrgCount) {
          continue;
        }
        foundOrg = true;
        // we found one
        String org = GrouperUtil.prefixOrSuffix(extension, "_", true);
  
        gsh_builtin_gshTemplateOutput.addOutputLine("Org: " + org + " (<a href=\"https://grouper.apps.upenn.edu/grouper/gr"
            + "ouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=" + id + "\">" + displayExtension + "</a>) has " + mshipOrgCount + 
            " members with " + mshipGroupCount + " members (" + mshipGroupDirectCount + " direct) in " + groupDisplayExtension + ": " + extension);
  
        
      }
      if (!foundOrg) {
        gsh_builtin_gshTemplateOutput.addOutputLine("No orgs matched the members of the group");
        
      }
  
      query = """
          select 
          (select count(1) from grouper_memberships_lw_v gmlv3 where gmlv3.group_id = gg.id 
          and gmlv3.subject_source = 'pennperson' and gmlv3.list_name = 'members') mship_sup_count,
          (select count(1) from grouper_memberships_lw_v gmlv4 
          where gmlv4.group_name = ?
          and gmlv4.subject_source = 'pennperson' and gmlv4.list_name = 'members'
          and exists (select 1 from grouper_memberships_lw_v gmlv5 where gmlv5.group_id = gg.id 
          and gmlv5.member_id = gmlv4.member_id  and gmlv5.list_name = 'members')
          ) vpn_mship_count, display_extension,
          extension, id,
        (select count(1) from grouper_memberships_all_v gmav, grouper_fields gf, grouper_members gm, grouper_groups gg2
         where gmav.owner_group_id = gg2.id
         and gg2.name = ?
         and gmav.field_id = gf.id
         and gmav.member_id = gm.id
         and gm.subject_source = 'pennperson'
         and gf.name = 'members'
         and gmav.mship_type = 'immediate'
        and exists (select 1 from grouper_memberships_lw_v gmlv5 where gmlv5.group_id = gg.id 
        and gmlv5.member_id = gmav.member_id  and gmlv5.list_name = 'members')
        ) vpn_mship_direct_count
          from grouper_groups gg where name like 'penn:community:employee:supervisoryOrg:%'
          and exists (select 1 from grouper_memberships_lw_v gmlv where gmlv.group_name = gg.name 
          and gmlv.group_name like 'penn:community:employee:supervisoryOrg:%'
          and gmlv.subject_source = 'pennperson' and gmlv.list_name = 'members'
          and gmlv.member_id in (select gmlv2.member_id from grouper_memberships_lw_v gmlv2
          where gmlv2.subject_source = 'pennperson' and gmlv2.list_name = 'members' 
          and gmlv2.group_name = ?)
          )
          order by 2 desc
            """;
      mshipOrgCountMshipGroupCountDisplayExtensionExtensions = new GcDbAccess().sql(query).addBindVar(group.getName()).
          addBindVar(group.getName()).addBindVar(group.getName()).selectList(Object[].class);
      foundOrg = false;
      gsh_builtin_gshTemplateOutput.addOutputLine("Supervisory orgs (direct reports only, includes supervisor) are displayed if there is more than one org member in " + groupDisplayExtension + ", and at least 10% matches.  Note the name might be outdated.");
  
      for (Object[] mshipOrgCountMshipGroupCountDisplayExtensionExtension : mshipOrgCountMshipGroupCountDisplayExtensionExtensions) {
        int mshipOrgCount = GrouperUtil.intValue(mshipOrgCountMshipGroupCountDisplayExtensionExtension[0]);
        int mshipGroupCount = GrouperUtil.intValue(mshipOrgCountMshipGroupCountDisplayExtensionExtension[1]);
        String displayExtension = (String)mshipOrgCountMshipGroupCountDisplayExtensionExtension[2];
        String extension = (String)mshipOrgCountMshipGroupCountDisplayExtensionExtension[3];
        String id = (String)mshipOrgCountMshipGroupCountDisplayExtensionExtension[4];
        int mshipGroupDirectCount = GrouperUtil.intValue(mshipOrgCountMshipGroupCountDisplayExtensionExtension[5]);
        
        if (mshipGroupCount < 2) {
          continue;
        }
        if (mshipGroupCount * 10 < mshipOrgCount) {
          continue;
        }
        foundOrg = true;
  
        gsh_builtin_gshTemplateOutput.addOutputLine("Supervisory org: <a href=\"https://grouper.apps.upenn.edu/grouper/gr"
            + "ouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=" + id + "\">" + displayExtension + "</a> has " + mshipOrgCount + 
            " members with " + mshipGroupCount + " members (" + mshipGroupDirectCount + " direct) in " + groupDisplayExtension + ": " + extension);
  
        
      }
      if (!foundOrg) {
        gsh_builtin_gshTemplateOutput.addOutputLine("No supervisory orgs matched the members of the group");
        
      }
  
      query = """
          select 
          (select count(1) from grouper_memberships_lw_v gmlv3 where gmlv3.group_id = gg.id 
          and gmlv3.subject_source = 'pennperson' and gmlv3.list_name = 'members') mship_sup_count,
          (select count(1) from grouper_memberships_lw_v gmlv4 
          where gmlv4.group_name = ?
          and gmlv4.subject_source = 'pennperson' and gmlv4.list_name = 'members'
          and exists (select 1 from grouper_memberships_lw_v gmlv5 where gmlv5.group_id = gg.id 
          and gmlv5.member_id = gmlv4.member_id  and gmlv5.list_name = 'members')
          ) vpn_mship_count, display_extension,
          extension, id,
        (select count(1) from grouper_memberships_all_v gmav, grouper_fields gf, grouper_members gm, grouper_groups gg2
         where gmav.owner_group_id = gg2.id
         and gg2.name = ?
         and gmav.field_id = gf.id
         and gmav.member_id = gm.id
         and gm.subject_source = 'pennperson'
         and gf.name = 'members'
         and gmav.mship_type = 'immediate'
        and exists (select 1 from grouper_memberships_lw_v gmlv5 where gmlv5.group_id = gg.id 
        and gmlv5.member_id = gmav.member_id  and gmlv5.list_name = 'members')
        ) vpn_mship_direct_count
          from grouper_groups gg where name like 'penn:community:employee:supervisoryOrgInherit:%'
          and exists (select 1 from grouper_memberships_lw_v gmlv where gmlv.group_name = gg.name 
          and gmlv.group_name like 'penn:community:employee:supervisoryOrgInherit:%'
          and gmlv.subject_source = 'pennperson' and gmlv.list_name = 'members'
          and gmlv.member_id in (select gmlv2.member_id from grouper_memberships_lw_v gmlv2
          where gmlv2.subject_source = 'pennperson' and gmlv2.list_name = 'members' 
          and gmlv2.group_name = ?)
          ) and exists (select 1 from grouper_memberships_lw_v gmlv6, grouper_groups gg2
          where gmlv6.group_id = gg.id and gg2.id = gmlv6.subject_id 
          and gg2.name like 'penn:community:employee:supervisoryOrgInherit:%'
          and gmlv6.list_name = 'members' and gmlv6.subject_source = 'g:gsa')
          order by 2 desc
            """;
      mshipOrgCountMshipGroupCountDisplayExtensionExtensions = new GcDbAccess().sql(query).addBindVar(group.getName()).
          addBindVar(group.getName()).addBindVar(group.getName()).selectList(Object[].class);
      foundOrg = false;
      gsh_builtin_gshTemplateOutput.addOutputLine("Inherited supervisory orgs (includes supervisor, direct reports and indirect reports) are displayed if there is more than one org member in " + groupDisplayExtension + ", at least one indirect report, and at least 10% matches.  Note the name might be outdated.");
  
      for (Object[] mshipOrgCountMshipGroupCountDisplayExtensionExtension : mshipOrgCountMshipGroupCountDisplayExtensionExtensions) {
        int mshipOrgCount = GrouperUtil.intValue(mshipOrgCountMshipGroupCountDisplayExtensionExtension[0]);
        int mshipGroupCount = GrouperUtil.intValue(mshipOrgCountMshipGroupCountDisplayExtensionExtension[1]);
        String displayExtension = (String)mshipOrgCountMshipGroupCountDisplayExtensionExtension[2];
        String extension = (String)mshipOrgCountMshipGroupCountDisplayExtensionExtension[3];
        String id = (String)mshipOrgCountMshipGroupCountDisplayExtensionExtension[4];
        int mshipGroupDirectCount = GrouperUtil.intValue(mshipOrgCountMshipGroupCountDisplayExtensionExtension[5]);
        
        if (mshipGroupCount < 2) {
          continue;
        }
        if (mshipGroupCount * 10 < mshipOrgCount) {
          continue;
        }
        foundOrg = true;
  
        gsh_builtin_gshTemplateOutput.addOutputLine("Inh sup org: <a href=\"https://grouper.apps.upenn.edu/grouper/gr"
            + "ouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId=" + id + "\">" + displayExtension + "</a> has " + mshipOrgCount + 
            " members with " + mshipGroupCount + " members (" + mshipGroupDirectCount + " direct) in " + groupDisplayExtension + ": " + extension);
  
        
      }
      if (!foundOrg) {
        gsh_builtin_gshTemplateOutput.addOutputLine("No inherited supervisory orgs matched the members of the group");
        
      }
      gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("<br /><br />");
    }
    
    gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("Success: analysis complete");
    gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("NONE");

  }

  
  public static void main(String[] args) {

    GrouperStartup.startup();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        // subject using the app
        Subject subject = SubjectFinder.findByIdAndSource("10021368", "pennperson", true);
        
        Test103analyzeGroupForOrgsAndSupervisors test103analyzeGroupForOrgsAndSupervisors = new Test103analyzeGroupForOrgsAndSupervisors();
        GshTemplateV2input gshTemplateV2input = new GshTemplateV2input();
        gshTemplateV2input.setGsh_builtin_subject(subject);

        // call the template for the group
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_groupName", "penn:isc:ait:apps:vpn:service:policy:SP2_VPN_Users");
        GshTemplateRuntime gshTemplateRuntime = new GshTemplateRuntime();

        gshTemplateRuntime.setTemplateConfigId("analyzeGroupForOrgsAndSupervisors");
        gshTemplateV2input.setGsh_builtin_gshTemplateRuntime(gshTemplateRuntime);

        GshTemplateV2output gshTemplateV2output = new GshTemplateV2output();
        
        test103analyzeGroupForOrgsAndSupervisors.gshRunLogic(gshTemplateV2input, gshTemplateV2output);
        
        // print out all the output
        System.out.println(gshTemplateV2output.getGsh_builtin_gshTemplateOutput());

        return null;
      }
    });
    
    System.exit(0);

  }

}
```

```
grouperGshTemplate.analyzeGroupForOrgsAndSupervisorsGroup.defaultRunButtonGroupUuidOrName = test\u003AtestGroup
grouperGshTemplate.analyzeGroupForOrgsAndSupervisorsGroup.displayErrorOutput = true
grouperGshTemplate.analyzeGroupForOrgsAndSupervisorsGroup.groupShowType = allGroups
grouperGshTemplate.analyzeGroupForOrgsAndSupervisorsGroup.groupUuidCanRun = penn\u003Aetc\u003Atemplates\u003AanalyzeGroupForOrgsAndSupervisors\u003AanalyzeGroupForOrgsAndSupervisors
grouperGshTemplate.analyzeGroupForOrgsAndSupervisorsGroup.gshTemplate = //
grouperGshTemplate.analyzeGroupForOrgsAndSupervisorsGroup.moreActionsLabel = Analyze group for orgs
grouperGshTemplate.analyzeGroupForOrgsAndSupervisorsGroup.runAsType = GrouperSystem
grouperGshTemplate.analyzeGroupForOrgsAndSupervisorsGroup.runButtonGroupOrFolder = group
grouperGshTemplate.analyzeGroupForOrgsAndSupervisorsGroup.securityRunType = specifiedGroup
grouperGshTemplate.analyzeGroupForOrgsAndSupervisorsGroup.showInMoreActions = true
grouperGshTemplate.analyzeGroupForOrgsAndSupervisorsGroup.showOnGroups = true
grouperGshTemplate.analyzeGroupForOrgsAndSupervisorsGroup.templateDescription = See if there are orgs or supervisors which can help populate a group automatically.<br />Note not all hierarchical supervisory orgs are loaded.<br />If you want hierarchical supervisory orgs in a department open a ticket.<br />All groups in a folder can be organized with a different template
grouperGshTemplate.analyzeGroupForOrgsAndSupervisorsGroup.templateName = Analyze group for orgs
grouperGshTemplate.analyzeGroupForOrgsAndSupervisorsGroup.templateVersion = V2

```

Configuration:
