package tufts.vue;

import java.awt.Container;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.*;

public class ZoomTool extends VueTool
    implements VueConstants
{
    static private final int ZOOM_MANUAL = -1;
    static private final double[] ZoomDefaults = {
        1.0/32, 1.0/24, 1.0/16, 1.0/12, 1.0/8, 1.0/6, 1.0/5, 1.0/4, 1.0/3, 1.0/2, 2.0/3, 0.75,
        1.0,
        1.25, 1.5, 2, 3, 4, 6, 8, 12, 16, 24, 32, 48, 64
        //, 96, 128, 256, 384, 512
    };
    static private final int ZOOM_FIT_PAD = 20; // make this is > SelectionStrokeWidth & SelectionHandleSize
    static private final double MaxZoom = ZoomDefaults[ZoomDefaults.length - 1];

    
    public ZoomTool() {
        super();
    }
	
    public JPanel getContextualPanel() {
		return VueToolbarController.getController().getSuggestedContextualPanel();
    }

    public void handleSelection() {
    }
	
    private static final Color SelectorColor = Color.red;
    private static final Color SelectorColorInverted = new Color(0,255,255); // inverse of red
    public void drawSelector(java.awt.Graphics2D g, java.awt.Rectangle r)
    {
        /*
        if (VueUtil.isMacPlatform())
            g.setXORMode(SelectorColorInverted);
        else
            g.setXORMode(SelectorColor);
        */
        g.setColor(Color.red);
        super.drawSelector(g, r);
    }

    public boolean usesRightClick()
    {
        return true;
    }

    public boolean isZoomOutMode()
    {
        return getSelectedSubTool().getID().equals("zoomTool.zoomOut");
    }

    public boolean supportsSelection() { return false; }

    public boolean supportsDraggedSelector(MouseEvent e)
    {
        // todo: take a map mouse event, and if zoom level on viewer == MaxZoom, return false
        
        // This is so that if they RIGHT click, the dragged selector doesn't appear --
        // because right click in zoom does a zoom out, and it makes less sense to
        // zoom out on a particular region.
        // Need to recognize button 1 on a drag, where getButton=0, or a release, where modifiers 0 but getButton=1
        return !isZoomOutMode() && (e.getButton() == MouseEvent.BUTTON1 || (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0);
    }
    
    public boolean handleMouseReleased(MapMouseEvent e)
    {
        System.out.println(this + " handleMouseReleased " + e);

        Point p = e.getPoint();
        
        if (e.isShiftDown() || e.getButton() != MouseEvent.BUTTON1
            //|| toolKeyEvent != null && toolKeyEvent.isShiftDown()
            ) {
            if (isZoomOutMode())
                setZoomBigger(p);
            else
                setZoomSmaller(p);
        } else {
            Rectangle box = e.getSelectorBox();
            if (box != null && box.width > 10 && box.height > 10) {
                setZoomFitRegion(e.getMapSelectorBox());
            } else {
                if (isZoomOutMode())
                    setZoomSmaller(p);
                else
                    setZoomBigger(p);
            }
        }
        return true;
    }
    

    /*
        if (e.isShiftDown() || e.getButton() != MouseEvent.BUTTON1
            || toolKeyEvent != null && toolKeyEvent.isShiftDown()
            ) {
            setZoomPoint(e.getPoint());
            if (ZoomTool.isZoomOutMode())
                setZoomBigger();
            else
                setZoomSmaller();
        } else {
            if (draggedSelectorBox != null &&
                draggedSelectorBox.getWidth() > 10 && draggedSelectorBox.getHeight() > 10) {
                setZoomFitRegion(screenToMapRect(draggedSelectorBox));
            } else {
                setZoomPoint(e.getPoint());
                if (ZoomTool.isZoomOutMode())
                    setZoomSmaller();
                else
                    setZoomBigger();
            }
        }
    */
    
    public boolean handleKeyPressed(KeyEvent e){return false;}
    
    public static boolean setZoomBigger(Point focus)
    {
        double curZoom = VUE.getActiveViewer().getZoomFactor();
        for (int i = 0; i < ZoomDefaults.length; i++) {
            if (ZoomDefaults[i] > curZoom) {
                setZoom(ZoomDefaults[i], focus);
                return true;
            }
        }
        return false;
    }
    
    public static boolean setZoomSmaller(Point focus)
    {
        double curZoom = VUE.getActiveViewer().getZoomFactor();
        for (int i = ZoomDefaults.length - 1; i >= 0; i--) {
            if (ZoomDefaults[i] < curZoom) {
                setZoom(ZoomDefaults[i], focus);
                return true;
            }
        }
        return false;
    }

    private static final Point CENTER_FOCUS = new Point(); // marker only
    private static final Point DONT_FOCUS = new Point(); // marker only
    
    public static void setZoom(double zoomFactor)
    {
        setZoom(zoomFactor, true, CENTER_FOCUS, false);
    }
    public static void setZoom(double zoomFactor, Point focus)
    {
        setZoom(zoomFactor, true, focus, false);
    }
    
    private static void setZoom(double newZoomFactor, boolean adjustViewport, Point focus, boolean reset)
    {
        // this is much simpler as the viewer now handles adjusting for the focal point
        MapViewer viewer = VUE.getActiveViewer();
        if (focus == DONT_FOCUS) {
            focus = null;
        } else if (adjustViewport && (focus == null || focus == CENTER_FOCUS)) {
            // If no user selected zoom focus point, zoom in to
            // towards the map location at the center of the
            // viewport.
            focus = viewer.getVisibleCenter();
        }
        viewer.setZoomFactor(newZoomFactor, reset, focus);
    }
    
    /*
    private static void OLD_setZoom(double newZoomFactor, boolean adjustViewport, Point focus, boolean reset)
    {
        MapViewer viewer = VUE.getActiveViewer();
        
        //        if (!DEBUG_SCROLL && adjustViewport) {
        if (adjustViewport) {
            if (focus == null || focus == CENTER_FOCUS) {
                // If no user selected zoom focus point, zoom in to
                // towards the map location at the center of the
                // viewport.

                focus = viewer.getVisibleCenter();
                
                //focus = new Point(viewer.getVisibleWidth() / 2,
                //viewer.getVisibleHeight() / 2);
                // this probably doesn't compute right if we're scrolled...
            }
            //viewer.scrollMapLocationToScreenLocation(mapAnchor, focus);

            //Point2D mapAnchor = viewer.screenToMapPoint(focus);
            //double offsetX = (mapAnchor.getX() * newZoomFactor) - focus.getX();
            //double offsetY = (mapAnchor.getY() * newZoomFactor) - focus.getY();
            //viewer.setMapOriginOffset(offsetX, offsetY);
        }

        if (focus == DONT_FOCUS)
            focus = null;
        
        viewer.setZoomFactor(newZoomFactor, reset, focus);
    }
    */    
    public static void setZoomFitRegion(Rectangle2D mapRegion, int edgePadding)
    {
        Point2D.Double offset = new Point2D.Double();
        MapViewer viewer = VUE.getActiveViewer();
        double newZoom = computeZoomFit(viewer.getVisibleSize(),
                                        edgePadding,
                                        mapRegion,
                                        offset);
        if (newZoom > MaxZoom) {
            setZoom(MaxZoom, true, CENTER_FOCUS, true);
            Point2D mapAnchor = new Point2D.Double(mapRegion.getCenterX(), mapRegion.getCenterY());
            Point focus = new Point(viewer.getVisibleWidth()/2, viewer.getVisibleHeight()/2);
            double offsetX = (mapAnchor.getX() * MaxZoom) - focus.getX();
            double offsetY = (mapAnchor.getY() * MaxZoom) - focus.getY();
            viewer.setMapOriginOffset(offsetX, offsetY);
            viewer.resetScrollRegion();
        } else {
            setZoom(newZoom, false, DONT_FOCUS, true);
            // don't adjust the scroll region, we've already got
            // the adjustment here -- tho will need work for scrolling!
            
            if (viewer.getParent() instanceof JViewport)
                ;
            else
                viewer.setMapOriginOffset(offset.getX(), offset.getY());
            
        }
    }
    
    /** fit everything in the current map into the current viewport */
    public static void setZoomFit()
    {
        setZoomFitRegion(VUE.getActiveMap().getBounds(), DEBUG.MARGINS ? 0 : ZOOM_FIT_PAD);
        // while it would be nice to call getActiveViewer().getContentBounds()
        // as a way to get bounds with max selection edges, etc, it computes some
        // of it's size based on current zoom, which we're about to change, so
        // we can't use it as our zoom fit becomes a circular, cycling computation.
    }
    
    public static void setZoomFitRegion(Rectangle2D mapRegion)
    {
        setZoomFitRegion(mapRegion, 0);
    }
    
    public static double computeZoomFit(Dimension viewport, int borderGap, Rectangle2D bounds, Point2D offset) {
        return computeZoomFit(viewport, borderGap, bounds, offset, true);
    }
    
    /*
     * Compute two items: the zoom factor that will fit
     * everything within the given bounds into the given
     * viewport, and put into @param offset the offset
     * to place the viewport at. Used to figure out how
     * to fit everything within a map on the screen and
     * where to pan to so you can see it all.
     */
    public static double computeZoomFit(java.awt.Dimension viewport,
                                        int borderGap,
                                        java.awt.geom.Rectangle2D bounds,
                                        java.awt.geom.Point2D offset,
                                        boolean centerSmallerDimensionInViewport)
    {
        int viewWidth = viewport.width - borderGap * 2;
        int viewHeight = viewport.height - borderGap * 2;
        double vertZoom = (double) viewHeight / bounds.getHeight();
        double horzZoom = (double) viewWidth / bounds.getWidth();
        boolean centerVertical;
        double newZoom;
        if (horzZoom < vertZoom) {
            newZoom = horzZoom;
            centerVertical = true;
        } else {
            newZoom = vertZoom;
            centerVertical = false;
        }

        // Now center the components within the dimension
        // that had extra room to scale in.
                    
        double offsetX = bounds.getX() * newZoom - borderGap;
        double offsetY = bounds.getY() * newZoom - borderGap;

        if (centerSmallerDimensionInViewport) {
            if (centerVertical)
                offsetY -= (viewHeight - bounds.getHeight()*newZoom) / 2;
            else // center horizontal
                offsetX -= (viewWidth - bounds.getWidth()*newZoom) / 2;
        }
            
        offset.setLocation(offsetX, offsetY);
        return newZoom;
    }
    
}
