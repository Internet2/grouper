package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import net.sf.hibernate.*;
import net.sf.hibernate.type.Type;

/** Automatically generated Finder class for GroupTypeFinder.
 * @author Hibernate FinderGenerator  **/
public class GroupTypeFinder implements Serializable {

    public static List findByName(java.lang.String name) throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.GroupType as grouptype where grouptype.name=?", name, Hibernate.STRING);
        return finds;
    }

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from GroupType in class edu.internet2.middleware.grouper.GroupType");
        return finds;
    }

}
