/**
 * Copyright (C) 2010 BonitaSoft S.A.
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
package org.bonitasoft.studio.intro.content;

import java.io.PrintWriter;
import java.util.List;

import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.examples.store.ExampleFileStore;
import org.bonitasoft.studio.examples.store.ExamplesRepositoryStore;
import org.bonitasoft.studio.intro.actions.OpenSpecificExampleAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;
import org.eclipse.ui.intro.config.IIntroXHTMLContentProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author Mickael Istria
 *
 */
public class ProcessExampleContentProvider implements IIntroXHTMLContentProvider {

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.config.IIntroContentProvider#init(org.eclipse.ui.intro.config.IIntroContentProviderSite)
     */
    public void init(IIntroContentProviderSite site) {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.config.IIntroContentProvider#createContent(java.lang.String, java.io.PrintWriter)
     */
    public void createContent(String id, PrintWriter out) {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.config.IIntroContentProvider#createContent(java.lang.String, org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    public void createContent(String id, Composite parent, FormToolkit toolkit) {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.config.IIntroContentProvider#dispose()
     */
    public void dispose() {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.config.IIntroXHTMLContentProvider#createContent(java.lang.String, org.w3c.dom.Element)
     */
    public void createContent(String id, Element parent) {
        int nbProc = Integer.parseInt(id.split(",")[0]);
        ExamplesRepositoryStore examplesSotre = (ExamplesRepositoryStore) RepositoryManager.getInstance().getCurrentRepository().getRepositoryStore(ExamplesRepositoryStore.class) ;
        List<ExampleFileStore> allArtifacts = examplesSotre.getChildren();
        nbProc = Math.min(nbProc, allArtifacts.size());
        Document doc = parent.getOwnerDocument();
        Element ul = doc.createElement("ul");
        parent.appendChild(ul);
        if (nbProc == 0) {
            return;
        }
        Object[] array =  allArtifacts.toArray();

        for (int i = 0; i < nbProc; i++) {
            IRepositoryFileStore proc = (IRepositoryFileStore) array[i];
            Element li = doc.createElement("li");
            Element a = doc.createElement("a");
            li.appendChild(a);
            a.setAttribute("href", createOpenProcessHref(proc));
            String displayNameForLabelProvider = proc.getDisplayName() ;
            a.setAttribute("title", displayNameForLabelProvider) ;
            if (displayNameForLabelProvider.length() > 40) {
                displayNameForLabelProvider = displayNameForLabelProvider.substring(0, 40) + "...";
            }
            Text procName = doc.createTextNode(displayNameForLabelProvider);
            a.appendChild(procName);
            ul.appendChild(li);
        }
    }

    /**
     * @param proc
     * @return
     */
    private String createOpenProcessHref(IRepositoryFileStore proc) {
        return "http://org.eclipse.ui.intro/runAction?pluginId=org.bonitasoft.studio.intro&class=" + OpenSpecificExampleAction.class.getName() + "&file=" + proc.getName();
    }

}
