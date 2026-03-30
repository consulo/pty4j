package com.pty4j.util;

import com.pty4j.TestUtil;
import com.sun.jna.Platform;
import org.junit.Assert;
import org.junit.Test;

public class PtyUtilTest {

  @Test
  public void findBundledNativeFile() {
    TestUtil.useLocalNativeLib(true);
    try {
      Assert.assertTrue(PtyUtil.resolveNativeFile(getLibraryName()).exists());
    }
    finally {
      TestUtil.useLocalNativeLib(false);
    }
  }

  private String getLibraryName() {
    if (Platform.isMac()) return "libpty.dylib";
    if (Platform.isWindows()) return "winpty.dll";
    return "libpty.so";
  }
}
