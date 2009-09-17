package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.RoleSetViewDAO;
import edu.internet2.middleware.grouper.permissions.RoleSetView;

/**
 * Data Access Object for role set view
 * @author  mchyzer
 * @version $Id: Hib3RoleSetViewDAO.java,v 1.1 2009-09-17 04:19:15 mchyzer Exp $
 */
public class Hib3RoleSetViewDAO extends Hib3DAO implements RoleSetViewDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3RoleSetViewDAO.class.getName();

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.RoleSetViewDAO#findByRoleSetViews(java.util.Set)
   */
  public Set<RoleSetView> findByRoleSetViews(Set<String> roleNames) {

    Criterion ifHasCriteria = Restrictions.in(RoleSetView.FIELD_IF_HAS_ROLE_NAME, roleNames);
    Criterion thenHasCriteria = Restrictions.in(RoleSetView.FIELD_THEN_HAS_ROLE_NAME, roleNames);
    
    return HibernateSession.byCriteriaStatic().listSet(RoleSetView.class, 
        HibUtils.listCritOr(ifHasCriteria, thenHasCriteria));
  }

} 

