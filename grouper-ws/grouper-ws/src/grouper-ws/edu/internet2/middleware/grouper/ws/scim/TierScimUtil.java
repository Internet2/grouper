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
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.SubjectFinder;
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

/**
 * Converts Grouper objects to SCIM 2 resources.
 *
 * <p>Subject identity data in a response (member refs, values and display names) is taken
 * from the subject as resolved under the current (caller's) GrouperSession, never from the
 * cached columns of the grouper_members row (Member.getName(), getSubjectId(), ...). The cached
 * columns are filled from the unfiltered source record, so serializing them would bypass the
 * configured SubjectCustomizer (which can hide or rewrite subjects for some callers).
 * The SCIM providers start a GrouperSession as the logged-in subject before calling the
 * converters, so the static session here is the caller's session.</p>
 */
public class TierScimUtil {

  /**
   * Resolve the subject of a member as the subject of the current GrouperSession, applying the
   * SubjectCustomizer (SubjectFinder.findByIdAndSource filters per session subject, and its
   * cache is keyed by the session subject, so a subject primed as root is not reused here).
   * @param member the member to resolve, may be null
   * @return the subject the caller may see, or null if the subject is hidden from the caller,
   * no longer exists, or the source cannot resolve it (fail closed: callers must then emit
   * no subject-derived data)
   */
  private static Subject visibleSubject(Member member) {
    if (member == null) {
      return null;
    }
    try {
      return SubjectFinder.findByIdAndSource(member.getSubjectId(), member.getSubjectSourceId(), false);
    } catch (RuntimeException e) {
      // source unavailable, not unique, etc: do not fall back to the cached member columns
      return null;
    }
  }

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
      // resolve as the caller; a member the caller may not see is left out of the list
      // entirely (same as the SubjectCustomizer removing it from a subject search)
      Subject subject = visibleSubject(member);
      if (subject == null) {
        continue;
      }
      com.unboundid.scim2.common.types.Member rsrcMember = new com.unboundid.scim2.common.types.Member();
      rsrcMember.setRef(URI.create("../Users/" + subject.getId()));  // match existing behavior
      rsrcMember.setValue(member.getId());
      // display name as the caller sees it (the customizer may rewrite it), not the cached name
      rsrcMember.setDisplay(subject.getName());
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
    // SECURITY (issue 92, 2026-11-10): userName comes from the subject as resolved for the caller (subject
    // customizer and attribute rules applied), not from the member cache; omitted when hidden
    String scimUserName = scimUserName(subject);
    userResource.setUserName(scimUserName);
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

    // SECURITY (issue 129, 2026-11-10): only groups the caller can READ.  Without privileges GroupFinder
    // does not filter, so every group of the subject was listed, hidden ones included
    Set<Group> groups = new GroupFinder().assignSubject(subject).assignFieldName("members")
        .assignPrivileges(AccessPrivilege.READ_PRIVILEGES).findGroups();
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
            (scimUserName == null ? "" : scimUserName);

    String version = DigestUtils.sha1Hex(subjectAsString);
    Meta meta = new Meta();
    meta.setVersion(version);
    meta.setResourceType("User");

    userResource.setMeta(meta);

    return userResource;

  }

  /**
   * the SCIM userName: the subject's identifier attribute (the source's subjectIdentifierAttribute 0) as
   * the caller sees it, or null if the source has none or the value is hidden from the caller
   * @param subject resolved in the caller's session
   * @return the user name or null
   */
  private static String scimUserName(Subject subject) {
    try {
      java.util.Map<Integer, String> identifierAttributes = subject.getSource() == null ? null
          : subject.getSource().getSubjectIdentifierAttributes();
      String attributeName = identifierAttributes == null ? null : identifierAttributes.get(0);
      if (attributeName == null || attributeName.trim().length() == 0) {
        return null;
      }
      String value = subject.getAttributeValue(attributeName);
      return value == null || value.trim().length() == 0 ? null : value;
    } catch (RuntimeException re) {
      // fail closed: no user name rather than the cached one
      return null;
    }
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

    // resolve the member as the caller; if it is hidden (or unresolvable) the member object
    // carries no ref, value or display, so nothing from the cached member row is disclosed
    com.unboundid.scim2.common.types.Member member = new com.unboundid.scim2.common.types.Member();
    Subject memberSubject = visibleSubject(mship.getMember());
    if (memberSubject != null) {
      member.setRef(URI.create("../Users/" + memberSubject.getId()))
            .setDisplay(memberSubject.getName())
            .setValue(memberSubject.getId());
    }
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
