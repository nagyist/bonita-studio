/**
 * Copyright (C) 2017 BonitaSoft S.A.
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
package org.bonitasoft.studio.la.application.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.bonitasoft.studio.fakes.IResourceFakesBuilder.anIFile;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;

import org.bonitasoft.engine.business.application.ApplicationState;
import org.bonitasoft.engine.business.application.exporter.ApplicationNodeContainerConverter;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeBuilder;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.bonitasoft.studio.common.repository.AbstractRepository;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ApplicationFileStoreTest {

    private ApplicationRepositoryStore store;
    private URL appFile;

    @Before
    public void openStreams() throws Exception {
        store = mock(ApplicationRepositoryStore.class);
        Mockito.when(store.getConverter()).thenReturn(new ApplicationNodeContainerConverter());
        Mockito.when(store.validate(Mockito.anyString(), Mockito.any(InputStream.class)))
                .thenReturn(ValidationStatus.ok());
        appFile = FileLocator.toFileURL(ApplicationFileStoreTest.class.getResource("/myApp.xml"));
    }

    @Test
    public void should_retrieve_model_from_application_xml_file() throws Exception {
        ApplicationFileStore applicationFileStore = spy(new ApplicationFileStore("myApp.xml", store));
        doReturn(anIFile().withName("myApp.xml")
                .withContentSupplier(() -> {
                    try {
                        return appFile.openStream();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }).build())
                        .when(applicationFileStore)
                        .getResource();

        ApplicationNodeContainer applicationNodeContainer = applicationFileStore.getContent();

        assertThat(applicationNodeContainer.getApplications())
                .extracting("displayName", "token", "version", "state")
                .containsExactly(tuple("My App", "myAppHome", "1.0", ApplicationState.ACTIVATED.name()));
    }

    @Test
    public void should_create_a_new_application_xml_file_from_model() throws Exception {
        ApplicationFileStore applicationFileStore = spy(new ApplicationFileStore("myApp.xml", store));
        IFile resource = anIFile().withName("myApp.xml").build();
        doReturn(resource)
                .when(applicationFileStore)
                .getResource();

        applicationFileStore
                .save(ApplicationNodeBuilder.newApplicationContainer()
                        .havingApplications(
                                ApplicationNodeBuilder.newApplication("anAppToken", "A Display Name", "2.0"))
                        .create());

        verify(resource).create((InputStream) notNull(), eq(IResource.FORCE),
                eq(AbstractRepository.NULL_PROGRESS_MONITOR));
    }

    @Test
    public void should_update_application_xml_file_from_model() throws Exception {
        ApplicationFileStore applicationFileStore = spy(new ApplicationFileStore("myApp.xml", store));
        IFile resource = anIFile().withName("myApp.xml").exists().build();
        doReturn(resource)
                .when(applicationFileStore)
                .getResource();

        applicationFileStore
                .save(ApplicationNodeBuilder.newApplicationContainer()
                        .havingApplications(
                                ApplicationNodeBuilder.newApplication("anAppToken", "A Display Name", "2.0"))
                        .create());

        verify(resource).setContents((InputStream) notNull(), eq(IResource.FORCE | IResource.KEEP_HISTORY),
                eq(AbstractRepository.NULL_PROGRESS_MONITOR));
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_fail_when_saving_unsupported_content() throws Exception {
        ApplicationFileStore applicationFileStore = new ApplicationFileStore("myApp.xml", null);

        applicationFileStore.doSave(null);
    }

}
