# cli-progress
> Progress bars for java command line with support for nice coloring even on windows

Cli-progress is focusing on cross-platform progress bars for command line

Progress bars should work:
* In Ux terminals supporting ANSI coloring
* In IDEs supporting ANSI coloring (such as IntelliJ Idea) with limited support (single line)
* In CI tools supporting ANSI coloring (such as Jenkins CI with AnsiColor plugin) (single line)
* In Windows command line (yay!)
    * Utilizing the [Jansi library](https://github.com/fusesource/jansi)
* In any other standard output without coloring and/or in batch mode

## Why
I want java tools to have the same possibilities as javascript.
npm and related tools have nice progress bars...
Java does not have this possibility.

## Install
As soon as first release is available, we shall have it on Maven central.

     <dependency>
       <groupId>com.github.tomas-langer.cli</groupId>
       <artifactId>cli-progress</artifactId>
       <version>1.0.1</version>
     </dependency>

This project has one dependency - com.github.tomas-langer:chalk for colors

# Usage
The "usual" progress bar for showing download progress

```java
    ProgressBar pb = new ProgressBar.Builder().build();
    pb.begin();

    try {
        for (int i = 0; i <= 100; i++) {
            pb.setProgress(i, "Downloading " + i + " KB of 100 KB");
            Thread.sleep(50);
        }
    } finally {
        // By default the standard output is claimed, so bar is not interrupted by other processes printing
        // we must return the original standard output, or all System.out.* will fail afterwords
        pb.end();
    }
    System.out.println("Finished downloading");
```

will show:

![progress_bar](https://cloud.githubusercontent.com/assets/13766491/11954075/b27b1924-a8a5-11e5-8da2-aa136c0ea9d3.gif)

## Other options
Configuration options are all on builder and some on ProgressBar itself.

```java
ProgressBar.Builder builder = new ProgressBar.Builder();
builder
    .setBatch() // configure batch (e.g. forward configure only, no status)
    .setBatchHeader(true); //print a separate line to show the length of bar
builder
    .setMax(250) //set maximum for progress (default is 100) - for example set the size of a file to download
    .setCharCount(50) //length of the progress bar (default is 32)
    .setBaseChar('-') //character to print for unprocessed part (default is underline '_')
    .setProgressChar('*') //character to print for processed part (default is space, colored with background color)
    .setStatusLocation(StatusLoc.SAME_LINE) //print status after the progress bar (default is on separate line before)
    .setStatusColor(Ansi.Color.CYAN) //status will be colored in cyan
    .setKeepSingleColor(true) //if color is changed on progress bar, it will change the whole bar (default changes only from the progress location at time of change)
    .noColors() //disable colors, only print characters
    .setBeginString("" + Chalk.on("begin").cyan() + Chalk.on(":::").bgGreen().white()) //string to print before the progress bar, colored example
    .setEndString(":::end") //string to print after the progress bar
    .disablePercents() //disable automatic printing of progress percentage
    .claimNoOuts(); //will not replace standard and error outputs. Use in case you are quite certain nobody will debug messages to standard output during processing

ProgressBar pb = builder.build();
pb.setProgress(24, "Just a bit longer to go."); //set progress and status
pb.setProgress(35); //keep status, update progress
pb.setStatus("Something is different"); //keep progress, update status
pb.setStatus(null); //clear status
```
### Master/Detail progress bar
This progress bar allows you to have a "master" progress with child tasks that have their own progress bars.

```java
// Prepare builders - masterBuilder will be used once to create the master progress bar
ProgressBar.Builder masterBuilder = new ProgressBar.Builder().setBgColor(Ansi.BgColor.RED).setMax(overallMax);
// Child builder will be used repeatedly, each time setting the max to max of the next task
ProgressBar.Builder childBuilder = new ProgressBar.Builder();
// Master detail builder combines these two together
ProgressBarMasterDetail.Builder builder = new ProgressBarMasterDetail.Builder().
                setMasterPbBuilder(masterBuilder).
                setChildPbBuilder(childBuilder);
ProgressBarMasterDetail pb = builder.build();
pb.begin();
try {
    for (int i = 0; i < subCount; i++) {
        pbmd.nextTask(max, "Task " + i);
        for (int j = 0; j <= max; j++) {
            //this will set progress of the child task and update progress of the master progress bar
            pb.setProgress(j, "Task " + i + ", progress: " + j + "    ");
            Thread.sleep(5);
        }
    }
} finally {
  pb.end();
}
```
## Configuration
There are some system properties to control behavior:
* jansi.strip - if set to "true", colors will not be sent to output (on any environment)
* jansi.passthrough - if set to "true", ANSI escapes will be sent to output (on any environment, including Windows)
* idea.launcher.bin.path - if set, I know I am running from IntelliJ Idea, will expect output to be ANSI compliant, but single line only
* cliprogress.isBatch - if set to "true" batch processing will be done (no carriage returns, no multiline processing)

The following CI tools are currently recognized:
* Hudson - if in Hudson, ANSI escapes are passed through (requires AnsiColor plugin), single line expected
* Jenkins - if in Jenkins, ANSI escapes are passed through (requires AnsiColor plugin), single line expected



# License

[Apache License](http://www.apache.org/licenses/)