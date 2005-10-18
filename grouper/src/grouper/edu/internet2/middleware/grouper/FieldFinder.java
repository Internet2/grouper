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

/** Automatically generated Finder class for FieldFinder.
 * @author Hibernate FinderGenerator  **/
class FieldFinder implements Serializable {

    public static List findByName(java.lang.String name) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Field as field where field.name=?", name, Hibernate.STRING);
        return finds;
    }

    public static List findByIsList(boolean is_list) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Field as field where field.is_list=?", new Boolean( is_list ), Hibernate.BOOLEAN);
        return finds;
    }

    public static List findByTypeId(edu.internet2.middleware.grouper.Type type_id) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Field as field where field.type_id=?", type_id, Hibernate.OBJECT);
        return finds;
    }

    public static List findByReadPrivilege(edu.internet2.middleware.grouper.Privilege read_privilege_id) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Field as field where field.read_privilege_id=?", read_privilege_id, Hibernate.OBJECT);
        return finds;
    }

    public static List findByWritePrivilege(edu.internet2.middleware.grouper.Privilege write_privilege_id) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Field as field where field.write_privilege_id=?", write_privilege_id, Hibernate.OBJECT);
        return finds;
    }

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from Field in class edu.internet2.middleware.grouper.Field");
        return finds;
    }

}
