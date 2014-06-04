/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
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

package edu.internet2.middleware.grouper.xml;
import  edu.internet2.middleware.grouper.internal.util.Quote;
import  java.util.Properties;

/**
 * XML Command Line Argument Processing.
 * <p/>
 * @author  blair christensen.
 * @version $Id: XmlArgs.java,v 1.4 2009-03-31 06:58:28 mchyzer Exp $
 * @since   1.1.0
 */
public class XmlArgs {

  // TODO 20070321 i could probably make this smarter if i required instantiation

  // PROTECTED CLASS CONSTANTS //
  public static final String RC_EFILE      = "export.file";
  public static final String RC_IFILE      = "import.file";
  public static final String RC_NAME       = "owner.name";
  public static final String RC_PARENT     = "mystery.parent";
  public static final String RC_RELATIVE   = "mystery.relative";
  public static final String RC_CHILDREN   = "mystery.children";
  public static final String RC_IGNORE     = "mystery.ignore";
  public static final String RC_NOPROMPT   = "mystery.noprompt";
  public static final String RC_SUBJ       = "subject.identifier";
  public static final String RC_UPROPS     = "properties.user";
  public static final String RC_UPDATELIST = "update.list";
  public static final String RC_UUID       = "owner.uuid";


  // PRIVATE CLASS CONSTANTS //
  private static final String E_INSUFFICIENT_ARGS = "insufficient arguments";
  private static final String E_NAME_AND_UUID     = "cannot specify uuid and name";
  private static final String E_TOO_MANY_ARGS     = "too many arguments: ";
  private static final String E_UNKNOWN_OPTION    = "unknown option: ";


  // public CLASS METHODS //

  // @since   1.2.0
  public static Properties internal_getXmlExportArgs(String args[])
    throws  IllegalArgumentException,
            IllegalStateException
  {
    // TODO 20070321 DRY `getXmlImportArgs()`
    Properties  rc        = new Properties();
    String      arg;
    int         inputPos  = 0;
    int         pos       = 0;
    while (pos < args.length) {
      arg = args[pos];
      if (arg.startsWith("-")) {
        if (_handleNamedArgUuid(rc, arg))       {
          rc.setProperty(RC_UUID, args[pos + 1]);
          pos += 2;
          continue;
        } 
        else if (_handleNamedArgName(rc, arg))  {
          rc.setProperty(RC_NAME, args[pos + 1]);
          pos += 2;
          continue;
        } 
        else if (arg.equals("-userAuditFilename")) {
          rc.setProperty("userAuditFilename", args[pos + 1]);
          pos += 2;
          continue;
        }  
        else if (arg.equals("-userAuditOnly")) {
          rc.setProperty("userAuditOnly", "true");
          pos += 1;
          continue;
        }  
        else if (arg.equals("-relative"))       {
          rc.setProperty(RC_RELATIVE, "true");
          pos++;
          continue;
        }  
        else if (arg.equalsIgnoreCase("-includeparent")) {
          rc.setProperty(RC_PARENT, "true");
          pos++;
          continue;
        }
        else if (arg.equalsIgnoreCase("-childrenonly")) {
            rc.setProperty(RC_CHILDREN, "true");
            pos++;
            continue;
          }else {
          throw new IllegalArgumentException(E_UNKNOWN_OPTION + arg);
        }
      }
      _handlePositionalArg(rc, inputPos, arg, RC_EFILE);
      pos++;
      inputPos++;
    }
    _enoughArgs(inputPos);
    return rc;
  } // protected static Properties internal_getXmlExportArgs(args)

  // @since   1.2.0
  public static Properties internal_getXmlImportArgs(String[] args) 
    throws  IllegalArgumentException,
            IllegalStateException
  {
    Properties  rc        = new Properties();
    String      arg;
    int         inputPos  = 0;
    int         pos       = 0;
    while (pos < args.length) {
      arg = args[pos];
      if (arg.startsWith("-")) {
        if      (_handleNamedArgUuid(rc, arg))  {
          rc.setProperty(RC_UUID, args[pos + 1]);
          pos += 2;
          continue;
        } 
        else if (_handleNamedArgName(rc, arg))  {
          rc.setProperty(RC_NAME, args[pos + 1]);
          pos += 2;
          continue;
        } 
        else if (arg.equals("-userAuditFilename")) {
          rc.setProperty("userAuditFilename", args[pos + 1]);
          pos += 2;
          continue;
        }  
        else if (arg.equals("-userAuditOnly")) {
          rc.setProperty("userAuditOnly", "true");
          pos += 1;
          continue;
        }  
        else if (arg.equals("-list"))           {
          rc.setProperty(RC_UPDATELIST, "true");
          pos++;
          continue;
        }
        else if (arg.equalsIgnoreCase("-noprompt"))       {
            rc.setProperty(RC_NOPROMPT, "true");
            pos++;
            continue;
          }
        else if (arg.equalsIgnoreCase("-ignoreInternal"))           {
            rc.setProperty(RC_IGNORE, "true");
            pos++;
            continue;
          } 
        else {
          throw new IllegalArgumentException(E_UNKNOWN_OPTION + arg);
        }
      }
      _handlePositionalArg(rc, inputPos, arg, RC_IFILE);
      pos++;
      inputPos++;
    }
    _enoughArgs(inputPos);
    return rc;
  } // protected static Properties internal_getXmlImportArgs(args)

  // @since   1.2.0
  public static boolean internal_wantsHelp(String[] args) {
    if (
      args.length == 0
      || 
      "--h --? /h /? --help /help ${cmd}".indexOf(args[0]) > -1
    ) 
    {
      return true;
    }
    return false;
  } // protected static void internal_wantsHelp(args)


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static void _enoughArgs(int inputPos) 
    throws  IllegalStateException
  {
    if (inputPos < 1) {
      throw new IllegalStateException(E_INSUFFICIENT_ARGS);
    }
  } // private static void _enoughArgs(inputPos)

  // @since   1.1.0
  private static boolean _handleNamedArgName(Properties rc, String arg)
    throws  IllegalArgumentException
  {
    boolean rv = false;
    if (arg.equals("-name")) {
      if (rc.getProperty(RC_UUID) != null) {
        throw new IllegalArgumentException(E_NAME_AND_UUID);
      }
      rv = true;
    }
    return rv;
  } // private static boolean _handleNamedArgName(rc, arg)

  // @since   1.1.0
  private static boolean _handleNamedArgUuid(Properties rc, String arg) 
    throws  IllegalArgumentException
  {
    boolean rv = false;
    if (arg.equals("-id")) {
      if (rc.getProperty(RC_NAME) != null) {
        throw new IllegalArgumentException(E_NAME_AND_UUID);
      }
      rv = true;
    }
    return rv;
  } // private static boolean _handleNamedArgUuid(rc, arg)

  // @since   1.1.0
  private static void _handlePositionalArg(Properties rc, int inputPos, String arg, String file) 
    throws  IllegalArgumentException
  {
    switch (inputPos) {
    case 0:
      rc.setProperty(RC_SUBJ, arg);
      break;
    case 1:
      rc.setProperty(file, arg);
      break;
    case 2:
      rc.setProperty(RC_UPROPS, arg);
      break;
    case 3:
      throw new IllegalArgumentException(E_TOO_MANY_ARGS + Quote.single(arg));
    }
  } // private static void _handlePositionalArg(rc, inputPos, arg, file)

} // class XmlArgs

