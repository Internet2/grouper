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
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;
import edu.internet2.middleware.grouper.ws.scim.TierScimUtil;
import edu.internet2.middleware.grouper.ws.scim.resources.TierGroupResource;
import edu.internet2.middleware.grouper.ws.scim.resources.TierMetaResource;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.unboundid.scim2.common.utils.ApiConstants.MEDIA_TYPE_SCIM;
import static com.unboundid.scim2.common.utils.ApiConstants.QUERY_PARAMETER_FILTER;
import static com.unboundid.scim2.common.utils.ApiConstants.QUERY_PARAMETER_PAGE_START_INDEX;

@ResourceType(
        description = "SCIM 2.0 Group",
        name = "Group",
        schema = TierGroupResource.class,
        discoverable = false)
@Path("/v2/Groups")
public class TierGroupProvider {

  @Context
  private Application application;

  /**
   * Upper limit on sise of returned list responses
   */
  public static final int MAX_RESULTS_PER_PAGE = 1000;

  @GET
  @Path("/{id}")
  @Consumes({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  @Produces({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  public Response getById(
          @QueryParam(QUERY_PARAMETER_FILTER) final String filterString,
          @PathParam("id") String id,
          @Context final UriInfo uriInfo) {
    if (filterString != null) {
      return TierScimUtil.errorResponse(Response.Status.BAD_REQUEST, "Groups.get", "Filtering not allowed with id");
    }

    GrouperSession grouperSession = null;

    try {
      final Subject loggedInSubject = GrouperServiceJ2ee.retrieveSubjectLoggedIn();

      grouperSession = GrouperSession.start(loggedInSubject);


      Group foundGroup = findGroupById(id);

      GroupResource groupResource = TierScimUtil.convertGrouperGroupToScimGroup(foundGroup);

      return Response
              .status(Response.Status.OK)
              .entity(groupResource)
              .build();
    } catch (GroupNotFoundException | IllegalArgumentException e) {
      return TierScimUtil.errorResponse(Response.Status.NOT_FOUND, "Groups.get", e.toString());
    } catch (Exception e) {
      return TierScimUtil.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Groups.get", e.toString());
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

    Set<Group> foundGroups = new HashSet<>();

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
        if ("name".equals(filter.getAttributePath().toString())) {
          foundGroups = new GroupFinder().addGroupName(filter.getComparisonValue().asText()).findGroups();
        } else if ("idIndex".equals(filter.getAttributePath().toString())) {
          foundGroups = GrouperUtil.toSet(GroupFinder.findByIdIndexSecure(
                  filter.getComparisonValue().asLong(),
                  false,
                  null));
        } else if ("displayName".equals(filter.getAttributePath().toString())) {
          foundGroups = GroupFinder.findByDisplayNameSecure(
                  filter.getComparisonValue().asText(),
                  null,
                  null);
        } else if ("extension".equals(filter.getAttributePath().toString())) {
          foundGroups = GroupFinder.findByExtensionSecure(
                  filter.getComparisonValue().asText(),
                  null,
                  null);
        } else if ("displayExtension".equals(filter.getAttributePath().toString())) {
          foundGroups = GroupFinder.findByDisplayExtensionSecure(
                  filter.getComparisonValue().asText(),
                  null,
                  null);
        } else if ("uuid".equals(filter.getAttributePath().toString())) {
          foundGroups = GrouperUtil.toSet(GroupFinder.findByUuid(
                  grouperSession,
                  filter.getComparisonValue().asText(),
                  false));
        } else if ("description".equals(filter.getAttributePath().toString())) {
          foundGroups = GroupFinder.findByDescriptionSecure(
                  filter.getComparisonValue().asText(),
                  null,
                  null);
        } else {
          throw new IllegalArgumentException("Unknown search type: " + filter.getAttributePath().toString());
        }
      } else if (filter.getFilterType().equals(FilterType.CONTAINS)) {
        if ("displayName".equals(filter.getAttributePath().toString())) {
          foundGroups = GroupFinder.findByApproximateDisplayNameSecure(
                  filter.getComparisonValue().asText(),
                  null,
                  null);
        } else if ("extension".equals(filter.getAttributePath().toString())) {
          foundGroups = GroupFinder.findByApproximateExtensionSecure(
                  filter.getComparisonValue().asText(),
                  null,
                  null);
        } else if ("displayExtension".equals(filter.getAttributePath().toString())) {
          foundGroups = GroupFinder.findByApproximateDisplayExtensionSecure(
                  filter.getComparisonValue().asText(),
                  null,
                  null);
        } else if ("description".equals(filter.getAttributePath().toString())) {
          foundGroups = GroupFinder.findByApproximateDescriptionSecure(
                  filter.getComparisonValue().asText(),
                  null,
                  null);
        } else {
          throw new IllegalArgumentException("Unknown search type: " + filter.getAttributePath().toString());
        }
      } else {
        throw new IllegalArgumentException("Only eq and co filters supported");
      }

      List<GroupResource> groupResources = new ArrayList<>();
      int numAdded = 0;
      int maxItems = GrouperUtil.isEmpty(itemsPerPage) ? MAX_RESULTS_PER_PAGE : itemsPerPage;
      for (Group g: foundGroups) {
        groupResources.add(TierScimUtil.convertGrouperGroupToScimGroup(g));
        if (++numAdded > maxItems) {
          break;
        }
      }

      ListResponse<GroupResource> listResponse = new ListResponse<GroupResource>(
              groupResources.size(),
              groupResources,
              1, numAdded);

      return Response
              .status(Response.Status.OK)
              .entity(listResponse)
              .build();

    } catch (GroupNotFoundException e) {
      ListResponse<GroupResource> listResponse = new ListResponse<GroupResource>(
              0,
              new ArrayList<GroupResource>(),
              1, 0);

      return Response
              .status(Response.Status.OK)
              .entity(listResponse)
              .build();
    } catch (IllegalArgumentException e) {
      return TierScimUtil.errorResponse(Response.Status.BAD_REQUEST, "Groups.getWithFilter", e.toString());
    } catch (Exception e) {
      return TierScimUtil.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Groups.getWithFilter", e.toString());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  @POST
  @Consumes({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  @Produces({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  public Response create(
          GroupResource groupResource,
          @Context final UriInfo uriInfo) {

    return saveOrUpdateResource(null, groupResource, SaveMode.INSERT, "Groups.create");
  }

  @PUT
  @Path("/{id}")
  @Consumes({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  @Produces({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  public Response update(
          @PathParam("id") String id,
          GroupResource groupResource,
          @Context final UriInfo uriInfo) {

    return saveOrUpdateResource(id, groupResource, SaveMode.UPDATE, "Groups.update");
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

      Group foundGroup = findGroupById(id);
      foundGroup.delete();

      return Response
              .status(Response.Status.NO_CONTENT)
              .build();
    } catch (GroupNotFoundException e) {
      return TierScimUtil.errorResponse(Response.Status.NOT_FOUND, "Groups.delete", e.toString());
    } catch (Exception e) {
      return TierScimUtil.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Groups.delete", e.toString());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  private Group findGroupById(String id) throws GroupNotFoundException {
    if (id.startsWith("systemName:")) {
      return GroupFinder.findByName(id.substring(11), true);
    } else if (id.startsWith("idIndex:")) {
      return GroupFinder.findByIdIndexSecure(Long.valueOf(id.substring(8)), true, null);
    } else {
      return GroupFinder.findByUuid(id, true);
    }
  }

  /* handle both POST and PUT to avoid duplicating logic */
  private Response saveOrUpdateResource(String id, GroupResource groupResource, SaveMode saveMode, String scimType) {
    GrouperSession grouperSession = null;

    try {

      final Subject loggedInSubject = GrouperServiceJ2ee.retrieveSubjectLoggedIn();

      grouperSession = GrouperSession.start(loggedInSubject);

      String displayName = null;
      String description = null;
      Long idIndex = null;

      TierGroupResource tierGroupExtension = groupResource.getExtension(TierGroupResource.class);
      String groupName = tierGroupExtension != null && tierGroupExtension.getSystemName() != null ? tierGroupExtension.getSystemName() : groupResource.getDisplayName();

      if (saveMode == SaveMode.INSERT) {
        if (groupName == null || !groupName.contains(":")) {
          throw new IllegalArgumentException("name must contain at least one colon (:)");
        }
        if (id != null) {
          throw new IllegalArgumentException("id in path not used for POST");
        }
      } else if (saveMode == SaveMode.UPDATE) {
        if (groupName != null) {
          throw new NotImplementedException("Group name changes not yet implemented");
        }

        // make sure the group is resolvable by id, otherwise return 404
        GroupFinder.findByUuid(id, true);
      } else {
        throw new RuntimeException("Unknown SaveMode: " + saveMode.name());
      }

      if (groupResource.getDisplayName() != null) {
        displayName = groupResource.getDisplayName();
      }
      if (tierGroupExtension != null) {
        if (tierGroupExtension.getDescription() != null) {
          description = tierGroupExtension.getDescription();
        }
        if (tierGroupExtension.getIdIndex() != null) {
          idIndex = tierGroupExtension.getIdIndex();
        }
      }

      // get any members to initialize
      Collection<Subject> members = new HashSet<>();
      if (groupResource.getMembers() != null) {
        for (com.unboundid.scim2.common.types.Member m: groupResource.getMembers()) {
          Subject s = SubjectFinder.findById(m.getValue(), true);
          members.add(s);
        }
      }
      Group grouperGroup = saveOrUpdateGroup(grouperSession, id, groupName, displayName, description, idIndex, saveMode, members);

      GroupResource createdGroupResource = TierScimUtil.convertGrouperGroupToScimGroup(grouperGroup);

      /* Fix the result status to that used by the previous SCIM implementation (Note: extensions can't be updated in
         place; updating creates a copy that then can be set as the extension */
      createdGroupResource.setExtension(createdGroupResource.getExtension(TierMetaResource.class).setResultCode(
              saveMode == SaveMode.INSERT ? "SUCCESS_CREATED" : "SUCCESS_UPDATED"
      ));

      return Response
              .status(saveMode == SaveMode.INSERT ? Response.Status.CREATED : Response.Status.OK)
              .entity(createdGroupResource)
              .build();

    } catch (GroupNotFoundException e) {
      return TierScimUtil.errorResponse(Response.Status.NOT_FOUND, scimType, e.toString());
    } catch (IllegalArgumentException e) {
      return TierScimUtil.errorResponse(Response.Status.BAD_REQUEST, scimType, e.toString());
    } catch (Exception e) {
      return TierScimUtil.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, scimType, e.toString());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  /* Split the save into a separate method so it can be replaced by Mockito */
  protected Group saveOrUpdateGroup(GrouperSession grouperSession, String uuid, String groupName, String displayName,
                                    String description, Long idIndex, SaveMode saveMode, Collection<Subject> members) {
    GroupSave save = new GroupSave(grouperSession).assignSaveMode(saveMode).assignUuid(uuid);
    if (groupName != null) {
      save.assignName(groupName);
      save.assignCreateParentStemsIfNotExist(true);
    }
    if (displayName != null) {
      save.assignDisplayName(displayName);
    }
    if (description != null) {
      save.assignDescription(description);
    }
    if (idIndex != null) {
      save.assignIdIndex(idIndex);
    }

    Group group = save.save();

    group.replaceMembers(members);
    return group;
  }
}
