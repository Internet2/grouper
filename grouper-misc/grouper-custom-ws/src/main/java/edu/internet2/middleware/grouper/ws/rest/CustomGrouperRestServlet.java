package edu.internet2.middleware.grouper.ws.rest;

import edu.internet2.middleware.grouper.instrumentation.InstrumentationDataBuiltinTypes;
import edu.internet2.middleware.grouper.instrumentation.InstrumentationThread;
import edu.internet2.middleware.grouper.j2ee.status.GrouperStatusServlet;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;
import edu.internet2.middleware.grouper.ws.rest.contentType.WsRestResponseContentType;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

public class CustomGrouperRestServlet extends GrouperRestServlet {
    private final ServiceLoader<CustomGrouperRestProvider> loader = ServiceLoader.load(CustomGrouperRestProvider.class);

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        GrouperStartup.startup();

        GrouperStatusServlet.incrementNumberOfRequest();
        InstrumentationThread.addCount(InstrumentationDataBuiltinTypes.WS_REQUESTS.name());

        GrouperServiceJ2ee.assignHttpServlet(this);

        // TODO: handle requested type
        WsRestResponseContentType wsRestResponseContentType = WsRestResponseContentType.json;
        resp.setContentType(wsRestResponseContentType.getContentType());

        List<String> urlStrings = CustomGrouperRestServletUtils.extractUrlStrings(req);
        Map<String, String[]> parameterMap = req.getParameterMap();
        String body = IOUtils.toString(req.getReader());

        CustomGrouperRestRequest customGrouperRestRequest = new CustomGrouperRestRequest(urlStrings, parameterMap, body, req, resp);

        //TODO: extract
        CustomGrouperRestProvider provider = StreamSupport.stream(loader.spliterator(), false).filter( i -> i.supports(customGrouperRestRequest)).findFirst().orElseThrow(() -> new RuntimeException("could not find a provider"));
        Object result = provider.provide(customGrouperRestRequest);

        wsRestResponseContentType.writeString(result, resp.getWriter());
        resp.getWriter().close();
    }

    public Iterator<CustomGrouperRestProvider> getProviders() {
        return loader.iterator();
    }
}
