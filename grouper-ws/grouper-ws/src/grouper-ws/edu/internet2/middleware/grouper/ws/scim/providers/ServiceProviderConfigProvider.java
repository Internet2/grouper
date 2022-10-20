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

import com.unboundid.scim2.common.types.BulkConfig;
import com.unboundid.scim2.common.types.ChangePasswordConfig;
import com.unboundid.scim2.common.types.ETagConfig;
import com.unboundid.scim2.common.types.FilterConfig;
import com.unboundid.scim2.common.types.Meta;
import com.unboundid.scim2.common.types.PatchConfig;
import com.unboundid.scim2.common.types.ServiceProviderConfigResource;
import com.unboundid.scim2.common.types.SortConfig;
import com.unboundid.scim2.server.annotations.ResourceType;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;

import static com.unboundid.scim2.common.utils.ApiConstants.MEDIA_TYPE_SCIM;

@ResourceType(
        description = "SCIM 2.0 ServiceProviderConfig",
        name = "ServiceProviderConfig",
        schema = ServiceProviderConfigResource.class,
        discoverable = false)
@Path("/v2/ServiceProviderConfig")
public class ServiceProviderConfigProvider {

  /**
   * Location of the Grouper SCIM documentation
   */
  public static final String GROUPER_SCIM_WIKI = "https://spaces.at.internet2.edu/display/Grouper/Grouper+TIER+SCIM+server";

  @GET
  @Consumes({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  @Produces({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  public ServiceProviderConfigResource get(@Context final UriInfo uriInfo) {
    ServiceProviderConfigResource spc = new ServiceProviderConfigResource(
            GROUPER_SCIM_WIKI,
            new PatchConfig(false),
            new BulkConfig(false, 0, 0),
            new FilterConfig(true, TierMembershipProvider.MAX_RESULTS_PER_PAGE),
            new ChangePasswordConfig(false),
            new SortConfig(false),
            new ETagConfig(true),
            //Collections.singletonList(
            //        new AuthenticationScheme(
            //                "Basic", "HTTP BASIC", null, null, "httpbasic", true))
            Collections.emptyList()
    );
    spc.setId("spc");
    spc.setExternalId("spc");
    Meta meta = new Meta();
    meta.setLocation(uriInfo.getRequestUri());
    spc.setMeta(meta);

    return spc;
  }

}
