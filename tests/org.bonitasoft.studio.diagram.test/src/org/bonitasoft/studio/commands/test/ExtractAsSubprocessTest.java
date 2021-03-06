/**
 * Copyright (C) 2010 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.commands.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.gmf.tools.GMFTools;
import org.bonitasoft.studio.model.process.Lane;
import org.bonitasoft.studio.model.process.MainProcess;
import org.bonitasoft.studio.model.process.Pool;
import org.bonitasoft.studio.test.swtbot.util.SWTBotTestUtil;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.SWTBotGefTestCase;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Mickael Istria
 *
 */
public class ExtractAsSubprocessTest extends SWTBotGefTestCase {
    /**
     * @author Mickael Istria
     *
     */
    public class OneMoreEditor implements ICondition {

        private final SWTGefBot bot;
        private final int size;

        /**
         * @param bot
         * @param size
         */
        public OneMoreEditor(SWTGefBot bot, int size) {
            this.bot = bot;
            this.size = size;
        }

        /* (non-Javadoc)
         * @see org.eclipse.swtbot.swt.finder.waits.ICondition#test()
         */
        public boolean test() throws Exception {
            if (bot.activeShell().getText().toLowerCase().startsWith("overwrite")) {
                bot.button("OK").click();
            }
            return bot.editors().size() > size;
        }

        /* (non-Javadoc)
         * @see org.eclipse.swtbot.swt.finder.waits.ICondition#init(org.eclipse.swtbot.swt.finder.SWTBot)
         */
        public void init(SWTBot bot) {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see org.eclipse.swtbot.swt.finder.waits.ICondition#getFailureMessage()
         */
        public String getFailureMessage() {
            return "No new editor opened";
        }

    }

    @Override
    @Before
    public void setUp() {
        bot.closeAllEditors();
    }

    @Override
    @After
    public void tearDown() {
        bot.activeEditor().saveAndClose();
        bot.closeAllEditors();
    }


    @Test
    public void testExtractAsSubprocess() throws Exception {
        SWTBotTestUtil.createNewDiagram(bot);
        SWTBotGefEditor editor1 = bot.gefEditor(bot.activeEditor().getTitle());
        SWTBotGefEditPart stepPart = editor1.getEditPart("Step1").parent();
        SWTBotGefEditPart startPart = editor1.getEditPart("Start1").parent();
        editor1.select(stepPart, startPart);
        final SWTBotGefEditPart poolPart = stepPart.parent(/*Compartment*/).parent().parent().parent();
        editor1.clickContextMenu("Extract subprocess");
        editor1.select(poolPart);
        Pool pool = (Pool) ((IGraphicalEditPart)poolPart.part()).resolveSemanticElement();
        assertEquals("Not same number of nodes in main as expected", 1, pool.getElements().size());
        assertEquals("Not same number of transitions in main as expected", 0, pool.getConnections().size());
        Pool subprocessPool = (Pool) ((MainProcess)pool.eContainer()).getElements().get(1);
        assertEquals("Not same number of nodes as expected", 2, subprocessPool.getElements().size());
        assertEquals("Not same number of transitions as expected", 1, subprocessPool.getConnections().size());
    }


    @Test
    public void testExtractAsSubprocessFromLane() throws Exception {
        // Test Bug 3100
        SWTBotTestUtil.createNewDiagram(bot);
        SWTBotGefEditor editor1 = bot.gefEditor(bot.activeEditor().getTitle());
        editor1.activateTool("Lane");
        editor1.click(100, 100);
        SWTBotGefEditPart stepPart = editor1.getEditPart("Step1").parent();
        SWTBotGefEditPart startPart = editor1.getEditPart("Start1").parent();
        editor1.select(stepPart, startPart);
        final SWTBotGefEditPart lanePart = stepPart.parent(/*Compartment*/).parent();
        editor1.clickContextMenu("Extract subprocess");
        editor1.select(lanePart);
        Lane lane = (Lane) ((IGraphicalEditPart)lanePart.part()).resolveSemanticElement();
        Pool pool = (Pool) lane.eContainer();
        assertEquals("Not same number of nodes in main as expected", 1, lane.getElements().size());
        assertEquals("Not same number of transitions in main as expected", 0, pool.getConnections().size());
        Pool subprocessPool = (Pool) ((MainProcess)pool.eContainer()).getElements().get(1);
        assertEquals("Not same number of nodes as expected", 2, subprocessPool.getElements().size());
        assertEquals("Not same number of transitions as expected", 1, subprocessPool.getConnections().size());
    }

    @Test
    public void testAddMissingConnectionsAndBoundaries() throws Exception {
        importProcess();

        SWTBotGefEditor editor1 = bot.gefEditor(bot.activeEditor().getTitle());
        IGraphicalEditPart step1Part = (IGraphicalEditPart) editor1.getEditPart("Step1").parent().part();
        IGraphicalEditPart step2Part = (IGraphicalEditPart) editor1.getEditPart("Step2").parent().part();
        List<IGraphicalEditPart> list = new ArrayList<IGraphicalEditPart>();
        list.add(step2Part);
        list.add(step1Part);
        assertEquals("Util method does not work", 5, GMFTools.addMissingConnectionsAndBoundaries(list).size());
    }


    @Test
    public void testExtractSubprocessWithBoundary() throws Exception {
        // Test bug 3102
        importProcess();

        SWTBotGefEditor editor1 = bot.gefEditor(bot.activeEditor().getTitle());
        SWTBotGefEditPart step1Part = editor1.getEditPart("Step1").parent();
        SWTBotGefEditPart step2Part = editor1.getEditPart("Step2").parent();
        SWTBotGefEditPart error1Part = editor1.getEditPart("Error1").parent();
        editor1.select(step1Part, step2Part, error1Part);
        final SWTBotGefEditPart poolPart = step1Part.parent().parent();
        editor1.clickContextMenu("Extract subprocess");

        Lane lane = (Lane) ((IGraphicalEditPart)poolPart.part()).resolveSemanticElement();
        assertEquals("Not same number of nodes in main as expected", 2, lane.getElements().size());
        assertEquals("Not same number of transitions in main as expected", 1, ((Pool)lane.eContainer()).getConnections().size());
        Pool subprocessPool = (Pool) ModelHelper.getMainProcess(lane).getElements().get(1);
        assertEquals("Not same number of nodes as expected", 2, subprocessPool.getElements().size());
        assertEquals("Not same number of transitions as expected", 2, subprocessPool.getConnections().size());
    }

    @Test
    public void testExtractSubprocessWithBoundary2() throws Exception {
        // Test bug 3102, selecting only the 2 steps, and not the boundary
        importProcess();

        SWTBotGefEditor editor1 = bot.gefEditor(bot.activeEditor().getTitle());
        SWTBotGefEditPart step1Part = editor1.getEditPart("Step1").parent();
        SWTBotGefEditPart step2Part = editor1.getEditPart("Step2").parent();
        editor1.select(step1Part, step2Part);
        final SWTBotGefEditPart poolPart = step1Part.parent().parent();
        editor1.clickContextMenu("Extract subprocess");

        Lane lane = (Lane) ((IGraphicalEditPart)poolPart.part()).resolveSemanticElement();

        assertEquals("Not same number of nodes in main as expected", 2, lane.getElements().size());
        assertEquals("Not same number of transitions in main as expected", 1,((Pool)lane.eContainer()).getConnections().size());
        Pool subprocessPool = (Pool) ModelHelper.getMainProcess(lane).getElements().get(1);
        assertEquals("Not same number of nodes as expected", 2, subprocessPool.getElements().size());
        assertEquals("Not same number of transitions as expected", 2, subprocessPool.getConnections().size());
    }

    /**
     * @throws IOException
     */
    public void importProcess() throws IOException {
        ICondition newEditorCond = new OneMoreEditor(bot, bot.editors().size());
        SWTBotTestUtil.importProcessWIthPathFromClass(bot, "BoundaryProcess_1_0.bos", "Bonita 6.x", "BoundaryProcess", this.getClass(), false);
        bot.waitUntil(newEditorCond);
    }

}
