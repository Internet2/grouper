/**
 * Copyright 2014 Internet2
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
/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import  bsh.Interpreter;
import  java.io.BufferedReader;
import  java.io.FileNotFoundException;
import  java.io.FileReader;
import java.io.InputStream;
import  java.io.InputStreamReader;
import  java.io.IOException;
import java.io.StringReader;
import  java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * {@link GrouperShell} Shell Command Reader.
 * <p/>
 * @author  blair christensen.
 * @since   0.1.1
 */
class ShellCommandReader implements CommandReader {

  // PRIVATE INSTANCE VARIABLES //
  private Interpreter     i       = null;
  private BufferedReader  in      = null;
  private String          prompt  = null;


  /**
   * 
   * @param args
   * @param inputStreamParam
   * @throws GrouperShellException
   */
  protected ShellCommandReader(String[] args, InputStream inputStreamParam) 
    throws  GrouperShellException
  {
	StringBuffer preEval=null;

    if (args != null && args.length > 0 && StringUtils.equals(args[0], "-runarg")) {
      if (args.length != 2) {
        for (int i=0;i<GrouperUtil.length(args);i++) {
          System.out.println("args[" + i + "] is: '" + args[i] + "'");
        }
        throw new RuntimeException("When passing -runarg, pass one other argument, the gsh command to run");
      }
      String commands = args[1];
      //if \\n was in there, then make it a newline...
      commands = commands.replace("\\n", "\n");
      this.in = new BufferedReader(new StringReader(commands));
      System.out.println("Running command(s):\n\n" + commands + "\n\n");
      
    } else if (args != null && args.length > 0 && !args[0].equalsIgnoreCase("-check")) {
      String file = args[0];
      if ("-".equals( file )) {
        this.in = new BufferedReader( new InputStreamReader(System.in) );
      }
      else if("-main".equals(file)){
    	  String cName = args[1];
    	  preEval=new StringBuffer();
    	  preEval.append("p(\"#Making command line args available\")\n");
    	  preEval.append("args=new String[" + (args.length-2)+ "]\n");
    	  for(int i=2;i<args.length;i++) {
    		  preEval.append("args["+(i-2)+"]=\""+args[i]+"\"\n");
    	  }
    	  preEval.append("p(\"#Starting "+ cName+"...\")\n");
    	  preEval.append(cName + ".main(args)\n");
    	  preEval.append("p(\"#Finished!\")\n");
    	  this.in = new BufferedReader(new StringReader(preEval.toString()));
      }
      else {
        try {
          this.in = new BufferedReader( new FileReader(file) );
        }
        catch (FileNotFoundException eFNF) {
          throw new GrouperShellException(eFNF);
        }
      }
    } else {
      if (inputStreamParam != null) {
        this.in = new BufferedReader(new InputStreamReader(inputStreamParam));
      } else {
        this.in     = new BufferedReader( new InputStreamReader(System.in) );
        this.prompt = GrouperShell.NAME + " ";
      }
    }
    this.i = new Interpreter(this.in, System.out, System.err, false);
    
  } // protected ShellCommandReader(args)


  // PUBLIC INSTANCE  METHODS //

  /**
   * @since   0.1.1
   */
  public Interpreter getInterpreter() 
    throws  GrouperShellException
  {
    if (this.i == null) {
      throw new GrouperShellException(GshErrorMessages.I_NULL);
    }
    return this.i;
  } // public Interpreter getInterpreter()

  /**
   * @since   0.1.1
   */
  public String getNext() 
    throws  GrouperShellException
  {
    if (this.prompt != null) {
      this.i.print( this.prompt + this._getCnt() + "% " );
    }
    try {
      return this.in.readLine();
    }
    catch (IOException eIO) {
      throw new GrouperShellException(eIO);
    }
  } // public String getNext()


  // PRIVATE INSTANCE METHODS //

  // @since   0.0.1
  private int _getCnt() {
    int cnt = 0;
    try {
      List history = GrouperShell.getHistory(this.i);
      cnt = history.size();
    }
    catch (bsh.EvalError eBEE) {
      this.i.error(eBEE.getMessage());
    }
    return cnt;
  } // protected int _getCnt()

} // class ShellCommandReader implements CommandReader

