package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.attr.AttributeDefNameSetView;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetViewDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;

/**
 * Data Access Object for attribute def name set view
 * @author  mchyzer
 * @version $Id: Hib3AttributeDefNameSetViewDAO.java,v 1.2 2009-10-26 02:26:07 mchyzer Exp $
 */
public class Hib3AttributeDefNameSetViewDAO extends Hib3DAO implements AttributeDefNameSetViewDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeDefNameSetViewDAO.class.getName();

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetViewDAO#findByAttributeDefNameSetViews(java.util.Set)
   */
  public Set<AttributeDefNameSetView> findByAttributeDefNameSetViews(Set<String> attributeDefNames) {

    Criterion ifHasCriteria = Restrictions.in(AttributeDefNameSetView.FIELD_IF_HAS_ATTR_DEF_NAME_NAME, attributeDefNames);
    Criterion thenHasCriteria = Restrictions.in(AttributeDefNameSetView.FIELD_THEN_HAS_ATTR_DEF_NAME_NAME, attributeDefNames);
    
    QueryOptions queryOptions = new QueryOptions();
    //ifHas.name, thenHas.name, gadns.depth, gadnParentIfHas.name, gadnParentThenHas.name
    QuerySort querySort = QuerySort.asc(AttributeDefNameSetView.FIELD_PARENT_THEN_HAS_NAME);
    querySort.insertSortToBeginning(AttributeDefNameSetView.FIELD_PARENT_IF_HAS_NAME, true);
    querySort.insertSortToBeginning(AttributeDefNameSetView.FIELD_DEPTH, true);
    querySort.insertSortToBeginning(AttributeDefNameSetView.FIELD_THEN_HAS_ATTR_DEF_NAME_NAME, true);
    querySort.insertSortToBeginning(AttributeDefNameSetView.FIELD_IF_HAS_ATTR_DEF_NAME_NAME, true);
    
    queryOptions.sort(querySort);
    
    return HibernateSession.byCriteriaStatic().options(queryOptions).listSet(AttributeDefNameSetView.class, 
        HibUtils.listCritOr(ifHasCriteria, thenHasCriteria));
  }

} 

