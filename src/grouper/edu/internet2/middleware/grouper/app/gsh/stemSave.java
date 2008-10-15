/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.misc.SaveMode;

/**
 * Call Stem.saveStem()
 * <p/>
 * @author  Chris Hyzer
 * @version $Id: stemSave.java,v 1.1 2008-10-15 03:57:06 mchyzer Exp $
 * @since   0.0.1
 */
public class stemSave {

  /** insert a stem */
  public static final String INSERT = "INSERT";

  /** INSERT_OR_UPDATE */
  public static final String INSERT_OR_UPDATE = "INSERT_OR_UPDATE";
  
  /** UPDATE */
  public static final String UPDATE = "UPDATE";
  
  /**
   * Save a stem
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param stem 
   * @param uuid 
   * @param name 
   * @param displayExtension 
   * @param description 
   * @param saveMode 
   * @return  a string
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, Stem stem,
      String uuid, String name, String displayExtension, String description, 
      String saveMode) {
    GrouperShell.setOurCommand(interpreter, true);
    try {
      GrouperSession  grouperSession = GrouperShell.getSession(interpreter);
      SaveMode saveModeEnum = SaveMode.valueOfIgnoreCase(saveMode);
      
      Stem.saveStem(grouperSession, stem.getName(), uuid, name, 
          displayExtension, description, saveModeEnum, false);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    return "Stem saved";    
  } // public static boolean invoke(i, stack, name)

} // public class resetRegistry

