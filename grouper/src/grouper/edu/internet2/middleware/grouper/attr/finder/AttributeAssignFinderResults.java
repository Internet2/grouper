/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.finder;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 */
public class AttributeAssignFinderResults {

  /**
   * 
   */
  public AttributeAssignFinderResults() {
  }

  /** result objects */
  private Set<Object[]> resultObjects;

  
  /**
   * result objects
   * @return the resultObjects
   */
  public Set<Object[]> getResultObjects() {
    return this.resultObjects;
  }

  
  /**
   * result objects
   * @param resultObjects1 the resultObjects to set
   */
  public void setResultObjects(Set<Object[]> resultObjects1) {
    this.resultObjects = resultObjects1;
    
    this.attributeAssignFinderResults = new LinkedHashSet<AttributeAssignFinderResult>();
    
    for (Object[] result : GrouperUtil.nonNull(resultObjects1)) {
      Group group = (Group) result[0];
      AttributeAssign attributeAssign = (AttributeAssign) result[1];
      AttributeAssignFinderResult attributeAssignFinderResult = new AttributeAssignFinderResult();
      this.attributeAssignFinderResults.add(attributeAssignFinderResult);
      attributeAssignFinderResult.setGroup(group);
      attributeAssignFinderResult.setAttributeAssign(attributeAssign);
    }
    
  }
  
  /**
   * attribute assign results
   */
  private Set<AttributeAssignFinderResult> attributeAssignFinderResults = null;
  
  /**
   * attribute assign results
   * @return the attributeAssignResults
   */
  public Set<AttributeAssignFinderResult> getAttributeAssignFinderResults() {
    return this.attributeAssignFinderResults;
  }
  
  /**
   * attribute assign finder results
   * @param attributeAssignFinderResults1 the attributeAssignFinderResults to set
   */
  public void setAttributeAssignFinderResults(Set<AttributeAssignFinderResult> attributeAssignFinderResults1) {
    this.attributeAssignFinderResults = attributeAssignFinderResults1;
  }

}
