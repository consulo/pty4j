package com.pty4j.unix;

import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import com.pty4j.util.PtyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ProcessBuilderUnixLauncher {

  private static final Logger LOG = LoggerFactory.getLogger(ProcessBuilderUnixLauncher.class);

  private final Process process;

  ProcessBuilderUnixLauncher(List<String> command,
                             Map<String, String> environmentMap,
                             String workingDirectory,
                             Pty pty,
                             Pty errPty,
                             boolean consoleMode,
                             Integer initialColumns,
                             Integer initialRows,
                             PtyProcess ptyProcess) throws Exception {
    File spawnHelper = PtyUtil.resolveNativeFile("pty4j-unix-spawn-helper");
    ProcessBuilder builder = new ProcessBuilder();
    List<String> cmd = new ArrayList<>();
    cmd.add(spawnHelper.getAbsolutePath());
    cmd.add(workingDirectory);
    cmd.add(consoleMode ? "1" : "0");
    cmd.add(pty.getSlaveName());
    cmd.add(String.valueOf(pty.getMasterFD()));
    cmd.add(errPty != null ? errPty.getSlaveName() : "");
    cmd.add(String.valueOf(errPty != null ? errPty.getMasterFD() : -1));
    cmd.addAll(command);
    builder.command(cmd);
    builder.environment().clear();
    builder.environment().putAll(environmentMap);
    builder.directory(new File(workingDirectory));
    builder.redirectInput(ProcessBuilder.Redirect.from(new File("/dev/null")));
    builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
    if (errPty == null) {
      builder.redirectErrorStream(true);
    }
    else {
      builder.redirectError(ProcessBuilder.Redirect.DISCARD);
    }
    process = builder.start();

    initTermSize(pty, ptyProcess, new WinSize(initialColumns != null ? initialColumns : 80,
                                             initialRows != null ? initialRows : 25));
  }

  Process getProcess() {
    return process;
  }

  private void initTermSize(Pty pty, PtyProcess ptyProcess, WinSize initSize) {
    // Pty will be fully initialized after `open(slave_name, O_RDWR)` in the child process.
    // Until it happens, resize attempts will fail with `ENOTTY`.
    long startNanos = System.nanoTime();
    UnixPtyException lastException = null;
    int performedAttempts = 0;
    for (int attempt = 1; attempt <= 1000; attempt++) {
      try {
        performedAttempts++;
        pty.setWindowSize(initSize, ptyProcess);
        lastException = null;
        break;
      }
      catch (UnixPtyException e) {
        lastException = e;
        if (e.getErrno() != CLibrary.ENOTTY) {
          break;
        }
        try {
          Thread.sleep(2);
        }
        catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }
    if (lastException != null) {
      LOG.warn("Failed to set initial terminal size, attempts: " + performedAttempts, lastException);
    }
    else if (LOG.isDebugEnabled()) {
      long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
      LOG.debug("Terminal initial size set to (" + initSize + ") in " + elapsedMs + "ms, attempt: " + performedAttempts);
    }
  }
}
