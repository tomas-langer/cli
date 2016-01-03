package com.github.tomaslanger.cli.choice;

import com.github.tomaslanger.chalk.Chalk;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Choice - checkboxes or radiobuttons.
 * <p>
 * User: Tomas.Langer
 * Date: 24.12.2015
 * Time: 0:14
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
public class SingleChoice extends ChoiceBase {
    static {
        Chalk.on("init");
    }

    private final boolean choiceSelects;

    private SingleChoice(Builder builder) {
        super(builder);

        this.choiceSelects = builder.isChoiceSelects();
    }

    /**
     * Show options and return the option selected by user.
     *
     * @param options      options to show to user. The {@link Object#toString()} is used to print the text to user.
     * @param defaultValue default value to preselect. Default value MUST be provided, as otherwise it would not
     *                     work on environments that do not have input support. You can override the default value
     *                     if you use {@link #select(Object, List, String)}
     * @return selected value (or default if this environment does not support user input).
     */
    public <T> T select(T defaultValue, List<T> options) {
        return select(defaultValue, options, null);
    }

    /**
     * Show options and return the option selected by user.
     *
     * @see #select(Object, List)
     */
    @SafeVarargs
    public final <T> T select(T defaultValue, T... options) {
        return select(defaultValue, Arrays.asList(options));
    }

    /**
     * Show options and return the option selected by user.
     *
     * @param options        options to show to user. The {@link Object#toString()} is used to print the text to user.
     * @param defaultValue   default value to preselect. Default value MUST be provided, as otherwise it would not
     *                       work on environments that do not have input support.
     * @param systemProperty name of system property to use to pre-select default value, e.g. when doing silent installation. The value must be equal to the
     *                       string representation of the option.
     * @return selected value (or default if this environment does not support user input).
     */
    public <T> T select(T defaultValue, List<T> options, String systemProperty) {
        String sysProp = null;
        if (null != systemProperty) {
            sysProp = System.getProperty(systemProperty);
            if (null == sysProp || sysProp.isEmpty()) {
                sysProp = null;
            }
        }

        if (null == defaultValue && null == sysProp) {
            throw new NullPointerException("Default value MUST always be filled.");
        }

        List<Option<T>> optionList = new ArrayList<>(options.size());
        boolean hasDefault = false;

        for (final T option : options) {
            Option<T> nextOption = new Option<>(option);

            if (null == sysProp) {
                if (option.equals(defaultValue)) {
                    hasDefault = true;
                    nextOption.setSelected(true);
                }
            } else {
                if (sysProp.equals(option.toString())) {
                    hasDefault = true;
                    nextOption.setSelected(true);
                }
            }
            optionList.add(nextOption);
        }

        if (!hasDefault) {
            throw new IllegalArgumentException("Default value " + (null == sysProp ? defaultValue : sysProp) + " must be included in options");
        }

        return _select(optionList).getWrapped();
    }

    /**
     * Select with full control on options - can have separate value and text description, can define defaults.
     *
     * @param options options to choose from
     * @return selected option
     */
    @SafeVarargs
    public final <T> Option<T> select(Option<T>... options) {
        return select(null, options);
    }

    /**
     * Select with full control on options - can have separate value and text description, can define defaults.
     *
     * @param options        options to choose from
     * @param systemProperty name of system property that can define default value (e.g. for silent installs)
     * @return selected option
     */
    @SafeVarargs
    public final <T> Option<T> select(String systemProperty, Option<T>... options) {
        List<Option<T>> optionList = Arrays.asList(options);
        return select(systemProperty, optionList);
    }

    /**
     * Select with full control on options - can have separate value and text description, can define defaults.
     *
     * @param options        options to choose from, mutated as selected by user
     * @param systemProperty name of system property that can define default value (e.g. for silent installs)
     * @return selected option
     */
    public <T> Option<T> select(final String systemProperty, final List<Option<T>> options) {
        String sysProp = null;
        if (null != systemProperty) {
            sysProp = System.getProperty(systemProperty);
            if (null == sysProp || sysProp.isEmpty()) {
                sysProp = null;
            }
        }

        Option<T> defaultValue = null;

        for (final Option<T> option : options) {
            if (null == sysProp) {
                if (option.isSelected()) {
                    if (null != defaultValue) {
                        throw new IllegalArgumentException("Single choice requires exactly one default value. At least: " + defaultValue.getOptionText() + " and " + option.getOptionText() + " are both marked as selected and thus default.");
                    }
                    defaultValue = option;
                }
            } else {
                option.setSelected(sysProp.equals(option.getSysPropValue()));
                if (option.isSelected()) {
                    defaultValue = option;
                }
            }
        }

        if (null == defaultValue) {
            throw new IllegalArgumentException("Single choice requires exactly one default value. Yet no option is marked as selected.");
        }

        return _select(options);
    }

    /**
     * Select with full control on options - can have separate value and text description, can define defaults.
     *
     * @param options        options to choose from, mutated as selected by user
     * @return selected option
     */
    public <T> Option<T> select(final List<Option<T>> options) {
        return select((String)null, options);
    }

    private <T> Option<T> _select(final List<Option<T>> options) {
        //values to ordinals -> so we can accept value as user's choice
        Map<String, Integer> textToOrdinal = getTextToOrdinalMap(options);
        //fill in ordinals for options
        setIntOrdinals(options);

        if (Boolean.getBoolean("clichoice.quiet")) {
            return getSelected(options);
        }

        //print the choice
        super.printChoice(options, super.messageLineString);

        if (Boolean.getBoolean("clichoice.useDefault")) {
            System.out.println("User choice disabled by system property, returning default value: " + getSelected(options).getOptionText());
            return getSelected(options);
        }

        Reader r = getReader();

        if (null == r) {
            //there is no console detected
            System.out.println(); //choice line end
            System.out.println("There is no console to interact with, returning default value: " + getSelected(options).getOptionText());
            return getSelected(options);
        }


        int selectionSize = options.size();

        while (true) {
            String input = super.read(r);

            if (null == input) {
                return getSelected(options);
            }

            if (input.length() > 0) {
                int selectedOrdinal;

                super.clearChoice(selectionSize);

                try {
                    selectedOrdinal = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    //first check if they just wrote the value
                    Integer ordinal = textToOrdinal.get(input);
                    if (null != ordinal) {
                        selectedOrdinal = ordinal;
                    } else {
                        super.printChoice(options, notNumberMessage);
                        continue;
                    }
                }
                if (selectedOrdinal > options.size() || selectedOrdinal < 1) {
                    super.printChoice(options, notValidOrdinalMessage);
                    continue;
                }

                resetSelected(selectedOrdinal, options);

                printChoice(options, messageLineString);

                if (choiceSelects) {
                    //go to next line (after input message)
                    System.out.println(selectedOrdinal);
                    return getSelected(options);
                }
            } else {
                return getSelected(options);
            }
        }
    }

    private <T> void resetSelected(final int selectedOrdinal, final List<Option<T>> options) {
        for (final Option<T> option : options) {
            option.setSelected(false);
        }
        options.get(selectedOrdinal - 1).setSelected(true);
    }

    private <T> Option<T> getSelected(final List<Option<T>> options) {
        for (final Option<T> option : options) {
            if (option.isSelected()) {
                return option;
            }
        }

        //this should not happen, as there is always one selection valid
        throw new IllegalStateException("One and only one selection can be done. If this happened, there is a bug.");
    }


    public static class Builder extends ChoiceBase.Builder<SingleChoice.Builder> {
        private boolean choiceSelects = false;
        private boolean messageExplicit;

        public static SingleChoice singleChoice() {
            Builder builder = new Builder();
            return builder.build();
        }

        public SingleChoice build() {
            return new SingleChoice(this);
        }

        public boolean isChoiceSelects() {
            return choiceSelects;
        }

        /**
         * By default changing a choice just prints the choice tree again and waits for input.
         * If this method is called, when a user selects an option, it is immediately chosen and the method
         * select returns.
         * If {@link #setMessageLineString(String)} was not called, also sets a reasonable message: "Enter number and press enter to confirm choice."
         *
         * @return Builder instance
         */
        public Builder setChoiceSelects() {
            this.choiceSelects = true;

            if (!messageExplicit) {
                super.setMessageLineString("Enter number and press enter to confirm choice.");
            }

            return this;
        }

        @Override
        public Builder setMessageLineString(final String messageLineString) {
            super.setMessageLineString(messageLineString);
            messageExplicit = true;
            return this;
        }
    }
}
