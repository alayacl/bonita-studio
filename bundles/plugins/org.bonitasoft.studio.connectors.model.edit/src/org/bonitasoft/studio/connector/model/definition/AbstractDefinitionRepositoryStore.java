package org.bonitasoft.studio.connector.model.definition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.Repository;
import org.bonitasoft.studio.common.repository.filestore.EMFFileStore;
import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
import org.bonitasoft.studio.common.repository.store.AbstractEMFRepositoryStore;
import org.bonitasoft.studio.connector.model.definition.util.ConnectorDefinitionAdapterFactory;
import org.bonitasoft.studio.connector.model.definition.util.ConnectorDefinitionResourceImpl;
import org.bonitasoft.studio.connector.model.definition.util.ConnectorDefinitionXMLProcessor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.eclipse.emf.edapt.history.Release;
import org.eclipse.emf.edapt.migration.MigrationException;
import org.eclipse.emf.edapt.migration.execution.Migrator;
import org.eclipse.emf.edapt.migration.execution.ValidationLevel;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.osgi.framework.Bundle;

public abstract class AbstractDefinitionRepositoryStore<T extends EMFFileStore> extends AbstractEMFRepositoryStore<T> implements IDefinitionRepositoryStore {

    @Override
    public List<ConnectorDefinition> getDefinitions() {
        final List<ConnectorDefinition> result = new ArrayList<ConnectorDefinition>();
        for(IRepositoryFileStore fileStore : getChildren()){
            ConnectorDefinition def ;
            try{
                def = (ConnectorDefinition) fileStore.getContent();
            }catch(Exception e){
                def = ConnectorDefinitionFactory.eINSTANCE.createUnloadableConnectorDefinition();
                def.setId(fileStore.getName());
                def.setVersion("");
            }
            if(def == null){
            	  def = ConnectorDefinitionFactory.eINSTANCE.createUnloadableConnectorDefinition();
                  def.setId(fileStore.getName());
                  def.setVersion("");
            }
            result.add(def) ;
        }
        return result ;
    }

    @Override
    public ConnectorDefinition getDefinition(String id, String version) {
        for(ConnectorDefinition def : getDefinitions()){
            if(def.getId().equals(id) && def.getVersion().equals(version)){
                return def ;
            }
        }
        return null;
    }

    @Override
    public ConnectorDefinition getDefinition(String id, String version,Collection<ConnectorDefinition> existingDefinitions) {
        for(ConnectorDefinition def : existingDefinitions){
            if(def.getId().equals(id) && def.getVersion().equals(version)){
                return def ;
            }
        }
        return null;
    }

    @Override
    public List<T> getChildren() {
        final List<T> result = super.getChildren();
        Enumeration<URL> connectorDefs = getBundle().findEntries(getName(), "*.def", false);
        if( connectorDefs != null ){
            while (connectorDefs.hasMoreElements()) {
                URL url = connectorDefs.nextElement();
                String[] segments = url.getFile().split("/") ;
                String fileName = segments[segments.length-1] ;
                if(fileName.lastIndexOf(".") != -1){
                    String extension = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length()) ;
                    if(getCompatibleExtensions().contains(extension)){
                        result.add(getDefFileStore(url)) ;
                    }
                }
            }
        }
        return result ;
    }

    @Override
    public T getChild(String fileName) {
        T file = super.getChild(fileName) ;
        if(file == null){
            URL url = getBundle().getResource(getName()+ "/" +fileName);
            if(url != null){
                return getDefFileStore(url) ;
            }else{
                return null ;
            }
        }else{
            return file ;
        }

    }

    @Override
    protected void addAdapterFactory(ComposedAdapterFactory adapterFactory) {
        adapterFactory.addAdapterFactory(new ConnectorDefinitionAdapterFactory());
    }

    protected abstract T getDefFileStore(URL url);

    protected abstract Bundle getBundle();

	protected void performMigration(Migrator migrator, URI resourceURI, Release release) throws MigrationException {
		migrator.setLevel(ValidationLevel.NONE);
		ResourceSet rSet = migrator.migrateAndLoad(
				Collections.singletonList(resourceURI), release,
				null, Repository.NULL_PROGRESS_MONITOR);
		if(!rSet.getResources().isEmpty()){
			FileOutputStream fos = null;
			try{
				ConnectorDefinitionResourceImpl r = (ConnectorDefinitionResourceImpl) rSet.getResources().get(0);
				Resource resource = new XMLResourceImpl(resourceURI) ;
				DocumentRoot root = ConnectorDefinitionFactory.eINSTANCE.createDocumentRoot() ;
				final ConnectorDefinition definition = EcoreUtil.copy(((DocumentRoot)r.getContents().get(0)).getConnectorDefinition());
				root.setConnectorDefinition(definition);
				resource.getContents().add(root) ;
				Map<String, String> options = new HashMap<String, String>() ;
				options.put(XMLResource.OPTION_ENCODING, "UTF-8");
				options.put(XMLResource.OPTION_XML_VERSION, "1.0");
				File target = new File(resourceURI.toFileString());
				fos = new FileOutputStream(target)  ;
				new ConnectorDefinitionXMLProcessor().save(fos, resource, options)  ;
			}catch (Exception e) {
				BonitaStudioLog.error(e, "org.bonitasoft.studio.connectors.model.edit");
			}finally{
				if(fos != null){
					try {
						fos.close() ;
					} catch (IOException e) {
						BonitaStudioLog.error(e,"org.bonitasoft.studio.connectors.model.edit");
					}
				}
			}
		}
	}
    
}
