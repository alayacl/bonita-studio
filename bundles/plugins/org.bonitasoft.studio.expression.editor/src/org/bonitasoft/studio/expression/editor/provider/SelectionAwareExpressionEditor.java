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
package org.bonitasoft.studio.expression.editor.provider;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.expression.editor.ExpressionEditorPlugin;
import org.bonitasoft.studio.model.expression.Expression;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.jface.dialogs.DialogTray;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Romain Bioteau
 *
 */
public abstract class SelectionAwareExpressionEditor implements IExpressionEditor {

    private final List<Listener> listeners = new ArrayList<Listener>();

    @Override
    public void addListener(Listener listener) {
        listeners.add(listener) ;
    }

    @Override
    public void dispose() {
        listeners.clear() ;
    }

    protected void fireSelectionChanged(){
        for(Listener l : listeners){
            l.handleEvent(new Event()) ;
        }
    }

    @Override
    public List<Listener> getListeners(){
        return listeners ;
    }

    @Override
    public boolean provideDialogTray() {
        return false;
    }

    @Override
    public DialogTray createDialogTray() {
        return null;
    }

    @Override
    public IObservable getContentObservable() {
        return null;
    }
    
    @Override
    public Control createExpressionEditor(Composite contentComposite,
    		boolean isPassword) {
    	return createExpressionEditor(contentComposite);
    }
    
    protected boolean compatibleReturnType(Expression inputExpression,Expression e) {
		final String currentReturnType = inputExpression.getReturnType();
		final String expressionReturnType = e.getReturnType();
		if(currentReturnType.equals(expressionReturnType)){
			return true;
		}
		try{
			Class<?> currentReturnTypeClass = Class.forName(currentReturnType);
			Class<?> expressionReturnTypeClass = Class.forName(expressionReturnType);
			return currentReturnTypeClass.isAssignableFrom(expressionReturnTypeClass);
		}catch (Exception ex) {
			BonitaStudioLog.debug("Failed to determine the compatbility between "+expressionReturnType+" and "+currentReturnType, ExpressionEditorPlugin.PLUGIN_ID);
		}
		return true;
	}

}
