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

package org.bonitasoft.studio.data.provider;

import java.util.HashSet;
import java.util.Set;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.data.i18n.Messages;
import org.bonitasoft.studio.expression.editor.provider.IExpressionEditor;
import org.bonitasoft.studio.expression.editor.provider.IExpressionProvider;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.bonitasoft.studio.model.process.Document;
import org.bonitasoft.studio.model.process.Pool;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.pics.PicsConstants;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.swt.graphics.Image;

/**
 * @author Romain Bioteau
 *
 */
public class DocumentReferenceExpressionProvider implements IExpressionProvider {

	
	@Override
	public Set<Expression> getExpressions(EObject context) {
		Set<Expression> result = new HashSet<Expression>() ;
		Pool process = null;
		if(!(context instanceof Pool)){
			EObject parent = ModelHelper.getParentProcess(context);
			if(parent instanceof Pool){
				process = (Pool) parent;
			}
		}
		if(context != null && process != null){
			for(Document d : process.getDocuments()){
				result.add(createExpression(d));
			}
		}
		return result;
	}

	@Override
	public String getExpressionType() {
		return ExpressionConstants.DOCUMENT_REF_TYPE;
	}

	@Override
	public Image getIcon(Expression expression) {
		return getTypeIcon();
	}

	@Override
	public String getProposalLabel(Expression expression) {
		return expression.getName();
	}



	private Expression createExpression(Document d) {
		Expression exp = ExpressionFactory.eINSTANCE.createExpression() ;
		exp.setType(getExpressionType()) ;
		exp.setContent(d.getName()) ;
		exp.setName(d.getName()) ;
		exp.setReturnType(String.class.getName()) ;
		exp.getReferencedElements().add(EcoreUtil.copy(d)) ;
		return exp;
	}

	@Override
	public boolean isRelevantFor(EObject context) {
		return true;
	}

	@Override
	public Image getTypeIcon() {
		return Pics.getImage(PicsConstants.attachmentData);
	}

	@Override
	public String getTypeLabel() {
		return Messages.documentReferenceType;
	}

	@Override
	public IExpressionEditor getExpressionEditor(Expression expression,EObject context) {
		return null;
	}



}
