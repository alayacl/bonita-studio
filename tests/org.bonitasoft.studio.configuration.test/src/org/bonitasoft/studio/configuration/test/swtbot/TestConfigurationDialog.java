/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.configuration.test.swtbot;

import org.bonitasoft.studio.model.process.MainProcess;
import org.bonitasoft.studio.model.process.Pool;
import org.bonitasoft.studio.test.swtbot.util.SWTBotTestUtil;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.gef.finder.SWTBotGefTestCase;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Romain Bioteau
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TestConfigurationDialog extends SWTBotGefTestCase {

	
	final String defaultEmployeeActor = "Employee actor";
	
    @Test
    public void testOpenDialog(){
        SWTBotTestUtil.createNewDiagram(bot);

        SWTBotEditor botEditor = bot.activeEditor();
        SWTBotGefEditor gmfEditor = bot.gefEditor(botEditor.getTitle());

        IGraphicalEditPart part = (IGraphicalEditPart)gmfEditor.mainEditPart().part();
        MainProcess model = (MainProcess)part.resolveSemanticElement();
        Pool pool = (Pool)model.getElements().get(0);
        String processLabel = pool.getName() +" ("+pool.getVersion()+")";
        if(SWTBotTestUtil.testingBosSp()){
            bot.toolbarDropDownButton("Configure").click();
        }else{
            bot.toolbarButton("Configure").click();
        }
        bot.waitUntil(Conditions.shellIsActive("Local configuration for "+processLabel));


        if(SWTBotTestUtil.testingBosSp()){
            bot.table().getTableItem("Parameter").select();
        }
        bot.table().getTableItem("Actor mapping").select();

        bot.button(IDialogConstants.FINISH_LABEL).click();
    }

    @Test
    public void testAdvancedCheckbox(){
        SWTBotTestUtil.createNewDiagram(bot);

        SWTBotEditor botEditor = bot.activeEditor();
        SWTBotGefEditor gmfEditor = bot.gefEditor(botEditor.getTitle());

        IGraphicalEditPart part = (IGraphicalEditPart)gmfEditor.mainEditPart().part();
        MainProcess model = (MainProcess)part.resolveSemanticElement();
        Pool pool = (Pool)model.getElements().get(0);
        String processLabel = pool.getName() +" ("+pool.getVersion()+")";
        if(SWTBotTestUtil.testingBosSp()){
            bot.toolbarDropDownButton("Configure").click();
        }else{
            bot.toolbarButton("Configure").click();
        }
        bot.waitUntil(Conditions.shellIsActive("Local configuration for "+processLabel));
        if(bot.checkBox().isChecked()){
            bot.checkBox().click();
        }
        boolean notFound = false ;
        try{
            bot.table().getTableItem("Process dependencies").select();
        }catch (WidgetNotFoundException e) {
            notFound = true ;
        }
        assertTrue("Proces dependencies menu should not be visible", notFound);


        if(!bot.checkBox().isChecked()){
        	bot.checkBox().click();
        }
        bot.table().getTableItem("Process dependencies").select();

        bot.button(IDialogConstants.FINISH_LABEL).click();

        //VALIDATE CHECKBOX STATE RESTORED
        if(SWTBotTestUtil.testingBosSp()){
            bot.toolbarDropDownButton("Configure").click();
        }else{
            bot.toolbarButton("Configure").click();
        }
        bot.waitUntil(Conditions.shellIsActive("Local configuration for "+processLabel));
        assertTrue("Advanced checbox should be ckecked",bot.checkBox().isChecked());
        bot.button(IDialogConstants.FINISH_LABEL).click();
    }

    @Test
    public void testMappingOfActors(){
        SWTBotTestUtil.createNewDiagram(bot);

        SWTBotEditor botEditor = bot.activeEditor();
        SWTBotGefEditor gmfEditor = bot.gefEditor(botEditor.getTitle());

        IGraphicalEditPart part = (IGraphicalEditPart)gmfEditor.mainEditPart().part();
        MainProcess model = (MainProcess)part.resolveSemanticElement();
        Pool pool = (Pool)model.getElements().get(0);
        String processLabel = pool.getName() +" ("+pool.getVersion()+")";
        if(SWTBotTestUtil.testingBosSp()){
            bot.toolbarDropDownButton("Configure").click();
        }else{
            bot.toolbarButton("Configure").click();
        }
        bot.waitUntil(Conditions.shellIsActive("Local configuration for "+processLabel));
        bot.table().getTableItem("Actor mapping").select();

        //Remove default mapping
        bot.tree().getTreeItem(defaultEmployeeActor).select();
        bot.button("Groups...").click();
        bot.table().getTableItem(0).uncheck();
        bot.button(IDialogConstants.FINISH_LABEL).click();

        //Map to a group
        bot.tree().getTreeItem(defaultEmployeeActor+" -- Not mapped").select();
        bot.button("Groups...").click();
        bot.table().getTableItem(0).check();
        bot.button(IDialogConstants.FINISH_LABEL).click();


        //Map to a role
        bot.tree().getTreeItem(defaultEmployeeActor).select();
        bot.button("Roles...").click();
        bot.table().getTableItem(0).check();
        bot.button(IDialogConstants.FINISH_LABEL).click();


        //Map to a user
        bot.tree().getTreeItem(defaultEmployeeActor).select();
        bot.button("Users...").click();
        bot.table().getTableItem(0).check();
        bot.button(IDialogConstants.FINISH_LABEL).click();


        //Map to a membership
        bot.tree().getTreeItem(defaultEmployeeActor).select();
        bot.button("Memberships...").click();
        bot.button("Add membership...").click();
        assertFalse("Finish button should be disabled",bot.button(IDialogConstants.FINISH_LABEL).isEnabled());
        bot.comboBoxWithLabel("Group").setSelection(1);
        assertFalse("Finish button should be disabled",bot.button(IDialogConstants.FINISH_LABEL).isEnabled());
        bot.comboBoxWithLabel("Role").setSelection(1);
        assertTrue("Finish button should be enabled",bot.button(IDialogConstants.FINISH_LABEL).isEnabled());
        bot.button(IDialogConstants.FINISH_LABEL).click();


        bot.tree().getTreeItem(defaultEmployeeActor).getNode("Groups").expand();
        bot.tree().getTreeItem(defaultEmployeeActor).getNode("Roles").expand();
        bot.tree().getTreeItem(defaultEmployeeActor).getNode("Users").expand();
        bot.tree().getTreeItem(defaultEmployeeActor).getNode("Membership").expand();

        bot.button(IDialogConstants.FINISH_LABEL).click();

        if(SWTBotTestUtil.testingBosSp()){
            bot.toolbarDropDownButton("Configure").click();
        }else{
            bot.toolbarButton("Configure").click();
        }
        bot.waitUntil(Conditions.shellIsActive("Local configuration for "+processLabel));
        bot.table().select("Actor mapping");
        bot.tree().getTreeItem(defaultEmployeeActor).getNode("Groups").expand();
        bot.tree().getTreeItem(defaultEmployeeActor).getNode("Roles").expand();
        bot.tree().getTreeItem(defaultEmployeeActor).getNode("Users").expand();
        bot.tree().getTreeItem(defaultEmployeeActor).getNode("Membership").expand();

        bot.button(IDialogConstants.FINISH_LABEL).click();
    }




}
