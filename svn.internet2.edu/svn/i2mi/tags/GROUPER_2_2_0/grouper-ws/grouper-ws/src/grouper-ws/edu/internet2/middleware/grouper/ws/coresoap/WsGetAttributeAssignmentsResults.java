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
package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.ResultMetadataHolder;
import edu.internet2.middleware.grouper.ws.WsResultCode;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * results for the get attributeAssignments call.
 * 
 * result code:
 * code of the result for this attribute assignment overall
 * SUCCESS: means everything ok
 * INSUFFICIENT_PRIVILEGES: not allowed
 * INVALID_QUERY: bad inputs
 * EXCEPTION: something bad happened
 * </pre>
 * @author mchyzer
 */
public class WsGetAttributeAssignmentsResults implements WsResponseBean, ResultMetadataHolder {

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsGetAttributeAssignmentsResults.class);

  /**
   * attribute def references in the assignments or inputs (and able to be read)
   */
  private WsAttributeDef[] wsAttributeDefs;
  
  /**
   * attribute def references in the assignments or inputs (and able to be read)
   * @return attribute defs
   */
  public WsAttributeDef[] getWsAttributeDefs() {
    return this.wsAttributeDefs;
  }

  /**
   * attribute def references in the assignments or inputs (and able to be read)
   * @param wsAttributeDefs1
   */
  public void setWsAttributeDefs(WsAttributeDef[] wsAttributeDefs1) {
    this.wsAttributeDefs = wsAttributeDefs1;
  }

  /**
   * attribute def names referenced in the assignments or inputs (and able to read)
   */
  private WsAttributeDefName[] wsAttributeDefNames;
  
  /**
   * attribute def names referenced in the assignments or inputs (and able to read)
   * @return attribute def names
   */
  public WsAttributeDefName[] getWsAttributeDefNames() {
    return this.wsAttributeDefNames;
  }

  /**
   * attribute def names referenced in the assignments or inputs (and able to read)
   * @param wsAttributeDefNames1
   */
  public void setWsAttributeDefNames(WsAttributeDefName[] wsAttributeDefNames1) {
    this.wsAttributeDefNames = wsAttributeDefNames1;
  }

  /**
   * the assignments being queried
   */
  private WsAttributeAssign[] wsAttributeAssigns;
  
  /**
   * the assignments being queried
   * @return the assignments being queried
   */
  public WsAttributeAssign[] getWsAttributeAssigns() {
    return this.wsAttributeAssigns;
  }

  /**
   * the assignments being queried
   * @param wsAttributeAssigns1
   */
  public void setWsAttributeAssigns(WsAttributeAssign[] wsAttributeAssigns1) {
    this.wsAttributeAssigns = wsAttributeAssigns1;
  }

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * result code of a request.  The possible result codes 
   * of WsGetMembersResultCode (with http status codes) are:
   * SUCCESS(200), EXCEPTION(500), INVALID_QUERY(400)
   */
  public static enum WsGetAttributeAssignmentsResultsCode implements WsResultCode {

    /** found the attributeAssignments (lite status code 200) (success: T) */
    SUCCESS(200),

    /** something bad happened (lite status code 500) (success: F) */
    EXCEPTION(500),

    /** invalid query (e.g. if everything blank) (lite status code 400) (success: F) */
    INVALID_QUERY(400),
    
    /** not allowed to the privileges on the inputs.  Note if broad search, then the results wont
     * contain items not allowed.  If a specific search e.g. on a group, then if you cant read the
     * group then you cant read the privs
     */
    INSUFFICIENT_PRIVILEGES(403);

    /** get the name label for a certain version of client 
     * @param clientVersion 
     * @return name
     */
    public String nameForVersion(GrouperVersion clientVersion) {
      return this.name();
    }

    /**
     * construct with http code
     * @param theHttpStatusCode the code
     */
    private WsGetAttributeAssignmentsResultsCode(int theHttpStatusCode) {
      this.httpStatusCode = theHttpStatusCode;
    }

    /** http status code for result code */
    private int httpStatusCode;

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

    /** get the http result code for this status code
     * @return the status code
     */
    public int getHttpStatusCode() {
      return this.httpStatusCode;
    }
  }

  /**
   * assign the code from the enum
   * @param getAttributeAssignmentsResultCode
   */
  public void assignResultCode(WsGetAttributeAssignmentsResultsCode getAttributeAssignmentsResultCode) {
    this.getResultMetadata().assignResultCode(getAttributeAssignmentsResultCode);
  }

  /**
   * prcess an exception, log, etc
   * @param wsGetAttributeAssignmentsResultsCodeOverride
   * @param theError
   * @param e
   */
  public void assignResultCodeException(
      WsGetAttributeAssignmentsResultsCode wsGetAttributeAssignmentsResultsCodeOverride, String theError,
      Exception e) {

    if (e instanceof WsInvalidQueryException) {
      wsGetAttributeAssignmentsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetAttributeAssignmentsResultsCodeOverride, WsGetAttributeAssignmentsResultsCode.INVALID_QUERY);
      //a helpful exception will probably be in the getMessage()
      this.assignResultCode(wsGetAttributeAssignmentsResultsCodeOverride);
      this.getResultMetadata().appendResultMessage(e.getMessage());
      this.getResultMetadata().appendResultMessage(theError);
      LOG.warn(e);

    } else {
      wsGetAttributeAssignmentsResultsCodeOverride = GrouperUtil.defaultIfNull(
          wsGetAttributeAssignmentsResultsCodeOverride, WsGetAttributeAssignmentsResultsCode.EXCEPTION);
      LOG.error(theError, e);

      theError = StringUtils.isBlank(theError) ? "" : (theError + ", ");
      this.getResultMetadata().appendResultMessage(
          theError + ExceptionUtils.getFullStackTrace(e));
      this.assignResultCode(wsGetAttributeAssignmentsResultsCodeOverride);

    }
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @return the attributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @param attributeNamesa the attributeNames to set
   */
  public void setSubjectAttributeNames(String[] attributeNamesa) {
    this.subjectAttributeNames = attributeNamesa;
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * groups that are in the results
   */
  private WsGroup[] wsGroups;

  /**
   * stems that are in the results
   */
  private WsStem[] wsStems;

  /**
   * stems that are in the results
   * @return stems
   */
  public WsStem[] getWsStems() {
    return this.wsStems;
  }

  /**
   * stems that are in the results
   * @param wsStems1
   */
  public void setWsStems(WsStem[] wsStems1) {
    this.wsStems = wsStems1;
  }

  /**
   * results for each assignment sent in
   */
  private WsMembership[] wsMemberships;

  /**
   * subjects that are in the results
   */
  private WsSubject[] wsSubjects;

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  
  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

  /**
   * @return the wsGroups
   */
  public WsGroup[] getWsGroups() {
    return this.wsGroups;
  }

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsMembership[] getWsMemberships() {
    return this.wsMemberships;
  }

  /**
   * subjects that are in the results
   * @return the subjects
   */
  public WsSubject[] getWsSubjects() {
    return this.wsSubjects;
  }

  /**
   * @param wsGroup1 the wsGroups to set
   */
  public void setWsGroups(WsGroup[] wsGroup1) {
    this.wsGroups = wsGroup1;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setWsMemberships(WsMembership[] results1) {
    this.wsMemberships = results1;
  }

  /**
   * subjects that are in the results
   * @param wsSubjects1
   */
  public void setWsSubjects(WsSubject[] wsSubjects1) {
    this.wsSubjects = wsSubjects1;
  }

  /**
   * convert assignments to ws assignments
   * @param attributeAssigns 
   * @param theSubjectAttributeNames 
   */
  public void assignResult(Set<AttributeAssign> attributeAssigns, String[] theSubjectAttributeNames) {
    
    this.subjectAttributeNames = theSubjectAttributeNames;

    this.setWsAttributeAssigns(WsAttributeAssign.convertAttributeAssigns(attributeAssigns));
    
  }

  /**
   * sort the assignments by def, name, etc  
   */
  public void sortResults() {
    //maybe we shouldnt do this for huge resultsets, but this makes things more organized and easier to test
    if (this.wsAttributeAssigns != null) {
      Arrays.sort(this.wsAttributeAssigns);
    }
    if (this.wsAttributeDefNames != null) {
      Arrays.sort(this.wsAttributeDefNames);
    }
    if (this.wsAttributeDefs != null) {
      Arrays.sort(this.wsAttributeDefs);
    }
    if (this.wsGroups != null) {
      Arrays.sort(this.wsGroups);
    }
    if (this.wsMemberships != null) {
      Arrays.sort(this.wsMemberships);
    }
    if (this.wsStems != null) {
      Arrays.sort(this.wsStems);
    }
    if (this.wsSubjects != null) {
      Arrays.sort(this.wsSubjects);
    }
  }

  /**
   * pass in the attribute def ids that were found by inputs, and add the attribute
   * def ids found by the attribute assign results
   * @param attributeDefIds
   */
  public void fillInAttributeDefs(Set<String> attributeDefIds) {
    
    Set<String> allAttributeDefIds = new HashSet<String>(GrouperUtil.nonNull(attributeDefIds));
    
    for (WsAttributeAssign wsAttributeAssign : GrouperUtil.nonNull(this.wsAttributeAssigns, WsAttributeAssign.class)) {
      if (!StringUtils.isBlank(wsAttributeAssign.getAttributeDefId())) {
        allAttributeDefIds.add(wsAttributeAssign.getAttributeDefId());
      }
      if (!StringUtils.isBlank(wsAttributeAssign.getOwnerAttributeDefId())) {
        allAttributeDefIds.add(wsAttributeAssign.getOwnerAttributeDefId());
      }
    }
    
    //make sure all attr def names are there
    for (WsAttributeDefName wsAttributeDefName : GrouperUtil.nonNull(this.wsAttributeDefNames, WsAttributeDefName.class) ) {
      allAttributeDefIds.add(wsAttributeDefName.getAttributeDefId());
    }
    
    //security is already checked, lets pass these through...
    this.wsAttributeDefs = new WsAttributeDef[allAttributeDefIds.size()];
    
    int i = 0;
    for (String wsAttributeDefId : allAttributeDefIds) {
      AttributeDef attributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findById(wsAttributeDefId, true);
      this.wsAttributeDefs[i] = new WsAttributeDef(attributeDef, null);
      i++;
    }
  }

  /**
   * pass in the group ids that were found by inputs, and add the group id 
   * found by the attribute assign results
   * @param groupIds
   * @param includeGroupDetail 
   */
  public void fillInGroups(Set<String> groupIds, boolean includeGroupDetail) {
    
    Set<String> allGroupIds = new HashSet<String>(GrouperUtil.nonNull(groupIds));
    
    for (WsAttributeAssign wsAttributeAssign : GrouperUtil.nonNull(this.wsAttributeAssigns, WsAttributeAssign.class)) {
      if (!StringUtils.isBlank(wsAttributeAssign.getOwnerGroupId())) {
        allGroupIds.add(wsAttributeAssign.getOwnerGroupId());
      }
    }
    
    //security is already checked, lets pass these through...
    this.wsGroups = new WsGroup[allGroupIds.size()];
    
    int i = 0;
    for (String wsGroupId : allGroupIds) {
      Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(wsGroupId, true);
      this.wsGroups[i] = new WsGroup(group, null, includeGroupDetail);
      i++;
    }
  }

  /**
   * pass in the stem ids that were found by inputs, and add the stem id 
   * found by the attribute assign results
   * @param stemIds
   */
  public void fillInStems(Set<String> stemIds) {
    
    Set<String> allStemIds = new HashSet<String>(GrouperUtil.nonNull(stemIds));
    
    for (WsAttributeAssign wsAttributeAssign : GrouperUtil.nonNull(this.wsAttributeAssigns, WsAttributeAssign.class)) {
      if (!StringUtils.isBlank(wsAttributeAssign.getOwnerStemId())) {
        allStemIds.add(wsAttributeAssign.getOwnerStemId());
      }
    }
    
    //security is already checked, lets pass these through...
    this.wsStems = new WsStem[allStemIds.size()];
    
    int i = 0;
    for (String wsStemId : allStemIds) {
      Stem stem = GrouperDAOFactory.getFactory().getStem().findByUuid(wsStemId, true);
      this.wsStems[i] = new WsStem(stem);
      i++;
    }
  }

  /**
   * pass in the stem ids that were found by inputs, and add the stem id 
   * found by the attribute assign results
   * @param membershipIds
   */
  public void fillInMemberships(Set<String> membershipIds) {
    
    Set<String> allMembershipIds = new HashSet<String>(GrouperUtil.nonNull(membershipIds));
    
    for (WsAttributeAssign wsAttributeAssign : GrouperUtil.nonNull(this.wsAttributeAssigns, WsAttributeAssign.class)) {
      if (!StringUtils.isBlank(wsAttributeAssign.getOwnerMembershipId())) {
        allMembershipIds.add(wsAttributeAssign.getOwnerMembershipId());
      }
    }
    
    //put in a set to make sure no duplicates, could be since multiple types of ids
    
    Set<Membership> memberships = new HashSet<Membership>();
    
    for (String membershipId : allMembershipIds) {
      //security is already checked, lets pass these through...
      Membership membership = GrouperDAOFactory.getFactory().getMembership().findByUuid(membershipId, false, false);
      if (membership != null) {
        memberships.add(membership);
      }
    }
    
    this.wsMemberships = new WsMembership[memberships.size()];
    
    int i = 0;
    for (Membership membership : memberships) {
      this.wsMemberships[i] = new WsMembership(membership);
      i++;
    }
  }

  /**
   * pass in the subject lookups that were found by inputs, and add the subject ids 
   * found by the attribute assign results
   * @param subjectLookups
   * @param extraMemberIds 
   * @param includeSubjectDetail 
   * @param theSubjectAttributeNames 
   */
  public void fillInSubjects(WsSubjectLookup[] subjectLookups, Set<String> extraMemberIds, 
      boolean includeSubjectDetail, String[] theSubjectAttributeNames) {
        
    Set<Subject> allSubjects = new HashSet<Subject>();
    
    for (WsSubjectLookup wsSubjectLookup : GrouperUtil.nonNull(subjectLookups, WsSubjectLookup.class)) {
      if (wsSubjectLookup == null) {
        continue;
      }
      Subject subject = wsSubjectLookup.retrieveSubject();
      if (subject != null) {
        if (!SubjectHelper.inList(allSubjects, subject)) {
          allSubjects.add(subject);
        }
      }
    }
    
    //process extra ones e.g. from list of any memberships passed in
    for (String memberId : GrouperUtil.nonNull(extraMemberIds)) {
      Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), 
          memberId, false);
      if (member != null && !SubjectHelper.inList(allSubjects, 
          member.getSubjectSourceId(), member.getSubjectId())) {
        allSubjects.add(member.getSubject());
      }
    }
    
    for (WsAttributeAssign wsAttributeAssign : GrouperUtil.nonNull(this.wsAttributeAssigns, WsAttributeAssign.class)) {
      if (!StringUtils.isBlank(wsAttributeAssign.getOwnerMemberSubjectId())) {
        if (!SubjectHelper.inList(allSubjects, wsAttributeAssign.getOwnerMemberSourceId(), wsAttributeAssign.getOwnerMemberSubjectId())) {
          Subject subject = SubjectFinder.findById(wsAttributeAssign.getOwnerMemberSubjectId(), null, wsAttributeAssign.getOwnerMemberSourceId(), false);
          if (subject != null) {
            allSubjects.add(subject);
          }
        }
      }
    }
    
    //security is already checked, lets pass these through...
    this.wsSubjects = new WsSubject[allSubjects.size()];
    
    int i = 0;
    for (Subject subject : allSubjects) {
      this.wsSubjects[i] = new WsSubject(subject, theSubjectAttributeNames, null);
      i++;
    }
  }

  
  /**
   * pass in the attribute def name ids that were found by inputs, and add the attribute
   * def name ids found by the attribute assign results
   * @param attributeDefNameIds
   */
  public void fillInAttributeDefNames(Set<String> attributeDefNameIds) {
    
    Set<String> allAttributeDefNameIds = new HashSet<String>(GrouperUtil.nonNull(attributeDefNameIds));
    
    for (WsAttributeAssign wsAttributeAssign : GrouperUtil.nonNull(this.wsAttributeAssigns, WsAttributeAssign.class)) {
      if (!StringUtils.isBlank(wsAttributeAssign.getAttributeDefNameId())) {
        allAttributeDefNameIds.add(wsAttributeAssign.getAttributeDefNameId());
      }
    }
    
    //security is already checked, lets pass these through...
    this.wsAttributeDefNames = new WsAttributeDefName[allAttributeDefNameIds.size()];
    
    int i = 0;
    for (String wsAttributeDefNameId : allAttributeDefNameIds) {
      AttributeDefName attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByUuidOrName(wsAttributeDefNameId, null, true);
      this.wsAttributeDefNames[i] = new WsAttributeDefName(attributeDefName, null);
      i++;
    }
  }
}
