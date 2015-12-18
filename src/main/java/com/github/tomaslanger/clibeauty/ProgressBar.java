package com.github.tomaslanger.clibeauty;

import com.github.tomaslanger.chalk.Ansi;
import com.github.tomaslanger.chalk.Chalk;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.EnumSet;
import java.util.Set;

/**
 * TODO: Javadoc
 * User: Tomas.Langer
 * Date: 18.12.2015
 * Time: 13:53
 *
 * TODO if I do not own the system out, do not print progress!§!
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
public class ProgressBar {
    private static boolean replaced;
    private static ByteArrayOutputStream stdOutBuffer = new ByteArrayOutputStream();
    private static ByteArrayOutputStream stdErrBuffer = new ByteArrayOutputStream();
    private static PrintStream sysOut;
    private static PrintStream sysErr;

    /*
     * Configuration of output
     */
    private boolean batchHeader = true;
    private int charCount = 25;
    private String beginString = "[";
    private char baseChar = '-';
    private String endString = "]";
    private char progressChar = ' ';
    private Ansi.Color color;
    private Ansi.BgColor bgColor = Ansi.BgColor.GREEN;
    private Set<Ansi.Modifier> modifiers = EnumSet.noneOf(Ansi.Modifier.class);

    private boolean batch;

    /*
     * Configuration of progress bar
     */
    private int max = 100;

    /*
     * Runtime
     */
    private boolean iOwnIt;
    private int printedChars = 0;
    private boolean printedBegin = false;
    private int currentProgress;

    public ProgressBar() {
        //this line is needed to initialize the native stuff on windows...
        Chalk.on("init");
    }

    public ProgressBar setMax(final int max) {
        this.max = max;

        return this;
    }

    public ProgressBar setBatchHeader(final boolean batchHeader) {
        this.batchHeader = batchHeader;
        return this;
    }

    public ProgressBar setCharCount(final int charCount) {
        this.charCount = charCount;
        return this;
    }

    public ProgressBar setBeginString(final String beginString) {
        this.beginString = beginString;
        return this;
    }

    public ProgressBar setBaseChar(final char baseChar) {
        this.baseChar = baseChar;
        return this;
    }

    public ProgressBar setEndString(final String endString) {
        this.endString = endString;
        return this;
    }

    public ProgressBar setProgressChar(final char progressChar) {
        this.progressChar = progressChar;
        return this;
    }

    public ProgressBar setColor(final Ansi.Color color) {
        this.color = color;

        return this;
    }

    public ProgressBar setBgColor(final Ansi.BgColor color) {
        this.bgColor = color;

        return this;
    }

    public ProgressBar addModifier(final Ansi.Modifier modifier) {
        this.modifiers.add(modifier);

        return this;
    }

    public ProgressBar setBatch(final boolean batch) {
        this.batch = batch;

        return this;
    }

    private static synchronized boolean replace() {
        if (replaced) {
            return false;
        }

        sysOut = System.out;
        sysErr = System.err;

        System.setOut(new PrintStream(stdOutBuffer));
        System.setErr(new PrintStream(stdErrBuffer));

        replaced = true;

        return true;
    }

    private static synchronized void replaceBack() {
        if (!replaced) {
            return;
        }

        replaced = false;

        System.setOut(sysOut);
        System.setErr(sysErr);

        System.out.println(new String(stdOutBuffer.toByteArray()));
        System.err.println(new String(stdErrBuffer.toByteArray()));

        sysOut = null;
        sysErr = null;

        stdOutBuffer = new ByteArrayOutputStream();
        stdErrBuffer = new ByteArrayOutputStream();
    }

    public void begin() {
        //redirect out and error streams until progress finishes
        iOwnIt = replace();

        if (iOwnIt) {
            //print the progress bar
            if (!batch || batchHeader) {
                printProgressBar(sysOut, 0);
            }
        }
    }

    private void printProgressBar(final PrintStream out, final int progress) {
        out.print(beginString);
        for (int i = 0; i < progress; i++) {
            out.print(chalked(progressChar));
        }
        for (int i = progress; i < charCount; i++) {
            out.print(baseChar);
        }
        out.print(endString);
        out.flush();
    }

    private Chalk chalked(final char progressChar) {
        Chalk result = Chalk.on(String.valueOf(progressChar));

        if (null != color) {
            result = result.apply(color);
        }

        if (null != bgColor) {
            result = result.apply(bgColor);
        }

        if (!modifiers.isEmpty()) {
            for (final Ansi.Modifier modifier : modifiers) {
                result = result.apply(modifier);
            }

        }

        return result;
    }

    public void setProgress(final int progress) {
        this.currentProgress = progress;

        if (!iOwnIt) {
            return;
        }

        if (progress > max) {
            setProgress(max);
        } else {
            int shouldPrintChars = (progress * charCount) / max;
            printIt(shouldPrintChars);
        }
    }

    private void printIt(final int shouldPrintChars) {
        if (printedChars == 0 && !printedBegin) {
            if (batch) {
                //end line, start a new one
                if (batchHeader) {
                    sysOut.println();
                }
                sysOut.print(beginString);
            }

            printedBegin = true;
            sysOut.flush();
        }
        if (shouldPrintChars == printedChars) {
            return;
        }

        if (batch) {
            if (shouldPrintChars > printedChars) {
                for (int i = printedChars; i < shouldPrintChars; i++) {
                    sysOut.print(chalked(progressChar));
                }
                sysOut.flush();
                printedChars = shouldPrintChars;
            }
        } else {
            sysOut.print('\r');
            printProgressBar(sysOut, shouldPrintChars);
            printedChars = shouldPrintChars;
        }

        if (shouldPrintChars == charCount) {
            if (batch) {
                sysOut.println(endString);
            } else {
                sysOut.println();
            }
            sysOut.flush();
        }
    }

    public void finish() {
        if (iOwnIt) {
            setProgress(max);
            replaceBack();
        } else {
            printProgressBar(System.out, charCount);
            System.out.println();
        }
    }

    public void cancel() {
        if (iOwnIt) {
            if (batch) {
                if (charCount > printedChars) {
                    for (int i = printedChars; i < charCount; i++) {
                        sysOut.print(baseChar);
                    }
                    sysOut.println(endString);
                    sysOut.flush();
                }
            }
            replaceBack();
        } else {
            printProgressBar(System.out, currentProgress);
            System.out.println();
        }
    }
}
