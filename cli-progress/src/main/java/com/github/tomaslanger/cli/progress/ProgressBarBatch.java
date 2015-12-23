package com.github.tomaslanger.cli.progress;

import java.io.PrintStream;

/**
 * Batch progress bar.
 *
 * User: Tomas.Langer
 * Date: 20.12.2015
 * Time: 23:12
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
class ProgressBarBatch extends ProgressBar {
    protected final boolean batchHeader;

    private int printedChars;
    private boolean isBeginStringPrinted;

    protected ProgressBarBatch(final Builder builder) {
        super(builder);
        this.batchHeader = builder.isBatchHeader();
    }

    @Override
    protected void printBarHeader(final PrintStream out) {
        if (batchHeader) {
            out.print(beginString);

            for (int i = 0; i < charCount; i++) {
                out.print(baseChar);
            }
            out.print(endString);
            out.flush();
        }
    }


    @Override
    protected void printBar(final PrintStream out, final int progress) {
        int shouldPrintChars = (progress * charCount) / max;

        if (printedChars == 0 && !isBeginStringPrinted) {
            if (batchHeader) {
                out.println();
            }
            out.print(beginString);

            isBeginStringPrinted = true;
            out.flush();
        }

        if (shouldPrintChars == printedChars) {
            return;
        }

        if (shouldPrintChars > printedChars) {
            for (int i = printedChars; i < shouldPrintChars; i++) {
                out.print(chalked(progressChar));
            }
            out.flush();
            printedChars = shouldPrintChars;
        }
    }

    @Override
    protected void printBarEnd(final PrintStream out) {
        out.println(endString);
        out.flush();
        printedChars = 0;
        isBeginStringPrinted = false;
    }

    @Override
    protected void printCancel(final PrintStream out) {
        if (charCount > printedChars) {
            for (int i = printedChars; i < charCount; i++) {
                out.print(baseChar);
            }
            out.println(endString);
            out.flush();
        }
    }
}
