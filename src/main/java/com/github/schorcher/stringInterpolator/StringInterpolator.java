package com.github.schorcher.stringInterpolator;


import com.github.schorcher.stringInterpolator.expression.InnerStringVarsAstTranslator;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import static javax.lang.model.element.ElementKind.*;


@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class StringInterpolator extends AbstractProcessor {

    private JavacProcessingEnvironment env;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        env = (JavacProcessingEnvironment) processingEnv;
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver())
            return false;

        Context context = env.getContext();
        Trees trees = Trees.instance(env);

        for (Element codeElement : roundEnv.getRootElements()) {
            if (!isValidType(codeElement))
                continue;

            JCTree tree = (JCTree) trees.getPath(codeElement).getCompilationUnit();
            new InnerStringVarsAstTranslator(context).translate(tree);
        }
        return false;
    }

    private boolean isValidType(Element codeElement) {
        return codeElement.getKind() == CLASS || codeElement.getKind() == INTERFACE || codeElement.getKind() == ENUM;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
