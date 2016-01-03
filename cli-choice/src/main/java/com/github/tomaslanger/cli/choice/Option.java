package com.github.tomaslanger.cli.choice;

import java.util.ArrayList;
import java.util.List;

/**
 * An option to display. Can be used for the most complex scenarios.
 * <p>
 * User: Tomas.Langer
 * Date: 2.1.2016
 * Time: 18:59
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
public class Option<T> {
    private final T wrapped;
    private String optionText;
    private boolean isSelected;
    private boolean isFixed;
    private String ordinal;
    private String sysPropValue;
    private List<Option<T>> children;


    /**
     * Create a new option, option text is toWrap.toString(), selected is false and fixed is false.
     *
     * @param toWrap Object to wrap
     */
    public Option(T toWrap) {
        this(toWrap, String.valueOf(toWrap));
    }

    /**
     * Create a new option, selected is false and fixed is false.
     *
     * @param toWrap     Object to wrap
     * @param optionText String to display to user
     */
    public Option(T toWrap, String optionText) {
        this(toWrap, optionText, false);
    }

    /**
     * Create a new option, fixed is false.
     *
     * @param toWrap     Object to wrap
     * @param optionText String to display to user
     * @param isSelected whether this option is selected (checked) or not by default
     */
    public Option(T toWrap, String optionText, boolean isSelected) {
        this(toWrap, optionText, isSelected, false);
    }

    /**
     * Create a new option.
     *
     * @param toWrap     Object to wrap
     * @param optionText String to display to user
     * @param isSelected whether this option is selected (checked) or not by default
     * @param isFixed    whether this option is fixed (e.g. cannot be modified by user)
     */
    public Option(final T toWrap, final String optionText, final boolean isSelected, final boolean isFixed) {
        this.wrapped = toWrap;
        this.optionText = optionText;
        this.isSelected = isSelected;
        this.isFixed = isFixed;
        this.sysPropValue = optionText;
    }

    /**
     * Get the object that is wrapped by this option.
     *
     * @return T original object from constructor
     */
    public T getWrapped() {
        return wrapped;
    }

    /**
     * Get the string to display to user for this option. Should be unique in one choice to avoid ambiguity (not enforced).
     * @return String option text
     */
    public String getOptionText() {
        return optionText;
    }

    /**
     * Set the text to display to user for this option.
     *
     * @param optionText Text to display
     */
    public void setOptionText(final String optionText) {
        this.optionText = optionText;
    }

    /**
     * Get the state of this option.
     *
     * @return whether user selected this option or not
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Set the state of this option. Use for default value(s). Will be displayed to user as already checked.
     * If multiple options are selected for {@link SingleChoice}, IllegalArgumentException will be thrown..
     *
     * @param selected whether this option is selected or not
     */
    public void setSelected(final boolean selected) {
        isSelected = selected;
    }

    /**
     * Whether selection is fixed or not. Note that if this is used for a {@link SingleChoice} in connection with
     * selected set to true, the choice is useless (only one possible value) and will immediately return the value.
     *
     * @return whether this option is a fixed selection value (e.g. the user cannot change the state of it) or
     * an option that can be toggled by user.
     */
    public boolean isFixed() {
        return isFixed;
    }

    /**
     * Set whether this selection is fixed (cannot be modified by user) or not (can be modified by user).
     *
     * @param fixed boolean indicating whether option is fixed or not
     */
    public void setFixed(final boolean fixed) {
        isFixed = fixed;
    }

    String getOrdinal() {
        return ordinal;
    }

    void setOrdinal(final String ordinal) {
        this.ordinal = ordinal;
    }

    /**
     * Value a system property is compared against if provided. Defaults to optionText.
     * @return system property expected value for this option
     */
    public String getSysPropValue() {
        return sysPropValue;
    }

    /**
     * Set an expected value of a system property to toggle this option (if default is provided through command line).
     * Defaults to optionText.
     *
     * @param sysPropValue Value to check system property against.
     */
    public Option setSysPropValue(final String sysPropValue) {
        this.sysPropValue = sysPropValue;

        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Option)) return false;

        Option<?> option = (Option<?>) o;

        if (!getWrapped().equals(option.getWrapped())) return false;
        return getOptionText().equals(option.getOptionText());

    }

    @Override
    public int hashCode() {
        int result = getWrapped().hashCode();
        result = 31 * result + getOptionText().hashCode();
        return result;
    }

    /**
     * Adds a child for tree options. Note that children are absolutely ignored by {@link SingleChoice single} and {@link MultipleChoice multi} choices.
     * @param child Child to be added to children collection.
     */
    public synchronized void addChild(Option<T> child) {
        if (null == children) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    List<Option<T>> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return optionText;
    }
}
