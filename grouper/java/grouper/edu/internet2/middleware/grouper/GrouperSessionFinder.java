package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import net.sf.hibernate.*;
import net.sf.hibernate.type.Type;

/** Automatically generated Finder class for GrouperSessionFinder.
 * @author Hibernate FinderGenerator  **/
public class GrouperSessionFinder implements Serializable {

    public static List findByStartTime(java.util.Date start_time) throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from edu.internet2.middleware.grouper.GrouperSession as groupersession where groupersession.start_time=?", start_time, Hibernate.OBJECT);
        return finds;
    }

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from GrouperSession in class edu.internet2.middleware.grouper.GrouperSession");
        return finds;
    }

}
