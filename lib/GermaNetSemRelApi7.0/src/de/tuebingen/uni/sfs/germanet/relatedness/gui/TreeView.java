/*
 * Copyright (C) 2012 Department of General and Computational Linguistics,
 * University of Tuebingen
 *
 * This file is part of the Java Relatedness API to GermaNet.
 *
 * The Java Relatedness API to GermaNet is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Java Relatedness API to GermaNet is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this API; if not, see <http://www.gnu.org/licenses/>.
 */
package de.tuebingen.uni.sfs.germanet.relatedness.gui;

/**
 * @author University of Tuebingen, Department of Linguistics
 * (germanetinfo at sfs.uni-tuebingen.de)
 */
import java.awt.BorderLayout;
import java.awt.Color;

import java.awt.event.ActionEvent;


import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.AbstractAction;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;


import javax.swing.JScrollPane;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;

import prefuse.action.ActionList;
import prefuse.action.ItemAction;


import prefuse.action.RepaintAction;
import prefuse.action.animate.LocationAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;

import prefuse.action.layout.graph.NodeLinkTreeLayout;

import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.FocusControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Tree;

import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;

import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;


/**
 * Class for showing the tree structure
 * after the example of TreeView.java demo by Jeffrey Heer
 * 
 */
public class TreeView extends Display {

    
    
    private static final String tree = "tree";
    private static final String treeNodes = "tree.nodes";
    private static final String treeEdges = "tree.edges";
    
    private LabelRenderer m_nodeRenderer;
    private EdgeRenderer m_edgeRenderer;
    

    private int m_orientation = Constants.ORIENT_TOP_BOTTOM;
    
    public TreeView(Tree t, String label) {
        super(new Visualization());
       

        m_vis.add(tree, t);
     
        m_nodeRenderer = new LabelRenderer(label);
          
        m_edgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_CURVE);
        m_nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
        DefaultRendererFactory rf = new DefaultRendererFactory(m_nodeRenderer);
        rf.add(new InGroupPredicate(treeEdges), m_edgeRenderer);
      
        rf.add(new InGroupPredicate("labels"), new LabelRenderer(label));
        m_vis.setRendererFactory(rf);
               
        // colors
       ItemAction nodeColor = new NodeColorAction(treeNodes);
        ItemAction textColor = new ColorAction(treeNodes,
                VisualItem.TEXTCOLOR, ColorLib.rgb(30,30,30));
        m_vis.putAction("textColor", textColor);
         
       
        
        ItemAction edgeColor = new ColorAction(treeEdges,
                VisualItem.STROKECOLOR, ColorLib.rgb(200,200,200));
       
       
        
        // full paint
        ActionList fullPaint = new ActionList();
        fullPaint.add(nodeColor);
        m_vis.putAction("fullPaint", fullPaint);
        
         // quick repaint
        ActionList repaint = new ActionList();
        repaint.add(nodeColor);
        repaint.add(new RepaintAction());
        m_vis.putAction("repaint", repaint);
        
         // animate paint change
        ActionList animatePaint = new ActionList(400);
      
        animatePaint.add(new RepaintAction());
        m_vis.putAction("animatePaint", animatePaint);
        
      
        
        // create the tree layout action
        NodeLinkTreeLayout treeLayout = new NodeLinkTreeLayout(tree,
                m_orientation, 50, 0, 8);
        m_vis.putAction("treeLayout", treeLayout);
     
         
         
        
       
        
        // create the filtering and layout
        ActionList filter = new ActionList();
      
        filter.add(new FontAction(treeNodes, FontLib.getFont("Tahoma", 16)));
        filter.add(treeLayout);
        filter.add(textColor);
      
      
        filter.add(edgeColor);
        m_vis.putAction("filter", filter);
        
         ActionList resize = new ActionList(1000);
        resize.setPacingFunction(new SlowInSlowOutPacer());
       
     
        resize.add(new VisibilityAnimator(tree));
        resize.add(new LocationAnimator(treeNodes));
        resize.add(new LocationAnimator(treeEdges));
        
     
        resize.add(new RepaintAction());
        m_vis.putAction("resize", resize);
        //m_vis.alwaysRunAfter("filter", "resize");
        
        // ------------------------------------------------
        
        // initialize the display
        setSize(700,700);
        setItemSorter(new TreeDepthItemSorter());
        addControlListener(new ZoomToFitControl());
        addControlListener(new ZoomControl());
        addControlListener(new WheelZoomControl());
        addControlListener(new PanControl());
        //addControlListener(new FocusControl(1, "filter"));
     
        
          addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                m_vis.run("layout");
                m_vis.run("resize");
                
                invalidate();
            }
        });
        
        
        
       NodeLinkTreeLayout rtl 
            = (NodeLinkTreeLayout)m_vis.getAction("treeLayout");
           
        rtl.setOrientation(Constants.ORIENT_TOP_BOTTOM);
        m_vis.run("filter");
        
      
    }
    

    
    public JComponent showPath(Tree t, final String label) {
       Color BACKGROUND=Color.WHITE;
       Color FOREGROUND=Color.BLACK;
        // create a new treemap
        final TreeView tview = new TreeView(t, label);
       
        tview.setBackground(BACKGROUND);
        tview.setForeground(FOREGROUND);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.setForeground(FOREGROUND);
        panel.add(tview, BorderLayout.CENTER);
        JScrollPane scroll=new JScrollPane(panel);
        add(scroll, BorderLayout.CENTER);
        JFrame frame = new JFrame("Path");
        frame.setContentPane(tview);
        frame.pack();
        frame.setVisible(true);
        return panel;

    }
    
    // ------------------------------------------------------------------------
   
    public class OrientAction extends AbstractAction {
        private int orientation;
        
        public OrientAction(int orientation) {
            this.orientation = orientation;
        }
        public void actionPerformed(ActionEvent evt) {
          
            getVisualization().cancel("orient");
            getVisualization().run("treeLayout");
            getVisualization().run("orient");
        }
    }
    
   
       
    
    public static class NodeColorAction extends ColorAction {
        
        public NodeColorAction(String group) {
            super(group, VisualItem.FILLCOLOR);
        }
        
        public int getColor(VisualItem item) {
            if ( m_vis.isInGroup(item, Visualization.SEARCH_ITEMS) )
                return ColorLib.rgb(255,190,190);
            else if ( m_vis.isInGroup(item, Visualization.FOCUS_ITEMS) )
                return ColorLib.rgb(198,229,229);
            else if ( item.getDOI() > -1 )
                return ColorLib.rgb(164,193,193);
            else
                return ColorLib.rgba(255,255,255,0);
        }
        
    } // end of inner class TreeMapColorAction
    
     
    
    
} // end of class TreeMap