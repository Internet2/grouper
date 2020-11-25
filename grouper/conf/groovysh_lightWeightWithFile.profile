:set verbosity QUIET
:set interpreterMode false

import edu.internet2.middleware.grouper.*
import edu.internet2.middleware.grouper.app.gsh.*
import edu.internet2.middleware.grouper.privs.*
import edu.internet2.middleware.grouper.misc.*

class GSHFileLoad extends org.codehaus.groovy.tools.shell.CommandSupport {
  protected GSHFileLoad(final org.codehaus.groovy.tools.shell.Groovysh shell) {
    super(shell, ':gshFileLoad', ':gshfl')
  }

  @Override
  Object execute(final List<String> args) {
    assert args != null

    if (args.size() == 0) {
      throw new RuntimeException("Command 'gshFileLoad' requires at least one argument")
    }

    for (source in args) {
      log.debug("Attempting to load: \"$source\"")

      def file = new File("$source")

      if (!file.exists()) {
        throw new RuntimeException("File not found: \"$file\"")
      }

      URL url = file.toURI().toURL()

      load(url)
    }
  }

  void load(final URL url) {
    assert url != null

    if (io.verbose) {
      io.out.println("Loading: $url")
    }

    url.eachLine { String it, int lineNumber ->
      if (lineNumber == 1 && it.startsWith('#!')) {
        return
      }

      if (it.equals("exit") || it.startsWith("exit ") || it.startsWith("exit;") || it.equals("quit") || it.startsWith("quit ") || it.startsWith("quit;")) {
        return
      }

      if (!it.startsWith("#")) {
        try {
          shell << it as String
        } catch (Throwable t) {
          GrouperShell.handleFileLoadError(it, t);
        }
      }
    }
  }
}

:register GSHFileLoad

GrouperSession.startRootSession()

import edu.internet2.middleware.grouper.util.*

:set verbosity INFO
:set interpreterMode true
