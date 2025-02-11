/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.assertions;

import static java.lang.String.format;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.eclipse.core.runtime.IStatus;

/**
 * {@link IStatus} specific assertions - Generated by CustomAssertionGenerator.
 */
public class StatusAssert extends AbstractAssert<StatusAssert, IStatus> {

    /**
     * Creates a new </code>{@link StatusAssert}</code> to make assertions on actual IStatus.
     *
     * @param actual the IStatus we want to make assertions on.
     */
    public StatusAssert(final IStatus actual) {
        super(actual, StatusAssert.class);
    }

    /**
     * An entry point for IStatusAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one's can write directly : <code>assertThat(myIStatus)</code> and get specific assertion with
     * code completion.
     *
     * @param actual the IStatus we want to make assertions on.
     * @return a new </code>{@link StatusAssert}</code>
     */
    public static StatusAssert assertThat(final IStatus actual) {
        return new StatusAssert(actual);
    }

    /**
     * Verifies that the actual IStatus's children contains the given IStatus elements.
     *
     * @param children the given elements that should be contained in actual IStatus's children.
     * @return this assertion object.
     * @throws AssertionError if the actual IStatus's children does not contain all given IStatus elements.
     */
    public StatusAssert hasChildren(final IStatus... children) {
        // check that actual IStatus we want to make assertions on is not null.
        isNotNull();

        // check that given IStatus varargs is not null.
        if (children == null) {
            throw new AssertionError("Expecting children parameter not to be null.");
        }

        // check with standard error message (see commented below to set your own message).
        Assertions.assertThat(actual.getChildren()).contains(children);

        return this;
    }

    /**
     * Verifies that the actual IStatus has no children.
     *
     * @return this assertion object.
     * @throws AssertionError if the actual IStatus's children is not empty.
     */
    public StatusAssert hasNoChildren() {
        // check that actual IStatus we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("\nExpected :\n  <%s>\nnot to have children but had :\n  <%s>", actual,
                java.util.Arrays.toString(actual.getChildren()));

        // check
        if (actual.getChildren().length > 0) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IStatus's code is equal to the given one.
     *
     * @param code the given code to compare the actual IStatus's code to.
     * @return this assertion object.
     * @throws AssertionError - if the actual IStatus's code is not equal to the given one.
     */
    public StatusAssert hasCode(final int code) {
        // check that actual IStatus we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("\nExpected <%s> code to be:\n  <%s>\n but was:\n  <%s>", actual, code,
                actual.getCode());

        // check
        if (actual.getCode() != code) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IStatus's exception is equal to the given one.
     *
     * @param exception the given exception to compare the actual IStatus's exception to.
     * @return this assertion object.
     * @throws AssertionError - if the actual IStatus's exception is not equal to the given one.
     */
    public StatusAssert hasException(final Throwable exception) {
        // check that actual IStatus we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("\nExpected <%s> exception to be:\n  <%s>\n but was:\n  <%s>", actual, exception,
                actual.getException());

        // check
        if (!actual.getException().equals(exception)) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IStatus's message is equal to the given one.
     *
     * @param message the given message to compare the actual IStatus's message to.
     * @return this assertion object.
     * @throws AssertionError - if the actual IStatus's message is not equal to the given one.
     */
    public StatusAssert hasMessage(final String message) {
        // check that actual IStatus we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("\nExpected <%s> message to be:\n  <%s>\n but was:\n  <%s>", actual, message,
                actual.getMessage());

        // check
        if (!actual.getMessage().equals(message)) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IStatus is multiStatus.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual IStatus is not multiStatus.
     */
    public StatusAssert isMultiStatus() {
        // check that actual IStatus we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("Expected actual IStatus to be multiStatus but was not.", actual);

        // check
        if (!actual.isMultiStatus()) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IStatus is not multiStatus.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual IStatus is multiStatus.
     */
    public StatusAssert isNotMultiStatus() {
        // check that actual IStatus we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("Expected actual IStatus not to be multiStatus but was.", actual);

        // check
        if (actual.isMultiStatus()) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IStatus is oK.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual IStatus is not oK.
     */
    public StatusAssert isOK() {
        // check that actual IStatus we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("Expected actual IStatus to be oK but was not: %s", actual);

        // check
        if (!actual.isOK()) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IStatus is not oK.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual IStatus is oK.
     */
    public StatusAssert isNotOK() {
        // check that actual IStatus we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("Expected actual IStatus not to be oK but was not.", actual);

        // check
        if (actual.isOK()) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    public StatusAssert isInfo() {
        isNotNull();
        final String errorMessage = format("Expected actual IStatus to be Info but was not.", actual);
        if (!Objects.equals(actual.getSeverity(), IStatus.INFO)) {
            throw new AssertionError(errorMessage);
        }
        return this;
    }

    public StatusAssert isWarning() {
        isNotNull();
        final String errorMessage = format("Expected actual IStatus to be Warning but was not.", actual);
        if (!Objects.equals(actual.getSeverity(), IStatus.WARNING)) {
            throw new AssertionError(errorMessage);
        }
        return this;
    }

    public StatusAssert isError() {
        isNotNull();
        final String errorMessage = format("Expected actual IStatus to be Error but was not.", actual);
        if (!Objects.equals(actual.getSeverity(), IStatus.ERROR)) {
            throw new AssertionError(errorMessage);
        }
        return this;
    }

    /**
     * Verifies that the actual IStatus's plugin is equal to the given one.
     *
     * @param plugin the given plugin to compare the actual IStatus's plugin to.
     * @return this assertion object.
     * @throws AssertionError - if the actual IStatus's plugin is not equal to the given one.
     */
    public StatusAssert hasPlugin(final String plugin) {
        // check that actual IStatus we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("\nExpected <%s> plugin to be:\n  <%s>\n but was:\n  <%s>", actual, plugin,
                actual.getPlugin());

        // check
        if (!actual.getPlugin().equals(plugin)) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IStatus's severity is equal to the given one.
     *
     * @param severity the given severity to compare the actual IStatus's severity to.
     * @return this assertion object.
     * @throws AssertionError - if the actual IStatus's severity is not equal to the given one.
     */
    public StatusAssert hasSeverity(final int severity) {
        // check that actual IStatus we want to make assertions on is not null.
        isNotNull();

        // we overrides the default error message with a more explicit one
        final String errorMessage = format("\nExpected <%s> severity to be:\n  <%s>\n but was:\n  <%s>", actual, severity,
                actual.getSeverity());

        // check
        if (actual.getSeverity() != severity) {
            throw new AssertionError(errorMessage);
        }

        // return the current assertion for method chaining
        return this;
    }

}
