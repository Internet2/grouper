/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * Call Stem.saveStem().  Note, this was renamed to stemSave2 since it conflicted with StemSave
 * <p/>
 * @author  Chris Hyzer
 * @version $Id: stemSave.java,v 1.1 2008-10-15 03:57:06 mchyzer Exp $
 * @since   0.0.1
 */
public class stemSave2 {

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

