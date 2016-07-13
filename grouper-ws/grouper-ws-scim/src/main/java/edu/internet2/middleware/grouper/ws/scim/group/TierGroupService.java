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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupAddAlreadyExistsException;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
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

      Group group = groupSave.save();
      
      Group savedGroup = GroupFinder.findByName(grouperSession, group.getName(), true);
      scimGroupOutput = new ScimGroup();
      scimGroupOutput.setId(savedGroup.getId());
      scimGroupOutput.setDisplayName(savedGroup.getDisplayExtension());
      if (tierGroupExtension != null) {
        TierGroupExtension groupExtension = new TierGroupExtension();
        groupExtension.setDescription(savedGroup.getDescription());
        groupExtension.setIdIndex(savedGroup.getIdIndex());
        groupExtension.setSystemName(tierGroupExtension.getSystemName());
        scimGroupOutput.addExtension(groupExtension);
      }
      
      //Add the TierMetaExtension
      //TODO create a new filter to get the correct response duration
      //TODO change the result code to a constant or use an existing constant
      TierMetaExtension tierMetaExtension = new TierMetaExtension();
      tierMetaExtension.setResponseDurationMillis(8686L);
      tierMetaExtension.setResultCode("SUCCESS_CREATED");
      scimGroupOutput.addExtension(tierMetaExtension);
      
    } catch(IllegalArgumentException e) {
      throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Please check the request payload and try again.");
    } catch(GroupAddAlreadyExistsException e) {
      throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Group with name "+groupName+" already exists.");
    } catch(InvalidExtensionException ie) {
      LOG.error("Unable to create group with id "+scimGroup.getId(), ie);
      throw new UnableToCreateResourceException(Status.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    return scimGroupOutput;
  }

  @Override
  public ScimGroup update(String name, ScimGroup scimGroup) throws UnableToUpdateResourceException {
    
    GrouperSession grouperSession = null;
    ScimGroup scimGroupOutput = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      Group grp = GroupFinder.findByName(grouperSession, name, false);
      
      if (grp == null) {
        throw new UnableToUpdateResourceException(Status.NOT_FOUND, "group with name " + scimGroup.getId() + " not found.");
      }
      
//      GroupSave groupSave1 = new GroupSave(GROUPER_SESSION).assignUuid(group.getId())
//          .assignSaveMode(SaveMode.UPDATE)
//          .assignName(group.getParentStemName() + ":" + extension)
//          .assignDisplayExtension(displayExtension).assignDescription(description).assignTypeOfGroup(typeOfGroup)
//          .assignPrivAllAdmin(adminChecked).assignPrivAllAttrRead(attrReadChecked)
//          .assignPrivAllAttrUpdate(attrUpdateChecked).assignPrivAllOptin(optinChecked)
//          .assignPrivAllOptout(optoutChecked).assignPrivAllRead(readChecked)
//          .assignPrivAllUpdate(updateChecked).assignPrivAllView(viewChecked);
      
      GroupSave groupSave = new GroupSave(grouperSession).assignName(scimGroup.getId())
          .assignUuid(grp.getUuid())
          .assignSaveMode(SaveMode.UPDATE)
          .assignDescription(scimGroup.getExtension(TierGroupExtension.class).getDescription());
      
      Group savedGroup = groupSave.save();
      
      scimGroupOutput = new ScimGroup();
      scimGroupOutput.setId(savedGroup.getName());
      scimGroupOutput.setDisplayName(savedGroup.getDisplayName());
      scimGroupOutput.setExternalId(savedGroup.getUuid());
      TierGroupExtension groupExtension = new TierGroupExtension();
      groupExtension.setDescription(savedGroup.getDescription());
      Set<AccessPrivilege> privs = savedGroup.getPrivs(SubjectFinder.findAllSubject());
      Set<String> privNames = new HashSet<String>();
      for (AccessPrivilege accessPriv: privs) {
        privNames.add(accessPriv.getName());
      }
      
      scimGroupOutput.addExtension(groupExtension);
      
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
      
      TierGroupExtension groupExtension = new TierGroupExtension();
      groupExtension.setDescription(grp.getDescription());
      Set<AccessPrivilege> privs = grp.getPrivs(SubjectFinder.findAllSubject());
      Set<String> privNames = new HashSet<String>();
      for (AccessPrivilege accessPriv: privs) {
        privNames.add(accessPriv.getName());
      }
      
      group.addExtension(groupExtension);      
      return group;
    } catch(InvalidExtensionException ie) {
      LOG.error("Unable to get a group "+ name, ie);
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
    return Arrays.asList(TierGroupExtension.class, TierMetaExtension.class);
  }
  
}
