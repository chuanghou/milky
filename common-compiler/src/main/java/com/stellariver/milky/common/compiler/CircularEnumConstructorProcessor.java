package com.stellariver.milky.common.compiler;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 检测两个（或多个）枚举在常量构造参数中互相引用对方类型。
 * <p>
 * 该写法能通过 javac 语法检查，但按 JVM 枚举初始化顺序会导致部分成员字段为 null。
 */
@SupportedAnnotationTypes("*")
@SupportedOptions("milky.circularEnum.check.disable")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CircularEnumConstructorProcessor extends AbstractProcessor {

    private static final String ERROR_TEMPLATE =
            "枚举 %s 与 %s 在构造参数中互相引用对方常量，初始化时成员字段可能为 null；"
                    + "请改用 name()/ordinal、单向引用或静态解析方法";

    private Trees trees;
    private Elements elements;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        trees = Trees.instance(processingEnv);
        elements = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        if (isDisabled()) {
            return false;
        }

        Map<String, Set<String>> enumConstructorRefs = new HashMap<>();
        Map<String, TypeElement> enumTypes = new HashMap<>();

        for (Element root : roundEnv.getRootElements()) {
            collectEnumRefs(root, enumConstructorRefs, enumTypes);
        }

        reportMutualReferences(enumConstructorRefs, enumTypes);
        return false;
    }

    private boolean isDisabled() {
        return Boolean.parseBoolean(
                processingEnv.getOptions().getOrDefault("milky.circularEnum.check.disable", "false"));
    }

    private void collectEnumRefs(Element element,
                                 Map<String, Set<String>> enumConstructorRefs,
                                 Map<String, TypeElement> enumTypes) {
        if (element.getKind() == ElementKind.ENUM) {
            TypeElement enumType = (TypeElement) element;
            String fqn = enumType.getQualifiedName().toString();
            enumTypes.put(fqn, enumType);
            enumConstructorRefs.put(fqn, collectReferencedEnumTypes(enumType));
        }
        for (Element enclosed : element.getEnclosedElements()) {
            collectEnumRefs(enclosed, enumConstructorRefs, enumTypes);
        }
    }

    private Set<String> collectReferencedEnumTypes(TypeElement enumType) {
        Set<String> refs = new HashSet<>();
        String selfFqn = enumType.getQualifiedName().toString();
        String packageName = packageNameOf(enumType);

        for (Element enclosed : enumType.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.ENUM_CONSTANT) {
                continue;
            }
            Tree tree = trees.getTree(enclosed);
            if (!(tree instanceof VariableTree)) {
                continue;
            }
            new TreeScanner<Void, Void>() {
                @Override
                public Void visitMemberSelect(MemberSelectTree node, Void unused) {
                    String typeName = memberSelectToTypeName(node);
                    if (typeName == null) {
                        return super.visitMemberSelect(node, unused);
                    }
                    TypeElement refEnum = resolveEnumType(typeName, packageName, enumType);
                    if (refEnum != null) {
                        String refFqn = refEnum.getQualifiedName().toString();
                        if (!refFqn.equals(selfFqn)) {
                            refs.add(refFqn);
                        }
                    }
                    return super.visitMemberSelect(node, unused);
                }
            }.scan(((VariableTree) tree).getInitializer(), null);
        }
        return refs;
    }

    private TypeElement resolveEnumType(String typeName, String packageName, TypeElement hostEnum) {
        TypeElement type = elements.getTypeElement(typeName);
        if (type != null && type.getKind() == ElementKind.ENUM) {
            return type;
        }
        if (!typeName.contains(".") && packageName != null && !packageName.isEmpty()) {
            type = elements.getTypeElement(packageName + "." + typeName);
            if (type != null && type.getKind() == ElementKind.ENUM) {
                return type;
            }
        }
        if (!typeName.contains(".")) {
            TypeElement outer = hostEnum.getEnclosingElement() instanceof TypeElement
                    ? (TypeElement) hostEnum.getEnclosingElement()
                    : null;
            if (outer != null) {
                type = elements.getTypeElement(outer.getQualifiedName() + "." + typeName);
                if (type != null && type.getKind() == ElementKind.ENUM) {
                    return type;
                }
            }
        }
        return null;
    }

    private static String memberSelectToTypeName(MemberSelectTree node) {
        if (node.getExpression() instanceof IdentifierTree) {
            return ((IdentifierTree) node.getExpression()).getName().toString();
        }
        if (node.getExpression() instanceof MemberSelectTree) {
            String prefix = memberSelectToTypeName((MemberSelectTree) node.getExpression());
            return prefix == null ? null : prefix + "." + node.getIdentifier();
        }
        return null;
    }

    private static String packageNameOf(TypeElement type) {
        Element pkg = type.getEnclosingElement();
        while (pkg != null && pkg.getKind() != ElementKind.PACKAGE) {
            pkg = pkg.getEnclosingElement();
        }
        return pkg instanceof PackageElement ? ((PackageElement) pkg).getQualifiedName().toString() : "";
    }

    private void reportMutualReferences(Map<String, Set<String>> enumConstructorRefs,
                                        Map<String, TypeElement> enumTypes) {
        Set<String> reported = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : enumConstructorRefs.entrySet()) {
            String from = entry.getKey();
            for (String to : entry.getValue()) {
                Set<String> reverse = enumConstructorRefs.get(to);
                if (reverse == null || !reverse.contains(from)) {
                    continue;
                }
                String pairKey = from.compareTo(to) <= 0 ? from + "|" + to : to + "|" + from;
                if (!reported.add(pairKey)) {
                    continue;
                }
                TypeElement errorOn = enumTypes.getOrDefault(from, enumTypes.get(to));
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format(ERROR_TEMPLATE, simpleName(from), simpleName(to)),
                        errorOn);
            }
        }
    }

    private static String simpleName(String qualifiedName) {
        int idx = qualifiedName.lastIndexOf('.');
        return idx < 0 ? qualifiedName : qualifiedName.substring(idx + 1);
    }
}
