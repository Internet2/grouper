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
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.helper.FieldHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Test {@link Field}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestField.java,v 1.14 2009-11-05 06:10:51 mchyzer Exp $
 */
public class TestField extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestField("testXmlDifferentUpdateProperties"));
    //TestRunner.run(TestField.class);
  }
  
  /**
   * 
   * @param name
   */
  public TestField(String name) {
    super(name);
  }

  protected void setUp () {
    FieldFinder.find("viewers", true);
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testFields() {
    Set       fields  = FieldFinder.findAll();
    Assert.assertEquals("fields", 21, fields.size());
    Iterator  iter    = fields.iterator();
    FieldHelper.testField( 
      (Field) iter.next()   , 
      Field.FIELD_NAME_ADMINS              , FieldType.ACCESS,
      AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
    );
    FieldHelper.testField( 
        (Field) iter.next()   , 
        "attrAdmins"              , FieldType.ATTRIBUTE_DEF,
        AttributeDefPrivilege.ATTR_ADMIN , AttributeDefPrivilege.ATTR_ADMIN
      );
    
    FieldHelper.testField( 
        (Field) iter.next()   , 
        "attrDefAttrReaders"              , FieldType.ATTRIBUTE_DEF,
        AttributeDefPrivilege.ATTR_ADMIN , AttributeDefPrivilege.ATTR_ADMIN
      );
    
    FieldHelper.testField( 
        (Field) iter.next()   , 
        "attrDefAttrUpdaters"              , FieldType.ATTRIBUTE_DEF,
        AttributeDefPrivilege.ATTR_ADMIN , AttributeDefPrivilege.ATTR_ADMIN
      );
    FieldHelper.testField( 
        (Field) iter.next()   , 
        "attrOptins"              , FieldType.ATTRIBUTE_DEF,
        AttributeDefPrivilege.ATTR_UPDATE , AttributeDefPrivilege.ATTR_UPDATE
      );
    FieldHelper.testField( 
        (Field) iter.next()   , 
        "attrOptouts"              , FieldType.ATTRIBUTE_DEF,
        AttributeDefPrivilege.ATTR_UPDATE , AttributeDefPrivilege.ATTR_UPDATE
      );
    FieldHelper.testField( 
        (Field) iter.next()   , 
        "attrReaders"              , FieldType.ATTRIBUTE_DEF,
        AttributeDefPrivilege.ATTR_ADMIN , AttributeDefPrivilege.ATTR_ADMIN
      );
    FieldHelper.testField( 
        (Field) iter.next()   , 
        "attrUpdaters"              , FieldType.ATTRIBUTE_DEF,
        AttributeDefPrivilege.ATTR_ADMIN , AttributeDefPrivilege.ATTR_ADMIN
      );
    FieldHelper.testField( 
        (Field) iter.next()   , 
        "attrViewers"              , FieldType.ATTRIBUTE_DEF,
        AttributeDefPrivilege.ATTR_ADMIN , AttributeDefPrivilege.ATTR_ADMIN
      );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      Field.FIELD_NAME_CREATORS            , FieldType.NAMING,
      NamingPrivilege.STEM  , NamingPrivilege.STEM
    );
    FieldHelper.testField( 
        (Field) iter.next()   , 
        Field.FIELD_NAME_GROUP_ATTR_READERS              , FieldType.ACCESS,
        AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
      );
    FieldHelper.testField( 
        (Field) iter.next()   , 
        Field.FIELD_NAME_GROUP_ATTR_UPDATERS              , FieldType.ACCESS,
        AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
      );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "members"             , FieldType.LIST,
      AccessPrivilege.READ  , AccessPrivilege.UPDATE
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      Field.FIELD_NAME_OPTINS              , FieldType.ACCESS,
      AccessPrivilege.UPDATE, AccessPrivilege.UPDATE
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      Field.FIELD_NAME_OPTOUTS             , FieldType.ACCESS,
      AccessPrivilege.UPDATE, AccessPrivilege.UPDATE
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "readers"             , FieldType.ACCESS,
      AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
    );
    FieldHelper.testField( 
        (Field) iter.next()   , 
        Field.FIELD_NAME_STEM_ATTR_READERS          , FieldType.NAMING,
        NamingPrivilege.STEM  , NamingPrivilege.STEM
      );
    FieldHelper.testField( 
        (Field) iter.next()   , 
        Field.FIELD_NAME_STEM_ATTR_UPDATERS            , FieldType.NAMING,
        NamingPrivilege.STEM  , NamingPrivilege.STEM
      );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      Field.FIELD_NAME_STEMMERS            , FieldType.NAMING,
      NamingPrivilege.STEM  , NamingPrivilege.STEM
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      Field.FIELD_NAME_UPDATERS            , FieldType.ACCESS,
      AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "viewers"             , FieldType.ACCESS,
      AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
    );
  } // public void testFields()

  /**
   * 
   */
  public void testCache() throws Exception {
    
    int originalFieldCacheSeconds = FieldFinder.defaultFieldCacheSeconds;
    String originalFieldCacheName = FieldFinder.cacheName;
    
    try {
    
      FieldFinder.defaultFieldCacheSeconds = 3;
      FieldFinder.cacheName = TestField.class.getName() + ".testFieldCache";
      FieldFinder.fieldGrouperCache = null;

      try {
        FieldFinder.find("sadfasdf");
      } catch (SchemaException se) {
        
      }
  
      //refreshed in last second
      long theLastRefreshed = FieldFinder.lastTimeRefreshed;
      assertTrue(System.currentTimeMillis() - theLastRefreshed < 1000);
  
      GrouperUtil.sleep(100);
      
      try {
        FieldFinder.find("sadfasdf");
      } catch (SchemaException se) {
        
      }
  
      assertEquals(theLastRefreshed, FieldFinder.lastTimeRefreshed);
      
      //wait 3 seconds
      GrouperUtil.sleep(3000);
      
      try {
        FieldFinder.find("sadfasdf");
      } catch (SchemaException se) {
        
      }
  
      assertTrue(theLastRefreshed < FieldFinder.lastTimeRefreshed);
      
      theLastRefreshed = FieldFinder.lastTimeRefreshed;
      
      Field field = FieldFinder.find(Field.FIELD_NAME_UPDATERS);
      FieldFinder.findById(field.getUuid());
      
      assertEquals(theLastRefreshed, FieldFinder.lastTimeRefreshed);
      
      //make sure clock updates
      GrouperUtil.sleep(100);
      
      //find one not there, should refresh cache
      try {
        FieldFinder.findById("abc");
        fail("Should throw exception");
      } catch (RuntimeException re) {
        //good
      }

      assertTrue(theLastRefreshed < FieldFinder.lastTimeRefreshed);

      int allFieldsSize = FieldFinder.findAll().size();
      assertTrue(allFieldsSize > 5);
      
      int accessFieldsSize = FieldFinder.findAllByType(FieldType.ACCESS).size();
      
      assertTrue(accessFieldsSize > 1 && allFieldsSize > accessFieldsSize);
    } finally {
      FieldFinder.cacheName = originalFieldCacheName;
      FieldFinder.defaultFieldCacheSeconds = originalFieldCacheSeconds;
      FieldFinder.fieldGrouperCache = null;
    }
  }

  /**
   * make an example field for testing
   * @return an example field
   */
  public static Field exampleField() {
    Field field = new Field();
    field.setContextId("contextId");
    field.setHibernateVersionNumber(3L);
    field.setName("name");
    field.setReadPrivilege("readPrivilege");
    field.setTypeString("type");
    field.setUuid("uuid");
    field.setWritePrivilege("writePrivilege");
    return field;
  }
  
  /**
   * make an example field for testing
   * @return an example field
   */
  public static Field exampleFieldDb() {
    Field field = FieldFinder.find("example", false);
    if (field == null) {
      GroupType groupType = TestGroupType.exampleGroupTypeDb();
      field = groupType.addList(GrouperSession.staticGrouperSession(), "example", AccessPrivilege.READ, AccessPrivilege.ADMIN );
    }
    
    return field;
  }

  
  /**
   * make an example field for testing
   * @return an example field
   */
  public static Field exampleRetrieveFieldDb() {
    FieldFinder.clearCache();
    Field field = FieldFinder.find("example", true);
    return field;
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    Field fieldOriginal = TestGroupType.exampleGroupTypeDb().addList(
        GrouperSession.staticGrouperSession(), "exampleInsert", AccessPrivilege.READ, AccessPrivilege.ADMIN);
    fieldOriginal = FieldFinder.find("exampleInsert", true);
    //dont let cache corrupt things
    fieldOriginal = GrouperDAOFactory.getFactory().getField().findByUuidOrName(fieldOriginal.getUuid(), "sdafsadfasdf", true);
    //do this because last membership update isnt there, only in db
    Field fieldCopy = GrouperDAOFactory.getFactory().getField().findByUuidOrName(fieldOriginal.getUuid(), "sdafsadfasdf", true);
    Field fieldCopy2 = GrouperDAOFactory.getFactory().getField().findByUuidOrName(fieldOriginal.getUuid(), "sdafsadfasdf", true);
    TestGroupType.exampleGroupTypeDb().deleteField(GrouperSession.staticGrouperSession(), fieldCopy.getName());
    
    //lets insert the original
    fieldCopy2.xmlSaveBusinessProperties(null);
    fieldCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    fieldCopy = FieldFinder.findById(fieldOriginal.getUuid(), true);
    
    assertFalse(fieldCopy == fieldOriginal);
    assertFalse(fieldCopy.xmlDifferentBusinessProperties(fieldOriginal));
    assertFalse(fieldCopy.xmlDifferentUpdateProperties(fieldOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Field field = null;
    Field exampleField = null;

    
    //TEST UPDATE PROPERTIES
    {
      field = exampleFieldDb();
      exampleField = field.clone();
      
      field.setContextId("abc");
      
      assertFalse(field.xmlDifferentBusinessProperties(exampleField));
      assertTrue(field.xmlDifferentUpdateProperties(exampleField));

      field.setContextId(exampleField.getContextId());
      field.xmlSaveUpdateProperties();

      field = exampleRetrieveFieldDb();
      
      assertFalse(field.xmlDifferentBusinessProperties(exampleField));
      assertFalse(field.xmlDifferentUpdateProperties(exampleField));
      
    }
    
    {
      field = exampleFieldDb();
      exampleField = field.clone();

      field.setHibernateVersionNumber(99L);
      
      assertFalse(field.xmlDifferentBusinessProperties(exampleField));
      assertTrue(field.xmlDifferentUpdateProperties(exampleField));

      field.setHibernateVersionNumber(exampleField.getHibernateVersionNumber());
      field.xmlSaveUpdateProperties();
      
      field = exampleRetrieveFieldDb();
      
      assertFalse(field.xmlDifferentBusinessProperties(exampleField));
      assertFalse(field.xmlDifferentUpdateProperties(exampleField));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      field = exampleFieldDb();
      exampleField = field.clone();

      field.setName("abc");
      
      assertTrue(field.xmlDifferentBusinessProperties(exampleField));
      assertFalse(field.xmlDifferentUpdateProperties(exampleField));

      field.setName(exampleField.getName());
      field.xmlSaveBusinessProperties(exampleField.clone());
      field.xmlSaveUpdateProperties();
      
      field = exampleRetrieveFieldDb();
      
      assertFalse(field.xmlDifferentBusinessProperties(exampleField));
      assertFalse(field.xmlDifferentUpdateProperties(exampleField));
    
    }

    {
      field = exampleFieldDb();
      exampleField = field.clone();

      field.setName("abc");
      
      assertTrue(field.xmlDifferentBusinessProperties(exampleField));
      assertFalse(field.xmlDifferentUpdateProperties(exampleField));

      field.setName(exampleField.getName());
      field.xmlSaveBusinessProperties(exampleField.clone());
      field.xmlSaveUpdateProperties();
      
      field = exampleRetrieveFieldDb();
      
      assertFalse(field.xmlDifferentBusinessProperties(exampleField));
      assertFalse(field.xmlDifferentUpdateProperties(exampleField));
    
    }
    
    {
      field = exampleFieldDb();
      exampleField = field.clone();

      field.setReadPrivilege(AccessPrivilege.OPTIN);
      
      assertTrue(field.xmlDifferentBusinessProperties(exampleField));
      assertFalse(field.xmlDifferentUpdateProperties(exampleField));

      field.setReadPrivilege(exampleField.getReadPrivilege());
      field.xmlSaveBusinessProperties(exampleField.clone());
      field.xmlSaveUpdateProperties();
      
      field = exampleRetrieveFieldDb();
      
      assertFalse(field.xmlDifferentBusinessProperties(exampleField));
      assertFalse(field.xmlDifferentUpdateProperties(exampleField));
    
    }

    {
      field = exampleFieldDb();
      exampleField = field.clone();

      field.setType(FieldType.ACCESS);
      
      assertTrue(field.xmlDifferentBusinessProperties(exampleField));
      assertFalse(field.xmlDifferentUpdateProperties(exampleField));

      field.setType(exampleField.getType());
      field.xmlSaveBusinessProperties(exampleField.clone());
      field.xmlSaveUpdateProperties();
      
      field = exampleRetrieveFieldDb();
      
      assertFalse(field.xmlDifferentBusinessProperties(exampleField));
      assertFalse(field.xmlDifferentUpdateProperties(exampleField));
    
    }

    {
      field = exampleFieldDb();
      exampleField = field.clone();

      field.setUuid("abc");
      
      assertTrue(field.xmlDifferentBusinessProperties(exampleField));
      assertFalse(field.xmlDifferentUpdateProperties(exampleField));

      field.setUuid(exampleField.getUuid());
      field.xmlSaveBusinessProperties(exampleField.clone());
      field.xmlSaveUpdateProperties();
      
      field = exampleRetrieveFieldDb();
      
      assertFalse(field.xmlDifferentBusinessProperties(exampleField));
      assertFalse(field.xmlDifferentUpdateProperties(exampleField));
    
    }
    {
      field = exampleFieldDb();
      exampleField = field.clone();

      field.setWritePrivilege(AccessPrivilege.OPTOUT);
      
      assertTrue(field.xmlDifferentBusinessProperties(exampleField));
      assertFalse(field.xmlDifferentUpdateProperties(exampleField));

      field.setWritePrivilege(exampleField.getWritePrivilege());
      field.xmlSaveBusinessProperties(exampleField.clone());
      field.xmlSaveUpdateProperties();
      
      field = exampleRetrieveFieldDb();
      
      assertFalse(field.xmlDifferentBusinessProperties(exampleField));
      assertFalse(field.xmlDifferentUpdateProperties(exampleField));
    
    }
  }


  
}

