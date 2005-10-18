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

/** Automatically generated Finder class for AttributeFinder.
 * @author Hibernate FinderGenerator  **/
class AttributeFinder implements Serializable {

    public static List findByValue(java.lang.String value) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Attribute as attribute where attribute.value=?", value, Hibernate.STRING);
        return finds;
    }

    public static List findByGroupId(edu.internet2.middleware.grouper.Group group_id) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Attribute as attribute where attribute.group_id=?", group_id, Hibernate.OBJECT);
        return finds;
    }

    public static List findByFieldId(edu.internet2.middleware.grouper.Field field_id) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Attribute as attribute where attribute.field_id=?", field_id, Hibernate.OBJECT);
        return finds;
    }

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from Attribute in class edu.internet2.middleware.grouper.Attribute");
        return finds;
    }

}
