/**
 * Copyright 2014 Internet2
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

/**
 * Add stem.
 * <p/>
 * @author  blair christensen.
 * @version $Id: addRootStem.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class addRootStem {

  // PUBLIC CLASS METHODS //

  /**
   * Add a root stem.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   extn        <i>extension</i> of {@link Stem}.
   * @param   displayExtn <i>displayExtension</i> of {@link Stem}.
   * @return  Added {@link Stem}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Stem invoke(
    Interpreter i, CallStack stack, String extn, String displayExtn
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    return StemHelper.addStem(i, null, extn, displayExtn);
  }
  
  /**
   * Add a root stem.
   * <p/>
   * @param   grouperSession
   * @param   extn        <i>extension</i> of {@link Stem}.
   * @param   displayExtn <i>displayExtension</i> of {@link Stem}.
   * @return  Added {@link Stem}.
   */
  public static Stem invoke(GrouperSession grouperSession, String extn, String displayExtn) {
    return StemHelper.addStem(grouperSession, null, extn, displayExtn);
  }

} // public class addRootStem

