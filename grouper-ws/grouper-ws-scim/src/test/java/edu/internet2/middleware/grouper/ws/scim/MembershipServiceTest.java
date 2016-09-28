/**
 * 
 */
package edu.internet2.middleware.grouper.ws.scim;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ws.scim.membership.MembershipResource;
import edu.internet2.middleware.grouper.ws.scim.membership.MembershipService;
import edu.internet2.middleware.subject.Subject;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.spec.exception.InvalidExtensionException;

/**
 * @author vsachdeva
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({TierFilter.class, MembershipFinder.class, GrouperSession.class, PrivilegeHelper.class})
public class MembershipServiceTest {
  
  MembershipService membershipService;
  
  GrouperSession mockGrouperSession;
  
  Membership mockMembership;
  Member mockMember;
  Subject mockMemberSubject;
  Group mockOwnerGroup;
  
  Subject subject;
  
  @Before
  public void setup() {
    
    membershipService = new MembershipService();
    
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

}
