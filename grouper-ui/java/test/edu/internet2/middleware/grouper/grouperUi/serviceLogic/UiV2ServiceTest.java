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
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;

/**
 * Security advisory H5 (service attribute def name view bypass): the UI service screens
 * (UiV2Service.viewService, serviceReset, viewServiceSubmit) look up the attribute def name from
 * the request with the unsecured DAO (by id, name or sequential idIndex) and render its name and
 * description.  The fix adds UiV2Service.attributeDefNameIfCanView, called by
 * retrieveServiceHelper after the lookup, which returns null (shown as "service not found")
 * unless the logged-in subject can VIEW the owning attribute def.
 * <p>
 * The helper is tested directly since the UI handlers need a servlet request and response.
 * Each check runs in a session started as the subject being checked, like the UI handlers do,
 * which also proves the check does not need ATTR_ADMIN in the session (the attribute def
 * privilege delegate's canAttrView would throw for a normal user).
 * </p>
 * Before the fix this class does not compile (the helper does not exist).
 */
public class UiV2ServiceTest extends GrouperTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2ServiceTest("testNoPrivilegeIsNotFound"));
  }

  /**
   *
   */
  public UiV2ServiceTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2ServiceTest(String name) {
    super(name);
  }

  /** root session made in setUp */
  private GrouperSession rootSession;

  /** attribute def that owns the service name */
  private AttributeDef serviceDef;

  /** the attribute def name the service screen would show */
  private AttributeDefName serviceName;

  /**
   * make the attribute def and name as root, with no privileges for anyone else
   */
  @Override
  public void setUp() {
    super.setUp();
    this.rootSession = GrouperSession.startRootSession();
    Stem root = StemFinder.findRootStem(this.rootSession);
    root.addChildStem("top", "top");

    this.serviceDef = new AttributeDefSave(this.rootSession).assignName("top:serviceDef")
        .assignAttributeDefType(AttributeDefType.service).assignToStem(true).save();
    this.serviceName = new AttributeDefNameSave(this.rootSession, this.serviceDef).assignName("top:secretService")
        .assignDisplayExtension("secretService").assignDescription("secret description").save();

    // make sure no default grant to everyone (attributeDefs.create.grant.all.*) lets SUBJ0 see it
    Subject everyone = SubjectFinder.findAllSubject();
    for (Privilege privilege : AttributeDefPrivilege.ALL_PRIVILEGES) {
      this.serviceDef.getPrivilegeDelegate().revokePriv(everyone, privilege, false);
    }
  }

  /**
   * stop sessions
   */
  @Override
  public void tearDown() {
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  /**
   * call the helper in a session started as the subject, like the UI handlers do
   * @param subject
   * @return the helper result
   */
  private AttributeDefName ifCanViewAs(Subject subject) {
    GrouperSession session = GrouperSession.start(subject);
    try {
      return UiV2Service.attributeDefNameIfCanView(this.serviceName, subject);
    } finally {
      GrouperSession.stopQuietly(session);
      // put the root session back as the static session
      GrouperSession.startRootSession();
    }
  }

  /**
   * a subject with no privilege on the owning attribute def gets null (not found)
   */
  public void testNoPrivilegeIsNotFound() {
    assertNull(ifCanViewAs(SubjectTestHelper.SUBJ0));
  }

  /**
   * ATTR_VIEW, and privileges that imply it, are enough to see the name
   */
  public void testViewAndImpliedPrivilegesAllowed() {
    this.serviceDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
    assertEquals(this.serviceName.getId(), ifCanViewAs(SubjectTestHelper.SUBJ0).getId());

    this.serviceDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
    assertEquals(this.serviceName.getId(), ifCanViewAs(SubjectTestHelper.SUBJ1).getId());

    this.serviceDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_ADMIN, false);
    assertEquals(this.serviceName.getId(), ifCanViewAs(SubjectTestHelper.SUBJ2).getId());

    // and a subject still without any privilege is still denied
    assertNull(ifCanViewAs(SubjectTestHelper.SUBJ3));
  }

  /**
   * root and a wheel member see the name with no privileges on the attribute def
   */
  public void testRootAndWheelAllowed() {
    assertEquals(this.serviceName.getId(),
        UiV2Service.attributeDefNameIfCanView(this.serviceName, this.rootSession.getSubject()).getId());

    // wheel: SUBJ1 has no privileges on the attribute def but is in the wheel group
    Stem root = StemFinder.findRootStem(this.rootSession);
    Stem etc = StemFinder.findByName(this.rootSession, "etc", false);
    if (etc == null) {
      etc = root.addChildStem("etc", "etc");
    }
    Group wheel = etc.addChildGroup("testWheel", "testWheel");
    wheel.addMember(SubjectTestHelper.SUBJ1, false);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());

    assertEquals(this.serviceName.getId(), ifCanViewAs(SubjectTestHelper.SUBJ1).getId());
  }

  /**
   * null in (the lookup found nothing) gives null, and no subject fails closed
   */
  public void testNullInputs() {
    assertNull(UiV2Service.attributeDefNameIfCanView(null, SubjectTestHelper.SUBJ0));
    assertNull(UiV2Service.attributeDefNameIfCanView(this.serviceName, null));
  }
}
