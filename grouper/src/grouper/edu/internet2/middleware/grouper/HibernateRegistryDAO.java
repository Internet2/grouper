/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  java.util.List;
import  net.sf.hibernate.*;

/**
 * Stub Hibernate {@link Registry} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateRegistryDAO.java,v 1.1 2006-12-20 18:41:56 blair Exp $
 * @since   1.2.0
 */
class HibernateRegistryDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateRegistryDAO.class.getName();


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void resetRegistry() 
    throws  GrouperException
  {
    try {
      Session     hs  = HibernateHelper.getSession();
      Transaction tx  = hs.beginTransaction();

      hs.delete("from Membership");
      hs.delete("from GrouperSession");

      hs.delete("from Composite");
      hs.delete("from Group");
      List l = hs.find("from Stem as ns where ns.stem_name like '" + Stem.ROOT_INT + "'");
      if (l.size() == 1) {
        Stem    root  = (Stem) l.get(0);
        String  uuid  = root.getUuid();
        root.setModifier_id(  null);
        root.setModify_source(null);
        root.setModify_time(  0   );
        hs.saveOrUpdate(root);
        hs.delete("from Owner as o where o.uuid != '" + uuid + "'");
      }
      else {
        hs.delete("from Owner");
      }

      hs.delete("from Member as m where m.subject_id != 'GrouperSystem'");
      hs.delete(
        "from GroupType as t where (  "
        + "     t.name != 'base'      "
        + "and  t.name != 'naming'    "
        + ")"
      );
      // TODO 20061018 Once properly mapped I can delete the explicit attr delete
      hs.delete("from HibernateSubjectAttribute");
      hs.delete("from HibernateSubject");

      tx.commit();
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperException( eH.getMessage(), eH );
    }
  } // protected static void resetRegistry()

} // class HibernateRegistryDAO

