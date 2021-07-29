package com.github.schorcher.stringInterpolator.expression;

import com.github.schorcher.stringInterpolator.StringInterpolation;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import java.util.List;
import java.util.function.Supplier;

public class InnerStringVarsAstTranslator extends TreeTranslator {

    private final TreeMaker treeMaker;
    private final ExpUtil expUtil;
    private final ExpressionParser expressionParser;

    private boolean interpolate;

    public InnerStringVarsAstTranslator(Context context) {
        this.treeMaker = TreeMaker.instance(context);
        this.expUtil = new ExpUtil();
        this.expressionParser = new ExpressionParser(Names.instance(context));
    }

    @Override
    public <T extends JCTree> T translate(T t) {
        return super.translate(t);
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        doWithInterpolateResolving(() -> isAnnotatedByInterpolate(jcClassDecl.getModifiers()) || interpolate,
                () -> super.visitClassDef(jcClassDecl));
    }

    @Override
    public void visitAnnotation(JCTree.JCAnnotation jcAnnotation) {
        doWithInterpolateResolving(() -> false,
                () -> super.visitAnnotation(jcAnnotation));
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
        doWithInterpolateResolving(() -> isAnnotatedByInterpolate(jcMethodDecl.getModifiers()) || interpolate,
                () -> super.visitMethodDef(jcMethodDecl));
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
        doWithInterpolateResolving(() -> isAnnotatedByInterpolate(jcVariableDecl.getModifiers()) || interpolate,
                () -> super.visitVarDef(jcVariableDecl));
    }

    private boolean isAnnotatedByInterpolate(JCTree.JCModifiers modifiers) {
        for (JCTree.JCAnnotation annotation : modifiers.getAnnotations()) {
            if (annotation.getAnnotationType() == null || annotation.type == null) {
                continue;
            }
            if (annotation.type.toString().equals(StringInterpolation.class.getCanonicalName())) {
                return true;
            }
        }
        return false;
    }

    private void doWithInterpolateResolving(Supplier<Boolean> interpolateResolver, Runnable run) {
        boolean interpolateBefore = interpolate;
        interpolate = interpolateResolver.get();
        run.run();
        interpolate = interpolateBefore;
    }

    @Override
    public void visitLiteral(JCTree.JCLiteral jcLiteral) {
        super.visitLiteral(jcLiteral);
        if (!interpolate) {
            return;
        }
        if (jcLiteral.getValue() instanceof String) {

            List<Expression> expressions = expUtil.split(jcLiteral);

            if (expressions.isEmpty()) {
                return;
            }

            if (expressions.size() == 1) {
                result = convertToExpression(expressions.get(0));
                return;
            }

            JCTree.JCExpression exprLeft = convertToExpression(expressions.get(0));
            for (int i = 1; i < expressions.size(); i++) {
                JCTree.JCExpression exprRight = convertToExpression(expressions.get(i));
                exprLeft = treeMaker.Binary(JCTree.Tag.PLUS, exprLeft, exprRight);
                exprLeft.setPos(expressions.get(0).getOffset());
            }

            result = exprLeft;
        }
    }

    private JCTree.JCExpression convertToExpression(Expression expression) {
        switch (expression.getExpType()) {
            case EXPRESSION:
                return expressionParser.parse(expression);
            case STRING_LITERAL:
                JCTree.JCLiteral literal = treeMaker.Literal(expression.getValue());
                literal.setPos(expression.getOffset());
                return literal;
            default:
                throw new RuntimeException("Unexpected token type: " + expression.getExpType());
        }
    }

}
