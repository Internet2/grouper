/** *****************************************************************************
 * Copyright 2012 Internet2
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************** */
/*
 * Copyright (C) 20016 nicolas marcotte.
 *
 *
 * You may use and distribute under the same terms as Grouper itself.
 */
package edu.internet2.middleware.grouper.app.gsh;

import bsh.BshMethod;
import bsh.Interpreter;
import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jline.Terminal;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import static jline.internal.Preconditions.checkNotNull;
import org.apache.commons.logging.Log;

/**
 *
 * @author nicolas.marcotte@usherbrooke.ca
 */
public class JlineInterpreter {

    private static final String CLASSNAME = JlineInterpreter.class.getSimpleName();

    private static final Log LOG = GrouperUtil.getLog(JlineInterpreter.class);

    private final Interpreter interpreter;
    private final ConsoleReaderReader reader;

    public JlineInterpreter(String prompt, InputStream in, OutputStream out) {
        final Terminal terminal = TerminalFactory.create();
        final ConsoleReader cr;
        try {
            cr = new ConsoleReader(prompt, in, out, terminal);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        reader = new ConsoleReaderReader(cr);
        PrintStream printStream = new ConsoleReaderPrintStream(cr);
        PrintStream printStreamError = new ConsoleReaderPrintStream(cr, true);
        this.interpreter = new Interpreter(reader, printStream, printStreamError, true);
        final ArgumentCompleter argumentCompleter = new ArgumentCompleter(new BshAllNameCompleter(getLowerCaseClassName()));

        cr.addCompleter(argumentCompleter);

    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public Reader getReader() {
        return reader;
    }
    private class BshAllNameCompleter implements Completer {
        
        private Collection<String> additionalStrings;
        
        public BshAllNameCompleter() {
            this.additionalStrings = Collections.emptyList();
        }
        
        private BshAllNameCompleter(Collection<String> additionalStrings) {
            this.additionalStrings = additionalStrings;
        }
        
        @Override
        public int complete(String buffer, int cursor, List<CharSequence> candidates) {
            
            TreeSet<String> localCandidates = new TreeSet<String>(additionalStrings);
            localCandidates.addAll(Arrays.asList(interpreter.getNameSpace().getAllNames()));
            
            for (BshMethod m : interpreter.getNameSpace().getMethods()) {
                
                localCandidates.add(m.getName());
                
            }
            checkNotNull(candidates);
            
            if (buffer == null) {
                candidates.addAll(localCandidates);
            } else {
                for (String match : localCandidates.tailSet(buffer)) {
                    if (!match.startsWith(buffer)) {
                        break;
                    }
                    
                    candidates.add(match);
                }
            }
            
            return candidates.isEmpty() ? -1 : 0;
            
        }
    }

    private static class ConsoleReaderPrintStream extends PrintStream {

        private final ConsoleReader cr;
        private boolean error;

        public ConsoleReaderPrintStream(final ConsoleReader cr) {

            super(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    cr.getOutput().write(b);
                    cr.getOutput().flush();

                }
            });
            this.cr = cr;
        }

        private ConsoleReaderPrintStream(ConsoleReader cr, boolean error) {
            this(cr);
            this.error = error;
        }

        @Override
        public void print(Object obj) {
            print(obj.toString());
        }

        @Override
        public void println(Object x) {
            println(x.toString());
        }

        @Override
        public void print(String s) {
            try {

                while (s.contains("\n")) {
                    int newIndex = s.indexOf("\n");
                    cr.println(s.substring(0, newIndex));

                    if (newIndex <= s.length()) {
                        s = "";
                    } else {
                        s = s.substring(newIndex);
                    }
                }

                if (!s.isEmpty()) {
                    cr.print(s);
                }

                cr.getOutput().flush();

            } catch (IOException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }

        }

        @Override
        public void println(String s) {
            try {
                s = "error: " + s;
                cr.println(s);
                cr.getOutput().flush();
            } catch (IOException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        }
    }

    private static class ConsoleReaderReader extends Reader {

        private final ConsoleReader cr;
        private CharBuffer currentLine;

        public ConsoleReaderReader(ConsoleReader cr) {
            this.cr = cr;
        }

        @Override
        public void close() throws IOException {
            cr.close();
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {

            if (currentLine == null || !currentLine.hasRemaining()) {
                currentLine = CharBuffer.wrap(cr.readLine() + "\n");
            }
            len = Math.min(currentLine.remaining(), len);
            currentLine.get(cbuf, off, len);

            return len;
        }
    }

    private static Collection<String> getLowerCaseClassName() {
        try {
            ArrayList<String> retVal = new ArrayList<String>();
            Pattern pattern = Pattern.compile("([a-z]+[a-zA-Z]*)\\.class");
            URL resource = JlineInterpreter.class.getResource(CLASSNAME + ".class");
            
            String[] classList = null;
            if (resource.toString().startsWith("file:")) {   //Running in ide
                classList = new File(JlineInterpreter.class.getResource("/" + JlineInterpreter.class.getPackage().getName().replace(".", "/") + "/").toURI()).list();
            } else {
                classList = listClassesFromJar();
            }
            
            for (String classFile : classList) {
                Matcher matcher = pattern.matcher(classFile);
                if (matcher.matches()) {
                    retVal.add(matcher.group(1));
                }
            }
            
            return retVal;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        } catch (URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }
    private static String[] listClassesFromJar() throws IOException {
        ArrayList<String> classes = new ArrayList<String>();
        String resourceUrl = JlineInterpreter.class.getResource(CLASSNAME + ".class").toString();
        String resourcePath = resourceUrl.substring("jar:".length(), resourceUrl.indexOf("!"));
        String packagePath = resourceUrl.substring(resourceUrl.indexOf("!/") + 2, resourceUrl.lastIndexOf("/"));
        JarInputStream jarInputStream = new JarInputStream(new URL(resourcePath).openConnection().getInputStream());
        try {
            final int pakageNameLength = packagePath.length();
            for (JarEntry jarEntry = jarInputStream.getNextJarEntry(); jarEntry != null; jarEntry = jarInputStream.getNextJarEntry()) {
                final String name = jarEntry.getName();
                
                if (name.startsWith(packagePath) && name.endsWith(".class") && name.lastIndexOf("/") == pakageNameLength) {
                    classes.add(name.substring(name.lastIndexOf("/") + 1));                 
                }
                jarInputStream.closeEntry();
            }
            return classes.toArray(new String[classes.size()]);
        } finally {
            jarInputStream.close();
        }
    }
}
