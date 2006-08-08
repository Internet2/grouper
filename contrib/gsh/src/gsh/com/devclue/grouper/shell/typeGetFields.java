/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;

/**
 * Find {@link Field}s belonging to a {@link GroupType}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: typeGetFields.java,v 1.1 2006-08-08 17:56:10 blair Exp $
 * @since   0.0.2
 */
public class typeGetFields {

  // PUBLIC CLASS METHODS //

  /**
   * Find {@link Field}s belonging to a {@link GroupType}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   type        Find this {@link GroupType}s {@link Field}s.
   * @return  {@link Set} of {@link Field}s.
   * @throws  GrouperShellException
   * @since   0.0.2
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

