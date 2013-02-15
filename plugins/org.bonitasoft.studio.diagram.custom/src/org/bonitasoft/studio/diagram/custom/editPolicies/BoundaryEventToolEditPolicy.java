/**
 * Copyright (C) 2009-2010 BonitaSoft S.A.
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
import java.util.List;

import org.bonitasoft.studio.common.NamingUtils;
import org.bonitasoft.studio.common.Pair;
import org.bonitasoft.studio.common.gmf.tools.GMFTools;
import org.bonitasoft.studio.common.gmf.tools.GMFTools.ElementTypeResolver;
import org.bonitasoft.studio.diagram.custom.Activator;
import org.bonitasoft.studio.diagram.custom.Messages;
import org.bonitasoft.studio.diagram.custom.figures.DropDownMenuFigure;
import org.bonitasoft.studio.diagram.custom.figures.SlideMenuBarFigure;
import org.bonitasoft.studio.model.process.Activity;
import org.bonitasoft.studio.model.process.CallActivity;
import org.bonitasoft.studio.model.process.ReceiveTask;
import org.bonitasoft.studio.model.process.ScriptTask;
import org.bonitasoft.studio.model.process.SendTask;
import org.bonitasoft.studio.model.process.ServiceTask;
import org.bonitasoft.studio.model.process.Task;
import org.bonitasoft.studio.model.process.diagram.edit.parts.Activity2EditPart;
import org.bonitasoft.studio.model.process.diagram.edit.parts.ActivityEditPart;
import org.bonitasoft.studio.model.process.diagram.edit.parts.CallActivity2EditPart;
import org.bonitasoft.studio.model.process.diagram.edit.parts.CallActivityEditPart;
import org.bonitasoft.studio.model.process.diagram.edit.parts.ReceiveTask2EditPart;
import org.bonitasoft.studio.model.process.diagram.edit.parts.ReceiveTaskEditPart;
import org.bonitasoft.studio.model.process.diagram.edit.parts.ScriptTask2EditPart;
import org.bonitasoft.studio.model.process.diagram.edit.parts.ScriptTaskEditPart;
import org.bonitasoft.studio.model.process.diagram.edit.parts.ServiceTask2EditPart;
import org.bonitasoft.studio.model.process.diagram.edit.parts.ServiceTaskEditPart;
import org.bonitasoft.studio.model.process.diagram.edit.parts.Task2EditPart;
import org.bonitasoft.studio.model.process.diagram.edit.parts.TaskEditPart;
import org.bonitasoft.studio.model.process.diagram.providers.ProcessElementTypes;
import org.bonitasoft.studio.pics.Pics;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gmf.runtime.common.ui.services.icon.IconService;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Listener;

/**
 * 
 * Show a tool that enable to create boundary events on the activity
 * 
 * @author Baptiste Mesta
 */
public class BoundaryEventToolEditPolicy extends AbstractSingleSelectionEditPolicy implements ZoomListener , LayerConstants {

    public static final String BOUNDARY_TOOL_ROLE = "boundaryTool"; //$NON-NLS-1$
    private static final IFigure EMPTY_FIGURE = new RectangleFigure();
    static {
        EMPTY_FIGURE.setSize(20, 20);
        EMPTY_FIGURE.setVisible(false);
    }

    private final List<IFigure> figures;
    private IFigure layer;
    private SlideMenuBarFigure toolBarFigure;
    private ImageFigure toolImage;
    private FreeformLayer composite;
    private final ElementTypeResolver elementTypeResolver = new ElementTypeResolver() {

        public IElementType getElementType(GraphicalEditPart parentEditPart, EClass targetEClass) {
            List<IElementType> allowedChildren = new ArrayList<IElementType>();

            if (parentEditPart instanceof Task2EditPart || parentEditPart instanceof TaskEditPart) {
                allowedChildren.add(ProcessElementTypes.IntermediateErrorCatchEvent_3029);
                allowedChildren.add(ProcessElementTypes.BoundaryMessageEvent_3035);
                allowedChildren.add(ProcessElementTypes.BoundaryTimerEvent_3043);
                allowedChildren.add(ProcessElementTypes.BoundarySignalEvent_3052);
            } else if (parentEditPart instanceof CallActivity2EditPart || parentEditPart instanceof CallActivityEditPart) {
                allowedChildren.add(ProcessElementTypes.IntermediateErrorCatchEvent_3030);
                allowedChildren.add(ProcessElementTypes.BoundaryMessageEvent_3036);
                allowedChildren.add(ProcessElementTypes.BoundaryTimerEvent_3044);
                allowedChildren.add(ProcessElementTypes.BoundarySignalEvent_3053);
            } else if (parentEditPart instanceof ScriptTask2EditPart || parentEditPart instanceof ScriptTaskEditPart) {
                allowedChildren.add(ProcessElementTypes.IntermediateErrorCatchEvent_3033);
            } else if (parentEditPart instanceof ServiceTask2EditPart || parentEditPart instanceof ServiceTaskEditPart) {
                allowedChildren.add(ProcessElementTypes.IntermediateErrorCatchEvent_3032);
            } else if (parentEditPart instanceof ReceiveTask2EditPart || parentEditPart instanceof ReceiveTaskEditPart) {
                allowedChildren.add(ProcessElementTypes.IntermediateErrorCatchEvent_3031);
            } else if (parentEditPart instanceof Activity2EditPart || parentEditPart instanceof ActivityEditPart) {
                allowedChildren.add(ProcessElementTypes.IntermediateErrorCatchEvent_3034);
            }


            for (IElementType child : allowedChildren) {
                if (child.getEClass().equals(targetEClass)) {
                    return child;
                }
            }

            return null;

        }
    };

    public BoundaryEventToolEditPolicy() {
        super();
        figures = new ArrayList<IFigure>();

    }

    private DropDownMenuFigure dropMenu;
    private IFigure referenceFigure;
    private Image iconImage;
    private ZoomManager zoomManager;

    private Pair<IFigure,MouseListener> createClickableItem(final Point location, final EditPart host, final IElementType type) {
        ImageFigure image;
        image = new ImageFigure(IconService.getInstance().getIcon(type));
        image.setSize(16, 16);
        image.setToolTip(new Label(NamingUtils.getPaletteText(false, type.getEClass())));

        MouseListener listener = new MouseListener() {

            public void mouseReleased(MouseEvent me) {
            }

            public void mousePressed(MouseEvent me) {
                ShapeNodeEditPart node = (ShapeNodeEditPart) host.getAdapter(ShapeNodeEditPart.class);
                if (node != null) {
                    EClass targetEClass = getTargetEClass(type);
                    GMFTools.create(targetEClass, node, elementTypeResolver);
                    me.consume();
                }
            }

            public void mouseDoubleClicked(MouseEvent me) {

            }
        };
        Pair<IFigure, MouseListener> pair = new Pair<IFigure, MouseListener>(image, listener);
        figures.add(image);
        return pair;
    }

    private EClass getTargetEClass(IElementType child) {
        EClass eClass = child.getEClass();
        return eClass;
    }


    @Override
    @SuppressWarnings("unchecked")
    protected void hideFeedback() {
        List<IFigure> figToDelete = new ArrayList<IFigure>();
        if (layer != null) {
            List<Object> children = layer.getChildren();
            for (Object fig : children) {
                if (figures.contains(fig)) {
                    if (fig instanceof IFigure) {
                        figToDelete.add((IFigure) fig);
                    }
                }
            }
            figures.clear();
            for (IFigure fig : figToDelete) {
                layer.remove(fig);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editpolicies.SelectionEditPolicy#showSelection()
     */
    @Override
    protected void showFeedback() {

        if(zoomManager.getZoom() > GMFTools.MINIMAL_ZOOM_DISPLAY){
            EditPart host = getHost();
            if (host == null) {
                return;
            }
            while (host.getAdapter(ShapeNodeEditPart.class) == null) {
                host = host.getParent();
            }


            layer = getLayer(LayerConstants.HANDLE_LAYER);

            referenceFigure = SelectionFeedbackEditPolicy.getFeedbackFigure(getHost());
            if (referenceFigure == null) {
                referenceFigure = getHostFigure();
            }
            Rectangle bounds = referenceFigure.getBounds().getCopy();

            // Get the absolute coordinate
            referenceFigure.translateToAbsolute(bounds);
            IFigure parentFigure = referenceFigure.getParent();
            while (parentFigure != null) {
                if (parentFigure instanceof Viewport) {
                    Viewport viewport = (Viewport) parentFigure;
                    bounds.translate(viewport.getHorizontalRangeModel().getValue(), viewport.getVerticalRangeModel().getValue());
                    parentFigure = parentFigure.getParent();
                } else {
                    parentFigure = parentFigure.getParent();
                }
            }

            composite = new FreeformLayer();
            composite.setSize(20, 20);
            composite.setLocation(new Point(bounds.getLeft().x + 50, bounds.getBottomRight().y));

            toolBarFigure = new SlideMenuBarFigure(composite);

            iconImage =  Pics.getImage("boundary_tool.png", Activator.getDefault()) ;
            toolImage = new ImageFigure(iconImage);
            dropMenu = new DropDownMenuFigure(toolImage, composite,layer, Messages.boundaryTool);
            toolBarFigure.addToMenu(dropMenu);
            dropMenu.addToggleVisibilityListener(new Listener() {

                public void handleEvent(org.eclipse.swt.widgets.Event event) {
                    if(!dropMenu.isCollapsed()){
                        if(	getHost().getEditPolicy(ActivitySwitchEditPolicy.SWITCH_TYPE_ROLE) != null){
                            if(((ActivitySwitchEditPolicy) getHost().getEditPolicy(ActivitySwitchEditPolicy.SWITCH_TYPE_ROLE)).getDropMenu() != null ){
                                ((ActivitySwitchEditPolicy) getHost().getEditPolicy(ActivitySwitchEditPolicy.SWITCH_TYPE_ROLE)).getDropMenu().collapse() ;
                            }
                        }
                    }
                }
            }) ;
            List<Pair<IFigure,MouseListener>> clickableItems = new ArrayList<Pair<IFigure,MouseListener>>();

            if (getHost().getAdapter(Task.class) != null) {
                clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.IntermediateErrorCatchEvent_3029));
                clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundaryMessageEvent_3035));
                clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundaryTimerEvent_3043));
                clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundarySignalEvent_3052));
            } else if (getHost().getAdapter(CallActivity.class) != null) {
                clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.IntermediateErrorCatchEvent_3030));
                clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundaryMessageEvent_3036));
                clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundaryTimerEvent_3044));
                clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundarySignalEvent_3053));
            } else if (getHost().getAdapter(ServiceTask.class) != null) {
                clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.IntermediateErrorCatchEvent_3032));
                //			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundaryMessageEvent_3039));
                //			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundaryTimerEvent_3047));
                //			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundarySignalEvent_3056));
            } else if (getHost().getAdapter(ScriptTask.class) != null) {
                clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.IntermediateErrorCatchEvent_3033));
                //			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundaryMessageEvent_3040));
                //			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundaryTimerEvent_3048));
                //			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundarySignalEvent_3057));
            }/* else if (getHost().getAdapter(SendTask.class) != null) {
//			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.IntermediateErrorCatchEvent_3032));
//			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundaryMessageEvent_3038));
//			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundaryTimerEvent_3046));
//			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundarySignalEvent_3055));
	} */else if (getHost().getAdapter(ReceiveTask.class) != null) {
	    clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.IntermediateErrorCatchEvent_3031));
	    //			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundaryMessageEvent_3037));
	    //			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundaryTimerEvent_3045));
	    //			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundarySignalEvent_3054));
	} else if (getHost().getAdapter(Task.class) == null && getHost().getAdapter(CallActivity.class) == null && getHost().getAdapter(SendTask.class) == null
	        && getHost().getAdapter(ReceiveTask.class) == null && getHost().getAdapter(Activity.class) != null) {// automatic
	    // task
	    clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.IntermediateErrorCatchEvent_3034));
	    //			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundaryMessageEvent_3042));
	    //			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundaryTimerEvent_3049));
	    //			clickableItems.add(createClickableItem(new Point(0, 0), getHost(), ProcessElementTypes.BoundarySignalEvent_3058));
	}

            for (Pair<IFigure,MouseListener> item : clickableItems) {
                dropMenu.addToMenu(item.getFirst(),item.getSecond());
            }

            dropMenu.createSubMenuFigure();
            //		dropMenu.paintElements();

            figures.add(referenceFigure);
            figures.add(composite);
            layer.add(composite);

            composite.addMouseMotionListener(new MouseMotionListener() {

                public void mouseMoved(MouseEvent me) {

                }

                public void mouseHover(MouseEvent arg0) {

                }

                public void mouseExited(MouseEvent me) {

                }

                public void mouseEntered(MouseEvent me) {
                    referenceFigure.translateToAbsolute(me.getLocation());
                    IFigure parentFigure = referenceFigure.getParent();
                    while (parentFigure != null) {
                        if (parentFigure instanceof Viewport) {
                            Viewport viewport = (Viewport) parentFigure;
                            me.getLocation().translate(viewport.getHorizontalRangeModel().getValue(), viewport.getVerticalRangeModel().getValue());
                            parentFigure = parentFigure.getParent();
                        } else {
                            parentFigure = parentFigure.getParent();
                        }
                    }
                }

                public void mouseDragged(MouseEvent arg0) {

                }
            });
        }
    }


    @Override
    public void activate() {
        super.activate();
        zoomManager = ((DiagramRootEditPart) getHost().getRoot()).getZoomManager();
        zoomManager.addZoomListener(this) ;
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editpolicies.SelectionEditPolicy#deactivate()
     */
    @Override
    public void deactivate() {
        super.deactivate();
        zoomManager.removeZoomListener(this) ;
        iconImage = null;
        referenceFigure = null;
        toolImage = null;
        toolBarFigure = null;
    }

    public SlideMenuBarFigure getToolbarFigure() {
        return toolBarFigure;
    }

    public DropDownMenuFigure getDropMenu() {
        return dropMenu ;
    }

    public void zoomChanged(double zoom) {
        hideSelection() ;
    }

}
