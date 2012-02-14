// Copyright (C) 2012 Chris Newell
//
// This file is part of MyMediaLite.
//
// MyMediaLite is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// MyMediaLite is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with MyMediaLite.  If not, see <http://www.gnu.org/licenses/>.

package org.mymedialite.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Class providing exception handlers.
 * @version 2.03
 */
public class Handlers implements UncaughtExceptionHandler {

  /**
   * React to an unhandled exception.
   * 
   * Give out the error message and the stack trace, then terminate the program.
   * IOExceptions get special treatment.
   * 
   * @param sender the thread issuing the exception
   * @param unhandledException the unhandled exception
   */
  @Override
  public void uncaughtException(Thread sender, Throwable unhandledException) {

    try {   
      if (unhandledException instanceof IOException) {
        System.err.println(unhandledException.getMessage());
        System.exit(-1);
      } else if(unhandledException instanceof OutOfMemoryError) {
        System.err.println("Out of memory!");
        System.err.println(unhandledException.getMessage());
        System.exit(-1);        
      }
      
      System.err.println();
      System.err.println("******************************************************************************");
      System.err.println("*** An uncaught exception occured. Please report the issue with details to ***");
      System.err.println("*** issue tracker: https://github.com/zenogantner/MyMediaLiteJava/issues   ***");
      System.err.println("******************************************************************************");
      System.err.println(unhandledException.getMessage());
      unhandledException.printStackTrace();
      System.err.println ("Terminate on unhandled exception.");
    } finally {
      System.exit(-1);
    }
  }

}
