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
package org.bonitasoft.studio.tests.timer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.URL;

import org.bonitasoft.bpm.model.process.MainProcess;
import org.bonitasoft.bpm.model.process.Pool;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.studio.common.repository.AbstractRepository;
import org.bonitasoft.studio.common.repository.RepositoryAccessor;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.engine.BOSEngineManager;
import org.bonitasoft.studio.engine.command.RunProcessCommand;
import org.bonitasoft.studio.engine.operation.ProcessSelector;
import org.bonitasoft.studio.importer.bos.operation.ImportBosArchiveOperation;
import org.bonitasoft.studio.model.process.diagram.part.ProcessDiagramEditor;
import org.bonitasoft.studio.tests.util.Await;
import org.bonitasoft.studio.tests.util.InitialProjectRule;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Romain Bioteau
 */
public class TestNonInterruptingBoundaryTimerEvent {

    @Rule
    public InitialProjectRule projectRule = InitialProjectRule.INSTANCE;
    
    private APISession session;
    private RepositoryAccessor repositoryAccessor;

    @Before
    public void setUp() throws LoginException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        session = BOSEngineManager.getInstance().loginDefaultTenant(AbstractRepository.NULL_PROGRESS_MONITOR);
        repositoryAccessor = RepositoryManager.getInstance().getAccessor();
    }

    @After
    public void tearDown() {
        BOSEngineManager.getInstance().logoutDefaultTenant(session);
    }

    @Test
    public void testNonInterruptingBoundaryEvent() throws Exception {
        final ProcessAPI processApi = BOSEngineManager.getInstance().getProcessAPI(session);
        final ImportBosArchiveOperation op = new ImportBosArchiveOperation(repositoryAccessor);
        final URL fileURL1 = FileLocator
                .toFileURL(TestNonInterruptingBoundaryTimerEvent.class
                        .getResource("TestNonInterruptingTimerEvent-1.0.bos")); //$NON-NLS-1$
        op.setArchiveFile(FileLocator.toFileURL(fileURL1).getFile());
        op.setCurrentRepository(repositoryAccessor.getCurrentRepository());
        PlatformUI.getWorkbench().getProgressService().run(true, false, op);
        for (final IRepositoryFileStore f : op.getFileStoresToOpen()) {
            f.open();
        }

        final ProcessDiagramEditor processEditor = (ProcessDiagramEditor) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        final MainProcess mainProcess = (MainProcess) processEditor.getDiagramEditPart().resolveSemanticElement();
        assertEquals("TestNonInterruptingTimerEvent", mainProcess.getName());
        final SearchOptions searchOptions = new SearchOptionsBuilder(0, 10).done();

        final RunProcessCommand runProcessCommand = new RunProcessCommand(true);
        runProcessCommand
                .execute(ProcessSelector.createExecutionEvent((Pool) mainProcess.getElements().get(0)));
        final String urlGivenToBrowser = runProcessCommand.getUrl().toString();
        assertFalse("The url contains null:" + urlGivenToBrowser, urlGivenToBrowser.contains("null"));
        final long processId = processApi.getProcessDefinitionId("TestNonInterruptingTimerEvent", "1.0");
        processApi.startProcess(processId);
        var identityAPI = BOSEngineManager.getInstance().getIdentityAPI(session);
        Await.waitUntil(() -> {
            try {
                return processApi.searchPendingTasksForUser(
                        identityAPI.getUserByUserName("walter.bates").getId(),
                        searchOptions)
                        .getResult()
                        .stream()
                        .map(HumanTaskInstance::getProcessDefinitionId)
                        .filter(id -> id.equals(processId))
                        .count() == 2;
            } catch (UserNotFoundException | SearchException e) {
                throw new RuntimeException(e);
            }
        }, 30000, 200);
    }

}
