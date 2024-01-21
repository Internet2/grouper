package edu.internet2.middleware.grouper.misc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.examples.GroupUniqueExtensionInFoldersHook;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * 
 * @author mchyzer
 *
 */
@DisallowConcurrentExecution
public class FindBadGroupsUniqueExtensionInFolderDaemon extends OtherJobBase {

  
  /**
   * 
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    Map<String, Object> debugMap = new LinkedHashMap();
    RuntimeException runtimeException = null;
    try {

      Map<String, Set<String>> configIdToSetOfFolderNames = GroupUniqueExtensionInFoldersHook.configIdToSetOfFolderNames();
      
      Set<String> groupNamesWithProblem = new TreeSet<String>();
      
      for (String configIdAndCaseSensitive : configIdToSetOfFolderNames.keySet()) {

        List<String> folderNames = new ArrayList<String>(GrouperUtil.nonNull(configIdToSetOfFolderNames.get(configIdAndCaseSensitive)));
        
        boolean caseSensitive = configIdAndCaseSensitive.endsWith("_true");
        
        GcDbAccess gcDbAccess = new GcDbAccess();

        StringBuilder sql = new StringBuilder("select gg1.name from grouper_groups gg1, grouper_groups gg2 where gg2.id != gg1.id and ");
        
        if (caseSensitive) {
          sql.append(" gg1.extension = gg2.extension ");
        } else {
          sql.append(" lower(gg1.extension) = lower(gg2.extension) ");
        }
        
        if (!folderNames.contains(":%")) {
          sql.append(" and ( ");
          boolean first = true;
          for (String folderName : folderNames) {
            if (!first) {
              sql.append(" or ");
            }
            sql.append(" gg1.name like ? ");
            gcDbAccess.addBindVar(folderName);
            first = false;
          }
          sql.append(" ) ");
          sql.append(" and ( ");
          first = true;
          for (String folderName : folderNames) {
            if (!first) {
              sql.append(" or ");
            }
            sql.append(" gg2.name like ? ");
            gcDbAccess.addBindVar(folderName);
            first = false;
          }
          sql.append(" ) ");
        }
        
        gcDbAccess.sql(sql.toString());
        groupNamesWithProblem.addAll(GrouperUtil.nonNull(gcDbAccess.selectList(String.class)));
      }
      debugMap.put("errors", GrouperUtil.length(groupNamesWithProblem));
      otherJobInput.getHib3GrouperLoaderLog().addTotalCount(GrouperUtil.length(groupNamesWithProblem));
      if (GrouperUtil.length(groupNamesWithProblem) == 0) {
        return null;
      }

      String groupNamesString = GrouperUtil.toStringForLog(groupNamesWithProblem, 2000);
      debugMap.put("errorGroups", groupNamesString);

      String mailTo = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob.findBadGroupsUniqueExtensionInFolder.mailToOnError");
      if (!StringUtils.isBlank(mailTo)) {
        new GrouperEmail().setTo(mailTo).setSubject(GrouperTextContainer.textOrNull("group.unique.extension.in.folder.email.subject"))
          .setBody(StringUtils.replace(GrouperTextContainer.textOrNull("group.unique.extension.in.folder.email.body"), "$groupNames$", groupNamesString)).send();
      }
      
          
    } catch (RuntimeException re) {
      runtimeException = re;
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));

    } finally {
      otherJobInput.getHib3GrouperLoaderLog().setJobMessage(GrouperUtil.mapToString(debugMap));
    }
    
    if (runtimeException != null) {
      throw runtimeException;
    }

    if (GrouperUtil.intValue(debugMap.get("errors")) > 0) {
      throw new RuntimeException("Had " + debugMap.get("errors") + " errors, check logs");
    }
    return null;
  }
  
  
  /**
   * run standalone
   */
  public static Hib3GrouperLoaderLog runDaemonStandalone() {
    return (Hib3GrouperLoaderLog)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
        
        hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
        String jobName = "OTHER_JOB_findBadGroupsUniqueExtensionInFolder";

        hib3GrouperLoaderLog.setJobName(jobName);
        hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
        hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
        hib3GrouperLoaderLog.store();
        
        OtherJobInput otherJobInput = new OtherJobInput();
        otherJobInput.setJobName(jobName);
        otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
        otherJobInput.setGrouperSession(grouperSession);
        new FindBadGroupsUniqueExtensionInFolderDaemon().run(otherJobInput);
        return hib3GrouperLoaderLog;
      }
    });
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(FindBadGroupsUniqueExtensionInFolderDaemon.class);

}
