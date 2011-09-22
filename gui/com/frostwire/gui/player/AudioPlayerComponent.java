package com.frostwire.gui.player;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.limewire.concurrent.ExecutorsHelper;
import org.limewire.util.OSUtils;

import com.frostwire.gui.library.LibraryUtils;
import com.frostwire.mplayer.MediaPlaybackState;
import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.MediaButton;
import com.limegroup.gnutella.gui.MediaSlider;
import com.limegroup.gnutella.gui.RefreshListener;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;
import com.limegroup.gnutella.util.FrostWireUtils;
import com.limegroup.gnutella.util.Tagged;

/**
 * This class sets up JPanel with MediaPlayer on it, and takes care of GUI
 * MediaPlayer events.
 */
public final class AudioPlayerComponent implements AudioPlayerListener, RefreshListener, ThemeObserver {

    public static final String STREAMING_AUDIO = "Streaming Audio";

    /**
     * Constant for the play button.
     */
    private final MediaButton PLAY_BUTTON = new MediaButton(I18n.tr("Play"), "play_up", "play_dn");

    /**
     * Constant for the pause button.
     */
    private final MediaButton PAUSE_BUTTON = new MediaButton(I18n.tr("Pause"), "pause_up", "pause_dn");

    /**
     * Constant for the stop button.
     */
    private final MediaButton STOP_BUTTON = new MediaButton(I18n.tr("Stop"), "stop_up", "stop_dn");

    /**
     * Constant for the forward button.
     */
    private final MediaButton NEXT_BUTTON = new MediaButton(I18n.tr("Next"), "forward_up", "forward_dn");

    /**
     * Constant for the rewind button.
     */
    private final MediaButton PREV_BUTTON = new MediaButton(I18n.tr("Previous"), "rewind_up", "rewind_dn");

    /**
     * Constant for the volume control
     */
    private final MediaSlider VOLUME = new MediaSlider("volume_labels");

    /**
     * Constant for the progress bar
     */
    private final JProgressBar PROGRESS = new JProgressBar();
    
    private final JLabel progressCurrentTime = new JLabel();
    
    private final JLabel progressSongLength = new JLabel();

    /**
     * Executor to ensure all thread creation on the frostwireplayer is called from
     * a single thread
     */
    private final ExecutorService SONG_QUEUE = ExecutorsHelper.newProcessingQueue("SongProcessor");

    /**
     * The MP3 player.
     */
    private final AudioPlayer PLAYER;

    /**
     * The ProgressBar dimensions for showing the name & play progress.
     */
    private final Dimension progressBarDimension = new Dimension(129, 10);

    /**
     * Volume slider dimensions for adjusting the audio level of a song
     */
    private final Dimension volumeSliderDimension = new Dimension(70, 19);

    /**
     * The current song that is playing
     */
    private AudioSource currentPlayListItem;

    /**
     * The lazily constructed media panel.
     */
    private JPanel myMediaPanel = null;

    /**
     * If true, will only play current song and stop, regradless of position
     * in the playlist or value of continous or random. If false, continous
     * and random control the play feature
     */
    private boolean playOneTime = false;

    /**
     * Lock for access to the above String.
     */
    //private final Object cfnLock = new Object();

    private float _progress;

	private JToggleButton SHUFFLE_BUTTON;

	private JToggleButton LOOP_BUTTON;

    /**
     * Constructs a new <tt>MediaPlayerComponent</tt>.
     */
    public AudioPlayerComponent() {
        PLAYER = AudioPlayer.instance();
        PLAYER.addAudioPlayerListener(this);

        GUIMediator.addRefreshListener(this);
        ThemeMediator.addThemeObserver(this);
    }

    public JPanel getMediaPanel() {
    	return getMediaPanel(false);
    }
    
    /**
     * Gets the media panel, constructing it if necessary.
     */
    public JPanel getMediaPanel(boolean showPlaybackModeControls) {
        if (myMediaPanel == null)
            myMediaPanel = constructMediaPanel(showPlaybackModeControls);
        return myMediaPanel;
    }

    /**
     * Constructs the media panel.
     * @param showPlaybackModeControls 
     */
    private JPanel constructMediaPanel(boolean showPlaybackModeControls) {
        int tempHeight = 0;
        tempHeight += PLAY_BUTTON.getIcon().getIconHeight() + 2;

        // create sliders
        PROGRESS.setMinimumSize(progressBarDimension);
        PROGRESS.setMaximumSize(progressBarDimension);
        PROGRESS.setPreferredSize(progressBarDimension);
        //PROGRESS.setString(I18n.tr("FrostWire Media Player"));
        PROGRESS.setMaximum(3600);
        PROGRESS.setEnabled(false);

        VOLUME.setMaximumSize(volumeSliderDimension);
        VOLUME.setPreferredSize(volumeSliderDimension);
        VOLUME.setMinimum(0);
        VOLUME.setValue(50);
        VOLUME.setMaximum(100);
        VOLUME.setEnabled(true);

        // setup buttons
        registerListeners();

        // add everything
        JPanel buttonPanel = new BoxPanel(BoxPanel.X_AXIS);
        buttonPanel.setMaximumSize(new Dimension(370, tempHeight)); //tempWidth + PROGRESS.getWidth() + VOLUME.getWidth()
        buttonPanel.add(Box.createHorizontalGlue());
        
        if (showPlaybackModeControls) {
        	initPlaylistPlaybackModeControls();
        	buttonPanel.add(SHUFFLE_BUTTON);
        	buttonPanel.add(Box.createHorizontalStrut(5));
        	buttonPanel.add(LOOP_BUTTON);
        	buttonPanel.add(Box.createHorizontalStrut(5));
        }
        
        buttonPanel.add(VOLUME);
        buttonPanel.add(Box.createHorizontalStrut(13));
        buttonPanel.add(PREV_BUTTON);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(PLAY_BUTTON);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(PAUSE_BUTTON);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(STOP_BUTTON);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(NEXT_BUTTON);
        buttonPanel.add(Box.createHorizontalStrut(18));
        
        //set font for time labels.
        Font f = new Font(progressCurrentTime.getFont().getFontName(),Font.PLAIN, 10);
        progressCurrentTime.setFont(f);
        progressSongLength.setFont(f);        
        
        buttonPanel.add(progressCurrentTime);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(PROGRESS);
        buttonPanel.add(Box.createHorizontalStrut(5));        
        buttonPanel.add(progressSongLength);
        
        if (OSUtils.isMacOSX())
            buttonPanel.add(Box.createHorizontalStrut(16));
        buttonPanel.add(Box.createHorizontalGlue());

        return buttonPanel;
    }

	public void initPlaylistPlaybackModeControls() {
		SHUFFLE_BUTTON = new JToggleButton();
		SHUFFLE_BUTTON.setBorderPainted(false);
		SHUFFLE_BUTTON.setContentAreaFilled(false);
		SHUFFLE_BUTTON.setBackground(null);
		SHUFFLE_BUTTON.setIcon(GUIMediator.getThemeImage("shuffle_off"));
		SHUFFLE_BUTTON.setSelectedIcon(GUIMediator.getThemeImage("shuffle_on"));
		SHUFFLE_BUTTON.setToolTipText(I18n.tr("Shuffle songs"));
		SHUFFLE_BUTTON.setSelected(PLAYER.isShuffle());

		LOOP_BUTTON = new JToggleButton();
		LOOP_BUTTON.setBorderPainted(false);
		LOOP_BUTTON.setContentAreaFilled(false);
		LOOP_BUTTON.setBackground(null);
		LOOP_BUTTON.setIcon(GUIMediator.getThemeImage("loop_off"));
		LOOP_BUTTON.setSelectedIcon(GUIMediator.getThemeImage("loop_on"));
		LOOP_BUTTON.setToolTipText(I18n.tr("Repeat songs"));
		LOOP_BUTTON.setSelected(PLAYER.getRepeatMode()==RepeatMode.All);
		
		SHUFFLE_BUTTON.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				PLAYER.setShuffle(SHUFFLE_BUTTON.isSelected());
			}
		});
		
		LOOP_BUTTON.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PLAYER.setRepeatMode((LOOP_BUTTON.isSelected()) ? RepeatMode.All : RepeatMode.None);
			}
		});
		
	}

    public void registerListeners() {
        PLAY_BUTTON.addActionListener(new PlayListener());
        PAUSE_BUTTON.addActionListener(new PauseListener());
        STOP_BUTTON.addActionListener(new StopListener());
        NEXT_BUTTON.addActionListener(new NextListener());
        PREV_BUTTON.addActionListener(new BackListener());
        VOLUME.addChangeListener(new VolumeSliderListener());
        PROGRESS.addMouseListener(new ProgressBarMouseAdapter());

    }

    public void unregisterListeners() {
        PLAY_BUTTON.removeActionListener(new PlayListener());
        PAUSE_BUTTON.removeActionListener(new PauseListener());
        STOP_BUTTON.removeActionListener(new StopListener());
        NEXT_BUTTON.removeActionListener(new NextListener());
        PREV_BUTTON.removeActionListener(new BackListener());
        VOLUME.removeChangeListener(new VolumeSliderListener());
        PROGRESS.removeMouseListener(new ProgressBarMouseAdapter());
    }

    /**
     * Updates the audio player.
     */
    public void refresh() {
        PLAYER.refresh();
    }

    public void updateTheme() {
        PLAY_BUTTON.updateTheme();
        PAUSE_BUTTON.updateTheme();
        STOP_BUTTON.updateTheme();
        NEXT_BUTTON.updateTheme();
        PREV_BUTTON.updateTheme();
        VOLUME.updateTheme();
    }

    /**
     * Updates the current progress of the progress bar, on the Swing thread.
     */
    private void setProgressValue(final int update) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                PROGRESS.setValue(update);
            }
        });
    }

    /**
     * Enables or disables the skipping action on the progress bar
     * safely from the swing event queue
     * 
     * @param enabled - true to allow skipping, false otherwise
     */
    private void setProgressEnabled(final boolean enabled) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                PROGRESS.setEnabled(enabled);
            }
        });
    }

    /**
     * Updates the volume based on the position of the volume slider
     */
    private void setVolumeValue() {
        VOLUME.repaint();
        PLAYER.setVolume(((float) VOLUME.getValue()) / VOLUME.getMaximum());
    }

    /**
     * Public accessor for loading a song to be played,
     * @playOnce - if true, play song one time regardless of continous
     *			and random values and stop the player after completing, 
     *			if false, observe the continous and	random control 
     */
    public void loadSong(AudioSource item, boolean playOnce) {
        // fail silently if there's nothing to play
        if (item == null)
            return;
        currentPlayListItem = item;
        playOneTime = playOnce;

        loadSong(currentPlayListItem, "");
    }

    /**
     * Loads an audiosource to be played. 
     */
    private void loadSong(final AudioSource audioSource, String displayName) {
        if (audioSource == null) {
            return;
        }

        // load song on Executor thread
        SONG_QUEUE.execute(new SongLoader(audioSource));
    }

    /**
     * Begins playing the loaded song
     */
    public void play() {
        if (PLAYER.getState() == MediaPlaybackState.Paused || PLAYER.getState() == MediaPlaybackState.Playing) {
            PLAYER.togglePause();
        } else {
            loadSong(currentPlayListItem, playOneTime);
        }
    }

    /**
     * Pauses the currently playing audio file.
     */
    public void pauseSong() {
        PLAYER.togglePause();
    }

    /**
     * Stops the currently playing audio file.
     */
    public void stopSong() {
        PLAYER.stop();
    }

    public void seek(float percent) {
        if (PLAYER.canSeek()) {
            float timeInSecs = PLAYER.getDurationInSecs() * percent;
            PLAYER.seek(timeInSecs);
        }
    }

    /**
     * @return the current song that is playing, null if there is no song loaded
     *         or the song is streaming audio
     */
    public AudioSource getCurrentSong() {
        return currentPlayListItem;
    }

    /**
     * This event is thrown everytime a new song is opened and is ready to be
     * played.
     */
    public void songOpened(AudioPlayer audioPlayer, AudioSource audioSource) {
        currentPlayListItem = audioSource;

        setVolumeValue();
        if (PLAYER.canSeek()) {
            setProgressEnabled(true);
            progressSongLength.setText(LibraryUtils.getSecondsInDDHHMMSS((int) PLAYER.getDurationInSecs()));
        } else {
            setProgressEnabled(false);
        }
    }

    /**
     * This event is thrown a number of times a second. It updates the current
     * frames that have been read, along with position and bytes read
     */
    public void progressChange(AudioPlayer audioPlayer, float currentTimeInSecs) {
        _progress = currentTimeInSecs;
        progressCurrentTime.setText(LibraryUtils.getSecondsInDDHHMMSS((int) _progress));

        if (PLAYER.canSeek()) {
            float progressUpdate = ((PROGRESS.getMaximum() * currentTimeInSecs) / PLAYER.getDurationInSecs());
            setProgressValue((int) progressUpdate);
        }
    }

    public void stateChange(AudioPlayer audioPlayer, MediaPlaybackState state) {
        if (state == MediaPlaybackState.Opening) {
            setVolumeValue();
        } else if (state == MediaPlaybackState.Stopped) {
            setProgressValue(PROGRESS.getMinimum());
        }
    }

    /**
     * Begins playing the loaded song in url of args.
     */
    String playSong(Map<String, String> args) {

        //        Tagged<String> urlString = FrostWireUtils.getArg(args, "url", "AddToPlaylist");
        //        if (!urlString.isValid())
        //            return urlString.getValue();
        //        String url = urlString.getValue();
        //
        //        // Find the song with this url
        //        PlaylistMediator pl = GUIMediator.getPlayList();
        //        List<PlayListItem> songs = pl.getSongs();
        //        PlayListItem targetTrack = null;
        //        for (PlayListItem it : songs) {
        //            try {
        //                String thatOne = URLDecoder.decode(it.getURI().toString());
        //                String thisOne = URLDecoder.decode(url);
        //                if (thatOne.equals(thisOne)) {
        //                    targetTrack = it;
        //                    break;
        //                }
        //            } catch (IOException e) {
        //                // ignore
        //            }
        //        }
        //
        //        if (targetTrack != null) {
        //            loadSong(targetTrack);
        //            return "ok";
        //        }
        //
        //        if (PLAYER.getStatus() == MediaPlaybackState.Paused || PLAYER.getStatus() == MediaPlaybackState.Playing)
        //            PLAYER.unpause();
        //        else {
        //            loadSong(currentPlayListItem);
        //        }

        return "ok";
    }

    /**
     * Returns "ok" on success and a
     * failure message on failure after taking an index into the playlist and
     * remove it.
     * 
     * @param index index of the item to remove
     * @return "ok" on success and a
     *         failure message on failure after taking an index into the
     *         playlist and remove it;
     */
    String removeFromPlaylist(int index) {
//        PlaylistMediator pl = GUIMediator.getPlayList();
//        if (pl.removeFileFromPlaylist(index)) {
//            return "ok";
//        }
        return "invalid.index: " + index;
    }

    /**
     * Returns "ok" on success and a
     * failure message on failure after taking an index into the playlist and
     * remove it.
     * 
     * @param index index of the item to remove
     * @return "ok" on success and a
     *         failure message on failure after taking an index into the
     *         playlist and remove it;
     */
    String playIndexInPlaylist(int index) {
//        PlaylistMediator pl = GUIMediator.getPlayList();
//        if (pl.removeFileFromPlaylist(index)) {
//            return "ok";
//        }
        return "invalid.index: " + index;
    }

    /**
     * @return <code>PROGRESS.getValue() + "\t" + PROGRESS.getMaximum()</code>
     *         or <code>"stopped"</code> if we're not playing
     */
    String getProgress() {
        String res;
        if (isPlaying()) {
            int secs = PROGRESS.getValue();
            int length = PROGRESS.getMaximum();
            System.out.println(secs + ":" + length);
            res = secs + "\t" + length;
        } else {
            res = "stopped";
        }
        return res.toString();
    }

    String addToPlaylist(Map<String, String> args) {

        Tagged<String> urlString = FrostWireUtils.getArg(args, "url", "AddToPlaylist");
        if (!urlString.isValid())
            return urlString.getValue();

        Tagged<String> nameString = FrostWireUtils.getArg(args, "name", "AddtoPlaylist");
        if (!nameString.isValid())
            return nameString.getValue();

        Tagged<String> lengthString = FrostWireUtils.getArg(args, "length", "AddtoPlaylist");
        if (!lengthString.isValid())
            return lengthString.getValue();

        Tagged<String> artistString = FrostWireUtils.getArg(args, "artist", "AddtoPlaylist");
        if (!artistString.isValid())
            return artistString.getValue();

        Tagged<String> albumString = FrostWireUtils.getArg(args, "album", "AddtoPlaylist");
        if (!albumString.isValid())
            return albumString.getValue();

        // We won't accept full URLs
//        String baseDir = "http://riaa.com";
//        int port = 0;
//        if (port > 0) {
//            baseDir += ":" + port;
//        }

        //        String url = baseDir + urlString.getValue();
        //        try {
        //            String decodedURL = URLDecoder.decode(url);
        //            URL u = new URL(decodedURL);
        //            PlayListItem song = new PlayListItem(u.toURI(), new AudioSource(u), nameString.getValue(), false);
        //            GUIMediator.instance().launchAudio(song);
        //        } catch (IOException e) {
        //            ErrorService.error(e, "invalid URL:" + url);
        //            return "ERROR:invalid.url:" + url;
        //        } catch (URISyntaxException e) {
        //            ErrorService.error(e, "invalid URL:" + url);
        //            return "ERROR:invalid.url:" + url;
        //        }
        return "ok";
    }

    String playURL(Map<String, String> args) {

        Tagged<String> urlString = FrostWireUtils.getArg(args, "url", "PlayURL");
        if (!urlString.isValid())
            return urlString.getValue();

        // We won't accept full URLs
//        String baseDir = "http://riaa.com";
//        int port = 0;
//        if (port > 0) {
//            baseDir += ":" + port;
//        }

        //        String url = baseDir + urlString.getValue();
        //        String name = getName(url);
        //        try {
        //            String decodedURL = URLDecoder.decode(url);
        //            URL u = new URL(decodedURL);
        //            PlayListItem song = new PlayListItem(u.toURI(), new AudioSource(u), name, false);
        //            GUIMediator.instance().launchAudio(song);
        //        } catch (IOException e) {
        //            ErrorService.error(e, "invalid URL:" + url);
        //            return "ERROR:invalid.url:" + url;
        //        } catch (URISyntaxException e) {
        //            ErrorService.error(e, "invalid URL:" + url);
        //            return "ERRORinvalid.url:" + url;
        //        }
        return "ok";
    }

//    private String getName(String url) {
//        int ilast = url.lastIndexOf('/');
//        if (ilast == -1) {
//            ilast = url.lastIndexOf('\\');
//        }
//        if (ilast == -1) {
//            return url;
//        }
//        return url.substring(ilast + 1);
//    }

    private boolean isPlaying() {
        return !(PLAYER.getState() == MediaPlaybackState.Stopped || PLAYER.getState() == MediaPlaybackState.Uninitialized
                || PLAYER.getState() == MediaPlaybackState.Paused || PLAYER.getState() == MediaPlaybackState.Failed);
    }

    /** Attempts to stop a song if its playing any song
     *
     * Returns true if it actually stopped, false if there was no need to do so.
     *
     * */
    public boolean attemptStop() {

        if (PLAYER.getState() != MediaPlaybackState.Stopped) {
            PLAYER.stop();
            return true;
        }

        return false;
    }

    /**
     * Disables the Volume Slider, And Pause Button
     */
    public void disableControls() {
        VOLUME.setEnabled(false);
        PAUSE_BUTTON.setEnabled(false);
    }

    /** 
     * Enables the Volume Slider, And Pause Button
     * 
     */
    public void enableControls() {
        VOLUME.setEnabled(true);
        PAUSE_BUTTON.setEnabled(true);
    }

    /**
     * Listens for the play button being pressed.
     */
    private class PlayListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            play();
        }
    }

    /**
     * Listens for the stopped button being pressed.
     */
    private class StopListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            stopSong();
        }
    }

    /**
     * Listens for the next button being pressed.
     */
    private class NextListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            next();
        }
    }

    private void next() {
    	AudioSource currentSong = PLAYER.getCurrentSong();
    	
    	if (currentSong != null) {
    		AudioSource nextSong = null;
    		
    		if (PLAYER.isShuffle()) {
    			nextSong = PLAYER.getNextRandomSong(currentSong);
    		} else {
    			nextSong = PLAYER.getNextSong(currentSong);
    		}
    		
    		if (nextSong != null) {
    			PLAYER.loadSong(nextSong,true,true);
    		}
    	}
    }

    /**
     * Listens for the back button being pressed.
     */
    private class BackListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            back();
        }
    }

    private void back() {
    	AudioSource currentSong = PLAYER.getCurrentSong();
    	
    	if (currentSong != null) {
    		AudioSource previousSong = PLAYER.getPreviousSong(currentSong);
    		
    		if (previousSong != null) {
    			PLAYER.loadSong(previousSong,true,true);
    		}
    	}
    }

    /**
     * Listens for the pause button being pressed.
     */
    private class PauseListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            pauseSong();
        }
    }

    /**
     * This listener is added to the progressbar to process when the user has
     * skipped to a new part of the song with a mouse
     */
    private class ProgressBarMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            seek(e.getX() * 1.0f / ((Component) e.getSource()).getWidth());
        }
    }

    /**
     * This listener is added to the volume slider to process whene the user has
     * adjusted the volume of the audio player
     */
    private class VolumeSliderListener implements ChangeListener {
        /**
         * If the user moved the thumb, adjust the volume of the player
         */
        public void stateChanged(ChangeEvent e) {
            setVolumeValue();
        }
    }

    /**
     * Ensures that all songs will be loaded/played from the same thread.
     */
    private class SongLoader implements Runnable {

        /**
         * Audio source to load
         */
        private final AudioSource audio;

        public SongLoader(AudioSource audio) {
            this.audio = audio;
        }

        public void run() {
            if (PLAYER == null) {
                System.err.println("SongLoader.run(): There's no PLAYER to load the Song to");
                return;
            }

            if (audio != null)
                PLAYER.loadSong(audio);

            if (PLAYER.getState() != MediaPlaybackState.Playing)
                PLAYER.stop();

            try {
                PLAYER.playSong();
            } catch (Exception e) {
                PLAYER.stop();
                //System.out.println("Could not play song " + audio.getURL().toString());
                e.printStackTrace();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    //e1.printStackTrace();
                }

                try {
                    synchronized (PLAYER) {
                        PLAYER.notifyAll();
                    }
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
        }
    }

	@Override
	public void volumeChange(AudioPlayer audioPlayer, double currentVolume) {
		VolumeSliderListener oldListener = (VolumeSliderListener) VOLUME.getChangeListeners()[0];
		VOLUME.removeChangeListener(oldListener);
		VOLUME.setValue((int) (VOLUME.getMaximum()*currentVolume));
		VOLUME.addChangeListener(oldListener);
	}
}