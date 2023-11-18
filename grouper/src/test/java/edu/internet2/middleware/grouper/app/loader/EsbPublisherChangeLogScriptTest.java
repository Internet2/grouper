/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvent;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import junit.textui.TestRunner;


/**
 *
 */
public class EsbPublisherChangeLogScriptTest extends GrouperTest {

  /**
   * var for testing
   */
  public static int count = 0;

  /**
   * var for testing
   */
  public static void processEsbEventContainer(EsbEventContainer esbEventContainer) {
    System.out.println(GrouperClientUtils.toStringReflection(esbEventContainer));
  }

  public static void main(String[] args) {
    TestRunner.run(new EsbPublisherChangeLogScriptTest("testEsbScriptIncrementSomething"));
  }
  
  public EsbPublisherChangeLogScriptTest(String name) {
    super(name);
  }
  
  
  public void testEsbScriptIncrementSomething() {
    

    //  #####################################################
    //  ## Change log script daemon
    //  ## "changeLogScriptDaemonConfigKey" is the key of the config, change that for your change log script daemon
    //  #####################################################
    //  
    //  # set this to enable the script daemon
    //  # {valueType: "class", readOnly: true, mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase"}
    //  # changeLog.consumer.changeLogScriptDaemonConfigKey.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
    //  
    //  # cron string
    //  # {valueType: "cron", required: true}
    //  # changeLog.consumer.changeLogScriptDaemonConfigKey.quartzCron = 0 * * * * ?
    //  
    //  # el filter, e.g. event.eventType eq 'GROUP_DELETE' || event.eventType eq 'GROUP_ADD' || event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD'
    //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.elfilter$"}
    //  # changeLog.consumer.changeLogScriptDaemonConfigKey.elfilter = 
    //  
    //  # publishing class
    //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbMessagingPublisher", regex: "^changeLog\\.consumer\\.([^.]+)\\.publisher\\.class$"}
    //  # changeLog.consumer.changeLogScriptDaemonConfigKey.publisher.class = edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript
    //  
    //  # file type, you can run a script in config, or run a file in your container
    //  # {valueType: "string", required: true, regex: "^otherJob\\.([^.]+)\\.changeLogFileType$", formElement: "dropdown", optionValues: ["script", "file"]}
    //  # changeLog.consumer.changeLogScriptDaemonConfigKey.changeLogFileType = 
    //  
    //  # source of script
    //  # {valueType: "string", required: true, regex: "^otherJob\\.([^.]+)\\.changeLogScriptSource$", formElement: "textarea", showEl: "${changeLogFileType == 'script'}"}
    //  # changeLog.consumer.changeLogScriptDaemonConfigKey.changeLogScriptSource = 
    //  
    //  # file name in container to run
    //  # {valueType: "string", required: true, regex: "^otherJob\\.([^.]+)\\.changeLogFileName$", showEl: "${changeLogFileType == 'file'}"}
    //  # changeLog.consumer.changeLogScriptDaemonConfigKey.changeLogFileName = 

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.myChangeLogScript.class").value("edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.myChangeLogScript.quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.myChangeLogScript.elfilter").value("(event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD') &&  (event.groupName =~ '^test\\:.*$')").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.myChangeLogScript.publisher.class").value("edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.myChangeLogScript.changeLogFileType").value("script").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.myChangeLogScript.changeLogScriptSource").value(
        "long lastSequenceProcessed = -1;\n"
        + "for (EsbEventContainer esbEventContainer : gsh_builtin_esbEventContainers) { \n"
        + "  EsbPublisherChangeLogScriptTest.count++; \n"
        + "  EsbEvent esbEvent = esbEventContainer.getEsbEvent(); \n"
        + "  gsh_builtin_debugMap.put(esbEventContainer.getSequenceNumber() + \"_\" + esbEvent.getGroupName(), esbEvent.getSourceId() + \"_\" + esbEvent.getSubjectId()); \n"
        + "  gsh_builtin_hib3GrouperLoaderLog.addInsertCount(1); \n"
        + "  lastSequenceProcessed = esbEventContainer.getSequenceNumber(); \n"
        + "} \n"
        +  "return lastSequenceProcessed;\n").store();

    int originalCount = count;
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_myChangeLogScript");

    assertEquals(originalCount, count);
    
    //  List<EsbEventContainer> gsh_builtin_esbEventContainers = new ArrayList<>();
    //  EsbPublisherChangeLogScript esbPublisherChangeLogScript = new EsbPublisherChangeLogScript();
    //  java.util.HashMap gsh_builtin_debugMap = new HashMap<>();
    //  ChangeLogProcessorMetadata gsh_builtin_changeLogProcessorMetadata = esbPublisherChangeLogScript.getChangeLogProcessorMetadata();
    //  ProvisioningSyncConsumerResult gsh_builtin_provisioningSyncConsumerResult = esbPublisherChangeLogScript.getProvisioningSyncConsumerResult();
    //  GrouperSession gsh_builtin_grouperSession = GrouperSession.startRootSession();
    //  Hib3GrouperLoaderLog gsh_builtin_hib3GrouperLoaderLog = gsh_builtin_changeLogProcessorMetadata.getHib3GrouperLoaderLog();
    //  
    //  long lastSequenceProcessed = -1;
    //  for (EsbEventContainer esbEventContainer : gsh_builtin_esbEventContainers) {
    //    EsbPublisherChangeLogScriptTest.count++;
    //    EsbEvent esbEvent = esbEventContainer.getEsbEvent();
    //    gsh_builtin_debugMap.put(esbEventContainer.getSequenceNumber() + "_" + esbEvent.getGroupName(), esbEvent.getSourceId() + "_" + esbEvent.getSubjectId());
    //    gsh_builtin_hib3GrouperLoaderLog.addInsertCount(1);
    //    EsbPublisherChangeLogScriptTest.processEsbEventContainer(esbEventContainer);
    //    lastSequenceProcessed = esbEventContainer.getSequenceNumber();
    //  }
    //  
    //  return lastSequenceProcessed;
    
    Group group = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_myChangeLogScript");

    assertEquals(originalCount+2, count);

  }



}
