package com.bono.view;

import com.bono.CustemTabbedPaneUI;
import com.bono.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;

/**
 * Created by hendriknieuwenhuis on 23/02/16.
 */
public class ApplicationView  {

    private JFrame frame;
    private ControlView controlView;
    private PlaylistView playlistView;
    private SoundcloudView soundcloudView;
    private DirectoryView directoryView;

    public ApplicationView(Dimension dimension, WindowAdapter adapter) {
        build(dimension, adapter);
    }

    public ApplicationView(JList list) {
        //playlistView.getViewport()
    }

    private void build(Dimension dimension, WindowAdapter adapter) {
        frame = new JFrame("zero");
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(adapter);

        frame.getContentPane().setPreferredSize(dimension);

        controlView = new ControlView();
        frame.getContentPane().add(controlView, BorderLayout.NORTH);

        soundcloudView = new SoundcloudView();

        directoryView = new DirectoryView();

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.setTabPlacement(SwingConstants.TOP);
        //tabbedPane.setUI(new CustemTabbedPaneUI());
        tabbedPane.addTab("directory", directoryView.getScrollPane());
        tabbedPane.addTab("soundcloud", soundcloudView);


        playlistView = new PlaylistView();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        splitPane.setLeftComponent(tabbedPane);
        splitPane.setRightComponent(playlistView);

        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
    }

    public ControlView getControlView() {
        return controlView;
    }

    public PlaylistView getPlaylistView() {
        return playlistView;
    }

    public SoundcloudView getSoundcloudView() {
        return soundcloudView;
    }

    public DirectoryView getDirectoryView() {
        return directoryView;
    }

    public void show() {
        frame.pack();
        frame.setVisible(true);
    }

    public boolean isIconified() {
        return frame.isBackgroundSet();
    }
}
