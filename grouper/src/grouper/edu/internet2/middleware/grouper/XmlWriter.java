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

/**
 * Create XML representation of the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: XmlWriter.java,v 1.1 2006-09-12 17:19:58 blair Exp $
 * @since   1.1.0
 */
class XmlWriter {

  // PRIVATE INSTANCE VARIABLES //
  private String  newLine;
  private Writer  w;


  // CONSTRUCTORS //

  // @since   1.1.0
  protected XmlWriter(Writer w) {
    this.newLine  = System.getProperty("line.separator");
    this.w        = w;
  } // protected XmlWriter(w)


  // PROTECTED INSTANCE METHODS //

  // Close {@link Writer}.
  // @throws  IOException
  // @since   1.1.0
  protected void close() 
    throws  IOException
  {
    this.w.close();
  } // protected void close()

  // Output string to {@link Writer}.
  // @throws  IOException
  // @since   1.1.0
  protected void put(String s) 
    throws  IOException
  {
    this.w.write(s);
  } // protected void put(s)

  // Output platform-appropriate newline to {@link Writer}.
  // @throws  IOException
  // @since   1.1.10
  protected void puts() 
    throws  IOException
  {
    this.w.write(this.newLine);
  } // protected void puts(s)

  // Output string plus platform-appropriate newline to {@link Writer}.
  // @throws  IOException
  // @since   1.1.10
  protected void puts(String s) 
    throws  IOException
  {
    this.w.write(s + this.newLine);
  } // protected void puts(s)

} // class XmlWriter

