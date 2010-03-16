/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.value;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;


/**
 *
 */
public class AttributeValueDelegate {

  /**
   * reference to the attribute delegate
   */
  private AttributeAssignBaseDelegate attributeAssignBaseDelegate = null;

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
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, true);
    
    this.attributeAssignBaseDelegate.assertCanReadAttributeDefName(attributeDefName);
    
    AttributeAssign attributeAssign = this.attributeAssignBaseDelegate.retrieveAssignment(
        null, attributeDefName, false, false);
    
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
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, true);
    
    this.attributeAssignBaseDelegate.assertCanReadAttributeDefName(attributeDefName);
    
    AttributeAssign attributeAssign = this.attributeAssignBaseDelegate.retrieveAssignment(
        null, attributeDefName, false, false);
    
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
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, true);
    
    this.attributeAssignBaseDelegate.assertCanReadAttributeDefName(attributeDefName);
    
    AttributeAssign attributeAssign = this.attributeAssignBaseDelegate.retrieveAssignment(
        null, attributeDefName, false, false);
    
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
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, true);
    
    this.attributeAssignBaseDelegate.assertCanReadAttributeDefName(attributeDefName);
    
    AttributeAssign attributeAssign = this.attributeAssignBaseDelegate.retrieveAssignment(
        null, attributeDefName, false, false);
    
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
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, true);
    
    this.attributeAssignBaseDelegate.assertCanReadAttributeDefName(attributeDefName);
    
    AttributeAssign attributeAssign = this.attributeAssignBaseDelegate.retrieveAssignment(
        null, attributeDefName, false, false);
    
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
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, true);
    
    this.attributeAssignBaseDelegate.assertCanReadAttributeDefName(attributeDefName);
    
    AttributeAssign attributeAssign = this.attributeAssignBaseDelegate.retrieveAssignment(
        null, attributeDefName, false, false);
    
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
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, true);
    
    this.attributeAssignBaseDelegate.assertCanReadAttributeDefName(attributeDefName);
    
    AttributeAssign attributeAssign = this.attributeAssignBaseDelegate.retrieveAssignment(
        null, attributeDefName, false, false);
    
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
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, true);
    
    this.attributeAssignBaseDelegate.assertCanReadAttributeDefName(attributeDefName);
    
    AttributeAssign attributeAssign = this.attributeAssignBaseDelegate.retrieveAssignment(
        null, attributeDefName, false, false);
    
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
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, true);
    
    this.attributeAssignBaseDelegate.assertCanReadAttributeDefName(attributeDefName);
    
    AttributeAssign attributeAssign = this.attributeAssignBaseDelegate.retrieveAssignment(
        null, attributeDefName, false, false);
    
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
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, true);
    
    this.attributeAssignBaseDelegate.assertCanReadAttributeDefName(attributeDefName);
    
    AttributeAssign attributeAssign = this.attributeAssignBaseDelegate.retrieveAssignment(
        null, attributeDefName, false, false);
    
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
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, true);
    
    this.attributeAssignBaseDelegate.assertCanReadAttributeDefName(attributeDefName);
    
    AttributeAssign attributeAssign = this.attributeAssignBaseDelegate.retrieveAssignment(
        null, attributeDefName, false, false);
    
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
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeDefNameName, true);
    
    this.attributeAssignBaseDelegate.assertCanReadAttributeDefName(attributeDefName);
    
    AttributeAssign attributeAssign = this.attributeAssignBaseDelegate.retrieveAssignment(
        null, attributeDefName, false, false);
    
    if (attributeAssign == null) {
      return null;
    }
    
    return attributeAssign.getValueDelegate().retrieveValuesTimestamp();
  }
  

}
