package com.github.tomaslanger.cliprogress;

import com.github.tomaslanger.chalk.Ansi;
import com.github.tomaslanger.chalk.Chalk;

import java.io.PrintStream;

/**
 * Progress bar combination of master (overall progress) and detail (current task progress).
 *
 * User: Tomas.Langer
 * Date: 21.12.2015
 * Time: 13:22
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
public class ProgressBarMasterDetail extends ProgressBarBase {

    private final ProgressBar.Builder childBulder;

    private PrintStream out;
    private boolean isBatch;

    private ProgressBar master;
    private ProgressBar child;

    private int currentTaskProgress;
    private int overallProgress;

    protected ProgressBarMasterDetail(final Builder builder) {
        super(builder);

        this.isBatch = builder.isBatch() || !Chalk.isCommandEnabled();
        ProgressBar.Builder masterBuilder = builder.getMasterPbBuilder();
        if (isBatch) {
            masterBuilder.setBatch();
        }
        this.master = masterBuilder.build();
        this.childBulder = builder.getChildPbBuilder();
    }

    /**
     * Indicate that we will start next task in the sequence - will create a new child progress bar.
     *
     * @param max Maximum for the current task, to progress the child progress bar (and indirectly the master)
     * @param infoText Text to write above the child progress bar (such as "Downloading test.jpg, 0 KB of 1 MB done")
     */
    public void nextTask(final int max, final String infoText) {
        super.checkSetProgress();

        //overall progress now must contain the previous child progress
        overallProgress += currentTaskProgress;
        currentTaskProgress = 0;

        if (isBatch) {
            return;
        }

        if (null == child) {
            //first child, must go to next line from master bar
            out.println();
        } else {
            //clear status line
            child.setStatus(null);
            child.end();
            //compensate for last end of line after we finish the sub task
            out.print(Ansi.AnsiCommand.CURSOR_UP);
            out.print(Ansi.AnsiCommand.CURSOR_UP);
            out.print('\r');
        }

        //create new child progress bar
        child = childBulder.setMax(max).build();
        child.begin(out);

        child.setProgress(0, infoText);
    }

    /**
     * Set progress of current task. Automatically adjust progress of master (overall progress).
     *
     * @param currentTaskProgress Progress between 0 and current task max
     * @param infoText Text to write above the child progress bar (such as "Downloading test.jpg, 100 KB of 1 MB done")
     */
    public void setProgress(int currentTaskProgress, String infoText) {
        super.checkSetProgress();

        this.currentTaskProgress = currentTaskProgress;

        if (!isBatch) {
            out.print(Ansi.AnsiCommand.CURSOR_UP);
            out.print(Ansi.AnsiCommand.CURSOR_UP);
        }

        master.setProgress(overallProgress + this.currentTaskProgress);

        if (!isBatch) {
            out.print(Ansi.AnsiCommand.CURSOR_DOWN);
            out.print(Ansi.AnsiCommand.CURSOR_DOWN);
            child.setProgress(currentTaskProgress, infoText);
        }
    }

    @Override
    protected void finishProgressBar(final boolean isCancel) {
        out.println();
    }

    @Override
    protected void initProgressBar() {
        master.begin(out);
        master.setProgress(0);
    }

    @Override
    protected void setOut(final PrintStream printStream) {
        this.out = printStream;
    }

    /**
     * Builds instances for you.
     */
    public static class Builder extends ProgressBarBase.Builder<ProgressBarMasterDetail.Builder> {
        private ProgressBar.Builder masterPbBuilder;
        private ProgressBar.Builder childPbBuilder;

        /**
         * Must have set overall maximum (sum of all max of all children). Otherwise it will not measure progress
         * correctly.
         * Master pb will not have status published.
         *
         * @param masterPbBuilder Builder for main instance of progress bar (master)
         * @return Builder this instance
         */
        public Builder setMasterPbBuilder(final ProgressBar.Builder masterPbBuilder) {
            this.masterPbBuilder = masterPbBuilder;
            return this;
        }

        /**
         * Status location and max will be ignored. Status location will be set to first line, max will be configured
         * for each child task as they are created.
         *
         * @param childPbBuilder Builder for child instances of progress bars (detail)
         * @return Builder this instance
         */
        public Builder setChildPbBuilder(final ProgressBar.Builder childPbBuilder) {
            this.childPbBuilder = childPbBuilder;

            return this;
        }

        private ProgressBar.Builder getMasterPbBuilder() {
            return masterPbBuilder;
        }

        private ProgressBar.Builder getChildPbBuilder() {
            return childPbBuilder;
        }

        /**
         * Build the progress bar instance with settings configured with this builder.
         *
         * @return ProgressBarMasterDetail configured
         */
        public ProgressBarMasterDetail build() {
            masterPbBuilder.claimNoOuts().setStatusLocation(StatusLoc.SAME_LINE);
            childPbBuilder.claimNoOuts().setStatusLocation(StatusLoc.FIRST_LINE);

            return new ProgressBarMasterDetail(this);
        }
    }
}
