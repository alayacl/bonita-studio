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
package org.bonitasoft.studio.connectors.ui.wizard.page;

import java.io.File;
import java.util.List;

import org.bonitasoft.studio.common.NamingUtils;
import org.bonitasoft.studio.common.jface.databinding.validator.EmptyInputValidator;
import org.bonitasoft.studio.connector.model.definition.ConnectorDefinition;
import org.bonitasoft.studio.connector.model.definition.IDefinitionRepositoryStore;
import org.bonitasoft.studio.connector.model.implementation.ConnectorImplementation;
import org.bonitasoft.studio.connectors.ConnectorPlugin;
import org.bonitasoft.studio.connectors.i18n.Messages;
import org.bonitasoft.studio.model.process.Connector;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class ExportConnectorWizardPage extends WizardPage implements ISelectionChangedListener,IDoubleClickListener {

    protected Connector connector;
    private EMFDataBindingContext context;
    private WizardPageSupport pageSupport;
    private ConnectorImplementation selectedImplementation;
    private Button removeButton;
    private String destFilePath ;
    private boolean includeSources =true ;
    private boolean addDependencies = true ;
    private final IContentProvider contentProvider;
    private final LabelProvider labelProvider;
    private final IDefinitionRepositoryStore defStore;
    private final List<ConnectorDefinition> definitions;

    public ExportConnectorWizardPage(String pageTitle,String pageDesc,IContentProvider contentProvider,LabelProvider labelProvider,IDefinitionRepositoryStore defStore) {
        super(ExportConnectorWizardPage.class.getName());
        setTitle(pageTitle);
        setDescription(pageDesc);
        this.contentProvider = contentProvider ;
        this.labelProvider = labelProvider ;
        this.defStore=defStore;
        definitions = defStore.getDefinitions();
    }


    @Override
    public void createControl(Composite parent) {
        context = new EMFDataBindingContext() ;
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).extendedMargins(10, 10, 10, 0).create());
        composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

        final Text serachBox = new Text(composite, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
        serachBox.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(3, 1).create());
        serachBox.setMessage(Messages.search);

        final TableViewer tableViewer = new TableViewer(composite, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        tableViewer.getTable().setLayoutData(GridDataFactory.fillDefaults().grab(true,true).span(3, 1).hint(SWT.DEFAULT, 280).create()) ;
        tableViewer.setContentProvider(contentProvider);
        tableViewer.setLabelProvider(labelProvider);
        tableViewer.addSelectionChangedListener(this) ;
        tableViewer.addDoubleClickListener(this) ;
        tableViewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer arg0, Object parentElement, Object element) {
                String search = serachBox.getText();
                return search == null || search.isEmpty() || labelProvider.getText(element).toLowerCase().contains(search.toLowerCase()) ;
            }
        });
        tableViewer.setInput(new Object());
        tableViewer.getTable().setFocus();
        serachBox.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                tableViewer.refresh();
            }
        });

        IValidator selectionValidator = new IValidator() {
            @Override
            public IStatus validate(Object value) {
                if(value == null){
                    return new Status(IStatus.ERROR,ConnectorPlugin.PLUGIN_ID, Messages.selectAConnectorImplWarning);
                }
                if(value instanceof ConnectorImplementation){
                    String id = ((ConnectorImplementation) value).getDefinitionId() ;
                    String version = ((ConnectorImplementation) value).getDefinitionVersion() ;
                    if(id == null || id.isEmpty()){
                        return new Status(IStatus.ERROR,ConnectorPlugin.PLUGIN_ID, Messages.selectAValidConnectorImplWarning);
                    }
                    if(version == null || version.isEmpty()){
                        return new Status(IStatus.ERROR,ConnectorPlugin.PLUGIN_ID, Messages.selectAValidConnectorImplWarning);
                    }
                    if( defStore.getDefinition(id, version,definitions) == null){
                        return new Status(IStatus.ERROR,ConnectorPlugin.PLUGIN_ID, Messages.selectAValidConnectorImplWarning);
                    }

                    if(((ConnectorImplementation) value).getImplementationClassname() == null || ((ConnectorImplementation) value).getImplementationClassname().isEmpty()){
                        return new Status(IStatus.ERROR,ConnectorPlugin.PLUGIN_ID, Messages.selectAValidConnectorImplWarning);
                    }
                }
                return Status.OK_STATUS;
            }
        } ;

        UpdateValueStrategy selectionStrategy = new UpdateValueStrategy() ;
        selectionStrategy.setBeforeSetValidator(selectionValidator) ;



        final Label inculeSourcesLabel = new Label(composite, SWT.NONE);
        inculeSourcesLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).create()) ;
        inculeSourcesLabel.setText(Messages.inculeSourcesLabel);

        final Button includeSourceCheckbox = new Button(composite,SWT.CHECK) ;
        includeSourceCheckbox.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create()) ;



        final Label addDependenciesLabel = new Label(composite, SWT.NONE);
        addDependenciesLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).create()) ;
        addDependenciesLabel.setText(Messages.addDependencies);

        final Button addDependenciesCheckbox = new Button(composite,SWT.CHECK) ;
        addDependenciesCheckbox.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create()) ;

        final Label destFileLabel = new Label(composite, SWT.NONE);
        destFileLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).create()) ;
        destFileLabel.setText(Messages.destinationLabel+" *");

        final Text destFileText = new Text(composite, SWT.BORDER);
        destFileText.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true,false).create());

        Button destFileButton = new Button(composite, SWT.PUSH);
        destFileButton.setLayoutData(GridDataFactory.fillDefaults().hint(85, SWT.DEFAULT).create()) ;
        destFileButton.setText(Messages.browsePackages);
        destFileButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ConnectorImplementation impl = (ConnectorImplementation) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement() ;
                String defaultName = "" ;
                if(impl != null){
                    defaultName = NamingUtils.toConnectorImplementationFilename(impl.getImplementationId(), impl.getImplementationVersion(), false) + ".zip" ;
                }
                selectZip(destFileText,defaultName);
            }
        });

        UpdateValueStrategy startegy = new UpdateValueStrategy() ;
        final EmptyInputValidator emptyValidator = new EmptyInputValidator(Messages.destinationLabel);
        startegy.setBeforeSetValidator(new IValidator() {

            @Override
            public IStatus validate(Object value) {
                IStatus status = emptyValidator.validate(value);
                if(!status.isOK()){
                    return status;
                }
                File target = new File(value.toString()) ;
                File parentFile = target.getParentFile() ;
                if(parentFile == null || !parentFile.exists() || !value.toString().endsWith(".zip") ){
                    return new Status(IStatus.ERROR, ConnectorPlugin.PLUGIN_ID, Messages.targetPathIsInvalid) ;
                }

                if(target.exists()){
                    return new Status(IStatus.WARNING, ConnectorPlugin.PLUGIN_ID, Messages.existingFileWillBeOverwrite) ;
                }
                return Status.OK_STATUS;
            }
        }) ;

        context.bindValue(ViewersObservables.observeSingleSelection(tableViewer), PojoProperties.value(ExportConnectorWizardPage.class, "selectedImplementation").observe(this),selectionStrategy,null)  ;
        context.bindValue(SWTObservables.observeSelection(includeSourceCheckbox), PojoProperties.value(ExportConnectorWizardPage.class, "includeSources").observe(this)) ;
        context.bindValue(SWTObservables.observeSelection(addDependenciesCheckbox), PojoProperties.value(ExportConnectorWizardPage.class, "addDependencies").observe(this)) ;
        context.bindValue(SWTObservables.observeText(destFileText, SWT.Modify), PojoProperties.value(ExportConnectorWizardPage.class, "destFilePath").observe(this),startegy,null) ;

        pageSupport = WizardPageSupport.create(this, context) ;

        setControl(composite);
    }


    protected void filterViewer(TableViewer tableViewer, String text) {


    }


    protected void selectZip(Text destFileText,String defualtFileName) {
        FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
        fileDialog.setFilterExtensions(new String[] { "*.zip" });
        if (destFileText.getText() != null && !destFileText.getText().isEmpty()) {
            File beforeFile = new File(destFileText.getText());
            fileDialog.setFilterPath(beforeFile.getParent());
            fileDialog.setFileName(beforeFile.getName());
        } else {
            fileDialog.setFilterPath(System.getProperty("user.home"));
            fileDialog.setFileName(defualtFileName) ;
        }
        String res = fileDialog.open();
        if (res != null) {
            if (!res.endsWith(".zip")) {
                res += ".zip";
            }
            destFileText.setText(res);
        }
    }


    @Override
    public void dispose() {
        super.dispose();
        if(context != null){
            context.dispose() ;
        }
        if(pageSupport != null){
            pageSupport.dispose() ;
        }
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        Object selection =  ((IStructuredSelection) event.getSelection()).getFirstElement() ;
        if(removeButton != null && selection instanceof ConnectorImplementation){
            removeButton.setEnabled(true) ;
        }
    }


    @Override
    public void doubleClick(DoubleClickEvent event) {
        Object selection =  ((IStructuredSelection) event.getSelection()).getFirstElement() ;
        if(selection instanceof ConnectorImplementation){
            if(getNextPage() != null){
                getContainer().showPage(getNextPage());
            }else{
                if(getWizard().performFinish()){
                    ((WizardDialog) getContainer()).close() ;
                }
            }
        }
    }

    public ConnectorImplementation getSelectedImplementation() {
        return selectedImplementation;
    }

    public void setSelectedImplementation(ConnectorImplementation selectedImplementation) {
        this.selectedImplementation = selectedImplementation;
    }


    public String getDestFilePath() {
        return destFilePath;
    }


    public void setDestFilePath(String destFilePath) {
        this.destFilePath = destFilePath;
    }

    public boolean isIncludeSources() {
        return includeSources;
    }


    public void setIncludeSources(boolean includeSources) {
        this.includeSources = includeSources;
    }


    public boolean isAddDependencies() {
        return addDependencies;
    }


    public void setAddDependencies(boolean addDependencies) {
        this.addDependencies = addDependencies;
    }




}
