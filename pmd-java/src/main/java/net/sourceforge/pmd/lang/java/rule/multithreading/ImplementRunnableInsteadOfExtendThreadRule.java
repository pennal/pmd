package net.sourceforge.pmd.lang.java.rule.multithreading;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 *
 * @author Jennifer Busta
 */
public class ImplementRunnableInsteadOfExtendThreadRule extends AbstractJavaRule {

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        ASTClassOrInterfaceBody body = node.getFirstChildOfType(ASTClassOrInterfaceBody.class);

        if (hasValidRunMethod(body) && !isTypeDeclarationValid(node)) {
            addViolation(data, node);
        }

        return super.visit(node, data);
    }

    private boolean isTypeDeclarationValid(ASTClassOrInterfaceDeclaration declaration) {
        ASTImplementsList implementsList = declaration.getFirstChildOfType(ASTImplementsList.class);
        if (!isTypeInList(implementsList, Runnable.class)) {
            return false;
        }

        ASTExtendsList extendsList = declaration.getFirstChildOfType(ASTExtendsList.class);
        if (isTypeInList(extendsList, Thread.class)) {
            return false;
        }

        return true;
    }

    private boolean isTypeInList(AbstractJavaNode list, Class<?> type) {
        if (list == null) {
            return false;
        }

        for (int i = 0; i < list.jjtGetNumChildren(); i++) {
            ASTClassOrInterfaceType classOrInterfaceType = (ASTClassOrInterfaceType) list.jjtGetChild(i);
            if (classOrInterfaceType.getType().equals(type)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasValidRunMethod(ASTClassOrInterfaceBody node) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (node.jjtGetChild(i) instanceof ASTClassOrInterfaceBodyDeclaration) {
                ASTClassOrInterfaceBodyDeclaration declaration = (ASTClassOrInterfaceBodyDeclaration) node.jjtGetChild(i);
                if (isValidRunMethod(declaration)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidRunMethod(ASTClassOrInterfaceBodyDeclaration declaration) {
        ASTAnnotation annotation = declaration.getFirstChildOfType(ASTAnnotation.class);
        if (annotation == null) {
            return false;
        }

        ASTMarkerAnnotation markerAnnotation = annotation.getFirstChildOfType(ASTMarkerAnnotation.class);
        if(!isOverrideAnnotation(markerAnnotation)) {
            return false;
        }

        ASTMethodDeclaration methodDeclaration = declaration.getFirstChildOfType(ASTMethodDeclaration.class);
        if (!isValidMethodDeclaration(methodDeclaration)) {
            return false;
        }

        return true;
    }

    private boolean isOverrideAnnotation(ASTMarkerAnnotation markerAnnotation) {
        if (markerAnnotation == null) {
            return false;
        }

        if (!markerAnnotation.jjtGetFirstToken().getImage().equals("@")) {
            return false;
        }

        if (!markerAnnotation.jjtGetLastToken().getImage().equals("Override")) {
            return false;
        }

        return true;
    }

    private boolean isValidMethodDeclaration(ASTMethodDeclaration methodDeclaration) {
        if (!methodDeclaration.isVoid()) {
            return false;
        }

        if (!methodDeclaration.getMethodName().equals("run")) {
            return false;
        }

        ASTMethodDeclarator methodDeclarator = methodDeclaration.getFirstChildOfType(ASTMethodDeclarator.class);
        if (methodDeclarator.getParameterCount() != 0) {
            return false;
        }

        return true;
    }
}
