/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.rest.attribute;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueOperation;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueResult;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValuesResult;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.soap.WsAssignAttributeResult;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeAssignValue;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeAssignValueResult;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;


/**
 * logic for attribute assigning...
 */
public class WsAssignAttributeLogic {

  /**
   * deal with metadata on assignment and values and indicate in the result if changed
   * (will set to T, or leave alone)
   * @param wsAssignAttributeResult
   * @param attributeAssign
   * @param values
   * @param assignmentNotes
   * @param assignmentEnabledTime
   * @param assignmentDisabledTime
   * @param delegatable
   * @param attributeAssignValueOperation
   */
  public static void assignmentMetadataAndValues(WsAssignAttributeResult wsAssignAttributeResult, 
      AttributeAssign attributeAssign, WsAttributeAssignValue[] values,
      String assignmentNotes, Timestamp assignmentEnabledTime,
      Timestamp assignmentDisabledTime, AttributeAssignDelegatable delegatable,
      AttributeAssignValueOperation attributeAssignValueOperation) {
    
    String existingNotes = StringUtils.trimToNull(attributeAssign.getNotes());
    assignmentNotes = StringUtils.trimToNull(assignmentNotes);
    
    boolean attributeNeedsCommit = false;
    
    if (!StringUtils.equals(existingNotes, assignmentNotes)) {
      
      attributeAssign.setNotes(assignmentNotes);
      attributeNeedsCommit = true;
      
    }

    if (!GrouperUtil.equals(assignmentEnabledTime, attributeAssign.getEnabledTime())) {
      attributeAssign.setEnabledTime(assignmentEnabledTime);
      attributeNeedsCommit = true;
    }
    
    if (!GrouperUtil.equals(assignmentDisabledTime, attributeAssign.getDisabledTime())) {
      attributeAssign.setDisabledTime(assignmentDisabledTime);
      attributeNeedsCommit = true;
    }
    
    //default to false
    if (delegatable == null) {
      delegatable = AttributeAssignDelegatable.FALSE;
    }
    
    if (!GrouperUtil.equals(delegatable, attributeAssign.getAttributeAssignDelegatable())) {
      attributeAssign.setAttributeAssignDelegatable(delegatable);
      attributeNeedsCommit = true;
    }
    
    if (attributeNeedsCommit) {
      attributeAssign.saveOrUpdate();
      wsAssignAttributeResult.setChanged("T");
    }
    boolean hasValueOperation = attributeAssignValueOperation != null;
    boolean hasValues = !GrouperServiceUtils.nullArray(values);
    if (hasValueOperation && !hasValues) {
      throw new WsInvalidQueryException("If you pass attributeAssignValueOperation then you must pass values.  ");
    }
    if (!hasValueOperation && hasValues) {
      throw new WsInvalidQueryException("If you pass values then you must pass attributeAssignValueOperation.  ");
    }
    if (hasValueOperation) {
      
      //lets see if by system value, id, or formatted value
      boolean hasId = false;
      boolean allId = true;
      
      List<String> valuesAnyType = new ArrayList<String>();
      
      for (WsAttributeAssignValue wsAttributeAssignValue : values) {
        int fieldCount = 0;
        if (!StringUtils.isBlank(wsAttributeAssignValue.getId())) {
          hasId = true;
          fieldCount++;
        } else {
          allId = false;
        }
        if (!StringUtils.isBlank(wsAttributeAssignValue.getValueFormatted())) {
          fieldCount++;
          throw new WsInvalidQueryException("valueFormatted is not supported yet: " + wsAttributeAssignValue + ".  ");
        }
        if (!StringUtils.isBlank(wsAttributeAssignValue.getValueSystem())) {
          valuesAnyType.add(wsAttributeAssignValue.getValueSystem());
          fieldCount++;
        }
        if (fieldCount != 1) {
          throw new WsInvalidQueryException("A value can have id, value system, or value formatted (mutually exclusive): " + wsAttributeAssignValue + ".  ");
        }
      }

      if (hasId && !allId) {
        throw new WsInvalidQueryException("If you pass a value by value id, then all values must be by id.  ");
      }
      AttributeAssignValuesResult attributeAssignValuesResult = null;
      switch (attributeAssignValueOperation) {
        case add_value:
          if (hasId) {
            throw new WsInvalidQueryException("If you pass a value by value id, must be removing values.  ");
          }
          attributeAssignValuesResult = attributeAssign.getValueDelegate().addValuesAnyType(valuesAnyType);
          break;
        case assign_value:
          if (hasId) {
            throw new WsInvalidQueryException("If you pass a value by value id, must be removing values.  ");
          }
          attributeAssignValuesResult = attributeAssign.getValueDelegate().assignValuesAnyType(new HashSet<String>(valuesAnyType), false);
          break;
        case remove_value:
          if (hasId) {
            //delete these values by id
            Set<AttributeAssignValue> attributeAssignValueSet = new LinkedHashSet<AttributeAssignValue>();
            for (WsAttributeAssignValue wsAttributeAssignValue : values) {
              AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(wsAttributeAssignValue.getId(), true);
              attributeAssignValueSet.add(attributeAssignValue);
            }
            attributeAssignValuesResult = attributeAssign.getValueDelegate().deleteValues(attributeAssignValueSet);
          } else {
            //delete by value
            attributeAssignValuesResult = attributeAssign.getValueDelegate().deleteValuesAnyType(valuesAnyType);
            
          }
          break;
        case replace_values: 
          if (hasId) {
            throw new WsInvalidQueryException("If you pass a value by value id, must be removing values.  ");
          }
          attributeAssignValuesResult = attributeAssign.getValueDelegate().assignValuesAnyType(new HashSet<String>(valuesAnyType), true);
          
          break;
        default:
          throw new WsInvalidQueryException("Invalid attributeAssignValueOperation: " + attributeAssignValueOperation + ".  ");
      }
      
      wsAssignAttributeResult.setValuesChanged(attributeAssignValuesResult.isChanged() ? "T" : "F");
      
      Set<AttributeAssignValueResult> attributeAssignValueResultSet = attributeAssignValuesResult.getAttributeAssignValueResults();
      WsAttributeAssignValueResult[] wsAttributeAssignValueResultArray = new WsAttributeAssignValueResult[attributeAssignValueResultSet.size()];
      int i=0;
      for (AttributeAssignValueResult attributeAssignValueResult : attributeAssignValueResultSet) {
        
        wsAttributeAssignValueResultArray[i] = new WsAttributeAssignValueResult();
        wsAttributeAssignValueResultArray[i].setChanged(attributeAssignValueResult.isChanged() ? "T" : "F");
        wsAttributeAssignValueResultArray[i].setDeleted(attributeAssignValueResult.isDeleted() ? "T" : "F");
        wsAttributeAssignValueResultArray[i].setWsAttributeAssignValue(new WsAttributeAssignValue(attributeAssignValueResult.getAttributeAssignValue()));
        i++;
      }
      Arrays.sort(wsAttributeAssignValueResultArray);
      wsAssignAttributeResult.setWsAttributeAssignValueResults(wsAttributeAssignValueResultArray);
    }
    
  }
  
}
