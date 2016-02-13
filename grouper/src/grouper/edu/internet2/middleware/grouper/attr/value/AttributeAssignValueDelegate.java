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
 * $Id: AttributeAssignGroupDelegate.java,v 1.6 2009-10-12 09:46:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.value;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue.AttributeAssignValueType;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * delegate privilege calls from attribute defs
 */
public class AttributeAssignValueDelegate {

  /** cache hits for testing */
  public static long allAttributeAssignValuesCacheHitsForTest = 0;
  
  /** cache misses for testing */
  public static long allAttributeAssignValuesCacheMissesForTest = 0;
  
  /** keep a cache of values */
  private Set<AttributeAssignValue> allAttributeAssignValuesCache = null;

  /**
   * cache of values
   * @return values
   */
  public Set<AttributeAssignValue> getAllAttributeAssignValuesCache() {
    return this.allAttributeAssignValuesCache;
  }

  /**
   * cache of values
   * @param allAttributeAssignValuesCache1
   */
  public void setAllAttributeAssignValuesCache(
      Set<AttributeAssignValue> allAttributeAssignValuesCache1) {
    this.allAttributeAssignValuesCache = allAttributeAssignValuesCache1;
  }


  /**
   * reference to the group in question
   */
  private AttributeAssign attributeAssign = null;
  
  /**
   * 
   * @param attributeAssign1
   */
  public AttributeAssignValueDelegate(AttributeAssign attributeAssign1) {
    this.attributeAssign = attributeAssign1;
  }

  /**
   * get the values for an assignment or empty set if none
   * @return the values
   */
  public Set<AttributeAssignValue> retrieveValues() {
    return internal_retrieveValues(true, true);
  }
  
  /**
   * get the values for an assignment or empty set if none
   * @return the values
   */
  public Set<AttributeAssignValue> getAttributeAssignValues() {
    return retrieveValues();
  }
  
  /**
   * get the values for an assignment or empty set if none
   * @param checkSecurity 
   * @param filterInvalidTypes if values of invalid types should be filtered out
   * @return the values
   */
  public Set<AttributeAssignValue> internal_retrieveValues(boolean checkSecurity, boolean filterInvalidTypes) {
    return internal_retrieveValues(checkSecurity, filterInvalidTypes, false);
  }
  
  /**
   * get the values for an assignment or empty set if none
   * @param checkSecurity 
   * @param filterInvalidTypes if values of invalid types should be filtered out
   * @param useCache
   * @return the values
   */
  public Set<AttributeAssignValue> internal_retrieveValues(boolean checkSecurity, boolean filterInvalidTypes, boolean useCache) {
    
    if (checkSecurity) {
      //make sure can read
      AttributeAssignable attributeAssignable = this.attributeAssign.retrieveAttributeAssignable();
      attributeAssignable.getAttributeDelegate().assertCanReadAttributeDefName(this.attributeAssign.getAttributeDefName());
    }
    
    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    
    Set<AttributeAssignValue> results = this.allAttributeAssignValuesCache;
    
    if (results == null) {
      results = GrouperDAOFactory.getFactory().getAttributeAssignValue()
        .findByAttributeAssignId(this.attributeAssign.getId(), new QueryOptions().secondLevelCache(useCache));
      allAttributeAssignValuesCacheMissesForTest++;
    } else {
      allAttributeAssignValuesCacheHitsForTest++;
    }

    if (filterInvalidTypes) {
      //lets filter if not the right type...  not sure why this would be, might be because the type was changed mid-use
      Iterator<AttributeAssignValue> iterator = results.iterator();
      while (iterator.hasNext()) {
        AttributeAssignValue attributeAssignValue = iterator.next();
        AttributeAssignValueType attributeAssignValueType = attributeAssignValue.getCurrentAssignValueType();
        if (!attributeAssignValueType.compatibleWith(attributeDef.getValueType())) {
          iterator.remove();
        }
        
      }
    } 
    
    if (!attributeDef.isMultiValued() && GrouperUtil.length(results) > 1) {
      throw new RuntimeException("Attribute is not multi-valued, but has multiple results! " 
          + this.attributeAssign.getAttributeDefName().getName() + ", " + this + ", size: " 
          + GrouperUtil.length(results));
    }
    return results;
  }

  /**
   * assign a value to this assignment.  If one exists with a different value, replace, else, reuse, 
   * replace it
   * @param attributeAssignValue
   * @return the value
   */
  public AttributeAssignValueResult assignValue(AttributeAssignValue attributeAssignValue) {
    return internal_assignValue(attributeAssignValue, true);
  }
  
  /**
   * assign a value to this assignment.  If one exists with a different value, replace, else, reuse, 
   * remove it and replace
   * @param attributeAssignValue
   * @param checkSecurity 
   * @return the value
   */
  public AttributeAssignValueResult internal_assignValue(AttributeAssignValue attributeAssignValue, boolean checkSecurity) {
    
    if (checkSecurity) {
      //make sure can edit
      this.attributeAssign.retrieveAttributeAssignable()
        .getAttributeDelegate().assertCanUpdateAttributeDefName(
            this.attributeAssign.getAttributeDefName());
    }

    //no need to check security
    Set<AttributeAssignValue> existingValues = this.internal_retrieveValues(false, false);

    for (AttributeAssignValue existingAttributeValue : existingValues) {
      if (existingAttributeValue.sameValue(attributeAssignValue)) {
        return new AttributeAssignValueResult(false, false, existingAttributeValue);
      }
    }

    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();

    //if one exists, replace it
    if (existingValues.size() > 0) {
      AttributeAssignValue existing = existingValues.iterator().next();
      existing.assignValue(attributeAssignValue);
      
      if (!existing.getCurrentAssignValueType().compatibleWith(attributeDef.getValueType())) {
        throw new RuntimeException("Types not compatible: " 
            + attributeAssignValue.getCurrentAssignValueType() + ", " 
            + attributeDef.getValueType() + ", " + attributeDef);
      }
      
      existing.saveOrUpdate();
      return new AttributeAssignValueResult(true, false, existing);
    }
    
    //couldnt find, add, but we already checked security...
    internal_addValues(GrouperUtil.toSet(attributeAssignValue), false);
    return new AttributeAssignValueResult(true, false, attributeAssignValue);
  }

  /**
   * add a value to the attribute assignment
   * @param attributeAssignValue
   * @return result
   */
  public AttributeAssignValueResult addValue(AttributeAssignValue attributeAssignValue) {
    return internal_addValues(GrouperUtil.toSet(attributeAssignValue), true).getAttributeAssignValueResults().iterator().next();
  }
  
  /**
   * add values to the attribute assignment
   * @param attributeAssignValues
   * @return result
   */
  public AttributeAssignValuesResult addValues(Collection<AttributeAssignValue> attributeAssignValues) {
    return internal_addValues(attributeAssignValues, true);
  }
  
  /**
   * add a value of any type 
   * @param value 
   * @return the value object
   */
  public AttributeAssignValueResult addValue(String value) {
    return addValuesAnyType(GrouperUtil.toSet(value)).getAttributeAssignValueResults().iterator().next();
  }
  
  /**
   * add values of any type 
   * @param values
   * @return the value object
   */
  public AttributeAssignValuesResult addValuesAnyType(Collection<String> values) {
    AttributeAssignValuesResult attributeAssignValuesResult = new AttributeAssignValuesResult();
    attributeAssignValuesResult.setAttributeAssignValueResults(new LinkedHashSet<AttributeAssignValueResult>());
    for (String value : values) {
      AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
      attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
      attributeAssignValue.assignValue(value);
      AttributeAssignValuesResult currentResult = internal_addValues(GrouperUtil.toSet(attributeAssignValue), true);
      attributeAssignValuesResult.setChanged(attributeAssignValuesResult.isChanged() || currentResult.isChanged());
      attributeAssignValuesResult.getAttributeAssignValueResults().addAll(currentResult.getAttributeAssignValueResults());
    }
    return attributeAssignValuesResult;
  }
  
  /**
   * assign a value of any type 
   * @param value 
   * @return the value object
   */
  public AttributeAssignValueResult assignValue(String value) {
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
    attributeAssignValue.assignValue(value);
    return assignValue(attributeAssignValue);
  }
  
  /**
   * add a value of type string.  use addValue() to add a value of any type
   * @param value 
   * @return the value object
   */
  public AttributeAssignValueResult addValueString(String value) {
    return addValuesString(GrouperUtil.toSet(value)).getAttributeAssignValueResults().iterator().next();
  }

  /**
   * add values of type string.  use addValue() to add a value of any type
   * @param values
   * @return the value object
   */
  public AttributeAssignValuesResult addValuesString(Collection<String> values) {
    
    AttributeAssignValuesResult attributeAssignValuesResult = new AttributeAssignValuesResult();
    attributeAssignValuesResult.setAttributeAssignValueResults(new LinkedHashSet<AttributeAssignValueResult>());

    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    if (attributeDef.getValueType() != AttributeDefValueType.string) {
      throw new RuntimeException("Expecting String value type: " + attributeDef.getValueType());
    }
    for (String value : values) {
      AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
      attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
      attributeAssignValue.setValueString(value);
      AttributeAssignValuesResult currentResult = internal_addValues(GrouperUtil.toSet(attributeAssignValue), true);
      attributeAssignValuesResult.setChanged(attributeAssignValuesResult.isChanged() || currentResult.isChanged());
      attributeAssignValuesResult.getAttributeAssignValueResults().addAll(currentResult.getAttributeAssignValueResults());
    }
    return attributeAssignValuesResult;
  }
  
  /**
   * assign a value of type string.  use assignValue() to assign a value of any type
   * @param value 
   * @return the value object
   */
  public AttributeAssignValueResult assignValueString(String value) {
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    if (attributeDef.getValueType() != AttributeDefValueType.string) {
      throw new RuntimeException("Expecting String value type: " + attributeDef.getValueType());
    }
    attributeAssignValue.setValueString(value);
    return assignValue(attributeAssignValue);
  }
  
  /**
   * assign a value of type double.  use assignValue() to assign a value of any type
   * @param value 
   * @return the value object
   */
  public AttributeAssignValueResult assignValueFloating(Double value) {
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    if (attributeDef.getValueType() != AttributeDefValueType.floating) {
      throw new RuntimeException("Expecting floating value type: " + attributeDef.getValueType());
    }
    attributeAssignValue.setValueFloating(value);
    return assignValue(attributeAssignValue);
  }
  
  /**
   * assign a value of type integer.  use assignValue() to assign a value of any type
   * @param value 
   * @return the value object
   */
  public AttributeAssignValueResult assignValueInteger(Long value) {
    return internal_assignValueInteger(value, AttributeDefValueType.integer);
  }
  
  /**
   * assign a value of type integer.  use assignValue() to assign a value of any type
   * @param value 
   * @param attributeDefValueType 
   * @return the value object
   */
  public AttributeAssignValueResult internal_assignValueInteger(Long value, AttributeDefValueType attributeDefValueType) {
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    if (attributeDef.getValueType() != attributeDefValueType) {
      throw new RuntimeException("Expecting " + attributeDefValueType + " value type: " + attributeDef.getValueType());
    }
    attributeAssignValue.setValueInteger(value);
    return assignValue(attributeAssignValue);
  }

  /**
   * assign a value of type member.  use assignValue() to assign a value of any type
   * @param value 
   * @return the value object
   */
  public AttributeAssignValueResult assignValueMember(Member value) {
    return this.assignValueMember(value == null ? null : value.getUuid());
  }
  
  /**
   * assign a value of type timestamp.  use assignValue() to assign a value of any type
   * @param value 
   * @return the value object
   */
  public AttributeAssignValueResult assignValueTimestamp(Timestamp value) {
    return this.internal_assignValueInteger(value == null ? null : value.getTime(), AttributeDefValueType.timestamp);
  }
  
  /**
   * assign a value of type member.  use assignValue() to assign a value of any type
   * @param value 
   * @return the value object
   */
  public AttributeAssignValueResult assignValueMember(String value) {
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    if (attributeDef.getValueType() != AttributeDefValueType.memberId) {
      throw new RuntimeException("Expecting member value type: " + attributeDef.getValueType());
    }
    attributeAssignValue.setValueMemberId(value);
    return assignValue(attributeAssignValue);
  }

  /**
   * add a value of double type
   * @param value 
   * @return the value object
   */
  public AttributeAssignValueResult addValueFloating(Double value) {
    return addValuesFloating(GrouperUtil.toSet(value)).getAttributeAssignValueResults().iterator().next();
  }

  /**
   * add values of double type
   * @param values
   * @return the value object
   */
  public AttributeAssignValuesResult addValuesFloating(Collection<Double> values) {
    
    AttributeAssignValuesResult attributeAssignValuesResult = new AttributeAssignValuesResult();
    attributeAssignValuesResult.setAttributeAssignValueResults(new LinkedHashSet<AttributeAssignValueResult>());

    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    if (attributeDef.getValueType() != AttributeDefValueType.floating) {
      throw new RuntimeException("Expecting floating value type: " + attributeDef.getValueType());
    }
    for (Double value : values) {
      AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
      attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
      attributeAssignValue.setValueFloating(value);
      
      AttributeAssignValuesResult currentResult = internal_addValues(GrouperUtil.toSet(attributeAssignValue), true);
      attributeAssignValuesResult.setChanged(attributeAssignValuesResult.isChanged() || currentResult.isChanged());
      attributeAssignValuesResult.getAttributeAssignValueResults().addAll(currentResult.getAttributeAssignValueResults());
    }
    
    return attributeAssignValuesResult;
  }
  
  /**
   * add a value of integer type
   * @param value 
   * @return the value object
   */
  public AttributeAssignValueResult addValueInteger(Long value) {
    return addValuesInteger(GrouperUtil.toSet(value)).getAttributeAssignValueResults().iterator().next();
  }

  /**
   * add values of integer type
   * @param values
   * @return the value object
   */
  public AttributeAssignValuesResult addValuesInteger(Collection<Long> values) {
    return internal_addValuesInteger(values, AttributeDefValueType.integer);
  }

  /**
   * add values of integer type
   * @param values
   * @param attributeDefValueType 
   * @return the value object
   */
  public AttributeAssignValuesResult internal_addValuesInteger(Collection<Long> values,
      AttributeDefValueType attributeDefValueType) {
    
    AttributeAssignValuesResult attributeAssignValuesResult = new AttributeAssignValuesResult();
    attributeAssignValuesResult.setAttributeAssignValueResults(new LinkedHashSet<AttributeAssignValueResult>());
    
    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    if (attributeDef.getValueType() != attributeDefValueType) {
      throw new RuntimeException("Expecting " + attributeDefValueType + " value type: " + attributeDef.getValueType());
    }
    for (Long value : values) {
      AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
      attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
      attributeAssignValue.setValueInteger(value);
      AttributeAssignValuesResult currentResult = internal_addValues(GrouperUtil.toSet(attributeAssignValue), true);
      attributeAssignValuesResult.setChanged(attributeAssignValuesResult.isChanged() || currentResult.isChanged());
      attributeAssignValuesResult.getAttributeAssignValueResults().addAll(currentResult.getAttributeAssignValueResults());

    }

    return attributeAssignValuesResult;
  }
  
  /**
   * add values of timestamp type
   * @param values 
   * @return the value objects
   */
  public AttributeAssignValuesResult addValuesTimestamp(Collection<Timestamp> values) {
    Collection<Long> timestampLongs = new ArrayList<Long>();
    for (Timestamp timestamp : values) {
      timestampLongs.add(timestamp == null ? null : timestamp.getTime());
    }
    return internal_addValuesInteger(timestampLongs, AttributeDefValueType.timestamp);
  }
  
  /**
   * add a value of timestamp type
   * @param value 
   * @return the value object
   */
  public AttributeAssignValueResult addValueTimestamp(Timestamp value) {
    return this.addValuesTimestamp(GrouperUtil.toSet(value)).getAttributeAssignValueResults().iterator().next();
  }
  
  /**
   * add a value of member type
   * @param value 
   * @return the value object
   */
  public AttributeAssignValueResult addValueMember(Member value) {
    return this.addValueMember(value == null ? null : value.getUuid());
  }

  /**
   * add values of member type
   * @param values
   * @return the value object
   */
  public AttributeAssignValuesResult addValuesMember(Collection<Member> values) {
    List<String> memberIds = new ArrayList<String>();
    for (Member member : values) {
      memberIds.add(member == null ? null : member.getUuid());
    }
    return addValuesMemberIds(memberIds);
  }

  /**
   * add a value of member type
   * @param value 
   * @return the value object
   */
  public AttributeAssignValueResult addValueMember(String value) {
    return addValuesMemberIds(GrouperUtil.toSet(value)).getAttributeAssignValueResults().iterator().next();
  }
  
  /**
   * add a values of member type
   * @param values
   * @return the value object
   */
  public AttributeAssignValuesResult addValuesMemberIds(Collection<String> values) {
    
    AttributeAssignValuesResult attributeAssignValuesResult = new AttributeAssignValuesResult();
    attributeAssignValuesResult.setAttributeAssignValueResults(new LinkedHashSet<AttributeAssignValueResult>());
    
    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    if (attributeDef.getValueType() != AttributeDefValueType.memberId) {
      throw new RuntimeException("Expecting member value type: " + attributeDef.getValueType());
    }
    for (String value: values) {
      AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
      attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
      attributeAssignValue.setValueMemberId(value);
      AttributeAssignValuesResult currentResult = internal_addValues(GrouperUtil.toSet(attributeAssignValue), true);
      attributeAssignValuesResult.setChanged(attributeAssignValuesResult.isChanged() || currentResult.isChanged());
      attributeAssignValuesResult.getAttributeAssignValueResults().addAll(currentResult.getAttributeAssignValueResults());
    }
    return attributeAssignValuesResult;
  }
  
  /**
   * 
   * @param attributeAssignValues
   * @param checkSecurity 
   * @return result
   */
  public AttributeAssignValuesResult internal_addValues(Collection<AttributeAssignValue> attributeAssignValues, boolean checkSecurity) {

    if (checkSecurity) {
      //make sure can edit
      this.attributeAssign.retrieveAttributeAssignable()
        .getAttributeDelegate().assertCanUpdateAttributeDefName(
            this.attributeAssign.getAttributeDefName());
    }
    
    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    if (!attributeDef.isMultiValued()) {
      
      Set<AttributeAssignValue> existingSet = this.internal_retrieveValues(false, true);
      if (GrouperUtil.nonNull(existingSet).size() + attributeAssignValues.size() > 1) {
        throw new RuntimeException("Cannot add multiple values " +
        		"to a single valued attribute: " + this.attributeAssign.getAttributeDefName());
      }
    }
    
    Set<AttributeAssignValueResult> attributeAssignValueResults = new LinkedHashSet<AttributeAssignValueResult>();
    
    for (AttributeAssignValue attributeAssignValue : attributeAssignValues) {
      
      if (!attributeAssignValue.getCurrentAssignValueType().compatibleWith(attributeDef.getValueType())) {
        throw new RuntimeException("Types not compatible: " 
            + attributeAssignValue.getCurrentAssignValueType() + ", " + attributeDef.getValueType()
            + ", " + attributeDef);
      }
      
      attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
      
      attributeAssignValue.saveOrUpdate();
      
      attributeAssignValueResults.add(new AttributeAssignValueResult(true, false, attributeAssignValue));
    }
    
    return new AttributeAssignValuesResult(attributeAssignValueResults.size() > 0, attributeAssignValueResults);
  }
  
  /**
   * remove this value of any type
   * @param value
   * @return the strings that were deleted
   */
  public AttributeAssignValueResult deleteValue(String value) {
    return deleteValuesAnyType(GrouperUtil.toSet(value)).getAttributeAssignValueResults().iterator().next();
  }
  
  /**
   * remove this value of integer type
   * @param value
   * @return the strings that were deleted
   */
  public AttributeAssignValueResult deleteValueInteger(Long value) {
    return deleteValuesInteger(GrouperUtil.toSet(value)).getAttributeAssignValueResults().iterator().next();
  }
  
  /**
   * remove this value of floating type
   * @param value
   * @return the strings that were deleted
   */
  public AttributeAssignValueResult deleteValueFloating(Double value) {
    return deleteValuesFloating(GrouperUtil.toSet(value)).getAttributeAssignValueResults().iterator().next();
  }
  
  /**
   * remove this value of string type
   * @param value
   * @return the strings that were deleted
   */
  public AttributeAssignValueResult deleteValueString(String value) {
    return deleteValuesString(GrouperUtil.toSet(value)).getAttributeAssignValueResults().iterator().next();
  }
  
  /**
   * remove this value of any type
   * @param value
   * @return the strings that were deleted
   */
  public AttributeAssignValueResult deleteValueTimestamp(Timestamp value) {
    return deleteValuesTimestamp(GrouperUtil.toSet(value)).getAttributeAssignValueResults().iterator().next();
  }
  
  /**
   * remove this value of member type
   * @param value
   * @return the strings that were deleted
   */
  public AttributeAssignValueResult deleteValueMember(Member value) {
    return deleteValuesMember(GrouperUtil.toSet(value)).getAttributeAssignValueResults().iterator().next();
  }
  
  /**
   * remove this value of member id type
   * @param value
   * @return the strings that were deleted
   */
  public AttributeAssignValueResult deleteValueMember(String value) {
    return deleteValuesMemberIds(GrouperUtil.toSet(value)).getAttributeAssignValueResults().iterator().next();
  }
  
  /**
   * remove this value of integer type
   * @param values
   * @return the strings that were deleted
   */
  public AttributeAssignValuesResult deleteValuesInteger(Collection<Long> values) {
    
    //make sure can edit
    this.attributeAssign.retrieveAttributeAssignable()
      .getAttributeDelegate().assertCanUpdateAttributeDefName(
          this.attributeAssign.getAttributeDefName());
    
    Set<Long> result = new LinkedHashSet<Long>();
    
    Set<AttributeAssignValue> attributeAssignValues = new LinkedHashSet<AttributeAssignValue>();
    for (Long value : values) {
      Set<AttributeAssignValue> foundValues = internal_findValuesInteger(value, false);
      
      if (GrouperUtil.nonNull(foundValues).size() > 0) {
        result.add(value);
        attributeAssignValues.addAll(foundValues);
      }
      
    }
    return internal_deleteValues(attributeAssignValues, false);
    
  }

  /**
   * remove this value of floating type
   * @param values
   * @return the strings that were deleted
   */
  public AttributeAssignValuesResult deleteValuesFloating(Collection<Double> values) {
    
    //make sure can edit
    this.attributeAssign.retrieveAttributeAssignable()
      .getAttributeDelegate().assertCanUpdateAttributeDefName(
          this.attributeAssign.getAttributeDefName());
    
    Set<Double> result = new LinkedHashSet<Double>();
    
    Set<AttributeAssignValue> attributeAssignValues = new LinkedHashSet<AttributeAssignValue>();
    for (Double value : values) {
      Set<AttributeAssignValue> foundValues = internal_findValuesFloating(value, false);
      
      if (GrouperUtil.nonNull(foundValues).size() > 0) {
        result.add(value);
        attributeAssignValues.addAll(foundValues);
      }
      
    }
    return internal_deleteValues(attributeAssignValues, false);
    
  }

  /**
   * remove this value of timestamp type
   * @param values
   * @return the timestamps that were deleted
   */
  public AttributeAssignValuesResult deleteValuesTimestamp(Collection<Timestamp> values) {
    
    //make sure can edit
    this.attributeAssign.retrieveAttributeAssignable()
      .getAttributeDelegate().assertCanUpdateAttributeDefName(
          this.attributeAssign.getAttributeDefName());
    
    Set<Timestamp> result = new LinkedHashSet<Timestamp>();
    
    Set<AttributeAssignValue> attributeAssignValues = new LinkedHashSet<AttributeAssignValue>();
    for (Timestamp value : values) {
      Set<AttributeAssignValue> foundValues = internal_findValuesTimestamp(value, false);
      
      if (GrouperUtil.nonNull(foundValues).size() > 0) {
        result.add(value);
        attributeAssignValues.addAll(foundValues);
      }
      
    }
    return internal_deleteValues(attributeAssignValues, false);
  }

  /**
   * replace the values.  If the values are there already, ignore, if not, add, if extra already there,
   * remove.  Note, the uuids will change if not the same and the values exist
   * @param values
   * @param deleteOrphans if ones in DB should be removed if not match
   * @return true if made changes, false if not
   */
  public AttributeAssignValuesResult assignValuesAnyType(Set<String> values, boolean deleteOrphans) {
    
    Set<AttributeAssignValue> attributeAssignValues = new LinkedHashSet<AttributeAssignValue>();
    
    for (String value : values) {
      
      AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
      attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
      attributeAssignValue.assignValue(value);
      attributeAssignValues.add(attributeAssignValue);
      
    }
    
    return assignValues(attributeAssignValues, deleteOrphans);
    
  }
  
  /**
   * replace the values.  If the values are there already, ignore, if not, add, if extra already there,
   * remove.  Note, the uuids will change if not the same and the values exist
   * @param values
   * @param deleteOrphans if ones in DB should be removed if not match
   * @return true if made changes, false if not
   */
  public AttributeAssignValuesResult assignValuesString(Set<String> values, boolean deleteOrphans) {
    
    Set<AttributeAssignValue> attributeAssignValues = new LinkedHashSet<AttributeAssignValue>();
    
    for (String value : values) {
      
      AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
      attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
      attributeAssignValue.setValueString(value);
      attributeAssignValues.add(attributeAssignValue);
      
    }
    
    return assignValues(attributeAssignValues, deleteOrphans);
    
  }
  
  /**
   * replace the values.  If the values are there already, ignore, if not, add, if extra already there,
   * remove.  Note, the uuids will change if not the same and the values exist
   * @param values
   * @param deleteOrphans if ones in DB should be removed if not match
   * @return true if made changes, false if not
   */
  public AttributeAssignValuesResult assignValuesMember(Set<Member> values, boolean deleteOrphans) {
    
    Set<String> memberIds = new LinkedHashSet<String>();
    for (Member member : GrouperUtil.nonNull(values)) {
      memberIds.add(member == null ? null : member.getUuid());
    }
    return assignValuesMemberIds(memberIds, deleteOrphans);
  }
  
  /**
   * replace the values.  If the values are there already, ignore, if not, add, if extra already there,
   * remove.  Note, the uuids will change if not the same and the values exist
   * @param values
   * @param deleteOrphans if ones in DB should be removed if not match
   * @return true if made changes, false if not
   */
  public AttributeAssignValuesResult assignValuesMemberIds(Set<String> values, boolean deleteOrphans) {
    
    Set<AttributeAssignValue> attributeAssignValues = new LinkedHashSet<AttributeAssignValue>();
    
    for (String value : values) {
      
      AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
      attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
      attributeAssignValue.setValueMemberId(value);
      attributeAssignValues.add(attributeAssignValue);
      
    }
    
    return assignValues(attributeAssignValues, deleteOrphans);
    
  }
  

  /**
   * replace the values.  If the values are there already, ignore, if not, add, if extra already there,
   * remove.  Note, the uuids will change if not the same and the values exist
   * @param values
   * @param deleteOrphans if ones in DB should be removed if not match
   * @return true if made changes, false if not
   */
  public AttributeAssignValuesResult assignValuesTimestamp(Set<Timestamp> values, boolean deleteOrphans) {
    
    Set<AttributeAssignValue> attributeAssignValues = new LinkedHashSet<AttributeAssignValue>();
    
    for (Timestamp value : values) {
      
      AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
      attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
      attributeAssignValue.setValueInteger(value == null ? null : value.getTime());
      attributeAssignValues.add(attributeAssignValue);
      
    }
    
    return assignValues(attributeAssignValues, deleteOrphans);
    
  }
  

  /**
   * replace the values.  If the values are there already, ignore, if not, add, if extra already there,
   * remove.  Note, the uuids will change if not the same and the values exist
   * @param values
   * @param deleteOrphans if ones in DB should be removed if not match
   * @return true if made changes, false if not
   */
  public AttributeAssignValuesResult assignValuesFloating(Set<Double> values, boolean deleteOrphans) {
    
    Set<AttributeAssignValue> attributeAssignValues = new LinkedHashSet<AttributeAssignValue>();
    
    for (Double value : values) {
      
      AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
      attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
      attributeAssignValue.setValueFloating(value);
      attributeAssignValues.add(attributeAssignValue);
      
    }
    
    return assignValues(attributeAssignValues, deleteOrphans);
    
  }
  

  
  /**
   * replace the values.  If the values are there already, ignore, if not, add, if extra already there,
   * remove.  Note, the uuids will change if not the same and the values exist
   * @param values
   * @param deleteOrphans if ones in DB should be removed if not match
   * @return true if made changes, false if not
   */
  public AttributeAssignValuesResult assignValuesInteger(Set<Long> values, boolean deleteOrphans) {
    
    Set<AttributeAssignValue> attributeAssignValues = new LinkedHashSet<AttributeAssignValue>();
    
    for (Long value : values) {
      
      AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
      attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
      attributeAssignValue.setValueInteger(value);
      attributeAssignValues.add(attributeAssignValue);
      
    }
    
    return assignValues(attributeAssignValues, deleteOrphans);
    
  }
  
  /**
   * replace the values.  If the values are there already, ignore, if not, add, if extra already there,
   * remove.  Note, the uuids will change if not the same and the values exist
   * @param attributeAssignValues
   * @param deleteOrphans if ones in DB should be removed if not match
   * @return true if made changes, false if not
   */
  public AttributeAssignValuesResult assignValues(Set<AttributeAssignValue> attributeAssignValues, boolean deleteOrphans) {
    
    Set<AttributeAssignValueResult> attributeAssignValueResults = new LinkedHashSet<AttributeAssignValueResult>();
    
    //make sure can edit
    this.attributeAssign.retrieveAttributeAssignable()
      .getAttributeDelegate().assertCanUpdateAttributeDefName(
          this.attributeAssign.getAttributeDefName());

    //get all values
    Set<AttributeAssignValue> existingValues = new LinkedHashSet<AttributeAssignValue>(
        GrouperUtil.nonNull(this.internal_retrieveValues(false, false, false)));
    
    //remake so we dont destroy the input
    attributeAssignValues = new LinkedHashSet<AttributeAssignValue>(GrouperUtil.nonNull(attributeAssignValues));
    
    boolean changed = false;
    
    //lets remove by id
    Iterator<AttributeAssignValue> existingIterator = existingValues.iterator();
    while (existingIterator.hasNext()) {
      AttributeAssignValue currentExisting = existingIterator.next();
      Iterator<AttributeAssignValue> newValuesIterator = attributeAssignValues.iterator();
      while (newValuesIterator.hasNext()) {
        AttributeAssignValue currentNew = newValuesIterator.next();
        if (StringUtils.equals(currentNew.getId(), currentExisting.getId())) {
          existingIterator.remove();
          newValuesIterator.remove();
          
          //make sure values are same
          if (!currentExisting.sameValue(currentNew)) {
            currentExisting.assignValue(currentNew);
            currentExisting.saveOrUpdate();
            changed = true;
            attributeAssignValueResults.add(new AttributeAssignValueResult(true, false, currentExisting));
          } else {
            attributeAssignValueResults.add(new AttributeAssignValueResult(false, false, currentExisting));
          }
        }
      }
    }
    
    //lets remove by value
    existingIterator = existingValues.iterator();
    while (existingIterator.hasNext()) {
      AttributeAssignValue currentExisting = existingIterator.next();
      Iterator<AttributeAssignValue> newValuesIterator = attributeAssignValues.iterator();
      while (newValuesIterator.hasNext()) {
        AttributeAssignValue currentNew = newValuesIterator.next();
        if (currentExisting.sameValue(currentNew)) {
          existingIterator.remove();
          newValuesIterator.remove();
          attributeAssignValueResults.add(new AttributeAssignValueResult(false, false, currentExisting));
        }
      }
    }
    
    if (deleteOrphans) {
      //remove the ones that shouldnt be there
      if (GrouperUtil.length(existingValues) > 0) {
        AttributeAssignValuesResult attributeAssignValuesResult = internal_deleteValues(existingValues, false);
        changed = true;
        attributeAssignValueResults.addAll(attributeAssignValuesResult.getAttributeAssignValueResults());
      }
    }
    
    //add new ones
    if (GrouperUtil.length(attributeAssignValues) > 0) {
      AttributeAssignValuesResult attributeAssignValuesResult = internal_addValues(attributeAssignValues, false);
      changed = true;
      attributeAssignValueResults.addAll(attributeAssignValuesResult.getAttributeAssignValueResults());
    }
    return new AttributeAssignValuesResult(changed, attributeAssignValueResults);
  }
  
  /**
   * remove this value of string type
   * @param values
   * @return the strings that were deleted
   */
  public AttributeAssignValuesResult deleteValuesString(Collection<String> values) {
    
    //make sure can edit
    this.attributeAssign.retrieveAttributeAssignable()
      .getAttributeDelegate().assertCanUpdateAttributeDefName(
          this.attributeAssign.getAttributeDefName());
    
    Set<String> result = new LinkedHashSet<String>();
    
    Set<AttributeAssignValue> attributeAssignValues = new LinkedHashSet<AttributeAssignValue>();
    for (String value : values) {
      Set<AttributeAssignValue> foundValues = internal_findValuesString(value, false);
      
      if (GrouperUtil.nonNull(foundValues).size() > 0) {
        result.add(value);
        attributeAssignValues.addAll(foundValues);
      }
      
    }
    return internal_deleteValues(attributeAssignValues, false);
  }

  /**
   * remove this value of string type
   * @param memberIds
   * @return the strings that were deleted
   */
  public AttributeAssignValuesResult deleteValuesMemberIds(Collection<String> memberIds) {
    
    //make sure can edit
    this.attributeAssign.retrieveAttributeAssignable()
      .getAttributeDelegate().assertCanUpdateAttributeDefName(
          this.attributeAssign.getAttributeDefName());
    
    Set<String> result = new LinkedHashSet<String>();
    
    Set<AttributeAssignValue> attributeAssignValues = new LinkedHashSet<AttributeAssignValue>();
    for (String memberId : memberIds) {
      Set<AttributeAssignValue> foundValues = internal_findValuesMember(memberId, false);
      
      if (GrouperUtil.nonNull(foundValues).size() > 0) {
        result.add(memberId);
        attributeAssignValues.addAll(foundValues);
      }
      
    }
    return internal_deleteValues(attributeAssignValues, false);
    
  }

  /**
   * remove this value of member type
   * @param members
   * @return the strings that were deleted
   */
  public AttributeAssignValuesResult deleteValuesMember(Collection<Member> members) {
    
    //make sure can edit
    this.attributeAssign.retrieveAttributeAssignable()
      .getAttributeDelegate().assertCanUpdateAttributeDefName(
          this.attributeAssign.getAttributeDefName());
    
    Set<Member> result = new LinkedHashSet<Member>();
    
    Set<AttributeAssignValue> attributeAssignValues = new LinkedHashSet<AttributeAssignValue>();
    for (Member member : members) {
      Set<AttributeAssignValue> foundValues = internal_findValuesMember(member, false);
      
      if (GrouperUtil.nonNull(foundValues).size() > 0) {
        result.add(member);
        attributeAssignValues.addAll(foundValues);
      }
      
    }
    return internal_deleteValues(attributeAssignValues, false);
  }

  /**
   * remove this value of any type
   * @param values
   * @return the strings that were deleted
   */
  public AttributeAssignValuesResult deleteValuesAnyType(Collection<String> values) {

    //make sure can edit
    this.attributeAssign.retrieveAttributeAssignable()
      .getAttributeDelegate().assertCanUpdateAttributeDefName(
          this.attributeAssign.getAttributeDefName());

    Set<String> result = new LinkedHashSet<String>();
    
    Set<AttributeAssignValue> attributeAssignValues = new LinkedHashSet<AttributeAssignValue>();
    for (String value : values) {
      Set<AttributeAssignValue> foundValues = internal_findValues(value, false);
      
      if (GrouperUtil.nonNull(foundValues).size() > 0) {
        result.add(value);
        attributeAssignValues.addAll(foundValues);
      }
      
    }
    return internal_deleteValues(attributeAssignValues, false);
    
  }
  
  /**
   * remove this value
   * @param attributeAssignValue
   * @return the result
   */
  public AttributeAssignValueResult deleteValue(AttributeAssignValue attributeAssignValue) {
    return this.internal_deleteValues(GrouperUtil.toSet(attributeAssignValue), true)
      .getAttributeAssignValueResults().iterator().next();
  }
  
  /**
   * remove this value
   * @param attributeAssignValues
   * @return result
   */
  public AttributeAssignValuesResult deleteValues(Collection<AttributeAssignValue> attributeAssignValues) {
    return this.internal_deleteValues(attributeAssignValues, true);
  }
  
  /**
   * find this value (return one if more than one)
   * @param attributeAssignValue to find (by value)
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValue(AttributeAssignValue attributeAssignValue) {
    return GrouperUtil.collectionPopOne(findValues(attributeAssignValue), false);
  }
  
  /**
   * find these values
   * @param attributeAssignValue to find (by value)
   * @return the value if found, or null if not
   */
  public Set<AttributeAssignValue> findValues(AttributeAssignValue attributeAssignValue) {
    return this.internal_findValues(attributeAssignValue, true);
  }
  
  /**
   * find this value
   * @param value to find (any type)
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValue(String value) {
    return GrouperUtil.collectionPopOne(findValues(value), false);
  }
  
  /**
   * find this value
   * @param value to find (any type)
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> findValues(String value) {
    return internal_findValues(value, true);
  }
  
  /**
   * find this value
   * @param value to find (any type)
   * @param checkSecurity 
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> internal_findValues(String value, boolean checkSecurity) {
    
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setAttributeAssignId(this.attributeAssign.getId());
    attributeAssignValue.assignValue(value);
    
    return this.internal_findValues(attributeAssignValue, checkSecurity);
  }
  
  /**
   * find this value
   * @param value to find (floating type)
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValueFloating(Double value) {
    return GrouperUtil.collectionPopOne(findValuesFloating(value), false);
  }

  /**
   * find this value
   * @param value to find (floating type)
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> findValuesFloating(Double value) {
    return internal_findValuesFloating(value, true);
  }
  
  /**
   * find this value
   * @param value to find (floating type)
   * @param checkSecurity 
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> internal_findValuesFloating(Double value, boolean checkSecurity) {
    
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setValueFloating(value);
    
    return this.internal_findValues(attributeAssignValue, checkSecurity);
  }
  
  /**
   * find this value
   * @param value to find (integer type)
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValueInteger(Long value) {
    return GrouperUtil.collectionPopOne(findValuesInteger(value), false);
  }
  
  /**
   * find this value
   * @param value to find (string type)
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValueString(String value) {
    return GrouperUtil.collectionPopOne(findValuesString(value), false);
  }
  
  /**
   * find these values
   * @param value to find (integer type)
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> findValuesInteger(Long value) {
    return internal_findValuesInteger(value, true);
  }
  
  /**
   * find these values
   * @param value to find (string type)
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> findValuesString(String value) {
    return internal_findValuesString(value, true);
  }
  
  /**
   * find these values
   * @param value to find (integer type)
   * @param checkSecurity
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> internal_findValuesInteger(Long value, boolean checkSecurity) {
    
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setValueInteger(value);
    
    return this.internal_findValues(attributeAssignValue, checkSecurity);
  }
  
  /**
   * find these values
   * @param value to find (string type)
   * @param checkSecurity
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> internal_findValuesString(String value, boolean checkSecurity) {
    
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setValueString(value);
    
    return this.internal_findValues(attributeAssignValue, checkSecurity);
  }
  
  /**
   * find this value
   * @param value to find (member type)
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValueMember(String value) {
    return GrouperUtil.collectionPopOne(findValuesMember(value), false);
  }
  
  /**
   * find this value
   * @param value to find (member type)
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> findValuesMember(String value) {
    return internal_findValuesMember(value, true);
  }
  
  /**
   * find this value
   * @param value to find (member type)
   * @param checkSecurity
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> internal_findValuesMember(String value, boolean checkSecurity) {
    
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setValueMemberId(value);
    
    return this.internal_findValues(attributeAssignValue, checkSecurity);
  }
  
  /**
   * find this value
   * @param value to find (timestamp type)
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValueTimestamp(Timestamp value) {
    return GrouperUtil.collectionPopOne(findValuesTimestamp(value), false);
  }
  
  /**
   * find these values
   * @param value to find (timestamp type)
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> findValuesTimestamp(Timestamp value) {
    return internal_findValuesTimestamp(value, true);
  }
  
  /**
   * find these values
   * @param value to find (timestamp type)
   * @param checkSecurity 
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> internal_findValuesTimestamp(Timestamp value, boolean checkSecurity) {
    
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setValueInteger(value == null ? null : value.getTime());
    
    return this.internal_findValues(attributeAssignValue, checkSecurity);
  }
  
  /**
   * find this value
   * @param value to find (floating type)
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> findValuesMember(Member value) {
    return internal_findValuesMember(value, true);
  }
  
  /**
   * find this value
   * @param value to find (floating type)
   * @param checkSecurity 
   * @return the value if found, or empty if not
   */
  public Set<AttributeAssignValue> internal_findValuesMember(Member value, boolean checkSecurity) {
    return internal_findValuesMember(value == null ? null : value.getUuid(), checkSecurity);
  }
  
  /**
   * find this value
   * @param value to find (floating type)
   * @return the value if found, or null if not
   */
  public AttributeAssignValue findValueMember(Member value) {
    
    return findValueMember(value == null ? null : value.getUuid());

  }
  
  /**
   * find this value, must be the right value type
   * @param attributeAssignValue to find (by value)
   * @param checkSecurity 
   * @return the values if found, or empty if not
   */
  public Set<AttributeAssignValue> internal_findValues(AttributeAssignValue attributeAssignValue, boolean checkSecurity) {
    
    if (checkSecurity) {
      //make sure can edit
      this.attributeAssign.retrieveAttributeAssignable()
        .getAttributeDelegate().assertCanReadAttributeDefName(
            this.attributeAssign.getAttributeDefName());
    }
    
    Set<AttributeAssignValue> allValues = this.internal_retrieveValues(false, true);
    
    Set<AttributeAssignValue> result = new LinkedHashSet<AttributeAssignValue>();
    
    for (AttributeAssignValue current : allValues) {
      
      if (current.sameValue(attributeAssignValue)) {
        result.add(current);
      }
      
    }
    
    return result;
  }
  
  /**
   * remove these values
   * @param attributeAssignValues
   * @param checkSecurity 
   * @return the result
   */
  public AttributeAssignValuesResult internal_deleteValues(Collection<AttributeAssignValue> attributeAssignValues, boolean checkSecurity) {

    Set<AttributeAssignValueResult> attributeAssignValueResults = new LinkedHashSet<AttributeAssignValueResult>();

    if (checkSecurity) {
      //make sure can edit
      this.attributeAssign.retrieveAttributeAssignable()
        .getAttributeDelegate().assertCanUpdateAttributeDefName(
            this.attributeAssign.getAttributeDefName());
    }
    
    boolean changed = false;
    
    for (AttributeAssignValue current : attributeAssignValues) {
      changed = true;
      attributeAssignValueResults.add(new AttributeAssignValueResult(true, true, current));
      current.delete();
    }
    
    return new AttributeAssignValuesResult(changed, attributeAssignValueResults);
  }
  
  /**
   * get the values for an assignment or empty set if none
   * @return the values
   */
  public AttributeAssignValue retrieveValue() {
    Set<AttributeAssignValue> results = this.retrieveValues();
    
    if (GrouperUtil.length(results) > 1) {
      throw new RuntimeException("Found multiple results, but calling the single result method! " 
          + this.attributeAssign.getAttributeDefName().getName() + ", " + this + ", size: " 
          + GrouperUtil.length(results));
    }
    
    if (GrouperUtil.length(results) == 1) {
      return results.iterator().next();
    }
    
    return null;
    
  }

  /**
   * get the string value
   * @return the string value
   */
  public String retrieveValueString() {
    AttributeAssignValue attributeAssignValue = retrieveValue();
    return convertToString(attributeAssignValue);
  }

  /**
   * convert to string
   * @param attributeAssignValue
   * @return string
   */
  private String convertToString(AttributeAssignValue attributeAssignValue) {
    if (attributeAssignValue == null) {
      return null;
    }
    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    switch (attributeDef.getValueType()) {
      case timestamp:
        Long timestampLong = attributeAssignValue.getValueInteger();
        if (timestampLong == null) {
          return null;
        }
        return AttributeAssignValue.dateToString(new Timestamp(timestampLong));
      case string:
        return attributeAssignValue.getValueString();
      case floating:
        Double valueFloating = attributeAssignValue.getValueFloating();
        return valueFloating == null ? null : Double.toString(valueFloating);
      case integer:
        Long valueLong = attributeAssignValue.getValueInteger();
        return valueLong == null ? null : Double.toString(valueLong);
      case marker:
        return null;
      case memberId:
        return attributeAssignValue.getValueMemberId();
      default:
        throw new RuntimeException("Not expecting type: " + attributeDef.getValueType());
    }
  }
  
  /**
   * get the integer value (must be integer type)
   * @return the integer value
   */
  public Long retrieveValueInteger() {
    AttributeAssignValue attributeAssignValue = retrieveValue();
    return convertToInteger(attributeAssignValue); 
  }

  /**
   * convert to integer
   * @param attributeAssignValue
   * @return integer
   */
  private Long convertToInteger(AttributeAssignValue attributeAssignValue) {
    if (attributeAssignValue == null) {
      return null;
    }
    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    if (attributeDef.getValueType() == AttributeDefValueType.integer) {
      return attributeAssignValue.getValueInteger();
    }
    throw new RuntimeException("Expecting type integer, but was: " + attributeDef.getValueType() 
        + ", " + this.attributeAssign);
  }
  
  /**
   * get the floating value (must be floating type)
   * @return the floating value
   */
  public Double retrieveValueFloating() {
    AttributeAssignValue attributeAssignValue = retrieveValue();
    return convertToFloating(attributeAssignValue); 
  }

  /**
   * get the floating values (must be floating type)
   * @return the floating values
   */
  public List<Double> retrieveValuesFloating() {
    Set<AttributeAssignValue> attributeAssignValues = retrieveValues();
    List<Double> result = new ArrayList<Double>();
    for (AttributeAssignValue attributeAssignValue : GrouperUtil.nonNull(attributeAssignValues)) {
      Double doubleValue = convertToFloating(attributeAssignValue);
      if (doubleValue != null) {
        result.add(doubleValue);
      }
    }
    Collections.sort(result);
    return result; 
  }

  /**
   * get the integer values (must be integer type)
   * @return the integer values
   */
  public List<Long> retrieveValuesInteger() {
    Set<AttributeAssignValue> attributeAssignValues = retrieveValues();
    List<Long> result = new ArrayList<Long>();
    for (AttributeAssignValue attributeAssignValue : GrouperUtil.nonNull(attributeAssignValues)) {
      Long longValue = convertToInteger(attributeAssignValue);
      if (longValue != null) {
        result.add(longValue);
      }
    }
    Collections.sort(result);
    return result; 
  }

  /**
   * get the member values (must be member type)
   * @return the member values
   */
  public List<Member> retrieveValuesMember() {
    Set<AttributeAssignValue> attributeAssignValues = retrieveValues();
    List<Member> result = new ArrayList<Member>();
    for (AttributeAssignValue attributeAssignValue : GrouperUtil.nonNull(attributeAssignValues)) {
      Member member = convertToMember(attributeAssignValue);
      if (member != null) {
        result.add(member);
      }
    }
    Collections.sort(result);
    return result; 
  }
  
  /**
   * get the member values (must be member type)
   * @return the member values
   */
  public List<String> retrieveValuesMemberId() {
    List<String> result = new ArrayList<String>();
    List<Member> members = retrieveValuesMember();
    for (Member member : GrouperUtil.nonNull(members)) {
      result.add(member.getUuid());
    }
    Collections.sort(result);
    return result;
  }
  
  /**
   * get the timestamp value (must be timestamp type)
   * @return the timestamp value
   */
  public List<Timestamp> retrieveValuesTimestamp() {
    Set<AttributeAssignValue> attributeAssignValues = retrieveValues();
    List<Timestamp> result = new ArrayList<Timestamp>();
    for (AttributeAssignValue attributeAssignValue : GrouperUtil.nonNull(attributeAssignValues)) {
      Timestamp timestampValue = convertToTimestamp(attributeAssignValue);
      if (timestampValue != null) {
        result.add(timestampValue);
      }
    }
    Collections.sort(result);
    return result; 
  }

  /**
   * get the string values
   * @return the string values
   */
  public List<String> retrieveValuesString() {
    Set<AttributeAssignValue> attributeAssignValues = retrieveValues();
    List<String> result = new ArrayList<String>();
    for (AttributeAssignValue attributeAssignValue : GrouperUtil.nonNull(attributeAssignValues)) {
      String stringValue = convertToString(attributeAssignValue);
      if (stringValue != null) {
        result.add(stringValue);
      }
    }
    Collections.sort(result);
    return result; 
  }

  
  /**
   * convert to floating
   * @param attributeAssignValue
   * @return floating
   */
  private Double convertToFloating(AttributeAssignValue attributeAssignValue) {
    if (attributeAssignValue == null) {
      return null;
    }
    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    if (attributeDef.getValueType() == AttributeDefValueType.floating) {
      return attributeAssignValue.getValueFloating();
    }
    throw new RuntimeException("Expecting type floating, but was: " + attributeDef.getValueType() 
        + ", " + this.attributeAssign);
  }
  
  /**
   * get the member value (must be member type)
   * @return the member value
   */
  public Member retrieveValueMember() {
    AttributeAssignValue attributeAssignValue = retrieveValue();
    return convertToMember(attributeAssignValue); 
  }

  /**
   * get the member id value (must be member type)
   * @return the member value
   */
  public String retrieveValueMemberId() {
    AttributeAssignValue attributeAssignValue = retrieveValue();
    return convertToMemberId(attributeAssignValue); 
  }

  /**
   * convert to member
   * @param attributeAssignValue
   * @return member
   */
  private Member convertToMember(AttributeAssignValue attributeAssignValue) {
    if (attributeAssignValue == null) {
      return null;
    }
    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    if (attributeDef.getValueType() == AttributeDefValueType.memberId) {
      String memberId = attributeAssignValue.getValueMemberId();
      if (StringUtils.isBlank(memberId)) {
        return null;
      }
      return MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, true);
    }
    throw new RuntimeException("Expecting type member, but was: " + attributeDef.getValueType() 
        + ", " + this.attributeAssign);
  }
  
  /**
   * convert to member
   * @param attributeAssignValue
   * @return member
   */
  private String convertToMemberId(AttributeAssignValue attributeAssignValue) {
    if (attributeAssignValue == null) {
      return null;
    }
    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    if (attributeDef.getValueType() == AttributeDefValueType.memberId) {
      String memberId = attributeAssignValue.getValueMemberId();
      if (StringUtils.isBlank(memberId)) {
        return null;
      }
      return memberId;
    }
    throw new RuntimeException("Expecting type member, but was: " + attributeDef.getValueType() 
        + ", " + this.attributeAssign);
  }
  
  /**
   * get the timestamp value (must be integer type)
   * @return the timestamp value
   */
  public Timestamp retrieveValueTimestamp() {
    AttributeAssignValue attributeAssignValue = retrieveValue();
    return convertToTimestamp(attributeAssignValue); 
  }

  /**
   * convert to timestamp
   * @param attributeAssignValue
   * @return timestamp
   */
  private Timestamp convertToTimestamp(AttributeAssignValue attributeAssignValue) {
    if (attributeAssignValue == null) {
      return null;
    }
    AttributeDef attributeDef = this.attributeAssign.getAttributeDef();
    if (attributeDef.getValueType() == AttributeDefValueType.timestamp) {
      Long longValue = attributeAssignValue.getValueInteger();
      if (longValue == null) {
        return null;
      }
      return new Timestamp(longValue.longValue());
    }
    throw new RuntimeException("Expecting type integer, but was: " + attributeDef.getValueType() 
        + ", " + this.attributeAssign);
  }
  

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    return new ToStringBuilder(this)
      .append( "attributeAssign", this.attributeAssign)
      .toString();
  }

  /**
   * replace values, update if possible... works for single or multi-assign
   * @param expectedAttributeAssignValues
   * @return the number of records updated
   */
  public int replaceValues(Set<AttributeAssignValue> expectedAttributeAssignValues) {
    Set<AttributeAssignValue> existingAttributeAssignValues = this.getAttributeAssignValues();

    Iterator<AttributeAssignValue> expectedAttributeAssignValueIterator = expectedAttributeAssignValues.iterator();
    
    int count = 0;
    
    // loop through expected values
    while (expectedAttributeAssignValueIterator.hasNext()) {
      
      AttributeAssignValue expectedAttributeAssignValue = expectedAttributeAssignValueIterator.next();
      
      Iterator<AttributeAssignValue> existingAttributeAssignValueIterator = existingAttributeAssignValues.iterator();
      
      //loop through existing values
      while (existingAttributeAssignValueIterator.hasNext()) {
        AttributeAssignValue existingAttributeAssignValue = existingAttributeAssignValueIterator.next();
        
        // if the same then remove both from each set
        if (GrouperUtil.equals(expectedAttributeAssignValue.getValue(), existingAttributeAssignValue.getValue())) {
          expectedAttributeAssignValueIterator.remove();
          existingAttributeAssignValueIterator.remove();
          break;
        }
      }
    }

    //see if there are changes
    if (GrouperUtil.length(existingAttributeAssignValues) > 0 || GrouperUtil.length(expectedAttributeAssignValues) > 0) {

      AttributeDefName theAttributeDefName = attributeAssign.getAttributeDefName();
      
      if (!theAttributeDefName.getAttributeDef().isMultiValued()) {
        
        if (GrouperUtil.length(expectedAttributeAssignValues) > 1) {
          throw new RuntimeException("Why assigning more than one value to a single valued attribute definition? " 
              + GrouperUtil.toStringForLog(expectedAttributeAssignValues) + ", " + theAttributeDefName.getName());  
        }
        
        if (GrouperUtil.length(expectedAttributeAssignValues) == 1) {
          count++;
          this.assignValue(expectedAttributeAssignValues.iterator().next());
        } else {
          count++;
          //must be a delete
          this.deleteValue(existingAttributeAssignValues.iterator().next());
        }
        
      } else {
        
        expectedAttributeAssignValueIterator = expectedAttributeAssignValues.iterator();
        Iterator<AttributeAssignValue> existingAttributeAssignValueIterator = existingAttributeAssignValues.iterator();
        
        // loop through expected values
        while (expectedAttributeAssignValueIterator.hasNext()) {
          
          AttributeAssignValue expectedAttributeAssignValue = expectedAttributeAssignValueIterator.next();
          
          count++;

          if (existingAttributeAssignValueIterator.hasNext()) {
            AttributeAssignValue existingAttributeAssignValue = existingAttributeAssignValueIterator.next();
            existingAttributeAssignValue.assignValue(expectedAttributeAssignValue);
            existingAttributeAssignValue.saveOrUpdate();
          } else {
            expectedAttributeAssignValue.setAttributeAssignId(attributeAssign.getId());
            expectedAttributeAssignValue.saveOrUpdate();
          }
        }
        
        //delete extra things that need to be deleted 
        while (existingAttributeAssignValueIterator.hasNext()) {
          count++;
          AttributeAssignValue attributeAssignValue = existingAttributeAssignValueIterator.next();
          attributeAssignValue.delete();
        }
      }
    }
    return count;
  }

}
