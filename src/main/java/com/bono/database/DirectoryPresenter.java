package com.bono.database;

import com.bono.Utils;
import com.bono.api.*;
import com.bono.api.protocol.MPDDatabase;
import com.bono.api.protocol.MPDPlaylist;
import com.bono.view.DirectoryView;
import com.bono.view.MPDPopup;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hendriknieuwenhuis on 26/05/16.
 */
public class DirectoryPresenter implements TreeWillExpandListener, TreeExpansionListener, MouseListener {

    private final String DIRECTORY_PREFIX  = "database";
    private final String FILE_PREFIX       = "file";

    private DefaultMutableTreeNode root = null;

    private JTree tree;

    private ClientExecutor clientExecutor;

    public DirectoryPresenter(ClientExecutor clientExecutor, DirectoryView directoryView) {
        this.clientExecutor = clientExecutor;
        tree = directoryView.getDirectory();
        //tree.setRootVisible(false);
        //initDirectory();
        //root = (DefaultMutableTreeNode) tree.getModel().getRoot();
    }

    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        TreePath path = event.getPath();

        DefaultMutableTreeNode current = (DefaultMutableTreeNode)path.getLastPathComponent();
        //current.removeAllChildren();
        System.out.println(current.toString());

        // TODO soms wordt de treenode vals voor root gezien !!!!!
        //if (current.isRoot()) {
        //    System.out.println("root " + current.toString());
        //}
        List<MutableTreeNode> list = loadNodes(current);

        current.removeAllChildren();

        Iterator<MutableTreeNode> i = list.iterator();
        while (i.hasNext()) {
            current.add(i.next());
        }
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
        //System.out.println("Tree will collapse");
        //DefaultMutableTreeNode current = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
        //current.removeAllChildren();
        //current.add(new DefaultMutableTreeNode("loading...", false));
    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {

    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
        DefaultMutableTreeNode current = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
        //current.removeAllChildren();
        current.add(new DefaultMutableTreeNode("loading...", false));
    }

    public void initDirectory() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getModel().getRoot();

        List<MutableTreeNode> list = loadNodes(node);

        node.removeAllChildren();

        Iterator<MutableTreeNode> i = list.iterator();
        while (i.hasNext()) {
            MutableTreeNode tNode = i.next();
            node.add(tNode);
            //System.out.println(tNode.toString());
        }
        tree.expandPath(new TreePath(node));
    }

    private String listfilesUrl(Object[] path) {

        if ( (path == null)) {
            return null;
        }

        String url = "\"";

        if (path.length == 1) {
            //System.out.println("One");
            url += path[0] + "\"";
        }

        for (int i = 0; i < path.length; i++) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) path[i];

            if (i == (path.length - 1)) {
                url = url + n.toString() + "\"";
                return url;
            }
            url = url + n.toString() + File.separator;
        }
        System.out.println(url);
        return url;
    }

    private List<MutableTreeNode> loadNodes(DefaultMutableTreeNode current) {
        List<MutableTreeNode> list = new ArrayList<>();
        DefaultMutableTreeNode node;
        String[] name;
        //String response = "";
        List<String> response = new ArrayList<>();


        //if (!current.isRoot()) {
            try {
                //response = lsinfo(listfilesUrl(current.getPath()));
                //System.out.println("Is not root!: " + current.toString());
                response = clientExecutor.execute(new DefaultCommand(MPDDatabase.LSINFO, listfilesUrl(current.getPath())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        /*
        } else {
            try {
                //response = lsinfo("");
                System.out.println("Is root!: " + current.toString());
                response = clientExecutor.execute(new DefaultCommand(MPDDatabase.LSINFO, ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

        //Reply reply = new Reply(response);

        Iterator<String> i = response.iterator();
        while (i.hasNext()) {
            String[] line = i.next().split(": ");

            switch (line[0]) {
                case DIRECTORY_PREFIX:
                    name = line[1].split(File.separator);
                    node = new DefaultMutableTreeNode(name[(name.length -1)]);
                    node.add(new DefaultMutableTreeNode("loading...", false));
                    list.add(node);
                    break;
                case FILE_PREFIX:
                    name = line[1].split(File.separator);
                    node = new DefaultMutableTreeNode(name[(name.length -1)]);
                    list.add(node);
                    break;
                default:
                    break;
            }
        }
        return list;
    }


    /*

	MouseAdapter.

	 */
    @Override
    public void mouseClicked(MouseEvent e) {

        if (e.getButton() == MouseEvent.BUTTON3) {
            // root can not be added!
            if (tree.getSelectionPath().getPathCount() > 1) {
                MPDPopup popup = new MPDPopup();
                popup.addMenuItem("add", new AddListener());
                popup.show(tree, e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        /*
        if (e.getButton() == MouseEvent.BUTTON1) {
            JComponent component = (JComponent) e.getSource();
            TransferHandler transferHandler = new TransferHandler("tree");
        }*/
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private class AddListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            DefaultTreeSelectionModel model = (DefaultTreeSelectionModel) tree.getSelectionModel();

            if (!model.isSelectionEmpty()) {
                TreePath path = model.getSelectionPath();
                List<String> response = new ArrayList<>();
                try {
                    response = clientExecutor.execute(new DefaultCommand(MPDPlaylist.ADD, Utils.filesUrl(path.getPath())));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                for (String s : response) {
                    Utils.Log.print(s);
                }
            }
        }
    }

}