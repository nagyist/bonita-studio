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
package org.bonitasoft.studio.la.extension;

import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.bonitasoft.studio.common.repository.extension.update.DependencyUpdate;
import org.bonitasoft.studio.common.repository.extension.update.participant.ExtensionUpdateParticipant;
import org.bonitasoft.studio.common.repository.extension.update.participant.ExtensionUpdateParticipantFactory;
import org.bonitasoft.studio.common.repository.extension.update.participant.ExtensionUpdateParticipantFactoryRegistry;

public class ThemeUpdateParticipantFactory implements ExtensionUpdateParticipantFactory {

    private ThemeArtifactProvider themeArtifactProvider;
    private ApplicationCollector applicationCollector;

    @Inject
    public ThemeUpdateParticipantFactory(ThemeArtifactProvider themeArtifactProvider,
            ApplicationCollector applicationCollector) {
        this.themeArtifactProvider = themeArtifactProvider;
        this.applicationCollector = applicationCollector;
    }

    @PostConstruct
    public void register() {
        ExtensionUpdateParticipantFactoryRegistry.getInstance().register(this);
    }

    @Override
    public ExtensionUpdateParticipant create(List<DependencyUpdate> dependenciesUpdate) {
        return new ThemeUpdateParticipant(dependenciesUpdate, themeArtifactProvider, applicationCollector);
    }
}
