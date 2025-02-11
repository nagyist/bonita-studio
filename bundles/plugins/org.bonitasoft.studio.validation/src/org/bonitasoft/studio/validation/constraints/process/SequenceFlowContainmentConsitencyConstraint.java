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
package org.bonitasoft.studio.validation.constraints.process;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;

import org.bonitasoft.bpm.model.process.Pool;
import org.bonitasoft.bpm.model.process.SequenceFlow;
import org.bonitasoft.bpm.model.process.SourceElement;
import org.bonitasoft.bpm.model.process.TargetElement;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.validation.constraints.AbstractLiveValidationMarkerConstraint;
import org.bonitasoft.studio.validation.i18n.Messages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.IValidationContext;

/**
 * @author Romain Bioteau
 */
public class SequenceFlowContainmentConsitencyConstraint extends AbstractLiveValidationMarkerConstraint {

    public static final String ID = "org.bonitasoft.studio.validation.constraints.sequenceflow.consistency";

    @Override
    protected String getConstraintId() {
        return ID;
    }

    @Override
    protected IStatus performBatchValidation(final IValidationContext ctx) {
        final EObject eObj = ctx.getTarget();
        checkArgument(eObj instanceof SequenceFlow);
        final SequenceFlow sequenceFlow = (SequenceFlow) eObj;

        final TargetElement targetElement = sequenceFlow.getTarget();
        if (targetElement == null) {
            return ctx.createFailureStatus(Messages.sequenceFlow_Without_Target_Element);
        }
        final SourceElement sourceElement = sequenceFlow.getSource();
        if (sourceElement == null) {
            return ctx.createFailureStatus(Messages.sequenceFlow_Without_Source_Element);
        }

        final Pool sequenceFlowContainer = ModelHelper.getParentPool(sequenceFlow);
        if (sequenceFlowContainer == null) {
            return ctx.createFailureStatus(Messages.sequenceFlow_Without_Container);
        }

        if (!Objects.equals(ModelHelper.getParentPool(sourceElement), sequenceFlowContainer)) {
            return ctx.createFailureStatus(Messages.sequenceFlow_And_SourceElement_Not_In_The_Same_Container);
        }
        if (!Objects.equals(ModelHelper.getParentPool(targetElement), sequenceFlowContainer)) {
            return ctx.createFailureStatus(Messages.sequenceFlow_And_TargetElement_Not_In_The_Same_Container);
        }

        return ctx.createSuccessStatus();
    }
}
