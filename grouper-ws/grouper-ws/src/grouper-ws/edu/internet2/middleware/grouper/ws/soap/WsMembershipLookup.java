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
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;

/**
 * <pre>
 * Class to lookup a membership via web service.  Put in a uuid, or fill in the other fields
 * 
 * developers make sure each setter calls this.clearMembership();
 * </pre>
 * @author mchyzer
 */
public class WsMembershipLookup {

  /**
   * see if blank
   * @return true if blank
   */
  public boolean blank() {
    return !this.hasData()
      && this.membership == null && this.membershipFindResult == null;
  }

  
  /**
   * see if this membership lookup has data
   * @return true if it has data
   */
  public boolean hasData() {
    return (!StringUtils.isBlank(this.listName) && !StringUtils.isBlank(this.membershipType)
        && (this.wsAttributeDefLookup != null || this.wsGroupLookup != null 
            || this.wsStemLookup != null ) && this.wsSubjectLookup != null) 
      || !StringUtils.isBlank(this.uuid);
  }
  
  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsMembershipLookup.class);

  /** find the membership */
  @XStreamOmitField
  private Membership membership = null;

  /** result of attribute def name find */
  public static enum MembershipFindResult {

    /** found the membership */
    SUCCESS,

    /** uuid doesnt match name */
    MEMBERSHIP_UUID_DOESNT_MATCH,

    /** cant find the membership */
    MEMBERSHIP_NOT_FOUND,

    /** invalid query (e.g. if everything blank) */
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
   * uuid of the membership to find
   */
  private String uuid;
  
  /** if this is a group lookup, this is the group */
  private WsGroupLookup wsGroupLookup;
  
  /** if this is a stem lookup, this is the stem */
  private WsStemLookup wsStemLookup;
  
  /** if an attribute def membership, this is the lookup */
  private WsAttributeDefLookup wsAttributeDefLookup;
  
  /** if this is an enabled membership or not */
  private String enabled;
  
  /** name of the list, e.g. members */
  private String listName;
  
  /** list type, e.g. list, access, naming, attributeDef */
  private String listType;
  
  /** IMMEDIATE or EFFECTIVE */
  private String membershipType;
  
  /** subject lookup the membership is assigned to */
  private WsSubjectLookup wsSubjectLookup;
  
  /**
   * if this is a group lookup, this is the group
   * @return group lookup
   */
  public WsGroupLookup getWsGroupLookup() {
    return this.wsGroupLookup;
  }


  /**
   * if this is a group lookup, this is the group
   * @param wsGroupLookup1
   */
  public void setWsGroupLookup(WsGroupLookup wsGroupLookup1) {
    this.wsGroupLookup = wsGroupLookup1;
  }


  /**
   * if this is a stem lookup, this is the stem
   * @return stem lookup
   */
  public WsStemLookup getWsStemLookup() {
    return this.wsStemLookup;
  }


  /**
   * if this is a group lookup, this is the group
   * @param wsStemLookup1
   */
  public void setWsStemLookup(WsStemLookup wsStemLookup1) {
    this.wsStemLookup = wsStemLookup1;
  }


  /**
   * if an attribute def membership, this is the lookup
   * @return attribute def
   */
  public WsAttributeDefLookup getWsAttributeDefLookup() {
    return this.wsAttributeDefLookup;
  }


  /**
   * if an attribute def membership, this is the lookup
   * @param wsAttributeDefLookup1
   */
  public void setWsAttributeDefLookup(WsAttributeDefLookup wsAttributeDefLookup1) {
    this.wsAttributeDefLookup = wsAttributeDefLookup1;
  }


  /**
   * if this is an enabled membership or not
   * @return if enabled
   */
  public String getEnabled() {
    return this.enabled;
  }


  /**
   * if this is an enabled membership or not
   * @param enabled1
   */
  public void setEnabled(String enabled1) {
    this.enabled = enabled1;
  }


  /**
   * name of the list, e.g. members
   * @return list name
   */
  public String getListName() {
    return this.listName;
  }


  /**
   * name of the list, e.g. members
   * @param listName1
   */
  public void setListName(String listName1) {
    this.listName = listName1;
  }


  /**
   * list type, e.g. list, access, naming, attributeDef
   * @return list type
   */
  public String getListType() {
    return this.listType;
  }


  /**
   * list type, e.g. list, access, naming, attributeDef
   * @param listType1
   */
  public void setListType(String listType1) {
    this.listType = listType1;
  }


  /**
   * IMMEDIATE or EFFECTIVE
   * @return IMMEDIATE or EFFECTIVE
   */
  public String getMembershipType() {
    return this.membershipType;
  }


  /**
   * IMMEDIATE or EFFECTIVE
   * @param membershipType1
   */
  public void setMembershipType(String membershipType1) {
    this.membershipType = membershipType1;
  }


  /**
   * subject lookup the membership is assigned to
   * @return subject lookup
   */
  public WsSubjectLookup getWsSubjectLookup() {
    return this.wsSubjectLookup;
  }


  /**
   * subject lookup the membership is assigned to
   * @param wsSubjectLookup1
   */
  public void setWsSubjectLookup(WsSubjectLookup wsSubjectLookup1) {
    this.wsSubjectLookup = wsSubjectLookup1;
  }


  
  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the membership
   */
  public Membership retrieveMembership() {
    return this.membership;
  }

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the membershipFindResult, this is never null
   */
  public MembershipFindResult retrieveMembershipFindResult() {
    return this.membershipFindResult;
  }

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * retrieve the membership for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   */
  public void retrieveMembershipIfNeeded(GrouperSession grouperSession) {
    this.retrieveMembershipIfNeeded(grouperSession, null);
  }

  /**
   * retrieve the membership for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   * @param invalidQueryReason is the text to go in the WsInvalidQueryException
   * @return the membership
   * @throws WsInvalidQueryException if there is a problem, and if the invalidQueryReason is set
   */
  public Membership retrieveMembershipIfNeeded(GrouperSession grouperSession,
      String invalidQueryReason) throws WsInvalidQueryException {
    
    //see if we already retrieved
    if (this.membershipFindResult != null) {
      return this.membership;
    }
    try {
      boolean hasUuid = !StringUtils.isBlank(this.uuid);

      //must have a uuid or the other stuff
      if (!hasData()) {
        this.membershipFindResult = MembershipFindResult.INVALID_QUERY;
        if (!StringUtils.isEmpty(invalidQueryReason)) {
          throw new WsInvalidQueryException("Invalid membership query for '"
              + invalidQueryReason + "', " + this);
        }
        String logMessage = "Invalid query: " + this;
        LOG.warn(logMessage);
      }

//      if (hasName) {
//        
//        //TODO make this more efficient
//        Membership theMembership = MembershipFinder.findByName(this.name, true);
//
//        //make sure uuid matches 
//        if (hasUuid && !StringUtils.equals(this.uuid, theMembership.getId())) {
//          this.membershipFindResult = MembershipFindResult.MEMBERSHIP_UUID_DOESNT_MATCH;
//          String error = "Membership name '" + this.name + "' and uuid '" + this.uuid
//              + "' do not match";
//          if (!StringUtils.isEmpty(invalidQueryReason)) {
//            throw new WsInvalidQueryException(error + " for '" + invalidQueryReason
//                + "', " + this);
//          }
//          String logMessage = "Invalid query: " + this;
//          LOG.warn(logMessage);
//        }
//
//        //success
//        this.membership = theMembership;
//
//      } else if (hasUuid) {
//        this.membership = MembershipFinder.findById(this.uuid, true);
//      }
      //assume success (set otherwise if there is a problem)
      this.membershipFindResult = MembershipFindResult.SUCCESS;

    } catch (MembershipNotFoundException anf) {
      this.membershipFindResult = MembershipFindResult.MEMBERSHIP_NOT_FOUND;
      if (!StringUtils.isBlank(invalidQueryReason)) {
        throw new WsInvalidQueryException("Invalid membership for '" + invalidQueryReason
            + "', " + this, anf);
      }
    }
    return this.membership;
  }

  /**
   * clear the membership if a setter is called
   */
  private void clearMembership() {
    this.membership = null;
    this.membershipFindResult = null;
  }

  /** result of membership find */
  @XStreamOmitField
  private MembershipFindResult membershipFindResult = null;

  /**
   * uuid of the membership to find
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the membership to find
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
    this.clearMembership();
  }

  /**
   * 
   */
  public WsMembershipLookup() {
    //blank
  }

}
