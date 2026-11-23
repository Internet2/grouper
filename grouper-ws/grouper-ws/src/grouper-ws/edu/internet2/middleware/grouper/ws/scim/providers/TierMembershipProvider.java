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

package edu.internet2.middleware.grouper.ws.scim.providers;

import com.unboundid.scim2.common.exceptions.NotImplementedException;
import com.unboundid.scim2.common.filters.Filter;
import com.unboundid.scim2.common.filters.FilterType;
import com.unboundid.scim2.common.messages.ListResponse;
import com.unboundid.scim2.common.types.GroupResource;
import com.unboundid.scim2.server.annotations.ResourceType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.MembershipSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.membership.MembershipWriteAuthorization;
import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;
import edu.internet2.middleware.grouper.ws.scim.TierScimPaging;
import edu.internet2.middleware.grouper.ws.scim.TierScimUtil;
import edu.internet2.middleware.grouper.ws.scim.resources.TierGroupResource;
import edu.internet2.middleware.grouper.ws.scim.resources.TierMembershipResource;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.unboundid.scim2.common.utils.ApiConstants.MEDIA_TYPE_SCIM;
import static com.unboundid.scim2.common.utils.ApiConstants.QUERY_PARAMETER_FILTER;
import static com.unboundid.scim2.common.utils.ApiConstants.QUERY_PARAMETER_PAGE_START_INDEX;

@ResourceType(
        description = "SCIM 2.0 Membership",
        name = "Membership",
        schema = TierGroupResource.class,
        discoverable = false)
@Path("/v2/Memberships")
public class TierMembershipProvider {

  @Context
  private Application application;

  /**
   * Upper limit on sise of returned list responses
   */
  public static final int MAX_RESULTS_PER_PAGE = TierScimPaging.MAX_RESULTS_PER_PAGE_DEFAULT;

  @GET
  @Path("/{id}")
  @Consumes({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  @Produces({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  public Response getById(
          @QueryParam(QUERY_PARAMETER_FILTER) final String filterString,
          @PathParam("id") String id,
          @Context final UriInfo uriInfo) {
    if (filterString != null) {
      return TierScimUtil.errorResponse(Response.Status.BAD_REQUEST, "Memberships.get", "Filtering not allowed with id");
    }

    GrouperSession grouperSession = null;

    try {
      final Subject loggedInSubject = GrouperServiceJ2ee.retrieveSubjectLoggedIn();

      grouperSession = GrouperSession.start(loggedInSubject);

      Membership mship = MembershipFinder.findByUuid(grouperSession, id, true, true);

      TierMembershipResource resource = TierScimUtil.convertGrouperMembershipToScimResource(mship);

      return Response
              .status(Response.Status.OK)
              .entity(resource)
              .build();
    } catch (MembershipNotFoundException e) {
      return TierScimUtil.errorResponse(Response.Status.NOT_FOUND, "Memberships.get", e.toString());
    } catch (IllegalArgumentException e) {
      return TierScimUtil.errorResponse(Response.Status.BAD_REQUEST, "Memberships.get", e.toString());
    } catch (Exception e) {
      if (isInsufficientPrivilege(e)) {
        return TierScimUtil.errorResponse(Response.Status.FORBIDDEN, "Memberships.get", e.toString());
      }
      return TierScimUtil.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Memberships.get", e.toString());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  @GET
  @Consumes({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  @Produces({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  public Response getWithFilter(
          @QueryParam(QUERY_PARAMETER_FILTER) final String filterString,
          @QueryParam(QUERY_PARAMETER_PAGE_START_INDEX) final String startIndex,
          @QueryParam("itemsPerPage") final Integer itemsPerPage,
          @Context final UriInfo uriInfo) {

    GrouperSession grouperSession = null;

    final Subject loggedInSubject = GrouperServiceJ2ee.retrieveSubjectLoggedIn();

    Set<Membership> foundMships = new HashSet<>();

    try {
      if (GrouperUtil.isEmpty(filterString)) {
        throw new IllegalArgumentException("Missing filter parameter");
      }

      // SECURITY (GRP-7585): itemsPerPage is clamped to ws.scim.maxResultsPerPage and
      // startIndex is supported; only one page of memberships is converted.  The search itself is
      // bounded by ws.getMemberships.maxResultSize (grouper.properties, "Too many results" above it)
      TierScimPaging paging = new TierScimPaging(startIndex, itemsPerPage);

      grouperSession = GrouperSession.start(loggedInSubject);

      Filter filter = Filter.fromString(filterString);

      MembershipFinder membershipFinder = new MembershipFinder();

      if (filter.getFilterType().equals(FilterType.AND)) {
        for (Filter f: filter.getCombinedFilters()) {
          addFilterToFinder(membershipFinder, f);
        }
      } else if (filter.getFilterType().equals(FilterType.EQUAL)) {
        addFilterToFinder(membershipFinder, filter);
      } else {
        // other filter types (or, pr, ...) used to add no criteria at all: refuse them (400)
        throw new IllegalArgumentException("Only eq and and filters supported");
      }

      List<TierMembershipResource> resources = new ArrayList<>();

      Set<Object[]> memberTuples = membershipFinder.findMembershipsMembers();

      // stable order (by membership id) so startIndex pages do not overlap
      List<Membership> memberships = new ArrayList<>();
      for (Object[] memberTuple: GrouperUtil.nonNull(memberTuples)) {
        memberships.add((Membership) memberTuple[0]);
      }
      Collections.sort(memberships, new Comparator<Membership>() {

        public int compare(Membership o1, Membership o2) {
          String id1 = o1.getUuid() == null ? "" : o1.getUuid();
          String id2 = o2.getUuid() == null ? "" : o2.getUuid();
          return id1.compareTo(id2);
        }
      });

      for (Membership membership: paging.page(memberships)) {
        resources.add(TierScimUtil.convertGrouperMembershipToScimResource(membership));
      }

      ListResponse<TierMembershipResource> listResponse = new ListResponse<TierMembershipResource>(
              memberships.size(),
              resources,
              paging.getStartIndex(), resources.size());

      return Response
              .status(Response.Status.OK)
              .entity(listResponse)
              .build();

    } catch (MembershipNotFoundException | SubjectNotFoundException | GroupNotFoundException e) {
      ListResponse<GroupResource> listResponse = new ListResponse<GroupResource>(
              0,
              new ArrayList<GroupResource>(),
              1, 0);

      return Response
              .status(Response.Status.OK)
              .entity(listResponse)
              .build();
    } catch (IllegalArgumentException e) {
      return TierScimUtil.errorResponse(Response.Status.BAD_REQUEST, "Memberships.getWithFilter", e.toString());
    } catch (Exception e) {
      return TierScimUtil.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Memberships.getWithFilter", e.toString());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  @POST
  @Consumes({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  @Produces({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  public Response create(
          TierMembershipResource resource,
          @Context final UriInfo uriInfo) {

    GrouperSession grouperSession = null;

    try {

      final Subject loggedInSubject = GrouperServiceJ2ee.retrieveSubjectLoggedIn();

      grouperSession = GrouperSession.start(loggedInSubject);

      if (resource.getOwner() == null || resource.getMember() == null) {
        throw new IllegalArgumentException("owner or member property is missing in the request body.");
      }

      Group group = GroupFinder.findByUuid(resource.getOwner().getValue(), true);
      Subject subject = SubjectFinder.findById(resource.getMember().getValue(), true);
      Member member = MemberFinder.findBySubject(grouperSession, subject, false);

      //GRP-4429: MembershipSave.save() doesn't return a membership on insert
      new MembershipSave()
              .assignGroup(group)
              .assignMember(member)
              .assignImmediateMshipEnabledTime(GrouperUtil.longObjectValue(resource.getEnabledTime(), true))
              .assignImmediateMshipDisabledTime(GrouperUtil.longObjectValue(resource.getDisabledTime(), true)).save();

      Membership mship = new MembershipFinder().addGroup(group).addSubject(subject).findMembership(true);

      return Response
              .status(Response.Status.CREATED)
              .entity(TierScimUtil.convertGrouperMembershipToScimResource(mship))
              .build();

    } catch (GroupNotFoundException | SubjectNotFoundException | IllegalArgumentException e) {
      return TierScimUtil.errorResponse(Response.Status.BAD_REQUEST, "Memberships.create", e.toString());
    } catch (Exception e) {
      return TierScimUtil.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Memberships.create", e.toString());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  @PUT
  @Path("/{id}")
  @Consumes({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  @Produces({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  public Response update(
          @PathParam("id") String id,
          TierMembershipResource resource,
          @Context final UriInfo uriInfo) {

    GrouperSession grouperSession = null;

    try {
      final Subject loggedInSubject = GrouperServiceJ2ee.retrieveSubjectLoggedIn();

      grouperSession = GrouperSession.start(loggedInSubject);

      Timestamp enabledTime = null;
      Timestamp disabledTime = null;

      Membership mship = MembershipFinder.findByUuid(grouperSession, id, true, false);
      enabledTime = mship.getEnabledTime();
      disabledTime = mship.getDisabledTime();

      enabledTime = Timestamp.valueOf(resource.getEnabledTime());
      disabledTime = Timestamp.valueOf(resource.getDisabledTime());

      mship.setEnabledTime(Timestamp.valueOf(resource.getEnabledTime()));
      mship.setDisabledTime(Timestamp.valueOf(resource.getDisabledTime()));
      mship.update();

      return Response
              .status(Response.Status.OK)
              .entity(TierScimUtil.convertGrouperMembershipToScimResource(mship))
              .build();

    } catch (MembershipNotFoundException e) {
      return TierScimUtil.errorResponse(Response.Status.NOT_FOUND, "Memberships.update", e.toString());
    } catch (Exception e) {
      if (isInsufficientPrivilege(e)) {
        return TierScimUtil.errorResponse(Response.Status.FORBIDDEN, "Memberships.update", e.toString());
      }
      return TierScimUtil.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Memberships.update", e.toString());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  @DELETE
  @Path("/{id}")
  @Consumes({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  @Produces({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  public Response delete(
          @PathParam("id") String id,
          @Context final UriInfo uriInfo) {

    GrouperSession grouperSession = null;

    try {
      final Subject loggedInSubject = GrouperServiceJ2ee.retrieveSubjectLoggedIn();

      grouperSession = GrouperSession.start(loggedInSubject);

      Membership mship = MembershipFinder.findByUuid(grouperSession, id, true, false);
      // Membership.delete() checks no privileges and writes no audit entry.  Delete through the
      // normal Group / Stem / AttributeDef API for the row's owner, as the logged in subject
      MembershipWriteAuthorization.deleteChecked(grouperSession, mship);

      return Response
              .status(Response.Status.NO_CONTENT)
              .build();
    } catch (GroupNotFoundException | MembershipNotFoundException e) {
      return TierScimUtil.errorResponse(Response.Status.NOT_FOUND, "Memberships.delete", e.toString());
    } catch (Exception e) {
      if (isInsufficientPrivilege(e)) {
        return TierScimUtil.errorResponse(Response.Status.FORBIDDEN, "Memberships.delete", e.toString());
      }
      return TierScimUtil.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Memberships.delete", e.toString());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * see if an exception is (or is caused by) an InsufficientPrivilegeException, so the caller
   * gets 403 and not 500.  Some Grouper APIs wrap the privilege exception in another one
   * @param throwable
   * @return true if not allowed
   */
  private static boolean isInsufficientPrivilege(Throwable throwable) {
    int depth = 0;
    while (throwable != null && depth++ < 20) {
      if (throwable instanceof InsufficientPrivilegeException) {
        return true;
      }
      throwable = throwable.getCause();
    }
    return false;
  }

  private void addFilterToFinder(MembershipFinder membershipFinder, Filter filter) {
    if (filter.getFilterType().equals(FilterType.EQUAL)) {
      if ("groupName".equals(filter.getAttributePath().toString())) {
        membershipFinder.addGroup(filter.getComparisonValue().asText());
      } else if ("subjectId".equals(filter.getAttributePath().toString())) {
        Subject s = SubjectFinder.findById(filter.getComparisonValue().asText(), true);
        membershipFinder.addSubject(s);
      } else if ("subjectIdentifier".equals(filter.getAttributePath().toString())) {
        Subject s = SubjectFinder.findByIdentifier(filter.getComparisonValue().asText(), true);
        membershipFinder.addSubject(s);
      } else {
        throw new IllegalArgumentException("Unknown search type: " + filter.getAttributePath().toString());
      }
    } else {
      // co and anything else (nested or, pr, ...) used to fall through and add no criteria
      throw new IllegalArgumentException("Only eq filters supported");
    }

  }
}
