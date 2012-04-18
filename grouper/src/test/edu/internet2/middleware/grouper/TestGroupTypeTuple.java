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
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Test {@link Stem}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStem.java,v 1.34 2009-12-07 07:31:09 mchyzer Exp $
 */
public class TestGroupTypeTuple extends GrouperTest {

  /** */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(TestGroupTypeTuple.class);

  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
    TestRunner.run(new TestGroupTypeTuple("testXmlDifferentUpdateProperties"));
    //TestRunner.run(TestGroupTypeTuple.class);
  }

  /**
   * 
   * @param name
   */
  public TestGroupTypeTuple(String name) {
    super(name);
  }
  
  /**
   * make an example group type tuple for testing
   * @return an example group type tuple
   */
  public static GroupTypeTuple exampleGroupTypeTuple() {
    GroupTypeTuple groupTypeTuple = new GroupTypeTuple();
    groupTypeTuple.setContextId("contextId");
    groupTypeTuple.setGroupUuid("groupId");
    groupTypeTuple.setHibernateVersionNumber(3L);
    groupTypeTuple.setTypeUuid("typeId");
    groupTypeTuple.setId("uuid");
    return groupTypeTuple;
  }
  
  /**
   * make an example group type tuple for testing
   * @return an example group type tuple
   */
  public static GroupTypeTuple exampleGroupTypeTupleDb() {
    return exampleGroupTypeTupleDb("groupType");
  }
  
  /**
   * make an example group type tuple for testing
   * @param prefix
   * @return an example group type tuple
   */
  public static GroupTypeTuple exampleGroupTypeTupleDb(String prefix) {
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:" + prefix + "TupleTest").assignName("test:" + prefix + "TupleTest")
      .assignCreateParentStemsIfNotExist(true).assignDescription("description").save();

    GroupType groupType = GroupType.createType(GrouperSession.staticGrouperSession(), prefix + "Test", false);
    
    group.addType(groupType, false);
    
    GroupTypeTuple groupTypeTuple = GrouperDAOFactory.getFactory().getGroupTypeTuple().findByUuidOrKey(null,
        group.getId(), groupType.getUuid(), true);
    
    return groupTypeTuple;
  }

  
  /**
   * retrieve example group type tuple for testing
   * @return an example group type tuple
   */
  public static GroupTypeTuple exampleRetrieveGroupTypeTupleDb() {
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), "test:groupTypeTupleTest", true);
    GroupType groupType = GroupTypeFinder.find("groupTypeTest", true);
    GroupTypeTuple groupTypeTuple = GrouperDAOFactory.getFactory().getGroupTypeTuple()
      .findByUuidOrKey(null, group.getId(), groupType.getUuid(), true);
    return groupTypeTuple;
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    GroupTypeTuple groupTypeTupleOriginal = exampleGroupTypeTupleDb("insert");
    GroupTypeTuple groupTypeTupleCopy = GrouperDAOFactory.getFactory().getGroupTypeTuple().findByUuidOrKey(groupTypeTupleOriginal.getId(), null, null, true);
    GroupTypeTuple groupTypeTupleCopy2 = GrouperDAOFactory.getFactory().getGroupTypeTuple().findByUuidOrKey(groupTypeTupleOriginal.getId(), null, null, true);
    groupTypeTupleCopy.delete();
    
    //lets insert the original
    groupTypeTupleCopy2.xmlSaveBusinessProperties(null);
    groupTypeTupleCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    groupTypeTupleCopy = GrouperDAOFactory.getFactory().getGroupTypeTuple().findByUuidOrKey(groupTypeTupleOriginal.getId(), null, null, true);
    
    assertFalse(groupTypeTupleCopy == groupTypeTupleOriginal);
    assertFalse(groupTypeTupleCopy.xmlDifferentBusinessProperties(groupTypeTupleOriginal));
    assertFalse(groupTypeTupleCopy.xmlDifferentUpdateProperties(groupTypeTupleOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    GroupTypeTuple groupTypeTuple = null;
    GroupTypeTuple exampleGroupTypeTuple = null;

    //lets do an insert
    
    
    //TEST UPDATE PROPERTIES
    {
      groupTypeTuple = exampleGroupTypeTupleDb();
      exampleGroupTypeTuple = exampleRetrieveGroupTypeTupleDb();
      
      groupTypeTuple.setContextId("abc");
      
      assertFalse(groupTypeTuple.xmlDifferentBusinessProperties(exampleGroupTypeTuple));
      assertTrue(groupTypeTuple.xmlDifferentUpdateProperties(exampleGroupTypeTuple));

      groupTypeTuple.setContextId(exampleGroupTypeTuple.getContextId());
      groupTypeTuple.xmlSaveUpdateProperties();

      groupTypeTuple = exampleRetrieveGroupTypeTupleDb();
      
      assertFalse(groupTypeTuple.xmlDifferentBusinessProperties(exampleGroupTypeTuple));
      assertFalse(groupTypeTuple.xmlDifferentUpdateProperties(exampleGroupTypeTuple));
      
    }

    {
      groupTypeTuple = exampleGroupTypeTupleDb();
      exampleGroupTypeTuple =  exampleRetrieveGroupTypeTupleDb();

      groupTypeTuple.setHibernateVersionNumber(99L);
      
      assertFalse(groupTypeTuple.xmlDifferentBusinessProperties(exampleGroupTypeTuple));
      assertTrue(groupTypeTuple.xmlDifferentUpdateProperties(exampleGroupTypeTuple));

      groupTypeTuple.setHibernateVersionNumber(exampleGroupTypeTuple.getHibernateVersionNumber());
      groupTypeTuple.xmlSaveUpdateProperties();
      
      groupTypeTuple = exampleRetrieveGroupTypeTupleDb();
      
      assertFalse(groupTypeTuple.xmlDifferentBusinessProperties(exampleGroupTypeTuple));
      assertFalse(groupTypeTuple.xmlDifferentUpdateProperties(exampleGroupTypeTuple));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      groupTypeTuple = exampleGroupTypeTupleDb();
      exampleGroupTypeTuple =  exampleRetrieveGroupTypeTupleDb();

      groupTypeTuple.setGroupUuid("abc");
      
      assertTrue(groupTypeTuple.xmlDifferentBusinessProperties(exampleGroupTypeTuple));
      assertFalse(groupTypeTuple.xmlDifferentUpdateProperties(exampleGroupTypeTuple));

      groupTypeTuple.setGroupUuid(exampleGroupTypeTuple.getGroupUuid());
      groupTypeTuple.xmlSaveBusinessProperties(exampleGroupTypeTuple.clone());
      groupTypeTuple.xmlSaveUpdateProperties();
      
      groupTypeTuple = exampleRetrieveGroupTypeTupleDb();
      
      assertFalse(groupTypeTuple.xmlDifferentBusinessProperties(exampleGroupTypeTuple));
      assertFalse(groupTypeTuple.xmlDifferentUpdateProperties(exampleGroupTypeTuple));
    
    }
    
    {
      groupTypeTuple = exampleGroupTypeTupleDb();
      exampleGroupTypeTuple =  exampleRetrieveGroupTypeTupleDb();

      groupTypeTuple.setTypeUuid("abc");
      
      assertTrue(groupTypeTuple.xmlDifferentBusinessProperties(exampleGroupTypeTuple));
      assertFalse(groupTypeTuple.xmlDifferentUpdateProperties(exampleGroupTypeTuple));

      groupTypeTuple.setTypeUuid(exampleGroupTypeTuple.getTypeUuid());
      groupTypeTuple.xmlSaveBusinessProperties(exampleGroupTypeTuple.clone());
      groupTypeTuple.xmlSaveUpdateProperties();
      
      groupTypeTuple = exampleRetrieveGroupTypeTupleDb();
      
      assertFalse(groupTypeTuple.xmlDifferentBusinessProperties(exampleGroupTypeTuple));
      assertFalse(groupTypeTuple.xmlDifferentUpdateProperties(exampleGroupTypeTuple));
    
    }
    
    {
      groupTypeTuple = exampleGroupTypeTupleDb();
      exampleGroupTypeTuple =  exampleRetrieveGroupTypeTupleDb();

      groupTypeTuple.setId("abc");
      
      assertTrue(groupTypeTuple.xmlDifferentBusinessProperties(exampleGroupTypeTuple));
      assertFalse(groupTypeTuple.xmlDifferentUpdateProperties(exampleGroupTypeTuple));

      groupTypeTuple.setId(exampleGroupTypeTuple.getId());
      groupTypeTuple.xmlSaveBusinessProperties(exampleGroupTypeTuple.clone());
      groupTypeTuple.xmlSaveUpdateProperties();
      
      groupTypeTuple = exampleRetrieveGroupTypeTupleDb();
      
      assertFalse(groupTypeTuple.xmlDifferentBusinessProperties(exampleGroupTypeTuple));
      assertFalse(groupTypeTuple.xmlDifferentUpdateProperties(exampleGroupTypeTuple));
    
    }
  }
  
}

