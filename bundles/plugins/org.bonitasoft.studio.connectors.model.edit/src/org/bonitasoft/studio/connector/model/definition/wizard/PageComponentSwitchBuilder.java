/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.studio.connector.model.definition.wizard;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.common.jface.BonitaStudioFontRegistry;
import org.bonitasoft.studio.common.jface.databinding.validator.EmptyInputValidator;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.connector.model.definition.Array;
import org.bonitasoft.studio.connector.model.definition.Checkbox;
import org.bonitasoft.studio.connector.model.definition.ConnectorDefinition;
import org.bonitasoft.studio.connector.model.definition.Group;
import org.bonitasoft.studio.connector.model.definition.Input;
import org.bonitasoft.studio.connector.model.definition.List;
import org.bonitasoft.studio.connector.model.definition.Orientation;
import org.bonitasoft.studio.connector.model.definition.Password;
import org.bonitasoft.studio.connector.model.definition.RadioGroup;
import org.bonitasoft.studio.connector.model.definition.ScriptEditor;
import org.bonitasoft.studio.connector.model.definition.Select;
import org.bonitasoft.studio.connector.model.definition.Text;
import org.bonitasoft.studio.connector.model.definition.TextArea;
import org.bonitasoft.studio.connector.model.definition.WidgetComponent;
import org.bonitasoft.studio.connector.model.i18n.DefinitionResourceProvider;
import org.bonitasoft.studio.expression.editor.filter.AvailableExpressionTypeFilter;
import org.bonitasoft.studio.expression.editor.provider.IExpressionNatureProvider;
import org.bonitasoft.studio.expression.editor.viewer.CheckBoxExpressionViewer;
import org.bonitasoft.studio.expression.editor.viewer.ExpressionCollectionViewer;
import org.bonitasoft.studio.expression.editor.viewer.ExpressionViewer;
import org.bonitasoft.studio.expression.editor.viewer.GroovyOnlyExpressionViewer;
import org.bonitasoft.studio.expression.editor.viewer.IExpressionModeListener;
import org.bonitasoft.studio.expression.editor.viewer.PatternExpressionViewer;
import org.bonitasoft.studio.model.connectorconfiguration.ConnectorConfiguration;
import org.bonitasoft.studio.model.connectorconfiguration.ConnectorConfigurationFactory;
import org.bonitasoft.studio.model.connectorconfiguration.ConnectorConfigurationPackage;
import org.bonitasoft.studio.model.connectorconfiguration.ConnectorParameter;
import org.bonitasoft.studio.model.expression.AbstractExpression;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.bonitasoft.studio.model.expression.ExpressionPackage;
import org.bonitasoft.studio.model.expression.ListExpression;
import org.bonitasoft.studio.model.expression.TableExpression;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Section;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class PageComponentSwitchBuilder {

    protected final ConnectorDefinition definition;

    private final ConnectorConfiguration connectorConfiguration;

    protected DefinitionResourceProvider messageProvider;

    private boolean isPageFlowContext = false;

    protected EObject container;

    protected EMFDataBindingContext context;

    protected AvailableExpressionTypeFilter connectorExpressionContentTypeFilter;

    private static final int DEFAULT_WITH_VALUE = -1;

    private int labelWidth = DEFAULT_WITH_VALUE;

    public PageComponentSwitchBuilder(final EObject container, final ConnectorDefinition definition,
            final ConnectorConfiguration connectorConfiguration, final EMFDataBindingContext context, final DefinitionResourceProvider messageProvider,
            final AvailableExpressionTypeFilter connectorExpressionContentTypeFilter) {
        super();
        this.definition = definition;
        this.connectorConfiguration = connectorConfiguration;
        this.messageProvider = messageProvider;
        this.container = container;
        this.context = context;
        this.connectorExpressionContentTypeFilter = connectorExpressionContentTypeFilter;
    }

    public PageComponentSwitchBuilder(final EObject container, final ConnectorDefinition definition,
            final ConnectorConfiguration connectorConfiguration, final EMFDataBindingContext context, final DefinitionResourceProvider messageProvider,
            final AvailableExpressionTypeFilter connectorExpressionContentTypeFilter, final int labelWidth) {
        this(container, definition, connectorConfiguration, context, messageProvider, connectorExpressionContentTypeFilter);
        this.labelWidth = labelWidth;
    }

    public ExpressionViewer createTextControl(final Composite composite, final Text object, final IExpressionNatureProvider expressionProvider,
            final LabelProvider autoCompletionLabelProvider) {
        final Input input = getConnectorInput(object.getInputName());
        if (input != null) {
            final ConnectorParameter parameter = getConnectorParameter(object.getInputName(), object, input);
            if (parameter != null) {
                createFieldLabel(composite, SWT.CENTER, object.getId(), input.isMandatory());
                return buildExpressionViewer(composite, object, expressionProvider, input, parameter, autoCompletionLabelProvider);
            }
        } else {
            //Should we create a label to warn final user?
            BonitaStudioLog.log("WARNING: No input found with name " + object.getInputName());
        }
        return null;
    }

    private ExpressionViewer buildExpressionViewer(final Composite composite, final Text object, final IExpressionNatureProvider expressionProvider,
            final Input input, final ConnectorParameter parameter, final LabelProvider autoCompletionLabelProvider) {
        final ExpressionViewer viewer = new ExpressionViewer(composite, SWT.BORDER,
                ConnectorConfigurationPackage.Literals.CONNECTOR_PARAMETER__EXPRESSION);
        viewer.setIsPageFlowContext(isPageFlowContext);
        viewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        viewer.setContext(container);
        if (autoCompletionLabelProvider != null) {
            viewer.setAutocomplitionLabelProvider(autoCompletionLabelProvider);
        }
        handleExpressionProvider(expressionProvider, viewer);
        handleMandatory(object, input, viewer);
        handleDocumentsOption(object, viewer);
        viewer.setInput(parameter);
        handleDescription(object, viewer);
        context.bindValue(ViewersObservables.observeSingleSelection(viewer),
                EMFObservables.observeValue(parameter, ConnectorConfigurationPackage.Literals.CONNECTOR_PARAMETER__EXPRESSION));
        return viewer;
    }

    private void handleDescription(final Text object, final ExpressionViewer viewer) {
        final String desc = messageProvider.getFieldDescription(definition, object.getId());
        if (desc != null && !desc.isEmpty()) {
            viewer.setMessage(desc, IStatus.INFO);
        }
    }

    private void handleDocumentsOption(final Text object, final ExpressionViewer viewer) {
        if (object.isShowDocuments()) {
            final Set<String> contentTypes = new HashSet<String>(connectorExpressionContentTypeFilter.getContentTypes());
            contentTypes.add(ExpressionConstants.DOCUMENT_REF_TYPE);
            viewer.addFilter(new AvailableExpressionTypeFilter(contentTypes.toArray(new String[contentTypes.size()])));
        } else {
            viewer.addFilter(connectorExpressionContentTypeFilter);
        }
    }

    private void handleMandatory(final Text object, final Input input, final ExpressionViewer viewer) {
        if (input.isMandatory()) {
            viewer.setMandatoryField(getLabel(object.getId()), context);
        }
    }

    private void handleExpressionProvider(final IExpressionNatureProvider expressionProvider, final ExpressionViewer viewer) {
        if (expressionProvider != null) {
            viewer.setExpressionNatureProvider(expressionProvider);
        }
    }

    public ExpressionViewer createTextControl(final Composite composite, final Text object) {
        return createTextControl(composite, object, null, null);
    }

    protected Input getConnectorInput(final String inputName) {
        for (final Input input : definition.getInput()) {
            if (input.getName().equals(inputName)) {
                return input;
            }
        }
        return null;
    }

    public Label createFieldLabel(final Composite composite, final int verticalAlignment, final String id, final boolean isMandatory) {
        final Composite labelContainer = getParentCompositeForLabel(composite);
        final Label fieldLabel = new Label(labelContainer, SWT.WRAP);
        setText(id, isMandatory, fieldLabel);
        final GridDataFactory factory = GridDataFactory.fillDefaults().align(SWT.END, verticalAlignment);
        if(hasFixedSize()) {
            factory.grab(true, false);
        }
        fieldLabel.setLayoutData(factory.create());
        return fieldLabel;
    }

    private void setText(final String id, final boolean isMandatory, final Label fieldLabel) {
        if (id != null) {
            String label = getLabel(id);
            if (isMandatory) {
                label = label + " *";
            }
            fieldLabel.setText(label);
        }
    }

    private Composite getParentCompositeForLabel(final Composite currentComposite) {
        Composite labelContainer = currentComposite;
        //add a composite because align and hint methods seems incompatible. End align is not working if horizontal hint is defined.
        //by adding a new composite it's possible to set the hint property in the composite and then define the align in the label.
        if (hasFixedSize()) {
            labelContainer = new Composite(currentComposite, SWT.INHERIT_DEFAULT);
            labelContainer.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).create());
            labelContainer.setLayoutData(GridDataFactory.fillDefaults().hint(labelWidth, SWT.DEFAULT).create());
        }
        return labelContainer;
    }

    private boolean hasFixedSize() {
        return labelWidth != DEFAULT_WITH_VALUE;
    }

    protected String getLabel(final String inputName) {
        String label = messageProvider.getFieldLabel(definition, inputName);
        if (label == null) {
            label = "";
        }

        return label;
    }

    protected ConnectorParameter getConnectorParameter(final String inputName, final WidgetComponent object, final Input input) {
        for (final ConnectorParameter param : connectorConfiguration.getParameters()) {
            if (param.getKey().equals(inputName)) {
                if (param.getExpression() == null) {
                    param.setExpression(createExpression(object, input));
                } else {
                    if (param.getExpression() instanceof Expression) {
                        final Expression exp = (Expression) param.getExpression();
                        if (!input.getType().equals(exp.getReturnType())) {
                            exp.setReturnType(input.getType());
                        }
                    }

                }
                return param;
            }
        }

        final ConnectorParameter parameter = ConnectorConfigurationFactory.eINSTANCE.createConnectorParameter();
        parameter.setKey(inputName);
        parameter.setExpression(createExpression(object, input));
        connectorConfiguration.getParameters().add(parameter);

        return parameter;
    }

    protected AbstractExpression createExpression(final WidgetComponent widget, final Input input) {
        final String inputClassName = input.getType();
        if (widget instanceof Array) {
            final TableExpression expression = ExpressionFactory.eINSTANCE.createTableExpression();
            return expression;
        } else if (widget instanceof org.bonitasoft.studio.connector.model.definition.List) {
            final ListExpression expression = ExpressionFactory.eINSTANCE.createListExpression();
            return expression;
        } else {
            final Expression expression = ExpressionFactory.eINSTANCE.createExpression();
            expression.setReturnType(inputClassName);
            expression.setReturnTypeFixed(true);
            expression.setType(ExpressionConstants.CONSTANT_TYPE);
            expression.setName(input.getDefaultValue());
            expression.setContent(input.getDefaultValue());
            if (widget instanceof ScriptEditor) {
                expression.setType(ExpressionConstants.SCRIPT_TYPE);
                expression.setInterpreter(((ScriptEditor) widget).getInterpreter());
            } else if (widget instanceof TextArea) {
                expression.setType(ExpressionConstants.PATTERN_TYPE);
            }
            return expression;
        }
    }

    public CheckBoxExpressionViewer createCheckboxControl(final Composite composite, final Checkbox object) {
        final Input input = getConnectorInput(object.getInputName());
        final ConnectorParameter parameter = getConnectorParameter(object.getInputName(), object, input);
        if (parameter != null) {
            final Composite exprLabelComposite = new Composite(composite, SWT.INHERIT_DEFAULT);
            exprLabelComposite.setLayoutData(GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).create());
            exprLabelComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).spacing(0, 0).create());
            final Label fieldLabel = createFieldLabel(exprLabelComposite, SWT.CENTER, object.getId(), input.isMandatory());
            final Label emptyLine = new Label(exprLabelComposite, SWT.NONE);
            emptyLine.setText("");

            final CheckBoxExpressionViewer viewer = new CheckBoxExpressionViewer(composite, fieldLabel, SWT.BORDER,
                    ConnectorConfigurationPackage.Literals.CONNECTOR_PARAMETER__EXPRESSION);
            viewer.getCheckboxControl().setText(fieldLabel.getText());
            viewer.setIsPageFlowContext(isPageFlowContext);
            viewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
            viewer.setContext(container);

            if (input.isMandatory()) {
                viewer.setMandatoryField(getLabel(object.getId()), context);
            }
            viewer.addFilter(connectorExpressionContentTypeFilter);
            viewer.setInput(parameter);
            final String desc = messageProvider.getFieldDescription(definition, object.getId());
            if (desc != null && !desc.isEmpty()) {
                viewer.setMessage(desc, IStatus.INFO);
            }
            if (((Expression) parameter.getExpression()).getName() == null) {
                final Expression falseExp = (Expression) parameter.getExpression();
                falseExp.setContent(Boolean.FALSE.toString());
                falseExp.setName(Boolean.FALSE.toString());
                falseExp.setContent(Boolean.FALSE.toString());
                falseExp.setReturnType(Boolean.class.getName());
                falseExp.setType(ExpressionConstants.CONSTANT_TYPE);
            }
            viewer.setSelection(new StructuredSelection(parameter.getExpression()));
            fieldLabel.setVisible(!viewer.isCheckboxMode());
            return viewer;
        }
        return null;
    }

    public Composite createRadioGroupControl(final Composite composite, final RadioGroup object) {
        final Input input = getConnectorInput(object.getInputName());
        final ConnectorParameter parameter = getConnectorParameter(object.getInputName(), object, input);

        if (parameter != null) {
            Label label = null;
            if (object.getOrientation() == Orientation.HORIZONTAL) {
                label = createFieldLabel(composite, SWT.CENTER, object.getId(), input.isMandatory());
            } else {
                label = createFieldLabel(composite, SWT.TOP, object.getId(), input.isMandatory());
            }
            final String desc = messageProvider.getFieldDescription(definition, object.getId());
            if (desc != null && !desc.isEmpty()) {
                label.setToolTipText(desc);
            }

            final Composite radioCompoiste = new Composite(composite, SWT.NONE);
            radioCompoiste.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
            if (object.getOrientation() == Orientation.HORIZONTAL) {
                radioCompoiste.setLayout(GridLayoutFactory.fillDefaults().numColumns(object.getChoices().size()).create());
            } else {
                radioCompoiste.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).create());
            }
            final String defaultSelection = input.getDefaultValue();

            ((Expression) parameter.getExpression()).setType(ExpressionConstants.CONSTANT_TYPE);
            ((Expression) parameter.getExpression()).setReturnType(input.getType());
            final String content = ((Expression) parameter.getExpression()).getContent();
            for (final String choice : object.getChoices()) {
                final Button radioButton = new Button(radioCompoiste, SWT.RADIO);
                radioButton.setText(choice);
                radioButton.setLayoutData(GridDataFactory.fillDefaults().create());
                radioButton.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(final SelectionEvent e) {
                        if (radioButton.getSelection()) {
                            ((Expression) parameter.getExpression()).setContent(choice);
                            ((Expression) parameter.getExpression()).setName(choice);
                        }
                    }
                });
                if (content == null && defaultSelection != null && defaultSelection.equals(choice)) {
                    radioButton.setSelection(true);
                } else if (content != null && content.equals(choice)) {
                    radioButton.setSelection(true);
                }

            }
            if (defaultSelection == null) {
                final Button button = (Button) radioCompoiste.getChildren()[0];
                button.setSelection(true);
            }
            return radioCompoiste;
        }
        return null;

    }

    public PatternExpressionViewer createTextAreaControl(final Composite composite, final TextArea object) {
        final Input input = getConnectorInput(object.getInputName());
        final ConnectorParameter parameter = getConnectorParameter(object.getInputName(), object, input);

        if (parameter != null) {
            createFieldLabel(composite, SWT.TOP, object.getId(), input.isMandatory());

            final PatternExpressionViewer viewer = new PatternExpressionViewer(composite, SWT.NONE);
            viewer.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(400, SWT.DEFAULT).create());
            viewer.addFilter(connectorExpressionContentTypeFilter);

            final Expression exp = (Expression) parameter.getExpression();
            final String desc = messageProvider.getFieldDescription(definition, object.getId());
            if (desc != null && !desc.isEmpty()) {
                viewer.setHint(desc);
            }
            viewer.setContextInput(container);

            final UpdateValueStrategy startegy = new UpdateValueStrategy();
            if (input.isMandatory()) {
                startegy.setAfterConvertValidator(new EmptyInputValidator(getLabel(object.getId())));
            }
            viewer.setEMFBindingContext(context);
            if (input.isMandatory()) {
                viewer.setMandatoryField(getLabel(object.getId()));
            }

            viewer.setExpression(exp);
            return viewer;
        }
        return null;
    }

    public ExpressionViewer createScriptEditorControl(final Composite composite, final ScriptEditor object) {
        final Input input = getConnectorInput(object.getInputName());
        final ConnectorParameter parameter = getConnectorParameter(object.getInputName(), object, input);

        if (parameter != null) {
            createFieldLabel(composite, SWT.CENTER, object.getId(), input.isMandatory());
            final ExpressionViewer viewer = new GroovyOnlyExpressionViewer(composite, SWT.BORDER,
                    ConnectorConfigurationPackage.Literals.CONNECTOR_PARAMETER__EXPRESSION);
            viewer.setIsPageFlowContext(isPageFlowContext);
            viewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
            viewer.setContext(container);
            if (input.isMandatory()) {
                viewer.setMandatoryField(getLabel(object.getId()), context);
            }
            viewer.addFilter(connectorExpressionContentTypeFilter);
            viewer.setInput(parameter);
            final String desc = messageProvider.getFieldDescription(definition, object.getId());
            if (desc != null && !desc.isEmpty()) {
                viewer.setMessage(desc, IStatus.INFO);
            }
            context.bindValue(ViewersObservables.observeSingleSelection(viewer),
                    EMFObservables.observeValue(parameter, ConnectorConfigurationPackage.Literals.CONNECTOR_PARAMETER__EXPRESSION));
            return viewer;
        }
        return null;
    }

    public ExpressionCollectionViewer createArrayControl(final Composite composite, final Array object) {
        final Input input = getConnectorInput(object.getInputName());
        final ConnectorParameter parameter = getConnectorParameter(object.getInputName(), object, input);

        if (parameter != null) {
            final Label labelField = createFieldLabel(composite, SWT.TOP, object.getId(), input.isMandatory());
            final String desc = messageProvider.getFieldDescription(definition, object.getId());
            if (desc != null && !desc.isEmpty()) {
                createDescriptionDecorator(composite, labelField, desc);
            }

            final ExpressionCollectionViewer viewer = new ExpressionCollectionViewer(composite, 0, object.isFixedRows(), object.getCols().intValue(),
                    object.isFixedCols(), object.getColsCaption(), true, false);
            if (desc != null && !desc.isEmpty()) {
                viewer.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).indent(10, 0).create());
            } else {
                viewer.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
            }
            for (int i = 0; i < object.getCols().intValue(); i++) {
                viewer.addFilter(connectorExpressionContentTypeFilter);
            }
            if (input.isMandatory()) {
                viewer.setMandatoryField(getLabel(object.getId()), context);
            }
            viewer.setInput(parameter);
            viewer.setSelection(parameter.getExpression());
            viewer.addExpressionModeListener(new IExpressionModeListener() {

                @Override
                public void useTable() {
                    AbstractExpression expression = parameter.getExpression();
                    if (!(expression instanceof TableExpression)) {
                        expression = ExpressionFactory.eINSTANCE.createTableExpression();
                        parameter.setExpression(expression);
                    }
                    viewer.setSelection(expression);
                }

                @Override
                public void useSimpleExpression() {
                    AbstractExpression expression = parameter.getExpression();
                    if (!(expression instanceof Expression)) {
                        expression = ExpressionFactory.eINSTANCE.createExpression();
                        parameter.setExpression(expression);
                    }
                    viewer.setSelection(expression);
                }
            });

            return viewer;
        }
        return null;
    }

    public Section createGroupControl(final Composite composite, final Group object) {

        final String desc = messageProvider.getFieldDescription(definition, object.getId());
        int style = Section.NO_TITLE_FOCUS_BOX | Section.TWISTIE | Section.CLIENT_INDENT;
        if (desc != null && !desc.isEmpty()) {
            style = style | Section.DESCRIPTION;
        }
        final Section groupSection = new Section(composite, style);
        groupSection.setText(getLabel(object.getId()));
        groupSection.setFont(BonitaStudioFontRegistry.getBoldFont());
        if (desc != null && !desc.isEmpty()) {
            groupSection.setDescription(desc);
        }
        groupSection.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create());
        return groupSection;
    }

    public ExpressionCollectionViewer createListControl(final Composite composite, final List object) {
        final Input input = getConnectorInput(object.getInputName());
        final ConnectorParameter parameter = getConnectorParameter(object.getInputName(), object, input);

        if (parameter != null) {
            final Label labelField = createFieldLabel(composite, SWT.TOP, object.getId(), input.isMandatory());
            final String desc = messageProvider.getFieldDescription(definition, object.getId());
            if (desc != null && !desc.isEmpty()) {
                createDescriptionDecorator(composite, labelField, desc);
            }

            @SuppressWarnings("unchecked")
            final ExpressionCollectionViewer viewer = new ExpressionCollectionViewer(composite, 0, false, 1, true, Collections.EMPTY_LIST, true, false);
            if (desc != null && !desc.isEmpty()) {
                viewer.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).indent(10, 0).create());
            } else {
                viewer.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
            }

            if (object.isShowDocuments()) {
                final Set<String> contentTypes = new HashSet<String>(connectorExpressionContentTypeFilter.getContentTypes());
                contentTypes.add(ExpressionConstants.DOCUMENT_REF_TYPE);
                viewer.addFilter(new AvailableExpressionTypeFilter(contentTypes.toArray(new String[contentTypes.size()])));
            } else {
                viewer.addFilter(connectorExpressionContentTypeFilter);
            }
            if (input.isMandatory()) {
                viewer.setMandatoryField(getLabel(object.getId()), context);
            }
            viewer.setInput(parameter);
            viewer.setSelection(parameter.getExpression());
            viewer.addExpressionModeListener(new IExpressionModeListener() {

                @Override
                public void useTable() {
                    AbstractExpression expression = parameter.getExpression();
                    if (!(expression instanceof TableExpression)) {
                        expression = ExpressionFactory.eINSTANCE.createListExpression();
                        parameter.setExpression(expression);
                    }
                    viewer.setSelection(expression);
                }

                @Override
                public void useSimpleExpression() {
                    AbstractExpression expression = parameter.getExpression();
                    if (!(expression instanceof Expression)) {
                        expression = ExpressionFactory.eINSTANCE.createExpression();
                        parameter.setExpression(expression);
                    }
                    viewer.setSelection(expression);
                }
            });

            return viewer;
        }
        return null;

    }

    protected void createDescriptionDecorator(final Composite composite,
            final Label labelField, final String desc) {
        final ControlDecoration descriptionDecoration = new ControlDecoration(labelField, SWT.RIGHT, composite);
        descriptionDecoration.setMarginWidth(0);
        descriptionDecoration.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK));
        descriptionDecoration.setDescriptionText(desc);
        descriptionDecoration.setShowOnlyOnFocus(false);
        descriptionDecoration.setShowHover(true);
        descriptionDecoration.show();
    }

    public ExpressionViewer createPasswordControl(final Composite composite, final Password object) {
        final Input input = getConnectorInput(object.getInputName());
        final ConnectorParameter parameter = getConnectorParameter(object.getInputName(), object, input);

        if (parameter != null) {
            createFieldLabel(composite, SWT.CENTER, object.getId(), input.isMandatory());
            final ExpressionViewer viewer = new ExpressionViewer(composite, SWT.BORDER | SWT.PASSWORD,
                    ConnectorConfigurationPackage.Literals.CONNECTOR_PARAMETER__EXPRESSION);
            viewer.setIsPageFlowContext(isPageFlowContext);
            viewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
            viewer.setContext(container);
            if (input.isMandatory()) {
                viewer.setMandatoryField(getLabel(object.getId()), context);
            }
            viewer.addFilter(connectorExpressionContentTypeFilter);
            viewer.setInput(parameter);
            final String desc = messageProvider.getFieldDescription(definition, object.getId());
            if (desc != null && !desc.isEmpty()) {
                viewer.setMessage(desc, IStatus.INFO);
            }
            context.bindValue(ViewersObservables.observeSingleSelection(viewer),
                    EMFObservables.observeValue(parameter, ConnectorConfigurationPackage.Literals.CONNECTOR_PARAMETER__EXPRESSION));
            return viewer;
        }
        return null;
    }

    public Combo createSelectControl(final Composite composite, final Select object) {
        final Input input = getConnectorInput(object.getInputName());
        final ConnectorParameter parameter = getConnectorParameter(object.getInputName(), object, input);

        if (parameter != null) {
            createFieldLabel(composite, SWT.CENTER, object.getId(), input.isMandatory());
            final Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.BORDER);
            combo.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

            final AbstractExpression inputExpression = parameter.getExpression();
            for (final String item : object.getItems()) {
                combo.add(item);
            }
            context.bindValue(SWTObservables.observeText(combo), EMFObservables.observeValue(inputExpression, ExpressionPackage.Literals.EXPRESSION__NAME));
            context.bindValue(SWTObservables.observeText(combo), EMFObservables.observeValue(inputExpression, ExpressionPackage.Literals.EXPRESSION__CONTENT));

            if (combo.getText() == null || combo.getText().isEmpty()) {
                final String defaultValue = input.getDefaultValue();
                for (final String item : combo.getItems()) {
                    if (defaultValue != null && item.equals(defaultValue)) {
                        combo.setText(defaultValue);
                    }
                }
                if (defaultValue == null) {
                    ((Expression) inputExpression).setName(combo.getItem(0));
                    ((Expression) inputExpression).setContent(combo.getItem(0));
                } else if (combo.getText() == null || combo.getText().isEmpty()) {
                    ((Expression) inputExpression).setName(combo.getItem(0));
                    ((Expression) inputExpression).setContent(combo.getItem(0));
                }
            }

            final String desc = messageProvider.getFieldDescription(definition, object.getId());
            if (desc != null && !desc.isEmpty()) {
                combo.setToolTipText(desc);
            }

            ((Expression) inputExpression).setType(ExpressionConstants.CONSTANT_TYPE);
            ((Expression) inputExpression).setReturnType(input.getType());
            return combo;
        }
        return null;
    }

    public boolean isPageFlowContext() {
        return isPageFlowContext;
    }

    public void setIsPageFlowContext(final boolean isPageFlowContext) {
        this.isPageFlowContext = isPageFlowContext;

    }

}
