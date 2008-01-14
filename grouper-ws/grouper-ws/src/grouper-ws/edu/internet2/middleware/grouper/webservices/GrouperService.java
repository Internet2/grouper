package edu.internet2.middleware.grouper.webservices;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SessionException;
import edu.internet2.middleware.grouper.webservices.WsAddMemberResults.WsAddMemberResultCode;
import edu.internet2.middleware.grouper.webservices.WsSubjectLookup.SubjectFindResult;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * All public methods in this class are available in the web service
 * as both SOAP and REST.
 * 
 * booleans can either be T, F, true, false (case-insensitive)
 * 
 * get wsdl from: http://localhost:8090/grouper/services/GrouperService?wsdl
 * 
 * generate client (after wsdl copied): C:\mchyzer\isc\dev\grouper\axisJar2>wsdl2java -p edu.internet2.middleware.grouper.webservicesClient -t -uri GrouperService.wsdl
 * 
 * @author mchyzer
 * </pre>
 */
public class GrouperService {

    /** logger */
    private static final Log LOG = LogFactory.getLog(GrouperService.class);

	/**
	 * add member to a group (if already a direct member, ignore)
	 * @param wsGroupLookup 
	 * @param subjectLookups subjects to be added to the group
	 * @param replaceAllExisting optional: T or F (default), if the 
	 * existing groups should be replaced 
	 * @param actAsSubject optional: is the subject to act as (if proxying)
	 * @return the results
	 */
	@SuppressWarnings("unchecked")
	public WsAddMemberResults addMember(WsGroupLookup wsGroupLookup,
			WsSubjectLookup[] subjectLookups, 
			String replaceAllExisting, WsSubjectLookup actAsSubjectLookup) {
		
		GrouperSession session = null;
		WsAddMemberResults wsAddMemberResults = new WsAddMemberResults();
		int subjectLength = subjectLookups == null ? 0 : subjectLookups.length;
		if (subjectLength == 0) {
			wsAddMemberResults.setSuccess("F");
			wsAddMemberResults.assignResultCode(WsAddMemberResultCode.INVALID_QUERY);
			wsAddMemberResults.appendErrorMessage("Subject length must be more than 1");
			return wsAddMemberResults;
		}
		
		//see if greater than the max (or default)
		Integer maxAddMember = GrouperWsConfig.getPropertyInteger(GrouperWsConfig.WS_ADD_MEMBER_SUBJECTS_MAX, 1000000);
		if (subjectLength > maxAddMember) {
			wsAddMemberResults.setSuccess("F");
			wsAddMemberResults.assignResultCode(WsAddMemberResultCode.INVALID_QUERY);
			wsAddMemberResults.appendErrorMessage("Subject length must be less than max: " + maxAddMember + " (sent in " + subjectLength + ")");
			return wsAddMemberResults;
		}
		
		//assume success
		wsAddMemberResults.setSuccess("F");
		wsAddMemberResults.assignResultCode(WsAddMemberResultCode.SUCCESS);
		Subject actAsSubject = null;
		try {
			actAsSubject = GrouperServiceServlet.retrieveSubjectActAs(actAsSubjectLookup);
			
			if (actAsSubject == null) {
				throw new RuntimeException("Cant find actAs user: " + actAsSubjectLookup);
			}
			
			//use this to be the user connected, or the user act-as
			try {
				session = GrouperSession.start(actAsSubject);
			} catch (SessionException se) {
				throw new RuntimeException("Problem with session for subject: " + actAsSubject, se);
			}
			wsGroupLookup.retrieveGroupIfNeeded(session);
			wsAddMemberResults.setResults(new WsAddMemberResult[subjectLength]);
			Group group = wsGroupLookup.retrieveGroup();
			
			if (group == null) {
				wsAddMemberResults.setSuccess("F");
				wsAddMemberResults.assignResultCode(WsAddMemberResultCode.INVALID_QUERY);
				wsAddMemberResults.appendErrorMessage("Cant find group: " + wsGroupLookup + ".  ");
				return wsAddMemberResults;
			}
			
			int resultIndex = 0;
			
			//TODO keep data in transaction?
			boolean replaceAllExistingBoolean = GrouperWsUtils.booleanValue(replaceAllExisting, false);
			Set<String> newSubjectIds = new HashSet<String>();
			
			for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
				WsAddMemberResult wsAddMemberResult = new WsAddMemberResult();
				wsAddMemberResults.getResults()[resultIndex] = wsAddMemberResult;
				try {
					//default to non-success
					wsAddMemberResult.setSuccess("F");
	
					wsAddMemberResult.setSubjectId(wsSubjectLookup.getSubjectId());
					wsAddMemberResult.setSubjectIdentifier(wsSubjectLookup.getSubjectIdentifier());
	
					Subject subject = wsSubjectLookup.retrieveSubject();
					
					//make sure the subject is there
					if (subject == null) {
						//see why not
						SubjectFindResult subjectFindResult = wsSubjectLookup.retrieveSubjectFindResult();
	
						wsAddMemberResult.setErrorMessage("Subject: " + wsSubjectLookup + " had problems: " + subjectFindResult); 
					} 
	
					//these will probably match, but just in case
					if (StringUtils.isBlank(wsAddMemberResult.getSubjectId())) {
						wsAddMemberResult.setSubjectId(subject.getId());
					}
	
					//keep track
					if (replaceAllExistingBoolean) {
						newSubjectIds.add(subject.getId());
					}
					
					try {
						//dont fail if already a direct member
						if (!group.hasImmediateMember(subject)) {
							group.addMember(subject);
						}
						wsAddMemberResult.setSuccess("T");
						
					} catch (InsufficientPrivilegeException ipe) {
						wsAddMemberResult.setResultCode("INSUFFICIENT_PRIVILEGES");
						wsAddMemberResult.setErrorMessage(ExceptionUtils.getFullStackTrace(ipe));
					}
				} catch (Exception e) {
					wsAddMemberResult.setResultCode("EXCEPTION");
					wsAddMemberResult.setErrorMessage(ExceptionUtils.getFullStackTrace(e));
					LOG.error(wsSubjectLookup + ", " + e, e);
				}
				resultIndex++;
			}
			
			//after adding all these, see if we are removing:
			if (replaceAllExistingBoolean) {
				
				//see who is there
				Set<Member> members = group.getImmediateMembers();
				
				for (Member member : members) {
					String subjectId = member.getSubjectId();
					Subject subject = null;
					
					if (!newSubjectIds.contains(subjectId)) {
						try {
							subject = member.getSubject();
							group.deleteMember(subject);
						} catch (Exception e) {
							String theError = "Error deleting subject: " + ObjectUtils.defaultIfNull(subject, subjectId) 
								+ " from group: " + group + ", " + e + ".  ";
							LOG.error(theError, e);

							wsAddMemberResults.appendErrorMessage(theError + ExceptionUtils.getFullStackTrace(e));
							wsAddMemberResults.setSuccess("F");
							wsAddMemberResults.assignResultCode(WsAddMemberResultCode.PROBLEM_DELETING_MEMBERS);
						}
					}
				}
			}
		} catch (RuntimeException re) {
			wsAddMemberResults.assignResultCode(WsAddMemberResultCode.EXCEPTION);
			wsAddMemberResults.setSuccess("F");
			String theError = "Problem adding member to group: wsGroupLookup: " + wsGroupLookup
				+ ", subjectLookups: " + GrouperServiceUtils.toStringForLog(subjectLookups)
				+ ", replaceAllExisting: " +  replaceAllExisting +  ", actAsSubject: " + actAsSubject
				 + ".  ";
			wsAddMemberResults.appendErrorMessage(theError);
			//this is sent back to the caller anyway, so just log, and not send back again
			LOG.error(theError + ", wsAddMemberResults: " + GrouperServiceUtils.toStringForLog(wsAddMemberResults), re);
		} finally {
			if (session != null) {
				try {
					session.stop();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		
		if (wsAddMemberResults.getResults() != null) {
			//check all entries
			int successes = 0;
			int failures = 0;
			for (WsAddMemberResult wsAddMemberResult : wsAddMemberResults.getResults()) {
				boolean success = "T".equalsIgnoreCase(wsAddMemberResult.getSuccess());
				if (success) {
					successes++;
				} else {
					failures++;
				}
			}
			if (failures > 0) {
				wsAddMemberResults.appendErrorMessage("There were " + successes + " successes and " + failures 
						+ " failures of users added to the group.   ");
				wsAddMemberResults.setSuccess("F");
				wsAddMemberResults.assignResultCode(WsAddMemberResultCode.PROBLEM_WITH_ASSIGNMENT);
			} else {
				wsAddMemberResults.setSuccess("T");
				wsAddMemberResults.assignResultCode(WsAddMemberResultCode.SUCCESS);
			}
		}
		if (!"T".equalsIgnoreCase(wsAddMemberResults.getSuccess())) {
			
			LOG.error(wsAddMemberResults.getErrorMessage());
		}
		return wsAddMemberResults;
	}
		
	/**
	 * split a string and trim each
	 * @param string
	 * @param separator
	 * @return the array
	 */
	@SuppressWarnings("unused")
	private static String[] splitTrim(String string, String separator) {
		String[] splitArray = StringUtils.split(string, separator);
		if (splitArray == null) {
			return null;
		}
		int index = 0;
		for (String stringInArray : splitArray) {
			splitArray[index++] = StringUtils.trimToNull(stringInArray);
		} 
		return splitArray;
	}
	
	/**
	 * add member to a group (if already a direct member, ignore)
	 * @param wsGroupLookup 
	 * @param subjectLookups subjects to be added to the group
	 * @param replaceAllExisting optional: T or F (default), if the 
	 * existing groups should be replaced 
	 * @param actAsSubject optional: is the subject to act as (if proxying)
	 * @return SUCCESS, or an error message
	 */
	public WsAddMemberResult addMemberSimple(String groupName,
			String groupUuid,
			String subjectId, 
			String subjectIdentifier,
			String actAsSubjectId,
			String actAsSubjectIdentifier) {
		
		//setup the group lookup
		WsGroupLookup wsGroupLookup = new WsGroupLookup();
		wsGroupLookup.setGroupName(groupName);
		wsGroupLookup.setUuid(groupUuid);
		
		//setup the subject lookup
		WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
		subjectLookups[0] = new WsSubjectLookup();
		subjectLookups[0].setSubjectId(subjectId);
		subjectLookups[0].setSubjectIdentifier(subjectIdentifier);
		WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup();
		actAsSubjectLookup.setSubjectId(actAsSubjectId);
		actAsSubjectLookup.setSubjectIdentifier(actAsSubjectIdentifier);
		
		WsAddMemberResults wsAddMemberResults = addMember(wsGroupLookup, 
				subjectLookups, "F", actAsSubjectLookup);
		
		WsAddMemberResult[] results = wsAddMemberResults.getResults();
		if (results != null && results.length > 0) {
			return results[0];
		}
		//didnt even get that far to where there is a subject result
		WsAddMemberResult wsAddMemberResult = new WsAddMemberResult();
		wsAddMemberResult.setErrorMessage(wsAddMemberResults.getErrorMessage());
		wsAddMemberResult.setResultCode(wsAddMemberResults.getResultCode());
		wsAddMemberResult.setSubjectId(subjectId);
		wsAddMemberResult.setSubjectIdentifier(subjectIdentifier);
		
		//definitely not a success
		wsAddMemberResult.setSuccess("F");
		
		return wsAddMemberResult;
			
	}
	
//	/**
//	 * web service wrapper for find all subjects based on query
//	 * @param query
//	 * @return
//	 */
//	@SuppressWarnings("unchecked")
//	public WsSubject[] findAll(String query) {
//		Set<JDBCSubject> subjectSet = SubjectFinder.findAll(query);
//		if (subjectSet == null || subjectSet.size() == 0) {
//			return null;
//		}
//		//convert the set to a list
//		WsSubject[] results = new WsSubject[subjectSet.size()];
//		int i=0;
//		for (JDBCSubject jdbcSubject : subjectSet) {
//			WsSubject wsSubject = new WsSubject(jdbcSubject);
//			results[i++] = wsSubject;
//		}
//		return results;
//	}
	
}
