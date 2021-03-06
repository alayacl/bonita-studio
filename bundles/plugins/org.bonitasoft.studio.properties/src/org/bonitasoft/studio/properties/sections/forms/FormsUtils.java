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
package org.bonitasoft.studio.properties.sections.forms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.studio.common.FileUtil;
import org.bonitasoft.studio.common.NamingUtils;
import org.bonitasoft.studio.common.ProjectUtil;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.jface.BonitaErrorDialog;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.diagram.custom.repository.DiagramRepositoryStore;
import org.bonitasoft.studio.diagram.custom.repository.WebTemplatesUtil;
import org.bonitasoft.studio.exporter.ExporterService;
import org.bonitasoft.studio.exporter.ExporterService.SERVICE_TYPE;
import org.bonitasoft.studio.exporter.application.HtmlTemplateGenerator;
import org.bonitasoft.studio.model.form.Form;
import org.bonitasoft.studio.model.form.FormPackage;
import org.bonitasoft.studio.model.form.Widget;
import org.bonitasoft.studio.model.process.Element;
import org.bonitasoft.studio.model.process.MainProcess;
import org.bonitasoft.studio.model.process.diagram.form.edit.parts.FormEditPart;
import org.bonitasoft.studio.model.process.diagram.form.part.FormDiagramEditor;
import org.bonitasoft.studio.model.process.diagram.form.part.FormDiagramEditorPlugin;
import org.bonitasoft.studio.properties.i18n.Messages;
import org.bonitasoft.studio.properties.sections.forms.commands.AddFormCommand;
import org.bonitasoft.studio.properties.sections.forms.commands.DuplicateFormCommand;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.ui.services.editor.EditorService;
import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * 
 * @author Charles Souillard
 * @author Aurelien Pupier
 * @author Baptiste Mesta
 */
public class FormsUtils {

    protected static final String TMP_DIR = ProjectUtil.getBonitaStudioWorkFolder().getAbsolutePath();

    private static final class WidgetAddedOrRemoved extends AdapterImpl {
        private final Form form;

        /**
         * 
         */
        public WidgetAddedOrRemoved(Form form) {
            this.form = form;
        }

        @Override
        public void notifyChanged(Notification notification) {
            // Listen for changes to features.
            switch (notification.getFeatureID(Form.class)) {
                case FormPackage.FORM__WIDGETS:
                    if (notification.getEventType() == Notification.ADD) {
                        final Widget widget = ((Widget) (notification.getNewValue()));
                        if (ModelHelper.formIsCustomized(form)) {
                            // there is a template

                            HtmlTemplateGenerator generator = ((HtmlTemplateGenerator) ExporterService.getInstance().getExporterService(SERVICE_TYPE.HtmlTemplateGenerator));

                            File file = WebTemplatesUtil.getFile(form.getHtmlTemplate().getPath());
                            FileInputStream fis;
                            try {
                                fis = new FileInputStream(file);
                                File tempFile = File.createTempFile("tempForm", ".html");
                                FileWriter fileWriter = new FileWriter(tempFile);
                                String label = NamingUtils.getDefaultNameFor(widget);
                                label = NamingUtils.convertToId(label);
                                int number = NamingUtils.getMaxElements(form, label);
                                number++;
                                label += number;
                                generator.addDivInTemplate(label, fis, fileWriter);
                                fis.close();
                                fileWriter.close();
                                FileUtil.copy(tempFile, file);
                                WebTemplatesUtil.refreshFile(form.getHtmlTemplate().getPath());
                            } catch (final Exception e) {
                                BonitaStudioLog.error(e);
                                Display.getDefault().syncExec(new Runnable() {
                                    @Override
                                    public void run() {
                                        new BonitaErrorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.Error, "Unexpected error", e).open();
                                    }
                                });
                            }
                        }
                    } else if (notification.getEventType() == Notification.REMOVE) {
                        if (form.getHtmlTemplate() != null && form.getHtmlTemplate().getPath() != null && !form.getHtmlTemplate().getPath().isEmpty()) {
                            // there is a template
                            Display.getDefault().syncExec(new Runnable() {
                                @Override
                                public void run() {
                                    MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.widgetRemovedWarning_title, Messages.widgetRemovedWarning_msg);
                                }
                            });
                        }

                    }
            }
        }


    }

    public static enum WidgetEnum {
        TEXT, TEXT_AREA, COMBO, CHECKBOX,CHECKBOX_LIST, DATE, LIST, PASSWORD, RADIO, SELECT, FILE
    };

    /**
     * add a form to the current PageFlow
     * 
     * @param feature
     * 
     * @param string
     * 
     * @param form
     *            the form to add to the current pageFlow
     */
    public static void addForm(Element pageFlow, TransactionalEditingDomain editingDomain, EStructuralFeature feature, String formName, String description,
            Map<Element, WidgetEnum> vars) {
        try {
            OperationHistoryFactory.getOperationHistory().execute(new AddFormCommand(pageFlow, feature, formName, description, vars, editingDomain),
                    new NullProgressMonitor(), null);
        } catch (ExecutionException e) {
            BonitaStudioLog.error(e);
        }
    }

    public static void duplicateForm(Element pageFlow, TransactionalEditingDomain editingDomain, EStructuralFeature feature, Form baseForm, String formName,
            String id, String formDesc) {
        try {
            OperationHistoryFactory.getOperationHistory().execute(new DuplicateFormCommand(pageFlow, feature, baseForm, formName, id, formDesc, editingDomain),
                    new NullProgressMonitor(), null);
        } catch (ExecutionException e) {
            BonitaStudioLog.error(e);
        }
    }

    /**
     * create the diagram of the form and put it in the same resource file
     * 
     * @param form
     *            create the diagram corresponding to this form
     * @return created diagram
     */
    public static void createDiagram(final Form form, final TransactionalEditingDomain editingDomain, final Element pageFlow) {
        List<IFile> list = new ArrayList<IFile>();
        for (Resource resource : editingDomain.getResourceSet().getResources()) {
            if (resource.getURI().isPlatform()) {
                list.add(ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(resource.getURI().toPlatformString(true))));
            } else {
                list.add(ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(resource.getURI().toFileString())));
            }
        }

        AbstractTransactionalCommand command = new AbstractTransactionalCommand(editingDomain, "", list) { //$NON-NLS-1$

            @Override
            protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                final Diagram diagram = ViewService.createDiagram(form, FormEditPart.MODEL_ID, FormDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
                final Resource resource =  getEditingDomain().getResourceSet().getResource(form.eResource().getURI(), false);
                resource.getContents().add(diagram);
                diagram.persist();
                diagram.setElement(form);
                return CommandResult.newOKCommandResult();
            }

        };

        try {
            OperationHistoryFactory.getOperationHistory().execute(command, null, null);
        } catch (ExecutionException e) {
            FormDiagramEditorPlugin.getInstance().logError("Unable to create model and diagram", e); //$NON-NLS-1$
        }
    }

    /**
     * open the diagram corresponding to the form
     * 
     * @param form
     *            the form to open
     */
    public static DiagramEditor openDiagram(Form form,EditingDomain domain) {

        /* get the Diagram element related to the form in the resource */
        Diagram diag = ModelHelper.getDiagramFor(form,domain);

        /*
         * need to get the URI after save because the name can change as it is
         * synchronized with the MainProcess name
         */
        URI uri = EcoreUtil.getURI(diag);

        /* open the form editor */
        FormDiagramEditor formEditor = (FormDiagramEditor) EditorService.getInstance().openEditor(new URIEditorInput(uri, form.getName()));
        // form.eAdapters().add(new EObjectAdapter(form))
        EList<Adapter> eAdapters = form.eAdapters();
        boolean alreadyHere = false;
        for (Adapter adapter : eAdapters) {
            if(adapter instanceof WidgetAddedOrRemoved){
                alreadyHere = true;
                break;
            }
        }
        if(!alreadyHere){
            WidgetAddedOrRemoved adapterImpl = new WidgetAddedOrRemoved(form);
            eAdapters.add(adapterImpl);
        }
        MainProcess mainProcess = ModelHelper.getMainProcess(form);

        DiagramRepositoryStore diagramStore = (DiagramRepositoryStore) RepositoryManager.getInstance().getCurrentRepository().getRepositoryStore(DiagramRepositoryStore.class) ;
        IRepositoryFileStore file = diagramStore.getChild(NamingUtils.toDiagramFilename(mainProcess)) ;
        if (file.isReadOnly()) {
            formEditor.getDiagramEditPart().disableEditMode();
        }
        return formEditor;
    }

    /**
     * @param form
     * @param srcName
     * @param name
     */
    public static void changeIdInTemplate(Form form, String oldId, String newId) {
        HtmlTemplateGenerator generator = ((HtmlTemplateGenerator) ExporterService.getInstance().getExporterService(SERVICE_TYPE.HtmlTemplateGenerator));
        File file = WebTemplatesUtil.getFile(form.getHtmlTemplate().getPath());
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
            File tempFile = File.createTempFile("tempForm", ".html");
            FileWriter fileWriter = new FileWriter(tempFile);
            generator.changeDivId(oldId, newId, fis, fileWriter);
            fis.close();
            fileWriter.close();
            FileUtil.copy(tempFile, file);
            WebTemplatesUtil.refreshFile(form.getHtmlTemplate().getPath());
        } catch (final Exception e) {
            BonitaStudioLog.error(e);
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    new BonitaErrorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.Error, "Unexpected error", e).open();
                }
            });
        }
    }



}
