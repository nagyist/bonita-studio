/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.studio.common.repository.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProjectDescription;

/**
 * @author Romain Bioteau
 */
public class ProjectDescriptionBuilder {

    private final List<String> natureIds = new ArrayList<>();
    private final Set<String> builderIds = new HashSet<>();
    private String comment;
    private String name;

    public ProjectDescriptionBuilder withProjectName(final String name) {
        this.name = name;
        return this;
    }

    public ProjectDescriptionBuilder withComment(final String comment) {
        this.comment = comment;
        return this;
    }

    public ProjectDescriptionBuilder havingNature(final String natureId) {
        natureIds.add(natureId);
        return this;
    }

    public ProjectDescriptionBuilder havingNatures(final Collection<String> natures) {
        natureIds.addAll(natures);
        return this;
    }

    public ProjectDescriptionBuilder havingBuilder(final String builderId) {
        builderIds.add(builderId);
        return this;
    }

    public ProjectDescriptionBuilder havingBuilders(final Collection<String> builders) {
        builderIds.addAll(builders);
        return this;
    }

    public IProjectDescription build() {
        return build(new ProjectDescription());
    }

    public IProjectDescription build(IProjectDescription existingDescriptor) {
        if (name != null) {
            existingDescriptor.setName(name);
        }
        existingDescriptor.setComment(comment);
        existingDescriptor.setNatureIds(natureIds.stream().distinct().toArray(String[]::new));
        final Map<String, ICommand> builderCommmands = new HashMap<>();
        for (final String builderId : builderIds) {
            final ICommand command = existingDescriptor.newCommand();
            command.setBuilderName(builderId);
            builderCommmands.put(builderId, command);
        }
        existingDescriptor
                .setBuildSpec(builderCommmands.values().toArray(new ICommand[builderCommmands.values().size()]));
        // Clear filters
        if(existingDescriptor instanceof ProjectDescription) {
            ((ProjectDescription) existingDescriptor).setFilterDescriptions(null);
        }
        return existingDescriptor;
    }
}
