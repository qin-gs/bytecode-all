package com.example.lombok;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.code.Flags;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Locale;
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

        // 获取被 Data 注解的类的集合，依次处理
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Data.class);
        for (Element element : set) {
            // 获取当前处理类的抽象语法树
            JCTree tree = javacTrees.getTree(element);
            tree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl tree) {
                    // 过滤器类中所有的字段，遍历字段生成 get/set 方法
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

    private JCTree genSetterMethod(JCTree.JCVariableDecl jCVariableDecl) {
        JCTree.JCReturn returnStatement = treeMaker.Return(
                treeMaker.Select(
                        treeMaker.Ident(names.fromString("this")),
                        jCVariableDecl.getName()
                )
        );
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<JCTree.JCStatement>().append(returnStatement);
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.PUBLIC);
        Name getMethodName = getMethodName(jCVariableDecl.getName());
        JCTree.JCExpression returnMethodType = jCVariableDecl.vartype;
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        List<JCTree.JCTypeParameter> methodGenericParamList = List.nil();
        List<JCTree.JCVariableDecl> parameterList = List.nil();
        List<JCTree.JCExpression> thrownCauseList = List.nil();

        // 方法访问级别修饰符， get 方法名， 返回值类型， 泛型参数列表， 参数值列表， 异常抛出列表， 方法体， 默认值
        return treeMaker.MethodDef(modifiers, getMethodName, returnMethodType, methodGenericParamList, parameterList, thrownCauseList, body, null);
    }

    private Name getMethodName(Name name) {
        String fieldName = name.toString();
        return names.fromString("get" + fieldName.substring(0, 1).toLowerCase(Locale.ROOT) + fieldName.substring(1));
    }

    private Name setMethodName(Name name) {
        String fieldName = name.toString();
        return names.fromString("set" + fieldName.substring(0, 1).toLowerCase(Locale.ROOT) + fieldName.substring(1));
    }

    private JCTree genGetterMethod(JCTree.JCVariableDecl jCVariableDecl) {
        JCTree.JCExpressionStatement returnStatement = treeMaker.Exec(
                treeMaker.Assign(
                        treeMaker.Select(
                                treeMaker.Ident(names.fromString("this")),
                                jCVariableDecl.getName()
                        ),
                        treeMaker.Ident(jCVariableDecl.getName())
                )
        );
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<JCTree.JCStatement>().append(returnStatement);
        JCTree.JCVariableDecl param = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PARAMETER, List.nil()),
                jCVariableDecl.name,
                jCVariableDecl.vartype,
                null
        );
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.PUBLIC);
        Name setMethodName = setMethodName(jCVariableDecl.getName());
        JCTree.JCExpression returnMethodType = treeMaker.Type(new Type.JCVoidType());
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        List<JCTree.JCTypeParameter> methodGenericParamList = List.nil();
        List<JCTree.JCVariableDecl> parameterList = List.of(param);
        List<JCTree.JCExpression> thrownCauseList = List.nil();

        // 生成方法定义语法树节点
        // 方法访问级别修饰符，set 方法名，返回值类型，泛型参数列表，参数值列表，异常抛出列表，方法体，默认值
        return treeMaker.MethodDef(
                modifiers,
                setMethodName,
                returnMethodType,
                methodGenericParamList,
                parameterList,
                thrownCauseList,
                body,
                null
        );
    }
}
