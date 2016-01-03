package com.github.tomaslanger.cli.choice;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for single choice.
 *
 * User: Tomas.Langer
 * Date: 2.1.2016
 * Time: 23:09
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
public class SingleChoiceTest {
    SingleChoice choice;
    @Before
    public void setUp() {
        choice = SingleChoice.Builder.singleChoice();
//        System.setProperty("clichoice.quiet", "true");
    }

    @Test
    public void testDefaultSimpleVarargInteractive() throws Exception {
        System.setProperty("clichoice.forceInput", "true");
        System.setProperty("clichoice.useDefault", "false");

        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        InputStream original = System.in;
        System.setIn(inputStream);

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(500);
                //let's play
                outputStream.write("1\r\n".getBytes());
                outputStream.write("47\r\n".getBytes());
                outputStream.write("blah\r\n".getBytes());
                outputStream.write("third\r\n".getBytes());
                outputStream.write("\r\n".getBytes());
                outputStream.flush();
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("Failed to write user input");
                e.printStackTrace();
            }
        });
        t.start();

        try {
            assertEquals("Must select user input when in interactive mode", "third", choice.select("default", "first", "default", "third"));
        } finally {
            System.setProperty("clichoice.forceInput", "false");
            System.setIn(original);
        }
    }

    @Test
    public void testDefaultSimpleVarargInteractiveChoiceSelects() throws Exception {
        System.setProperty("clichoice.forceInput", "true");
        System.setProperty("clichoice.useDefault", "false");

        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        InputStream original = System.in;
        System.setIn(inputStream);

        final Thread t = new Thread(() -> {
            try {
                Thread.sleep(500);
                //let's play
                outputStream.write("third\r\n".getBytes());
                outputStream.flush();
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("Failed to write user input");
                e.printStackTrace();
            }
        });
        t.start();


        try {
            SingleChoice customChoice = new SingleChoice.Builder().setChoiceSelects().build();

            assertEquals("Must select user input when in interactive mode", "third", customChoice.select("default", "first", "default", "third"));
        } finally {
            System.setProperty("clichoice.forceInput", "false");
            System.setIn(original);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testMissingDefaultGeneric() {
        choice.select(null, "first", "second", "third");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDefaultGeneric() {
        choice.select("fourth", "first", "second", "third");
    }

    @Test
    public void testDefaultSimpleVararg() throws Exception {
        System.setProperty("clichoice.useDefault", "true");

        assertEquals("Must select default value if in batch mode", "default", choice.select("default", "first", "default", "third"));

        System.setProperty("clichoice.useDefault", "false");
    }

    @Test
    public void testDefaultList() throws Exception {
        System.setProperty("clichoice.useDefault", "true");

        List<String> options = Arrays.asList("first", "default", "third");
        assertEquals("Must select default value if in batch mode", "default", choice.select("default", options));

        System.setProperty("clichoice.useDefault", "false");
    }

    @Test
    public void testDefaultListSysprop() throws Exception {
        System.setProperty("clichoice.useDefault", "true");

        List<String> options = Arrays.asList("first", "default", "third");

        assertEquals("Must select default value if in batch mode", "default", choice.select("default", options, "clichoice.test.sysprop"));
        System.setProperty("clichoice.test.sysprop", "third");
        assertEquals("Must select default value from system property if in batch mode", "third", choice.select("default", options, "clichoice.test.sysprop"));
        System.setProperty("clichoice.test.sysprop", "");
        assertEquals("Must select default value if in batch mode", "default", choice.select("default", options, "clichoice.test.sysprop"));

        System.setProperty("clichoice.useDefault", "false");
    }

    @Test
    public void testDefaultOptionsVararg() throws Exception {
        System.setProperty("clichoice.useDefault", "true");

        Option<String> selected = choice.select(new Option<>("first"),
                new Option<>("default", "default", true),
                new Option<>("third"));

        assertEquals("Must select default value if in batch mode", "default", selected.getWrapped());

        System.setProperty("clichoice.useDefault", "false");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingDefaultOptions() {
        choice.select("clichoice.test.sysprop",
                new Option<>("first"),
                new Option<>("default"),
                new Option<>("other"),
                new Option<>("third"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiDefaultOptions() {
        choice.select("clichoice.test.sysprop",
                new Option<>("first"),
                new Option<>("default", "default", true),
                new Option<>("other", "other", true),
                new Option<>("third"));
    }


    @Test
    public void testQuietMode() {
        System.setProperty("clichoice.quiet", "true");
        System.setProperty("clichoice.useDefault", "true");

        PrintStream systemOut = System.out;
        try {
            PipedInputStream in = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(in);

            System.setOut(new PrintStream(out));

            Option<String> select = choice.select("clichoice.test.sysprop",
                    new Option<>("first"),
                    new Option<>("default", "default", true),
                    new Option<>("other"),
                    new Option<>("third"));

            assertEquals("Must return the default", "default", select.getWrapped());

            System.out.println("!");
            System.out.flush();

            byte[] buffer = new byte[1024];
            int read = in.read(buffer);
            System.setOut(systemOut);
            //there should be max. three bytes from my output above
            if (read > 3) {
                fail("When in quiet mode, we should not get any output at all, but got: " + new String(buffer, 0, read));
            }

        } catch (IOException e) {
            System.setOut(systemOut);

            System.err.println("Failed test: cannot get piped streams working");
            e.printStackTrace();
        }

        System.setProperty("clichoice.quiet", "false");
        System.setProperty("clichoice.useDefault", "false");
    }

    @Test
    public void testDefaultOptionsVarargSysprop() throws Exception {
        System.setProperty("clichoice.useDefault", "true");

        Option<String> selected = choice.select("clichoice.test.sysprop", new Option<>("first"),
                new Option<>("default", "default", true),
                new Option<>("third"));

        assertEquals("Must select default value if in batch mode", "default", selected.getWrapped());
        System.setProperty("clichoice.test.sysprop", "third");
        selected = choice.select("clichoice.test.sysprop", new Option<>("first"),
                new Option<>("default", "default", true),
                new Option<>("third"));
        assertEquals("Must select default value from system property if in batch mode", "third", selected.getWrapped());
        System.setProperty("clichoice.test.sysprop", "");
        selected = choice.select("clichoice.test.sysprop", new Option<>("first"),
                new Option<>("default", "default", true),
                new Option<>("third"));
        assertEquals("Must select default value if in batch mode", "default", selected.getWrapped());

        System.setProperty("clichoice.useDefault", "false");
    }

    @Test
    public void testDefaultOptionsList() throws Exception {
        System.setProperty("clichoice.useDefault", "true");

        List<Option<String>> options = Arrays.asList(
                new Option<>("alpha"),
                new Option<>("beta", "default", true),
                new Option<>("delta"));

        Option<String> selected = choice.select(options);

        assertEquals("Must select default value if in batch mode", "beta", selected.getWrapped());

        System.setProperty("clichoice.useDefault", "false");
    }

    @Test
    public void testDefaultOptionsListSysprop() throws Exception {
        System.setProperty("clichoice.useDefault", "true");

        List<Option<String>> options = Arrays.asList(new Option<>("alpha"),
                new Option<>("beta", "beta", true),
                new Option<>("delta"));

        Option<String> selected = choice.select("clichoice.test.sysprop", options);

        assertEquals("Must select default value if in batch mode", "beta", selected.getWrapped());
        System.setProperty("clichoice.test.sysprop", "alpha");
        selected = choice.select("clichoice.test.sysprop", options);
        assertEquals("Must select default value from system property if in batch mode", "alpha", selected.getWrapped());
        System.setProperty("clichoice.test.sysprop", "");

        //must rebuild, as alpha will be set as default (options are mutable!!!)
        options = Arrays.asList(new Option<>("alpha"),
                new Option<>("beta", "beta", true),
                new Option<>("delta"));
        selected = choice.select("clichoice.test.sysprop", options);
        assertEquals("Must select default value if in batch mode", "beta", selected.getWrapped());

        System.setProperty("clichoice.useDefault", "false");
    }

    @Test
    public void testIntegerChoice() {
        System.setProperty("clichoice.useDefault", "true");

        assertEquals("Must return appropriate type", 14, (int)choice.select(14, 12,25,22,36,47,14));

        System.setProperty("clichoice.useDefault", "true");
    }

    @Test
    public void testByteArrays() {
        byte[] first = new byte[14];
        byte[] second = new byte[47];
        byte[] third = new byte[1024];

        Arrays.fill(first, (byte)147);
        Arrays.fill(second, (byte)55);
        Arrays.fill(third, (byte)14);

        System.setProperty("clichoice.useDefault", "true");

        assertEquals("Must return appropriate type", first, choice.select(first, first, second, third));

        System.setProperty("clichoice.useDefault", "true");
    }

    @Test
    public void testByteArraysWrapped() {
        byte[] first = new byte[14];
        byte[] second = new byte[47];
        byte[] third = new byte[1024];

        Arrays.fill(first, (byte)147);
        Arrays.fill(second, (byte)55);
        Arrays.fill(third, (byte)14);

        System.setProperty("clichoice.useDefault", "true");

        assertEquals("Must return appropriate type", first, choice.select(
                new Option<>(first, "First byte array", true).setSysPropValue("firstbytes"),
                new Option<>(second, "Second byte array"),
                new Option<>(third)
                ).getWrapped());

        System.setProperty("clichoice.useDefault", "true");
    }

    @Test
    public void testByteArraysWrappedSysprop() {
        byte[] first = new byte[14];
        byte[] second = new byte[47];
        byte[] third = new byte[1024];

        Arrays.fill(first, (byte)147);
        Arrays.fill(second, (byte)55);
        Arrays.fill(third, (byte)14);

        System.setProperty("clichoice.useDefault", "true");

        System.setProperty("clichoice.test.sysprop", "firstbytes");

        assertEquals("Must return appropriate type", first, choice.select("clichoice.test.sysprop",
                new Option<>(first, "First byte array").setSysPropValue("firstbytes"),
                new Option<>(second, "Second byte array", true),
                new Option<>(third)
        ).getWrapped());

        System.setProperty("clichoice.test.sysprop", "");

        System.setProperty("clichoice.useDefault", "true");
    }
}