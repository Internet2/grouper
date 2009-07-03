package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.attr.AttributeDefNameSetView;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetViewDAO;

/**
 * Data Access Object for attribute def name set view
 * @author  mchyzer
 * @version $Id: Hib3AttributeDefNameSetViewDAO.java,v 1.1 2009-07-03 21:15:13 mchyzer Exp $
 */
public class Hib3AttributeDefNameSetViewDAO extends Hib3DAO implements AttributeDefNameSetViewDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeDefNameSetViewDAO.class.getName();

  /**
   * get all records which are related to these attribute def names
   * @see AttributeDefNameSetDAO#findByThenHasAttributeDefNameId(String)
   */
  public Set<AttributeDefNameSetView> findByAttributeDefNameSetViews(Set<String> attributeDefNames) {

    Criterion ifHasCriteria = Restrictions.in(AttributeDefNameSetView.FIELD_IF_HAS_ATTR_DEF_NAME_NAME, attributeDefNames);
    Criterion thenHasCriteria = Restrictions.in(AttributeDefNameSetView.FIELD_THEN_HAS_ATTR_DEF_NAME_NAME, attributeDefNames);
    
    return HibernateSession.byCriteriaStatic().listSet(AttributeDefNameSetView.class, 
        HibUtils.listCritOr(ifHasCriteria, thenHasCriteria));
  }

} 

