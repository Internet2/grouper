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
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

/**
 *
 */
public class FindMember {

	FindMember() {
	}

	/**
	 * @param args
	 *            not used.
	 */
	public static void main(String[] args) {

		GcGetMembers getGroupsMembers = new GcGetMembers();
		getGroupsMembers.addGroupName("uc:org:nsit:webservices:members");
		System.out
				.println("looking for group: uc:org:nsit:webservices:members");
		WsGetMembersResults results = getGroupsMembers.execute();
		WsResultMeta resultMetadata = results.getResultMetadata();

		if (!"T".equals(resultMetadata.getSuccess())) {
			throw new RuntimeException("Error finding grops: "
					+ resultMetadata.getSuccess() + ", "
					+ resultMetadata.getResultCode() + ", "
					+ resultMetadata.getResultMessage());
		}

		WsGetMembersResult[] wsResults = results.getResults();

		if (wsResults != null) {
			for (WsGetMembersResult wsg : wsResults) {
				System.out.println(wsg);
				if (wsg.getResultMetadata() != null) {
					WsResultMeta md = wsg.getResultMetadata();
					System.out.println("has result meta data");
					System.out.println("result code: " + md.getResultCode());
					System.out.println("result code2: " + md.getResultCode2());
					System.out.println("message: " + md.getResultMessage());
					System.out.println("success: " + md.getSuccess());
					System.out.println("\n");
				}
				if (wsg.getWsGroup() != null) {
					System.out.println("has group");
					WsGroup grp = wsg.getWsGroup();
					System.out.println("group description: "
							+ grp.getDescription());
					System.out.println("group display extension: "
							+ grp.getDisplayExtension());
					System.out.println("group display name: "
							+ grp.getDisplayName());
					System.out
							.println("group extension: " + grp.getExtension());
					System.out.println("group name: " + grp.getName());
					System.out.println("group UUID: " + grp.getUuid());
				}

				if (wsg.getWsSubjects() != null) {
					System.out.println("has subjects");
					for (WsSubject sub : wsg.getWsSubjects()) {
						if (sub.getAttributeValues() != null) {
							System.out.println("subject has attribute values");
							for (String attr : sub.getAttributeValues()) {
								System.out.println("subject attr val: " + attr);
							}
						}
						System.out.println("subject id: " + sub.getId());
						System.out.println("subject identifier lookup: "
								+ sub.getIdentifierLookup());
						System.out.println("subject name: " + sub.getName());
						System.out.println("subject resultCode: "
								+ sub.getResultCode());
						System.out.println("subject source id: "
								+ sub.getSourceId());
						System.out.println("subject success: "
								+ sub.getSuccess());
						System.out.println("\n");
					}
				}
			}
		}

		/*
		 * //try using the UUID GcGetMembers getGroupsMembers = new
		 * GcGetMembers();getGroupsMembers.addSubjectAttributeName(
		 * "87f31a8d-142b-450c-a4fc-7b196aeb3a89");System.out.println(
		 * "looking for group: 87f31a8d-142b-450c-a4fc-7b196aeb3a89");
		 * WsGetMembersResults results = getGroupsMembers.execute();
		 * WsResultMeta resultMetadata = results.getResultMetadata();
		 * 
		 * if (!"T".equals(resultMetadata.getSuccess())) { throw new
		 * RuntimeException("Error finding grops: " +
		 * resultMetadata.getSuccess() + ", " + resultMetadata.getResultCode() +
		 * ", " + resultMetadata.getResultMessage()); }
		 * 
		 * WsGetMembersResult[] wsResults = results.getResults();
		 * 
		 * if (wsResults != null) { for (WsGetMembersResult wsg : wsResults) {
		 * System.out.println(wsg); if (wsg.getResultMetadata() != null) {
		 * WsResultMeta md = wsg.getResultMetadata();
		 * System.out.println("has result meta data");
		 * System.out.println("result code: " + md.getResultCode());
		 * System.out.println("result code2: " + md.getResultCode2());
		 * System.out.println("message: " + md.getResultMessage());
		 * System.out.println("success: " + md.getSuccess());
		 * System.out.println("\n"); } if (wsg.getWsGroup() != null) {
		 * System.out.println("has group"); WsGroup grp = wsg.getWsGroup();
		 * System.out.println("group description: " + grp.getDescription());
		 * System.out.println("group display extension: " +
		 * grp.getDisplayExtension()); System.out.println("group display name: "
		 * + grp.getDisplayName()); System.out.println("group extension: " +
		 * grp.getExtension()); System.out.println("group name: " +
		 * grp.getName()); System.out.println("group UUID: " + grp.getUuid()); }
		 * 
		 * 
		 * 
		 * 
		 * if (wsg.getWsSubjects() != null) {
		 * System.out.println("has subjects"); for (WsSubject sub :
		 * wsg.getWsSubjects()) { if (sub.getAttributeValues() != null) {
		 * System.out.println("subject has attribute values"); for (String attr
		 * : sub.getAttributeValues()) { System.out.println("subject attr val: "
		 * + attr); } } System.out.println("subject id: " + sub.getId());
		 * System.out.println("subject identifier lookup: " +
		 * sub.getIdentifierLookup()); System.out.println("subject name: " +
		 * sub.getName()); System.out.println("subject resultCode: " +
		 * sub.getResultCode()); System.out.println("subject source id: " +
		 * sub.getSourceId()); System.out.println("subject success: " +
		 * sub.getSuccess()); System.out.println("\n"); } } } }
		 */
	}
}
