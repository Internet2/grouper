/*
 * Created on Feb 14, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.internet2.middleware.signet.ui;

import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.SignetRuntimeException;
import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceSet;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LimitRenderer
{
  public static String render(Limit limit)
  {
    StringBuffer outStr = new StringBuffer();
    String limitName = "LIMIT_" + limit.getId();
    
    ChoiceSet choiceSet;
    try
    {
      choiceSet = limit.getChoiceSet();
    }
    catch (ObjectNotFoundException onfe)
    {
      throw new SignetRuntimeException(onfe);
    }

    Set choices = choiceSet.getChoices();
    boolean isFirstChoice = true;
    Iterator choicesIterator = choices.iterator();
    
    if (limit.getRenderer().equals("singleChoicePullDown.jsp"))
    {
      outStr.append
      	("<select class=\"" + limit.getDataType() + "\""
      	 + "name \"" + limitName + "\">\n");
      
      while (choicesIterator.hasNext())
      {
        Choice choice = (Choice)(choicesIterator.next());
        outStr.append("<option" + (isFirstChoice ? " selected" : "") + ">");
        outStr.append(choice.getDisplayValue());
        outStr.append("</option>\n");
        
        isFirstChoice = false;
      }
      
      outStr.append("</select>");
    }
    else if (limit.getRenderer().equals("multipleChoiceCheckboxes.jsp"))
    {
      int choiceNumber = 0;
      while (choicesIterator.hasNext())
      {
        if (choiceNumber > 0)
        {
          outStr.append("<br />\n");
        }
        
        Choice choice = (Choice)(choicesIterator.next());
        String choiceName = limitName + "_" + choiceNumber;
        outStr.append
          (" <input name=\"" + choiceName + "\""
           + " type=\"checkbox\" value=\"" + choice.getValue() + "\" />\n");
        outStr.append
        	("<label for=\"" + choiceName + "\">");
        outStr.append(choice.getDisplayValue());
        outStr.append("</label>\n");
        choiceNumber++;
      }
    }
    else
    {
      outStr.append
      	("ERROR: Unexpected value from limit.getRenderer(): "
      	 + limit.getRenderer());
    }
    
    return outStr.toString();
  }
}
