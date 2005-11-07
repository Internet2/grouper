package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import net.sf.hibernate.*;
import net.sf.hibernate.type.Type;

/** Automatically generated Finder class for GroupFinder.
 * @author Hibernate FinderGenerator  **/
public class GroupFinder implements Serializable {

    public static List findByGroupId(java.lang.String group_id) throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Group as group where group.group_id=?", group_id, Hibernate.STRING);
        return finds;
    }

    public static List findByCreator(edu.internet2.middleware.grouper.Member creator_id) throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Group as group where group.creator_id=?", creator_id, Hibernate.OBJECT);
        return finds;
    }

    public static List findByModifier(edu.internet2.middleware.grouper.Member modifier_id) throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Group as group where group.modifier_id=?", modifier_id, Hibernate.OBJECT);
        return finds;
    }

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from Group in class edu.internet2.middleware.grouper.Group");
        return finds;
    }

}
