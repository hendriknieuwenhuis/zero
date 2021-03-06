package com.bono;

import com.bono.api.*;
import com.bono.controls.MenuBarController;
import com.bono.controls.*;

import com.bono.database.MusicDatabase;
import com.bono.icons.BonoIcon;
import com.bono.icons.BonoIconFactory;
import com.bono.icons.BonoTreeIcon;
import com.bono.laf.BonoLafUtils;
import com.bono.playlist.PlaylistPresenter;
import com.bono.view.ApplicationView;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;

/**
 * Created by hendriknieuwenhuis on 11/05/16.
 */
public class ApplicationMain extends WindowAdapter {

    private String version;

    private ApplicationView applicationView;

    private PlaybackPresenter playbackPresenter;

    private PlaylistPresenter playlistPresenter;

    private MusicDatabase musicDatabase;

    private MenuBarController menuBarController;

    private Properties properties;

    private MPDClient mpdClient;

    public ApplicationMain() {
        initClient();
        initModels();
        buildView();
    }

    private void initClient() {
        try {
            properties = ConfigLoader.loadconfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mpdClient = new MPDClient(properties.getProperty(ConfigLoader.HOST),
                Integer.parseInt(properties.getProperty(ConfigLoader.PORT)));
    }


    public static Dimension appDimension() {
        GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        double width = (graphicsDevice.getDisplayMode().getWidth() * 0.8);
        double height = (graphicsDevice.getDisplayMode().getHeight() * 0.8);
        return  new Dimension((int) width, (int)height);
    }

    public static Dimension screenDimension() {
        GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        double width = (graphicsDevice.getDisplayMode().getWidth());
        double height = (graphicsDevice.getDisplayMode().getHeight());
        return  new Dimension((int) width, (int)height);
    }


    // build the view.
    private void buildView() {
        SwingUtilities.invokeLater(() -> {
            applicationView = new ApplicationView(ApplicationMain.appDimension(), this);

            playbackPresenter.addPlaybackView(applicationView.getPlaybackControlsView());

            musicDatabase.initDatabaseBrowserView(applicationView.getDatabaseBrowserView());
            musicDatabase.setSoundcloudView(applicationView.getSoundcloudView());
            musicDatabase.setStoredPlaylistsController(applicationView.getStoredPlaylistsView());
            applicationView.getCurrentPlaylistView().addMouseListener(playlistPresenter);
            playlistPresenter.addView(applicationView.getCurrentPlaylistView());

            applicationView.getVersionPanel().setVersion(version);
            applicationView.addConfigMenuItemlistener(menuBarController.configMenuItemListener());
            applicationView.addSavePlaylistMenuItemListener(menuBarController.savePlaylistMenuItemListener());
            applicationView.addTabChangeListener(musicDatabase);

            applicationView.show();
        });
    }

    private void initModels() {
        try {
            version = mpdClient.getVersion();
        } catch (IOException e) {
            e.printStackTrace();
        }
        playlistPresenter = new PlaylistPresenter(mpdClient);
        playlistPresenter.initPlaylist();
        playbackPresenter = new PlaybackPresenter(mpdClient);
        musicDatabase = new MusicDatabase(mpdClient);
        menuBarController = new MenuBarController(this);
    }

    private void updateStatus() {
        try {
            mpdClient.getServerMonitor().initMonitor();
        } catch (ACKException ack) {
            ack.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
    Implementation of the extended WindowAdapter.
     */

    // Adapter stuff
    private boolean closing = false;

    // Update status so the listeners are
    // triggered and various values are set.
    // Start the idle thread listening for
    // changes in the server.
    @Override
    public void windowOpened(WindowEvent e) {
        super.windowOpened(e);
        mpdClient.initServerMonitor();
        updateStatus();
        mpdClient.getServerMonitor().addMonitorListener(playlistPresenter.getIdleListener());
        mpdClient.getServerMonitor().addMonitorListener(musicDatabase.getStoredPlaylistsController());
        mpdClient.runMonitor();

    }

    // set the closing boolean to true so
    // in windowDeactivated it will end
    // the application.
    @Override
    public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
        System.out.println("closing");
        closing = true;
    }


    // close ServerMonitor while iconified.
    // it is newly initialized when the
    // window is activated again after
    // being deiconified.
    @Override
    public void windowIconified(WindowEvent e) {
        super.windowIconified(e);
        mpdClient.getServerMonitor().removeMonitorListeners();
        mpdClient.getServerMonitor().close();


    }

    // Same as windowOpened. ApplicationMain is
    // coming back from being iconified.
    @Override
    public void windowDeiconified(WindowEvent e) {
        super.windowDeiconified(e);
        mpdClient.initServerMonitor();
        updateStatus();
        mpdClient.getServerMonitor().addMonitorListener(playlistPresenter.getIdleListener());
        mpdClient.runMonitor();
    }


    // End the application if closing boolean is true.
    @Override
    public void windowDeactivated(WindowEvent e) {
        super.windowDeactivated(e);

        if (closing) {
            System.exit(0);
        }
    }

    // For testing purpose only.
    private class IdleListener implements ChangeListener {

        @Override
        public void stateChanged(EventObject eventObject) {
            String state = (String) eventObject.getSource();
            System.out.println(state);
        }
    }

    public static void main(String[] args) {

        try {
            // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException e) {
            // handle exception
        }
        catch (ClassNotFoundException e) {
            // handle exception
        }
        catch (InstantiationException e) {
            // handle exception
        }
        catch (IllegalAccessException e) {
            // handle exception
        }

        UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);
        UIManager.getDefaults().put("List.selectionBackground", BonoLafUtils.brightness(Color.gray, 1.8));
        UIManager.getDefaults().put("Tree.selectionBackground", BonoLafUtils.brightness(Color.gray, 1.8));
        UIManager.getDefaults().put("Tree.selectionBorderColor", BonoLafUtils.brightness(Color.gray, 1.7));
        UIManager.getDefaults().put("ScrollBar.width", 14);
        UIManager.getDefaults().put("ButtonUI", "com.bono.laf.BonoButtonUI");
        UIManager.getDefaults().put("SliderUI", "com.bono.laf.BonoSliderUI");
        UIManager.getDefaults().put("MenuItemUI", "com.bono.laf.BonoMenuItemUI");
        UIManager.getDefaults().put("MenuItem.borderPainted", false);

        //Border border = BorderFactory.createLineBorder(Color.gray);
        UIManager.getDefaults().put("PopupMenu.border", BorderFactory.createLineBorder(Color.gray));

        BonoTreeIcon file = BonoIconFactory.getFileIcon();
        file.setIconHeight(14);
        file.setIconWidth(14);
        //UIManager.getDefaults().put("Tree.leafIcon", file);
        BonoTreeIcon folder = BonoIconFactory.getFolderClosed();
        folder.setIconWidth(14);
        folder.setIconHeight(14);
        //UIManager.getDefaults().put("Tree.closedIcon", folder);
        //UIManager.getDefaults().put("Tree.openIcon", folder);
        new ApplicationMain();
    }
}
