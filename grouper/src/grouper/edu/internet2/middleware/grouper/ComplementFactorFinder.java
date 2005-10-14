package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import net.sf.hibernate.*;
import net.sf.hibernate.type.Type;

/** Automatically generated Finder class for ComplementFactorFinder.
 * @author Hibernate FinderGenerator  **/
public class ComplementFactorFinder implements Serializable {

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateUtil.getSession();
        List finds = session.find("from ComplementFactor in class edu.internet2.middleware.grouper.ComplementFactor");
        return finds;
    }

}
