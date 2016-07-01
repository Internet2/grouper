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
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
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
  
  private static final Log LOG = LogFactory.getLog(GrouperGroupService.class);

  @Override
  public ScimGroup create(ScimGroup scimGroup) throws UnableToCreateResourceException {
  
    GrouperSession grouperSession = null;
    ScimGroup scimGroupOutput = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      GroupSave groupSave = new GroupSave(grouperSession);
      Group group = groupSave.assignName(scimGroup.getId())
          .assignDisplayName(scimGroup.getDisplayName())
          .assignCreateParentStemsIfNotExist(true)
          .assignSaveMode(SaveMode.INSERT)
          .assignTypeOfGroup(scimGroup.getExtension(GroupExtension.class).getTypeOfGroup() == null ? null: 
            TypeOfGroup.valueOf(scimGroup.getExtension(GroupExtension.class).getTypeOfGroup()))
          .assignDescription(scimGroup.getExtension(GroupExtension.class).getDescription())
          .assignPrivAllAttrRead(scimGroup.getExtension(GroupExtension.class).getAssignAttributeReadPrivToAll())
          .assignPrivAllOptin(scimGroup.getExtension(GroupExtension.class).getAssignOptInPrivToAll())
          .assignPrivAllOptout(scimGroup.getExtension(GroupExtension.class).getAssignOptOutPrivToAll())
          .assignPrivAllRead(scimGroup.getExtension(GroupExtension.class).getAssignReadPrivToAll())
          .assignPrivAllView(scimGroup.getExtension(GroupExtension.class).getAssignViewPrivToAll())
          .save();
            
      Group savedGroup = GroupFinder.findByName(grouperSession, group.getName(), true);
      
      scimGroupOutput = new ScimGroup();
      
      scimGroupOutput.setId(savedGroup.getName());
      scimGroupOutput.setDisplayName(savedGroup.getDisplayName());
      scimGroupOutput.setExternalId(savedGroup.getUuid());
      GroupExtension groupExtension = new GroupExtension();
      groupExtension.setDescription(savedGroup.getDescription());
      groupExtension.setTypeOfGroup(savedGroup.getTypeOfGroup().getName());
      Set<AccessPrivilege> privs = savedGroup.getPrivs(SubjectFinder.findAllSubject());
      Set<String> privNames = new HashSet<String>();
      for (AccessPrivilege accessPriv: privs) {
        privNames.add(accessPriv.getName());
      }
      
      groupExtension.setAssignAttributeReadPrivToAll(privNames.contains(AccessPrivilege.GROUP_ATTR_READ.getName()));
      groupExtension.setAssignOptInPrivToAll(privNames.contains(AccessPrivilege.OPTIN.getName()));
      groupExtension.setAssignOptOutPrivToAll(privNames.contains(AccessPrivilege.OPTOUT.getName()));
      groupExtension.setAssignReadPrivToAll(privNames.contains(AccessPrivilege.READ.getName()));
      groupExtension.setAssignViewPrivToAll(privNames.contains(AccessPrivilege.VIEW.getName()));
      scimGroupOutput.addExtension(groupExtension);
      
    } catch(IllegalArgumentException e) {
      throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Please check the request payload and try again.");
    } catch(GroupAddAlreadyExistsException e) {
      throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Group with name "+scimGroup.getId()+" already exists.");
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
          .assignDescription(scimGroup.getExtension(GroupExtension.class).getDescription())
          .assignPrivAllAttrRead(scimGroup.getExtension(GroupExtension.class).getAssignAttributeReadPrivToAll())
          .assignPrivAllOptin(scimGroup.getExtension(GroupExtension.class).getAssignOptInPrivToAll())
          .assignPrivAllOptout(scimGroup.getExtension(GroupExtension.class).getAssignOptOutPrivToAll())
          .assignPrivAllRead(scimGroup.getExtension(GroupExtension.class).getAssignReadPrivToAll())
          .assignPrivAllView(scimGroup.getExtension(GroupExtension.class).getAssignViewPrivToAll());
      
      if (scimGroup.getExtension(GroupExtension.class).getTypeOfGroup() != null) {
        groupSave.assignTypeOfGroup(TypeOfGroup.valueOf(scimGroup.getExtension(GroupExtension.class).getTypeOfGroup()));
      }
      Group savedGroup = groupSave.save();
      
      scimGroupOutput = new ScimGroup();
      scimGroupOutput.setId(savedGroup.getName());
      scimGroupOutput.setDisplayName(savedGroup.getDisplayName());
      scimGroupOutput.setExternalId(savedGroup.getUuid());
      GroupExtension groupExtension = new GroupExtension();
      groupExtension.setDescription(savedGroup.getDescription());
      groupExtension.setTypeOfGroup(savedGroup.getTypeOfGroup().getName());
      Set<AccessPrivilege> privs = savedGroup.getPrivs(SubjectFinder.findAllSubject());
      Set<String> privNames = new HashSet<String>();
      for (AccessPrivilege accessPriv: privs) {
        privNames.add(accessPriv.getName());
      }
      
      groupExtension.setAssignAttributeReadPrivToAll(privNames.contains(AccessPrivilege.GROUP_ATTR_READ.getName()));
      groupExtension.setAssignOptInPrivToAll(privNames.contains(AccessPrivilege.OPTIN.getName()));
      groupExtension.setAssignOptOutPrivToAll(privNames.contains(AccessPrivilege.OPTOUT.getName()));
      groupExtension.setAssignReadPrivToAll(privNames.contains(AccessPrivilege.READ.getName()));
      groupExtension.setAssignViewPrivToAll(privNames.contains(AccessPrivilege.VIEW.getName()));
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
      
      GroupExtension groupExtension = new GroupExtension();
      groupExtension.setDescription(grp.getDescription());
      groupExtension.setTypeOfGroup(grp.getTypeOfGroup().getName());
      Set<AccessPrivilege> privs = grp.getPrivs(SubjectFinder.findAllSubject());
      Set<String> privNames = new HashSet<String>();
      for (AccessPrivilege accessPriv: privs) {
        privNames.add(accessPriv.getName());
      }
      
      groupExtension.setAssignAttributeReadPrivToAll(privNames.contains(AccessPrivilege.GROUP_ATTR_READ.getName()));
      groupExtension.setAssignOptInPrivToAll(privNames.contains(AccessPrivilege.OPTIN.getName()));
      groupExtension.setAssignOptOutPrivToAll(privNames.contains(AccessPrivilege.OPTOUT.getName()));
      groupExtension.setAssignReadPrivToAll(privNames.contains(AccessPrivilege.READ.getName()));
      groupExtension.setAssignViewPrivToAll(privNames.contains(AccessPrivilege.VIEW.getName()));
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
    List list =  Arrays.asList(GroupExtension.class);
    return list;
  }
  
}
