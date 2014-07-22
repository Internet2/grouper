/**
 * Copyright 2014 Internet2
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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.value;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Set;

import junit.textui.TestRunner;

import org.hibernate.exception.SQLGrammarException;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeAssignTest;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameTest;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignMemberDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class AttributeAssignValueTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignValueTest("testValuesOneQuery"));
  }
  
  /**
   * 
   */
  public AttributeAssignValueTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeAssignValueTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /** top stem */
  private Stem top;

  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");
  }
  
  /**
   * attribute def
   */
  public void testHibernate() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);
    attributeDef.setAssignToStem(true);
    attributeDef.store();
    
    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");

    AttributeAssign attributeAssign = new AttributeAssign(this.top, AttributeDef.ACTION_DEFAULT, attributeDefName, null);
    attributeAssign.saveOrUpdate(true);
    
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setAttributeAssignId(attributeAssign.getId());
    attributeAssignValue.setValueString("hello");
    attributeAssignValue.setId(GrouperUuid.getUuid());
    try {
      attributeAssignValue.saveOrUpdate();
    } catch (RuntimeException e) {
      
      Throwable cause = e.getCause();
      if (cause instanceof SQLGrammarException) {
        cause = cause.getCause();
      }
      if (cause instanceof BatchUpdateException) {
        SQLException sqlException = ((BatchUpdateException)cause).getNextException();
        if (sqlException != null) {
          sqlException.printStackTrace();
        }
      }
      
      throw e;
      
    }
  }

  /**
   * make an example AttributeDefScope for testing
   * @return an example AttributeDefScope
   */
  public static AttributeAssignValue exampleAttributeAssignValue() {
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setAttributeAssignId("attributeAssignId");
    attributeAssignValue.setContextId("contextId");
    attributeAssignValue.setCreatedOnDb(5L);
    attributeAssignValue.setHibernateVersionNumber(3L);
    attributeAssignValue.setLastUpdatedDb(6L);
    attributeAssignValue.setId("uuid");
    attributeAssignValue.setValueInteger(7L);
    attributeAssignValue.setValueMemberId("valueMemberId");
    attributeAssignValue.setValueString("valueString");
    return attributeAssignValue;
  }
  
  /**
   * make an example AttributeAssignValue from db for testing
   * @return an example AttributeAssignValue
   */
  public static AttributeAssignValue exampleAttributeAssignValueDb() {
    AttributeAssign attributeAssign = AttributeAssignTest.exampleAttributeAssignDb();
    AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory()
      .getAttributeAssignValue().findByUuidOrKey(null, null, attributeAssign.getId(), false, null, null, null);
    if (attributeAssignValue == null) {
      //create a new one
      attributeAssignValue = new AttributeAssignValue();
      attributeAssignValue.setId(GrouperUuid.getUuid());
      attributeAssignValue.setAttributeAssignId(attributeAssign.getId());
      attributeAssignValue.saveOrUpdate();
    }
    return attributeAssignValue;
  }

  
  /**
   * retrieve example AttributeAssignValue from db for testing
   * @return an example AttributeAssignValue
   */
  public static AttributeAssignValue exampleRetrieveAttributeAssignValueDb() {
    AttributeAssign attributeAssign = AttributeAssignTest.exampleAttributeAssignDb();
    AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory()
      .getAttributeAssignValue().findByUuidOrKey(null, null, attributeAssign.getId(), true, null, null, null);
    return attributeAssignValue;
  }

  /**
   * make sure update properties are detected correctly
   */
  public void testRetrieveMultiple() {
    
    AttributeAssign attributeAssign = AttributeAssignTest.exampleAttributeAssignDb();
    
    AttributeAssignValue attributeAssignValue1 = new AttributeAssignValue();
    attributeAssignValue1.setId(GrouperUuid.getUuid());
    attributeAssignValue1.setAttributeAssignId(attributeAssign.getId());
    attributeAssignValue1.setValueInteger(55L);
    attributeAssignValue1.saveOrUpdate();

    AttributeAssignValue attributeAssignValue2 = new AttributeAssignValue();
    attributeAssignValue2.setId(GrouperUuid.getUuid());
    attributeAssignValue2.setAttributeAssignId(attributeAssign.getId());
    attributeAssignValue2.setValueString("abc");
    attributeAssignValue2.saveOrUpdate();

    AttributeAssignValue attributeAssignValue3 = new AttributeAssignValue();
    attributeAssignValue3.setId(GrouperUuid.getUuid());
    attributeAssignValue3.setAttributeAssignId(attributeAssign.getId());
    attributeAssignValue3.setValueMemberId(MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ0, true).getUuid());
    attributeAssignValue3.saveOrUpdate();
    
    //get by id
    AttributeAssignValue attributeAssignValue = attributeAssignValue1.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeAssignValue1.getId(), attributeAssignValue.getId());

    attributeAssignValue = attributeAssignValue2.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeAssignValue2.getId(), attributeAssignValue.getId());

    attributeAssignValue = attributeAssignValue3.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeAssignValue3.getId(), attributeAssignValue.getId());
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    AttributeAssignValue attributeAssignValueOriginal = exampleAttributeAssignValueDb();
    
    //not sure why I need to sleep, but the last membership update gets messed up...
    GrouperUtil.sleep(1000);
    
    //do this because last membership update isnt there, only in db
    attributeAssignValueOriginal = exampleRetrieveAttributeAssignValueDb();
    AttributeAssignValue attributeAssignValueCopy = exampleRetrieveAttributeAssignValueDb();
    AttributeAssignValue attributeAssignValueCopy2 = exampleRetrieveAttributeAssignValueDb();
    attributeAssignValueCopy.delete();
    
    //lets insert the original
    attributeAssignValueCopy2.xmlSaveBusinessProperties(null);
    attributeAssignValueCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    attributeAssignValueCopy = exampleRetrieveAttributeAssignValueDb();
    
    assertFalse(attributeAssignValueCopy == attributeAssignValueOriginal);
    assertFalse(attributeAssignValueCopy.xmlDifferentBusinessProperties(attributeAssignValueOriginal));
    assertFalse(attributeAssignValueCopy.xmlDifferentUpdateProperties(attributeAssignValueOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeAssignValue attributeAssignValue = null;
    AttributeAssignValue exampleAttributeAssignValue = null;
    
    //TEST UPDATE PROPERTIES
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      attributeAssignValue.setContextId("abc");
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertTrue(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setContextId(exampleAttributeAssignValue.getContextId());
      attributeAssignValue.xmlSaveUpdateProperties();

      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
      
    }
    
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setCreatedOnDb(99L);
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertTrue(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setCreatedOnDb(exampleAttributeAssignValue.getCreatedOnDb());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
    }
    
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setLastUpdatedDb(99L);
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertTrue(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setLastUpdatedDb(exampleAttributeAssignValue.getLastUpdatedDb());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

    }

    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setHibernateVersionNumber(99L);
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertTrue(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setHibernateVersionNumber(exampleAttributeAssignValue.getHibernateVersionNumber());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setAttributeAssignId("abc");
      
      assertTrue(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setAttributeAssignId(exampleAttributeAssignValue.getAttributeAssignId());
      attributeAssignValue.xmlSaveBusinessProperties(exampleAttributeAssignValue.clone());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
    
    }
    
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setId("abc");
      
      assertTrue(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setId(exampleAttributeAssignValue.getId());
      attributeAssignValue.xmlSaveBusinessProperties(exampleAttributeAssignValue.clone());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
    
    }
    
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setValueInteger(99L);
      
      assertTrue(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setValueInteger(exampleAttributeAssignValue.getValueInteger());
      attributeAssignValue.xmlSaveBusinessProperties(exampleAttributeAssignValue.clone());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
    
    }
    
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setValueMemberId("abc");
      
      assertTrue(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setValueMemberId(exampleAttributeAssignValue.getValueMemberId());
      attributeAssignValue.xmlSaveBusinessProperties(exampleAttributeAssignValue.clone());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
    
    }
    
    {
      attributeAssignValue = exampleAttributeAssignValueDb();
      exampleAttributeAssignValue = exampleRetrieveAttributeAssignValueDb();

      attributeAssignValue.setValueString("abc");
      
      assertTrue(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));

      attributeAssignValue.setValueString(exampleAttributeAssignValue.getValueString());
      attributeAssignValue.xmlSaveBusinessProperties(exampleAttributeAssignValue.clone());
      attributeAssignValue.xmlSaveUpdateProperties();
      
      attributeAssignValue = exampleRetrieveAttributeAssignValueDb();
      
      assertFalse(attributeAssignValue.xmlDifferentBusinessProperties(exampleAttributeAssignValue));
      assertFalse(attributeAssignValue.xmlDifferentUpdateProperties(exampleAttributeAssignValue));
    
    }
    
  }

  /**
   * 
   */
  public void testMultiAttributeAssign() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignMultiDefName");
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrMultiAssign").assignName("test:groupTestAttrMultiAssign").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    //this is ok
    group.getAttributeDelegate().assignAttribute(attributeDefName);

    group.getAttributeDelegate().removeAttribute(attributeDefName);

    //add again, we need it
    group.getAttributeDelegate().assignAttribute(attributeDefName);

    assertTrue(group.getAttributeDelegate().hasAttribute(attributeDefName));
    
    //flag as multi-assign
    AttributeDef attributeDef = attributeDefName.getAttributeDef();
    attributeDef.setMultiAssignable(true);
    attributeDef.store();

    //this will work but is a no-op
    group.getAttributeDelegate().assignAttribute(attributeDefName);
    group.getAttributeDelegate().assignAttribute(attributeDefName);
    assertEquals(1, group.getAttributeDelegate().retrieveAssignments(attributeDefName).size());
    
    assertTrue(group.getAttributeDelegate().hasAttribute(attributeDefName));

    //should still work, will remove all
    group.getAttributeDelegate().removeAttribute(attributeDefName);
    
    //should be ok
    group.getAttributeDelegate().retrieveAssignments(attributeDefName);
    
    assertEquals(0, group.getAttributeDelegate().retrieveAssignments(attributeDefName).size());
    
    //add a couple of assignments
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().addAttribute(attributeDefName);
    @SuppressWarnings("unused")
    AttributeAssignResult attributeAssignResult2 = group.getAttributeDelegate().addAttribute(attributeDefName);
    
    assertEquals(2, group.getAttributeDelegate().retrieveAssignments(attributeDefName).size());
    
    //this is how to delete an assignment
    attributeAssignResult.getAttributeAssign().delete();
    
    assertEquals(1, group.getAttributeDelegate().retrieveAssignments(attributeDefName).size());
    
  }

  /**
   * 
   */
  public void testAttributeValueSecurity() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    //might be assigned to all, lets revoke
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);
    
    //this is ok
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.VIEW);
    
    attributeDefName.getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1,AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDefName.getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2,AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //GrouperSystem, ok
    attributeAssignResult.getAttributeAssign().getValueDelegate().retrieveValueInteger();
    
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      attributeAssignResult.getAttributeAssign().getValueDelegate().retrieveValueInteger();
      fail("Cant admin the attribute def");
    } catch (Exception e) {
      //good
    }
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      attributeAssignResult.getAttributeAssign().getValueDelegate().retrieveValueInteger();
      fail("Cant admin the group");
    } catch (Exception e) {
      //good
    }

    GrouperSession.start(SubjectTestHelper.SUBJ2);
    attributeAssignResult.getAttributeAssign().getValueDelegate().retrieveValueInteger();
    
    GrouperSession.start(SubjectTestHelper.SUBJ3);
    try {
      attributeAssignResult.getAttributeAssign().getValueDelegate().retrieveValueInteger();
      fail("Cant admin the attribute def or the group");
    } catch (Exception e) {
      //good
    }
  }
  
  /**
   * 
   */
  public void testAttributeValue() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    //this is ok
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);

    attributeAssignResult.getAttributeAssign().getValueDelegate().retrieveValueInteger();

  }
  
  /**
   * 
   */
  public void testExceptions() {

    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");

    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.integer);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    //this is ok
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignValue attributeAssignValue0 = attributeAssign.getValueDelegate().assignValueInteger(0L).getAttributeAssignValue();
    
    AttributeAssignValue attributeAssignValue0find = attributeAssign.getValueDelegate().findValue(attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesInteger().size());

    try {
      attributeAssign.getValueDelegate().addValueInteger(0L);
      fail("Cant assign multiple to a single valued attribute");
    } catch (Exception e) {
      //good, not multi valued
    }
  }
  
  /**
   * 
   */
  public void testExceptions2() {

    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");

    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.integer);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    //this is ok
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    try {
      attributeAssign.getValueDelegate().assignValueString("hey");
      fail("Cant assign string to an integer valued attribute");
    } catch (Exception e) {
      //good, not multi valued
    }
    
    try {
      attributeAssign.getValueDelegate().addValueString("hey");
      fail("Cant add string to an integer valued attribute");
    } catch (Exception e) {
      //good, not multi valued
    }
    
  }

  /**
   * 
   */
  public void testAttributeValueInteger() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");

    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.integer);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    //this is ok
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignValue attributeAssignValue0 = attributeAssign.getValueDelegate().assignValueInteger(0L).getAttributeAssignValue();
    
    AttributeAssignValue attributeAssignValue0find = attributeAssign.getValueDelegate().findValue(attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesInteger().size());
    attributeAssign.getValueDelegate().addValueInteger(0L);
    
    assertEquals(2, attributeAssign.getValueDelegate().retrieveValuesInteger().size());
    
    attributeAssign.getValueDelegate().assignValueInteger(0L);
    assertEquals(2, attributeAssign.getValueDelegate().retrieveValuesInteger().size());

    attributeAssign.getValueDelegate().deleteValue(attributeAssignValue0find);
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesInteger().size());

    attributeAssign.getValueDelegate().addValueInteger(0L);
    attributeAssign.getValueDelegate().deleteValueInteger(0L);
    
    assertEquals(0, attributeAssign.getValueDelegate().retrieveValuesInteger().size());
    
    attributeAssign.getValueDelegate().addValueInteger(2L);
    attributeAssign.getValueDelegate().addValueInteger(3L);
    
    attributeAssign.getValueDelegate().assignValuesInteger(GrouperUtil.toSet(3L, 4L, 5L), true);

    assertEquals(3, attributeAssign.getValueDelegate().retrieveValuesInteger().size());

    attributeAssign.getValueDelegate().addValuesInteger(GrouperUtil.toSet(5L, 6L));
    
    assertEquals(5, attributeAssign.getValueDelegate().retrieveValuesInteger().size());
    
    attributeAssign.getValueDelegate().deleteValuesInteger(GrouperUtil.toSet(4L, 5L, 6L));
    
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesInteger().size());
    assertEquals(new Long(3L), attributeAssign.getValueDelegate().retrieveValuesInteger().iterator().next());
  }
  
  /**
   * 
   */
  public void testValueEquals() {
    AttributeAssignValue attributeAssignValue1 = new AttributeAssignValue();
    AttributeAssignValue attributeAssignValue2 = new AttributeAssignValue();
    
    assertTrue(attributeAssignValue1.sameValue(attributeAssignValue2));
    
    attributeAssignValue1.setValueFloating(1.3);
    assertFalse(attributeAssignValue1.sameValue(attributeAssignValue2));

    attributeAssignValue2.setValueFloating(2.3);
    assertFalse(attributeAssignValue1.sameValue(attributeAssignValue2));

    attributeAssignValue1.setValueFloating(null);
    attributeAssignValue2.setValueFloating(null);
    assertTrue(attributeAssignValue1.sameValue(attributeAssignValue2));

    
    attributeAssignValue1.setValueInteger(4L);
    assertFalse(attributeAssignValue1.sameValue(attributeAssignValue2));

    attributeAssignValue2.setValueInteger(6L);
    assertFalse(attributeAssignValue1.sameValue(attributeAssignValue2));

    attributeAssignValue1.setValueInteger(null);
    attributeAssignValue2.setValueInteger(null);
    assertTrue(attributeAssignValue1.sameValue(attributeAssignValue2));

    
    
    attributeAssignValue1.setValueMemberId("abc");
    assertFalse(attributeAssignValue1.sameValue(attributeAssignValue2));

    attributeAssignValue2.setValueMemberId("bcd");
    assertFalse(attributeAssignValue1.sameValue(attributeAssignValue2));

    attributeAssignValue1.setValueMemberId(null);
    attributeAssignValue2.setValueMemberId(null);
    assertTrue(attributeAssignValue1.sameValue(attributeAssignValue2));

    
    
    attributeAssignValue1.setValueString("abc");
    assertFalse(attributeAssignValue1.sameValue(attributeAssignValue2));

    attributeAssignValue2.setValueString("bcd");
    assertFalse(attributeAssignValue1.sameValue(attributeAssignValue2));

    attributeAssignValue1.setValueString(null);
    attributeAssignValue2.setValueString(null);
    assertTrue(attributeAssignValue1.sameValue(attributeAssignValue2));

    
    
  }

  /**
   * 
   */
  public void testAttributeValueAddSecurity() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
    
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.string);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    
    //might be assigned to all, lets revoke
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);
    
    //this is ok
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
  
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    
    attributeDefName.getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1,AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDefName.getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2,AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //GrouperSystem, ok
    attributeAssignResult.getAttributeAssign().getValueDelegate().addValue("hey");
    
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      attributeAssignResult.getAttributeAssign().getValueDelegate().retrieveValueString();
      fail("Cant admin the attribute def");
    } catch (Exception e) {
      //good
    }
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      attributeAssignResult.getAttributeAssign().getValueDelegate().retrieveValueString();
      fail("Cant admin the group");
    } catch (Exception e) {
      //good
    }
  
    GrouperSession.start(SubjectTestHelper.SUBJ2);
    attributeAssignResult.getAttributeAssign().getValueDelegate().retrieveValueString();
    
    GrouperSession.start(SubjectTestHelper.SUBJ3);
    try {
      attributeAssignResult.getAttributeAssign().getValueDelegate().retrieveValueString();
      fail("Cant admin the attribute def or the group");
    } catch (Exception e) {
      //good
    }
  }

  /**
   * 
   */
  public void testAttributeValueFloating() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
  
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.floating);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    //this is ok
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignValue attributeAssignValue0 = attributeAssign.getValueDelegate().assignValueFloating(0D).getAttributeAssignValue();
    
    AttributeAssignValue attributeAssignValue0find = attributeAssign.getValueDelegate().findValue(attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesFloating().size());
    attributeAssign.getValueDelegate().addValueFloating(0D);
    
    assertEquals(2, attributeAssign.getValueDelegate().retrieveValuesFloating().size());
    
    attributeAssign.getValueDelegate().assignValueFloating(0D);
    assertEquals(2, attributeAssign.getValueDelegate().retrieveValuesFloating().size());
  
    attributeAssign.getValueDelegate().deleteValue(attributeAssignValue0find);
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesFloating().size());
  
    attributeAssign.getValueDelegate().addValueFloating(0D);
    attributeAssign.getValueDelegate().deleteValueFloating(0D);
    
    assertEquals(0, attributeAssign.getValueDelegate().retrieveValuesFloating().size());
    
    attributeAssign.getValueDelegate().addValueFloating(2D);
    attributeAssign.getValueDelegate().addValueFloating(3D);
    
    attributeAssign.getValueDelegate().assignValuesFloating(GrouperUtil.toSet(3D, 4D, 5D), true);
  
    assertEquals(3, attributeAssign.getValueDelegate().retrieveValuesFloating().size());
  
    attributeAssign.getValueDelegate().addValuesFloating(GrouperUtil.toSet(5D, 6D));
    
    assertEquals(5, attributeAssign.getValueDelegate().retrieveValuesFloating().size());
    
    attributeAssign.getValueDelegate().deleteValuesFloating(GrouperUtil.toSet(4D, 5D, 6D));
    
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesFloating().size());
    assertEquals(new Double(3D), attributeAssign.getValueDelegate().retrieveValuesFloating().iterator().next());
  }

  /**
   * 
   */
  public void testValueFloating() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
  
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.floating);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    //this is ok    
    AttributeAssignValue attributeAssignValue0 = group.getAttributeValueDelegate().assignValueFloating(
        "test:testAttributeAssignValueDefName", 0D).getAttributeAssignValueResult().getAttributeAssignValue();
    
    AttributeAssignValue attributeAssignValue0find = group.getAttributeValueDelegate().findValue("test:testAttributeAssignValueDefName", attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesFloating("test:testAttributeAssignValueDefName").size());
    group.getAttributeValueDelegate().addValueFloating("test:testAttributeAssignValueDefName", 0D);
    
    assertEquals(2, group.getAttributeValueDelegate().retrieveValuesFloating("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().assignValueFloating("test:testAttributeAssignValueDefName", 0D);
    assertEquals(2, group.getAttributeValueDelegate().retrieveValuesFloating("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().deleteValue("test:testAttributeAssignValueDefName", attributeAssignValue0find);
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesFloating("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().addValueFloating("test:testAttributeAssignValueDefName", 0D);
    group.getAttributeValueDelegate().deleteValueFloating("test:testAttributeAssignValueDefName", 0D);
    
    assertEquals(0, group.getAttributeValueDelegate().retrieveValuesFloating("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().addValueFloating("test:testAttributeAssignValueDefName", 2D);
    group.getAttributeValueDelegate().addValueFloating("test:testAttributeAssignValueDefName", 3D);
    
    group.getAttributeValueDelegate().assignValuesFloating("test:testAttributeAssignValueDefName", GrouperUtil.toSet(3D, 4D, 5D), true);
  
    assertEquals(3, group.getAttributeValueDelegate().retrieveValuesFloating("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().addValuesFloating("test:testAttributeAssignValueDefName", GrouperUtil.toSet(5D, 6D));
    
    assertEquals(5, group.getAttributeValueDelegate().retrieveValuesFloating("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().deleteValuesFloating("test:testAttributeAssignValueDefName", GrouperUtil.toSet(4D, 5D, 6D));
    
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesFloating("test:testAttributeAssignValueDefName").size());
    assertEquals(new Double(3D), group.getAttributeValueDelegate().retrieveValuesFloating("test:testAttributeAssignValueDefName").iterator().next());
  }

  /**
   * 
   */
  public void testAttributeValueString() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
  
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.string);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    //this is ok
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignValue attributeAssignValue0 = attributeAssign.getValueDelegate().assignValueString("0").getAttributeAssignValue();
    
    AttributeAssignValue attributeAssignValue0find = attributeAssign.getValueDelegate().findValue(attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesString().size());
    attributeAssign.getValueDelegate().addValueString("0");
    
    assertEquals(2, attributeAssign.getValueDelegate().retrieveValuesString().size());
    
    attributeAssign.getValueDelegate().assignValueString("0");
    assertEquals(2, attributeAssign.getValueDelegate().retrieveValuesString().size());
  
    attributeAssign.getValueDelegate().deleteValue(attributeAssignValue0find);
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesString().size());
  
    attributeAssign.getValueDelegate().addValueString("0");
    attributeAssign.getValueDelegate().deleteValueString("0");
    
    assertEquals(0, attributeAssign.getValueDelegate().retrieveValuesString().size());
    
    attributeAssign.getValueDelegate().addValueString("2");
    attributeAssign.getValueDelegate().addValueString("3");
    
    attributeAssign.getValueDelegate().assignValuesString(GrouperUtil.toSet("3", "4", "5"), true);
  
    assertEquals(3, attributeAssign.getValueDelegate().retrieveValuesString().size());
  
    attributeAssign.getValueDelegate().addValuesString(GrouperUtil.toSet("5", "6"));
    
    assertEquals(5, attributeAssign.getValueDelegate().retrieveValuesString().size());
    
    attributeAssign.getValueDelegate().deleteValuesString(GrouperUtil.toSet("4", "5", "6"));
    
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesString().size());
    assertEquals("3", attributeAssign.getValueDelegate().retrieveValuesString().iterator().next());
    
  }

  /**
   * 
   */
  public void testAttributeValueMember() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
  
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.memberId);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Member member0 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ0, true);

    Member member2 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ2, true);
    Member member3 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ3, true);
    Member member4 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ4, true);
    Member member5 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ5, true);
    Member member6 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ6, true);
    
    //this is ok
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignValue attributeAssignValue0 = attributeAssign.getValueDelegate().assignValueMember(member0).getAttributeAssignValue();
    
    AttributeAssignValue attributeAssignValue0find = attributeAssign.getValueDelegate().findValue(attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesMember().size());
    attributeAssign.getValueDelegate().addValueMember(member0);
    
    assertEquals(2, attributeAssign.getValueDelegate().retrieveValuesMember().size());
    
    attributeAssign.getValueDelegate().assignValueMember(member0);
    assertEquals(2, attributeAssign.getValueDelegate().retrieveValuesMember().size());
  
    attributeAssign.getValueDelegate().deleteValue(attributeAssignValue0find);
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesMember().size());
  
    attributeAssign.getValueDelegate().addValueMember(member0);
    attributeAssign.getValueDelegate().deleteValueMember(member0);
    
    assertEquals(0, attributeAssign.getValueDelegate().retrieveValuesMember().size());
    
    attributeAssign.getValueDelegate().addValueMember(member2);
    attributeAssign.getValueDelegate().addValueMember(member3);
    
    attributeAssign.getValueDelegate().assignValuesMember(GrouperUtil.toSet(member3, member4, member5), true);
  
    assertEquals(3, attributeAssign.getValueDelegate().retrieveValuesMember().size());
  
    attributeAssign.getValueDelegate().addValuesMember(GrouperUtil.toSet(member5, member6));
    
    assertEquals(5, attributeAssign.getValueDelegate().retrieveValuesMember().size());
    
    attributeAssign.getValueDelegate().deleteValuesMember(GrouperUtil.toSet(member4, member5, member6));
    
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesMember().size());
    assertEquals(member3, attributeAssign.getValueDelegate().retrieveValuesMember().iterator().next());
    
  }

  /**
   * 
   */
  public void testAttributeValueMemberId() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
  
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.memberId);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Member member0 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ0, true);
  
    Member member2 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ2, true);
    Member member3 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ3, true);
    Member member4 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ4, true);
    Member member5 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ5, true);
    Member member6 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ6, true);
    
    //this is ok
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignValue attributeAssignValue0 = attributeAssign.getValueDelegate().assignValueMember(member0.getUuid()).getAttributeAssignValue();
    
    AttributeAssignValue attributeAssignValue0find = attributeAssign.getValueDelegate().findValue(attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesMemberId().size());
    attributeAssign.getValueDelegate().addValueMember(member0.getUuid());
    
    assertEquals(2, attributeAssign.getValueDelegate().retrieveValuesMemberId().size());
    
    attributeAssign.getValueDelegate().assignValueMember(member0.getUuid());
    assertEquals(2, attributeAssign.getValueDelegate().retrieveValuesMemberId().size());
  
    attributeAssign.getValueDelegate().deleteValue(attributeAssignValue0find);
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesMemberId().size());
  
    attributeAssign.getValueDelegate().addValueMember(member0.getUuid());
    attributeAssign.getValueDelegate().deleteValueMember(member0.getUuid());
    
    assertEquals(0, attributeAssign.getValueDelegate().retrieveValuesMemberId().size());
    
    attributeAssign.getValueDelegate().addValueMember(member2.getUuid());
    attributeAssign.getValueDelegate().addValueMember(member3.getUuid());
    
    attributeAssign.getValueDelegate().assignValuesMemberIds(GrouperUtil.toSet(member3.getUuid(), member4.getUuid(), member5.getUuid()), true);
  
    assertEquals(3, attributeAssign.getValueDelegate().retrieveValuesMemberId().size());
  
    attributeAssign.getValueDelegate().addValuesMemberIds(GrouperUtil.toSet(member5.getUuid(), member6.getUuid()));
    
    assertEquals(5, attributeAssign.getValueDelegate().retrieveValuesMemberId().size());
    
    attributeAssign.getValueDelegate().deleteValuesMemberIds(GrouperUtil.toSet(member4.getUuid(), member5.getUuid(), member6.getUuid()));
    
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesMemberId().size());
    assertEquals(member3.getUuid(), attributeAssign.getValueDelegate().retrieveValuesMemberId().iterator().next());
    
  }

  /**
   * 
   */
  public void testAttributeValueTimestamp() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
  
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.timestamp);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();

    //this is ok
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignValue attributeAssignValue0 = attributeAssign.getValueDelegate().assignValueTimestamp(new Timestamp(0L)).getAttributeAssignValue();
    
    AttributeAssignValue attributeAssignValue0find = attributeAssign.getValueDelegate().findValue(attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesTimestamp().size());
    attributeAssign.getValueDelegate().addValueTimestamp(new Timestamp(0L));
    
    assertEquals(2, attributeAssign.getValueDelegate().retrieveValuesTimestamp().size());
    
    attributeAssign.getValueDelegate().assignValueTimestamp(new Timestamp(0L));
    assertEquals(2, attributeAssign.getValueDelegate().retrieveValuesTimestamp().size());
  
    attributeAssign.getValueDelegate().deleteValue(attributeAssignValue0find);
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesTimestamp().size());
  
    attributeAssign.getValueDelegate().addValueTimestamp(new Timestamp(0L));
    attributeAssign.getValueDelegate().deleteValueTimestamp(new Timestamp(0L));
    
    assertEquals(0, attributeAssign.getValueDelegate().retrieveValuesTimestamp().size());
    
    attributeAssign.getValueDelegate().addValueTimestamp(new Timestamp(2L));
    attributeAssign.getValueDelegate().addValueTimestamp(new Timestamp(3L));
    
    attributeAssign.getValueDelegate().assignValuesTimestamp(GrouperUtil.toSet(new Timestamp(3L), new Timestamp(4L), new Timestamp(5L)), true);
  
    assertEquals(3, attributeAssign.getValueDelegate().retrieveValuesTimestamp().size());
  
    attributeAssign.getValueDelegate().addValuesTimestamp(GrouperUtil.toSet(new Timestamp(5L), new Timestamp(6L)));
    
    assertEquals(5, attributeAssign.getValueDelegate().retrieveValuesTimestamp().size());
    
    attributeAssign.getValueDelegate().deleteValuesTimestamp(GrouperUtil.toSet(new Timestamp(4L), new Timestamp(5L), new Timestamp(6L)));
    
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesTimestamp().size());
    assertEquals(new Timestamp(3L), attributeAssign.getValueDelegate().retrieveValuesTimestamp().iterator().next());
  }

  /**
   * 
   */
  public void testValueInteger() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
  
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.integer);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    //this is ok    
    AttributeAssignValue attributeAssignValue0 = group.getAttributeValueDelegate().assignValueInteger(
        "test:testAttributeAssignValueDefName", 0L).getAttributeAssignValueResult().getAttributeAssignValue();
    
    AttributeAssignValue attributeAssignValue0find = group.getAttributeValueDelegate().findValue("test:testAttributeAssignValueDefName", attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesInteger("test:testAttributeAssignValueDefName").size());
    group.getAttributeValueDelegate().addValueInteger("test:testAttributeAssignValueDefName", 0L);
    
    assertEquals(2, group.getAttributeValueDelegate().retrieveValuesInteger("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().assignValueInteger("test:testAttributeAssignValueDefName", 0L);
    assertEquals(2, group.getAttributeValueDelegate().retrieveValuesInteger("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().deleteValue("test:testAttributeAssignValueDefName", attributeAssignValue0find);
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesInteger("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().addValueInteger("test:testAttributeAssignValueDefName", 0L);
    group.getAttributeValueDelegate().deleteValueInteger("test:testAttributeAssignValueDefName", 0L);
    
    assertEquals(0, group.getAttributeValueDelegate().retrieveValuesInteger("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().addValueInteger("test:testAttributeAssignValueDefName", 2L);
    group.getAttributeValueDelegate().addValueInteger("test:testAttributeAssignValueDefName", 3L);
    
    group.getAttributeValueDelegate().assignValuesInteger("test:testAttributeAssignValueDefName", GrouperUtil.toSet(3L, 4L, 5L), true);
  
    assertEquals(3, group.getAttributeValueDelegate().retrieveValuesInteger("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().addValuesInteger("test:testAttributeAssignValueDefName", GrouperUtil.toSet(5L, 6L));
    
    assertEquals(5, group.getAttributeValueDelegate().retrieveValuesInteger("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().deleteValuesInteger("test:testAttributeAssignValueDefName", GrouperUtil.toSet(4L, 5L, 6L));
    
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesInteger("test:testAttributeAssignValueDefName").size());
    assertEquals(new Long(3L), group.getAttributeValueDelegate().retrieveValuesInteger("test:testAttributeAssignValueDefName").iterator().next());
  }

  /**
   * 
   */
  public void testValueString() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
  
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.string);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    //this is ok    
    AttributeAssignValue attributeAssignValue0 = group.getAttributeValueDelegate().assignValueString(
        "test:testAttributeAssignValueDefName", "a").getAttributeAssignValueResult().getAttributeAssignValue();
    
    AttributeAssignValue attributeAssignValue0find = group.getAttributeValueDelegate().findValue("test:testAttributeAssignValueDefName", attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
    group.getAttributeValueDelegate().addValueString("test:testAttributeAssignValueDefName", "a");
    
    assertEquals(2, group.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().assignValueString("test:testAttributeAssignValueDefName", "a");
    assertEquals(2, group.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().deleteValue("test:testAttributeAssignValueDefName", attributeAssignValue0find);
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().addValueString("test:testAttributeAssignValueDefName", "a");
    group.getAttributeValueDelegate().deleteValueString("test:testAttributeAssignValueDefName", "a");
    
    assertEquals(0, group.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().addValueString("test:testAttributeAssignValueDefName", "b");
    group.getAttributeValueDelegate().addValueString("test:testAttributeAssignValueDefName", "c");
    
    group.getAttributeValueDelegate().assignValuesString("test:testAttributeAssignValueDefName", GrouperUtil.toSet("c", "d", "e"), true);
  
    assertEquals(3, group.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().addValuesString("test:testAttributeAssignValueDefName", GrouperUtil.toSet("e", "f"));
    
    assertEquals(5, group.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().deleteValuesString("test:testAttributeAssignValueDefName", GrouperUtil.toSet("d", "e", "f"));
    
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
    assertEquals(new String("c"), group.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").iterator().next());
  }

  /**
   * 
   */
  public void testValueTimestamp() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
  
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.timestamp);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    //this is ok    
    AttributeAssignValue attributeAssignValue0 = group.getAttributeValueDelegate().assignValueTimestamp(
        "test:testAttributeAssignValueDefName", new Timestamp(0)).getAttributeAssignValueResult().getAttributeAssignValue();
    
    AttributeAssignValue attributeAssignValue0find = group.getAttributeValueDelegate().findValue("test:testAttributeAssignValueDefName", attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesTimestamp("test:testAttributeAssignValueDefName").size());
    group.getAttributeValueDelegate().addValueTimestamp("test:testAttributeAssignValueDefName", new Timestamp(0));
    
    assertEquals(2, group.getAttributeValueDelegate().retrieveValuesTimestamp("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().assignValueTimestamp("test:testAttributeAssignValueDefName", new Timestamp(0));
    assertEquals(2, group.getAttributeValueDelegate().retrieveValuesTimestamp("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().deleteValue("test:testAttributeAssignValueDefName", attributeAssignValue0find);
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesTimestamp("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().addValueTimestamp("test:testAttributeAssignValueDefName", new Timestamp(0));
    group.getAttributeValueDelegate().deleteValueTimestamp("test:testAttributeAssignValueDefName", new Timestamp(0));
    
    assertEquals(0, group.getAttributeValueDelegate().retrieveValuesTimestamp("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().addValueTimestamp("test:testAttributeAssignValueDefName", new Timestamp(2));
    group.getAttributeValueDelegate().addValueTimestamp("test:testAttributeAssignValueDefName", new Timestamp(3));
    
    group.getAttributeValueDelegate().assignValuesTimestamp("test:testAttributeAssignValueDefName", GrouperUtil.toSet(new Timestamp(3), new Timestamp(4), new Timestamp(5)), true);
  
    assertEquals(3, group.getAttributeValueDelegate().retrieveValuesTimestamp("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().addValuesTimestamp("test:testAttributeAssignValueDefName", GrouperUtil.toSet(new Timestamp(5), new Timestamp(6)));
    
    assertEquals(5, group.getAttributeValueDelegate().retrieveValuesTimestamp("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().deleteValuesTimestamp("test:testAttributeAssignValueDefName", GrouperUtil.toSet(new Timestamp(4), new Timestamp(5), new Timestamp(6)));
    
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesTimestamp("test:testAttributeAssignValueDefName").size());
    assertEquals(new Timestamp(3), group.getAttributeValueDelegate().retrieveValuesTimestamp("test:testAttributeAssignValueDefName").iterator().next());
  }

  /**
   * 
   */
  public void testValueMember() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
  
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.memberId);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Member member0 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ0, true);

    Member member2 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ2, true);
    Member member3 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ3, true);
    Member member4 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ4, true);
    Member member5 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ5, true);
    Member member6 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ6, true);

    //this is ok    
    AttributeAssignValue attributeAssignValue0 = group.getAttributeValueDelegate().assignValueMember(
        "test:testAttributeAssignValueDefName", member0).getAttributeAssignValueResult().getAttributeAssignValue();
    
    AttributeAssignValue attributeAssignValue0find = group.getAttributeValueDelegate().findValue("test:testAttributeAssignValueDefName", attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
    group.getAttributeValueDelegate().addValueMember("test:testAttributeAssignValueDefName", member0);
    
    assertEquals(2, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().assignValueMember("test:testAttributeAssignValueDefName", member0);
    assertEquals(2, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().deleteValue("test:testAttributeAssignValueDefName", attributeAssignValue0find);
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().addValueMember("test:testAttributeAssignValueDefName", member0);
    group.getAttributeValueDelegate().deleteValueMember("test:testAttributeAssignValueDefName", member0);
    
    assertEquals(0, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().addValueMember("test:testAttributeAssignValueDefName", member2);
    group.getAttributeValueDelegate().addValueMember("test:testAttributeAssignValueDefName", member3);
    
    group.getAttributeValueDelegate().assignValuesMember("test:testAttributeAssignValueDefName", GrouperUtil.toSet(member3, member4, member5), true);
  
    assertEquals(3, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().addValuesMember("test:testAttributeAssignValueDefName", GrouperUtil.toSet(member5, member6));
    
    assertEquals(5, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().deleteValuesMember("test:testAttributeAssignValueDefName", GrouperUtil.toSet(member4, member5, member6));
    
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
    assertEquals(member3, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").iterator().next());
  }

  /**
   * 
   */
  public void testValueMemberId() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
  
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.memberId);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    Member member0 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ0, true);
  
    Member member2 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ2, true);
    Member member3 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ3, true);
    Member member4 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ4, true);
    Member member5 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ5, true);
    Member member6 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ6, true);
  
    //this is ok    
    AttributeAssignValue attributeAssignValue0 = group.getAttributeValueDelegate().assignValueMember(
        "test:testAttributeAssignValueDefName", member0.getUuid()).getAttributeAssignValueResult().getAttributeAssignValue();
    
    AttributeAssignValue attributeAssignValue0find = group.getAttributeValueDelegate().findValue("test:testAttributeAssignValueDefName", attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
    group.getAttributeValueDelegate().addValueMember("test:testAttributeAssignValueDefName", member0.getUuid());
    
    assertEquals(2, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().assignValueMember("test:testAttributeAssignValueDefName", member0.getUuid());
    assertEquals(2, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().deleteValue("test:testAttributeAssignValueDefName", attributeAssignValue0find);
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().addValueMember("test:testAttributeAssignValueDefName", member0.getUuid());
    group.getAttributeValueDelegate().deleteValueMember("test:testAttributeAssignValueDefName", member0.getUuid());
    
    assertEquals(0, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().addValueMember("test:testAttributeAssignValueDefName", member2.getUuid());
    group.getAttributeValueDelegate().addValueMember("test:testAttributeAssignValueDefName", member3.getUuid());
    
    group.getAttributeValueDelegate().assignValuesMemberIds("test:testAttributeAssignValueDefName", GrouperUtil.toSet(member3.getUuid(), member4.getUuid(), member5.getUuid()), true);
  
    assertEquals(3, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
  
    group.getAttributeValueDelegate().addValuesMemberIds("test:testAttributeAssignValueDefName", GrouperUtil.toSet(member5.getUuid(), member6.getUuid()));
    
    assertEquals(5, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
    
    group.getAttributeValueDelegate().deleteValuesMemberIds("test:testAttributeAssignValueDefName", GrouperUtil.toSet(member4.getUuid(), member5.getUuid(), member6.getUuid()));
    
    assertEquals(1, group.getAttributeValueDelegate().retrieveValuesMember("test:testAttributeAssignValueDefName").size());
    assertEquals(member3.getUuid(), group.getAttributeValueDelegate().retrieveValuesMemberId("test:testAttributeAssignValueDefName").iterator().next());
  }

  /**
   * 
   */
  public void testAttributeValueDeleteCascade() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
  
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.floating);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    //this is ok
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignValue attributeAssignValue0 = attributeAssign.getValueDelegate().assignValueFloating(0D).getAttributeAssignValue();
    
    AttributeAssignValue attributeAssignValue0find = attributeAssign.getValueDelegate().findValue(attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesFloating().size());
    
    group.getAttributeDelegate().removeAttributeByName("test:testAttributeAssignValueDefName");
    
    assertEquals(0, attributeAssign.getValueDelegate().retrieveValuesFloating().size());
    
  }
  
  /**
   * 
   */
  public void testAttributeValueDeleteCascade2() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
  
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.floating);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:groupTestAttrValue").assignName("test:groupTestAttrValue").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
  
    //this is ok
    AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(attributeDefName);
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    AttributeAssignValue attributeAssignValue0 = attributeAssign.getValueDelegate().assignValueFloating(0D).getAttributeAssignValue();
    
    AttributeAssignValue attributeAssignValue0find = attributeAssign.getValueDelegate().findValue(attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, attributeAssign.getValueDelegate().retrieveValuesFloating().size());
    
    attributeDefName.getAttributeDef().delete();
    
  }

  /**
   * pre populate members attributes
   */
  public void testMarkersOneQuery() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName2");
    AttributeDefName attributeDefName3 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName3");
  
    attributeDefName.getAttributeDef().setAssignToMember(true);
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.string);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();

    attributeDefName2.getAttributeDef().setAssignToMember(true);
    attributeDefName2.getAttributeDef().setValueType(AttributeDefValueType.string);
    attributeDefName2.getAttributeDef().setMultiValued(true);
    attributeDefName2.getAttributeDef().store();

    attributeDefName3.getAttributeDef().setAssignToMember(true);
    attributeDefName3.getAttributeDef().setValueType(AttributeDefValueType.string);
    attributeDefName3.getAttributeDef().setMultiValued(true);
    attributeDefName3.getAttributeDef().store();

    Member member0 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ2, true);
    Member member3 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ3, true);
    Member member4 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ4, true);
    @SuppressWarnings("unused")
    Member member7 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ7, true);
    
    //this is ok    
    AttributeAssignValue attributeAssignValue0 = member1.getAttributeValueDelegate().assignValueString(
        "test:testAttributeAssignValueDefName", "a").getAttributeAssignValueResult().getAttributeAssignValue();

    //assign one with no value
    member2.getAttributeDelegate().assignAttribute(attributeDefName3);

    member3.getAttributeValueDelegate().assignValueString(
        "test:testAttributeAssignValueDefName2", "c").getAttributeAssignValueResult().getAttributeAssignValue();

    member4.getAttributeValueDelegate().assignValueString(
        "test:testAttributeAssignValueDefName2", "d").getAttributeAssignValueResult().getAttributeAssignValue();

    Set<Member> members = GrouperUtil.toSet(member0, member1, member2);
    
    //lets preload with attributes
    AttributeAssignMemberDelegate.populateAttributeAssignments(members);

    Set<AttributeAssign> attributeAssigns = member1.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(1, attributeAssigns.size());

    
    AttributeAssignValue attributeAssignValue0find = member1.getAttributeValueDelegate().findValue("test:testAttributeAssignValueDefName", attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
    
    assertEquals(1, member1.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
    member1.getAttributeValueDelegate().addValueString("test:testAttributeAssignValueDefName", "a");
    
    assertEquals(2, member1.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
    
    member1.getAttributeValueDelegate().assignValueString("test:testAttributeAssignValueDefName", "a");
    assertEquals(2, member1.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
  
  }
  
  /**
   * pre populate members attributes
   */
  public void testAttrsOneQuery() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName2");
    AttributeDefName attributeDefName3 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName3");
  
    attributeDefName.getAttributeDef().setAssignToMember(true);
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.string);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();

    attributeDefName2.getAttributeDef().setAssignToMember(true);
    attributeDefName2.getAttributeDef().setValueType(AttributeDefValueType.string);
    attributeDefName2.getAttributeDef().setMultiValued(true);
    attributeDefName2.getAttributeDef().store();

    attributeDefName3.getAttributeDef().setAssignToMember(true);
    attributeDefName3.getAttributeDef().setValueType(AttributeDefValueType.string);
    attributeDefName3.getAttributeDef().setMultiValued(true);
    attributeDefName3.getAttributeDef().store();

    Member member0 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ1, true);
    Member member1nonCache = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ2, true);
    Member member3 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ3, true);
    Member member4 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ4, true);
    @SuppressWarnings("unused")
    Member member7 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ7, true);
    
    //this is ok    
    AttributeAssignValue attributeAssignValue0 = member1.getAttributeValueDelegate().assignValueString(
        "test:testAttributeAssignValueDefName", "a").getAttributeAssignValueResult().getAttributeAssignValue();

    //assign one with no value
    member2.getAttributeDelegate().assignAttribute(attributeDefName3);

    member3.getAttributeValueDelegate().assignValueString(
        "test:testAttributeAssignValueDefName2", "c").getAttributeAssignValueResult().getAttributeAssignValue();

    member4.getAttributeValueDelegate().assignValueString(
        "test:testAttributeAssignValueDefName2", "d").getAttributeAssignValueResult().getAttributeAssignValue();

    Set<Member> members = GrouperUtil.toSet(member0, member1, member2);
    
    //############## lets preload with attributes
    AttributeAssignMemberDelegate.populateAttributeAssignments(members);

    //############## check non cache
    int cacheHits = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest;
    int cacheMisses = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest;
    
    Set<AttributeAssign> attributeAssigns = member1nonCache.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(1, attributeAssigns.size());

    assertEquals(cacheHits, AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest);
    assertTrue(cacheMisses + ", " + AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest, cacheMisses < AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest);
    
    cacheHits = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest;
    cacheMisses = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest;

    
    //############## check cache
    
    cacheHits = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest;
    cacheMisses = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest;
    
    attributeAssigns = member1.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(1, attributeAssigns.size());

    assertEquals(cacheHits+1, AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest);
    assertEquals(cacheMisses, AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest);
    
    //############## check cache
    
    cacheHits = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest;
    cacheMisses = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest;
    
    attributeAssigns = member2.getAttributeDelegate().retrieveAssignments(attributeDefName3);
    assertEquals(1, attributeAssigns.size());
    
    assertEquals(attributeAssigns.iterator().next().getAttributeDefNameId(), attributeDefName3.getId());
    
    assertEquals(cacheHits+1, AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest);
    assertEquals(cacheMisses, AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest);
    
    //############## check non cache
    
    cacheHits = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest;
    cacheMisses = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest;
    
    attributeAssigns = member4.getAttributeDelegate().retrieveAssignments(attributeDefName2);
    assertEquals(1, attributeAssigns.size());
    
    assertEquals(attributeAssigns.iterator().next().getAttributeDefNameId(), attributeDefName2.getId());
    
    assertEquals(cacheHits, AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest);
    assertEquals(cacheMisses+1, AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest);
    
    //############## check cache, values arent in cache...
    
    cacheHits = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest;
    cacheMisses = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest;

    AttributeAssignValue attributeAssignValue0find = member1.getAttributeValueDelegate().findValue("test:testAttributeAssignValueDefName", attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));

    assertEquals(cacheHits+1, AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest);
    assertEquals(cacheMisses, AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest);

    assertEquals(1, member1.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
    member1.getAttributeValueDelegate().addValueString("test:testAttributeAssignValueDefName", "a");
    
    assertEquals(2, member1.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
    
    member1.getAttributeValueDelegate().assignValueString("test:testAttributeAssignValueDefName", "a");
    assertEquals(2, member1.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
  
    member1.getAttributeValueDelegate().deleteValue("test:testAttributeAssignValueDefName", attributeAssignValue0find);
    assertEquals(1, member1.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
  
    member1.getAttributeValueDelegate().addValueString("test:testAttributeAssignValueDefName", "a");
    member1.getAttributeValueDelegate().deleteValueString("test:testAttributeAssignValueDefName", "a");
    
    assertEquals(0, member1.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());
    
  }

  /**
   * pre populate members attributes
   */
  public void testValuesOneQuery() {
    AttributeDefName attributeDefName = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName");
    AttributeDefName attributeDefName2 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName2");
    AttributeDefName attributeDefName3 = AttributeDefNameTest.exampleAttributeDefNameDb("test", "testAttributeAssignValueDefName3");
  
    attributeDefName.getAttributeDef().setAssignToMember(true);
    attributeDefName.getAttributeDef().setValueType(AttributeDefValueType.string);
    attributeDefName.getAttributeDef().setMultiValued(true);
    attributeDefName.getAttributeDef().store();
  
    attributeDefName2.getAttributeDef().setAssignToMember(true);
    attributeDefName2.getAttributeDef().setValueType(AttributeDefValueType.string);
    attributeDefName2.getAttributeDef().setMultiValued(true);
    attributeDefName2.getAttributeDef().store();
  
    attributeDefName3.getAttributeDef().setAssignToMember(true);
    attributeDefName3.getAttributeDef().setValueType(AttributeDefValueType.string);
    attributeDefName3.getAttributeDef().setMultiValued(true);
    attributeDefName3.getAttributeDef().store();
  
    Member member0 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ1, true);
    Member member1nonCache = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ2, true);
    Member member3 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ3, true);
    Member member4 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ4, true);
    @SuppressWarnings("unused")
    Member member7 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ7, true);
    
    //this is ok    
    AttributeAssignValue attributeAssignValue0 = member1.getAttributeValueDelegate().assignValueString(
        "test:testAttributeAssignValueDefName", "a").getAttributeAssignValueResult().getAttributeAssignValue();
  
    //assign one with no value
    member2.getAttributeDelegate().assignAttribute(attributeDefName3);
  
    member3.getAttributeValueDelegate().assignValueString(
        "test:testAttributeAssignValueDefName2", "c").getAttributeAssignValueResult().getAttributeAssignValue();
  
    member4.getAttributeValueDelegate().assignValueString(
        "test:testAttributeAssignValueDefName2", "d").getAttributeAssignValueResult().getAttributeAssignValue();
  
    Set<Member> members = GrouperUtil.toSet(member0, member1, member2);
    
    //############## lets preload with attributes
    AttributeAssignMemberDelegate.populateAttributeAssignments(members);
  
    //############## check non cache
    int cacheHits = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest;
    int cacheMisses = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest;
    
    Set<AttributeAssign> attributeAssigns = member1nonCache.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(1, attributeAssigns.size());
  
    assertEquals(cacheHits, AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest);
    assertTrue(cacheMisses + ", " + AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest, cacheMisses < AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest);
    
    cacheHits = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest;
    cacheMisses = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest;
  
    
    //############## check cache
    
    cacheHits = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest;
    cacheMisses = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest;
    
    attributeAssigns = member1.getAttributeDelegate().retrieveAssignments(attributeDefName);
    assertEquals(1, attributeAssigns.size());
  
    assertEquals(cacheHits+1, AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest);
    assertEquals(cacheMisses, AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest);
    
    //############## check cache
    
    cacheHits = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest;
    cacheMisses = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest;
    
    attributeAssigns = member2.getAttributeDelegate().retrieveAssignments(attributeDefName3);
    assertEquals(1, attributeAssigns.size());
    
    assertEquals(attributeAssigns.iterator().next().getAttributeDefNameId(), attributeDefName3.getId());
    
    assertEquals(cacheHits+1, AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest);
    assertEquals(cacheMisses, AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest);
    
    //############## check non cache
    
    cacheHits = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest;
    cacheMisses = (int)AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest;
    
    attributeAssigns = member4.getAttributeDelegate().retrieveAssignments(attributeDefName2);
    assertEquals(1, attributeAssigns.size());
    
    assertEquals(attributeAssigns.iterator().next().getAttributeDefNameId(), attributeDefName2.getId());
    
    assertEquals(cacheHits, AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest);
    assertEquals(cacheMisses+1, AttributeAssignBaseDelegate.allAttributeAssignsCacheMissesForTest);
    
    //############## check cache,for values
    
    cacheHits = (int)AttributeAssignValueDelegate.allAttributeAssignValuesCacheHitsForTest;
    cacheMisses = (int)AttributeAssignValueDelegate.allAttributeAssignValuesCacheMissesForTest;
  
    AttributeAssignValue attributeAssignValue0find = member1.getAttributeValueDelegate().findValue("test:testAttributeAssignValueDefName", attributeAssignValue0);
    assertTrue(attributeAssignValue0.sameValue(attributeAssignValue0find));
  
    assertEquals(1, member1.getAttributeValueDelegate().retrieveValuesString("test:testAttributeAssignValueDefName").size());

    assertEquals("a", member1.getAttributeValueDelegate().retrieveValueString("test:testAttributeAssignValueDefName"));

    assertTrue(cacheHits + ", " + AttributeAssignValueDelegate.allAttributeAssignValuesCacheHitsForTest, cacheHits < AttributeAssignBaseDelegate.allAttributeAssignsCacheHitsForTest);
    assertEquals(cacheMisses, AttributeAssignValueDelegate.allAttributeAssignValuesCacheMissesForTest);
  

  }
  
}
