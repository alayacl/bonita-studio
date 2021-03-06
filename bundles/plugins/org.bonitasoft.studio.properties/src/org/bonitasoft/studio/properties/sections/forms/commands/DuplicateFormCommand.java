/**
 * Copyright (C) 2009 BonitaSoft S.A.
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
package org.bonitasoft.studio.properties.sections.forms.commands;

import java.util.List;

import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.gmf.tools.CopyEObjectFeaturesCommand;
import org.bonitasoft.studio.model.form.Form;
import org.bonitasoft.studio.model.form.FormFactory;
import org.bonitasoft.studio.model.form.FormPackage;
import org.bonitasoft.studio.model.form.ViewForm;
import org.bonitasoft.studio.model.process.Element;
import org.bonitasoft.studio.properties.sections.forms.FormsUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;

/**
 * 
 * 	allow to duplicate an existing Form
 * 
 * @author Baptiste Mesta
 * @author Aurelien Pupier - use AbstractTransactionalCommand to avoid memory leaks (instead of Command)
 */
public class DuplicateFormCommand  extends AbstractTransactionalCommand {

    private final Element pageFlow;
    private final Form baseForm;
    private final String formName;
    private final String formDesc;
    private final EStructuralFeature feature;

    public DuplicateFormCommand(Element pageFlow2, EStructuralFeature feature, Form baseForm,String formName, String id, String formDesc, TransactionalEditingDomain editingDomain) {
        super(editingDomain, "Duplicate form", getWorkspaceFiles(pageFlow2));
        pageFlow = pageFlow2;
        this.baseForm = baseForm;
        this.formName = id;
        this.formDesc = formDesc;
        this.feature = feature;
    }


    @Override
    protected CommandResult doExecuteWithResult(IProgressMonitor monitor,
            IAdaptable info) throws ExecutionException {

        Form form;
        if(baseForm instanceof ViewForm){
            if(feature.getEType().equals(FormPackage.Literals.VIEW_FORM)){
                //nothing to convert
                form = EcoreUtil.copy(baseForm);
            }else{
                form = FormFactory.eINSTANCE.createForm();
                CopyEObjectFeaturesCommand.copyFeatures(EcoreUtil.copy(baseForm), form);
            }
        }else{
            if(feature.getEType().equals(FormPackage.Literals.FORM)){
                //nothing to convert
                form = EcoreUtil.copy(baseForm);
            }else{
                form = FormFactory.eINSTANCE.createViewForm();
                CopyEObjectFeaturesCommand.copyFeatures(EcoreUtil.copy(baseForm), form);
            }
        }

        form.setName(formName);
        form.setDocumentation(formDesc);
        ((List) pageFlow.eGet(feature)).add(form);
        //remove data out of the scope
        ModelHelper.removedReferencedEObjects(form);

        FormsUtils.createDiagram(form, getEditingDomain(), pageFlow);
        FormsUtils.openDiagram(form,getEditingDomain());
        return CommandResult.newOKCommandResult(form);
    }

}
