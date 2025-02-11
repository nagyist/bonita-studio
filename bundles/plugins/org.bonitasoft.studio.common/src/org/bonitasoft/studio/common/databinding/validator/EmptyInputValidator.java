/**
 * Copyright (C) 2012 Bonitasoft S.A.
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
package org.bonitasoft.studio.common.databinding.validator;

import static com.google.common.base.Strings.isNullOrEmpty;

import org.bonitasoft.studio.common.Messages;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;

public class EmptyInputValidator implements IValidator<String> {

    private final String inputName;

    private final boolean raiseErrorForBlank;

    public EmptyInputValidator(final String inputName) {
        this(inputName, false);
    }

    public EmptyInputValidator(final String inputName, final boolean raiseErrorForBlank) {
        this.inputName = inputName;
        this.raiseErrorForBlank = raiseErrorForBlank;
    }

    @Override
    public IStatus validate(final String input) {
        boolean isInvalid = isNullOrEmpty(input) || this.raiseErrorForBlank && StringUtils.isBlank(input);
        return isInvalid ? ValidationStatus.error(NLS.bind(Messages.emptyField, inputName))
                : ValidationStatus.ok();
    }

}
