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
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestComposite.java,v 1.2 2009-03-20 19:56:40 mchyzer Exp $
 */
public class TestAttribute extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(TestAttribute.class);
    TestRunner.run(new TestAttribute("testXmlDifferentUpdateProperties"));
  }
  
  /** */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(TestAttribute.class);

  /**
   * 
   * @param name
   */
  public TestAttribute(String name) {
    super(name);
  }

  /**
   * make an example attribute for testing
   * @return an example attribute
   */
  public static Attribute exampleAttribute() {
    Attribute attribute = new Attribute();
    attribute.setContextId("contextId");
    attribute.setFieldId("fieldId");
    attribute.setGroupUuid("groupId");
    attribute.setHibernateVersionNumber(3L);
    attribute.setId("uuid");
    attribute.setValue("value");
    
    return attribute;
  }
  
  /**
   * make an example attribute for testing
   * @return an example attribute
   */
  public static Attribute exampleAttributeDb() {
    Group attributeGroup = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:attribute").assignName("test:attribute").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    GroupType groupType = GroupTypeFinder.find("testAttrType", false); 
    
    if (groupType == null) {
      groupType = GroupType.createType(GrouperSession.staticGrouperSession(), "testAttrType");
      groupType.addAttribute(GrouperSession.staticGrouperSession(), "attrTest", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false);
      
    }
    
    attributeGroup.addType(groupType, false);
    
    attributeGroup.setAttribute("attrTest", "testValue");
    attributeGroup.store();
    
    Field field = FieldFinder.find("attrTest", true);
    
    Attribute attribute = GrouperDAOFactory.getFactory().getAttribute().findByUuidOrName(null, attributeGroup.getId(), field.getUuid(), true);
    
    return attribute;
  }

  
  /**
   * make an example composite for testing
   * @return an example composite
   */
  public static Attribute exampleRetrieveAttributeDb() {
    Group attributeGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), "test:attribute", true);
    Field field = FieldFinder.find("attrTest", true);
    Attribute attribute = GrouperDAOFactory.getFactory().getAttribute().findByUuidOrName(null, attributeGroup.getId(), field.getUuid(), true);
    return attribute;
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    Group attributeGroup = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:attributeInsert").assignName("test:attributeInsert").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    GroupType groupType = GroupTypeFinder.find("testAttrTypeInsert", false); 
    
    if (groupType == null) {
      groupType = GroupType.createType(GrouperSession.staticGrouperSession(), "testAttrTypeInsert");
      groupType.addAttribute(GrouperSession.staticGrouperSession(), "attrTestInsert", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false);
      
    }
    
    attributeGroup.addType(groupType, false);
    
    attributeGroup.setAttribute("attrTestInsert", "testValueInsert");
    attributeGroup.store();
    
    Field field = FieldFinder.find("attrTestInsert", true);
    
    Attribute attribute = GrouperDAOFactory.getFactory().getAttribute().findByUuidOrName(null, attributeGroup.getId(), field.getUuid(), true);
    
    //do this because last membership update isnt there, only in db
    attribute = GrouperDAOFactory.getFactory().getAttribute().findByUuidOrName(attribute.getId(), null, null, true);
    Attribute attributeCopy = GrouperDAOFactory.getFactory().getAttribute().findByUuidOrName(attribute.getId(), null, null, true);
    Attribute attributeCopy2 = GrouperDAOFactory.getFactory().getAttribute().findByUuidOrName(attribute.getId(), null, null, true);
    attributeCopy.delete();
    
    //lets insert the original
    attributeCopy2.xmlSaveBusinessProperties(null);
    attributeCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    attributeCopy =  GrouperDAOFactory.getFactory().getAttribute().findByUuidOrName(null, attributeGroup.getId(), field.getUuid(), true);
    
    assertFalse(attributeCopy == attribute);
    assertFalse(attributeCopy.xmlDifferentBusinessProperties(attribute));
    assertFalse(attributeCopy.xmlDifferentUpdateProperties(attribute));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Attribute attribute = null;
    Attribute exampleAttribute = null;

    
    //TEST UPDATE PROPERTIES
    {
      attribute = exampleAttributeDb();
      exampleAttribute = attribute.clone();
      
      attribute.setContextId("abc");
      
      assertFalse(attribute.xmlDifferentBusinessProperties(exampleAttribute));
      assertTrue(attribute.xmlDifferentUpdateProperties(exampleAttribute));

      attribute.setContextId(exampleAttribute.getContextId());
      attribute.xmlSaveUpdateProperties();

      attribute = exampleRetrieveAttributeDb();
      
      assertFalse(attribute.xmlDifferentBusinessProperties(exampleAttribute));
      assertFalse(attribute.xmlDifferentUpdateProperties(exampleAttribute));
      
    }
    
    {
      attribute = exampleAttributeDb();
      exampleAttribute = attribute.clone();

      attribute.setHibernateVersionNumber(99L);
      
      assertFalse(attribute.xmlDifferentBusinessProperties(exampleAttribute));
      assertTrue(attribute.xmlDifferentUpdateProperties(exampleAttribute));

      attribute.setHibernateVersionNumber(exampleAttribute.getHibernateVersionNumber());
      attribute.xmlSaveUpdateProperties();
      
      attribute = exampleRetrieveAttributeDb();
      
      assertFalse(attribute.xmlDifferentBusinessProperties(exampleAttribute));
      assertFalse(attribute.xmlDifferentUpdateProperties(exampleAttribute));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      attribute = exampleAttributeDb();
      exampleAttribute = attribute.clone();

      attribute.setFieldId("abc");
      
      assertTrue(attribute.xmlDifferentBusinessProperties(exampleAttribute));
      assertFalse(attribute.xmlDifferentUpdateProperties(exampleAttribute));

      attribute.setFieldId(exampleAttribute.getFieldId());
      attribute.xmlSaveBusinessProperties(exampleAttribute.clone());
      attribute.xmlSaveUpdateProperties();
      
      attribute = exampleRetrieveAttributeDb();
      
      assertFalse(attribute.xmlDifferentBusinessProperties(exampleAttribute));
      assertFalse(attribute.xmlDifferentUpdateProperties(exampleAttribute));
    
    }
    
    {
      attribute = exampleAttributeDb();
      exampleAttribute = attribute.clone();

      attribute.setGroupUuid("abc");
      
      assertTrue(attribute.xmlDifferentBusinessProperties(exampleAttribute));
      assertFalse(attribute.xmlDifferentUpdateProperties(exampleAttribute));

      attribute.setGroupUuid(exampleAttribute.getGroupUuid());
      attribute.xmlSaveBusinessProperties(exampleAttribute.clone());
      attribute.xmlSaveUpdateProperties();
      
      attribute = exampleRetrieveAttributeDb();
      
      assertFalse(attribute.xmlDifferentBusinessProperties(exampleAttribute));
      assertFalse(attribute.xmlDifferentUpdateProperties(exampleAttribute));
    
    }
    
    {
      attribute = exampleAttributeDb();
      exampleAttribute = attribute.clone();

      attribute.setId("abc");
      
      assertTrue(attribute.xmlDifferentBusinessProperties(exampleAttribute));
      assertFalse(attribute.xmlDifferentUpdateProperties(exampleAttribute));

      attribute.setId(exampleAttribute.getId());
      attribute.xmlSaveBusinessProperties(exampleAttribute.clone());
      attribute.xmlSaveUpdateProperties();
      
      attribute = exampleRetrieveAttributeDb();
      
      assertFalse(attribute.xmlDifferentBusinessProperties(exampleAttribute));
      assertFalse(attribute.xmlDifferentUpdateProperties(exampleAttribute));
    
    }
    
    {
      attribute = exampleAttributeDb();
      exampleAttribute = attribute.clone();

      attribute.setValue("abc");
      
      assertTrue(attribute.xmlDifferentBusinessProperties(exampleAttribute));
      assertFalse(attribute.xmlDifferentUpdateProperties(exampleAttribute));

      attribute.setValue(exampleAttribute.getValue());
      attribute.xmlSaveBusinessProperties(exampleAttribute.clone());
      attribute.xmlSaveUpdateProperties();
      
      attribute = exampleRetrieveAttributeDb();
      
      assertFalse(attribute.xmlDifferentBusinessProperties(exampleAttribute));
      assertFalse(attribute.xmlDifferentUpdateProperties(exampleAttribute));
    
    }
    
  }

  
  
}

