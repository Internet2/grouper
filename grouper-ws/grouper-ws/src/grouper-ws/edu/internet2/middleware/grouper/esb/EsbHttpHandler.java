/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.esb;

import java.io.DataInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.esb.listener.EsbListener;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * Class to processes data received on servlet interface, extracts the payload and passes it to {@link EsbListener} 
 * for processing, returning an http result code and human readable result string to calling client
 *
 */
public class EsbHttpHandler extends HttpServlet {

  private GrouperSession grouperSession;

  private EsbListener esbListener;

  private static final Log LOG = GrouperUtil.getLog(EsbHttpHandler.class);
  
  public void doPost(HttpServletRequest request,
      HttpServletResponse response) {
	  //Enumeration<String> paramEnum = request.getParameterNames();
	  //paramEnum.hasMoreElements();
	  //String postContent = (String) paramEnum.nextElement();
	  try {
	  if(request.getContentLength()<0) {
		  if (LOG.isDebugEnabled())
		        LOG.debug("Invalid content received, ignoring");
		      response.setContentType("text/html;charset=utf-8");
		      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		      response.flushBuffer();
	  } else {
		  byte[] data = new byte[request.getContentLength()];
	      DataInputStream in = new DataInputStream(request.getInputStream());
	      in.readFully(data);
	      in.close();
	      String jsonString = new String(data);
	      this.grouperSession = GrouperSession.startRootSession();
	      if (this.esbListener == null)
	        this.esbListener = new EsbListener();
	      String result = esbListener.processEvent(jsonString, grouperSession);
	      response.setContentType("text/html;charset=utf-8");
	      response.setStatus(HttpServletResponse.SC_OK);
	      response.getWriter().print(result);
	      response.flushBuffer();
	      if (LOG.isDebugEnabled()) {
	        LOG.debug("Result " + result);
	      }
	      this.grouperSession.stop();
		  
	  }
	  }catch (IOException e) {
		  e.printStackTrace();
	  }
	  
  }
  
  

}
