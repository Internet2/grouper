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
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: Test_Integration_HibernateGroupDAO_delete.java,v 1.2 2007-03-06 20:19:00 blair Exp $
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

      assertTrue( 
        "group has attributes in registry before deletion", 
        HibernateGroupDAO.findAllAttributesByGroup(uuid).size() > 0 
      );
      g.delete(); // attributes should be deleted automatically when group is deleted
      assertEquals(
        "group does not have attributes in registry after deletion",
        0, HibernateGroupDAO.findAllAttributesByGroup(uuid).size()
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

      assertTrue( 
        "group has attributes in registry before reset", 
        HibernateGroupDAO.findAllAttributesByGroup(uuid).size() > 0 
      );
      RegistryReset.reset();  // attributes should be deleted when 
                              // registry is reset
      assertEquals(
        "group does not have attributes in registry after reset",
        0, HibernateGroupDAO.findAllAttributesByGroup(uuid).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testDelete_AttributesDeletedWhenRegistryIsReset()

} // public class Test_Integration_HibernateGroupDAO_delete extends GrouperTest

