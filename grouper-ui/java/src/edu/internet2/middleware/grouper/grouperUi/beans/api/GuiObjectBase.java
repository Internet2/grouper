package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperObjectSubjectWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


public abstract class GuiObjectBase {

  /**
   * if this is a subject
   * @return if this is a subject
   */
  public boolean isSubjectType() {
    return false;
  }
  
  /**
   * convert grouper objects to gui object bases
   * @param grouperObjects
   * @return the gui object bases
   */
  public static Set<GuiObjectBase> convertFromGrouperObjects(Set<GrouperObject> grouperObjects) {
    Set<GuiObjectBase> tempObjectBases = new LinkedHashSet<GuiObjectBase>();
    
    for (GrouperObject grouperObject : GrouperUtil.nonNull(grouperObjects)) {
      if (grouperObject instanceof Group) {
        tempObjectBases.add(new GuiGroup((Group)grouperObject));
      } else if (grouperObject instanceof Stem) {
        tempObjectBases.add(new GuiStem((Stem)grouperObject));
      } else if (grouperObject instanceof AttributeDef) {
        tempObjectBases.add(new GuiAttributeDef((AttributeDef)grouperObject));
      } else if (grouperObject instanceof AttributeDefName) {
        tempObjectBases.add(new GuiAttributeDefName((AttributeDefName)grouperObject));
      } else if (grouperObject instanceof GrouperObjectSubjectWrapper) {
        tempObjectBases.add(new GuiSubject(((GrouperObjectSubjectWrapper)grouperObject).getSubject()));
      } else {
        throw new RuntimeException("Not expecting object of type: " 
            + grouperObject.getClass().getSimpleName() + ", " + grouperObject.getName());
      }
      
    }
    
    return tempObjectBases;

  }
  
  /**
   * get the gui object
   * @return the object
   */
  public abstract GrouperObject getGrouperObject();
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "" + this.getGrouperObject();
  }
  
  /**
   * colon space separated path e.g.
   * Full : Path : To : The : Entity
   * @return the colon space separated path
   */
  public String getPathColonSpaceSeparated() {

    if (this.getGrouperObject() instanceof GrouperObjectSubjectWrapper) {
      return TextContainer.retrieveFromRequest().getText().get("guiStemNotApplicable");
    }
    
    String parentStemName = GrouperUtil.parentStemNameFromName(this.getGrouperObject().getDisplayName());
    
    if (StringUtils.isBlank(parentStemName) || StringUtils.equals(":", parentStemName)) {
      return TextContainer.retrieveFromRequest().getText().get("stem.root.display-name");
    }

    return parentStemName.replace(":", " : ");
    
  }

  /**
   * e.g. Sun Feb 9 5:48:12 PM EST 2014
   */
  final static SimpleDateFormat dateUiFormat = new SimpleDateFormat(
      "EE MMM d h:mm:ss aa zz yyyy");

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    System.out.println(dateUiFormat.format(new Date(System.currentTimeMillis())));
  }
  
  /**
   * who created the object
   * @return subject or null if none registered
   */
  public GuiSubject getCreatorGuiSubject() {
    Subject creator = null;
    if (this instanceof GuiGroup) {
      creator = ((GuiGroup)this).getGroup().getCreateSubject();
    } else if (this instanceof GuiStem) {
      creator = ((GuiStem)this).getStem().getCreateSubject();
    } else if (this instanceof GuiAttributeDef) {
      creator = ((GuiAttributeDef)this).getAttributeDef().getCreateSubject();
    } else if (this instanceof GuiAttributeDefName) {
      creator = ((GuiAttributeDefName)this).getAttributeDefName().getAttributeDef().getCreateSubject();
    }
    return creator == null ? null : new GuiSubject(creator);
  }
  
  /**
   * 
   * @return the parent gui stem
   */
  public GuiStem getParentGuiStem() {
    Stem parentStem = null;
    if (this instanceof GuiGroup) {
      parentStem = ((GuiGroup)this).getGroup().getParentStem();
    } else if (this instanceof GuiStem) {
      parentStem = ((GuiStem)this).getStem().getParentStem();
    } else if (this instanceof GuiAttributeDef) {
      parentStem = ((GuiAttributeDef)this).getAttributeDef().getParentStem();
    } else if (this instanceof GuiAttributeDefName) {
      parentStem = ((GuiAttributeDefName)this).getAttributeDefName().getAttributeDef().getParentStem();
    }
    return parentStem == null ? null : new GuiStem(parentStem);
  }
  

  /**
   * who lsat updated the object
   * @return subject or null if none registered
   */
  public GuiSubject getLastUpdatedByGuiSubject() {
    Subject lastUpdater = null;
    if (this instanceof GuiGroup) {
      lastUpdater = ((GuiGroup)this).getGroup().getModifySubject();
    } else if (this instanceof GuiStem) {
      lastUpdater = ((GuiStem)this).getStem().getModifySubject();
    }
    //note: attributes dont have this attribute
    return lastUpdater == null ? null : new GuiSubject(lastUpdater);
  }
  
  /**
   * get last edited string: Tue Sep 25 12:01:07 PM CDT 2012
   * @return the string of when last edited
   */
  public String getLastEditedString() {
    long lastEditedTimeLong = -1;
    if (this instanceof GuiGroup) {
      lastEditedTimeLong = ((GuiGroup)this).getGroup().getModifyTimeLong();
    } else if (this instanceof GuiStem) {
        lastEditedTimeLong = ((GuiStem)this).getStem().getModifyTimeLong();
    } else if (this instanceof GuiAttributeDef) {
      lastEditedTimeLong = GrouperUtil.longValue(((GuiAttributeDef)this).getAttributeDef().getLastUpdatedDb(), 0);
    } else if (this instanceof GuiAttributeDefName) {
      lastEditedTimeLong = GrouperUtil.longValue(((GuiAttributeDefName)this).getAttributeDefName().getLastUpdatedDb(), 0);
    }
    return lastEditedTimeLong <= 0 ? "" : dateUiFormat.format(new Date(lastEditedTimeLong));
  }
  
  /**
   * get created string: Tue Sep 25 12:01:07 PM CDT 2012
   * @return
   */
  public String getCreatedString() {
    long createTimeLong = -1;
    if (this instanceof GuiGroup) {
      createTimeLong = ((GuiGroup)this).getGroup().getCreateTimeLong();
    } else if (this instanceof GuiStem) {
        createTimeLong = ((GuiStem)this).getStem().getCreateTimeLong();
    } else if (this instanceof GuiAttributeDef) {
      createTimeLong = GrouperUtil.longValue(((GuiAttributeDef)this).getAttributeDef().getCreatedOnDb(), 0);
    } else if (this instanceof GuiAttributeDefName) {
      createTimeLong = GrouperUtil.longValue(((GuiAttributeDefName)this).getAttributeDefName().getCreatedOnDb(), 0);
    } else {
      return null;
    }
    return createTimeLong <= 0 ? "" : dateUiFormat.format(new Date(createTimeLong));
  }
  
  /**
   * colon space separated name e.g.
   * Full : Path : To : The : Entity
   * @return the colon space separated path
   */
  public String getNameColonSpaceSeparated() {

    String displayName = this.getGrouperObject().getDisplayName();
    
    if (StringUtils.isBlank(displayName) || StringUtils.equals(":", displayName)) {
      return TextContainer.retrieveFromRequest().getText().get("stem.root.display-name");
    }

    return displayName.replace(":", " : ");
    
  }
   /**
   * breadcrumbs for v2 ui
   * @return the breadcrumbs
   */
  public String getBreadcrumbs() {

    //<ul class="breadcrumb">
    //  <li><a href="index.html">Home </a><span class="divider"><i class='icon-angle-right'></i></span></li>
    //  <li><a href="#">Root </a><span class="divider"><i class='icon-angle-right'></i></span></li>
    //  <li><a href="view-folder-applications.html">Applications </a><span class="divider"><i class='icon-angle-right'></i></span></li>
    //  <li><a href="view-folder.html">Wiki </a><span class="divider"><i class='icon-angle-right'></i></span></li>
    //  <li class="active">Editors</li>
    //</ul>
    //GrouperUtil.xmlEscape(this.getPathColonSpaceSeparated(), true));
    
    StringBuilder result = new StringBuilder();
    result.append("<ul class=\"breadcrumb\">");
    result.append("<li><a href=\"#\" onclick=\"return guiV2link('operation=UiV2Main.indexMain');\">")
      .append(TextContainer.retrieveFromRequest().getText().get("guiBreadcrumbsHomeLabel"))
      .append(" </a><span class=\"divider\"><i class='icon-angle-right'></i></span></li>");

    if (this instanceof GuiSubject) {
      GuiSubject guiSubject = (GuiSubject)this;
      result.append("<li class=\"active\">").append(GrouperUtil.xmlEscape(guiSubject.getSubject().getName())).append("</li>");
    } else {
      GrouperObject grouperObject = this.getGrouperObject();
      if (grouperObject instanceof Stem && ((Stem)grouperObject).isRootStem()) {
        result.append("<li class=\"active\">").append(TextContainer.retrieveFromRequest().getText().get("stem.root.display-name")).append("</li>");
      } else {
        List<String> displayExtenstionsList = GrouperUtil.splitTrimToList(grouperObject.getDisplayName(), ":");
        List<String> theExtenstionsList = GrouperUtil.splitTrimToList(grouperObject.getName(), ":");
        displayExtenstionsList.add(0, TextContainer.retrieveFromRequest().getText().get("stem.root.display-name"));
        theExtenstionsList.add(0, ":");
        
        StringBuilder stemNameBuilder = new StringBuilder();
        
        for (int i=0;i<theExtenstionsList.size();i++) {
          //  <li><a href="view-folder-applications.html">Applications </a><span class="divider"><i class='icon-angle-right'></i></span></li>
          String stemName = null;
          if (i == theExtenstionsList.size() -1) {
            //  <li class="active">Editors</li>
            result.append("<li class=\"active\">").append(GrouperUtil.xmlEscape(displayExtenstionsList.get(i))).append("</li>");
          } else {
            if (i == 0) {
              stemName = ":";
            } else {
              if (i > 1) {
                stemNameBuilder.append(":");
              }
              stemNameBuilder.append(theExtenstionsList.get(i));
              stemName = stemNameBuilder.toString();
            }
            result.append("<li><a href=\"#\" onclick=\"return guiV2link('operation=UiV2Stem.viewStem&stemName=")
              .append(GrouperUtil.escapeUrlEncode(stemName))
              .append("');\" >").append(GrouperUtil.xmlEscape(displayExtenstionsList.get(i)))
              .append(" </a><span class=\"divider\"><i class='icon-angle-right'></i></span></li>");
          }
        }
      }
    }
    
    result.append("</ul>");
    return result.toString();
  }
  
  /**
   * title for browser:
   * &lt;strong&gt;FOLDER:&lt;/strong&gt;&lt;br /&gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
   * note, this is not xml escaped, if used in a title tag, it needs to be xml escaped...
   * @return the title
   */
  public String getTitle() {
    
    StringBuilder result = new StringBuilder();
    
    result.append("<strong>").append(
        TextContainer.retrieveFromRequest().getText().get("guiTooltipFolderLabel"))
        .append("</string><br />").append(GrouperUtil.xmlEscape(this.getPathColonSpaceSeparated(), true));
    result.append("<br />");
    result.append(GrouperUtil.xmlEscape(StringUtils.abbreviate(StringUtils.defaultString(this.getGrouperObject().getDescription()), 100), true));
    
    String resultString = result.toString();
    return resultString;
  }

  
}


