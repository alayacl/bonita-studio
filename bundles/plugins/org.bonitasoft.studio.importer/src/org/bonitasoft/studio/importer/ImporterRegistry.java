/**
 * Copyright (C) 2010-2011 BonitaSoft S.A.
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
package org.bonitasoft.studio.importer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.studio.common.extension.BonitaStudioExtensionRegistryManager;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * @author Mickael Istria
 *
 */
public class ImporterRegistry {

	private static final String EXTENSION_POINT = "org.bonitasoft.studio.importer.import";
	private static final String CLASS_FIELD = "importerFactoryClass";
	private static ImporterRegistry INSTANCE = null;
	
	
	private List<ImporterFactory> availableImportFactories;
	
	private ImporterRegistry() {
		availableImportFactories = new ArrayList<ImporterFactory>();
		processExtensionPoint();
	}
	
	/**
	 * 
	 */
	private void processExtensionPoint() {
		for (IConfigurationElement ext : BonitaStudioExtensionRegistryManager.getInstance().getConfigurationElements(EXTENSION_POINT)) {
			try {
				ImporterFactory factory = (ImporterFactory)ext.createExecutableExtension(CLASS_FIELD);
				factory.configure(ext);
				if(factory.isEnabled()){
					availableImportFactories.add(factory);
				}
			} catch (Exception ex) {
				BonitaStudioLog.error(ex);
			}
		}
		
	}

	public static synchronized ImporterRegistry getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ImporterRegistry();
		}
		return INSTANCE;
	}
	
	public ToProcProcessor createImporterFor(String resourceName, InputStream is) {
		for (ImporterFactory factory : availableImportFactories) {
			if (factory.appliesTo(resourceName, is)) {
				return factory.createProcessor(resourceName);
			}
		}
		return null;
	}

	/**
	 * @return
	 */
	public List<ImporterFactory> getAllAvailableImports() {
		return this.availableImportFactories;
	}
}
