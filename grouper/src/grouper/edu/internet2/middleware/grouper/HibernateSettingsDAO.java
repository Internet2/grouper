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
import  net.sf.hibernate.*;

/** 
 * Stub Hibernate {@link Settings} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateSettingsDAO.java,v 1.1 2007-02-08 16:25:25 blair Exp $
 * @since   1.2.0
 */
class HibernateSettingsDAO extends HibernateDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final int  CURRENT_SCHEMA_VERSION  = 2;  //  == 1.2
                                                  //  1       == 1.0
                                                  //  0       == 0.9


  // PRIVATE INSTANCE VARIABLES //
  private String  id;
  private int     schemaVersion;


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static String create(SettingsDTO settings)
    throws  GrouperDAOException
  {
    try {
      Session               hs  = HibernateDAO.getSession();
      Transaction           tx  = hs.beginTransaction();
      HibernateSettingsDAO  dao = (HibernateSettingsDAO) Rosetta.getDAO(settings);
      try {
        hs.save(dao);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      }
      return dao.getId();
    }
    catch (HibernateException eH) {
      String msg = "unable to initialize settings: " + eH.getMessage();
      ErrorLog.fatal(HibernateSettingsDAO.class, msg);
      throw new GrouperDAOException(msg, eH);
    }
  } // protected static String create(settings)

  // @since   1.2.0
  protected static SettingsDTO findSettings()
    throws  GrouperDAOException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateSettingsDAO");
      HibernateSettingsDAO dao = (HibernateSettingsDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        throw new GrouperDAOException("null settings"); // TODO 20070207 this is not right
      }
      return (SettingsDTO) Rosetta.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static SettingsDTO findSettings()


  // GETTERS //

  protected String getId() {
    return this.id;
  }
  protected int getSchemaVersion() {
    return this.schemaVersion;
  }


  // SETTERS //

  protected void setId(String id) {
    this.id = id;
  }
  protected void setSchemaVersion(int version) {
    this.schemaVersion = version;
  }

} // class HibernateSettingsDAO extends HibernateDAO

