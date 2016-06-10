package com.bono;

import com.bono.api.*;
import com.bono.config.ConfigOptions;
import com.bono.config.ZeroConfig;
import com.bono.controls.*;
import com.bono.controls.CurrentPlaylist;
import com.bono.directory.DirectoryPresenter;
import com.bono.soundcloud.SoundcloudController;
import com.bono.view.ApplicationView;
import com.bono.view.ConfigOptionsView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by hendriknieuwenhuis on 11/05/16.
 */
public class Application extends WindowAdapter {

    private Dimension dimension;

    private ApplicationView applicationView;

    private Player player;
    private PlaylistControl playlistControl;
    private CurrentPlaylist currentPlaylist;
    private CurrentSong currentSong;
    private DirectoryPresenter directoryPresenter;

    private SoundcloudController soundcloudController;

    private DBExecutor dbExecutor;

    private Status status;

    private Config config;

    private IdleRunner idleRunner;

    private Object object;

    public Application() {
        setupContact();
        initModels();
        buildView();
    }

    /*
    Sets up contact with the server by, loading a config file that
    on absence displays a config view to obtain the config values.
    */
    private void setupContact() {
        config = new Config();
        try {
            /*
            Load the config file.
            When file is not available,
            exception is thrown.
             */
            config.loadConfig();
        } catch (Exception e) {
            /*
            No config file so, standard
            values are set and ConfigOptions
            class is initialized to optain
            host and port value.
             */
            try {
                config.setProperty(ZeroConfig.SOUNDCLOUD_RESULTS, "30");
                ConfigOptions configOptions = new ConfigOptions(config);
            } catch (InterruptedException in) {
                in.printStackTrace();
            } catch (InvocationTargetException inv) {
                inv.printStackTrace();
            }
        }
        /*
        Check if host and port values are
        present and test them.
        */
        if (config.getProperty(ZeroConfig.HOST_PROPERTY) != null &&
                config.getProperty(ZeroConfig.PORT_PROPERTY) != null) {
            //System.out.println("Succesfully found host and port properties.");
            String host = config.getProperty(ZeroConfig.HOST_PROPERTY);
            int port = Integer.parseInt(config.getProperty(ZeroConfig.PORT_PROPERTY));

            dbExecutor = new DBExecutor(host, port);


            try {
                //System.out.println(dbExecutor.execute(new DefaultCommand("ping")));
                Endpoint endpoint = new Endpoint(host, port);
                System.out.println("Waiting for version...");
                System.out.println(endpoint.getVersion());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static Dimension frameDimension() {
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
            applicationView = new ApplicationView(Application.frameDimension(), this);

            player.addView(applicationView.getControlView());
            currentSong.addView(applicationView.getControlView());

            directoryPresenter = new DirectoryPresenter(dbExecutor, applicationView.getDirectoryView());
            applicationView.getDirectoryView().getDirectory().addTreeWillExpandListener(directoryPresenter);

            soundcloudController = new SoundcloudController(config, dbExecutor, applicationView.getSoundcloudView());

            applicationView.getControlView().addNextListener(player);
            applicationView.getControlView().addStopListener(player);
            applicationView.getControlView().addPlayListener(player);
            applicationView.getControlView().addPreviousListener(player);

            applicationView.getPlaylistView().addMouseListener(currentPlaylist);
            currentPlaylist.setPlaylistView(applicationView.getPlaylistView());
            currentPlaylist.initPlaylist();

            applicationView.show();
            updateStatus();
        });
    }

    private void initModels() {
        status = new Status();
        player = new Player(dbExecutor, status);
        playlistControl = new PlaylistControl(dbExecutor);
        currentPlaylist = new CurrentPlaylist(dbExecutor, player);
        currentSong = new CurrentSong(playlistControl);
        status.addListener(currentSong);
    }

    private void updateStatus() {
        try {
            //status.populate();
            status.populateStatus(dbExecutor.execute(new DefaultCommand(Status.STATUS)));
        } catch (ACKException ack) {
            ack.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showConfigView() {
        try {
            ConfigOptions configOptions = new ConfigOptions(config);
        } catch (InvocationTargetException inv) {
            inv.printStackTrace();
        } catch (InterruptedException in) {
            in.printStackTrace();
        }
    }


    /*
    Implementation of the extended WindowsAdapter.
     */
    // Adapter stuff
    private boolean closing = false;

    @Override
    public void windowOpened(WindowEvent e) {
        super.windowOpened(e);
        //System.out.println("windowOpened");
    }

    @Override
    public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
        System.out.println("windowClosing");

        closing = true;
    }

    @Override
    public void windowClosed(WindowEvent e) {
        super.windowClosed(e);
        //System.out.println("windowClosed");
    }

    @Override
    public void windowIconified(WindowEvent e) {
        super.windowIconified(e);
        //System.out.println("windowIconified");
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        super.windowDeiconified(e);
        //System.out.println("windowDeiconified");
    }

    @Override
    public void windowActivated(WindowEvent e) {
        super.windowActivated(e);
        System.out.println("windowActivated");

        idleRunner = new IdleRunner(status);
        //idleRunner.addListener(new StatusUpdate());
        idleRunner.addListener(player);
        idleRunner.addListener(currentPlaylist);
        idleRunner.start();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        super.windowDeactivated(e);
        System.out.println("windowDeactivated");

        // kan ook in iconified!!!!
        idleRunner.removeListeners();
        idleRunner = null;
        /// !!!!!!!!!!!!!!!!!!!!!!!

        if (closing) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        new Application();
    }
}
