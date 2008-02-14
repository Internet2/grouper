package edu.internet2.middleware.grouper.webservices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.AccessPrivilege;
import edu.internet2.middleware.grouper.AttributeNotFoundException;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupAnyAttributeFilter;
import edu.internet2.middleware.grouper.GroupAttributeFilter;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.GrouperQuery;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.MemberNotFoundException;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Privilege;
import edu.internet2.middleware.grouper.SessionException;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemDisplayExtensionFilter;
import edu.internet2.middleware.grouper.StemDisplayNameFilter;
import edu.internet2.middleware.grouper.StemExtensionFilter;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemNameAnyFilter;
import edu.internet2.middleware.grouper.StemNameFilter;
import edu.internet2.middleware.grouper.StemNotFoundException;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.webservices.WsAddMemberResult.WsAddMemberResultCode;
import edu.internet2.middleware.grouper.webservices.WsAddMemberResults.WsAddMemberResultsCode;
import edu.internet2.middleware.grouper.webservices.WsDeleteMemberResults.WsDeleteMemberResultCode;
import edu.internet2.middleware.grouper.webservices.WsFindGroupsResults.WsFindGroupsResultCode;
import edu.internet2.middleware.grouper.webservices.WsFindStemsResults.WsFindStemsResultsCode;
import edu.internet2.middleware.grouper.webservices.WsGetGroupsResults.WsGetGroupsResultsCode;
import edu.internet2.middleware.grouper.webservices.WsGetMembersResults.WsGetMembersResultCode;
import edu.internet2.middleware.grouper.webservices.WsGetMembershipsResults.WsGetMembershipsResultCode;
import edu.internet2.middleware.grouper.webservices.WsGroupDeleteResult.WsGroupDeleteResultCode;
import edu.internet2.middleware.grouper.webservices.WsGroupDeleteResults.WsGroupDeleteResultsCode;
import edu.internet2.middleware.grouper.webservices.WsGroupSaveResult.WsGroupSaveResultCode;
import edu.internet2.middleware.grouper.webservices.WsGroupSaveResults.WsGroupSaveResultsCode;
import edu.internet2.middleware.grouper.webservices.WsHasMemberResult.WsHasMemberResultCode;
import edu.internet2.middleware.grouper.webservices.WsHasMemberResults.WsHasMemberResultsCode;
import edu.internet2.middleware.grouper.webservices.WsStemDeleteResult.WsStemDeleteResultCode;
import edu.internet2.middleware.grouper.webservices.WsStemDeleteResults.WsStemDeleteResultsCode;
import edu.internet2.middleware.grouper.webservices.WsSubjectLookup.SubjectFindResult;
import edu.internet2.middleware.grouper.webservices.WsViewOrEditAttributesResult.WsViewOrEditAttributesResultCode;
import edu.internet2.middleware.grouper.webservices.WsViewOrEditAttributesResults.WsViewOrEditAttributesResultsCode;
import edu.internet2.middleware.grouper.webservices.WsViewOrEditPrivilegesResult.WsViewOrEditPrivilegesResultCode;
import edu.internet2.middleware.grouper.webservices.WsViewOrEditPrivilegesResults.WsViewOrEditPrivilegesResultsCode;
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
 * generate client (after wsdl copied): C:\mchyzer\isc\dev\grouper\axisJar2&gt;wsdl2java -p edu.internet2.middleware.grouper.webservicesClient -t -uri GrouperService.wsdl
 * 
 * @author mchyzer
 * </pre>
 */
public class GrouperService {

	/** logger */
	private static final Log LOG = LogFactory.getLog(GrouperService.class);

	/**
	 * find a group or groups
	 * 
	 * @param groupName
	 *            search by group name (must match exactly), cannot use other
	 *            params with this
	 * @param stemName
	 *            will return groups in this stem
	 * @param stemNameScope
	 *            if searching by stem, ONE_LEVEL is for one level,
	 *            ALL_IN_SUBTREE will return all in sub tree. Required if
	 *            searching by stem
	 * @param groupUuid
	 *            search by group uuid (must match exactly), cannot use other
	 *            params with this
	 * @param queryTerm
	 *            if searching by query, this is a term that will be matched to
	 *            name, extension, etc
	 * @param querySearchFromStemName
	 *            if a stem name is put here, that will narrow the search
	 * @param queryScope
	 *            NAME is searching by name, EXTENSION is display extension, and
	 *            DISPLAY_NAME is display name. This is required if a query
	 *            search
	 * @param actAsSubjectId
	 *            optional: is the subject id of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param actAsSubjectIdentifier
	 *            optional: is the subject identifier of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param paramName0
	 *            reserved for future use
	 * @param paramValue0
	 *            reserved for future use
	 * @param paramName1
	 *            reserved for future use
	 * @param paramValue1
	 *            reserved for future use
	 * @return the groups, or no groups if none found
	 */
	public WsFindGroupsResults findGroupsSimple(String groupName,
			String stemName, String stemNameScope, String groupUuid,
			String queryTerm, String querySearchFromStemName,
			String queryScope, String actAsSubjectId,
			String actAsSubjectIdentifier, String paramName0,
			String paramValue0, String paramName1, String paramValue1) {
	
		WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(
				actAsSubjectId, actAsSubjectIdentifier);
	
		String[][] params = GrouperServiceUtils.params(paramName0, paramValue0,
				paramName1, paramValue1);
		String[] paramNames = params[0];
		String[] paramValues = params[1];
	
		// pass through to the more comprehensive method
		WsFindGroupsResults wsFindGroupsResults = findGroups(groupName,
				stemName, stemNameScope, groupUuid, queryTerm,
				querySearchFromStemName, queryScope, actAsSubjectLookup,
				paramNames, paramValues);
	
		return wsFindGroupsResults;
	}

	/**
	 * find a stem or stems or groups
	 * 
	 * @param stemName
	 *            will return stem with this name (including parent stem extensions)
	 * @param parentStemName if searching from parent stem and getting children
	 * @param parentStemNameScope
	 *            if searching by stem, ONE_LEVEL is for one level,
	 *            ALL_IN_SUBTREE will return all in sub tree. Required if
	 *            searching by stem
	 * @param stemUuid is searching for stem by uuid
	 * @param groupUuid
	 *            search by group uuid (must match exactly), cannot use other
	 *            params with this
	 * @param queryTerm
	 *            if searching by query, this is a term that will be matched to
	 *            name, extension, etc
	 * @param querySearchFromStemName
	 *            if a stem name is put here, that will narrow the search
	 * @param queryScope
	 *            NAME is searching by name, EXTENSION is display extension, and
	 *            DISPLAY_NAME is display name. This is required if a query
	 *            search
	 * @param actAsSubjectId
	 *            optional: is the subject id of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param actAsSubjectIdentifier
	 *            optional: is the subject identifier of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param paramName0
	 *            reserved for future use
	 * @param paramValue0
	 *            reserved for future use
	 * @param paramName1
	 *            reserved for future use
	 * @param paramValue1
	 *            reserved for future use
	 * @return the groups, or no groups if none found
	 */
	public WsFindStemsResults findStemsSimple(
			String stemName, String parentStemName, String parentStemNameScope, 
			String stemUuid,
			String queryTerm, String querySearchFromStemName,
			String queryScope, String actAsSubjectId,
			String actAsSubjectIdentifier, String paramName0,
			String paramValue0, String paramName1, String paramValue1) {

		WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(
				actAsSubjectId, actAsSubjectIdentifier);

		String[][] params = GrouperServiceUtils.params(paramName0, paramValue0,
				paramName1, paramValue1);
		String[] paramNames = params[0];
		String[] paramValues = params[1];

		// pass through to the more comprehensive method
		WsFindStemsResults wsFindStemsResults = findStems(
				stemName, parentStemName, parentStemNameScope, stemUuid, queryTerm,
				querySearchFromStemName, queryScope, actAsSubjectLookup,
				paramNames, paramValues);

		return wsFindStemsResults;
	}

	/**
	 * find a stem or stems
	 * 
	 * @param groupName
	 *            search by group name (must match exactly), cannot use other
	 *            params with this
	 * @param stemName
	 *            will return groups in this stem
	 * @param parentStemName 
	 * 			  if searching by parent stem, this is the parent stem
	 * @param parentStemNameScope
	 *            if searching by stem, ONE_LEVEL is for one level,
	 *            ALL_IN_SUBTREE will return all in sub tree. Required if
	 *            searching by stem
	 * @param stemUuid to find a specific stem
	 * @param queryTerm
	 *            if searching by query, this is a term that will be matched to
	 *            name, extension, etc
	 * @param querySearchFromStemName
	 *            if a stem name is put here, that will narrow the search
	 * @param queryScope
	 *            NAME is searching by name, EXTENSION is display extension,
	 *            DISPLAY_NAME is display name, DISPLAY_EXTENSION is
	 *            searching by display extension, or ALL (default)
	 * @param actAsSubjectLookup to find a subject to act as (by id or identifier)
	 * @param paramNames
	 *            optional: reserved for future use
	 * @param paramValues
	 *            optional: reserved for future use
	 * @return the groups, or no groups if none found
	 */
	@SuppressWarnings("unchecked")
	public WsFindStemsResults findStems(String stemName, String parentStemName,
			String parentStemNameScope, String stemUuid, String queryTerm,
			String querySearchFromStemName, String queryScope,
			WsSubjectLookup actAsSubjectLookup, String[] paramNames,
			String[] paramValues) {
	
		GrouperSession session = null;
		WsFindStemsResults wsFindStemsResults = new WsFindStemsResults();
	
		boolean searchByParentStem = StringUtils.isNotBlank(parentStemName);
		boolean searchByName = StringUtils.isNotBlank(stemName);
		boolean searchByUuid = StringUtils.isNotBlank(stemUuid);
		boolean searchByQuery = StringUtils.isNotBlank(queryTerm);
	
		// TODO make sure size of params and values the same
	
		// count the search types
		int searchTypes = (searchByParentStem ? 1 : 0) + (searchByName ? 1 : 0)
				+ (searchByUuid ? 1 : 0) + (searchByQuery ? 1 : 0);
		// must only search by one type
		if (searchTypes != 1) {
			wsFindStemsResults
					.assignResultCode(WsFindStemsResultsCode.INVALID_QUERY);
			wsFindStemsResults
					.setResultMessage("Invalid query, only query on one thing, not multiple.  "
							+ "Only search by name, stem, uuid, or query");
			return wsFindStemsResults;
		}
	
		// assume success
		wsFindStemsResults.assignResultCode(WsFindStemsResultsCode.SUCCESS);
	
		Subject actAsSubject = null;
		try {
			actAsSubject = GrouperServiceJ2ee
					.retrieveSubjectActAs(actAsSubjectLookup);
	
			if (actAsSubject == null) {
				throw new RuntimeException("Cant find actAs user: "
						+ actAsSubjectLookup);
			}
	
			// use this to be the user connected, or the user act-as
			try {
				session = GrouperSession.start(actAsSubject);
			} catch (SessionException se) {
				throw new RuntimeException("Problem with session for subject: "
						+ actAsSubject, se);
			}
	
			// simple search by name
			if (searchByName) {
				try {
					Stem stem = StemFinder.findByName(session, stemName);
					wsFindStemsResults.assignStemResult(stem);
				} catch (StemNotFoundException snfe) {
					// just ignore, the stem results will be blank
				}
				return wsFindStemsResults;
			}
	
			// simple search for uuid
			if (searchByUuid) {
				try {
					Stem stem = StemFinder.findByUuid(session, stemUuid);
					wsFindStemsResults.assignStemResult(stem);
				} catch (StemNotFoundException gnfe) {
					// just ignore, the stem results will be blank
				}
				return wsFindStemsResults;
			}
	
			if (searchByName) {
				boolean oneLevel = StringUtils.equals("ONE_LEVEL",
						parentStemNameScope);
				boolean allInSubtree = StringUtils.equals("ALL_IN_SUBTREE",
						parentStemNameScope);
				// get the stem
				Stem stem = null;
				try {
					stem = StemFinder.findByName(session, parentStemName);
				} catch (StemNotFoundException snfe) {
					// this isnt good, this is a problem
					wsFindStemsResults
							.assignResultCode(WsFindStemsResultsCode.PARENT_STEM_NOT_FOUND);
					wsFindStemsResults
							.setResultMessage("Invalid query, cant find parent stem: '"
									+ stemName + "'");
					return wsFindStemsResults;
				}
				Set<Stem> stems = null;
				if (oneLevel) {
					stems = stem.getChildStems(Scope.ONE);
				} else if (allInSubtree) {
					stems = stem.getChildStems(Scope.SUB);
				} else {
					throw new RuntimeException("Invalid stemNameScope: '"
							+ parentStemNameScope + "' must be"
							+ "one of: ONE_LEVEL, ALL_IN_SUBTREE");
				}
				// now set these to the response
				wsFindStemsResults.assignStemResult(stems);
				return wsFindStemsResults;
			}
	
			if (searchByQuery) {
	
				// if empty string, that is the root stem
				querySearchFromStemName = StringUtils
						.trimToEmpty(querySearchFromStemName);
	
				// get the stem
				Stem querySearchFromStem = null;
				try {
					querySearchFromStem = StringUtils.isBlank(querySearchFromStemName) ? null 
							: StemFinder.findByName(session, querySearchFromStemName);
				} catch (StemNotFoundException snfe) {
					// this isnt good, this is a problem
					wsFindStemsResults
							.assignResultCode(WsFindStemsResultsCode.QUERY_PARENT_STEM_NOT_FOUND);
					wsFindStemsResults
							.setResultMessage("Invalid query, cant find query stem parent: '"
									+ querySearchFromStemName + "'");
					return wsFindStemsResults;
				}
				//hack, this should probably be in the filter classes...
				if (querySearchFromStem == null) {
					querySearchFromStem = StemFinder.findRootStem(session);
				}

				Set<Stem> results = null;
				
				// five scopes, the query is searches in stem names,
				// display names, extensions, or display extensions
				boolean searchScopeByName = StringUtils.equals("NAME",
						queryScope);
				boolean searchScopeByExtension = StringUtils.equals(
						"EXTENSION", queryScope);
				boolean searchScopeByDisplayName = StringUtils.equals(
						"DISPLAY_NAME", queryScope);
				boolean searchScopeByDisplayExtension = StringUtils.equals(
						"DISPLAY_EXTENSION", queryScope);
				boolean searchScopeByAll = StringUtils.isBlank(queryScope)
				 	|| StringUtils.equals("ALL", queryScope);
			
				if (!searchScopeByName && !searchScopeByExtension
						&& !searchScopeByDisplayName
						&& !searchScopeByDisplayExtension && !searchScopeByAll) {
					throw new RuntimeException(
							"Invalid queryScope, must be one of: NAME is searching by name, "
									+ "EXTENSION is display extension, DISPLAY_NAME is display name, "
									+ "DISPLAY_EXTENSION is searching by display extension, and ALL searches in all");
				}
				
				if (searchScopeByName) {
					results = new StemNameFilter(queryTerm, querySearchFromStem).getResults(session);
				} else if (searchScopeByExtension) {
					results = new StemExtensionFilter(queryTerm, querySearchFromStem).getResults(session);
				} else if (searchScopeByDisplayExtension) {
					results = new StemDisplayExtensionFilter(queryTerm, querySearchFromStem).getResults(session);
				} else if (searchScopeByDisplayName) {
					results = new StemDisplayNameFilter(queryTerm, querySearchFromStem).getResults(session);
				} else if (searchScopeByAll) {
					results = new StemNameAnyFilter(queryTerm, querySearchFromStem).getResults(session);
				}
				
				wsFindStemsResults.assignStemResult(results);
				return wsFindStemsResults;
	
			}
	
			throw new RuntimeException("Cant find search strategy");
		} catch (Exception re) {
			wsFindStemsResults
					.assignResultCode(WsFindStemsResultsCode.EXCEPTION);
			String theError = "Problem finding stems: stemName: " + stemName + ", stemNameScope: "
					+ parentStemNameScope + ", stemUuid: " + stemUuid
					+ ", queryTerm: " + queryTerm
					+ ", querySearchFromStemName: " + querySearchFromStemName
					+ ", queryScope: " + queryScope + ", actAsSubjectLookup: "
					+ actAsSubjectLookup
					/* TODO add in param names and values */
					+ ".  ";
			wsFindStemsResults.setResultMessage(theError);
			// this is sent back to the caller anyway, so just log, and not send
			// back again
			LOG.error(theError
							+ ", wsFindStemsResults: "
							+ GrouperServiceUtils
									.toStringForLog(wsFindStemsResults
											+ ",\n"
											+ ExceptionUtils
													.getFullStackTrace(re)), re);
		} finally {
			if (session != null) {
				try {
					session.stop();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	
		if (!"T".equalsIgnoreCase(wsFindStemsResults.getSuccess())) {
	
			LOG.error(wsFindStemsResults.getResultMessage());
		}
		return wsFindStemsResults;
	
	}

	/**
	 * find a group or groups
	 * 
	 * @param groupName
	 *            search by group name (must match exactly), cannot use other
	 *            params with this
	 * @param stemName
	 *            will return groups in this stem
	 * @param stemNameScope
	 *            if searching by stem, ONE_LEVEL is for one level,
	 *            ALL_IN_SUBTREE will return all in sub tree. Required if
	 *            searching by stem
	 * @param groupUuid
	 *            search by group uuid (must match exactly), cannot use other
	 *            params with this
	 * @param queryTerm
	 *            if searching by query, this is a term that will be matched to
	 *            name, extension, etc
	 * @param querySearchFromStemName
	 *            if a stem name is put here, that will narrow the search
	 * @param queryScope
	 *            NAME is searching by name, EXTENSION is display extension,
	 *            DISPLAY_NAME is display name, DISPLAY_EXTENSION is
	 *            searching by display extension, ALL is all scopes. The
	 *            default is ALL
	 * @param actAsSubjectLookup
	 * @param paramNames
	 *            optional: reserved for future use
	 * @param paramValues
	 *            optional: reserved for future use
	 * @return the groups, or no groups if none found
	 */
	@SuppressWarnings("unchecked")
	public WsFindGroupsResults findGroups(String groupName, String stemName,
			String stemNameScope, String groupUuid, String queryTerm,
			String querySearchFromStemName, String queryScope,
			WsSubjectLookup actAsSubjectLookup, String[] paramNames,
			String[] paramValues) {

		GrouperSession session = null;
		WsFindGroupsResults wsFindGroupsResults = new WsFindGroupsResults();

		boolean searchByName = StringUtils.isNotBlank(groupName);
		boolean searchByStem = StringUtils.isNotBlank(stemName);
		boolean searchByUuid = StringUtils.isNotBlank(groupUuid);
		boolean searchByQuery = StringUtils.isNotBlank(queryTerm);

		// TODO make sure size of params and values the same

		// count the search types
		int searchTypes = (searchByName ? 1 : 0) + (searchByStem ? 1 : 0)
				+ (searchByUuid ? 1 : 0) + (searchByQuery ? 1 : 0);
		// must only search by one type
		if (searchTypes != 1) {
			wsFindGroupsResults
					.assignResultCode(WsFindGroupsResultCode.INVALID_QUERY);
			wsFindGroupsResults
					.setResultMessage("Invalid query, only query on one thing, not multiple.  "
							+ "Only search by name, stem, uuid, or query");
			return wsFindGroupsResults;
		}

		// assume success
		wsFindGroupsResults.assignResultCode(WsFindGroupsResultCode.SUCCESS);

		Subject actAsSubject = null;
		try {
			actAsSubject = GrouperServiceJ2ee
					.retrieveSubjectActAs(actAsSubjectLookup);

			if (actAsSubject == null) {
				throw new RuntimeException("Cant find actAs user: "
						+ actAsSubjectLookup);
			}

			// use this to be the user connected, or the user act-as
			try {
				session = GrouperSession.start(actAsSubject);
			} catch (SessionException se) {
				throw new RuntimeException("Problem with session for subject: "
						+ actAsSubject, se);
			}

			// simple search by name
			if (searchByName) {
				try {
					Group group = GroupFinder.findByName(session, groupName);
					wsFindGroupsResults.assignGroupResult(group);
				} catch (GroupNotFoundException gnfe) {
					// just ignore, the group results will be blank
				}
				return wsFindGroupsResults;
			}

			// simple search for uuid
			if (searchByUuid) {
				try {
					Group group = GroupFinder.findByUuid(session, groupUuid);
					wsFindGroupsResults.assignGroupResult(group);
				} catch (GroupNotFoundException gnfe) {
					// just ignore, the group results will be blank
				}
				return wsFindGroupsResults;
			}

			if (searchByStem) {
				boolean oneLevel = StringUtils.equals("ONE_LEVEL",
						stemNameScope);
				boolean allInSubtree = StringUtils.equals("ALL_IN_SUBTREE",
						stemNameScope);
				// get the stem
				Stem stem = null;
				try {
					stem = StemFinder.findByName(session, stemName);
				} catch (StemNotFoundException snfe) {
					// this isnt good, this is a problem
					wsFindGroupsResults
							.assignResultCode(WsFindGroupsResultCode.STEM_NOT_FOUND);
					wsFindGroupsResults
							.setResultMessage("Invalid query, cant find stem: '"
									+ stemName + "'");
					return wsFindGroupsResults;
				}
				Set<Group> groups = null;
				if (oneLevel) {
					groups = stem.getChildGroups(Scope.ONE);
				} else if (allInSubtree) {
					groups = stem.getChildGroups(Scope.SUB);
				} else {
					throw new RuntimeException("Invalid stemNameScope: '"
							+ stemNameScope + "' must be"
							+ "one of: ONE_LEVEL, ALL_IN_SUBTREE");
				}
				// now set these to the response
				wsFindGroupsResults.assignGroupResult(groups);
				return wsFindGroupsResults;
			}

			if (searchByQuery) {

				// if empty string, that is the root stem
				querySearchFromStemName = StringUtils
						.trimToEmpty(querySearchFromStemName);

				// get the stem
				Stem querySearchFromStem = null;
				try {
					querySearchFromStem = StringUtils.isBlank(querySearchFromStemName) ? null 
							: StemFinder.findByName(session, querySearchFromStemName);
				} catch (StemNotFoundException snfe) {
					// this isnt good, this is a problem
					wsFindGroupsResults
							.assignResultCode(WsFindGroupsResultCode.STEM_NOT_FOUND);
					wsFindGroupsResults
							.setResultMessage("Invalid query, cant find stem: '"
									+ querySearchFromStemName + "'");
					return wsFindGroupsResults;
				}
				if (querySearchFromStem == null) {
					querySearchFromStem = StemFinder.findRootStem(session);
				}
				GrouperQuery grouperQuery = null;

				// see if there is a particular query scope
				if (!StringUtils.isBlank(queryScope) && !StringUtils.equals("ALL", queryScope)) {
					// four scopes, the query is searches in group names,
					// display names, extensions, or display extensions
					boolean searchScopeByName = StringUtils.equals("NAME",
							queryScope);
					boolean searchScopeByExtension = StringUtils.equals(
							"EXTENSION", queryScope);
					boolean searchScopeByDisplayName = StringUtils.equals(
							"DISPLAY_NAME", queryScope);
					boolean searchScopeByDisplayExtension = StringUtils.equals(
							"DISPLAY_EXTENSION", queryScope);

					if (!searchScopeByName && !searchScopeByExtension
							&& !searchScopeByDisplayName
							&& !searchScopeByDisplayExtension) {
						throw new RuntimeException(
								"Invalid queryScope, must be one of: NAME is searching by name, "
										+ "EXTENSION is display extension, DISPLAY_NAME is display name, AND "
										+ "DISPLAY_EXTENSION is searching by display extension");
					}
					String attribute = null;
					attribute = searchScopeByName ? "name" : attribute;
					attribute = searchScopeByExtension ? "extension"
							: attribute;
					attribute = searchScopeByDisplayName ? "displayName"
							: attribute;
					attribute = searchScopeByDisplayExtension ? "displayExtension"
							: attribute;

					grouperQuery = GrouperQuery.createQuery(session,
							new GroupAttributeFilter(attribute, queryTerm,
									querySearchFromStem));
				} else {
					// not constrained to a particular scope
					grouperQuery = GrouperQuery.createQuery(session,
							new GroupAnyAttributeFilter(queryTerm,
									querySearchFromStem));

				}
				Set<Group> results = grouperQuery.getGroups();
				wsFindGroupsResults.assignGroupResult(results);
				return wsFindGroupsResults;

			}

			throw new RuntimeException("Cant find search strategy");
		} catch (Exception re) {
			wsFindGroupsResults
					.assignResultCode(WsFindGroupsResultCode.EXCEPTION);
			String theError = "Problem finding group: groupName: " + groupName
					+ ", stemName: " + stemName + ", stemNameScope: "
					+ stemNameScope + ", groupUuid: " + groupUuid
					+ ", queryTerm: " + queryTerm
					+ ", querySearchFromStemName: " + querySearchFromStemName
					+ ", queryScope: " + queryScope + ", actAsSubjectLookup: "
					+ actAsSubjectLookup
					/* TODO add in param names and values */
					+ ".  ";
			wsFindGroupsResults.setResultMessage(theError);
			// this is sent back to the caller anyway, so just log, and not send
			// back again
			LOG
					.error(theError
							+ ", wsFindGroupsResults: "
							+ GrouperServiceUtils
									.toStringForLog(wsFindGroupsResults
											+ ",\n"
											+ ExceptionUtils
													.getFullStackTrace(re)), re);
		} finally {
			if (session != null) {
				try {
					session.stop();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		if (!"T".equalsIgnoreCase(wsFindGroupsResults.getSuccess())) {

			LOG.error(wsFindGroupsResults.getResultMessage());
		}
		return wsFindGroupsResults;

	}

	/**
	 * get memberships from a group based on a filter (all, immediate only,
	 * effective only, composite)
	 * 
	 * @param groupName
	 *            to lookup the group (mutually exclusive with groupUuid)
	 * @param groupUuid
	 *            to lookup the group (mutually exclusive with groupName)
	 * @param membershipFilter
	 *            must be one of All, Effective, Immediate, Composite
	 * @param retrieveExtendedSubjectData
	 *            true|false, for if the extended subject information should be
	 *            returned (anything more than just the id)
	 * @param actAsSubjectId
	 *            optional: is the subject id of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param actAsSubjectIdentifier
	 *            optional: is the subject identifier of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param paramName0
	 *            reserved for future use
	 * @param paramValue0
	 *            reserved for future use
	 * @param paramName1
	 *            reserved for future use
	 * @param paramValue1
	 *            reserved for future use
	 * @return the memberships, or none if none found
	 */
	public WsGetMembershipsResults getMembershipsSimple(String groupName,
			String groupUuid, String membershipFilter,
			String retrieveExtendedSubjectData, String actAsSubjectId,
			String actAsSubjectIdentifier, String paramName0,
			String paramValue0, String paramName1, String paramValue1) {

		// setup the group lookup
		WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);

		WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(
				actAsSubjectId, actAsSubjectIdentifier);

		String[][] params = GrouperServiceUtils.params(paramName0, paramValue0,
				paramName1, paramValue1);
		String[] paramNames = params[0];
		String[] paramValues = params[1];

		// pass through to the more comprehensive method
		WsGetMembershipsResults wsGetMembershipsResults = getMemberships(
				wsGroupLookup, membershipFilter, retrieveExtendedSubjectData,
				actAsSubjectLookup, paramNames, paramValues);

		return wsGetMembershipsResults;
	}

	/**
	 * get members from a group based on a filter (all, immediate only,
	 * effective only, composite)
	 * 
	 * @param groupName
	 *            to lookup the group (mutually exclusive with groupUuid)
	 * @param groupUuid
	 *            to lookup the group (mutually exclusive with groupName)
	 * @param memberFilter
	 *            must be one of All, Effective, Immediate, Composite
	 * @param retrieveExtendedSubjectData
	 *            true|false, for if the extended subject information should be
	 *            returned (anything more than just the id)
	 * @param actAsSubjectId
	 *            optional: is the subject id of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param actAsSubjectIdentifier
	 *            optional: is the subject identifier of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param paramName0
	 *            reserved for future use
	 * @param paramValue0
	 *            reserved for future use
	 * @param paramName1
	 *            reserved for future use
	 * @param paramValue1
	 *            reserved for future use
	 * @return the members, or no members if none found
	 */
	public WsGetMembersResults getMembersSimple(String groupName,
			String groupUuid, String memberFilter,
			String retrieveExtendedSubjectData, String actAsSubjectId,
			String actAsSubjectIdentifier, String paramName0,
			String paramValue0, String paramName1, String paramValue1) {

		// setup the group lookup
		WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);

		WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(
				actAsSubjectId, actAsSubjectIdentifier);

		String[][] params = GrouperServiceUtils.params(paramName0, paramValue0,
				paramName1, paramValue1);
		String[] paramNames = params[0];
		String[] paramValues = params[1];

		// pass through to the more comprehensive method
		WsGetMembersResults wsGetMembersResults = getMembers(wsGroupLookup,
				memberFilter, retrieveExtendedSubjectData, actAsSubjectLookup,
				paramNames, paramValues);

		return wsGetMembersResults;
	}

	/**
	 * get members from a group based on a filter (all, immediate only,
	 * effective only, composite)
	 * 
	 * @param wsGroupLookup
	 * @param memberFilter
	 *            must be one of All, Effective, Immediate, Composite
	 * @param retrieveExtendedSubjectData
	 *            true|false, for if the extended subject information should be
	 *            returned (anything more than just the id)
	 * @param actAsSubjectLookup
	 * @param paramNames
	 *            optional: reserved for future use
	 * @param paramValues
	 *            optional: reserved for future use
	 * @return the results
	 */
	@SuppressWarnings("unchecked")
	public WsGetMembersResults getMembers(WsGroupLookup wsGroupLookup,
			String memberFilter, String retrieveExtendedSubjectData,
			WsSubjectLookup actAsSubjectLookup, String[] paramNames,
			String[] paramValues) {

		GrouperSession session = null;
		WsGetMembersResults wsGetMembersResults = new WsGetMembersResults();
		WsMemberFilter wsMemberFilter = null;
		String parseWsFilterError = "";
		try {
			wsMemberFilter = WsMemberFilter.valueOfIgnoreCase(memberFilter);
		} catch (Exception e) {
			parseWsFilterError = ExceptionUtils.getFullStackTrace(e);
		}
		if (wsMemberFilter == null || !StringUtils.isBlank(parseWsFilterError)) {
			wsGetMembersResults
					.assignResultCode(WsGetMembersResultCode.INVALID_QUERY);
			wsGetMembersResults
					.appendResultMessage("memberFilter is required and must be valid: "
							+ parseWsFilterError);
			return wsGetMembersResults;
		}

		boolean retrieveExtendedSubjectDataBoolean = false;

		try {
			retrieveExtendedSubjectDataBoolean = GrouperServiceUtils
					.booleanValue(retrieveExtendedSubjectData, false);
		} catch (Exception e) {
			wsGetMembersResults
					.assignResultCode(WsGetMembersResultCode.INVALID_QUERY);
			wsGetMembersResults
					.appendResultMessage("retrieveExtendedSubjectData is invalid: '"
							+ retrieveExtendedSubjectData + "'");
			return wsGetMembersResults;
		}

		// TODO make sure size of params and values the same

		// assume success
		wsGetMembersResults.assignResultCode(WsGetMembersResultCode.SUCCESS);
		Subject actAsSubject = null;
		try {
			actAsSubject = GrouperServiceJ2ee
					.retrieveSubjectActAs(actAsSubjectLookup);

			if (actAsSubject == null) {
				throw new RuntimeException("Cant find actAs user: "
						+ actAsSubjectLookup);
			}

			// use this to be the user connected, or the user act-as
			try {
				session = GrouperSession.start(actAsSubject);
			} catch (SessionException se) {
				throw new RuntimeException("Problem with session for subject: "
						+ actAsSubject, se);
			}
			wsGroupLookup.retrieveGroupIfNeeded(session);

			Group group = wsGroupLookup.retrieveGroup();

			if (group == null) {
				wsGetMembersResults
						.assignResultCode(WsGetMembersResultCode.INVALID_QUERY);
				wsGetMembersResults.appendResultMessage("Cant find group: "
						+ wsGroupLookup + ".  ");
				return wsGetMembersResults;
			}

			// lets get the members, cant be null
			Set<Member> members = wsMemberFilter.getMembers(group);

			// lets set the attribute names if they exist
			{
				// see if attribute0
				String attributeName0 = GrouperWsConfig
						.getPropertyString(GrouperWsConfig.WS_GET_MEMBERS_ATTRIBUTE0);
				if (!StringUtils.isBlank(attributeName0)) {
					wsGetMembersResults.setAttributeName0(attributeName0);
				}
			}

			// lets set the attribute names if they exist
			{
				// see if attribute1
				String attributeName1 = GrouperWsConfig
						.getPropertyString(GrouperWsConfig.WS_GET_MEMBERS_ATTRIBUTE1);
				if (!StringUtils.isBlank(attributeName1)) {
					wsGetMembersResults.setAttributeName1(attributeName1);
				}
			}

			// lets set the attribute names if they exist
			{
				// see if attribute2
				String attributeName2 = GrouperWsConfig
						.getPropertyString(GrouperWsConfig.WS_GET_MEMBERS_ATTRIBUTE2);
				if (!StringUtils.isBlank(attributeName2)) {
					wsGetMembersResults.setAttributeName2(attributeName2);
				}
			}

			// if we dont have anything, then lets just return (already success)
			if (members.size() == 0) {
				return wsGetMembersResults;
			}

			wsGetMembersResults.setResults(new WsGetMembersResult[members
					.size()]);

			int resultIndex = 0;

			// loop through and set the result
			for (Member member : members) {
				WsGetMembersResult wsGetMembersResult = new WsGetMembersResult(
						member, retrieveExtendedSubjectDataBoolean);
				wsGetMembersResults.getResults()[resultIndex++] = wsGetMembersResult;
			}

		} catch (RuntimeException re) {

			wsGetMembersResults
					.assignResultCode(WsGetMembersResultCode.EXCEPTION);
			String theError = "Problem getting members from group: wsGroupLookup: "
					+ wsGroupLookup
					+ ", memberFilter: "
					+ memberFilter
					+ ", retrieveExtendedSubjectData: "
					+ retrieveExtendedSubjectData
					+ ", actAsSubject: "
					+ actAsSubject + ".  ";
			wsGetMembersResults.appendResultMessage(theError + "\n"
					+ ExceptionUtils.getFullStackTrace(re) + ".  ");
			// this is sent back to the caller anyway, so just log, and not send
			// back again
			LOG.error(theError + ", wsGetMemberResults: "
					+ GrouperServiceUtils.toStringForLog(wsGetMembersResults),
					re);
		} finally {
			if (session != null) {
				try {
					session.stop();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		return wsGetMembersResults;
	}

	/**
	 * get memberships from a group based on a filter (all, immediate only,
	 * effective only, composite)
	 * 
	 * @param wsGroupLookup
	 * @param membershipFilter
	 *            must be one of All, Effective, Immediate, Composite
	 * @param retrieveExtendedSubjectData
	 *            true|false, for if the extended subject information should be
	 *            returned (anything more than just the id)
	 * @param actAsSubjectLookup
	 * @param paramNames
	 *            optional: reserved for future use
	 * @param paramValues
	 *            optional: reserved for future use
	 * @return the results
	 */
	@SuppressWarnings("unchecked")
	public WsGetMembershipsResults getMemberships(WsGroupLookup wsGroupLookup,
			String membershipFilter, String retrieveExtendedSubjectData,
			WsSubjectLookup actAsSubjectLookup, String[] paramNames,
			String[] paramValues) {
		// TODO allow input based on subject or group or both
		GrouperSession session = null;
		WsGetMembershipsResults wsGetMembershipsResults = new WsGetMembershipsResults();
		WsMemberFilter wsMembershipFilter = null;
		String parseWsFilterError = "";
		try {
			wsMembershipFilter = WsMemberFilter
					.valueOfIgnoreCase(membershipFilter);
		} catch (Exception e) {
			parseWsFilterError = ExceptionUtils.getFullStackTrace(e);
		}
		if (wsMembershipFilter == null
				|| !StringUtils.isBlank(parseWsFilterError)) {
			wsGetMembershipsResults
					.assignResultCode(WsGetMembershipsResultCode.INVALID_QUERY);
			wsGetMembershipsResults
					.appendResultMessage("membershipFilter is required and must be valid: "
							+ parseWsFilterError);
			return wsGetMembershipsResults;
		}

		boolean retrieveExtendedSubjectDataBoolean = false;

		try {
			retrieveExtendedSubjectDataBoolean = GrouperServiceUtils
					.booleanValue(retrieveExtendedSubjectData, false);
		} catch (Exception e) {
			wsGetMembershipsResults
					.assignResultCode(WsGetMembershipsResultCode.INVALID_QUERY);
			wsGetMembershipsResults
					.appendResultMessage("retrieveExtendedSubjectData is invalid: '"
							+ retrieveExtendedSubjectData + "'");
			return wsGetMembershipsResults;
		}

		// TODO make sure size of params and values the same

		// assume success
		wsGetMembershipsResults
				.assignResultCode(WsGetMembershipsResultCode.SUCCESS);
		Subject actAsSubject = null;
		try {
			actAsSubject = GrouperServiceJ2ee
					.retrieveSubjectActAs(actAsSubjectLookup);

			if (actAsSubject == null) {
				throw new RuntimeException("Cant find actAs user: "
						+ actAsSubjectLookup);
			}

			// use this to be the user connected, or the user act-as
			try {
				session = GrouperSession.start(actAsSubject);
			} catch (SessionException se) {
				throw new RuntimeException("Problem with session for subject: "
						+ actAsSubject, se);
			}
			wsGroupLookup.retrieveGroupIfNeeded(session);

			Group group = wsGroupLookup.retrieveGroup();

			if (group == null) {
				wsGetMembershipsResults
						.assignResultCode(WsGetMembershipsResultCode.INVALID_QUERY);
				wsGetMembershipsResults.appendResultMessage("Cant find group: "
						+ wsGroupLookup + ".  ");
				return wsGetMembershipsResults;
			}

			// lets get the members, cant be null
			Set<Membership> memberships = wsMembershipFilter
					.getMemberships(group);

			// lets set the attribute names if they exist
			{
				// see if attribute0
				String attributeName0 = GrouperWsConfig
						.getPropertyString(GrouperWsConfig.WS_GET_MEMBERSHIPS_ATTRIBUTE0);
				if (!StringUtils.isBlank(attributeName0)) {
					wsGetMembershipsResults.setAttributeName0(attributeName0);
				}
			}

			// lets set the attribute names if they exist
			{
				// see if attribute1
				String attributeName1 = GrouperWsConfig
						.getPropertyString(GrouperWsConfig.WS_GET_MEMBERSHIPS_ATTRIBUTE1);
				if (!StringUtils.isBlank(attributeName1)) {
					wsGetMembershipsResults.setAttributeName1(attributeName1);
				}
			}

			// lets set the attribute names if they exist
			{
				// see if attribute2
				String attributeName2 = GrouperWsConfig
						.getPropertyString(GrouperWsConfig.WS_GET_MEMBERSHIPS_ATTRIBUTE2);
				if (!StringUtils.isBlank(attributeName2)) {
					wsGetMembershipsResults.setAttributeName2(attributeName2);
				}
			}

			// if we dont have anything, then lets just return (already success)
			if (memberships.size() == 0) {
				return wsGetMembershipsResults;
			}

			wsGetMembershipsResults
					.setResults(new WsGetMembershipsResult[memberships.size()]);

			int resultIndex = 0;

			// loop through and set the result
			for (Membership membership : memberships) {
				WsGetMembershipsResult wsGetMembershipsResult = new WsGetMembershipsResult(
						membership, retrieveExtendedSubjectDataBoolean);
				wsGetMembershipsResults.getResults()[resultIndex++] = wsGetMembershipsResult;
			}

		} catch (RuntimeException re) {

			wsGetMembershipsResults
					.assignResultCode(WsGetMembershipsResultCode.EXCEPTION);
			String theError = "Problem getting memberships from group: wsGroupLookup: "
					+ wsGroupLookup
					+ ", memberFilter: "
					+ membershipFilter
					+ ", retrieveExtendedSubjectData: "
					+ retrieveExtendedSubjectData
					+ ", actAsSubject: "
					+ actAsSubject + ".  ";
			wsGetMembershipsResults.appendResultMessage(theError + "\n"
					+ ExceptionUtils.getFullStackTrace(re) + ".  ");
			// this is sent back to the caller anyway, so just log, and not send
			// back again
			LOG.error(theError
					+ ", wsGetMembershipResults: "
					+ GrouperServiceUtils
							.toStringForLog(wsGetMembershipsResults), re);
		} finally {
			if (session != null) {
				try {
					session.stop();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		return wsGetMembershipsResults;
	}

	/**
	 * get groups from members based on filter (accepts batch of members)
	 * 
	 * @param subjectLookup
	 * @param subjectLookups
	 *            subjects to be examined to see if in group
	 * @param memberFilter
	 *            can be All, Effective (non immediate), Immediate (direct),
	 *            Composite (if composite group with group math (union, minus,
	 *            etc)
	 * @param actAsSubjectLookup
	 *            to act as a different user than the logged in user
	 * @param paramNames
	 *            optional: reserved for future use
	 * @param paramValues
	 *            optional: reserved for future use
	 * @return the results
	 */
	@SuppressWarnings("unchecked")
	public WsGetGroupsResults getGroups(WsSubjectLookup subjectLookup,
			String memberFilter, WsSubjectLookup actAsSubjectLookup,
			String[] paramNames, String[] paramValues) {

		GrouperSession session = null;
		WsGetGroupsResults wsGetGroupsResults = new WsGetGroupsResults();

		WsMemberFilter wsMemberFilter = null;
		String parseWsFilterError = "";
		try {
			wsMemberFilter = WsMemberFilter.valueOfIgnoreCase(memberFilter);
		} catch (Exception e) {
			parseWsFilterError = ExceptionUtils.getFullStackTrace(e);
		}
		if (wsMemberFilter == null || !StringUtils.isBlank(parseWsFilterError)) {
			wsGetGroupsResults
					.assignResultCode(WsGetGroupsResultsCode.INVALID_QUERY);
			wsGetGroupsResults
					.appendResultMessage("membershipFilter is required and must be valid: '"
							+ memberFilter + "'\n" + parseWsFilterError);
			return wsGetGroupsResults;
		}
		Subject subject = subjectLookup.retrieveSubject();

		// make sure the subject is there
		if (subject == null) {
			// see why not
			SubjectFindResult subjectFindResult = subjectLookup
					.retrieveSubjectFindResult();
			String error = "Subject: " + subjectLookup + " had problems: "
					+ subjectFindResult;
			wsGetGroupsResults.setResultMessage(error);

			if (subjectFindResult == SubjectFindResult.INVALID_QUERY) {
				wsGetGroupsResults
						.assignResultCode(WsGetGroupsResultsCode.INVALID_QUERY);
			} else if (subjectFindResult == SubjectFindResult.SUBJECT_NOT_FOUND) {
				wsGetGroupsResults
						.assignResultCode(WsGetGroupsResultsCode.SUBJECT_NOT_FOUND);
			} else if (subjectFindResult == SubjectFindResult.SUBJECT_DUPLICATE) {
				wsGetGroupsResults
						.assignResultCode(WsGetGroupsResultsCode.SUBJECT_DUPLICATE);
			} else {
				wsGetGroupsResults
						.assignResultCode(WsGetGroupsResultsCode.EXCEPTION);
			}
			return wsGetGroupsResults;
		}
		// TODO make sure size of params and values the same

		// assume success
		wsGetGroupsResults.assignResultCode(WsGetGroupsResultsCode.SUCCESS);
		Subject actAsSubject = null;
		try {
			actAsSubject = GrouperServiceJ2ee
					.retrieveSubjectActAs(actAsSubjectLookup);

			if (actAsSubject == null) {
				throw new RuntimeException("Cant find actAs user: "
						+ actAsSubjectLookup);
			}

			// use this to be the user connected, or the user act-as
			try {
				session = GrouperSession.start(actAsSubject);
			} catch (SessionException se) {
				throw new RuntimeException("Problem with session for subject: "
						+ actAsSubject, se);
			}

			Member member = null;

			try {
				member = MemberFinder.findBySubject(session, subject);
				if (member == null) {
					throw new NullPointerException(
							"Member is null after findBySubject");
				}
			} catch (MemberNotFoundException mvfe) {
				String error = "Subject: " + subjectLookup + " had problems";
				wsGetGroupsResults.setResultMessage(error);
				wsGetGroupsResults
						.assignResultCode(WsGetGroupsResultsCode.MEMBER_NOT_FOUND);
				return wsGetGroupsResults;
			}

			Set<Group> groups = wsMemberFilter.getGroups(member);
			int groupsSize = groups.size();
			if (groupsSize == 0) {
				// no groups, just return
				wsGetGroupsResults
						.assignResultCode(WsGetGroupsResultsCode.SUCCESS);
				return wsGetGroupsResults;
			}

			wsGetGroupsResults.setResults(new WsGetGroupsResult[groupsSize]);
			int resultIndex = 0;

			for (Group group : groups) {
				WsGetGroupsResult wsGetGroupsResult = new WsGetGroupsResult(
						group);
				wsGetGroupsResults.getResults()[resultIndex++] = wsGetGroupsResult;
			}

		} catch (RuntimeException re) {
			wsGetGroupsResults
					.assignResultCode(WsGetGroupsResultsCode.EXCEPTION);
			String theError = "Problem getting groups: subject: " + subject
					+ ", memberFilter: " + memberFilter + ", actAsSubject: "
					+ actAsSubject + ".  ";
			wsGetGroupsResults.appendResultMessage(theError + "\n"
					+ ExceptionUtils.getFullStackTrace(re) + "\n");
			// this is sent back to the caller anyway, so just log, and not send
			// back again
			LOG.error(theError + ", wsGetGroupsResults: "
					+ GrouperServiceUtils.toStringForLog(wsGetGroupsResults),
					re);
		} finally {
			if (session != null) {
				try {
					session.stop();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		return wsGetGroupsResults;
	}

	/**
	 * see if a group has members based on filter (accepts batch of members)
	 * 
	 * @param wsGroupLookup
	 *            for the group to see if the members are in there
	 * @param subjectLookups
	 *            subjects to be examined to see if in group
	 * @param memberFilter
	 *            can be All, Effective (non immediate), Immediate (direct),
	 *            Composite (if composite group with group math (union, minus,
	 *            etc)
	 * @param actAsSubjectLookup
	 *            to act as a different user than the logged in user
	 * @param fieldName
	 *            is if the Group.hasMember() method with field is to be called
	 *            (e.g. admins, optouts, optins, etc from Field table in DB)
	 * @param paramNames
	 *            optional: reserved for future use
	 * @param paramValues
	 *            optional: reserved for future use
	 * @return the results
	 */
	@SuppressWarnings("unchecked")
	public WsHasMemberResults hasMember(WsGroupLookup wsGroupLookup,
			WsSubjectLookup[] subjectLookups, String memberFilter,
			WsSubjectLookup actAsSubjectLookup, String fieldName,
			String[] paramNames, String[] paramValues) {

		GrouperSession session = null;
		WsHasMemberResults wsHasMemberResults = new WsHasMemberResults();
		int subjectLength = subjectLookups == null ? 0 : subjectLookups.length;
		if (subjectLength == 0) {
			wsHasMemberResults
					.assignResultCode(WsHasMemberResultsCode.INVALID_QUERY);
			wsHasMemberResults
					.appendResultMessage("Subject length must be more than 1");
			return wsHasMemberResults;
		}

		// see if greater than the max (or default)
		int maxHasMember = GrouperWsConfig.getPropertyInt(
				GrouperWsConfig.WS_HAS_MEMBER_SUBJECTS_MAX, 1000000);
		if (subjectLength > maxHasMember) {
			wsHasMemberResults
					.assignResultCode(WsHasMemberResultsCode.INVALID_QUERY);
			wsHasMemberResults
					.appendResultMessage("Subject length must be less than max: "
							+ maxHasMember + " (sent in " + subjectLength + ")");
			return wsHasMemberResults;
		}

		WsMemberFilter wsMemberFilter = null;
		String parseWsFilterError = "";
		try {
			wsMemberFilter = WsMemberFilter.valueOfIgnoreCase(memberFilter);
		} catch (Exception e) {
			parseWsFilterError = ExceptionUtils.getFullStackTrace(e);
		}
		if (wsMemberFilter == null || !StringUtils.isBlank(parseWsFilterError)) {
			wsHasMemberResults
					.assignResultCode(WsHasMemberResultsCode.INVALID_QUERY);
			wsHasMemberResults
					.appendResultMessage("membershipFilter is required and must be valid: '"
							+ memberFilter + "'\n" + parseWsFilterError);
			return wsHasMemberResults;
		}

		Field field = null;
		if (!StringUtils.isBlank(fieldName)) {
			try {
				field = FieldFinder.find(fieldName);
				if (field == null) {
					throw new RuntimeException("Field couldnt be found: null");
				}
			} catch (Exception e) {
				wsHasMemberResults
						.assignResultCode(WsHasMemberResultsCode.INVALID_QUERY);
				wsHasMemberResults
						.appendResultMessage("problems retrieving field: '"
								+ fieldName + "'\n"
								+ ExceptionUtils.getFullStackTrace(e));
				return wsHasMemberResults;
			}

		}

		// TODO make sure size of params and values the same

		// assume success
		wsHasMemberResults.assignResultCode(WsHasMemberResultsCode.SUCCESS);
		Subject actAsSubject = null;
		try {
			actAsSubject = GrouperServiceJ2ee
					.retrieveSubjectActAs(actAsSubjectLookup);

			if (actAsSubject == null) {
				throw new RuntimeException("Cant find actAs user: "
						+ actAsSubjectLookup);
			}

			// use this to be the user connected, or the user act-as
			try {
				session = GrouperSession.start(actAsSubject);
			} catch (SessionException se) {
				throw new RuntimeException("Problem with session for subject: "
						+ actAsSubject, se);
			}
			wsGroupLookup.retrieveGroupIfNeeded(session);
			wsHasMemberResults.setResults(new WsHasMemberResult[subjectLength]);
			Group group = wsGroupLookup.retrieveGroup();

			if (group == null) {
				wsHasMemberResults
						.assignResultCode(WsHasMemberResultsCode.INVALID_QUERY);
				wsHasMemberResults.appendResultMessage("Cant find group: "
						+ wsGroupLookup + ".  ");
				return wsHasMemberResults;
			}

			int resultIndex = 0;

			for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
				WsHasMemberResult wsHasMemberResult = new WsHasMemberResult();
				wsHasMemberResults.getResults()[resultIndex++] = wsHasMemberResult;
				try {

					wsHasMemberResult.setSubjectId(wsSubjectLookup
							.getSubjectId());
					wsHasMemberResult.setSubjectIdentifier(wsSubjectLookup
							.getSubjectIdentifier());

					Subject subject = wsSubjectLookup.retrieveSubject();

					// make sure the subject is there
					if (subject == null) {
						// see why not
						SubjectFindResult subjectFindResult = wsSubjectLookup
								.retrieveSubjectFindResult();
						String error = "Subject: " + wsSubjectLookup
								+ " had problems: " + subjectFindResult;
						wsHasMemberResult.setResultMessage(error);
						if (subjectFindResult == SubjectFindResult.SUBJECT_NOT_FOUND) {
							wsHasMemberResult
									.assignResultCode(WsHasMemberResultCode.MEMBER_NOT_FOUND);
						} else {
							// something bad happened
							wsHasMemberResult
									.assignResultCode(WsHasMemberResultCode.EXCEPTION);
						}
						continue;
					}

					// these will probably match, but just in case
					if (StringUtils.isBlank(wsHasMemberResult.getSubjectId())) {
						wsHasMemberResult.setSubjectId(subject.getId());
					}

					boolean hasMember = field == null ? wsMemberFilter
							.hasMember(group, subject) : wsMemberFilter
							.hasMember(group, subject, field);
					wsHasMemberResult
							.assignResultCode(hasMember ? WsHasMemberResultCode.IS_MEMBER
									: WsHasMemberResultCode.IS_NOT_MEMBER);

				} catch (Exception e) {
					wsHasMemberResult.setResultCode("EXCEPTION");
					wsHasMemberResult.setResultMessage(ExceptionUtils
							.getFullStackTrace(e));
					LOG.error(wsSubjectLookup + ", " + e, e);
				}
			}

		} catch (RuntimeException re) {
			wsHasMemberResults
					.assignResultCode(WsHasMemberResultsCode.EXCEPTION);
			String theError = "Problem querying member from group: wsGroupLookup: "
					+ wsGroupLookup
					+ ", subjectLookups: "
					+ GrouperServiceUtils.toStringForLog(subjectLookups)
					+ ", actAsSubject: " + actAsSubject + ".  ";
			wsHasMemberResults.appendResultMessage(theError);
			// this is sent back to the caller anyway, so just log, and not send
			// back again
			LOG.error(theError + ", wsHasMemberResults: "
					+ GrouperServiceUtils.toStringForLog(wsHasMemberResults),
					re);
		} finally {
			if (session != null) {
				try {
					session.stop();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		if (wsHasMemberResults.getResults() != null) {
			// check all entries
			int successes = 0;
			int failures = 0;
			for (WsHasMemberResult wsHasMemberResult : wsHasMemberResults
					.getResults()) {
				boolean success = "T".equalsIgnoreCase(wsHasMemberResult
						.getSuccess());
				if (success) {
					successes++;
				} else {
					failures++;
				}
			}
			if (failures > 0) {
				wsHasMemberResults
						.appendResultMessage("There were "
								+ successes
								+ " successes and "
								+ failures
								+ " failures of users queried hasMember from the group.   ");
				wsHasMemberResults
						.assignResultCode(WsHasMemberResultsCode.PROBLEM_WITH_QUERY);
			} else {
				wsHasMemberResults
						.assignResultCode(WsHasMemberResultsCode.SUCCESS);
			}
		}
		if (!"T".equalsIgnoreCase(wsHasMemberResults.getSuccess())) {

			LOG.error(wsHasMemberResults.getResultMessage());
		}
		return wsHasMemberResults;
	}

	/**
	 * delete a stem or many (if doesnt exist, ignore)
	 * 
	 * @param stemName
	 *            to delete the stem (mutually exclusive with stemUuid)
	 * @param stemUuid
	 *            to delete the stem (mutually exclusive with stemName)
	 * @param actAsSubjectId
	 *            optional: is the subject id of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param actAsSubjectIdentifier
	 *            optional: is the subject identifier of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param paramName0
	 *            reserved for future use
	 * @param paramValue0
	 *            reserved for future use
	 * @param paramName1
	 *            reserved for future use
	 * @param paramValue1
	 *            reserved for future use
	 * @return the result of one member add
	 */
	public WsStemDeleteResult stemDeleteSimple(String stemName,
			String stemUuid, String actAsSubjectId,
			String actAsSubjectIdentifier, String paramName0,
			String paramValue0, String paramName1, String paramValue1) {
	
		// setup the stem lookup
		WsStemLookup wsStemLookup = new WsStemLookup(stemName, stemUuid);
		WsStemLookup[] wsStemLookups = new WsStemLookup[] { wsStemLookup };
		WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(
				actAsSubjectId, actAsSubjectIdentifier);
	
		String[][] params = GrouperServiceUtils.params(paramName0, paramValue0,
				paramName1, paramValue1);
		String[] paramNames = params[0];
		String[] paramValues = params[1];
	
		WsStemDeleteResults wsStemDeleteResults = stemDelete(wsStemLookups,
				actAsSubjectLookup, paramNames, paramValues);
	
		WsStemDeleteResult[] results = wsStemDeleteResults.getResults();
		if (results != null && results.length > 0) {
			return results[0];
		}
		// didnt even get that far to where there is a subject result
		WsStemDeleteResult wsStemDeleteResult = new WsStemDeleteResult();
		wsStemDeleteResult.setResultMessage(wsStemDeleteResults
				.getResultMessage());
	
		// convert the outer code to the inner code
		WsStemDeleteResultsCode wsStemDeleteResultsCode = wsStemDeleteResults
				.retrieveResultCode();
		wsStemDeleteResult
				.assignResultCode(wsStemDeleteResultsCode == null ? WsStemDeleteResultCode.EXCEPTION
						: wsStemDeleteResultsCode.convertToResultCode());
	
		wsStemDeleteResult.setStemName(stemName);
		wsStemDeleteResult.setStemUuid(stemUuid);
	
		// definitely not a success
		wsStemDeleteResult.setSuccess("F");
	
		return wsStemDeleteResult;
	
	}

	/**
	 * delete a group or many (if doesnt exist, ignore)
	 * 
	 * @param groupName
	 *            to delete the group (mutually exclusive with groupUuid)
	 * @param groupUuid
	 *            to delete the group (mutually exclusive with groupName)
	 * @param actAsSubjectId
	 *            optional: is the subject id of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param actAsSubjectIdentifier
	 *            optional: is the subject identifier of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param paramName0
	 *            reserved for future use
	 * @param paramValue0
	 *            reserved for future use
	 * @param paramName1
	 *            reserved for future use
	 * @param paramValue1
	 *            reserved for future use
	 * @return the result of one member add
	 */
	public WsGroupDeleteResult groupDeleteSimple(String groupName,
			String groupUuid, String actAsSubjectId,
			String actAsSubjectIdentifier, String paramName0,
			String paramValue0, String paramName1, String paramValue1) {

		// setup the group lookup
		WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
		WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] { wsGroupLookup };
		WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(
				actAsSubjectId, actAsSubjectIdentifier);

		String[][] params = GrouperServiceUtils.params(paramName0, paramValue0,
				paramName1, paramValue1);
		String[] paramNames = params[0];
		String[] paramValues = params[1];

		WsGroupDeleteResults wsGroupDeleteResults = groupDelete(wsGroupLookups,
				actAsSubjectLookup, paramNames, paramValues);

		WsGroupDeleteResult[] results = wsGroupDeleteResults.getResults();
		if (results != null && results.length > 0) {
			return results[0];
		}
		// didnt even get that far to where there is a subject result
		WsGroupDeleteResult wsGroupDeleteResult = new WsGroupDeleteResult();
		wsGroupDeleteResult.setResultMessage(wsGroupDeleteResults
				.getResultMessage());

		// convert the outer code to the inner code
		WsGroupDeleteResultsCode wsGroupDeleteResultsCode = wsGroupDeleteResults
				.retrieveResultCode();
		wsGroupDeleteResult
				.assignResultCode(wsGroupDeleteResultsCode == null ? WsGroupDeleteResultCode.EXCEPTION
						: wsGroupDeleteResultsCode.convertToResultCode());

		wsGroupDeleteResult.setGroupName(groupName);
		wsGroupDeleteResult.setGroupUuid(groupUuid);

		// definitely not a success
		wsGroupDeleteResult.setSuccess("F");

		return wsGroupDeleteResult;

	}

	/**
	 * view or edit attributes for group.  pass in attribute names and values (and if delete), if they are null, then 
	 * just view.  
	 * 
	 * @param groupName
	 *            to delete the group (mutually exclusive with groupUuid)
	 * @param groupUuid
	 *            to delete the group (mutually exclusive with groupName)
	 * @param attributeName0 name of first attribute (optional)
	 * @param attributeValue0 value of first attribute (optional)
	 * @param attributeDelete0 if first attribute should be deleted (T|F) (optional)
	 * @param attributeName1 name of second attribute (optional)
	 * @param attributeValue1 value of second attribute (optional)
	 * @param attributeDelete1 if second attribute should be deleted (T|F) (optional)
	 * @param attributeName2 name of third attribute (optional)
	 * @param attributeValue2 value of third attribute (optional)
	 * @param attributeDelete2 if third attribute should be deleted (T|F) (optional)
	 * @param actAsSubjectId
	 *            optional: is the subject id of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param actAsSubjectIdentifier
	 *            optional: is the subject identifier of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param paramName0
	 *            reserved for future use
	 * @param paramValue0
	 *            reserved for future use
	 * @param paramName1
	 *            reserved for future use
	 * @param paramValue1
	 *            reserved for future use
	 * @return the result of one member add
	 */
	public WsViewOrEditAttributesResult viewOrEditAttributesSimple(String groupName,
			String groupUuid, String attributeName0, String attributeValue0, String attributeDelete0,
			String attributeName1, String attributeValue1, String attributeDelete1, String attributeName2,
			String attributeValue2, String attributeDelete2, String actAsSubjectId,
			String actAsSubjectIdentifier, String paramName0,
			String paramValue0, String paramName1, String paramValue1) {
	
		// setup the group lookup
		WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
		WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] { wsGroupLookup };
		
		//setup attributes
		List<WsAttributeEdit> attributeEditList = new ArrayList<WsAttributeEdit>();
		if (!StringUtils.isBlank(attributeName0) || !StringUtils.isBlank(attributeValue0)
				|| !StringUtils.isBlank(attributeDelete0)) {
			attributeEditList.add(new WsAttributeEdit(attributeName0, attributeValue0, attributeDelete0));
		}
		if (!StringUtils.isBlank(attributeName1) || !StringUtils.isBlank(attributeValue1)
				|| !StringUtils.isBlank(attributeDelete1)) {
			attributeEditList.add(new WsAttributeEdit(attributeName1, attributeValue1, attributeDelete1));
		}
		if (!StringUtils.isBlank(attributeName2) || !StringUtils.isBlank(attributeValue2)
				|| !StringUtils.isBlank(attributeDelete2)) {
			attributeEditList.add(new WsAttributeEdit(attributeName2, attributeValue2, attributeDelete2));
		}
		//convert to array
		WsAttributeEdit[] wsAttributeEdits = GrouperServiceUtils.toArray(attributeEditList, WsAttributeEdit.class);
		WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(
				actAsSubjectId, actAsSubjectIdentifier);
	
		String[][] params = GrouperServiceUtils.params(paramName0, paramValue0,
				paramName1, paramValue1);
		String[] paramNames = params[0];
		String[] paramValues = params[1];

		WsViewOrEditAttributesResults wsViewOrEditAttributesResults = viewOrEditAttributes(wsGroupLookups,wsAttributeEdits,
				actAsSubjectLookup, paramNames, paramValues);
	
		WsViewOrEditAttributesResult[] results = wsViewOrEditAttributesResults.getResults();
		if (results != null && results.length > 0) {
			return results[0];
		}
		// didnt even get that far to where there is a subject result
		WsViewOrEditAttributesResult wsViewOrEditAttributesResult = new WsViewOrEditAttributesResult();
		wsViewOrEditAttributesResult.setResultMessage(wsViewOrEditAttributesResults
				.getResultMessage());
	
		// convert the outer code to the inner code
		WsViewOrEditAttributesResultsCode wsViewOrEditAttributesResultsCode = wsViewOrEditAttributesResults
				.retrieveResultCode();
		wsViewOrEditAttributesResult
				.assignResultCode(wsViewOrEditAttributesResultsCode == null ? WsViewOrEditAttributesResultCode.EXCEPTION
						: wsViewOrEditAttributesResultsCode.convertToResultCode());
	
		wsViewOrEditAttributesResult.setGroupName(groupName);
		wsViewOrEditAttributesResult.setGroupUuid(groupUuid);
	
		// definitely not a success
		wsViewOrEditAttributesResult.setSuccess("F");
	
		return wsViewOrEditAttributesResult;
	
	}

	/**
	 * save a group (insert or update)
	 * 
	 * @see {@link Group#saveGroup(GrouperSession, String, String, String, String, boolean, boolean, boolean)}
	 * @param groupName
	 *            to delete the group (mutually exclusive with groupUuid)
	 * @param groupUuid
	 *            to delete the group (mutually exclusive with groupName)
	 * @param description
	 *            of the group, empty will be ignored
	 * @param displayExtension
	 *            display name of the group, empty will be ignored
	 * @param retrieveViaNameIfNoUuid
	 *            if to retrieve with name if uuid isnt specified, defaults to
	 *            true
	 * @param createGroupIfNotExist
	 *            if the group should be created if it doesnt exist
	 * @param createStemsIfNotExist
	 *            if the stems should be created if not exist
	 * @param actAsSubjectId
	 *            optional: is the subject id of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param actAsSubjectIdentifier
	 *            optional: is the subject identifier of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param paramName0
	 *            reserved for future use
	 * @param paramValue0
	 *            reserved for future use
	 * @param paramName1
	 *            reserved for future use
	 * @param paramValue1
	 *            reserved for future use
	 * @return the result of one member add
	 */
	public WsGroupSaveResult groupSaveSimple(String groupName,
			String groupUuid, String description, String displayExtension,
			String retrieveViaNameIfNoUuid, String createGroupIfNotExist,
			String createStemsIfNotExist, String actAsSubjectId,
			String actAsSubjectIdentifier, String paramName0,
			String paramValue0, String paramName1, String paramValue1) {

		// setup the group lookup
		WsGroupToSave wsGroupToSave = new WsGroupToSave(groupUuid, description,
				displayExtension, retrieveViaNameIfNoUuid,
				createGroupIfNotExist, createStemsIfNotExist, groupName);
		WsGroupToSave[] wsGroupsToSave = new WsGroupToSave[] { wsGroupToSave };

		WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(
				actAsSubjectId, actAsSubjectIdentifier);

		String[][] params = GrouperServiceUtils.params(paramName0, paramValue0,
				paramName1, paramValue1);
		String[] paramNames = params[0];
		String[] paramValues = params[1];

		WsGroupSaveResults wsGroupSaveResults = groupSave(wsGroupsToSave,
				actAsSubjectLookup, paramNames, paramValues);

		WsGroupSaveResult[] results = wsGroupSaveResults.getResults();
		if (results != null && results.length > 0) {
			return results[0];
		}
		// didnt even get that far to where there is a subject result
		WsGroupSaveResult wsGroupSaveResult = new WsGroupSaveResult();
		wsGroupSaveResult.setResultMessage(wsGroupSaveResults
				.getResultMessage());

		// convert the outer code to the inner code
		WsGroupSaveResultsCode wsGroupSaveResultsCode = wsGroupSaveResults
				.retrieveResultCode();
		wsGroupSaveResult
				.assignResultCode(wsGroupSaveResultsCode == null ? WsGroupSaveResultCode.EXCEPTION
						: wsGroupSaveResultsCode.convertToResultCode());

		wsGroupSaveResult.setGroupName(groupName);
		wsGroupSaveResult.setGroupUuid(groupUuid);

		// definitely not a success
		wsGroupSaveResult.setSuccess("F");

		return wsGroupSaveResult;

	}

	/**
	 * view or edit attributes for groups.  pass in attribute names and values (and if delete), if they are null, then 
	 * just view.  
	 * 
	 * @param wsGroupLookups
	 *            groups to save
	 * @param wsAttributeEdits are the attributes to change or delete
	 * @param actAsSubjectLookup
	 * @param paramNames
	 *            optional: reserved for future use
	 * @param paramValues
	 *            optional: reserved for future use
	 * @return the results
	 */
	@SuppressWarnings("unchecked")
	public WsViewOrEditAttributesResults viewOrEditAttributes(
			WsGroupLookup[] wsGroupLookups, WsAttributeEdit[] wsAttributeEdits, WsSubjectLookup actAsSubjectLookup,
			String[] paramNames, String[] paramValues) {

		GrouperSession session = null;
		int groupsSize = wsGroupLookups == null ? 0 : wsGroupLookups.length;

		WsViewOrEditAttributesResults wsViewOrEditAttributesResults = new WsViewOrEditAttributesResults();

		// see if greater than the max (or default)
		int maxAttributeGroup = GrouperWsConfig.getPropertyInt(
				GrouperWsConfig.WS_GROUP_ATTRIBUTE_MAX, 1000000);
		if (groupsSize > maxAttributeGroup) {
			wsViewOrEditAttributesResults
					.assignResultCode(WsViewOrEditAttributesResultsCode.INVALID_QUERY);
			wsViewOrEditAttributesResults
					.appendResultMessage("Number of groups must be less than max: "
							+ maxAttributeGroup + " (sent in " + groupsSize + ")");
			return wsViewOrEditAttributesResults;
		}

		// TODO make sure size of params and values the same

		//lets validate the attribute edits
		boolean readOnly = wsAttributeEdits == null || wsAttributeEdits.length == 0;
		if (!readOnly) {
			for (WsAttributeEdit wsAttributeEdit : wsAttributeEdits) {
				String errorMessage = wsAttributeEdit.validate();
				if (errorMessage != null) {
					wsViewOrEditAttributesResults.assignResultCode(WsViewOrEditAttributesResultsCode.INVALID_QUERY);
					wsViewOrEditAttributesResults.appendResultMessage(errorMessage + ", " + wsAttributeEdit);
				}
			}
		}
		 
		// assume success
		wsViewOrEditAttributesResults.assignResultCode(WsViewOrEditAttributesResultsCode.SUCCESS);
		Subject actAsSubject = null;
		// TODO have common try/catch
		try {
			actAsSubject = GrouperServiceJ2ee
					.retrieveSubjectActAs(actAsSubjectLookup);

			if (actAsSubject == null) {
				// TODO make this a result code
				throw new RuntimeException("Cant find actAs user: "
						+ actAsSubjectLookup);
			}

			// use this to be the user connected, or the user act-as
			try {
				session = GrouperSession.start(actAsSubject);
			} catch (SessionException se) {
				// TODO make this a result code
				throw new RuntimeException("Problem with session for subject: "
						+ actAsSubject, se);
			}

			int resultIndex = 0;

			wsViewOrEditAttributesResults.setResults(new WsViewOrEditAttributesResult[groupsSize]);
			GROUP_LOOP:
			for (WsGroupLookup wsGroupLookup : wsGroupLookups) {
				WsViewOrEditAttributesResult wsViewOrEditAttributesResult = new WsViewOrEditAttributesResult();
				wsViewOrEditAttributesResults.getResults()[resultIndex++] = wsViewOrEditAttributesResult;
				Group group = null;

				try {
					wsViewOrEditAttributesResult
							.setGroupName(wsGroupLookup.getGroupName());
					wsViewOrEditAttributesResult.setGroupUuid(wsGroupLookup.getUuid());
					
					//get the group
					wsGroupLookup.retrieveGroupIfNeeded(session);
					group = wsGroupLookup.retrieveGroup();
					if (group == null) {
						wsViewOrEditAttributesResult
								.assignResultCode(WsViewOrEditAttributesResultCode.GROUP_NOT_FOUND);
						wsViewOrEditAttributesResult
								.setResultMessage("Cant find group: '"
										+ wsGroupLookup + "'.  ");
						continue;
					}

					group = wsGroupLookup.retrieveGroup();

					// these will probably match, but just in case
					if (StringUtils.isBlank(wsViewOrEditAttributesResult.getGroupName())) {
						wsViewOrEditAttributesResult.setGroupName(group.getName());
					}
					if (StringUtils.isBlank(wsViewOrEditAttributesResult.getGroupUuid())) {
						wsViewOrEditAttributesResult.setGroupUuid(group.getUuid());
					}
					
					//lets read them
					Map<String, String> attributeMap = GrouperServiceUtils.nonNull(group.getAttributes());
					
					//see if we are updating
					if (!readOnly) {
						for (WsAttributeEdit wsAttributeEdit : wsAttributeEdits) {
							String attributeName = wsAttributeEdit.getName();
							try {
								//lets see if delete
								if (wsAttributeEdit.deleteBoolean()) {
									//if its not there, dont bother
									if (attributeMap.containsKey(attributeName)) {
										group.deleteAttribute(attributeName);
										//update map
										attributeMap.remove(attributeName);
									}
								} else {
									String attributeValue = wsAttributeEdit.getValue();
									//make sure it is different
									if (!StringUtils.equals(attributeValue, attributeMap.get(attributeName))) {
										//it is update
										group.setAttribute(attributeName, wsAttributeEdit.getValue());
										attributeMap.put(attributeName, attributeValue);
									}
								}
							} catch (AttributeNotFoundException anfe) {
								wsViewOrEditAttributesResult.assignResultCode(
										WsViewOrEditAttributesResultCode.ATTRIBUTE_NOT_FOUND);
								wsViewOrEditAttributesResult.setResultMessage("Cant find attribute: " + attributeName);
								//go to next group
								continue GROUP_LOOP;
								
							}
						}
					}
					//now take the attributes and put them in the result
					if (attributeMap.size() > 0) {
						int attributeIndex = 0;
						WsAttribute[] attributes = new WsAttribute[attributeMap.size()];
						wsViewOrEditAttributesResult.setAttributes(attributes);
						//lookup each from map and return
						for (String key : attributeMap.keySet()) {
							WsAttribute wsAttribute = new WsAttribute();
							attributes[attributeIndex++] = wsAttribute;
							wsAttribute.setName(key);
							wsAttribute.setValue(attributeMap.get(key));
						}
					}
					wsViewOrEditAttributesResult.setSuccess("T");
					wsViewOrEditAttributesResult.setResultCode("SUCCESS");
					if (readOnly) {
						wsViewOrEditAttributesResult.setResultMessage("Group '"
								+ group.getName() + "' was queried.");
					} else {
						wsViewOrEditAttributesResult.setResultMessage("Group '"
								+ group.getName() + "' had attributes edited.");
					}
				} catch (InsufficientPrivilegeException ipe) {
					wsViewOrEditAttributesResult
							.assignResultCode(WsViewOrEditAttributesResultCode.INSUFFICIENT_PRIVILEGES);
					wsViewOrEditAttributesResult
							.setResultMessage("Error: insufficient privileges to view/edit attributes '"
									+ wsGroupLookup.getGroupName() + "'");
				} catch (Exception e) {
					// lump the rest in there, group_add_exception, etc
					wsViewOrEditAttributesResult
							.assignResultCode(WsViewOrEditAttributesResultCode.EXCEPTION);
					wsViewOrEditAttributesResult.setResultMessage(ExceptionUtils
							.getFullStackTrace(e));
					LOG.error(wsGroupLookup + ", " + e, e);
				}
			}

		} catch (RuntimeException re) {
			wsViewOrEditAttributesResults
					.assignResultCode(WsViewOrEditAttributesResultsCode.EXCEPTION);
			String theError = "Problem view/edit attributes for groups: wsGroupLookup: "
					+ GrouperServiceUtils.toStringForLog(wsGroupLookups)
					+ ", attributeEdits: " + GrouperServiceUtils.toStringForLog(wsAttributeEdits)
					+ ", actAsSubject: " + actAsSubject + ".  \n" + "";
			wsViewOrEditAttributesResults.appendResultMessage(theError);
			// this is sent back to the caller anyway, so just log, and not send
			// back again
			LOG.error(theError + ", wsViewOrEditAttributesResults: "
					+ GrouperServiceUtils.toStringForLog(wsViewOrEditAttributesResults),
					re);
		} finally {
			if (session != null) {
				try {
					session.stop();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		if (wsViewOrEditAttributesResults.getResults() != null) {
			// check all entries
			int successes = 0;
			int failures = 0;
			for (WsViewOrEditAttributesResult wsGroupSaveResult : wsViewOrEditAttributesResults
					.getResults()) {
				boolean success = "T"
						.equalsIgnoreCase(wsGroupSaveResult == null ? null
								: wsGroupSaveResult.getSuccess());
				if (success) {
					successes++;
				} else {
					failures++;
				}
			}
			if (failures > 0) {
				wsViewOrEditAttributesResults.appendResultMessage("There were "
						+ successes + " successes and " + failures
						+ " failures of viewing/editing group attribues.   ");
				wsViewOrEditAttributesResults
						.assignResultCode(WsViewOrEditAttributesResultsCode.PROBLEM_WITH_GROUPS);
			} else {
				wsViewOrEditAttributesResults.assignResultCode(WsViewOrEditAttributesResultsCode.SUCCESS);
			}
		}
		if (!"T".equalsIgnoreCase(wsViewOrEditAttributesResults.getSuccess())) {

			LOG.error(wsViewOrEditAttributesResults.getResultMessage());
		}
		return wsViewOrEditAttributesResults;
	}

	/**
	 * save a group or many (insert or update)
	 * 
	 * @see {@link Group#saveGroup(GrouperSession, String, String, String, String, boolean, boolean, boolean)}
	 * @param wsGroupsToSave
	 *            groups to save
	 * @param actAsSubjectLookup
	 * @param paramNames
	 *            optional: reserved for future use
	 * @param paramValues
	 *            optional: reserved for future use
	 * @return the results
	 */
	@SuppressWarnings("unchecked")
	public WsGroupSaveResults groupSave(WsGroupToSave[] wsGroupsToSave,
			WsSubjectLookup actAsSubjectLookup, String[] paramNames,
			String[] paramValues) {

		GrouperSession session = null;
		int groupsSize = wsGroupsToSave == null ? 0 : wsGroupsToSave.length;

		WsGroupSaveResults wsGroupSaveResults = new WsGroupSaveResults();

		// see if greater than the max (or default)
		int maxSaveGroup = GrouperWsConfig.getPropertyInt(
				GrouperWsConfig.WS_GROUP_SAVE_MAX, 1000000);
		if (groupsSize > maxSaveGroup) {
			wsGroupSaveResults
					.assignResultCode(WsGroupSaveResultsCode.INVALID_QUERY);
			wsGroupSaveResults
					.appendResultMessage("Number of groups must be less than max: "
							+ maxSaveGroup + " (sent in " + groupsSize + ")");
			return wsGroupSaveResults;
		}

		// TODO make sure size of params and values the same

		// assume success
		wsGroupSaveResults.assignResultCode(WsGroupSaveResultsCode.SUCCESS);
		Subject actAsSubject = null;
		// TODO have common try/catch
		try {
			actAsSubject = GrouperServiceJ2ee
					.retrieveSubjectActAs(actAsSubjectLookup);

			if (actAsSubject == null) {
				// TODO make this a result code
				throw new RuntimeException("Cant find actAs user: "
						+ actAsSubjectLookup);
			}

			// use this to be the user connected, or the user act-as
			try {
				session = GrouperSession.start(actAsSubject);
			} catch (SessionException se) {
				// TODO make this a result code
				throw new RuntimeException("Problem with session for subject: "
						+ actAsSubject, se);
			}

			int resultIndex = 0;

			wsGroupSaveResults.setResults(new WsGroupSaveResult[groupsSize]);
			for (WsGroupToSave wsGroupToSave : wsGroupsToSave) {
				WsGroupSaveResult wsGroupSaveResult = new WsGroupSaveResult();
				wsGroupSaveResults.getResults()[resultIndex++] = wsGroupSaveResult;
				Group group = null;

				try {
					wsGroupSaveResult
							.setGroupName(wsGroupToSave.getGroupName());
					// TODO change this to groupUuid
					wsGroupSaveResult.setGroupUuid(wsGroupToSave.getUuid());

					try {
						wsGroupToSave.validate();
					} catch (Exception e) {
						wsGroupSaveResult
								.assignResultCode(WsGroupSaveResultCode.INVALID_QUERY);
						wsGroupSaveResult.setResultMessage(ExceptionUtils
								.getFullStackTrace(e));
						continue;
					}

					group = wsGroupToSave.save(session);

					// these will probably match, but just in case
					if (StringUtils.isBlank(wsGroupSaveResult.getGroupName())) {
						wsGroupSaveResult.setGroupName(group.getName());
					}
					if (StringUtils.isBlank(wsGroupSaveResult.getGroupUuid())) {
						wsGroupSaveResult.setGroupUuid(group.getUuid());
					}
					wsGroupSaveResult.setSuccess("T");
					wsGroupSaveResult.setResultCode("SUCCESS");
					wsGroupSaveResult.setResultMessage("Group '"
							+ group.getName() + "' was saved.");
				} catch (InsufficientPrivilegeException ipe) {
					wsGroupSaveResult
							.assignResultCode(WsGroupSaveResultCode.INSUFFICIENT_PRIVILEGES);
					wsGroupSaveResult
							.setResultMessage("Error: insufficient privileges to save group '"
									+ wsGroupToSave.getGroupName() + "'");
				} catch (StemNotFoundException snfe) {
					wsGroupSaveResult
							.assignResultCode(WsGroupSaveResultCode.STEM_NOT_FOUND);
					wsGroupSaveResult
							.setResultMessage("Error: stem not found to save group '"
									+ wsGroupToSave.getGroupName() + "'");
				} catch (GroupNotFoundException gnfe) {
					wsGroupSaveResult
							.assignResultCode(WsGroupSaveResultCode.GROUP_NOT_FOUND);
					wsGroupSaveResult
							.setResultMessage("Error: group not found to save group '"
									+ wsGroupToSave.getGroupName() + "'");
				} catch (Exception e) {
					// lump the rest in there, group_add_exception, etc
					wsGroupSaveResult
							.assignResultCode(WsGroupSaveResultCode.EXCEPTION);
					wsGroupSaveResult.setResultMessage(ExceptionUtils
							.getFullStackTrace(e));
					LOG.error(wsGroupToSave + ", " + e, e);
				}
			}

		} catch (RuntimeException re) {
			wsGroupSaveResults
					.assignResultCode(WsGroupSaveResultsCode.EXCEPTION);
			String theError = "Problem saving groups: wsGroupsToSave: "
					+ GrouperServiceUtils.toStringForLog(wsGroupsToSave)
					+ ", actAsSubject: " + actAsSubject + ".  \n" + "";
			wsGroupSaveResults.appendResultMessage(theError);
			// this is sent back to the caller anyway, so just log, and not send
			// back again
			LOG.error(theError + ", wsGroupSaveResults: "
					+ GrouperServiceUtils.toStringForLog(wsGroupSaveResults),
					re);
		} finally {
			if (session != null) {
				try {
					session.stop();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		if (wsGroupSaveResults.getResults() != null) {
			// check all entries
			int successes = 0;
			int failures = 0;
			for (WsGroupSaveResult wsGroupSaveResult : wsGroupSaveResults
					.getResults()) {
				boolean success = "T"
						.equalsIgnoreCase(wsGroupSaveResult == null ? null
								: wsGroupSaveResult.getSuccess());
				if (success) {
					successes++;
				} else {
					failures++;
				}
			}
			if (failures > 0) {
				wsGroupSaveResults.appendResultMessage("There were "
						+ successes + " successes and " + failures
						+ " failures of saving groups.   ");
				wsGroupSaveResults
						.assignResultCode(WsGroupSaveResultsCode.PROBLEM_DELETING_GROUPS);
			} else {
				wsGroupSaveResults
						.assignResultCode(WsGroupSaveResultsCode.SUCCESS);
			}
		}
		if (!"T".equalsIgnoreCase(wsGroupSaveResults.getSuccess())) {

			LOG.error(wsGroupSaveResults.getResultMessage());
		}
		return wsGroupSaveResults;
	}

	/**
	 * delete a stem or many (if doesnt exist, ignore)
	 * @param stemName name of stem to delete (mutually exclusive with uuid)
	 * @param stemUuid uuid of stem to delete (mutually exclusive with name)
	 * 
	 * @param wsStemLookups stem lookups of stems to delete (specify name or uuid)
	 * @param actAsSubjectLookup
	 * @param paramNames
	 *            optional: reserved for future use
	 * @param paramValues
	 *            optional: reserved for future use
	 * @return the results
	 */
	@SuppressWarnings("unchecked")
	public WsStemDeleteResults stemDelete(WsStemLookup[] wsStemLookups,
			WsSubjectLookup actAsSubjectLookup, String[] paramNames,
			String[] paramValues) {
	
		GrouperSession session = null;
		int stemsSize = wsStemLookups == null ? 0 : wsStemLookups.length;
		
	
		WsStemDeleteResults wsStemDeleteResults = new WsStemDeleteResults();
	
		// see if greater than the max (or default)
		int maxDeleteStem = GrouperWsConfig.getPropertyInt(
				GrouperWsConfig.WS_GROUP_DELETE_MAX, 1000000);
		if (stemsSize > maxDeleteStem) {
			wsStemDeleteResults
					.assignResultCode(WsStemDeleteResultsCode.INVALID_QUERY);
			wsStemDeleteResults
					.appendResultMessage("Number of stems must be less than max: "
							+ maxDeleteStem + " (sent in " + stemsSize + ")");
			return wsStemDeleteResults;
		}
	
		wsStemDeleteResults.setResults(new WsStemDeleteResult[stemsSize]);
	
		// TODO make sure size of params and values the same
	
		// assume success
		wsStemDeleteResults.assignResultCode(WsStemDeleteResultsCode.SUCCESS);
		Subject actAsSubject = null;
		try {
			actAsSubject = GrouperServiceJ2ee
					.retrieveSubjectActAs(actAsSubjectLookup);
	
			if (actAsSubject == null) {
				throw new RuntimeException("Cant find actAs user: "
						+ actAsSubjectLookup);
			}
	
			// use this to be the user connected, or the user act-as
			try {
				session = GrouperSession.start(actAsSubject);
			} catch (SessionException se) {
				throw new RuntimeException("Problem with session for subject: "
						+ actAsSubject, se);
			}
	
			int resultIndex = 0;
	
			for (WsStemLookup wsStemLookup : wsStemLookups) {
				WsStemDeleteResult wsStemDeleteResult = new WsStemDeleteResult();
				wsStemDeleteResults.getResults()[resultIndex++] = wsStemDeleteResult;
				try {
	
					wsStemLookup.retrieveStemIfNeeded(session);
					Stem stem = wsStemLookup.retrieveStem();
	
					wsStemDeleteResult.setStemName(wsStemLookup
							.getStemName());
					wsStemDeleteResult.setStemUuid(wsStemLookup.getUuid());
	
					if (stem == null) {
						wsStemDeleteResult
								.assignResultCode(WsStemDeleteResultCode.STEM_NOT_FOUND);
						wsStemDeleteResult
								.setResultMessage("Cant find stem: '"
										+ wsStemLookup + "'.  ");
						continue;
					}
	
					// these will probably match, but just in case
					if (StringUtils.isBlank(wsStemDeleteResult.getStemName())) {
						wsStemDeleteResult.setStemName(stem.getName());
					}
					if (StringUtils.isBlank(wsStemDeleteResult.getStemUuid())) {
						wsStemDeleteResult.setStemUuid(stem.getUuid());
					}
	
					try {
						stem.delete();
						wsStemDeleteResult.setSuccess("T");
						wsStemDeleteResult.setResultCode("SUCCESS");
						wsStemDeleteResult.setResultMessage("Stem '"
								+ stem.getName() + "' was deleted.");
					} catch (InsufficientPrivilegeException ipe) {
						wsStemDeleteResult
								.assignResultCode(WsStemDeleteResultCode.INSUFFICIENT_PRIVILEGES);
						wsStemDeleteResult
								.setResultMessage("Error: insufficient privileges to delete stem '"
										+ stem.getName() + "'");
					}
				} catch (Exception e) {
					wsStemDeleteResult.setResultCode("EXCEPTION");
					wsStemDeleteResult.setResultMessage(ExceptionUtils
							.getFullStackTrace(e));
					LOG.error(wsStemLookup + ", " + e, e);
				}
			}
	
		} catch (RuntimeException re) {
			wsStemDeleteResults
					.assignResultCode(WsStemDeleteResultsCode.EXCEPTION);
			String theError = "Problem deleting stems: wsStemLookups: "
					+ GrouperServiceUtils.toStringForLog(wsStemLookups)
					+ ", actAsSubject: " + actAsSubject + ".  \n" + "";
			wsStemDeleteResults.appendResultMessage(theError);
			// this is sent back to the caller anyway, so just log, and not send
			// back again
			LOG.error(theError + ", wsStemDeleteResults: "
					+ GrouperServiceUtils.toStringForLog(wsStemDeleteResults),
					re);
		} finally {
			if (session != null) {
				try {
					session.stop();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	
		if (wsStemDeleteResults.getResults() != null) {
			// check all entries
			int successes = 0;
			int failures = 0;
			for (WsStemDeleteResult wsStemDeleteResult : wsStemDeleteResults
					.getResults()) {
				if (wsStemDeleteResult == null) {
					failures++;
					continue;
						
				}
				boolean success = "T".equalsIgnoreCase(wsStemDeleteResult
						.getSuccess());
				if (success) {
					successes++;
				} else {
					failures++;
				}
			}
			if (failures > 0) {
				wsStemDeleteResults.appendResultMessage("There were "
						+ successes + " successes and " + failures
						+ " failures of deleting stems.   ");
				wsStemDeleteResults
						.assignResultCode(WsStemDeleteResultsCode.PROBLEM_DELETING_STEMS);
			} else {
				wsStemDeleteResults
						.assignResultCode(WsStemDeleteResultsCode.SUCCESS);
			}
		}
		if (!"T".equalsIgnoreCase(wsStemDeleteResults.getSuccess())) {
	
			LOG.error(wsStemDeleteResults.getResultMessage());
		}
		return wsStemDeleteResults;
	}

	/**
	 * delete a group or many (if doesnt exist, ignore)
	 * 
	 * @param wsGroupLookups
	 *            groups to delete
	 * @param actAsSubjectLookup
	 * @param paramNames
	 *            optional: reserved for future use
	 * @param paramValues
	 *            optional: reserved for future use
	 * @return the results
	 */
	@SuppressWarnings("unchecked")
	public WsGroupDeleteResults groupDelete(WsGroupLookup[] wsGroupLookups,
			WsSubjectLookup actAsSubjectLookup, String[] paramNames,
			String[] paramValues) {

		GrouperSession session = null;
		int groupsSize = wsGroupLookups == null ? 0 : wsGroupLookups.length;

		WsGroupDeleteResults wsGroupDeleteResults = new WsGroupDeleteResults();

		// see if greater than the max (or default)
		int maxDeleteGroup = GrouperWsConfig.getPropertyInt(
				GrouperWsConfig.WS_GROUP_DELETE_MAX, 1000000);
		if (groupsSize > maxDeleteGroup) {
			wsGroupDeleteResults
					.assignResultCode(WsGroupDeleteResultsCode.INVALID_QUERY);
			wsGroupDeleteResults
					.appendResultMessage("Number of groups must be less than max: "
							+ maxDeleteGroup + " (sent in " + groupsSize + ")");
			return wsGroupDeleteResults;
		}

		wsGroupDeleteResults.setResults(new WsGroupDeleteResult[groupsSize]);

		// TODO make sure size of params and values the same

		// assume success
		wsGroupDeleteResults.assignResultCode(WsGroupDeleteResultsCode.SUCCESS);
		Subject actAsSubject = null;
		try {
			actAsSubject = GrouperServiceJ2ee
					.retrieveSubjectActAs(actAsSubjectLookup);

			if (actAsSubject == null) {
				throw new RuntimeException("Cant find actAs user: "
						+ actAsSubjectLookup);
			}

			// use this to be the user connected, or the user act-as
			try {
				session = GrouperSession.start(actAsSubject);
			} catch (SessionException se) {
				throw new RuntimeException("Problem with session for subject: "
						+ actAsSubject, se);
			}

			int resultIndex = 0;

			for (WsGroupLookup wsGroupLookup : wsGroupLookups) {
				WsGroupDeleteResult wsGroupDeleteResult = new WsGroupDeleteResult();
				wsGroupDeleteResults.getResults()[resultIndex++] = wsGroupDeleteResult;
				try {

					wsGroupLookup.retrieveGroupIfNeeded(session);
					Group group = wsGroupLookup.retrieveGroup();

					wsGroupDeleteResult.setGroupName(wsGroupLookup
							.getGroupName());
					wsGroupDeleteResult.setGroupUuid(wsGroupLookup.getUuid());

					if (group == null) {
						wsGroupDeleteResult
								.assignResultCode(WsGroupDeleteResultCode.GROUP_NOT_FOUND);
						wsGroupDeleteResult
								.setResultMessage("Cant find group: '"
										+ wsGroupLookup + "'.  ");
						continue;
					}

					// these will probably match, but just in case
					if (StringUtils.isBlank(wsGroupDeleteResult.getGroupName())) {
						wsGroupDeleteResult.setGroupName(group.getName());
					}
					if (StringUtils.isBlank(wsGroupDeleteResult.getGroupUuid())) {
						wsGroupDeleteResult.setGroupUuid(group.getUuid());
					}

					try {
						group.delete();
						wsGroupDeleteResult.setSuccess("T");
						wsGroupDeleteResult.setResultCode("SUCCESS");
						wsGroupDeleteResult.setResultMessage("Group '"
								+ group.getName() + "' was deleted.");
					} catch (InsufficientPrivilegeException ipe) {
						wsGroupDeleteResult
								.assignResultCode(WsGroupDeleteResultCode.INSUFFICIENT_PRIVILEGES);
						wsGroupDeleteResult
								.setResultMessage("Error: insufficient privileges to delete group '"
										+ group.getName() + "'");
					}
				} catch (Exception e) {
					wsGroupDeleteResult.setResultCode("EXCEPTION");
					wsGroupDeleteResult.setResultMessage(ExceptionUtils
							.getFullStackTrace(e));
					LOG.error(wsGroupLookup + ", " + e, e);
				}
			}

		} catch (RuntimeException re) {
			wsGroupDeleteResults
					.assignResultCode(WsGroupDeleteResultsCode.EXCEPTION);
			String theError = "Problem deleting groups: wsGroupLookups: "
					+ GrouperServiceUtils.toStringForLog(wsGroupLookups)
					+ ", actAsSubject: " + actAsSubject + ".  \n" + "";
			wsGroupDeleteResults.appendResultMessage(theError);
			// this is sent back to the caller anyway, so just log, and not send
			// back again
			LOG.error(theError + ", wsGroupDeleteResults: "
					+ GrouperServiceUtils.toStringForLog(wsGroupDeleteResults),
					re);
		} finally {
			if (session != null) {
				try {
					session.stop();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		if (wsGroupDeleteResults.getResults() != null) {
			// check all entries
			int successes = 0;
			int failures = 0;
			for (WsGroupDeleteResult wsGroupDeleteResult : wsGroupDeleteResults
					.getResults()) {
				boolean success = "T".equalsIgnoreCase(wsGroupDeleteResult
						.getSuccess());
				if (success) {
					successes++;
				} else {
					failures++;
				}
			}
			if (failures > 0) {
				wsGroupDeleteResults.appendResultMessage("There were "
						+ successes + " successes and " + failures
						+ " failures of deleting groups.   ");
				wsGroupDeleteResults
						.assignResultCode(WsGroupDeleteResultsCode.PROBLEM_DELETING_GROUPS);
			} else {
				wsGroupDeleteResults
						.assignResultCode(WsGroupDeleteResultsCode.SUCCESS);
			}
		}
		if (!"T".equalsIgnoreCase(wsGroupDeleteResults.getSuccess())) {

			LOG.error(wsGroupDeleteResults.getResultMessage());
		}
		return wsGroupDeleteResults;
	}

	/**
	 * If all privilege params are empty, then it is viewonly. If any are set,
	 * then the privileges will be set (and returned)
	 * 
	 * @param wsGroupLookup
	 *            for group which is related to the privileges
	 * @param subjectLookups
	 *            subjects to be added to the group
	 * @param adminAllowed
	 *            T for allowed, F for not allowed, blank for unchanged
	 * @param optinAllowed
	 *            T for allowed, F for not allowed, blank for unchanged
	 * @param optoutAllowed
	 *            T for allowed, F for not allowed, blank for unchanged
	 * @param readAllowed
	 *            T for allowed, F for not allowed, blank for unchanged
	 * @param viewAllowed
	 *            T for allowed, F for not allowed, blank for unchanged
	 * @param updateAllowed
	 *            T for allowed, F for not allowed, blank for unchanged
	 * @param actAsSubjectLookup
	 * @param paramNames
	 *            optional: reserved for future use
	 * @param paramValues
	 *            optional: reserved for future use
	 * @return the results
	 */
	@SuppressWarnings("unchecked")
	public WsViewOrEditPrivilegesResults viewOrEditPrivileges(
			WsGroupLookup wsGroupLookup, WsSubjectLookup[] subjectLookups,
			String adminAllowed, String optinAllowed, String optoutAllowed,
			String readAllowed, String updateAllowed, String viewAllowed,
			WsSubjectLookup actAsSubjectLookup, String[] paramNames,
			String[] paramValues) {

		GrouperSession session = null;
		WsViewOrEditPrivilegesResults wsViewOrEditPrivilegesResults = new WsViewOrEditPrivilegesResults();
		int subjectLength = subjectLookups == null ? 0 : subjectLookups.length;
		if (subjectLength == 0) {
			wsViewOrEditPrivilegesResults
					.assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
			wsViewOrEditPrivilegesResults
					.appendResultMessage("Subject length must be more than 1");
			return wsViewOrEditPrivilegesResults;
		}

		// see if greater than the max (or default)
		int maxSavePrivileges = GrouperWsConfig.getPropertyInt(
				GrouperWsConfig.WS_VIEW_OR_EDIT_PRIVILEGES_SUBJECTS_MAX,
				1000000);
		if (subjectLength > maxSavePrivileges) {
			wsViewOrEditPrivilegesResults
					.assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
			wsViewOrEditPrivilegesResults
					.appendResultMessage("Subject length must be less than max: "
							+ maxSavePrivileges
							+ " (sent in "
							+ subjectLength
							+ ")");
			return wsViewOrEditPrivilegesResults;
		}

		// TODO make sure size of params and values the same

		// assume success
		wsViewOrEditPrivilegesResults
				.assignResultCode(WsViewOrEditPrivilegesResultsCode.SUCCESS);
		Subject actAsSubject = null;
		try {
			actAsSubject = GrouperServiceJ2ee
					.retrieveSubjectActAs(actAsSubjectLookup);

			if (actAsSubject == null) {
				throw new RuntimeException("Cant find actAs user: "
						+ actAsSubjectLookup);
			}

			// use this to be the user connected, or the user act-as
			try {
				session = GrouperSession.start(actAsSubject);
			} catch (SessionException se) {
				throw new RuntimeException("Problem with session for subject: "
						+ actAsSubject, se);
			}
			wsGroupLookup.retrieveGroupIfNeeded(session);
			Group group = wsGroupLookup.retrieveGroup();

			if (group == null) {
				wsViewOrEditPrivilegesResults
						.assignResultCode(WsViewOrEditPrivilegesResultsCode.INVALID_QUERY);
				wsViewOrEditPrivilegesResults
						.appendResultMessage("Cant find group: "
								+ wsGroupLookup + ".  ");
				return wsViewOrEditPrivilegesResults;
			}
			List<Privilege> privilegesToAssign = new ArrayList<Privilege>();

			List<Privilege> privilegesToRevoke = new ArrayList<Privilege>();

			// process the privilege inputs, keep in lists, handle invalid
			// queries
			if (!GrouperServiceUtils.processPrivilegesHelper(adminAllowed,
					"adminAllowed", privilegesToAssign, privilegesToRevoke,
					AccessPrivilege.ADMIN, wsViewOrEditPrivilegesResults)) {
				return wsViewOrEditPrivilegesResults;
			}
			if (!GrouperServiceUtils.processPrivilegesHelper(optinAllowed,
					"optinAllowed", privilegesToAssign, privilegesToRevoke,
					AccessPrivilege.OPTIN, wsViewOrEditPrivilegesResults)) {
				return wsViewOrEditPrivilegesResults;
			}
			if (!GrouperServiceUtils.processPrivilegesHelper(optoutAllowed,
					"optoutAllowed", privilegesToAssign, privilegesToRevoke,
					AccessPrivilege.OPTOUT, wsViewOrEditPrivilegesResults)) {
				return wsViewOrEditPrivilegesResults;
			}
			if (!GrouperServiceUtils.processPrivilegesHelper(readAllowed,
					"readAllowed", privilegesToAssign, privilegesToRevoke,
					AccessPrivilege.READ, wsViewOrEditPrivilegesResults)) {
				return wsViewOrEditPrivilegesResults;
			}
			if (!GrouperServiceUtils.processPrivilegesHelper(updateAllowed,
					"updateAllowed", privilegesToAssign, privilegesToRevoke,
					AccessPrivilege.UPDATE, wsViewOrEditPrivilegesResults)) {
				return wsViewOrEditPrivilegesResults;
			}
			if (!GrouperServiceUtils.processPrivilegesHelper(viewAllowed,
					"viewAllowed", privilegesToAssign, privilegesToRevoke,
					AccessPrivilege.VIEW, wsViewOrEditPrivilegesResults)) {
				return wsViewOrEditPrivilegesResults;
			}

			int resultIndex = 0;

			wsViewOrEditPrivilegesResults
					.setResults(new WsViewOrEditPrivilegesResult[subjectLength]);

			for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
				WsViewOrEditPrivilegesResult wsViewOrEditPrivilegesResult = new WsViewOrEditPrivilegesResult();
				wsViewOrEditPrivilegesResults.getResults()[resultIndex++] = wsViewOrEditPrivilegesResult;
				try {
					wsViewOrEditPrivilegesResult.setSubjectId(wsSubjectLookup
							.getSubjectId());
					wsViewOrEditPrivilegesResult
							.setSubjectIdentifier(wsSubjectLookup
									.getSubjectIdentifier());

					Subject subject = wsSubjectLookup.retrieveSubject();

					// make sure the subject is there
					if (subject == null) {
						// see why not
						SubjectFindResult subjectFindResult = wsSubjectLookup
								.retrieveSubjectFindResult();
						String error = "Subject: " + wsSubjectLookup
								+ " had problems: " + subjectFindResult;
						wsViewOrEditPrivilegesResult.setResultMessage(error);
						if (SubjectFindResult.SUBJECT_NOT_FOUND
								.equals(subjectFindResult)) {
							wsViewOrEditPrivilegesResult
									.assignResultCode(WsViewOrEditPrivilegesResultCode.SUBJECT_NOT_FOUND);
							continue;
						}
						if (SubjectFindResult.SUBJECT_DUPLICATE
								.equals(subjectFindResult)) {
							wsViewOrEditPrivilegesResult
									.assignResultCode(WsViewOrEditPrivilegesResultCode.SUBJECT_DUPLICATE);
							continue;
						}
						throw new NullPointerException(error);
					}

					// these will probably match, but just in case
					if (StringUtils.isBlank(wsViewOrEditPrivilegesResult
							.getSubjectId())) {
						wsViewOrEditPrivilegesResult.setSubjectId(subject
								.getId());
					}

					try {
						// lets get all the privileges for the group and user
						Set<AccessPrivilege> accessPrivileges = GrouperServiceUtils
								.nonNull(group.getPrivs(subject));

						// TODO keep track of isRevokable? Also, can you remove
						// a read priv? I tried and got exception

						// see what we really need to do. At the end, the
						// currentAccessPrivileges should be what it looks like
						// afterward
						// (add in assignments, remove revokes),
						// the privilegestoAssign will be what to assign (take
						// out what is already there)
						Set<Privilege> currentPrivilegesSet = GrouperServiceUtils
								.convertAccessPrivilegesToPrivileges(accessPrivileges);

						List<Privilege> privilegesToAssignToThisSubject = new ArrayList<Privilege>(
								privilegesToAssign);
						List<Privilege> privilegesToRevokeFromThisSubject = new ArrayList<Privilege>(
								privilegesToRevoke);

						// dont assign ones already in there
						privilegesToAssignToThisSubject
								.removeAll(currentPrivilegesSet);
						// dont revoke ones not in there
						privilegesToRevokeFromThisSubject
								.retainAll(currentPrivilegesSet);
						// assign
						for (Privilege privilegeToAssign : privilegesToAssignToThisSubject) {
							group.grantPriv(subject, privilegeToAssign);
						}
						// revoke
						for (Privilege privilegeToRevoke : privilegesToRevokeFromThisSubject) {
							group.revokePriv(subject, privilegeToRevoke);
						}
						// reset the current privileges set to reflect the new
						// state
						currentPrivilegesSet
								.addAll(privilegesToAssignToThisSubject);
						currentPrivilegesSet
								.removeAll(privilegesToRevokeFromThisSubject);

						wsViewOrEditPrivilegesResult
								.setAdminAllowed(GrouperServiceUtils
										.booleanToStringOneChar(currentPrivilegesSet
												.contains(AccessPrivilege.ADMIN)));
						wsViewOrEditPrivilegesResult
								.setOptinAllowed(GrouperServiceUtils
										.booleanToStringOneChar(currentPrivilegesSet
												.contains(AccessPrivilege.OPTIN)));
						wsViewOrEditPrivilegesResult
								.setOptoutAllowed(GrouperServiceUtils
										.booleanToStringOneChar(currentPrivilegesSet
												.contains(AccessPrivilege.OPTOUT)));
						wsViewOrEditPrivilegesResult
								.setReadAllowed(GrouperServiceUtils
										.booleanToStringOneChar(currentPrivilegesSet
												.contains(AccessPrivilege.READ)));
						wsViewOrEditPrivilegesResult
								.setSystemAllowed(GrouperServiceUtils
										.booleanToStringOneChar(currentPrivilegesSet
												.contains(AccessPrivilege.SYSTEM)));
						wsViewOrEditPrivilegesResult
								.setUpdateAllowed(GrouperServiceUtils
										.booleanToStringOneChar(currentPrivilegesSet
												.contains(AccessPrivilege.UPDATE)));

						wsViewOrEditPrivilegesResult
								.assignResultCode(WsViewOrEditPrivilegesResultCode.SUCCESS);
					} catch (InsufficientPrivilegeException ipe) {
						wsViewOrEditPrivilegesResult
								.assignResultCode(WsViewOrEditPrivilegesResultCode.INSUFFICIENT_PRIVILEGES);
						wsViewOrEditPrivilegesResult
								.setResultMessage(ExceptionUtils
										.getFullStackTrace(ipe));
					}
				} catch (Exception e) {
					wsViewOrEditPrivilegesResult.setResultCode("EXCEPTION");
					wsViewOrEditPrivilegesResult
							.setResultMessage(ExceptionUtils
									.getFullStackTrace(e));
					LOG.error(wsSubjectLookup + ", " + e, e);
				}

			}
		} catch (RuntimeException re) {
			wsViewOrEditPrivilegesResults
					.assignResultCode(WsViewOrEditPrivilegesResultsCode.EXCEPTION);
			String theError = "Problem with privileges for member and group: wsGroupLookup: "
					+ wsGroupLookup
					+ ", subjectLookups: "
					+ GrouperServiceUtils.toStringForLog(subjectLookups)
					+ ", actAsSubject: "
					+ actAsSubject
					+ ", admin: '"
					+ adminAllowed
					+ "', optin: '"
					+ optinAllowed
					+ "', optout: '"
					+ optoutAllowed
					+ "', read: '"
					+ readAllowed
					+ "', update: '"
					+ updateAllowed
					+ "', view: '" + viewAllowed + ".  ";
			wsViewOrEditPrivilegesResults.appendResultMessage(theError + "\n"
					+ ExceptionUtils.getFullStackTrace(re));
			// this is sent back to the caller anyway, so just log, and not send
			// back again
			LOG.error(theError
					+ ", wsViewOrEditPrivilegesResults: "
					+ GrouperServiceUtils
							.toStringForLog(wsViewOrEditPrivilegesResults), re);
		} finally {
			if (session != null) {
				try {
					session.stop();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		if (wsViewOrEditPrivilegesResults.getResults() != null) {
			// check all entries
			int successes = 0;
			int failures = 0;
			for (WsViewOrEditPrivilegesResult wsViewOrEditPrivilegesResult : wsViewOrEditPrivilegesResults
					.getResults()) {
				boolean success = "T"
						.equalsIgnoreCase(wsViewOrEditPrivilegesResult
								.getSuccess());
				if (success) {
					successes++;
				} else {
					failures++;
				}
			}
			if (failures > 0) {
				wsViewOrEditPrivilegesResults.appendResultMessage("There were "
						+ successes + " successes and " + failures
						+ " failures of user group privileges operations.   ");
				wsViewOrEditPrivilegesResults
						.assignResultCode(WsViewOrEditPrivilegesResultsCode.PROBLEM_WITH_MEMBERS);
			} else {
				wsViewOrEditPrivilegesResults
						.assignResultCode(WsViewOrEditPrivilegesResultsCode.SUCCESS);
			}
		}
		if (!"T".equalsIgnoreCase(wsViewOrEditPrivilegesResults.getSuccess())) {

			LOG.error(wsViewOrEditPrivilegesResults.getResultMessage());
		}
		return wsViewOrEditPrivilegesResults;
	}

	/**
	 * add member to a group (if already a direct member, ignore)
	 * 
	 * @param wsGroupLookup
	 *            group to add the members to
	 * @param subjectLookups
	 *            subjects to be added to the group
	 * @param replaceAllExisting
	 *            optional: T or F (default), if the existing groups should be
	 *            replaced
	 * @param actAsSubjectLookup
	 * @param paramNames
	 *            optional: reserved for future use
	 * @param paramValues
	 *            optional: reserved for future use
	 * @return the results
	 */
	@SuppressWarnings("unchecked")
	public WsAddMemberResults addMember(WsGroupLookup wsGroupLookup,
			WsSubjectLookup[] subjectLookups, String replaceAllExisting,
			WsSubjectLookup actAsSubjectLookup, String[] paramNames,
			String[] paramValues) {

		GrouperSession session = null;
		WsAddMemberResults wsAddMemberResults = new WsAddMemberResults();
		int subjectLength = subjectLookups == null ? 0 : subjectLookups.length;
		if (subjectLength == 0) {
			wsAddMemberResults
					.assignResultCode(WsAddMemberResultsCode.INVALID_QUERY);
			wsAddMemberResults
					.appendResultMessage("Subject length must be more than 1");
			return wsAddMemberResults;
		}

		// see if greater than the max (or default)
		int maxAddMember = GrouperWsConfig.getPropertyInt(
				GrouperWsConfig.WS_ADD_MEMBER_SUBJECTS_MAX, 1000000);
		if (subjectLength > maxAddMember) {
			wsAddMemberResults
					.assignResultCode(WsAddMemberResultsCode.INVALID_QUERY);
			wsAddMemberResults
					.appendResultMessage("Subject length must be less than max: "
							+ maxAddMember + " (sent in " + subjectLength + ")");
			return wsAddMemberResults;
		}

		// TODO make sure size of params and values the same

		// assume success
		wsAddMemberResults.assignResultCode(WsAddMemberResultsCode.SUCCESS);
		Subject actAsSubject = null;
		try {
			actAsSubject = GrouperServiceJ2ee
					.retrieveSubjectActAs(actAsSubjectLookup);

			if (actAsSubject == null) {
				throw new RuntimeException("Cant find actAs user: "
						+ actAsSubjectLookup);
			}

			// use this to be the user connected, or the user act-as
			try {
				session = GrouperSession.start(actAsSubject);
			} catch (SessionException se) {
				throw new RuntimeException("Problem with session for subject: "
						+ actAsSubject, se);
			}
			wsGroupLookup.retrieveGroupIfNeeded(session);
			Group group = wsGroupLookup.retrieveGroup();

			if (group == null) {
				wsAddMemberResults
						.assignResultCode(WsAddMemberResultsCode.INVALID_QUERY);
				wsAddMemberResults.appendResultMessage("Cant find group: "
						+ wsGroupLookup + ".  ");
				return wsAddMemberResults;
			}

			int resultIndex = 0;

			// TODO keep data in transaction?
			boolean replaceAllExistingBoolean = GrouperWsUtils.booleanValue(
					replaceAllExisting, false);
			Set<String> newSubjectIds = new HashSet<String>();
			wsAddMemberResults.setResults(new WsAddMemberResult[subjectLength]);

			for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
				WsAddMemberResult wsAddMemberResult = new WsAddMemberResult();
				wsAddMemberResults.getResults()[resultIndex++] = wsAddMemberResult;
				try {
					// default to non-success
					wsAddMemberResult.setSuccess("F");

					wsAddMemberResult.setSubjectId(wsSubjectLookup
							.getSubjectId());
					wsAddMemberResult.setSubjectIdentifier(wsSubjectLookup
							.getSubjectIdentifier());

					Subject subject = wsSubjectLookup.retrieveSubject();

					// make sure the subject is there
					if (subject == null) {
						// see why not
						SubjectFindResult subjectFindResult = wsSubjectLookup
								.retrieveSubjectFindResult();
						String error = "Subject: " + wsSubjectLookup
								+ " had problems: " + subjectFindResult;
						wsAddMemberResult.setResultMessage(error);
						throw new NullPointerException(error);
					}

					// these will probably match, but just in case
					if (StringUtils.isBlank(wsAddMemberResult.getSubjectId())) {
						wsAddMemberResult.setSubjectId(subject.getId());
					}

					// keep track
					if (replaceAllExistingBoolean) {
						newSubjectIds.add(subject.getId());
					}

					try {
						// dont fail if already a direct member
						if (!group.hasImmediateMember(subject)) {
							group.addMember(subject);
						}
						wsAddMemberResult.setSuccess("T");
						wsAddMemberResult.setResultCode("SUCCESS");

					} catch (InsufficientPrivilegeException ipe) {
						wsAddMemberResult
								.setResultCode("INSUFFICIENT_PRIVILEGES");
						wsAddMemberResult.setResultMessage(ExceptionUtils
								.getFullStackTrace(ipe));
					}
				} catch (Exception e) {
					wsAddMemberResult.setResultCode("EXCEPTION");
					wsAddMemberResult.setResultMessage(ExceptionUtils
							.getFullStackTrace(e));
					LOG.error(wsSubjectLookup + ", " + e, e);
				}
			}

			// after adding all these, see if we are removing:
			if (replaceAllExistingBoolean) {

				// see who is there
				Set<Member> members = group.getImmediateMembers();

				for (Member member : members) {
					String subjectId = member.getSubjectId();
					Subject subject = null;

					if (!newSubjectIds.contains(subjectId)) {
						try {
							subject = member.getSubject();
							group.deleteMember(subject);
						} catch (Exception e) {
							String theError = "Error deleting subject: "
									+ ObjectUtils.defaultIfNull(subject,
											subjectId) + " from group: "
									+ group + ", " + e + ".  ";
							LOG.error(theError, e);

							wsAddMemberResults.appendResultMessage(theError
									+ ExceptionUtils.getFullStackTrace(e));
							wsAddMemberResults
									.assignResultCode(WsAddMemberResultsCode.PROBLEM_DELETING_MEMBERS);
						}
					}
				}
			}
		} catch (RuntimeException re) {
			wsAddMemberResults
					.assignResultCode(WsAddMemberResultsCode.EXCEPTION);
			String theError = "Problem adding member to group: wsGroupLookup: "
					+ wsGroupLookup + ", subjectLookups: "
					+ GrouperServiceUtils.toStringForLog(subjectLookups)
					+ ", replaceAllExisting: " + replaceAllExisting
					+ ", actAsSubject: " + actAsSubject + ".  ";
			wsAddMemberResults.appendResultMessage(theError);
			// this is sent back to the caller anyway, so just log, and not send
			// back again
			LOG.error(theError + ", wsAddMemberResults: "
					+ GrouperServiceUtils.toStringForLog(wsAddMemberResults),
					re);
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
			// check all entries
			int successes = 0;
			int failures = 0;
			for (WsAddMemberResult wsAddMemberResult : wsAddMemberResults
					.getResults()) {
				boolean success = "T".equalsIgnoreCase(wsAddMemberResult
						.getSuccess());
				if (success) {
					successes++;
				} else {
					failures++;
				}
			}
			if (failures > 0) {
				wsAddMemberResults.appendResultMessage("There were "
						+ successes + " successes and " + failures
						+ " failures of users added to the group.   ");
				wsAddMemberResults
						.assignResultCode(WsAddMemberResultsCode.PROBLEM_WITH_ASSIGNMENT);
			} else {
				wsAddMemberResults
						.assignResultCode(WsAddMemberResultsCode.SUCCESS);
			}
		}
		if (!"T".equalsIgnoreCase(wsAddMemberResults.getSuccess())) {

			LOG.error(wsAddMemberResults.getResultMessage());
		}
		return wsAddMemberResults;
	}

	/**
	 * remove member(s) from a group (if not already a direct member, ignore)
	 * 
	 * @param wsGroupLookup
	 * @param subjectLookups
	 *            subjects to be deleted to the group
	 * @param actAsSubjectLookup
	 * @param paramNames
	 *            optional: reserved for future use
	 * @param paramValues
	 *            optional: reserved for future use
	 * @return the results
	 */
	@SuppressWarnings("unchecked")
	public WsDeleteMemberResults deleteMember(WsGroupLookup wsGroupLookup,
			WsSubjectLookup[] subjectLookups,
			WsSubjectLookup actAsSubjectLookup, String[] paramNames,
			String[] paramValues) {

		GrouperSession session = null;
		WsDeleteMemberResults wsDeleteMemberResults = new WsDeleteMemberResults();
		int subjectLength = subjectLookups == null ? 0 : subjectLookups.length;
		if (subjectLength == 0) {
			wsDeleteMemberResults
					.assignResultCode(WsDeleteMemberResultCode.INVALID_QUERY);
			wsDeleteMemberResults
					.appendResultMessage("Subject length must be more than 1");
			return wsDeleteMemberResults;
		}

		// see if greater than the max (or default)
		int maxDeleteMember = GrouperWsConfig.getPropertyInt(
				GrouperWsConfig.WS_DELETE_MEMBER_SUBJECTS_MAX, 1000000);
		if (subjectLength > maxDeleteMember) {
			wsDeleteMemberResults
					.assignResultCode(WsDeleteMemberResultCode.INVALID_QUERY);
			wsDeleteMemberResults
					.appendResultMessage("Subject length must be less than max: "
							+ maxDeleteMember
							+ " (sent in "
							+ subjectLength
							+ ")");
			return wsDeleteMemberResults;
		}

		// TODO make sure size of params and values the same

		// assume success
		wsDeleteMemberResults
				.assignResultCode(WsDeleteMemberResultCode.SUCCESS);
		Subject actAsSubject = null;
		try {
			actAsSubject = GrouperServiceJ2ee
					.retrieveSubjectActAs(actAsSubjectLookup);

			if (actAsSubject == null) {
				throw new RuntimeException("Cant find actAs user: "
						+ actAsSubjectLookup);
			}

			// use this to be the user connected, or the user act-as
			try {
				session = GrouperSession.start(actAsSubject);
			} catch (SessionException se) {
				throw new RuntimeException("Problem with session for subject: "
						+ actAsSubject, se);
			}
			wsGroupLookup.retrieveGroupIfNeeded(session);
			wsDeleteMemberResults
					.setResults(new WsDeleteMemberResult[subjectLength]);
			Group group = wsGroupLookup.retrieveGroup();

			if (group == null) {
				wsDeleteMemberResults
						.assignResultCode(WsDeleteMemberResultCode.INVALID_QUERY);
				wsDeleteMemberResults.appendResultMessage("Cant find group: "
						+ wsGroupLookup + ".  ");
				return wsDeleteMemberResults;
			}

			int resultIndex = 0;

			for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
				WsDeleteMemberResult wsDeleteMemberResult = new WsDeleteMemberResult();
				wsDeleteMemberResults.getResults()[resultIndex++] = wsDeleteMemberResult;
				try {
					// default to non-success
					wsDeleteMemberResult.setSuccess("F");

					wsDeleteMemberResult.setSubjectId(wsSubjectLookup
							.getSubjectId());
					wsDeleteMemberResult.setSubjectIdentifier(wsSubjectLookup
							.getSubjectIdentifier());

					Subject subject = wsSubjectLookup.retrieveSubject();

					// make sure the subject is there
					if (subject == null) {
						// see why not
						SubjectFindResult subjectFindResult = wsSubjectLookup
								.retrieveSubjectFindResult();
						String error = "Subject: " + wsSubjectLookup
								+ " had problems: " + subjectFindResult;
						wsDeleteMemberResult.setResultMessage(error);
						throw new NullPointerException(error);
					}

					// these will probably match, but just in case
					if (StringUtils
							.isBlank(wsDeleteMemberResult.getSubjectId())) {
						wsDeleteMemberResult.setSubjectId(subject.getId());
					}

					try {
						// dont fail if already a direct member
						if (group.hasImmediateMember(subject)) {
							group.deleteMember(subject);
						}
						wsDeleteMemberResult.setSuccess("T");
						wsDeleteMemberResult.setResultCode("SUCCESS");

					} catch (InsufficientPrivilegeException ipe) {
						wsDeleteMemberResult
								.setResultCode("INSUFFICIENT_PRIVILEGES");
						wsDeleteMemberResult.setResultMessage(ExceptionUtils
								.getFullStackTrace(ipe));
					}
				} catch (Exception e) {
					wsDeleteMemberResult.setResultCode("EXCEPTION");
					wsDeleteMemberResult.setResultMessage(ExceptionUtils
							.getFullStackTrace(e));
					LOG.error(wsSubjectLookup + ", " + e, e);
				}
			}

		} catch (RuntimeException re) {
			wsDeleteMemberResults
					.assignResultCode(WsDeleteMemberResultCode.EXCEPTION);
			String theError = "Problem deleting member to group: wsGroupLookup: "
					+ wsGroupLookup
					+ ", subjectLookups: "
					+ GrouperServiceUtils.toStringForLog(subjectLookups)
					+ ", actAsSubject: " + actAsSubject + ".  ";
			wsDeleteMemberResults.appendResultMessage(theError);
			// this is sent back to the caller anyway, so just log, and not send
			// back again
			LOG.error(
					theError
							+ ", wsDeleteMemberResults: "
							+ GrouperServiceUtils
									.toStringForLog(wsDeleteMemberResults), re);
		} finally {
			if (session != null) {
				try {
					session.stop();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		if (wsDeleteMemberResults.getResults() != null) {
			// check all entries
			int successes = 0;
			int failures = 0;
			for (WsDeleteMemberResult wsDeleteMemberResult : wsDeleteMemberResults
					.getResults()) {
				boolean success = "T".equalsIgnoreCase(wsDeleteMemberResult
						.getSuccess());
				if (success) {
					successes++;
				} else {
					failures++;
				}
			}
			if (failures > 0) {
				wsDeleteMemberResults.appendResultMessage("There were "
						+ successes + " successes and " + failures
						+ " failures of users deleted to the group.   ");
				wsDeleteMemberResults
						.assignResultCode(WsDeleteMemberResultCode.PROBLEM_DELETING_MEMBERS);
			} else {
				wsDeleteMemberResults
						.assignResultCode(WsDeleteMemberResultCode.SUCCESS);
			}
		}
		if (!"T".equalsIgnoreCase(wsDeleteMemberResults.getSuccess())) {

			LOG.error(wsDeleteMemberResults.getResultMessage());
		}
		return wsDeleteMemberResults;
	}

	/**
	 * get groups for a subject based on filter
	 * 
	 * @param subjectId
	 *            to add (mutually exclusive with subjectIdentifier)
	 * @param subjectIdentifier
	 *            to add (mutually exclusive with subjectId)
	 * @param memberFilter
	 *            can be All, Effective (non immediate), Immediate (direct),
	 *            Composite (if composite group with group math (union, minus,
	 *            etc)
	 * @param actAsSubjectId
	 *            optional: is the subject id of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param actAsSubjectIdentifier
	 *            optional: is the subject identifier of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param paramName0
	 *            reserved for future use
	 * @param paramValue0
	 *            reserved for future use
	 * @param paramName1
	 *            reserved for future use
	 * @param paramValue1
	 *            reserved for future use
	 * @return the result of one member add
	 */
	public WsGetGroupsResults getGroupsSimple(String subjectId,
			String subjectIdentifier, String memberFilter,
			String actAsSubjectId, String actAsSubjectIdentifier,
			String paramName0, String paramValue0, String paramName1,
			String paramValue1) {

		// setup the subject lookup
		WsSubjectLookup subjectLookup = new WsSubjectLookup(subjectId,
				subjectIdentifier);
		WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(
				actAsSubjectId, actAsSubjectIdentifier);

		String[][] params = GrouperServiceUtils.params(paramName0, paramValue0,
				paramName1, paramValue1);
		String[] paramNames = params[0];
		String[] paramValues = params[1];

		WsGetGroupsResults wsGetGroupsResults = getGroups(subjectLookup,
				memberFilter, actAsSubjectLookup, paramNames, paramValues);

		return wsGetGroupsResults;

	}

	/**
	 * add member to a group (if already a direct member, ignore)
	 * 
	 * @param groupName
	 *            to lookup the group (mutually exclusive with groupUuid)
	 * @param groupUuid
	 *            to lookup the group (mutually exclusive with groupName)
	 * @param subjectId
	 *            to add (mutually exclusive with subjectIdentifier)
	 * @param subjectIdentifier
	 *            to add (mutually exclusive with subjectId)
	 * @param actAsSubjectId
	 *            optional: is the subject id of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param actAsSubjectIdentifier
	 *            optional: is the subject identifier of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param paramName0
	 *            reserved for future use
	 * @param paramValue0
	 *            reserved for future use
	 * @param paramName1
	 *            reserved for future use
	 * @param paramValue1
	 *            reserved for future use
	 * @return the result of one member add
	 */
	public WsAddMemberResult addMemberSimple(String groupName,
			String groupUuid, String subjectId, String subjectIdentifier,
			String actAsSubjectId, String actAsSubjectIdentifier,
			String paramName0, String paramValue0, String paramName1,
			String paramValue1) {

		// setup the group lookup
		WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);

		// setup the subject lookup
		WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
		subjectLookups[0] = new WsSubjectLookup(subjectId, subjectIdentifier);
		WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(
				actAsSubjectId, actAsSubjectIdentifier);

		String[][] params = GrouperServiceUtils.params(paramName0, paramValue0,
				paramName1, paramValue1);
		String[] paramNames = params[0];
		String[] paramValues = params[1];

		WsAddMemberResults wsAddMemberResults = addMember(wsGroupLookup,
				subjectLookups, "F", actAsSubjectLookup, paramNames,
				paramValues);

		WsAddMemberResult[] results = wsAddMemberResults.getResults();
		if (results != null && results.length > 0) {
			return results[0];
		}
		// didnt even get that far to where there is a subject result
		WsAddMemberResult wsAddMemberResult = new WsAddMemberResult();
		wsAddMemberResult.setResultMessage(wsAddMemberResults
				.getResultMessage());

		// convert the outer code to the inner code
		WsAddMemberResultsCode wsAddMemberResultsCode = wsAddMemberResults
				.retrieveResultCode();
		wsAddMemberResult
				.assignResultCode(wsAddMemberResultsCode == null ? WsAddMemberResultCode.EXCEPTION
						: wsAddMemberResultsCode.convertToResultCode());

		wsAddMemberResult.setSubjectId(subjectId);
		wsAddMemberResult.setSubjectIdentifier(subjectIdentifier);

		// definitely not a success
		wsAddMemberResult.setSuccess("F");

		return wsAddMemberResult;

	}

	/**
	 * If all privilege params are empty, then it is viewonly. If any are set,
	 * then the privileges will be set (and returned)
	 * 
	 * @param groupName
	 *            to lookup the group (mutually exclusive with groupUuid)
	 * @param groupUuid
	 *            to lookup the group (mutually exclusive with groupName)
	 * @param subjectId
	 *            to assign (mutually exclusive with subjectIdentifier)
	 * @param subjectIdentifier
	 *            to assign (mutually exclusive with subjectId)
	 * @param adminAllowed
	 *            T for allowed, F for not allowed, blank for unchanged
	 * @param optinAllowed
	 *            T for allowed, F for not allowed, blank for unchanged
	 * @param optoutAllowed
	 *            T for allowed, F for not allowed, blank for unchanged
	 * @param readAllowed
	 *            T for allowed, F for not allowed, blank for unchanged
	 * @param viewAllowed
	 *            T for allowed, F for not allowed, blank for unchanged
	 * @param updateAllowed
	 *            T for allowed, F for not allowed, blank for unchanged
	 * @param actAsSubjectId
	 *            optional: is the subject id of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param actAsSubjectIdentifier
	 *            optional: is the subject identifier of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param paramName0
	 *            reserved for future use
	 * @param paramValue0
	 *            reserved for future use
	 * @param paramName1
	 *            reserved for future use
	 * @param paramValue1
	 *            reserved for future use
	 * @return the result of one member add
	 */
	public WsViewOrEditPrivilegesResult viewOrEditPrivilegesSimple(
			String groupName, String groupUuid, String subjectId,
			String subjectIdentifier, String adminAllowed, String optinAllowed,
			String optoutAllowed, String readAllowed, String updateAllowed,
			String viewAllowed, String actAsSubjectId,
			String actAsSubjectIdentifier, String paramName0,
			String paramValue0, String paramName1, String paramValue1) {

		// setup the group lookup
		WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);

		// setup the subject lookup
		WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
		subjectLookups[0] = new WsSubjectLookup(subjectId, subjectIdentifier);
		WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(
				actAsSubjectId, actAsSubjectIdentifier);

		String[][] params = GrouperServiceUtils.params(paramName0, paramValue0,
				paramName1, paramValue1);
		String[] paramNames = params[0];
		String[] paramValues = params[1];

		WsViewOrEditPrivilegesResults wsViewOrEditPrivilegesResults = viewOrEditPrivileges(
				wsGroupLookup, subjectLookups, adminAllowed, optinAllowed,
				optoutAllowed, readAllowed, updateAllowed, viewAllowed,
				actAsSubjectLookup, paramNames, paramValues);

		WsViewOrEditPrivilegesResult[] results = wsViewOrEditPrivilegesResults
				.getResults();
		if (results != null && results.length > 0) {
			return results[0];
		}
		// didnt even get that far to where there is a subject result
		WsViewOrEditPrivilegesResult wsViewOrEditPrivilegesResult = new WsViewOrEditPrivilegesResult();
		wsViewOrEditPrivilegesResult
				.setResultMessage(wsViewOrEditPrivilegesResults
						.getResultMessage());

		// convert the outer code to the inner code
		WsViewOrEditPrivilegesResultsCode wsViewOrEditPrivilegesResultsCode = wsViewOrEditPrivilegesResults
				.retrieveResultCode();
		wsViewOrEditPrivilegesResult
				.assignResultCode(wsViewOrEditPrivilegesResultsCode == null ? WsViewOrEditPrivilegesResultCode.EXCEPTION
						: wsViewOrEditPrivilegesResultsCode
								.convertToResultCode());

		wsViewOrEditPrivilegesResult.setSubjectId(subjectId);
		wsViewOrEditPrivilegesResult.setSubjectIdentifier(subjectIdentifier);

		// definitely not a success
		wsViewOrEditPrivilegesResult.setSuccess("F");

		return wsViewOrEditPrivilegesResult;

	}

	/**
	 * see if a group has a member (if already a direct member, ignore)
	 * 
	 * @param groupName
	 *            to lookup the group (mutually exclusive with groupUuid)
	 * @param groupUuid
	 *            to lookup the group (mutually exclusive with groupName)
	 * @param subjectId
	 *            to query (mutually exclusive with subjectIdentifier)
	 * @param subjectIdentifier
	 *            to query (mutually exclusive with subjectId)
	 * @param memberFilter
	 *            can be All, Effective (non immediate), Immediate (direct),
	 *            Composite (if composite group with group math (union, minus,
	 *            etc)
	 * @param actAsSubjectId
	 *            optional: is the subject id of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param actAsSubjectIdentifier
	 *            optional: is the subject identifier of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param fieldName
	 *            is if the Group.hasMember() method with field is to be called
	 *            (e.g. admins, optouts, optins, etc from Field table in DB)
	 * @param paramName0
	 *            reserved for future use
	 * @param paramValue0
	 *            reserved for future use
	 * @param paramName1
	 *            reserved for future use
	 * @param paramValue1
	 *            reserved for future use
	 * @return the result of one member query
	 */
	public WsHasMemberResult hasMemberSimple(String groupName,
			String groupUuid, String subjectId, String subjectIdentifier,
			String memberFilter, String actAsSubjectId,
			String actAsSubjectIdentifier, String fieldName, String paramName0,
			String paramValue0, String paramName1, String paramValue1) {

		// setup the group lookup
		WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);

		// setup the subject lookup
		WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
		subjectLookups[0] = new WsSubjectLookup(subjectId, subjectIdentifier);
		WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(
				actAsSubjectId, actAsSubjectIdentifier);

		String[][] params = GrouperServiceUtils.params(paramName0, paramValue0,
				paramName1, paramValue1);
		String[] paramNames = params[0];
		String[] paramValues = params[1];

		WsHasMemberResults wsHasMemberResults = hasMember(wsGroupLookup,
				subjectLookups, memberFilter, actAsSubjectLookup, fieldName,
				paramNames, paramValues);

		WsHasMemberResult[] results = wsHasMemberResults.getResults();
		if (results != null && results.length > 0) {
			return results[0];
		}
		// didnt even get that far to where there is a subject result
		WsHasMemberResult wsHasMemberResult = new WsHasMemberResult();
		wsHasMemberResult.setResultMessage(wsHasMemberResults
				.getResultMessage());

		// convert the outer code to the inner code
		WsHasMemberResultsCode wsHasMemberResultsCode = wsHasMemberResults
				.retrieveResultCode();
		wsHasMemberResult
				.assignResultCode(wsHasMemberResultsCode == null ? WsHasMemberResultCode.EXCEPTION
						: wsHasMemberResultsCode.convertToResultCode());

		wsHasMemberResult.setSubjectId(subjectId);
		wsHasMemberResult.setSubjectIdentifier(subjectIdentifier);

		// definitely not a success

		return wsHasMemberResult;

	}

	/**
	 * delete member to a group (if not already a direct member, ignore)
	 * 
	 * @param groupName
	 *            to lookup the group (mutually exclusive with groupUuid)
	 * @param groupUuid
	 *            to lookup the group (mutually exclusive with groupName)
	 * @param subjectId
	 *            to lookup the subject (mutually exclusive with
	 *            subjectIdentifier)
	 * @param subjectIdentifier
	 *            to lookup the subject (mutually exclusive with subjectId)
	 * @param actAsSubjectId
	 *            optional: is the subject id of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param actAsSubjectIdentifier
	 *            optional: is the subject identifier of subject to act as (if
	 *            proxying). Only pass one of actAsSubjectId or
	 *            actAsSubjectIdentifer
	 * @param paramName0
	 *            reserved for future use
	 * @param paramValue0
	 *            reserved for future use
	 * @param paramName1
	 *            reserved for future use
	 * @param paramValue1
	 *            reserved for future use
	 * @return the result of one member delete
	 */
	public WsDeleteMemberResult deleteMemberSimple(String groupName,
			String groupUuid, String subjectId, String subjectIdentifier,
			String actAsSubjectId, String actAsSubjectIdentifier,
			String paramName0, String paramValue0, String paramName1,
			String paramValue1) {

		// setup the group lookup
		WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);

		// setup the subject lookup
		WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
		subjectLookups[0] = new WsSubjectLookup(subjectId, subjectIdentifier);
		WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(
				actAsSubjectId, actAsSubjectIdentifier);

		String[][] params = GrouperServiceUtils.params(paramName0, paramValue0,
				paramName1, paramValue1);
		String[] paramNames = params[0];
		String[] paramValues = params[1];

		WsDeleteMemberResults wsDeleteMemberResults = deleteMember(
				wsGroupLookup, subjectLookups, actAsSubjectLookup, paramNames,
				paramValues);

		WsDeleteMemberResult[] results = wsDeleteMemberResults.getResults();
		if (results != null && results.length > 0) {
			return results[0];
		}
		// didnt even get that far to where there is a subject result
		WsDeleteMemberResult wsDeleteMemberResult = new WsDeleteMemberResult();
		wsDeleteMemberResult.setResultMessage(wsDeleteMemberResults
				.getResultMessage());
		wsDeleteMemberResult.setResultCode(wsDeleteMemberResults
				.getResultCode());
		wsDeleteMemberResult.setSubjectId(subjectId);
		wsDeleteMemberResult.setSubjectIdentifier(subjectIdentifier);

		// definitely not a success
		wsDeleteMemberResult.setSuccess("F");

		return wsDeleteMemberResult;

	}

	// /**
	// * web service wrapper for find all subjects based on query
	// * @param query
	// * @return
	// */
	// @SuppressWarnings("unchecked")
	// public WsSubject[] findAll(String query) {
	// Set<JDBCSubject> subjectSet = SubjectFinder.findAll(query);
	// if (subjectSet == null || subjectSet.size() == 0) {
	// return null;
	// }
	// //convert the set to a list
	// WsSubject[] results = new WsSubject[subjectSet.size()];
	// int i=0;
	// for (JDBCSubject jdbcSubject : subjectSet) {
	// WsSubject wsSubject = new WsSubject(jdbcSubject);
	// results[i++] = wsSubject;
	// }
	// return results;
	// }

}
