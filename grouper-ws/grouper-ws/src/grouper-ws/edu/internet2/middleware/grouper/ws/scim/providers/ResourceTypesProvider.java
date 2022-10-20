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

import com.unboundid.scim2.common.types.ResourceTypeResource;
import com.unboundid.scim2.server.annotations.ResourceType;
import edu.internet2.middleware.grouper.ws.scim.TierScimUtil;
import edu.internet2.middleware.grouper.ws.scim.resources.TierGroupResourceType;
import edu.internet2.middleware.grouper.ws.scim.resources.TierMembershipResourceType;
import edu.internet2.middleware.grouper.ws.scim.resources.TierUserResourceType;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

import static com.unboundid.scim2.common.utils.ApiConstants.MEDIA_TYPE_SCIM;

@ResourceType(
        description = "SCIM 2.0 ResourceTypes",
        name = "ResourceTypes",
        schema = ResourceTypeResource.class,
        discoverable = false)
@Path("/v2/ResourceTypes")
public class ResourceTypesProvider {
  @GET
  @Consumes({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  @Produces({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  public Response get(@Context final UriInfo uriInfo) {
    try {
      List<ResourceTypeResource> resourceTypes = new ArrayList<>();

      ResourceTypeResource userResource = TierUserResourceType.getInstance();
      ResourceTypeResource groupResource = TierGroupResourceType.getInstance();
      ResourceTypeResource mshipResource = TierMembershipResourceType.getInstance();

      userResource.getMeta().setLocation(uriInfo.getRequestUriBuilder().path("/User").build());
      groupResource.getMeta().setLocation(uriInfo.getRequestUriBuilder().path("/Group").build());
      mshipResource.getMeta().setLocation(uriInfo.getRequestUriBuilder().path("/Membership").build());

      resourceTypes.add(userResource);
      resourceTypes.add(groupResource);
      resourceTypes.add(mshipResource);

      return Response
              .status(Response.Status.OK)
              /* ListResponse not in spec, should just be a json array */
              //.entity(new ListResponse(resourceTypes.size(), resourceTypes, 1, resourceTypes.size()))
              .entity(resourceTypes)
              .build();

    } catch (Exception e) {
      return TierScimUtil.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "ResourceTypesProvider.get", e.getMessage());
    }
  }

  @GET
  @Path("/{resourceName}")
  @Consumes({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  @Produces({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  public Response get(@PathParam("resourceName") String resourceName, @Context final UriInfo uriInfo) {

    ResourceTypeResource resource = null;

    try {
      switch (resourceName) {
        case "User":
          resource = TierUserResourceType.getInstance();
          break;
        case "Group":
          resource = TierGroupResourceType.getInstance();
          break;
        case "Membership":
          resource = TierMembershipResourceType.getInstance();
          break;
        default:
          throw new IllegalArgumentException("Unexpected resource type: " + resourceName);
      }

      resource.getMeta().setLocation(uriInfo.getRequestUri());

      return Response
              .status(Response.Status.OK)
              .entity(resource)
              .build();
    } catch (IllegalArgumentException e) {
      return TierScimUtil.errorResponse(Response.Status.BAD_REQUEST, "ResourceTypes.getByResource", e.toString());
    } catch (Exception e) {
      return TierScimUtil.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "ResourceTypes.getByResource", e.toString());
    }
  }
}
