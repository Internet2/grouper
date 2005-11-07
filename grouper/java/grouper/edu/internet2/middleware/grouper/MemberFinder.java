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
        Session session = HibernateHelper.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Member as member where member.subject_id=?", subject_id, Hibernate.STRING);
        return finds;
    }

    public static List findBySubjectSource(java.lang.String subject_source) throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Member as member where member.subject_source=?", subject_source, Hibernate.STRING);
        return finds;
    }

    public static List findBySubjectType(java.lang.String subject_type) throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Member as member where member.subject_type=?", subject_type, Hibernate.STRING);
        return finds;
    }

    public static List findByMemberId(java.lang.String member_id) throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Member as member where member.member_id=?", member_id, Hibernate.STRING);
        return finds;
    }

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from Member in class edu.internet2.middleware.grouper.Member");
        return finds;
    }

}
