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
package org.bonitasoft.studio.connectors.expression;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.expression.editor.provider.ExpressionContentProvider;
import org.bonitasoft.studio.expression.editor.provider.IExpressionNatureProvider;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.bonitasoft.studio.model.process.Data;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;


/**
 * @author Romain Bioteau
 *
 */
public class DataExpressionNatureProvider extends ExpressionContentProvider implements IExpressionNatureProvider{

    private EObject context;
    private final EStructuralFeature dataFeature;

    public DataExpressionNatureProvider(EStructuralFeature dataFeature){
        this.dataFeature = dataFeature ;
    }

    @Override
    public Expression[] getExpressions() {
        Set<Expression> result = new HashSet<Expression>() ;
        EObject container = context ;
        while(container != null){
            if(container.eClass().getEAllStructuralFeatures().contains(dataFeature)){
                List<?> data = (List<?>) container.eGet(dataFeature) ;
                for(Object d : data){
                    if(d instanceof Data){
                        result.add(createExpression((Data) d)) ;
                    }
                }
                container = null ;
            }else{
                container = container.eContainer() ;
            }
        }
        return result.toArray(new Expression[result.size()]);
    }

    @Override
    public void setContext(EObject context) {
        this.context = context ;
    }

    private Expression createExpression(Data d) {
        Expression exp = ExpressionFactory.eINSTANCE.createExpression() ;
        exp.setType(ExpressionConstants.VARIABLE_TYPE) ;
        exp.setContent(d.getName()) ;
        exp.setName(d.getName()) ;
        exp.setReturnType(org.bonitasoft.studio.common.DataUtil.getTechnicalTypeFor(d)) ;
        exp.getReferencedElements().add(EcoreUtil.copy(d)) ;
        return exp;
    }

    @Override
    public EObject getContext() {
        return context;
    }

}
