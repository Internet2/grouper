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
import java.net.URL;
import java.util.Properties;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.XmlImporter;
import edu.internet2.middleware.grouper.xml.XmlReader;

/**
 * Import Groups Registry from XML URL.
 * <p/>
 * @author  blair christensen.
 * @version $Id: xmlFromURL.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.1.0
 */
public class xmlFromURL {

  // PUBLIC CLASS METHODS //

  /**
   * Import Groups Registry from XML URL.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   url         Import XML from this URL.
   * @return  True if successful.
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static boolean invoke(Interpreter i, CallStack stack, URL url) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s         = GrouperShell.getSession(i);
      return invoke(s, url);
    }
    catch (GrouperException eG) {
      GrouperShell.error(i, eG);
    }
    return false;
  }
  
  /**
   * Import Groups Registry from XML URL.
   * <p/>
   * @param   grouperSession
   * @param   url         Import XML from this URL.
   * @return  True if successful.
   */
  public static boolean invoke(GrouperSession grouperSession, URL url) {
    //make sure right db
    GrouperUtil.promptUserAboutDbChanges("import data from xml url", true);
    XmlImporter     importer  = new XmlImporter(grouperSession, new Properties());
    importer.load( XmlReader.getDocumentFromURL(url) );
    return true;
  }

} // public class xmlFromURL

