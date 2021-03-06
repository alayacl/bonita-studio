/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.properties.sections.message.wizards;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.expression.editor.provider.IExpressionNatureProvider;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.bonitasoft.studio.model.process.AbstractCatchMessageEvent;
import org.bonitasoft.studio.model.process.AbstractProcess;
import org.eclipse.emf.ecore.EObject;

/**
 * @author Romain Bioteau
 */
public class CatchMessageEventNamesExpressionNatureProvider implements IExpressionNatureProvider {

	private List<AbstractProcess> processes;

	@Override
	public Expression[] getExpressions() {
		List<Expression> result = new ArrayList<Expression>();
		if(processes != null && !processes.isEmpty()){
			final Set<String> names = new HashSet<String>();
			for(AbstractProcess process : processes){
				for(AbstractCatchMessageEvent catchMessage :  ModelHelper.getAllCatchEvent(process)){
					if(!names.contains(catchMessage.getName()) && catchMessage.getIncomingMessag() == null){
						names.add(catchMessage.getName());
					}
				}
				for(String pName : names){
					Expression exp = ExpressionFactory.eINSTANCE.createExpression();
					exp.setName(pName);
					exp.setContent(pName);
					exp.setReturnType(String.class.getName());
					exp.setReturnTypeFixed(true);
					exp.setType(ExpressionConstants.CONSTANT_TYPE);
					result.add(exp);
				}
			}
		}
		return result.toArray(new Expression[result.size()]);
	}

	public void setFoundProcesses(List<AbstractProcess> processes){
		this.processes = processes;
	}
	

	@Override
	public void setContext(final EObject context) {

	}

	@Override
	public AbstractProcess getContext() {
		return null;
	}

}
