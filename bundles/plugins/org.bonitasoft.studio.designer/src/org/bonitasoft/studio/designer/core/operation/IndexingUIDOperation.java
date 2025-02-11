/**
 * Copyright (C) 2018 Bonitasoft S.A.
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
package org.bonitasoft.studio.designer.core.operation;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import org.bonitasoft.studio.common.net.HttpClientFactory;
import org.bonitasoft.studio.designer.core.PageDesignerURLFactory;
import org.bonitasoft.studio.designer.core.UIDesignerServerManager;
import org.bonitasoft.studio.designer.i18n.Messages;
import org.bonitasoft.studio.preferences.BonitaStudioPreferencesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class IndexingUIDOperation implements IRunnableWithProgress {

    private PageDesignerURLFactory pageDesignerURLBuilder;

    public IndexingUIDOperation(PageDesignerURLFactory pageDesignerURLBuilder) {
        this.pageDesignerURLBuilder = pageDesignerURLBuilder;
    }

    public IndexingUIDOperation() {

    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        if (UIDesignerServerManager.getInstance().isStarted()) {
            monitor.setTaskName(Messages.indexingUIDPages);
            PageDesignerURLFactory urlBuilder = pageDesignerURLBuilder == null
                    ? new PageDesignerURLFactory(getPreferenceStore()) : pageDesignerURLBuilder;
            URI uri = null;
            try {
                uri = urlBuilder.indexation().toURI();
            } catch (MalformedURLException | URISyntaxException e1) {
                throw new InvocationTargetException(e1);
            }
            try {
                HttpClientFactory.INSTANCE.send(HttpRequest.newBuilder(uri).POST(BodyPublishers.noBody()).build(),
                        BodyHandlers.discarding());
            } catch (IOException | InterruptedException e) {
                throw new InvocationTargetException(e,
                        "Failed to post on " + uri);
            }
        }
    }

    protected IEclipsePreferences getPreferenceStore() {
        return InstanceScope.INSTANCE.getNode(BonitaStudioPreferencesPlugin.PLUGIN_ID);
    }

}
