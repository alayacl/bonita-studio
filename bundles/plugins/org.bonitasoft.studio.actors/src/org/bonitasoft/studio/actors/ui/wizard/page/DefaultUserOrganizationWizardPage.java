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
package org.bonitasoft.studio.actors.ui.wizard.page;

import java.util.HashSet;
import java.util.Set;

import org.bonitasoft.studio.actors.i18n.Messages;
import org.bonitasoft.studio.actors.model.organization.Organization;
import org.bonitasoft.studio.actors.model.organization.User;
import org.bonitasoft.studio.common.jface.databinding.validator.EmptyInputValidator;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Romain Bioteau
 *
 */
public class DefaultUserOrganizationWizardPage extends WizardPage {

    private String user ;
    private String password ;
    private DataBindingContext context;
    private WizardPageSupport pageSupport;
    private AutoCompleteField autoCompletionField;

    public DefaultUserOrganizationWizardPage() {
        super(DefaultUserOrganizationWizardPage.class.getName());
        setTitle(Messages.defaultUserOrganizationTitle) ;
        setDescription(Messages.defaultUserOrganizationDesc) ;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite mainComposite = new Composite(parent, SWT.NONE) ;
        mainComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create()) ;
        mainComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).create()) ;

        context = new DataBindingContext() ;

        final Label usernameLabel = new Label(mainComposite, SWT.NONE) ;
        usernameLabel.setLayoutData(GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).create()) ;
        usernameLabel.setText(Messages.userName) ;

        final Text usernameText = new Text(mainComposite, SWT.BORDER) ;
        usernameText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create()) ;

        autoCompletionField = new AutoCompleteField(usernameText, new TextContentAdapter(), new String[]{}) ;

        final Label passwordLabel = new Label(mainComposite, SWT.NONE) ;
        passwordLabel.setLayoutData(GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).create()) ;
        passwordLabel.setText(Messages.password) ;

        final Text passwordText = new Text(mainComposite, SWT.BORDER | SWT.PASSWORD) ;
        passwordText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create()) ;

        UpdateValueStrategy strategy = new UpdateValueStrategy() ;
        strategy.setBeforeSetValidator(new EmptyInputValidator(Messages.userName)) ;

        context.bindValue(SWTObservables.observeText(usernameText, SWT.Modify), PojoProperties.value(DefaultUserOrganizationWizardPage.class, "user").observe(this),strategy,null) ;
      
        UpdateValueStrategy strategy2 = new UpdateValueStrategy() ;
        strategy2.setBeforeSetValidator(new EmptyInputValidator(Messages.password)) ;
        context.bindValue(SWTObservables.observeText(passwordText, SWT.Modify), PojoProperties.value(DefaultUserOrganizationWizardPage.class, "password").observe(this),strategy2,null) ;

        pageSupport = WizardPageSupport.create(this,context) ;
        setControl(mainComposite) ;
    }

    @Override
    public void dispose() {
        super.dispose();
        if(pageSupport != null){
            pageSupport.dispose() ;
        }
        if(context != null){
            context.dispose() ;
        }

    }

    public void setOrganization(Organization organization){
        Set<String> usernames = new HashSet<String>() ;
        for(User user : organization.getUsers().getUser()){
            usernames.add(user.getUserName()) ;
        }
        autoCompletionField.setProposals(usernames.toArray(new String[usernames.size()])) ;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
