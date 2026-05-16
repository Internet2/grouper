package edu.internet2.middleware.grouper.abac;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerTestUtils;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

public class AbacExample {

  private static final String TABLE_NAME = "testgrouper_abac_affiliation";

  private static final String[] ORG_SUFFIXES = new String[] {
      "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ",
      "BA", "BB", "BC", "BD", "BE", "CA", "CB", "CC", "CD", "CE" };

  // student is intentionally excluded from rows
  private static final String[] AFFILIATIONS = new String[] {
      "staff", "student", "guest", "faculty", "alumni", "employee", "affiliate" };

  private static final String COMPOSITE_BASE = "app:litellm_c:service:policy";

  private static final String SCRIPTED_BASE = "app:litellm_ag:service:policy";

  private static final String SCRIPTED_ROW_BASE = "app:litellm_a:service:policy";

  public static void main(String[] args) {
    // setupAbacTestData();                  // groups already populated; re-running would add more random members
    setupAffiliationTableAndProvider();
    createPolicyGroups();
    createScriptedPolicyGroup();
    createRowBasedScriptedPolicyGroup();
  }

  // ---------------------------------------------------------------------------
  // Base setup: LDAP subject source, org groups, affiliation groups
  // ---------------------------------------------------------------------------

  public static void setupAbacTestData() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {

      LdapProvisionerTestUtils.setupLdapExternalSystem();
      LdapProvisionerTestUtils.setupSubjectSource();

      new StemSave(grouperSession).assignName("ref:affiliations").save();
      new StemSave(grouperSession).assignName("basis:orgs").save();

      List<Subject> ldapPool = collectLdapSubjects();
      System.out.println("Found " + ldapPool.size() + " candidate ldap subjects");
      if (ldapPool.isEmpty()) {
        throw new RuntimeException("No ldap subjects found, cannot populate groups");
      }

      Random random = new Random();

      for (String suffix : ORG_SUFFIXES) {
        String groupName = "basis:orgs:org_" + suffix;
        Group group = new GroupSave(grouperSession).assignName(groupName).save();
        int memberCount = 5 + random.nextInt(10);
        Set<String> added = new HashSet<String>();
        int attempts = 0;
        while (added.size() < memberCount && attempts < memberCount * 5) {
          attempts++;
          Subject subject = ldapPool.get(random.nextInt(ldapPool.size()));
          if (added.add(subject.getId())) {
            group.addMember(subject, false);
          }
        }
        System.out.println("Populated " + groupName + " with " + added.size() + " members");
      }

      for (String aff : AFFILIATIONS) {
        String groupName = "ref:affiliations:affiliation_" + aff;
        Group group = new GroupSave(grouperSession).assignName(groupName).save();
        int memberCount = 8 + random.nextInt(15);
        Set<String> added = new HashSet<String>();
        int attempts = 0;
        while (added.size() < memberCount && attempts < memberCount * 5) {
          attempts++;
          Subject subject = ldapPool.get(random.nextInt(ldapPool.size()));
          if (added.add(subject.getId())) {
            group.addMember(subject, false);
          }
        }
        System.out.println("Populated " + groupName + " with " + added.size() + " members");
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // ---------------------------------------------------------------------------
  // Affiliation table + data field/row/provider/query/daemon
  // ---------------------------------------------------------------------------

  /**
   * Creates testgrouper_abac_affiliation, populates rows derived from the org/affiliation
   * group memberships (no students), then registers data fields, a data row, a provider,
   * a query, and a full-sync daemon job. Runs the daemon and verifies the results.
   */
  public static void setupAffiliationTableAndProvider() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {

      createAffiliationTable();

      // org -> set of subjectIds
      Map<String, Set<String>> orgToSubjects = new HashMap<String, Set<String>>();
      for (String suffix : ORG_SUFFIXES) {
        orgToSubjects.put(suffix, subjectIdsInGroup("basis:orgs:org_" + suffix));
      }

      // subjectId -> set of non-student affiliations they hold
      Map<String, Set<String>> subjectToAffiliations = new HashMap<String, Set<String>>();
      for (String aff : AFFILIATIONS) {
        if ("student".equals(aff)) {
          continue;
        }
        Set<String> members = subjectIdsInGroup("ref:affiliations:affiliation_" + aff);
        for (String subjectId : members) {
          Set<String> set = subjectToAffiliations.get(subjectId);
          if (set == null) {
            set = new HashSet<String>();
            subjectToAffiliations.put(subjectId, set);
          }
          set.add(aff);
        }
      }

      // build rows: every (subject, org) where subject is in that org, paired with each
      // non-student affiliation they actually hold; subjects in an org but in no non-student
      // affiliation group get no row
      Set<String> rowKeys = new HashSet<String>();
      List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
      for (String suffix : ORG_SUFFIXES) {
        for (String subjectId : orgToSubjects.get(suffix)) {
          Set<String> affs = subjectToAffiliations.get(subjectId);
          if (affs == null || affs.isEmpty()) {
            continue;
          }
          for (String aff : affs) {
            String key = subjectId + "|" + suffix + "|" + aff;
            if (rowKeys.add(key)) {
              batchBindVars.add(toList(subjectId, suffix, aff));
            }
          }
        }
      }

      new GcDbAccess().sql("delete from " + TABLE_NAME).executeSql();
      new GcDbAccess().sql("insert into " + TABLE_NAME + " (subject_id, org_name, affiliation_name) values (?, ?, ?)")
          .batchBindVars(batchBindVars).executeBatchSql();
      System.out.println("Inserted " + batchBindVars.size() + " rows into " + TABLE_NAME);

      configureDataFieldsAndProvider();
      runProviderDaemon();
      verifyAgainstMemberships(orgToSubjects, subjectToAffiliations);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  private static void createAffiliationTable() {
    try {
      new GcDbAccess().sql("select count(*) from " + TABLE_NAME).select(int.class);
    } catch (Exception e) {
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          Database database = ddlVersionBean.getDatabase();
          Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, TABLE_NAME);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "subject_id", Types.VARCHAR, "40", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "org_name", Types.VARCHAR, "40", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "affiliation_name", Types.VARCHAR, "40", false, true);
        }
      });
    }
  }

  private static void configureDataFieldsAndProvider() {

    // privacy realm
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmName").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmPublic").value("true").store();

    // data fields
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliation_org.fieldAliases").value("affiliation_org").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliation_org.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliation_org.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliation_org.descriptionHtml").value("org code for affiliation row").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliation_name.fieldAliases").value("affiliation_name").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliation_name.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliation_name.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliation_name.descriptionHtml").value("affiliation name for affiliation row").store();

    // data row containing both fields, both keys (so each (subject, org, affiliation) is a unique row)
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowAliases").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowNumberOfDataFields").value("2").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.colDataFieldConfigId").value("affiliation_org").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.rowKeyField").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.1.colDataFieldConfigId").value("affiliation_name").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.1.rowKeyField").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.descriptionHtml").value("subject affiliation per org").store();

    // provider
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.abacAffiliation.name").value("abacAffiliation").store();

    // provider query that reads the affiliation table
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerConfigId").value("abacAffiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerQueryType").value("sql").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerQuerySqlConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerQuerySqlQuery").value("select subject_id, org_name, affiliation_name from " + TABLE_NAME).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerQueryDataStructure").value("row").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerQueryRowConfigId").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerQuerySubjectIdAttribute").value("subject_id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerQuerySubjectIdType").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerQuerySubjectSourceId").value("personLdapSource").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerQueryNumberOfDataFields").value("2").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerQueryDataField.0.providerDataFieldConfigId").value("affiliation_org").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerQueryDataField.0.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerQueryDataField.0.providerDataFieldAttribute").value("org_name").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerQueryDataField.1.providerDataFieldConfigId").value("affiliation_name").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerQueryDataField.1.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.abacAffiliationQuery.providerQueryDataField.1.providerDataFieldAttribute").value("affiliation_name").store();

    // full sync daemon job
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.abacAffiliationFullSync.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.abacAffiliationFullSync.dataProviderConfigId").value("abacAffiliation").store();

    ConfigPropertiesCascadeBase.clearCache();
  }

  private static void runProviderDaemon() {
    System.out.println("Running OTHER_JOB_abacAffiliationFullSync...");
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_abacAffiliationFullSync");
    System.out.println("Daemon done");
  }

  private static void verifyAgainstMemberships(Map<String, Set<String>> orgToSubjects,
      Map<String, Set<String>> subjectToAffiliations) {

    System.out.println();
    System.out.println("=== Verification ===");

    int tableRowCount = new GcDbAccess().sql("select count(1) from " + TABLE_NAME).select(int.class).intValue();
    int dataRowAssignCount = new GcDbAccess()
        .sql("select count(1) from grouper_data_row_assign_v where data_row_config_id = 'affiliation'")
        .select(int.class).intValue();
    System.out.println("Rows in " + TABLE_NAME + ": " + tableRowCount);
    System.out.println("Rows in grouper_data_row_assign_v (affiliation): " + dataRowAssignCount);
    if (tableRowCount != dataRowAssignCount) {
      System.out.println("MISMATCH: table row count does not equal provisioned row count");
    } else {
      System.out.println("OK: row counts match");
    }

    int orgMembershipsFromGrouper = 0;
    Set<String> orgPairsFromGrouper = new HashSet<String>();
    for (Map.Entry<String, Set<String>> e : orgToSubjects.entrySet()) {
      for (String subjectId : e.getValue()) {
        orgPairsFromGrouper.add(subjectId + "|" + e.getKey());
        orgMembershipsFromGrouper++;
      }
    }

    @SuppressWarnings("unchecked")
    List<Object[]> tableOrgPairs = (List<Object[]>) (List<?>) new GcDbAccess()
        .sql("select distinct subject_id, org_name from " + TABLE_NAME)
        .selectList(Object[].class);
    Set<String> orgPairsFromTable = new HashSet<String>();
    for (Object[] row : tableOrgPairs) {
      orgPairsFromTable.add(String.valueOf(row[0]) + "|" + String.valueOf(row[1]));
    }
    System.out.println("Distinct (subject, org) pairs in groups: " + orgPairsFromGrouper.size()
        + " (raw memberships: " + orgMembershipsFromGrouper + ")");
    System.out.println("Distinct (subject, org) pairs in " + TABLE_NAME + ": " + orgPairsFromTable.size());

    Set<String> missingInTable = new TreeSet<String>(orgPairsFromGrouper);
    missingInTable.removeAll(orgPairsFromTable);
    Set<String> extraInTable = new TreeSet<String>(orgPairsFromTable);
    extraInTable.removeAll(orgPairsFromGrouper);
    if (missingInTable.isEmpty() && extraInTable.isEmpty()) {
      System.out.println("OK: org pairs match between grouper_memberships and " + TABLE_NAME);
    } else {
      System.out.println("MISSING in table (" + missingInTable.size() + "): " + missingInTable);
      System.out.println("EXTRA in table (" + extraInTable.size() + "): " + extraInTable);
    }

    @SuppressWarnings("unchecked")
    List<Object[]> tableSubjAff = (List<Object[]>) (List<?>) new GcDbAccess()
        .sql("select distinct subject_id, affiliation_name from " + TABLE_NAME)
        .selectList(Object[].class);
    int realMatches = 0;
    int unexpected = 0;
    for (Object[] row : tableSubjAff) {
      String subjectId = String.valueOf(row[0]);
      String aff = String.valueOf(row[1]);
      Set<String> heldAffs = subjectToAffiliations.get(subjectId);
      if (heldAffs != null && heldAffs.contains(aff)) {
        realMatches++;
      } else {
        unexpected++;
        System.out.println("UNEXPECTED affiliation row: " + subjectId + " / " + aff
            + " (held: " + heldAffs + ")");
      }
    }
    System.out.println("Affiliation rows backed by real membership: " + realMatches);
    System.out.println("Unexpected affiliation rows: " + unexpected);

    int studentRows = new GcDbAccess().sql("select count(1) from " + TABLE_NAME + " where affiliation_name = 'student'")
        .select(int.class).intValue();
    if (studentRows == 0) {
      System.out.println("OK: no student affiliation rows");
    } else {
      System.out.println("MISMATCH: " + studentRows + " student affiliation rows present");
    }
  }

  // ---------------------------------------------------------------------------
  // Composite policy variant (app:litellm_c:service:policy)
  // ---------------------------------------------------------------------------

  /**
   * Composite-chain variant under app:litellm_c:service:policy:
   *
   *   service_litellm_rw                       composite COMPLEMENT = _allow MINUS _deny_manual
   *   service_litellm_rw_allow                 manual group, members: _allow_manual + _allow_automatic
   *   service_litellm_rw_allow_manual          manual group
   *   service_litellm_rw_allow_automatic       composite INTERSECTION = _allow_automatic_orgs AND ref:affiliations:affiliation_staff
   *   service_litellm_rw_allow_automatic_orgs  manual group, members: basis:orgs:org_AB + basis:orgs:org_BC
   *   service_litellm_rw_deny_manual           manual group
   */
  public static void createPolicyGroups() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {

      new StemSave(grouperSession).assignName(COMPOSITE_BASE).assignCreateParentStemsIfNotExist(true).save();

      Group orgAB = GroupFinder.findByName(grouperSession, "basis:orgs:org_AB", true);
      Group orgBC = GroupFinder.findByName(grouperSession, "basis:orgs:org_BC", true);
      Group affiliationStaff = GroupFinder.findByName(grouperSession, "ref:affiliations:affiliation_staff", true);

      Group autoOrgs = new GroupSave(grouperSession)
          .assignName(COMPOSITE_BASE + ":service_litellm_rw_allow_automatic_orgs")
          .assignDisplayExtension("service_litellm_rw_allow_automatic_orgs")
          .assignDescription("union of orgs whose staff are allowed").save();
      autoOrgs.addMember(orgAB.toSubject(), false);
      autoOrgs.addMember(orgBC.toSubject(), false);

      Group autoIntersect = new GroupSave(grouperSession)
          .assignName(COMPOSITE_BASE + ":service_litellm_rw_allow_automatic")
          .assignDisplayExtension("service_litellm_rw_allow_automatic")
          .assignDescription("staff within the allowed orgs").save();
      autoIntersect.addCompositeMember(CompositeType.INTERSECTION, autoOrgs, affiliationStaff);

      Group manualAllow = new GroupSave(grouperSession)
          .assignName(COMPOSITE_BASE + ":service_litellm_rw_allow_manual")
          .assignDisplayExtension("service_litellm_rw_allow_manual")
          .assignDescription("manually-added users allowed access").save();

      Group allow = new GroupSave(grouperSession)
          .assignName(COMPOSITE_BASE + ":service_litellm_rw_allow")
          .assignDisplayExtension("service_litellm_rw_allow")
          .assignDescription("everyone allowed (manual + automatic)").save();
      allow.addMember(manualAllow.toSubject(), false);
      allow.addMember(autoIntersect.toSubject(), false);

      Group denyManual = new GroupSave(grouperSession)
          .assignName(COMPOSITE_BASE + ":service_litellm_rw_deny_manual")
          .assignDisplayExtension("service_litellm_rw_deny_manual")
          .assignDescription("manually-added users denied access").save();

      Group policy = new GroupSave(grouperSession)
          .assignName(COMPOSITE_BASE + ":service_litellm_rw")
          .assignDisplayExtension("service_litellm_rw")
          .assignDescription("litellm read/write policy").save();
      policy.addCompositeMember(CompositeType.COMPLEMENT, allow, denyManual);

      System.out.println("Created composite policy under " + COMPOSITE_BASE);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // ---------------------------------------------------------------------------
  // Scripted ABAC variant, group-based predicates (app:litellm_ag:service:policy)
  // ---------------------------------------------------------------------------

  /**
   * Scripted ABAC variant whose JEXL uses bare 'group:name' shorthand. Under
   * app:litellm_ag:service:policy:
   *
   *   service_litellm_rw                scripted ABAC group; one JEXL expression replaces the
   *                                     composite chain
   *   service_litellm_rw_allow_manual   manual group
   *   service_litellm_rw_deny_manual    manual group
   *
   * Run OTHER_JOB_grouperLoaderJexlScriptFullSync to populate the scripted group.
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

      System.out.println("Created scripted (group-based) policy under " + SCRIPTED_BASE);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // ---------------------------------------------------------------------------
  // Scripted ABAC variant, row-based predicates (app:litellm_a:service:policy)
  // ---------------------------------------------------------------------------

  /**
   * Scripted ABAC variant whose JEXL queries the 'affiliation' data row. Under
   * app:litellm_a:service:policy:
   *
   *   service_litellm_rw                scripted ABAC group; JEXL uses entity.hasRow('affiliation', ...)
   *                                     to match (affiliation_org in AB,BC) AND affiliation_name == staff,
   *                                     unioned with allow_manual and minus deny_manual
   *   service_litellm_rw_allow_manual   manual group
   *   service_litellm_rw_deny_manual    manual group
   *
   * Requires the 'affiliation' data row configured by setupAffiliationTableAndProvider().
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

      System.out.println("Created scripted (row-based) policy under " + SCRIPTED_ROW_BASE);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static Set<String> subjectIdsInGroup(String groupName) {
    Set<String> result = new HashSet<String>();
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, true);
    Set<Member> members = group.getImmediateMembers();
    for (Member m : members) {
      if ("personLdapSource".equals(m.getSubjectSourceId())) {
        result.add(m.getSubjectId());
      }
    }
    return result;
  }

  private static List<Object> toList(Object... values) {
    List<Object> list = new ArrayList<Object>(values.length);
    for (Object v : values) {
      list.add(v);
    }
    return list;
  }

  private static List<Subject> collectLdapSubjects() {
    List<Subject> result = new ArrayList<Subject>();
    Set<String> seen = new HashSet<String>();
    String[] queries = new String[] {
        "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
        "k", "l", "m", "n", "o", "p", "r", "s", "t", "w" };
    for (String q : queries) {
      Set<Subject> found = SubjectFinder.findAll(q);
      for (Subject s : found) {
        if (!"personLdapSource".equals(s.getSourceId())) {
          continue;
        }
        if (seen.add(s.getId())) {
          result.add(s);
        }
      }
    }
    return result;
  }

}
