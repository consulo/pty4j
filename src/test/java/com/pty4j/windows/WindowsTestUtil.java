package com.pty4j.windows;

import com.pty4j.TestUtil;

import java.util.Arrays;
import java.util.List;

class WindowsTestUtil {

  /**
   * @param ps1FileRelativePath path to .ps1 file relative to testData folder
   */
  static List<String> getPowerShellScriptCommand(String ps1FileRelativePath) {
    // https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_powershell_exe
    return Arrays.asList(
      "powershell.exe",
      "-ExecutionPolicy", "Bypass",
      "-File", TestUtil.getTestDataFilePath(ps1FileRelativePath)
    );
  }
}
