package com.example.lombok;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

public class DataAnnotationProcessor extends AbstractProcessor {

    private JavacTrees javacTrees;
    private TreeMaker treeMaker;
    private Names names;


    /**
     * 初始化一些操作
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        javacTrees = JavacTrees.instance(context);
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Data.class);
        for (Element element : set) {
            JCTree tree = javacTrees.getTree(element);
            tree.accept(new TreeTranslator(){
                @Override
                public void visitClassDef(JCTree.JCClassDecl tree) {
                    tree.defs.stream()
                            .filter(it -> it.getKind().equals(Tree.Kind.VARIABLE))
                                    .map(it -> ((JCTree.JCVariableDecl) it))
                                            .forEach(it -> {
                                                tree.defs = tree.defs.prepend(genGetterMethod(it));
                                                tree.defs = tree.defs.prepend(genSetterMethod(it));
                                            });
                    super.visitClassDef(tree);
                }
            });
        }

        return true;
    }

    private JCTree genSetterMethod(JCTree.JCVariableDecl it) {
        return null;
    }

    private JCTree genGetterMethod(JCTree.JCVariableDecl it) {
        return null;
    }
}
