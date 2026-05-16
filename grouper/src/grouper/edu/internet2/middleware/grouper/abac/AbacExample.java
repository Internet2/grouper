package edu.internet2.middleware.grouper.abac;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.misc.CompositeType;

public class AbacExample extends GshTemplateV2 {

  private static final String BASE = "app:litellm_c:service:policy";

  private static final String SCRIPTED_BASE = "app:litellm_ag:service:policy";

  private static final String SCRIPTED_ROW_BASE = "app:litellm_a:service:policy";

  public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
      GshTemplateV2output gshTemplateV2output) {

    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();

    createPolicyGroups();
    createScriptedPolicyGroup();
    createRowBasedScriptedPolicyGroup();

    gsh_builtin_gshTemplateOutput.addOutputLine("Created litellm_rw composite policy under " + BASE);
    gsh_builtin_gshTemplateOutput.addOutputLine("Created litellm_rw scripted (group-based) policy under " + SCRIPTED_BASE);
    gsh_builtin_gshTemplateOutput.addOutputLine("Created litellm_rw scripted (row-based) policy under " + SCRIPTED_ROW_BASE);
  }

  /**
   * Builds the service_litellm_rw policy group structure under app:litellm_c:service:policy:
   *
   *   service_litellm_rw                       composite COMPLEMENT = _allow MINUS _deny_manual
   *   service_litellm_rw_allow                 manual group, members: _allow_manual + _allow_automatic
   *   service_litellm_rw_allow_manual          manual group
   *   service_litellm_rw_allow_automatic       composite INTERSECTION = _allow_automatic_orgs AND ref:affiliations:affiliation_staff
   *   service_litellm_rw_allow_automatic_orgs  manual group, members: basis:orgs:org_AB + basis:orgs:org_BC
   *   service_litellm_rw_deny_manual           manual group
   *
   * Assumes basis:orgs:org_AB, basis:orgs:org_BC, and ref:affiliations:affiliation_staff already exist.
   */
  public static void createPolicyGroups() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {

      new StemSave(grouperSession).assignName(BASE).assignCreateParentStemsIfNotExist(true).save();

      Group orgAB = GroupFinder.findByName(grouperSession, "basis:orgs:org_AB", true);
      Group orgBC = GroupFinder.findByName(grouperSession, "basis:orgs:org_BC", true);
      Group affiliationStaff = GroupFinder.findByName(grouperSession, "ref:affiliations:affiliation_staff", true);

      // intermediate: orgs union
      Group autoOrgs = new GroupSave(grouperSession)
          .assignName(BASE + ":service_litellm_rw_allow_automatic_orgs")
          .assignDisplayExtension("service_litellm_rw_allow_automatic_orgs")
          .assignDescription("union of orgs whose staff are allowed").save();
      autoOrgs.addMember(orgAB.toSubject(), false);
      autoOrgs.addMember(orgBC.toSubject(), false);

      // intermediate: orgs INTERSECT staff
      Group autoIntersect = new GroupSave(grouperSession)
          .assignName(BASE + ":service_litellm_rw_allow_automatic")
          .assignDisplayExtension("service_litellm_rw_allow_automatic")
          .assignDescription("staff within the allowed orgs").save();
      autoIntersect.addCompositeMember(CompositeType.INTERSECTION, autoOrgs, affiliationStaff);

      // manual allow
      new GroupSave(grouperSession)
          .assignName(BASE + ":service_litellm_rw_allow_manual")
          .assignDisplayExtension("service_litellm_rw_allow_manual")
          .assignDescription("manually-added users allowed access").save();

      // allow = manual_allow + automatic (effective union via membership)
      Group allow = new GroupSave(grouperSession)
          .assignName(BASE + ":service_litellm_rw_allow")
          .assignDisplayExtension("service_litellm_rw_allow")
          .assignDescription("everyone allowed (manual + automatic)").save();
      allow.addMember(GroupFinder.findByName(grouperSession, BASE + ":service_litellm_rw_allow_manual", true).toSubject(), false);
      allow.addMember(GroupFinder.findByName(grouperSession, BASE + ":service_litellm_rw_allow_automatic", true).toSubject(), false);

      // manual deny
      Group denyManual = new GroupSave(grouperSession)
          .assignName(BASE + ":service_litellm_rw_deny_manual")
          .assignDisplayExtension("service_litellm_rw_deny_manual")
          .assignDescription("manually-added users denied access").save();

      // policy = allow MINUS deny_manual
      Group policy = new GroupSave(grouperSession)
          .assignName(BASE + ":service_litellm_rw")
          .assignDisplayExtension("service_litellm_rw")
          .assignDescription("litellm read/write policy").save();
      policy.addCompositeMember(CompositeType.COMPLEMENT, allow, denyManual);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Builds the scripted/ABAC variant under app:litellm_ag:service:policy:
   *
   *   service_litellm_rw                scripted ABAC group; one JEXL expression replaces the
   *                                     composite chain. Members = allow_manual OR
   *                                     ((org_AB or org_BC) AND affiliation_staff), MINUS deny_manual
   *   service_litellm_rw_allow_manual   manual group
   *   service_litellm_rw_deny_manual    manual group
   *
   * Assumes basis:orgs:org_AB, basis:orgs:org_BC, and ref:affiliations:affiliation_staff exist.
   * After creation, run OTHER_JOB_grouperLoaderJexlScriptFullSync to populate the scripted group.
   */
  public static void createScriptedPolicyGroup() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {

      new StemSave(grouperSession).assignName(SCRIPTED_BASE).assignCreateParentStemsIfNotExist(true).save();

      new GroupSave(grouperSession)
          .assignName(SCRIPTED_BASE + ":service_litellm_rw_allow_manual")
          .assignDisplayExtension("service_litellm_rw_allow_manual")
          .assignDescription("manually-added users allowed access").save();

      new GroupSave(grouperSession)
          .assignName(SCRIPTED_BASE + ":service_litellm_rw_deny_manual")
          .assignDisplayExtension("service_litellm_rw_deny_manual")
          .assignDescription("manually-added users denied access").save();

      Group policy = new GroupSave(grouperSession)
          .assignName(SCRIPTED_BASE + ":service_litellm_rw")
          .assignDisplayExtension("service_litellm_rw")
          .assignDescription("litellm read/write policy (scripted ABAC)").save();

      AttributeDefName markerName = AttributeDefNameFinder.findByName(
          "etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
      AttributeDefName scriptName = AttributeDefNameFinder.findByName(
          "etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

      AttributeAssign markerAssign = new AttributeAssignSave(grouperSession)
          .assignOwnerGroup(policy)
          .assignAttributeDefName(markerName)
          .save();

      String jexl = "( '" + SCRIPTED_BASE + ":service_litellm_rw_allow_manual'"
          + " or"
          + " ( ( 'basis:orgs:org_AB' or 'basis:orgs:org_BC' )"
          + " and 'ref:affiliations:affiliation_staff' )"
          + " )"
          + " and !'" + SCRIPTED_BASE + ":service_litellm_rw_deny_manual'";

      markerAssign.getAttributeValueDelegate().assignValueString(scriptName.getName(), jexl);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Builds the scripted/ABAC variant that queries the entity data row 'affiliation'
   * instead of org/staff reference groups. Under app:litellm_a:service:policy:
   *
   *   service_litellm_rw                scripted ABAC group; JEXL uses entity.hasRow('affiliation', ...)
   *                                     to match (affiliation_org in AB,BC) AND affiliation_name == staff,
   *                                     unioned with allow_manual and minus deny_manual
   *   service_litellm_rw_allow_manual   manual group
   *   service_litellm_rw_deny_manual    manual group
   *
   * Requires the 'affiliation' data row to be configured with fields affiliation_org and
   * affiliation_name, and the data provider daemon to have populated rows.
   */
  public static void createRowBasedScriptedPolicyGroup() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {

      new StemSave(grouperSession).assignName(SCRIPTED_ROW_BASE).assignCreateParentStemsIfNotExist(true).save();

      new GroupSave(grouperSession)
          .assignName(SCRIPTED_ROW_BASE + ":service_litellm_rw_allow_manual")
          .assignDisplayExtension("service_litellm_rw_allow_manual")
          .assignDescription("manually-added users allowed access").save();

      new GroupSave(grouperSession)
          .assignName(SCRIPTED_ROW_BASE + ":service_litellm_rw_deny_manual")
          .assignDisplayExtension("service_litellm_rw_deny_manual")
          .assignDescription("manually-added users denied access").save();

      Group policy = new GroupSave(grouperSession)
          .assignName(SCRIPTED_ROW_BASE + ":service_litellm_rw")
          .assignDisplayExtension("service_litellm_rw")
          .assignDescription("litellm read/write policy (scripted ABAC, row-based)").save();

      AttributeDefName markerName = AttributeDefNameFinder.findByName(
          "etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
      AttributeDefName scriptName = AttributeDefNameFinder.findByName(
          "etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

      AttributeAssign markerAssign = new AttributeAssignSave(grouperSession)
          .assignOwnerGroup(policy)
          .assignAttributeDefName(markerName)
          .save();

      String jexl = "( '" + SCRIPTED_ROW_BASE + ":service_litellm_rw_allow_manual'"
          + " or"
          + " entity.hasRow('affiliation', \"( affiliation_org == 'AB' or affiliation_org == 'BC' )"
          + " and affiliation_name == 'staff' \")"
          + " )"
          + " and !'" + SCRIPTED_ROW_BASE + ":service_litellm_rw_deny_manual'";

      markerAssign.getAttributeValueDelegate().assignValueString(scriptName.getName(), jexl);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
