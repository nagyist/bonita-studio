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
package org.bonitasoft.studio.parameters.expression;

import java.util.HashSet;
import java.util.Set;

import org.bonitasoft.bpm.model.util.ExpressionConstants;
import org.bonitasoft.studio.common.emf.tools.ExpressionHelper;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.expression.editor.provider.IExpressionEditor;
import org.bonitasoft.studio.expression.editor.provider.IExpressionProvider;
import org.bonitasoft.bpm.model.expression.Expression;
import org.bonitasoft.bpm.model.parameter.Parameter;
import org.bonitasoft.bpm.model.process.AbstractProcess;
import org.bonitasoft.studio.parameters.i18n.Messages;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.pics.PicsConstants;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.graphics.Image;

/**
 * @author Romain Bioteau
 *
 */
public class ParameterExpressionProvider implements IExpressionProvider {

    @Override
    public Set<Expression> getExpressions(final EObject context) {
        final Set<Expression> result = new HashSet<>();
        if (context instanceof EObject) {
            final AbstractProcess process = ModelHelper.getParentProcess(context);
            if (process != null) {
                for (final Parameter p : process.getParameters()) {
                    result.add(ExpressionHelper.createParameterExpression(p));
                }
            }
        }
        return result;
    }

    @Override
    public String getExpressionType() {
        return ExpressionConstants.PARAMETER_TYPE;
    }

    @Override
    public Image getIcon(final Expression expression) {
        return Pics.getImage(PicsConstants.parameter);
    }

    @Override
    public Image getTypeIcon() {
        return Pics.getImage(PicsConstants.parameter);
    }

    @Override
    public String getProposalLabel(final Expression expression) {
        return expression.getName();
    }

    @Override
    public boolean isRelevantFor(final EObject context) {
        return true;
    }

    @Override
    public String getTypeLabel() {
        return Messages.parameters;
    }

    @Override
    public IExpressionEditor getExpressionEditor(final Expression expression, final EObject context) {
        return null;
    }

}
