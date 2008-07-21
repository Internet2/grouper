/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import  bsh.Interpreter;
//import  java.io.*;
//import  java.util.*;

/**
 * {@link GrouperShell} Command Reader interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CommandReader.java,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
 * @since   0.1.1
 */
interface CommandReader {

  /**
   * @return  Current <code>BeanShell</code> interpreter.
   * @throws  GrouperShellException
   * @since   0.1.1
   */
  public Interpreter getInterpreter() throws GrouperShellException;

  /**
   * @return  Get next command to evaluate.
   * @throws  GrouperShellException
   * @since   0.1.1
   */
  public String getNext() throws GrouperShellException;

} // interface CommandReader

