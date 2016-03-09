package com.bono.directory;

import com.bono.api.DBExecutor;
import com.bono.api.MPDCommand;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Created by hendriknieuwenhuis on 08/03/16.
 */
public class TreeMouseListener extends MouseAdapter {

    private DBExecutor dbExecutor;

    public TreeMouseListener(DBExecutor dbExecutor) {
        this.dbExecutor = dbExecutor;
    }

    private String listfilesUrl(Object[] path) {
        String url = "\"";
        for (int i = 1; i < path.length; i++) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) path[i];

            if (i == (path.length - 1)) {
                url = url + n.toString() + "\"";
                return url;
            }
            url = url + n.toString() + File.separator;
        }
        return url;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);

        JTree tree = (JTree) e.getSource();

        if (e.getClickCount() == 2) {

            TreeSelectionModel model = tree.getSelectionModel();

            TreePath path = model.getSelectionPath();

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

            Directory directory;

            if (node.getAllowsChildren()) {

                directory = new Directory(node, (DefaultTreeModel) tree.getModel());

                try {
                    directory.populate(dbExecutor.execute(new MPDCommand("lsinfo", listfilesUrl(path.getPath()))));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            }

            // keep focused.
            tree.setSelectionPath(new TreePath(node.getPath()));

        }

    }
}