package com.pty4j;

import com.jetbrains.signatureverifier.PeFile;
import com.jetbrains.signatureverifier.Resources;
import com.jetbrains.signatureverifier.SignatureData;
import com.jetbrains.signatureverifier.crypt.*;
import com.jetbrains.signatureverifier.macho.MachoArch;
import com.jetbrains.signatureverifier.macho.MachoFile;
import com.jetbrains.util.filetype.FileProperties;
import com.jetbrains.util.filetype.FileType;
import com.jetbrains.util.filetype.FileTypeDetector;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SignaturesTest {

  @Rule
  public JUnitSoftAssertions asserts = new JUnitSoftAssertions();

  @Test
  public void testMacOSHelpersSigned() throws Exception {
    Path root = Path.of("os/darwin");
    List<Path> natives;
    try (Stream<Path> walk = Files.walk(root)) {
      natives = walk.filter(Files::isRegularFile).collect(Collectors.toList());
    }
    try (InputStream defaultRootsStream = Resources.getDefaultRoots()) {
      SignatureVerificationParams verificationParams = new SignatureVerificationParams(
        defaultRootsStream, null, true, false
      );
      for (Path path : natives) {
        AbstractMap.SimpleImmutableEntry<FileType, EnumSet<FileProperties>> fileType;
        try (SeekableByteChannel fs = Files.newByteChannel(path)) {
          fileType = FileTypeDetector.detectFileType(fs);
        }
        System.out.println(root.relativize(path) + ": " + fileType);
        asserts.assertThat(fileType.getKey()).isEqualTo(FileType.MachO);
        verifyFatMacho(path, verificationParams);
      }
    }
  }

  @Test
  public void testWindowsHelpersSigned() throws Exception {
    Path root = Path.of("os/win");
    List<Path> natives;
    try (Stream<Path> walk = Files.walk(root)) {
      natives = new ArrayList<>(walk.filter(Files::isRegularFile).collect(Collectors.toList()));
    }
    natives.remove(root.resolve("x86-64/cyglaunch.exe"));
    try (InputStream defaultRootsStream = Resources.getDefaultRoots()) {
      SignatureVerificationParams verificationParams = new SignatureVerificationParams(
        defaultRootsStream, null, true, false
      );
      for (Path path : natives) {
        AbstractMap.SimpleImmutableEntry<FileType, EnumSet<FileProperties>> fileType;
        try (SeekableByteChannel fs = Files.newByteChannel(path)) {
          fileType = FileTypeDetector.detectFileType(fs);
        }
        System.out.println(root.relativize(path) + ": " + fileType);
        asserts.assertThat(fileType.getKey()).isEqualTo(FileType.Pe);
        verifyPortableExecutable(path, verificationParams);
      }
    }
  }

  private void verifyFatMacho(Path pathToExecutable,
                               SignatureVerificationParams verificationParams) throws Exception {
    try (SeekableByteChannel fs = Files.newByteChannel(pathToExecutable)) {
      MachoArch machoArch = new MachoArch(fs);
      for (MachoFile executable : machoArch.extract()) {
        VerifySignatureResult result = verifySignature(executable.getSignatureData(), verificationParams, pathToExecutable.toString());
        checkResult(result, pathToExecutable);
      }
    }
  }

  private void verifyPortableExecutable(Path pathToExecutable,
                                        SignatureVerificationParams verificationParams) throws Exception {
    try (SeekableByteChannel fs = Files.newByteChannel(pathToExecutable)) {
      PeFile executable = new PeFile(fs);
      VerifySignatureResult result = verifySignature(executable.getSignatureData(), verificationParams, pathToExecutable.toString());
      checkResult(result, pathToExecutable);
    }
  }

  private VerifySignatureResult verifySignature(SignatureData signatureData,
                                                SignatureVerificationParams verificationParams,
                                                String path) throws Exception {
    if (signatureData.isEmpty()) {
      return new VerifySignatureResult(VerifySignatureStatus.InvalidSignature, "No signature data found in '" + path + "'");
    }
    SignedMessage signedMessage = SignedMessage.createInstance(signatureData);
    SignedMessageVerifier signedMessageVerifier = new SignedMessageVerifier();
    return signedMessageVerifier.verifySignatureAsync(signedMessage, verificationParams);
  }

  private void checkResult(VerifySignatureResult result, Path pathToExecutable) {
    if (result.getStatus() == VerifySignatureStatus.Valid) {
      System.out.println(pathToExecutable + ": Signature is OK!");
    }
    else {
      asserts.fail(pathToExecutable + ": Signature is invalid! " + result.getMessage());
    }
  }
}
