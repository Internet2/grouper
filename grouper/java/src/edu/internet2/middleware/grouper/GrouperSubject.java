/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper;


import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  org.apache.commons.logging.Log;
import  org.apache.commons.logging.LogFactory;


/** 
 * {@link Grouper} {@link Subject} implementation.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.42 2005-06-20 20:09:43 blair Exp $
 */
public class GrouperSubject implements Subject {

  /*
   * PRIVATE CLASS VARIABLES
   */
  private static Log log = LogFactory.getLog(GrouperSubject.class);


  /*
   * PRIVATE INSTANCE METHODS
   */
  private GrouperSourceAdapter  adapter = null;
  private Map                   attrs   = null; 
  private GrouperGroup          g       = null;
  private String                id      = null;
  private String                name    = null;
  private SubjectType           type    = null;


  /*
   * CONSTRUCTORS
   */
  protected GrouperSubject(GrouperGroup g, GrouperSourceAdapter sa) {
    log.debug("Converting " + g + " to subject");
    this.g        = g;
    this.id       = g.id();
    this.name     = g.attribute("name").value();
    this.type     = SubjectTypeEnum.valueOf("group");
    this.adapter  = sa;
  }


  /**
   * {@inheritDoc}
   */
  public Map getAttributes() {
    this.loadAttributes();
    return this.attrs;
  }

  /**
   * {@inheritDoc}
   */
  public String getAttributeValue(String name) {
    this.loadAttributes();
    if (this.attrs.containsKey(name)) {
      // TODO Should I confirm that there is only one value?
      Iterator iter = ( (Set) this.attrs.get(name) ).iterator();
      while (iter.hasNext()) {
        return (String) iter.next();
      }
    } 
    return new String();
  }

  /**
   * {@inheritDoc}
   */
  public Set getAttributeValues(String name) {
    this.loadAttributes();
    if (this.attrs.containsKey(name)) {
      return (Set) this.attrs.get(name);
    }
    return new HashSet();
  }

  /**
   * {@inheritDoc}
   */
  public String getDescription() {
    return this.getAttributeValue("description");
  }

  /**
   * {@inheritDoc}
   */
  public String getId() {
    return this.id;
  }

  /**
   * {@inheritDoc}
   */
  public String getName() {
    return this.name;
  }

  /**
   * {@inheritDoc}
   */
  public SubjectType getType() {
    return this.type;
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Load group attributes and convert them to a more appropriate
   * format.  Although, frankly, the internal format I'm using *does*
   * irk me a fair amount.
   */
  private void loadAttributes() {
    if (this.attrs == null) {
      Map   gattrs  = g.attributes();
      attrs         = new HashMap(); 
      Iterator iter = gattrs.keySet().iterator();
      while (iter.hasNext()) {
        GrouperAttribute ga = (GrouperAttribute) gattrs.get( iter.next());
        Set vals = new HashSet();
        vals.add( ga.value() );
        attrs.put( ga.field(), vals );
      }
    }
  }
}

