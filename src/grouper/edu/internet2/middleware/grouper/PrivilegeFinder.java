/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import net.sf.hibernate.*;
import net.sf.hibernate.type.Type;

/** Automatically generated Finder class for PrivilegeFinder.
 * @author Hibernate FinderGenerator  **/
class PrivilegeFinder implements Serializable {

    public static List findByName(java.lang.String name) throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Privilege as privilege where privilege.name=?", name, Hibernate.STRING);
        return finds;
    }

    public static List findByIsAccess(boolean is_access) throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Privilege as privilege where privilege.is_access=?", new Boolean( is_access ), Hibernate.BOOLEAN);
        return finds;
    }

    public static List findByIsNaming(boolean is_naming) throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Privilege as privilege where privilege.is_naming=?", new Boolean( is_naming ), Hibernate.BOOLEAN);
        return finds;
    }

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from Privilege in class edu.internet2.middleware.grouper.Privilege");
        return finds;
    }

}
