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
 * @author mchyzer
 * $Id: XstreamPocGroup.java,v 1.4 2009-11-20 07:15:37 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.poc;


/**
 * poc class for groups
 */
public class XstreamPocGroup {

  /**
   * 
   */
  @SuppressWarnings("unused")
  private String somethingNotMarshaled = "whatever";
  
  /**
   * get an int by getter
   * @return the int by getter
   */
  public int getSomeIntByGetter() {
    return this.dontSerializeInt;
  }

  /**
   * dont marhsal this
   */
  private transient int dontSerializeInt;
  
  /**
   * get an int by getter
   * @param someIntByGetter
   */
  public void setSomeIntByGetter(int someIntByGetter) {
    this.dontSerializeInt = someIntByGetter;
  }
  
  /**
   * 
   * @param theName
   * @param theMembers
   */
  public XstreamPocGroup(String theName, XstreamPocMember[] theMembers) {
    this.name = theName;
    this.members = theMembers;
  }

  /**
   * 
   */
  public XstreamPocGroup() {
    //empty
  }
  
  /** */
  private String name;
  /** some int */
  private int someInt = 5;
  
  /** some bool */
  private boolean someBool = true;
  
  
  /**
   * @return the someInt
   */
  public int getSomeInt() {
    return this.someInt;
  }

  
  /**
   * @param someInt1 the someInt to set
   */
  public void setSomeInt(int someInt1) {
    this.someInt = someInt1;
  }

  
  /**
   * @return the someBool
   */
  public boolean isSomeBool() {
    return this.someBool;
  }

  
  /**
   * @param someBool1 the someBool to set
   */
  public void setSomeBool(boolean someBool1) {
    this.someBool = someBool1;
  }

  /** */
  private XstreamPocMember[] members;

  /**
   * 
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * 
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * 
   * @return array of members
   */
  public XstreamPocMember[] getMembers() {
    return this.members;
  }

  /**
   * 
   * @param members1
   */
  public void setMembers(XstreamPocMember[] members1) {
    this.members = members1;
  }
  
}
