package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import net.sf.hibernate.*;
import net.sf.hibernate.type.Type;

/** Automatically generated Finder class for FieldFinder.
 * @author Hibernate FinderGenerator  **/
public class FieldFinder implements Serializable {

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from Field in class edu.internet2.middleware.grouper.Field");
        return finds;
    }

}
