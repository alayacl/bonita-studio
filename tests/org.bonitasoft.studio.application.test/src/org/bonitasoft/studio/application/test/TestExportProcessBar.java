/**
 * Copyright (C) 2010-2013 BonitaSoft S.A.
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
package org.bonitasoft.studio.application.test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;

import org.bonitasoft.studio.common.platform.tools.PlatformUtil;
import org.bonitasoft.studio.common.repository.Repository;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.operation.ImportBosArchiveOperation;
import org.bonitasoft.studio.diagram.custom.repository.DiagramRepositoryStore;
import org.bonitasoft.studio.engine.operation.ExportBarOperation;
import org.bonitasoft.studio.model.process.AbstractProcess;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Aurelien Pupier
 */
public class TestExportProcessBar extends TestCase {


    public void testExportProcessBarWithAttachmentAndSeveralPool() throws Exception{
        /*Import the processus*/
        URL url = getClass().getResource("TestExportProcessBarWithDocument-1.0.bos");
        url = FileLocator.toFileURL(url);
        File barToImport = new File(url.getFile());
        ImportBosArchiveOperation op = new ImportBosArchiveOperation();
        op.setArchiveFile(barToImport.getAbsolutePath());
        op.run(Repository.NULL_PROGRESS_MONITOR);
        /*Retrieve the AbstractProcess*/
        DiagramRepositoryStore store = (DiagramRepositoryStore) RepositoryManager.getInstance().getRepositoryStore(DiagramRepositoryStore.class);
        final AbstractProcess proc = (AbstractProcess) store.getDiagram("TestExportProcessBarWithDocument","1.0").getContent().getElements().get(0);

        /*Export to the specified folder*/
        File targetFolder = new File(System.getProperty("java.io.tmpdir")+File.separator+"testExportBar");
        PlatformUtil.delete(targetFolder, Repository.NULL_PROGRESS_MONITOR);
        targetFolder.mkdirs();
        ExportBarOperation ebo = new ExportBarOperation();
        ebo.addProcessToDeploy(proc);
        ebo.setTargetFolder(targetFolder.getAbsolutePath());
        ebo.setConfigurationId("Local");
        IStatus exportStatus = ebo.run(new NullProgressMonitor());
        assertTrue("Export in bar has failed.", exportStatus.isOK());
        
        File generatedBarFile = ebo.getGeneratedBars().get(0);

        /*Check that attachment is in the bar*/
        ZipInputStream generatedBarStream = new ZipInputStream(new FileInputStream(generatedBarFile));
        ZipEntry barEntry;
        while((barEntry = generatedBarStream.getNextEntry()) != null){
            if(barEntry.getName().contains("documents")){
                return;
            }
        }

        fail("There is no attachment in the genrated bar.");

    }

    public void testExportProcessBarApplicationResources() throws Exception{
        /*Import the processus*/
        URL url = getClass().getResource("TestExportBarWithApplicationResources-1.0.bos");
        url = FileLocator.toFileURL(url);
        File barToImport = new File(url.getFile());

        ImportBosArchiveOperation op = new ImportBosArchiveOperation();
        op.setArchiveFile(barToImport.getAbsolutePath());
        op.run(Repository.NULL_PROGRESS_MONITOR);
        /*Retrieve the AbstractProcess*/
        DiagramRepositoryStore store = (DiagramRepositoryStore) RepositoryManager.getInstance().getRepositoryStore(DiagramRepositoryStore.class);
        final AbstractProcess proc = (AbstractProcess) store.getDiagram("TestExportBarWithApplicationResources","1.0").getContent().getElements().get(0);

        /*Export to the specified folder*/
        File targetFolder = new File(System.getProperty("java.io.tmpdir")+File.separator+"testExportBar");
        PlatformUtil.delete(targetFolder, Repository.NULL_PROGRESS_MONITOR);
        targetFolder.mkdirs();
        
        ExportBarOperation ebo = new ExportBarOperation();
        ebo.addProcessToDeploy(proc);
        ebo.setTargetFolder(targetFolder.getAbsolutePath());
        ebo.setConfigurationId("Local");
        IStatus exportStatus = ebo.run(new NullProgressMonitor());
        assertTrue("Export in bar has failed.", exportStatus.isOK());
        
        File generatedBarFile = ebo.getGeneratedBars().get(0);

        /*Check that attachment is in the bar*/
        ZipInputStream generatedBarStream = new ZipInputStream(new FileInputStream(generatedBarFile));
        ZipEntry barEntry;
        boolean formsFolderExists = false ;
        boolean formsXmlExists = false ;
        boolean resourcesExists = false ;
        boolean templateExists = false ;
        boolean dependenciesExists = false ;
        boolean validatorExists = false ;
        while((barEntry = generatedBarStream.getNextEntry()) != null){
            if(barEntry.getName().contains("resources/forms/")){
                formsFolderExists = true ;
            }
            if(barEntry.getName().contains("resources/forms/forms.xml")){
                formsXmlExists = true ;
            }
            if(barEntry.getName().contains("resources/forms/html")){
                templateExists = true ;
            }
            if(barEntry.getName().contains("resources/forms/resources/application/css")){
                resourcesExists = true ;
            }
            if(barEntry.getName().contains("resources/forms/validators")){
                validatorExists = true ;
            }
            if(barEntry.getName().contains("resources/forms/lib")){
                dependenciesExists = true ;
            }

        }

        assertTrue("forms folder not found in BAR",formsFolderExists) ;
        assertTrue("forms.xml folder not found in BAR",formsXmlExists) ;
        assertTrue("resources not found in BAR",resourcesExists) ;
        assertTrue("template folder not found in BAR",templateExists) ;
        assertTrue("validator folder not found in BAR",validatorExists) ;
        assertTrue("dependencies folder not found in BAR",dependenciesExists) ;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        File targetFolder = new File(System.getProperty("java.io.tmpdir")+File.separator+"testExportBar");
        PlatformUtil.delete(targetFolder, Repository.NULL_PROGRESS_MONITOR);
    }

}
