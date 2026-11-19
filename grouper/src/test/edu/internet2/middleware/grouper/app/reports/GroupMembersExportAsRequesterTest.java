/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.app.reports;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectCustomizer;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectImpl;
import junit.textui.TestRunner;

/**
 * SECURITY tests for advisory H5: the emailed group members export ("Email me a download link") is
 * requested by anyone with READ on the group, but the report job ran it as root, so subject
 * attribute filtering (requireGroupNameForView, the SubjectCustomizer) was skipped and the file
 * could hold attributes that "Download now" hides from the same user.  The fix builds the data as
 * the requester (READ checked again when the job runs) and binds the download to the requester.
 *
 * A test SubjectCustomizer stands in for the site's filtering: for a session subject that is not
 * root/wheel and not in the viewer group, it removes the "email" attribute and hides one subject.
 *
 * Before the fix this class does not compile (GroupMembersExportAsRequester is new) and the
 * restricted email appears in the requester's export.
 */
public class GroupMembersExportAsRequesterTest extends GrouperTest {

  /** members of the exported group get this email domain */
  private static final String EMAIL_DOMAIN = "@exportrequester.example.edu";

  /** session subjects in this group may see emails */
  public static final String VIEWER_GROUP_NAME = "test:exportEmailViewers";

  /** the customizer hides this subject entirely from non viewers */
  private static final String HIDDEN_SUBJECT_ID = "exportTest.hidden";

  /** a member that non viewers can see, without email */
  private static final String VISIBLE_SUBJECT_ID = "exportTest.visible";

  /** the "all" mode headers, sort field first line then headers (as UiV2GroupImport stores them) */
  private static final String ALL_SCRIPT = "subjectId\nsubjectId,sourceId,name,email";

  /**
   *
   */
  public GroupMembersExportAsRequesterTest() {
    super();
  }

  /**
   * @param name
   */
  public GroupMembersExportAsRequesterTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GroupMembersExportAsRequesterTest("testAllModeFiltersForRequester"));
  }

  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");
    // GrouperTest grants READ to GrouperAll on new groups; turn that off so READ comes only from
    // the explicit grants in exportGroup (else revoking a requester's READ changes nothing)
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subjects.customizer.className",
        TestExportSubjectCustomizer.class.getName());
    SubjectFinder.internalClearSubjectCustomizerCache();
  }

  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouperReporting.enable");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("subjects.customizer.className");
    SubjectFinder.internalClearSubjectCustomizerCache();
    super.tearDown();
  }

  /**
   * Test customizer: a session subject that is not root/wheel and not in VIEWER_GROUP_NAME does not
   * see the email attribute, and does not see HIDDEN_SUBJECT_ID at all
   */
  public static class TestExportSubjectCustomizer implements SubjectCustomizer {

    /**
     * @see SubjectCustomizer#decorateSubjects(GrouperSession, Set, Collection)
     */
    public Set<Subject> decorateSubjects(GrouperSession grouperSession, Set<Subject> subjects,
        Collection<String> attributeNamesRequested) {
      return subjects;
    }

    /**
     * @see SubjectCustomizer#filterSubjects(GrouperSession, Set, String)
     */
    public Set<Subject> filterSubjects(GrouperSession grouperSession, Set<Subject> subjects, String findSubjectsInStemName) {
      final Subject sessionSubject = grouperSession == null ? null : grouperSession.getSubject();
      if (sessionSubject == null || PrivilegeHelper.isWheelOrRoot(sessionSubject)) {
        return subjects;
      }
      Boolean isViewer = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        public Object callback(GrouperSession rootSession) throws GrouperSessionException {
          Group viewers = GroupFinder.findByName(rootSession, VIEWER_GROUP_NAME, false);
          return viewers != null && viewers.hasMember(sessionSubject);
        }
      });
      if (isViewer) {
        return subjects;
      }
      Iterator<Subject> iterator = subjects.iterator();
      while (iterator.hasNext()) {
        Subject subject = iterator.next();
        if (HIDDEN_SUBJECT_ID.equals(subject.getId())) {
          iterator.remove();
          continue;
        }
        subject.getAttributes(false).remove("email");
      }
      return subjects;
    }
  }

  /**
   * group with two registry subjects that have an email; SUBJ0 and SUBJ1 can READ it,
   * SUBJ1 is in the viewer group, SUBJ2 is an ADMIN of the group
   * @param name
   * @return the group
   */
  private static Group exportGroup(String name) {
    GrouperSession rootSession = GrouperSession.staticGrouperSession();
    for (String id : new String[] {VISIBLE_SUBJECT_ID, HIDDEN_SUBJECT_ID}) {
      if (SubjectFinder.findById(id, false) == null) {
        RegistrySubject.add(rootSession, id, "person", "name of " + id, "name of " + id, id, "description of " + id, id + EMAIL_DOMAIN);
      }
    }
    Group group = new GroupSave(rootSession).assignName(name).assignCreateParentStemsIfNotExist(true).save();
    group.addMember(SubjectFinder.findById(VISIBLE_SUBJECT_ID, true), false);
    group.addMember(SubjectFinder.findById(HIDDEN_SUBJECT_ID, true), false);
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN, false);

    Group viewers = new GroupSave(rootSession).assignName(VIEWER_GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
    viewers.addMember(SubjectTestHelper.SUBJ1, false);
    return group;
  }

  /**
   * save an export config the way UiV2GroupImport.groupExportEmailSubmit does (as root)
   * @param group
   * @param exportAll
   * @param sqlConfig the requester as sourceId::subjectId
   * @return the saved bean
   */
  private static GrouperReportConfigurationBean saveExportConfig(Group group, boolean exportAll, String sqlConfig) {
    String configName = "groupMembersExport_" + GrouperUuid.getUuid();
    GrouperReportConfigurationBean configBean = new GrouperReportConfigurationBean();
    configBean.setReportConfigName(configName);
    configBean.setReportConfigDescription("System managed configuration for the group members export feature");
    configBean.setReportConfigType(ReportConfigType.GROUP_MEMBERS_EXPORT);
    configBean.setReportConfigFormat(ReportConfigFormat.CSV);
    configBean.setReportConfigFilename("groupMembersExport_" + group.getExtension() + "_$$timestamp$$.csv");
    configBean.setReportConfigEnabled(true);
    configBean.setReportConfigSendEmail(true);
    configBean.setReportConfigSendEmailWithNoData(true);
    configBean.setReportConfigStoreWithNoData(true);
    configBean.setReportConfigSendEmailToViewers(false);
    configBean.setReportConfigQuery(exportAll ? "all" : "ids");
    configBean.setReportConfigScript(exportAll ? ALL_SCRIPT : null);
    configBean.setSqlConfig(sqlConfig);
    GrouperReportConfigService.saveOrUpdateReportConfigAttributes(configBean, group);
    return GrouperReportConfigService.getGrouperReportConfigBean(group, configName);
  }

  /**
   * @param subject
   * @return sourceId::subjectId
   */
  private static String requester(Subject subject) {
    return subject.getSourceId() + "::" + subject.getId();
  }

  /**
   * run the export the way GrouperReportJob does: in a root session
   * @param configBean
   * @return the data
   */
  private static GrouperReportData runAsJob(final GrouperReportConfigurationBean configBean) {
    return (GrouperReportData)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        return ReportConfigType.GROUP_MEMBERS_EXPORT.retrieveReportDataByConfig(configBean, new GrouperReportInstance());
      }
    });
  }

  /**
   * @param data
   * @param subjectId
   * @return the row (subjectId,sourceId,name,email) of the subject
   */
  private static String[] row(GrouperReportData data, String subjectId) {
    for (String[] row : (List<String[]>)data.getData()) {
      if (StringUtils.equals(subjectId, row[0])) {
        return row;
      }
    }
    fail("no row for " + subjectId);
    return null;
  }

  /**
   * a requester outside the viewer group gets no email and no data for the hidden subject, even
   * though the job runs as root and the subjects were resolved (and cached) as root first
   */
  public void testAllModeFiltersForRequester() {

    GrouperSession.startRootSession();
    Group group = exportGroup("test:exportRequester1");

    // prime the subject cache as root, and check the source really has the email
    Subject visibleAsRoot = SubjectFinder.findById(VISIBLE_SUBJECT_ID, true);
    assertEquals("precondition: the source returns the email", VISIBLE_SUBJECT_ID + EMAIL_DOMAIN,
        visibleAsRoot.getAttributeValue("email"));

    GrouperReportData data = runAsJob(saveExportConfig(group, true, requester(SubjectTestHelper.SUBJ0)));

    assertEquals(2, data.getData().size());
    String[] visibleRow = row(data, VISIBLE_SUBJECT_ID);
    assertEquals("name of " + VISIBLE_SUBJECT_ID, visibleRow[2]);
    assertTrue("email hidden from the requester: " + visibleRow[3], StringUtils.isBlank(visibleRow[3]));

    // the hidden subject keeps its id columns only (members are not hidden, their details are)
    String[] hiddenRow = row(data, HIDDEN_SUBJECT_ID);
    assertTrue(StringUtils.isBlank(hiddenRow[2]));
    assertTrue(StringUtils.isBlank(hiddenRow[3]));
  }

  /**
   * a requester in the viewer group sees the email, like "Download now" shows it to them
   */
  public void testAllModeViewerSeesAttribute() {

    GrouperSession.startRootSession();
    Group group = exportGroup("test:exportRequester2");

    GrouperReportData data = runAsJob(saveExportConfig(group, true, requester(SubjectTestHelper.SUBJ1)));

    assertEquals(VISIBLE_SUBJECT_ID + EMAIL_DOMAIN, row(data, VISIBLE_SUBJECT_ID)[3]);
    assertEquals(HIDDEN_SUBJECT_ID + EMAIL_DOMAIN, row(data, HIDDEN_SUBJECT_ID)[3]);
  }

  /**
   * ids mode still exports source and subject ids for a requester with READ
   */
  public void testIdsModeForReader() {

    GrouperSession.startRootSession();
    Group group = exportGroup("test:exportRequester3");

    GrouperReportData data = runAsJob(saveExportConfig(group, false, requester(SubjectTestHelper.SUBJ0)));
    assertEquals(2, data.getData().size());
  }

  /**
   * READ removed after the request: the job fails in both modes and produces no data
   */
  public void testRevokedReadFails() {

    GrouperSession.startRootSession();
    Group group = exportGroup("test:exportRequester4");

    GrouperReportConfigurationBean all = saveExportConfig(group, true, requester(SubjectTestHelper.SUBJ0));
    GrouperReportConfigurationBean ids = saveExportConfig(group, false, requester(SubjectTestHelper.SUBJ0));

    group.revokePriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ);

    for (GrouperReportConfigurationBean configBean : new GrouperReportConfigurationBean[] {all, ids}) {
      try {
        runAsJob(configBean);
        fail("requester without READ must not get an export: " + configBean.getReportConfigQuery());
      } catch (RuntimeException e) {
        // expected
      }
    }
  }

  /**
   * a missing, malformed or unknown requester fails closed
   */
  public void testBadRequesterFails() {

    GrouperSession.startRootSession();
    Group group = exportGroup("test:exportRequester5");

    for (String sqlConfig : new String[] {null, "", "noSeparator", "::", SubjectTestHelper.SUBJ0.getSourceId() + "::noSuchSubjectId"}) {
      GrouperReportConfigurationBean configBean = saveExportConfig(group, true, sqlConfig);
      try {
        runAsJob(configBean);
        fail("bad requester must not get an export: '" + sqlConfig + "'");
      } catch (RuntimeException e) {
        // expected
      }
    }
  }

  /**
   * the download binding (UiV2GroupImport.groupExportDownload): only the requester, matched by
   * source id and subject id; another reader, a group admin, the same id in another source, and a
   * missing or malformed stored requester are all refused
   */
  public void testIsRequester() {

    GrouperSession.startRootSession();
    Group group = exportGroup("test:exportRequester6");

    GrouperReportConfigurationBean configBean = saveExportConfig(group, true, requester(SubjectTestHelper.SUBJ0));

    assertTrue(GroupMembersExportAsRequester.isRequester(configBean, SubjectTestHelper.SUBJ0));
    assertFalse("other reader", GroupMembersExportAsRequester.isRequester(configBean, SubjectTestHelper.SUBJ1));
    assertFalse("group admin", GroupMembersExportAsRequester.isRequester(configBean, SubjectTestHelper.SUBJ2));
    assertFalse("root", GroupMembersExportAsRequester.isRequester(configBean, SubjectFinder.findRootSubject()));
    assertFalse("null", GroupMembersExportAsRequester.isRequester(configBean, null));

    Subject sameIdOtherSource = new SubjectImpl(SubjectTestHelper.SUBJ0.getId(), "x", "x", "person", "otherSourceId");
    assertFalse("same id, other source", GroupMembersExportAsRequester.isRequester(configBean, sameIdOtherSource));

    for (String sqlConfig : new String[] {null, "", "noSeparator", "::", SubjectTestHelper.SUBJ0.getId()}) {
      configBean.setSqlConfig(sqlConfig);
      assertFalse("stored requester '" + sqlConfig + "'", GroupMembersExportAsRequester.isRequester(configBean, SubjectTestHelper.SUBJ0));
    }
  }

}
