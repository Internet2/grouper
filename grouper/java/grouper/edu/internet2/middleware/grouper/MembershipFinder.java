package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import net.sf.hibernate.*;
import net.sf.hibernate.type.Type;

/** Automatically generated Finder class for MembershipFinder.
 * @author Hibernate FinderGenerator  **/
public class MembershipFinder implements Serializable {

    public static List findByContainerId(java.lang.String container_id) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Membership as membership where membership.container_id=?", container_id, Hibernate.STRING);
        return finds;
    }

    public static List findByMemberId(java.lang.String member_id) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Membership as membership where membership.member_id=?", member_id, Hibernate.STRING);
        return finds;
    }

    public static List findByListId(java.lang.String list_id) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Membership as membership where membership.list_id=?", list_id, Hibernate.STRING);
        return finds;
    }

    public static List findByViaId(java.lang.String via_id) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Membership as membership where membership.via_id=?", via_id, Hibernate.STRING);
        return finds;
    }

    public static List findByCnt(int count) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Membership as membership where membership.count=?", new Integer( count ), Hibernate.INTEGER);
        return finds;
    }

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from Membership in class edu.internet2.middleware.grouper.Membership");
        return finds;
    }

}
