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
package org.bonitasoft.studio.connector.model.definition.wizard;

import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.common.repository.model.IRepositoryStore;
import org.bonitasoft.studio.model.connectorconfiguration.ConnectorConfiguration;
import org.bonitasoft.studio.model.connectorconfiguration.ConnectorParameter;
import org.bonitasoft.studio.pics.Pics;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.wizard.Wizard;

/**
 * @author Romain Bioteau
 *
 */
public class SelectConnectorConfigurationWizard extends Wizard  {


    private final ConnectorConfiguration currentConfiguraiton;
    private SelectConnectorConfigurationWizardPage page;
    private final IRepositoryStore<? extends IRepositoryFileStore> configurationStore;

    public SelectConnectorConfigurationWizard(ConnectorConfiguration currentConfiguraiton,IRepositoryStore<? extends IRepositoryFileStore> configurationStore) {
        setDefaultPageImageDescriptor(Pics.getWizban()) ;
        this.currentConfiguraiton = currentConfiguraiton ;
        this.configurationStore = configurationStore ;
    }


    @Override
    public void addPages() {
        page = new SelectConnectorConfigurationWizardPage(currentConfiguraiton,configurationStore);
        addPage(page);
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        ConnectorConfiguration selectedConfiguration =  page.getSelectedConfiguration() ;
        currentConfiguraiton.getParameters().clear() ;
        for(ConnectorParameter parameter : selectedConfiguration.getParameters()){
            currentConfiguraiton.getParameters().add(EcoreUtil.copy(parameter)) ;
        }
        return true;
    }



}
