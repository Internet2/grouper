/*
 * @author mchyzer
 * $Id: GcAddMember.java,v 1.1 2008-11-30 10:57:19 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.examples.WsExample.GrouperTestClientWs;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAddMemberRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run an add member web service call
 */
public class GcAddMember {

  /** group name to add member to */
  private String groupName;
  
  /**
   * set the group name
   * @param theGroupName
   * @return this for chaining
   */
  public GcAddMember assignGroupName(String theGroupName) {
    this.groupName = theGroupName;
    return this;
  }
  
  /** subject lookups */
  private List<WsSubjectLookup> subjectLookups = new ArrayList<WsSubjectLookup>();

  /** 
   * add a subject lookup
   * @param wsSubjectLookup
   * @return this for chaining
   */
  public GcAddMember addSubjectLookup(WsSubjectLookup wsSubjectLookup) {
    this.subjectLookups.add(wsSubjectLookup);
    return this;
  }
  
  /** if we should replace all existing */
  private boolean replaceAllExisting = false;

  /**
   * set if we should replace all existing members with new list
   * @param isReplaceAllExisting
   * @return this for chaining
   */
  public GcAddMember assignReplaceAllExisting(boolean isReplaceAllExisting) {
    this.replaceAllExisting = isReplaceAllExisting;
    return this;
  }
  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcAddMember assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.isBlank(this.groupName)) {
      throw new RuntimeException("Group name is required: " + this);
    }
    if (GrouperClientUtils.length(this.subjectLookups) == 0) {
      throw new RuntimeException("Need at least one subject to add to group: " + this);
    }
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsAddMemberResults execute() {
    this.validate();
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAddMemberRequest addMember = new WsRestAddMemberRequest();

      addMember.setActAsSubjectLookup(this.actAsSubject);

      // just add, dont replace
      addMember.setReplaceAllExisting(this.replaceAllExisting ? "T" : "F");
      
      WsSubjectLookup[] subjectLookupsResults = GrouperClientUtils.toArray(this.subjectLookups, 
          WsSubjectLookup.class);
      addMember.setSubjectLookups(subjectLookupsResults);
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      WsAddMemberResults wsAddMemberResults = (WsAddMemberResults)
        grouperClientWs.executeService("groups/" + this.groupName + "/members", addMember, "addMember");
      
      String resultMessage = wsAddMemberResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(resultMessage);
      
      return wsAddMemberResults;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
  
  /**
   * add member(s)
   * @param groupName
   * @param subjectIds
   * @param subjectIdentifiers
   * @param sourceIds
   * @param replaceAllExisting if replace all existing
   * @return the status
   */
  public static String addMember(String groupName, List<String> subjectIds, 
      List<String> subjectIdentifiers, List<String> sourceIds, boolean replaceAllExisting) {
    StringBuilder result = new StringBuilder();
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAddMemberRequest addMember = new WsRestAddMemberRequest();

      // set the act as id
      WsSubjectLookup actAsSubject = new WsSubjectLookup("GrouperSystem", null, null);
      addMember.setActAsSubjectLookup(actAsSubject);

      // just add, dont replace
      addMember.setReplaceAllExisting(replaceAllExisting ? "T" : "F");

      // add two subjects to the group
      int subjectIdLength = GrouperClientUtils.length(subjectIds);
      int subjectIdentifierLength = GrouperClientUtils.length(subjectIdentifiers);
      int sourceIdLength = GrouperClientUtils.length(sourceIds);
      
      if (subjectIdLength == 0 && subjectIdentifierLength == 0) {
        throw new RuntimeException("Cant pass no subject ids and no subject identifiers!");
      }
      if (subjectIdLength != 0 && subjectIdentifierLength != 0) {
        throw new RuntimeException("Cant pass subject ids and subject identifiers! (pass one of the other)");
      }
      
      if (sourceIdLength > 0 && sourceIdLength != subjectIdLength 
          && sourceIdLength != subjectIdentifierLength) {
        throw new RuntimeException("If source ids are passed in, you " +
            "must pass the same number as subjectIds or subjectIdentifiers");
      }
      
      int subjectsLength = Math.max(subjectIdLength, subjectIdentifierLength);
      WsSubjectLookup[] subjectLookups = new WsSubjectLookup[subjectsLength];
      for (int i=0;i<subjectsLength;i++) {
        subjectLookups[i] = new WsSubjectLookup();
        if (subjectIdLength > 0) {
          subjectLookups[i].setSubjectId(subjectIds.get(i));
        }
        if (subjectIdentifierLength > 0) {
          subjectLookups[i].setSubjectIdentifier(subjectIdentifiers.get(i));
        }
        if (sourceIdLength > 0) {
          subjectLookups[i].setSubjectSourceId(sourceIds.get(i));
        }
      }
      addMember.setSubjectLookups(subjectLookups);
      
      GrouperTestClientWs grouperClientWs = new GrouperTestClientWs();
      
      //convert to object (from xhtml, xml, json, etc)
      WsAddMemberResults wsAddMemberResults = (WsAddMemberResults)grouperClientWs.executeService(addMember);
      
      String resultMessage = wsAddMemberResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(resultMessage);
      
      int index = 0;
      for (WsAddMemberResult wsAddMemberResult : wsAddMemberResults.getResults()) {

        result.append("Index " + index + ": success: " + wsAddMemberResult.getResultMetadata().getSuccess()
            + ": code: " + wsAddMemberResult.getResultMetadata().getResultCode() + ": " 
            + wsAddMemberResult.getWsSubject().getId() + "\n");
        index++;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result.toString();

  }
  
}
