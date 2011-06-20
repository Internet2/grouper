package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSetView;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionSetViewDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;

/**
 * Data Access Object for attribute assign action set view
 * @author  mchyzer
 * @version $Id: Hib3AttributeAssignActionSetViewDAO.java,v 1.1 2009-10-26 02:26:07 mchyzer Exp $
 */
public class Hib3AttributeAssignActionSetViewDAO extends Hib3DAO implements AttributeAssignActionSetViewDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeAssignActionSetViewDAO.class.getName();

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionSetViewDAO#findByAttributeAssignActionSetViews(java.util.Set)
   */
  public Set<AttributeAssignActionSetView> findByAttributeAssignActionSetViews(Set<String> attributeAssignActions) {

    Criterion ifHasCriteria = Restrictions.in(AttributeAssignActionSetView.FIELD_IF_HAS_ATTR_ASSIGN_ACTION_NAME, attributeAssignActions);
    Criterion thenHasCriteria = Restrictions.in(AttributeAssignActionSetView.FIELD_THEN_HAS_ATTR_ASSIGN_ACTION_NAME, attributeAssignActions);
    
    QueryOptions queryOptions = new QueryOptions();
    //ifHas.name, thenHas.name, gadns.depth, gadnParentIfHas.name, gadnParentThenHas.name
    QuerySort querySort = QuerySort.asc(AttributeAssignActionSetView.FIELD_PARENT_THEN_HAS_NAME);
    querySort.insertSortToBeginning(AttributeAssignActionSetView.FIELD_PARENT_IF_HAS_NAME, true);
    querySort.insertSortToBeginning(AttributeAssignActionSetView.FIELD_DEPTH, true);
    querySort.insertSortToBeginning(AttributeAssignActionSetView.FIELD_THEN_HAS_ATTR_ASSIGN_ACTION_NAME, true);
    querySort.insertSortToBeginning(AttributeAssignActionSetView.FIELD_IF_HAS_ATTR_ASSIGN_ACTION_NAME, true);
    
    queryOptions.sort(querySort);

    return HibernateSession.byCriteriaStatic().options(queryOptions).listSet(AttributeAssignActionSetView.class, 
        HibUtils.listCritOr(ifHasCriteria, thenHasCriteria));
  }

} 

