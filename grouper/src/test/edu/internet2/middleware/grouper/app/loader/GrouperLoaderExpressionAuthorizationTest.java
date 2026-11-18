package edu.internet2.middleware.grouper.app.loader;

import java.lang.reflect.Method;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <pre>
 * Loader expressions (${...} in the SQL query and group query, and in the LDAP filter and
 * expressions) run as GrouperSystem in the loader daemon, so a delegated loader editor who is not
 * wheel or root may not save them, may not change a loader that already has them, and may not
 * import a SQL or LDAP loader with them.  Plain queries are not affected; wheel and root may do
 * all of it.
 *
 * Checks GrouperLoaderExpressionAuthorization (the call UiV2GrouperLoader.editGrouperLoaderSave
 * makes with the submitted fields) and the import path through
 * GrouperLoaderImportExport.importLoaderConfig (5.18.2 and later; skipped where there is no
 * loader import).  Before the fix the test does not compile (the class does not exist) and the
 * reported query saves and imports are accepted.
 *
 * Same file for every release (2.6.19 on): Java 8, no commons-lang import, GrouperTest base.
 * </pre>
 */
public class GrouperLoaderExpressionAuthorizationTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperLoaderExpressionAuthorizationTest("testNonWheelSqlExpressionRefused"));
  }

  /** */
  public GrouperLoaderExpressionAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperLoaderExpressionAuthorizationTest(String name) {
    super(name);
  }

  /** the reported payload shape */
  private static final String SQL_EXPRESSION = "${var proof = grouperUtil.gshRunScript(\"addMember('etc:loaderEditors', 'test.subject.1');\"); "
      + "\"select 'test.subject.2' as subject_id, 'jdbc' as subject_source_id\"}";

  /** a plain query */
  private static final String SQL_PLAIN = "select subject_id, subject_source_id from some_view";

  /** root session */
  private GrouperSession rootSession;

  /** group with no loader */
  private Group plainGroup;

  // SUBJ0: non-wheel loader editor, SUBJ3: wheel

  @Override
  protected void setUp() {
    super.setUp();
    this.rootSession = GrouperSession.startRootSession();

    Group wheel = new GroupSave(this.rootSession).assignName("test:secPatch123wheel")
        .assignCreateParentStemsIfNotExist(true).save();
    wheel.addMember(SubjectTestHelper.SUBJ3, false);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
    PrivilegeHelper.wheelMemberCacheClear();

    this.plainGroup = new GroupSave(this.rootSession).assignName("test:secPatch123plain")
        .assignCreateParentStemsIfNotExist(true).save();
  }

  @Override
  protected void tearDown() {
    PrivilegeHelper.wheelMemberCacheClear();
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  // ---------------------------------------------------------------- helpers

  /**
   * the save check the loader screen makes
   * @param subject
   * @param group
   * @param type SQL or LDAP
   * @param sql query, group query
   * @param ldap filter, subject, group name, group display name, group description, attribute filter expression
   * @return null if allowed, else the message
   */
  private static String uiSave(Subject subject, Group group, String type, String[] sql, String[] ldap) {
    return GrouperLoaderExpressionAuthorization.checkUiSave(subject, group, true, type, sql, ldap);
  }

  /**
   * six LDAP field values, with value at position index
   * @param index
   * @param value
   * @return the array
   */
  private static String[] ldapFields(int index, String value) {
    String[] result = new String[] {"(objectClass=person)", null, null, null, null, null};
    result[index] = value;
    return result;
  }

  /**
   * a group with a SQL loader whose query is given, set up as root as the loader screen does
   * @param name
   * @param query
   * @return the group
   */
  private Group sqlLoaderGroup(String name, String query) {
    Group group = new GroupSave(this.rootSession).assignName(name).assignCreateParentStemsIfNotExist(true).save();
    AttributeDefName loaderType = AttributeDefNameFinder.findByName(
        GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc")
        + ":legacy:attribute:legacyGroupType_grouperLoader", true);
    group.getAttributeDelegate().assignAttribute(loaderType);
    group.setAttribute(GrouperLoader.GROUPER_LOADER_TYPE, "SQL_SIMPLE", false);
    group.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY, query, false);
    return group;
  }

  /**
   * a group with an LDAP loader with a subject expression, set up as root as the loader screen does
   * @param name
   * @param subjectExpression
   * @return the group
   */
  private Group ldapLoaderGroup(String name, String subjectExpression) {
    Group group = new GroupSave(this.rootSession).assignName(name).assignCreateParentStemsIfNotExist(true).save();
    AttributeDefName ldapMarker = AttributeDefNameFinder.findByName(LoaderLdapUtils.grouperLoaderLdapName(), true);
    AttributeAssign assign = group.getAttributeDelegate().assignAttribute(ldapMarker).getAttributeAssign();
    assign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapTypeName(), "LDAP_SIMPLE");
    assign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapFilterName(), "(objectClass=person)");
    assign.getAttributeValueDelegate().assignValue(LoaderLdapUtils.grouperLoaderLdapSubjectExpressionName(), subjectExpression);
    return group;
  }

  /**
   * run checkImport in a session of the subject (the UI imports in the logged in user's session)
   * @param subject
   * @param json
   * @return null or the message
   */
  private static String checkImportAs(Subject subject, String json) {
    GrouperSession session = GrouperSession.start(subject);
    try {
      return GrouperLoaderExpressionAuthorization.checkImport(
          GrouperUtil.jsonJacksonGetString(GrouperUtil.jsonJacksonNode(json), "loaderType"), GrouperUtil.jsonJacksonNode(json));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * a SQL loader export with the given query
   * @param query json-escaped
   * @return the json
   */
  // not the grouper database: a non-wheel SQL loader on it is refused by another check first
  private static String sqlImportJson(String query) {
    String prefix = GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":legacy:attribute:";
    return "{\"loaderType\":\"SQL\",\"attributes\":["
        + "{\"attributeName\":\"" + prefix + "legacyAttribute_grouperLoaderType\",\"attributeValue\":\"SQL_SIMPLE\"},"
        + "{\"attributeName\":\"" + prefix + "legacyAttribute_grouperLoaderDbName\",\"attributeValue\":\"secPatch123otherDb\"},"
        + "{\"attributeName\":\"" + prefix + "legacyAttribute_grouperLoaderQuery\",\"attributeValue\":\"" + query + "\"}]}";
  }

  // ---------------------------------------------------------------- tests

  /**
   * non-wheel save with ${ in the SQL query or the SQL group query
   */
  public void testNonWheelSqlExpressionRefused() {
    assertEquals(GrouperLoaderExpressionAuthorization.MESSAGE_SAVE,
        uiSave(SubjectTestHelper.SUBJ0, this.plainGroup, "SQL", new String[] {SQL_EXPRESSION, null}, null));
    assertEquals(GrouperLoaderExpressionAuthorization.MESSAGE_SAVE,
        uiSave(SubjectTestHelper.SUBJ0, this.plainGroup, "SQL", new String[] {SQL_PLAIN, "select group_name from x where a = '${1}'"}, null));

    // and the UI form of the call throws a veto (the screen shows its reason)
    try {
      GrouperLoaderExpressionAuthorization.assertUiSaveAllowed(SubjectTestHelper.SUBJ0, this.plainGroup, true, "SQL",
          new String[] {SQL_EXPRESSION, null}, null);
      fail("expected a veto");
    } catch (HookVeto hookVeto) {
      assertEquals(GrouperLoaderExpressionAuthorization.MESSAGE_SAVE, hookVeto.getReason());
    }
  }

  /**
   * non-wheel save with ${ in each LDAP filter / expression field
   */
  public void testNonWheelLdapExpressionRefused() {
    for (int i = 0; i < 6; i++) {
      assertEquals("LDAP field " + i, GrouperLoaderExpressionAuthorization.MESSAGE_SAVE,
          uiSave(SubjectTestHelper.SUBJ0, this.plainGroup, "LDAP", null, ldapFields(i, "${java.lang.Runtime.getRuntime()}")));
    }
  }

  /**
   * control: plain queries and filters are allowed for a non-wheel editor, and only the fields of
   * the type being saved are checked
   */
  public void testNonWheelPlainQueryAllowed() {
    assertNull(uiSave(SubjectTestHelper.SUBJ0, this.plainGroup, "SQL", new String[] {SQL_PLAIN, null}, null));
    assertNull(uiSave(SubjectTestHelper.SUBJ0, this.plainGroup, "SQL", new String[] {SQL_PLAIN, "select group_name from y"},
        ldapFields(1, "${stale hidden field}")));
    assertNull(uiSave(SubjectTestHelper.SUBJ0, this.plainGroup, "LDAP", null, ldapFields(0, "(uid=*)")));
    // a lone $ or { is not an expression
    assertNull(uiSave(SubjectTestHelper.SUBJ0, this.plainGroup, "SQL", new String[] {"select '$' || '{' from dual", null}, null));
  }

  /**
   * wheel and root may save expressions
   */
  public void testWheelAllowed() {
    assertNull(uiSave(SubjectTestHelper.SUBJ3, this.plainGroup, "SQL", new String[] {SQL_EXPRESSION, null}, null));
    assertNull(uiSave(SubjectFinder.findRootSubject(), this.plainGroup, "LDAP", null, ldapFields(5, "${attributeValue == 'a'}")));
  }

  /**
   * non-wheel edit of a SQL loader whose query already has ${ is refused, even with a plain new
   * query; wheel may.  Removing it is allowed for everyone (decision 2026-11-11)
   */
  public void testNonWheelEditOfExistingSqlExpressionRefused() {
    Group group = sqlLoaderGroup("test:secPatch123sqlExisting", SQL_EXPRESSION);
    assertEquals(GrouperLoaderExpressionAuthorization.MESSAGE_EXISTING,
        uiSave(SubjectTestHelper.SUBJ0, group, "SQL", new String[] {SQL_PLAIN, null}, null));
    assertNull("removing a loader is allowed",
        GrouperLoaderExpressionAuthorization.checkUiSave(SubjectTestHelper.SUBJ0, group, false, null, null, null));
    assertNull(uiSave(SubjectTestHelper.SUBJ3, group, "SQL", new String[] {SQL_PLAIN, null}, null));

    // control: an existing plain loader can be edited
    Group plainLoader = sqlLoaderGroup("test:secPatch123sqlPlain", SQL_PLAIN);
    assertNull(uiSave(SubjectTestHelper.SUBJ0, plainLoader, "SQL", new String[] {SQL_PLAIN + " where 1=1", null}, null));
  }

  /**
   * non-wheel edit of an LDAP loader whose subject expression already has ${ is refused
   */
  public void testNonWheelEditOfExistingLdapExpressionRefused() {
    Group group = ldapLoaderGroup("test:secPatch123ldapExisting", "${loaderLdapElUtils.convertDnToSpecificValue(subjectId)}");
    assertEquals(GrouperLoaderExpressionAuthorization.MESSAGE_EXISTING,
        uiSave(SubjectTestHelper.SUBJ0, group, "LDAP", null, ldapFields(0, "(uid=*)")));
    assertNull(uiSave(SubjectTestHelper.SUBJ3, group, "LDAP", null, ldapFields(0, "(uid=*)")));
  }

  /**
   * import check: non-wheel SQL / LDAP import with ${ refused (also when json-escaped), plain allowed,
   * wheel allowed, other loader types not checked here
   */
  public void testImportCheck() {
    assertEquals(GrouperLoaderExpressionAuthorization.MESSAGE_IMPORT, checkImportAs(SubjectTestHelper.SUBJ0, sqlImportJson("${1+1}")));
    // json escape backslash-u0024 is $: the check looks at the decoded value
    assertEquals(GrouperLoaderExpressionAuthorization.MESSAGE_IMPORT, checkImportAs(SubjectTestHelper.SUBJ0, sqlImportJson("\\u0024{1+1}")));
    assertEquals(GrouperLoaderExpressionAuthorization.MESSAGE_IMPORT, checkImportAs(SubjectTestHelper.SUBJ0,
        "{\"loaderType\":\"LDAP\",\"attributes\":[{\"attributeName\":\"x:grouperLoaderLdapSubjectExpression\",\"attributeValue\":\"${a}\"}]}"));
    assertNull(checkImportAs(SubjectTestHelper.SUBJ0, sqlImportJson(SQL_PLAIN)));
    assertNull(checkImportAs(SubjectTestHelper.SUBJ3, sqlImportJson("${1+1}")));
    assertNull(checkImportAs(SubjectTestHelper.SUBJ0,
        "{\"loaderType\":\"JEXL_SCRIPT\",\"attributes\":[{\"attributeName\":\"x\",\"attributeValue\":\"${entity.memberOf('a:b')}\"}]}"));
  }

  /**
   * the import itself (GrouperLoaderImportExport.importLoaderConfig, 5.18.2 and later): a refused
   * import writes nothing
   */
  @SuppressWarnings("unchecked")
  public void testImportLoaderConfigRefused() throws Exception {
    Method importMethod = null;
    try {
      importMethod = Class.forName("edu.internet2.middleware.grouper.app.loader.GrouperLoaderImportExport")
          .getMethod("importLoaderConfig", String.class, Group.class);
    } catch (ClassNotFoundException cnfe) {
      System.out.println("skip testImportLoaderConfigRefused: no loader import in this release");
      return;
    }
    // a second loader database with its own url, so the import is not refused as a SQL loader on
    // the Grouper database first (an unknown database counts as the Grouper one); never connected to
    for (String[] keyValue : new String[][] {{"user", "secPatch123"}, {"pass", "secPatch123"},
        {"url", "jdbc:postgresql://localhost:5432/secPatch123otherDb"}, {"driver", "org.postgresql.Driver"}}) {
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("db.secPatch123otherDb." + keyValue[0], keyValue[1]);
    }
    Group group = new GroupSave(this.rootSession).assignName("test:secPatch123import")
        .assignCreateParentStemsIfNotExist(true).save();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    List<String> errors = null;
    try {
      errors = (List<String>) importMethod.invoke(null, sqlImportJson(SQL_EXPRESSION.replace("\"", "\\\"")), group);
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertTrue("errors: " + errors, errors != null && errors.contains(GrouperLoaderExpressionAuthorization.MESSAGE_IMPORT));

    // nothing written
    GrouperSession.startRootSession();
    assertNull(group.getAttributeOrNull(GrouperLoader.GROUPER_LOADER_QUERY));
  }

}
