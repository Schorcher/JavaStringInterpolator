package io.github.schorcher.stringInterpolator.expression;

import com.sun.tools.javac.tree.JCTree;

import java.util.ArrayList;
import java.util.List;

public class ExpUtil {

    private final String START = "${";
    private final String END = "}";

    public List<Expression> split(JCTree.JCLiteral jcLiteral) {
        String literalValue = (String) jcLiteral.getValue();
        int originalOffset = jcLiteral.getPreferredPosition();
        return split(literalValue, originalOffset);
    }

    public List<Expression> split(String literalValue, int originalOffset) {
        List<Expression> expressions = new ArrayList<>();
        int startIndex = 0;

        while (startIndex < literalValue.length()) {
            int headIndex = literalValue.indexOf(START, startIndex), offset = originalOffset + headIndex + START.length() + 1;

            if (headIndex < 0) {
                expressions.add(new Expression(literalValue.substring(startIndex), ExpType.STRING_LITERAL, offset));
                break;
            }

            int endIndex = literalValue.indexOf(END, headIndex);
            if (endIndex < 0)
                throw new RuntimeException();

            String prefix = literalValue.substring(startIndex, headIndex);
            if (!prefix.equals(""))
                expressions.add(new Expression(prefix, ExpType.STRING_LITERAL, offset));

            String variable = literalValue.substring(headIndex + START.length(), endIndex);
            if (!variable.equals(""))
                expressions.add(new Expression(variable, ExpType.EXPRESSION, offset));

            startIndex = endIndex + END.length();
        }

        return expressions;
    }

}
