/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouperActivemqExt.org.apache.commons.jexl2.parser;


/**
 * This exception is thrown when parse errors are encountered.
 */
public class ParseException extends Exception {
    /**
     * The version identifier.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Last correct input before error occurs.
     */
    private String after = "";
    /**
     * Error line.
     */
    private int line = -1;
    /**
     * Error column.
     */
    private int column = -1;
    
    /**
     * Gets the line number.
     * @return line number.
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets the column number.
     * @return the column.
     */
    public int getColumn() {
        return column;
    }
    
    /**
     * Gets the last correct input.
     * @return the string after which the error occured
     */
    public String getAfter() {
        return after;
    }
    
    /**
     * This constructor is used by the method "generateParseException"
     * in the generated parser.  Calling this constructor generates
     * a new object of this type with the fields "currentToken",
     * "expectedTokenSequences", and "tokenImage" set.
     * @param currentToken This is the last token that has been consumed successfully.  If
     * this object has been created due to a parse error, the token
     * followng this token will (therefore) be the first error token.
     * @param expectedTokenSequences Each entry in this array is an array of integers.  Each array
     * of integers represents a sequence of tokens (by their ordinal
     * values) that is expected at this point of the parse.
     * @param tokenImage This is a reference to the "tokenImage" array of the generated
     * parser within which the parse error occurred.  This array is
     * defined in the generated ...Constants interface.
     */
    public ParseException(Token currentToken, int[][] expectedTokenSequences, String[] tokenImage) {
        super("parse error");
        Token tok = currentToken.next;
        after = tok.image;
        line = tok.beginLine;
        column = tok.beginColumn;
    }

    /**
     * Default ctor.
     */
    public ParseException() {
        super();
    }

    /** Constructor with message. */
    public ParseException(String message) {
        super(message);
    }
}
