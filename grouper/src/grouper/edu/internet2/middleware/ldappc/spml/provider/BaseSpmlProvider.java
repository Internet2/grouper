/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.spml.provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openspml.v2.msg.Marshallable;
import org.openspml.v2.msg.XMLMarshaller;
import org.openspml.v2.msg.XMLUnmarshaller;
import org.openspml.v2.msg.spml.ErrorCode;
import org.openspml.v2.msg.spml.ExecutionMode;
import org.openspml.v2.msg.spml.Request;
import org.openspml.v2.msg.spml.Response;
import org.openspml.v2.msg.spml.StatusCode;
import org.openspml.v2.util.Spml2Exception;
import org.openspml.v2.util.xml.ReflectiveDOMXMLUnmarshaller;
import org.openspml.v2.util.xml.ReflectiveXMLMarshaller;
import org.slf4j.Logger;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.config.BaseReloadableService;

public abstract class BaseSpmlProvider extends BaseReloadableService implements SpmlProvider {

  private static final Logger LOG = GrouperUtil.getLogger(BaseSpmlProvider.class);

  private String id;

  public static final String methodName = "execute";

  private XMLMarshaller xmlMarshaller;

  private XMLUnmarshaller xmlUnmarshaller;

  public Response execute(Request request) {

    // a generic Response returned under error conditions
    Response response = new Response();
    response.setRequestID(this.getOrGenerateRequestID(request));

    try {
      // FUTURE handle asynchronous requests
      if (request.getExecutionMode() == ExecutionMode.ASYNCHRONOUS) {
        LOG.error("Unable to execute asynchronous request with id '{}'", request.getRequestID());
        fail(response, ErrorCode.UNSUPPORTED_EXECUTION_MODE);
        LOG.trace("response:\n{}", this.toXML(response));
      } else {
        // determine the appropriate method
        Method method = this.getClass().getMethod(methodName, new Class[] { request.getClass() });
        // execute the request
        response = (Response) method.invoke(this, new Object[] { request });
        if (response.getRequestID() == null) {
          response.setRequestID(this.getOrGenerateRequestID(request));
        }
      }

      // TODO fix logging
    } catch (NoSuchMethodException e) {
      LOG.error("Unsupported request '" + request.getClass().getName() + "' id '" + request.getRequestID() + "'", e);
      fail(response, ErrorCode.UNSUPPORTED_OPERATION);
      LOG.trace("response:\n{}", this.toXML(response));
    } catch (IllegalAccessException e) {
      LOG.error("Unsupported request '" + request.getClass().getName() + "' id '" + request.getRequestID() + "'", e);
      fail(response, ErrorCode.UNSUPPORTED_OPERATION);
      LOG.trace("response:\n{}", this.toXML(response));
    } catch (InvocationTargetException e) {
      LOG.error("Unsupported request '" + request.getClass().getName() + "' id '" + request.getRequestID() + "'", e);
      fail(response, ErrorCode.UNSUPPORTED_OPERATION);
      LOG.trace("response:\n{}", this.toXML(response));
    }

    return response;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public XMLMarshaller getXMLMarshaller() {

    if (this.xmlMarshaller == null) {
      this.xmlMarshaller = new ReflectiveXMLMarshaller();
    }
    return this.xmlMarshaller;
  }

  public void setXMLMarshaller(XMLMarshaller xmlMarshaller) {

    this.xmlMarshaller = xmlMarshaller;
  }

  public XMLUnmarshaller getXmlUnmarshaller() {
    if (this.xmlUnmarshaller == null) {
      this.xmlUnmarshaller = new ReflectiveDOMXMLUnmarshaller();
    }
    return xmlUnmarshaller;
  }

  public void setXmlUnmarshaller(XMLUnmarshaller xmlUnmarshaller) {
    this.xmlUnmarshaller = xmlUnmarshaller;
  }

  public Response fail(Response response, ErrorCode errorCode, Exception e) {
    return fail(response, errorCode, e.getMessage());
  }

  public Response fail(Response response, ErrorCode errorCode, String... messages) {
    response.setStatus(StatusCode.FAILURE);
    response.setError(errorCode);
    if (messages != null) {
      for (String message : messages) {
        // TODO for Active Directory
        message = message.replace((char) 0x0, '_');
        response.addErrorMessage(message);
      }
    }
    return response;
  }

  public String generateRequestID(Request request) {
    return PSPUtil.uniqueRequestId();
  }

  public String getOrGenerateRequestID(Request request) {
    if (request.getRequestID() != null) {
      return request.getRequestID();
    }
    return this.generateRequestID(request);
  }

  public String setRequestId(Request request) {
    if (request.getRequestID() == null) {
      request.setRequestID(this.generateRequestID(request));
    }
    return request.getRequestID();
  }

  public String toXML(Marshallable marshallable) {
    try {
      return marshallable.toXML(this.getXMLMarshaller());
    } catch (Spml2Exception e) {
      LOG.error("Unable to marshal xml", e);
      return null;
    }
  }
}
