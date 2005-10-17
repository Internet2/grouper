package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import net.sf.hibernate.*;
import net.sf.hibernate.type.Type;

/** Automatically generated Finder class for MembershipFinder.
 * @author Hibernate FinderGenerator  **/
public class MembershipFinder implements Serializable {

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
