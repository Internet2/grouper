package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import net.sf.hibernate.*;
import net.sf.hibernate.type.Type;

/** Automatically generated Finder class for AttributeFinder.
 * @author Hibernate FinderGenerator  **/
public class AttributeFinder implements Serializable {

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
