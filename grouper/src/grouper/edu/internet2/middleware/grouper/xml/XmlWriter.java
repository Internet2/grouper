/**
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
 */
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
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;

/**
 * Create XML representation of the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: XmlWriter.java,v 1.4 2009-03-23 02:59:25 mchyzer Exp $
 * @since   1.1.0
 */
class XmlWriter {

  // PRIVATE INSTANCE VARIABLES //
  private String  newLine;
  private String  padding;
  private Writer  w;


  // CONSTRUCTORS //

  // @since   1.1.0
  protected XmlWriter(Writer w) {
    this.newLine  = GrouperConfig.NL;
    this.padding  = GrouperConfig.EMPTY_STRING;
    this.w        = w;
  } // protected XmlWriter(w)


  // PROTECTED INSTANCE METHODS //

  // Close {@link Writer}.
  // @since   1.2.0
  protected void internal_close() 
    throws  IOException
  {
    this.w.close();
  } // protected void internal_close()

  // Return a XML comment.
  // @since   1.2.0
  protected String internal_comment(String s) 
  {
    return "<!-- " + StringEscapeUtils.escapeXml(s) + " -->";
  } // protected String internal_comment(s)

  // @since   1.2.0
  protected void internal_indent() {
    this.padding = this._getPadding() + "  ";
  } // protected void internal_indent();

  // Output string to {@link Writer} with leading padding.
  // @since   1.2.0
  protected void internal_put(String s) 
    throws  IOException
  {
    this.w.write( this._getPadding() + s );
  } // protected void internal_put(s)

  // Output platform-appropriate newline to {@link Writer}.
  // @since   1.2.0
  protected void internal_puts() 
    throws  IOException
  {
    this.w.write(this.newLine);
  } // protected void internal_puts(s)
  
  /**
   * Output string to {@link Writer} with platform-appropriate newline and leading padding.
   * @param s
   * @throws IOException
   */
  protected void internal_puts(String s) 
    throws  IOException {
    //nothing to do if blank, right?
    if (StringUtils.isBlank(s)) {
      return;
    }
    this.internal_put( s + this.newLine );
  } 

  // @since   1.2.0
  protected void internal_undent() 
    throws  GrouperException
  {
    if (this._getPadding().length() < 2) {
      throw new GrouperException(
        "CANNOT UNDENT WHEN PADDING SIZE IS " + this._getPadding().length()
      );
    }
    this.padding = this._getPadding().substring(2);
  }
  

  // PRIVATE INSTANCE METHODS //

  // @since   1.1.0
  private String _getPadding() {
    return this.padding;
  } // private String _getPadding()

} // class XmlWriter

