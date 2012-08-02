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
 * 
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotFoundException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;

/**
 * <pre>
 * Class to lookup an attribute assignment via web service
 * 
 * developers make sure each setter calls this.clearAttributeAssignment();
 * </pre>
 * @author mchyzer
 */
public class WsAttributeAssignLookup {

  /**
   * see if blank
   * @return true if blank
   */
  public boolean blank() {
    return StringUtils.isBlank(this.uuid) && StringUtils.isBlank(this.batchIndex)
      && this.attributeAssign == null && this.attributeAssignFindResult == null;
  }

  
  /**
   * see if this attributeAssign lookup has data
   * @return true if it has data
   */
  public boolean hasData() {
    return !StringUtils.isBlank(this.uuid) || !StringUtils.isBlank(this.batchIndex);
  }
  
  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsAttributeAssignLookup.class);

  /** find the attributeAssign */
  @XStreamOmitField
  private AttributeAssign attributeAssign = null;

  /** result of attribute def name find */
  public static enum AttributeAssignResult {

    /** found the attributeAssign */
    SUCCESS,

    /** cant find the attributeAssign */
    ATTRIBUTE_ASSIGN_NOT_FOUND,

    /** incvalid query (e.g. if everything blank) */
    INVALID_QUERY;

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

  }

  /**
   * uuid of the attributeAssign to find
   */
  private String uuid;

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the attributeAssign
   */
  public AttributeAssign retrieveAttributeAssign() {
    return this.attributeAssign;
  }

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the attributeAssignFindResult, this is never null
   */
  public AttributeAssignResult retrieveAttributeAssignFindResult() {
    return this.attributeAssignFindResult;
  }

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * retrieve the attributeAssign for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   */
  public void retrieveAttributeAssignIfNeeded(GrouperSession grouperSession) {
    this.retrieveAttributeAssignIfNeeded(grouperSession, null);
  }

  /**
   * retrieve the attributeAssign for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   * @param invalidQueryReason is the text to go in the WsInvalidQueryException
   * @return the attributeAssign
   * @throws WsInvalidQueryException if there is a problem, and if the invalidQueryReason is set
   */
  public AttributeAssign retrieveAttributeAssignIfNeeded(GrouperSession grouperSession,
      String invalidQueryReason) throws WsInvalidQueryException {
    
    //see if we already retrieved
    if (this.attributeAssignFindResult != null) {
      return this.attributeAssign;
    }
    try {
      //assume success (set otherwise if there is a problem)
      this.attributeAssignFindResult = AttributeAssignResult.SUCCESS;

      boolean hasUuid = !StringUtils.isBlank(this.uuid);

      //must have a name or uuid
      if (!hasUuid) {
        this.attributeAssignFindResult = AttributeAssignResult.INVALID_QUERY;
        if (!StringUtils.isEmpty(invalidQueryReason)) {
          throw new WsInvalidQueryException("Invalid attributeAssign query for '"
              + invalidQueryReason + "', " + this);
        }
        String logMessage = "Invalid query: " + this;
        LOG.warn(logMessage);
      }

      if (hasUuid) {
        this.attributeAssign = AttributeAssignFinder.findById(this.uuid, true);

      }

    } catch (AttributeAssignNotFoundException anf) {
      this.attributeAssignFindResult = AttributeAssignResult.ATTRIBUTE_ASSIGN_NOT_FOUND;
      if (!StringUtils.isBlank(invalidQueryReason)) {
        throw new WsInvalidQueryException("Invalid attributeAssign for '" + invalidQueryReason
            + "', " + this, anf);
      }
    }
    return this.attributeAssign;
  }

  /**
   * clear the attributeAssign if a setter is called
   */
  private void clearAttributeAssign() {
    this.attributeAssign = null;
    this.attributeAssignFindResult = null;
  }

  /** result of attributeAssign find */
  @XStreamOmitField
  private AttributeAssignResult attributeAssignFindResult = null;

  /**
   * uuid of the attributeAssign to find
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the attributeAssign to find
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
    this.clearAttributeAssign();
  }

  /**
   * convert attributeAssign lookups to attributeAssign ids
   * @param grouperSession
   * @param wsAttributeAssignLookups
   * @param errorMessage
   * @param lookupCount is an array of size one int where 1 will be added if there are records, and no change if not
   * @return the membership ids
   */
  public static Set<String> convertToAttributeAssignIds(GrouperSession grouperSession, WsAttributeAssignLookup[] wsAttributeAssignLookups, StringBuilder errorMessage) {
    return convertToAttributeAssignIds(grouperSession, wsAttributeAssignLookups, errorMessage, new int[]{0});
  }

  /**
   * convert attributeAssign lookups to attributeAssign ids
   * @param grouperSession
   * @param wsAttributeAssignLookups
   * @param errorMessage
   * @param lookupCount is an array of size one int where 1 will be added if there are records, and no change if not
   * @return the membership ids
   */
  public static Set<String> convertToAttributeAssignIds(GrouperSession grouperSession, WsAttributeAssignLookup[] wsAttributeAssignLookups, StringBuilder errorMessage, int[] lookupCount) {
    //get all the attributeAssigns
    //we could probably batch these to get better performance.
    Set<String> attributeAssignIds = null;
    if (GrouperUtil.length(wsAttributeAssignLookups) > 0) {
      
      attributeAssignIds = new LinkedHashSet<String>();
      int i=0;
      
      boolean foundRecords = false;
      
      for (WsAttributeAssignLookup wsAttributeAssignLookup : wsAttributeAssignLookups) {
        
        if (wsAttributeAssignLookup == null || !wsAttributeAssignLookup.hasData()) {
          continue;
        }
        
        if (!foundRecords) {
          lookupCount[0]++;
          foundRecords = true;
        }
        
        wsAttributeAssignLookup.retrieveAttributeAssignIfNeeded(grouperSession);
        AttributeAssign attributeAssign = wsAttributeAssignLookup.retrieveAttributeAssign();
        if (attributeAssign != null) {
          attributeAssignIds.add(attributeAssign.getId());
        } else {
          
          if (errorMessage.length() > 0) {
            errorMessage.append(", ");
          }
          
          errorMessage.append("Error on attributeAssign index: " + i + ", " + wsAttributeAssignLookup.retrieveAttributeAssignFindResult() + ", " + wsAttributeAssignLookup.toStringCompact());
        }
        
        i++;
      }
      
    }
    return attributeAssignIds;
  }


  /**
   * make sure this is an explicit toString
   * @return return a compact to string
   */
  public String toStringCompact() {
    if (!StringUtils.isBlank(this.uuid)) {
      return "id: " + this.uuid;
    }
    if (!StringUtils.isBlank(this.batchIndex)) {
      return "batchIndex: " + this.batchIndex;
    }
    return "blank";
  }


  /**
   * 
   */
  public WsAttributeAssignLookup() {
    //blank
  }

  /**
   * 
   * @param uuid1
   * @param batchIndex1
   */
  public WsAttributeAssignLookup(String uuid1, String batchIndex1) {
    this.uuid = uuid1;
    this.batchIndex = batchIndex1;
  }


  /**
   * @param attributeAssign1 
   * @param uuid1
   */
  public WsAttributeAssignLookup(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * if there is a batch request, and this attribute assignment 
   * refers to a previously sent assignment, this is the index (0 indexed)
   */
  private String batchIndex;

  /**
   * if there is a batch request, and this attribute assignment 
   * refers to a previously sent assignment, this is the index (0 indexed)
   * @return the batch index
   */
  public String getBatchIndex() {
    return this.batchIndex;
  }

  /**
   * if there is a batch request, and this attribute assignment 
   * refers to a previously sent assignment, this is the index (0 indexed)
   * @param theIndex the index to set
   */
  public void setBatchIndex(String theIndex) {
    this.batchIndex = theIndex;
  }

}
