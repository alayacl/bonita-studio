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
package org.bonitasoft.studio.expression.editor.formfield;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.expression.editor.provider.IExpressionNatureProvider;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.bonitasoft.studio.model.form.Form;
import org.bonitasoft.studio.model.form.FormField;
import org.bonitasoft.studio.model.form.NextFormButton;
import org.bonitasoft.studio.model.form.Widget;
import org.bonitasoft.studio.model.process.PageFlow;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.model.process.RecapFlow;
import org.bonitasoft.studio.model.process.ViewPageFlow;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * @author Aurelie Zara
 *
 */
public class FormFieldExpressionNatureProvider implements
IExpressionNatureProvider {


	private EAttribute flowType;
	private EObject context;

	public FormFieldExpressionNatureProvider(EAttribute flowType,EObject context){
		this.flowType = flowType;
		this.context = context;

	}

	/* (non-Javadoc)
	 * @see org.bonitasoft.studio.expression.editor.provider.IExpressionNatureProvider#getExpressions()
	 */
	public Expression[] getExpressions() {
		EObject relevantParent = getRelevantParent(context) ;
		List<Expression> result = new ArrayList<Expression>();
		if (ProcessPackage.Literals.PAGE_FLOW__ENTRY_PAGE_FLOW_TYPE.equals(flowType)){
			final PageFlow pageFlow = (PageFlow) relevantParent;
			if(pageFlow != null){
				for (Form f : pageFlow.getForm()){
					for (Widget w : f.getWidgets()) {
						if (w instanceof FormField || w instanceof NextFormButton){
							result.add( createExpression(w) ) ;
						}
					}
				}
			}
		} else {
			if ( ProcessPackage.Literals.VIEW_PAGE_FLOW__VIEW_PAGE_FLOW_TYPE.equals(flowType)){
				final ViewPageFlow pageFlow = (ViewPageFlow) relevantParent;
				if(pageFlow != null){
					for (Form f : pageFlow.getViewForm()){
						for (Widget w : f.getWidgets()) {
							if (w instanceof FormField || w instanceof NextFormButton){
								result.add( createExpression(w) ) ;
							}
						}
					}
				}
			} else {
				if (ProcessPackage.Literals.RECAP_FLOW__RECAP_PAGE_FLOW_TYPE.equals(flowType)){
					final RecapFlow pageFlow = (RecapFlow) relevantParent;
					if(pageFlow != null){
						for (Form f : pageFlow.getRecapForms()){
							for (Widget w : f.getWidgets()) {
								if (w instanceof FormField || w instanceof NextFormButton){
									result.add( createExpression(w) ) ;
								}
							}
						}
					}
				}
			}
		}

		return result.toArray(new Expression[result.size()]) ;
	}

	/* (non-Javadoc)
	 * @see org.bonitasoft.studio.expression.editor.provider.IExpressionNatureProvider#setContext(org.eclipse.emf.ecore.EObject)
	 */
	public void setContext(EObject context) {
		this.context = context;

	}

	/* (non-Javadoc)
	 * @see org.bonitasoft.studio.expression.editor.provider.IExpressionNatureProvider#getContext()
	 */
	public EObject getContext() {
		// TODO Auto-generated method stub
		return context;
	}

	private EObject getRelevantParent(EObject context) {
		EObject parent = context ;
		while(parent != null && (!(parent instanceof Form) && !(parent instanceof Widget)) && !(parent instanceof PageFlow)){
			parent = parent.eContainer() ;
		}
		return parent;
	}


	private Expression createExpression(Widget w) {
		Expression exp = ExpressionFactory.eINSTANCE.createExpression() ;
		exp.setType(getExpressionType()) ;
		exp.setContent("field_"+w.getName()) ;
		exp.setName("field_"+w.getName()) ;
		if(w.getReturnTypeModifier() != null ){
			exp.setReturnType(w.getReturnTypeModifier()) ;
		}else{
			exp.setReturnType(w.getAssociatedReturnType()) ;
		}
		exp.getReferencedElements().add(EcoreUtil.copy(w)) ;
		return exp;
	}

	public String getExpressionType() {
		return ExpressionConstants.FORM_FIELD_TYPE;
	}



}
