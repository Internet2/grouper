/**
 * Base classes used by concrete Parser and Generator implementations;
 * contain functionality that is not specific to JSON or input
 * abstraction (byte vs char).
 * Most formats extend these types, although it is also possible to
 * directly extend {@link edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core.JsonParser} or
 * {@link edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core.JsonGenerator}.
 */
package edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core.base;
