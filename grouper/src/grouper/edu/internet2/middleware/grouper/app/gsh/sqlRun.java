/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import java.io.File;
import java.util.Properties;

import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;

import bsh.CallStack;
import bsh.Interpreter;

/**
 * Run a sql script against DB (like ant does it)
 * <p/>
 * @author  Chris Hyzer
 * @version $Id: sqlRun.java,v 1.3 2008-11-13 20:26:10 mchyzer Exp $
 * @since   0.0.1
 */
public class sqlRun {

  /**
   * Create tables and init schema (depending on configuration in grouper.properties)
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param scriptFile 
   * @since   0.0.1
   */
  public static void invoke(Interpreter interpreter, CallStack stack, File scriptFile) {
    GrouperShell.setOurCommand(interpreter, true);
    
    GrouperDdlUtils.sqlRun(scriptFile, false, true);
    
  }

  /**
   * Executes an SQL statement.
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   sql
   * @return  int The number of updates made.
   * @throws GrouperShellException 
   * @since   0.0.1
   */
  public static int invoke(Interpreter interpreter, CallStack stack, String sql) throws GrouperShellException {
    GrouperShell.setOurCommand(interpreter, true);
    return HibernateSession.bySqlStatic().executeSql(sql);
  }


}

