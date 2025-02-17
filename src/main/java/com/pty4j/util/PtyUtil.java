package com.pty4j.util;

import com.pty4j.windows.WinPty;
import com.sun.jna.Platform;

import java.io.File;
import java.net.URI;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author traff
 */
public class PtyUtil {
    private final static String PTY_LIB_FOLDER = System.getenv("PTY_LIB_FOLDER");

    public static String[] toStringArray(Map<String, String> environment) {
        if (environment == null) {
            return new String[0];
        }
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : environment.entrySet()) {
            list.add(entry.getKey() + "=" + entry.getValue());
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Returns the folder that contains a jar that contains the class
     *
     * @param aclass a class to find a jar
     * @return
     */
    public static String getJarContainingFolderPath(Class aclass) throws Exception {
        CodeSource codeSource = aclass.getProtectionDomain().getCodeSource();

        File jarFile;

        String urlLocation;
        if (codeSource.getLocation() != null) {
            urlLocation = codeSource.getLocation().getFile();
        }
        else {
            urlLocation = aclass.getResource(aclass.getSimpleName() + ".class").getPath();
        }

        int startIndex = urlLocation.indexOf(":") + 1;
        int endIndex = urlLocation.indexOf("!");
        if (startIndex == -1 || endIndex == -1) {
            throw new IllegalStateException("Class " + aclass.getSimpleName() + " is located not within a jar: " + urlLocation);
        }
        String jarFilePath = urlLocation.substring(startIndex, endIndex);
        jarFilePath = new URI(jarFilePath).getPath();
        jarFile = new File(jarFilePath);
        return jarFile.getParentFile().getAbsolutePath();
    }

    public static String getPtyLibFolderPath() throws Exception {
        if (PTY_LIB_FOLDER != null) {
            return PTY_LIB_FOLDER;
        }
        //Class aclass = WinPty.class.getClassLoader().loadClass("com.jediterm.pty.PtyMain");
        Class aclass = WinPty.class;

        return getJarContainingFolderPath(aclass);
    }

    public static File resolveNativeLibrary() throws Exception {
        String libFolderPath = getPtyLibFolderPath();

        if (libFolderPath != null) {

            File libFolder = new File(libFolderPath);
            File lib = resolveNativeLibrary(libFolder);

            lib = lib.exists() ? lib : resolveNativeLibrary(new File(libFolder, "libpty"));

            if (!lib.exists()) {
                lib = resolveNativeLibrary(new File(libFolder.getParentFile(), "libpty"));
            }

            if (!lib.exists()) {
                throw new IllegalStateException(String.format("Couldn't find %s, jar folder %s", lib.getName(),
                    libFolder.getAbsolutePath()));
            }

            return lib;
        }
        else {
            throw new IllegalStateException("Couldn't detect lib folder");
        }
    }

    public static File resolveNativeLibrary(File parent) {
        return resolveNativeFile(parent, getNativeLibraryName());
    }

    public static File resolveNativeFile(String fileName) throws Exception {
        File libFolder = new File(getPtyLibFolderPath());
        File file = resolveNativeFile(libFolder, fileName);
        return file.exists() ? file : resolveNativeFile(new File(libFolder, "libpty"), fileName);
    }

    public static File resolveNativeFile(File parent, String fileName) {
        final File path = new File(parent, getPlatformFolder());

        String arch = Platform.ARCH;

        if (new File(parent, arch).exists()) {
            return new File(new File(parent, arch), fileName);
        }
        else {
            return new File(new File(path, arch), fileName);
        }
    }

    private static String getPlatformFolder() {
        String result;

        if (Platform.isMac()) {
            result = "macosx";
        }
        else if (Platform.isWindows()) {
            result = "win";
        }
        else if (Platform.isLinux()) {
            result = "linux";
        }
        else if (Platform.isFreeBSD()) {
            result = "freebsd";
        }
        else if (Platform.isOpenBSD()) {
            result = "openbsd";
        }
        else {
            throw new IllegalStateException("Platform " + Platform.getOSType() + " is not supported");
        }

        return result;
    }

    private static String getNativeLibraryName() {
        String result;

        if (Platform.isMac()) {
            result = "libpty.dylib";
        }
        else if (Platform.isWindows()) {
            result = "winpty.dll";
        }
        else if (Platform.isLinux() || Platform.isFreeBSD() || Platform.isOpenBSD()) {
            result = "libpty.so";
        }
        else {
            throw new IllegalStateException("Platform " + Platform.getOSType() + " is not supported");
        }

        return result;
    }
}
