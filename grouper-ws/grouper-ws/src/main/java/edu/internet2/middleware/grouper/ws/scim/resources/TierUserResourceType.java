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

package edu.internet2.middleware.grouper.ws.scim.resources;

import com.unboundid.scim2.common.types.Meta;
import com.unboundid.scim2.common.types.ResourceTypeResource;
import com.unboundid.scim2.common.types.SchemaResource;
import com.unboundid.scim2.common.types.UserResource;
import com.unboundid.scim2.common.utils.SchemaUtils;

import java.beans.IntrospectionException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class TierUserResourceType {
  public static ResourceTypeResource getInstance() throws IntrospectionException, URISyntaxException {
    List<ResourceTypeResource.SchemaExtension> extensions = new ArrayList<>();
    for (String ext: new TierMetaResource().getSchemaUrns()) {
      extensions.add(new ResourceTypeResource.SchemaExtension(new URI(ext), false));
    }
    SchemaResource userSchema = SchemaUtils.getSchema(UserResource.class);
    ResourceTypeResource userResource = new ResourceTypeResource("User", "User Account",
            "Top level SCIM User",
            new URI("/Users"),
            new URI(userSchema.getId()),
            extensions);

    Meta userMeta = new Meta();
    userMeta.setResourceType("ResourceType");
    userResource.setMeta(userMeta);

    return userResource;
  }
}
