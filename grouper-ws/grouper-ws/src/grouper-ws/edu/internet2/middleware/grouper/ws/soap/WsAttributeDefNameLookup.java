/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;

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
   * 
   * @param wsAttributeDefName
   */
  public WsAttributeDefNameLookup(WsAttributeDefName wsAttributeDefName) {
    this.name = wsAttributeDefName.getName();
    this.uuid = wsAttributeDefName.getUuid();
  }
  
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
   * clear the attributeDefName if a setter is called
   */
  private void clearAttributeDefName() {
    this.attributeDefName = null;
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
