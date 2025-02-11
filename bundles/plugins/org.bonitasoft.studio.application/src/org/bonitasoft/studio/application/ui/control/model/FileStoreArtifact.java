/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.studio.application.ui.control.model;

import java.util.Map;

import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.studio.common.repository.model.IDeployable;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.common.repository.model.IValidable;
import org.bonitasoft.studio.common.ui.IDisplayable;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public class FileStoreArtifact implements Artifact, IValidable {

    protected IRepositoryFileStore fStore;
    private Object parent;

    public FileStoreArtifact(Object parent, IRepositoryFileStore fStore) {
        this.parent = parent;
        this.fStore = fStore;
    }

    @Override
    public String getName() {
        return fStore.getName();
    }

    @Override
    public String toString() {
        return fStore.getResource().getLocation().toString();
    }

    @Override
    public Object getParent() {
        return parent;
    }

    public IRepositoryFileStore getFileStore() {
        return fStore;
    }

    public IStatus deploy(APISession session, Map<String, Object> options, IProgressMonitor monitor) {
        if (fStore instanceof IDeployable) {
            return ((IDeployable) fStore).deploy(session, options, monitor);
        }
        return ValidationStatus.error(
                String.format("%s is not deployable.", Adapters.adapt(fStore, IDisplayable.class).getDisplayName()));
    }

    @Override
    public IStatus validate(IProgressMonitor monitor) {
        if (fStore instanceof IValidable) {
            return ((IValidable) fStore).validate(monitor);
        }
        return ValidationStatus.ok();
    }
    
    @Override
    public boolean isInClasspath() {
    	return fStore.isInClasspath();
    }

}
