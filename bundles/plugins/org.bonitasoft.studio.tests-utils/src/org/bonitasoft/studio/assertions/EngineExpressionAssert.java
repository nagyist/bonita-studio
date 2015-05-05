package org.bonitasoft.studio.assertions;

import static java.lang.String.format;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.expression.Expression;

/**
 * {@link Expression} specific assertions - Generated by CustomAssertionGenerator.
 */
public class EngineExpressionAssert extends AbstractAssert<EngineExpressionAssert, Expression> {

    /**
     * Creates a new </code>{@link EngineExpressionAssert}</code> to make assertions on actual Expression.
     *
     * @param actual the Expression we want to make assertions on.
     */
    public EngineExpressionAssert(final Expression actual) {
        super(actual, EngineExpressionAssert.class);
    }

    /**
     * An entry point for ExpressionAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one's can write directly : <code>assertThat(myExpression)</code> and get specific assertion with code completion.
     *
     * @param actual the Expression we want to make assertions on.
     * @return a new </code>{@link EngineExpressionAssert}</code>
     */
    public static EngineExpressionAssert assertThat(final Expression actual) {
        return new EngineExpressionAssert(actual);
    }

    /**
     * Verifies that the actual Expression's content is equal to the given one.
     *
     * @param content the given content to compare the actual Expression's content to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Expression's content is not equal to the given one.
     */
    public EngineExpressionAssert hasContent(final String content) {
        // check that actual Expression we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("\nExpected <%s> content to be:\n  <%s>\n but was:\n  <%s>", actual, content, actual.getContent());

        // check
        if (!actual.getContent().equals(content)) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual Expression's interpreter is equal to the given one.
     *
     * @param interpreter the given interpreter to compare the actual Expression's interpreter to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Expression's interpreter is not equal to the given one.
     */
    public EngineExpressionAssert hasInterpreter(final String interpreter) {
        // check that actual Expression we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("\nExpected <%s> interpreter to be:\n  <%s>\n but was:\n  <%s>", actual, interpreter, actual.getInterpreter());

        // check
        if (!actual.getInterpreter().equals(interpreter)) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual Expression's name is equal to the given one.
     *
     * @param name the given name to compare the actual Expression's name to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Expression's name is not equal to the given one.
     */
    public EngineExpressionAssert hasName(final String name) {
        // check that actual Expression we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("\nExpected <%s> name to be:\n  <%s>\n but was:\n  <%s>", actual, name, actual.getName());

        // check
        if (!actual.getName().equals(name)) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual Expression's referencedElements contains the given EObject elements.
     *
     * @param referencedElements the given elements that should be contained in actual Expression's referencedElements.
     * @return this assertion object.
     * @throws AssertionError if the actual Expression's referencedElements does not contain all given EObject elements.
     */
    public EngineExpressionAssert hasReferencedElements(final Expression... dependencies) {
        // check that actual Expression we want to make assertions on is not null.
        isNotNull();

        // check that given EObject varargs is not null.
        if (dependencies == null) {
            throw new AssertionError("Expecting dependencies parameter not to be null.");
        }

        // check with standard error message (see commented below to set your own message).
        Assertions.assertThat(actual.getDependencies()).contains(dependencies);

        // uncomment the 4 lines below if you want to build your own error message :
        // WritableAssertionInfo assertionInfo = new WritableAssertionInfo();
        // String errorMessage = "my error message";
        // assertionInfo.overridingErrorMessage(errorMessage);
        // Iterables.instance().assertContains(assertionInfo, actual.getTeamMates(), teamMates);

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual Expression has no referencedElements.
     *
     * @return this assertion object.
     * @throws AssertionError if the actual Expression's referencedElements is not empty.
     */
    public EngineExpressionAssert hasNoDependencies() {
        // check that actual Expression we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("\nExpected :\n  <%s>\nnot to have dependencies but had :\n  <%s>", actual, actual.getDependencies());

        // check
        if (!actual.getDependencies().isEmpty()) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual Expression's returnType is equal to the given one.
     *
     * @param returnType the given returnType to compare the actual Expression's returnType to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Expression's returnType is not equal to the given one.
     */
    public EngineExpressionAssert hasReturnType(final String returnType) {
        // check that actual Expression we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("\nExpected <%s> returnType to be:\n  <%s>\n but was:\n  <%s>", actual, returnType, actual.getReturnType());

        // check
        if (!actual.getReturnType().equals(returnType)) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual Expression's type is equal to the given one.
     *
     * @param type the given type to compare the actual Expression's type to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Expression's type is not equal to the given one.
     */
    public EngineExpressionAssert hasExpressionType(final String type) {
        // check that actual Expression we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("\nExpected <%s> expression type to be:\n  <%s>\n but was:\n  <%s>", actual, type, actual.getExpressionType());

        // check
        if (!actual.getExpressionType().equals(type)) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

}
