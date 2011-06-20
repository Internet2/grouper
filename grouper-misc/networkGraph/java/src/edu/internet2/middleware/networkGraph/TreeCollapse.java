package edu.internet2.middleware.networkGraph;

/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of California
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.PolarPoint;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.subLayout.TreeCollapser;

/**
 * Demonstrates "collapsing"/"expanding" of a tree's subtrees.
 * @author Tom Nelson
 * 
 */
@SuppressWarnings("serial")
public class TreeCollapse extends JApplet {

  /** if we have a hierarchy of relationships, then the graph layout can be a forest which is easier to read */
  private boolean isHierarchy = false;
  
  /**
   * the graph will be either a DirectedSparseMultigraph if not a hierarchy, and Forest if a hierarchy
   */
  private DirectedSparseMultigraph<String, Integer> directedSparseMultigraph = null;

  private Forest<String, Integer> forest = null;
  
  Factory<DirectedGraph<String, Integer>> graphFactory =
      new Factory<DirectedGraph<String, Integer>>() {

        public DirectedGraph<String, Integer> create() {
          return new DirectedSparseMultigraph<String, Integer>();
        }
      };

  Factory<Tree<String, Integer>> treeFactory =
      new Factory<Tree<String, Integer>>() {

        public Tree<String, Integer> create() {
          return new DelegateTree<String, Integer>(graphFactory);
        }
      };

  Factory<Integer> edgeFactory = new Factory<Integer>() {

    int i = 0;

    public Integer create() {
      return i++;
    }
  };

  Factory<String> vertexFactory = new Factory<String>() {

    int i = 0;

    public String create() {
      return "V" + i++;
    }
  };

  /**
   * the visual component and renderer for the graph
   */
  VisualizationViewer<String, Integer> vv;

  VisualizationServer.Paintable rings;

  String root;

  FRLayout<String, Integer> directedGraphLayout;
  TreeLayout<String, Integer> hierarchyLayout;

  TreeCollapser collapser;

  RadialTreeLayout<String, Integer> radialLayout;

  Mode mode;
  
  @Override
  public void start() {
    super.start();

    createTree();

    int width = Integer.parseInt(getParameter("width"));
    int height = Integer.parseInt(getParameter("height"));
    
    if (this.isHierarchy) {
      this.hierarchyLayout = new TreeLayout<String, Integer>(this.forest);
      this.radialLayout = new RadialTreeLayout<String, Integer>(this.forest);
      this.radialLayout.setSize(new Dimension(width, height));
    } else {
      this.directedGraphLayout = new FRLayout<String, Integer>(this.directedSparseMultigraph);
    }

    collapser = new TreeCollapser();
    
    vv = new VisualizationViewer<String, Integer>(this.isHierarchy ? this.hierarchyLayout : this.directedGraphLayout, new Dimension(width, height));
    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
    vv.getRenderContext().setVertexShapeTransformer(new ClusterVertexShapeFunction());
    // add a listener for ToolTips
    vv.setVertexToolTipTransformer(new ToStringLabeller());
    vv.getRenderContext().setArrowFillPaintTransformer(
        new ConstantTransformer(Color.lightGray));
    
    if (this.isHierarchy) {
      rings = new Rings();
    }
    
    Container content = getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);

    final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();

    vv.setGraphMouse(graphMouse);

//    JComboBox modeBox = graphMouse.getModeComboBox();
//    
//    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
    
    mode = ModalGraphMouse.Mode.TRANSFORMING;
    
    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        scaler.scale(vv, 1.1f, vv.getCenter());
      }
    });
    JButton minus = new JButton("-");
    minus.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        scaler.scale(vv, 1 / 1.1f, vv.getCenter());
      }
    });

    //for hierarchy
    String radialText = getParameter("text_radial");
    
    JToggleButton radial = null;
    
    if (this.isHierarchy) {
      radial = new JToggleButton(radialText);
      radial.addItemListener(new ItemListener() {
  
        public void itemStateChanged(ItemEvent e) {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            //          layout.setRadial(true);
            vv.setGraphLayout(radialLayout);
            vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
            vv.addPreRenderPaintable(rings);
          } else {
            //          layout.setRadial(false);
            vv.setGraphLayout(TreeCollapse.this.hierarchyLayout);
            vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
            vv.removePreRenderPaintable(rings);
          }
          vv.repaint();
        }
      });
      //end for hierarchy
    }
    
    final String pickingText = getParameter("text_picking");
    final String transformingText = getParameter("text_transforming");

    final JToggleButton pickingTransformingButton = new JToggleButton(pickingText);
    pickingTransformingButton.addItemListener(new ItemListener() {

      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          pickingTransformingButton.setText(transformingText);
          graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
        } else {
          pickingTransformingButton.setText(pickingText);
          graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        }
      }
    });
    

//    String collapseText = getParameter("text_collapse");
//    JButton collapse = new JButton(collapseText);
//    collapse.addActionListener(new ActionListener() {
//
//      public void actionPerformed(ActionEvent e) {
//        Collection picked = new HashSet(vv.getPickedVertexState().getPicked());
//        if (picked.size() == 1) {
//          Object root = picked.iterator().next();
//          Forest inGraph = (Forest) layout.getGraph();
//
//          try {
//            collapser.collapse(vv.getGraphLayout(), inGraph, root);
//          } catch (InstantiationException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//          } catch (IllegalAccessException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//          }
//
//          vv.getPickedVertexState().clear();
//          vv.repaint();
//        }
//      }
//    });
//
//    String expandText = getParameter("text_expand");
//    JButton expand = new JButton(expandText);
//    expand.addActionListener(new ActionListener() {
//
//      public void actionPerformed(ActionEvent e) {
//        Collection picked = vv.getPickedVertexState().getPicked();
//        for (Object v : picked) {
//          if (v instanceof Forest) {
//            Forest inGraph = (Forest) layout.getGraph();
//            collapser.expand(inGraph, (Forest) v);
//          }
//          vv.getPickedVertexState().clear();
//          vv.repaint();
//        }
//      }
//    });

    JPanel scaleGrid = new JPanel(new GridLayout(1, 0));

    String zoomText = getParameter("text_zoom");
    scaleGrid.setBorder(BorderFactory.createTitledBorder(zoomText));

    JPanel controls = new JPanel();
    scaleGrid.add(plus);
    scaleGrid.add(minus);
    if (this.isHierarchy) {
      controls.add(radial);
    }
    controls.add(scaleGrid);
    //controls.add(modeBox);
    controls.add(pickingTransformingButton);
    
//    controls.add(collapse);
//    controls.add(expand);
    content.add(controls, BorderLayout.SOUTH);

    //lets zoom in a little if hierarchy
    if (this.isHierarchy) {
      Point2D point2d = new Point((int)(vv.getCenter().getX() * 0.5), (int)(vv.getCenter().getY() *  0.5));
      scaler.scale(vv, 1.2f, point2d);
    }
  }

  class Rings implements VisualizationServer.Paintable {

    Collection<Double> depths;

    public Rings() {
      depths = getDepths();
    }

    private Collection<Double> getDepths() {
      Set<Double> depths = new HashSet<Double>();
      Map<String, PolarPoint> polarLocations = radialLayout.getPolarLocations();
      for (String v : TreeCollapse.this.forest.getVertices()) {
        PolarPoint pp = polarLocations.get(v);
        depths.add(pp.getRadius());
      }
      return depths;
    }

    public void paint(Graphics g) {
      g.setColor(Color.lightGray);

      Graphics2D g2d = (Graphics2D) g;
      Point2D center = radialLayout.getCenter();

      Ellipse2D ellipse = new Ellipse2D.Double();
      for (double d : depths) {
        ellipse.setFrameFromDiagonal(center.getX() - d, center.getY() - d,
            center.getX() + d, center.getY() + d);
        Shape shape = vv.getRenderContext().
            getMultiLayerTransformer().getTransformer(Layer.LAYOUT).transform(ellipse);
        g2d.draw(shape);
      }
    }

    public boolean useTransform() {
      return true;
    }
  }

  /**
   * 
   */
  private void createTree() {
    //graph.addVertex("V0");
    //graph.addEdge(edgeFactory.create(), "V0", "V1");
    //graph.addEdge(edgeFactory.create(), "V0", "V2");
    //graph.addEdge(edgeFactory.create(), "V1", "V4");
    //graph.addEdge(edgeFactory.create(), "V2", "V3");
    //graph.addEdge(edgeFactory.create(), "V2", "V5");
    //graph.addEdge(edgeFactory.create(), "V4", "V6");
    //graph.addEdge(edgeFactory.create(), "V4", "V7");
    //graph.addEdge(edgeFactory.create(), "V3", "V8");
    //graph.addEdge(edgeFactory.create(), "V6", "V9");
    //graph.addEdge(edgeFactory.create(), "V4", "V10");
    //
    //graph.addVertex("A0");
    //graph.addEdge(edgeFactory.create(), "A0", "A1");
    //graph.addEdge(edgeFactory.create(), "A0", "A2");
    //graph.addEdge(edgeFactory.create(), "A0", "A3");
    //
    //graph.addVertex("B0");
    //graph.addEdge(edgeFactory.create(), "B0", "B1");
    //graph.addEdge(edgeFactory.create(), "B0", "B2");
    //graph.addEdge(edgeFactory.create(), "B1", "B4");
    //graph.addEdge(edgeFactory.create(), "B2", "B3");
    //graph.addEdge(edgeFactory.create(), "B2", "B5");
    //graph.addEdge(edgeFactory.create(), "B4", "B6");
    //graph.addEdge(edgeFactory.create(), "B4", "B7");
    //graph.addEdge(edgeFactory.create(), "B3", "B8");
    //graph.addEdge(edgeFactory.create(), "B6", "B9");

    //<param name="vertex_0" value="Arts and Sciences"/>
    //<param name="edgeFrom_0" value="Arts and Sciences"/>
    //<param name="edgeTo_0" value="Math"/>
    //<param name="edgeFrom_1" value="Arts and Sciences"/>
    //<param name="edgeTo_1" value="English"/>
    
    List<String> vertices = new ArrayList<String>();
    
    //get vertices
    for (int i=0;i<5000;i++) {
      String vertex = null;
      vertex = getParameter("vertex_" + i);  
      if (vertex == null) {
        break;
      }
      vertices.add(vertex);
    }
    
    List<String> edgesFrom = new ArrayList<String>();
    List<String> edgesTo = new ArrayList<String>();
    
    Set<String> uniqueEdgesTo = new HashSet<String>();
    
    //its a hierarchy until it isnt
    this.isHierarchy = true;
    
    //get edge
    for (int i=0;i<5000;i++) {
      String edgeFrom = null;
      String edgeTo = null;
      
      edgeFrom = getParameter("edgeFrom_" + i);  
      edgeTo = getParameter("edgeTo_" + i);  
      if (edgeFrom == null) {
        break;
      }
      if (edgeTo == null) {
        break;
      }
      
      if (uniqueEdgesTo.contains(edgeTo)) {
        this.isHierarchy = false;
      }
      uniqueEdgesTo.add(edgeTo);
      
      edgesFrom.add(edgeFrom);
      edgesTo.add(edgeTo);
      
    }
    
    // create a simple graph for the demo
    if (this.isHierarchy) {
      this.forest = new DelegateForest<String, Integer>();
    } else {
      this.directedSparseMultigraph = new DirectedSparseMultigraph<String, Integer>();
    }

    for (String vertex : vertices) {
      if (this.isHierarchy) {
        this.forest.addVertex(vertex);
      } else {
        this.directedSparseMultigraph.addVertex(vertex);
      }
    }

    int i=0;
    for (String edgeFrom : edgesFrom) {
      String edgeTo = edgesTo.get(i);
      if (this.isHierarchy) {
        this.forest.addEdge(edgeFactory.create(), edgeFrom, edgeTo);
      } else {
        this.directedSparseMultigraph.addEdge(edgeFactory.create(), edgeFrom, edgeTo);
      }
      i++;
    }

    
//    graph.addVertex("V0");
//    graph.addEdge(edgeFactory.create(), "V0", "V1");
//    graph.addEdge(edgeFactory.create(), "V0", "V2");
  }

  /**
  * a demo class that will create a vertex shape that is either a
  * polygon or star. The number of sides corresponds to the number
  * of vertices that were collapsed into the vertex represented by
  * this shape.
  * 
  * @author Tom Nelson
  *
  * @param <V>
  */
  class ClusterVertexShapeFunction<V> extends EllipseVertexShapeTransformer<V> {

    ClusterVertexShapeFunction() {
      setSizeTransformer(new ClusterVertexSizeFunction<V>(20));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Shape transform(V v) {
      if (v instanceof Graph) {
        int size = ((Graph) v).getVertexCount();
        if (size < 8) {
          int sides = Math.max(size, 3);
          return factory.getRegularPolygon(v, sides);
        } else {
          return factory.getRegularStar(v, size);
        }
      }
      return super.transform(v);
    }
  }

  /**
   * A demo class that will make vertices larger if they represent
   * a collapsed collection of original vertices
   * @author Tom Nelson
   *
   * @param <V>
   */
  class ClusterVertexSizeFunction<V> implements Transformer<V, Integer> {

    int size;

    public ClusterVertexSizeFunction(Integer size) {
      this.size = size;
    }

    public Integer transform(V v) {
      if (v instanceof Graph) {
        return 30;
      }
      return size;
    }
  }

  /**
   * a driver for this demo
   */
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    content.add(new TreeCollapse());
    frame.pack();
    frame.setVisible(true);
  }
}
