/**
 * 
 */
package edu.internet2.middleware.grouper.ws.scim.membership;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.ws.scim.TierFilter;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;
import edu.psu.swe.scim.server.exception.UnableToCreateResourceException;
import edu.psu.swe.scim.server.exception.UnableToDeleteResourceException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveExtensionsException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.server.provider.UpdateRequest;
import edu.psu.swe.scim.spec.protocol.filter.AttributeComparisonExpression;
import edu.psu.swe.scim.spec.protocol.filter.CompareOperator;
import edu.psu.swe.scim.spec.protocol.filter.FilterExpression;
import edu.psu.swe.scim.spec.protocol.filter.FilterResponse;
import edu.psu.swe.scim.spec.protocol.filter.LogicalExpression;
import edu.psu.swe.scim.spec.protocol.filter.LogicalOperator;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.PageRequest;
import edu.psu.swe.scim.spec.protocol.search.SortRequest;
import edu.psu.swe.scim.spec.resources.ScimExtension;

/**
 * @author vsachdeva
 *
 */
@Named
@ApplicationScoped
public class TierMembershipService implements Provider<MembershipResource> {
  
  private static final Log LOG = LogFactory.getLog(TierMembershipService.class);

  @Override
  public MembershipResource create(MembershipResource resource) throws UnableToCreateResourceException {
    
    GrouperSession grouperSession = null;
    LOG.info("Starting to create a membership: "+resource);
    try {
      Subject authenticatedSubject = TierFilter.retrieveSubjectFromRemoteUser();
      grouperSession = GrouperSession.start(authenticatedSubject);
      
      Group membershipGroup = null;
      if (resource.getOwner() == null || resource.getMember() == null) {
        throw new UnableToCreateResourceException(Status.BAD_REQUEST, "owner or member propery is missing in the request body.");
      }
      OwnerGroup ownerGroup = resource.getOwner();
      if (StringUtils.isNotBlank(ownerGroup.getValue())) {
        membershipGroup = GroupFinder.findByUuid(grouperSession, ownerGroup.getValue(), false);
        if (membershipGroup == null) {
          throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Group with uuid value "+ownerGroup.getValue()+" doesn't exist.");
        }
      }
      if (membershipGroup == null && StringUtils.isNotBlank(ownerGroup.getSystemName())) {
        membershipGroup = GroupFinder.findByName(grouperSession, ownerGroup.getSystemName(), false);
        if (membershipGroup == null) {
          throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Group with systemName "+ownerGroup.getSystemName()+" doesn't exist.");
        }
      }
      if (membershipGroup == null && ownerGroup.getIdIndex() != null) {
        membershipGroup = GroupFinder.findByIdIndexSecure(ownerGroup.getIdIndex(), false, new QueryOptions());
        if (membershipGroup == null) {
          throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Group with idIndex "+ownerGroup.getIdIndex()+" doesn't exist.");
        }
      }
      if (membershipGroup == null) {
        throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Please provide correct uuid value or system name or idIndex for the group.");
      }
      
      Subject membershipSubject = SubjectFinder.findByIdOrIdentifier(resource.getMember().getValue(), false);
      
      if (membershipSubject != null) {
        boolean didNotExistAlready = membershipGroup.addMember(membershipSubject, false);
        if (!didNotExistAlready) {
          throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Member already exists.");
        }
      } else {
        throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Group or Member with id "+resource.getMember().getValue()+" doesn't exist.");
      }
      
      Membership membership = membershipGroup.getImmediateMembership(Group.getDefaultList(), membershipSubject, true, true);
      boolean needsUpdate = false;
      if (resource.getDisabledTime() != null) {
        membership.setDisabledTime(Timestamp.valueOf(resource.getDisabledTime()));
        needsUpdate = true;
      }
      if (resource.getEnabledTime() != null) {
        membership.setEnabledTime(Timestamp.valueOf(resource.getEnabledTime()));
        needsUpdate = true;
      }
      if (needsUpdate) {
        membership.update();
      }
      return buildMembershipResourceFromMembership(membership);
    } catch(InsufficientPrivilegeException e) {
      throw new UnableToCreateResourceException(Status.FORBIDDEN, "User doesn't have sufficient priviliges.");
    }  catch(MemberAddException e) {
      throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Please fix the request and try again.");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  @Override
  public MembershipResource update(UpdateRequest<MembershipResource> updateRequest) throws UnableToUpdateResourceException {
    
    GrouperSession grouperSession = null;
    LOG.info("Starting to update a membership with id: "+updateRequest.getOriginal().getId());
    try {
      Subject authenticatedSubject = TierFilter.retrieveSubjectFromRemoteUser();
      grouperSession = GrouperSession.start(authenticatedSubject);
      MembershipResource originalResource = updateRequest.getOriginal();
      MembershipResource newResource = updateRequest.getResource();
      Membership membership = MembershipFinder.findByUuid(grouperSession, originalResource.getId(), false, false);
      if (membership == null) {
        throw new UnableToUpdateResourceException(Status.NOT_FOUND, "Membership with id "+originalResource.getId()+" doesn't exist.");
      }
      
      boolean needsUpdate = false;
      if (newResource.getDisabledTime() != null) {
        membership.setDisabledTime(Timestamp.valueOf(newResource.getDisabledTime()));
        needsUpdate = true;
      }
      if (newResource.getEnabledTime() != null) {
        membership.setEnabledTime(Timestamp.valueOf(newResource.getEnabledTime()));
        needsUpdate = true;
      }
      if (needsUpdate) {
        membership.update();
      }
      
      return buildMembershipResourceFromMembership(membership);
      
    } catch(InsufficientPrivilegeException e) {
      throw new UnableToUpdateResourceException(Status.FORBIDDEN, "User doesn't have sufficient priviliges.");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  @Override
  public MembershipResource get(String id) throws UnableToRetrieveResourceException {
    GrouperSession grouperSession = null;
    LOG.info("Starting to retrieve a membership with id: "+id);
    try {
      Subject authenticatedSubject = TierFilter.retrieveSubjectFromRemoteUser();
      grouperSession = GrouperSession.start(authenticatedSubject);
      
      Membership membership = MembershipFinder.findByUuid(grouperSession, id, false, false);
      if (membership == null) {
        throw new UnableToRetrieveResourceException(Status.NOT_FOUND, "Membership with id "+id+" doesn't exist.");
      }
      return buildMembershipResourceFromMembership(membership);
      
    } catch(InsufficientPrivilegeException e) {
      throw new UnableToRetrieveResourceException(Status.FORBIDDEN, "User doesn't have sufficient priviliges.");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  private MembershipResource buildMembershipResourceFromMembership(Membership membership) {
    
    MembershipResource membershipResource = new MembershipResource();
    membershipResource.setId(membership.getUuid());
    if (membership.isImmediate()) {
      membershipResource.setEnabled(membership.isEnabled());
      membershipResource.setDisabledTime(membership.getDisabledTime() != null ? membership.getDisabledTime().toLocalDateTime(): null);
      membershipResource.setEnabledTime(membership.getEnabledTime() != null? membership.getEnabledTime().toLocalDateTime(): null);
    } else {
      membershipResource.setEnabled(null);
      membershipResource.setDisabledTime(null);
      membershipResource.setEnabledTime(null);
    }
    membershipResource.setMembershipType(membership.isImmediate() ? "immediate":"effective");
    
    Member member = new Member();
    Subject membershipSubject = membership.getMember().getSubject();
    member.setValue(membershipSubject.getId());
    member.setDisplay(membershipSubject.getName());
    if (membershipSubject.getTypeName().equalsIgnoreCase(SubjectTypeEnum.PERSON.getName())) {
      member.setRef("../Users/"+membershipSubject.getId());
    } else {
      member.setRef("../Groups/"+membershipSubject.getId());
    }
    membershipResource.setMember(member);

    Group membershipGroup = membership.getOwnerGroup();
    OwnerGroup ownerGroupOutput = new OwnerGroup();
    ownerGroupOutput.setDisplay(membershipGroup.getDisplayName());
    ownerGroupOutput.setRef("../Groups/"+membershipGroup.getUuid());
    ownerGroupOutput.setSystemName(membershipGroup.getName());
    ownerGroupOutput.setValue(membershipGroup.getUuid());
    membershipResource.setOwner(ownerGroupOutput);
    
    return membershipResource;
  }
  
  private void buildMembershipFinder(String attribute, String attributeValue, MembershipFinder finder, GrouperSession session) throws IllegalArgumentException {
    String attributeName = attribute.toLowerCase();
    if (attributeName.equals("groupid")) {
      Group group = GroupFinder.findByUuid(session, attributeValue, false);
      if (group == null) {
        throw new IllegalArgumentException("Group with id/uuid "+attributeValue+" cannot be found.");
      }
      finder.addGroup(group);      
    } else if (attributeName.equals("groupname")) {
      Group group = GroupFinder.findByName(session, attributeValue, false);
      if (group == null) {
        throw new IllegalArgumentException("Group with name "+attributeValue+" cannot be found.");
      }
      finder.addGroup(group);
    } else if (attributeName.equals("groupidindex")) {
      if (org.apache.commons.lang3.StringUtils.isNumeric(attributeValue)) {
        Group group = GroupFinder.findByIdIndexSecure(Long.valueOf(attributeValue), false, new QueryOptions());
        if (group == null) {
          throw new IllegalArgumentException("Group with idIndex "+attributeValue+" cannot be found.");
        }
        finder.addGroup(group);
      } else {
        throw new IllegalArgumentException("Group idIndex can only be a numeric value.");
      }
    } else if (attributeName.equals("subjectid")) {
      Subject subjectMember = SubjectFinder.findById(attributeValue, false);
      if (subjectMember == null) {
        throw new IllegalArgumentException("Subject with id "+attributeValue+" cannot be found.");
      }
      finder.addSubject(subjectMember);
    } else if (attributeName.equals("subjectidentifier")) {
      Subject subjectMember = SubjectFinder.findByIdentifier(attributeValue, false);
      if (subjectMember == null) {
        throw new IllegalArgumentException("Subject with identifier "+attributeValue+" cannot be found.");
      }
      finder.addSubject(subjectMember);
    } else {
      throw new IllegalArgumentException("Invalid attribute name "+attributeName+" provided.");
    }
  }
  
  @Override
  public FilterResponse<MembershipResource> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) throws UnableToRetrieveResourceException {
    
    GrouperSession grouperSession = null;
    List<String> groupAttributeNames = new ArrayList<>();
    groupAttributeNames.add("groupid");
    groupAttributeNames.add("groupname");
    groupAttributeNames.add("groupidindex");
    
    List<String> subjectAttributeNames = new ArrayList<>();
    subjectAttributeNames.add("subjectid");
    subjectAttributeNames.add("subjectidentifier");
    FilterResponse<MembershipResource> response = new FilterResponse<>();
    
    MembershipFinder finder = new MembershipFinder();
    
    try {
      Subject subject = TierFilter.retrieveSubjectFromRemoteUser();
      grouperSession = GrouperSession.start(subject);
      if (filter == null) {
        throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "Must pass in group and/or subject");
      } else {
        
        FilterExpression filterExpression = filter.getExpression();
        if (filterExpression instanceof AttributeComparisonExpression) {
          AttributeComparisonExpression ace = (AttributeComparisonExpression) filterExpression;
          
          String attributeName = ace.getAttributePath().getFullAttributeName();
          String value = ace.getCompareValue().toString();
          CompareOperator operation = ace.getOperation();
          if (operation == CompareOperator.EQ) {
            buildMembershipFinder(attributeName, value, finder, grouperSession);
          } else {
            throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "only eq comparison operator is allowed.");
          }
          
        } else if (filterExpression instanceof LogicalExpression) {
          LogicalExpression le = (LogicalExpression) filterExpression;
          FilterExpression leftExpression = le.getLeft();
          FilterExpression rightExpression = le.getRight();
          LogicalOperator logicalOperator = le.getOperator();
          if (!(leftExpression instanceof AttributeComparisonExpression) || !(rightExpression instanceof AttributeComparisonExpression)) {
            throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "Only one level deep logical expression is allowed.");
          }
          AttributeComparisonExpression aceLeft = (AttributeComparisonExpression) leftExpression;
          AttributeComparisonExpression aceRight = (AttributeComparisonExpression) rightExpression;
          
          String attributeNameLeft = aceLeft.getAttributePath().getFullAttributeName();
          String valueLeft = aceLeft.getCompareValue().toString();
          CompareOperator operationLeft = aceLeft.getOperation();
          
          String attributeNameRight = aceRight.getAttributePath().getFullAttributeName();
          String valueRight = aceRight.getCompareValue().toString();
          CompareOperator operationRight = aceRight.getOperation();
          
          if (groupAttributeNames.contains(attributeNameLeft.toLowerCase()) && groupAttributeNames.contains(attributeNameRight.toLowerCase())) {
            throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "Left and right attributes cannot be same type.");
          }
          if (subjectAttributeNames.contains(attributeNameLeft.toLowerCase()) && subjectAttributeNames.contains(attributeNameRight.toLowerCase())) {
            throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "Left and right attributes cannot be same type.");
          }
          if (operationLeft != CompareOperator.EQ || operationRight != CompareOperator.EQ) {
            throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "Only eq operator is allowed.");
          }
          if (logicalOperator == LogicalOperator.AND) {
            buildMembershipFinder(attributeNameLeft, valueLeft, finder, grouperSession);
            buildMembershipFinder(attributeNameRight, valueRight, finder, grouperSession);
          } else {
            throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "Only AND logical operator is allowed.");
          }
          
        } else {
          throw new UnableToRetrieveResourceException(Status.BAD_REQUEST, "only attribute comparison and logical expressions are allowed.");
        }
        
        MembershipResult membershipResult = finder.findMembershipResult();
        List<MembershipResource> membershipResources = new ArrayList<>();
        Set<Object[]> membershipsOwnersMembers = membershipResult.getMembershipsOwnersMembers();
        membershipsOwnersMembers.forEach( array -> {
          Membership membership = (Membership)array[0];
          MembershipResource membershipResource = buildMembershipResourceFromMembership(membership);
          membershipResources.add(membershipResource);
        });
        response.setResources(membershipResources);
        PageRequest pr = new PageRequest();
        pr.setCount(membershipResources.size());
        pr.setStartIndex(0);
        response.setPageRequest(pr);
        response.setTotalResults(membershipResources.size());
      }
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
    LOG.info("Starting to delete membership with id: "+id);
    try {
      Subject authenticatedSubject = TierFilter.retrieveSubjectFromRemoteUser();
      grouperSession = GrouperSession.start(authenticatedSubject);
      Membership membership = MembershipFinder.findByUuid(grouperSession, id, false, false);
      if (membership == null) {
        throw new UnableToDeleteResourceException(Status.NOT_FOUND, "Membership with id "+id+" doesn't exist.");
      }
      membership.delete();
    } catch(InsufficientPrivilegeException e) {
      throw new UnableToDeleteResourceException(Status.FORBIDDEN, "User doesn't have sufficient priviliges.");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  @Override
  public List<Class<? extends ScimExtension>> getExtensionList()
      throws UnableToRetrieveExtensionsException {
    return Collections.emptyList();
  }
  

}
