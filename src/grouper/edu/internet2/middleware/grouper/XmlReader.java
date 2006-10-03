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
import  javax.xml.parsers.*;
import  org.w3c.dom.*;
import  org.xml.sax.*;

/**
 * Read XML representation of the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: XmlReader.java,v 1.2 2006-10-03 17:22:10 blair Exp $
 * @since   1.1.0
 */
public class XmlReader {

  // PUBLIC CLASS METHODS //

  /**
   * Convert a <tt>String</tt> to a <tt>Document</tt>.
   * <pre class="eg">
   * try {
   *   Document doc = XmlReader.getDocumentFromString(s);
   * }
   * catch (GrouperException eG) {
   *   // conversion error
   * }
   * </pre>
   * @param   s   Convert this String.
   * @throws  GrouperException
   * @since   1.1.0
   */
  public static Document getDocumentFromString(String s) 
    throws  GrouperException  
  {
    try {
      return  DocumentBuilderFactory.newInstance()
                                    .newDocumentBuilder()
                                    .parse(
                                      new ByteArrayInputStream( s.getBytes() )
                                    );
    }
    catch (IOException eIO)                   {
      throw new GrouperException(eIO.getMessage(), eIO);
    }
    catch (ParserConfigurationException ePCF) {
      throw new GrouperException(ePCF.getMessage(), ePCF);
    }
    catch (SAXException eSAX)                 {
      throw new GrouperException(eSAX.getMessage(), eSAX);
    }
  } // public static Document getDocumentFromString(s)

} // public class XmlReader

