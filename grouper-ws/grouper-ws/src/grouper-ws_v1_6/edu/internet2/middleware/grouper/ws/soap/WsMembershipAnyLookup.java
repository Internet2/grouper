/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.group.GroupMember;

/**
 * <pre>
 * Class to lookup a membership via web service.  Put in a uuid, or fill in the other fields
 * 
 * developers make sure each setter calls this.clearMembership();
 * </pre>
 * @author mchyzer
 */
public class WsMembershipAnyLookup {

  /** group lookup for group */
  private WsGroupLookup wsGroupLookup;
  
  /** subject lookup for subject */
  private WsSubjectLookup wsSubjectLookup;
  
  /** error message to return why this is invalid */
  @XStreamOmitField
  private String errorMessage;
  
  /**
   * group lookup for group
   * @return group lookup
   */
  public WsGroupLookup getWsGroupLookup() {
    return this.wsGroupLookup;
  }

  /**
   * group lookup for group
   * @param wsGroupLookup1
   */
  public void setWsGroupLookup(WsGroupLookup wsGroupLookup1) {
    this.wsGroupLookup = wsGroupLookup1;
    this.clearMembershipAny();
  }

  /**
   * subject lookup for subject
   * @return subject lookup
   */
  public WsSubjectLookup getWsSubjectLookup() {
    return this.wsSubjectLookup;
  }

  /**
   * subject lookup for subject
   * @param wsSubjectLookup1
   */
  public void setWsSubjectLookup(WsSubjectLookup wsSubjectLookup1) {
    this.wsSubjectLookup = wsSubjectLookup1;
    this.clearMembershipAny();
  }

  /**
   * see if blank
   * @return true if blank
   */
  public boolean blank() {
    return !this.hasData()
      && this.groupMember == null && this.membershipAnyFindResult == null;
  }
  
  /**
   * see if this membership lookup has data
   * @return true if it has data
   */
  public boolean hasData() {
    return this.wsGroupLookup != null && this.wsSubjectLookup != null 
      && this.wsGroupLookup.hasData() && this.wsSubjectLookup.hasData();
  }
  
  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsMembershipAnyLookup.class);

  /** group / subject combination */
  @XStreamOmitField
  private GroupMember groupMember = null;

  /** result of attribute def name find */
  public static enum MembershipAnyFindResult {

    /** found the membership */
    SUCCESS,

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
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * clear the membership if a setter is called
   */
  private void clearMembershipAny() {
    this.groupMember = null;
    this.membershipAnyFindResult = null;
  }

  /**
   * make sure this is an explicit toString
   * @return return a compact to string
   */
  public String toStringCompact() {
    StringBuilder result = new StringBuilder();
    
    if (this.wsGroupLookup != null) {
      result.append("Group: ").append(this.wsGroupLookup.toStringCompact());
    }
    if (this.wsSubjectLookup != null) {
      if (result.length() > 0) {
        result.append(", ");
      }
      result.append(this.wsSubjectLookup.toStringCompact());
    }
    if (result.length() == 0) {
      return "blank";
    }
    return result.toString();
  }

  /** result of membership find */
  @XStreamOmitField
  private MembershipAnyFindResult membershipAnyFindResult = null;

  /**
   * 
   */
  public WsMembershipAnyLookup() {
    //blank
  }

  /**
   * @param wsGroupLookup1
   * @param wsSubjectLookup1
   */
  public WsMembershipAnyLookup(WsGroupLookup wsGroupLookup1,
      WsSubjectLookup wsSubjectLookup1) {
    super();
    this.wsGroupLookup = wsGroupLookup1;
    this.wsSubjectLookup = wsSubjectLookup1;
  }

}
