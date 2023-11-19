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
import java.util.Set;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.filter.StemNameAnyFilter;

/**
 * Query for stems by name.
 * <p/>
 * @author  blair christensen.
 * @version $Id: getStems.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class getStems {

  // PUBLIC CLASS METHODS //

  /**
   * Query for stems by name.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Find stems with <i>name</i> as part of their name.
   * @return  {@link Set} of {@link Stem}s.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Set invoke(Interpreter i, CallStack stack, String name) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s     = GrouperShell.getSession(i);
      return invoke(s, name);
    }
    catch (QueryException eQ) {
      GrouperShell.error(i, eQ);
    }
    return null;
  }
  
  /**
   * Query for stems by name.
   * <p/>
   * @param   grouperSession
   * @param   name        Find stems with <i>name</i> as part of their name.
   * @return  {@link Set} of {@link Stem}s.
   */
  public static Set invoke(GrouperSession grouperSession, String name) {
    Stem            root  = StemFinder.findRootStem(grouperSession);
    GrouperQuery    gq    = GrouperQuery.createQuery(
      grouperSession, 
      new StemNameAnyFilter(name, root)
    );
    return gq.getStems();
  }

} // public class getStems

