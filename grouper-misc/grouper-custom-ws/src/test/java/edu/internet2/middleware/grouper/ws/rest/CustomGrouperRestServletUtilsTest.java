package edu.internet2.middleware.grouper.ws.rest;

import static org.junit.Assert.assertEquals;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.junit.Test;

import java.util.List;

public class CustomGrouperRestServletUtilsTest {
    @Test
    public void testExtractUrlStrings() {
        String input = "grouper-ws/servicesRest/xhtml/v1_3_000/groups/members";
        List<String> expected = Arrays.asList(new String[]{"xhtml", "v1_3_000", "groups", "members"});

        assertEquals(expected, CustomGrouperRestServletUtils.extractUrlStrings(input));
    }
}
