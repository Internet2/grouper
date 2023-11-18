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
import com.unboundid.scim2.common.types.UserResource;
import com.unboundid.scim2.server.annotations.ResourceType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;
import edu.internet2.middleware.grouper.ws.scim.TierScimUtil;
import edu.internet2.middleware.grouper.ws.scim.resources.TierGroupResource;
import edu.internet2.middleware.grouper.ws.scim.resources.TierMetaResource;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

import static com.unboundid.scim2.common.utils.ApiConstants.MEDIA_TYPE_SCIM;
import static com.unboundid.scim2.common.utils.ApiConstants.QUERY_PARAMETER_FILTER;
import static com.unboundid.scim2.common.utils.ApiConstants.QUERY_PARAMETER_PAGE_START_INDEX;

@ResourceType(
        description = "SCIM 2.0 User",
        name = "User",
        schema = TierGroupResource.class,
        discoverable = false)
@Path("/v2/Users")
public class TierUserProvider {

  @Context
  private Application application;

  @GET
  @Path("/{id}")
  @Consumes({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  @Produces({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  public Response get(
          @PathParam("id") String id,
          @Context final UriInfo uriInfo) {
    GrouperSession grouperSession = null;

    try {
      final Subject loggedInSubject = GrouperServiceJ2ee.retrieveSubjectLoggedIn();

      grouperSession = GrouperSession.start(loggedInSubject);

      Subject foundSubject = SubjectFinder.findByIdOrIdentifier(id, true);

      UserResource userResource = TierScimUtil.convertGrouperMemberToScimUser(grouperSession, foundSubject);

      userResource.setExtension(userResource.getExtension(TierMetaResource.class).setResultCode("SUCCESS"));


      return Response
              .status(Response.Status.OK)
              .entity(userResource)
              .build();
    } catch (SubjectNotFoundException e) {
      return TierScimUtil.errorResponse(Response.Status.NOT_FOUND, "Users.get", e.toString());
    } catch (IllegalArgumentException e) {
      return TierScimUtil.errorResponse(Response.Status.BAD_REQUEST, "Users.get", e.toString());
    } catch (Exception e) {
      return TierScimUtil.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Users.get", e.getMessage());
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

    // since only exact filters are currently implemented, there should only be one result
    Subject foundSubject = null;

    try {
      if (GrouperUtil.isEmpty(filterString)) {
        throw new IllegalArgumentException("Missing filter parameter");
      }

      if (!GrouperUtil.isEmpty(startIndex)) {
        throw new NotImplementedException("startIndex not currently implemented");
      }

      grouperSession = GrouperSession.start(loggedInSubject);

      Filter filter = Filter.fromString(filterString);

      if (filter.getFilterType().equals(FilterType.EQUAL)) {
        if ("id".equals(filter.getAttributePath().toString())) {
          foundSubject = SubjectFinder.findById(filter.getComparisonValue().asText(), false);
        } else if ("identifier".equals(filter.getAttributePath().toString())) {
          foundSubject = SubjectFinder.findByIdentifier(filter.getComparisonValue().asText(), false);
        } else {
          throw new IllegalArgumentException("only id and identifier attribute names are allowed");
        }
      } else if (filter.getFilterType().equals(FilterType.CONTAINS)) {
        throw new IllegalArgumentException("Only eq filter supported");
      }

      List<UserResource> userResources = new ArrayList<>();
      ListResponse<UserResource> listResponse;

      if (foundSubject != null) {
        userResources.add(TierScimUtil.convertGrouperMemberToScimUser(grouperSession, foundSubject));

        listResponse = new ListResponse<UserResource>(
                userResources.size(),
                userResources,
                1, 1);
      } else {
        listResponse = new ListResponse<UserResource>(
                0,
                new ArrayList<UserResource>(),
                1, 0);
      }

      return Response
              .status(Response.Status.OK)
              .entity(listResponse)
              .build();

    } catch (IllegalArgumentException e) {
      return TierScimUtil.errorResponse(Response.Status.BAD_REQUEST, "Users.getWithFilter", e.toString());
    } catch (Exception e) {
      return TierScimUtil.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Users.getWithFilter", e.toString());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
}
