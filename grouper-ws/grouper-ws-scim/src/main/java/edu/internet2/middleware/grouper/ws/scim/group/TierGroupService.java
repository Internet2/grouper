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

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GroupAddAlreadyExistsException;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.ws.scim.TierMetaExtension;
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
public class TierGroupService implements Provider<ScimGroup> {
  
  private static final Log LOG = LogFactory.getLog(TierGroupService.class);

  @Override
  public ScimGroup create(ScimGroup scimGroup) throws UnableToCreateResourceException {
  
    GrouperSession grouperSession = null;
    ScimGroup scimGroupOutput = null;
    String groupName = null;
    try {
      TierGroupExtension tierGroupExtension = scimGroup.getExtension(TierGroupExtension.class);
      groupName = tierGroupExtension != null && tierGroupExtension.getSystemName() != null ? tierGroupExtension.getSystemName() : scimGroup.getDisplayName();
      if (groupName == null || !groupName.contains(":")) {
        throw new UnableToCreateResourceException(Status.BAD_REQUEST, "name must contain atleast one colon (:)"); 
      }
      grouperSession = GrouperSession.startRootSession();
      GroupSave groupSave = new GroupSave(grouperSession)
          .assignName(groupName)
          .assignDisplayName(scimGroup.getDisplayName())
          .assignCreateParentStemsIfNotExist(true)
          .assignSaveMode(SaveMode.INSERT);
      if (tierGroupExtension != null) {
        groupSave.assignDescription(scimGroup.getExtension(TierGroupExtension.class).getDescription());
        groupSave.assignIdIndex(scimGroup.getExtension(TierGroupExtension.class).getIdIndex());
      }

      Group savedGroup = groupSave.save();
      
      scimGroupOutput = new ScimGroup();
      scimGroupOutput.setId(savedGroup.getId());
      scimGroupOutput.setDisplayName(savedGroup.getDisplayName());
      if (tierGroupExtension != null) {
        TierGroupExtension groupExtension = new TierGroupExtension();
        groupExtension.setDescription(savedGroup.getDescription());
        groupExtension.setIdIndex(savedGroup.getIdIndex());
        groupExtension.setSystemName(tierGroupExtension.getSystemName());
        scimGroupOutput.addExtension(groupExtension);
      }
      
      TierMetaExtension tierMetaExtension = new TierMetaExtension();
      tierMetaExtension.setResultCode("SUCCESS_CREATED");
      scimGroupOutput.addExtension(tierMetaExtension);
      
    } catch(IllegalArgumentException e) {
      throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Please check the request payload and try again.");
    } catch(GroupAddAlreadyExistsException e) {
      throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Group with name "+groupName+" already exists.");
    } catch(InvalidExtensionException ie) {
      LOG.error("Unable to create group with name "+groupName, ie);
      throw new UnableToCreateResourceException(Status.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    return scimGroupOutput;
  }

  @Override
  public ScimGroup update(String id, ScimGroup scimGroup) throws UnableToUpdateResourceException {
    
    GrouperSession grouperSession = null;
    ScimGroup scimGroupOutput = null;
    String groupName = null;
    try {
      TierGroupExtension tierGroupExtension = scimGroup.getExtension(TierGroupExtension.class);
      groupName = tierGroupExtension != null && tierGroupExtension.getSystemName() != null ? tierGroupExtension.getSystemName() : scimGroup.getDisplayName();
      if (groupName == null || !groupName.contains(":")) {
        throw new UnableToUpdateResourceException(Status.BAD_REQUEST, "name must contain atleast one colon (:)"); 
      }
      grouperSession = GrouperSession.startRootSession();
      Group group = null;
      
      if (id.startsWith("systemName:")) {
        group = GroupFinder.findByName(grouperSession, id.substring(11), false);
      }
      
      if (id.startsWith("idIndex:")) {
        if (NumberUtils.isNumber(id.substring(8))) {
          group = GroupFinder.findByIdIndexSecure(Long.valueOf(id.substring(8)), false, null);
        } else {
          throw new UnableToUpdateResourceException(Status.BAD_REQUEST, "idIndex can only be  numeric");
        }
      }
      
      if (!id.startsWith("systemName:") && !id.startsWith("idIndex:")) {
        group = GroupFinder.findByUuid(grouperSession, id, false);
      }
      if (group == null) {
        throw new UnableToUpdateResourceException(Status.NOT_FOUND, "group " + id + " not found.");
      }
      
      GroupSave groupSave = new GroupSave(grouperSession)
          .assignName(groupName)
          .assignUuid(group.getUuid())
          .assignCreateParentStemsIfNotExist(true)
          .assignSaveMode(SaveMode.UPDATE);
      if (tierGroupExtension != null) {
        groupSave.assignDescription(scimGroup.getExtension(TierGroupExtension.class).getDescription());
      }
      
      Group savedGroup = groupSave.save();
      
      scimGroupOutput = new ScimGroup();
      scimGroupOutput.setId(savedGroup.getId());
      scimGroupOutput.setDisplayName(savedGroup.getDisplayName());
      if (tierGroupExtension != null) {
        TierGroupExtension groupExtension = new TierGroupExtension();
        groupExtension.setDescription(savedGroup.getDescription());
        groupExtension.setIdIndex(savedGroup.getIdIndex());
        groupExtension.setSystemName(tierGroupExtension.getSystemName());
        scimGroupOutput.addExtension(groupExtension);
      }
      
      TierMetaExtension tierMetaExtension = new TierMetaExtension();
      tierMetaExtension.setResultCode("SUCCESS_UPDATED");
      scimGroupOutput.addExtension(tierMetaExtension);
      
    } catch(IllegalArgumentException e) {
      throw new UnableToUpdateResourceException(Status.BAD_REQUEST, "Please check the request payload and try again.");
    } catch(InvalidExtensionException ie) {
      LOG.error("Unable to create group with id "+scimGroup.getId(), ie);
      throw new UnableToUpdateResourceException(Status.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.");
    } 
    finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    return scimGroupOutput;
    
  }

  @Override
  public ScimGroup get(String id) throws UnableToRetrieveResourceException {
    
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      Group group = null;
      
      if (id.startsWith("systemName:")) {
        group = GroupFinder.findByName(grouperSession, id.substring(11), false);
      }
      
      if (id.startsWith("idIndex:")) {
        if (NumberUtils.isNumber(id.substring(8))) {
          group = GroupFinder.findByIdIndexSecure(Long.valueOf(id.substring(8)), false, null);
        } else {
          throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "idIndex can only be  numeric");
        }
      }
      
      if (!id.startsWith("systemName:") && !id.startsWith("idIndex:")) {
        group = GroupFinder.findByUuid(grouperSession, id, false);
      }
      
      if (group == null) {
        throw new UnableToRetrieveResourceException(Status.NOT_FOUND, "group " + id + " not found.");
      }
      
      ScimGroup scimGroupOutput = new ScimGroup();
      scimGroupOutput.setId(group.getId());
      scimGroupOutput.setDisplayName(group.getDisplayName());
      TierGroupExtension groupExtension = new TierGroupExtension();
      groupExtension.setDescription(group.getDescription());
      groupExtension.setIdIndex(group.getIdIndex());
      groupExtension.setSystemName(group.getName());
      scimGroupOutput.addExtension(groupExtension);
      
      TierMetaExtension tierMetaExtension = new TierMetaExtension();
      tierMetaExtension.setResultCode("SUCCESS");
      scimGroupOutput.addExtension(tierMetaExtension);    
      return scimGroupOutput;
    } catch(InvalidExtensionException ie) {
      LOG.error("Unable to get a group "+ id, ie);
      throw new UnableToRetrieveResourceException(Status.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  @Override
  public FilterResponse<ScimGroup> find(Filter filter, PageRequest pageRequest,
      SortRequest sortRequest) throws UnableToRetrieveResourceException {
    
    return null;
  }

  @Override
  public void delete(String id) throws UnableToDeleteResourceException {
   
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      Group group = null;
      
      if (id.startsWith("systemName:")) {
        group = GroupFinder.findByName(grouperSession, id.substring(11), false);
      }
      
      if (id.startsWith("idIndex:")) {
        if (NumberUtils.isNumber(id.substring(8))) {
          group = GroupFinder.findByIdIndexSecure(Long.valueOf(id.substring(8)), false, null);
        } else {
          throw new UnableToDeleteResourceException(Status.BAD_REQUEST, "idIndex can only be  numeric");
        }
      }
      
      if (!id.startsWith("systemName:") && !id.startsWith("idIndex:")) {
        group = GroupFinder.findByUuid(grouperSession, id, false);
      }
      if (group == null) {
        throw new UnableToDeleteResourceException(Status.NOT_FOUND, "group " + id + " not found.");
      }
      group.delete();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  @SuppressWarnings("unchecked")
  public List<Class<? extends ScimExtension>> getExtensionList() throws UnableToRetrieveExtensionsException {
    return Arrays.asList(TierGroupExtension.class, TierMetaExtension.class);
  }
  
}
