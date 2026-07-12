---
title: "Grouper custom template via GSH testing the Grouper health"
space: Grouper
pageId: 28548022
version: 3
lastUpdated: 2026-07-01T05:45:32.220Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548022/Grouper+custom+template+via+GSH+testing+the+Grouper+health
---

## Description

This will test the Grouper health, which can be helpful on upgrades

## Testing from UI

Script to test external systems

```
import java.util.List;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class Test77externalSystemTest extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {

    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();

    gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("NONE");

    List<GrouperExternalSystem> grouperExternalSystems = GrouperExternalSystem.retrieveAllGrouperExternalSystems();
    
    for (GrouperExternalSystem grouperExternalSystem : grouperExternalSystems) {
      try {
        List<String> errors = grouperExternalSystem.test();
        if (GrouperUtil.length(errors) == 0) {
          gsh_builtin_gshTemplateOutput.addOutputLine("Success: external system '" + grouperExternalSystem.getConfigId() + "' passed its test");
          continue;
        }
        for (String error : errors) {
          gsh_builtin_gshTemplateOutput.addOutputLine("error", "Error: external system '" + grouperExternalSystem.getConfigId() + "': " + error);
        }
      } catch (Exception e) {
        gsh_builtin_gshTemplateOutput.addOutputLine("error", "Error: external system '" + grouperExternalSystem.getConfigId() + "' failed: " + e.getMessage());
      }
    }
    
  }

}
 
```

Run this template:

## Template for WS

Instead of printing out errors, just throw exceptions so the http code is 500 not 200

```
import java.util.List;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class Test77externalSystemTest extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {

    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();

    gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("NONE");

    List<GrouperExternalSystem> grouperExternalSystems = GrouperExternalSystem.retrieveAllGrouperExternalSystems();
    
    for (GrouperExternalSystem grouperExternalSystem : grouperExternalSystems) {
      try {
        List<String> errors = grouperExternalSystem.test();
        if (GrouperUtil.length(errors) == 0) {
          gsh_builtin_gshTemplateOutput.addOutputLine("Success: external system '" + grouperExternalSystem.getConfigId() + "' passed its test");
          continue;
        }
        throw new RuntimeException("Error: external system '" + grouperExternalSystem.getConfigId() + "' failed: " + GrouperUtil.toStringForLog(errors));
      } catch (Exception e) {
        throw new RuntimeException("Error: external system '" + grouperExternalSystem.getConfigId() + "' failed: " + GrouperUtil.getFullStackTrace(e), e);
      }
    }
    
  }

}

```

```
curl -X POST -k -H 'Content-Type: application/json' -H 'Authorization: Basic xxx' -i 'https://grouperws.school.edu/grouper-ws/servicesRest/4.10.3/gshTemplateExec' --data '{
  "WsRestGshTemplateExecRequest":{
    "configId":"grouperTest"
  }
}'
```

```
500

{
  "WsGshTemplateExecResult": {
    "resultMetadata": {
      "success": "F",
      "resultCode": "EXCEPTION",
      "resultMessage": "java.lang.RuntimeException: Error: external system 'someDatabase' failed: could not connect to server,\nError on original script line number: 35\nError on prepended script line number: 36\nError on profile prepended script line number: 332\nError with line:     throw new RuntimeException(\"problem\");\n,\nProblem in HibernateSession: HibernateSession (5f60f6ef): new, notReadonly, READ_WRITE_NEW, notActiveTransaction, session (62bac2b3)\n\tat java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)\n\tat java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:77)\n\tat java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)\n\tat Test77externalSystemTest.gshRunLogic(Script2.groovy:332)\n\tat edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExec$2$1.callback(GshTemplateExec.java:533)\n\tat edu.internet2.middleware.grouper.hibernate.HibernateSession.callbackHibernateSession(HibernateSession.java:722)\n\tat edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExec$2.callback(GshTemplateExec.java:509)\n\tat edu.internet2.middleware.grouper.GrouperSession.callbackGrouperSession(GrouperSession.java:1000)\n\tat edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExec.execute(GshTemplateExec.java:494)\n\tat edu.internet2.middleware.grouper.ws.GrouperServiceLogic.executeGshTemplate(GrouperServiceLogic.java:11073)\n\tat edu.internet2.middleware.grouper.ws.rest.GrouperServiceRest.executeGshTemplate(GrouperServiceRest.java:3004)\n\tat edu.internet2.middleware.grouper.ws.rest.method.GrouperWsRestPut$12.service(GrouperWsRestPut.java:561)\n\tat edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod$3.service(GrouperRestHttpMethod.java:104)\n\tat edu.internet2.middleware.grouper.ws.rest.GrouperRestServlet.service(GrouperRestServlet.java:202)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:212)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:156)\n\tat org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:181)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:156)\n\tat edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee.doFilter(GrouperServiceJ2ee.java:1081)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:181)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:156)\n\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167)\n\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90)\n\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:483)\n\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:130)\n\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93)\n\tat org.apache.catalina.valves.rewrite.RewriteValve.invoke(RewriteValve.java:305)\n\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74)\n\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:343)\n\tat org.apache.coyote.ajp.AjpProcessor.service(AjpProcessor.java:533)\n\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)\n\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:932)\n\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1695)\n\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)\n\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)"
    },
    "responseMetadata": {
      "serverVersion": "4.10.2",
      "millis": "7664"
    }
  }
}
```
