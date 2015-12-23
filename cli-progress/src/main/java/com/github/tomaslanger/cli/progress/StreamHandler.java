package com.github.tomaslanger.cli.progress;

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
    enum Replace {
        /**
         * Somebody else owns the replaced streams
         */
        IMPOSSIBLE,
        /**
         * You own the replaced streams
         */
        DONE,
        /**
         * Streams not replaced
         */
        NOT_REPLACED
    }
    private static boolean replaced;
    private static ByteArrayOutputStream stdOutBuffer = new ByteArrayOutputStream();
    private static ByteArrayOutputStream stdErrBuffer = new ByteArrayOutputStream();
    private static PrintStream sysOut;
    private static PrintStream sysErr;

    static synchronized Replace replace(final boolean replaceOut, final boolean replaceErr, final Consumer<PrintStream> setter) {
        if (replaced) {
            //currently configured system output (writing to one of my buffers above)
            setter.accept(System.out);

            return Replace.IMPOSSIBLE;
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

        return (replaced? Replace.DONE: Replace.NOT_REPLACED);
    }

    static synchronized void replaceBack(final Replace replace) {
        switch(replace) {
            case IMPOSSIBLE:
            case NOT_REPLACED:
                return;
        }

        //this should not be possible, maybe somebody called me twice?
        if (!replaced) {
            return;
        }

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

        replaced = false;
    }

}
