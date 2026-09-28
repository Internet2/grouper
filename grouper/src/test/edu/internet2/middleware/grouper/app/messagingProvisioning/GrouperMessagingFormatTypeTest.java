package edu.internet2.middleware.grouper.app.messagingProvisioning;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * test the JEXL translation script message format, does not need a database
 */
public class GrouperMessagingFormatTypeTest extends TestCase {

  public static void main(String[] args) {
    TestRunner.run(GrouperMessagingFormatTypeTest.class);
  }

  public GrouperMessagingFormatTypeTest(String name) {
    super(name);
  }

  private static GrouperMessagingConfiguration configuration() {
    GrouperMessagingConfiguration grouperMessagingConfiguration = new GrouperMessagingConfiguration();
    grouperMessagingConfiguration.setMessagingFormatType(GrouperMessagingFormatType.TranslationScript);
    return grouperMessagingConfiguration;
  }

  private static GrouperMessagingEntity entity() {
    ProvisioningEntity provisioningEntity = new ProvisioningEntity();
    provisioningEntity.setId("entityId1");
    provisioningEntity.setSubjectId("subj1");
    provisioningEntity.setEmail("subj1@example.edu");
    return GrouperMessagingEntity.fromProvisioningEntity(provisioningEntity);
  }

  /**
   * the example from the Jira, with the JEXL map literal.  Also a null property (name) does not fail the script
   */
  public void testEntityScript() {
    GrouperMessagingConfiguration grouperMessagingConfiguration = configuration();
    grouperMessagingConfiguration.setEntityFormatTranslationScript("${ {\n"
        + "    \"subjectId\": targetEntity.getSubjectId(),\n"
        + "    \"name\": targetEntity.name,\n"
        + "    \"email\": targetEntity.email\n"
        + "  }\n"
        + "}");
    ObjectNode objectNode = entity().toJson(grouperMessagingConfiguration, "MEMBER_ADD");
    assertEquals("subj1", GrouperUtil.jsonJacksonGetString(objectNode, "subjectId"));
    assertEquals("subj1@example.edu", GrouperUtil.jsonJacksonGetString(objectNode, "email"));
    // name is not set on the entity so it is left out, like the standard format leaves out empty values
    assertEquals(2, GrouperUtil.jsonJacksonFieldNames(objectNode).size());
  }

  public void testEntityNoScriptUsesEsbEventJson() {
    ObjectNode objectNode = entity().toJson(configuration(), "MEMBER_ADD");
    assertEquals("entityId1", GrouperUtil.jsonJacksonGetString(objectNode, "id"));
    assertEquals("subj1", GrouperUtil.jsonJacksonGetString(objectNode, "subjectId"));
  }

  public void testScriptMustReturnMap() {
    GrouperMessagingConfiguration grouperMessagingConfiguration = configuration();
    grouperMessagingConfiguration.setEntityFormatTranslationScript("${targetEntity.getSubjectId()}");
    try {
      entity().toJson(grouperMessagingConfiguration, "MEMBER_ADD");
      fail("Expecting exception");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("must return a map"));
    }
  }

  public void testGroupScript() {
    ProvisioningGroup provisioningGroup = new ProvisioningGroup();
    provisioningGroup.setId("groupId1");
    provisioningGroup.setName("test:group1");
    GrouperMessagingConfiguration grouperMessagingConfiguration = configuration();
    grouperMessagingConfiguration.setGroupFormatTranslationScript("${ {\"name\": targetGroup.name, \"count\": 2} }");
    ObjectNode objectNode = GrouperMessagingGroup.fromProvisioningGroup(provisioningGroup).toJson(grouperMessagingConfiguration, "GROUP_ADD");
    assertEquals("test:group1", GrouperUtil.jsonJacksonGetString(objectNode, "name"));
    assertEquals(Integer.valueOf(2), GrouperUtil.jsonJacksonGetInteger(objectNode, "count"));
  }

  /**
   * the script can vary its output by the event type being sent
   */
  public void testEventTypeVariable() {
    GrouperMessagingConfiguration grouperMessagingConfiguration = configuration();
    grouperMessagingConfiguration.setEntityFormatTranslationScript("${ eventType == 'MEMBER_DELETE' ? {\"subjectId\": targetEntity.getSubjectId()} : "
        + "{\"subjectId\": targetEntity.getSubjectId(), \"email\": targetEntity.email, \"event\": eventType} }");
    
    ObjectNode addNode = entity().toJson(grouperMessagingConfiguration, "MEMBER_ADD");
    assertEquals(3, GrouperUtil.jsonJacksonFieldNames(addNode).size());
    assertEquals("MEMBER_ADD", GrouperUtil.jsonJacksonGetString(addNode, "event"));
    
    ObjectNode deleteNode = entity().toJson(grouperMessagingConfiguration, "MEMBER_DELETE");
    assertEquals(1, GrouperUtil.jsonJacksonFieldNames(deleteNode).size());
  }
}
