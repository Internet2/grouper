package edu.internet2.middleware.grouper.app.usdu;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;

public class UsduServiceTest extends GrouperTest {
  
  private Date lastResolved;
  
  private Date lastChecked;
  
  private int daysUnresolved;
  
  private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
  
  @Override
  protected void setUp() {
    super.setUp();
    
    try {
      lastResolved = dateFormat.parse("2019/01/05");
      lastChecked = dateFormat.parse("2019/01/10");
    } catch(ParseException e) {
      throw new RuntimeException("why??");
    }
    
    daysUnresolved = 10;
    
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
  }
  
  public void testGetSubjectResolutionAttributeValue() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group1").save();
    group.addMember(SubjectTestHelper.SUBJ0);
    
    Member member = group.getMembers().iterator().next();
    
    saveSubjectResolutionAttributeMetadata(member, lastResolved, lastChecked, daysUnresolved);
    
    //When
    SubjectResolutionAttributeValue subjectResolutionAttributeValue = UsduService.getSubjectResolutionAttributeValue(member);
    
    //Then
    assertTrue(subjectResolutionAttributeValue.isSubjectResolutionResolvable());
    assertEquals(10L, subjectResolutionAttributeValue.getSubjectResolutionDaysUnresolved().longValue());
    assertEquals(dateFormat.format(lastResolved),subjectResolutionAttributeValue.getSubjectResolutionDateLastResolvedString());
    assertEquals(dateFormat.format(lastChecked),subjectResolutionAttributeValue.getSubjectResolutionDateLastCheckedString());
    
  }
  
  public void testSaveOrUpldateSubjectResolutionAttributeValue() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group1").save();
    group.addMember(SubjectTestHelper.SUBJ1);
    
    Member member = group.getMembers().iterator().next();
    
    SubjectResolutionAttributeValue subjectResolutionAttributeValue = new SubjectResolutionAttributeValue();
    subjectResolutionAttributeValue.setSubjectResolutionDateLastCheckedString(dateFormat.format(lastChecked));
    subjectResolutionAttributeValue.setSubjectResolutionDateLastResolvedString(dateFormat.format(lastResolved));
    subjectResolutionAttributeValue.setSubjectResolutionDaysUnresolvedString(String.valueOf(daysUnresolved));
    subjectResolutionAttributeValue.setSubjectResolutionResolvableString("true");
    
    //When
    UsduService.saveOrUpldateSubjectResolutionAttributeValue(subjectResolutionAttributeValue, member);
    
    //Then
    SubjectResolutionAttributeValue attributeValue = UsduService.getSubjectResolutionAttributeValue(member);
    assertTrue(attributeValue.isSubjectResolutionResolvable());
    assertEquals(10L, attributeValue.getSubjectResolutionDaysUnresolved().longValue());
    assertEquals(dateFormat.format(lastResolved),attributeValue.getSubjectResolutionDateLastResolvedString());
    assertEquals(dateFormat.format(lastChecked),attributeValue.getSubjectResolutionDateLastCheckedString());
    
  }
  
  private static void saveSubjectResolutionAttributeMetadata(Member member, Date lastResolved, Date lastChecked, int daysUnresolved) {
    
    String lastResolvedString = dateFormat.format(lastResolved);
    String lastCheckedString = dateFormat.format(lastChecked);
    
    AttributeAssign attributeAssign = member.getAttributeDelegate().assignAttribute(UsduAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName() +":"+UsduAttributeNames.SUBJECT_RESOLUTION_RESOLVABLE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(true));
    
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DATE_LAST_RESOLVED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), lastResolvedString);
    
    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_DAYS_UNRESOLVED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(daysUnresolved));

    attributeDefName = AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+UsduAttributeNames.SUBJECT_RESOLUTION_LAST_CHECKED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), lastCheckedString);
    
    attributeAssign.saveOrUpdate();
    
  }


}
