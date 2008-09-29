/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import java.util.LinkedHashSet;
import java.util.Set;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.exception.SchemaException;

/**
 * Find <code>Field</code>s belonging to a <code>GroupType</code>.
 * <p/>
 * @author  blair christensen.
 * @version $Id: typeGetFields.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.1.0
 */
public class typeGetFields {

  // PUBLIC CLASS METHODS //

  /**
   * Find <code>Field</code>s belonging to a <code>GroupType</code>.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Find this <code>GroupType</code>'s <code>Field</code>s.
   * @return  {@link Set} of {@link Field}s.
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static Set invoke(Interpreter i, CallStack stack, String name) 
    throws  GrouperShellException
  {
    Set fields = new LinkedHashSet();
    GrouperShell.setOurCommand(i, true);
    try {
      GroupType t = GroupTypeFinder.find(name);
      fields = t.getFields();
    }
    catch (SchemaException eS) {
      GrouperShell.error(i, eS);
    }
    return fields;
  } // public static Set invoke(i, stack, name)

} // public class typeGetFields

