package com.github.tomaslanger.cliprogress;

import com.github.tomaslanger.chalk.Ansi;
import com.github.tomaslanger.chalk.Chalk;

import java.io.PrintStream;
import java.util.EnumSet;
import java.util.Set;

/**
 * In place progress bar.
 *
 * User: Tomas.Langer
 * Date: 20.12.2015
 * Time: 23:00
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
class ProgressBarInPlace extends ProgressBar {
    private static final String THREE_SPACES = "   ";

    /*
     * Runtime
     */
    //when in place, this is used to store the full line of the bar to print
    private final StringBuilder textToPrint = new StringBuilder();
    private final boolean keepSingleColor;
    private final boolean shouldPrintPercents;
    private final Ansi.Color statusColor;
    private final Ansi.BgColor statusBgColor;
    private final Set<Ansi.Modifier> statusModifiers = EnumSet.noneOf(Ansi.Modifier.class);
    private final StatusLoc statusLocation;

    private int printedChars;
    private String statusTextOnScreen;
    private int wantedPercentage;
    private int printedPercentage;

    private boolean isVisible;


    protected ProgressBarInPlace(final ProgressBar.Builder builder) {
        super(builder);
        this.keepSingleColor = builder.shouldKeepSingleColor();
        this.shouldPrintPercents = builder.shouldPrintPercents();
        this.statusColor = builder.getStatusColor();
        this.statusBgColor = builder.getStatusBgColor();
        this.statusModifiers.addAll(builder.getStatusModifiers());
        this.statusLocation = builder.getStatusLocation();
    }

    protected void printBar(final PrintStream out, final int progress) {
        int shouldPrintChars = (progress * charCount) / max;

        if (shouldPrintPercents) {
            wantedPercentage = (progress * 100) / max;
            if (wantedPercentage == 100 && progress != max) {
                //only set 100% if we are really done
                wantedPercentage = 99;
            }
        }

        if (!requiresUpdate(shouldPrintChars)) {
            return;
        }

        if (keepSingleColor) {
            StringBuilder colorless = new StringBuilder(shouldPrintChars);
            for (int i = 0; i < shouldPrintChars; i++) {
                colorless.append(progressChar);
            }

            textToPrint.setLength(0);
            textToPrint.append(chalked(colorless.toString()));
        } else {
            if (shouldPrintChars > printedChars) {
                for (int i = printedChars; i < shouldPrintChars; i++) {
                    textToPrint.append(chalked(progressChar));
                }
            }

            if (shouldPrintChars < printedChars) {
                textToPrint.delete(textToPrint.length() - (printedChars - shouldPrintChars), textToPrint.length());
            }
        }

        printProgressBar(out, shouldPrintChars);
        printedChars = shouldPrintChars;
    }

    private boolean requiresUpdate(final int shouldPrintChars) {
        if (shouldPrintChars != printedChars) {
            return true;
        }
        if (statusChanged()) {
            return true;
        }

        if (!isVisible) {
            return true;
        }

        return shouldPrintPercents && (wantedPercentage != printedPercentage);
    }


    @Override
    protected void printBarHeader(final PrintStream out) {
        //no-op, in place doesn't have a header
    }

    @Override
    protected void printBarEnd(final PrintStream out) {
        out.println();
        textToPrint.setLength(0);
        printedChars = 0;
        statusTextOnScreen = null;
    }

    @Override
    protected void printCancel(final PrintStream out) {
        //in place just terminates
        out.println();
    }

    private boolean statusChanged() {
        //this will return true if both are null... And if for any reason, they are same string instance, no problem
        //noinspection StringEquality
        if (wantedStatus == statusTextOnScreen) {
            return false;
        }

        if (null == statusTextOnScreen || null == wantedStatus) {
            return true;
        }

        return !statusTextOnScreen.equals(wantedStatus);
    }

    private void printProgressBar(final PrintStream out, final int shouldPrintChars) {
        if (isVisible) {
            if (statusLocation != StatusLoc.SAME_LINE) {
                //max two lines, I always move just one line up
                out.print(Ansi.AnsiCommand.CURSOR_UP);
            }
            out.print('\r');
        }

        //status if before progress bar
        if (statusLocation == StatusLoc.FIRST_LINE) {
            printStatus(out);
            out.println();
        }

        //the actual progress bar
        out.print(beginString);
        out.print(textToPrint);
        for (int i = shouldPrintChars; i < charCount; i++) {
            out.print(baseChar);
        }
        out.print(endString);

        //percentage if enabled
        if (shouldPrintPercents) {
            out.print(formatPercentage());
            printedPercentage = wantedPercentage;
        }

        //status after progress bar
        if (statusLocation == StatusLoc.SAME_LINE) {
            out.print(' ');
            printStatus(out);
        } else if (statusLocation == StatusLoc.LAST_LINE) {
            out.println();
            printStatus(out);
        }

        out.flush();

        isVisible = true;
    }

    private void printStatus(final PrintStream out) {
        if (wantedStatus != null) {
            out.print(statusChalked(wantedStatus));
        }
        //now remove trailing characters if previous status was longer than current
        int toPrint = charDif(statusTextOnScreen, wantedStatus);
        for (int i = 0; i < toPrint; i++) {
            out.print(" ");
        }
        statusTextOnScreen = wantedStatus;
    }


    private String formatPercentage() {
        if (wantedPercentage > 100) {
            wantedPercentage = 100;
        }
        String result = wantedPercentage + "%";

        String spaces = THREE_SPACES.substring(result.length() - 2);

        return spaces + statusChalked(result);
    }

    private int charDif(final String printedStatus, final String statusText) {
        if (null == printedStatus) {
            return 0;
        }
        if (null == statusText) {
            //add the space between status and bar
            return printedStatus.length() + 1;
        }

        return printedStatus.length() - statusText.length();
    }

    protected Chalk statusChalked(final String toChalk) {
        return statusChalked(Chalk.on(String.valueOf(toChalk)));
    }

    private Chalk statusChalked(final Chalk toChalk) {
        if (null != statusColor) {
            toChalk.apply(statusColor);
        }

        if (null != statusBgColor) {
            toChalk.apply(statusBgColor);
        }

        if (!statusModifiers.isEmpty()) {
            for (final Ansi.Modifier modifier : statusModifiers) {
                toChalk.apply(modifier);
            }

        }

        return toChalk;
    }

}
