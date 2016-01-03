package com.github.tomaslanger.cli.choice;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit test for multiple choice.
 *
 * User: Tomas.Langer
 * Date: 3.1.2016
 * Time: 21:52
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
public class MultipleChoiceTest {
    MultipleChoice choice;
    @Before
    public void setUp() {
        choice = MultipleChoice.Builder.multipleChoice();
//        System.setProperty("clichoice.quiet", "true");
    }

    @Test
    public void testDefaultSimpleVarargs() throws Exception {
        System.setProperty("clichoice.useDefault", "true");

        String[] defaults = new String[]{"a", "d"};
        List<String> select = choice.select(defaults, "a", "b", "c", "d");

        assertArrayEquals("Must select default value if in batch mode", defaults, select.toArray(new String[select.size()]));

        System.setProperty("clichoice.useDefault", "false");
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
                //select b
                outputStream.write("b\r\n".getBytes());
                //select c
                outputStream.write("c\r\n".getBytes());
                //unselect d
                outputStream.write("d\r\n".getBytes());
                //do some nasty stuff
                outputStream.write("x\r\n".getBytes());
                outputStream.write("45\r\n".getBytes());
                //end it
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
            String[] defaults = new String[]{"a", "d"};
            String[] expected = new String[]{"a", "b", "c"};
            List<String> select = choice.select(defaults, "a", "b", "c", "d");

            assertArrayEquals("Must select the correct values in interactive mode", expected, select.toArray(new String[select.size()]));
        } finally {
            System.setProperty("clichoice.forceInput", "false");
            System.setIn(original);
        }
    }


    @Test
    public void testDefaultSimpleVarargsQuiet() throws Exception {
        System.setProperty("clichoice.quiet", "true");

        String[] defaults = new String[]{"a", "d"};
        List<String> select = choice.select(defaults, "a", "b", "c", "d");

        assertArrayEquals("Must select default value if in batch mode", defaults, select.toArray(new String[select.size()]));

        System.setProperty("clichoice.quiet", "false");
    }

    @Test
    public void testDefaultSimpleList() throws Exception {
        System.setProperty("clichoice.useDefault", "true");

        String[] defaults = new String[]{"a", "d"};
        List<String> options = Arrays.asList("a", "b", "c", "d");
        List<String> select = choice.select(defaults, options);

        assertArrayEquals("Must select default value if in batch mode", defaults, select.toArray(new String[select.size()]));

        System.setProperty("clichoice.useDefault", "false");
    }

    @Test(expected = NullPointerException.class)
    public void testSimpleListNoDefaults() throws Exception {
        System.setProperty("clichoice.useDefault", "true");

        List<String> options = Arrays.asList("a", "b", "c", "d");
        List<String> select = choice.select(null, options);

        System.setProperty("clichoice.useDefault", "false");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSimpleListWrongDefaults() throws Exception {
        System.setProperty("clichoice.useDefault", "true");

        String[] defaults = new String[]{"a", "x"};
        List<String> options = Arrays.asList("a", "b", "c", "d");
        List<String> select = choice.select(defaults, options);

        assertArrayEquals("Must select default value if in batch mode", defaults, select.toArray(new String[select.size()]));

        System.setProperty("clichoice.useDefault", "false");
    }


    @Test
    public void testDefaultOptionVarargs() throws Exception {
        System.setProperty("clichoice.useDefault", "true");

        List<Option<String>> select = choice.select(
                new Option<>("a", "a", true),
                new Option<>("b"),
                new Option<>("c"),
                new Option<>("d", "d", true)
        );

        List<Option<String>> defaults = new ArrayList<>();
        defaults.add(new Option<>("a", "a", true));
        defaults.add(new Option<>("d", "d", true));

        assertEquals("Must select default value if in batch mode", defaults, select);

        System.setProperty("clichoice.useDefault", "false");
    }

    @Test
    public void testDefaultOptionList() throws Exception {
        System.setProperty("clichoice.useDefault", "true");

        List<Option<String>> options = Arrays.asList(new Option<>("a", "a", true),
                new Option<>("b"),
                new Option<>("c"),
                new Option<>("d", "d", true));

        List<Option<String>> select = choice.select(options);

        List<Option<String>> defaults = new ArrayList<>();
        defaults.add(new Option<>("a", "a", true));
        defaults.add(new Option<>("d", "d", true));

        assertEquals("Must select default value if in batch mode", defaults, select);

        System.setProperty("clichoice.useDefault", "false");
    }

    @Test
    public void testDefaultOptionVarargsSysprop() throws Exception {
        System.setProperty("clichoice.useDefault", "true");
        System.setProperty("clichoice.test.sysprop", "b,d");

        List<Option<String>> select = choice.select(
                "clichoice.test.sysprop",
                new Option<>("a", "a", true),
                new Option<>("b"),
                new Option<>("c"),
                new Option<>("d", "d", true)
        );

        List<Option<String>> defaults = new ArrayList<>();
        defaults.add(new Option<>("b", "b", true));
        defaults.add(new Option<>("d", "d", true));

        assertEquals("Must select default value if in batch mode", defaults, select);

        System.setProperty("clichoice.test.sysprop", "");
        System.setProperty("clichoice.useDefault", "false");
    }


    @Test
    public void testDefaultOptionListSysprop() throws Exception {
        System.setProperty("clichoice.useDefault", "true");
        System.setProperty("clichoice.test.sysprop", "b,d");

        List<Option<String>> options = Arrays.asList(new Option<>("a"),
                new Option<>("b"),
                new Option<>("c"),
                new Option<>("d"));

        List<Option<String>> select = choice.select("clichoice.test.sysprop", options);

        List<Option<String>> defaults = new ArrayList<>();
        defaults.add(new Option<>("b", "b", true));
        defaults.add(new Option<>("d", "d", true));

        assertEquals("Must select default value if in batch mode", defaults, select);

        System.setProperty("clichoice.test.sysprop", "");
        System.setProperty("clichoice.useDefault", "false");
    }
}