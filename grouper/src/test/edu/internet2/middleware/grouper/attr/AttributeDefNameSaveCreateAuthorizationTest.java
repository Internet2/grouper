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
package edu.internet2.middleware.grouper.attr;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.subject.Subject;

/**
 * Security advisory M2 (WS attribute def name create authorization): inserting an attribute def
 * name through AttributeDefNameSave (what the WS attributeDefNameSave operation runs) must
 * require ATTR_ADMIN on the attribute definition, not only CREATE on the destination folder.
 * Before the fix a subject with ATTR_VIEW on someone else's definition could add names to it.
 * <p>
 * Setup: definition top:defs:protectedDef is owned by root.  Folder top:a is where names are
 * created.  SUBJ0 has CREATE and STEM on top:a and ATTR_VIEW, ATTR_READ, ATTR_UPDATE (not
 * ATTR_ADMIN) on the definition.  SUBJ1 has CREATE on top:a and ATTR_ADMIN.  SUBJ2 has
 * ATTR_ADMIN but nothing on top:a.
 * </p>
 * Before the fix testInsertDeniedWithoutAttrAdmin and testDeniedInsertCreatesNoParentFolder
 * fail.  The other tests are regressions.
 */
public class AttributeDefNameSaveCreateAuthorizationTest extends GrouperTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefNameSaveCreateAuthorizationTest("testInsertDeniedWithoutAttrAdmin"));
  }

  /**
   *
   */
  public AttributeDefNameSaveCreateAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public AttributeDefNameSaveCreateAuthorizationTest(String name) {
    super(name);
  }

  /** root session made in setUp */
  private GrouperSession rootSession;

  /** the definition whose names are protected */
  private AttributeDef attributeDef;

  /** the folder where names are created */
  private Stem folderA;

  /**
   * make the folders, the definition and the privileges as root
   */
  @Override
  public void setUp() {
    super.setUp();
    this.rootSession = GrouperSession.startRootSession();
    new StemSave(this.rootSession).assignName("top").save();
    new StemSave(this.rootSession).assignName("top:defs").save();
    this.folderA = new StemSave(this.rootSession).assignName("top:a").save();

    this.attributeDef = new AttributeDefSave(this.rootSession).assignName("top:defs:protectedDef")
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).save();

    // SUBJ0 can see, read and assign the definition but is not ATTR_ADMIN; can create in top:a
    this.attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
    this.attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    this.attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
    this.folderA.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE, false);
    this.folderA.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM, false);

    // SUBJ1 is ATTR_ADMIN and can create in top:a
    this.attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN, false);
    this.folderA.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE, false);

    // SUBJ2 is ATTR_ADMIN but has nothing on top:a
    this.attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_ADMIN, false);

    // sanity check of the setup
    assertFalse(this.attributeDef.getPrivilegeDelegate().canAttrAdmin(SubjectTestHelper.SUBJ0));
    assertTrue(this.attributeDef.getPrivilegeDelegate().canAttrAdmin(SubjectTestHelper.SUBJ1));
  }

  /**
   * stop sessions
   */
  @Override
  public void tearDown() {
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------

  /**
   * run the handler in a session of the subject.  The root session from setUp is the static
   * session again afterwards.
   * @param subject
   * @param handler
   * @return what the handler returns
   */
  private static Object as(Subject subject, GrouperSessionHandler handler) {
    GrouperSession session = GrouperSession.start(subject, false);
    try {
      return GrouperSession.callbackGrouperSession(session, handler);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * insert a name in the definition as the subject
   * @param subject
   * @param name
   * @param createParentStems
   * @return the save (after save() was called)
   */
  private AttributeDefNameSave insertAs(Subject subject, final String name, final boolean createParentStems) {
    return (AttributeDefNameSave)as(subject, new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) {
        AttributeDefNameSave save = new AttributeDefNameSave(grouperSession, 
            AttributeDefNameSaveCreateAuthorizationTest.this.attributeDef)
            .assignName(name).assignCreateParentStemsIfNotExist(createParentStems)
            .assignSaveMode(SaveMode.INSERT_OR_UPDATE);
        save.save();
        return save;
      }
    });
  }

  /**
   * assert that the insert is denied with an InsufficientPrivilegeException (anywhere in the
   * cause chain, since the transaction and session callbacks may wrap it)
   * @param subject
   * @param name
   * @param createParentStems
   */
  private void assertInsertDenied(Subject subject, String name, boolean createParentStems) {
    try {
      insertAs(subject, name, createParentStems);
      fail("insert of " + name + " by " + subject.getId() + " should be denied");
    } catch (RuntimeException re) {
      Throwable t = re;
      boolean found = false;
      while (t != null) {
        if (t instanceof InsufficientPrivilegeException) {
          found = true;
          break;
        }
        t = t.getCause();
      }
      if (!found) {
        throw re;
      }
    }
  }

  /**
   * find a name as root, fresh from the database
   * @param name
   * @return the name or null
   */
  private static AttributeDefName nameAsRoot(final String name) {
    return (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) {
        return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(
            name, false, new QueryOptions().secondLevelCache(false));
      }
    });
  }

  /**
   * find a folder as root
   * @param name
   * @return the folder or null
   */
  private static Stem stemAsRoot(final String name) {
    return (Stem)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) {
        return StemFinder.findByName(grouperSession, name, false);
      }
    });
  }

  // ---------------------------------------------------------------------------------------
  // tests
  // ---------------------------------------------------------------------------------------

  /**
   * ATTR_ADMIN on the definition and CREATE on the folder: the insert works
   */
  public void testInsertAllowedWithAttrAdmin() {
    AttributeDefNameSave save = insertAs(SubjectTestHelper.SUBJ1, "top:a:adminName", false);
    assertEquals(SaveResultType.INSERT, save.getSaveResultType());
    AttributeDefName created = nameAsRoot("top:a:adminName");
    assertNotNull(created);
    assertEquals(this.attributeDef.getId(), created.getAttributeDefId());
  }

  /**
   * ATTR_VIEW, ATTR_READ and ATTR_UPDATE but not ATTR_ADMIN: denied, no name row
   */
  public void testInsertDeniedWithoutAttrAdmin() {
    assertInsertDenied(SubjectTestHelper.SUBJ0, "top:a:injectedName", false);
    assertNull(nameAsRoot("top:a:injectedName"));
  }

  /**
   * denied insert with createParentStemsIfNotExist: the missing parent folder is not created
   */
  public void testDeniedInsertCreatesNoParentFolder() {
    assertNull(stemAsRoot("top:a:newFolder"));
    assertInsertDenied(SubjectTestHelper.SUBJ0, "top:a:newFolder:injectedName", true);
    assertNull(nameAsRoot("top:a:newFolder:injectedName"));
    assertNull(stemAsRoot("top:a:newFolder"));
  }

  /**
   * ATTR_ADMIN on the definition but no CREATE on the folder: still denied (existing check)
   */
  public void testInsertDeniedWithoutFolderCreate() {
    assertInsertDenied(SubjectTestHelper.SUBJ2, "top:a:noCreateName", false);
    assertNull(nameAsRoot("top:a:noCreateName"));
  }

  /**
   * root can insert
   */
  public void testInsertAsRoot() {
    AttributeDefNameSave save = new AttributeDefNameSave(this.rootSession, this.attributeDef)
        .assignName("top:a:rootName");
    save.save();
    assertEquals(SaveResultType.INSERT, save.getSaveResultType());
    assertNotNull(nameAsRoot("top:a:rootName"));
  }

  /**
   * a view-only subject saving an existing name with no changes gets NO_CHANGE (the check is
   * only for inserts)
   */
  public void testUnchangedSaveByViewOnlyIsNoChange() {
    new AttributeDefNameSave(this.rootSession, this.attributeDef).assignName("top:a:existingName").save();
    AttributeDefNameSave save = insertAs(SubjectTestHelper.SUBJ0, "top:a:existingName", false);
    assertEquals(SaveResultType.NO_CHANGE, save.getSaveResultType());
  }

  /**
   * a view-only subject still cannot update an existing name (AttributeDefName.store() check)
   */
  public void testUpdateByViewOnlyStillDenied() {
    new AttributeDefNameSave(this.rootSession, this.attributeDef).assignName("top:a:existingName2")
        .assignDescription("original").save();
    try {
      as(SubjectTestHelper.SUBJ0, new GrouperSessionHandler() {
        public Object callback(GrouperSession grouperSession) {
          return new AttributeDefNameSave(grouperSession, AttributeDefNameSaveCreateAuthorizationTest.this.attributeDef)
              .assignName("top:a:existingName2").assignDescription("changed").save();
        }
      });
      fail("update by a view-only subject should be denied");
    } catch (RuntimeException re) {
      // expected
    }
    assertEquals("original", nameAsRoot("top:a:existingName2").getDescription());
  }

}
