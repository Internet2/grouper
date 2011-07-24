/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.rest.attribute;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignOperation;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueOperation;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueResult;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValuesResult;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.group.GroupMember;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignValue;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignValueResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsMembershipAnyLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsMembershipLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesResults.WsAssignAttributesResultsCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
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

  /**
   * helper method for assigning attributes
   * @param attributeAssignType
   * @param wsAttributeDefNameLookups
   * @param attributeAssignOperation
   * @param values
   * @param assignmentNotes
   * @param assignmentEnabledTime
   * @param assignmentDisabledTime
   * @param delegatable
   * @param attributeAssignValueOperation
   * @param wsAttributeAssignLookups
   * @param wsOwnerGroupLookups
   * @param wsOwnerStemLookups
   * @param wsOwnerSubjectLookups
   * @param wsOwnerMembershipLookups
   * @param wsOwnerMembershipAnyLookups
   * @param wsOwnerAttributeDefLookups
   * @param wsOwnerAttributeAssignLookups
   * @param actions
   * @param includeSubjectDetail
   * @param subjectAttributeNames
   * @param includeGroupDetail
   * @param wsAssignAttributesResults
   * @param session
   * @param params 
   * @param typeOfGroup 
   * @param attributeDefType 
   * @param attributeDefsToReplace if replacing attributeDefNames, then these 
   * are the related attributeDefs, if blank, then just do all
   * @param actionsToReplace if replacing attributeDefNames, then these are the
   * related actions, if blank, then just do all
   * @param attributeDefTypesToReplace if replacing attributeDefNames, then these are the
   * related attributeDefTypes, if blank, then just do all
   * @param disallowed is disallowed
   */
  public static void assignAttributesHelper(AttributeAssignType attributeAssignType,
      WsAttributeDefNameLookup[] wsAttributeDefNameLookups,
      AttributeAssignOperation attributeAssignOperation, WsAttributeAssignValue[] values,
      String assignmentNotes, Timestamp assignmentEnabledTime,
      Timestamp assignmentDisabledTime, AttributeAssignDelegatable delegatable,
      AttributeAssignValueOperation attributeAssignValueOperation,
      WsAttributeAssignLookup[] wsAttributeAssignLookups,
      WsGroupLookup[] wsOwnerGroupLookups, WsStemLookup[] wsOwnerStemLookups,
      WsSubjectLookup[] wsOwnerSubjectLookups,
      WsMembershipLookup[] wsOwnerMembershipLookups,
      WsMembershipAnyLookup[] wsOwnerMembershipAnyLookups,
      WsAttributeDefLookup[] wsOwnerAttributeDefLookups,
      WsAttributeAssignLookup[] wsOwnerAttributeAssignLookups, String[] actions,
      boolean includeSubjectDetail, String[] subjectAttributeNames,
      boolean includeGroupDetail, WsAssignAttributesResults wsAssignAttributesResults,
      GrouperSession session, WsParam[] params, TypeOfGroup typeOfGroup, AttributeDefType attributeDefType,
      WsAttributeDefLookup[] attributeDefsToReplace, String[] actionsToReplace, String[] attributeDefTypesToReplace, 
      Boolean disallowed) {
    
    final String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
      .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
  
    wsAssignAttributesResults.setSubjectAttributeNames(subjectAttributeNamesToRetrieve);
  
    //convert the options to a map for easy access, and validate them
    @SuppressWarnings("unused")
    Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(params);
    
    //this is for error checking
    
    int[] lookupCount = new int[]{0};
  
    StringBuilder errorMessage = new StringBuilder();


    
    if (!GrouperServiceUtils.nullArray(wsAttributeAssignLookups)) {
      if (!GrouperServiceUtils.nullArray(wsAttributeDefNameLookups)) {
        throw new WsInvalidQueryException("If you are passing in assign lookup ids to query, you cant specify attribute def names.  ");
      }
      
      if (attributeAssignOperation != AttributeAssignOperation.assign_attr 
          && attributeAssignOperation != AttributeAssignOperation.remove_attr) {
        throw new WsInvalidQueryException("If you are passing in assign lookup ids to query, " +
        		"attributeAssignOperation must be assign_attr or remove_attr.  ");
      }
      
    }
    
    //get the attributeAssignids to retrieve.  but shouldnt have owner as well as this...
    Set<String> attributeAssignIds = WsAttributeAssignLookup.convertToAttributeAssignIds(session, wsAttributeAssignLookups, errorMessage, lookupCount);
    
    //get the owner attributeAssignids to retrieve
    Set<String> ownerAttributeAssignIds = WsAttributeAssignLookup.convertToAttributeAssignIds(session, wsOwnerAttributeAssignLookups, errorMessage, lookupCount);
    
    //get the attributeDefNames to retrieve
    Set<String> attributeDefNameIds = WsAttributeDefNameLookup.convertToAttributeDefNameIds(session, wsAttributeDefNameLookups, errorMessage, attributeDefType, false, null, null);
    
    //get all the owner groups
    Set<String> ownerGroupIds = WsGroupLookup.convertToGroupIds(session, wsOwnerGroupLookups, errorMessage, typeOfGroup, false, null, null, lookupCount);
    
    //get all the owner stems
    Set<String> ownerStemIds = WsStemLookup.convertToStemIds(session, wsOwnerStemLookups, errorMessage, lookupCount);
    
    //get all the owner member ids
    Set<String> ownerMemberIds = WsSubjectLookup.convertToMemberIds(session, wsOwnerSubjectLookups, errorMessage, lookupCount);
    
    //get all the owner membership ids
    Set<String> ownerMembershipIds = WsMembershipLookup.convertToMembershipIds(session, wsOwnerMembershipLookups, errorMessage, lookupCount);
    
    //get all the owner membership any ids
    Set<MultiKey> ownerGroupMemberIds = WsMembershipAnyLookup.convertToGroupMemberIds(session, wsOwnerMembershipAnyLookups, errorMessage, typeOfGroup, lookupCount);
    
    //get all the owner attributeDef ids
    Set<String> ownerAttributeDefIds = WsAttributeDefLookup.convertToAttributeDefIds(session, wsOwnerAttributeDefLookups, errorMessage, attributeDefType, false, null, null, lookupCount);
    
    List<WsAssignAttributeResult> wsAssignAttributeResultList = new ArrayList<WsAssignAttributeResult>();
    
    if (lookupCount[0] > 1) {
      throw new WsInvalidQueryException("Why is there more than one type of lookup?  ");
    }
  
    //cant delete and do anything with values
    if (attributeAssignOperation == AttributeAssignOperation.remove_attr) {
      if (!GrouperServiceUtils.nullArray(values)) {
        throw new WsInvalidQueryException("Cant pass in values when deleting attributes.  ");
      }
      if (!StringUtils.isBlank(assignmentNotes)) {
        throw new WsInvalidQueryException("Cant pass in assignmentNotes when deleting attributes.  ");
      }
      if (assignmentEnabledTime != null) {
        throw new WsInvalidQueryException("Cant pass in assignmentEnabledTime when deleting attributes.  ");
      }
      if (assignmentDisabledTime != null) {
        throw new WsInvalidQueryException("Cant pass in assignmentDisabledTime when deleting attributes.  ");
      }
      if (delegatable != null) {
        throw new WsInvalidQueryException("Cant pass in delegatable when deleting attributes.  ");
      }
      if (attributeAssignValueOperation != null) {
        throw new WsInvalidQueryException("Cant pass in attributeAssignValueOperation when deleting attributes.  ");
      }
        
    }
  
    if (GrouperUtil.length(attributeAssignIds) > 0) {
      
      if (attributeAssignOperation == AttributeAssignOperation.replace_attrs) {
        throw new RuntimeException("Cannot replace attributes based on attributeAssign id");
      }
      
      for (WsAttributeAssignLookup wsAttributeAssignLookup : GrouperUtil.nonNull(wsAttributeAssignLookups, WsAttributeAssignLookup.class)) {
        AttributeAssign attributeAssign = wsAttributeAssignLookup.retrieveAttributeAssign();
        if (attributeAssign.getAttributeAssignType() != attributeAssignType) {
          throw new WsInvalidQueryException("attributeAssign " + attributeAssign.getId() 
              + " has attributeAssignType: " + attributeAssign.getAttributeAssignType() 
              + " but this operation was passed attributeAssignType: " + attributeAssignType + ".  ");
        }
        if (disallowed != null && (attributeAssign.isDisallowed() != disallowed)) {
          throw new WsInvalidQueryException("Cannot change the disallowed property of an assignment.  ");
        }
      }
      
      //dont pass in an action
      if (!GrouperServiceUtils.nullArray(actions)) {
        throw new WsInvalidQueryException("Cant pass in actions when using attribute assign id lookup.  ");
      }
      
      //move to results
      for (WsAttributeAssignLookup wsAttributeAssignLookup : GrouperUtil.nonNull(wsAttributeAssignLookups, WsAttributeAssignLookup.class)) {
        
        AttributeAssign attributeAssign = wsAttributeAssignLookup.retrieveAttributeAssign();
        
        //if its null the error is handled above
        if (attributeAssign != null) {
          WsAssignAttributeResult wsAssignAttributeResult = new WsAssignAttributeResult();
          wsAssignAttributeResult.setChanged("F");
          wsAssignAttributeResult.setDeleted("F");
          wsAssignAttributeResult.setValuesChanged("F");
          
          switch(attributeAssignOperation) {
            
            case assign_attr:
              
              if (disallowed) {
                throw new WsInvalidQueryException("Cant pass in disallowed if ws attribute assign lookups...  delete and re-add");
              }
              
              assignmentMetadataAndValues(wsAssignAttributeResult, 
                  attributeAssign, values, assignmentNotes, assignmentEnabledTime, 
                  assignmentDisabledTime, delegatable, attributeAssignValueOperation);
              
              break;
            case remove_attr:
              attributeAssign.delete();
              wsAssignAttributeResult.setDeleted("T");
              wsAssignAttributeResult.setChanged("T");
              break;
            default:
              throw new WsInvalidQueryException("Invalid attributeAssignOperation: " + attributeAssignOperation + ".  ");
            
          }
          WsAttributeAssign wsAttributeAssign = new WsAttributeAssign(attributeAssign);
          wsAssignAttributeResult.setWsAttributeAssigns(new WsAttributeAssign[]{wsAttributeAssign});
          wsAssignAttributeResultList.add(wsAssignAttributeResult);
          
        }
        
      }
      
    } else {
      
      //else not going by id
      
      //we are looping through actions, so dont have a null array
      if (GrouperServiceUtils.nullArray(actions)) {
        actions = new String[]{AttributeDef.ACTION_DEFAULT};
      }
    
      List<AttributeAssignable> attributeAssignableList = new ArrayList<AttributeAssignable>();
      
      switch(attributeAssignType) {
      case group:
        
        //if there is a lookup and its not about groups, then there is a problem
        if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerGroupLookups) == 0) {
          throw new WsInvalidQueryException("Group calls can only have group owner lookups.  ");
        }
        
        Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().findByUuids(ownerGroupIds, true);
        attributeAssignableList.addAll(groups);
        
        break;  
      case stem:
        
        //if there is a lookup and its not about stems, then there is a problem
        if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerStemLookups) == 0) {
          throw new WsInvalidQueryException("Stem calls can only have stem owner lookups.  ");
        }
        
        for (String stemId : GrouperUtil.nonNull(ownerStemIds)) {
          Stem stem = GrouperDAOFactory.getFactory().getStem().findByUuid(stemId, true);
          attributeAssignableList.add(stem);
        }
                    
        break;  
      case member:
        
        //if there is a lookup and its not about subjects, then there is a problem
        if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerSubjectLookups) == 0) {
          throw new WsInvalidQueryException("Subject calls can only have subject owner lookups.  ");
        }
        
        for (String memberId : GrouperUtil.nonNull(ownerMemberIds)) {
          Member member = GrouperDAOFactory.getFactory().getMember().findByUuid(memberId, true);
          attributeAssignableList.add(member);
        }
        
        
        break;  
      case imm_mem:
        
        //if there is a lookup and its not about memberships, then there is a problem
        if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerMembershipLookups) == 0) {
          throw new WsInvalidQueryException("Membership calls can only have membership owner lookups.  ");
        }
        
        for (String membershipId : GrouperUtil.nonNull(ownerMembershipIds)) {
          Membership membership = GrouperDAOFactory.getFactory().getMembership().findByUuid(membershipId, true, false);
          attributeAssignableList.add(membership);
        }
        
        
        break;  
      case any_mem:
        
        //if there is a lookup and its not about memberships, then there is a problem
        if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerMembershipAnyLookups) == 0) {
          throw new WsInvalidQueryException("MembershipAny calls can only have membershipAny owner lookups.  ");
        }
        
        for (MultiKey groupMemberId : GrouperUtil.nonNull(ownerGroupMemberIds)) {
          Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid((String)groupMemberId.getKey(0), true);
          Member member = GrouperDAOFactory.getFactory().getMember().findByUuid((String)groupMemberId.getKey(1), true);
          GroupMember groupMember = new GroupMember(group, member);
          attributeAssignableList.add(groupMember);
        }
        
        
        break;  
      case attr_def:
        
        //if there is a lookup and its not about attr def, then there is a problem
        if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerAttributeDefLookups) == 0) {
          throw new WsInvalidQueryException("attributeDef calls can only have attributeDef owner lookups.  ");
        }
  
        for (String attributeDefId : GrouperUtil.nonNull(ownerAttributeDefIds)) {
          AttributeDef attributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findById(attributeDefId, true);
          attributeAssignableList.add(attributeDef);
        }
  
        break;  
      case any_mem_asgn:
      case attr_def_asgn:
      case group_asgn:
      case imm_mem_asgn:
      case mem_asgn:
      case stem_asgn:
        
        //if there is a lookup and its not about attr assign, then there is a problem
        if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerAttributeAssignLookups) == 0) {
          throw new WsInvalidQueryException("attributeAssign calls can only have attributeAssign owner lookups.  ");
        }
  
        for (String attributeAssignId : GrouperUtil.nonNull(ownerAttributeAssignIds)) {
          AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignId, true);
          attributeAssignableList.add(attributeAssign);
        }
        
        break;
      default: 
        throw new RuntimeException("Not expecting attribute assign type: " + attributeAssignType);
      }
      
      Set<AttributeDefType> attributeDefTypeEnumsToReplace = null;
      Set<String> attributeDefIdsToReplace = null;

      if (attributeAssignOperation == AttributeAssignOperation.replace_attrs) {
        attributeDefTypeEnumsToReplace = AttributeDefType.toSet(attributeDefTypesToReplace);
        //lets filter out by attributeDefName
        if (GrouperUtil.length(attributeDefsToReplace) > 0) {
          attributeDefIdsToReplace = WsAttributeDefLookup.convertToAttributeDefIds(
              session, attributeDefsToReplace, errorMessage, attributeDefType, false, null, null);
        }
        
      }

      //loop through the assignables
      for (AttributeAssignable attributeAssignable : attributeAssignableList) {
        
        //if replacing get the assignments of what is there
        Set<AttributeAssign> existingAttributeAssignsBeforeReplace = null;
        if (attributeAssignOperation == AttributeAssignOperation.replace_attrs) {
          existingAttributeAssignsBeforeReplace = attributeAssignsBeforeReplace(actionsToReplace, attributeDefTypeEnumsToReplace,
              attributeDefIdsToReplace, attributeAssignable);
          
          
        }
        
        attributeAssignOnOwnerHelper(attributeAssignOperation, values, assignmentNotes,
            assignmentEnabledTime, assignmentDisabledTime, delegatable,
            attributeAssignValueOperation, actions, errorMessage, attributeDefNameIds,
            wsAssignAttributeResultList, attributeAssignable,
            existingAttributeAssignsBeforeReplace, disallowed);
        
        //remove existings if replacing
        if (attributeAssignOperation == AttributeAssignOperation.replace_attrs) {
          
          deleteAttributesForReplace(errorMessage, wsAssignAttributeResultList,
              attributeAssignable, existingAttributeAssignsBeforeReplace);
        }
        
      }
    }
  
    Set<String> allGroupIds = new HashSet<String>(GrouperUtil.nonNull(ownerGroupIds));
    Set<String> extraMemberIds = new HashSet<String>();
    for (MultiKey multiKey : GrouperUtil.nonNull(ownerGroupMemberIds)) {
      allGroupIds.add((String)multiKey.getKey(0));
      extraMemberIds.add((String)multiKey.getKey(1));
    }
    
    wsAssignAttributesResults.assignResult(wsAssignAttributeResultList, subjectAttributeNames);
    
    wsAssignAttributesResults.fillInAttributeDefNames(attributeDefNameIds);
    wsAssignAttributesResults.fillInAttributeDefs(ownerAttributeDefIds);
    
    wsAssignAttributesResults.fillInGroups(ownerGroupIds, includeGroupDetail);
    wsAssignAttributesResults.fillInStems(ownerStemIds);
    wsAssignAttributesResults.fillInSubjects(wsOwnerSubjectLookups, extraMemberIds, 
        includeSubjectDetail, subjectAttributeNamesToRetrieve);
    wsAssignAttributesResults.fillInMemberships(ownerMembershipIds);
    
    wsAssignAttributesResults.sortResults();
    
    if (errorMessage.length() > 0) {
      wsAssignAttributesResults.assignResultCode(WsAssignAttributesResultsCode.INVALID_QUERY);
      wsAssignAttributesResults.getResultMetadata().appendResultMessage(errorMessage.toString());
    } else {
      wsAssignAttributesResults.assignResultCode(WsAssignAttributesResultsCode.SUCCESS);
    }
    
    wsAssignAttributesResults.getResultMetadata().appendResultMessage(
        ", Found " + GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults())
        + " results.  ");
  }

  /**
   * delete attributes for replace
   * @param errorMessage
   * @param wsAssignAttributeResultList
   * @param attributeAssignable
   * @param existingAttributeAssignsBeforeReplace
   */
  private static void deleteAttributesForReplace(StringBuilder errorMessage,
      List<WsAssignAttributeResult> wsAssignAttributeResultList,
      AttributeAssignable attributeAssignable,
      Set<AttributeAssign> existingAttributeAssignsBeforeReplace) {
    for (AttributeAssign attributeAssign : existingAttributeAssignsBeforeReplace) {
      String action = null;
      AttributeDefName attributeDefName = null;
      try {
        WsAssignAttributeResult wsAssignAttributeResult = new WsAssignAttributeResult();
        wsAssignAttributeResult.setChanged("T");
        wsAssignAttributeResult.setDeleted("T");
        wsAssignAttributeResult.setValuesChanged("F");
        AttributeAssign[] attributeAssigns = null;
        AttributeAssignResult attributeAssignResult = null;
        action = attributeAssign.getAttributeAssignAction().getName();
        attributeDefName = attributeAssign.getAttributeDefName();
        attributeAssignResult = attributeAssignable.getAttributeDelegate().removeAttribute(
            action, attributeDefName);
        attributeAssigns = GrouperUtil.toArray(attributeAssignResult.getAttributeAssigns(), AttributeAssign.class);
        
        //convert the attribute assigns to ws attribute assigns
        int attributeAssignsLength = GrouperUtil.length(attributeAssigns);
        WsAttributeAssign[] wsAttributeAssigns = attributeAssignsLength == 0 ? null : new WsAttributeAssign[attributeAssignsLength];
        int i=0;
        for (AttributeAssign currentAttributeAssign : GrouperUtil.nonNull(attributeAssigns, AttributeAssign.class)) {
          wsAttributeAssigns[i] = new WsAttributeAssign(currentAttributeAssign);
          i++;
        }
        wsAssignAttributeResult.setWsAttributeAssigns(wsAttributeAssigns);
        //the result knows if it is changed or not
        if (StringUtils.equals("F", wsAssignAttributeResult.getChanged())) {
          wsAssignAttributeResult.setChanged(attributeAssignResult.isChanged() ? "T" : "F");
        }
        wsAssignAttributeResultList.add(wsAssignAttributeResult);
      } catch (Exception e) {
        //add to error and keep going
        errorMessage.append(
            "Problem with " + attributeDefName + ", action: " + action 
            + ", owner: " + attributeAssignable + ", " + ExceptionUtils.getFullStackTrace(e) + ".  ");
      }
      
    }
  }

  /**
   * pass in an owner and perform the appropriate operation
   * @param attributeAssignOperation
   * @param values
   * @param assignmentNotes
   * @param assignmentEnabledTime
   * @param assignmentDisabledTime
   * @param delegatable
   * @param attributeAssignValueOperation
   * @param actions
   * @param errorMessage
   * @param attributeDefNameIds
   * @param wsAssignAttributeResultList
   * @param attributeAssignable
   * @param existingAttributeAssignsBeforeReplace
   * @param disallowed true to make a permission disallowed
   */
  private static void attributeAssignOnOwnerHelper(
      AttributeAssignOperation attributeAssignOperation, WsAttributeAssignValue[] values,
      String assignmentNotes, Timestamp assignmentEnabledTime,
      Timestamp assignmentDisabledTime, AttributeAssignDelegatable delegatable,
      AttributeAssignValueOperation attributeAssignValueOperation, String[] actions,
      StringBuilder errorMessage, Set<String> attributeDefNameIds,
      List<WsAssignAttributeResult> wsAssignAttributeResultList,
      AttributeAssignable attributeAssignable,
      Set<AttributeAssign> existingAttributeAssignsBeforeReplace, Boolean disallowed) {

    if (GrouperUtil.length(attributeDefNameIds) == 0 
        || ((GrouperUtil.length(attributeDefNameIds) == 1 && GrouperUtil.isBlank(attributeDefNameIds.iterator().next())))) {
      throw new WsInvalidQueryException("You need to pass in an attributeDefName lookup.  ");
    }
    
    PermissionAllowed permissionAllowed = (disallowed != null && disallowed) ? PermissionAllowed.DISALLOWED 
        : PermissionAllowed.ALLOWED;
    
    for (String attributeDefNameId : GrouperUtil.nonNull(attributeDefNameIds)) {
      if (StringUtils.isBlank(attributeDefNameId)) {
        continue;
      }
      AttributeDefName attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByUuidOrName(attributeDefNameId, null, true); 
 
      for (String action : GrouperUtil.nonNull(actions, String.class)) {
        if (StringUtils.isBlank(action)) {
          continue;
        }
        try {
          WsAssignAttributeResult wsAssignAttributeResult = new WsAssignAttributeResult();
          wsAssignAttributeResult.setChanged("F");
          wsAssignAttributeResult.setDeleted("F");
          wsAssignAttributeResult.setValuesChanged("F");
          AttributeAssign[] attributeAssigns = null;
          AttributeAssignResult attributeAssignResult = null;
          switch (attributeAssignOperation) {
            case add_attr:
              if (permissionAllowed.isDisallowed()) {
                throw new WsInvalidQueryException("Cant pass in disallowed with add_attr, must be assign_attr");
              }
              attributeAssignResult = attributeAssignable.getAttributeDelegate().addAttribute(action, attributeDefName);
              attributeAssigns = new AttributeAssign[]{attributeAssignResult.getAttributeAssign()};
              assignmentMetadataAndValues(wsAssignAttributeResult, 
                  attributeAssigns[0], values, assignmentNotes, assignmentEnabledTime, assignmentDisabledTime, 
                  delegatable, attributeAssignValueOperation);
              break;
            case replace_attrs:
              removeAttributeAssignForReplace(existingAttributeAssignsBeforeReplace, attributeDefNameId, action);
              //fall through to assign
            case assign_attr:
              attributeAssignResult = attributeAssignable.getAttributeDelegate().assignAttribute(action, attributeDefName, permissionAllowed);
              attributeAssigns = new AttributeAssign[]{attributeAssignResult.getAttributeAssign()};
              assignmentMetadataAndValues(wsAssignAttributeResult, 
                  attributeAssigns[0], values, assignmentNotes, assignmentEnabledTime, assignmentDisabledTime, 
                  delegatable, attributeAssignValueOperation);
              
              break;
              
            case remove_attr:
              attributeAssignResult = attributeAssignable.getAttributeDelegate().removeAttribute(action, attributeDefName);
              attributeAssigns = GrouperUtil.toArray(attributeAssignResult.getAttributeAssigns(), AttributeAssign.class);
              wsAssignAttributeResult.setDeleted("T");
              
              break;
              
            default: 
              throw new RuntimeException("Not expecting AttributeAssignOperation: " + attributeAssignOperation);
          }
          
          //convert the attribute assigns to ws attribute assigns
          int attributeAssignsLength = GrouperUtil.length(attributeAssigns);
          WsAttributeAssign[] wsAttributeAssigns = attributeAssignsLength == 0 ? null : new WsAttributeAssign[attributeAssignsLength];
          int i=0;
          for (AttributeAssign attributeAssign : GrouperUtil.nonNull(attributeAssigns, AttributeAssign.class)) {
            wsAttributeAssigns[i] = new WsAttributeAssign(attributeAssign);
            i++;
          }
          wsAssignAttributeResult.setWsAttributeAssigns(wsAttributeAssigns);
          //the result knows if it is changed or not
          if (StringUtils.equals("F", wsAssignAttributeResult.getChanged())) {
            wsAssignAttributeResult.setChanged(attributeAssignResult.isChanged() ? "T" : "F");
          }
          wsAssignAttributeResultList.add(wsAssignAttributeResult);
        } catch (Exception e) {
          //add to error and keep going
          errorMessage.append(
              "Problem with " + attributeDefName + ", action: " + action 
              + ", owner: " + attributeAssignable + ", " + ExceptionUtils.getFullStackTrace(e) + ".  ");
        }
        
        
      }
      
    }
  }

  /**
   * if the attributeDefNameId and action already existed, then remove it
   * @param existingAttributeAssignsBeforeReplace
   * @param attributeDefNameId
   * @param action
   */
  private static void removeAttributeAssignForReplace(Set<AttributeAssign> existingAttributeAssignsBeforeReplace, String attributeDefNameId, String action) {
    if (GrouperUtil.length(existingAttributeAssignsBeforeReplace) > 0) {
      
      //note, this might replace multple
      Iterator<AttributeAssign> iterator = existingAttributeAssignsBeforeReplace.iterator();
      while (iterator.hasNext()) {
        AttributeAssign attributeAssign = iterator.next();
        if (StringUtils.equals(attributeAssign.getAttributeAssignAction().getName(), action)
            && StringUtils.equals(attributeAssign.getAttributeDefNameId(), attributeDefNameId)) {
          iterator.remove();
        }
      }
    }
  }

  /**
   * get the assignments before replace, filter out things not applicable
   * @param actionsToReplace
   * @param attributeDefTypeEnumsToReplace
   * @param attributeDefIdsToReplace
   * @param attributeAssignable
   * @return the assigns
   */
  private static Set<AttributeAssign> attributeAssignsBeforeReplace(String[] actionsToReplace,
      Set<AttributeDefType> attributeDefTypeEnumsToReplace,
      Set<String> attributeDefIdsToReplace, AttributeAssignable attributeAssignable) {

    Set<AttributeAssign> existingAttributeAssignsBeforeReplace = attributeAssignable.getAttributeDelegate().retrieveAssignments();
    
    Iterator<AttributeAssign> iterator = existingAttributeAssignsBeforeReplace.iterator();
    
    //lets filter by type
    while (iterator.hasNext()) {
      AttributeAssign current = iterator.next();
      AttributeDef attributeDef = current.getAttributeDef();
      //check attribute def type
      if (attributeDefTypeEnumsToReplace.size() > 0 
          && !attributeDefTypeEnumsToReplace.contains(attributeDef.getAttributeDefType())) {
        iterator.remove();
        continue;
      }
      
      //check attribute def
      if (GrouperUtil.length(attributeDefIdsToReplace) > 0 && !attributeDefIdsToReplace.contains(attributeDef.getId())) {
        iterator.remove();
        continue;
      }
      
      //check action
      if (GrouperUtil.length(actionsToReplace) > 0 && !GrouperUtil.contains(actionsToReplace, current.getAttributeAssignAction().getName())) {
        iterator.remove();
        continue;
      }
      
    }
    return existingAttributeAssignsBeforeReplace;
  }

  /**
   * @param wsAttributeDefNameName
   * @param wsAttributeDefNameId
   * @param attributeAssignOperation
   * @param attributeDefTypesToReplace
   * @return the def types
   */
  public static String[] retrieveAttributeDefTypesForReplace(
      String wsAttributeDefNameName, String wsAttributeDefNameId,
      AttributeAssignOperation attributeAssignOperation) {

    String[] attributeDefTypesToReplace = null;
    //get the attribute def type if replace
    if (attributeAssignOperation == AttributeAssignOperation.replace_attrs) {
      GrouperSession grouperSession = null;
      boolean startedSession = false;
      try {
        final WsAttributeDefNameLookup wsAttributeDefNameLookup = new WsAttributeDefNameLookup(wsAttributeDefNameName,wsAttributeDefNameId );
        grouperSession = GrouperSession.staticGrouperSession();
        if (grouperSession == null) {
          grouperSession = GrouperSession.startRootSession();
          startedSession = true;
        }
        attributeDefTypesToReplace = (String[])GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
          
          public Object callback(GrouperSession grouperSession2) throws GrouperSessionException {
            wsAttributeDefNameLookup.retrieveAttributeDefNameIfNeeded(grouperSession2);
            AttributeDefName attributeDefName = wsAttributeDefNameLookup.retrieveAttributeDefName();
            return new String[]{attributeDefName.getAttributeDef().getAttributeDefType().name()};
          }
        });
      } catch (Exception e) {
        LOG.debug("error", e);
        //ignore for the most part
      } finally {
        if (startedSession) {
          GrouperSession.stopQuietly(grouperSession);
        }
      }
    }
    return attributeDefTypesToReplace;
  }
  
  /**
   * @param wsAttributeDefNameName
   * @param wsAttributeDefNameId
   * @param attributeAssignOperation
   * @param attributeDefTypesToReplace
   * @return the def types
   */
  public static WsAttributeDefLookup[] retrieveAttributeDefsForReplace(
      String wsAttributeDefNameName, String wsAttributeDefNameId,
      AttributeAssignOperation attributeAssignOperation) {

    WsAttributeDefLookup[] attributeDefsToReplace = null; 
    //get the attribute def type if replace
    if (attributeAssignOperation == AttributeAssignOperation.replace_attrs) {
      GrouperSession grouperSession = null;
      boolean startedSession = false;
      try {
        final WsAttributeDefNameLookup wsAttributeDefNameLookup = new WsAttributeDefNameLookup(wsAttributeDefNameName,wsAttributeDefNameId );
        grouperSession = GrouperSession.staticGrouperSession();
        if (grouperSession == null) {
          grouperSession = GrouperSession.startRootSession();
          startedSession = true;
        }
        attributeDefsToReplace = (WsAttributeDefLookup[])GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
          
          public Object callback(GrouperSession grouperSession2) throws GrouperSessionException {
            wsAttributeDefNameLookup.retrieveAttributeDefNameIfNeeded(grouperSession2);
            AttributeDefName attributeDefName = wsAttributeDefNameLookup.retrieveAttributeDefName();
            return new WsAttributeDefLookup[]{new WsAttributeDefLookup(attributeDefName.getAttributeDef().getName(), null)};
          }
        });
      } catch (Exception e) {
        LOG.debug("error", e);
        //ignore for the most part
      } finally {
        if (startedSession) {
          GrouperSession.stopQuietly(grouperSession);
        }
      }
    }
    return attributeDefsToReplace;
  }
  
  /**
   * logger 
   */
  public static final Log LOG = LogFactory.getLog(WsAssignAttributeLogic.class);

}
