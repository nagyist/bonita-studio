/**
 * Copyright (C) 2012-2014 Bonitasoft S.A.
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
package org.bonitasoft.studio.diagram.custom.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bonitasoft.studio.common.NamingUtils;
import org.bonitasoft.studio.common.editingdomain.BonitaEditingDomainUtil;
import org.bonitasoft.studio.common.emf.tools.EMFResourceUtil;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.emf.tools.RemoveDanglingReferences;
import org.bonitasoft.studio.common.extension.BonitaStudioExtensionRegistryManager;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.platform.tools.CopyInputStream;
import org.bonitasoft.studio.common.repository.ImportArchiveData;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.model.IRepository;
import org.bonitasoft.studio.common.repository.model.ReadFileStoreException;
import org.bonitasoft.studio.common.repository.store.AbstractEMFRepositoryStore;
import org.bonitasoft.studio.diagram.custom.Activator;
import org.bonitasoft.studio.diagram.custom.i18n.Messages;
import org.bonitasoft.studio.model.configuration.Configuration;
import org.bonitasoft.studio.model.process.AbstractProcess;
import org.bonitasoft.studio.model.process.MainProcess;
import org.bonitasoft.studio.model.process.Pool;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.model.process.diagram.part.ProcessDiagramEditorUtil;
import org.bonitasoft.studio.model.process.provider.ProcessItemProviderAdapterFactory;
import org.bonitasoft.studio.pics.Pics;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.FeatureNotFoundException;
import org.eclipse.emf.edapt.internal.common.URIUtils;
import org.eclipse.emf.edapt.migration.MigrationException;
import org.eclipse.emf.edapt.migration.execution.Migrator;
import org.eclipse.emf.edapt.spi.history.Release;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.util.NotationAdapterFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class DiagramRepositoryStore extends AbstractEMFRepositoryStore<DiagramFileStore> {

    public static final String STORE_NAME = "diagrams";
    private static final Set<String> extensions = new HashSet<>();
    static {
        extensions.add("proc");
    }

    private AdapterFactoryLabelProvider labelProvider;

    private final Map<String, String> eObjectIdToLabel = new HashMap<>();
    private Synchronizer synchronizer;
    private List<AbstractProcess> computedProcessesList;

    @Override
    public void createRepositoryStore(IRepository repository) {
        super.createRepositoryStore(repository);
        synchronizer = loadSynchronizer();
    }

    private Synchronizer loadSynchronizer() {
        for (IConfigurationElement elem : BonitaStudioExtensionRegistryManager.getInstance()
                .getConfigurationElements("org.bonitasoft.studio.diagram.custom.configurationSynchronizer")) {
            try {
                return (Synchronizer) elem.createExecutableExtension("class");
            } catch (CoreException e) {
                BonitaStudioLog.error(e);
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return STORE_NAME;
    }

    @Override
    public String getDisplayName() {
        return Messages.diagrams;
    }

    @Override
    public Image getIcon() {
        return Pics.getImage("ProcessDiagramFile.gif", Activator.getDefault());
    }

    @Override
    public DiagramFileStore createRepositoryFileStore(final String fileName) {
        return new DiagramFileStore(fileName, this);
    }

    @Override
    public EditingDomain getEditingDomain(final URI uri) {
        if (uri != null) {
            return BonitaEditingDomainUtil.getSharedEditingDomain(uri);
        }
        return super.getEditingDomain(uri);
    }

    public List<AbstractProcess> getAllProcesses() {
        final List<AbstractProcess> processes = new ArrayList<>();
        for (final DiagramFileStore file : getChildren()) {
            processes.addAll(file.getProcesses(true));
        }
        return processes;
    }

    @Override
    protected void handleOverwrite(final IFile file) throws CoreException {
        final DiagramFileStore fileStore = createRepositoryFileStore(file.getName());
        fileStore.delete();
    }

    @Override
    public Set<String> getCompatibleExtensions() {
        return extensions;
    }

    public List<AbstractProcess> findProcesses(final String name) {
        List<AbstractProcess> processes = hasComputedProcesses() ? getComputedProcesses() : getAllProcesses();
        return processes
                .stream()
                .filter(p -> Objects.equals(p.getName(), name))
                .collect(Collectors.toList());
    }

    public AbstractProcess findProcess(String name, String version) {
        List<AbstractProcess> processes = hasComputedProcesses() ? getComputedProcesses() : getAllProcesses();
        if (version != null && !version.trim().isEmpty()) {
            return processes.stream()
                    .filter(p -> Objects.equals(p.getName(), name) && Objects.equals(p.getVersion(), version))
                    .findFirst()
                    .orElse(null);
        } else {
            // return the process with the higher version
            return findProcesses(name).stream()
                    .max((p1,p2) -> p1.getVersion().compareTo(p2.getVersion()))
                    .orElse(null);
        }
    }

    public List<DiagramFileStore> getRecentChildren(final int nbResult) {
        if (!getResource().exists()) {
            return Collections.emptyList();
        }
        refresh();

        final List<DiagramFileStore> result = new ArrayList<>();
        final List<IResource> resources = new ArrayList<>();
        final IFolder folder = getResource();
        try {
            for (final IResource r : folder.members()) {
                if (r.getFileExtension() != null
                        && getCompatibleExtensions().contains(
                                r.getFileExtension())) {
                    resources.add(r);
                }
            }
        } catch (final CoreException e) {
            BonitaStudioLog.error(e);
        }

        Collections.sort(resources, new Comparator<>() {

            @Override
            public int compare(final IResource arg0, final IResource arg1) {
                final long lastModifiedArg1 = arg1.getLocation().toFile()
                        .lastModified();
                final long lastModifiedArg0 = arg0.getLocation().toFile()
                        .lastModified();
                return Long.valueOf(lastModifiedArg1).compareTo(
                        Long.valueOf(lastModifiedArg0));
            }
        });

        for (int i = 0; i < nbResult; i++) {
            if (resources.size() > i) {
                result.add(createRepositoryFileStore(resources.get(i).getName()));
            }
        }

        return result;
    }

    public DiagramFileStore getDiagram(final String name, final String version) {
        for (final DiagramFileStore diagram : getChildren()) {
            MainProcess diagramModel;
            try {
                diagramModel = diagram.getContent();
            } catch (ReadFileStoreException e) {
                return null;
            }
            if (diagramModel != null) {
                final String diagramName = diagramModel.getName();
                if (diagramName.equals(name)) {
                    final String diagramVersion = diagramModel.getVersion();
                    if (diagramVersion.equals(version)) {
                        return diagram;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected DiagramFileStore doImportArchiveData(ImportArchiveData importArchiveData, IProgressMonitor monitor)
            throws CoreException {
        final DiagramFileStore diagramfileStore = super.doImportArchiveData(importArchiveData, monitor);
        if (diagramfileStore == null) {
            return null;
        }
        try {
            MainProcess content = diagramfileStore.getContent();
            if (content == null) {
                diagramfileStore.delete();
                return null;
            }
        } catch (ReadFileStoreException e) {
            diagramfileStore.delete();
            return null;
        }
        return diagramfileStore;
    }

    @Override
    protected DiagramFileStore doImportInputStream(final String fileName,
            final InputStream inputStream) {
        final CopyInputStream copyIs = new CopyInputStream(inputStream);
        final InputStream originalStream = copyIs.getCopy();
        final String newFileName = getValidFileName(fileName, copyIs.getCopy());
        copyIs.close();
        return super.doImportInputStream(newFileName, originalStream);
    }

    protected String getValidFileName(final String fileName, final InputStream is) {
        try {
            final Map<String, String[]> featureValueFromEObjectType = EMFResourceUtil.getFeatureValueFromEObjectType(
                    is,
                    "process:MainProcess",
                    ProcessPackage.Literals.ELEMENT__NAME,
                    ProcessPackage.Literals.ABSTRACT_PROCESS__VERSION);
            if (featureValueFromEObjectType.size() == 1) {
                final String[] next = featureValueFromEObjectType.values()
                        .iterator().next();
                return NamingUtils.toDiagramFilename(next[0], next[1]);
            }
        } catch (final Exception e) {
            BonitaStudioLog.error(e, Activator.PLUGIN_ID);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (final IOException e) {
                    BonitaStudioLog.error(e, Activator.PLUGIN_ID);
                }
            }
        }
        return fileName;
    }

    public Set<String> getAllProcessIds() {
        final Set<String> resut = new HashSet<>();
        for (final DiagramFileStore fStore : getChildren()) {
            File file = fStore.getResource().getLocation().toFile();
            try (InputStream is = Files.newInputStream(file.toPath())) {
                String[] poolIds = EMFResourceUtil.getEObectIfFromEObjectType(is, "process:Pool");
                if (poolIds != null) {
                    resut.addAll(Arrays.asList(poolIds));
                }
            } catch (final FeatureNotFoundException | IOException e) {
                BonitaStudioLog.error(e);
            }
        }
        return resut;
    }

    @Override
    public AdapterFactoryLabelProvider getLabelProvider() {
        if (labelProvider != null) {
            labelProvider.dispose();
        }
        final ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(
                ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
        adapterFactory
                .addAdapterFactory(new ResourceItemProviderAdapterFactory());
        adapterFactory
                .addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
        adapterFactory.addAdapterFactory(new ProcessItemProviderAdapterFactory());
        adapterFactory.addAdapterFactory(new NotationAdapterFactory());
        labelProvider = new AdapterFactoryLabelProvider(adapterFactory);
        return labelProvider;
    }

    @Override
    protected void addAdapterFactory(final ComposedAdapterFactory adapterFactory) {
        adapterFactory.addAdapterFactory(new ProcessItemProviderAdapterFactory());
        adapterFactory.addAdapterFactory(new NotationAdapterFactory());
    }

    @Override
    protected InputStream handlePreImport(final String fileName,
            final InputStream inputStream) throws MigrationException, IOException {

        DiagramFileStore fileStore = getChild(fileName, false);
        Display.getDefault().syncExec(() -> {
            if (fileStore != null && fileStore.isOpened()) {
                fileStore.close();
            }
        });

        CopyInputStream copyIs = null;
        Resource diagramResource = null;
        try {
            final InputStream is = super.handlePreImport(fileName, inputStream);
            copyIs = new CopyInputStream(is);
            diagramResource = getTmpEMFResource("beforeImport.proc",
                    copyIs.getFile());

            diagramResource.load(Collections.EMPTY_MAP);
            if (diagramResource.getContents().isEmpty()) {
                throw new IOException("Resource is empty.");
            }

            MainProcess diagram = diagramResource.getContents().stream()
                    .filter(MainProcess.class::isInstance)
                    .map(MainProcess.class::cast)
                    .findFirst()
                    .orElseThrow(() -> new IOException(
                            "Resource content is invalid. There should be only one MainProcess per .proc file."));

            //Sanitize model
            new RemoveDanglingReferences(diagram).execute();
            diagram.eResource().getContents().stream().filter(Diagram.class::isInstance).findFirst()
                    .ifPresent(d -> new RemoveDanglingReferences(d).execute());
            diagram.eResource().getContents().removeIf(eObject -> isFormDiagram(eObject));

            applyTransformations(diagram);

            ProcessConfigurationRepositoryStore confStore = RepositoryManager.getInstance()
                    .getRepositoryStore(ProcessConfigurationRepositoryStore.class);
            for (Pool process : ModelHelper.getAllElementOfTypeIn(diagram, Pool.class)) {
                ProcessConfigurationFileStore file = confStore.getChild(ModelHelper.getEObjectID(process) + ".conf",
                        true);
                if (file != null) {
                    try {
                        Configuration configuration = file.getContent();
                        synchronizer.synchronize(process, configuration);
                        file.save(configuration);
                    } catch (ReadFileStoreException e) {
                        BonitaStudioLog.warning(file.getName() + ": " + e.getMessage(), Activator.PLUGIN_ID);
                    }
                }
                process.getConfigurations().stream().forEach(conf -> synchronizer.synchronize(process, conf));
            }
            try {
                diagramResource.save(ProcessDiagramEditorUtil.getSaveOptions());
            } catch (final IOException e) {
                BonitaStudioLog.error(e);
            }
            return new FileInputStream(new File(diagramResource.getURI()
                    .toFileString()));
        } finally {
            if (copyIs != null) {
                copyIs.close();
            }
            if (diagramResource != null) {
                diagramResource.delete(Collections.emptyMap());
            }
        }
    }



    private boolean isFormDiagram(EObject eObject) {
        return eObject instanceof Diagram && "Form".equals(((Diagram) eObject).getType());
    }

    protected InputStream openError(final String fileName) {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                BonitaStudioLog.log("Incompatible Version for "
                        + fileName);
                MessageDialog.openWarning(Display.getDefault()
                        .getActiveShell(),
                        Messages.incompatibleVersionTitle,
                        Messages.incompatibleVersionMsg);
            }
        });
        return null;
    }

    @Override
    protected Release getRelease(final Migrator targetMigrator,
            final Resource resource) {
        final String modelVersion = getModelVersion(resource);
        return getRelease(targetMigrator, modelVersion);
    }

    public Release getRelease(final Migrator targetMigrator,
            final String modelVersion) {
        for (final Release release : targetMigrator.getReleases()) {
            if (release.getLabel().equals(modelVersion)) {
                return release;
            }
        }
        return targetMigrator.getReleases().iterator().next(); // First release
        // of all time
    }

    private String getModelVersion(final Resource resource) {
        try (InputStream is = URIUtils.getInputStream(resource.getURI())) {
            return DiagramCompatibilityValidator.readModelVersion(is);
        } catch (IOException | FeatureNotFoundException e) {
            BonitaStudioLog.error(e);
            return null;
        }
    }

    public void updateProcessLabel(final String processId,
            final String processLabel) {
        eObjectIdToLabel.put(processId, processLabel);
    }

    @Override
    public void close() {
        BonitaEditingDomainUtil.cleanEditingDomainRegistry();
        super.close();
    }

    @Override
    public IStatus validate(String filename, InputStream inputStream) {
        if (filename != null && filename.endsWith(".proc")) {
            return new DiagramCompatibilityValidator(
                    String.format(org.bonitasoft.studio.common.Messages.incompatibleModelVersion, filename),
                    String.format(org.bonitasoft.studio.common.Messages.migrationWillBreakRetroCompatibility, filename))
                            .validate(inputStream);
        }
        return super.validate(filename, inputStream);
    }

    public List<AbstractProcess> computeProcesses(IProgressMonitor monitor) {
        monitor.beginTask(Messages.loadingAllProcesses, IProgressMonitor.UNKNOWN);
        resetComputedProcesses();
        computedProcessesList = getAllProcesses();
        return computedProcessesList;
    }

    public void resetComputedProcesses() {
        if (computedProcessesList != null) {
            computedProcessesList.clear();
            computedProcessesList = null;
        }
    }

    public boolean hasComputedProcesses() {
        return computedProcessesList != null;
    }

    public List<AbstractProcess> getComputedProcesses() {
        if (!hasComputedProcesses()) {
            throw new IllegalArgumentException("Project process list not computed yet !");
        }
        return computedProcessesList;
    }

    public List<AbstractProcess> getAllProcessesWithoutReoload() {
        final List<AbstractProcess> processes = new ArrayList<>();
        for (final DiagramFileStore file : getChildren()) {
            processes.addAll(file.getProcesses(false));
        }
        return processes;
    }
}
