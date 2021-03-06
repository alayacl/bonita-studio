/**
 * Copyright (C) 2009 BonitaSoft S.A.
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
package org.bonitasoft.studio.properties.form.sections.general.contributions;

import java.lang.reflect.InvocationTargetException;

import org.bonitasoft.studio.common.NamingUtils;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.jface.OpenNameDialog;
import org.bonitasoft.studio.common.jface.databinding.validator.InputLengthValidator;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.properties.AbstractNamePropertySectionContribution;
import org.bonitasoft.studio.common.properties.ExtensibleGridPropertySection;
import org.bonitasoft.studio.form.properties.i18n.Messages;
import org.bonitasoft.studio.model.form.Form;
import org.bonitasoft.studio.model.form.Widget;
import org.bonitasoft.studio.model.process.Element;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.model.process.diagram.form.part.FormDiagramEditor;
import org.bonitasoft.studio.properties.sections.forms.FormsUtils;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.edit.EMFEditObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * @author Mickael Istria
 * @author Baptiste Mesta
 * @author Aurelien Pupier - refactored, use Abstract class
 * @author Bioteau Romain - migration to Databinding
 */
public class NameGridPropertySectionContribution extends AbstractNamePropertySectionContribution {

    /**
     * @param tabbedPropertySheetPage
     * @param extensibleGridPropertySection
     */
    public NameGridPropertySectionContribution(TabbedPropertySheetPage tabbedPropertySheetPage,
            ExtensibleGridPropertySection extensibleGridPropertySection) {
        super(tabbedPropertySheetPage, extensibleGridPropertySection);
    }



    @Override
    protected void createBinding(EMFDataBindingContext context) {


        Converter convertToId = new Converter(String.class,String.class) {

            public Object convert(final Object fromObject) {
                updatePropertyTabTitle();
                /*Update the tab of the editor if the form name change*/
                if(element instanceof Form){
                    FormDiagramEditor editor = (FormDiagramEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
                    editor.setPartName(text.getText());
                }
                if(!fromObject.toString().equals(element.getName()) && element instanceof Widget){
                    IProgressService service = PlatformUI.getWorkbench().getProgressService();
                    try {
                        service.busyCursorWhile(new RefactorWidgetOperation((Widget)element,fromObject.toString()));
                    } catch (InvocationTargetException e) {
                        BonitaStudioLog.error(e);
                    } catch (InterruptedException e) {
                        BonitaStudioLog.error(e);
                    }
                }
                return fromObject;
            }
        };

        UpdateValueStrategy labelTargetToModelUpdate = new UpdateValueStrategy();
        labelTargetToModelUpdate.setConverter(convertToId);
        labelTargetToModelUpdate.setAfterGetValidator(new IValidator() {

            public IStatus validate(Object value) {
                return JavaConventions.validateFieldName(value.toString(), JavaCore.VERSION_1_6, JavaCore.VERSION_1_6);
            }
        }) ;
        labelTargetToModelUpdate.setBeforeSetValidator(new InputLengthValidator(Messages.name, 50)) ;
        ISWTObservableValue observable = SWTObservables.observeDelayedValue(400, SWTObservables.observeText(text, SWT.Modify));


        ControlDecorationSupport.create(context.bindValue(observable, EMFEditObservables.observeValue(editingDomain, element, ProcessPackage.Literals.ELEMENT__NAME),labelTargetToModelUpdate,null),SWT.LEFT);
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.properties.sections.general.IExtenstibleGridPropertySectionContribution#getLabel()
     */
    public String getLabel() {
        return Messages.GeneralSection_Name;
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.properties.sections.general.IExtenstibleGridPropertySectionContribution#refresh()
     */
    @Override
    public void refresh() {

    }


    /* (non-Javadoc)
     * @see org.bonitasoft.studio.properties.sections.general.IExtenstibleGridPropertySectionContribution#setEObject(org.eclipse.emf.ecore.EObject)
     */
    public void setEObject(EObject object) {
        element = (Element)object;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.bonitasoft.studio.properties.sections.general.
     * IExtenstibleGridPropertySectionContribution
     * #setSelection(org.eclipse.jface.viewers.ISelection)
     */
    public void setSelection(ISelection selection) {
        this.selection = selection;
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.common.properties.AbstractNamePropertySectionContribution#editProcessNameAndVersion()
     */
    @Override
    protected void editProcessNameAndVersion() {
        // change the id of the form
        if (element instanceof Widget && element.eContainer() instanceof Form && ModelHelper.formIsCustomized((Form) element.eContainer())) {
            Form form = (Form) element.eContainer();
            OpenNameDialog dialog = new OpenNameDialog(Display.getDefault().getActiveShell(), element.getName());
            if (dialog.open() == Dialog.OK) {
                // TODO check that the id does not already exists
                String srcName = dialog.getSrcName();
                String name = dialog.getNewName();
                CompoundCommand cc = new CompoundCommand();
                cc.append(SetCommand.create(editingDomain, element, ProcessPackage.eINSTANCE.getElement_Name(), NamingUtils.convertToId(name)));
                editingDomain.getCommandStack().execute(cc);
                // change the template
                FormsUtils.changeIdInTemplate(form, srcName, name);
            }
        }
    }
}
