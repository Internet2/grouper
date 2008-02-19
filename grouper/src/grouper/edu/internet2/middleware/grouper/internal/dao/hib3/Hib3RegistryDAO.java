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

package edu.internet2.middleware.grouper.internal.dao.hib3;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.RegistryDAO;

/**
 * Basic Hibernate <code>Registry</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3RegistryDAO.java,v 1.2 2008-02-19 07:50:47 mchyzer Exp $
 * @since   @HEAD@
 */
class Hib3RegistryDAO implements RegistryDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final boolean PRINT_DDL_TO_CONSOLE = false;
  private static final boolean EXPORT_DDL_TO_DB     = true;
  
  // PUBLIC INSTANCE METHODS //

  /**
   * @since   @HEAD@
   */
  public void initializeSchema() 
    throws  GrouperDAOException
  {
    try {
      new SchemaExport( Hib3DAO.getConfiguration() )
        .setDelimiter(";")
        .setOutputFile( GrouperConfig.getBuildProperty("schemaexport.out") )
        .create(PRINT_DDL_TO_CONSOLE, EXPORT_DDL_TO_DB);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  }

  /**
   * @since   @HEAD@
   */
  public void reset() 
    throws  GrouperDAOException {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session     hs  = hibernateSession.getSession();
            Hib3MembershipDAO.reset(hs);
            Hib3GrouperSessionDAO.reset(hs);
            Hib3CompositeDAO.reset(hs);
            Hib3GroupDAO.reset(hs);
            Hib3StemDAO.reset(hs);
            Hib3MemberDAO.reset(hs);
            Hib3GroupTypeDAO.reset(hs);
            Hib3RegistrySubjectDAO.reset(hs);
            return null;
          }
      
    });
    
  } 

} 

