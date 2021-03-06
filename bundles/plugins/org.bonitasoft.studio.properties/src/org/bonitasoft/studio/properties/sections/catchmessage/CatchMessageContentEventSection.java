/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.studio.properties.sections.catchmessage;

import java.util.List;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.common.properties.AbstractBonitaDescriptionSection;
import org.bonitasoft.studio.expression.editor.filter.AvailableExpressionTypeFilter;
import org.bonitasoft.studio.expression.editor.operation.OperationsComposite;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.bonitasoft.studio.model.expression.ListExpression;
import org.bonitasoft.studio.model.expression.Operation;
import org.bonitasoft.studio.model.expression.Operator;
import org.bonitasoft.studio.model.expression.TableExpression;
import org.bonitasoft.studio.model.process.AbstractCatchMessageEvent;
import org.bonitasoft.studio.model.process.Data;
import org.bonitasoft.studio.model.process.Message;
import org.bonitasoft.studio.model.process.MessageFlow;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.properties.i18n.Messages;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * @author Aurelien Pupier
 *@author Aurelie Zara
 */
public class CatchMessageContentEventSection extends AbstractBonitaDescriptionSection {

	private OperationsComposite alc;
	private Composite mainComposite;
	MessageContentExpressionValidator validator;
	private EObject lastEObject;


	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		mainComposite = getWidgetFactory().createComposite(parent);
		mainComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).extendedMargins(15, 25, 15, 10).create());
		mainComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		createAutoFillButton(aTabbedPropertySheetPage);
		validator= new MessageContentExpressionValidator();
		alc = new OperationsComposite(aTabbedPropertySheetPage,mainComposite,new AvailableExpressionTypeFilter(new String[]{ExpressionConstants.CONSTANT_TYPE,ExpressionConstants.MESSAGE_ID_TYPE}), new AvailableExpressionTypeFilter(new String[]{ExpressionConstants.VARIABLE_TYPE}));
		alc.addActionExpressionValidator(ExpressionConstants.MESSAGE_ID_TYPE, validator);
		alc.addActionExpressionValidator(ExpressionConstants.CONSTANT_TYPE, validator);
		//        ActionExpressionNatureProvider actionExprNatureProvider = new ActionExpressionNatureProvider();
		//        actionExprNatureProvider.setContext(getEObject());
		//        alc.setActionExpressionNatureContentProvider(actionExprNatureProvider);
		alc.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create()) ;
	}

	private void createAutoFillButton(
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		Button autoFillButton = aTabbedPropertySheetPage.getWidgetFactory().createButton(mainComposite, Messages.autoFillMessageContent, SWT.FLAT);
		autoFillButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
					
					@Override
					public void run() {
						MessageFlow incomingMessag = getCatchMessageEvent().getIncomingMessag();
						TableExpression throwMessageContent=null;
						if(incomingMessag != null){
							final Message message = ModelHelper.findEvent(getCatchMessageEvent(), incomingMessag.getName());
							if(message != null){
								throwMessageContent = message.getMessageContent();
								for (ListExpression row : throwMessageContent.getExpressions()) {
									List<org.bonitasoft.studio.model.expression.Expression> col =  row.getExpressions() ;
									if (col.size()==2){
										boolean alreadyExist = false;
										String throwMessageContentExpressionName = col.get(0).getName();
										if(throwMessageContentExpressionName != null){
											/*Check if the item already exists*/
											EList<Operation> catchMessageContents = getCatchMessageEvent().getMessageContent();
											for (Operation messageContent : catchMessageContents) {
												Expression actionExpression = messageContent.getRightOperand();
												if(actionExpression != null
														&& throwMessageContentExpressionName.equals(actionExpression.getName())){
													alreadyExist = true;
													break;
												}
											}

											if(!alreadyExist){
												createNewMessageContentLine(throwMessageContentExpressionName);
											}
										}
									}
								}
								/*refresh UI*/
								validator.setCatchMessageEvent(getCatchMessageEvent());
								alc.setEObject(getCatchMessageEvent());
								alc.setContext(new EMFDataBindingContext());
								alc.removeLinesUI();
								alc.fillTable();
								alc.refresh();
							}
						}
					}
				});
				

			}



			private void createNewMessageContentLine(
					String throwMessageContentExpressionName) {
				/*add it if not*/
				Operation newActionMessageContent = ExpressionFactory.eINSTANCE.createOperation();
				Operator assignment = ExpressionFactory.eINSTANCE.createOperator();
				assignment.setType(ExpressionConstants.ASSIGNMENT_OPERATOR) ;
				newActionMessageContent.setOperator(assignment) ;
				Expression createExpression = ExpressionFactory.eINSTANCE.createExpression();
				createExpression.setName(throwMessageContentExpressionName);
				createExpression.setContent(throwMessageContentExpressionName);
				createExpression.setReturnType(String.class.getName());
				createExpression.setType(ExpressionConstants.MESSAGE_ID_TYPE) ;
				newActionMessageContent.setRightOperand(createExpression);


				/*check if there is a data with the same name,
				 * if yes, assign it*/
				List<Data> accessibleData = ModelHelper.getAccessibleData(getCatchMessageEvent());
				for (Data data : accessibleData) {
					if(throwMessageContentExpressionName.equals(data.getName())){
						Expression dataExpression = ExpressionFactory.eINSTANCE.createExpression();
						dataExpression.setName(data.getName());
						dataExpression.setContent(data.getName());
						dataExpression.setReturnType(org.bonitasoft.studio.common.DataUtil.getTechnicalTypeFor(data));
						dataExpression.setType(ExpressionConstants.VARIABLE_TYPE);
						dataExpression.getReferencedElements().add(EcoreUtil.copy(data));
						newActionMessageContent.setLeftOperand(dataExpression);
					}
				}

				Command addCommand = AddCommand.create(getEditingDomain(), getCatchMessageEvent(), ProcessPackage.Literals.ABSTRACT_CATCH_MESSAGE_EVENT__MESSAGE_CONTENT, newActionMessageContent);
				getEditingDomain().getCommandStack().execute(addCommand);
			}
		});
	}

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		if(lastEObject == null || (lastEObject != null && !lastEObject.equals(getEObject()))){
			lastEObject = getEObject();
			validator.setCatchMessageEvent(getCatchMessageEvent());
			alc.setEObject(getCatchMessageEvent());
			alc.setContext(new EMFDataBindingContext());
			alc.removeLinesUI();
			alc.fillTable();
			alc.refresh() ;
		}

	}

	private AbstractCatchMessageEvent getCatchMessageEvent(){
		return (AbstractCatchMessageEvent)getEObject();
	}

	@Override
	public String getSectionDescription() {
		return Messages.catchMessageContentEventSectionDescription;
	}
}
