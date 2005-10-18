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

/** Automatically generated Finder class for MembershipFinder.
 * @author Hibernate FinderGenerator  **/
class MembershipFinder implements Serializable {

    public static List findByCnt(int count) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Membership as membership where membership.count=?", new Integer( count ), Hibernate.INTEGER);
        return finds;
    }

    public static List findByGroupId(edu.internet2.middleware.grouper.Group group_id) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Membership as membership where membership.group_id=?", group_id, Hibernate.OBJECT);
        return finds;
    }

    public static List findByMemberId(edu.internet2.middleware.grouper.Member member_id) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Membership as membership where membership.member_id=?", member_id, Hibernate.OBJECT);
        return finds;
    }

    public static List findByListId(edu.internet2.middleware.grouper.Field list_id) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Membership as membership where membership.list_id=?", list_id, Hibernate.OBJECT);
        return finds;
    }

    public static List findByViaId(edu.internet2.middleware.grouper.Group via_id) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Membership as membership where membership.via_id=?", via_id, Hibernate.OBJECT);
        return finds;
    }

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from Membership in class edu.internet2.middleware.grouper.Membership");
        return finds;
    }

}
