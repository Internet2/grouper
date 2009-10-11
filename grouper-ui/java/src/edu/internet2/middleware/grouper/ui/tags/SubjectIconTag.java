/**
 * @author mchyzer
 * $Id: SubjectIconTag.java,v 1.1 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.subject.Subject;


/**
 * tag to print out subject icon
 */
public class SubjectIconTag extends SimpleTagSupport  {

  /**
   * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
   */
  @Override
  public void doTag() throws JspException, IOException {
    
    StringBuilder result = new StringBuilder();
    
    String sourceId = this.subject != null ? this.subject.getSource().getId() : null;

    sourceId = this.guiSubject != null ? this.guiSubject.getSubject().getSource().getId() : sourceId;
    
    String icon = GrouperUiUtils.imageFromSubjectSource(sourceId);

    if (!StringUtils.isBlank(icon)) {
      
      result.append("<img src=\"").append(icon).append("\" alt=\"subjectIcon\" />");
      
      this.getJspContext().getOut().print(result.toString());
    }
    
  }

  /** subject */
  private Subject subject;

  /** gui subject */
  private GuiSubject guiSubject;
  
  
  /**
   * @return the guiSubject
   */
  public GuiSubject getGuiSubject() {
    return this.guiSubject;
  }


  
  /**
   * @param guiSubject1 the guiSubject to set
   */
  public void setGuiSubject(GuiSubject guiSubject1) {
    this.guiSubject = guiSubject1;
  }


  /**
   * group name
   * @return the groupName
   */
  public Subject getSubject() {
    return this.subject;
  }

  
  /**
   * group name
   * @param subject1 the groupName to set
   */
  public void setSubject(Subject subject1) {
    this.subject = subject1;
  }

  
}
