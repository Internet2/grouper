/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

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
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeDefFinder;
import edu.internet2.middleware.grouper.pit.finder.PITAttributeDefNameFinder;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * <pre>
 * Class to lookup an attribute def name via web service
 * 
 * developers make sure each setter calls this.clearAttributeDefName();
 * </pre>
 * @author mchyzer
 */
public class WsAttributeDefNameLookup {

  /**
   * see if blank
   * @return true if blank
   */
  public boolean blank() {
    return StringUtils.isBlank(this.name) && StringUtils.isBlank(this.uuid)
      && this.attributeDefName == null && this.attributeDefNameFindResult == null;
  }

  
  /**
   * see if this attributeDefName lookup has data
   * @return true if it has data
   */
  public boolean hasData() {
    return !StringUtils.isBlank(this.name) || !StringUtils.isBlank(this.uuid);
  }
  
  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsAttributeDefNameLookup.class);

  /** find the attributeDefName */
  @XStreamOmitField
  private AttributeDefName attributeDefName = null;
  
  /** find the pit attributeDefName */
  @XStreamOmitField
  private Set<PITAttributeDefName> pitAttributeDefNames = new LinkedHashSet<PITAttributeDefName>();

  /** result of attribute def name find */
  public static enum AttributeDefNameFindResult {

    /** found the attributeDefName */
    SUCCESS,

    /** uuid doesnt match name */
    ATTRIBUTE_DEF_NAME_UUID_DOESNT_MATCH_NAME,

    /** cant find the attributeDefName */
    ATTRIBUTE_DEF_NAME_NOT_FOUND,

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
   * uuid of the attributeDefName to find
   */
  private String uuid;

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the attributeDefName
   */
  public AttributeDefName retrieveAttributeDefName() {
    return this.attributeDefName;
  }
  
  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the pit attributeDefs
   */
  public Set<PITAttributeDefName> retrievePITAttributeDefNames() {
    return this.pitAttributeDefNames;
  }

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the attributeDefNameFindResult, this is never null
   */
  public AttributeDefNameFindResult retrieveAttributeDefNameFindResult() {
    return this.attributeDefNameFindResult;
  }

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * retrieve the attributeDefName for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   */
  public void retrieveAttributeDefNameIfNeeded(GrouperSession grouperSession) {
    this.retrieveAttributeDefNameIfNeeded(grouperSession, null);
  }

  /**
   * retrieve the attributeDefName for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   * @param invalidQueryReason is the text to go in the WsInvalidQueryException
   * @return the attributeDefName
   * @throws WsInvalidQueryException if there is a problem, and if the invalidQueryReason is set
   */
  public AttributeDefName retrieveAttributeDefNameIfNeeded(GrouperSession grouperSession,
      String invalidQueryReason) throws WsInvalidQueryException {
    
    //see if we already retrieved
    if (this.attributeDefNameFindResult != null) {
      return this.attributeDefName;
    }
    try {
      //assume success (set otherwise if there is a problem)
      this.attributeDefNameFindResult = AttributeDefNameFindResult.SUCCESS;

      boolean hasUuid = !StringUtils.isBlank(this.uuid);

      boolean hasName = !StringUtils.isBlank(this.name);

      //must have a name or uuid
      if (!hasUuid && !hasName) {
        this.attributeDefNameFindResult = AttributeDefNameFindResult.INVALID_QUERY;
        if (!StringUtils.isEmpty(invalidQueryReason)) {
          throw new WsInvalidQueryException("Invalid attributeDefName query for '"
              + invalidQueryReason + "', " + this);
        }
        String logMessage = "Invalid query: " + this;
        LOG.warn(logMessage);
      }

      if (hasName) {
        
        //TODO make this more efficient
        AttributeDefName theAttributeDefName = AttributeDefNameFinder.findByName(this.name, true);

        //make sure uuid matches 
        if (hasUuid && !StringUtils.equals(this.uuid, theAttributeDefName.getId())) {
          this.attributeDefNameFindResult = AttributeDefNameFindResult.ATTRIBUTE_DEF_NAME_UUID_DOESNT_MATCH_NAME;
          String error = "AttributeDefName name '" + this.name + "' and uuid '" + this.uuid
              + "' do not match";
          if (!StringUtils.isEmpty(invalidQueryReason)) {
            throw new WsInvalidQueryException(error + " for '" + invalidQueryReason
                + "', " + this);
          }
          String logMessage = "Invalid query: " + this;
          LOG.warn(logMessage);
        }

        //success
        this.attributeDefName = theAttributeDefName;

      } else if (hasUuid) {
        this.attributeDefName = AttributeDefNameFinder.findById(this.uuid, true);
      }

    } catch (AttributeDefNameNotFoundException anf) {
      this.attributeDefNameFindResult = AttributeDefNameFindResult.ATTRIBUTE_DEF_NAME_NOT_FOUND;
      if (!StringUtils.isBlank(invalidQueryReason)) {
        throw new WsInvalidQueryException("Invalid attributeDefName for '" + invalidQueryReason
            + "', " + this, anf);
      }
    }
    return this.attributeDefName;
  }
  
  /**
   * retrieve the pit attribute def names for this lookup if not looked up yet.
   * @param invalidQueryReason is the text to go in the WsInvalidQueryException
   * @param pointInTimeFrom 
   * @param pointInTimeTo 
   * @return the pit attribute def name
   * @throws WsInvalidQueryException if there is a problem, and if the invalidQueryReason is set
   */
  public Set<PITAttributeDefName> retrievePITAttributeDefNamesIfNeeded(String invalidQueryReason, Timestamp pointInTimeFrom, 
      Timestamp pointInTimeTo) throws WsInvalidQueryException {

    //see if we already retrieved
    if (this.attributeDefNameFindResult != null) {
      return this.pitAttributeDefNames;
    }

    //assume success (set otherwise if there is a problem)
    this.attributeDefNameFindResult = AttributeDefNameFindResult.SUCCESS;
    
    try {
      boolean hasUuid = !StringUtils.isBlank(this.uuid);

      boolean hasName = !StringUtils.isBlank(this.name);

      //must have a name or uuid
      if (!hasUuid && !hasName) {
        this.attributeDefNameFindResult = AttributeDefNameFindResult.INVALID_QUERY;
        if (!StringUtils.isEmpty(invalidQueryReason)) {
          throw new WsInvalidQueryException("Invalid attributeDefName query for '"
              + invalidQueryReason + "', " + this);
        }
        String logMessage = "Invalid query: " + this;
        LOG.warn(logMessage);
      }

      if (hasUuid) {        
        PITAttributeDefName theAttributeDefName = PITAttributeDefNameFinder.findById(this.uuid, true);

        //make sure name matches 
        if (hasName && !StringUtils.equals(this.name, theAttributeDefName.getName())) {
          this.attributeDefNameFindResult = AttributeDefNameFindResult.ATTRIBUTE_DEF_NAME_UUID_DOESNT_MATCH_NAME;
          String error = "AttributeDefName name '" + this.name + "' and uuid '" + this.uuid
              + "' do not match";
          if (!StringUtils.isEmpty(invalidQueryReason)) {
            throw new WsInvalidQueryException(error + " for '" + invalidQueryReason
                + "', " + this);
          }
          String logMessage = "Invalid query: " + this;
          LOG.warn(logMessage);
        }

        //success
        this.pitAttributeDefNames = new LinkedHashSet<PITAttributeDefName>();
        this.pitAttributeDefNames.add(theAttributeDefName);

      } else if (hasName) {
        this.pitAttributeDefNames = PITAttributeDefNameFinder.findByName(this.name, pointInTimeFrom, pointInTimeTo, true, true);
      }

    } catch (AttributeDefNameNotFoundException anf) {
      this.attributeDefNameFindResult = AttributeDefNameFindResult.ATTRIBUTE_DEF_NAME_NOT_FOUND;
      if (!StringUtils.isBlank(invalidQueryReason)) {
        throw new WsInvalidQueryException("Invalid attributeDefName for '" + invalidQueryReason
            + "', " + this, anf);
      }
    }
    return this.pitAttributeDefNames;
  }

  /**
   * clear the attributeDefName if a setter is called
   */
  private void clearAttributeDefName() {
    this.attributeDefName = null;
    this.pitAttributeDefNames = new LinkedHashSet<PITAttributeDefName>();
    this.attributeDefNameFindResult = null;
  }

  /** name of the attributeDefName to find (includes stems, e.g. stem1:stem2:attributeDefNameName */
  private String name;

  /** result of attributeDefName find */
  @XStreamOmitField
  private AttributeDefNameFindResult attributeDefNameFindResult = null;

  /**
   * uuid of the attributeDefName to find
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the attributeDefName to find
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
    this.clearAttributeDefName();
  }

  /**
   * name of the attributeDefName to find (includes stems, e.g. stem1:stem2:attributeDefNameName
   * @return the theName
   */
  public String getName() {
    return this.name;
  }

  /**
   * name of the attributeDefName to find (includes stems, e.g. stem1:stem2:attributeDefNameName
   * @param theName the theName to set
   */
  public void setName(String theName) {
    this.name = theName;
    this.clearAttributeDefName();
  }

  /**
   * convert attributeDefName lookups to attributeDefName ids
   * @param grouperSession
   * @param wsAttributeDefNameLookups
   * @param errorMessage
   * @param attributeDefType 
   * @param usePIT 
   * @param pointInTimeFrom 
   * @param pointInTimeTo 
   * @param lookupCount is an array of size one int where 1 will be added if there are records, and no change if not
   * @return the attributeDef ids
   */
  public static Set<String> convertToAttributeDefNameIds(GrouperSession grouperSession, 
      WsAttributeDefNameLookup[] wsAttributeDefNameLookups, StringBuilder errorMessage, AttributeDefType attributeDefType,
      boolean usePIT, Timestamp pointInTimeFrom, Timestamp pointInTimeTo) {
    return convertToAttributeDefNameIds(grouperSession, wsAttributeDefNameLookups, errorMessage, attributeDefType, 
        usePIT, pointInTimeFrom, pointInTimeTo, new int[]{0});
  }

  /**
   * convert attributeDefName lookups to attributeDefName ids
   * @param grouperSession
   * @param wsAttributeDefNameLookups
   * @param errorMessage
   * @param attributeDefType 
   * @param usePIT 
   * @param pointInTimeFrom 
   * @param pointInTimeTo 
   * @param lookupCount is an array of size one int where 1 will be added if there are records, and no change if not
   * @return the attributeDefName ids
   */
  public static Set<String> convertToAttributeDefNameIds(GrouperSession grouperSession, 
      WsAttributeDefNameLookup[] wsAttributeDefNameLookups, StringBuilder errorMessage, 
      AttributeDefType attributeDefType, boolean usePIT, Timestamp pointInTimeFrom,
      Timestamp pointInTimeTo, int[] lookupCount) {
    //get all the attributeDefNames
    //we could probably batch these to get better performance.
    Set<String> attributeDefNameIds = null;
    if (!GrouperServiceUtils.nullArray(wsAttributeDefNameLookups)) {
      attributeDefNameIds = new LinkedHashSet<String>();
      int i=0;
      
      boolean foundRecords = false;
      
      for (WsAttributeDefNameLookup wsAttributeDefNameLookup : wsAttributeDefNameLookups) {
        
        if (wsAttributeDefNameLookup == null || !wsAttributeDefNameLookup.hasData()) {
          continue;
        }
        
        if (!foundRecords) {
          lookupCount[0]++;
          foundRecords = true;
        }
        
        if (!usePIT) {
          wsAttributeDefNameLookup.retrieveAttributeDefNameIfNeeded(grouperSession);
        } else {
          wsAttributeDefNameLookup.retrievePITAttributeDefNamesIfNeeded(null, pointInTimeFrom, pointInTimeTo);
        }
        
        AttributeDefName attributeDefName = wsAttributeDefNameLookup.retrieveAttributeDefName();
        Set<PITAttributeDefName> pitAttributeDefNames = wsAttributeDefNameLookup.retrievePITAttributeDefNames();
        
        if (!usePIT && attributeDefName != null) {
          if (attributeDefType != null) {
            AttributeDef attributeDef = attributeDefName.getAttributeDef();
            if (attributeDefType == attributeDef.getAttributeDefType()) {
              attributeDefNameIds.add(attributeDefName.getId());
            } else {
              
              if (errorMessage.length() > 0) {
                errorMessage.append(", ");
              }
              
              errorMessage.append("Error on attributeDefName index: " + i + ", expecting attributeDefType: " 
                  + attributeDefType + ", " 
                  + wsAttributeDefNameLookup.toStringCompact());
              
            }
          } else {
            attributeDefNameIds.add(attributeDefName.getId());
          }
        } else if (usePIT && pitAttributeDefNames != null && pitAttributeDefNames.size() > 0) {
          for (PITAttributeDefName pitAttributeDefName : pitAttributeDefNames) {
            if (attributeDefType == null) {
              attributeDefNameIds.add(pitAttributeDefName.getId());
            } else {
              PITAttributeDef pitAttributeDef = PITAttributeDefFinder.findById(pitAttributeDefName.getAttributeDefId(), true);
              if (attributeDefType.name().equals(pitAttributeDef.getAttributeDefTypeDb())) {
                attributeDefNameIds.add(pitAttributeDefName.getId());
              } else {
                if (errorMessage.length() > 0) {
                  errorMessage.append(", ");
                }
                
                errorMessage.append("Error on attributeDefName index: " + i + ", expecting attributeDefType: " 
                    + attributeDefType + ", " 
                    + wsAttributeDefNameLookup.toStringCompact());
                
              }
            }
          }
        } else {
          
          if (errorMessage.length() > 0) {
            errorMessage.append(", ");
          }
          
          errorMessage.append("Error on attributeDefName index: " + i + ", " 
              + wsAttributeDefNameLookup.retrieveAttributeDefNameFindResult() + ", " 
              + wsAttributeDefNameLookup.toStringCompact());
        }
        
        i++;
      }
      
    }
    return attributeDefNameIds;
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
    return "blank";
  }

  /**
   * 
   */
  public WsAttributeDefNameLookup() {
    //blank
  }

  /**
   * @param attributeDefNameName1 
   * @param uuid1
   */
  public WsAttributeDefNameLookup(String attributeDefNameName1, String uuid1) {
    this.uuid = uuid1;
    this.setName(attributeDefNameName1);
  }

}
