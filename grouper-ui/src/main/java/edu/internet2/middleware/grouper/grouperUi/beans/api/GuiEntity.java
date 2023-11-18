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
/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.entity.Entity;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.subject.Subject;


/**
 * Result of one entity retrieved.
 * 
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class GuiEntity extends GuiObjectBase implements Serializable {

  /**
   * &lt;a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&amp;lt;strong&amp;gt;FOLDER:&amp;lt;/strong&amp;gt;&amp;lt;br /&amp;gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>
   * @return short link
   */
  public String getShortLink() {
    
    return this.getGuiSubject().getShortLink();
  }
  
  /**
   * display short link with image next to it in li
   * &lt;a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&amp;lt;strong&amp;gt;FOLDER:&amp;lt;/strong&amp;gt;&amp;lt;br /&amp;gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>
   * @return short link
   */
  @Override
  public String getShortLinkWithIcon() {
    
    return this.getGuiSubject().getShortLinkWithIcon();
  }

  /**
   * display short link with image next to it in li and the path info below it
   * &lt;a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&amp;lt;strong&amp;gt;FOLDER:&amp;lt;/strong&amp;gt;&amp;lt;br /&amp;gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>
   * @return short link
   */
  public String getShortLinkWithIconAndPath() {
    
    return this.getGuiSubject().getShortLinkWithIcon();
  }
  
  /** entity */
  private Entity entity;
  
  /**
   * return the entity
   * @return the entity
   */
  public Entity getEntity() {
    return this.entity;
  }

  /**
   * 
   */
  public GuiEntity() {
    
  }
  
  /**
   * 
   * @param theEntity
   */
  public GuiEntity(Entity theEntity) {
    this.entity = theEntity;
  }
  
  /**
   * @see GuiObjectBase#getGrouperObject()
   */
  @Override
  public GrouperObject getGrouperObject() {
    return this.entity;
  }
  
  /**
   * gui subject
   */
  private GuiSubject guiSubject = null;
  
  /**
   * 
   * @return gui subject
   */
  public GuiSubject getGuiSubject() {
    if (this.guiSubject == null) {
      if (this.entity != null) {
        
        Subject subject = SubjectFinder.findByIdAndSource(this.entity.getId(), "grouperEntities", false);
        this.guiSubject = new GuiSubject(subject);
      }
    }
    return this.guiSubject;
  }
  
}
