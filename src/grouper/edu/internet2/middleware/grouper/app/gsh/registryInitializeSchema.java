/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.registry.RegistryInitializeSchema;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Create tables and init schema (if configured)
 * <p/>
 * @author  Chris Hyzer
 * @version $Id: registryInitializeSchema.java,v 1.2 2008-10-24 05:51:47 mchyzer Exp $
 * @since   0.0.1
 */
public class registryInitializeSchema {

  /** constant to drop then create */
  public static final int DROP_THEN_CREATE = 1;

  /** if we should write and run script, or just write it */
  public static final int WRITE_AND_RUN_SCRIPT = 2;
  
  /**
   * Create tables and init schema (depending on configuration in grouper.properties)
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @return  a string
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack) {
    return invoke(interpreter, stack, 0);
  }

  /**
   * Create tables and init schema (depending on configuration in grouper.properties)
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param options 
   * @return  a string
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, int options) {
    RegistryInitializeSchema.inInitSchema = true;
    try {
      GrouperShell.setOurCommand(interpreter, true);
      boolean dropThenCreate = GrouperUtil.hasOption(options, DROP_THEN_CREATE);
      boolean writeAndRunScript = GrouperUtil.hasOption(options, WRITE_AND_RUN_SCRIPT);
      boolean installGrouperData = RegistryInitializeSchema.isInstallGrouperData();
      GrouperDdlUtils.bootstrapHelper(true, false, true, dropThenCreate, writeAndRunScript, false, installGrouperData, null, true);
      return "Registry DDL created: dropThenCreate: " + dropThenCreate 
        + ", writeAndRunScript: " + writeAndRunScript;    
    } finally {
      RegistryInitializeSchema.inInitSchema = false;
    }
  } // public static boolean invoke(i, stack, name)

} // public class resetRegistry

