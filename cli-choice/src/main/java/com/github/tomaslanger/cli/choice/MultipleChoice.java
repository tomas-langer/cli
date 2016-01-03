package com.github.tomaslanger.cli.choice;

import com.github.tomaslanger.chalk.Chalk;

import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Choice - checkboxes or radiobuttons.
 * <p>
 * User: Tomas.Langer
 * Date: 24.12.2015
 * Time: 0:14
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
public class MultipleChoice extends ChoiceBase {
    static {
        Chalk.on("init");
    }

    public MultipleChoice(final Builder builder) {
        super(builder);
    }

    /**
     * Show options and return the option selected by user.
     *
     * @param options      options to show to user. The {@link Object#toString()} is used to print the text to user.
     * @param defaultValue default value to preselect. Default value MUST be provided, as otherwise it would not
     *                     work on environments that do not have input support. You can override the default value
     *                     if you use {@link #select(String, Option[])} or {@link #select(String, List)}
     * @param <T>          the type of the option
     * @return selected values (or default if this environment does not support user input).
     */
    public <T> List<T> select(T[] defaultValue, List<T> options) {
        if (null == defaultValue) {
            throw new NullPointerException("Default value MUST be always filled.");
        }

        Set<T> allDefaults = new HashSet<>();
        List<T> defaults = new ArrayList<>(defaultValue.length);
        defaults.addAll(Arrays.asList(defaultValue));
        allDefaults.addAll(defaults);

        options.forEach(defaults::remove);

        if (defaults.size() > 0) {
            throw new IllegalArgumentException("Defaults must be included in options, but these were not: " + defaults);
        }

        List<Option<T>> optionList = new ArrayList<>(options.size());

        for (final T option : options) {
            Option<T> newOption = new Option<>(option);
            if (allDefaults.contains(option)) {
                newOption.setSelected(true);
            }
            optionList.add(newOption);
        }

        return optionsAsList(_select(optionList));
    }


    /**
     * Show options and return the option selected by user.
     *
     * @param defaultValue default values (preselected)
     * @param options      options (that must contain default values)
     * @param <T>          the type of the option
     * @return List of selected options by user
     */
    @SafeVarargs
    public final <T> List<T> select(T[] defaultValue, T... options) {
        return select(defaultValue, Arrays.asList(options));
    }

    /**
     * Show options and return the option selected by user.
     *
     * @param options Options wrapping the original types, defining text to show, whether pre-selected or not etc.
     * @param <T>     the type of the wrapped object
     * @return List of selected options by user
     */
    @SafeVarargs
    public final <T> List<Option<T>> select(final Option<T>... options) {
        return select(Arrays.asList(options));
    }

    /**
     * Show options and return the option selected by user.
     *
     * @param systemProperty name of system property that can override the pre-selected values
     * @param options        Options wrapping the original types, defining text to show, whether pre-selected or not etc.
     * @param <T>            the type of the wrapped object
     * @return List of selected options by user
     */
    @SafeVarargs
    public final <T> List<Option<T>> select(final String systemProperty, final Option<T>... options) {
        return select(systemProperty, Arrays.asList(options));
    }

    /**
     * Show options and return the option selected by user.
     *
     * @param options Options wrapping the original types, defining text to show, whether pre-selected or not etc.
     * @param <T>     the type of the wrapped object
     * @return List of selected options by user
     */
    public <T> List<Option<T>> select(final List<Option<T>> options) {
        return select((String) null, options);
    }

    /**
     * Show options to user and return option(s) selected by user. Default values are the options that have selected
     * set to true.
     *
     * @param systemProperty system property that defines default values (can be used for silent installations etc.). The value of the property is expected to be comma separated list of {@link Option#getSysPropValue()}
     * @param options        List of options to present to users. Note that options are mutable and selection status will be written to them
     * @param <T>            Type of the wrapped object
     * @return List of selected options.
     */
    public <T> List<Option<T>> select(final String systemProperty, final List<Option<T>> options) {
        if (null != systemProperty) {
            String sysProp = System.getProperty(systemProperty);
            if (sysProp != null && !sysProp.isEmpty()) {
                //modify defaults according to the value
                String[] values = sysProp.split(",");
                Set<String> valueSet = new HashSet<>(Arrays.asList(values));

                for (final Option<T> option : options) {
                    if (!option.isFixed()) {
                        option.setSelected(valueSet.contains(option.getSysPropValue()));
                    }
                }
            }
        }

        return _select(options);
    }

    private <T> List<Option<T>> _select(final List<Option<T>> options) {
        //values to ordinals -> so we can accept value as user's choice
        Map<String, Integer> textToOrdinal = getTextToOrdinalMap(options);
        //fill in ordinals for options
        setIntOrdinals(options);

        if (Boolean.getBoolean("clichoice.quiet")) {
            return getSelected(options);
        }

        //print the choice
        super.printChoice(options, super.messageLineString);

        int selectionSize = options.size();

        Reader r = super.getReader();

        if (null == r) {
            //there is no console detected
            System.out.println(); //choice line end
            System.out.println("There is no console to interact with, returning default value: " + getSelected(options));
            return getSelected(options);
        }

        while (true) {
            String input = super.read(r);

            if (null == input) {
                return getSelected(options);
            }

            if (input.length() > 0) {
                int selectedOrdinal;

                clearChoice(selectionSize);

                try {
                    selectedOrdinal = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    Integer ordinal = textToOrdinal.get(input);
                    if (null == ordinal) {
                        printChoice(options, notNumberMessage);
                        continue;
                    } else {
                        selectedOrdinal = ordinal;
                    }
                }

                if (selectedOrdinal > options.size() || selectedOrdinal < 1) {
                    printChoice(options, notValidOrdinalMessage);
                } else {
                    toggleSelection(options, selectedOrdinal);
                    printChoice(options, messageLineString);
                }
            } else {
                return getSelected(options);
            }
        }
    }

    private <T> void toggleSelection(final List<Option<T>> options, final int selectedOrdinal) {
        Option<T> option = options.get(selectedOrdinal - 1);
        if (option.isFixed()) {
            return;
        }
        option.setSelected(!option.isSelected());
    }

    private <T> List<Option<T>> getSelected(final List<Option<T>> options) {
        return options.stream().filter(Option::isSelected).collect(Collectors.toList());
    }

    private <T> List<T> optionsAsList(final List<Option<T>> options) {
        return options.stream().map(Option::getWrapped).collect(Collectors.toList());
    }


    public static class Builder extends ChoiceBase.Builder<MultipleChoice.Builder> {
        public Builder() {
            super();

            setToggleOnString("+");
        }

        public static MultipleChoice multipleChoice() {
            Builder builder = new Builder();
            return builder.build();
        }

        public MultipleChoice build() {
            return new MultipleChoice(this);
        }
    }
}
