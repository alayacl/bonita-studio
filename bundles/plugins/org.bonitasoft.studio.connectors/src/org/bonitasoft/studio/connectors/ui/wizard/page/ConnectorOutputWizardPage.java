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
package org.bonitasoft.studio.connectors.ui.wizard.page;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.connectors.expression.DataExpressionNatureProvider;
import org.bonitasoft.studio.expression.editor.filter.AvailableExpressionTypeFilter;
import org.bonitasoft.studio.expression.editor.operation.OperationsComposite;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Romain Bioteau
 *
 */
public class ConnectorOutputWizardPage extends AbstractConnectorOutputWizardPage {


    private OperationsComposite lineComposite;


	@Override
    protected Control doCreateControl(Composite parent,final EMFDataBindingContext context) {
        final Composite mainComposite = new Composite(parent, SWT.NONE) ;
        mainComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create()) ;
        mainComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).create()) ;
        final AvailableExpressionTypeFilter leftFilter =  new AvailableExpressionTypeFilter(new String[]{ 
                ExpressionConstants.VARIABLE_TYPE,
                ExpressionConstants.DOCUMENT_REF_TYPE}) ;
        final AvailableExpressionTypeFilter rightFilter =  new AvailableExpressionTypeFilter(new String[]{ 
        		 ExpressionConstants.CONSTANT_TYPE,
        		ExpressionConstants.CONNECTOR_OUTPUT_TYPE,
        		ExpressionConstants.VARIABLE_TYPE,
        		ExpressionConstants.PARAMETER_TYPE,
                ExpressionConstants.SCRIPT_TYPE,
                ExpressionConstants.DOCUMENT_TYPE
        }) ;

        lineComposite = new OperationsComposite(null, mainComposite, rightFilter, leftFilter) ;
        lineComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true,false).create()) ;
        lineComposite.setContext(context) ;
        lineComposite.setContext(getElementContainer());
        lineComposite.setEObject(getConnector()) ;
        if(getOutputDataFeature() != null){
            lineComposite.setStorageExpressionNatureContentProvider(new DataExpressionNatureProvider(getOutputDataFeature())) ;
        }
        lineComposite.fillTable() ;
        return mainComposite ;
    }


}
