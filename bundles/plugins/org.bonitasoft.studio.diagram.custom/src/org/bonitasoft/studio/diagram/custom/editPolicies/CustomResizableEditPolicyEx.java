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
package org.bonitasoft.studio.diagram.custom.editPolicies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.studio.common.diagram.tools.FiguresHelper;
import org.bonitasoft.studio.common.figures.CustomSVGFigure;
import org.bonitasoft.studio.common.figures.EventSubprocessFigureWrapper;
import org.bonitasoft.studio.common.gmf.tools.GMFTools;
import org.bonitasoft.studio.diagram.custom.parts.CustomLaneEditPart;
import org.bonitasoft.studio.diagram.custom.parts.CustomPoolEditPart;
import org.bonitasoft.studio.diagram.custom.parts.CustomSubProcessEvent2EditPart;
import org.bonitasoft.studio.diagram.custom.parts.CustomSubprocessEventCompartmentEditPart;
import org.bonitasoft.studio.model.process.Container;
import org.bonitasoft.studio.model.process.Lane;
import org.bonitasoft.studio.model.process.Pool;
import org.bonitasoft.studio.model.process.SubProcessEvent;
import org.bonitasoft.studio.model.process.diagram.edit.parts.SubProcessEvent2EditPart;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.handles.ResizableHandleKit;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeCompartmentEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ResizableEditPolicyEx;
import org.eclipse.gmf.runtime.draw2d.ui.figures.FigureUtilities;
import org.eclipse.gmf.runtime.draw2d.ui.graphics.ColorRegistry;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;
import org.eclipse.gmf.runtime.notation.DrawerStyle;
import org.eclipse.gmf.runtime.notation.FillStyle;
import org.eclipse.gmf.runtime.notation.LineStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.swt.graphics.Color;

/**
 * @author Romain Bioteau
 *
 */
public class CustomResizableEditPolicyEx extends ResizableEditPolicyEx implements ZoomListener {

	public static final String MOVE_COMPARTMENT_CHILDREN = "MOVE_COMPARTMENT_CHILDREN";
	private ZoomManager zoomManager;


	public CustomResizableEditPolicyEx(){}

	@Override
	public void activate() {
		super.activate();
		this.zoomManager = ((DiagramRootEditPart) getHost().getRoot()).getZoomManager();
		zoomManager.addZoomListener(this) ;
	}

	@Override
	public void deactivate() {
		super.deactivate();
		if(zoomManager != null){
			zoomManager.removeZoomListener(this) ;
		}
	}
	@Override
	protected IFigure createDragSourceFeedbackFigure() {
		boolean isSubprocessEvent = false ;
		if(((IGraphicalEditPart) getHost()).resolveSemanticElement() instanceof SubProcessEvent){
			isSubprocessEvent = true ;
		}
		if(((IGraphicalEditPart) getHost()).resolveSemanticElement() instanceof Pool 
				|| ((IGraphicalEditPart) getHost()).resolveSemanticElement() instanceof Lane){
			RectangleFigure r = new RectangleFigure();
			r.setAlpha(30) ;
			r.setLineStyle(Graphics.LINE_DOT);
			r.setForegroundColor(ColorConstants.black);
			r.setBackgroundColor(ColorConstants.black);
			r.setBounds(getInitialFeedbackBounds());
			addFeedback(r);
			return r;
		}else if(isSubprocessEvent){
			EventSubprocessFigureWrapper figure = new EventSubprocessFigureWrapper() ;
			Color background = ColorRegistry.getInstance().getColor(((FillStyle) ((IGraphicalEditPart)getHost()).getNotationView().getStyle(NotationPackage.eINSTANCE.getFillStyle())).getFillColor()) ;
			Color foreground = ColorRegistry.getInstance().getColor(((LineStyle) ((IGraphicalEditPart)getHost()).getNotationView().getStyle(NotationPackage.eINSTANCE.getLineStyle())).getLineColor()) ; 
			figure.setForegroundColor(foreground) ;
			figure.setBackgroundColor(background) ;
			figure.setOpaque(false) ;
			figure.setAlpha(50) ;
			addFeedback(figure);
			return figure ;
		}else{
			Rectangle bounds = ((ShapeEditPart)getHost()).getFigure().getBounds().getCopy() ; 
			Color background = ColorRegistry.getInstance().getColor(((FillStyle) ((IGraphicalEditPart)getHost()).getNotationView().getStyle(NotationPackage.eINSTANCE.getFillStyle())).getFillColor()) ;
			Color foreground = ColorRegistry.getInstance().getColor(((LineStyle) ((IGraphicalEditPart)getHost()).getNotationView().getStyle(NotationPackage.eINSTANCE.getLineStyle())).getLineColor()) ; 
			IFigure res = FiguresHelper.getSelectedFigure(((ShapeEditPart)getHost()).resolveSemanticElement().eClass(),bounds.width,bounds.height,foreground,background);
			if(res != null){
				addFeedback(res);
			}
			return res;
		}
	}

	@Override
	protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
		IFigure feedback = getDragSourceFeedbackFigure();
		Rectangle bounds = getInitialFeedbackBounds() ;
		bounds.getSize().performScale(zoomManager.getZoom()) ; 
		PrecisionRectangle rect = new PrecisionRectangle(bounds);
		getHostFigure().translateToAbsolute(rect);
		rect.translate(request.getMoveDelta());
		rect.resize(request.getSizeDelta());

		IFigure f = getHostFigure();

		Dimension min = f.getMinimumSize().getCopy();
		min.performScale(zoomManager.getZoom()) ;


		Dimension max = f.getMaximumSize().getCopy();
		IMapMode mmode = MapModeUtil.getMapMode(f);
		min.height = mmode.LPtoDP(min.height);
		min.width = mmode.LPtoDP(min.width);
		max.height = mmode.LPtoDP(max.height);
		max.width = mmode.LPtoDP(max.width);

		if (min.width>rect.width)
			rect.width = min.width;
		else if (max.width < rect.width)
			rect.width = max.width;

		if (min.height>rect.height)
			rect.height = min.height;
		else if (max.height < rect.height)
			rect.height = max.height;

		if(request.getType().equals(REQ_RESIZE)){
			if(!isReizeValid(request)){
				feedback.getBounds().setLocation(rect.x, rect.y) ;
				if(feedback instanceof CustomSVGFigure){
					((CustomSVGFigure) feedback).setColor(FigureUtilities.lighter(ColorConstants.red), 
							ColorRegistry.getInstance().getColor(((FillStyle) ((IGraphicalEditPart)getHost()).getNotationView().getStyle(NotationPackage.eINSTANCE.getFillStyle())).getFillColor())) ;
				}else{
					feedback.setForegroundColor(FigureUtilities.lighter(ColorConstants.red)) ;
				}

				return ;
			}else{
				Color foreground = ColorRegistry.getInstance().getColor(((LineStyle) ((IGraphicalEditPart)getHost()).getNotationView().getStyle(NotationPackage.eINSTANCE.getLineStyle())).getLineColor()) ; 
				if(feedback instanceof CustomSVGFigure){
					((CustomSVGFigure) feedback).setColor(foreground, 
							ColorRegistry.getInstance().getColor(((FillStyle) ((IGraphicalEditPart)getHost()).getNotationView().getStyle(NotationPackage.eINSTANCE.getFillStyle())).getFillColor())) ;
				}else{
					feedback.setForegroundColor(foreground) ;
				}
			}
		}

		feedback.translateToRelative(rect);
	
		feedback.setBounds(rect);


	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected List createSelectionHandles() {
		List list = new ArrayList();
		if(((IGraphicalEditPart) getHost()).resolveSemanticElement() instanceof Pool 
				|| ((IGraphicalEditPart) getHost()).resolveSemanticElement() instanceof Lane){
			ResizableHandleKit.addMoveHandle((GraphicalEditPart) getHost(),
					list);
			if(!(((IGraphicalEditPart) getHost()).resolveSemanticElement() instanceof Lane)){
				ResizableHandleKit.addHandle((GraphicalEditPart) getHost(),
						list, PositionConstants.EAST);
				Container container = (Container) (((IGraphicalEditPart) getHost()).resolveSemanticElement()) ; 
				if(container.getElements().isEmpty() || !(container.getElements().get(0) instanceof Lane)){
					ResizableHandleKit.addHandle((GraphicalEditPart) getHost(),
							list, PositionConstants.SOUTH);
				}
			}else{
				ResizableHandleKit.addHandle((GraphicalEditPart) getHost(),
						list, PositionConstants.SOUTH);
			}
			return list; 
		}else{
			if(((IGraphicalEditPart) getHost()).resolveSemanticElement() instanceof SubProcessEvent){
				for(Object child : getHost().getChildren()){
					if(child instanceof ShapeCompartmentEditPart){
						if(((ShapeCompartmentEditPart) child).getCompartmentFigure().isExpanded()){
							list.add(new CustomResizeHandle((GraphicalEditPart) getHost(),PositionConstants.SOUTH_EAST,false)) ;
							return list ;
						}
					}
				}

			}

			list.add(new CustomResizeHandle((GraphicalEditPart) getHost(),PositionConstants.SOUTH_EAST)) ;

			return list;
		}
	}

	@Override
	protected void showSelection() {
		if(zoomManager.getZoom() > GMFTools.MINIMAL_ZOOM_DISPLAY){
			super.showSelection();
		}
	}

	@Override
	protected Command getResizeCommand(ChangeBoundsRequest request) {	
		if(isReizeValid(request)){
			return super.getResizeCommand(request);
		}
		return null;
	}


	protected boolean isReizeValid(ChangeBoundsRequest request){
		if(request.getEditParts() != null && !request.getEditParts().isEmpty()){
			IGraphicalEditPart ep = (IGraphicalEditPart) request.getEditParts().get(0) ;
			if( request.getSizeDelta().height <= 0 &&  request.getSizeDelta().width <= 0 && ep.resolveSemanticElement() instanceof SubProcessEvent){
				return checkSubprocessEventCanReduce(request) ;
			}else if( ep.resolveSemanticElement() instanceof Lane){
				if(request.getSizeDelta().height <= 0 && request.getSizeDelta().width <= 0){
					return checkSwimLanesCanReduce(request); 
				}else{
					return true ;
				}
			}else if(ep.resolveSemanticElement() instanceof Pool){
				if(request.getSizeDelta().height <= 0 && request.getSizeDelta().width <= 0){
					return checkSwimLanesCanReduce(request); 
				}else{
					return true ;
				}
			}
			return  !checkOverlapOtherFigures(request);
		}

		return true ;
	}

	protected boolean checkSubprocessEventCanReduce(ChangeBoundsRequest request) {
		for(Object c : getHost().getChildren()){
			if(c instanceof CustomSubprocessEventCompartmentEditPart){
				View view = ((org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart) c).getNotationView();
				if (view != null) {
					DrawerStyle style = (DrawerStyle) view.getStyle(NotationPackage.eINSTANCE.getDrawerStyle());
					if (style != null) {
						if(!style.isCollapsed() && !((Container) ((CustomSubprocessEventCompartmentEditPart)c).resolveSemanticElement()).getElements().isEmpty()){
							Rectangle bounds = request.getTransformedRectangle(getHostFigure().getBounds()) ;
							for(Object child : ((CustomSubprocessEventCompartmentEditPart)c).getChildren()){
								int x =  ((IGraphicalEditPart)child).getFigure().getBounds().x ; 
								int width =  ((IGraphicalEditPart)child).getFigure().getBounds().width ; 
								int y =  ((IGraphicalEditPart)child).getFigure().getBounds().y ; 
								int height =  ((IGraphicalEditPart)child).getFigure().getBounds().height ; 

								if((request.getSizeDelta().height < 0 || request.getSizeDelta().width < 0 ) && (bounds.width +  bounds.x - 20 <= x + width || bounds.height + bounds.y - 30<= y + height)){
									return false ;
								}
							}
						}
					}
				}
			}
		}
		return true ;
	}

	protected boolean checkLaneCanReduce(CustomLaneEditPart ep,Rectangle bounds) {
		for(Object c : ep.getChildren()){
			if(c instanceof ShapeCompartmentEditPart){
				for(Object child : ((ShapeCompartmentEditPart)c).getChildren()){
					Rectangle childBound = ((IGraphicalEditPart)child).getFigure().getBounds().getCopy() ;
					childBound.x  = childBound.x + bounds.x ;
					childBound.y  = childBound.y + bounds.y ;
					int x =  childBound.x ; 
					int width =  childBound.width ; 
					int y =  childBound.y ; 
					int height =  childBound.height ; 
					if((bounds.width +  bounds.x  <= x + width + 15 || bounds.height + bounds.y <= y + height + 15)){
						return false ;
					}
				}
			}
		}
		return true ;
	}
	protected boolean checkSwimLanesCanReduce(ChangeBoundsRequest request) {
		if(getHost() instanceof CustomLaneEditPart){
			Rectangle bounds = request.getTransformedRectangle(getHostFigure().getBounds()) ;
			for(Object c : getHost().getChildren()){
				if(c instanceof ShapeCompartmentEditPart){
					for(Object child : ((ShapeCompartmentEditPart)c).getChildren()){
						Rectangle childBound = ((IGraphicalEditPart)child).getFigure().getBounds().getCopy() ;
						childBound.x  = childBound.x + bounds.x ;
						childBound.y  = childBound.y + bounds.y ;
						int x =  childBound.x ; 
						int width =  childBound.width ; 
						int y =  childBound.y ; 
						int height =  childBound.height ; 
						if((request.getSizeDelta().height < 0 || request.getSizeDelta().width < 0 ) && (bounds.width +  bounds.x  <= x + width + 15 || bounds.height + bounds.y <= y + height + 15)){
							return false ;
						}
					}
				}
			}

		}else if(getHost() instanceof CustomPoolEditPart){
			Rectangle bounds = request.getTransformedRectangle(getHostFigure().getBounds()) ;
			for(Object c : getHost().getChildren()){
				if(c instanceof ShapeCompartmentEditPart){
					for(Object child : ((ShapeCompartmentEditPart)c).getChildren()){
						if(child instanceof CustomLaneEditPart){
							boolean allLaneCanReduce = true ; 
							for(Object laneEp : ((ShapeCompartmentEditPart)c).getChildren()){
								allLaneCanReduce = checkLaneCanReduce((CustomLaneEditPart) laneEp,bounds) ;
								if(!allLaneCanReduce) break ;
							}
							return (request.getSizeDelta().height < 0 || request.getSizeDelta().width < 0 ) && allLaneCanReduce ;
						}else{
							Rectangle childBound = ((IGraphicalEditPart)child).getFigure().getBounds().getCopy() ;
							childBound.x  = childBound.x + bounds.x ;
							childBound.y  = childBound.y + bounds.y ;
							int x =  childBound.x ; 
							int width =  childBound.width ; 
							int y =  childBound.y ; 
							int height =  childBound.height ; 
							if((request.getSizeDelta().height < 0 || request.getSizeDelta().width < 0 ) && (bounds.width +  bounds.x  <= x + width + 15 || bounds.height + bounds.y <= y + height + 15)){
								return false ;
							}
						}
					}
				}
			}
		}

		return true ;
	}

	protected boolean checkOverlapOtherFigures(ChangeBoundsRequest request) {
		if(FiguresHelper.AVOID_OVERLAP_ENABLE){
			getHost().getParent().refresh();
			for(Object c : getHost().getParent().getChildren()){
				if(c instanceof IGraphicalEditPart){
					if(!c.equals(getHost()) && ((IGraphicalEditPart) c).getFigure().intersects(request.getTransformedRectangle(getHostFigure().getBounds()))){
						return true ;
					}
				}
			}
		}
		return false ;
	}

	public void zoomChanged(double zoom) {
		hideSelection() ;
	}

	public void performCollapse() {
		hideSelection() ;
	}

	@Override
	protected Command getMoveCommand(ChangeBoundsRequest request) {
		if(request.getEditParts() != null && !request.getEditParts().isEmpty() && request.getEditParts().get(0) instanceof CustomLaneEditPart){
			return null ;// DON'T MOVE A LANE
		}

		if(!request.getEditParts().isEmpty() && request.getEditParts().get(0) instanceof CustomSubProcessEvent2EditPart && checkOverlapOtherFigures(request) && request.getEditParts() != null ){
			return null ; // DON'T MOVE A SUBPROCESS EVENT IF LOCATION NOT VALID
		}
		
		CompoundCommand cc = new CompoundCommand("Move");
		cc.add(super.getMoveCommand(request)) ;
		if(request.getEditParts() != null && !request.getEditParts().isEmpty()){
			for(Object ep : request.getEditParts()){
				if(ep instanceof SubProcessEvent2EditPart){
					for(Object c : ((SubProcessEvent2EditPart) ep).getChildren()){
						if(c instanceof CustomSubprocessEventCompartmentEditPart){
							ChangeBoundsRequest childRequest = new ChangeBoundsRequest(REQ_MOVE_CHILDREN) ;
							List eps = new ArrayList();
							for(Object child : ((CustomSubprocessEventCompartmentEditPart) c).getChildren()){
								eps.add(child) ;
							}
							childRequest.setEditParts(eps) ;
							childRequest.setMoveDelta(request.getMoveDelta()) ;
							HashMap<Object, Object> map = new HashMap<Object, Object>() ;
							map.put(MOVE_COMPARTMENT_CHILDREN, MOVE_COMPARTMENT_CHILDREN) ;
							childRequest.setExtendedData(map) ;
							if(!eps.isEmpty()){
								cc.add(((CustomSubprocessEventCompartmentEditPart) c).getCommand(childRequest)) ;
							}
						}
					}
				}
			}
		}
		return cc.unwrap();



	}

}
