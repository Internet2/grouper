package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import net.sf.hibernate.*;
import net.sf.hibernate.type.Type;

/** Automatically generated Finder class for StemFinder.
 * @author Hibernate FinderGenerator  **/
public class StemFinder implements Serializable {

    public static List findByUuid(java.lang.String uuid) throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.Stem as stem where stem.uuid=?", uuid, Hibernate.STRING);
        return finds;
    }

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from Stem in class edu.internet2.middleware.grouper.Stem");
        return finds;
    }

}
