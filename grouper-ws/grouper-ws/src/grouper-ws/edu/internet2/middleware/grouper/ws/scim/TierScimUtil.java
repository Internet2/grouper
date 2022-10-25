/****
 * Copyright 2022 Internet2
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
 ***/

package edu.internet2.middleware.grouper.ws.scim;

import com.unboundid.scim2.common.messages.ErrorResponse;
import com.unboundid.scim2.common.types.EnterpriseUserExtension;
import com.unboundid.scim2.common.types.GroupResource;
import com.unboundid.scim2.common.types.Meta;
import com.unboundid.scim2.common.types.UserResource;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;
import edu.internet2.middleware.grouper.ws.scim.resources.TierGroupResource;
import edu.internet2.middleware.grouper.ws.scim.resources.TierMembershipResource;
import edu.internet2.middleware.grouper.ws.scim.resources.TierMetaResource;
import edu.internet2.middleware.subject.Subject;
import org.apache.commons.codec.digest.DigestUtils;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TierScimUtil {

  /**
   *
   * @param status HTTP response code to return
   * @param scimType keyword for error message
   * @param detail full error text
   * @return SCIM 2 Error Response
   */
  public static Response errorResponse(Response.Status status, String scimType, String detail) {
    ErrorResponse errorResponse = new ErrorResponse(status.getStatusCode());
    errorResponse.setScimType(scimType);
    errorResponse.setDetail(detail);
    return Response.status(status)
            .entity(errorResponse).build();
  }

  public static GroupResource convertGrouperGroupToScimGroup(Group group) {
    GroupResource groupResource = new GroupResource();
    groupResource.setId(group.getId());
    groupResource.setDisplayName(group.getDisplayName());
    groupResource.setExternalId(group.getName());

    List<com.unboundid.scim2.common.types.Member> rsrcMembers = new ArrayList<>();
    for (Member member: group.getMembers()) {
      com.unboundid.scim2.common.types.Member rsrcMember = new com.unboundid.scim2.common.types.Member();
      rsrcMember.setRef(URI.create("../Users/" + member.getSubjectId()));  // match existing behavior
      rsrcMember.setValue(member.getId());
      rsrcMember.setDisplay(member.getName());
      rsrcMembers.add(rsrcMember);
    }
    groupResource.setMembers(rsrcMembers);

    TierMetaResource extensionMeta = new TierMetaResource()
            .setResultCode("SUCCESS")
            .setResponseDurationMillis(System.currentTimeMillis() - GrouperServiceJ2ee.retrieveRequestStartMillis());

    TierGroupResource extensionGroup = new TierGroupResource()
            .setSystemName(group.getName())
            .setDescription(group.getDescription())
            .setIdIndex(group.getIdIndex());

    groupResource.setExtension(extensionMeta);
    groupResource.setExtension(extensionGroup);

    String groupAsString = group.getId() + "\0" +
            group.getName() + "\0" +
            group.getIdIndex();

    String version = DigestUtils.sha1Hex(groupAsString);
    Meta meta = new Meta();
    meta.setVersion(version);
    meta.setResourceType("Group");

    // Bug in Meta (or json lib?) serializes calendars as numbers; spec requires DateTime (e.g. 2008-01-23T04:56:22Z), so best leave it out
    //Calendar createTime = Calendar.getInstance();
    //createTime.setTime(group.getCreateTime());
    //meta.setCreated(createTime);
    //
    //Calendar modifyTime = Calendar.getInstance();
    //modifyTime.setTime(group.getModifyTime());
    //meta.setLastModified(modifyTime);

    groupResource.setMeta(meta);

    return groupResource;
  }

  public static UserResource convertGrouperMemberToScimUser(GrouperSession grouperSession, Subject subject) {
    UserResource userResource = new UserResource();
    Member member = MemberFinder.findBySubject(grouperSession, subject, true);

    userResource.setId(subject.getId()); // better would be member uuid
    //userResource.setExternalId(member.getSubjectId());
    userResource.setUserName(member.getSubjectIdentifier0()); // should the identifier to use be configurable?
    userResource.setUserType(subject.getSourceId());
    userResource.setDisplayName(subject.getName());
    userResource.setActive(true);

    TierMetaResource extensionMeta = new TierMetaResource()
            .setResultCode("SUCCESS")
            .setResponseDurationMillis(System.currentTimeMillis() - GrouperServiceJ2ee.retrieveRequestStartMillis());

    userResource.setExtension(extensionMeta);

    EnterpriseUserExtension entUserExtension = new EnterpriseUserExtension();
    entUserExtension.setEmployeeNumber(subject.getId());
    userResource.setExtension(entUserExtension);

    Set<Group> groups = new GroupFinder().assignSubject(subject).assignFieldName("members").findGroups();
    List<com.unboundid.scim2.common.types.Group> rsrcGroups = new ArrayList<>();
    for (Group group: groups) {
      com.unboundid.scim2.common.types.Group rsrcGroup = new com.unboundid.scim2.common.types.Group();
      rsrcGroup.setRef(URI.create("../Groups/" + group.getUuid()));
      rsrcGroup.setValue(group.getName());
      rsrcGroup.setDisplay(group.getDisplayExtension());
      rsrcGroups.add(rsrcGroup);
    }
    userResource.setGroups(rsrcGroups);

    String subjectAsString = member.getUuid() + "\0" +
            member.getSubjectSourceId() + "\0" +
            member.getSubjectId() + "\0" +
            member.getSubjectIdentifier0();

    String version = DigestUtils.sha1Hex(subjectAsString);
    Meta meta = new Meta();
    meta.setVersion(version);
    meta.setResourceType("User");

    userResource.setMeta(meta);

    return userResource;

  }

  public static TierMembershipResource convertGrouperMembershipToScimResource(Membership mship) {
    TierMembershipResource resource = new TierMembershipResource()
            .setMembershipType(mship.getType())
            .setEnabled(mship.isEnabled());

    resource.setId(mship.getUuid());

    com.unboundid.scim2.common.types.Group group = new com.unboundid.scim2.common.types.Group()
            .setRef(URI.create("../Groups/" + mship.getOwnerGroupId()))
            .setDisplay(mship.getOwnerGroup().getDisplayName())
            .setValue(mship.getOwnerGroupId());
    resource.setOwner(group);

    com.unboundid.scim2.common.types.Member member = new com.unboundid.scim2.common.types.Member()
            .setRef(URI.create("../Users/" + mship.getMemberSubjectId()))
            .setDisplay(mship.getMember().getName())
            .setValue(mship.getMemberSubjectId());
    resource.setMember(member);

    String hashable = mship.getUuid() + "\0" + mship.getType() + "\0" + mship.isEnabled();
    String version = DigestUtils.sha1Hex(hashable);
    Meta meta = new Meta();
    meta.setVersion(version);
    meta.setResourceType("Membership");

    resource.setMeta(meta);
    return resource;
  }
}
