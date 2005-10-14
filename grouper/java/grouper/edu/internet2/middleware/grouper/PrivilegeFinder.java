package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import net.sf.hibernate.*;
import net.sf.hibernate.type.Type;

/** Automatically generated Finder class for PrivilegeFinder.
 * @author Hibernate FinderGenerator  **/
public class PrivilegeFinder implements Serializable {

    public static List findByName(java.lang.String name) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Privilege as privilege where privilege.name=?", name, Hibernate.STRING);
        return finds;
    }

    public static List findByIsAccess(boolean is_access) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Privilege as privilege where privilege.is_access=?", new Boolean( is_access ), Hibernate.BOOLEAN);
        return finds;
    }

    public static List findByIsNaming(boolean is_naming) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Privilege as privilege where privilege.is_naming=?", new Boolean( is_naming ), Hibernate.BOOLEAN);
        return finds;
    }

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from Privilege in class edu.internet2.middleware.grouper.Privilege");
        return finds;
    }

}
