/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.externalSubjects;

import java.util.HashSet;
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * @author mchyzer
 *
 */
public class ExternalSubjectTest extends GrouperTest {

  /**
   * @param name
   */
  public ExternalSubjectTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ExternalSubjectTest("testDynamicFieldsDaemon"));
    //TestRunner.run(ExternalSubjectTest.class);
  }

  /**
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    setupHelper();
  }

  /** if we are testing jabber */
  private static boolean hasJabber = false;
  
  /**
   * 
   */
  public static void setupHelper() {
    hasJabber = StringUtils.equals(GrouperConfig.getProperty("externalSubjects.attributes.jabber.systemName"), "jabber");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.desc.el", 
        "${grouperUtil.appendIfNotBlankString(externalSubject.name, ' - ', externalSubject.institution)}");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.name.required", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.email.required", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.email.enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.institution.required", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.institution.enabled", "true");
    if (hasJabber) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.attributes.jabber.friendlyName", "Jabber ID");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.attributes.jabber.systemName", "jabber");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.attributes.jabber.required", "false");
    }
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.wheelOrRootCanEdit", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.groupAllowedForEdit");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.validateIndentiferLikeEmail", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.autoCreateSource", "true");
    
    SourceManager.getInstance().loadSource(ExternalSubjectAutoSourceAdapter.instance());
  }
  
  /**
   * test insert/update/delete on external subject
   */
  public void testPersistence() {
    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setEmail("a@b.c");
    externalSubject.setIdentifier("a@id.b.c");
    externalSubject.setInstitution("My Institution");
    externalSubject.setName("My Name");
    externalSubject.store();
    
    String uuid = externalSubject.getUuid();
    
    assertTrue(!StringUtils.isBlank(uuid));
    
    //lets find the subject by subject api
    Subject subject = SubjectFinder.findByIdentifier("a@id.b.c", true);
    
    assertEquals("My Name", subject.getName());
    assertEquals("My Name - My Institution", subject.getDescription());
    assertEquals(uuid, subject.getId());
    
    //lets find subject by hib api
    externalSubject = ExternalSubjectStorageController.findByIdentifier("a@id.b.c", true, null);
    
    assertEquals("My Name", externalSubject.getName());
    assertEquals("My Name - My Institution", externalSubject.getDescription());
    assertEquals(uuid, externalSubject.getUuid());
    assertEquals("My Institution", externalSubject.getInstitution());
    assertEquals("a@b.c", externalSubject.getEmail());
    
    
    //lets update it
    externalSubject.setName("New Name");
    externalSubject.store();

    externalSubject = ExternalSubjectStorageController.findByIdentifier("a@id.b.c", true, null);
    
    assertEquals("New Name", externalSubject.getName());
    
    //lets delete it
    externalSubject.delete();

    externalSubject = ExternalSubjectStorageController.findByIdentifier("a@id.b.c", false, null);

    assertNull(externalSubject);
    
  }
  
  /**
   * 
   */
  public void testEnabledDisabled() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //make an externalSubject which should not be edited
    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("a@b.c");
    externalSubject.store();
      
    //make an externalSubject which should not be edited
    externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("b@b.c");
    //one hour in future
    externalSubject.setDisabledTimeDb(System.currentTimeMillis() + (1000*60*60*1));
    externalSubject.store();
    assertTrue(externalSubject.isEnabled());

    //make an externalSubject which should not be edited
    externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("c@b.c");
    //one hour in past
    externalSubject.setDisabledTimeDb(System.currentTimeMillis() - (1000*60*60*1));
    externalSubject.store();
    assertFalse(externalSubject.isEnabled());

    //this one should be edited, if null and disabled, flip it
    externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("d@b.c");
    externalSubject.store();
    HibernateSession.bySqlStatic().executeSql("update grouper_ext_subj set enabled = 'F' where identifier = 'd@b.c'");

    //this one should be edited, should be disabled, flip it
    externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("e@b.c");
    externalSubject.setDisabledTimeDb(System.currentTimeMillis() - (1000*60*60*1));
    externalSubject.store();
    HibernateSession.bySqlStatic().executeSql("update grouper_ext_subj set enabled = 'T' where identifier = 'e@b.c'");
    
    //this one shouldnt be edited, shouldnt be disabled, flip it
    externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("f@b.c");
    externalSubject.setDisabledTimeDb(System.currentTimeMillis() + (1000*60*60*1));
    externalSubject.store();
    HibernateSession.bySqlStatic().executeSql("update grouper_ext_subj set enabled = 'F' where identifier = 'f@b.c'");

    //give a buffer so the times work out ok
    GrouperUtil.sleep(1000);
    
    long daemonTime = System.currentTimeMillis();
    
    GrouperUtil.sleep(1000);

    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_ENABLED_DISABLED);
    assertTrue(status.toLowerCase().contains("success"));

    assertEquals(3, ExternalSubject.lastDisabledFixCount);

    //lets find subject by hib api and check it out
    externalSubject = ExternalSubjectStorageController.findByIdentifier("a@b.c", true, null);
    assertTrue(externalSubject.getModifyTimeDb() < daemonTime);
    assertTrue(externalSubject.isEnabled());

    externalSubject = ExternalSubjectStorageController.findByIdentifier("b@b.c", true, null);
    assertTrue(externalSubject.getModifyTimeDb() < daemonTime);
    assertTrue(externalSubject.isEnabled());

    externalSubject = ExternalSubjectStorageController.findByIdentifier("c@b.c", true, null);
    assertTrue(externalSubject.getModifyTimeDb() < daemonTime);
    assertFalse(externalSubject.isEnabled());
  
    externalSubject = ExternalSubjectStorageController.findByIdentifier("d@b.c", true, null);
    assertTrue(externalSubject.getModifyTimeDb() > daemonTime);
    assertTrue(externalSubject.isEnabled());
  
    externalSubject = ExternalSubjectStorageController.findByIdentifier("e@b.c", true, null);
    assertTrue(externalSubject.getModifyTimeDb() > daemonTime);
    assertFalse(externalSubject.isEnabled());
  
    externalSubject = ExternalSubjectStorageController.findByIdentifier("f@b.c", true, null);
    assertTrue(externalSubject.getModifyTimeDb() > daemonTime);
    assertTrue(externalSubject.isEnabled());
  
    //at this point, the enabled ones should be resolvable and not the disabled ones
    assertNotNull(SubjectFinder.findByIdentifierAndSource("a@b.c", "grouperExternal", false));
    assertNotNull(SubjectFinder.findByIdentifierAndSource("b@b.c", "grouperExternal", false));
    assertNull(SubjectFinder.findByIdentifierAndSource("c@b.c", "grouperExternal", false));
    assertNotNull(SubjectFinder.findByIdentifierAndSource("d@b.c", "grouperExternal", false));
    assertNull(SubjectFinder.findByIdentifierAndSource("e@b.c", "grouperExternal", false));
    assertNotNull(SubjectFinder.findByIdentifierAndSource("f@b.c", "grouperExternal", false));
    
  }

  /**
   * test insert/update/delete on external subject
   */
  public void testSecurity() {

    //###########################################
    //externalSubjects.wheelOrRootCanEdit = false
    //externalSubjects.groupAllowedForEdit = 
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.wheelOrRootCanEdit", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.groupAllowedForEdit");
    ExternalSubjectConfig.clearCache();

    //no one should be allowed
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //grouper session should be able to insert/delete
    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("a");
    
    try {
      externalSubject.store();
      fail("Shouldnt be allowed");
    } catch (Exception e) {
      //ok
      assertTrue(ExceptionUtils.getFullStackTrace(e).toLowerCase().contains("subject cannot edit external users"));
    }
    try {
      externalSubject.delete();
      fail("Shouldnt be allowed");
    } catch (Exception e) {
      //ok
      assertTrue(ExceptionUtils.getFullStackTrace(e).toLowerCase().contains("subject cannot edit external users"));
    }

    //subject0 cannot do this
    Subject subject0 = SubjectFinder.findById("test.subject.0", true);
    grouperSession.stop();
    grouperSession = GrouperSession.start(subject0);

    try {
      externalSubject.store();
      fail("Shouldnt be allowed");
    } catch (Exception e) {
      //ok
      assertTrue(ExceptionUtils.getFullStackTrace(e).toLowerCase().contains("subject cannot edit external users"));
    }
    try {
      externalSubject.delete();
      fail("Shouldnt be allowed");
    } catch (Exception e) {
      //ok
      assertTrue(ExceptionUtils.getFullStackTrace(e).toLowerCase().contains("subject cannot edit external users"));
    }
    
    //###########################################
    //externalSubjects.wheelOrRootCanEdit = true
    //externalSubjects.groupAllowedForEdit = 
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.wheelOrRootCanEdit", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.groupAllowedForEdit");
    ExternalSubjectConfig.clearCache();

    grouperSession = GrouperSession.startRootSession();

    //grouper session should be able to insert/delete
    externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("a@idp.b.c");
    externalSubject.store();
    externalSubject.delete();

    //subject0 cannot do this
    grouperSession.stop();
    grouperSession = GrouperSession.start(subject0);

    try {
      externalSubject.delete();
      fail("Shouldnt be allowed");
    } catch (Exception e) {
      //ok
      assertTrue(ExceptionUtils.getFullStackTrace(e).toLowerCase().contains("subject cannot edit external users"));
    }
    try {
      externalSubject.store();
      fail("Shouldnt be allowed");
    } catch (Exception e) {
      //ok
      assertTrue(ExceptionUtils.getFullStackTrace(e).toLowerCase().contains("subject cannot edit external users"));
    }
    
    //###########################################
    //externalSubjects.wheelOrRootCanEdit = true
    //externalSubjects.groupAllowedForEdit = stem:group
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.wheelOrRootCanEdit", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.groupAllowedForEdit", "stem:group");
    
    Group securityGroup = new GroupSave(grouperSession.internal_getRootSession())
      .assignCreateParentStemsIfNotExist(true).assignName("stem:group").save();
    
    ExternalSubjectConfig.clearCache();
    grouperSession = GrouperSession.startRootSession();

    //grouper session should be able to insert/delete
    externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("a@idp.b.c");
    externalSubject.store();
    externalSubject.delete();

    //subject0 cannot do this
    grouperSession.stop();
    grouperSession = GrouperSession.start(subject0);

    try {
      externalSubject.delete();
      fail("Shouldnt be allowed");
    } catch (Exception e) {
      //ok
      assertTrue(ExceptionUtils.getFullStackTrace(e).toLowerCase().contains("subject cannot edit external users"));
    }
    try {
      externalSubject.store();
      fail("Shouldnt be allowed");
    } catch (Exception e) {
      //ok
      assertTrue(ExceptionUtils.getFullStackTrace(e).toLowerCase().contains("subject cannot edit external users"));
    }
    
    
    //subject1 can do this
    grouperSession.stop();
    GrouperSession.startRootSession();
    
    Subject subject1 = SubjectFinder.findById("test.subject.1", true);
    securityGroup.addMember(subject1);

    grouperSession.stop();
    grouperSession = GrouperSession.start(subject1);

    externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("a@idp.b.c");
    externalSubject.store();
    externalSubject.delete();
    
    //insert it again
    externalSubject.setHibernateVersionNumber(GrouperAPI.INITIAL_VERSION_NUMBER);
    externalSubject.store();
    
  }

  /**
   * test store on external subject which does not have the required fields or attributes
   */
  public void testRequiredFieldsAttributesName() {
  
    //###########################################
    //externalSubjects.name.required = false
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.name.required", "false");
    ExternalSubjectConfig.clearCache();
  
    GrouperSession.startRootSession();
    
    //name is not required
    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("a@idp.b.c");
    externalSubject.store();
    
    //###########################################
    //externalSubjects.name.required = true
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.name.required", "true");
    ExternalSubjectConfig.clearCache();
  
    //name is not required
    externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("b@idp.b.c");
    try {
      externalSubject.store();
      fail("Name is required");
    } catch (Exception e) {
      assertTrue(ExceptionUtils.getFullStackTrace(e).toLowerCase().contains("name is a required"));
    }
    externalSubject.setName("my name");
    //sohuld work now
    externalSubject.store();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.name.required");
    ExternalSubjectConfig.clearCache();

  }

  /**
   * test store on external subject which does not have the required fields or attributes
   */
  public void testRequiredFieldsAttributesEmail() {
  
    //###########################################
    //externalSubjects.email.required = true
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.email.required", "true");
    ExternalSubjectConfig.clearCache();
  
    //name is not required
    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setName("my name");
    externalSubject.setIdentifier("c@idp.b.c");
    try {
      externalSubject.store();
      fail("Email is required");
    } catch (Exception e) {
      assertTrue(ExceptionUtils.getFullStackTrace(e).toLowerCase().contains("email is a required"));
    }
    externalSubject.setEmail("my@email.address");
    //should work now
    externalSubject.store();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.email.required");
    ExternalSubjectConfig.clearCache();
  
  }

  /**
   * test store on external subject which does not have the required fields or attributes
   */
  public void testRequiredFieldsAttributesInstitution() {
  
    //###########################################
    //externalSubjects.institution.required = true
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.institution.required", "true");
    ExternalSubjectConfig.clearCache();
  
    //institution is required
    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setName("my name");
    externalSubject.setIdentifier("d@idp.b.c");
    try {
      externalSubject.store();
      fail("Institution is a required");
    } catch (Exception e) {
      assertTrue(e.getMessage(), ExceptionUtils
          .getFullStackTrace(e).toLowerCase().contains("institution is a required"));
    }
    externalSubject.setInstitution("My institution");
    //should work now
    externalSubject.store();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.institution.required");
    ExternalSubjectConfig.clearCache();
    
  
  }

  /**
   * test store on external subject which does not have the required fields or attributes
   */
  public void testRequiredFieldsAttributesJabber() {
  
    if (!hasJabber) {
      return;
    }
    //###########################################
    //externalSubjects.institution.required = true
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.attributes.jabber.required", "true");
    try {
      ExternalSubjectConfig.clearCache();
    
      //institution is required
      ExternalSubject externalSubject = new ExternalSubject();
      externalSubject.setName("my name");
      externalSubject.setIdentifier("d@idp.b.c");
      try {
        externalSubject.store();
        fail("Jabber is a required");
      } catch (Exception e) {
        assertTrue(e.getMessage(), ExceptionUtils
            .getFullStackTrace(e).toLowerCase().contains("jabber is a required"));
      }
  
      //institution is required
      externalSubject = new ExternalSubject();
      externalSubject.setName("my name");
      externalSubject.setIdentifier("e@idp.b.c");
      ExternalSubjectAttribute externalSubjectAttribute = new ExternalSubjectAttribute();
      externalSubjectAttribute.setAttributeSystemName("jabber");
      externalSubjectAttribute.setAttributeValue("w@e.r");
      externalSubject.store(GrouperUtil.toSet(externalSubjectAttribute), null, true, true, false);
  
      externalSubject.assignAttribute("jabber", "a@b.c");
      //should work now
      externalSubject.store();
      
      try {
        externalSubject.removeAttribute("jabber");
        fail("Jabber is a required");
      } catch (Exception e) {
        assertTrue(e.getMessage(), ExceptionUtils
            .getFullStackTrace(e).toLowerCase().contains("jabber is a required"));
      }
  
      //institution is required
      externalSubject = new ExternalSubject();
      externalSubject.setName("my name");
      externalSubject.setIdentifier("f@idp.b.c");
      try {
        externalSubject.store(new HashSet<ExternalSubjectAttribute>(), null, true, true, false);
        fail("Jabber is a required");
      } catch (Exception e) {
        assertTrue(e.getMessage(), ExceptionUtils
            .getFullStackTrace(e).toLowerCase().contains("jabber is a required"));
      }
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.attributes.jabber.required");
      ExternalSubjectConfig.clearCache();
      
    }
    
  
  }

  /**
   * test dynamic description
   */
  public void testDynamicDescription() {

    //externalSubjects.desc.el = ${grouperUtil.appendIfNotBlankString(externalSubject.name, ' - ', externalSubject.institution)}
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.desc.el", 
        "${grouperUtil.appendIfNotBlankString(externalSubject.getName(), ' - ', externalSubject.getInstitution())}");
    ExternalSubjectConfig.clearCache();

    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("a@idp.b.c");
    externalSubject.setInstitution("My Institution");
    externalSubject.setName("My Name");
    externalSubject.store();

    assertEquals("My Name - My Institution", externalSubject.getDescription());

    externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("b@idp.b.c");
    externalSubject.setName("My Name");
    externalSubject.store();

    assertEquals("My Name", externalSubject.getDescription());

    externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("c@idp.b.c");
    externalSubject.setInstitution("My Institution");
    externalSubject.store();

    assertEquals("My Institution", externalSubject.getDescription());

    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.desc.el");
    ExternalSubjectConfig.clearCache();

  }
  
  /**
   * test dynamic searchString
   */
  public void testDynamicSearchString() {

    //externalSubjects.searchStringFields = name, institution, identifier, uuid, email, jabber, description
    if (hasJabber) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.searchStringFields", 
          "name, institution, identifier, uuid, email, jabber, description");
    } else {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.searchStringFields", 
        "name, institution, identifier, uuid, email, description");
    }
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.desc.el", 
      "${grouperUtil.appendIfNotBlankString(externalSubject.getName(), ' - ', externalSubject.getInstitution())}");
    ExternalSubjectConfig.clearCache();

    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("a@idp.b.c");
    externalSubject.setInstitution("My Institution");
    externalSubject.setName("My Name");
    externalSubject.store();

    externalSubject.setEmail("a@b.c");
    externalSubject.store();
    if (hasJabber) {
      externalSubject.assignAttribute("jabber", "e@r.t");
    }
    
    externalSubject = ExternalSubjectStorageController.findByIdentifier("a@idp.b.c", true, null);
    
    if (hasJabber) {
      assertEquals("my name, my institution, a@idp.b.c, " + externalSubject.getUuid() + ", a@b.c, e@r.t, my name - my institution", externalSubject.getSearchStringLower());
    } else {
      assertEquals("my name, my institution, a@idp.b.c, " + externalSubject.getUuid() + ", a@b.c, my name - my institution", externalSubject.getSearchStringLower());
    }
    //make sure searches work
    Set<Subject> subjects = null;
    
    if (hasJabber) {
      subjects = SubjectFinder.findAll("e@r instit");
    } else {
      subjects = SubjectFinder.findAll("ion instit");
    }
    assertEquals(1, GrouperUtil.length(subjects));
    assertEquals("My Name", subjects.iterator().next().getName());
    
    subjects = SubjectFinder.findAll(externalSubject.getUuid());
    assertEquals(1, GrouperUtil.length(subjects));
    assertEquals("My Name", subjects.iterator().next().getName());
    
    subjects = SubjectFinder.findAll("name - my");
    assertEquals(1, GrouperUtil.length(subjects));
    assertEquals("My Name", subjects.iterator().next().getName());
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.searchStringFields");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.desc.el");
    ExternalSubjectConfig.clearCache();

  }
  
  /**
   * test dynamic description
   */
  public void testNonDynamicDescription() {

    //externalSubjects.desc.manual = true
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.desc.manual", "true");
    ExternalSubjectConfig.clearCache();

    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("a@idp.b.c");
    externalSubject.setInstitution("My Institution");
    externalSubject.setName("My Name");
    externalSubject.setDescription("My Description");
    externalSubject.store();

    assertEquals("My Description", externalSubject.getDescription());

    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.desc.manual");
    ExternalSubjectConfig.clearCache();

  }

  
  /**
   * test dynamic description, updated by daemon
   */
  public void testDynamicFieldsDaemon() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //externalSubjects.desc.el = ${grouperUtil.appendIfNotBlankString(externalSubject.name, ' - ', externalSubject.institution)}
    if (hasJabber) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.searchStringFields", 
        "name, institution, identifier, uuid, email, jabber, description");
    } else {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.searchStringFields", 
        "name, institution, identifier, uuid, email, description");
    }
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.desc.el", 
      "${grouperUtil.appendIfNotBlankString(externalSubject.getName(), ' - ', externalSubject.getInstitution())}");
    ExternalSubjectConfig.clearCache();

    ExternalSubject externalSubject = new ExternalSubject();
    externalSubject.setIdentifier("a@idp.b.c");
    externalSubject.setInstitution("My Institution");
    externalSubject.setName("My Name");
    externalSubject.setEmail("a@b.c");
    externalSubject.store();
    if (hasJabber) {
      externalSubject.assignAttribute("jabber", "e@r.t");
    }
    
    HibernateSession.bySqlStatic().executeSql("update grouper_ext_subj set description = 'a', search_string_lower = 'b' where identifier = 'a@idp.b.c'");

    externalSubject = ExternalSubjectStorageController.findByIdentifier("a@idp.b.c", true, null);
    
    assertEquals("a", externalSubject.getDescription());
    assertEquals("b", externalSubject.getSearchStringLower());
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_EXTERNAL_SUBJ_CALC_FIELDS);
    assertTrue(status.toLowerCase().contains("success"));

    externalSubject = ExternalSubjectStorageController.findByIdentifier("a@idp.b.c", true, new QueryOptions().secondLevelCache(false));

    assertEquals("My Name - My Institution", externalSubject.getDescription());
    if (hasJabber) {
      assertEquals("my name, my institution, a@idp.b.c, " + externalSubject.getUuid() + ", a@b.c, e@r.t, my name - my institution", externalSubject.getSearchStringLower());
    } else {
      assertEquals("my name, my institution, a@idp.b.c, " + externalSubject.getUuid() + ", a@b.c, my name - my institution", externalSubject.getSearchStringLower());
    }

    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.searchStringFields");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.desc.el");
    ExternalSubjectConfig.clearCache();

  }
  

}




