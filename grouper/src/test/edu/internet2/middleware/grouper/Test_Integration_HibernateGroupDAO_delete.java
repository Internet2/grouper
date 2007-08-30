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
import  edu.internet2.middleware.grouper.cfg.ApiConfig;
import  edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import  edu.internet2.middleware.grouper.internal.dao.hibernate.HibernateGroupTypeTupleDAO;
import  edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import  edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: Test_Integration_HibernateGroupDAO_delete.java,v 1.8 2007-08-30 15:52:22 blair Exp $
 * @since   1.2.0
 */
public class Test_Integration_HibernateGroupDAO_delete extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Test_Integration_HibernateGroupDAO_delete.class);


  // TESTS //  

  public void testDelete_AttributesDeletedWhenGroupIsDeleted() {
    try {
      LOG.info("testDelete_AttributesDeletedWhenGroupIsDeleted");
      R       r     = R.getContext("grouper");
      Group   g     = r.getGroup("i2mi:grouper", "grouper-dev");
      String  uuid  = g.getUuid();

      GroupDAO dao = GrouperDAOFactory.getFactory().getGroup();
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

      GroupDAO dao = GrouperDAOFactory.getFactory().getGroup();
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
    // TODO 20070828 this check is ugly AND don't make this test dao specific
    if ( new ApiConfig().getProperty("dao.factory").equals("edu.internet2.middleware.grouper.internal.dao.hibernate.HibernateDAOFactory") ) {
      R         r     = R.getContext("grouper");
      Group     g     = r.getGroup("i2mi:grouper", "grouper-dev");
      GroupType type  = GroupTypeFinder.find("base");

      GroupDTO      _g  = (GroupDTO) g.getDTO();
      GroupTypeDTO  _gt = (GroupTypeDTO) type.getDTO();
      HibernateGroupTypeTupleDAO.findByGroupAndType(_g, _gt);
      assertTrue("group has type tuple in registry before deletion", true);
      g.delete(); // type tuples should be deleted automatically when group is deleted
      try {
        HibernateGroupTypeTupleDAO.findByGroupAndType(_g, _gt);
        fail("type tuple still exists after group deletion");
      }
      catch (GrouperDAOException eExpected) {
        assertTrue("group no longer has type tuple after group deletion", true);
      }
    }
  } 

  public void testDelete_GroupTypeTuplesDeletedWhenRegistryIsReset() 
    throws  Exception,
            SchemaException
  {
    LOG.info("testDelete_GroupTypeTuplesDeletedWhenRegistryIsReset");
    // TODO 20070828 this check is ugly AND don't make this test dao specific
    if ( new ApiConfig().getProperty("dao.factory").equals("edu.internet2.middleware.grouper.internal.dao.hibernate.HibernateDAOFactory") ) {
      R         r     = R.getContext("grouper");
      Group     g     = r.getGroup("i2mi:grouper", "grouper-dev");
      GroupType type  = GroupTypeFinder.find("base");

      GroupDTO      _g  = (GroupDTO) g.getDTO();
      GroupTypeDTO  _gt = (GroupTypeDTO) type.getDTO();
      HibernateGroupTypeTupleDAO.findByGroupAndType(_g, _gt);
      assertTrue("group has type tuple in registry before reset", true);
      RegistryReset.reset();  // tuples should be deleted when registry is reset
      try {
        HibernateGroupTypeTupleDAO.findByGroupAndType(_g, _gt);
        fail("type tuple still exists after reset");
      }
      catch (GrouperDAOException eExpected) {
        assertTrue("group no longer has type tuple after reset", true);
      }
    }
  } 

} 

