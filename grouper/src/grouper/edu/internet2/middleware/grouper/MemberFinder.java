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

/** Automatically generated Finder class for MemberFinder.
 * @author Hibernate FinderGenerator  **/
public class MemberFinder implements Serializable {

    public static List findBySubjectId(java.lang.String subject_id) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Member as member where member.subject_id=?", subject_id, Hibernate.STRING);
        return finds;
    }

    public static List findBySubjectSource(java.lang.String subject_source) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Member as member where member.subject_source=?", subject_source, Hibernate.STRING);
        return finds;
    }

    public static List findBySubjectType(java.lang.String subject_type) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Member as member where member.subject_type=?", subject_type, Hibernate.STRING);
        return finds;
    }

    public static List findByUuid(java.lang.String uuid) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Member as member where member.uuid=?", uuid, Hibernate.STRING);
        return finds;
    }

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from Member in class edu.internet2.middleware.grouper.Member");
        return finds;
    }

}
