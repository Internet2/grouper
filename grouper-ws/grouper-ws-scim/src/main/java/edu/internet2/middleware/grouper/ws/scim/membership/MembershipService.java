/**
 * 
 */
package edu.internet2.middleware.grouper.ws.scim.membership;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

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
import edu.psu.swe.scim.spec.protocol.filter.FilterResponse;
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
public class MembershipService implements Provider<MembershipResource> {
  
  private static final Log LOG = LogFactory.getLog(MembershipService.class);

  @Override
  public MembershipResource create(MembershipResource resource) throws UnableToCreateResourceException {
    
    GrouperSession grouperSession = null;
    MembershipResource membershipResource = null;
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
      if (membershipGroup == null) {
        throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Please provide uuid value or system name for the group.");
      }
      
      String groupOrSubjectId = resource.getMember().getValue();
      Subject membershipSubject = SubjectFinder.findById(groupOrSubjectId, false);
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
      
      membershipResource = new MembershipResource();
      membershipResource.setId(membership.getUuid());
      membershipResource.setEnabled(membership.isEnabled());
      
      membershipResource.setDisabledTime(membership.getDisabledTime() != null ? membership.getDisabledTime().toLocalDateTime(): null);
      membershipResource.setEnabledTime(membership.getEnabledTime() != null? membership.getEnabledTime().toLocalDateTime(): null);
      membershipResource.setMembershipType("immediate");
      
      Member member = new Member();
      member.setValue(membershipSubject.getId());
      member.setDisplay(membershipSubject.getName());
      if (membershipSubject.getTypeName().equalsIgnoreCase(SubjectTypeEnum.PERSON.getName())) {
        member.setRef("../Users/"+membershipSubject.getId());
      } else {
        member.setRef("../Groups/"+membershipSubject.getId());
      }
      membershipResource.setMember(member);

      OwnerGroup ownerGroupOutput = new OwnerGroup();
      ownerGroupOutput.setDisplay(membershipGroup.getDisplayName());
      ownerGroupOutput.setRef("../Groups/"+membershipGroup.getUuid());
      ownerGroupOutput.setSystemName(membershipGroup.getName());
      ownerGroupOutput.setValue(membershipGroup.getUuid());
      membershipResource.setOwner(ownerGroupOutput);
      
      
    } catch(InsufficientPrivilegeException e) {
      throw new UnableToCreateResourceException(Status.FORBIDDEN, "User doesn't have sufficient priviliges.");
    }  catch(MemberAddException e) {
      throw new UnableToCreateResourceException(Status.BAD_REQUEST, "Please fix the request and try again.");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
    return membershipResource;
  }

  @Override
  public MembershipResource update(String id, MembershipResource resource) throws UnableToUpdateResourceException {
    
    MembershipResource membershipResource = null;
    GrouperSession grouperSession = null;
    LOG.info("Starting to update a membership with id: "+id);
    try {
      Subject authenticatedSubject = TierFilter.retrieveSubjectFromRemoteUser();
      grouperSession = GrouperSession.start(authenticatedSubject);
      Membership membership = MembershipFinder.findByUuid(grouperSession, id, false, false);
      if (membership == null) {
        throw new UnableToUpdateResourceException(Status.NOT_FOUND, "Membership with id "+id+" doesn't exist.");
      }
      
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
      
      membershipResource = new MembershipResource();
      membershipResource.setId(membership.getUuid());
      membershipResource.setEnabled(membership.isEnabled());
      
      membershipResource.setDisabledTime(membership.getDisabledTime() != null ? membership.getDisabledTime().toLocalDateTime(): null);
      membershipResource.setEnabledTime(membership.getEnabledTime() != null? membership.getEnabledTime().toLocalDateTime(): null);
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
      
    } catch(InsufficientPrivilegeException e) {
      throw new UnableToUpdateResourceException(Status.FORBIDDEN, "User doesn't have sufficient priviliges.");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    return membershipResource;
  }

  @Override
  public MembershipResource get(String id) throws UnableToRetrieveResourceException {
    MembershipResource membershipResource = null;
    GrouperSession grouperSession = null;
    LOG.info("Starting to retrieve a membership with id: "+id);
    try {
      Subject authenticatedSubject = TierFilter.retrieveSubjectFromRemoteUser();
      grouperSession = GrouperSession.start(authenticatedSubject);
      Membership membership = MembershipFinder.findByUuid(grouperSession, id, false, false);
      if (membership == null) {
        throw new UnableToRetrieveResourceException(Status.NOT_FOUND, "Membership with id "+id+" doesn't exist.");
      }
      
      membershipResource = new MembershipResource();
      membershipResource.setId(membership.getUuid());
      membershipResource.setEnabled(membership.isEnabled());
      membershipResource.setDisabledTime(membership.getDisabledTime() != null ? membership.getDisabledTime().toLocalDateTime(): null);
      membershipResource.setEnabledTime(membership.getEnabledTime() != null? membership.getEnabledTime().toLocalDateTime(): null);
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
      
    } catch(InsufficientPrivilegeException e) {
      throw new UnableToRetrieveResourceException(Status.FORBIDDEN, "User doesn't have sufficient priviliges.");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    return membershipResource;
  }

  @Override
  public FilterResponse<MembershipResource> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) throws UnableToRetrieveResourceException {
    return null;
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
