package com.github.tomaslanger.cli.progress;

import com.github.tomaslanger.chalk.Ansi;
import com.github.tomaslanger.chalk.Chalk;

import java.io.PrintStream;
import java.util.EnumSet;
import java.util.Set;

/**
 * ProgressBar allows you to print nice progressing colored bar to use in standard output console, as long
 * as it supports ANSI coloring or is a Windows console.
 * If colors are not supported, you can still use it, just as a character progress bar.
 * <p>
 * User: Tomas.Langer
 * Date: 18.12.2015
 * Time: 13:53
 * <p>
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
public abstract class ProgressBar extends ProgressBarBase {
    protected final int charCount;
    protected final String beginString;
    protected final String endString;
    protected final char baseChar;
    protected final int max;

    protected char progressChar;
    protected Ansi.Color color;
    protected Ansi.BgColor bgColor;
    protected final Set<Ansi.Modifier> modifiers = EnumSet.noneOf(Ansi.Modifier.class);

    protected int wantedProgress;
    protected String wantedStatus;
    private PrintStream sysOut;

    protected ProgressBar(Builder builder) {
        super(builder);

        this.charCount = builder.charCount;
        this.beginString = builder.beginString;
        this.endString = builder.endString;

        this.baseChar = builder.baseChar;
        this.progressChar = builder.progressChar;
        this.color = builder.color;
        this.bgColor = builder.bgColor;
        this.modifiers.addAll(builder.modifiers);
        this.max = builder.max;
    }

    /**
     * Set character to be printed that shows the already done progress. Default is space for colored output and '*'
     * for no colors.
     * Modifies the output on the fly, unless {@link Builder#setKeepSingleColor(boolean)} was used in builder and
     * this is not a batch.
     *
     * @param progressChar character to print as progress increases
     */
    public void setProgressChar(final char progressChar) {
        this.progressChar = progressChar;
    }

    /**
     * Set foreground color of characters that show progress. Default is none, as colored output uses space character
     * and background color.
     * Modifies the output on the fly, unless {@link Builder#setKeepSingleColor(boolean)} was used in builder and
     * this is not a batch.
     *
     * @param color foreground color to set
     */
    public void setFgColor(final Ansi.Color color) {
        this.color = color;
    }

    /**
     * Set background color of characters that show progress. This is the usual way to change progress bar color.
     * <p>
     * Modifies the output on the fly, unless {@link Builder#setKeepSingleColor(boolean)} was used in builder and
     * this is not a batch.
     *
     * @param color foreground color to set
     */
    public void setBgColor(final Ansi.BgColor color) {
        this.bgColor = color;
    }

    /**
     * Add modifier for characters that show progress. As we use {@link #setBgColor(Ansi.BgColor)} as normal way
     * for coloring output, this is useful only when using custom {@link #setProgressChar(char)}.
     * <p>
     * Modifies the output on the fly, unless {@link Builder#setKeepSingleColor(boolean)} was used in builder and
     * this is not a batch.
     *
     * @param modifier modifier to add to output. Note that underlining on Windows just creates different background...
     */
    public void addModifier(final Ansi.Modifier modifier) {
        this.modifiers.add(modifier);
    }

    protected void setOut(PrintStream out) {
        this.sysOut = out;
    }

    protected abstract void printBarHeader(final PrintStream out);

    protected Chalk chalked(final String toChalk) {
        return chalked(Chalk.on(toChalk));
    }

    protected Chalk chalked(final char toChalk) {
        return chalked(Chalk.on(String.valueOf(toChalk)));
    }

    private Chalk chalked(final Chalk toChalk) {
        if (null != color) {
            toChalk.apply(color);
        }

        if (null != bgColor) {
            toChalk.apply(bgColor);
        }

        if (!modifiers.isEmpty()) {
            for (final Ansi.Modifier modifier : modifiers) {
                toChalk.apply(modifier);
            }

        }

        return toChalk;
    }

    /**
     * Set progress of this bar. This may or may not change the progress or percentage on screen, depends on
     * {@link Builder#setMax(int)} and {@link Builder#setCharCount(int)}.
     *
     * @param progress Progress between 0 and {@link Builder#setMax(int)}.
     */
    public synchronized void setProgress(final int progress) {
        this.setProgress(progress, wantedStatus);
    }

    /**
     * Set status to be printed according to {@link Builder#setStatusLocation(StatusLoc)}. By default status
     * has same color as progress bar. It is not printed when running in batch environment.
     *
     * @param status Status to print (may be colored using {@link com.github.tomaslanger.chalk.Chalk})
     */
    public synchronized void setStatus(final String status) {
        setProgress(wantedProgress, status);
    }

    /**
     * Set progress and status of this bar. Combination of {@link #setProgress(int)} and {@link #setStatus(String)}.
     *
     * @param progress progress Progress between 0 and {@link Builder#setMax(int)}.
     * @param status   Status to print (may be colored using {@link com.github.tomaslanger.chalk.Chalk})
     */
    public synchronized void setProgress(final int progress, final String status) {
        super.checkSetProgress();

        this.wantedStatus = status;
        this.wantedProgress = progress;

        if (!iOwnOutput) {
            return;
        }

        if (progress > max) {
            setProgress(max);
        } else {
            printBar(sysOut, progress);
        }
    }

    protected abstract void printBar(final PrintStream out, int progress);

    @Override
    protected void finishProgressBar(final boolean isCancel) {
        if (iOwnOutput) {
            if (isCancel) {
                printCancel(sysOut);
            } else {
                setProgress(max);
                printBarEnd(sysOut);
            }
        } else {
            printBarHeader(System.out);
            printBar(System.out, (isCancel ? wantedProgress : max));
            printBarEnd(System.out);
        }

        wantedProgress = 0;
        wantedStatus = null;
    }

    @Override
    protected void initProgressBar() {
        printBarHeader(sysOut);
    }

    protected abstract void printBarEnd(final PrintStream out);

    protected abstract void printCancel(final PrintStream out);

    public int getMax() {
        return max;
    }

    public static class Builder extends ProgressBarBase.Builder<ProgressBar.Builder> {
        /*
         * Common properties
         */
        private int charCount = 32;
        private String beginString = "";
        private char baseChar = '_';
        private String endString = "";
        private char progressChar = ' ';
        private Ansi.Color color;
        private Ansi.BgColor bgColor = Ansi.BgColor.GREEN;
        private Set<Ansi.Modifier> modifiers = EnumSet.noneOf(Ansi.Modifier.class);

        private int max = 100;

        /*
         * Batch properties
         */
        private boolean batchHeader = false;

        /*
         * In place properties
         */
        private boolean keepSingleColor = false;
        private boolean printPercents = true;
        private Ansi.Color statusColor;
        private Ansi.BgColor statusBgColor;
        private Set<Ansi.Modifier> statusModifiers = EnumSet.noneOf(Ansi.Modifier.class);
        private StatusLoc statusLocation = StatusLoc.FIRST_LINE;

        public Builder() {
        }

        /**
         * Only valid for in-place progress bars (not for batch). Batch progress bars always keep different
         * colors if changed during progress.
         *
         * @param keepSingleColor Set to true to change the progress bar from first to last valid progressing character.
         *                        Default is false, in which case one bar can have multiple colors if changed during progress.
         * @return Builder instance
         */
        public Builder setKeepSingleColor(final boolean keepSingleColor) {
            this.keepSingleColor = keepSingleColor;

            return this;
        }

        public boolean shouldKeepSingleColor() {
            return keepSingleColor;
        }

        public StatusLoc getStatusLocation() {
            return statusLocation;
        }

        /**
         * Where to print status when not running in a batch. Options are:
         * <ul>
         * <li>{@link StatusLoc#FIRST_LINE} - status will be printed on a separate line above the progress bar. This is default.</li>
         * <li>{@link StatusLoc#SAME_LINE} - status will be printed on the same line as the progress bar, after it</li>
         * <li>{@link StatusLoc#LAST_LINE} - status will be printed on a separate line below the progress bar</li>
         * </ul>
         *
         * @param statusLocation StatusLoc to indicate where to place the status
         * @return Builder instance
         */
        public Builder setStatusLocation(final StatusLoc statusLocation) {
            this.statusLocation = statusLocation;

            return this;
        }

        /**
         * If called, percentage will not be printed for in-place progress bars.
         *
         * @return Builder instance
         */
        public Builder disablePercents() {
            printPercents = false;

            return this;
        }

        public boolean shouldPrintPercents() {
            return printPercents;
        }

        /**
         * Set the max progress of this bar. Used to calculate progress of the bar itself and to calculate percentage
         * of progress.
         *
         * @param max max value that can be sent in {@link #setProgress(int)} or {@link #setProgress(int, String)}
         * @return Builder instance
         */
        public Builder setMax(final int max) {
            this.max = max;

            return this;
        }

        /**
         * Set whether to print batch header or not. Batch header is a line indicating the width of the progress
         * bar, that is on the next line, so you can see how far the progress is.
         * <p>
         * Example:<br>
         * <span>__________</span> <i> This is the header line</i><br>
         * <span style="background-color: #FF0000;">_______</span> <i> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;This is the progress bar line</i>
         * </p>
         *
         * @param batchHeader whether to print header or not
         * @return Builder instance
         */
        public Builder setBatchHeader(final boolean batchHeader) {
            this.batchHeader = batchHeader;
            return this;
        }

        /**
         * Number of characters of the progress bar (width of the bar).
         *
         * @param charCount number of characters to print when progress is 100%
         * @return Builder instance
         */
        public Builder setCharCount(final int charCount) {
            this.charCount = charCount;
            return this;
        }

        /**
         * Prefix string, printed before header and progress bar lines.
         * <p>
         * Example for prefix '[':<br>
         * <span>[__________</span> <i> This is the header line</i><br>
         * [<span style="background-color: #FF0000;">_______</span> <i> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;This is the progress bar line</i>
         * </p>
         *
         * @param beginString String to print at line start, may be colored using {@link Chalk}.
         * @return Builder instance
         */
        public Builder setBeginString(final String beginString) {
            this.beginString = beginString;
            return this;
        }

        /**
         * Character used to print the remaining progress (for in-place bars) or header line (for batch).
         * <p>
         * Example for char '-' and in-place bar:<br>
         * <span style="background-color: #FF0000;">_______</span>----
         * </p>
         *
         * @param baseChar character to print for remaining progress
         * @return Builder instance
         */
        public Builder setBaseChar(final char baseChar) {
            this.baseChar = baseChar;
            return this;
        }

        /**
         * Suffix string, printed after header and progress bar lines.
         * <p>
         * Example for postfix ']':<br>
         * <span>__________]</span> <i> This is the header line</i><br>
         * <span style="background-color: #FF0000;">__________</span>] <i>This is the progress bar line</i>
         * </p>
         *
         * @param endString String to print at line end, may be colored using {@link Chalk}.
         * @return Builder instance
         */
        public Builder setEndString(final String endString) {
            this.endString = endString;
            return this;
        }

        /**
         * Character used to print the progress.
         * <p>
         * Example for char '*' and in-place bar:<br>
         * <span style="background-color: #FF0000;">*******</span>----
         * </p>
         *
         * @param progressChar character to print for progress. Default is space (as we use background color).
         * @return Builder instance
         */
        public Builder setProgressChar(final char progressChar) {
            this.progressChar = progressChar;
            return this;
        }

        /**
         * Set foreground color of progress character. By default this is not used, as we use background color.
         *
         * @param color foreground color to use
         * @return Builder instance
         */
        public Builder setFgColor(final Ansi.Color color) {
            this.color = color;

            return this;
        }

        /**
         * Set background color of progress bar (and hence the color of it). As the default character is space
         * setting background color will set the color of the bar.
         *
         * @param color Background color to set
         * @return Builder instance
         */
        public Builder setBgColor(final Ansi.BgColor color) {
            this.bgColor = color;

            return this;
        }

        /**
         * Add modifier for characters that show progress. As we use {@link #setBgColor(Ansi.BgColor)} as normal way
         * for coloring output, this is useful only when using custom {@link #setProgressChar(char)}.
         *
         * @param modifier modifier to add to output. Note that underlining on Windows just creates different background...
         * @return Builder instance
         */
        public Builder addModifier(final Ansi.Modifier modifier) {
            this.modifiers.add(modifier);

            return this;
        }

        /**
         * When called, the progress char is set to '*', base char to '-' and colors are removed.
         * If you add colors after calling this method, they would be added back...
         *
         * @return Builder instance
         */
        public Builder noColors() {
            return setBgColor(null).setProgressChar('*').setBaseChar('-').setStatusColor(null);
        }

        public ProgressBar build() {
            if (NO_COLOR) {
                noColors();
            }
            if (ONE_LINE_ONLY) {
                setStatusLocation(StatusLoc.SAME_LINE);
            }
            if (isBatch()) {
                return new ProgressBarBatch(this);
            }

            return new ProgressBarInPlace(this);
        }

        public boolean isBatchHeader() {
            return batchHeader;
        }

        public int getCharCount() {
            return charCount;
        }

        public String getBeginString() {
            return beginString;
        }

        public char getBaseChar() {
            return baseChar;
        }

        public String getEndString() {
            return endString;
        }

        public char getProgressChar() {
            return progressChar;
        }

        public Ansi.Color getColor() {
            return color;
        }

        public Ansi.BgColor getBgColor() {
            return bgColor;
        }

        public Set<Ansi.Modifier> getModifiers() {
            return modifiers;
        }


        public int getMax() {
            return max;
        }

        public Ansi.Color getStatusColor() {
            return statusColor;
        }

        /**
         * Set foreground color of status text.
         *
         * @param statusColor Color to use
         * @return Builder instance
         */
        public Builder setStatusColor(final Ansi.Color statusColor) {
            this.statusColor = statusColor;

            return this;
        }

        public Ansi.BgColor getStatusBgColor() {
            return statusBgColor;
        }

        /**
         * Set background color of status text
         * @param statusBgColor Background color
         * @return Builder instance
         */
        public Builder setStatusBgColor(final Ansi.BgColor statusBgColor) {
            this.statusBgColor = statusBgColor;

            return this;
        }

        public void addStatusModifier(final Ansi.Modifier modifier) {
            this.statusModifiers.add(modifier);
        }

        public Set<Ansi.Modifier> getStatusModifiers() {
            return statusModifiers;
        }

        public boolean isPrintPercents() {
            return printPercents;
        }

        public boolean isKeepSingleColor() {
            return keepSingleColor;
        }
    }
}
