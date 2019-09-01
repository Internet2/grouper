/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.finder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
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
   * id to group mape
   */
  private Map<String, Group> idToGroupMap = new HashMap<String, Group>();

  /**
   * id to group map
   * @return id to group map
   */
  public Map<String, Group> getIdToGroupMap() {
    return this.idToGroupMap;
  }
  
  /**
   * id to attribute assign map
   */
  private Map<String, AttributeAssign> idToAttributeAssignMap = new HashMap<String, AttributeAssign>();

  /**
   * id to attribute assign map
   * @return id to attribute assign map
   */
  public Map<String, AttributeAssign> getIdToAttributeAssignMap() {
    return this.idToAttributeAssignMap;
  }
  
  /**
   * attribute assign id to attribute assign value map
   */
  private Map<String, Set<AttributeAssignValue>> attributeAssignIdToAttributeAssignValuesMap = new HashMap<String, Set<AttributeAssignValue>>();

  /**
   * attribute assign id to attribute assign value map
   * @return attribute assign id to attribute assign value map
   */
  public Map<String, Set<AttributeAssignValue>> getAttributeAssignIdToAttributeAssignValueMap() {
    return this.attributeAssignIdToAttributeAssignValuesMap;
  }
  
  /**
   * result objects
   * @param resultObjects1 the resultObjects to set
   */
  public void setResultObjects(Set<Object[]> resultObjects1) {
    this.resultObjects = resultObjects1;
    
    this.attributeAssignFinderResults = new LinkedHashSet<AttributeAssignFinderResult>();

    // index everything
    for (Object[] result : GrouperUtil.nonNull(resultObjects1)) {
      AttributeAssignFinderResult attributeAssignFinderResult = new AttributeAssignFinderResult();
      if (result[0] instanceof Group) {
        Group group = (Group) result[0];
        idToGroupMap.put(group.getId(), group);
        attributeAssignFinderResult.setGroup(group);
      }
      
      AttributeAssign attributeAssign = (AttributeAssign) result[1];
      idToAttributeAssignMap.put(attributeAssign.getId(), attributeAssign);
      
      if (result.length > 2 && result[2] instanceof Set) {
        Set<AttributeAssignValue> attributeAssignValueSet = (Set<AttributeAssignValue>)result[2];
        attributeAssignIdToAttributeAssignValuesMap.put(attributeAssign.getId(), attributeAssignValueSet);
        attributeAssignFinderResult.setAttributeAssignValues(attributeAssignValueSet);
      }
      this.attributeAssignFinderResults.add(attributeAssignFinderResult);
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
