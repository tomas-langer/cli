package com.github.tomaslanger.cli.progress;

import com.github.tomaslanger.chalk.Chalk;

import java.io.PrintStream;
import java.util.Map;

/**
 * Shared stuff for all progress bars.
 *
 * User: Tomas.Langer
 * Date: 21.12.2015
 * Time: 17:08
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
abstract class ProgressBarBase {
    protected static final boolean ONE_LINE_ONLY;
    protected static final boolean NO_COLOR;

    static {
        //initialize native if not yet done
        Chalk.on("init");
        ONE_LINE_ONLY = !Chalk.isCommandEnabled();
        NO_COLOR = !Chalk.isColorEnabled();
    }

    private final boolean claimStdout;
    private final boolean claimStderr;

    private boolean started;
    protected boolean iOwnOutput;
    private StreamHandler.Replace replace;

    protected ProgressBarBase(Builder builder) {
        this.claimStdout = builder.shouldClaimStdout();
        this.claimStderr = builder.shouldClaimStderr();
    }

    public final synchronized void begin() {
        if (started) {
            throw new IllegalStateException("Cannot start a started progress bar.");
        }
        //redirect out and error streams until progress finishes
        this.replace = StreamHandler.replace(claimStdout, claimStderr, this::setOut);

        switch(replace) {
            case IMPOSSIBLE:
                iOwnOutput = false;
                break;
            case DONE:
            case NOT_REPLACED:
                iOwnOutput = true;
                break;
        }

        if (iOwnOutput) {
            //print the progress bar header
            initProgressBar();
        }

        started = true;
    }

    /**
     * Initialized elsewhere, just accept this stream.
     * @param out Print stream to use.
     */
    public final synchronized void begin(PrintStream out) {
        if (started) {
            throw new IllegalStateException("Cannot start a started progress bar.");
        }
        replace = StreamHandler.Replace.NOT_REPLACED;
        iOwnOutput = true;

        setOut(out);
        initProgressBar();

        started = true;

    }

    /**
     * If we claimed standard and/or error outputs, we must release them back. Otherwise we are in deep trouble...
     * This would cause an out of memory if left unchecked!!!!
     * Always end your progress bars in finally block, unless you max cancel it.
     * End can be called multiple times (so you can cancel and then end in finally).
     */
    public final synchronized void end() {
        if (!started) {
            return;
        }

        finishProgressBar(false);

        StreamHandler.replaceBack(replace);

        iOwnOutput = false;
        started = false;
    }

    /**
     * Cancel progress bar. Will do whatever must be done to finish and go to next line.
     */
    public final void cancel() {
        if (!started) {
            return;
        }

        finishProgressBar(true);
        StreamHandler.replaceBack(replace);

        iOwnOutput = false;
        started = false;
    }

    protected void checkSetProgress() {
        if (!started) {
            begin();
        }
    }

    /**
     * Finish this progress bar and all associated stuff.
     * @param isCancel If set to true, do not change progress. If false, set progress to max
     */
    protected abstract void finishProgressBar(boolean isCancel);

    /**
     * This method will be called if we can safely use the output stream once at the beginning.
     *
     */
    protected abstract void initProgressBar();

    /**
     * Set PrintStream to use for all operations of this progress bar.
     *
     * @param printStream toUse - may be System.out or some other stream
     */
    protected abstract void setOut(final PrintStream printStream);

    @SuppressWarnings("unchecked")
    protected static class Builder<T extends Builder>  {
        private boolean isBatch;
        private boolean claimStdout = true;
        private boolean claimStderr = true;

        protected Builder() {
            this.isBatch = Boolean.getBoolean("cliprogress.isBatch");
            //If we are in jenkins or hudson, use batch by default
            Map<String, String> env = System.getenv();
            if (env.containsKey("HUDSON_URL") || env.containsKey("JENKINS_URL")) {
                this.isBatch = true;
            }
        }

        /**
         * If set, standard output and error are shared with any processes running in parallel. As this would
         * break our progress bar, if anybody printed stuff, we usually want to claim them for ourselves.
         * But this setting allows you to say "I know it is safe, just do not claim it". If set, end() does not
         * have to be called on the progress bar, unless you want to re-use it.
         *
         * @return Builder instance
         */
        public T claimNoOuts() {
            doNotClaimStderr();
            doNotClaimStdout();

            return (T) this;
        }

        /**
         * Set that this is a batch processing environment. In such a case, colors would be used, but progress bars
         * are going to be simplier (as I cannot go back to beginning of line).
         *
         * @return Builder instance
         */
        public T setBatch() {
            this.isBatch = true;

            return (T) this;
        }

        /**
         * I do not care about parallel processing printing to my progress bar, just let them!
         *
         * @return Builder instance
         */
        public T doNotClaimStdout() {
            this.claimStdout = false;
            return (T) this;
        }

        /**
         * I do not care about parallel processing printing to my progress bar, just let them!
         *
         * @return Builder instance
         */
        public T doNotClaimStderr() {
            this.claimStderr = false;
            return (T) this;
        }

        boolean isBatch() {
            return isBatch;
        }

        boolean shouldClaimStdout() {
            return claimStdout;
        }

        boolean shouldClaimStderr() {
            return claimStderr;
        }
    }
}
