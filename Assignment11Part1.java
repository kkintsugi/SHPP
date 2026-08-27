package com.shpp.p2p.cs.dyushchenko.assignment11;

import java.util.*;

import static java.util.Map.entry;

/**
 * A console calculator that retrieves a formula from args[], and variables, if they are present in the formula.
 * Parses expression from args[0] to Revere Polish Notation (RPN).
 * Creates HashMap with variables and their values from other arguments from args[], not including formula in args[0].
 * Supports unary/binary operators, parenthesis and certain mathematical functions.
 */
public class Assignment11Part1 {
    /**
     * Map of supported operators (including unary minus '~') and their corresponding precedence
     */
    private static final Map<Character, Integer> OPERATORS = Map.ofEntries(
            entry('+', 1),
            entry('-', 1),
            entry('*', 2),
            entry('/', 2),
            entry('^', 3),
            entry('~', 4)
    );
    /**
     * Array of supported right associative operators (evaluated from right to left)
     */
    private static final char[] RIGHT_ASSOCIATIVE_OPERATORS = {'^', '~'};

    /**
     * Maps supported math function names to their corresponding Java functions.
     */
    private static final Map<String, IAction> FUNCTIONS = Map.ofEntries(
            entry("sin", Math::sin),
            entry("cos", Math::cos),
            entry("tan", Math::tan),
            entry("atan", Math::atan),
            entry("log10", Math::log10),
            entry("log2", Assignment11Part1::log2),
            entry("sqrt", Math::sqrt)
    );

    /**
     * Calculates the base-2 logarithm of a given number.
     *
     * @param number The given number to calculate.
     * @return The logarithm of the argument base-2.
     */
    private static double log2(double number) {
        return Math.log(number) / Math.log(2.0);
    }

    /**
     * Starting point of the program. Calculates given mathematical expression with or without given variables
     *
     * @param args A line entered by the user, where args[0] is the formula
     *             and the rest is other variables for the formula
     */
    static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.out.println("!Invalid input: please type formula and, if necessary, the variables");
                return;
            }

            String formula = args[0];
            System.out.println("Your entered formula: \"" + formula + "\"");

            HashMap<String, Double> variables = createVariablesMap(args);
            if (variables.isEmpty()) {
                System.out.println("You haven't entered any variables.");
            } else {
                System.out.println("Your variables:\n\t" + variables);
            }

            String parsedFormula = parseFormula(formula);
            double result = calculate(parsedFormula, variables);
            if (Double.isNaN(result)) {
                throw new ArithmeticException("!Result is not a number: expression calculated as an undefined result.");
            } else if (Double.isInfinite(result)) {
                throw new ArithmeticException("!Result is not a number: expression calculated as infinity (\"" + result + "\").");
            }
            System.out.println("Result: " + result);
        } catch (ArithmeticException | VariableNotFoundException | StringIndexOutOfBoundsException |
                 InvalidVariableFormatException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (EmptyStackException e) {
            System.out.println("\n!Invalid mathematical input: missing operand or mismatched operators.");
        } catch (Exception e) {
            System.out.println("\nException: " + e.getClass().getSimpleName());
            System.out.println(e.getMessage());
        }
    }

    /**
     * Creates HashMap with variables and their values from line entered by the user.
     *
     * @param args A line entered by the user
     * @return HashMap of variables or null, if there is no variables in the line, or they are typed incorrectly
     * throws NumberFormatException if a variable name is invalid, a variable is missing the '=' sign,
     * a variable has no value, or the value is not a valid number.
     */
    public static HashMap<String, Double> createVariablesMap(String[] args) {
        HashMap<String, Double> variables = new HashMap<>();
        if (args.length == 1) {
            return variables;
        }

        String[] arguments = Arrays.copyOfRange(args, 1, args.length);
        for (String argument : arguments) {
            if (argument.isBlank()) {
                throw new InvalidVariableFormatException("!Incorrect variables input: you entered a blank argument for a variable.");
            } else if (!hasEqualSign(argument)) {
                throw new InvalidVariableFormatException("!Incorrect variables input: \"" + argument + "\" doesn`t have equal sign");
            }

            String[] parts = argument.split("=");

            String variableName = getVariableName(parts);
            double variableValue = getVariableValue(parts, variableName);

            if (variables.containsKey(variableName)) {
                System.out.println("\tYou entered the same variable \"" + variableName + "\" more than once:");
                System.out.println("\t\"" + variableName + "\" is already added - " + variableName + " = " + variables.get(variableName));
                continue;
            }

            variables.put(variableName, variableValue);
        }
        return variables;
    }

    /**
     * Checks if the given string contains an equal sign.
     * This is used to check whether a variable is formed correctly with equal sign.
     *
     * @param argument the variable string to check (e.g. "x = 2")
     * @return True if argument contains equal sign character, false otherwise.
     */
    private static boolean hasEqualSign(String argument) {
        return argument.indexOf('=') != -1;
    }

    /**
     * Gets and checks variable name from the array returned by the split.
     *
     * @param parts The array of strings passed using split, where the variable name is at index 0.
     * @return The name of the variable.
     * throws InvalidVariableFormatException if variable name contains not letter or numerical elements.
     */
    private static String getVariableName(String[] parts) {
        String variableName = parts[0].trim();
        if (!containsOnlyLettersOrDigits(variableName)) {
            throw new InvalidVariableFormatException("!Incorrect variables input: variable name \" " + variableName + " \" contains not letter or numerical elements.");
        }
        return variableName;
    }

    /**
     * Gets and checks variable value from the array returned by the split.
     *
     * @param parts        The array of strings passed using split, where the variable value is at index 1.
     * @param variableName The name of the variable.
     * @return The numerical value of the variable.
     * throws InvalidVariableFormatException if string at index 1 is empty or is not numerical.
     */
    private static double getVariableValue(String[] parts, String variableName) {
        String potentialValue = parts[1].trim();
        if (potentialValue.isEmpty()) {
            throw new InvalidVariableFormatException("!Incorrect variables input: variable \"" + variableName + "\" has no numeric value.");
        } else if (!isNumerical(potentialValue)) {
            throw new InvalidVariableFormatException("!Incorrect variables input: " + potentialValue + " is not a valid numerical character.");
        }
        return Double.parseDouble(potentialValue);
    }

    /**
     * Checks if the name of the given variable is valid by containing only letters or digits.
     *
     * @param variableName The name of the given variable.
     * @return True if the name contains only letters or digits, false otherwise.
     */
    private static boolean containsOnlyLettersOrDigits(String variableName) {
        if (variableName.isEmpty()) {
            return false;
        } else {
            for (char ch : variableName.toCharArray()) {
                if (!Character.isLetter(ch) & !Character.isDigit(ch)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if the value of the variable is a number.
     *
     * @param variableValue The potential value of the variable.
     * @return True if it is numerical, false otherwise.
     */
    private static boolean isNumerical(String variableValue) {
        try {
            Double.parseDouble(variableValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Parses the formula entered by the user into the RPN (Reverse Polish Notation) format.
     *
     * @param formula The given mathematical expression.
     * @return The parsed formula in RPN format.
     * throws StringIndexOutOfBoundsException if the formula is empty
     * throws EmptyStackException if the entered formula has unnecessary or mismatched operators.
     * throws IllegalArgumentException if the entered formula contains some not supported characters.
     */
    public static String parseFormula(String formula) {
        formula = formula.replaceAll("\\s+", "");
        if (formula.isEmpty()) {
            throw new StringIndexOutOfBoundsException("!Invalid formula input: formula is empty.");
        }

        char currentChar;
        char[] formulaCharArray = formula.toCharArray();
        Queue<String> outputQueue = new LinkedList<>();
        Stack<String> operationsStack = new Stack<>();
        StringBuilder sb = new StringBuilder();

        for (int index = 0; index < formulaCharArray.length; index++) {
            currentChar = formulaCharArray[index];
            if (Character.isDigit(currentChar)) {
                while (index < formulaCharArray.length && (Character.isDigit(formulaCharArray[index]) || formulaCharArray[index] == '.')) {
                    sb.append(formulaCharArray[index]);
                    index++;
                }
                index--;
                outputQueue.add(String.valueOf(sb));
                sb.setLength(0);
            } else if (Character.isAlphabetic(currentChar)) {
                while (index < formulaCharArray.length && (Character.isDigit(formulaCharArray[index]) || Character.isLetter(formulaCharArray[index]))) {
                    sb.append(formulaCharArray[index]);
                    index++;
                }
                index--;
                if (isFunction(String.valueOf(sb))) {
                    operationsStack.push(sb.toString());
                    sb.setLength(0);
                } else {
                    outputQueue.add(String.valueOf(sb));
                    sb.setLength(0);
                }
            } else if (isOperator(currentChar)) {
                if (currentChar == '-' && isUnary(formulaCharArray, index)) {
                    currentChar = '~';
                }
                while ((!operationsStack.empty() && !operationsStack.peek().equals("(")) && shouldPop(currentChar, operationsStack.peek().charAt(0))) {
                    outputQueue.add(operationsStack.pop());
                }
                operationsStack.push(String.valueOf(currentChar));
            } else if (currentChar == '(') {
                if (isNumberBeforeParenthesis(index, formulaCharArray, operationsStack)) {
                    throw new ArithmeticException("!Missing operator before the \"(\".");
                }

                operationsStack.push(String.valueOf(currentChar));
            } else if (currentChar == ')') {
                while (!operationsStack.empty() && !operationsStack.peek().equals("(")) {
                    outputQueue.add(operationsStack.pop());
                }
                if (operationsStack.empty()) {
                    throw new EmptyStackException(); // If mismatched parenthesis
                }
                operationsStack.pop(); // Pops the left parenthesis
                if (!operationsStack.empty() && isFunction(operationsStack.peek())) {
                    outputQueue.add(operationsStack.pop());
                }
            } else {
                if (currentChar == ',') {
                    throw new NumberFormatException("!Invalid mathematical format: entered numbers have commas (\",\") instead of periods (\".\").");
                } else {
                    throw new IllegalArgumentException("!Invalid input for parsing: the entered formula contains some not supported characters.");
                }
            }
        }
        while (!operationsStack.empty()) {
            outputQueue.add(operationsStack.pop());
        }
        return String.join(" ", outputQueue);
    }

    /**
     * Checks if the character is operator included in the map of supported operators.
     *
     * @param givenChar The given character.
     * @return True if it is operator, false otherwise.
     */
    private static boolean isOperator(char givenChar) {
        for (char ch : OPERATORS.keySet()) {
            if (givenChar == ch) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the '-' operator is unary in the given formula.
     *
     * @param formulaCharArray The given formula in character array format.
     * @param index            The index of the '-' in the character array.
     * @return True if it is unary, false otherwise.
     */
    private static boolean isUnary(char[] formulaCharArray, int index) {
        if (index == 0) {
            return true;
        } else if (index - 1 >= 0) {
            char previousChar = formulaCharArray[index - 1];
            if (previousChar == '(') {
                return true;
            } else if (isOperator(previousChar) && previousChar != '~') {
                return true;
            }
        }
        return false;
    }

    /**
     * Main logic for parsing algorithm.
     * Decides if an operator that currently on top of the stack should be popped before pushing the new one.
     *
     * @param newOperator   The new operator that came from the formula.
     * @param operatorOnTop The operator that currently at the top of the stack.
     * @return True if the operator on top should be popped and added to the output queue, false otherwise.
     */
    private static boolean shouldPop(char newOperator, char operatorOnTop) {
        int precedenceNew = getPrecedence(newOperator);
        int precedenceTop = getPrecedence(operatorOnTop);

        if (precedenceTop > precedenceNew) {
            return true;
        } else if (precedenceNew == precedenceTop && isLeftAssociative(newOperator)) {
            return true;
        }
        return false;
    }

    /**
     * Gets precedence of the operator.
     * Used to correctly calculate RPN parsing algorithm.
     *
     * @param givenOperator The given operator.
     * @return The precedence in int format. The higher the number, the higher the operator's precedence
     */
    private static int getPrecedence(char givenOperator) {
        for (Character operator : OPERATORS.keySet()) {
            if (givenOperator == operator) {
                return OPERATORS.get(givenOperator);
            }
        }
        return 0;
    }

    /**
     * Checks if the operator is left associative (evaluated from left to right).
     *
     * @param operator The given operator.
     * @return True if it is left associative, false otherwise.
     */
    private static boolean isLeftAssociative(char operator) {
        for (char op : RIGHT_ASSOCIATIVE_OPERATORS) {
            if (operator == op) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given string is the name of one of the supported functions.
     *
     * @param potentialFunctionName The given string for comparison.
     * @return True if the name matches, false otherwise
     */
    private static boolean isFunction(String potentialFunctionName) {
        for (String function : FUNCTIONS.keySet()) {
            if (potentialFunctionName.equals(function)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is a number before left parenthesis.
     * Used to avoid calculation errors in cases such as "1 + 2(x * 2)", where there
     * is missing between the number and parenthesis.
     *
     * @param charIndex        The index of a current char.
     * @param formulaCharArray User-entered formula as an array of characters.
     * @param operationsStack  Stack of already parsed operators.
     * @return True if there is a number just before left parenthesis, false otherwise.
     */
    private static boolean isNumberBeforeParenthesis(int charIndex, char[] formulaCharArray, Stack<String> operationsStack) {
        if (charIndex != 0) {
            char previousChar = formulaCharArray[charIndex - 1];
            if (Character.isDigit(previousChar) && !isFunction(operationsStack.peek())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates a mathematical expression from parsed formula in Reverse Polish Notation (RPN) format.
     *
     * @param parsedFormula The formula string formatted in RPN format, space separated
     * @param variables     A map containing variable names and their corresponding values.
     * @return The calculated result of the expression, but throws exceptions if formula can't be calculated correctly.
     */
    public static double calculate(String parsedFormula, HashMap<String, Double> variables) {
        String[] tokens = parsedFormula.split("\\s+");
        Stack<String> numbersStack = new Stack<>();

        char firstChar;
        for (String currentToken : tokens) {
            firstChar = currentToken.charAt(0);
            if (Character.isDigit(firstChar)) {
                numbersStack.push(currentToken);
            } else if (Character.isLetter(firstChar)) {
                if (isFunction(currentToken)) {
                    double lastNumber = Double.parseDouble(numbersStack.pop());
                    double result = calculateFunction(lastNumber, currentToken);
                    numbersStack.push(String.valueOf(result));
                } else {
                    Object potentialVariableValue = variables.get(currentToken);
                    if (potentialVariableValue == null) {
                        throw new VariableNotFoundException();
                    }
                    double variableValue = (double) potentialVariableValue;
                    numbersStack.push(String.valueOf(variableValue));
                }
            } else if (isOperator(firstChar) && currentToken.length() == 1) {
                if (firstChar == '~') {
                    try {
                        double negativeNum = Double.parseDouble(numbersStack.pop());
                        numbersStack.push(String.valueOf(-negativeNum));
                    } catch (NumberFormatException e) {
                        throw new NumberFormatException("!Invalid number input: multiple decimal points in a number.");
                    }
                } else {
                    try {
                        double num2 = Double.parseDouble(numbersStack.pop());
                        double num1 = Double.parseDouble(numbersStack.pop());
                        double result = calculateExpression(num1, num2, firstChar);
                        numbersStack.push(String.valueOf(result));
                    } catch (NumberFormatException e) {
                        throw new NumberFormatException("!Invalid number input: multiple decimal points in a number.");
                    }
                }
            } else {
                throw new IllegalArgumentException("!Unpredictable error occurred during calculation.");
            }
        }
        return Double.parseDouble(numbersStack.pop());
    }

    /**
     * Calculates a function of a number.
     *
     * @param number       The number to calculate
     * @param functionName The name of the function to use.
     * @return The result of a calculation.
     * throws IllegalArgumentException if the function could not be found by name in the map of supported functions.
     */
    private static double calculateFunction(double number, String functionName) {
        IAction function = FUNCTIONS.get(functionName);
        if (function != null) {
            return function.calculate(number);
        } else {
            throw new IllegalArgumentException("!Function \"" + functionName + "\" is not supported and cannot be calculated.");
        }
    }

    /**
     * Calculates operation between two given numbers.
     *
     * @param num1     The first given number.
     * @param num2     The second given number.
     * @param operator The operation to be executed.
     * @return The calculated result value.
     * throws ArithmeticException if an illegal mathematical operation appears
     * throws IllegalArgumentException if a not supported operator appears
     */
    private static double calculateExpression(double num1, double num2, char operator) {
        switch (operator) {
            case '+':
                return num1 + num2;
            case '-':
                return num1 - num2;
            case '*':
                return num1 * num2;
            case '/':
                if (num2 == 0) {
                    throw new ArithmeticException("!You cannot divide a number by zero.");
                }
                return num1 / num2;
            case '^':
                if (num1 == 0 && num2 < 0) {
                    throw new ArithmeticException("!You can`t raise zero to a negative power.");
                }
                if (num1 < 0 && num2 == (double) 1 / 3) {
                    return Math.cbrt(num1);
                }
                return Math.pow(num1, num2);
            default:
                throw new IllegalArgumentException("!Unexpected operator: " + operator);
        }
    }
}

/**
 * Custom exception thrown when a variable used in the formula is not present in the variables map.
 */
class VariableNotFoundException extends RuntimeException {
    public VariableNotFoundException() {
        super("!The variable used for the calculation can`t be found.");
    }
}

/**
 * Custom exception thrown when an error occurs during the creation of the variables map.
 */
class InvalidVariableFormatException extends RuntimeException {
    public InvalidVariableFormatException(String message) {
        super(message);
    }
}