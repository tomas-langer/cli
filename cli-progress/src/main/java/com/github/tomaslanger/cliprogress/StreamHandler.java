package com.github.tomaslanger.cliprogress;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

/**
 * Utility to handle buffering and replacement of system and error outputs.
 *
 * User: Tomas.Langer
 * Date: 21.12.2015
 * Time: 17:01
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
class StreamHandler {
    private static boolean replaced;
    private static ByteArrayOutputStream stdOutBuffer = new ByteArrayOutputStream();
    private static ByteArrayOutputStream stdErrBuffer = new ByteArrayOutputStream();
    private static PrintStream sysOut;
    private static PrintStream sysErr;

    static synchronized boolean replace(final boolean replaceOut, final boolean replaceErr, final Consumer<PrintStream> setter) {
        if (replaced) {
            //currently configured system output (writing to one of my buffers above)
            setter.accept(System.out);
            return !(replaceOut || replaceErr);
        }

        sysOut = System.out;
        sysErr = System.err;

        if (replaceOut) {
            System.setOut(new PrintStream(stdOutBuffer));
        }
        if (replaceErr) {
            System.setErr(new PrintStream(stdErrBuffer));
        }

        setter.accept(sysOut);

        replaced = replaceOut || replaceErr;

        return true;
    }

    static synchronized void replaceBack() {
        if (!replaced) {
            return;
        }

        replaced = false;

        System.setOut(sysOut);
        System.setErr(sysErr);

        if (stdOutBuffer.size() > 0) {
            System.out.println(new String(stdOutBuffer.toByteArray()));
        }
        if (stdErrBuffer.size() > 0) {
            System.err.println(new String(stdErrBuffer.toByteArray()));
        }

        sysOut = null;
        sysErr = null;

        stdOutBuffer = new ByteArrayOutputStream();
        stdErrBuffer = new ByteArrayOutputStream();
    }

}
