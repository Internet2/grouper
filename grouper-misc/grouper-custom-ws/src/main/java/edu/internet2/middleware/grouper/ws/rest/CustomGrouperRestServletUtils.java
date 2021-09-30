package edu.internet2.middleware.grouper.ws.rest;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomGrouperRestServletUtils {
    /**
     * take a request and get the list of url strings for the rest web service
     * @see #extractUrlStrings(String)
     * @param request is the request to get the url strings out of
     * @return the list of url strings
     */
    public static List<String> extractUrlStrings(HttpServletRequest request) {
        return extractUrlStrings(request.getRequestURI());
    }

    /**
     * <pre>
     * take a request uri and break up the url strings not including the app name or servlet
     * this does not include the url params (if applicable)
     * if the input is: grouper-ws/servicesRest/xhtml/v1_3_000/groups/members
     * then the result is a list of size 3: {"v1_3_000", "group", "members"}
     *
     * </pre>
     * @param requestUri
     * @return the url strings
     */
    public static List<String> extractUrlStrings(String requestUri) {
        String[] parts = requestUri.split("/");
        return Arrays.asList(Arrays.copyOfRange(parts, 3, parts.length));
    }
}
