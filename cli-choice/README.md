# cli-choice
> Choice (radio buttons and checkboxes) for java command line


## Why
I want java tools to have the same possibilities as javascript.
npm and related tools have nice choices ...

## Install
We are on Maven central:

     <dependency>
       <groupId>com.github.tomas-langer.cli</groupId>
       <artifactId>cli-choice</artifactId>
       <version>1.0.0</version>
     </dependency>

This project depends on:
1. [chalk - Command line colors](https://github.com/tomas-langer/chalk) - on maven central

Check my other projects:
1. [cli-progress - Command line progress bars](https://github.com/tomas-langer/cli/tree/master/cli-progress) - on maven central


# Usage
## Single option (radiobuttons):
```java
SingleChoice choice = SingleChoice.Builder.singleChoice();
String selected = choice.select("love", "make", "love", "not", "war");
```

will show:
```
Please select your option:
 1: ( ) make
 2: (*) love
 3: ( ) not
 4: ( ) war
Enter number and press enter to toggle choice. Press enter to accept the current choice.
Your choice:>
```
### Configuration
Single option specific builder options:
* choiceSelects - if set, user's choice is immediately the return value

## Multiple options (checkboxes):

```java
MultipleChoice multi = MultipleChoice.Builder.multipleChoice();
List<String> select = multi.select(new String[]{"a", "d"}, "a", "b", "c", "d");
```

will show:
```
 1: (+)  a
 2: ( )  b
 3: ( )  c
 4: (+)  d
Enter number and press enter to toggle choice. Press enter to accept the current choice.
Your choice:>
```

## Common configuration options
Builder allows you to customize:
* line format - how to print line for each option
* toggle on string - * for single and + for multi choice is default
* toggle off string - space is default
* input line string - the beginning of line on which user enters values (Your choice:> ) by default
* accept values as input - if set to true, user can input the values directly (string values)
* message line string - the string printed as "guiding" text for user
* not number message - the string printed when invalid value is entered (colored by default)
* not valid ordinal message - the string printed when out of range value is entered (colored by default)


## Configuration
There are some system properties to control behavior:
* clichoice.useDefault - disable user input and automatically use defaults
* clichoice.quiet - disable user input, automatically use defaults and do not print anything to console.

The following CI tools are currently recognized:
* Hudson - if in Hudson, same as clichoice.useDefaults=true
* Jenkins - if in Jenkins, same as clichoice.useDefaults=true


# License

[Apache License](http://www.apache.org/licenses/)