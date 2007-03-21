/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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
import  edu.internet2.middleware.subject.*;
import  java.io.*;
import  java.util.*;
import  java.util.Properties;
import  org.apache.commons.lang.StringEscapeUtils;
import  org.apache.commons.logging.*;

/**
 * XML Utilities.
 * <p/>
 * @author  blair christensen.
 * @version $Id: XmlUtils.java,v 1.16 2007-03-21 18:02:28 blair Exp $
 * @since   1.1.0
 */
class XmlUtils {

  // PROTECTED CLASS CONSTANTS //
  protected static final String SPECIAL_STAR      = "*";  // this seems to indicate relative import
                                                          // if at beginning of idfr but not at end


  // PROTECTED CLASS METHODS //
  
  // @since   1.2.0
  protected static boolean internal_getBooleanOption(Properties opts, String key) {
    return Boolean.valueOf( opts.getProperty(key, "false") );
  } // protected static boolean internal_getBooleanOption(opts, key)

  // @since   1.2.0 
  protected static Properties internal_getSystemProperties(Log log, String file) 
    throws  IOException
  {
    Properties props = new Properties();
    log.debug("loading system properties: " + file);
    props.load( XmlExporter.class.getResourceAsStream(file) );
    return props;
  } // protected static Properties internal-getSystemProperties(log, file);

  // @since   1.2.0
  protected static Properties internal_getUserProperties(Log log, String file) 
    throws  FileNotFoundException,
            IOException
  {
    Properties props = new Properties();
    if (file != null) {
      log.debug("loading user-specified properties: " + file);
      InputStream is = null;
      try {
        is = new FileInputStream(file);
        props.load(is);
      }
      catch (FileNotFoundException eFNF) {
        throw eFNF;
      }
      catch (IOException eIO) {
        throw eIO;
      }
      finally {
        if (is != null) { is.close(); }
      }
    } 
    return props;
  } // protected static Properties internal_getUserProperties(log, file)

  // @since   1.2.0
  protected static boolean internal_hasImmediatePrivilege(Subject subj, Owner o, String p) {
    if (o instanceof Group) {
      return XmlUtils._hasImmediateAccessPrivilege(subj, (Group) o, p);
    }
    return XmlUtils._hasImmediateNamingPrivilege(subj, (Stem) o, p);
  } // protected static boolean internal_hasImmediatePrivilege(subj, o, p)

  // @since   1.2.0
  protected static String internal_xmlE(String s) {
    return StringEscapeUtils.escapeXml(s);
  } // protected static String internal_xmlE(s)


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static boolean _hasImmediateAccessPrivilege(Subject subj, Group g, String p) {
    AccessPrivilege ap;
    Iterator        it  = g.getPrivs(subj).iterator();
    while (it.hasNext()) {
      ap = (AccessPrivilege) it.next();
      if ( ap.getName().equals(p) && SubjectHelper.eq( ap.getOwner(), subj ) ) {
        return true;
      }
    }
    return false;
  } // private static boolean _hasImmediatePrivilege(subj, g, p)

  // @since   1.1.0
  protected static boolean _hasImmediateNamingPrivilege(Subject subj, Stem ns, String p) {
    NamingPrivilege np;
    Iterator        it  = ns.getPrivs(subj).iterator();
    while (it.hasNext()) {
      np = (NamingPrivilege) it.next();
      if ( np.getName().equals(p) && SubjectHelper.eq( np.getOwner(), subj ) ) {
        return true;
      }
    }
    return false;
  } // protected static boolean hasImmediatePrivilege(subject, stem, privilege)

} // class XmlUtils

