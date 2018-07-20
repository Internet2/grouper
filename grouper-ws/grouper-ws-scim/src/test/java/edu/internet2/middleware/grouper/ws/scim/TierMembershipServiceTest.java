/**
 * 
 */
package edu.internet2.middleware.grouper.ws.scim;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ws.scim.membership.MembershipResource;
import edu.internet2.middleware.grouper.ws.scim.membership.TierMembershipService;
import edu.internet2.middleware.grouper.ws.scim.membership.OwnerGroup;
import edu.internet2.middleware.subject.Subject;
import edu.psu.swe.scim.server.exception.UnableToCreateResourceException;
import edu.psu.swe.scim.server.exception.UnableToDeleteResourceException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
import edu.psu.swe.scim.server.provider.UpdateRequest;
import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.exception.InvalidExtensionException;

/**
 * @author vsachdeva
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({TierFilter.class, MembershipFinder.class, GroupFinder.class, Group.class, SubjectFinder.class, GrouperSession.class, PrivilegeHelper.class})
public class TierMembershipServiceTest {
  
  TierMembershipService membershipService;
  
  GrouperSession mockGrouperSession;
  
  Membership mockMembership;
  Member mockMember;
  Subject mockMemberSubject;
  Group mockOwnerGroup;
  
  Subject subject;
  
  @Before
  public void setup() {
    
    membershipService = new TierMembershipService();
    
    subject = mock(Subject.class);
    mockStatic(TierFilter.class);
    when(TierFilter.retrieveSubjectFromRemoteUser()).thenReturn(subject);
    
    mockMembership = mock(Membership.class);
    mockMember = mock(Member.class);
    mockMemberSubject = mock(Subject.class);
    mockOwnerGroup = mock(Group.class);
    
    when(mockOwnerGroup.getDisplayName()).thenReturn("ownerGroupDisplayName");
    when(mockOwnerGroup.getUuid()).thenReturn("ownerGroupUUID");
    when(mockOwnerGroup.getName()).thenReturn("ownerGroupName");
    when(mockMemberSubject.getId()).thenReturn("subjectId");
    when(mockMemberSubject.getName()).thenReturn("subjectName");
    when(mockMemberSubject.getTypeName()).thenReturn("subjectTypeName");
    when(mockMember.getId()).thenReturn("member1Id");
    when(mockMember.getSubject()).thenReturn(mockMemberSubject);
    when(mockMembership.getMember()).thenReturn(mockMember);
    when(mockMembership.getOwnerGroup()).thenReturn(mockOwnerGroup);
    when(mockMembership.getUuid()).thenReturn("uuid");
    
    mockGrouperSession = mock(GrouperSession.class);
    
    mockStatic(GrouperSession.class);
    when(GrouperSession.start(subject)).thenReturn(mockGrouperSession);
    
  }
  
  @Test
  public void getMembershipById() throws UnableToRetrieveResourceException, InvalidExtensionException {
    
    //given
    mockStatic(MembershipFinder.class);
    when(MembershipFinder.findByUuid(mockGrouperSession, "uuid", false, false)).thenReturn(mockMembership);
    
    //when
    MembershipResource membershipResource = membershipService.get("uuid");
    
    //then
    verifyStatic();
    MembershipFinder.findByUuid(mockGrouperSession, "uuid", false, false);
    assertThat(membershipResource.getId(), equalTo("uuid"));
    assertThat(membershipResource.getMember().getValue(), equalTo("subjectId"));
  }
  
  @Test
  public void getMembershipByIdNotFound() throws InvalidExtensionException {
    
    //given
    mockStatic(MembershipFinder.class);
    when(MembershipFinder.findByUuid(mockGrouperSession, "uuid", false, false)).thenReturn(null);
    
    try {
      //when
      membershipService.get("uuid");
      fail("expected UnableToRetrieveResourceException");
    } catch(UnableToRetrieveResourceException e) {
      //then
      verifyStatic();
      MembershipFinder.findByUuid(mockGrouperSession, "uuid", false, false);
      assertThat(e.getStatus(), equalTo(Status.NOT_FOUND));
    }
  }
  
  @Test
  public void createMembershipSuccessfully() throws UnableToCreateResourceException {
    
    //given
    mockStatic(GroupFinder.class);
    when(GroupFinder.findByUuid(mockGrouperSession, "ownerGroupUUID", false)).thenReturn(mockOwnerGroup);
    
    mockStatic(SubjectFinder.class);
    when(SubjectFinder.findByIdOrIdentifier("subjectId", false)).thenReturn(mockMemberSubject);
    
    mockStatic(Group.class);
    when(Group.getDefaultList()).thenReturn(null);
    
    when(mockOwnerGroup.addMember(mockMemberSubject, false)).thenReturn(true);
    when(mockOwnerGroup.getImmediateMembership(Group.getDefaultList(), mockMemberSubject, true, true)).thenReturn(mockMembership);
    
    MembershipResource resource = new MembershipResource();
    OwnerGroup ownerGroup = new OwnerGroup();
    ownerGroup.setValue(mockOwnerGroup.getUuid());
    edu.internet2.middleware.grouper.ws.scim.membership.Member member = new edu.internet2.middleware.grouper.ws.scim.membership.Member();
    member.setValue("subjectId");
    resource.setMember(member);
    resource.setOwner(ownerGroup);
    
    //when
    MembershipResource membershipResourceOutput = membershipService.create(resource);
    
    //then
    assertThat(membershipResourceOutput.getMember().getValue(), equalTo(mockMembership.getMember().getSubject().getId()));
    assertThat(membershipResourceOutput.getId(), equalTo(mockMembership.getUuid()));
    assertThat(membershipResourceOutput.getOwner().getValue(), equalTo(mockOwnerGroup.getUuid()));
  }

  @Test
  public void createMembershipFailsWhenOwnerGroupNotProvided() {
    
    //given
    MembershipResource resource = new MembershipResource();
    edu.internet2.middleware.grouper.ws.scim.membership.Member member = new edu.internet2.middleware.grouper.ws.scim.membership.Member();
    member.setValue("subjectId");
    resource.setMember(member);
    try {
      //when
      membershipService.create(resource);
      fail("expected UnableToCreateResourceException");
    } catch(UnableToCreateResourceException e) {
      //then
      assertThat(e.getStatus(), equalTo(Status.BAD_REQUEST));
    }
  }
  
  @Test
  public void createMembershipFailsWhenMemberNotProvided() {
    
    //given
    MembershipResource resource = new MembershipResource();
    OwnerGroup ownerGroup = new OwnerGroup();
    ownerGroup.setValue(mockOwnerGroup.getUuid());
    resource.setOwner(ownerGroup);
    
    try {
      //when
      membershipService.create(resource);
      fail("expected UnableToCreateResourceException");
    } catch(UnableToCreateResourceException e) {
      //then
      assertThat(e.getStatus(), equalTo(Status.BAD_REQUEST));
    }
  }
  
  @Test
  public void createMembershipFailsWhenGroupNotFoundInDatabase() {
    
    //given
    mockStatic(GroupFinder.class);
    when(GroupFinder.findByUuid(mockGrouperSession, "ownerGroupUUID", false)).thenReturn(null);

    MembershipResource resource = new MembershipResource();
    OwnerGroup ownerGroup = new OwnerGroup();
    ownerGroup.setValue(mockOwnerGroup.getUuid());
    edu.internet2.middleware.grouper.ws.scim.membership.Member member = new edu.internet2.middleware.grouper.ws.scim.membership.Member();
    member.setValue("subjectId");
    resource.setMember(member);
    resource.setOwner(ownerGroup);
    
    try {
      //when
      membershipService.create(resource);
      fail("expected UnableToCreateResourceException");
    } catch(UnableToCreateResourceException e) {
      //then
      assertThat(e.getStatus(), equalTo(Status.BAD_REQUEST));
    }
  }
  
  @Test
  public void deleteMembership() throws UnableToDeleteResourceException {
    
    //given
    mockStatic(MembershipFinder.class);
    when(MembershipFinder.findByUuid(mockGrouperSession, "uuid", false, false)).thenReturn(mockMembership);
    
    //when 
    membershipService.delete("uuid");
    
    //then
    verifyStatic();
    MembershipFinder.findByUuid(mockGrouperSession, "uuid", false, false);
    verify(mockMembership, Mockito.times(1)).delete();
  }
  
  @Test
  public void deleteMembershipFailsWhenMembershipNotFound() {
    
    //given
    mockStatic(MembershipFinder.class);
    when(MembershipFinder.findByUuid(mockGrouperSession, "non existent uuid", false, false)).thenReturn(null);
    
    try {
      //when
      membershipService.delete("non existent uuid");
      fail("UnableToDeleteResourceException should have been thrown");
    } catch (UnableToDeleteResourceException e) {
      //then
      verifyStatic();
      MembershipFinder.findByUuid(mockGrouperSession, "non existent uuid", false, false);
      assertThat(e.getStatus(), equalTo(Status.NOT_FOUND));
    }
    
  }
  
  @Test
  public void updateMembershipSuccessfully() throws UnableToUpdateResourceException {
    
    //given
    LocalDateTime disabledTime = LocalDateTime.now().plusDays(10);
    LocalDateTime enabledTime = LocalDateTime.now();
    when(mockMembership.isImmediate()).thenReturn(true);
    when(mockMembership.getEnabledTime()).thenReturn(Timestamp.valueOf(enabledTime));
    when(mockMembership.getDisabledTime()).thenReturn(Timestamp.valueOf(disabledTime));
    
    mockStatic(MembershipFinder.class);
    when(MembershipFinder.findByUuid(mockGrouperSession, "uuid", false, false)).thenReturn(mockMembership);
    
    MembershipResource resource = new MembershipResource();
    resource.setDisabledTime(disabledTime);
    resource.setEnabledTime(enabledTime);
    resource.setId("uuid");
    
    Registry registry = new Registry();
    UpdateRequest<MembershipResource> updateRequest = new UpdateRequest<MembershipResource>(registry);
    updateRequest.initWithResource(resource.getId(), resource, resource);
    
    //when
    MembershipResource membershipResourceOutput = membershipService.update(updateRequest);
    
    //then
    verify(mockMembership, Mockito.times(1)).update();
    assertThat(membershipResourceOutput.getMember().getValue(), equalTo(mockMembership.getMember().getSubject().getId()));
    assertThat(membershipResourceOutput.getId(), equalTo(mockMembership.getUuid()));
    assertThat(membershipResourceOutput.getOwner().getValue(), equalTo(mockOwnerGroup.getUuid()));
    assertThat(membershipResourceOutput.getEnabledTime(), equalTo(enabledTime));
    assertThat(membershipResourceOutput.getDisabledTime(), equalTo(disabledTime));
  }
  
  @Test
  public void updateMembershipFailsWhenMembershipNotFound() {
    
    //given
    mockStatic(MembershipFinder.class);
    when(MembershipFinder.findByUuid(mockGrouperSession, "non existent uuid", false, false)).thenReturn(null);
    
    MembershipResource resource = new MembershipResource();
    resource.setId("non existent uuid");
    
    Registry registry = new Registry();
    UpdateRequest<MembershipResource> updateRequest = new UpdateRequest<MembershipResource>(registry);
    updateRequest.initWithResource(resource.getId(), resource, resource);
    
    try {
      //when
      membershipService.update(updateRequest);
      fail("UnableToDeleteResourceException should have been thrown");
    } catch (UnableToUpdateResourceException e) {
      //then
      verifyStatic();
      MembershipFinder.findByUuid(mockGrouperSession, "non existent uuid", false, false);
      assertThat(e.getStatus(), equalTo(Status.NOT_FOUND));
    }
    
  }
  
}
