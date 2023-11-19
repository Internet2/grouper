/**
 * Copyright 2017 Internet2
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
 */

package edu.internet2.middleware.grouper.app.gsh.jline;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.fusesource.jansi.internal.WindowsSupport;
import org.fusesource.jansi.internal.Kernel32.INPUT_RECORD;
import org.fusesource.jansi.internal.Kernel32.KEY_EVENT_RECORD;

import edu.internet2.middleware.grouper.app.gsh.GrouperShell;

import jline.AnsiWindowsTerminal;

public class WindowsTerminal extends AnsiWindowsTerminal {
  
  /**
   * @throws Exception
   */
  public WindowsTerminal() throws Exception {
    super();
  }
  
  @Override
  public InputStream wrapInIfNeeded(InputStream in) throws IOException {

    String groovyPreloadString = GrouperShell.getGroovyPreloadString() + "\n";
    final ByteArrayInputStream groovyPreload = new ByteArrayInputStream(groovyPreloadString.getBytes("UTF-8"));
    
    return new InputStream() {
      private byte[] buf = null;
      int bufIdx = 0;

      @Override
      public int read() throws IOException {
        if (groovyPreload.available() > 0) {
          return groovyPreload.read();
        }
        
        while (buf == null || bufIdx == buf.length) {
          buf = readConsoleInput();
          bufIdx = 0;
        }
        int c = buf[bufIdx] & 0xFF;
        bufIdx++;
        return c;
      }
    };
  }

  // completely copied from jline
  private byte[] readConsoleInput() {
    // XXX does how many events to read in one call matter?
    INPUT_RECORD[] events = null;
    try {
      events = WindowsSupport.readConsoleInput(1);
    } catch (IOException e) {

    }
    if (events == null) {
      return new byte[0];
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < events.length; i++ ) {
      KEY_EVENT_RECORD keyEvent = events[i].keyEvent;
      //Log.trace(keyEvent.keyDown? "KEY_DOWN" : "KEY_UP", "key code:", keyEvent.keyCode, "char:", (long)keyEvent.uchar); 
      if (keyEvent.keyDown) {
        if (keyEvent.uchar > 0) {
          // support some C1 control sequences: ALT + [@-_] (and [a-z]?) => ESC <ascii>
          // http://en.wikipedia.org/wiki/C0_and_C1_control_codes#C1_set
          final int altState = KEY_EVENT_RECORD.LEFT_ALT_PRESSED | KEY_EVENT_RECORD.RIGHT_ALT_PRESSED;
          // Pressing "Alt Gr" is translated to Alt-Ctrl, hence it has to be checked that Ctrl is _not_ pressed,
          // otherwise inserting of "Alt Gr" codes on non-US keyboards would yield errors
          final int ctrlState = KEY_EVENT_RECORD.LEFT_CTRL_PRESSED | KEY_EVENT_RECORD.RIGHT_CTRL_PRESSED;
          if (((keyEvent.uchar >= '@' && keyEvent.uchar <= '_') || (keyEvent.uchar >= 'a' && keyEvent.uchar <= 'z'))
              && ((keyEvent.controlKeyState & altState) != 0) && ((keyEvent.controlKeyState & ctrlState) == 0)) {
            sb.append('\u001B'); // ESC
          }

          sb.append(keyEvent.uchar);
          continue;
        }
        // virtual keycodes: http://msdn.microsoft.com/en-us/library/windows/desktop/dd375731(v=vs.85).aspx
        // just add support for basic editing keys (no control state, no numpad keys)
        String escapeSequence = null;
        switch (keyEvent.keyCode) {
          case 0x21: // VK_PRIOR PageUp
            escapeSequence = "\u001B[5~";
            break;
          case 0x22: // VK_NEXT PageDown
            escapeSequence = "\u001B[6~";
            break;
          case 0x23: // VK_END
            escapeSequence = "\u001B[4~";
            break;
          case 0x24: // VK_HOME
            escapeSequence = "\u001B[1~";
            break;
          case 0x25: // VK_LEFT
            escapeSequence = "\u001B[D";
            break;
          case 0x26: // VK_UP
            escapeSequence = "\u001B[A";
            break;
          case 0x27: // VK_RIGHT
            escapeSequence = "\u001B[C";
            break;
          case 0x28: // VK_DOWN
            escapeSequence = "\u001B[B";
            break;
          case 0x2D: // VK_INSERT
            escapeSequence = "\u001B[2~";
            break;
          case 0x2E: // VK_DELETE
            escapeSequence = "\u001B[3~";
            break;
          default:
            break;
        }
        if (escapeSequence != null) {
          for (int k = 0; k < keyEvent.repeatCount; k++) {
            sb.append(escapeSequence);
          }
        }
      } else {
        // key up event
        // support ALT+NumPad input method
        if (keyEvent.keyCode == 0x12/*VK_MENU ALT key*/ && keyEvent.uchar > 0) {
          sb.append(keyEvent.uchar);
        }
      }
    }
    return sb.toString().getBytes();
  }
}
