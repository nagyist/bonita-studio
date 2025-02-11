/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.common.repository.provider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.bonitasoft.studio.common.FileUtil;
import org.bonitasoft.studio.common.NamingUtils;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.AbstractRepository;
import org.bonitasoft.studio.common.repository.CommonRepositoryPlugin;
import org.bonitasoft.studio.common.repository.Messages;
import org.bonitasoft.studio.common.repository.core.maven.MavenProjectDependenciesStore;
import org.bonitasoft.studio.common.repository.model.IDefinitionRepositoryStore;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.common.repository.model.IRepositoryStore;
import org.bonitasoft.bpm.connector.model.definition.Category;
import org.bonitasoft.bpm.connector.model.definition.ConnectorDefinition;
import org.bonitasoft.bpm.connector.model.definition.ConnectorDefinitionFactory;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.pics.PicsConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * @author Romain Bioteau
 */
public class DefinitionResourceProvider implements EventHandler {

    public static final String pageTilte = "pageTitle";
    public static final String pageField = "label";
    public static final String pageDescription = "pageDescription";
    public static final String category = "category";
    public static final String connectorDefinition = "connectorDefinitionLabel";
    public static final String connectorDefinitionDescription = "connectorDefinitionDescription";
    public static final String fieldDescription = "description";
    public static final String fieldExample = "example";
    public static final String OUTPUTS_DESC = "outputsDescription";
    public static final String OUTPUT_DESC = "output.description";

    private ImageRegistry categoryImageRegistry;
    private final IRepositoryStore<? extends IRepositoryFileStore<?>> store;
    private final Bundle bundle;
    private List<ExtendedCategory> categories;
    private ExtendedCategory uncategorized;
    private static final Map<String, ResourceBundle> RESOURCE_BUNDLE_CACHE = new HashMap<>();
    private static final Map<IRepositoryStore<? extends IRepositoryFileStore<?>>, DefinitionResourceProvider> INSTANCES_MAP;
    private ConnectorDefinitionRegistry definitionRegistry = new ConnectorDefinitionRegistry();

    static {
        INSTANCES_MAP = new HashMap<>();
    }

    public static DefinitionResourceProvider getInstance(
            final IRepositoryStore<?> store,
            final Bundle bundle) {
        return INSTANCES_MAP.computeIfAbsent(store, s -> new DefinitionResourceProvider(s, bundle));
    }

    private DefinitionResourceProvider(
            final IRepositoryStore<? extends IRepositoryFileStore<?>> store,
            final Bundle bundle) {
        this.store = store;
        this.bundle = bundle;
        eventBroker().subscribe(MavenProjectDependenciesStore.PROJECT_DEPENDENCIES_ANALYZED_TOPIC, this);
    }

    private IEventBroker eventBroker() {
        IEclipseContext context = EclipseContextFactory
                .getServiceContext(CommonRepositoryPlugin.getDefault().getBundle().getBundleContext());
        return context.get(IEventBroker.class);
    }

    protected ImageRegistry createImageRegistry() {
        // If we are in the UI Thread use that
        if (Display.getDefault() != null) {
            return new ImageRegistry(Display.getDefault());
        }

        if (PlatformUI.isWorkbenchRunning()) {
            return new ImageRegistry(PlatformUI.getWorkbench().getDisplay());
        }

        // Invalid thread access if it is not the UI Thread
        // and the workbench is not created.
        throw new SWTError(SWT.ERROR_THREAD_INVALID_ACCESS);
    }

    public ResourceBundle getResourceBundle(final ConnectorDefinition definition,
            final Locale locale) {
        if (definition != null) {
            return RESOURCE_BUNDLE_CACHE.computeIfAbsent(definitionKey(definition), key -> {
                IDefinitionRepositoryStore<?> defStore = (IDefinitionRepositoryStore<?>) store;
                return defStore.find(definition)
                        .map(DefinitionResourceLoaderProvider.class::cast)
                        .map(DefinitionResourceLoaderProvider::getBundleResourceLoader)
                        .map(resourceLoader -> resourceLoader.getResourceBundle(locale))
                        .orElse(null);
            });
        }
        return null;
    }

    private String definitionKey(ConnectorDefinition definition) {
        return definition.getId() + "-" + definition.getVersion();
    }

    private String getMessage(final ConnectorDefinition definition, final String key) {
        final ResourceBundle messages = getResourceBundle(definition, new Locale(Platform.getNL()));
        if (messages == null) {
            return null;
        }
        try {
            return messages.getString(key);
        } catch (final Exception e) {
            return null;
        }
    }

    public String getPageTitle(final ConnectorDefinition definition, final String pageId) {
        return getMessage(definition, pageId + "." + pageTilte);
    }

    public String getPageDescription(final ConnectorDefinition definition,
            final String pageId) {
        return getMessage(definition, pageId + "." + pageDescription);
    }

    public String getFieldLabel(final ConnectorDefinition definition, final String fieldId) {
        return getMessage(definition, fieldId + "." + pageField);
    }

    public String getFieldDescription(final ConnectorDefinition definition,
            final String fieldId) {
        return getMessage(definition, fieldId + "." + fieldDescription);
    }

    public String getFieldExample(final ConnectorDefinition definition,
            final String fieldId) {
        return getMessage(definition, fieldId + "." + fieldExample);
    }

    public String getOutputsDescription(final ConnectorDefinition definition) {
        return getMessage(definition, OUTPUTS_DESC);
    }

    public String getOutputDescription(final ConnectorDefinition definition, final String outputName) {
        return getMessage(definition, outputName + "." + OUTPUT_DESC);
    }

    public String getCategoryLabel(final ConnectorDefinition definition,
            final String categoryId) {
        String label = getMessage(definition, categoryId + "." + category);
        if (label == null || label.isEmpty()) {
            label = categoryId;
        }
        return label;
    }

    public void setCategoryLabel(final Properties messages, final String categoryId,
            final String label) {
        if (label != null) {
            messages.put(categoryId + "." + category, label);
        }
    }

    public void setPageTitleLabel(final Properties messages, final String pageId,
            String label) {
        if (label == null) {
            label = "";
        }
        messages.put(pageId + "." + pageTilte, label);
    }

    public void setPageDescriptionLabel(final Properties messages, final String pageId,
            String label) {
        if (label == null) {
            label = "";
        }
        messages.put(pageId + "." + pageDescription, label);
    }

    public void setConnectorDefinitionLabel(final Properties messages, final String label) {
        if (label != null) {
            messages.put(connectorDefinition, label);
        }
    }

    public void setFieldLabel(final Properties messages, final String fieldId, final String label) {
        String value = label;
        if (value == null) {
            value = fieldId;
        }
        messages.put(fieldId + "." + pageField, value);
    }

    public String getConnectorDefinitionLabel(final ConnectorDefinition definition) {
        String label = getMessage(definition, connectorDefinition);
        if (label == null || label.isEmpty()) {
            label = null;
        }
        return label;
    }

    public Properties getDefaultMessageProperties(final ConnectorDefinition definition) {
        final ResourceBundle bundle = getResourceBundle(definition, new Locale(""));
        final Properties properties = new Properties();
        if (bundle != null) {
            for (final String key : bundle.keySet()) {
                properties.put(key, bundle.getString(key));
            }
        }
        return properties;
    }

    public void saveMessagesProperties(final ConnectorDefinition definition,
            final Properties messages) {

        final String fileName = store.getResource().getLocation().toFile()
                .getAbsolutePath()
                + File.separatorChar
                + NamingUtils.toConnectorDefinitionFilename(definition.getId(),
                        definition.getVersion(), false)
                + ".properties";
        final File defaultMessageFile = new File(fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(defaultMessageFile);
            messages.store(fos, null);
        } catch (final Exception e) {
            BonitaStudioLog.error(e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (final IOException e) {
                    BonitaStudioLog.error(e);
                }
            }
        }
    }

    public Set<Locale> getExistingLocale(final ConnectorDefinition definition) {
        final Set<Locale> result = new HashSet<>();
        String defId = null;
        if (definition == null) {
            return result;
        }
        if (definition.eResource() == null) {
            defId = NamingUtils.toConnectorDefinitionFilename(definition.getId(), definition.getVersion(), false);
        } else {
            defId = URI.decode(definition.eResource().getURI().trimFileExtension().lastSegment());
        }
        try {
            for (final IResource r : store.getResource().members()) {
                if (r.getFileExtension() != null
                        && r.getFileExtension().equals("properties")) {
                    final String resourceName = r.getName();
                    if (resourceName.length() >= defId.length()) {
                        final String baseName = resourceName.substring(0,
                                defId.length());
                        if (baseName.equals(defId)) {
                            if (resourceName.substring(baseName.length())
                                    .indexOf("_") != -1
                                    && resourceName
                                            .substring(baseName.length())
                                            .indexOf(".") != -1) {
                                String language = resourceName
                                        .substring(baseName.length());
                                language = language.substring(1,
                                        language.lastIndexOf("."));
                                String country = null;
                                String variant = null;
                                if (language.indexOf("_") != -1) {
                                    final String[] split = language.split("_");
                                    language = split[0];
                                    country = split[1];
                                    if (split.length == 3) {
                                        variant = split[2];
                                    }
                                }
                                result.add(new Locale(language,
                                        country == null ? "" : country,
                                        variant == null ? "" : variant));
                            }
                        }
                    }
                }
            }
        } catch (final CoreException e) {
            BonitaStudioLog.error(e);
        }
        return result;
    }

    public String getFieldLabel(final Properties messages, final String fieldId) {
        return messages.getProperty(fieldId + "." + pageField);
    }

    public String getPageTitle(final Properties messages, final String pageId) {
        return messages.getProperty(pageId + "." + pageTilte);
    }

    public String getPageDescription(final Properties messages, final String pageId) {
        return messages.getProperty(pageId + "." + pageDescription);
    }

    public String getOutputsDescription(final Properties messages) {
        return messages.getProperty(OUTPUTS_DESC);
    }

    public String getCategoryLabel(final Properties messages, final Category category) {
        String label = messages.getProperty(category.getId() + "."
                + DefinitionResourceProvider.category);
        if (label == null || label.isEmpty()) {
            label = category.getId();
        }
        return label;
    }

    public List<File> getExistingLocalesResource(final ConnectorDefinition def) {
        final List<File> result = new ArrayList<>();
        if (def.eResource() == null) {
            return result;
        }
        final String defId = URI.decode(def.eResource().getURI().trimFileExtension().lastSegment());
        try {
            IFolder resource = store.getResource();
            if (resource.exists()) {
                for (final IResource r : resource.members()) {
                    if (r.getFileExtension() != null
                            && r.getFileExtension().equals("properties")) {
                        final String resourceName = r.getName();
                        if (resourceName.contains(defId)) {
                            final String baseName = resourceName.substring(0,
                                    defId.length());
                            if (baseName.equals(defId)) {
                                result.add(((IFile) r).getLocation().toFile());
                            }
                        }
                    }
                }
            }
        } catch (final CoreException e) {
            BonitaStudioLog.error(e);
        }

        try {
            final URL defaultPropertyFile = bundle.getResource(store.getName() + "/"
                    + defId + ".properties");
            if (defaultPropertyFile != null) {
                result.add(new File(FileLocator.toFileURL(defaultPropertyFile)
                        .getFile()));
            }
            final Enumeration<URL> files = bundle.findEntries(store.getName() + "/",
                    defId + "*.properties", false);
            if (files != null) {
                while (files.hasMoreElements()) {
                    final URL url = files.nextElement();
                    result.add(new File(FileLocator.toFileURL(url).getFile()));
                }
            }
        } catch (final IOException e) {
            BonitaStudioLog.error(e);
        }

        return Collections.unmodifiableList(result);
    }

    public ImageDescriptor createIconDescritpor(final File imageFile, final String iconName) {
        final IFolder targetFoler = store.getResource();
        final IFile iconFile = targetFoler.getFile(iconName);
        BusyIndicator.showWhile(Display.getDefault(), () -> {
            try {
                if (iconFile.exists()) {
                    iconFile.delete(true, AbstractRepository.NULL_PROGRESS_MONITOR);
                }
                BufferedImage image = ImageIO.read(imageFile);
                image = FileUtil.resizeImage(image, 16);
                ImageIO.write(image, "PNG", iconFile.getLocation().toFile());
            } catch (final Exception ex) {
                BonitaStudioLog.error(ex);
            }
        });
        try {
            return ImageDescriptor.createFromURL(iconFile.getLocation().toFile().toURI().toURL());
        } catch (MalformedURLException e) {
            return null;
        }

    }

    public String getConnectorDefinitionDescription(
            final ConnectorDefinition definition) {
        String label = getMessage(definition, connectorDefinitionDescription);
        if (label == null || label.isEmpty()) {
            label = "";
        }
        return label;
    }

    public void setConnectorDefinitionDescription(final Properties messages,
            final String label) {
        if (label != null) {
            messages.put(connectorDefinitionDescription, label);
        }
    }

    public String getFieldDescription(final Properties messages, final String fieldId) {
        return messages.getProperty(fieldId + "." + fieldDescription);
    }

    public String getFieldExample(final Properties messages, final String fieldId) {
        return messages.getProperty(fieldId + "." + fieldExample);
    }

    public void setFieldDescription(final Properties messages, final String fieldId,
            final String description) {
        String value = description;
        if (value == null) {
            value = "";
        }
        messages.put(fieldId + "." + fieldDescription, value);
    }

    public Set<String> getCategoriesIds() {
        return categories
                .stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
    }

    public List<ExtendedCategory> getAllCategories() {
        if (categories == null) {
            loadDefinitionsCategories(null);
        }
        return categories;
    }

    public Category getUnloadableCategory() {
        final Category unloadable = ConnectorDefinitionFactory.eINSTANCE
                .createCategory();
        unloadable.setId(Messages.unloadable);
        return unloadable;
    }

    public Category getUncategorizedCategory() {
        return uncategorized;
    }

    public Image getCategoryIcon(final Category category) {
        if (Messages.unloadable.equals(category.getId())) {
            return Pics.getImage(PicsConstants.error);
        }
        Image icon = categoryImageRegistry.get(category.getId());
        if (icon == null) {
            icon = definitionRegistry.find(category)
                    .map(ExtendedCategory::getImage)
                    .orElse(null);
            if (icon != null) {
                categoryImageRegistry.put(category.getId(), icon);
            } else {
                Image image = categoryImageRegistry.get(category.getId());
                if (image != null) {
                    image.dispose();
                    categoryImageRegistry.remove(category.getId());
                }
            }
        }
        return icon;
    }

    public String getCategoryLabel(final Category category) {
        return definitionRegistry.find(category)
                .map(ExtendedCategory::getLabel)
                .orElse("");
    }

    public synchronized void loadDefinitionsCategories(final IProgressMonitor monitor) {
        RESOURCE_BUNDLE_CACHE.clear();
        categories = definitionRegistry
                .build((IDefinitionRepositoryStore<IRepositoryFileStore<?>>) store)
                .getCategories()
                .stream()
                .collect(Collectors.toList());

        uncategorized = new ExtendedCategory(ConnectorDefinitionFactory.eINSTANCE.createCategory(),
                null,
                Messages.uncategorized);
        uncategorized.setId(Messages.uncategorized);
        categories.add(uncategorized);

        Collections.sort(categories, (c1, c2) -> getCategoryLabel(c1).compareTo(getCategoryLabel(c2)));
    }

    public Image getDefinitionIcon(final ConnectorDefinition definition) {
        if (definition == null) {
            return Pics.getImage(PicsConstants.error);
        }
       return definitionRegistry.find(definition)
            .map(ExtendedConnectorDefinition::getImage)
            .orElse(null);
    }

    public void removeCategoryLabel(final Properties messages, final Category c) {
        messages.remove(c.getId() + "." + category);
    }

    @Override
    public void handleEvent(Event event) {
        loadDefinitionsCategories(AbstractRepository.NULL_PROGRESS_MONITOR);
    }

    public Category getParentCategory(Category category) {
        return definitionRegistry.findParentCategory(category);
    }

    public Category getCategory(Category category) {
        return definitionRegistry.find(category).orElse(null);
    }

    public ConnectorDefinitionRegistry getConnectorDefinitionRegistry() {
        return definitionRegistry;
    }

}
