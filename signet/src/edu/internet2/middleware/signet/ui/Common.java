/*--
  $Id: Common.java,v 1.3 2005-02-24 00:19:50 acohen Exp $
  $Date: 2005-02-24 00:19:50 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.ui;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.Signet;

public class Common
{
  /**
   * @param log
   * @param request
   */
  public static void showHttpParams
  	(String prefix, Log log, HttpServletRequest request)
  {
    Enumeration paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements())
    {
      String paramName = (String)(paramNames.nextElement());
      String[] paramValues = request.getParameterValues(paramName);
      log.warn(prefix + ": " + paramName + "=" + printArray(paramValues));
    }
  }

  /**
   * @param log
   * @param request
   */
  public static void showHttpParams
  	(String prefix, Logger logger, HttpServletRequest request)
  {
    Enumeration paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements())
    {
      String paramName = (String)(paramNames.nextElement());
      String[] paramValues = request.getParameterValues(paramName);
      logger.log
      	(Level.WARNING,
      	 prefix + ": " + paramName + "=" + printArray(paramValues));
    }
  }
  
  private static String printArray(String[] items)
  {
    StringBuffer out = new StringBuffer();
    out.append("[");
    for (int i = 0; i < items.length; i++)
    {
      if (i > 0)
      {
        out.append(", ");
      }
      
      out.append(items[i]);
    }
    
    out.append("]");
    
    return out.toString();
  }

  public static String displayLimitValues(Assignment assignment)
  {
    StringBuffer strBuf = new StringBuffer();

    Limit[] limits = assignment.getFunction().getLimitsArray();
    LimitValue[] limitValues = assignment.getLimitValuesArray();
    for (int limitIndex = 0; limitIndex < limits.length; limitIndex++)
    {
      Limit limit = limits[limitIndex];
//      strBuf.append("<tr>\n");
      
      strBuf.append("  <td align=\"right\">\n");
      strBuf.append("    " + limit.getName() + ":\n");
      strBuf.append("  </td>\n");
      
      strBuf.append("  <td>\n");

      int limitValuesPrinted = 0;
      for (int limitValueIndex = 0;
           limitValueIndex < limitValues.length;
           limitValueIndex++)
      {
        LimitValue limitValue = limitValues[limitValueIndex];
        if (limitValue.getLimit().equals(limit))
        {
          strBuf.append((limitValuesPrinted++ > 0) ? "    <br />\n" : "");
          strBuf.append("    " + limitValue.getValue() + "\n");
        }
      }
                 
      strBuf.append("  </td>\n");
//      strBuf.append("</tr>\n");
    }
    
    return strBuf.toString();
  }
}
