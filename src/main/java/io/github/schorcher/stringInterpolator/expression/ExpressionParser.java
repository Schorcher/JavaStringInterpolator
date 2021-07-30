package io.github.schorcher.stringInterpolator.expression;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Names;

import javax.tools.*;
import java.net.URI;
import java.util.Collections;

import static java.lang.String.format;

public class ExpressionParser {

    private final Names names;

    public ExpressionParser(Names names) {
        this.names = names;
    }

    public JCTree.JCExpression parse(Expression token) {
        CompilationUnitTree tree = getCompilationUnitTree(token.getValue());
        JCTree.JCClassDecl declr = (JCTree.JCClassDecl) tree.getTypeDecls().get(0);
        JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) declr.getMembers().get(0);
        JCTree.JCExpression expression = field.getInitializer();
        expression.setPos(token.getOffset());
        expression.accept(new IdentResolver(token.getOffset()));
        return expression;
    }

    private CompilationUnitTree getCompilationUnitTree(String code) {
        JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        JavaFileManager fileManager = tool.getStandardFileManager(null, null, null);
        JavacTaskImpl task = (JavacTaskImpl) tool.getTask(null, fileManager, null, null, null,
                Collections.singletonList(new TempClassWrapper(code)));

        try {
            return task.parse().iterator().next();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(format("Error parsing in string literal: %s", code), e);
        }
    }

    private static class TempClassWrapper extends SimpleJavaFileObject {

        private final String text;

        public TempClassWrapper(String text) {
            super(URI.create("myfake:/Test.java"), JavaFileObject.Kind.SOURCE);
            this.text = "class Test { Object value = String.valueOf(" + text + "); }";
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return text;
        }
    }

    private class IdentResolver extends TreeTranslator {

        private final int offset;

        public IdentResolver(int offset) {
            this.offset = offset;
        }

        @Override
        public void visitApply(JCTree.JCMethodInvocation jcMethodInvocation) {
            super.visitApply(jcMethodInvocation);
            jcMethodInvocation.pos = offset;
        }

        @Override
        public void visitIdent(JCTree.JCIdent jcIdent) {
            super.visitIdent(jcIdent);
            jcIdent.name = names.fromString(jcIdent.getName().toString());
            jcIdent.pos = offset;
        }

        @Override
        public void visitSelect(JCTree.JCFieldAccess jcFieldAccess) {
            super.visitSelect(jcFieldAccess);
            jcFieldAccess.name = names.fromString(jcFieldAccess.name.toString());
            jcFieldAccess.pos = offset;
        }
    }

}
