package org.xmind.core.internal.xpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Evaluator {

    private static final List<Object> EMPTY_SEQUENCE = Collections.emptyList();

    private static final String AXIS_ATTRIBUTE = "attribute"; //$NON-NLS-1$
    private static final String AXIS_CHILD = "child"; //$NON-NLS-1$
    private static final String AXIS_SELF = "self"; //$NON-NLS-1$
    private static final String AXIS_PARENT = "parent"; //$NON-NLS-1$

    private static final String KIND_TEXT = "text"; //$NON-NLS-1$
    private static final String KIND_NODE = "node"; //$NON-NLS-1$
    private static final Set<String> KINDS = new HashSet<String>(
            Arrays.asList(KIND_TEXT, KIND_NODE));

    private static final String TOKEN_SINGLE_SLASH = "/"; //$NON-NLS-1$
    private static final String TOKEN_PREDICATE_START = "["; //$NON-NLS-1$
    private static final String TOKEN_PREDICATE_END = "]"; //$NON-NLS-1$
    private static final String TOKEN_SELF = "."; //$NON-NLS-1$
    private static final String TOKEN_PARENT = ".."; //$NON-NLS-1$
    private static final String TOKEN_PAREN_START = "("; //$NON-NLS-1$
    private static final String TOKEN_PAREN_END = ")"; //$NON-NLS-1$
    private static final String TOKEN_ARGUMENT_SEPARATOR = ","; //$NON-NLS-1$
    private static final String TOKEN_AXIS_SEPARATOR = "::"; //$NON-NLS-1$
    private static final String TOKEN_AXIS_ATTRIBUTE = "@"; //$NON-NLS-1$

    private static final Pattern RE_LEXER = Pattern.compile(
            "\\$?(?:(?![0-9-])(?:[\\w-]+|\\*):)?(?![0-9-])(?:[\\w-]+|\\*)|\\(:|:\\)|\\/\\/|\\.\\.|::|\\d+(?:\\.\\d*)?(?:[eE][+-]?\\d+)?|\\.\\d+(?:[eE][+-]?\\d+)?|\"[^\"]*(?:\"\"[^\"]*)*\"|'[^']*(?:''[^']*)*'|<<|>>|[!<>]=|(?![0-9-])[\\w-]+:\\*|\\s+|."); //$NON-NLS-1$
    private static final Pattern RE_SPACE = Pattern.compile("^\\s+$"); //$NON-NLS-1$
    private static final Pattern RE_NAME = Pattern.compile(
            "^(?:(?![0-9-])([\\w-]+|\\*)\\:)?(?![0-9-])([\\w-]+|\\*)$"); //$NON-NLS-1$
    private static final Pattern RE_INTEGER = Pattern.compile("^\\d+$"); //$NON-NLS-1$
    private static final Pattern RE_DOUBLE = Pattern.compile("^\\d*\\.\\d+$"); //$NON-NLS-1$
    private static final Pattern RE_STRING = Pattern
            .compile("^'([^']*(?:''[^']*)*)'|\"([^\"]*(?:\"\"[^\"]*)*)\"$"); //$NON-NLS-1$

    private static class EvaluationContext {

        public Evaluator staticContext;

        public Object item;

        public int position;

        public int size;

        public EvaluationContext(Evaluator staticContext, Object item) {
            this.staticContext = staticContext;
            this.item = item;
            this.position = 0;
            this.size = 0;
        }

    }

    private static abstract class Expression {

        private List<Expression> arguments = new ArrayList<Expression>();

        protected Expression() {
        }

        protected void addArgument(Expression argument) {
            this.arguments.add(argument);
        }

        protected Expression getArgument(int index, Expression defaultArg) {
            return index < arguments.size() ? arguments.get(index) : defaultArg;
        }

        protected List<Expression> getArguments() {
            return this.arguments;
        }

        public abstract List<Object> evaluate(EvaluationContext context);

    }

    private static final Expression NULL = new Expression() {

        @Override
        public List<Object> evaluate(EvaluationContext context) {
            return EMPTY_SEQUENCE;
        }

        @Override
        public String toString() {
            return "null"; //$NON-NLS-1$
        }

    };

    private static class Literal extends Expression {

        private Object value;

        public Literal(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public List<Object> evaluate(EvaluationContext context) {
            return Arrays.asList(value);
        }

        @Override
        public String toString() {
            return value == null ? "null" : value.toString(); //$NON-NLS-1$
        }

    }

    private static class PathExpression extends Expression {

        @Override
        public List<Object> evaluate(EvaluationContext context) {
            Object oldItem = context.item;

            List<Object> sequence = Arrays.asList(oldItem);
            List<Object> inputSequence, results;

            for (Expression arg : getArguments()) {
                inputSequence = sequence;
                sequence = new ArrayList<Object>();
                for (Object item : inputSequence) {
                    context.item = item;
                    results = arg.evaluate(context);
                    for (Object obj : results) {
                        if (!sequence.contains(obj)) {
                            sequence.add(obj);
                        }
                    }
                }
            }

            context.item = oldItem;

            return sequence;
        }

    }

    private static class AxisExpression extends Expression {

        public String axis;

        public String nameToTest;

        public String kindToTest;

        public AxisExpression(String axis, String nameToTest,
                String kindToTest) {
            this.axis = axis;
            this.nameToTest = nameToTest;
            this.kindToTest = kindToTest;
        }

        @Override
        public List<Object> evaluate(EvaluationContext context) {
            List<Object> sequence = new ArrayList<Object>();

            IAxisProvider axisProvider = context.staticContext
                    .getAxisProvider();
            if (axisProvider == null)
                return sequence;

            Object item = context.item;
            if (AXIS_ATTRIBUTE.equals(axis)) {
                if (nameToTest != null)
                    sequence.add(axisProvider.getAttribute(item, nameToTest));
            } else if (AXIS_CHILD.equals(axis)) {
                if (nameToTest != null)
                    sequence.addAll(
                            axisProvider.getChildNodes(item, nameToTest));
            } else if (AXIS_PARENT.equals(axis)) {
                if (KIND_NODE.equals(kindToTest)) {
                    sequence.add(axisProvider.getParentNode(item));
                }
            } else if (AXIS_SELF.equals(axis)) {
                if (KIND_TEXT.equals(kindToTest)) {
                    sequence.add(axisProvider.getTextContent(item));
                } else if (KIND_NODE.equals(kindToTest)) {
                    sequence.add(item);
                }
            }

            return sequence;
        }

        @Override
        public String toString() {
            return String.format("%s::%s", axis, //$NON-NLS-1$
                    nameToTest != null ? nameToTest : kindToTest + "()"); //$NON-NLS-1$
        }

    }

    private static class FilterExpression extends Expression {

        private Expression primary;

        public FilterExpression(Expression primary) {
            this.primary = primary;
        }

        @Override
        public List<Object> evaluate(EvaluationContext context) {
            List<Object> sequence = primary.evaluate(context);
            sequence = applyPredicates(context, sequence);
            return sequence;
        }

        private List<Object> applyPredicates(EvaluationContext context,
                List<Object> sequence) {
            if (sequence.isEmpty() || getArguments().isEmpty())
                return sequence;

            Object oldItem = context.item;
            int oldPosition = context.position;
            int oldSize = context.size;

            List<Object> inputSequence, results;
            int sequenceSize;

            for (Expression predicate : getArguments()) {
                inputSequence = sequence;
                sequenceSize = inputSequence.size();
                sequence = new ArrayList<Object>();

                if (predicate instanceof Literal && ((Literal) predicate)
                        .getValue() instanceof Integer) {
                    int targetIndex = ((Integer) ((Literal) predicate)
                            .getValue()).intValue() - 1;
                    if (targetIndex >= 0 && targetIndex < sequenceSize) {
                        sequence.add(inputSequence.get(targetIndex));
                    }
                } else {
                    for (int index = 0; index < sequenceSize; index++) {
                        Object item = inputSequence.get(index);
                        context.item = item;
                        context.position = index + 1;
                        context.size = sequenceSize;
                        results = predicate.evaluate(context);
                        if (test(results, context, index + 1, predicate)) {
                            sequence.add(item);
                        }
                    }
                }
            }

            context.item = oldItem;
            context.position = oldPosition;
            context.size = oldSize;

            return sequence;
        }

        private boolean test(List<Object> conditions, EvaluationContext context,
                int position, Expression predicate) {
            if (conditions.size() != 1)
                return false;

            Object condition = conditions.get(0);
            if (condition instanceof Boolean)
                return ((Boolean) condition).booleanValue();
            if (condition instanceof String)
                return !"".equals(condition); //$NON-NLS-1$
            if (condition instanceof Integer)
                return ((Integer) condition).intValue() != 0;
            if (condition instanceof Double)
                return ((Double) condition).doubleValue() != 0;

            return condition != null;
        }

    }

    private static class FunctionArgument {
        private List<Object> sequence;

        public FunctionArgument(List<Object> sequence) {
            this.sequence = sequence;
        }

        public Object anyItem() {
            return anyItem(null);
        }

        public Object anyItem(Object defaultItem) {
            return sequence.isEmpty() ? defaultItem : sequence.get(0);
        }

        public List<Object> sequence() {
            return sequence;
        }

    }

    private static final FunctionArgument NULL_ARGUMENT = new FunctionArgument(
            EMPTY_SEQUENCE);

    private static class FunctionArgumentList {
        private List<FunctionArgument> arguments;

        public FunctionArgumentList() {
            this.arguments = new ArrayList<FunctionArgument>();
        }

        public void add(FunctionArgument arg) {
            arguments.add(arg);
        }

        public FunctionArgument argumentAt(int index) {
            return index >= 0 && index < arguments.size() ? arguments.get(index)
                    : NULL_ARGUMENT;
        }

        public List<Object> sequenceAt(int index) {
            return argumentAt(index).sequence();
        }

        public Object itemAt(int index) {
            return argumentAt(index).anyItem();
        }

    }

    private static interface Function {

        Object call(EvaluationContext context, FunctionArgumentList args);

    }

    private static Map<String, Function> FUNCTIONS = new HashMap<String, Function>();

    static {
        FUNCTIONS.put("true", new Function() { //$NON-NLS-1$
            public Object call(EvaluationContext context,
                    FunctionArgumentList args) {
                return Boolean.TRUE;
            }
        });

        FUNCTIONS.put("false", new Function() { //$NON-NLS-1$
            public Object call(EvaluationContext context,
                    FunctionArgumentList args) {
                return Boolean.FALSE;
            }
        });

        FUNCTIONS.put("position", new Function() { //$NON-NLS-1$
            public Object call(EvaluationContext context,
                    FunctionArgumentList args) {
                return Integer.valueOf(context.position);
            }
        });

        FUNCTIONS.put("count", new Function() { //$NON-NLS-1$
            public Object call(EvaluationContext context,
                    FunctionArgumentList args) {
                return Integer.valueOf(args.sequenceAt(0).size());
            }
        });

        FUNCTIONS.put("matches", new Function() { //$NON-NLS-1$
            public Object call(EvaluationContext context,
                    FunctionArgumentList args) {
                Object text = args.itemAt(0);
                Object regex = args.itemAt(1);
                if (text == null || !(text instanceof String) || regex == null
                        || !(regex instanceof String))
                    return Boolean.FALSE;
                return Boolean.valueOf(
                        Pattern.matches((String) regex, (String) text));
            }
        });

        FUNCTIONS.put("eq", new Function() { //$NON-NLS-1$
            public Object call(EvaluationContext context,
                    FunctionArgumentList args) {
                Object op1 = args.itemAt(0);
                Object op2 = args.itemAt(1);
                return op1 == op2 || (op1 != null && op1.equals(op2));
            }
        });

        FUNCTIONS.put("ne", new Function() { //$NON-NLS-1$
            public Object call(EvaluationContext context,
                    FunctionArgumentList args) {
                Object op1 = args.itemAt(0);
                Object op2 = args.itemAt(1);
                return op1 != op2 && (op1 == null || !op1.equals(op2));
            }
        });

        FUNCTIONS.put("lt", new Function() { //$NON-NLS-1$
            public Object call(EvaluationContext context,
                    FunctionArgumentList args) {
                Object op1 = args.itemAt(0);
                Object op2 = args.itemAt(1);
                if ((!(op1 instanceof Integer) && !(op1 instanceof Double))
                        || (!(op2 instanceof Integer)
                                && !(op2 instanceof Double)))
                    return false;
                Number n1 = (Number) op1;
                Number n2 = (Number) op2;
                if (op1 instanceof Double || op2 instanceof Double) {
                    return n1.doubleValue() < n2.doubleValue();
                }
                return n1.intValue() < n2.intValue();
            }
        });

        FUNCTIONS.put("gt", new Function() { //$NON-NLS-1$
            public Object call(EvaluationContext context,
                    FunctionArgumentList args) {
                Object op1 = args.itemAt(0);
                Object op2 = args.itemAt(1);
                if ((!(op1 instanceof Integer) && !(op1 instanceof Double))
                        || (!(op2 instanceof Integer)
                                && !(op2 instanceof Double)))
                    return false;
                Number n1 = (Number) op1;
                Number n2 = (Number) op2;
                if (op1 instanceof Double || op2 instanceof Double) {
                    return n1.doubleValue() > n2.doubleValue();
                }
                return n1.intValue() > n2.intValue();
            }
        });

        FUNCTIONS.put("lte", new Function() { //$NON-NLS-1$
            public Object call(EvaluationContext context,
                    FunctionArgumentList args) {
                Object op1 = args.itemAt(0);
                Object op2 = args.itemAt(1);
                if ((!(op1 instanceof Integer) && !(op1 instanceof Double))
                        || (!(op2 instanceof Integer)
                                && !(op2 instanceof Double)))
                    return false;
                Number n1 = (Number) op1;
                Number n2 = (Number) op2;
                if (op1 instanceof Double || op2 instanceof Double) {
                    return n1.doubleValue() <= n2.doubleValue();
                }
                return n1.intValue() <= n2.intValue();
            }
        });

        FUNCTIONS.put("gte", new Function() { //$NON-NLS-1$
            public Object call(EvaluationContext context,
                    FunctionArgumentList args) {
                Object op1 = args.itemAt(0);
                Object op2 = args.itemAt(1);
                if ((!(op1 instanceof Integer) && !(op1 instanceof Double))
                        || (!(op2 instanceof Integer)
                                && !(op2 instanceof Double)))
                    return false;
                Number n1 = (Number) op1;
                Number n2 = (Number) op2;
                if (op1 instanceof Double || op2 instanceof Double) {
                    return n1.doubleValue() >= n2.doubleValue();
                }
                return n1.intValue() >= n2.intValue();
            }
        });

    }

    private static final Map<String, String> OPERATORS = new HashMap<String, String>();

    static {
        OPERATORS.put("=", "eq"); //$NON-NLS-1$ //$NON-NLS-2$
        OPERATORS.put("!=", "ne"); //$NON-NLS-1$ //$NON-NLS-2$
        OPERATORS.put("<", "lt"); //$NON-NLS-1$ //$NON-NLS-2$
        OPERATORS.put(">", "gt"); //$NON-NLS-1$ //$NON-NLS-2$
        OPERATORS.put("<=", "lte"); //$NON-NLS-1$ //$NON-NLS-2$
        OPERATORS.put(">=", "gte"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static class FunctionCall extends Expression {

        private String name;

        public FunctionCall(String name) {
            this.name = name;
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<Object> evaluate(EvaluationContext context) {
            Function f = FUNCTIONS.get(name);
            if (f == null)
                return EMPTY_SEQUENCE;

            FunctionArgumentList args = new FunctionArgumentList();
            for (Expression arg : getArguments()) {
                List<Object> sequence = arg.evaluate(context);
                args.add(new FunctionArgument(sequence));
            }

            Object result = f.call(context, args);
            if (result instanceof List)
                return (List) result;
            return Arrays.asList(result);
        }

    }

    private String expressionText;

    private Expression expression;

    private IAxisProvider axisProvider;

    public Evaluator(String expression) {
        this(expression, null);
    }

    public Evaluator(String expression, IAxisProvider axisProvider) {
        this.expressionText = expression;
        this.axisProvider = axisProvider;
        this.expression = null;
    }

    public IAxisProvider getAxisProvider() {
        return axisProvider;
    }

    public void setAxisProvider(IAxisProvider axisProvider) {
        this.axisProvider = axisProvider;
    }

    @Override
    public String toString() {
        return expression.toString();
    }

    public List<Object> evaluate(Object context) {
        if (expression == null)
            expression = new ExpressionParser(expressionText).parse();

        EvaluationContext ctx = new EvaluationContext(this, context);
        return expression.evaluate(ctx);
    }

    private static class ExpressionParser {

        private String[] tokens;

        private int tokenIndex;

        public ExpressionParser(String expression) {
            List<String> tokens = new ArrayList<String>();
            Matcher matcher = RE_LEXER.matcher(expression);
            while (matcher.find()) {
                String token = matcher.group();
                if (!RE_SPACE.matcher(token).matches()) {
                    tokens.add(token);
                }
            }
            this.tokens = tokens.toArray(new String[tokens.size()]);
            this.tokenIndex = 0;
        }

        public Expression parse() {
            return parseExpression();
        }

        private String token() {
            return token(0);
        }

        private String token(int offset) {
            int index = tokenIndex + offset;
            return index < tokens.length ? tokens[index] : null;
        }

        private boolean nextToken() {
            return nextToken(1);
        }

        private boolean nextToken(int offset) {
            tokenIndex += Math.max(offset, 1);
            return tokenIndex < tokens.length;
        }

        private boolean hasToken() {
            return tokenIndex < tokens.length;
        }

        private Expression parseExpression() {
            return parseSingleExpression();
        }

        private Expression parseSingleExpression() {
            return parseOrExpression();
        }

        private Expression parseOrExpression() {
            return parseAndExpression();
        }

        private Expression parseAndExpression() {
            return parseComparisonExpression();
        }

        private Expression parseComparisonExpression() {
            if (!hasToken())
                return NULL;

            Expression arg1 = null;
            if (hasToken()) {
                arg1 = parseRangeExpression();
            }
            if (!hasToken())
                return arg1;

            String funcName = OPERATORS.get(token());
            if (funcName == null)
                return arg1;

            nextToken();

            Expression arg2 = parseRangeExpression();

            Expression func = new FunctionCall(funcName);
            func.addArgument(arg1);
            func.addArgument(arg2);
            return func;
        }

        private Expression parseRangeExpression() {
            return parseAdditiveExpression();
        }

        private Expression parseAdditiveExpression() {
            return parseMultiplicativeExpression();
        }

        private Expression parseMultiplicativeExpression() {
            return parseUnionExpression();
        }

        private Expression parseUnionExpression() {
            return parseIntersectExceptExpression();
        }

        private Expression parseIntersectExceptExpression() {
            return parseInstanceofExpression();
        }

        private Expression parseInstanceofExpression() {
            return parseTreatExpression();
        }

        private Expression parseTreatExpression() {
            return parseCastableExpression();
        }

        private Expression parseCastableExpression() {
            return parseCastExpression();
        }

        private Expression parseCastExpression() {
            return parseUnaryExpression();
        }

        private Expression parseUnaryExpression() {
            return parseValueExpression();
        }

        private Expression parseValueExpression() {
            return parsePathExpression();
        }

        private Expression parsePathExpression() {
            if (!hasToken())
                return NULL;

            PathExpression path = new PathExpression();

            if (TOKEN_SINGLE_SLASH.equals(token()))
                nextToken();

            while (true) {
                Expression step = parseStepExpression();
                if (step == NULL)
                    break;
                path.addArgument(step);
                if (TOKEN_SINGLE_SLASH.equals(token()))
                    nextToken();
                else
                    break;
            }

            if (path.getArguments().size() == 1)
                return path.getArgument(0, NULL);

            return path;
        }

        private Expression parseStepExpression() {
            return parseFilterExpression();
        }

        private Expression parseFilterExpression() {
            if (!hasToken())
                return NULL;

            Expression exp = parsePrimaryExpression();
            if (exp == NULL)
                return NULL;

            Expression filter = new FilterExpression(exp);
            while (TOKEN_PREDICATE_START.equals(token())) {
                nextToken();
                Expression predicate = parseExpression();
                if (predicate != NULL) {
                    filter.addArgument(predicate);
                }
                if (TOKEN_PREDICATE_END.equals(token())) {
                    nextToken();
                }
            }
            if (filter.getArguments().isEmpty())
                return exp;
            return filter;
        }

        private Expression parsePrimaryExpression() {
            if (!hasToken())
                return NULL;

            String token = token();
            if (TOKEN_PARENT.equals(token)) {
                nextToken();
                return new AxisExpression(AXIS_PARENT, null, KIND_NODE);
            }
            if (TOKEN_SELF.equals(token)) {
                nextToken();
                return new AxisExpression(AXIS_SELF, null, KIND_NODE);
            }
            Expression exp;

            exp = parseParenthesizedExpression();
            if (exp != NULL)
                return exp;

            exp = parseFunctionCall();
            if (exp != NULL)
                return exp;

            exp = parseVarRef();
            if (exp != NULL)
                return exp;

            exp = parseLiteral();
            if (exp != NULL)
                return exp;

            exp = parseAxisExpression();
            if (exp != NULL)
                return exp;

            return NULL;
        }

        private Expression parseParenthesizedExpression() {
            Expression exp = NULL;
            if (TOKEN_PAREN_START.equals(token())) {
                nextToken();
                if (!TOKEN_PAREN_END.equals(token())) {
                    exp = parseExpression();
                }
                if (TOKEN_PAREN_END.equals(token()))
                    nextToken();
            }
            return exp;
        }

        private Expression parseFunctionCall() {
            if (!hasToken())
                return NULL;

            Matcher nameMatch = RE_NAME.matcher(token());
            if (nameMatch.matches() && TOKEN_PAREN_START.equals(token(1))) {
                String namespace = nameMatch.group(1);
                String funcName = nameMatch.group(2);

                if (namespace == null && KINDS.contains(funcName))
                    return parseAxisExpression();

                FunctionCall functionCall = new FunctionCall(funcName);
                nextToken(2);

                if (!TOKEN_PAREN_END.equals(token())) {
                    do {
                        Expression exp = parseSingleExpression();
                        functionCall.addArgument(exp);
                    } while (TOKEN_ARGUMENT_SEPARATOR.equals(token())
                            && nextToken());
                }

                if (TOKEN_PAREN_END.equals(token())) {
                    nextToken();
                }
                return functionCall;
            }
            return NULL;
        }

        private Expression parseVarRef() {
            return NULL;
        }

        private Expression parseLiteral() {
            if (!hasToken())
                return NULL;

            String token = token();
            if (RE_INTEGER.matcher(token).matches()) {
                nextToken();
                return new Literal(
                        Integer.valueOf(Integer.parseInt(token, 10)));
            } else if (RE_DOUBLE.matcher(token).matches()) {
                nextToken();
                return new Literal(Double.valueOf(Double.parseDouble(token)));
            } else {
                Matcher m = RE_STRING.matcher(token);
                if (m.matches()) {
                    nextToken();
                    String string;
                    if (m.group(1) != null) {
                        string = m.group(1).replace("''", "'"); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (m.group(2) != null) {
                        string = m.group(2).replace("\"\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
                    } else {
                        string = ""; //$NON-NLS-1$
                    }
                    return new Literal(string);
                }

            }
            return NULL;
        }

        private Expression parseAxisExpression() {
            if (!hasToken())
                return NULL;

            String axis = token();
            if (TOKEN_AXIS_SEPARATOR.equals(token(1))) {
                nextToken(2);
                String toTest = token();
                if (toTest != null && RE_NAME.matcher(toTest).matches()) {
                    nextToken();
                    if (TOKEN_PAREN_START.equals(token(1))
                            && TOKEN_PAREN_END.equals(token(2))) {
                        nextToken(2);
                        return new AxisExpression(axis, null, toTest);
                    } else {
                        return new AxisExpression(axis, toTest, null);
                    }
                } else {
                    return new AxisExpression(axis, null, KIND_NODE);
                }
            } else if (TOKEN_AXIS_ATTRIBUTE.equals(axis)) {
                nextToken();
                String name = token();
                if (RE_NAME.matcher(name).matches()) {
                    nextToken();
                    return new AxisExpression(AXIS_ATTRIBUTE, name, null);
                }
            } else {
                String name = token();
                if (RE_NAME.matcher(name).matches()) {
                    nextToken();
                    return new AxisExpression(AXIS_CHILD, name, null);
                }
            }
            return NULL;
        }

    }

}
