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
package org.bonitasoft.studio.common.perspectives;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * 
 * Must extends this class to enable the automatic switch between perspectives depending of the editor type
 * 
 * @author Baptiste Mesta
 * @author Aurelien Pupier
 */
public abstract class AbstractPerspectiveFactory implements IPerspectiveFactory {

	/**
	 * value of VIEW_KIND to tell the view is tabbed
	 */
	public static final String BONITA_TABS = "bonita.tabs";
	/**
	 * value of VIEW_KIND to tell the view to show toolbar
	 */
	public static final String BONITA_VIEWS_TOOLBAR = "bonita.view.toolbar";
	/**
	 * value of VIEW_KIND to tell the view to hide toolbar
	 */
	public static final String BONITA_VIEWS_NO_TOOLBAR = "bonita.view.no.toolbar";
	/**
	 * property to set the kind of view it is (with or without tabs)
	 */
	public static final String VIEW_KIND = "viewKind";
	public static final String PERSPECTIVE_ID = "org.bonitasoft.studio.application.perspective";
	
	public static final String BONITA_OVERVIEW = "bonita.overview";
	/**
	 * returns true if the perspective must be activated when opening the editor part given in param
	 * 
	 * @param part
	 * 			the editor part that is brought to top
	 * @return
	 * 		true if perspective must be activated, false otherwise
	 */
	public abstract boolean isRelevantFor(IEditorPart part);
	
	/**
	 * 
	 * @return the id of the perspective
	 */
	public abstract String getID();
	
}
