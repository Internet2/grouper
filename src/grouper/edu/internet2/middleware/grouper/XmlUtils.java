/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import  java.io.*;
import  java.util.Properties;
import  org.apache.commons.logging.*;

/**
 * XML Utilities.
 * <p/>
 * @author  blair christensen.
 * @version $Id: XmlUtils.java,v 1.2 2006-09-20 16:59:50 blair Exp $
 * @since   1.1.0
 */
class XmlUtils {

  // CONSTRUCTORS //

  // @since   1.1.0
  protected XmlUtils() {
    super();
  } // protected XmlUtils()

  
  // PROTECTED CLASS METHODS //
  
  // @throws  IOException
  // @since   1.1.0 
  protected static Properties getSystemProperties(Log log, String file) 
    throws  IOException
  {
    Properties props = new Properties();
    log.debug("loading system properties: " + file);
    props.load( XmlExporter.class.getResourceAsStream(file) );
    return props;
  } // protected static Properties getSystemProperties(log, file);

  // @throws  FileNotFoundException
  // @throws  IOException
  // @since   1.1.0
  protected static Properties getUserProperties(Log log, String file) 
    throws  FileNotFoundException,
            IOException
  {
    Properties props = new Properties();
    if (file != null) {
      log.debug("loading user-specified properties: " + file);
      props.load(new FileInputStream(file));
    } 
    return props;
  } // protected static Properties getUserProperties(log, file)

  // @since   1.1.0
  protected static boolean isEmpty(Object obj) {
    if (obj == null || obj.equals(GrouperConfig.EMPTY_STRING)) {
      return true;
    }
    return false;
  } // protected static boolean isEmpty(obj)
  
  // @since   1.1.0
  protected static boolean wantsHelp(String[] args) {
    if (
      args.length == 0
      || 
      "--h --? /h /? --help /help ${cmd}".indexOf(args[0]) > -1
    ) 
    {
      return true;
    }
    return false;
  } // protected static void wantsHelp(args)

} // class XmlUtils

