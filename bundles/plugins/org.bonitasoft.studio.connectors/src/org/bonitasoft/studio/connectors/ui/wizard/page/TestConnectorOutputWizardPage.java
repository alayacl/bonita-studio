/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.List;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.connectors.i18n.Messages;
import org.bonitasoft.studio.expression.editor.filter.AvailableExpressionTypeFilter;
import org.bonitasoft.studio.expression.editor.provider.IExpressionValidator;
import org.bonitasoft.studio.expression.editor.viewer.ExpressionViewer;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionPackage;
import org.bonitasoft.studio.model.expression.Operation;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @author Aurelie Zara
 *
 */
public class TestConnectorOutputWizardPage extends
AbstractConnectorOutputWizardPage {

	List<ExpressionViewer>resultViewers;
	/* (non-Javadoc)
	 * @see org.bonitasoft.studio.connectors.ui.wizard.page.AbstractConnectorOutputWizardPage#doCreateControl(org.eclipse.swt.widgets.Composite, org.eclipse.emf.databinding.EMFDataBindingContext)
	 */
	@Override
	protected Control doCreateControl(Composite parent,
			EMFDataBindingContext context) {

		final Composite mainComposite = new Composite(parent, SWT.NONE) ;
		mainComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create()) ;
		mainComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create()) ;

		AvailableExpressionTypeFilter connectorOutputFilter =  new AvailableExpressionTypeFilter(new String[]{
				ExpressionConstants.CONNECTOR_OUTPUT_TYPE,
				ExpressionConstants.VARIABLE_TYPE,
				ExpressionConstants.SCRIPT_TYPE
		}) ;
		final List<Operation> outputOperations = getConnector().getOutputs();

		for (Operation output:outputOperations){
			final Label expressionLabel = new Label(mainComposite, SWT.READ_ONLY);
			expressionLabel.setText(Messages.connectorExpressionViewerLabel);


			final ExpressionViewer outputExpressionViewer = new ExpressionViewer(mainComposite, SWT.BORDER, ExpressionPackage.Literals.OPERATION__RIGHT_OPERAND);
			outputExpressionViewer.getControl().setLayoutData( GridDataFactory.fillDefaults().grab(true, false).create());
			outputExpressionViewer.addFilter(connectorOutputFilter);
			outputExpressionViewer.setContext(getConnector());
			outputExpressionViewer.setMessage(Messages.connectorExpressionViewerMessage, IStatus.INFO);
			outputExpressionViewer.setExternalDataBindingContext(context);
			outputExpressionViewer.setInput(output);
			outputExpressionViewer.addExpressionValidator(ExpressionConstants.ALL_TYPES, new IExpressionValidator() {

				private Expression inputExpression;

				@Override
				public IStatus validate(Object value) {
					Expression exp = (Expression) inputExpression;
					if(exp.getType().equals(ExpressionConstants.SCRIPT_TYPE) || exp.getType().equals(ExpressionConstants.CONNECTOR_OUTPUT_TYPE)) {
						return ValidationStatus.ok();
					}
					return ValidationStatus.error(Messages.connectorTypeValidationMessage);
				}

				@Override
				public void setInputExpression(Expression inputExpression) {
					this.inputExpression = inputExpression;
				}

				@Override
				public void setDomain(EditingDomain domain) {

				}

				@Override
				public void setContext(EObject context) {

				}
			});

			context.bindValue(ViewersObservables.observeSingleSelection(outputExpressionViewer), EMFObservables.observeValue(output, ExpressionPackage.Literals.OPERATION__RIGHT_OPERAND));
			outputExpressionViewer.setProposalsFiltering(false);
		}
		return mainComposite ;
	}

}
