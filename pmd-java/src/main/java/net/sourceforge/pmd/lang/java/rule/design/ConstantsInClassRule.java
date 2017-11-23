package net.sourceforge.pmd.lang.java.rule.design;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Jennifer Busta
 */
public class ConstantsInClassRule extends AbstractJavaRule {

    // Identifiers in the counters map
    public static final String PUBLIC_CONSTANTS = "publicConstants";
    public static final String PUBLIC_CONSTRUCTORS = "publicConstructors";
    public static final String PRIVATE_CONSTRUCTORS = "privateConstructors";
    public static final String OTHER_ELEMENTS = "otherElements";

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        ASTImplementsList implementsList = node.getFirstChildOfType(ASTImplementsList.class);
        ASTExtendsList extendsList = node.getFirstChildOfType(ASTExtendsList.class);

        ASTClassOrInterfaceBody body = node.getFirstChildOfType(ASTClassOrInterfaceBody.class);
        Map<String, Integer> counters = getCounters(body);

        if (isPublicClass(node) &&
                implementsList == null &&
                extendsList == null &&
                hasOnlyPublicConstants(counters.get(PUBLIC_CONSTANTS), counters.get(OTHER_ELEMENTS)) &&
                !hasOnlyPrivateConstructors(counters.get(PRIVATE_CONSTRUCTORS), counters.get(PUBLIC_CONSTRUCTORS))) {
            addViolation(data, node);
        }

        return super.visit(node, data);
    }

    private boolean hasOnlyPublicConstants(int publicConstants, int otherElements) {
        if (otherElements == 0 && publicConstants > 0) {
            return true;
        }

        return false;
    }

    private boolean hasOnlyPrivateConstructors(int privateConstructors, int publicConstructors){
        if (privateConstructors > 0 && publicConstructors == 0) {
            return true;
        }

        return false;
    }

    private boolean isPublicClass(ASTClassOrInterfaceDeclaration node) {
        if (node.isPublic() && !node.isInterface()) {
            return true;
        }

        return false;
    }

    private boolean isPublicConstant(ASTFieldDeclaration fieldDeclaration) {
        if(fieldDeclaration.isPublic() && fieldDeclaration.isStatic() && fieldDeclaration.isFinal()) {
            return true;
        }

        return false;
    }

    private boolean isPrivateConstructor(ASTConstructorDeclaration constructorDeclaration) {
        if (constructorDeclaration.isPrivate()) {
            return true;
        }

        return false;
    }

    private Map<String, Integer> getCounters(ASTClassOrInterfaceBody body) {
        Map<String, Integer> counters = new ConcurrentHashMap<>();
        counters.put(PUBLIC_CONSTANTS, 0);
        counters.put(PRIVATE_CONSTRUCTORS, 0);
        counters.put(PUBLIC_CONSTRUCTORS, 0);
        counters.put(OTHER_ELEMENTS, 0);

        for (int i = 0; i < body.jjtGetNumChildren(); i++) {
            ASTClassOrInterfaceBodyDeclaration declaration = (ASTClassOrInterfaceBodyDeclaration) body.jjtGetChild(i);
            Node node = declaration.jjtGetChild(0);

            if (node instanceof ASTFieldDeclaration && isPublicConstant((ASTFieldDeclaration) node)) {
                counters.put(PUBLIC_CONSTANTS, counters.get(PUBLIC_CONSTANTS) + 1);
            } else if(node instanceof ASTConstructorDeclaration) {
                if (isPrivateConstructor((ASTConstructorDeclaration) node)) {
                    counters.put(PRIVATE_CONSTRUCTORS, counters.get(PRIVATE_CONSTRUCTORS) + 1);
                } else {
                    counters.put(PUBLIC_CONSTRUCTORS, counters.get(PUBLIC_CONSTRUCTORS) + 1);
                }
            } else {
                counters.put(OTHER_ELEMENTS, counters.get(OTHER_ELEMENTS) + 1);
            }
        }

        return counters;
    }
}
