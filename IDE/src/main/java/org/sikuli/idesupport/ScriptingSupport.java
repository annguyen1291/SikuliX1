/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.idesupport;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.ImageIcon;

import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.RunTime;
import org.sikuli.script.support.Runner;
import org.sikuli.script.Sikulix;
import org.sikuli.script.runners.*;

public class ScriptingSupport {

  private static final String me = "ScriptingSupport: ";
  private static final int lvl = 3;
  private static void log(int level, String message, Object... args) {
    Debug.logx(level, me + message, args);
  }

  private static final Class<?>[] IDE_RUNNER_CLASSES = new Class<?>[]{JythonRunner.class, PythonRunner.class,
      JRubyRunner.class, JavaScriptRunner.class, TextRunner.class};
  private static final List<IScriptRunner> IDE_RUNNERS = new ArrayList<>();

  public static void init() {
    synchronized(IDE_RUNNERS) {
      if(IDE_RUNNERS.isEmpty()) {
        log(lvl, "enter");

        List<IScriptRunner> runners = Runner.getRunners();

        for (Class<?> runnerClass : IDE_RUNNER_CLASSES) {
          for (IScriptRunner runner : runners) {
            if(runnerClass.equals(runner.getClass())) {
              log(lvl, "added: %s", runner.getName());
              IDE_RUNNERS.add(runner);
              break;
            }
          }
        }

        if (IDE_RUNNERS.isEmpty()) {
          String em = "Terminating: No scripting support available. Rerun Setup!";
          log(-1, em);
          Sikulix.popError(em, "IDE has problems ...");
          System.exit(1);
        }

        IScriptRunner defaultRunner = IDE_RUNNERS.get(0);
        log(lvl, "exit: defaultrunner: %s (%s)", defaultRunner.getName(), defaultRunner.getExtensions()[0]);
      }
    }
  }

  public static String getDefaultExtension() {
    return getDefaultRunner().getExtensions()[0];
  }

  public static IScriptRunner getDefaultRunner() {
    return Runner.getRunner(RunTime.getDefaultRunnerType());
  }

  public static synchronized List<IScriptRunner> getRunners(){
    synchronized(IDE_RUNNERS) {
      init();
      return new ArrayList<IScriptRunner>(IDE_RUNNERS);
    }
  }

  public static boolean transferScript(String src, String dest) {
    log(lvl, "transferScript: %s\nto: %s", src, dest);
    FileManager.FileFilter filter = new FileManager.FileFilter() {
      @Override
      public boolean accept(File entry) {
        if (entry.getName().endsWith(".html")) {
          return false;
        } else if (entry.getName().endsWith(".$py.class")) {
          return false;
        } else {
          for (String ending : Runner.getExtensions()) {
            if (entry.getName().endsWith("." + ending)) {
              return false;
            }
          }
        }
        return true;
      }
    };
    try {
      FileManager.xcopy(src, dest, filter);
    } catch (IOException ex) {
      log(-1, "transferScript: IOError: %s", ex.getMessage(), src, dest);
      return false;
    }
    log(lvl, "transferScript: completed");
    return true;
  }
}
