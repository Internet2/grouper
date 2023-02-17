/**
 * Classes used for exposing logical structure of POJOs as Jackson
 * sees it, and exposed via
 * {@link edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.ObjectMapper#acceptJsonFormatVisitor(Class, JsonFormatVisitorWrapper)}
 * and
 * {@link edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.ObjectMapper#acceptJsonFormatVisitor(edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.JavaType, JsonFormatVisitorWrapper)}
 * methods.
 *<p>
 * The main entrypoint for code, then, is {@link edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper} and other
 * types are recursively needed during traversal.
 */
package edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.jsonFormatVisitors;
