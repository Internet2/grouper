package edu.internet2.middleware.grouper;

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import net.sf.hibernate.*;
import net.sf.hibernate.type.Type;

/** Automatically generated Finder class for UnionFactorFinder.
 * @author Hibernate FinderGenerator  **/
public class UnionFactorFinder implements Serializable {

    public static List findAll() throws SQLException, HibernateException {
        Session session = HibernateHelper.getSession();
        List finds = session.find("from UnionFactor in class edu.internet2.middleware.grouper.UnionFactor");
        return finds;
    }

}
