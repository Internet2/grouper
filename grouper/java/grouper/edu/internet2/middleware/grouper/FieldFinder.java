package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import net.sf.hibernate.*;
import net.sf.hibernate.type.Type;

/** Automatically generated Finder class for FieldFinder.
 * @author Hibernate FinderGenerator  **/
public class FieldFinder implements Serializable {

    public static List findByTypeId(java.lang.String type_id) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Field as field where field.type_id=?", type_id, Hibernate.STRING);
        return finds;
    }

    public static List findByName(java.lang.String name) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Field as field where field.name=?", name, Hibernate.STRING);
        return finds;
    }

    public static List findByReadPrivilege(java.lang.String read_privilege) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Field as field where field.read_privilege=?", read_privilege, Hibernate.STRING);
        return finds;
    }

    public static List findByWritePrivilege(java.lang.String write_privilege) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Field as field where field.write_privilege=?", write_privilege, Hibernate.STRING);
        return finds;
    }

    public static List findByIsList(boolean is_list) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Field as field where field.is_list=?", new Boolean( is_list ), Hibernate.BOOLEAN);
        return finds;
    }

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from Field in class edu.internet2.middleware.grouper.Field");
        return finds;
    }

}
