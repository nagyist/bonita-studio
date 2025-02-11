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
package org.bonitasoft.studio.contract.ui.property.constraint.edit;

import static org.bonitasoft.studio.common.ui.jface.databinding.UpdateStrategyFactory.convertUpdateValueStrategy;

import org.bonitasoft.studio.common.ui.jface.databinding.CustomTextEMFObservableValueEditingSupport;
import org.bonitasoft.bpm.model.process.ProcessPackage;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.ui.forms.IMessageManager;

public class ErrorMessageObservableEditingSupport extends CustomTextEMFObservableValueEditingSupport {

    public ErrorMessageObservableEditingSupport(final ColumnViewer viewer,
            final IMessageManager messageManager,
            final DataBindingContext dbc) {
        super(viewer, ProcessPackage.Literals.CONTRACT_CONSTRAINT__ERROR_MESSAGE, messageManager, dbc);
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.studio.common.ui.jface.databinding.CustomTextEMFObservableValueEditingSupport#taregtToModelConvertStrategy()
     */
    @Override
    protected UpdateValueStrategy targetToModelConvertStrategy(final EObject element) {
        return convertUpdateValueStrategy().create();
    }

}
