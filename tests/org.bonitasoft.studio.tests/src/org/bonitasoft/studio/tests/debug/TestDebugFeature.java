/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.tests.debug;

import org.bonitasoft.engine.api.ProcessManagementAPI;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.debug.command.DebugProcessCommand;
import org.bonitasoft.studio.debug.i18n.Messages;
import org.bonitasoft.studio.engine.BOSEngineManager;
import org.bonitasoft.studio.test.swtbot.util.SWTBotTestUtil;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.gef.finder.SWTBotGefTestCase;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestDebugFeature extends SWTBotGefTestCase {
	
	@Test
	public void testSimpleDebug() throws Exception{
		SWTBotTestUtil.createNewDiagram(bot);
		launchDebugWizard();
		bot.button(Messages.DebugProcessButtonLabel).click();
		checkNbOfProcDefInEngine("Simple debug is not working", getNBProcessDefinitions() +1);
	}

	@Test
	public void testDebugWithConnectorOnCallActivity() throws Exception{
		SWTBotTestUtil.importProcessWIthPathFromClass(bot, "DiagramToTestDebugWitghCallActivityWithConnector-1.0.bos", "Bonita 6.x", "DiagramToTestDebugWitghCallActivityWithConnector", TestDebugFeature.class, false);
		launchDebugWizard();
		bot.button(Messages.DebugProcessButtonLabel).click();
		checkNbOfProcDefInEngine("Debug with Connector on call activity is not working", getNBProcessDefinitions() +1);
	}
	
	private long getNBProcessDefinitions() throws Exception{
        APISession session = null;
        long nbProc = 0;
        try{
            session = BOSEngineManager.getInstance().loginDefaultTenant(new NullProgressMonitor()) ;
            final ProcessManagementAPI processAPI = BOSEngineManager.getInstance().getProcessAPI(session) ;
            nbProc = processAPI.getNumberOfProcessDeploymentInfos();
        }finally{
            if(session != null){
                BOSEngineManager.getInstance().logoutDefaultTenant(session);
            }
        }

        return nbProc;
    }
	
	private void checkNbOfProcDefInEngine(final String failureMessage,
			final long nbProcDefToCheck) {
		bot.waitUntil(new ICondition() {

            public boolean test() throws Exception {
                return getNBProcessDefinitions() == nbProcDefToCheck ;
            }

            public void init(SWTBot bot) {
            }

            public String getFailureMessage() {
                
				return failureMessage;
            }
        },10000,500);
	}
	
	private void launchDebugWizard() {
		final Runnable runnable = new Runnable() {
			
			public void run() {
				try {
					new DebugProcessCommand().execute(null);
				} catch (ExecutionException e) {
					BonitaStudioLog.error(e);
				}
				
			}
		};
		Display.getDefault().asyncExec(runnable);
		
		bot.waitUntil(Conditions.shellIsActive(Messages.debugProcessWizardtitle));
	}
	
}
