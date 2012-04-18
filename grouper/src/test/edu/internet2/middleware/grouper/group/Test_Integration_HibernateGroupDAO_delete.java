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

package edu.internet2.middleware.grouper.group;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.internal.dao.AttributeDAO;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GroupTypeTupleDAO;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: Test_Integration_HibernateGroupDAO_delete.java,v 1.2 2009-03-24 17:12:08 mchyzer Exp $
 * @since   1.2.0
 */
public class Test_Integration_HibernateGroupDAO_delete extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = GrouperUtil.getLog(Test_Integration_HibernateGroupDAO_delete.class);
  /**
   */
  private static final String ATTRIBUTE1 = "attribute1";
  /**
   */
  private static final String GROUP_TYPE1 = "groupType1";


  // TESTS //  

  /**
   * 
   */
  public Test_Integration_HibernateGroupDAO_delete() {
    super();
  }

  /**
   * @param name
   */
  public Test_Integration_HibernateGroupDAO_delete(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testDelete_AttributesDeletedWhenGroupIsDeleted() {
    try {
      R       r     = R.getContext("grouper");
      Group   g     = r.getGroup("i2mi:grouper", "grouper-dev");
      String  uuid  = g.getUuid();
      GrouperSession grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );

      GroupType groupType = GroupType.createType(grouperSession, GROUP_TYPE1, false); 
      groupType.addAttribute(grouperSession,ATTRIBUTE1, 
            AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
      g.addType(groupType, false);
      g.setAttribute(ATTRIBUTE1, "whatever");

      AttributeDAO dao = GrouperDAOFactory.getFactory().getAttribute();
      assertTrue( 
        "group has attributes in registry before deletion", 
        dao.findAllAttributesByGroup(uuid).size() > 0 
      );
      g.delete(); // attributes should be deleted automatically when group is deleted
      assertEquals(
        "group does not have attributes in registry after deletion",
        0, dao.findAllAttributesByGroup(uuid).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testDelete_AttributesDeletedWhenGroupIsDeleted()

  public void testDelete_AttributesDeletedWhenRegistryIsReset() {
    try {
      LOG.info("testDelete_AttributesDeletedWhenRegistryIsReset");
      R       r     = R.getContext("grouper");
      Group   g     = r.getGroup("i2mi:grouper", "grouper-dev");
      String  uuid  = g.getUuid();

      GrouperSession grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );

      GroupType groupType = GroupType.createType(grouperSession, GROUP_TYPE1, false); 
      groupType.addAttribute(grouperSession,ATTRIBUTE1, 
            AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
      g.addType(groupType, false);
      g.setAttribute(ATTRIBUTE1, "whatever");

      AttributeDAO dao = GrouperDAOFactory.getFactory().getAttribute();
      assertTrue( 
        "group has attributes in registry before reset", 
        dao.findAllAttributesByGroup(uuid).size() > 0 
      );
      RegistryReset.reset();  // attributes should be deleted when 
                              // registry is reset
      assertEquals(
        "group does not have attributes in registry after reset",
        0, dao.findAllAttributesByGroup(uuid).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testDelete_AttributesDeletedWhenRegistryIsReset()

  // TODO 20070418  refactor test so that "HibernateGroupTypeTuple" and
  //                "findByGroupAndType" do not need to be public
  public void testDelete_GroupTypeTuplesDeletedWhenGroupIsDeleted() 
    throws  GroupDeleteException,
            Exception,
            SchemaException
  {
    LOG.info("testDelete_GroupTypeTuplesDeletedWhenGroupIsDeleted");
    R         r     = R.getContext("grouper");
    Group     g     = r.getGroup("i2mi:grouper", "grouper-dev");
    GroupType type  = GroupTypeFinder.find("base", true);

    Hib3GroupTypeTupleDAO.findByGroupAndType(g, type);
    assertTrue("group has type tuple in registry before deletion", true);
    g.delete(); // type tuples should be deleted automatically when group is deleted
    try {
      Hib3GroupTypeTupleDAO.findByGroupAndType(g, type);
      fail("type tuple still exists after group deletion");
    }
    catch (GrouperDAOException eExpected) {
      assertTrue("group no longer has type tuple after group deletion", true);
    }
  } 

  public void testDelete_GroupTypeTuplesDeletedWhenRegistryIsReset() 
    throws  Exception,
            SchemaException
  {
    LOG.info("testDelete_GroupTypeTuplesDeletedWhenRegistryIsReset");
    R         r     = R.getContext("grouper");
    Group     g     = r.getGroup("i2mi:grouper", "grouper-dev");
    GroupType type  = GroupTypeFinder.find("base", true);

    Hib3GroupTypeTupleDAO.findByGroupAndType(g, type);
    assertTrue("group has type tuple in registry before reset", true);
    RegistryReset.reset();  // tuples should be deleted when registry is reset
    try {
      Hib3GroupTypeTupleDAO.findByGroupAndType(g, type);
      fail("type tuple still exists after reset");
    }
    catch (GrouperDAOException eExpected) {
      assertTrue("group no longer has type tuple after reset", true);
    }
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new Test_Integration_HibernateGroupDAO_delete("testDelete_AttributesDeletedWhenGroupIsDeleted"));
    TestRunner.run(Test_Integration_HibernateGroupDAO_delete.class);
  } 

} 

