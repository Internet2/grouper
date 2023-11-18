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

import com.unboundid.scim2.common.types.GroupResource;
import com.unboundid.scim2.common.types.Meta;
import com.unboundid.scim2.common.types.ResourceTypeResource;
import com.unboundid.scim2.common.types.SchemaResource;
import com.unboundid.scim2.common.utils.SchemaUtils;

import java.beans.IntrospectionException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class TierGroupResourceType {
  public static ResourceTypeResource getInstance() throws IntrospectionException, URISyntaxException {

    List<ResourceTypeResource.SchemaExtension> extensions = new ArrayList<>();
    for (String ext: new TierMetaResource().getSchemaUrns()) {
      extensions.add(new ResourceTypeResource.SchemaExtension(new URI(ext), false));
    }
    for (String ext: new TierGroupResource().getSchemaUrns()) {
      extensions.add(new ResourceTypeResource.SchemaExtension(new URI(ext), false));
    }
    SchemaResource groupSchema = SchemaUtils.getSchema(GroupResource.class);
    ResourceTypeResource groupResource = new ResourceTypeResource("Group", "Group",
            "Top level SCIM Group",
            new URI("/Groups"),
            new URI(groupSchema.getId()),
            extensions);

    Meta groupMeta = new Meta();
    groupMeta.setResourceType("ResourceType");
    groupResource.setMeta(groupMeta);

    return groupResource;
  }
}
