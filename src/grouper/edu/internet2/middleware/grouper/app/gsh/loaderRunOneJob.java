/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import org.apache.commons.lang.StringUtils;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;

/**
 * run one loader job by name or group
 * <p/>
 * @author  Chris Hyzer
 * @version $Id: loaderRunOneJob.java,v 1.1 2008-10-30 20:57:17 mchyzer Exp $
 * @since   0.0.1
 */
public class loaderRunOneJob {

  /**
   * run one loader job
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param group 
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, Group group) {
    GrouperShell.setOurCommand(interpreter, true);
    try {
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
      hib3GrouperLoaderLog.setJobScheduleType("MANUAL_FROM_GSH");
      
      String grouperLoaderTypeString = GrouperLoaderType.attributeValueOrDefaultOrNull(group, GrouperLoader.GROUPER_LOADER_TYPE);

      if (!group.hasType(GroupTypeFinder.find("grouperLoader")) 
          || StringUtils.isBlank(grouperLoaderTypeString)) {
        throw new RuntimeException("Cant find grouper loader type of group: " + group.getName());
      }
      
      GrouperLoaderType grouperLoaderType = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderTypeString, true);
      
      hib3GrouperLoaderLog.setJobName(grouperLoaderType.name() + "__" + group.getName() + "__" + group.getUuid());

      GrouperSession  grouperSession     = GrouperShell.getSession(interpreter);

      GrouperLoaderJob.runJob(hib3GrouperLoaderLog, group, grouperSession);
      
      return "loader ran successfully, inserted " + hib3GrouperLoaderLog.getInsertCount()
        + " memberships, deleted " + hib3GrouperLoaderLog.getDeleteCount() + " memberships, total membership count: "
        + hib3GrouperLoaderLog.getTotalCount();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * run one loader job
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param jobName 
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, String jobName) {
    GrouperShell.setOurCommand(interpreter, true);
    try {

      GrouperSession  grouperSession     = GrouperShell.getSession(interpreter);
      GrouperLoaderType grouperLoaderType = GrouperLoaderType.typeForThisName(jobName);
      if (grouperLoaderType.equals(GrouperLoaderType.SQL_SIMPLE) || grouperLoaderType.equals(GrouperLoaderType.SQL_GROUP_LIST)) {
        
        int uuidIndexStart = jobName.lastIndexOf("__");
      
        String grouperLoaderGroupUuid = jobName.substring(uuidIndexStart+2, jobName.length());
        Group group = GroupFinder.findByUuid(grouperSession, grouperLoaderGroupUuid);
        return invoke(interpreter, stack, group);
      }
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
      hib3GrouperLoaderLog.setJobScheduleType("MANUAL_FROM_GSH");
      hib3GrouperLoaderLog.setJobName(jobName);
      GrouperLoaderJob.runJob(hib3GrouperLoaderLog, null, grouperSession);
      
      return "loader ran successfully: " + hib3GrouperLoaderLog.getJobMessage();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

