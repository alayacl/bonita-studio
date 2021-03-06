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
package org.bonitasoft.studio.connector.model.definition.wizard;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.connector.model.definition.ConnectorDefinition;
import org.bonitasoft.studio.connector.model.definition.Input;
import org.bonitasoft.studio.connector.model.definition.Page;
import org.bonitasoft.studio.connector.model.i18n.DefinitionResourceProvider;
import org.bonitasoft.studio.expression.editor.filter.AvailableExpressionTypeFilter;
import org.bonitasoft.studio.model.connectorconfiguration.ConnectorConfiguration;
import org.bonitasoft.studio.model.connectorconfiguration.ConnectorConfigurationFactory;
import org.bonitasoft.studio.model.connectorconfiguration.ConnectorParameter;
import org.bonitasoft.studio.model.expression.AbstractExpression;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * @author Romain Bioteau
 *
 */
public abstract class AbstractConnectorConfigurationWizardPage extends WizardPage {

    private ConnectorConfiguration configuration;
    private Page page;
    private EObject elementContainer;
    private ConnectorDefinition definition;
    private final EMFDataBindingContext context;
    private WizardPageSupport supportPage;
    private DefinitionResourceProvider messageProvider;
    private AvailableExpressionTypeFilter connectorExpressionContentTypeFilter ;

    public AbstractConnectorConfigurationWizardPage(){
        this(AbstractConnectorConfigurationWizardPage.class.getName()) ;
    }
    
    public AbstractConnectorConfigurationWizardPage(String pageName){
        super(pageName) ;
        context = new EMFDataBindingContext() ;
    }

    @Override
    public final void createControl(final Composite parent) {
        setControl(doCreateControl(parent,context)) ;
        supportPage = WizardPageSupport.create(this, context) ;
    }

    protected EMFDataBindingContext getDatabindingContext(){
    	return context;
    }

    protected abstract Control doCreateControl(Composite parent,EMFDataBindingContext context) ;

    @Override
    public void dispose() {
        super.dispose();
        if(supportPage != null){
            supportPage.dispose() ;
        }
        if(context != null){
            context.dispose() ;
        }
    }

    public ConnectorConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ConnectorConfiguration configuration) {
        this.configuration = configuration;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
        if(messageProvider != null){
            setTitle(messageProvider.getPageTitle(definition, page.getId())) ;
            setDescription(messageProvider.getPageDescription(definition, page.getId())) ;
        }
    }


    public EObject getElementContainer() {
        return elementContainer;
    }

    public void setElementContainer(EObject elementContainer) {
        this.elementContainer = elementContainer;
    }

    public ConnectorDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(ConnectorDefinition definition) {
        this.definition = definition;
    }

    public DefinitionResourceProvider getMessageProvider() {
        return messageProvider;
    }

    public void setMessageProvider(DefinitionResourceProvider messageProvider) {
        this.messageProvider = messageProvider;
    }
    
    protected Input getInput(String inputName) {
		for(Input input : getDefinition().getInput()){
			if(input.getName().equals(inputName)){
				return input;
			}
		}
		throw new IllegalArgumentException("Input "+inputName +" not found in connector definition "+getDefinition().getId());
	}

    protected ConnectorParameter getConnectorParameter(Input input) {
		for(ConnectorParameter param : configuration.getParameters()){
			if(param.getKey().equals(input.getName())){
				if(param.getExpression() == null){
					param.setExpression(createExpression(input)) ;
				}
				return param ;
			}
		}

		final ConnectorParameter parameter = ConnectorConfigurationFactory.eINSTANCE.createConnectorParameter() ;
		parameter.setKey(input.getName()) ;
		parameter.setExpression(createExpression(input)) ;
		getConfiguration().getParameters().add(parameter) ;

		return parameter ;
	}

	protected AbstractExpression createExpression(Input input) {
		String inputClassName = input.getType() ;
		final Expression expression = ExpressionFactory.eINSTANCE.createExpression() ;
		expression.setReturnType(inputClassName) ;
		expression.setReturnTypeFixed(true) ;
		expression.setType(ExpressionConstants.CONSTANT_TYPE) ;
		expression.setName(input.getDefaultValue()) ;
		expression.setContent(input.getDefaultValue()) ;
		return expression ;
	}

    @Override
    public IWizardPage getPreviousPage() {
        IWizard wizard = getWizard();
        if(wizard != null){
            return wizard.getPreviousPage(this);
        }
        return super.getPreviousPage();
    }

	public AvailableExpressionTypeFilter getExpressionTypeFilter() {
		return connectorExpressionContentTypeFilter;
	}

	public void setExpressionTypeFilter(AvailableExpressionTypeFilter connectorExpressionContentTypeFilter) {
		this.connectorExpressionContentTypeFilter = connectorExpressionContentTypeFilter;
	}


}
