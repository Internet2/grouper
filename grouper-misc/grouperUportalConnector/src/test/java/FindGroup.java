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
 * Copyright 2010 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import edu.internet2.middleware.grouperClient.api.GcFindGroups;
//import edu.internet2.middleware.grouperClient.api.GcFindStems;
//import edu.internet2.middleware.grouperClient.api.GcGetGroups; //import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
//import edu.internet2.middleware.grouperClient.ws.beans.WsFindStemsResults;
//import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
//import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults; //import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
//import edu.internet2.middleware.grouperClient.ws.beans.WsStem;
//import edu.internet2.middleware.grouperClient.ws.beans.WsStemQueryFilter;
//import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

/**
 *
 */
public class FindGroup {

	FindGroup() {
	}

	/**
	 * @param args
	 *            not used.
	 */
	public static void main(String[] args) {

		/*
		 * GcGetGroups getGroups = new GcGetGroups();
		 * 
		 * getGroups.addSubjectIdentifier("uc:applications:lists:webserv");
		 * System
		 * .out.println("looking for group: uc:org:nsit:webservices:members");
		 * WsGetGroupsResults results = getGroups.execute(); WsResultMeta
		 * resultMetadata = results.getResultMetadata();
		 * 
		 * if (!"T".equals(resultMetadata.getSuccess())) { throw new
		 * RuntimeException("Error finding grops: " +
		 * resultMetadata.getSuccess() + ", " + resultMetadata.getResultCode() +
		 * ", " + resultMetadata.getResultMessage()); }
		 * 
		 * WsGetGroupsResult[] wsResults = results.getResults();
		 * 
		 * if (wsResults != null) { for (WsGetGroupsResult wsg : wsResults) {
		 * System.out.println(wsg); if (wsg.getResultMetadata() != null) {
		 * WsResultMeta md = wsg.getResultMetadata();
		 * System.out.println("has result meta data");
		 * System.out.println("result code: " + md.getResultCode());
		 * System.out.println("result code2: " + md.getResultCode2());
		 * System.out.println("message: " + md.getResultMessage());
		 * System.out.println("success: " + md.getSuccess());
		 * System.out.println("\n"); } if (wsg.getWsSubject() != null) {
		 * System.out.println("has subject"); WsSubject sub =
		 * wsg.getWsSubject(); if (sub.getAttributeValues() != null) {
		 * System.out.println("subject has attribute values"); for (String attr
		 * : sub.getAttributeValues()) { System.out.println("subject attr val: "
		 * + attr); } } System.out.println("subject id: " + sub.getId());
		 * System.out.println("subject identifier lookup: " +
		 * sub.getIdentifierLookup()); System.out.println("subject name: " +
		 * sub.getName()); System.out.println("subject resultCode: " +
		 * sub.getResultCode()); System.out.println("subject source id: " +
		 * sub.getSourceId()); System.out.println("subject success: " +
		 * sub.getSuccess()); System.out.println("\n"); }
		 * 
		 * if (wsg.getWsGroups() != null) { System.out.println("has groups");
		 * for (WsGroup g : wsg.getWsGroups()) {
		 * System.out.println("group description: " + g.getDescription());
		 * System.out.println("group display extension: " +
		 * g.getDisplayExtension()); System.out.println("group display name: " +
		 * g.getDisplayName()); System.out.println("group extension: " +
		 * g.getExtension()); System.out.println("group name: " + g.getName());
		 * System.out.println("group UUID: " + g.getUuid()); } } } }
		 */
		/*
		 * GcGetSubjects gps = new GcGetSubjects(); System.out.println(
		 * "looking for groups where the subject Attribute Name cn or uid 'William'"
		 * ); gps.addSubjectAttributeName("cn");
		 * gps.assignSearchString("William");
		 * 
		 * WsGetSubjectsResults results = gps.execute(); WsResultMeta
		 * resultMetadata = results.getResultMetadata();
		 * 
		 * if (!"T".equals(resultMetadata.getSuccess())) { throw new
		 * RuntimeException("Error finding grops: " +
		 * resultMetadata.getSuccess() + ", " + resultMetadata.getResultCode() +
		 * ", " + resultMetadata.getResultMessage()); }
		 * 
		 * WsSubject[] wsResults = results.getWsSubjects();
		 * 
		 * if (results.getResultMetadata() != null) { WsResultMeta md =
		 * results.getResultMetadata();
		 * System.out.println("has result meta data");
		 * System.out.println("result code: " + md.getResultCode());
		 * System.out.println("result code2: " + md.getResultCode2());
		 * System.out.println("message: " + md.getResultMessage());
		 * System.out.println("success: " + md.getSuccess());
		 * System.out.println("\n"); }
		 * 
		 * if (wsResults != null) { for (WsSubject sub : wsResults) { for
		 * (String attr : sub.getAttributeValues()) {
		 * System.out.println("subject attr val: " + attr); }
		 * System.out.println("subject id: " + sub.getId());
		 * System.out.println("subject identifier lookup: " +
		 * sub.getIdentifierLookup()); System.out.println("subject name: " +
		 * sub.getName()); System.out .println("subject resultCode: " +
		 * sub.getResultCode()); System.out.println("subject source id: " +
		 * sub.getSourceId()); System.out.println("subject success: " +
		 * sub.getSuccess()); System.out.println("\n"); }
		 * 
		 * }
		 */
		/*
		 * getGroups = new GcGetGroups();
		 * getGroups.addSubjectIdentifier("billbrown");
		 * System.out.println("looking for group: billbrown"); results =
		 * getGroups.execute(); resultMetadata = results.getResultMetadata();
		 * 
		 * if (!"T".equals(resultMetadata.getSuccess())) { throw new
		 * RuntimeException("Error finding grops: " +
		 * resultMetadata.getSuccess() + ", " + resultMetadata.getResultCode() +
		 * ", " + resultMetadata.getResultMessage()); }
		 * 
		 * wsResults = results.getResults();
		 * 
		 * if (wsResults != null) { for (WsGetGroupsResult wsg : wsResults) {
		 * System.out.println(wsg); if (wsg.getResultMetadata() != null) {
		 * WsResultMeta md = wsg.getResultMetadata();
		 * System.out.println("has result meta data");
		 * System.out.println("result code: " + md.getResultCode());
		 * System.out.println("result code2: " + md.getResultCode2());
		 * System.out.println("message: " + md.getResultMessage());
		 * System.out.println("success: " + md.getSuccess());
		 * System.out.println("\n"); } if (wsg.getWsSubject() != null) {
		 * System.out.println("has subject"); WsSubject sub =
		 * wsg.getWsSubject(); if (sub.getAttributeValues() != null) {
		 * System.out.println("subject has attribute values"); for (String attr
		 * : sub.getAttributeValues()) { System.out.println("subject attr val: "
		 * + attr); } } System.out.println("subject id: " + sub.getId());
		 * System.out.println("subject identifier lookup: " +
		 * sub.getIdentifierLookup()); System.out.println("subject name: " +
		 * sub.getName()); System.out.println("subject resultCode: " +
		 * sub.getResultCode()); System.out.println("subject source id: " +
		 * sub.getSourceId()); System.out.println("subject success: " +
		 * sub.getSuccess()); System.out.println("\n"); }
		 * 
		 * if (wsg.getWsGroups() != null) { System.out.println("has groups");
		 * for (WsGroup g : wsg.getWsGroups()) {
		 * System.out.println("group description: " + g.getDescription());
		 * System.out.println("group display extension: " +
		 * g.getDisplayExtension()); System.out.println("group display name: " +
		 * g.getDisplayName()); System.out.println("group extension: " +
		 * g.getExtension()); System.out.println("group name: " + g.getName());
		 * System.out.println("group UUID: " + g.getUuid()); } } } }
		 */

		GcFindGroups groups = new GcFindGroups();
		WsQueryFilter filter = new WsQueryFilter();
		// filter.setQueryTerm("webserv");
		filter.setQueryFilterType("FIND_BY_GROUP_NAME_APPROXIMATE");
		filter.setGroupName("webserv");
		groups.assignQueryFilter(filter);

		WsFindGroupsResults results = groups.execute();

		WsResultMeta resultMetadata = results.getResultMetadata();

		if (!"T".equals(resultMetadata.getSuccess())) {
			throw new RuntimeException("Error finding grops: "
					+ resultMetadata.getSuccess() + ", "
					+ resultMetadata.getResultCode() + ", "
					+ resultMetadata.getResultMessage());
		}

		WsGroup[] wsResults = results.getGroupResults();

		int i = 0;
		if (wsResults != null) {
			for (WsGroup g : wsResults) {
				System.out.println("group " + (++i));
				System.out.println("group description: " + g.getDescription());
				System.out.println("group display extension: "
						+ g.getDisplayExtension());
				System.out.println("group display name: " + g.getDisplayName());
				System.out.println("group extension: " + g.getExtension());
				System.out.println("group name: " + g.getName());
				System.out.println("group UUID: " + g.getUuid());
			}
		}

		// ////////////STEMS////////////////////////

		/*
		 * 
		 * GcFindStems gcFindStems = new GcFindStems(); WsStemQueryFilter
		 * wsStemQueryFilter = new WsStemQueryFilter();
		 * wsStemQueryFilter.setStemName("web services"); //
		 * wsStemQueryFilter.setStemAttributeName("name"); //
		 * wsStemQueryFilter.setStemAttributeValue("billb"); //
		 * wsStemQueryFilter.setStemQueryFilterType("FIND_BY_STEM_UUID"); //
		 * wsStemQueryFilter.setStemQueryFilterType("FIND_BY_STEM_NAME");
		 * wsStemQueryFilter
		 * .setStemQueryFilterType("FIND_BY_STEM_NAME_APPROXIMATE"); //
		 * wsStemQueryFilter //
		 * .setStemQueryFilterType("FIND_BY_PARENT_STEM_NAME"); //
		 * wsStemQueryFilter //
		 * .setStemQueryFilterType("FIND_BY_APPROXIMATE_ATTRIBUTE"); //
		 * wsStemQueryFilter.setStemQueryFilterType("AND"); //
		 * wsStemQueryFilter.setStemQueryFilterType("OR"); //
		 * wsStemQueryFilter.setStemQueryFilterType("MINUS");
		 * 
		 * gcFindStems.assignStemQueryFilter(wsStemQueryFilter);
		 * 
		 * WsFindStemsResults wsFindStemsResults = gcFindStems.execute();
		 * 
		 * resultMetadata = wsFindStemsResults.getResultMetadata();
		 * 
		 * if (!"T".equals(resultMetadata.getSuccess())) { throw new
		 * RuntimeException("Error finding stems: " +
		 * resultMetadata.getSuccess() + ", " + resultMetadata.getResultCode() +
		 * ", " + resultMetadata.getResultMessage()); }
		 * 
		 * WsStem[] wsStems = wsFindStemsResults.getStemResults();
		 * 
		 * if (wsStems != null) { for (WsStem wsStem : wsStems) {
		 * System.out.println("stem name: " + wsStem.getName()); } }
		 */

		// ///////////////////////////////////

		/*
		 * getGroups = new GcGetGroups();
		 * getGroups.addSubjectAttributeName("William R Brown");
		 * System.out.println("looking for group: billbrown");
		 * 
		 * WsMemberFilter fil = new WsMemberFilter(); fil. results =
		 * getGroups.execute(); resultMetadata = results.getResultMetadata();
		 * 
		 * if (!"T".equals(resultMetadata.getSuccess())) { throw new
		 * RuntimeException("Error finding grops: " +
		 * resultMetadata.getSuccess() + ", " + resultMetadata.getResultCode() +
		 * ", " + resultMetadata.getResultMessage()); }
		 * 
		 * wsResults = results.getResults();
		 * 
		 * if (wsResults != null) { for (WsGetGroupsResult wsg : wsResults) {
		 * System.out.println(wsg); if (wsg.getResultMetadata() != null) {
		 * WsResultMeta md = wsg.getResultMetadata();
		 * System.out.println("has result meta data");
		 * System.out.println("result code: " + md.getResultCode());
		 * System.out.println("result code2: " + md.getResultCode2());
		 * System.out.println("message: " + md.getResultMessage());
		 * System.out.println("success: " + md.getSuccess());
		 * System.out.println("\n"); } if (wsg.getWsSubject() != null) {
		 * System.out.println("has subject"); WsSubject sub =
		 * wsg.getWsSubject(); if (sub.getAttributeValues() != null) {
		 * System.out.println("subject has attribute values"); for (String attr
		 * : sub.getAttributeValues()) { System.out.println("subject attr val: "
		 * + attr); } } System.out.println("subject id: " + sub.getId());
		 * System.out.println("subject identifier lookup: " +
		 * sub.getIdentifierLookup()); System.out.println("subject name: " +
		 * sub.getName()); System.out.println("subject resultCode: " +
		 * sub.getResultCode()); System.out.println("subject source id: " +
		 * sub.getSourceId()); System.out.println("subject success: " +
		 * sub.getSuccess()); System.out.println("\n"); }
		 * 
		 * if (wsg.getWsGroups() != null) { System.out.println("has groups");
		 * for (WsGroup g : wsg.getWsGroups()) {
		 * System.out.println("group description: " + g.getDescription());
		 * System.out.println("group display extension: " +
		 * g.getDisplayExtension()); System.out.println("group display name: " +
		 * g.getDisplayName()); System.out.println("group extension: " +
		 * g.getExtension()); System.out.println("group name: " + g.getName());
		 * System.out.println("group UUID: " + g.getUuid()); } } } }
		 */
	}
}
