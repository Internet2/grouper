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
import  edu.internet2.middleware.grouper.internal.dao.hibernate.HibernateFieldDAO;
import  edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;
import  java.util.Iterator;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: Test_Integration_HibernateGroupTypeDAO_delete.java,v 1.5 2007-04-18 14:03:11 blair Exp $
 * @since   1.2.0
 */
public class Test_Integration_HibernateGroupTypeDAO_delete extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Test_Integration_HibernateGroupTypeDAO_delete.class);


  // TESTS //  

  public void testDelete_FieldsDeletedWhenGroupTypeIsDeleted() {
    try {
      LOG.info("testDelete_FieldsDeletedWhenGroupTypeIsDeleted");
      R               r     = new R();
      GrouperSession  s     = r.getSession();
      GroupType       type  = GroupType.createType(s, "custom type");
      type.addAttribute(s, "custom attribute", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false);
      type.addList(s, "custom list", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN);

      GroupTypeDTO _gt = (GroupTypeDTO) type.getDTO();
      assertEquals(
        "grouptype has fields before deletion",
        2, new HibernateFieldDAO().findAllFieldsByGroupType( _gt.getUuid() ).size()
      );
      type.delete(s); // fields show be automatically deleted when the parent type is deleted
      assertEquals(
        "grouptype does not have fields after deletion",
        0, new HibernateFieldDAO().findAllFieldsByGroupType( _gt.getUuid() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testDelete_FieldsDeletedWhenGroupTypeIsDeleted()

  public void testDelete_FieldsDeletedWhenRegistryIsReset() {
    try {
      LOG.info("testDelete_FieldsDeletedWhenRegistryIsReset");
      R               r     = new R();
      GrouperSession  s     = r.getSession();
      GroupType       type  = GroupType.createType(s, "custom type");
      type.addAttribute(s, "custom attribute", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false);
      type.addList(s, "custom list", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN);

      GroupTypeDTO _gt = (GroupTypeDTO) type.getDTO();
      assertEquals(
        "grouptype has fields before reset",
        2, new HibernateFieldDAO().findAllFieldsByGroupType( _gt.getUuid() ).size()
      );
      RegistryReset.reset();  // fields should be deleted when registry is reset
      assertEquals(
        "grouptype does not have fields after reset",
        0, new HibernateFieldDAO().findAllFieldsByGroupType( _gt.getUuid() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testDelete_FieldsDeletedWhenRegistryIsReset()

} // public class Test_Integration_HibernateGroupTypeDAO_delete extends GrouperTest

