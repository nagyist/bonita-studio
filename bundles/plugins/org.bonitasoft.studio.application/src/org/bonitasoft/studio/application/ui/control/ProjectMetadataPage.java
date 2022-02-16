/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.studio.application.ui.control;

import java.io.IOException;
import java.util.Objects;

import org.bonitasoft.studio.application.i18n.Messages;
import org.bonitasoft.studio.common.RedirectURLBuilder;
import org.bonitasoft.studio.common.jface.databinding.validator.EmptyInputValidator;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryNameValidator;
import org.bonitasoft.studio.common.repository.core.maven.BonitaProjectBuilder.BonitaRuntimeVersionValidator;
import org.bonitasoft.studio.common.repository.core.maven.contribution.InstallBonitaMavenArtifactsOperation;
import org.bonitasoft.studio.common.repository.core.maven.model.ProjectMetadata;
import org.bonitasoft.studio.common.repository.ui.validator.MavenIdValidator;
import org.bonitasoft.studio.ui.converter.ConverterBuilder;
import org.bonitasoft.studio.ui.databinding.UpdateStrategyFactory;
import org.bonitasoft.studio.ui.validator.MultiValidator;
import org.bonitasoft.studio.ui.widget.ComboWidget;
import org.bonitasoft.studio.ui.widget.TextAreaWidget;
import org.bonitasoft.studio.ui.widget.TextWidget;
import org.bonitasoft.studio.ui.wizard.ControlSupplier;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;

public class ProjectMetadataPage implements ControlSupplier {

    private static final String STUDIO_MAINTENANCE_UPDATE_REDIRECT_ID = "735";
    private IObservableValue<ProjectMetadata> metadataObservale;
    private boolean createProject;

    public ProjectMetadataPage(ProjectMetadata metadata, boolean createProject) {
        this.createProject = createProject;
        this.metadataObservale = new WritableValue<>(metadata, ProjectMetadata.class);
    }

    @Override
    public Control createControl(Composite parent, IWizardContainer wizardContainer, DataBindingContext ctx) {
        Composite composite = new Composite(parent, SWT.None);
        composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2)
                .margins(10, 10)
                .spacing(20, 10)
                .extendedMargins(0, 0, 0, 20).create());
        composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

        var nameObservable = PojoProperties.value("name", String.class)
                .observeDetail(metadataObservale);
        var artifactIdObservable = PojoProperties.value("artifactId", String.class).observeDetail(metadataObservale);
        new TextWidget.Builder()
                .withLabel(Messages.name + " *")
                .labelAbove()
                .grabHorizontalSpace()
                .fill()
                .bindTo(nameObservable)
                .withValidator(new MultiValidator.Builder()
                        .havingValidators(new RepositoryNameValidator(() -> createProject),
                                engineRestartWarning(nameObservable.getValue()))
                        .create())
                .inContext(ctx)
                .useNativeRender()
                .createIn(composite);

        new TextWidget.Builder()
                .withLabel(Messages.version + " *")
                .labelAbove()
                .grabHorizontalSpace()
                .fill()
                .bindTo(PojoProperties.value("version").observeDetail(metadataObservale))
                .withValidator(new EmptyInputValidator(Messages.version))
                .inContext(ctx)
                .useNativeRender()
                .createIn(composite);

        new TextWidget.Builder()
                .withLabel("Group ID *")
                .withTootltip(Messages.groupIdTootltip)
                .labelAbove()
                .grabHorizontalSpace()
                .fill()
                .bindTo(PojoProperties.value("groupId").observeDetail(metadataObservale))
                .withValidator(new MavenIdValidator("Group ID"))
                .inContext(ctx)
                .useNativeRender()
                .createIn(composite);

        TextWidget textWidget = new TextWidget.Builder()
                .withLabel("Artifact ID")
                .withTootltip(Messages.artifactIdTootltip)
                .labelAbove()
                .grabHorizontalSpace()
                .fill()
                .bindTo(artifactIdObservable)
                .withValidator(new MavenIdValidator("Artifact ID", false))
                .inContext(ctx)
                .useNativeRender()
                .createIn(composite);

        ctx.bindValue(WidgetProperties.message().observe(textWidget.getTextControl()),
                nameObservable,
                UpdateStrategyFactory.neverUpdateValueStrategy().create(),
                UpdateStrategyFactory.updateValueStrategy()
                        .withConverter(ConverterBuilder.<String, String> newConverter()
                                .fromType(String.class)
                                .toType(String.class)
                                .withConvertFunction(ProjectMetadata::toArtifactId)
                                .create())
                        .create());

        new ComboWidget.Builder()
                .withLabel(Messages.targetRuntimeVersion + " *")
                .labelAbove()
                .horizontalSpan(2)
                .grabHorizontalSpace()
                .fill()
                .widthHint(750)
                .withItems(availableCompatibleVersions())
                .withValidator(new MultiValidator.Builder()
                        .havingValidators(new EmptyInputValidator(Messages.targetRuntimeVersion), new BonitaRuntimeVersionValidator())
                        .create())
                .bindTo(PojoProperties.value("bonitaRuntimeVersion", String.class).observeDetail(metadataObservale))
                .inContext(ctx)
                .useNativeRender()
                .createIn(composite);
        
        var targetVersionMessage = new Link(composite, SWT.NONE);
        targetVersionMessage
                .setLayoutData(GridDataFactory.fillDefaults().grab(true, false).indent(0, -10).span(2, 1).create());
        targetVersionMessage.setText(Messages.studioMaintenanceUpdateMessage);
        targetVersionMessage.addListener(SWT.Selection, e -> {
            try {
                java.awt.Desktop.getDesktop()
                        .browse(RedirectURLBuilder.createURI(STUDIO_MAINTENANCE_UPDATE_REDIRECT_ID));
            } catch (final IOException ioe) {
                BonitaStudioLog.error(ioe);
            }
        });

        var textArea = new TextAreaWidget.Builder()
                .withLabel(Messages.description)
                .labelAbove()
                .heightHint(100)
                .widthHint(500)
                .grabVerticalSpace()
                .grabHorizontalSpace()
                .fill()
                .bindTo(PojoProperties.value("description").observeDetail(metadataObservale))
                .inContext(ctx)
                .horizontalSpan(2)
                .useNativeRender()
                .createIn(composite);

        textArea.getTextControl().addTraverseListener(event -> {
            if (event.detail == SWT.TRAVERSE_TAB_NEXT
                    || event.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                event.doit = true;
            }
        });

        return composite;
    }

    private String[] availableCompatibleVersions() {
        try {
            return InstallBonitaMavenArtifactsOperation.listBonitaRuntimeBomVersions();
        } catch (IOException e) {
            BonitaStudioLog.error(e);
            return new String[0];
        }
    }

    private IValidator<String> engineRestartWarning(String originalName) {
        return name -> !Objects.equals(originalName, name)
                ? ValidationStatus.warning(Messages.engineRestartWarning)
                : ValidationStatus.ok();
    }

    public ProjectMetadata getMetadata() {
        return metadataObservale.getValue();
    }
}
