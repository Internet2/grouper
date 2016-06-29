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
package edu.internet2.middleware.grouper.ws.scim.group;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.ws.rs.core.Response.Status;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.psu.swe.scim.server.exception.UnableToCreateResourceException;
import edu.psu.swe.scim.server.exception.UnableToDeleteResourceException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveExtensionsException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.spec.exception.InvalidExtensionException;
import edu.psu.swe.scim.spec.protocol.filter.FilterResponse;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.PageRequest;
import edu.psu.swe.scim.spec.protocol.search.SortRequest;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimGroup;

@Named
@ApplicationScoped
public class GrouperGroupService implements Provider<ScimGroup> {

  @Override
  public ScimGroup create(ScimGroup scimGroup) throws UnableToCreateResourceException {
  
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      GroupSave groupSave = new GroupSave(grouperSession);
      Group group = groupSave.assignName(scimGroup.getId())
          .assignDisplayName(scimGroup.getDisplayName())
          .assignCreateParentStemsIfNotExist(true)
          .save();
            
      Group savedGroup = GroupFinder.findByName(grouperSession, group.getName(), true);
      
      ScimGroup scmGroup = new ScimGroup();
      
      scmGroup.setId(savedGroup.getName());
      scmGroup.setDisplayName(savedGroup.getDisplayName());
      scmGroup.setExternalId(savedGroup.getUuid());
      return scmGroup;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  @Override
  public ScimGroup update(String name, ScimGroup scimGroup)
      throws UnableToUpdateResourceException {
    
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      Group grp = GroupFinder.findByName(grouperSession, name, false);
      
      if (grp == null) {
        throw new UnableToUpdateResourceException(Status.NOT_FOUND, "Resource with id " + scimGroup.getId() + " not found");
      }
      
      grp.setDisplayName(scimGroup.getDisplayName());
      
      Group savedGroup = GroupFinder.findByName(grouperSession, grp.getName(), true);
      
      ScimGroup scmGroup = new ScimGroup();
      scmGroup.setId(savedGroup.getName());
      scmGroup.setDisplayName(savedGroup.getDisplayName());
      scmGroup.setExternalId(savedGroup.getUuid());
      return scmGroup;
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  @Override
  public ScimGroup get(String name) throws UnableToRetrieveResourceException {
    
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      Group grp = GroupFinder.findByName(grouperSession, name, false);
      if (grp == null) {
        throw new UnableToRetrieveResourceException(Status.NOT_FOUND, "Group "+name+" doesn't exist");
      }
      ScimGroup group = new ScimGroup();
      group.setId(grp.getName());
      group.setDisplayName(grp.getDisplayName());
      group.setExternalId(grp.getUuid());
      
      GroupExtension groupExtension = new GroupExtension();
      groupExtension.setDescription(grp.getDescription());
      
      try {
        group.addExtension(groupExtension);
      } catch (InvalidExtensionException e) {
        e.printStackTrace();
      }
      
      return group;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  @Override
  public FilterResponse<ScimGroup> find(Filter filter, PageRequest pageRequest,
      SortRequest sortRequest) throws UnableToRetrieveResourceException {
    
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void delete(String name) throws UnableToDeleteResourceException {
   
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      Group grp = GroupFinder.findByName(grouperSession, name, false);
      if (grp == null) {
        throw new UnableToDeleteResourceException(Status.NOT_FOUND, "Group "+name+" doesn't exist");
      }
      grp.delete();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  @SuppressWarnings("unchecked")
  public List<Class<? extends ScimExtension>> getExtensionList() throws UnableToRetrieveExtensionsException {
    List list =  Arrays.asList(GroupExtension.class);
    return list;
  }

}
