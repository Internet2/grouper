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

import  edu.internet2.middleware.grouper.*;
import  java.io.Serializable;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.EqualsBuilder;
import  org.apache.commons.lang.builder.HashCodeBuilder;
import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class modeling a {@link Grouper} via path element.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: MemberVia.java,v 1.10 2005-03-22 02:02:49 blair Exp $
 */
public class MemberVia implements Serializable {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private String  chainKey;
  private int     chainIdx;
  private String  listKey;


  /*
   * CONSTRUCTORS
   */

  public MemberVia() { 
    // Nothing 
  }

  protected MemberVia(GrouperList gl) {
    // TODO Try to load?  If not, save chainKey setting for later.
    this.listKey = gl.key();
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Compares the specified object with this type definition for equality.
   * <p />
   * @param o Object to be compared for equality with this type
   *   definition.
   * @return  True if the specified object is equal to this type
   *   definition.
   */
  public boolean equals(Object o) {
     return EqualsBuilder.reflectionEquals(this, o);
   }

  /**
   * Returns the hash code value for this type definition.
   * <p />
   * @return  The hash code value for this type definition.
   */
  public int hashCode() {
     return HashCodeBuilder.reflectionHashCode(this);
   }

  /**
   * Return a string representation of this object.
   * <p />
   * @return String representation of this object.
   */
  public String toString() {
    return new ToStringBuilder(this)      .
      append("chain", this.getChainKey()) .
      append("index", this.getChainIdx()) .
      append("list",  this.getListKey())  .
      toString();
  }



  /*
   * PROTECTED CLASS METHODS
   */

  /*
   * If a chain exists, find and return its chainKey.
   */
  protected static String findKey(
                            GrouperSession s, String listKey, List chain
                          ) 
  {
    String key = _compare(
             chain,
             _filterByLength(
               chain.size(), _findChains(s, listKey)
             )
           );
    return key;
  }

  /*
   * Load a via chain list.
   * <p />
   * @return List of {@link MemberVia} objects.
   */
  protected static List load(GrouperSession s, String key) {
    String  qry   = "MemberVia.by.key";
    List    chain = new ArrayList();

    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, key);
      try {
        chain.addAll( q.list() );
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }

    return chain;
  }


  /*
   * PROTECTED INSTANCE METHODS
   */

  /*
   * Set this object's index position.
   */
  protected void idx(int idx) {
    this.chainIdx = idx;
  }

  /*
   * @return This object's chainKey.
   */
  protected String key() {
    return this.getChainKey();
  }

  /* 
   * Set this object's chainKey.
   */
  protected void key(String key) {
    this.chainKey = key;
  }

  /* 
   * @return This object's listKey.
   */
  protected String listKey() {
    return this.listKey;
  }

  /*
   * Save this object to the groups registry.
   */
  protected void save(DbSess dbSess) {
    // TODO I should validate our state first
    try {
      dbSess.session().save(this);
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Error saving chain element " + this + ": " + e
                );
    }   
  }

  /*
   * @return This object's {@link GrouperList} object.
   */
  protected GrouperList toList(GrouperSession s) {
    GrouperList gl = null;
    try {
      gl = (GrouperList) s.dbSess().session().get(
                           GrouperList.class, this.getListKey()
                         );
      gl.load(s);
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Error converting MemberVia to list: " + e
                ); 
    }
    return gl;
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /*
   * Compare two chains.
   * @return chainKey is chains are equal.
   */
  private static String _compare(List chain, List chains) {
    String key = null;
    Iterator chainsIter = chains.iterator();
    while (chainsIter.hasNext()) {
      List another = (List) chainsIter.next();
      if (_equals(chain, another)) {
        MemberVia mv = (MemberVia) another.get(0);
        key = mv.key();
        break;
      }
    }
    return key;
  }

  /*
   *
   * Compare lists based upon index position and listKey.
   * @return true if lists are equivalenet.
   */
  private static boolean _equals(List a, List b) {
    boolean rv  = false;
    int     idx = 0; 
    Iterator iter = a.iterator();
    while (iter.hasNext()) {
      MemberVia mvA = (MemberVia) iter.next();
      MemberVia mvB = (MemberVia) b.get(idx);
      if (mvA.getListKey().equals(mvB.getListKey())) {
        rv = true;
      } else {
        rv = false;
        break;
      }
      idx++;
    }
    return rv;
  }

  /*
   * Filter candidate chains based upon chain size.
   * @return List of chains that are of the appropriate length.
   */
  private static List _filterByLength(int length, List chains) {
    List filtered = new ArrayList();
    Iterator iter = chains.iterator();
    while (iter.hasNext()) {
      List chain = (List) iter.next();
      if (chain.size() == length) {
        filtered.add(chain);
      }
    }
    return filtered;
  }

  /*
   * Find all chains that begin with listKey.
   * @return List of chains.
   */
  private static List _findChains(GrouperSession s, String listKey) {
    List    chains  = new ArrayList();
    String  qry     = "MemberVia.by.list.and.first";
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, listKey); 
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          MemberVia mv = (MemberVia) iter.next();
          chains.add( load(s, mv.getChainKey()) );
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return chains;
  }


  /*
   * HIBERNATE
   */

  private String getChainKey() {
    return this.chainKey;
  }

  private void setChainKey(String key) {
    this.chainKey = key;
  } 

  private int getChainIdx() {
    return this.chainIdx;
  }

  private void setChainIdx(int idx) {
    this.chainIdx = idx;
  } 

  private String getListKey() {
    return this.listKey;
  }

  private void setListKey(String key) {
    this.listKey = key;
  } 

}

