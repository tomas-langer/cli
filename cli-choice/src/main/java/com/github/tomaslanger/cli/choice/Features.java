package com.github.tomaslanger.cli.choice;

import com.github.tomaslanger.chalk.Chalk;

import java.util.List;

/**
 * Demonstration of features of this module. Just run it ;)
 *
 * User: Tomas.Langer
 * Date: 24.12.2015
 * Time: 0:14
 *
 * @author Tomas Langer (tomas.langer@gmail.com)
 */
public class Features {
    public static void main(String[] args) {
        Features features = new Features();
        features.singleChoice();
        features.multiChoice();
    }

    private void multiChoice() {
        System.out.println();
        System.out.println("***************************************************");
        System.out.println("** Multiple Choice                               **");
        System.out.println("***************************************************");
        System.out.println();

        MultipleChoice multi = MultipleChoice.Builder.multipleChoice();
        List<String> select = multi.select(new String[]{"a", "d"}, "a", "b", "c", "d");

        System.out.println("You have selected: " + select);

        System.out.println();
        System.out.println("Multiple choice with option \"b\" configured as fixed");
        List<Option<String>> options = multi.select(new Option<>("a", "a", true), new Option<>("b", "b", true, true), new Option<>("c"), new Option<>("d"));

        System.out.println("You have selected: " + options);
    }

    private void singleChoice() {
        System.out.println();
        System.out.println("***************************************************");
        System.out.println("** Single Choice                                 **");
        System.out.println("***************************************************");
        System.out.println();
        SingleChoice choice = SingleChoice.Builder.singleChoice();

        System.out.println("Please select your option:");
        String selected = choice.select("love", "make", "love", "not", "war");
        System.out.println("You have selected = " + selected);

        System.out.println("Single choice, immediate select");
        choice = new SingleChoice.Builder()
                .setChoiceSelects()
                .build();
        selected = choice.select("love", "make", "love", "not", "war");
        System.out.println("You have selected = " + selected);


        System.out.println("Customized single choice:");
        choice = new SingleChoice.Builder()
                .setInputLineString("Customized input:> ")
                .setToggleOffString("_")
                .setToggleOnString("0")
                .setLineFormat("[%2$1s] %1$2s: %3$2s")
                .setMessageLineString("Customized message line")
                .setNotNumberMessage(Chalk.on("Enter number, customized!").yellow() + " Enter number and press enter to toggle choice. Press enter to accept the current choice.")
                .setNotValidOrdinalMessage(Chalk.on("Enter valid number, customized!").bgRed().white() + " and press enter to toggle choice. Press enter to accept the current choice.")
                .build();
        selected = choice.select("love", "make", "love", "not", "war");
        System.out.println("You have selected = " + selected);

    }
}
