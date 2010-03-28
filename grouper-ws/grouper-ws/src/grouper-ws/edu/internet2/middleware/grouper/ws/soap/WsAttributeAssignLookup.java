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
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
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
    return StringUtils.isBlank(this.uuid)
      && this.attributeAssign == null && this.attributeAssignFindResult == null;
  }

  
  /**
   * see if this attributeAssign lookup has data
   * @return true if it has data
   */
  public boolean hasData() {
    return !StringUtils.isBlank(this.uuid);
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
        this.attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(this.uuid, true);
      }
      //assume success (set otherwise if there is a problem)
      this.attributeAssignFindResult = AttributeAssignResult.SUCCESS;

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
   * 
   */
  public WsAttributeAssignLookup() {
    //blank
  }

  /**
   * @param attributeAssign1 
   * @param uuid1
   */
  public WsAttributeAssignLookup(String uuid1) {
    this.uuid = uuid1;
  }

}
