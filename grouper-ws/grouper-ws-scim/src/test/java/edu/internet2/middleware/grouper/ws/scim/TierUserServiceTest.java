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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ws.scim.user.TierUserService;
import edu.internet2.middleware.subject.Subject;
import edu.psu.swe.scim.server.exception.UnableToRetrieveResourceException;
import edu.psu.swe.scim.spec.exception.InvalidExtensionException;
import edu.psu.swe.scim.spec.resources.ScimUser;

/**
 * @author vsachdeva
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({TierFilter.class, GrouperSession.class, SubjectFinder.class})
public class TierUserServiceTest {
  
  private TierUserService userService;
  
  private Subject mockAuthenticatedSubject;
  
  private GrouperSession mockGrouperSession;
  
  @Before
  public void setup() {
    
    userService = new TierUserService();
    
    mockAuthenticatedSubject = mock(Subject.class);
    mockStatic(TierFilter.class);
    when(TierFilter.retrieveSubjectFromRemoteUser()).thenReturn(mockAuthenticatedSubject);
    
    mockGrouperSession = mock(GrouperSession.class);
    mockStatic(GrouperSession.class);
    when(GrouperSession.start(mockAuthenticatedSubject)).thenReturn(mockGrouperSession);
    
  }
  
  @Test
  public void getUserByIdSuccessfully() throws UnableToRetrieveResourceException, InvalidExtensionException {
    
    //given
    Subject subject = mock(Subject.class);
    when(subject.getName()).thenReturn("name");
    when(subject.getId()).thenReturn("id");
    
    mockStatic(SubjectFinder.class);
    when(SubjectFinder.findByIdOrIdentifier("id", false)).thenReturn(subject);
    
    //when
    ScimUser scimUser = userService.get("id");
    
    //then
    verifyStatic();
    SubjectFinder.findByIdOrIdentifier("id", false);
    assertThat(scimUser.getId(), equalTo("id"));
    assertThat(scimUser.getDisplayName(), equalTo("name"));
    assertThat(scimUser.getExtension(TierMetaExtension.class).getResultCode(), equalTo("SUCCESS"));
    
  }
  
  @Test
  public void subjectNotFound() {
    
    //given
    mockStatic(SubjectFinder.class);
    when(SubjectFinder.findByIdOrIdentifier("id", false)).thenReturn(null);
    
    try {
      //when
      userService.get("id");
      fail("expected UnableToRetrieveResourceException");
    } catch(UnableToRetrieveResourceException e) {
      //then
      verifyStatic();
      SubjectFinder.findByIdOrIdentifier("id", false);
      assertThat(e.getStatus(), equalTo(Status.NOT_FOUND));
    }
    
  }

}
