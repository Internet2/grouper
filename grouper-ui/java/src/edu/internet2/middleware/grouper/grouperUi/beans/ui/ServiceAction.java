/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

/**
 * @author vsachdeva
 *
 */
public class ServiceAction {
  
  private GrouperTemplateLogicBase service;
  
  private int indentLevel;
  
  private String type;
  
  private String arg0;

  private String arg1;
  
  private String arg2;
  
  private String externalizedkey;
  
  private boolean defaulChecked;
  
  private boolean checkSubmitted;
  

  public GrouperTemplateLogicBase getService() {
    return service;
  }

  public void setService(GrouperTemplateLogicBase service) {
    this.service = service;
  }

  public int getIndentLevel() {
    return indentLevel;
  }

  public void setIndentLevel(int indentLevel) {
    this.indentLevel = indentLevel;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getArg0() {
    return arg0;
  }

  public void setArg0(String arg0) {
    this.arg0 = arg0;
  }

  public String getArg1() {
    return arg1;
  }

  public void setArg1(String arg1) {
    this.arg1 = arg1;
  }

  public String getArg2() {
    return arg2;
  }

  public void setArg2(String arg2) {
    this.arg2 = arg2;
  }

  public String getExternalizedkey() {
    return externalizedkey;
  }

  public void setExternalizedkey(String externalizedkey) {
    this.externalizedkey = externalizedkey;
  }

  public boolean isDefaulChecked() {
    return defaulChecked;
  }

  public void setDefaulChecked(boolean defaulChecked) {
    this.defaulChecked = defaulChecked;
  }

  public boolean isCheckSubmitted() {
    return checkSubmitted;
  }

  public void setCheckSubmitted(boolean checkSubmitted) {
    this.checkSubmitted = checkSubmitted;
  }
  
  

}
