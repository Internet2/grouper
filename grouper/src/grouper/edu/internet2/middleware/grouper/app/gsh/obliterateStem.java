/*******************************************************************************
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
 ******************************************************************************/
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
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemDeleteException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.pit.PITUtils;

/**
 * Obliterate a stem no matter what is in there.
 * <p/>
 * @author  chris hyzer
 * @version $Id: delStem.java,v 1.3 2009-03-15 06:37:23 mchyzer Exp $
 * @since   2.0.2
 */
public class obliterateStem {

  // PUBLIC CLASS METHODS //

  /**
   * Obliterate a stem.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   name  <i>name</i> of {@link Stem} to delete.
   * @param testOnly true if just seeing what it would do.  This is not supported if deleting from point in time.
   * @param deleteFromPointInTime true if you want to delete from point in time only.  False if you don't want to delete from point in time.
   * @return  True if {@link Stem} was deleted.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static boolean invoke(Interpreter i, CallStack stack, String name, boolean testOnly, boolean deleteFromPointInTime) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s   = GrouperShell.getSession(i);
      
      if (deleteFromPointInTime && testOnly) {
        throw new RuntimeException("testOnly option is not supported when deleting from point in time.");
      }
      
      if (deleteFromPointInTime) {
        PITUtils.deleteInactiveStem(name, true);
      } else {
        Stem ns  = StemFinder.findByName(s, name, true);
        ns.obliterate(true, testOnly);
      }
      
      return true;
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (StemDeleteException eNSD)            {
      GrouperShell.error(i, eNSD);
    }
    catch (StemNotFoundException eNSNF)         {
      GrouperShell.error(i, eNSNF);
    }
    return false;
  } // public static boolean invoke(i, stack, name)

} // public class delStem

