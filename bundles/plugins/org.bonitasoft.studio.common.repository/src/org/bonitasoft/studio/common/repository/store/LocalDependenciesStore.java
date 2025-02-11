/**
 * Copyright (C) 2021 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.common.repository.store;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.maven.model.Dependency;
import org.bonitasoft.studio.common.Strings;
import org.bonitasoft.studio.common.repository.core.maven.migration.model.DependencyLookup;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class LocalDependenciesStore {

    private static final String BACKUP_EXT = ".backup";
    public static final String NAME = ".store";

    private Path project;

    public LocalDependenciesStore(Path project) {
        this.project = project;
    }

    public DependencyLookup install(DependencyLookup dependencyLookup) throws CoreException {
        if (dependencyLookup.getStatus() == DependencyLookup.Status.FOUND || dependencyLookup.getFile() == null) {
            return dependencyLookup;
        }
        File dependencyFile = dependencyLookup.getFile();
        if (!dependencyFile.isFile()) {
            throw new CoreException(new Status(IStatus.ERROR, getClass(),
                    String.format("Cannot install %s dependency. %s is not a file.",
                            dependencyFile.getName(),
                            dependencyLookup.getFile().toPath())));
        }
        Dependency dependency = dependencyLookup.toMavenDependency();
        Path targetFolder = dependencyPath(dependency);
        try {
            Files.createDirectories(targetFolder);
            if (!targetFolder.toFile().exists()) {
                throw new CoreException(new Status(IStatus.ERROR, getClass(),
                        String.format("Cannot install %s dependency. Failed to create store folders.",
                                dependencyFile.getName())));
            }
            Path dependencyPath = targetFolder.resolve(dependencyFileName(dependency));
            backup(dependency);
            Files.copy(dependencyFile.toPath(), dependencyPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new CoreException(new Status(IStatus.ERROR, getClass(),
                    String.format("Cannot install %s dependency.",
                            dependencyFile.getName()),
                    e));
        } finally {
            dependencyLookup.deleteCopy();
        }
        return dependencyLookup;
    }

    public void backup(Dependency dependency) throws IOException {
        Path targetFolder = dependencyPath(dependency);
        Path dependencyPath = targetFolder.resolve(dependencyFileName(dependency));
        if (dependencyPath.toFile().exists()) {
            File backup = toBackupFile(dependencyPath);
            Files.move(dependencyPath, backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private File toBackupFile(Path dependencyPath) {
        return new File(dependencyPath.getParent().toFile(), dependencyPath.getFileName() + BACKUP_EXT);
    }

    public Path dependencyPath(Dependency dependency) {
        return project
                .resolve(NAME)
                .resolve(dependency.getGroupId().replace(".", "/"))
                .resolve(dependency.getArtifactId())
                .resolve(dependency.getVersion());
    }

    public static String dependencyFileName(Dependency dependency) {
        if (dependency.getClassifier() != null && !dependency.getClassifier().isBlank()) {
            return String.format("%s-%s-%s.%s", dependency.getArtifactId(),
                    dependency.getVersion(),
                    dependency.getClassifier(),
                    dependency.getType());
        }
        return String.format("%s-%s.%s", dependency.getArtifactId(),
                dependency.getVersion(),
                dependency.getType());
    }

    public void remove(Dependency dependency) throws CoreException {
        Path dependencyPath = dependencyPath(dependency).resolve(dependencyFileName(dependency));
        if (dependencyPath.toFile().exists()) {
            try {
                Files.delete(dependencyPath);
            } catch (IOException e) {
                throw new CoreException(
                        new Status(IStatus.ERROR, LocalDependenciesStore.class, "Failed to delete " + dependencyPath,
                                e));
            }
            Path parent = dependencyPath.getParent();
            while (parent.toFile().exists() && isEmptyFolder(parent)) {
                try {
                    Files.delete(parent);
                } catch (IOException e) {
                    throw new CoreException(
                            new Status(IStatus.ERROR, LocalDependenciesStore.class, "Failed to delete " + parent, e));
                }
                parent = parent.getParent();
            }
        }
    }

    // Use the backup file if exists to revert install
    public void revert(Dependency dependency) throws CoreException {
        Path dependencyPath = dependencyPath(dependency).resolve(dependencyFileName(dependency));
        try {
            File backupFile = toBackupFile(dependencyPath);
            if (backupFile.exists()) {
                Files.move(backupFile.toPath(), dependencyPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new CoreException(new Status(IStatus.ERROR, getClass(),
                    String.format("Cannot retrieve backup for %s dependency.", dependencyPath), e));
        }
    }

    public void deleteBackup(Dependency dependency) throws CoreException {
        Path dependencyPath = dependencyPath(dependency).resolve(dependencyFileName(dependency));
        try {
            File backupFile = toBackupFile(dependencyPath);
            if (backupFile.exists()) {
                Files.delete(backupFile.toPath());
            }
            Path parent = backupFile.toPath().getParent();
            while (parent.toFile().exists() && isEmptyFolder(parent)) {
                try {
                    Files.delete(parent);
                } catch (IOException e) {
                    throw new CoreException(
                            new Status(IStatus.ERROR, LocalDependenciesStore.class, "Failed to delete " + parent, e));
                }
                parent = parent.getParent();
            }
        } catch (IOException e) {
            throw new CoreException(new Status(IStatus.ERROR, getClass(),
                    String.format("Cannot delete backup of %s dependency.", dependencyPath), e));
        }
    }

    public boolean isLocalDependency(Dependency dependency) {
        if(Strings.isNullOrEmpty(dependency.getVersion())){
            return false;
        }
        return dependencyPath(dependency).resolve(dependencyFileName(dependency)).toFile().exists();
    }

    private boolean isEmptyFolder(Path folder) {
        var children = folder.toFile().listFiles();
        return children == null || children.length == 0;
    }

}
