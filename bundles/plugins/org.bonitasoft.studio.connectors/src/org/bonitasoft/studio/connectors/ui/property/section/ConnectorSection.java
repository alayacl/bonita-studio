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
package org.bonitasoft.studio.connectors.ui.property.section;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.jface.EMFListFeatureTreeContentProvider;
import org.bonitasoft.studio.common.properties.AbstractBonitaDescriptionSection;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.connector.model.definition.ConnectorDefinition;
import org.bonitasoft.studio.connectors.i18n.Messages;
import org.bonitasoft.studio.connectors.repository.ConnectorDefRepositoryStore;
import org.bonitasoft.studio.connectors.ui.provider.StyledConnectorLabelProvider;
import org.bonitasoft.studio.connectors.ui.wizard.ConnectorContainerSwitchWizard;
import org.bonitasoft.studio.connectors.ui.wizard.ConnectorDefinitionWizardDialog;
import org.bonitasoft.studio.connectors.ui.wizard.ConnectorWizard;
import org.bonitasoft.studio.model.process.Connector;
import org.bonitasoft.studio.model.process.Lane;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import static org.bonitasoft.studio.common.Messages.bosProductName;
/**
 * 
 * @author Romain Bioteau
 */
public class ConnectorSection extends AbstractBonitaDescriptionSection implements IDoubleClickListener, ISelectionChangedListener {

    private Button removeConnectorButton;
    private Button updateConnectorButton;
    private Button upConnectorButton;
    private Button downConnectorButton;
    private Composite mainComposite;
    private TableViewer tableViewer;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.bonitasoft.studio.properties.sections.data.DataSection#createControls
     * (org.eclipse.swt.widgets.Composite,
     * org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
     */
    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);
        mainComposite = getWidgetFactory().createComposite(parent);
        mainComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).margins(20, 15).create());
        mainComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        final Composite viewerComposite = getWidgetFactory().createComposite(mainComposite) ;
        viewerComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create()) ;
        viewerComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 0).create()) ;
        createConnectorComposite(viewerComposite);
    }


    private void createConnectorComposite(Composite parent) {
        Composite buttonsComposite = getWidgetFactory().createPlainComposite(parent, SWT.NONE);
        buttonsComposite.setLayoutData(GridDataFactory.fillDefaults().grab(false, true).create()) ;
        buttonsComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).margins(5,0).spacing(0, 3).create());

        createAddConnectorButton(buttonsComposite);
        updateConnectorButton = createUpdateConnectorButton(buttonsComposite);
        removeConnectorButton = createRemoveConnectorButton(buttonsComposite);
        upConnectorButton = createUpConnectorButton(buttonsComposite);
        downConnectorButton = createDownConnectorButton(buttonsComposite);
        createMoveConnectorButton(buttonsComposite);



        tableViewer = new TableViewer(parent, SWT.BORDER | SWT.MULTI | SWT.NO_FOCUS);
        getWidgetFactory().adapt(tableViewer.getTable(), false, false) ;
        tableViewer.getTable().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 120).create());

        tableViewer.addDoubleClickListener(this);
        tableViewer.addSelectionChangedListener(this);

        tableViewer.setContentProvider(new EMFListFeatureTreeContentProvider(getConnectorFeature()));
        tableViewer.setLabelProvider(new StyledConnectorLabelProvider());

    }


    private void updateButtons() {
        if (tableViewer != null) {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();

            if (!removeConnectorButton.isDisposed()) {
                removeConnectorButton.setEnabled(!selection.isEmpty());
            }

            final boolean isAnElementSelected = selection.size() == 1;
            final boolean hasMoreThanOneItemInTheTable = tableViewer.getTable().getItemCount() > 1;
			if(!downConnectorButton.isDisposed()){
                downConnectorButton.setEnabled(isAnElementSelected && hasMoreThanOneItemInTheTable) ;
            }

            if(!upConnectorButton.isDisposed()){
                upConnectorButton.setEnabled(isAnElementSelected && hasMoreThanOneItemInTheTable) ;
            }

            if(!updateConnectorButton.isDisposed()){
                if(isAnElementSelected){
                    Connector connector = (Connector) selection.getFirstElement() ;
                    ConnectorDefRepositoryStore connectorDefStore = (ConnectorDefRepositoryStore) RepositoryManager.getInstance().getRepositoryStore(ConnectorDefRepositoryStore.class) ;
                    ConnectorDefinition def = connectorDefStore.getDefinition(connector.getDefinitionId(),connector.getDefinitionVersion()) ;
                    updateConnectorButton.setEnabled(def!= null) ;
                }else{
                    updateConnectorButton.setEnabled(false) ;
                }

            }
        }
    }

    /**
     * @param buttonsComposite
     * @return
     */
    private Button createRemoveConnectorButton(final Composite buttonComposite) {
        Button removeButton = getWidgetFactory().createButton(buttonComposite, Messages.removeData, SWT.FLAT);
        removeButton.setLayoutData(GridDataFactory.fillDefaults().hint(85, SWT.DEFAULT).create()) ;
        removeButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (tableViewer != null && ((IStructuredSelection) tableViewer.getSelection()).size() > 0) {
                    List<?> selection = ((IStructuredSelection) tableViewer.getSelection()).toList();
                    if (MessageDialog.openConfirm(buttonComposite.getShell(), Messages.deleteDialogTitle, createMessage())) {
                        getEditingDomain().getCommandStack().execute(new RemoveCommand(getEditingDomain(), getEObject(), getConnectorFeature(), selection));
                        tableViewer.refresh() ;
                    }
                }
            }

            public String createMessage() {
                Object[] selection = ((IStructuredSelection) tableViewer.getSelection()).toArray();
                StringBuilder res = new StringBuilder(Messages.deleteDialogConfirmMessage);
                res.append(' ');
                res.append(((Connector) selection[0]).getName());
                for (int i = 1; i < selection.length; i++) {
                    res.append(", ");res.append(((Connector) selection[i]).getName()); //$NON-NLS-1$
                }
                res.append(" ?"); //$NON-NLS-1$
                return res.toString();
            }
        });
        return removeButton;
    }

    protected Button createMoveConnectorButton(Composite buttonsComposite) {
        Button switchContainerButton = getWidgetFactory().createButton(buttonsComposite, Messages.move, SWT.FLAT);
        switchContainerButton.setLayoutData(GridDataFactory.fillDefaults().hint(85, SWT.DEFAULT).create()) ;
        switchContainerButton.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
            	final WizardDialog dialog =  new WizardDialog(Display.getDefault().getActiveShell(),new ConnectorContainerSwitchWizard(getEditingDomain(),ModelHelper.getParentProcess(getEObject())));
                if(dialog.open() == Dialog.OK){
                    tableViewer.refresh();
                }
            }
        });
        return switchContainerButton;
    }

    private Button createAddConnectorButton(final Composite parent) {
        final Button addData = getWidgetFactory().createButton(parent, Messages.add, SWT.FLAT);
        addData.setLayoutData(GridDataFactory.fillDefaults().hint(85, SWT.DEFAULT).create()) ;
        addData.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                WizardDialog wizardDialog = new ConnectorDefinitionWizardDialog(Display.getCurrent().getActiveShell(), new ConnectorWizard(getEObject(), getConnectorFeature(), getConnectorFeatureToCheckUniqueID()));
                if(wizardDialog.open() == Dialog.OK){
                    tableViewer.refresh();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });
        return addData;
    }

    private Button createUpdateConnectorButton(final Composite parent) {
        Button updateButton = getWidgetFactory().createButton(parent, Messages.update, SWT.FLAT);
        updateButton.setLayoutData(GridDataFactory.fillDefaults().hint(85, SWT.DEFAULT).create()) ;
        updateButton.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                updateConnectorAction();
            }
        });
        return updateButton;
    }


    private void updateConnectorAction() {
        IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
        if (selection.size() != 1) {
            MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Messages.selectOnlyOneElementTitle, Messages.selectOnlyOneElementMessage);
        } else {
            Connector connector = (Connector) selection.getFirstElement() ;
            ConnectorDefRepositoryStore connectorDefStore = (ConnectorDefRepositoryStore) RepositoryManager.getInstance().getRepositoryStore(ConnectorDefRepositoryStore.class) ;
            ConnectorDefinition def = connectorDefStore.getDefinition(connector.getDefinitionId(),connector.getDefinitionVersion()) ;
            if(def != null){
                WizardDialog wizardDialog = new ConnectorDefinitionWizardDialog(Display.getCurrent().getActiveShell(), new ConnectorWizard(connector,getConnectorFeature(),getConnectorFeatureToCheckUniqueID()));
                if(wizardDialog.open() == Dialog.OK){
                    tableViewer.refresh() ;
                }
            }
        }
    }

    protected boolean getShowAutoGenerateForm() {
        return true;
    }

    protected EStructuralFeature getConnectorFeature() {
        return ProcessPackage.Literals.CONNECTABLE_ELEMENT__CONNECTORS;
    }

    protected Set<EStructuralFeature> getConnectorFeatureToCheckUniqueID() {
        Set<EStructuralFeature> res = new HashSet<EStructuralFeature>();
        res.add(ProcessPackage.Literals.CONNECTABLE_ELEMENT__CONNECTORS);
        return res;
    }


    protected void refreshBindings() {
        if (tableViewer != null && getEObject() != null) {
            bindTree();
        }
    }


    private void bindTree() {
        tableViewer.setInput(getEObject());
        updateButtons();
    }


    protected void refreshTree() {
        if(!tableViewer.getTable().isDisposed()){
            tableViewer.setInput(getEObject());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gmf.runtime.diagram.ui.properties.sections.
     * AbstractModelerPropertySection#setEObject(org.eclipse.emf.ecore.EObject)
     */
    @Override
    public void setEObject(EObject object) {
        super.setEObject(object);
        refreshBindings();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gmf.runtime.diagram.ui.properties.sections.
     * AbstractModelerPropertySection#getEObject()
     */
    @Override
    protected EObject getEObject() {
        EObject eObject = super.getEObject();
        if (eObject instanceof Lane) {
            return ModelHelper.getParentProcess(eObject);
        }
        return eObject;
    }


    /* (non-Javadoc)
     * @see org.eclipse.gmf.runtime.diagram.ui.properties.sections.AbstractModelerPropertySection#dispose()
     */
    @Override
    public void dispose() {
        super.dispose() ;
    }

    @Override
    public void doubleClick(DoubleClickEvent event) {
        updateConnectorAction();
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        updateButtons() ;
    }

    /**
     * @param buttonsComposite
     * @return
     */
    protected Button createUpConnectorButton(Composite buttonsComposite) {
        Button addConnectorButton = getWidgetFactory().createButton(buttonsComposite, Messages.up, SWT.FLAT);
        addConnectorButton.setLayoutData(GridDataFactory.fillDefaults().hint(85, SWT.DEFAULT).create()) ;
        addConnectorButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                moveSelectedConnector(-1);
            }
        });
        return addConnectorButton;
    }

    /**
     * @param buttonsComposite
     * @return
     */
    protected Button createDownConnectorButton(Composite buttonsComposite) {
        Button addConnectorButton = getWidgetFactory().createButton(buttonsComposite, Messages.down, SWT.FLAT);
        addConnectorButton.setLayoutData(GridDataFactory.fillDefaults().hint(85, SWT.DEFAULT).create()) ;
        addConnectorButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                moveSelectedConnector(+1);
            }
        });
        return addConnectorButton;
    }



    private void moveSelectedConnector(int diff) {
        EObject selectConnector = (EObject) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement() ;
        @SuppressWarnings("unchecked")
        EList<Connector> connectors = (EList<Connector>) getEObject().eGet(getConnectorFeature());
        int destIndex = connectors.indexOf(selectConnector) + diff;
        Command c = new MoveCommand(getEditingDomain(), connectors, selectConnector, destIndex);
        getEditingDomain().getCommandStack().execute(c);
        refresh();
    }


	@Override
	public String getSectionDescription() {
		return Messages.bind(Messages.connectorSectionDescription, bosProductName);
	}
	
	@Override
	public void refresh() {
		super.refresh();
		refreshTree();
	}

}
