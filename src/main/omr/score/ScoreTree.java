//----------------------------------------------------------------------------//
//                                                                            //
//                             S c o r e T r e e                              //
//                                                                            //
//  Copyright (C) Herve Bitteur 2000-2006. All rights reserved.               //
//  This software is released under the terms of the GNU General Public       //
//  License. Please contact the author at herve.bitteur@laposte.net           //
//  to report bugs & suggestions.                                             //
//----------------------------------------------------------------------------//
//
package omr.score;

import omr.util.Dumper;
import omr.util.Implement;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/**
 * Class <code>ScoreTree</code> provides a user interface (a frame) where the
 * whole score hierarchy can be browsed as a tree.
 *
 * @author Herv&eacute; Bitteur
 * @version $Id$
 */
public class ScoreTree
{
    //~ Static fields/initializers ---------------------------------------------

    /** Default window height in pixels */
    private static final int WINDOW_HEIGHT = 550;

    /** Default width in pixels for the left part (the tree) */
    private static final int LEFT_WIDTH = 300;

    /** Default width in pixels for the right part (the detail) */
    private static final int RIGHT_WIDTH = 340;

    /** Default windows width in pixels */
    private static final int WINDOW_WIDTH = LEFT_WIDTH + RIGHT_WIDTH;

    //~ Instance fields --------------------------------------------------------

    /** Concrete UI component */
    private JPanel component;

    //~ Constructors -----------------------------------------------------------

    //-----------//
    // ScoreTree //
    //-----------//
    private ScoreTree (Score score)
    {
        component = new JPanel();

        // Make a nice border
        EmptyBorder    eb = new EmptyBorder(5, 5, 5, 5);
        BevelBorder    bb = new BevelBorder(BevelBorder.LOWERED);
        CompoundBorder cb = new CompoundBorder(eb, bb);
        component.setBorder(new CompoundBorder(cb, eb));

        // Set up the tree
        JTree       tree = new JTree(new Adapter(score));

        // Build left-side view
        JScrollPane treeView = new JScrollPane(tree);
        treeView.setPreferredSize(new Dimension(LEFT_WIDTH, WINDOW_HEIGHT));

        // Build right-side view
        final JEditorPane htmlPane = new JEditorPane("text/html", "");
        htmlPane.setEditable(false);

        JScrollPane htmlView = new JScrollPane(htmlPane);
        htmlView.setPreferredSize(new Dimension(RIGHT_WIDTH, WINDOW_HEIGHT));

        // Allow only single selections
        tree.getSelectionModel()
            .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Display lines to explicit relationships
        tree.putClientProperty("JTree.lineStyle", "Angled");

        // Wire the two views together. Use a selection listener
        // created with an anonymous inner-class adapter.
        // Listen for when the selection changes.
        tree.addTreeSelectionListener(
            new TreeSelectionListener() {
                    public void valueChanged (TreeSelectionEvent e)
                    {
                        TreePath p = e.getNewLeadSelectionPath();

                        if (p != null) {
                            ScoreNode node = (ScoreNode) p.getLastPathComponent();
                            htmlPane.setText(Dumper.htmlDumpOf(node));
                        }
                    }
                });

        // Build split-pane view
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            treeView,
            htmlView);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(LEFT_WIDTH);
        splitPane.setPreferredSize(
            new Dimension(WINDOW_WIDTH + 10, WINDOW_HEIGHT + 10));

        // Add GUI components
        component.setLayout(new BorderLayout());
        component.add("Center", splitPane);
    }

    //~ Methods ----------------------------------------------------------------

    //     //------//
    //     // main //
    //     //------//
    //     /**
    //      * This class can be used in stand-alone, to browse a score specified
    //      * in the command line
    //      *
    //      * @param argv only one argument : the name of the score XML file
    //      */
    //     public static void main (String[] argv)
    //     {
    //         // Global OMR properties
    //         //Constant.loadResource ("/User.properties");
    //         // Load score from an XML file
    //         Score score = ScoreManager.getInstance().load(new File(argv[0]));

    //         // Build the display frame
    //         JFrame frame = makeFrame(argv[0], score);
    //         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //     }

    //-----------//
    // makeFrame //
    //-----------//
    /**
     * Create a frame for the score tree
     *
     * @param name  the score name
     * @param score the score entity
     *
     * @return the created frame
     */
    public static JFrame makeFrame (String name,
                                    Score  score)
    {
        // Set up a GUI framework
        JFrame          frame = new JFrame("Tree of " + name);

        // Set up the tree, the views, and display it all
        final ScoreTree scoreTree = new ScoreTree(score);
        frame.getContentPane()
             .add("Center", scoreTree.component);
        frame.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit()
                                      .getScreenSize();
        int       w = WINDOW_WIDTH + 10;
        int       h = WINDOW_HEIGHT + 10;
        frame.setLocation(
            (screenSize.width / 3) - (w / 2),
            (screenSize.height / 2) - (h / 2));
        frame.setSize(w, h);
        frame.setVisible(true);

        return frame;
    }

    //~ Inner Classes ----------------------------------------------------------

    // This adapter converts the current Score into a JTree model.
    private static class Adapter
        implements TreeModel
    {
        private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
        private Score                   score;

        //---------//
        // Adapter //
        //---------//
        public Adapter (Score score)
        {
            this.score = score;
        }

        //----------//
        // getChild //
        //----------//
        @Implement(TreeModel.class)
        public Object getChild (Object parent,
                                int    index)
        {
            ScoreNode node = (ScoreNode) parent;

            return (ScoreNode) node.getChildren()
                                   .get(index);
        }

        //---------------//
        // getChildCount //
        //---------------//
        @Implement(TreeModel.class)
        public int getChildCount (Object parent)
        {
            ScoreNode node = (ScoreNode) parent;

            return node.getChildren()
                       .size();
        }

        //-----------------//
        // getIndexOfChild //
        //-----------------//
        @Implement(TreeModel.class)
        public int getIndexOfChild (Object parent,
                                    Object child)
        {
            ScoreNode node = (ScoreNode) parent;

            return node.getChildren()
                       .indexOf(child);
        }

        //--------//
        // isLeaf //
        //--------//
        @Implement(TreeModel.class)
        public boolean isLeaf (Object node)
        {
            // Determines whether the icon shows up to the left.
            // Return true for any node with no children
            ScoreNode musicNode = (ScoreNode) node;

            return getChildCount(musicNode) <= 0;
        }

        //---------//
        // getRoot //
        //---------//
        @Implement(TreeModel.class)
        public Object getRoot ()
        {
            return score;
        }

        /*
         * Use these methods to add and remove event listeners.
         * (Needed to satisfy TreeModel interface, but not used.)
         */

        //----------------------//
        // addTreeModelListener //
        //----------------------//
        @Implement(TreeModel.class)
        public void addTreeModelListener (TreeModelListener listener)
        {
            if ((listener != null) && !listeners.contains(listener)) {
                listeners.add(listener);
            }
        }

        //-------------------------//
        // removeTreeModelListener //
        //-------------------------//
        @Implement(TreeModel.class)
        public void removeTreeModelListener (TreeModelListener listener)
        {
            if (listener != null) {
                listeners.remove(listener);
            }
        }

        //---------------------//
        // valueForPathChanged //
        //---------------------//
        @Implement(TreeModel.class)
        public void valueForPathChanged (TreePath path,
                                         Object   newValue)
        {
            // Null. We won't be making changes in the GUI.  If we did, we would
            // ensure the new value was really new and then fire a
            // TreeNodesChanged event.
        }
    }
}
