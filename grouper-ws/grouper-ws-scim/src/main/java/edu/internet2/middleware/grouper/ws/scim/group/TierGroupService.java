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

import static edu.internet2.middleware.grouper.membership.MembershipType.IMMEDIATE;
import static edu.psu.swe.scim.spec.schema.ResourceReference.ReferenceType.DIRECT;
import static edu.psu.swe.scim.spec.schema.ResourceReference.ReferenceType.INDIRECT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.exception.GroupAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.ws.scim.TierFilter;
import edu.internet2.middleware.grouper.ws.scim.TierMetaExtension;
import edu.internet2.middleware.subject.Subject;
import edu.psu.swe.scim.server.exception.UnableToCreateResourceException;
import edu.psu.swe.scim.server.exception.UnableToDeleteResourceException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveExtensionsException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.spec.exception.InvalidExtensionException;
import edu.psu.swe.scim.spec.protocol.filter.AttributeComparisonExpression;
import edu.psu.swe.scim.spec.protocol.filter.CompareOperator;
import edu.psu.swe.scim.spec.protocol.filter.FilterExpression;
import edu.psu.swe.scim.spec.protocol.filter.FilterResponse;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.PageRequest;
import edu.psu.swe.scim.spec.protocol.search.SortRequest;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimGroup;
import edu.psu.swe.scim.spec.schema.ResourceReference;

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
      Subject subject = TierFilter.retrieveSubjectFromRemoteUser();
      grouperSession = GrouperSession.start(subject);
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
      
      scimGroupOutput = convertGrouperGroupToScimGroup(savedGroup);
      
      TierMetaExtension tierMetaExtension = new TierMetaExtension();
      tierMetaExtension.setResultCode("SUCCESS_CREATED");
      scimGroupOutput.addExtension(tierMetaExtension);
      
    } catch(IllegalArgumentException e) {
      throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Please check the request payload and try again.");
    } catch(InsufficientPrivilegeException e) {
      throw new UnableToCreateResourceException(Status.FORBIDDEN, "User doesn't have sufficient priviliges");
    } 
    catch(GroupAddAlreadyExistsException e) {
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
      Subject subject = TierFilter.retrieveSubjectFromRemoteUser();
      grouperSession = GrouperSession.start(subject);
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
      scimGroupOutput = convertGrouperGroupToScimGroup(savedGroup);
      
      TierMetaExtension tierMetaExtension = new TierMetaExtension();
      tierMetaExtension.setResultCode("SUCCESS_UPDATED");
      scimGroupOutput.addExtension(tierMetaExtension);
      
    } catch(IllegalArgumentException e) {
      throw new UnableToUpdateResourceException(Status.BAD_REQUEST, "Please check the request payload and try again.");
    } catch(InsufficientPrivilegeException e) {
      throw new UnableToUpdateResourceException(Status.FORBIDDEN, "User doesn't have sufficient priviliges");
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
      Subject subject = TierFilter.retrieveSubjectFromRemoteUser();
      grouperSession = GrouperSession.start(subject);
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
      
      ScimGroup scimGroupOutput = convertGrouperGroupToScimGroup(group);
      
      List<ResourceReference> resourceReferences = new ArrayList<ResourceReference>();
      for (Membership membership: group.getMemberships()) {
        Member member = membership.getMember();
        ResourceReference resourceReference = new ResourceReference();
        resourceReference.setValue(member.getId());
        resourceReference.setType(membership.getTypeEnum() == IMMEDIATE ? DIRECT : INDIRECT);
        resourceReference.setRef("../Users/"+member.getSubjectId());
        resourceReferences.add(resourceReference);
      }
      scimGroupOutput.setMembers(resourceReferences);
      
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
  
  private Set<Group> findApproximateGroups(GrouperSession session, String attributeName, String attributeValue) {
    Set<Group> groups = new HashSet<>();
    Set<TypeOfGroup> typeOfGroups = new HashSet<>();
    typeOfGroups.add(TypeOfGroup.group);
    if (attributeName.equalsIgnoreCase("displayName")) {
      groups = GroupFinder.findByApproximateDisplayNameSecure(attributeValue, new QueryOptions(), typeOfGroups);
    } else if (attributeName.equalsIgnoreCase("extension")) {
      groups = GroupFinder.findByApproximateExtensionSecure(attributeValue, new QueryOptions(), typeOfGroups);
    } else if (attributeName.equalsIgnoreCase("displayExtension")) {
      groups = GroupFinder.findByApproximateDisplayExtensionSecure(attributeValue, new QueryOptions(), typeOfGroups);
    } else if (attributeName.equalsIgnoreCase("description")) {
      groups = GroupFinder.findByApproximateDescriptionSecure(attributeValue, new QueryOptions(), typeOfGroups);
    } else {
      throw new IllegalArgumentException("Invalid attribute name. Only displayName, extension, displayExtension and description are allowed.");
    }
    return groups;
  }
  
  
  private Set<Group> findExactGroups(GrouperSession session, String attributeName, String attributeValue) {
    Set<Group> groups = new HashSet<>();
    Set<TypeOfGroup> typeOfGroups = new HashSet<>();
    typeOfGroups.add(TypeOfGroup.group);
    if (attributeName.equalsIgnoreCase("name")) {
      Group group = GroupFinder.findByName(session, attributeValue, false);
      if (group!= null && group.getTypeOfGroup() == TypeOfGroup.group) {
        groups.add(group);
      }
    } else if (attributeName.equalsIgnoreCase("displayName")) {
      groups = GroupFinder.findByDisplayNameSecure(attributeValue, new QueryOptions(), typeOfGroups);
    } else if (attributeName.equalsIgnoreCase("extension")) {
      groups = GroupFinder.findByExtensionSecure(attributeValue, new QueryOptions(), typeOfGroups);
    } else if (attributeName.equalsIgnoreCase("displayExtension")) {
      groups = GroupFinder.findByDisplayExtensionSecure(attributeValue, new QueryOptions(), typeOfGroups);
    } else if (attributeName.equalsIgnoreCase("uuid")) {
      Group group = GroupFinder.findByUuid(session, attributeValue, false);
      if (group!= null && group.getTypeOfGroup() == TypeOfGroup.group) {
        groups.add(group);
      }
    } else if (attributeName.equalsIgnoreCase("idIndex")) {
      
      if (NumberUtils.isNumber(attributeValue)) {
        Group group = GroupFinder.findByIdIndexSecure(Long.valueOf(attributeValue), false, new QueryOptions());
        if (group != null && group.getTypeOfGroup() == TypeOfGroup.group) {
          groups.add(group);
        }
      } else {
        throw new IllegalArgumentException("idIndex can only be a numeric value.");
      }
      
    } else if (attributeName.equalsIgnoreCase("description")) {
      groups = GroupFinder.findByDescriptionSecure(attributeValue, new QueryOptions(), typeOfGroups);
    } else {
      throw new IllegalArgumentException("Invalid attribute name. Only name, displayName, extension, displayExtension, uuid, idIndex and description are allowed.");
    }
    return groups;
  }

  @Override
  public FilterResponse<ScimGroup> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) throws UnableToRetrieveResourceException {
    
    GrouperSession grouperSession = null;
    FilterResponse<ScimGroup> response = new FilterResponse<>();
    List<ScimGroup> scimGroupList = null;
    try {
      Subject subject = TierFilter.retrieveSubjectFromRemoteUser();
      grouperSession = GrouperSession.start(subject);
      if (filter == null) {
        Set<Group> groups = new GroupFinder().findGroups();
        scimGroupList = groups.stream()
            .map(group -> convertGrouperGroupToScimGroup(group))
            .collect(Collectors.toList());
      } else {
        FilterExpression filterExpression = filter.getExpression();
        if (filterExpression instanceof AttributeComparisonExpression) {
          AttributeComparisonExpression ace = (AttributeComparisonExpression) filterExpression;
          String attributeName = ace.getAttributePath().getFullAttributeName();
          
          CompareOperator operation = ace.getOperation();
          if (operation == CompareOperator.EQ) {
            scimGroupList = findExactGroups(grouperSession, attributeName, ace.getCompareValue().toString())
                .stream()
                .map(group -> convertGrouperGroupToScimGroup(group))
                .collect(Collectors.toList());
          } else if (operation == CompareOperator.CO) {
            
            scimGroupList = findApproximateGroups(grouperSession, attributeName, ace.getCompareValue().toString())
                .stream()
                .map(group -> convertGrouperGroupToScimGroup(group))
                .collect(Collectors.toList());
                        
          } else {
            throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "only eq and co comparison operators are allowed without grouping.");
          }
        } else {
          throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "only eq and co comparison operators are allowed without grouping.");
        }
      }
      
      response.setResources(scimGroupList);
      PageRequest pr = new PageRequest();
      pr.setCount(scimGroupList.size());
      pr.setStartIndex(0);
      response.setPageRequest(pr);
      response.setTotalResults(scimGroupList.size());
    } catch(IllegalArgumentException e) {
      throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, e.getMessage());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    return response;
  }

  @Override
  public void delete(String id) throws UnableToDeleteResourceException {
   
    GrouperSession grouperSession = null;
    try {
      Subject subject = TierFilter.retrieveSubjectFromRemoteUser();
      grouperSession = GrouperSession.start(subject);
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
    } catch(InsufficientPrivilegeException e) {
      throw new UnableToDeleteResourceException(Status.FORBIDDEN, "User doesn't have sufficient priviliges");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  public List<Class<? extends ScimExtension>> getExtensionList() throws UnableToRetrieveExtensionsException {
    return Arrays.asList(TierGroupExtension.class, TierMetaExtension.class);
  }
  
  private ScimGroup convertGrouperGroupToScimGroup(Group group) {
    ScimGroup scimGroupOutput = null;
    try {
      scimGroupOutput = new ScimGroup();
      scimGroupOutput.setId(group.getId());
      scimGroupOutput.setDisplayName(group.getDisplayName());
      TierGroupExtension groupExtension = new TierGroupExtension();
      groupExtension.setDescription(group.getDescription());
      groupExtension.setIdIndex(group.getIdIndex());
      groupExtension.setSystemName(group.getName());
      scimGroupOutput.addExtension(groupExtension);
      return scimGroupOutput;
    } catch(InvalidExtensionException e) {
      throw new RuntimeException("Invalid Extension");
    }
  }
  
}
