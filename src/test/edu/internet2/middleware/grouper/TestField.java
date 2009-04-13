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
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Test {@link Field}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestField.java,v 1.13 2009-04-13 20:24:29 mchyzer Exp $
 */
public class TestField extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestField("testFields"));
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
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testFields() {
    Set       fields  = FieldFinder.findAll();
    Assert.assertEquals("fields: 9", 9, fields.size());
    Iterator  iter    = fields.iterator();
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "admins"              , FieldType.ACCESS,
      AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "creators"            , FieldType.NAMING,
      NamingPrivilege.STEM  , NamingPrivilege.STEM
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "members"             , FieldType.LIST,
      AccessPrivilege.READ  , AccessPrivilege.UPDATE
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "optins"              , FieldType.ACCESS,
      AccessPrivilege.UPDATE, AccessPrivilege.UPDATE
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "optouts"             , FieldType.ACCESS,
      AccessPrivilege.UPDATE, AccessPrivilege.UPDATE
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "readers"             , FieldType.ACCESS,
      AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "stemmers"            , FieldType.NAMING,
      NamingPrivilege.STEM  , NamingPrivilege.STEM
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "updaters"            , FieldType.ACCESS,
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
      
      Field field = FieldFinder.find("updaters");
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

}

