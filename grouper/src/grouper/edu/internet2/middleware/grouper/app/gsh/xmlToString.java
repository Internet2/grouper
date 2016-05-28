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
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.xml.XmlExporter;

/**
 * Export Groups Registry to XML string.
 * 
 * @author  blair christensen.
 * @version $Id: xmlToString.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.1.0
 */
public class xmlToString {

  // PUBLIC CLASS METHODS //

  /**
   * Export Groups Registry to XML <tt>String</tt>.
   * 
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @return  XML string.
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static String invoke(Interpreter i, CallStack stack) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s         = GrouperShell.getSession(i);
      XmlExporter     exporter  = new XmlExporter(s, new Properties());
      Writer          w         = new StringWriter();
      exporter.export(w);
      return w.toString();
    }
    catch (GrouperException eG) {
      GrouperShell.error(i, eG);
    }
    return null;
  } // public static String invoke(i, stack, group, subjId)

} // public class xmlToString

