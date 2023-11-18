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

