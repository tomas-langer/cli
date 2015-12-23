package com.github.tomaslanger.cli.progress;

import com.github.tomaslanger.chalk.Ansi;
import com.github.tomaslanger.chalk.Chalk;
/**
 * Prints examples of most features of this library.
 * User: Tomas.Langer
 * Date: 18.12.2015
 * Time: 13:49
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
public class Features {
    public static void main(String[] args) throws InterruptedException {
        Chalk.on("init");

        Features f = new Features();
//        f.recording();

        f.masterDetail(false);
        f.masterDetail(true);

        f.replicateBowerSummary();

        //progress bars
        f.progressBars();
        //progress bar with master/detail


        //allows user to select (checkbox or radio buttons)
        //f.inputChoice();
    }

    private void recording() throws InterruptedException {
        System.out.println();
        System.out.println();

        ProgressBar pb = new ProgressBar.Builder().build();

        pb.begin();
        try {
            for (int i = 0; i <= 100; i++) {
                pb.setProgress(i, "Downloading " + i + " KB of 100 KB");
                Thread.sleep(50);
            }
        } finally {
            pb.end();
        }

        System.out.println("Finished downloading");
    }

    private void masterDetail(boolean isBatch) throws InterruptedException {
        System.out.println();
        System.out.println("***************************************************");
        System.out.println("** Master/detail progress bars: " + (isBatch?"batch":"inplace"));
        System.out.println("***************************************************");
        System.out.println();


        int subCount = 10;
        int max = 100;
        int overallMax = max * subCount;

        ProgressBar.Builder masterBuilder = new ProgressBar.Builder().setBgColor(Ansi.BgColor.RED).setMax(overallMax);
        ProgressBar.Builder childBuilder = new ProgressBar.Builder();

        ProgressBarMasterDetail.Builder builder = new ProgressBarMasterDetail.Builder().
                setMasterPbBuilder(masterBuilder).
                setChildPbBuilder(childBuilder);
        if (isBatch) {
            builder.setBatch();
        }
        ProgressBarMasterDetail pbmd = builder.build();


        pbmd.begin();


        for (int i = 0; i < subCount; i++) {
            pbmd.nextTask(max, "Task " + i);
            for (int j = 0; j <= max; j++) {
                pbmd.setProgress(j, "Task " + i + ", progress: " + j + "    ");
                Thread.sleep(5);
            }
        }

        pbmd.end();
    }

    private void replicateBowerSummary() {
        System.out.println();
        System.out.println("***************************************************");
        System.out.println("** Example for java script lovers - Grunt        **");
        System.out.println("***************************************************");
        System.out.println();

        System.out.println("[user@host]$ grunt build");
        System.out.println("Running \"clean:dist\" (clean) task");
        System.out.println(Chalk.on("Warning: Cannot delete files outside the current working directory. Use --force to continue.").yellow());
        System.out.println();
        System.out.println(Chalk.on("Aborted due to warnings.").red());
        System.out.println();
        System.out.println();
        System.out.println(Chalk.on("Execution Time (2015-12-21 09:08:31 UTC)").white());
        String begin1 = "loading tasks   " + Chalk.on("20ms  ").blue();
        ProgressBar pb = new ProgressBar.Builder().setCharCount(20).setBeginString(begin1).setStatusColor(Ansi.Color.BLUE).setBgColor(Ansi.BgColor.BLUE).disablePercents().setStatusLocation(StatusLoc.SAME_LINE).build();
        pb.begin();
        pb.setProgress(100, "20%");
        pb.end();
        String begin2 = "clean:dist      " + Chalk.on("70ms  ").blue();
        pb = new ProgressBar.Builder().setCharCount(77).setBeginString(begin2).setStatusColor(Ansi.Color.BLUE).setBgColor(Ansi.BgColor.BLUE).disablePercents().setStatusLocation(StatusLoc.SAME_LINE).build();
        pb.begin();
        pb.setProgress(100, "77%");
        pb.end();

        System.out.println(Chalk.on("Total 101ms").magenta());
    }


    private void progressBars() throws InterruptedException {
        System.out.println();
        System.out.println("***************************************************");
        System.out.println("** Various types of progress bars                **");
        System.out.println("***************************************************");
        System.out.println();

        System.out.println("Default behavior:");
        ProgressBar pb = new ProgressBar.Builder().build();
        executeProgressingTask(pb, false);

        System.out.println("Default behavior, batch:");
        pb = new ProgressBar.Builder().setBatch().build();
        executeProgressingTask(pb, false);

        System.out.println("Batch with header:");
        pb = new ProgressBar.Builder().setBatch().setBatchHeader(true).build();
        executeProgressingTask(pb, false);

        System.out.println("No percentage:");
        pb = new ProgressBar.Builder().disablePercents().build();
        executeProgressingTask(pb, false);

        System.out.println("Customized max, status before:");
        pb = new ProgressBar.Builder().
                setMax(250).
                setStatusLocation(StatusLoc.FIRST_LINE).
                build();
        executeDifferentMax(pb);

        System.out.println("Customized max, status after:");
        pb = new ProgressBar.Builder().
                setMax(250).
                setStatusLocation(StatusLoc.LAST_LINE).
                build();
        executeDifferentMax(pb);

        System.out.println("Changing colors and character:");
        pb = new ProgressBar.Builder().build();
        executeProgressingTaskChangeColor(pb);

        System.out.println("Changing colors whole bar:");
        pb = new ProgressBar.Builder().setKeepSingleColor(true).build();
        executeProgressingTaskChangeColor(pb);

        System.out.println("Character only (no colors):");
        pb = new ProgressBar.Builder().noColors().build();
        executeProgressingTask(pb, false);

        System.out.println("Fully customized batch with no header:");
        ProgressBar.Builder builder = new ProgressBar.Builder();
        builder.setMax(100).setBgColor(Ansi.BgColor.GREEN).setFgColor(Ansi.Color.RED).setBatch().build();

        builder.setBaseChar('*');
        builder.setBatchHeader(false);
        builder.setBeginString("begin:::");
        builder.setEndString(":::end");
        builder.setCharCount(50);
        builder.setProgressChar('-');
        builder.addModifier(Ansi.Modifier.BOLD);

        pb = builder.build();
        executeProgressingTask(pb, false);

        System.out.println("Fully customized in-place:");
        builder = new ProgressBar.Builder().setMax(100).setBgColor(Ansi.BgColor.MAGENTA).setFgColor(Ansi.Color.BLACK);
        builder.setBaseChar('*');
        builder.setBeginString("" + Chalk.on("begin").cyan() + Chalk.on(":::").bgGreen().white());
        builder.setEndString(":::end");
        builder.setCharCount(50);
        builder.setProgressChar('-');
        pb = builder.build();
        executeProgressingTask(pb, false);


        System.out.println("Conflicting progress bars (in front - green, conflicting in background - red)");
        final ProgressBar backPb = new ProgressBar.Builder().setBgColor(Ansi.BgColor.RED).build();
        ProgressBar frontPb = new ProgressBar.Builder().build();
        frontPb.begin();
        Thread thread = new Thread(() -> executeProgressingTask(backPb, false));
        thread.start();
        executeProgressingTask(frontPb, true);

        Thread.sleep(2000);

        //cancel
        System.out.println("Cancelled in place progress in 50%");
        pb = new ProgressBar.Builder().build();
        executeAndCancel(pb, 50);
        System.out.println("Cancelled batch progress in 20%");
        pb = new ProgressBar.Builder().setBatch().build();
        executeAndCancel(pb, 20);


        System.out.println();
        System.out.println(Chalk.on("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!").red());
        System.out.println("This is what happens if you do not replace stdout....");
        System.out.println("Conflicting progress bars, not protecting output (does not require end to be called)");
        final ProgressBar backPb2 = new ProgressBar.Builder().setBgColor(Ansi.BgColor.RED).claimNoOuts().build();
        frontPb = new ProgressBar.Builder().claimNoOuts().build();
        frontPb.begin();
        thread = new Thread(() -> executeProgressingTask(backPb2, false));
        thread.start();
        executeProgressingTask(frontPb, true);

        System.out.println("Finished");
    }

    private void executeAndCancel(final ProgressBar pb, final int max) {
        pb.begin();

        try {
            for (int progress = 0; progress <= max; progress += 1) {
                pb.setProgress(progress);
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
        } finally {
            pb.cancel();
        }
    }

    private static void executeDifferentMax(final ProgressBar pb) {
        pb.begin();
        try {
            for (int progress = 0; progress < pb.getMax(); progress += 1) {
                pb.setProgress(progress, "Progress " + Chalk.on("at").yellow() + " " + progress);
                Thread.sleep(20);
            }
            pb.setProgress(pb.getMax(), "Progress " + Chalk.on("at").yellow() + " " + pb.getMax());
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
        } finally {
            pb.end();
        }
    }

    private static void executeProgressingTask(final ProgressBar pb, final boolean started) {
        if (!started) {
            pb.begin();
        }

        try {
            for (int progress = 0; progress < 100; progress += 1) {
                pb.setProgress(progress);
                Thread.sleep(30);
            }
            pb.setStatus("Finished");
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
        } finally {
            pb.end();
        }
    }

    private static void executeProgressingTaskChangeColor(final ProgressBar pb) {
        pb.begin();

        try {
            for (int progress = 0; progress < 100; progress += 1) {
                if (progress < 30) {
                    pb.setBgColor(Ansi.BgColor.RED);
                } else if (progress < 70) {
                    pb.setBgColor(Ansi.BgColor.CYAN);
                    pb.setProgressChar('*');
                } else {
                    pb.setBgColor(Ansi.BgColor.GREEN);
                    pb.setProgressChar('o');
                }

                pb.setProgress(progress);
                Thread.sleep(30);
            }
            pb.setStatus("Finished");
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
        } finally {
            pb.end();
        }
    }
}
