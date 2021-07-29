package com.github.schorcher.stringInterpolator;

import com.sun.tools.javac.tree.JCTree;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private final String START = "${";
    private final String END = "}";

    public List<Token> split(JCTree.JCLiteral jcLiteral) {
        String literalValue = (String) jcLiteral.getValue();
        int originalOffset = jcLiteral.getPreferredPosition();
        return split(literalValue, originalOffset);
    }

    public List<Token> split(String literalValue) {
        return split(literalValue, 0);
    }

    public List<Token> split(String literalValue, int originalOffset) {
        List<Token> tokens = new ArrayList<>();
        int startIndex = 0;

        while (startIndex < literalValue.length()) {

            int headIndex = literalValue.indexOf(START, startIndex);
            int offset = originalOffset + headIndex + START.length() + 1;
            if (headIndex < 0) {
                tokens.add(new Token(literalValue.substring(startIndex), TokenType.STRING_LITERAL, offset));
                break;
            }

            int endIndex = literalValue.indexOf(END, headIndex);
            if (endIndex < 0) {
                throw new RuntimeException("Not found ending bracket `}` of a variable declaration in string value: " +
                        literalValue);
            }

            String prefix = literalValue.substring(startIndex, headIndex);
            if (!prefix.equals("")) {
                tokens.add(new Token(prefix, TokenType.STRING_LITERAL, offset));
            }

            String variable = literalValue.substring(headIndex + START.length(), endIndex);
            if (!variable.equals("")) {
                tokens.add(new Token(variable, TokenType.EXPRESSION, offset));
            }

            startIndex = endIndex + END.length();
        }

        return tokens;
    }

}
