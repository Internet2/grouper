/**
 * Copyright 2016 Internet2
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
 */
package edu.internet2.middleware.grouper.ws.scim;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.psu.swe.scim.server.exception.UnableToCreateResourceException;
import edu.psu.swe.scim.server.exception.UnableToDeleteResourceException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveExtensionsException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.PageRequest;
import edu.psu.swe.scim.spec.protocol.search.SortRequest;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimGroup;
import edu.psu.swe.scim.spec.schema.Meta;
import edu.psu.swe.scim.spec.schema.ResourceReference;

@Named
@ApplicationScoped
public class GrouperGroupService implements Provider<ScimGroup> {

  @Override
  public ScimGroup create(ScimGroup resource) throws UnableToCreateResourceException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ScimGroup update(String id, ScimGroup resource)
      throws UnableToUpdateResourceException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ScimGroup get(String id) throws UnableToRetrieveResourceException {
    
    Group grp = GroupFinder.findByName(GrouperSession.startRootSession(), id, false);
    ScimGroup group = new ScimGroup();
    
    group.setId(grp.getId());
    group.setDisplayName(grp.getDisplayName());
    group.setExternalId(UUID.randomUUID().toString());
    
    return group;
  }

  @Override
  public List<ScimGroup> find(Filter filter, PageRequest pageRequest,
      SortRequest sortRequest) throws UnableToRetrieveResourceException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void delete(String id) throws UnableToDeleteResourceException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<Class<? extends ScimExtension>> getExtensionList()
      throws UnableToRetrieveExtensionsException {
    // TODO Auto-generated method stub
    return null;
  }

}
