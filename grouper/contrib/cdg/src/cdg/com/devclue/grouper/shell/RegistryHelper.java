/*
 * Copyright (C) 2005 blair christensen.
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
 * Registry Helper Methods.
 * <p />
 * @author  blair christensen.
 * @version $Id: RegistryHelper.java,v 1.1 2006-06-21 20:28:55 blair Exp $
 * @since   0.0.1
 */
class RegistryHelper {

  // PROTECTED CLASS METHODS //

  // @since 0.0.1
  protected static boolean reset(Interpreter i) 
  {
    RegistryReset.reset();
    return true;
  } // protected static boolean reset(i)

} // class RegistryHelper

