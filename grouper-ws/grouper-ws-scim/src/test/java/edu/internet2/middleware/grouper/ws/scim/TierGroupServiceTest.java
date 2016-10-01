/**
 * 
 */
package edu.internet2.middleware.grouper.ws.scim;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashSet;
import java.util.Set;

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
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ws.scim.group.TierGroupExtension;
import edu.internet2.middleware.grouper.ws.scim.group.TierGroupService;
import edu.internet2.middleware.subject.Subject;
import edu.psu.swe.scim.server.exception.UnableToCreateResourceException;
import edu.psu.swe.scim.server.exception.UnableToDeleteResourceException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.server.exception.UnableToUpdateResourceException;
import edu.psu.swe.scim.spec.exception.InvalidExtensionException;
import edu.psu.swe.scim.spec.resources.ScimGroup;

/**
 * @author vsachdeva
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({TierFilter.class, GroupFinder.class, GrouperSession.class, PrivilegeHelper.class})
public class TierGroupServiceTest {
  
  TierGroupService groupService;
  
  GrouperSession mockGrouperSession;
  
  Group mockGroup;
  
  Subject subject;
  
  @Before
  public void setup() {
    
    groupService = new TierGroupService();
    
    subject = mock(Subject.class);
    mockStatic(TierFilter.class);
    when(TierFilter.retrieveSubjectFromRemoteUser()).thenReturn(subject);
    
    Membership membership1 = mock(Membership.class);
    Member member1 = mock(Member.class);
    when(member1.getId()).thenReturn("member1Id");
    when(membership1.getMember()).thenReturn(member1);
    
    Membership membership2 = mock(Membership.class);
    Member member2 = mock(Member.class);
    when(member2.getId()).thenReturn("member2Id");
    when(membership2.getMember()).thenReturn(member2);
    Set<Membership> memberships = new HashSet<>();
    memberships.add(membership1);
    memberships.add(membership2);
    
    mockGrouperSession = mock(GrouperSession.class);
    
    mockGroup = mock(Group.class);
    when(mockGroup.getId()).thenReturn("id");
    when(mockGroup.getName()).thenReturn("name");
    when(mockGroup.getDisplayName()).thenReturn("display name");
    when(mockGroup.getDescription()).thenReturn("description");
    when(mockGroup.getIdIndex()).thenReturn(123L);
    when(mockGroup.getMemberships()).thenReturn(memberships);
    
    mockStatic(GrouperSession.class);
    when(GrouperSession.start(subject)).thenReturn(mockGrouperSession);
    
  }
  
  @Test
  public void getGroupByUuId() throws UnableToRetrieveResourceException, InvalidExtensionException {

    //given
    mockStatic(GroupFinder.class);
    when(GroupFinder.findByUuid(mockGrouperSession, "uuid", false)).thenReturn(mockGroup);
    
    mock(GrouperSession.class);
    when(GrouperSession.startRootSession()).thenReturn(mockGrouperSession);
    
    mockStatic(PrivilegeHelper.class);
    when(PrivilegeHelper.canView(any(), any(), any())).thenReturn(true);
    
    //when
    ScimGroup scimGroup = groupService.get("uuid");
    
    //then
    verifyStatic();
    GroupFinder.findByUuid(mockGrouperSession, "uuid", false);
    assertThat(scimGroup.getId(), equalTo("id"));
    assertThat(scimGroup.getDisplayName(), equalTo("display name"));
    assertThat(scimGroup.getExtension(TierGroupExtension.class).getDescription(), equalTo("description"));
    assertThat(scimGroup.getExtension(TierGroupExtension.class).getIdIndex(), equalTo(123L));
    assertThat(scimGroup.getExtension(TierGroupExtension.class).getSystemName(), equalTo("name"));
    assertThat(scimGroup.getMembers(), hasSize(2));
    assertThat(scimGroup.getExtension(TierMetaExtension.class).getResultCode(), equalTo("SUCCESS"));
    
  }
  
  @Test
  public void throwsExceptionWhenGroupNotFoundWhileGettingGroup() {

    //given
    mockStatic(GroupFinder.class);
    when(GroupFinder.findByUuid(mockGrouperSession, "uuid", false)).thenReturn(null);
    
    mock(GrouperSession.class);
    when(GrouperSession.startRootSession()).thenReturn(mockGrouperSession);
    
    try {
      //when
      groupService.get("uuid");
      fail("expected UnableToRetrieveResourceException");
    } catch(UnableToRetrieveResourceException e) {
      //then
      verifyStatic();
      GroupFinder.findByUuid(mockGrouperSession, "uuid", false);
      assertThat(e.getStatus(), equalTo(Status.BAD_REQUEST));
    }
    
  }
  
  
  @Test
  public void getGroupBySystemName() throws UnableToRetrieveResourceException, InvalidExtensionException {

    //given
    mockStatic(GroupFinder.class);
    when(GroupFinder.findByName(mockGrouperSession, "name", false)).thenReturn(mockGroup);
    
    mockStatic(PrivilegeHelper.class);
    when(PrivilegeHelper.canView(any(), any(), any())).thenReturn(true);
    
    mock(GrouperSession.class);
    when(GrouperSession.startRootSession()).thenReturn(mockGrouperSession);
    
    //when
    ScimGroup scimGroup = groupService.get("systemName:name");
    
    //then
    verifyStatic();
    GroupFinder.findByName(mockGrouperSession, "name", false);
    assertThat(scimGroup.getId(), equalTo("id"));
    assertThat(scimGroup.getDisplayName(), equalTo("display name"));
    assertThat(scimGroup.getExtension(TierGroupExtension.class).getDescription(), equalTo("description"));
    assertThat(scimGroup.getExtension(TierGroupExtension.class).getIdIndex(), equalTo(123L));
    assertThat(scimGroup.getExtension(TierGroupExtension.class).getSystemName(), equalTo("name"));
    assertThat(scimGroup.getMembers(), hasSize(2));
    assertThat(scimGroup.getExtension(TierMetaExtension.class).getResultCode(), equalTo("SUCCESS"));
    
  }
  
  @Test
  public void idIndexCanOnlyByNumberic() throws UnableToRetrieveResourceException {

    try {
      //when
      groupService.get("idIndex:nonNumeric");
      fail("expected UnableToRetrieveResourceException");
    } catch(UnableToRetrieveResourceException e) {
      //then
      assertThat(e.getStatus(), equalTo(Status.BAD_REQUEST));
    }
    
  }
  
  @Test
  public void getGroupByIdIndex() throws UnableToRetrieveResourceException, InvalidExtensionException {

    //given
    mockStatic(GroupFinder.class);
    when(GroupFinder.findByIdIndexSecure(102L, false, null)).thenReturn(mockGroup);
    
    mockStatic(PrivilegeHelper.class);
    when(PrivilegeHelper.canView(any(), any(), any())).thenReturn(true);
    
    //when
    ScimGroup scimGroup = groupService.get("idIndex:102");
    
    //then
    verifyStatic();
    GroupFinder.findByIdIndexSecure(102L, false, null);
    assertThat(scimGroup.getId(), equalTo("id"));
    assertThat(scimGroup.getDisplayName(), equalTo("display name"));
    assertThat(scimGroup.getExtension(TierGroupExtension.class).getDescription(), equalTo("description"));
    assertThat(scimGroup.getExtension(TierGroupExtension.class).getIdIndex(), equalTo(123L));
    assertThat(scimGroup.getExtension(TierGroupExtension.class).getSystemName(), equalTo("name"));
    assertThat(scimGroup.getMembers(), hasSize(2));
    assertThat(scimGroup.getExtension(TierMetaExtension.class).getResultCode(), equalTo("SUCCESS"));
    
  }
  
  @Test
  public void createGroup() throws InvalidExtensionException, UnableToCreateResourceException {
    
    //given
    ScimGroup scimGroup = new ScimGroup();
    scimGroup.setDisplayName("test:test123");

    class TierGroupServiceToBeTested extends TierGroupService {
      @Override
      protected Group saveGroup(GrouperSession grouperSession, ScimGroup scimGroup)
          throws InvalidExtensionException {
        return mockGroup;
      }
    }
    
    //when
    ScimGroup scimGroup2 = new TierGroupServiceToBeTested().create(scimGroup);
    
    //then
    assertThat(scimGroup2.getDisplayName(), equalTo(mockGroup.getDisplayName()));
    assertThat(scimGroup2.getId(), equalTo(mockGroup.getId()));
    assertThat(scimGroup2.getExtensions().size(), equalTo(2));
    
  }
  
  @Test
  public void groupNameMustIncludeColonWhileCreating() {
    
    //given
    ScimGroup scimGroup = new ScimGroup();
    scimGroup.setDisplayName("no colon");
    
    try {
    //when
      groupService.create(scimGroup);
      fail("UnableToCreateResourceException should have been thrown");
    } catch (UnableToCreateResourceException e) {
      //then
      assertThat(e.getStatus(), equalTo(Status.BAD_REQUEST));
    }
        
  }
  
  
  @Test
  public void updateGroup() throws InvalidExtensionException, UnableToUpdateResourceException {
    
    //given
    ScimGroup scimGroup = new ScimGroup();
    scimGroup.setDisplayName("test:test123");
    
    mockStatic(GroupFinder.class);
    when(GroupFinder.findByUuid(mockGrouperSession, "uuid", false)).thenReturn(mockGroup);
    
    mockStatic(PrivilegeHelper.class);
    when(PrivilegeHelper.canView(any(), any(), any())).thenReturn(true);
    
    mock(GrouperSession.class);
    when(GrouperSession.startRootSession()).thenReturn(mockGrouperSession);

    class TierGroupServiceToBeTested extends TierGroupService {
      @Override
      protected Group updateGroup(GrouperSession grouperSession, ScimGroup scimGroup, String uuid)
          throws InvalidExtensionException {
        return mockGroup;
      }
    }
    
    //when
    ScimGroup scimGroup2 = new TierGroupServiceToBeTested().update("uuid", scimGroup);
    
    //then
    verifyStatic();
    GroupFinder.findByUuid(mockGrouperSession, "uuid", false);
    assertThat(scimGroup2.getDisplayName(), equalTo(mockGroup.getDisplayName()));
    assertThat(scimGroup2.getId(), equalTo(mockGroup.getId()));
    assertThat(scimGroup2.getExtensions().size(), equalTo(2));
    
  }
  
  
  @Test
  public void throwsExceptionIfGroupCouldNotBeFoundWhileUpdating() {
    
    //given
    ScimGroup scimGroup = new ScimGroup();
    mockStatic(GroupFinder.class);
    when(GroupFinder.findByUuid(mockGrouperSession, "non existent uuid", false)).thenReturn(null);
    
    mock(GrouperSession.class);
    when(GrouperSession.startRootSession()).thenReturn(mockGrouperSession);
    
    try {
      //when
      groupService.update("non existent uuid", scimGroup);
      fail("UnableToUpdateResourceException should have been thrown");
    } catch (UnableToUpdateResourceException e) {
      //then
      verifyStatic();
      GroupFinder.findByUuid(mockGrouperSession, "non existent uuid", false);
      assertThat(e.getStatus(), equalTo(Status.BAD_REQUEST));
    }
  }
  
  
  @Test
  public void deleteGroup() throws UnableToDeleteResourceException {

    //given
    mockStatic(GroupFinder.class);
    when(GroupFinder.findByUuid(mockGrouperSession, "uuid", false)).thenReturn(mockGroup);
    
    mockStatic(PrivilegeHelper.class);
    when(PrivilegeHelper.canView(any(), any(), any())).thenReturn(true);
    
    mock(GrouperSession.class);
    when(GrouperSession.startRootSession()).thenReturn(mockGrouperSession);
    
    //when 
    groupService.delete("uuid");
    
    //then
    verifyStatic();
    GroupFinder.findByUuid(mockGrouperSession, "uuid", false);
    verify(mockGroup, Mockito.times(1)).delete();
    
  }
  
  
  @Test
  public void deleteGroupThrowsExceptionWhenGroupNotFound() {
    
    //given
    mockStatic(GroupFinder.class);
    when(GroupFinder.findByUuid(mockGrouperSession, "non existent uuid", false)).thenReturn(null);
    
    mock(GrouperSession.class);
    when(GrouperSession.startRootSession()).thenReturn(mockGrouperSession);
    
    try {
      //when
      groupService.delete("non existent uuid");
      fail("UnableToDeleteResourceException should have been thrown");
    } catch (UnableToDeleteResourceException e) {
      //then
      verifyStatic();
      GroupFinder.findByUuid(mockGrouperSession, "non existent uuid", false);
      assertThat(e.getStatus(), equalTo(Status.BAD_REQUEST));
    }
    
  }
  
  
  
}
