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
package org.bonitasoft.studio.common.repository.filestore;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.CommonRepositoryPlugin;
import org.bonitasoft.studio.common.repository.Repository;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.model.IRepositoryStore;
import org.bonitasoft.studio.pics.Pics;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPart;


/**
 * @author Romain Bioteau
 *
 */
public class PackageFileStore extends AbstractFileStore {

    private final String packageName;

    public PackageFileStore(String packageName, IRepositoryStore parentStore) {
        super("", parentStore);
        this.packageName = packageName ;
    }

    @Override
    public String getName() {
        return packageName;
    }

    @Override
    public String getDisplayName() {
        return getName() ;
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.common.repository.model.IRepositoryFileStore#getIcon()
     */
    @Override
    public Image getIcon() {
        return Pics.getImage("package.gif", CommonRepositoryPlugin.getDefault());
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.common.repository.model.IRepositoryFileStore#getContent()
     */
    @Override
    public IFolder getContent() {
        return getResource();
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.common.repository.model.IRepositoryFileStore#getResource()
     */
    @Override
    public IFolder getResource() {
        final IPackageFragment packageFragment = getPackageFragment();
        if(packageFragment != null){
            return (IFolder) packageFragment.getResource() ;
        }

        return null;
    }

    public void exportAsJar(String absoluteTargetFilePath, boolean includeSources) {
        final JarPackageData jarPackakeData = new JarPackageData() ;
        final IPackageFragment packageFragment = getPackageFragment() ;
        if(packageFragment==null){
        	throw new RuntimeException("Error while exporting as JAR : package Fragment is null");
        }
        
        	final List<Object> toExport = new ArrayList<Object>() ;
        	try {
        		for(ICompilationUnit cunit : packageFragment.getCompilationUnits()){
        			toExport.add(cunit) ;
        		}
        	} catch (Exception e2) {
        		BonitaStudioLog.error(e2) ;
        	}

        	jarPackakeData.setBuildIfNeeded(true) ;
        	jarPackakeData.setJarLocation(Path.fromOSString(absoluteTargetFilePath)) ;
        	jarPackakeData.setCompress(true) ;
        	jarPackakeData.setElements(toExport.toArray(new Object[toExport.size()])) ;
        	jarPackakeData.setExportErrors(true) ;
        	jarPackakeData.setDeprecationAware(true) ;
        	jarPackakeData.setExportClassFiles(true) ;
        	jarPackakeData.setExportJavaFiles(includeSources) ;
        	jarPackakeData.setGenerateManifest(true) ;
        	jarPackakeData.setOverwrite(true) ;

        	final IJarExportRunnable runnable = jarPackakeData.createJarExportRunnable(null) ;
        	try {
        		runnable.run(Repository.NULL_PROGRESS_MONITOR) ;
        	} catch (Exception e){
        		BonitaStudioLog.error(e) ;
        	}
        
    }

    public IPackageFragment getPackageFragment() {
        IJavaProject project = RepositoryManager.getInstance().getCurrentRepository().getJavaProject() ;
        try {
            return project.findPackageFragment(getParentStore().getResource().getFullPath().append(packageName.replace(".", "/")));

        } catch (JavaModelException e) {
            BonitaStudioLog.error(e) ;
        }
        return null;
    }

    public List<IFile> getChildren() {
        List<IFile> result = new ArrayList<IFile>() ;
        retrieveChildren(getResource(),result) ;
        return result ;
    }

    private void retrieveChildren(IResource resource, List<IFile> result) {
        try{
            if(resource instanceof IFolder){
                for(IResource r : ((IFolder) resource).members()){
                    retrieveChildren(r, result) ;
                }
            }else if(resource instanceof IFile){
                result.add((IFile) resource) ;
            }
        }catch (Exception e) {
            BonitaStudioLog.error(e) ;
        }
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.common.repository.filestore.AbstractFileStore#doSave(java.lang.Object)
     */
    @Override
    protected void doSave(Object content) {

    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.common.repository.filestore.AbstractFileStore#doOpen()
     */
    @Override
    protected IWorkbenchPart doOpen() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.bonitasoft.studio.common.repository.filestore.AbstractFileStore#doClose()
     */
    @Override
    protected void doClose() {

    }


}
