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
package org.bonitasoft.studio.properties.sections.document;


import static org.bonitasoft.studio.common.Messages.bonitaStudioModuleName;

import org.bonitasoft.studio.data.attachment.repository.DocumentFileStore;
import org.bonitasoft.studio.data.attachment.repository.DocumentRepositoryStore;
import org.bonitasoft.studio.properties.i18n.Messages;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
/**
 * @author Aurelien Pupier
 *
 */
public class SelectDocumentInBonitaStudioRepository extends FileStoreSelectDialog {

    public SelectDocumentInBonitaStudioRepository(IShellProvider parentShell) {
        super(parentShell);
    }

    public SelectDocumentInBonitaStudioRepository(Shell parentShell){
        super(parentShell);
    }

    public DocumentFileStore getSelectedDocument(){
        return (DocumentFileStore)getSelectedFileStore();
    }

    @Override
    protected Class<DocumentRepositoryStore> getRepositoryStoreClass() {
        return DocumentRepositoryStore.class;
    }

    @Override
    protected String getDialogTitle() {
        return Messages.bind(Messages.selectDocumentDialogTitle, new Object[]{bonitaStudioModuleName});
    }
}
