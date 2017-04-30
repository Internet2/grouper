package edu.internet2.middleware.grouper.app.attestation;

import static edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob.ATTESTATION_DATE_CERTIFIED;
import static edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob.ATTESTATION_DAYS_BEFORE_TO_REMIND;
import static edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob.ATTESTATION_DAYS_UNTIL_RECERTIFY;
import static edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob.ATTESTATION_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob.ATTESTATION_EMAIL_ADDRESSES;
import static edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob.ATTESTATION_LAST_EMAILED_DATE;
import static edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob.ATTESTATION_SEND_EMAIL;
import static edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob.ATTESTATION_STEM_SCOPE;
import static edu.internet2.middleware.grouper.misc.GrouperCheckConfig.checkAttribute;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob.EmailObject;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignSave;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;

public class GrouperAttestationJobTest extends GrouperTest {
  
  /**
   * @param name
   */
  public GrouperAttestationJobTest(String name) {
    super(name);
  }
  
  
  private void setupGroupEmailAttributes() {
    
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    String attestationRootStemName = GrouperAttestationJob.attestationStemName();
    
    Stem attestationStem = StemFinder.findByName(grouperSession, attestationRootStemName, false);
    if (attestationStem == null) {
      attestationStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignDescription("folder for built in Grouper attestation attributes").assignName(attestationRootStemName)
        .save();
    }

    //see if attributeDef is there
    String attestationTypeDefName = attestationRootStemName + ":attestationDef";
    AttributeDef attestationType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(
        attestationTypeDefName, false, new QueryOptions().secondLevelCache(false));
    if (attestationType == null) {
      attestationType = attestationStem.addChildAttributeDef("attestationDef", AttributeDefType.type);
      attestationType.setAssignToGroup(true);
      attestationType.setAssignToStem(true);
      attestationType.store();
    }
    
    boolean wasInCheckConfig = true;
    //add a name
    AttributeDefName attribute = checkAttribute(attestationStem, attestationType, "attestation", "attestation", "has attestation attributes", wasInCheckConfig);
    
    //lets add some rule attributes
    String attestationAttrDefName = attestationRootStemName + ":attestationValueDef";
    AttributeDef attestationAttrType = GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(  
        attestationAttrDefName, false, new QueryOptions().secondLevelCache(false));
    if (attestationAttrType == null) {
      attestationAttrType = attestationStem.addChildAttributeDef("attestationValueDef", AttributeDefType.attr);
      attestationAttrType.setAssignToGroupAssn(true);
      attestationAttrType.setAssignToStemAssn(true);
      attestationAttrType.setValueType(AttributeDefValueType.string);
      attestationAttrType.store();
    }

    //the attributes can only be assigned to the type def
    // try an attribute def dependent on an attribute def name
    attestationAttrType.getAttributeDefScopeDelegate().assignOwnerNameEquals(attribute.getName());

    //add some names
    checkAttribute(attestationStem, attestationAttrType, ATTESTATION_DATE_CERTIFIED, 
        GrouperAttestationJob.ATTESTATION_DATE_CERTIFIED, "Last certified date for this group", wasInCheckConfig);
    checkAttribute(attestationStem, attestationAttrType, ATTESTATION_DAYS_BEFORE_TO_REMIND,
        ATTESTATION_DAYS_BEFORE_TO_REMIND, "Number of days before attestation deadline to start sending emails about it to owners", wasInCheckConfig);
    checkAttribute(attestationStem, attestationAttrType, ATTESTATION_DAYS_UNTIL_RECERTIFY,
        ATTESTATION_DAYS_UNTIL_RECERTIFY, "Number of days until need to recertify from last certification", wasInCheckConfig);
    checkAttribute(attestationStem, attestationAttrType, ATTESTATION_DIRECT_ASSIGNMENT,
        ATTESTATION_DIRECT_ASSIGNMENT, "If this group has attestation settings and not inheriting from ancestor folders (group only)", wasInCheckConfig);
    AttributeDefName attributeDefNameMeta4 = checkAttribute(attestationStem, attestationAttrType, ATTESTATION_EMAIL_ADDRESSES,
        ATTESTATION_EMAIL_ADDRESSES, "Comma separated email addresses to send reminders to, if blank then send to group admins", wasInCheckConfig);
    checkAttribute(attestationStem, attestationAttrType, ATTESTATION_LAST_EMAILED_DATE,
        ATTESTATION_LAST_EMAILED_DATE, "yyyy/mm/dd date that this was last emailed so multiple emails don't go out on same day (group only)", wasInCheckConfig);
    checkAttribute(attestationStem, attestationAttrType, ATTESTATION_SEND_EMAIL,
        ATTESTATION_SEND_EMAIL, "true or false if emails should be sent", wasInCheckConfig);
    checkAttribute(attestationStem, attestationAttrType, ATTESTATION_STEM_SCOPE,
        ATTESTATION_STEM_SCOPE, "one or sub for if attestation settings inherit to just this folder or also to subfolders (folder only)", wasInCheckConfig);
    
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group0").save();
    
    AttributeAssign attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0).assignAttributeDefName(attribute).save();
    
    AttributeAssignValue attributeAssignValueEmailAddresses = new AttributeAssignValue();
    attributeAssignValueEmailAddresses.setValueString("test@test.com,test1@test.com");
    
    new AttributeAssignSave(grouperSession).assignOwnerAttributeAssign(attributeAssignBase).assignAttributeDefName(attributeDefNameMeta4).addAttributeAssignValue(attributeAssignValueEmailAddresses).save();
 
    attributeAssignBase = group0.getAttributeDelegate().retrieveAssignment(null, attribute, false, true);
  }
  
  public void testBuildAttestationGroupEmails() {
   
    setupGroupEmailAttributes();
   
    GrouperAttestationJob job = new GrouperAttestationJob();
    AttributeDef attributeDef = AttributeDefFinder.findByName(GrouperAttestationJob.attestationStemName() + ":" + "attestationDef", false);
    Set<AttributeAssign> groupAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeAssignments(
        AttributeAssignType.group,
        attributeDef.getId(), null, null,
        null, null, null, 
        null, 
        Boolean.TRUE, false);
    
    Map<String, Set<EmailObject>> attestationGroupEmails = job.buildAttestationGroupEmails(groupAttributeAssigns);
   
    assertEquals(2, attestationGroupEmails.size());
    assertTrue(attestationGroupEmails.containsKey("test@test.com"));
    assertTrue(attestationGroupEmails.containsKey("test1@test.com"));
  }

}
