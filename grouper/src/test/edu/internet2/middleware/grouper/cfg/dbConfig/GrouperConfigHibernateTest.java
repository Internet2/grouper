/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITGrouperConfigHibernate;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.lang3.RandomStringUtils;
import junit.textui.TestRunner;


/**
 *
 */
public class GrouperConfigHibernateTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperConfigHibernateTest("testEscapeDollar"));
  }
  
  @Override
  protected void setupConfigs() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("audit.maxLengthTruncateTextFieldsIndexed", "2675");
  }

  /**
   * @param name
   */
  public GrouperConfigHibernateTest(String name) {
    super(name);
  }
  
  public void testEscapeDollar() {

    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigComment("comment");
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileName(ConfigFileName.GROUPER_PROPERTIES);
    grouperConfigHibernate.setConfigKey("someKey");
    
//    value = GrouperClientUtils.replace(value, "U+0024", "$");
//    value = GrouperClientUtils.replace(value, "U+0020", " ");
//    value = GrouperClientUtils.replace(value, "U+007B", "{");
//    value = GrouperClientUtils.replace(value, "U+007D", "}");
//    value = GrouperClientUtils.replace(value, "U+000A", "\n");
//    value = GrouperClientUtils.replace(value, "U+002B", "+");

    
    grouperConfigHibernate.setValueToSave("U+0024U+0020U+007BU+007DU+000AU+002B");
    grouperConfigHibernate.saveOrUpdate(true);

    GrouperConfigHibernate grouperConfigHibernate2 = GrouperDAOFactory.getFactory().getConfig().findById(grouperConfigHibernate.getId(), true);
    
    assertEquals("U+0024U+0020U+007BU+007DU+000AU+002B", grouperConfigHibernate2.retrieveValue());
    assertEquals("U+0024U+0020U+007BU+007DU+000AU+002B", grouperConfigHibernate2.getConfigValueDb());
    
    String value = GrouperConfig.retrieveConfig().propertyValueString("someKey");
    assertEquals("$ {}\n+", value);
  }
  
  public void testSaveLessThan3000Value() {
    Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, "some.key");

    for (GrouperConfigHibernate grouperConfigHibernate : grouperConfigHibernates) {
      grouperConfigHibernate.delete();
    }
        
    GrouperConfigHibernate grouperConfigHibernate2 = null;
    
    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigComment("comment");
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileName(ConfigFileName.GROUPER_PROPERTIES);
    grouperConfigHibernate.setConfigKey("some.key");
    
    String randomValue = RandomStringUtils.randomAscii(2999);
    
    grouperConfigHibernate.setValueToSave(randomValue);
    grouperConfigHibernate.saveOrUpdate(true);
    
    grouperConfigHibernate2 = GrouperDAOFactory.getFactory().getConfig().findById(grouperConfigHibernate.getId(), true);
    
    assertEquals(randomValue, grouperConfigHibernate2.retrieveValue());
    assertEquals(randomValue, grouperConfigHibernate2.getConfigValueDb());
    assertEquals(2999L, grouperConfigHibernate2.getConfigValueBytes().longValue());
    
    PITGrouperConfigHibernate pitGrouperConfigHibernate = GrouperDAOFactory.getFactory().getPITConfig().findBySourceIdActive(grouperConfigHibernate.getId(), true);
    
    assertEquals(grouperConfigHibernate.getId(), pitGrouperConfigHibernate.getSourceId());
    assertEquals(grouperConfigHibernate.getConfigComment(), pitGrouperConfigHibernate.getConfigComment());
    assertEquals(grouperConfigHibernate.retrieveValue(), pitGrouperConfigHibernate.getValue());
    assertEquals(grouperConfigHibernate.getConfigKey(), pitGrouperConfigHibernate.getConfigKey());
    assertEquals(grouperConfigHibernate.getConfigEncryptedDb(), pitGrouperConfigHibernate.getConfigEncryptedDb());
    assertEquals(grouperConfigHibernate.getConfigFileNameDb(), pitGrouperConfigHibernate.getConfigFileNameDb());
    assertEquals(grouperConfigHibernate.getConfigSequence(), pitGrouperConfigHibernate.getConfigSequence());
    assertEquals(grouperConfigHibernate.getConfigValueBytes(), pitGrouperConfigHibernate.getConfigValueBytes());
    assertEquals("T", pitGrouperConfigHibernate.getActiveDb());
    assertNull(pitGrouperConfigHibernate.getEndTime());
    assertNotNull(pitGrouperConfigHibernate.getStartTime());
    
    assertEquals(randomValue, GrouperConfig.retrieveConfig().propertyValueString("some.key"));
  }
  
  public void testSaveMoreThan3000Value() {
    
    Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, "some.key");

    for (GrouperConfigHibernate grouperConfigHibernate : grouperConfigHibernates) {
      grouperConfigHibernate.delete();
    }
        
    GrouperConfigHibernate grouperConfigHibernate2 = null;
    
    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigComment("comment");
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileName(ConfigFileName.GROUPER_PROPERTIES);
    grouperConfigHibernate.setConfigKey("some.key.1");
    
    String randomValue = RandomStringUtils.randomAscii(3001);
    
    grouperConfigHibernate.setValueToSave(randomValue);
    grouperConfigHibernate.saveOrUpdate(true);
    
    grouperConfigHibernate2 = GrouperDAOFactory.getFactory().getConfig().findById(grouperConfigHibernate.getId(), true);
    
    assertEquals(randomValue, grouperConfigHibernate2.retrieveValue());
    assertEquals(randomValue, grouperConfigHibernate2.getConfigValueClobDb());
    assertEquals(3001L, grouperConfigHibernate2.getConfigValueBytes().longValue());
    assertEquals(randomValue, GrouperConfig.retrieveConfig().propertyValueString("some.key.1"));
    
  }

  /**
   * Test method for {@link edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate#saveOrUpdate(boolean addNew)}.
   */
  public void testSaveOrUpdate() {
    
    Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, "some.key");

    for (GrouperConfigHibernate grouperConfigHibernate : grouperConfigHibernates) {
      grouperConfigHibernate.delete();
    }
        
    String id = null;
    
    GrouperConfigHibernate grouperConfigHibernate2 = null;
    
    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigComment("comment");
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.ENVIRONMENT);
    grouperConfigHibernate.setConfigFileName(ConfigFileName.GROUPER_PROPERTIES);
    grouperConfigHibernate.setConfigKey("some.key");
    grouperConfigHibernate.setValueToSave("theValue");
    grouperConfigHibernate.saveOrUpdate(true);
    
    grouperConfigHibernate2 = GrouperDAOFactory.getFactory().getConfig().findById(grouperConfigHibernate.getId(), true);
    id = grouperConfigHibernate2.getId();
    
    assertEquals(grouperConfigHibernate.getConfigComment(), grouperConfigHibernate2.getConfigComment());
    assertEquals(grouperConfigHibernate.getConfigEncryptedDb(), grouperConfigHibernate2.getConfigEncryptedDb());
    assertEquals(grouperConfigHibernate.getConfigFileHierarchyDb(), grouperConfigHibernate2.getConfigFileHierarchyDb());
    assertEquals(grouperConfigHibernate.getConfigKey(), grouperConfigHibernate2.getConfigKey());
    assertEquals(grouperConfigHibernate.getConfigSequence(), grouperConfigHibernate2.getConfigSequence());
    assertEquals(grouperConfigHibernate.retrieveValue(), grouperConfigHibernate2.retrieveValue());
    assertEquals(grouperConfigHibernate.getConfigVersionIndex(), grouperConfigHibernate2.getConfigVersionIndex());
    assertEquals(grouperConfigHibernate.getId(), grouperConfigHibernate2.getId());
    assertNotNull(grouperConfigHibernate.getId());
    assertEquals(grouperConfigHibernate.getLastUpdated(), grouperConfigHibernate2.getLastUpdated());
    assertNotNull(grouperConfigHibernate.getLastUpdated());

    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, grouperConfigHibernate.getConfigKey());

    assertEquals(1, GrouperUtil.length(grouperConfigHibernates));

    grouperConfigHibernate2 = grouperConfigHibernates.iterator().next();
    
    assertEquals(grouperConfigHibernate.getConfigComment(), grouperConfigHibernate2.getConfigComment());
    assertEquals(grouperConfigHibernate.getConfigEncryptedDb(), grouperConfigHibernate2.getConfigEncryptedDb());
    assertEquals(grouperConfigHibernate.getConfigFileHierarchyDb(), grouperConfigHibernate2.getConfigFileHierarchyDb());
    assertEquals(grouperConfigHibernate.getConfigKey(), grouperConfigHibernate2.getConfigKey());
    assertEquals(grouperConfigHibernate.getConfigSequence(), grouperConfigHibernate2.getConfigSequence());
    assertEquals(grouperConfigHibernate.retrieveValue(), grouperConfigHibernate2.retrieveValue());
    assertEquals(grouperConfigHibernate.getConfigVersionIndex(), grouperConfigHibernate2.getConfigVersionIndex());
    assertEquals(grouperConfigHibernate.getId(), grouperConfigHibernate2.getId());
    assertNotNull(grouperConfigHibernate.getId());
    assertEquals(grouperConfigHibernate.getLastUpdated(), grouperConfigHibernate2.getLastUpdated());
    assertNotNull(grouperConfigHibernate.getLastUpdated());

    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, null);
    
    assertTrue(GrouperUtil.length(grouperConfigHibernates) > 0);

    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(null, new Timestamp(0), null);

    assertTrue(GrouperUtil.length(grouperConfigHibernates) > 0);

    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, new Timestamp(0), null);

    assertTrue(GrouperUtil.length(grouperConfigHibernates) > 0);

    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, new Timestamp(0), grouperConfigHibernate.getConfigKey());

    assertEquals(1, GrouperUtil.length(grouperConfigHibernates));

    GrouperUtil.sleep(1000);

    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, new Timestamp(System.currentTimeMillis()), grouperConfigHibernate.getConfigKey());

    assertEquals(0, GrouperUtil.length(grouperConfigHibernates));

    String longValue = RandomStringUtils.randomAlphanumeric(3500);
    grouperConfigHibernate.setValueToSave(longValue);
    grouperConfigHibernate.saveOrUpdate(false);
    
    grouperConfigHibernate2 = GrouperDAOFactory.getFactory().getConfig().findById(grouperConfigHibernate.getId(), true);
    
    assertEquals(grouperConfigHibernate.getConfigComment(), grouperConfigHibernate2.getConfigComment());
    assertEquals(grouperConfigHibernate.getConfigEncryptedDb(), grouperConfigHibernate2.getConfigEncryptedDb());
    assertEquals(grouperConfigHibernate.getConfigFileHierarchyDb(), grouperConfigHibernate2.getConfigFileHierarchyDb());
    assertEquals(grouperConfigHibernate.getConfigKey(), grouperConfigHibernate2.getConfigKey());
    assertEquals(grouperConfigHibernate.getConfigSequence(), grouperConfigHibernate2.getConfigSequence());
    assertEquals(grouperConfigHibernate.retrieveValue(), grouperConfigHibernate2.retrieveValue());
    assertEquals(grouperConfigHibernate.getConfigVersionIndex(), grouperConfigHibernate2.getConfigVersionIndex());
    assertEquals(grouperConfigHibernate.getId(), grouperConfigHibernate2.getId());
    assertNotNull(grouperConfigHibernate.getId());
    assertEquals(grouperConfigHibernate.getLastUpdated(), grouperConfigHibernate2.getLastUpdated());
    assertNotNull(grouperConfigHibernate.getLastUpdated());
    
    grouperConfigHibernate.delete();
    
    grouperConfigHibernate2 = GrouperDAOFactory.getFactory().getConfig().findById(grouperConfigHibernate.getId(), false);
    
    assertNull(grouperConfigHibernate2);
    
    Set<PITGrouperConfigHibernate> pitGrouperConfigHibernates = GrouperDAOFactory.getFactory().getPITConfig().findBySourceId(grouperConfigHibernate.getId(), true);
    
    assertEquals(3, GrouperUtil.length(pitGrouperConfigHibernates));
    
    List<PITGrouperConfigHibernate> pits = new ArrayList<PITGrouperConfigHibernate>(pitGrouperConfigHibernates);
    
    assertEquals(id, pits.get(0).getSourceId());
    assertEquals(id, pits.get(1).getSourceId());
    assertEquals(id, pits.get(2).getSourceId());
    
    assertEquals("F", pits.get(0).getActiveDb());
    assertEquals("F", pits.get(1).getActiveDb());
    assertEquals("F", pits.get(1).getActiveDb());
    
    Collections.sort(pits, new Comparator<PITGrouperConfigHibernate>() {

      @Override
      public int compare(PITGrouperConfigHibernate o1, PITGrouperConfigHibernate o2) {
        return o1.getStartTimeDb() < o2.getStartTimeDb() ? -1 : 1;
      }
    });
    
    assertEquals("theValue", pits.get(0).getValue());
    assertNull(pits.get(0).getPreviousConfigValueDb());
    assertNull(pits.get(0).getPreviousConfigValueClobDb());
    
    assertEquals(longValue, pits.get(1).getValue());
    assertEquals("theValue", pits.get(1).getPreviousConfigValueDb());
    assertNull(pits.get(1).getPreviousConfigValueClobDb());
    
    assertNull(pits.get(2).getValue());
    assertEquals(longValue, pits.get(2).getPreviousConfigValueClobDb());
    assertNull(pits.get(2).getPreviousConfigValueDb());
    
  }
  
  public void testCanNotRevertIfSameConfigKeyAppearsMultipleTimes() {
    Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, "some.key");

    for (GrouperConfigHibernate grouperConfigHibernate : grouperConfigHibernates) {
      grouperConfigHibernate.delete();
    }
    
    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigComment("comment");
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileName(ConfigFileName.GROUPER_PROPERTIES);
    grouperConfigHibernate.setConfigKey("some.key");
    grouperConfigHibernate.setValueToSave("theValue");
    grouperConfigHibernate.saveOrUpdate(true);
    
    grouperConfigHibernate.setValueToSave("newValue");
    grouperConfigHibernate.saveOrUpdate(true);
    
    Set<PITGrouperConfigHibernate> pitGrouperConfigHibernates = GrouperDAOFactory.getFactory().getPITConfig().findBySourceId(grouperConfigHibernate.getId(), true);
    
    assertEquals(2, GrouperUtil.length(pitGrouperConfigHibernates));
    
    Iterator<PITGrouperConfigHibernate> ir = pitGrouperConfigHibernates.iterator();
    
    Set<String> pitIds = GrouperUtil.toSet(ir.next().getId(), ir.next().getId());
    
    StringBuilder message = new StringBuilder();
    List<String> errorsToDisplay = new ArrayList<String>();
    Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
    
    // when
    GrouperDAOFactory.getFactory().getPITConfig().revertConfigs(pitIds, message, errorsToDisplay, validationErrorsToDisplay);
    
    // Then
    assertEquals(1, errorsToDisplay.size());
    assertTrue(errorsToDisplay.get(0).contains("some.key"));
    
  }
  
  public void testRevertConfigs_GoToDeletedValue() {
    
    Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, "some.key");

    for (GrouperConfigHibernate grouperConfigHibernate : grouperConfigHibernates) {
      grouperConfigHibernate.delete();
    }
        
    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigComment("comment");
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileName(ConfigFileName.GROUPER_PROPERTIES);
    grouperConfigHibernate.setConfigKey("some.key");
    grouperConfigHibernate.setValueToSave("theValue");
    grouperConfigHibernate.saveOrUpdate(true);
    
    Set<PITGrouperConfigHibernate> pitGrouperConfigHibernates = GrouperDAOFactory.getFactory().getPITConfig().findBySourceId(grouperConfigHibernate.getId(), true);
    
    assertEquals(1, GrouperUtil.length(pitGrouperConfigHibernates));
    
    StringBuilder message = new StringBuilder();
    List<String> errorsToDisplay = new ArrayList<String>();
    Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
    
    // when
    GrouperDAOFactory.getFactory().getPITConfig().revertConfigs(GrouperUtil.toSet(pitGrouperConfigHibernates.iterator().next().getId()), message, 
        errorsToDisplay, validationErrorsToDisplay);
    
    // then
    pitGrouperConfigHibernates = GrouperDAOFactory.getFactory().getPITConfig().findBySourceId(grouperConfigHibernate.getId(), true);
    assertEquals(2, GrouperUtil.length(pitGrouperConfigHibernates));
    
    List<PITGrouperConfigHibernate> pits = new ArrayList<PITGrouperConfigHibernate>(pitGrouperConfigHibernates);
    
    Collections.sort(pits, new Comparator<PITGrouperConfigHibernate>() {

      @Override
      public int compare(PITGrouperConfigHibernate o1, PITGrouperConfigHibernate o2) {
        return o1.getStartTimeDb() < o2.getStartTimeDb() ? -1 : 1;
      }
    });
    
    assertEquals("theValue", pits.get(0).getValue());
    assertNull(pits.get(0).getPreviousConfigValueDb());
    assertNull(pits.get(0).getPreviousConfigValueClobDb());
    
    assertNull(pits.get(1).getValue());
    assertEquals("theValue", pits.get(1).getPreviousConfigValueDb());
    assertNull(pits.get(1).getPreviousConfigValueClobDb());
    
  }
  
  public void testRevertConfigs_GoToEditedValue() {
    
    Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, "some.key");

    for (GrouperConfigHibernate grouperConfigHibernate : grouperConfigHibernates) {
      grouperConfigHibernate.delete();
    }
        
    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigComment("comment");
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileName(ConfigFileName.GROUPER_PROPERTIES);
    grouperConfigHibernate.setConfigKey("some.key");
    grouperConfigHibernate.setValueToSave("theValue");
    grouperConfigHibernate.saveOrUpdate(true);
    
    String longValue = RandomStringUtils.randomAlphanumeric(3500);
    grouperConfigHibernate.setValueToSave(longValue);
    grouperConfigHibernate.saveOrUpdate(false);
    
    Set<PITGrouperConfigHibernate> pitGrouperConfigHibernates = GrouperDAOFactory.getFactory().getPITConfig().findBySourceId(grouperConfigHibernate.getId(), true);
    
    assertEquals(2, GrouperUtil.length(pitGrouperConfigHibernates));
    
    StringBuilder message = new StringBuilder();
    List<String> errorsToDisplay = new ArrayList<String>();
    Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
    
    PITGrouperConfigHibernate oneWithOldValue = null;
    for (PITGrouperConfigHibernate pitConfig: pitGrouperConfigHibernates) {
      if (pitConfig.getValue().equals("theValue")) {
        oneWithOldValue = pitConfig;
      }
    }
    
    // when
    GrouperDAOFactory.getFactory().getPITConfig().revertConfigs(GrouperUtil.toSet(oneWithOldValue.getId()), message, 
        errorsToDisplay, validationErrorsToDisplay);
    
    // then
    pitGrouperConfigHibernates = GrouperDAOFactory.getFactory().getPITConfig().findBySourceId(grouperConfigHibernate.getId(), true);
    assertEquals(3, GrouperUtil.length(pitGrouperConfigHibernates));
    
    List<PITGrouperConfigHibernate> pits = new ArrayList<PITGrouperConfigHibernate>(pitGrouperConfigHibernates);
    
    Collections.sort(pits, new Comparator<PITGrouperConfigHibernate>() {

      @Override
      public int compare(PITGrouperConfigHibernate o1, PITGrouperConfigHibernate o2) {
        return o1.getStartTimeDb() < o2.getStartTimeDb() ? -1 : 1;
      }
    });
    
    assertEquals("theValue", pits.get(0).getValue());
    assertNull(pits.get(0).getPreviousConfigValueDb());
    assertNull(pits.get(0).getPreviousConfigValueClobDb());
    
    assertEquals(longValue, pits.get(1).getValue());
    assertEquals("theValue", pits.get(1).getPreviousConfigValueDb());
    assertNull(pits.get(1).getPreviousConfigValueClobDb());
    
    assertNull(pits.get(2).getValue());
    assertEquals(longValue, pits.get(2).getPreviousConfigValueClobDb());
    assertNull(pits.get(2).getPreviousConfigValueDb());
    
  }

}
