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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.internet2.middleware.grouper.exception.GrouperException;

/**
 * Read XML representation of the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: XmlReader.java,v 1.2 2008-09-29 03:38:30 mchyzer Exp $
 * @since   1.1.0
 */
public class XmlReader {

  // PUBLIC CLASS METHODS //

  /**
   * Read <tt>Document</tt> from file.
   * <pre class="eg">
   * try {
   *   Document doc = XmlReader.getDocumentFromFile(filename);
   * }
   * catch (GrouperException eG) {
   *   // unable to retrieve document
   * }
   * </pre>
   * @param   filename  Read <tt>Document</tt> from this file.
   * @throws  GrouperException
   * @since   1.1.0
   */
  public static Document getDocumentFromFile(String filename) 
    throws  GrouperException
  {
    try {
      return _getDocumentBuilder().parse( new File(filename) );
    }
    catch (IOException eIO)   {
      throw new GrouperException(eIO.getMessage(), eIO);
    }
    catch (SAXException eSAX) {
      throw new GrouperException(eSAX.getMessage(), eSAX);
    }
  } // public static Document getDocumentFromFile(filename)

  /**
   * Read <tt>Document</tt> from <tt>String</tt>.
   * <pre class="eg">
   * try {
   *   Document doc = XmlReader.getDocumentFromString(s);
   * }
   * catch (GrouperException eG) {
   *   // unable to retrieve document
   * }
   * </pre>
   * @param   s   Read document from this <tt>String</tt>.
   * @throws  GrouperException
   * @since   1.1.0
   */
  public static Document getDocumentFromString(String s) 
    throws  GrouperException  
  {
    try {
      return _getDocumentBuilder().parse( new ByteArrayInputStream( s.getBytes() ));
    }
    catch (IOException eIO)   {
      throw new GrouperException(eIO.getMessage(), eIO);
    }
    catch (SAXException eSAX) {
      throw new GrouperException(eSAX.getMessage(), eSAX);
    }
  } // public static Document getDocumentFromString(s)

  /**
   * Read <tt>Document</tt> from <tt>URL</tt>.
   * <pre class="eg">
   * try {
   * }
   * catch (GrouperException eG) {
   * }
   * </pre>
   * @param   url   Read <tt>Document</tt> from this <tt>URL</tt>.
   * @throws  GrouperException
   * @since   1.1.0
   */
  public static Document getDocumentFromURL(URL url) 
    throws  GrouperException
  {
    try {
      return _getDocumentBuilder().parse( url.openStream() );
    }
    catch (IOException eIO)   {
      throw new GrouperException(eIO.getMessage(), eIO);
    }
    catch (SAXException eSAX) {
      throw new GrouperException(eSAX.getMessage(), eSAX);
    }
  } // private static Document _getDocument(url)


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static DocumentBuilder _getDocumentBuilder() 
    throws  GrouperException
  {
    try {
      return DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }
    catch (ParserConfigurationException ePC)  {
      throw new GrouperException(ePC.getMessage(), ePC);
    }
  } // private static DocumentBuilder _getDocumentBuilder()

} // public class XmlReader

