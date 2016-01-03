package com.github.tomaslanger.cli.choice;

import com.github.tomaslanger.chalk.Ansi;
import com.github.tomaslanger.chalk.Chalk;

import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common ancestor with helpful methods.
 * <p>
 * User: Tomas.Langer
 * Date: 30.12.2015
 * Time: 20:43
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
abstract class ChoiceBase {
    protected final String lineFormat;
    protected final String toggleOnString;
    protected final String toggleOffString;
    protected final String inputLineString;
    protected final boolean shouldAcceptValuesAsInput;
    protected final String notValidOrdinalMessage;
    protected final String notNumberMessage;
    protected final String messageLineString;

    protected ChoiceBase(final Builder b) {
        this.lineFormat = b.getLineFormat();
        this.toggleOnString = b.getToggleOnString();
        this.toggleOffString = b.getToggleOffString();
        this.inputLineString = b.getInputLineString();
        this.shouldAcceptValuesAsInput = b.shouldAcceptValuesAsInput();
        this.messageLineString = b.getMessageLineString();
        this.notNumberMessage = b.getNotNumberMessage();
        this.notValidOrdinalMessage = b.getNotValidOrdinalMessage();
    }

    protected void clearChoice(int choiceCount) {
        for (int i = 0; i < (choiceCount + 2); i++) {
            System.out.print(Ansi.cursorUp());
            System.out.print(Ansi.eraseLine());
        }
        System.out.print('\r');
        System.out.flush();
    }

    protected <T> void setIntOrdinals(final List<Option<T>> options) {
        int ordinal = 1;
        for (final Option<T> option : options) {
            option.setOrdinal(String.valueOf(ordinal));
            ordinal++;
        }
    }

    protected Reader getReader() {
        Reader r = null;
        boolean forceInput = Boolean.getBoolean("clichoice.forceInput");
        Console c = System.console();
        if (null == c) {
            if (forceInput) {
                r = new InputStreamReader(System.in);
            }
        } else {
            r = c.reader();
        }

        return r;
    }

    protected <T> Map<String,Integer> getTextToOrdinalMap(final List<Option<T>> options) {
        int ordinal = 1;
        Map<String, Integer> map = new HashMap<>();
        for (final Option<T> option : options) {
            map.put(option.getOptionText(), ordinal);
            ordinal++;
        }

        return map;
    }

    protected <T> void printChoice(final List<Option<T>> options,
                               final String message) {

        for (final Option option : options) {
            String value = option.getOptionText();
            boolean isSelected = option.isSelected();

            System.out.format(lineFormat, option.getOrdinal(), (isSelected ? toggleOnString : toggleOffString), value);
            System.out.println();
        }

        System.out.println(message);
        System.out.print(inputLineString);
        System.out.flush();
    }

    protected String read(final Reader reader) {
        StringBuilder sb = new StringBuilder();

        while (true) {
            int intRead;
            try {
                intRead = reader.read();
            } catch (IOException e) {
                System.out.println();
                System.out.println(Chalk.on("Failed to read from input").red() + ", returning default value");
                e.printStackTrace();
                return null;
            }
            if (intRead == -1) {
                return null;
            }

            char read = (char) intRead;

            switch (read) {
                case '\r':
                    //ignore
                    continue;
                case '\n':
                    return sb.toString();
                default:
                    sb.append(read);
                    break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected static class Builder<T extends Builder> {
        private String lineFormat = "%1$2s: (%2$1s) %3$2s";
        private String toggleOnString = "*";
        private String toggleOffString = " ";
        private String inputLineString = "Your choice:> ";
        private boolean acceptValuesAsInput = true;

        private String messageLineString = "Enter number and press enter to toggle choice. Press enter to accept the current choice.";
        private String notNumberMessage = Chalk.on("Not a number!").bgRed().white() + " Enter number and press enter to toggle choice. Press enter to accept the current choice.";
        private String notValidOrdinalMessage = Chalk.on("Please enter a valid choice").magenta() + " and press enter to toggle choice. Press enter to accept the current choice.";

        protected Builder() {

        }

        /**
         * Set the format of a line with selection.
         * Default format is "%1$2s: (%2$1s) %3$2s". First parameter is ordinal of the choice (index starting with 1),
         * second parameter is toggleOn or toggleOff string, third parameter is the string value of the choice.
         * <ul>
         *     <li> 1: ( ) Unselected choice</li>
         *     <li> 2: (*) Selected choice</li>
         *     <li>Your choice:&gt;
         * </ul>
         * The toggle strings are customizable (the space for unselected and asterisk for selected). The input line
         * is also customizable.
         *
         *
         * @param lineFormat format to use
         * @return Builder instance
         */
        public T setLineFormat(final String lineFormat) {
            this.lineFormat = lineFormat;
            return (T) this;
        }

        /**
         * Set the string to fill when selection is on. Set to asterisk for single choice and plus sign for multiple
         * choice.
         *
         * @param toggleOnString String to send as second parameter to text format if selection enabled
         * @return Builder instance
         */
        public T setToggleOnString(final String toggleOnString) {
            this.toggleOnString = toggleOnString;

            return (T) this;
        }

        /**
         * Set the string to fill when selection is off. Set to space as default.
         *
         * @param toggleOffString String to send as second parameter to text format if selection disabled
         * @return Builder instance
         */
        public T setToggleOffString(final String toggleOffString) {
            this.toggleOffString = toggleOffString;

            return (T) this;
        }

        /**
         * Set the string to print as beginning of line that is expecting input from user.
         *
         * @param inputLineString String to print on screen
         * @return Builder instance
         */
        public T setInputLineString(final String inputLineString) {
            this.inputLineString = inputLineString;

            return (T) this;
        }

        /**
         * Set to true if user can input values of choice instead of its ordinal number. This is default behavior.
         *
         * @param acceptValuesAsInput Set to true if values should be accepted. Set to false if only ordinal numbers should.
         * @return Builder instance
         */
        public T setAcceptValuesAsInput(final boolean acceptValuesAsInput) {
            this.acceptValuesAsInput = acceptValuesAsInput;

            return (T) this;
        }

        protected String getLineFormat() {
            return lineFormat;
        }

        protected String getToggleOnString() {
            return toggleOnString;
        }

        protected String getToggleOffString() {
            return toggleOffString;
        }

        protected String getInputLineString() {
            return inputLineString;
        }

        protected boolean shouldAcceptValuesAsInput() {
            return acceptValuesAsInput;
        }

        public T setMessageLineString(final String messageLineString) {
            this.messageLineString = messageLineString;

            return (T) this;
        }

        public T setNotNumberMessage(final String notNumberMessage) {
            this.notNumberMessage = notNumberMessage;

            return (T) this;
        }

        public T setNotValidOrdinalMessage(final String notValidOrdinalMessage) {
            this.notValidOrdinalMessage = notValidOrdinalMessage;

            return (T) this;
        }

        protected String getMessageLineString() {
            return messageLineString;
        }

        protected String getNotNumberMessage() {
            return notNumberMessage;
        }

        protected String getNotValidOrdinalMessage() {
            return notValidOrdinalMessage;
        }
    }
}
