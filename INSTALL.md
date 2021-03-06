# Installing BamQC

BamQC is a java application.  In order to run it needs your system to have a suitable
Java Runtime Environment (JRE) installed.  Before you try to run BamQC you should therefore
ensure that you have a suitable JRE.  There are a number of different JREs available
however the ones we have tested are the v1.6 and v1.7 JREs from Oracle (also called Java6
and Java7).  These are available for a number of different platforms from www.java.com 
(click the download now button at the top).  If you are a running a 64 bit operating system
please check that you download the 64 bit version of the JRE (this will probably not be 
the default download offered to you).

If you're not sure whether you have java installed then you can test this from a command
prompt.  To get a command prompt try:

**Windows:** Select _Start > Run_, and type `cmd` in the box which appears, press OK

**MaxOSX:** Run _Applications > Utilities > Terminal_

**Linux:** From your applications menu look for an application called _Terminal_ or _Konsole_.
Either of these will give you a usable shell.

At the command prompt type `java -version` and press enter.  You should see something like:

```
java version "1.6.0_17"
Java(TM) SE Runtime Environment (build 1.6.0_17-b04-248-10M3025)
Java HotSpot(TM) Client VM (build 14.3-b01-101, mixed mode)
```

If you get an error then you don't have java installed.  If the version listed on the first
line is less than 1.6 then you might have problems running BamQC.

Actually installing BamQC is as simple as unzipping the zip file it comes in into a
suitable location.  That's it.  Once unzipped it's ready to go.

## Running BamQC
You can run BamQC in one of two modes, either as an interactive graphical application
in which you can dynamically load FastQ files and view their results.

Alternatively you can run BamQC in a non-interactive mode where you specify the files
you want to process on the command line and BamQC will generate an HTML report for
each file without launching a user interface.  This would allow BamQC to be run as
part of an analysis pipeline.


## Running BamQC Interactively
**Windows:** Simply double click on the run_bamqc bat file.  If you want to make a pretty 
shortcut then we've included an icon file in the top level directory so you don't have
to use the generic bat file icon.

**MacOSX:** There is an application bundle for MacOSX which you can use to install and run
BamQC.  Just drag the application from the disk image to your Applications folder (or
wherever you want to install the program).

**Linux:**  We have included a wrapper script, called 'bamqc' which is the easiest way to 
start the program.  The wrapper is in the top level of the BamQC installation.  You 
may need to make this file executable:

```bash
chmod 755 bamqc

# Now you can run it directly:
./bamqc

# Alternatively, place a link in /usr/local/bin to to run the program from any location:
sudo ln -s /path/to/BamQC/bamqc /usr/local/bin/bamqc
```


## Running BamQC as part of a pipeline
To run BamQC non-interactively you should use the bamqc wrapper script to launch
the program.  You will probably want to use the zipped install file on every platform
(even OSX).

To run non-interactively you simply have to specify a list of files to process
on the command line

```
bamqc --gff some.gtf somefile.bam someotherfile.bam
```

You can specify as many files to process in a single run as you like.  If you don't
specify any files to process the program will try to open the interactive application
which may result in an error if you're running in a non-graphical environment.

There are a few extra options you can specify when running non-interactively.  Full
details of these can be found by running 

```
bamqc --help
```

By default, in non-interactive mode BamQC will create an HTML report with embedded
graphs, but also a zip file containing individual graph files and additional data files
containing the raw data from which plots were drawn.  The zip file will not be extracted
by default but you can enable this by adding `--extract` to the launch command.

If you want to save your reports in a folder other than the folder which contained
your original FastQ files then you can specify an alternative location by setting
`--outdir`. For example:

```
bamqc --outdir=/some/other/dir/ somefile.bam
```


## Customising the report output
If you want to run BamQC as part of a sequencing pipeline you may wish to change the
formatting of the report to add in your own branding or to include extra information.

In the Templates directory you will find a file called `header_template.html` which
you can edit to change the look of the report.  This file contains all of the header for
the report file, including the CSS section and you can alter this however you see fit.

Whilst you can make whatever changes you like you should probably leave in place the
`<div>` structure of the html template since later code will expect to close the main div
which is left open at the end of the header.  There is no facility to change the code in
the main body of the report or the footer (although you can of course change the styling).

The text tags `@@FILENAME@@` and `@@DATE@@` are placeholders which are filled in when the
report it created.  You can use these placeholders in other parts of the header if you
wish.
