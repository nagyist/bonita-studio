/**
 * Copyright (C) 2020 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.connector.wizard.sforce.pages;

import org.bonitasoft.studio.connector.model.definition.wizard.PageComponentSwitch;
import org.bonitasoft.studio.connector.wizard.sforce.componentSwitch.SForceCreateObjectComponentSwitch;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Maxence Raoux
 * 
 */
public class SForceCreateObjectWizardPage extends AbstractSforceWizardPage {

	@Override
	protected PageComponentSwitch getPageComponentSwitch(
			EMFDataBindingContext context, Composite pageComposite) {
		return new SForceCreateObjectComponentSwitch(getContainer(),
				pageComposite, getElementContainer(), getDefinition(),
				getConfiguration(), context, 
				getExpressionTypeFilter());
	}
}
