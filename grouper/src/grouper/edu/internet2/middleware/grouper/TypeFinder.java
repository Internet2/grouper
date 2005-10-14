package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import net.sf.hibernate.*;
import net.sf.hibernate.type.Type;

/** Automatically generated Finder class for TypeFinder.
 * @author Hibernate FinderGenerator  **/
public class TypeFinder implements Serializable {

    public static List findByName(java.lang.String name) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Type as type where type.name=?", name, Hibernate.STRING);
        return finds;
    }

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from Type in class edu.internet2.middleware.grouper.Type");
        return finds;
    }

}
