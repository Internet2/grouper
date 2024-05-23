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

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeDefFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.GrouperWsToStringCompact;
import io.swagger.annotations.ApiModelProperty;

/**
 * <pre>
 * Class to lookup an attribute def via web service
 * 
 * developers make sure each setter calls this.clearAttributeDef();
 * </pre>
 * @author mchyzer
 */
public class WsAttributeDefLookup implements GrouperWsToStringCompact {

  /**
   * integer ID for object
   */
  private String idIndex;
  
  /**
   * integer ID for object
   * @return the id
   */
  @ApiModelProperty(value = "Id index of attribute def to be found", example = "1009")
  public String getIdIndex() {
    return this.idIndex;
  }

  /**
   * integer ID for object
   * @param idIndex1
   */
  public void setIdIndex(String idIndex1) {
    this.idIndex = idIndex1;
  }

  /**
   * see if blank
   * @return true if blank
   */
  public boolean blank() {
    return StringUtils.isBlank(this.name) && StringUtils.isBlank(this.uuid)
      && this.attributeDef == null && this.attributeDefFindResult == null && StringUtils.isBlank(this.idIndex);
  }

  
  /**
   * see if this attributeDef lookup has data
   * @return true if it has data
   */
  public boolean hasData() {
    return !StringUtils.isBlank(this.name) || !StringUtils.isBlank(this.uuid)
        || !StringUtils.isBlank(this.idIndex);
  }
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(WsAttributeDefLookup.class);

  /** find the attributeDef */
  @XStreamOmitField
  private AttributeDef attributeDef = null;
  
  /** find the pit attributeDef */
  @XStreamOmitField
  private Set<PITAttributeDef> pitAttributeDefs = new LinkedHashSet<PITAttributeDef>();

  /** result of attribute def name find */
  public static enum AttributeDefFindResult {

    /** found the attributeDef */
    SUCCESS,

    /** uuid doesnt match name */
    ATTRIBUTE_DEF_UUID_DOESNT_MATCH_NAME,

    /** cant find the attributeDef */
    ATTRIBUTE_DEF_NOT_FOUND,

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
   * uuid of the attributeDef to find
   */
  private String uuid;

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the attributeDef
   */
  public AttributeDef retrieveAttributeDef() {
    return this.attributeDef;
  }
  
  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the pit attributeDefs
   */
  public Set<PITAttributeDef> retrievePITAttributeDefs() {
    return this.pitAttributeDefs;
  }

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the attributeDefFindResult, this is never null
   */
  public AttributeDefFindResult retrieveAttributeDefFindResult() {
    return this.attributeDefFindResult;
  }

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * retrieve the attributeDef for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   */
  public void retrieveAttributeDefIfNeeded(GrouperSession grouperSession) {
    this.retrieveAttributeDefIfNeeded(grouperSession, null);
  }

  /**
   * retrieve the attributeDef for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   * @param invalidQueryReason is the text to go in the WsInvalidQueryException
   * @return the attributeDef
   * @throws WsInvalidQueryException if there is a problem, and if the invalidQueryReason is set
   */
  public AttributeDef retrieveAttributeDefIfNeeded(GrouperSession grouperSession,
      String invalidQueryReason) throws WsInvalidQueryException {
    
    //see if we already retrieved
    if (this.attributeDefFindResult != null) {
      return this.attributeDef;
    }
    try {
      //assume success (set otherwise if there is a problem)
      this.attributeDefFindResult = AttributeDefFindResult.SUCCESS;

      boolean hasUuid = !StringUtils.isBlank(this.uuid);

      boolean hasName = !StringUtils.isBlank(this.name);

      boolean hasIdIndex = !StringUtils.isBlank(this.idIndex);

      //must have a name or uuid
      if (!hasUuid && !hasName && !hasIdIndex) {
        this.attributeDefFindResult = AttributeDefFindResult.INVALID_QUERY;
        if (!StringUtils.isEmpty(invalidQueryReason)) {
          throw new WsInvalidQueryException("Invalid attributeDef query for '"
              + invalidQueryReason + "', " + this);
        }
        String logMessage = "Invalid query: " + this;
        LOG.warn(logMessage);
      }

      AttributeDef theAttributeDef = null;
      
      if (hasName) {
        
        theAttributeDef = AttributeDefFinder.findByName(this.name, true);

      } else if (hasUuid) {

        theAttributeDef = AttributeDefFinder.findById(this.uuid, true);
      
      } else if (hasIdIndex) {
        
        theAttributeDef = AttributeDefFinder.findByIdIndexSecure(GrouperUtil.longValue(this.idIndex), true, new QueryOptions().secondLevelCache(false));
        
      }

      //make sure uuid matches 
      if ((hasUuid && !StringUtils.equals(this.uuid, theAttributeDef.getUuid()))
          || (hasName && !StringUtils.equals(this.name, theAttributeDef.getName()))
          || (hasIdIndex && !GrouperUtil.equals(GrouperUtil.longValue(this.idIndex), theAttributeDef.getIdIndex()))) {
        this.attributeDefFindResult = AttributeDefFindResult.ATTRIBUTE_DEF_UUID_DOESNT_MATCH_NAME;
        String error = "AttributeDef name '" + this.name + "', uuid '" + this.uuid
            + "', idIndex: " + this.idIndex + " do not match";
        if (!StringUtils.isEmpty(invalidQueryReason)) {
          throw new WsInvalidQueryException(error + " for '" + invalidQueryReason
              + "', " + this);
        }
        String logMessage = "Invalid query: " + this;
        LOG.warn(logMessage);
      }

      //success
      this.attributeDef = theAttributeDef;

    } catch (AttributeDefNotFoundException anf) {
      this.attributeDefFindResult = AttributeDefFindResult.ATTRIBUTE_DEF_NOT_FOUND;
      if (!StringUtils.isBlank(invalidQueryReason)) {
        throw new WsInvalidQueryException("Invalid attributeDef for '" + invalidQueryReason
            + "', " + this, anf);
      }
    }
    return this.attributeDef;
  }
  
  /**
   * retrieve the pit attribute defs for this lookup if not looked up yet.
   * @param invalidQueryReason is the text to go in the WsInvalidQueryException
   * @param pointInTimeFrom 
   * @param pointInTimeTo 
   * @return the pit attribute def
   * @throws WsInvalidQueryException if there is a problem, and if the invalidQueryReason is set
   */
  public Set<PITAttributeDef> retrievePITAttributeDefsIfNeeded(String invalidQueryReason, Timestamp pointInTimeFrom, 
      Timestamp pointInTimeTo) throws WsInvalidQueryException {

    //see if we already retrieved
    if (this.attributeDefFindResult != null) {
      return this.pitAttributeDefs;
    }

    //assume success (set otherwise if there is a problem)
    this.attributeDefFindResult = AttributeDefFindResult.SUCCESS;
    
    try {
      boolean hasUuid = !StringUtils.isBlank(this.uuid);

      boolean hasName = !StringUtils.isBlank(this.name);

      //must have a name or uuid
      if (!hasUuid && !hasName) {
        this.attributeDefFindResult = AttributeDefFindResult.INVALID_QUERY;
        if (!StringUtils.isEmpty(invalidQueryReason)) {
          throw new WsInvalidQueryException("Invalid point in time attributeDef query for '"
              + invalidQueryReason + "', " + this);
        }
        String logMessage = "Invalid query: " + this;
        LOG.warn(logMessage);
      }

      if (hasUuid) {        
        Set<PITAttributeDef> theAttributeDefs = PITAttributeDefFinder.findBySourceId(this.uuid, pointInTimeFrom, pointInTimeTo, true);

        //make sure name matches 
        if (hasName && !StringUtils.equals(this.name, theAttributeDefs.iterator().next().getName())) {
          this.attributeDefFindResult = AttributeDefFindResult.ATTRIBUTE_DEF_UUID_DOESNT_MATCH_NAME;
          String error = "AttributeDef name '" + this.name + "' and uuid '" + this.uuid
              + "' do not match";
          if (!StringUtils.isEmpty(invalidQueryReason)) {
            throw new WsInvalidQueryException(error + " for '" + invalidQueryReason
                + "', " + this);
          }
          String logMessage = "Invalid query: " + this;
          LOG.warn(logMessage);
        }

        //success
        this.pitAttributeDefs = new LinkedHashSet<PITAttributeDef>(theAttributeDefs);

      } else if (hasName) {
        this.pitAttributeDefs = PITAttributeDefFinder.findByName(this.name, pointInTimeFrom, pointInTimeTo, true, true);
      }

    } catch (AttributeDefNotFoundException anf) {
      this.attributeDefFindResult = AttributeDefFindResult.ATTRIBUTE_DEF_NOT_FOUND;
      if (!StringUtils.isBlank(invalidQueryReason)) {
        throw new WsInvalidQueryException("Invalid attributeDef for '" + invalidQueryReason
            + "', " + this, anf);
      }
    }
    return this.pitAttributeDefs;
  }

  /**
   * clear the attributeDef if a setter is called
   */
  private void clearAttributeDef() {
    this.attributeDef = null;
    this.pitAttributeDefs = new LinkedHashSet<PITAttributeDef>();
    this.attributeDefFindResult = null;
  }

  /** name of the attributeDef to find (includes stems, e.g. stem1:stem2:attributeDef */
  private String name;

  /** result of attributeDef find */
  @XStreamOmitField
  private AttributeDefFindResult attributeDefFindResult = null;

  /**
   * uuid of the attributeDef to find
   * @return the uuid
   */
  @ApiModelProperty(value = "Uuid of attribute def to be found", example = "a1b2c3d4")
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the attributeDef to find
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
    this.clearAttributeDef();
  }

  /**
   * name of the attributeDef to find (includes stems, e.g. stem1:stem2:attributeDef
   * @return the theName
   */
  @ApiModelProperty(value = "Name of attribute def to be found", example = "stem1:stem2:attributeDef")
  public String getName() {
    return this.name;
  }

  /**
   * name of the attributeDef to find (includes stems, e.g. stem1:stem2:attributeDef
   * @param theName the theName to set
   */
  public void setName(String theName) {
    this.name = theName;
    this.clearAttributeDef();
  }

  /**
   * convert attributeDef lookups to attributeDef ids
   * @param grouperSession
   * @param wsAttributeDefLookups
   * @param errorMessage
   * @param attributeDefType 
   * @param usePIT 
   * @param pointInTimeFrom 
   * @param pointInTimeTo 
   * @param lookupCount is an array of size one int where 1 will be added if there are records, and no change if not
   * @return the attributeDef ids
   */
  public static Set<String> convertToAttributeDefIds(GrouperSession grouperSession, 
      WsAttributeDefLookup[] wsAttributeDefLookups, StringBuilder errorMessage, AttributeDefType attributeDefType, 
      boolean usePIT, Timestamp pointInTimeFrom, Timestamp pointInTimeTo) {
    return convertToAttributeDefIds(grouperSession, wsAttributeDefLookups, errorMessage, attributeDefType, 
        usePIT, pointInTimeFrom, pointInTimeTo, new int[]{0});
  }

  /**
   * convert attributeDef lookups to attributeDef ids
   * @param grouperSession
   * @param wsAttributeDefLookups
   * @param errorMessage
   * @param attributeDefType 
   * @param usePIT 
   * @param pointInTimeFrom 
   * @param pointInTimeTo 
   * @param lookupCount is an array of size one int where 1 will be added if there are records, and no change if not
   * @return the attributeDef ids
   */
  public static Set<String> convertToAttributeDefIds(GrouperSession grouperSession, 
      WsAttributeDefLookup[] wsAttributeDefLookups, StringBuilder errorMessage, AttributeDefType attributeDefType, 
      boolean usePIT, Timestamp pointInTimeFrom, Timestamp pointInTimeTo, int[] lookupCount) {
    //get all the attributeDefs
    //we could probably batch these to get better performance.
    Set<String> attributeDefIds = null;
    if (!GrouperServiceUtils.nullArray(wsAttributeDefLookups)) {
      
      attributeDefIds = new LinkedHashSet<String>();
      int i=0;
      
      boolean foundRecords = false;
      
      for (WsAttributeDefLookup wsAttributeDefLookup : wsAttributeDefLookups) {
        
        if (wsAttributeDefLookup == null || !wsAttributeDefLookup.hasData()) {
          continue;
        }
        
        if (!foundRecords) {
          lookupCount[0]++;
          foundRecords = true;
        }
        
        if (!usePIT) {
          wsAttributeDefLookup.retrieveAttributeDefIfNeeded(grouperSession);
        } else {
          wsAttributeDefLookup.retrievePITAttributeDefsIfNeeded(null, pointInTimeFrom, pointInTimeTo);
        }
        
        AttributeDef attributeDef = wsAttributeDefLookup.retrieveAttributeDef();
        Set<PITAttributeDef> pitAttributeDefs = wsAttributeDefLookup.retrievePITAttributeDefs();
        
        if (!usePIT && attributeDef != null) {
          if (attributeDefType == null) {
            attributeDefIds.add(attributeDef.getUuid());
          } else {
            if (attributeDefType == attributeDef.getAttributeDefType()) {
              attributeDefIds.add(attributeDef.getUuid());
            } else {
              if (errorMessage.length() > 0) {
                errorMessage.append(", ");
              }
              
              errorMessage.append("Error on attributeDef index: " + i 
                  + ", expecting attributeDefType:  " + attributeDefType
                  + ", " + wsAttributeDefLookup.toStringCompact());
              
            }
          }
        } else if (usePIT && pitAttributeDefs != null && pitAttributeDefs.size() > 0) {
          for (PITAttributeDef pitAttributeDef : pitAttributeDefs) {
            if (attributeDefType == null) {
              attributeDefIds.add(pitAttributeDef.getSourceId());
            } else {
              if (attributeDefType.name().equals(pitAttributeDef.getAttributeDefTypeDb())) {
                attributeDefIds.add(pitAttributeDef.getSourceId());
              } else {
                if (errorMessage.length() > 0) {
                  errorMessage.append(", ");
                }
                
                errorMessage.append("Error on attributeDef index: " + i 
                    + ", expecting attributeDefType:  " + attributeDefType
                    + ", " + wsAttributeDefLookup.toStringCompact());
                
              }
            }
          }
        } else {
          
          if (errorMessage.length() > 0) {
            errorMessage.append(", ");
          }
          
          errorMessage.append("Error on attributeDef index: " + i + ", " + wsAttributeDefLookup.retrieveAttributeDefFindResult() + ", " + wsAttributeDefLookup.toStringCompact());
        }
        
        i++;
      }
      
    }
    return attributeDefIds;
  }

  /**
   * make sure this is an explicit toString
   * @return return a compact to string
   */
  public String toStringCompact() {
    if (!StringUtils.isBlank(this.name)) {
      return "name: " + this.name;
    }
    if (!StringUtils.isBlank(this.uuid)) {
      return "id: " + this.uuid;
    }
    if (!StringUtils.isBlank(this.idIndex)) {
      return "idIndex: " + this.idIndex;
    }
    return "blank";
  }

  /**
   * 
   */
  public WsAttributeDefLookup() {
    //blank
  }

  /**
   * @param name1 
   * @param uuid1
   */
  public WsAttributeDefLookup(String name1, String uuid1) {
    this.uuid = uuid1;
    this.setName(name1);
  }

  /**
   * @param name1 
   * @param uuid1
   * @param idIndex1
   */
  public WsAttributeDefLookup(String name1, String uuid1, String idIndex1) {
    this.uuid = uuid1;
    this.setName(name1);
    this.idIndex = idIndex1;
  }

}
