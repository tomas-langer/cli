package com.github.tomaslanger.clibeauty;

import com.github.tomaslanger.chalk.Ansi;

/**
 * TODO: Javadoc
 * User: Tomas.Langer
 * Date: 18.12.2015
 * Time: 13:49
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
public class Features {
    public static void main(String[] args) throws InterruptedException {
        new Features().showFeatures();
    }

    private void showFeatures() throws InterruptedException {
        System.out.println("Simple batch mode:");
        ProgressBar pb = new ProgressBar().setMax(100).setBatch(true).setBgColor(Ansi.BgColor.YELLOW).setBeginString("").setEndString("").setBatchHeader(false);
        executeProgressingTask(pb, false);

        System.out.println("Batch mode:");
        pb = new ProgressBar().setMax(100).setBatch(true).setBgColor(Ansi.BgColor.GREEN);
        executeProgressingTask(pb, false);

        System.out.println("In place mode:");
        pb = new ProgressBar().setMax(100).setBgColor(Ansi.BgColor.GREEN).setBatch(false);
        executeProgressingTask(pb, false);

        System.out.println("Fully customized batch with no header:");
        pb = new ProgressBar().setMax(100).setBgColor(Ansi.BgColor.GREEN).setColor(Ansi.Color.RED).setBatch(true);
        pb.setBaseChar('*');
        pb.setBatchHeader(false);
        pb.setBeginString("begin:::");
        pb.setEndString(":::end");
        pb.setCharCount(50);
        pb.setProgressChar('-');
        pb.addModifier(Ansi.Modifier.BOLD);
        executeProgressingTask(pb, false);

        System.out.println("Fully customized in-place:");
        pb = new ProgressBar().setMax(100).setBgColor(Ansi.BgColor.MAGENTA).setColor(Ansi.Color.BLACK).setBatch(false);
        pb.setBaseChar('*');
        pb.setBeginString("begin:::");
        pb.setEndString(":::end");
        pb.setCharCount(50);
        pb.setProgressChar('-');
        executeProgressingTask(pb, false);


        System.out.println("Conflicting progress bars (in front - green, conflicting in background - red)");
        final ProgressBar backPb = new ProgressBar().setBgColor(Ansi.BgColor.RED);
        ProgressBar frontPb = new ProgressBar();
        frontPb.begin();
        Thread thread = new Thread(() -> executeProgressingTask(backPb, false));
        thread.start();
        executeProgressingTask(frontPb, true);


        //cancel
        System.out.println("Cancelled in place progress in 50%");
        pb = new ProgressBar();
        executeAndCancel(pb);
        System.out.println("Cancelled batch progress in 50%");
        pb = new ProgressBar().setBatch(true);
        executeAndCancel(pb);

        System.out.println("Finished");
    }

    private void executeAndCancel(final ProgressBar pb) {
        pb.begin();

        try {
            for (int progress = 0; progress < 50; progress += 1) {
                pb.setProgress(progress);
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
        } finally {
            pb.cancel();
        }
    }

    private void executeProgressingTask(final ProgressBar pb, final boolean started) {
        if (!started) {
            pb.begin();
        }

        try {
            for (int progress = 0; progress < 100; progress += 1) {
                pb.setProgress(progress);
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
        } finally {
            pb.finish();
        }
    }
}
