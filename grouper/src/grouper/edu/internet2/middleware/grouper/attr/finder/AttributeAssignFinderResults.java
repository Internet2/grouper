/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.finder;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
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
   * id to group map
   */
  private Map<String, Group> idToGroupMap = new HashMap<String, Group>();

  /**
   * id to stem map
   */
  private Map<String, Stem> idToStemMap = new HashMap<String, Stem>();

  /**
   * id to member map
   */
  private Map<String, Member> idToMemberMap = new HashMap<String, Member>();

  
  /**
   * id to stem map
   * @return the idToStemMap
   */
  public Map<String, Stem> getIdToStemMap() {
    return this.idToStemMap;
  }

  /**
   * id to attributeDefName map
   */
  private Map<String, AttributeDefName> idToAttributeDefNameMap = new HashMap<String, AttributeDefName>();

  /**
   * id to attributeDefName map
   * @return map
   */
  public Map<String, AttributeDefName> getIdToAttributeDefNameMap() {
    return this.idToAttributeDefNameMap;
  }

  /**
   * id to attributeDef map
   */
  private Map<String, AttributeDef> idToAttributeDefMap = new HashMap<String, AttributeDef>();

  /**
   * id to attributeDef map
   * @return map
   */
  public Map<String, AttributeDef> getIdToAttributeDefMap() {
    return this.idToAttributeDefMap;
  }

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

      if (result[0] instanceof AttributeDef) {
        AttributeDef attributeDef = (AttributeDef) result[0];
        idToAttributeDefMap.put(attributeDef.getId(), attributeDef);
        attributeAssignFinderResult.setAttributeDef(attributeDef);
      } else {
        throw new RuntimeException("Not expecting non attribute def object: " + result[0]);
      }

      if (result[1] instanceof AttributeDefName) {
        AttributeDefName attributeDefName = (AttributeDefName) result[1];
        idToAttributeDefNameMap.put(attributeDefName.getId(), attributeDefName);
        attributeAssignFinderResult.setAttributeDefName(attributeDefName);
      } else {
        throw new RuntimeException("Not expecting non attribute def name object: " + result[1]);
      }

      if (result[2] instanceof Group) {
        Group group = (Group) result[2];
        idToGroupMap.put(group.getId(), group);
        attributeAssignFinderResult.setOwnerGroup(group);
      } else if (result[2] instanceof Stem) {
        Stem stem = (Stem) result[2];
        idToStemMap.put(stem.getId(), stem);
        attributeAssignFinderResult.setOwnerStem(stem);
      } else if (result[2] instanceof AttributeDef) {
        AttributeDef attributeDef = (AttributeDef) result[2];
        idToAttributeDefMap.put(attributeDef.getId(), attributeDef);
        attributeAssignFinderResult.setOwnerAttributeDef(attributeDef);
      } else if (result[2] instanceof Member) {
        Member member = (Member) result[2];
        idToMemberMap.put(member.getId(), member);
        attributeAssignFinderResult.setOwnerMember(member);
      } else if (result[2] instanceof AttributeAssign) {
        AttributeAssign attributeAssign = (AttributeAssign) result[2];
        idToAttributeAssignMap.put(attributeAssign.getId(), attributeAssign);
        attributeAssignFinderResult.setOwnerAttributeAssign(attributeAssign);
      } else {
        throw new RuntimeException("Not expecting owner object: " + result[2]);
      }
      
      AttributeAssign attributeAssign = (AttributeAssign) result[3];
      idToAttributeAssignMap.put(attributeAssign.getId(), attributeAssign);
      
      if (result.length > 4) {
        if (result[4] instanceof Set) {
          Set<AttributeAssignValue> attributeAssignValueSet = (Set<AttributeAssignValue>)result[4];
          attributeAssignIdToAttributeAssignValuesMap.put(attributeAssign.getId(), attributeAssignValueSet);
          attributeAssignFinderResult.setAttributeAssignValues(attributeAssignValueSet);
        } else if (result[4] != null) {
          throw new RuntimeException("Not expecting value object: " + result[4]);
        }
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
