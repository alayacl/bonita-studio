/**
 * 
 */
package org.bonitasoft.studio.application.advisor;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;

import org.bonitasoft.studio.common.ZipInputStreamIFileFriendly;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.jface.FileActionDialog;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.common.repository.model.IRepository;
import org.bonitasoft.studio.common.repository.preferences.RepositoryPreferenceConstant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.PlatformUI;

/**
 * @author Romain Bioteau
 *
 */
public class InitWorkspaceAdvisor extends InstallerApplicationWorkbenchAdvisor {

	/* (non-Javadoc)
	 * @see org.bonitasoft.studio.application.advisor.InstallerApplicationWorkbenchAdvisor#executePostStartupHandler()
	 */
	@Override
	protected void executePostStartupHandler() {
		File[] repositoryToImport = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(".bos");
			}
		}) ;

		if(repositoryToImport != null && repositoryToImport.length > 0){
			for(File workspaceArchive : repositoryToImport){
				String repositoryName = workspaceArchive.getName().substring(0,workspaceArchive.getName().lastIndexOf(".")) ;
				IRepository repository = RepositoryManager.getInstance().getRepository(repositoryName) ;
				if(!repositoryName.equals(RepositoryPreferenceConstant.DEFAULT_REPOSITORY_NAME) && repository == null){
					RepositoryManager.getInstance().setRepository(repositoryName);
					repository = RepositoryManager.getInstance().getRepository(repositoryName) ;
				}
				if(repository != null){
					try{
						repository.importFromArchive(workspaceArchive,false) ;
						workspaceArchive.delete() ;
					}catch (Exception e) {
						BonitaStudioLog.error(e) ;
					}
				}
			}
		}

		PlatformUI.getWorkbench().close() ;

	}

}
