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

import edu.internet2.middleware.grouper.cache.GrouperCacheDatabase;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITGrouperConfigHibernate;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.db.ConfigDatabaseLogic;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
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
    TestRunner.run(new GrouperConfigHibernateTest("testDeleteCommittedBeforeCacheNotification"));
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

  // ---------------------------------------------------------------------------------------------
  // GRP-7395 / GRP-7346: cross JVM config cache notifications.
  //
  // A config change on one JVM writes a row to grouper_cache_instance (scoped to the config file,
  // e.g. "...databaseConfigs____grouper.properties") and bumps grouper_cache_overall. Every other
  // JVM polls those tables and drops its in memory config when a row moved. Two things have to
  // hold for that to work:
  //
  //   sending side:   the config change is committed BEFORE the notification is visible, or a
  //                   JVM that polls in between reloads the old value and marks it as seen
  //   receiving side: a poll that sees the notification actually drops the right config
  //
  // The sending tests read on their own GcDbAccess connection, which is what another JVM sees.
  // The receiving tests play the other JVM: they change grouper_config with raw SQL (so this JVM's
  // cache is not cleared as a side effect), notify with updateLastUpdatedNanos=false (so this JVM
  // does not treat the notification as its own), and run one poll directly instead of sleeping.
  // ---------------------------------------------------------------------------------------------

  /** every test key starts with this so tearDown can remove them */
  private static final String TEST_KEY_PREFIX = "zzzGrp7395";

  @Override
  protected void tearDown() {
    // raw SQL, since some tests leave this JVM's view of the rows deliberately stale
    new GcDbAccess().sql("delete from grouper_config where config_key like ?").addBindVar(TEST_KEY_PREFIX + "%").executeSql();
    GrouperConfigHibernate.clearConfigsInMemory();
    super.tearDown();
  }

  /**
   * GRP-7395: a delete inside a caller's transaction (like the UI delete in DbConfigEngine) must be
   * committed before other JVMs are notified. This is the prod bug: the delete joined the caller's
   * transaction, the notification committed on its own connection, and a WS node that polled before
   * the caller committed reloaded the deleted value and kept it.
   */
  public void testDeleteCommittedBeforeCacheNotification() {

    final GrouperConfigHibernate grouperConfigHibernate = saveTestConfig(ConfigFileName.GROUPER_PROPERTIES, TEST_KEY_PREFIX + "Delete.value", "before");
    final String cacheName = scopedCacheName(ConfigFileName.GROUPER_PROPERTIES);
    final Long nanosBefore = cacheInstanceNanos(cacheName);
    assertNotNull(nanosBefore);

    // simulate the caller's transaction, like DbConfigEngine does around the UI delete
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_AUDIT, new HibernateHandler() {

      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {

        grouperConfigHibernate.delete();

        // the notification has gone out...
        assertNotified("delete should notify other JVMs", cacheName, nanosBefore);

        // ...so the delete must already be visible, or that JVM would reload the old value
        assertEquals("delete must be committed before other JVMs are notified", 0, configRowCount(grouperConfigHibernate.getId()));
        return null;
      }
    });
  }

  /**
   * GRP-7395: same guarantee for an update inside a caller's transaction (e.g. the UI edit or an
   * import). saveOrUpdate already used its own transaction; this keeps it that way.
   */
  public void testUpdateCommittedBeforeCacheNotification() {

    final GrouperConfigHibernate grouperConfigHibernate = saveTestConfig(ConfigFileName.GROUPER_PROPERTIES, TEST_KEY_PREFIX + "Update.value", "before");
    final String cacheName = scopedCacheName(ConfigFileName.GROUPER_PROPERTIES);
    final Long nanosBefore = cacheInstanceNanos(cacheName);

    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_AUDIT, new HibernateHandler() {

      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {

        grouperConfigHibernate.setValueToSave("after");
        grouperConfigHibernate.saveOrUpdate(false);

        assertNotified("update should notify other JVMs", cacheName, nanosBefore);
        assertEquals("update must be committed before other JVMs are notified", "after", configRowValue(grouperConfigHibernate.getId()));
        return null;
      }
    });
  }

  /**
   * GRP-7395: same guarantee for an insert inside a caller's transaction.
   */
  public void testInsertCommittedBeforeCacheNotification() {

    final String cacheName = scopedCacheName(ConfigFileName.GROUPER_PROPERTIES);
    final Long nanosBefore = cacheInstanceNanos(cacheName);
    final GrouperConfigHibernate[] inserted = new GrouperConfigHibernate[1];

    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_AUDIT, new HibernateHandler() {

      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {

        inserted[0] = saveTestConfig(ConfigFileName.GROUPER_PROPERTIES, TEST_KEY_PREFIX + "Insert.value", "inserted");

        assertNotified("insert should notify other JVMs", cacheName, nanosBefore);
        assertEquals("insert must be committed before other JVMs are notified", "inserted", configRowValue(inserted[0].getId()));
        return null;
      }
    });
  }

  /**
   * GRP-7395: the trade off of the fix, written down so it is a decision and not a surprise. Since
   * delete commits on its own (like saveOrUpdate always has), a caller's transaction that fails
   * afterwards does not bring the deleted row back.
   */
  public void testDeleteNotRolledBackWithCallerTransaction() {

    final GrouperConfigHibernate grouperConfigHibernate = saveTestConfig(ConfigFileName.GROUPER_PROPERTIES, TEST_KEY_PREFIX + "Rollback.value", "before");

    try {
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_AUDIT, new HibernateHandler() {

        @Override
        public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
          grouperConfigHibernate.delete();
          throw new RuntimeException("caller fails after the delete");
        }
      });
      fail("the caller's transaction should have thrown");
    } catch (RuntimeException re) {
      assertTrue(GrouperUtil.getFullStackTrace(re), GrouperUtil.getFullStackTrace(re).contains("caller fails after the delete"));
    }

    assertEquals(0, configRowCount(grouperConfigHibernate.getId()));
  }

  /**
   * GRP-7346: a change to one config file notifies under that file's name only, so the other JVMs
   * do not throw away the config for files that did not change.
   */
  public void testDeleteNotifiesOnlyItsOwnConfigFile() {

    GrouperConfigHibernate grouperConfigHibernate = saveTestConfig(ConfigFileName.GROUPER_LOADER_PROPERTIES, TEST_KEY_PREFIX + "Loader.value", "before");

    String loaderCacheName = scopedCacheName(ConfigFileName.GROUPER_LOADER_PROPERTIES);
    String grouperCacheName = scopedCacheName(ConfigFileName.GROUPER_PROPERTIES);
    String unscopedCacheName = unscopedCacheName();

    Long loaderNanosBefore = cacheInstanceNanos(loaderCacheName);
    Long grouperNanosBefore = cacheInstanceNanos(grouperCacheName);
    Long unscopedNanosBefore = cacheInstanceNanos(unscopedCacheName);

    grouperConfigHibernate.delete();

    assertNotified("delete should notify its own config file", loaderCacheName, loaderNanosBefore);
    assertEquals("delete should not notify other config files", grouperNanosBefore, cacheInstanceNanos(grouperCacheName));
    assertEquals("delete should not send an all config notification", unscopedNanosBefore, cacheInstanceNanos(unscopedCacheName));
  }

  /**
   * the JVM that makes the change sees it right away, without waiting for a poll
   */
  public void testDeleteClearsLocalConfigImmediately() {

    String configKey = TEST_KEY_PREFIX + "Local.value";
    GrouperConfigHibernate grouperConfigHibernate = saveTestConfig(ConfigFileName.GROUPER_PROPERTIES, configKey, "before");
    assertEquals("before", GrouperConfig.retrieveConfig().propertyValueString(configKey));

    grouperConfigHibernate.delete();

    assertNull(GrouperConfig.retrieveConfig().propertyValueString(configKey));
  }

  /**
   * another JVM updates a grouper.properties key and sends a scoped notification; one poll here
   * picks up the new value
   */
  public void testOtherJvmScopedUpdatePickedUp() {

    String configKey = TEST_KEY_PREFIX + "RemoteUpdate.value";
    GrouperConfigHibernate grouperConfigHibernate = saveTestConfigAndPrimePoll(configKey, "before");

    updateConfigRowAsOtherJvm(grouperConfigHibernate.getId(), "after");
    assertEquals("value should still be cached before the notification", "before", GrouperConfig.retrieveConfig().propertyValueString(configKey));

    notifyAsOtherJvm(scopedCacheName(ConfigFileName.GROUPER_PROPERTIES));
    GrouperCacheDatabase.retrieveIncremental();

    assertEquals("after", GrouperConfig.retrieveConfig().propertyValueString(configKey));
  }

  /**
   * GRP-7395: the prod case from the receiving side. Another JVM deletes a grouper.properties key
   * and sends a scoped notification; one poll here drops the value
   */
  public void testOtherJvmScopedDeletePickedUp() {

    String configKey = TEST_KEY_PREFIX + "RemoteDelete.value";
    GrouperConfigHibernate grouperConfigHibernate = saveTestConfigAndPrimePoll(configKey, "true");

    new GcDbAccess().sql("delete from grouper_config where id = ?").addBindVar(grouperConfigHibernate.getId()).executeSql();
    assertEquals("value should still be cached before the notification", "true", GrouperConfig.retrieveConfig().propertyValueString(configKey));

    notifyAsOtherJvm(scopedCacheName(ConfigFileName.GROUPER_PROPERTIES));
    GrouperCacheDatabase.retrieveIncremental();

    assertNull(GrouperConfig.retrieveConfig().propertyValueString(configKey));
  }

  /**
   * an unscoped (all config) notification from an older node, or from updateLastUpdated() with no
   * file, still drops everything
   */
  public void testOtherJvmUnscopedNotificationPickedUp() {

    String configKey = TEST_KEY_PREFIX + "RemoteUnscoped.value";
    GrouperConfigHibernate grouperConfigHibernate = saveTestConfigAndPrimePoll(configKey, "before");

    updateConfigRowAsOtherJvm(grouperConfigHibernate.getId(), "after");

    notifyAsOtherJvm(unscopedCacheName());
    GrouperCacheDatabase.retrieveIncremental();

    assertEquals("after", GrouperConfig.retrieveConfig().propertyValueString(configKey));
  }

  /**
   * GRP-7346: a scoped notification for a different config file leaves grouper.properties alone.
   * Then the notification for the right file picks the change up, which shows it was only the
   * scoping that kept the old value.
   */
  public void testOtherJvmScopedNotificationForOtherFileKeepsConfig() {

    String configKey = TEST_KEY_PREFIX + "RemoteOtherFile.value";
    GrouperConfigHibernate grouperConfigHibernate = saveTestConfigAndPrimePoll(configKey, "before");

    updateConfigRowAsOtherJvm(grouperConfigHibernate.getId(), "after");

    notifyAsOtherJvm(scopedCacheName(ConfigFileName.GROUPER_LOADER_PROPERTIES));
    GrouperCacheDatabase.retrieveIncremental();
    assertEquals("a grouper-loader.properties notification should not reload grouper.properties",
        "before", GrouperConfig.retrieveConfig().propertyValueString(configKey));

    notifyAsOtherJvm(scopedCacheName(ConfigFileName.GROUPER_PROPERTIES));
    GrouperCacheDatabase.retrieveIncremental();
    assertEquals("after", GrouperConfig.retrieveConfig().propertyValueString(configKey));
  }

  /**
   * save a test key through the normal API so the row has every column it needs
   * @param configFileName file the key goes in
   * @param configKey must start with TEST_KEY_PREFIX so tearDown removes it
   * @param value value to save
   * @return the saved object
   */
  private static GrouperConfigHibernate saveTestConfig(ConfigFileName configFileName, String configKey, String value) {
    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileName(configFileName);
    grouperConfigHibernate.setConfigKey(configKey);
    grouperConfigHibernate.setValueToSave(value);
    grouperConfigHibernate.saveOrUpdate(true);
    return grouperConfigHibernate;
  }

  /**
   * save a grouper.properties test key, load it into this JVM's config, and bring the poll state up
   * to date with the database, so the next poll only reacts to what the test does afterwards
   * @param configKey must start with TEST_KEY_PREFIX
   * @param value value to save
   * @return the saved object
   */
  private static GrouperConfigHibernate saveTestConfigAndPrimePoll(String configKey, String value) {
    GrouperConfigHibernate grouperConfigHibernate = saveTestConfig(ConfigFileName.GROUPER_PROPERTIES, configKey, value);
    assertEquals(value, GrouperConfig.retrieveConfig().propertyValueString(configKey));
    // forStartup=true records every row's timestamp without clearing anything
    GrouperCacheDatabase.retrieveFull(true);
    return grouperConfigHibernate;
  }

  /**
   * change a row the way another JVM would, without touching this JVM's cache
   * @param id grouper_config id
   * @param value new value
   */
  private static void updateConfigRowAsOtherJvm(String id, String value) {
    new GcDbAccess().sql("update grouper_config set config_value = ?, last_updated = ? where id = ?")
        .addBindVar(value).addBindVar(System.currentTimeMillis()).addBindVar(id).executeSql();
  }

  /**
   * notify the way another JVM would: false so this JVM does not record it as already seen
   * @param cacheName full cache name
   */
  private static void notifyAsOtherJvm(String cacheName) {
    GrouperCacheDatabase.notifyDatabaseOfCacheUpdate(cacheName, false);
  }

  /**
   * @param configFileName config file
   * @return the full cache name a change to that file notifies under
   */
  private static String scopedCacheName(ConfigFileName configFileName) {
    return unscopedCacheName() + "____" + configFileName.getConfigFileName();
  }

  /**
   * @return the full cache name for "all config changed"
   */
  private static String unscopedCacheName() {
    return "custom__" + ConfigDatabaseLogic.DATABASE_CACHE_KEY;
  }

  /**
   * @param cacheName full cache name in grouper_cache_instance
   * @return the notification timestamp, read on its own connection like another JVM would
   */
  private static Long cacheInstanceNanos(String cacheName) {
    return new GcDbAccess().sql("select nanos_since_1970 from grouper_cache_instance where cache_name = ?")
        .addBindVar(cacheName).select(Long.class);
  }

  /**
   * @param message assert message
   * @param cacheName full cache name
   * @param nanosBefore timestamp before the change, or null if the row did not exist
   */
  private static void assertNotified(String message, String cacheName, Long nanosBefore) {
    Long nanosAfter = cacheInstanceNanos(cacheName);
    assertTrue(message + ", before: " + nanosBefore + ", after: " + nanosAfter,
        nanosAfter != null && (nanosBefore == null || nanosAfter > nanosBefore));
  }

  /**
   * @param id grouper_config id
   * @return number of rows with that id, seen from a separate connection
   */
  private static int configRowCount(String id) {
    return new GcDbAccess().sql("select count(1) from grouper_config where id = ?").addBindVar(id).select(int.class);
  }

  /**
   * @param id grouper_config id
   * @return the committed value, seen from a separate connection
   */
  private static String configRowValue(String id) {
    return new GcDbAccess().sql("select config_value from grouper_config where id = ?").addBindVar(id).select(String.class);
  }

}
