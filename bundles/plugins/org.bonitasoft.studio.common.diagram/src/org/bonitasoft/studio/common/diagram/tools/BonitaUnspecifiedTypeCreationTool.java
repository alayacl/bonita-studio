/**
 * Copyright (C) 2009 BonitaSoft S.A.
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
package org.bonitasoft.studio.common.diagram.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bonitasoft.studio.model.process.Lane;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.model.process.SubProcessEvent;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.handles.HandleBounds;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
import org.eclipse.gmf.runtime.diagram.ui.commands.DeferredCreateConnectionViewAndElementCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.SetBoundsCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.internal.commands.SetConnectionBendpointsCommand;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramCommandStack;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest.ConnectionViewAndElementDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest.ViewAndElementDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.tools.UnspecifiedTypeCreationTool;
import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.IHintedType;
import org.eclipse.gmf.runtime.notation.Connector;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;


/**
 * @author Mickael Istria
 * @author Aurelien Pupier : improve resource management (free handles)
 *@author Romain Bioteau
 */
public class BonitaUnspecifiedTypeCreationTool extends UnspecifiedTypeCreationTool {

	private static final int FLAG_ACTIVE = 8;

	private IFigure figure;
	private IFigure layer;
	protected EClass eClass;
	private Image cursorImage;
	private Cursor cursor;
	protected SnapToHelper helper;
	private PrecisionRectangle sourceRectangle, compoundSrcRect;

	private boolean noRedraw = false;

	private double oldZoom;

	/**
	 * @param elementTypes
	 */
	public BonitaUnspecifiedTypeCreationTool(List<?> elementTypes) {
		super(elementTypes);
		if(!elementTypes.get(0).equals(BonitaConnectionTypes.getElementType("org.bonitasoft.studio.diagram.Event_3024"))){ //$NON-NLS-1$
			eClass = ((IElementType) elementTypes.get(0)).getEClass();
		}
	}

	/**
	 * @param elementTypes
	 * @return
	 */
	protected boolean applies() {
		if(eClass != null){
			return (ProcessPackage.Literals.FLOW_ELEMENT.isSuperTypeOf(eClass)|| ProcessPackage.Literals.BOUNDARY_EVENT.isSuperTypeOf(eClass) || ProcessPackage.Literals.SUB_PROCESS_EVENT.equals(eClass))
					&& !(eClass.equals(ProcessPackage.Literals.POOL));
		}else{
			return true ;
		}
	}

	/**
	 * @return
	 */
	private IFigure createImage() {
		CreateUnspecifiedTypeRequest createUnspecifiedTypeRequest = (CreateUnspecifiedTypeRequest) createCreateRequest();
		EClass eClass = ((IElementType) createUnspecifiedTypeRequest.getElementTypes().get(0)).getEClass();
		IFigure svgFigure = FiguresHelper.getSelectedFigure(eClass,-1,-1,null,null);
		return svgFigure ;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.gef.tools.TargetingTool#updateTargetUnderMouse()
	 */
	@Override
	protected boolean updateTargetUnderMouse() {
		if (!isTargetLocked()) {
			EditPart editPart = null;
			if(figure != null){
				final Rectangle copy = figure.getBounds().getCopy();
			//	System.out.println(copy.translate(0, 15));
				final Rectangle shrink = copy;
				for(Object v : getCurrentViewer().getEditPartRegistry().keySet()){
					if(v instanceof Connector){
						ConnectionEditPart ep = (ConnectionEditPart) getCurrentViewer().getEditPartRegistry().get(v);
						if(((PolylineConnectionEx)ep.getFigure()).getSimpleBounds().getCopy().expand(new Insets(((PolylineConnectionEx)ep.getFigure()).getLineWidth(), 0, 0, 0)).intersects(shrink)){
							editPart = ep;
						}
					}
				}
			}
			if(editPart == null){
				editPart = getCurrentViewer().findObjectAtExcluding(
						getLocation(),
						getExclusionSet(),
						getTargetingConditional());
				if (editPart != null) {
					editPart = editPart.getTargetEditPart(getTargetRequest());
				}
			}
			boolean changed = getTargetEditPart() != editPart;
			//In order to create additional Lanes in the diagram, we change the target edit part when we want to create a
			//lane and when the mouse is already over a lane
			if(eClass != null){
				if(eClass.equals(ProcessPackage.eINSTANCE.getLane())
						&& ((IGraphicalEditPart)editPart).resolveSemanticElement() instanceof Lane){
					setTargetEditPart(editPart.getParent().getParent());//set the target edit part as the CustomPoolCompartmentEditPart
				}else{
					setTargetEditPart(editPart);
				}
			}else{
				setTargetEditPart(editPart);
			}
			return changed;
		} else {
			return false;
		}
	}


	@Override
	protected boolean handleDrag() {
		if (applies()) {
			updateTargetUnderMouse();
			updateTargetRequest();
			redrawFeedback();
		}
		Point locPoint = getLocation();
		getCreateRequest().setLocation(locPoint);
		return super.handleDrag();
	}

	public void setCursor(Cursor newCursor,boolean noRedraw){
		this.noRedraw = noRedraw ;
		eraseTargetFeedback();
		super.setCursor(newCursor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.tools.CreationTool#handleMove()
	 */
	@Override
	protected boolean handleMove() {
		if (applies()) {
			super.handleMove();
			redrawFeedback();
			if (getTargetEditPart() != null && !(getTargetEditPart() instanceof ConnectionEditPart)) {
				helper = (SnapToHelper) getTargetEditPart().getAdapter(SnapToHelper.class);
			}

			return true;
		} else {
			return super.handleMove();
		}

	}
	
	protected Insets getInsetsFor(CreateUnspecifiedTypeRequest request) {
		IElementType type = (IElementType) request.getElementTypes().get(0);
		final String id = type.getId();
		if(id.contains("Gateway")){
			return new Insets(15, 0, 15, 0);
		}else if(id.contains("Event")){
			return new Insets(0, 0, 0, 0);
		}else{
			return new Insets(15, 0, 25, 0);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void executeCurrentCommand() {
		Command c = getCurrentCommand() ;
		super.executeCurrentCommand();
		CompoundCommand cc = new CompoundCommand("Check Overlap") ;

		final List editparts = new ArrayList();
		Collection objects = DiagramCommandStack.getReturnValues(c) ;
		final EditPart targetEditPart = getTargetEditPart();
		if(targetEditPart instanceof ConnectionEditPart){
			for (Iterator i = objects.iterator(); i.hasNext();) {
				Object object = i.next();
				if (object instanceof ViewAndElementDescriptor) {
					final ViewAndElementDescriptor descriptor = (ViewAndElementDescriptor) object; 
					final ShapeEditPart editPart = (ShapeEditPart) getCurrentViewer().getEditPartRegistry().get(descriptor.getAdapter(View.class));
					final ReconnectRequest reconnect = new ReconnectRequest(RequestConstants.REQ_RECONNECT_TARGET);
					reconnect.setConnectionEditPart((ConnectionEditPart) targetEditPart);
					reconnect.setTargetEditPart(editPart);
					Command reconnectCommand = 	editPart.getCommand(reconnect);
					CreateConnectionViewAndElementRequest connectionRequest = null;
					DeferredCreateConnectionViewAndElementCommand connectionCommand = null;
					connectionRequest = new CreateConnectionViewAndElementRequest(BonitaConnectionTypes.getElementType("org.bonitasoft.studio.diagram.SequenceFlow_4001"),
							((IHintedType) BonitaConnectionTypes.getElementType("org.bonitasoft.studio.diagram.SequenceFlow_4001")).getSemanticHint(), getPreferencesHint());

					connectionCommand = new DeferredCreateConnectionViewAndElementCommand(connectionRequest, descriptor, ((ConnectionEditPart)targetEditPart).getTarget(), editPart.getViewer());
					CompoundCommand coupound = 	new CompoundCommand();
					coupound.add(reconnectCommand);
					coupound.add(new ICommandProxy(connectionCommand));
					executeCommand(coupound);
					if(connectionRequest != null){
						ConnectionViewAndElementDescriptor connectionDescriptor = (ConnectionViewAndElementDescriptor) connectionRequest.getNewObject() ;
						Connector edge = (Connector) connectionDescriptor.getAdapter(Edge.class) ;
						org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart connectionEP =  (org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionEditPart) editPart.getViewer().getEditPartRegistry().get(edge);

						if(connectionEP != null){
							SetConnectionBendpointsCommand setConnectionBendPointsCommand = new SetConnectionBendpointsCommand(connectionEP.getEditingDomain());
							setConnectionBendPointsCommand.setEdgeAdapter(connectionDescriptor);	
							PointList bendpoints = new PointList() ;
							bendpoints.addPoint(0, 0) ;
							bendpoints.addPoint(0, 0) ;
							setConnectionBendPointsCommand.setNewPointList(bendpoints, bendpoints.getFirstPoint(), bendpoints.getLastPoint());
							executeCommand(new ICommandProxy(setConnectionBendPointsCommand));
						}
					}
				}
			}

			for (Iterator i = objects.iterator(); i.hasNext();) {
				Object object = i.next();
				if (object instanceof IAdaptable) {
					if (getCurrentViewer() != null) {
						Object editPart = getCurrentViewer().getEditPartRegistry().get(((IAdaptable) object).getAdapter(View.class));
						if (editPart != null) {
							editparts.add(editPart);
						}
					}
				}
			}

			for(Object ep :editparts){
				if(ep instanceof IGraphicalEditPart){
					Location loc = (Location) ((Node)((IGraphicalEditPart) ep).getNotationView()).getLayoutConstraint() ;
					Point newLoc = FiguresHelper.handleCompartmentMargin((IGraphicalEditPart) ep, loc.getX(), loc.getY(),(((IGraphicalEditPart) ep).resolveSemanticElement() instanceof SubProcessEvent)) ;
					cc.add(new ICommandProxy(new SetBoundsCommand(((IGraphicalEditPart) ep).getEditingDomain(), "Check Overlap", new EObjectAdapter(((IGraphicalEditPart) ep).getNotationView()),newLoc))) ;
				}
			}


			if(!cc.isEmpty()){
				executeCommand(cc) ;
			}
		}

	}

	/**
	 * 
	 */
	protected void redrawFeedback() {
		if(!noRedraw){
			calculateCursor();
			if(getTargetEditPart() != null){
				layer = LayerManager.Helper.find(getTargetEditPart()).getLayer(LayerConstants.HANDLE_LAYER);
				IFigure parentFigure = layer;
				Point location = ((CreateRequest)getTargetRequest()).getLocation();
				FiguresHelper.translateToAbsolute(parentFigure, location);
				Command command = getCommand();
				if(figure != null) {
					figure.setLocation(location);
				}
				if (command != null && command.canExecute()) {
					if (!layer.getChildren().contains(figure)) {
						if(figure != null) {
							layer.add(figure);
						}
					}
					showTargetCompartmentFeedback();
				} else {
					if (layer.getChildren().contains(figure)) {
						layer.remove(figure);
					}
				}
			}else{
				eraseTargetFeedback();
			}
		}
	}

	/**
	 * 
	 */
	private void showTargetCompartmentFeedback() {
		//		Color revertColor = null;
		//		if (revertColor == null && getTargetEditPart() instanceof ShapeCompartmentEditPart) {
		//			IFigure targetFigure = getFigure(getTargetEditPart());
		//			getTargetEditPart().getViewer().getVisualPartMap().get(getTargetEditPart());
		//			revertColor =targetFigure.getBackgroundColor(); 
		//			boolean opacity = targetFigure.isOpaque();
		//			targetFigure.setBackgroundColor(FigureUtilities.mixColors(FiguresHelper.COMPARTMENT_FEEDBACK_COLOR,revertColor)); 
		//			targetFigure.setOpaque(true); 
		//		}
		// DO NOTHING
	}

	/**
	 * @param targetEditPart
	 * @return
	 */
	private IFigure getFigure(EditPart targetEditPart) {
		for (Object item :
			targetEditPart.getViewer().getVisualPartMap().entrySet()) {
			Entry entry = (Entry)item;
			if (entry.getValue().equals(targetEditPart)) {
				return (IFigure)entry.getKey();
			}
		}
		return null;
	}

	@Override
	public void eraseTargetFeedback() {
		if (layer != null && applies() && layer.getChildren().contains(figure)) {
			layer.remove(figure);
		}
		hideTargetCompartmentFeedback();
		super.eraseTargetFeedback();

	}

	private void hideTargetCompartmentFeedback() {
		//		  if (revertColor != null) {
		//			  targetFigure.setBackgroundColor(revertColor);
		//			  targetFigure.setOpaque(opacity);
		//			  revertColor = null; 
		//		  }
		// DO NOTHING
	}

	@Override
	protected void updateTargetRequest() {
		CreateRequest req = getCreateRequest();
		req.getExtendedData().clear();
		snapPoint(req);

		if (isInState(STATE_DRAG_IN_PROGRESS) && eClass.getName().equals("Pool")) { //$NON-NLS-1$
			Point loq = getStartLocation();
			Rectangle bounds = new Rectangle(loq, loq);
			bounds.union(loq.getTranslated(getDragMoveDelta()));
			req.setSize(bounds.getSize());
			req.setLocation(bounds.getLocation());
			req.getExtendedData().clear();
			if (!getCurrentInput().isAltKeyDown() && helper != null) {
				PrecisionRectangle baseRect = new PrecisionRectangle(bounds);
				PrecisionRectangle result = baseRect.getPreciseCopy();
				helper.snapRectangle(req, PositionConstants.NSEW, baseRect, result);
				req.setLocation(result.getLocation());
				req.setSize(result.getSize());
			}
		} else if (isInState(STATE_DRAG_IN_PROGRESS)) {// avoid resize on drag
			Point loq = getStartLocation();
			Rectangle bounds = new Rectangle(loq, loq);
			bounds.union(loq.getTranslated(getDragMoveDelta()));
			req.getExtendedData().clear();
			if (!getCurrentInput().isAltKeyDown() && helper != null) {
				PrecisionRectangle baseRect = new PrecisionRectangle(bounds);
				PrecisionRectangle result = baseRect.getPreciseCopy();
				helper.snapRectangle(req, PositionConstants.NSEW, baseRect, result);
			}
			req.setLocation(getLocation());

		} else {
			req.setSize(null);
			req.setLocation(getLocation());
		}


	}



	protected void snapPoint(CreateRequest request) {
		Dimension delta =getDragMoveDelta();
		Point moveDelta = new Point(delta.preciseWidth(),delta.preciseHeight());

		if (helper != null && sourceRectangle != null && compoundSrcRect != null) {
			PrecisionRectangle baseRect = sourceRectangle.getPreciseCopy();
			PrecisionRectangle jointRect = compoundSrcRect.getPreciseCopy();
			baseRect.translate(moveDelta);
			jointRect.translate(moveDelta);
			PrecisionPoint preciseDelta = new PrecisionPoint(moveDelta);
			helper.snapPoint(request, PositionConstants.HORIZONTAL | PositionConstants.VERTICAL, new PrecisionRectangle[] {
					baseRect, jointRect }, preciseDelta);

			request.setLocation(preciseDelta);
		}

	}

	@SuppressWarnings("deprecation")
	private void captureSourceDimensions() {
		if (figure == null) {
			return ;
		}

		PrecisionRectangle bounds = new PrecisionRectangle(figure.getBounds());
		figure.translateToAbsolute(bounds);

		if (sourceRectangle == null) {
			if (figure instanceof HandleBounds) {
				sourceRectangle = new PrecisionRectangle(((HandleBounds) figure).getHandleBounds());
			} else {
				sourceRectangle = new PrecisionRectangle(figure.getBounds());
			}
			figure.translateToAbsolute(sourceRectangle);

		}

		if (compoundSrcRect == null) {
			compoundSrcRect = new PrecisionRectangle(bounds);
		} else {
			compoundSrcRect = compoundSrcRect.union(bounds);
		}
	}

	@Override
	protected void setState(int state) {
		captureSourceDimensions();
		super.setState(state);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.tools.CreationTool#handleButtonDown(int)
	 */
	@Override
	protected boolean handleButtonDown(int button) {
		if (button != 1) {
			setState(STATE_INVALID);
			handleInvalidInput();
			return true;
		}
		if (stateTransition(STATE_INITIAL, STATE_DRAG)) {
			//<<<<<<< .mine
			//			//	getCreateRequest().setLocation(getLocation());
			//			//lockTargetEditPart(getTargetEditPart());
			//=======
			//			getCreateRequest().setLocation(getLocation());
			//			// lockTargetEditPart(getTargetEditPart());
			//>>>>>>> .r3427
			// Snap only when size on drop is employed
			if (getTargetEditPart() != null) {
				helper = (SnapToHelper) getTargetEditPart().getAdapter(SnapToHelper.class);
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.tools.CreationTool#handleButtonUp(int)
	 */
	@Override
	protected boolean handleButtonUp(int button) {
		if (stateTransition(STATE_DRAG | STATE_DRAG_IN_PROGRESS, STATE_TERMINAL)) {
			eraseTargetFeedback();
			unlockTargetEditPart();
			performCreation(button);
			/*
            if(getTargetEditPart() != null
                    && getTargetEditPart().getClass().getSimpleName().contains("Pool")
                    && eClass != null
                    && eClass.getName().contains("Lane")){ //$NON-NLS-1$
                selectAddedObject(getCurrentViewer(), Collections.singletonList(getCurrentViewer().getRootEditPart().getChildren().get(0)));
            }
			 */
		}

		setState(STATE_TERMINAL);

		handleFinished();

		return true;
	}

	@Override
	protected void selectAddedObject(EditPartViewer viewer, Collection objects) {
		ViewAndElementDescriptor descriptor = null ;
		if(!objects.isEmpty()){
			Object desc =  objects.iterator().next() ;

			if(desc != null && desc instanceof ViewAndElementDescriptor){
				descriptor = (ViewAndElementDescriptor) desc ;
			}
		}
		if(descriptor != null){
			if(((CreateElementRequestAdapter)descriptor.getElementAdapter()).resolve() instanceof Lane ){
				super.selectAddedObject(viewer, Collections.singletonList(viewer.getRootEditPart().getChildren().get(0)));
			}else{
				super.selectAddedObject(viewer, objects);
			}
		}else{
			super.selectAddedObject(viewer, objects);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.tools.CreationTool#deactivate()
	 */
	@Override
	public void deactivate() {
		//super.deactivate();
		//TODO : call super instead of copy what is does but by now it throw a NPE because getDomain() return null
		setTargetRequest(null);
		if (isHoverActive()) {
			resetHover();
		}
		eraseTargetFeedback();
		setTargetEditPart(null);
		setTargetRequest(null);
		setAutoexposeHelper(null);
		setFlag(FLAG_ACTIVE, false);
		setViewer(null);
		setCurrentCommand(null);
		setState(STATE_TERMINAL);
		//				operationSet = null;
		//				current = null;
		//				getDomain().getCommandStack().removeCommandStackEventListener(commandStackListener);


		// remove swt element in order to release handle
		if(cursorImage != null && !cursorImage.isDisposed()){
			cursorImage.dispose();
		}
		if (cursor != null && !cursor.isDisposed()) {
			cursor.dispose();
		}
		if (figure != null && !figure.isVisible()) {
			figure.erase();
		}
		figure = null;
		//		if (layer != null && !layer.isVisible()) {
		//			layer.erase();
		//		}
		compoundSrcRect = null;
		sourceRectangle = null;
	}



	/* (non-Javadoc)
	 * @see org.eclipse.gef.tools.AbstractTool#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		cursorImage = new Image(PlatformUI.getWorkbench().getDisplay(),
				1, 1);
		cursor = new Cursor(PlatformUI.getWorkbench().getDisplay(), cursorImage.getImageData(), 0, 0);
		if (applies()) {
			figure = createImage();
			setDefaultCursor(cursor);
			captureSourceDimensions();
		}

	}

	@Override
	public void setViewer(EditPartViewer viewer) {
		super.setViewer(viewer);
		if(viewer != null && figure != null){
			double zoom = ((DiagramRootEditPart)((DiagramGraphicalViewer)getCurrentViewer()).getRootEditPart()).getZoomManager().getZoom() ;
			if(zoom != oldZoom){
				Rectangle r = figure.getBounds().getCopy() ;
				r.performScale(zoom);
				figure.setBounds(r) ;
				oldZoom = zoom ;
			}
		}
	}

}
