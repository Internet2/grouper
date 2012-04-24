/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.value;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class AttributeValueDelegate {

  /**
   * reference to the attribute delegate
   */
  private AttributeAssignBaseDelegate attributeAssignBaseDelegate = null;
  /** cache hits for testing */
  public static long allAttributeAssignValuesCacheHitsForTest = 0;
  /** cache misses for testing */
  public static long allAttributeAssignValuesCacheMissesForTest = 0;
  /** keep a cache of attribute assigns and values */
  private Map<AttributeAssign, Set<AttributeAssignValue>> allAttributeAssignValuesCache = null;

  /**
   * 
   * @param attributeAssignBaseDelegate1
   */
  public AttributeValueDelegate(AttributeAssignBaseDelegate attributeAssignBaseDelegate1) {
    this.attributeAssignBaseDelegate = attributeAssignBaseDelegate1;
  }

  /**
   * assign a value of any type 
   * @param attributeDefNameName 
   * @param value 
   * @return the value object
   */
  public AttributeValueResult assignValue(String attributeDefNameName, String value) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
    
    AttributeAssignValueResult attributeAssignValueResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().assignValue(value);
    
    return new AttributeValueResult(attributeAssignResult, attributeAssignValueResult);
  }

  /**
   * assign a string
   * @param attributeDefNameName 
   * @param value 
   * @return the value object
   */
  public AttributeValueResult assignValueString(String attributeDefNameName, String value) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
    
    AttributeAssignValueResult attributeAssignValueResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().assignValueString(value);
    
    return new AttributeValueResult(attributeAssignResult, attributeAssignValueResult);
  }

  /**
   * assign a value integer
   * @param attributeDefNameName 
   * @param value 
   * @return the value object
   */
  public AttributeValueResult assignValueInteger(String attributeDefNameName, Long value) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
    
    AttributeAssignValueResult attributeAssignValueResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().assignValueInteger(value);
    
    return new AttributeValueResult(attributeAssignResult, attributeAssignValueResult);
  }

  /**
   * assign a value floating
   * @param attributeDefNameName 
   * @param value 
   * @return the value object
   */
  public AttributeValueResult assignValueFloating(String attributeDefNameName, Double value) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
    
    AttributeAssignValueResult attributeAssignValueResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().assignValueFloating(value);
    
    return new AttributeValueResult(attributeAssignResult, attributeAssignValueResult);
  }

  /**
   * assign a value memberId
   * @param attributeDefNameName 
   * @param memberId 
   * @return the value object
   */
  public AttributeValueResult assignValueMember(String attributeDefNameName, String memberId) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
    
    AttributeAssignValueResult attributeAssignValueResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().assignValueMember(memberId);
    
    return new AttributeValueResult(attributeAssignResult, attributeAssignValueResult);
  }

  /**
   * assign a value of member type
   * @param attributeDefNameName 
   * @param member 
   * @return the value object
   */
  public AttributeValueResult assignValueMember(String attributeDefNameName, Member member) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
    
    AttributeAssignValueResult attributeAssignValueResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().assignValueMember(member);
    
    return new AttributeValueResult(attributeAssignResult, attributeAssignValueResult);
  }

  /**
   * assign a value of member type
   * @param attributeDefNameName 
   * @param timestamp 
   * @return the value object
   */
  public AttributeValueResult assignValueTimestamp(String attributeDefNameName, Timestamp timestamp) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
    
    AttributeAssignValueResult attributeAssignValueResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().assignValueTimestamp(timestamp);
    
    return new AttributeValueResult(attributeAssignResult, attributeAssignValueResult);
  }

  /**
   * assign a values of any type
   * @param attributeDefNameName 
   * @param values 
   * @param deleteOrphans
   * @return the value object
   */
  public AttributeValueResult assignValuesAnyType(String attributeDefNameName, Set<String> values, boolean deleteOrphans) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
    
    AttributeAssignValuesResult attributeAssignValuesResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().assignValuesAnyType(values, deleteOrphans);
    
    return new AttributeValueResult(attributeAssignResult, attributeAssignValuesResult);
  }

  
  /**
   * assign a values of integer type
   * @param attributeDefNameName 
   * @param values 
   * @param deleteOrphans
   * @return the value object
   */
  public AttributeValueResult assignValuesInteger(String attributeDefNameName, Set<Long> values, boolean deleteOrphans) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
    
    AttributeAssignValuesResult attributeAssignValuesResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().assignValuesInteger(values, deleteOrphans);
    
    return new AttributeValueResult(attributeAssignResult, attributeAssignValuesResult);
  }

  /**
   * assign a values of floating type
   * @param attributeDefNameName 
   * @param values 
   * @param deleteOrphans
   * @return the value object
   */
  public AttributeValueResult assignValuesFloating(String attributeDefNameName, Set<Double> values, boolean deleteOrphans) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
    
    AttributeAssignValuesResult attributeAssignValuesResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().assignValuesFloating(values, deleteOrphans);
    
    return new AttributeValueResult(attributeAssignResult, attributeAssignValuesResult);
  }

  /**
   * assign a values of timestamp type
   * @param attributeDefNameName 
   * @param values 
   * @param deleteOrphans
   * @return the value object
   */
  public AttributeValueResult assignValuesTimestamp(String attributeDefNameName, Set<Timestamp> values, boolean deleteOrphans) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
    
    AttributeAssignValuesResult attributeAssignValuesResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().assignValuesTimestamp(values, deleteOrphans);
    
    return new AttributeValueResult(attributeAssignResult, attributeAssignValuesResult);
  }

  /**
   * assign a values of member type
   * @param attributeDefNameName 
   * @param values 
   * @param deleteOrphans
   * @return the value object
   */
  public AttributeValueResult assignValuesMember(String attributeDefNameName, Set<Member> values, boolean deleteOrphans) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
    
    AttributeAssignValuesResult attributeAssignValuesResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().assignValuesMember(values, deleteOrphans);
    
    return new AttributeValueResult(attributeAssignResult, attributeAssignValuesResult);
  }

  /**
   * assign a values of memberid type
   * @param attributeDefNameName 
   * @param memberIds 
   * @param deleteOrphans
   * @return the value object
   */
  public AttributeValueResult assignValuesMemberIds(String attributeDefNameName, Set<String> memberIds, boolean deleteOrphans) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
    
    AttributeAssignValuesResult attributeAssignValuesResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().assignValuesMemberIds(memberIds, deleteOrphans);
    
    return new AttributeValueResult(attributeAssignResult, attributeAssignValuesResult);
  }

  /**
   * assign a values of string type
   * @param attributeDefNameName 
   * @param values 
   * @param deleteOrphans
   * @return the value object
   */
  public AttributeValueResult assignValuesString(String attributeDefNameName, Set<String> values, boolean deleteOrphans) {
    
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
    
    AttributeAssignValuesResult attributeAssignValuesResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().assignValuesString(values, deleteOrphans);
    
    return new AttributeValueResult(attributeAssignResult, attributeAssignValuesResult);
  }

  /**
   * get the floating value (must be floating type).
   * if attribute not assigned, return null
   * @param attributeDefNameName 
   * @return the value
   */
  public Double retrieveValueFloating(String attributeDefNameName) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().retrieveValueFloating();
  }

  /**
   * get the integer value (must be floating type).
   * if attribute not assigned, return null
   * @param attributeDefNameName 
   * @return the value
   */
  public Long retrieveValueInteger(String attributeDefNameName) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().retrieveValueInteger();
  }

  /**
   * get the string value (any type).
   * if attribute not assigned, return null
   * @param attributeDefNameName 
   * @return the value
   */
  public String retrieveValueString(String attributeDefNameName) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().retrieveValueString();
  }

  /**
   * get the member value (must be member type).
   * if attribute not assigned, return null
   * @param attributeDefNameName 
   * @return the value
   */
  public Member retrieveValueMember(String attributeDefNameName) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().retrieveValueMember();
  }


  /**
   * get the member id value (must be member type).
   * if attribute not assigned, return null
   * @param attributeDefNameName 
   * @return the value
   */
  public String retrieveValueMemberId(String attributeDefNameName) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().retrieveValueMemberId();
  }

  /**
   * get the timestamp value (must be timestamp type).
   * if attribute not assigned, return null
   * @param attributeDefNameName 
   * @return the value
   */
  public Timestamp retrieveValueTimestamp(String attributeDefNameName) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().retrieveValueTimestamp();
  }

  /**
   * get the member values (must be member type).
   * if attribute not assigned, return null
   * @param attributeDefNameName 
   * @return the value
   */
  public List<Member> retrieveValuesMember(String attributeDefNameName) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().retrieveValuesMember();
  }


  /**
   * get the string values (any type).
   * if attribute not assigned, return null
   * @param attributeDefNameName 
   * @return the value
   */
  public List<String> retrieveValuesString(String attributeDefNameName) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().retrieveValuesString();
  }


  /**
   * get the integer values (must be integer type).
   * if attribute not assigned, return null
   * @param attributeDefNameName 
   * @return the value
   */
  public List<Long> retrieveValuesInteger(String attributeDefNameName) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().retrieveValuesInteger();
  }


  /**
   * get the floating values (must be floating type).
   * if attribute not assigned, return null
   * @param attributeDefNameName 
   * @return the floating value
   */
  public List<Double> retrieveValuesFloating(String attributeDefNameName) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().retrieveValuesFloating();
  }


  /**
   * get the member id values (must be member type).
   * if attribute not assigned, return null
   * @param attributeDefNameName 
   * @return the value
   */
  public List<String> retrieveValuesMemberId(String attributeDefNameName) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().retrieveValuesMemberId();
  }

  /**
   * get the member values (must be floating type).
   * if attribute not assigned, return null
   * @param attributeDefNameName 
   * @return the floating value
   */
  public List<Timestamp> retrieveValuesTimestamp(String attributeDefNameName) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().retrieveValuesTimestamp();
  }

  /**
   * find this value (return one if more than one)
   * @param attributeAssignValue to find (by value)
   * @param attributeDefNameName name of attributeDefName to find
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValue(String attributeDefNameName, AttributeAssignValue attributeAssignValue) {
    return GrouperUtil.collectionPopOne(findValues(attributeDefNameName, attributeAssignValue), false);
  }

  /**
   * find this value
   * @param attributeDefNameName name of attributeDefName to find
   * @param value to find (any type)
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValue(String attributeDefNameName, String value) {
    return GrouperUtil.collectionPopOne(findValues(attributeDefNameName, value), false);
  }

  /**
   * find this value
   * @param attributeDefNameName name of attributeDefName to find
   * @param value to find (floating type)
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValueFloating(String attributeDefNameName, Double value) {
    return GrouperUtil.collectionPopOne(findValuesFloating(attributeDefNameName, value), false);
  }

  /**
   * find this value
   * @param attributeDefNameName name of attributeDefName to find
   * @param value to find (integer type)
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValueInteger(String attributeDefNameName, Long value) {
    return GrouperUtil.collectionPopOne(findValuesInteger(attributeDefNameName, value), false);
  }

  /**
   * find this value
   * @param attributeDefNameName name of attributeDefName to find
   * @param value to find (floating type)
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValueMember(String attributeDefNameName, Member value) {
    
    return findValueMember(attributeDefNameName, value == null ? null : value.getUuid());
  
  }

  /**
   * find this value
   * @param attributeDefNameName name of attributeDefName to find
   * @param value to find (member type)
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValueMember(String attributeDefNameName, String value) {
    return GrouperUtil.collectionPopOne(findValuesMember(attributeDefNameName, value), false);
  }

  /**
   * find these values
   * @param attributeDefNameName name of attributeDefName to find
   * @param attributeAssignValue to find (by value)
   * @return the value if found, or null if not
   */
  public Set<AttributeAssignValue> findValues(String attributeDefNameName, AttributeAssignValue attributeAssignValue) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().internal_findValues(attributeAssignValue, false);
  }

  /**
   * find this value
   * @param attributeDefNameName name of attributeDefName to find
   * @param value to find (any type)
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> findValues(String attributeDefNameName, String value) {
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().internal_findValues(value, false);
  }

  /**
   * retrieveAssignment
   * @param attributeDefNameName
   * @return assignment
   */
  private AttributeAssign retrieveAssignmentForRead(String attributeDefNameName) {
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, true);
    
    this.attributeAssignBaseDelegate.assertCanReadAttributeDefName(attributeDefName);
    
    Map<AttributeAssign, Set<AttributeAssignValue>> cachedMap = this.getAllAttributeAssignsForCache();
    
    if (cachedMap != null) {
      Set<AttributeAssign> matching = new HashSet<AttributeAssign>();
      for (AttributeAssign attributeAssign : cachedMap.keySet()) {
        if (StringUtils.equals(attributeAssign.getAttributeDefNameId(), attributeDefName.getId())) {
          matching.add(attributeAssign);
        }
      }
      return GrouperUtil.setPopOne(matching);
    }
    
    AttributeAssign attributeAssign = this.attributeAssignBaseDelegate.retrieveAssignment(
        null, attributeDefName, false, false);
    return attributeAssign;
  }

  /**
   * retrieveAssignment
   * @param attributeDefNameName
   * @return assignment
   */
  private AttributeAssign retrieveAssignmentForUpdate(String attributeDefNameName) {
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, true);
    
    this.attributeAssignBaseDelegate.assertCanUpdateAttributeDefName(attributeDefName);
    
    AttributeAssign attributeAssign = this.attributeAssignBaseDelegate.retrieveAssignment(
        null, attributeDefName, false, false);
    return attributeAssign;
  }

  /**
   * find this value
   * @param attributeDefNameName name of attributeDefName to find
   * @param value to find (floating type)
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> findValuesFloating(String attributeDefNameName, Double value) {
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().internal_findValuesFloating(value, false);
  }

  /**
   * find these values
   * @param attributeDefNameName name of attributeDefName to find
   * @param value to find (integer type)
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> findValuesInteger(String attributeDefNameName, Long value) {
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().internal_findValuesInteger(value, false);
  }

  /**
   * find this value
   * @param attributeDefNameName name of attributeDefName to find
   * @param value to find (floating type)
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> findValuesMember(String attributeDefNameName, Member value) {
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().internal_findValuesMember(value, true);
  }

  /**
   * find this value
   * @param attributeDefNameName name of attributeDefName to find
   * @param value to find (member type)
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> findValuesMember(String attributeDefNameName, String value) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().internal_findValuesMember(value, true);
  }

  /**
   * find these values
   * @param attributeDefNameName name of attributeDefName to find
   * @param value to find (string type)
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> findValuesString(String attributeDefNameName, String value) {
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().internal_findValuesString(value, true);
  }

  /**
   * find these values
   * @param attributeDefNameName name of attributeDefName to find
   * @param value to find (timestamp type)
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> findValuesTimestamp(String attributeDefNameName, Timestamp value) {
    AttributeAssign attributeAssign = retrieveAssignmentForRead(attributeDefNameName);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().internal_findValuesTimestamp(value, true);
  }

  /**
   * find this value
   * @param attributeDefNameName name of attributeDefName to find
   * @param value to find (string type)
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValueString(String attributeDefNameName, String value) {
    return GrouperUtil.collectionPopOne(findValuesString(attributeDefNameName, value), false);
  }

  /**
   * find this value
   * @param attributeDefNameName name of attributeDefName to find
   * @param value to find (timestamp type)
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValueTimestamp(String attributeDefNameName, Timestamp value) {
    return GrouperUtil.collectionPopOne(findValuesTimestamp(attributeDefNameName, value), false);
  }

  /**
   * add a value to the attribute assignment
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param attributeAssignValue
   * @return result
   */
  public AttributeValueResult addValue(String attributeDefNameName, AttributeAssignValue attributeAssignValue) {
    
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
  
    AttributeAssignValueResult attributeAssignValueResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().addValue(attributeAssignValue);
  
    return new AttributeValueResult(attributeAssignResult, attributeAssignValueResult);

  }

  /**
   * add a value of any type 
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param value 
   * @return the value object
   */
  public AttributeValueResult addValue(String attributeDefNameName, String value) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);

    AttributeAssignValueResult attributeAssignValueResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().addValue(value);
  
    return new AttributeValueResult(attributeAssignResult, attributeAssignValueResult);
  }

  /**
   * add a value of double type
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param value 
   * @return the value object
   */
  public AttributeValueResult addValueFloating(String attributeDefNameName, Double value) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);

    AttributeAssignValueResult attributeAssignValueResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().addValueFloating(value);
  
    return new AttributeValueResult(attributeAssignResult, attributeAssignValueResult);
  }

  /**
   * add a value of integer type
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param value 
   * @return the value object
   */
  public AttributeValueResult addValueInteger(String attributeDefNameName, Long value) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);

    AttributeAssignValueResult attributeAssignValueResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().addValueInteger(value);
  
    return new AttributeValueResult(attributeAssignResult, attributeAssignValueResult);
  }

  /**
   * add a value of member type
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param value 
   * @return the value object
   */
  public AttributeValueResult addValueMember(String attributeDefNameName, Member value) {
    return this.addValueMember(attributeDefNameName, value == null ? null : value.getUuid());
  }

  /**
   * add a value of member type
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param memberId 
   * @return the value object
   */
  public AttributeValueResult addValueMember(String attributeDefNameName, String memberId) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
  
    AttributeAssignValueResult attributeAssignValueResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().addValueMember(memberId);
  
    return new AttributeValueResult(attributeAssignResult, attributeAssignValueResult);
  }

  /**
   * add values to the attribute assignment
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param attributeAssignValues
   * @return result
   */
  public AttributeValueResult addValues(String attributeDefNameName, Collection<AttributeAssignValue> attributeAssignValues) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
  
    AttributeAssignValuesResult attributeAssignValuesResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().addValues(attributeAssignValues);
  
    return new AttributeValueResult(attributeAssignResult, attributeAssignValuesResult);
  }

  /**
   * add values of any type 
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param values
   * @return the value object
   */
  public AttributeValueResult addValuesAnyType(String attributeDefNameName, Collection<String> values) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
  
    AttributeAssignValuesResult attributeAssignValuesResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().addValuesAnyType(values);
  
    return new AttributeValueResult(attributeAssignResult, attributeAssignValuesResult);
  }

  /**
   * add values of double type
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param values
   * @return the value object
   */
  public AttributeValueResult addValuesFloating(String attributeDefNameName, Collection<Double> values) {
    
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
  
    AttributeAssignValuesResult attributeAssignValuesResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().addValuesFloating(values);
  
    return new AttributeValueResult(attributeAssignResult, attributeAssignValuesResult);
  }

  /**
   * add values of integer type
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param values
   * @return the value object
   */
  public AttributeValueResult addValuesInteger(String attributeDefNameName, Collection<Long> values) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
  
    AttributeAssignValuesResult attributeAssignValuesResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().addValuesInteger(values);
  
    return new AttributeValueResult(attributeAssignResult, attributeAssignValuesResult);
  }

  /**
   * add values of member type
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param values
   * @return the value object
   */
  public AttributeValueResult addValuesMember(String attributeDefNameName, Collection<Member> values) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
  
    AttributeAssignValuesResult attributeAssignValuesResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().addValuesMember(values);
  
    return new AttributeValueResult(attributeAssignResult, attributeAssignValuesResult);
  }

  /**
   * add a values of member type
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param memberIds
   * @return the value object
   */
  public AttributeValueResult addValuesMemberIds(String attributeDefNameName, Collection<String> memberIds) {
    
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
  
    AttributeAssignValuesResult attributeAssignValuesResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().addValuesMemberIds(memberIds);
  
    return new AttributeValueResult(attributeAssignResult, attributeAssignValuesResult);
  }

  /**
   * add values of type string.  use addValue() to add a value of any type
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param values
   * @return the value object
   */
  public AttributeValueResult addValuesString(String attributeDefNameName, Collection<String> values) {
    
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
  
    AttributeAssignValuesResult attributeAssignValuesResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().addValuesString(values);
  
    return new AttributeValueResult(attributeAssignResult, attributeAssignValuesResult);
  }

  /**
   * add values of timestamp type
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param values 
   * @return the value objects
   */
  public AttributeValueResult addValuesTimestamp(String attributeDefNameName, Collection<Timestamp> values) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
  
    AttributeAssignValuesResult attributeAssignValuesResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().addValuesTimestamp(values);
  
    return new AttributeValueResult(attributeAssignResult, attributeAssignValuesResult);
  }

  /**
   * add a value of type string.  use addValue() to add a value of any type
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param value 
   * @return the value object
   */
  public AttributeValueResult addValueString(String attributeDefNameName, String value) {
    
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
  
    AttributeAssignValueResult attributeAssignValueResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().addValueString(value);
  
    return new AttributeValueResult(attributeAssignResult, attributeAssignValueResult);
  }

  /**
   * add a value of timestamp type
   * @param attributeDefNameName name of attributeDefName to add a value to
   * @param value 
   * @return the value object
   */
  public AttributeValueResult addValueTimestamp(String attributeDefNameName, Timestamp value) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
  
    AttributeAssignValueResult attributeAssignValueResult = attributeAssignResult
      .getAttributeAssign().getValueDelegate().addValueTimestamp(value);
  
    return new AttributeValueResult(attributeAssignResult, attributeAssignValueResult);
  }

  /**
   * remove this value
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param attributeAssignValue
   * @return result
   */
  public AttributeValueResult deleteValue(String attributeDefNameName, AttributeAssignValue attributeAssignValue) {
    AttributeAssign attributeAssign = retrieveAssignmentForUpdate(attributeDefNameName);
    
    if (attributeAssign == null) {
      //not sure why this would happen
      return new AttributeValueResult();
    }
    
    return new AttributeValueResult(attributeAssign.getValueDelegate().internal_deleteValues(GrouperUtil.toSet(attributeAssignValue), false));
  }

  /**
   * remove this value of any type
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param value
   * @return the strings that were deleted
   */
  public AttributeValueResult deleteValue(String attributeDefNameName, String value) {
    return deleteValuesAnyType(attributeDefNameName, GrouperUtil.toSet(value));
  }

  /**
   * remove this value of floating type
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param value
   * @return the strings that were deleted
   */
  public AttributeValueResult deleteValueFloating(String attributeDefNameName, Double value) {
    return deleteValuesFloating(attributeDefNameName, GrouperUtil.toSet(value));
  }

  /**
   * remove this value of integer type
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param value
   * @return the strings that were deleted
   */
  public AttributeValueResult deleteValueInteger(String attributeDefNameName, Long value) {
    return deleteValuesInteger(attributeDefNameName, GrouperUtil.toSet(value));
  }

  /**
   * remove this value of member type
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param value
   * @return the strings that were deleted
   */
  public AttributeValueResult deleteValueMember(String attributeDefNameName, Member value) {
    return deleteValuesMember(attributeDefNameName, GrouperUtil.toSet(value));
  }

  /**
   * remove this value of member id type
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param value
   * @return the strings that were deleted
   */
  public AttributeValueResult deleteValueMember(String attributeDefNameName, String value) {
    return deleteValuesMemberIds(attributeDefNameName, GrouperUtil.toSet(value));
  }

  /**
   * remove this value
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param attributeAssignValues
   * @return the result
   */
  public AttributeValueResult deleteValues(String attributeDefNameName, Collection<AttributeAssignValue> attributeAssignValues) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForUpdate(attributeDefNameName);
    
    if (attributeAssign == null) {
      //not sure why this would happen
      return new AttributeValueResult();
    }
    
    return new AttributeValueResult(attributeAssign.getValueDelegate().internal_deleteValues(attributeAssignValues, false));
  }

  /**
   * remove this value of any type
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param values
   * @return the strings that were deleted
   */
  public AttributeValueResult deleteValuesAnyType(String attributeDefNameName, Collection<String> values) {
  
    AttributeAssign attributeAssign = retrieveAssignmentForUpdate(attributeDefNameName);
    
    if (attributeAssign == null) {
      //not sure why this would happen
      return new AttributeValueResult();
    }
    
    return new AttributeValueResult(attributeAssign.getValueDelegate().deleteValuesAnyType(values));
  }

  /**
   * remove this value of floating type
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param values
   * @return the strings that were deleted
   */
  public AttributeValueResult deleteValuesFloating(String attributeDefNameName, Collection<Double> values) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForUpdate(attributeDefNameName);
    
    if (attributeAssign == null) {
      //not sure why this would happen
      return new AttributeValueResult();
    }
    
    return new AttributeValueResult(attributeAssign.getValueDelegate().deleteValuesFloating(values));
  }

  /**
   * remove this value of integer type
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param values
   * @return the strings that were deleted
   */
  public AttributeValueResult deleteValuesInteger(String attributeDefNameName, Collection<Long> values) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForUpdate(attributeDefNameName);
    
    if (attributeAssign == null) {
      //not sure why this would happen
      return new AttributeValueResult();
    }
    
    return new AttributeValueResult(attributeAssign.getValueDelegate().deleteValuesInteger(values));
  }

  /**
   * remove this value of member type
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param members
   * @return the strings that were deleted
   */
  public AttributeValueResult deleteValuesMember(String attributeDefNameName, Collection<Member> members) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForUpdate(attributeDefNameName);
    
    if (attributeAssign == null) {
      //not sure why this would happen
      return new AttributeValueResult();
    }
    
    return new AttributeValueResult(attributeAssign.getValueDelegate().deleteValuesMember(members));
  }

  /**
   * remove this value of string type
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param memberIds
   * @return the strings that were deleted
   */
  public AttributeValueResult deleteValuesMemberIds(String attributeDefNameName, Collection<String> memberIds) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForUpdate(attributeDefNameName);
    
    if (attributeAssign == null) {
      //not sure why this would happen
      return new AttributeValueResult();
    }
    
    return new AttributeValueResult(attributeAssign.getValueDelegate().deleteValuesMemberIds(memberIds));
  }

  /**
   * remove this value of string type
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param values
   * @return the strings that were deleted
   */
  public AttributeValueResult deleteValuesString(String attributeDefNameName, Collection<String> values) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForUpdate(attributeDefNameName);
    
    if (attributeAssign == null) {
      //not sure why this would happen
      return new AttributeValueResult();
    }
    
    return new AttributeValueResult(attributeAssign.getValueDelegate().deleteValuesString(values));
  }

  /**
   * remove this value of timestamp type
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param values
   * @return the timestamps that were deleted
   */
  public AttributeValueResult deleteValuesTimestamp(String attributeDefNameName, Collection<Timestamp> values) {
    
    AttributeAssign attributeAssign = retrieveAssignmentForUpdate(attributeDefNameName);
    
    if (attributeAssign == null) {
      //not sure why this would happen
      return new AttributeValueResult();
    }
    
    return new AttributeValueResult(attributeAssign.getValueDelegate().deleteValuesTimestamp(values));
  }

  /**
   * remove this value of string type
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param value
   * @return the strings that were deleted
   */
  public AttributeValueResult deleteValueString(String attributeDefNameName, String value) {
    return deleteValuesString(attributeDefNameName, GrouperUtil.toSet(value));
  }

  /**
   * remove this value of any type
   * @param attributeDefNameName name of attributeDefName to delete a value from
   * @param value
   * @return the strings that were deleted
   */
  public AttributeValueResult deleteValueTimestamp(String attributeDefNameName, Timestamp value) {
    return deleteValuesTimestamp(attributeDefNameName, GrouperUtil.toSet(value));
  }

  /**
   * return the cache of all attribute assigns, might be null if not caching
   * @return the allAttributeAssignsCache
   */
  public Map<AttributeAssign, Set<AttributeAssignValue>> getAllAttributeAssignsForCache() {
    if (this.allAttributeAssignValuesCache == null) {
      allAttributeAssignValuesCacheMissesForTest++;
      return null;
    }
    allAttributeAssignValuesCacheHitsForTest++;
    return this.allAttributeAssignValuesCache;
  }

  /**
   * @param theAllAttributeAssignValuesForCache the Set of attributes to put in cache
   */
  public void setAllAttributeAssignValuesForCache(
      Map<AttributeAssign, Set<AttributeAssignValue>> theAllAttributeAssignValuesForCache) {
    this.allAttributeAssignValuesCache = theAllAttributeAssignValuesForCache;
  }

}
