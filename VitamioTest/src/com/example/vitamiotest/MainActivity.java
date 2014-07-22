package com.example.vitamiotest;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.MediaController.MediaPlayerControl;
import io.vov.vitamio.widget.VideoView;
import io.vov.vitamio.widget.VideoView.DisplayOrientation;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

public class MainActivity extends Activity implements MediaPlayerControl {
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String VIDEO_PATH = "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4";

	private VideoView videoView;
	private ProgressBar progressBar;
	private MediaController controller;

	/** The current activities configuration used to test screen orientation. */
	private Configuration configuration;

	private boolean isVideoPaused = false;
	private long videoCurrentPosition = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
			return;
		setContentView(R.layout.activity_main);
		configuration = getResources().getConfiguration();

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);

		// Initialize Vitamio video view
		videoView = (VideoView) findViewById(R.id.videoView);
		videoView.setMediaBufferingIndicator(progressBar);
		videoView.registerOrientationListener();

		refreshVideoData();
		updateLayout();
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (videoView != null && videoView.isPlaying() && !isVideoPaused) {
			videoCurrentPosition = videoView.getCurrentPosition();
			videoView.suspend();
			isVideoPaused = true;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (videoView != null && isVideoPaused) {
			videoView.resume();
			videoView.seekTo(videoCurrentPosition);
			isVideoPaused = false;
			videoCurrentPosition = -1;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		updateLayout();
	}

	/**
	 * Refresh Video data on screen
	 */
	private void refreshVideoData() {
		if (VIDEO_PATH.length() > 0 && videoView != null) {
			// release player
			videoView.release(true);

			videoView.setVideoPath(VIDEO_PATH);
			videoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
			videoView.requestFocus();

			// Initializing the video player’s media controller
			controller = new MediaController(this);
			controller.setMediaPlayer(this);

			// Binding media controller with VideoView
			videoView.setMediaController(controller);

			videoView.start();
			progressBar.setVisibility(View.VISIBLE);

			// Registering a callback to be invoked when the media file is
			// loaded
			// and ready to go
			videoView.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mediaPlayer) {
					progressBar.setVisibility(View.INVISIBLE);
					videoView.start();
				}
			});
		}
	}

	/**
	 * Update layout views
	 */
	private void updateLayout() {
		Log.d(TAG, "updateLayout");
		WindowManager.LayoutParams attrs = getWindow().getAttributes();

		// Show the Video controller
		if (controller != null)
			controller.show();

		switch (configuration.orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			// Hide the status bar
			attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			getWindow().setAttributes(attrs);

			// Hide the whole view except the video
			findViewById(R.id.bottomView).setVisibility(View.GONE);
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			// Show the status bar
			attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
			getWindow().setAttributes(attrs);

			// Show the whole view with the video
			findViewById(R.id.bottomView).setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	@Override
	public void start() {
		if (videoView != null)
			videoView.start();
	}

	@Override
	public void pause() {
		if (videoView != null)
			videoView.pause();
	}

	@Override
	public long getDuration() {
		if (videoView != null)
			return videoView.getDuration();
		else
			return 0;
	}

	@Override
	public long getCurrentPosition() {
		if (videoView != null)
			return videoView.getCurrentPosition();
		else
			return 0;
	}

	@Override
	public void seekTo(long pos) {
		if (videoView != null)
			videoView.seekTo(pos);
	}

	@Override
	public boolean isPlaying() {
		if (videoView != null)
			return videoView.isPlaying();
		else
			return false;
	}

	@Override
	public int getBufferPercentage() {
		if (videoView != null)
			return videoView.getBufferPercentage();
		else
			return 0;
	}

	@Override
	public boolean isFullScreen() {
		if (videoView == null)
			return false;

		DisplayMetrics disp = getResources().getDisplayMetrics();
		int windowWidth = disp.widthPixels;
		int windowHeight = disp.heightPixels;

		return windowWidth >= windowHeight;
	}

	@Override
	public void toggleFullScreen() {
		if (videoView == null)
			return;

		if (!isFullScreen())
			videoView.setDisplayOrientation(DisplayOrientation.LANDSCAPE);
		else
			videoView.setDisplayOrientation(DisplayOrientation.PORTRAIT);
	}
}
