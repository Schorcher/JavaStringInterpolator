package com.github.schorcher.stringInterpolator;


import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Set;


@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class StringInterpolator extends AbstractProcessor {

    private JavacProcessingEnvironment env;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        messager = processingEnv.getMessager();
        env = (JavacProcessingEnvironment) processingEnv;
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        Context context = env.getContext();
        Trees trees = Trees.instance(env);

        for (Element codeElement : roundEnv.getRootElements()) {
            if (!isClassOrEnum(codeElement)) {
                continue;
            }
            JCTree tree = (JCTree) trees.getPath(codeElement).getCompilationUnit();
            new InnerStringVarsAstTranslator(context).translate(tree);
        }

        return false;
    }

    private boolean isClassOrEnum(Element codeElement) {
        return codeElement.getKind() == ElementKind.CLASS ||
                codeElement.getKind() == ElementKind.INTERFACE ||
                codeElement.getKind() == ElementKind.ENUM;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
