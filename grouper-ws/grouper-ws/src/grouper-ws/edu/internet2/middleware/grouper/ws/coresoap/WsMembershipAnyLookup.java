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

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.group.GroupMember;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup.GroupFindResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup.SubjectFindResult;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.GrouperWsToStringCompact;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Class to lookup a membership via web service.  Put in a uuid, or fill in the other fields
 * 
 * developers make sure each setter calls this.clearMembership();
 * </pre>
 * @author mchyzer
 */
public class WsMembershipAnyLookup implements GrouperWsToStringCompact {

  /** group lookup for group */
  private WsGroupLookup wsGroupLookup;
  
  /** subject lookup for subject */
  private WsSubjectLookup wsSubjectLookup;
  
  /** error message to return why this is invalid */
  @XStreamOmitField
  private String errorMessage;
  
  /**
   * error message to return why this is invalid
   * this is not a javabean property since we dont want it in xml
   * @return error message
   */
  public String retrieveErrorMessage() {
    return this.errorMessage;
  }

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
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the membership
   */
  public GroupMember retrieveGroupMember() {
    return this.groupMember;
  }

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the membershipFindResult, this is never null
   */
  public MembershipAnyFindResult retrieveMembershipAnyFindResult() {
    return this.membershipAnyFindResult;
  }

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * retrieve the membership any for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   */
  public void retrieveMembershipAnyIfNeeded(GrouperSession grouperSession) {
    this.retrieveMembershipAnyIfNeeded(grouperSession, null);
  }

  /**
   * retrieve the membership any for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   * @param invalidQueryReason is the text to go in the WsInvalidQueryException
   * @return the membership
   * @throws WsInvalidQueryException if there is a problem, and if the invalidQueryReason is set
   */
  public GroupMember retrieveMembershipAnyIfNeeded(GrouperSession grouperSession,
      String invalidQueryReason) throws WsInvalidQueryException {
    
    //see if we already retrieved
    if (this.membershipAnyFindResult != null) {
      return this.groupMember;
    }

    //assume success (set otherwise if there is a problem)
    this.membershipAnyFindResult = MembershipAnyFindResult.SUCCESS;

    //must have a uuid or the other stuff
    if (!hasData()) {
      this.membershipAnyFindResult = MembershipAnyFindResult.INVALID_QUERY;
      if (!StringUtils.isEmpty(invalidQueryReason)) {
        throw new WsInvalidQueryException("Invalid membership any query (doesnt have data) for '"
            + invalidQueryReason + "', " + this);
      }
      String logMessage = "Invalid query (no data): " + this;
      LOG.warn(logMessage);
    } else {
      
      this.wsGroupLookup.retrieveGroupIfNeeded(grouperSession);
      this.wsSubjectLookup.retrieveSubject();
      
      if (GroupFindResult.SUCCESS != this.wsGroupLookup.retrieveGroupFindResult()) {
        
        this.errorMessage = GrouperUtil.toStringSafe(this.wsGroupLookup.retrieveGroupFindResult());
        this.membershipAnyFindResult = MembershipAnyFindResult.INVALID_QUERY;
        
      } else if (SubjectFindResult.SUCCESS != this.wsSubjectLookup.retrieveSubjectFindResult()) {
        
        this.errorMessage = GrouperUtil.toStringSafe(this.wsSubjectLookup.retrieveSubjectFindResult());
        this.membershipAnyFindResult = MembershipAnyFindResult.INVALID_QUERY;

      } else if (true) {
        
        Group group = this.wsGroupLookup.retrieveGroup();
        Subject subject = this.wsSubjectLookup.retrieveSubject();
        Member member = MemberFinder.findBySubject(grouperSession, subject, true);
        this.groupMember = new GroupMember(group, member);
      }
      
    }
    return this.groupMember;
  }

  /**
   * clear the membership if a setter is called
   */
  private void clearMembershipAny() {
    this.groupMember = null;
    this.membershipAnyFindResult = null;
  }

  /**
   * convert membership any lookups to membership any ids
   * @param grouperSession
   * @param wsMembershipAnyLookups
   * @param errorMessage
   * @param typeOfGroup 
   * @param lookupCount is an array of size one int where 1 will be added if there are records, and no change if not
   * @return the GroupMember ids
   */
  public static Set<MultiKey> convertToGroupMemberIds(GrouperSession grouperSession, 
      WsMembershipAnyLookup[] wsMembershipAnyLookups, StringBuilder errorMessage, 
      TypeOfGroup typeOfGroup, int[] lookupCount) {
    //get all the memberships
    //we could probably batch these to get better performance.
    Set<MultiKey> groupMemberIds = null;
    if (!GrouperServiceUtils.nullArray(wsMembershipAnyLookups)) {
      
      groupMemberIds = new LinkedHashSet<MultiKey>();
      int i=0;
      
      boolean foundRecords = false;
      
      for (WsMembershipAnyLookup wsMembershipAnyLookup : wsMembershipAnyLookups) {
        
        if (wsMembershipAnyLookup == null || !wsMembershipAnyLookup.hasData()) {
          continue;
        }
        
        if (!foundRecords) {
          lookupCount[0]++;
          foundRecords = true;
        }
        
        wsMembershipAnyLookup.retrieveMembershipAnyIfNeeded(grouperSession);
        GroupMember groupMember = wsMembershipAnyLookup.retrieveGroupMember();
        if (groupMember != null) {
          if (typeOfGroup == null || typeOfGroup == groupMember.getGroup().getTypeOfGroup()) {
            groupMemberIds.add(new MultiKey(groupMember.getGroup().getId(), groupMember.getMember().getUuid()));
          } else {
            if (errorMessage.length() > 0) {
              errorMessage.append(", ");
            }
            
            errorMessage.append("Error on membershipAny index: " + i 
                + ", expecting type of group: " + typeOfGroup + ", " 
                + wsMembershipAnyLookup.toStringCompact());
            
          }
        } else {
          
          if (errorMessage.length() > 0) {
            errorMessage.append(", ");
          }
          
          errorMessage.append("Error on membershipAny index: " + i 
              + ", " + wsMembershipAnyLookup.retrieveMembershipAnyFindResult() 
              + ", " + wsMembershipAnyLookup.toStringCompact());
        }
        
        i++;
      }
      
    }
    return groupMemberIds;
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
