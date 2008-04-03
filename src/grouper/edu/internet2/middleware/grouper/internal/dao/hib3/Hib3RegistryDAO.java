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

import java.io.IOException;
import java.io.StringWriter;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import org.apache.commons.dbcp.BasicDataSource;

import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.SqlBuilder;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;

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
 * @version $Id: Hib3RegistryDAO.java,v 1.3 2008-04-03 18:09:43 shilen Exp $
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
    dropForeignKeys();

    try {
      new SchemaExport( Hib3DAO.getConfiguration() )
        .setDelimiter(";")
        .setOutputFile( GrouperConfig.getBuildProperty("schemaexport.out") )
        .create(PRINT_DDL_TO_CONSOLE, EXPORT_DDL_TO_DB);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }

    addForeignKeys();
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

  public void addForeignKeys()
    throws GrouperDAOException {

    try {
      Hib3DaoConfig config = new Hib3DaoConfig();
      BasicDataSource ds = new BasicDataSource();
      ds.setUrl(config.getProperty("hibernate.connection.url"));
      ds.setDriverClassName(config.getProperty("hibernate.connection.driver_class"));
      ds.setUsername(config.getProperty("hibernate.connection.username"));
      ds.setPassword(config.getProperty("hibernate.connection.password"));

      Platform platform = PlatformFactory.createNewPlatformInstance(ds);

      if (Hib3DAO.class.getResource("Hib3ForeignKeys.xml") == null) {
        throw new RuntimeException("Cannot find resource Hib3ForeignKeys.xml.");
      }
      Database model = new DatabaseIO().read(Hib3DAO.class.getResource("Hib3ForeignKeys.xml").toString());

      StringWriter writer = new StringWriter();
      SqlBuilder builder = platform.getSqlBuilder();
      builder.setWriter(writer);
      builder.createExternalForeignKeys(model);
      platform.evaluateBatch(writer.toString(), false);
    }
    catch (IOException e) {
      throw new GrouperDAOException( e.getMessage(), e );
    }
  }

  public void dropForeignKeys()
    throws GrouperDAOException {

    try {
      Hib3DaoConfig config = new Hib3DaoConfig();
      BasicDataSource ds = new BasicDataSource();
      ds.setUrl(config.getProperty("hibernate.connection.url"));
      ds.setDriverClassName(config.getProperty("hibernate.connection.driver_class"));
      ds.setUsername(config.getProperty("hibernate.connection.username"));
      ds.setPassword(config.getProperty("hibernate.connection.password"));

      Platform platform = PlatformFactory.createNewPlatformInstance(ds);

      if (Hib3DAO.class.getResource("Hib3ForeignKeys.xml") == null) {
        throw new RuntimeException("Cannot find resource Hib3ForeignKeys.xml.");
      }
      Database model = new DatabaseIO().read(Hib3DAO.class.getResource("Hib3ForeignKeys.xml").toString());
      Table[] tables = model.getTables();

      StringWriter writer = new StringWriter();
      SqlBuilder builder = platform.getSqlBuilder();
      builder.setWriter(writer);

      for (int i = 0; i < tables.length; i++) {
        builder.dropExternalForeignKeys(tables[i]);
      }

      int ret = platform.evaluateBatch(writer.toString(), true);
    }
    catch (IOException e) {
      throw new GrouperDAOException( e.getMessage(), e );
    }
  }
} 

