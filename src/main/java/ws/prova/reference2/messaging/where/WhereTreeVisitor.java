package ws.prova.reference2.messaging.where;

import org.antlr.runtime.tree.*;

import static ws.prova.reference2.messaging.where.WhereBinaryOperation.*;
import static ws.prova.reference2.messaging.where.WhereComparison.*;

public class WhereTreeVisitor {
	private WhereTreeVisitor() {
	}

	private static WhereNode createBinaryOperation(Tree tree, Operator op) {
		return new WhereBinaryOperation(visit(tree.getChild(0)), visit(tree
				.getChild(1)), op);
	}

	private static WhereNode createComparison(Tree tree, Type type) {
		return new WhereComparison(tree.getChild(0).toString(), tree.getChild(1).toString(), type);
	}

	public static WhereNode visit(Tree tree) {
		String type = tree.toString();

            switch (type) {
                case "and":
                    return createBinaryOperation(tree, Operator.And);
                case "or":
                    return createBinaryOperation(tree, Operator.Or);
                case "xor":
                    return createBinaryOperation(tree, Operator.Xor);
                case "not":
                    return new WhereNotOperator(visit(tree.getChild(0)));
                case "<":
                    return createComparison(tree, Type.LessThan);
                case "<=":
                    return createComparison(tree, Type.LessThanOrEqual);
                case ">":
                    return createComparison(tree, Type.GreaterThan);
                case ">=":
                    return createComparison(tree, Type.GreaterThanOrEqual);
                case "!=":
                    String compareTo = tree.getChild(1).toString();
                    if (compareTo.startsWith("'")
                            || Character.isLetter(compareTo.charAt(0))) {
                        return new WhereNotEqual(tree.getChild(0).toString(),
                                compareTo);
                    } else {
                        return createComparison(tree, Type.NotEqual);
                    }
                case "IN":
                    return new WhereInOperator(tree);
                default:
                    return null;
            }
	}
}
