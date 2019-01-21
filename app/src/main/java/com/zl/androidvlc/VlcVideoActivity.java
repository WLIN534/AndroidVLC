//package com.zl.androidvlc;
//
///**
// * 项目名称：AndroidVLC
// * 类描述：
// * 创建人：zhanglin
// * 创建时间：2019/1/17 15:36
// * 修改人：Administrator
// * 修改时间：2019/1/17 15:36
// * 修改备注：
// */
//import android.app.Activity;
//import android.content.Context;
//import android.content.IntentFilter;
//import android.content.res.Configuration;
//import android.graphics.PixelFormat;
//import android.media.AudioManager;
//import android.os.Bundle;
//import android.os.Environment;
//import android.os.Handler;
//import android.os.Message;
//import android.provider.Settings;
//import android.telephony.PhoneStateListener;
//import android.telephony.TelephonyManager;
//import android.util.Log;
//import android.view.GestureDetector;
//import android.view.KeyEvent;
//import android.view.MotionEvent;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.ImageButton;
//import android.widget.SeekBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//
//import org.videolan.libvlc.LibVLC;
//import org.videolan.libvlc.Media;
//
//import java.util.Timer;
//import java.util.TimerTask;
//
//public class VlcVideoActivity extends Activity implements SurfaceHolder.Callback, IVideoPlayer,View.OnTouchListener {
//
//    private final static String TAG = "[VlcVideoActivity]";
//
//    private SurfaceView mSurfaceView;
//    private LibVLC mMediaPlayer;
//    private SurfaceHolder mSurfaceHolder;
//    private VideoInfo mVideoInfo;
//
//    private View mLoadingView;
//
//    private int mVideoHeight;
//    private int mVideoWidth;
//    private int mVideoVisibleHeight;
//    private int mVideoVisibleWidth;
//    private int mSarNum;
//    private int mSarDen;
//
//    private long videoLength = 0L;
//    private int videoDuration = 0;
//
//    // Video controller
//    private View mVideoControllerLayout;
//    private View mVideoControllerRootView;
//    private ImageButton mVideoControllerPlayOrPause;
//    private SeekBar mVideoControllerVideoSeekBar;
//    private TextView mVideoControllerCurrentTime;
//    private TextView mVideoControllerTotalTime;
//    private boolean mIsPaused;
//    private boolean isPlaying;
//    private boolean mIsVideoControllerShowing = false;
//    private boolean firstVideoControllerShowing = true;
//    private boolean mIsTouchingVideoSeekBar;
//    private Timer mHideVideoControllerTimer;
//    private Handler handlerSeekbar;
//    private Runnable runnableSeekbar;
//
//    private AudioManager audioManager;
//    private int currentVolume;
//    private int maxVolume;
//    private View volumeCtrl;
//    private Verticalseekbar volumebar;
//    private VolumeReceiver volumeReceiver;
//    private boolean mIsVolumeBarShowing;
//    private Timer mHideVolumeBarTimer;
//    //定义手势检测器实例
//    private GestureDetector gestureDetector;
//    private boolean mIsBrightnessCtrlShowing;
//    private Timer mHideBrightnessBarTimer;
//    private  View  brightnessCtrl;
//    private Verticalseekbar brightnessBar;
//    private TelephonyManager telephony;
//    private  boolean lostFocus;
//
//    private HomeListener mHomeWatcher;
//    private ScreenObserver screenListener;
//    private static Boolean isLockScreen = false;
//    private static Boolean homeExit = false;
//
//    private float startY = 0;//手指按下时的Y坐标
//    private float startX = 0;//手指按下时的X坐标
//
//    // Handler message
//    private final int HANDER_VIDEO_CONTROLLER = 0;
//    private final int MSG_HIDE_VOLUME = 13;
//    private final int MSG_HIDE_BRIGHNTNESS = 12;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mVideoInfo = (VideoInfo) getIntent().getExtras().getSerializable(VideoInfo.class.getSimpleName());
//
//        if(mVideoInfo == null) {
//            Toast.makeText(this, "No video!", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        setContentView(R.layout.activity_video_portrait);
//
//        mSurfaceView = (SurfaceView) findViewById(R.id.video_portrait);
//        mLoadingView = findViewById(R.id.video_loading_portrait);
//
//        try {
//            mMediaPlayer = VLCInstance.getLibVlcInstance();
//        } catch (LibVlcException e) {
//            e.printStackTrace();
//        }
//
//        mSurfaceHolder = mSurfaceView.getHolder();
//        mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
//        mSurfaceHolder.addCallback(this);
//
//        mVideoControllerLayout =  (View) findViewById(R.id.video_controller_portrait_layout);
//        mVideoControllerRootView = (View) findViewById(R.id.video_controller_portrait);
//        mVideoControllerPlayOrPause = (ImageButton) mVideoControllerRootView.findViewById(R.id.video_controller_play_pause);
//        mVideoControllerVideoSeekBar = (SeekBar) mVideoControllerRootView.findViewById(R.id.video_controller_seek_bar);
//
//        mMediaPlayer.eventVideoPlayerActivityCreated(true);
//
//        EventHandler em = EventHandler.getInstance();
//        em.addHandler(mVlcHandler);
//
//        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
//        mSurfaceView.setKeepScreenOn(true);
//
//        //网络本地视屏皆可
//        String  videoPath = mVideoInfo.getPath();
//        videoPath = LibVLC.PathToURI(videoPath);
//        Media media = new Media(mMediaPlayer, videoPath);
//        mMediaPlayer.setMediaList();
//        mMediaPlayer.getMediaList().add(media, false);
//
//        initPortraitVideoController();
//
//        try{
//            mMediaPlayer.playIndex(0);
//        }catch (Exception e){
//            Toast.makeText(this,"播放信息无效，请检查...",Toast.LENGTH_SHORT).show();
//        }
//
//        mVideoControllerRootView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (!mIsVideoControllerShowing){
//                    hideVideoController();
//                }
//            }
//        },1000);
//
//        //音量控制初始化
//        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//
//        //Home键点击监听事件
//        mHomeWatcher = new HomeListener(this);
//        mHomeWatcher.setOnHomePressedListener(mOnHomePressedListener);
//        mHomeWatcher.startWatch();
//
//        screenListener = new ScreenObserver(this);
//        screenListener.requestScreenStateUpdate(mScreenListener);
//
//    }
//
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        if(hasFocus){
//            if(null == telephony){
//                //监听来电
//                telephony = (TelephonyManager)getSystemService(
//                        Context.TELEPHONY_SERVICE);
//                telephony.listen(new OnePhoneStateListener(),
//                        PhoneStateListener.LISTEN_CALL_STATE);
//            }
//        }
//
//        super.onWindowFocusChanged(hasFocus);
//    }
//
//    //初始化竖屏播放控制组件
//    private void initPortraitVideoController(){
//        mVideoControllerLayout.setOnClickListener(mVideoControllerOnClickListener);
//        mVideoControllerPlayOrPause.setOnClickListener(mOnPlayOrPauseClickListener);
//        initVideoDuration();
//
//        mVideoControllerVideoSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
//
//        if(mIsVideoControllerShowing){
//            startHideVideoControllerTimer();
//        }
//    }
//
//    //初始化竖屏播放控制组件
//    private void initLandscapeVideoController(){
//        mVideoControllerLayout.setOnClickListener(mVideoControllerOnClickListener);
//        mVideoControllerPlayOrPause.setOnClickListener(mOnPlayOrPauseClickListener);
//        initVideoDuration();
//
//        mVideoControllerVideoSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
//
//        if(mIsVideoControllerShowing){
//            startHideVideoControllerTimer();
//        }
//
//        volumebar.setOnSeekBarChangeListener(volumeOnSeekBarChangeListener);
////        hideVolumeBar(); //隐藏VolumeBar
////        hideBrightnessBar();//隐藏BrightnessBar
////      brightnessBar.setOnSeekBarChangeListener(brightnessOnSeekBarChangeListener);
//
//        //创建手势检测器
//        gestureDetector = new GestureDetector(this,onGestureListener);
//        mVideoControllerLayout.setOnTouchListener(this);
//
//        if(null != brightnessBar){
//            brightnessBar.setMax(255);
//            int currentBrightness = getSystemBrightness();
//            brightnessBar.setProgress(currentBrightness);
//            //设置初始的屏幕亮度与系统一致
//            changeAppBrightness(this,currentBrightness);
//            brightnessBar.setOnSeekBarChangeListener(brightnessOnSeekBarChangeListener);
//
//        }
//    }
//
//    private boolean canSeekForward(){
//        return mMediaPlayer.isSeekable();
//    }
//
//    private boolean canSeekBackward(){
//        return mMediaPlayer.isSeekable();
//    }
//
//    private void initVideoDuration() {
//        // SEEKBAR
//        handlerSeekbar = new Handler();
//        runnableSeekbar = new Runnable() {
//            @Override
//            public void run() {
//                if (mMediaPlayer != null) {
//                    long curTime = mMediaPlayer.getTime();
//                    long totalTime = (long) (curTime / mMediaPlayer.getPosition());
//                    int minutes = (int) (curTime / (60 * 1000));
//                    int seconds = (int) ((curTime / 1000) % 60);
//                    int endMinutes = (int) (totalTime / (60 * 1000));
//                    int endSeconds = (int) ((totalTime / 1000) % 60);
//                    String duration = String.format("%02d:%02d / %02d:%02d", minutes, seconds, endMinutes, endSeconds);
//                    String durationS = String.format("%02d:%02d", endMinutes, endSeconds);
//                    String currentS = String.format("%02d:%02d", minutes, seconds);
//                    mVideoControllerVideoSeekBar.setProgress((int) (mMediaPlayer.getPosition() * 100));
//
//                    videoDuration = (int) totalTime;
//                    if(null != mVideoControllerCurrentTime){
//                        mVideoControllerCurrentTime.setText(currentS);
//                    }
//                    if(null != mVideoControllerTotalTime){
//                        mVideoControllerTotalTime.setText(durationS);
//                    }
//
////                  if(curTime >= 50 && curTime<1050){
////                      if(firstVideoControllerShowing){
////                      }
////                  }
//
////                  Toast.makeText(VlcVideoActivity.this, "视频长度：" + videoDuration,Toast.LENGTH_SHORT).show();
//                }
//                handlerSeekbar.postDelayed(runnableSeekbar, 1000);
//            }
//        };
//
//        runnableSeekbar.run();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//
//        if (mMediaPlayer != null) {
//            mMediaPlayer.stop();
//            mSurfaceView.setKeepScreenOn(false);
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        if(homeExit){
//            resume();
//            homeExit = false;
//        }else if(isLockScreen){
//            onCreate(null);
//            isLockScreen = false;
//        }
//
//        if(null != mVideoControllerPlayOrPause){
//            mVideoControllerPlayOrPause.setImageResource(R.drawable.video_controller_pause);
//        }
//        mIsPaused = false;
//
//        super.onResume();
//    }
//
//    public void resume() {
//        if (mMediaPlayer != null) {
//            mMediaPlayer.play();
//            mSurfaceView.setKeepScreenOn(true);
//        }
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        if (mMediaPlayer != null) {
//            mMediaPlayer.eventVideoPlayerActivityCreated(false);
//
//            EventHandler em = EventHandler.getInstance();
//            em.removeHandler(mVlcHandler);
//
//            mMediaPlayer.stop();
//            mSurfaceView.setKeepScreenOn(false);
//        }
//
//        super.onDestroy();
//    }
//
//    private View.OnClickListener mVideoControllerOnClickListener = new View.OnClickListener(){
//        @Override
//        public void onClick(View v) {
//            if(mIsVideoControllerShowing) {
//                hideVideoController();
//            } else {
//                showVideoController();
//            }
//        }
//    };
//
//    private  View.OnClickListener mOnPlayOrPauseClickListener = new View.OnClickListener(){
//        @Override
//        public void onClick(View v) {
//            if(!mIsPaused){
//                if (mMediaPlayer != null) {
//                    mMediaPlayer.pause();
//                    mSurfaceView.setKeepScreenOn(false);
//                }
//
//                mVideoControllerPlayOrPause.setImageResource(R.drawable.video_controller_play);
//            }else{
//                resume();
//
//                mVideoControllerPlayOrPause.setImageResource(R.drawable.video_controller_pause);
//            }
//            mIsPaused = !mIsPaused;
//        }
//    };
//
//    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
//        @Override
//        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            if(fromUser){
//
//            }
//        }
//
//        @Override
//        public void onStartTrackingTouch(SeekBar seekBar) {
//            mIsTouchingVideoSeekBar = true;
//
//            if(mHideVideoControllerTimer != null) {
//                mHideVideoControllerTimer.cancel();
//            }
//        }
//
//        @Override
//        public void onStopTrackingTouch(SeekBar seekBar) {
//            mIsTouchingVideoSeekBar = false;
//            seekTo(seekBar.getProgress() * videoDuration / 100);
//            //当滑动进度条时缓冲进度会变为播放处的值或者归零
////          buffering = 0;
//            startHideVideoControllerTimer();
//        }
//    };
//
//    public void seekTo(int milliSeconds) {
//        int mTotalTime = (int) videoDuration;
//        if(mMediaPlayer.isSeekable()) {
//            if(milliSeconds > mTotalTime) {
//                milliSeconds = mTotalTime;
//            }
//
//            mMediaPlayer.setTime(milliSeconds);
//        }
//    }
//
//    private  void startupInPortraitLayout(){
//        setContentView(R.layout.activity_video_portrait);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
//
//        mSurfaceView = (SurfaceView) findViewById(R.id.video_portrait);
//        mLoadingView = findViewById(R.id.video_loading_portrait);
//
//        mVideoControllerLayout =  findViewById(R.id.video_controller_portrait_layout);
//        mVideoControllerRootView = findViewById(R.id.video_controller_portrait);
//        mVideoControllerPlayOrPause = (ImageButton) mVideoControllerRootView.findViewById(R.id.video_controller_play_pause);
//        mVideoControllerVideoSeekBar = (SeekBar) mVideoControllerRootView.findViewById(R.id.video_controller_seek_bar);
//
//        mSurfaceHolder = mSurfaceView.getHolder();
//        mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
//        mSurfaceHolder.addCallback(this);
//
//        mLoadingView.setVisibility(View.INVISIBLE);
//        mSurfaceView.setKeepScreenOn(true);
//
//        //如果当前视频播放为暂停状态，则暂停图标不变
//        if(mIsPaused){
//            mVideoControllerPlayOrPause.setImageResource(R.drawable.video_controller_play);
//        }
//
//        initPortraitVideoController();
//
//        if(!mIsVideoControllerShowing){
//            mVideoControllerRootView.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (!mIsVideoControllerShowing){
//                        hideVideoController();
//                    }
//                }
//            },200);
//        }
//    }
//
//    private void  startupInLandscapeLayout(){
//        setContentView(R.layout.activity_video_vlc);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
//
//        mSurfaceView = (SurfaceView) findViewById(R.id.video_view);
//        mLoadingView = findViewById(R.id.video_loading);
//
//        mVideoControllerLayout =  findViewById(R.id.video_controller_layout);
//        mVideoControllerRootView = findViewById(R.id.video_controller);
//        mVideoControllerPlayOrPause = (ImageButton) mVideoControllerRootView.findViewById(R.id.video_landscape_play_pause);
//        mVideoControllerVideoSeekBar = (SeekBar) mVideoControllerRootView.findViewById(R.id.video_landscape_seek_bar);
////      video_controller_current_time
//        mVideoControllerCurrentTime = (TextView) findViewById(R.id.video_controller_current_time);
//        mVideoControllerTotalTime = (TextView) findViewById(R.id.video_controller_total_time);
//
//        //获取音量bar
//        volumeCtrl = findViewById(R.id.volume_ctrl);
//        volumebar = (Verticalseekbar) findViewById(R.id.volume_bar);
//        //获取亮度调节组件
//        brightnessCtrl = findViewById(R.id.brightness_ctrl);
//        brightnessBar = (Verticalseekbar) brightnessCtrl.findViewById(R.id.brightness_bar);
//
//        //音量，亮度滑动调节
//        volumebar.setMax(maxVolume);
//        volumebar.setProgress(currentVolume);
////        Toast.makeText(this, currentVolume*100/maxVolume + " %", Toast.LENGTH_SHORT).show();
//        //注册按键音量接受
//        volumeReceiver = new VolumeReceiver(volumebar);
//        registerReceiver(volumeReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION")) ;
//
//        mSurfaceHolder = mSurfaceView.getHolder();
//        mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
//        mSurfaceHolder.addCallback(this);
//
//        mLoadingView.setVisibility(View.INVISIBLE);
//        mSurfaceView.setKeepScreenOn(true);
//
//        //如果当前视频播放为暂停状态，则暂停图标不变
//        if(mIsPaused){
//            mVideoControllerPlayOrPause.setImageResource(R.drawable.video_controller_play);
//        }
//        if(!mIsVideoControllerShowing){
//            hideVideoController();
//        }
//
//        initLandscapeVideoController();
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        setSurfaceSize(mVideoWidth, mVideoHeight, mVideoVisibleWidth, mVideoVisibleHeight, mSarNum, mSarDen);
//        Configuration cfg =getResources().getConfiguration();
//        if (cfg.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            startupInLandscapeLayout();
//        }else if(cfg.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            startupInPortraitLayout();
//        }
//
//        super.onConfigurationChanged(newConfig);
//    }
//
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        if (mMediaPlayer != null) {
//            mSurfaceHolder = holder;
//            mMediaPlayer.attachSurface(holder.getSurface(), this);
//        }
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        mSurfaceHolder = holder;
//        if (mMediaPlayer != null) {
//            mMediaPlayer.attachSurface(holder.getSurface(), this);//, width, height
//        }
//        if (width > 0) {
//            mVideoHeight = height;
//            mVideoWidth = width;
//        }
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        if (mMediaPlayer != null) {
//            mMediaPlayer.detachSurface();
//        }
//    }
//
//    @Override
//    public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
//        mVideoHeight = height;
//        mVideoWidth = width;
//        mVideoVisibleHeight = visible_height;
//        mVideoVisibleWidth = visible_width;
//        mSarNum = sar_num;
//        mSarDen = sar_den;
//        mHandler.removeMessages(HANDLER_SURFACE_SIZE);
//        mHandler.sendEmptyMessage(HANDLER_SURFACE_SIZE);
//    }
//
//    private static final int HANDLER_BUFFER_START = 1;
//    private static final int HANDLER_BUFFER_END = 2;
//    private static final int HANDLER_SURFACE_SIZE = 3;
//
//    private static final int SURFACE_BEST_FIT = 0;
//    private static final int SURFACE_FIT_HORIZONTAL = 1;
//    private static final int SURFACE_FIT_VERTICAL = 2;
//    private static final int SURFACE_FILL = 3;
//    private static final int SURFACE_16_9 = 4;
//    private static final int SURFACE_4_3 = 5;
//    private static final int SURFACE_ORIGINAL = 6;
//    private int mCurrentSize = SURFACE_BEST_FIT;
//
//    private Handler mVlcHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            if (msg == null || msg.getData() == null)
//                return;
//
//            switch (msg.getData().getInt("event")) {
//                case EventHandler.MediaPlayerTimeChanged:
//                    break;
//                case EventHandler.MediaPlayerPositionChanged:
//                    break;
//                case EventHandler.MediaPlayerOpening:
//                    mHandler.removeMessages(HANDLER_BUFFER_START);
//                    mHandler.sendEmptyMessage(HANDLER_BUFFER_START);
//                    break;
//                case EventHandler.MediaPlayerPlaying:
//                    mHandler.removeMessages(HANDLER_BUFFER_END);
//                    mHandler.sendEmptyMessage(HANDLER_BUFFER_END);
//                    break;
//                case EventHandler.MediaPlayerBuffering:
//                    break;
//                case EventHandler.MediaPlayerLengthChanged:
//                    break;
//                case EventHandler.MediaPlayerEndReached:
//                    finish();
//                    //播放完成
//                    break;
//            }
//        }
//    };
//
//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case HANDLER_BUFFER_START:
//                    showLoading();
//                    hideVideoController();
//                    break;
//                case HANDLER_BUFFER_END:
//                    hideLoading();
//                    break;
//                case HANDLER_SURFACE_SIZE:
//                    changeSurfaceSize();
//                    break;
//                case HANDER_VIDEO_CONTROLLER:
//                    hideVideoController();
//                    break;
//                case MSG_HIDE_VOLUME:
//                    hideVolumeBar();
//                    break;
//                case MSG_HIDE_BRIGHNTNESS:
//                    hideBrightnessBar();
//            }
//        }
//    };
//
//    private void showLoading() {
//        mLoadingView.setVisibility(View.VISIBLE);
//    }
//
//    private void hideLoading() {
//        mLoadingView.setVisibility(View.GONE);
//    }
//
//    private void changeSurfaceSize() {
//        // get screen size
//        int dw = getWindowManager().getDefaultDisplay().getWidth();
//        int dh = getWindowManager().getDefaultDisplay().getHeight();
//
//        // calculate aspect ratio
//        double ar = (double) mVideoWidth / (double) mVideoHeight;
//        // calculate display aspect ratio
//        double dar = (double) dw / (double) dh;
//
//        switch (mCurrentSize) {
//            case SURFACE_BEST_FIT:
//                if (dar < ar)
//                    dh = (int) (dw / ar);
//                else
//                    dw = (int) (dh * ar);
//                break;
//            case SURFACE_FIT_HORIZONTAL:
//                dh = (int) (dw / ar);
//                break;
//            case SURFACE_FIT_VERTICAL:
//                dw = (int) (dh * ar);
//                break;
//            case SURFACE_FILL:
//                break;
//            case SURFACE_16_9:
//                ar = 16.0 / 9.0;
//                if (dar < ar)
//                    dh = (int) (dw / ar);
//                else
//                    dw = (int) (dh * ar);
//                break;
//            case SURFACE_4_3:
//                ar = 4.0 / 3.0;
//                if (dar < ar)
//                    dh = (int) (dw / ar);
//                else
//                    dw = (int) (dh * ar);
//                break;
//            case SURFACE_ORIGINAL:
//                dh = mVideoHeight;
//                dw = mVideoWidth;
//                break;
//        }
//
//        mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
//        ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
//        lp.width = dw;
//        lp.height = dh;
//        mSurfaceView.setLayoutParams(lp);
//        mSurfaceView.invalidate();
//    }
//
//    private void hideVideoController() {
//        mVideoControllerRootView.setVisibility(View.INVISIBLE);
//        mIsVideoControllerShowing = false;
//    }
//
//    private void showVideoController() {
//        mVideoControllerRootView.setVisibility(View.VISIBLE);
//        startHideVideoControllerTimer();
//        mIsVideoControllerShowing = true;
//    }
//
//    private void startHideVideoControllerTimer() {
//        if(mHideVideoControllerTimer != null) {
//            mHideVideoControllerTimer.cancel();
//        }
//
//        mHideVideoControllerTimer = new Timer();
//
//        TimerTask timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                mHandler.obtainMessage(HANDER_VIDEO_CONTROLLER).sendToTarget();
//            }
//        };
//
//        mHideVideoControllerTimer.schedule(timerTask, 3000);
//    }
//
//    private void showVolumeBar() {
//        volumeCtrl.setVisibility(View.VISIBLE);
//        startHideVolumeBarTimer();
//        mIsVolumeBarShowing = true;
//    }
//
//    private void hideVolumeBar() {
//        volumeCtrl.setVisibility(View.INVISIBLE);
//        mIsVolumeBarShowing = false;
//    }
//
//    private void startHideVolumeBarTimer() {
//        if(mHideVolumeBarTimer != null) {
//            mHideVolumeBarTimer.cancel();
//        }
//
//        mHideVolumeBarTimer = new Timer();
//
//        TimerTask timerTask3 = new TimerTask() {
//            @Override
//            public void run() {
//                mHandler.obtainMessage(MSG_HIDE_VOLUME).sendToTarget();
//            }
//        };
//
//        mHideVolumeBarTimer.schedule(timerTask3, 3000);
//    }
//
//    private void showBrightnessBar() {
//        brightnessCtrl.setVisibility(View.VISIBLE);
//        startHideBrightnessBarTimer();
//        mIsBrightnessCtrlShowing = true;
//    }
//
//    private void hideBrightnessBar() {
//        brightnessCtrl.setVisibility(View.INVISIBLE);
//        mIsBrightnessCtrlShowing = false;
//    }
//
//    private void startHideBrightnessBarTimer() {
//        if(mHideBrightnessBarTimer != null) {
//            mHideBrightnessBarTimer.cancel();
//        }
//
//        mHideBrightnessBarTimer = new Timer();
//
//        TimerTask timerTask2 = new TimerTask() {
//            @Override
//            public void run() {
//                mHandler.obtainMessage(MSG_HIDE_BRIGHNTNESS).sendToTarget();
//            }
//        };
//
//        mHideBrightnessBarTimer.schedule(timerTask2, 3000);
//    }
//
//    Verticalseekbar.OnSeekBarChangeListener volumeOnSeekBarChangeListener = new Verticalseekbar.OnSeekBarChangeListener(){
//        @Override
//        public void onProgressChanged(Verticalseekbar verticalseekbar, int progress, boolean fromUser) {
//        }
//
//        @Override
//        public void onStartTrackingTouch(Verticalseekbar verticalseekbar) {
//
//        }
//
//        @Override
//        public void onStopTrackingTouch(Verticalseekbar verticalseekbar) {
//            //滑动时调节多媒体音量
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, verticalseekbar.getProgress(), 0);
//        }
//    };
//
//    Verticalseekbar.OnSeekBarChangeListener brightnessOnSeekBarChangeListener = new Verticalseekbar.OnSeekBarChangeListener(){
//        @Override
//        public void onProgressChanged(Verticalseekbar verticalseekbar, int progress, boolean fromUser) {
//        }
//
//        @Override
//        public void onStartTrackingTouch(Verticalseekbar verticalseekbar) {
//        }
//
//        @Override
//        public void onStopTrackingTouch(Verticalseekbar verticalseekbar) {
//            int brightBress = verticalseekbar.getProgress();
//            changeAppBrightness(VlcVideoActivity.this, brightBress);
//        }
//    };
//
//    /**
//     * 设置声音
//     * @param volume
//     */
//    public void setVolume(int volume){
////        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
//        if(volume == 1){
//            if (currentVolume < maxVolume) {// 为避免调节过快，distanceY应大于一个设定值
//                currentVolume++;
//            }
//        }else if(volume == -1){
//            if (currentVolume > 0) {
//                currentVolume--;
//            }
//        }
//        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume,0);
//        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前音量值
//        //控制界面展示
////        hideVideoController();
////        showVideoController();
////        startHideVideoControllerTimer();
//        volumebar.setProgress(currentVolume);
//    }
//
//    /**
//     * 设置屏幕亮度
//     * 0 最暗
//     * 1 最亮
//     */
//    public void setBrightness(float brightness) {
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
//        if (lp.screenBrightness > 1) {
//            lp.screenBrightness = 1;
//        } else if (lp.screenBrightness < 0) {
//            lp.screenBrightness = (float) 0.0;
//        }
//        getWindow().setAttributes(lp);
//
//        int brightnessV =  Math.round(lp.screenBrightness * 255);
//        brightnessV = brightnessV > 255 ? 255 : brightnessV;
//        brightnessBar.setProgress(brightnessV);
////        float sb = lp.screenBrightness;
////        brightnessTextView.setText((int) Math.ceil(sb * 100) + "%");
//    }
//
//    // 根据亮度值修改当前window亮度
//    public void changeAppBrightness(Context context, int brightness) {
//        Window window = ((Activity) context).getWindow();
//        WindowManager.LayoutParams lp = window.getAttributes();
//        if (brightness == -1) {
//            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
//        } else {
//            lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 255f;
//        }
//        window.setAttributes(lp);
//    }
//
//    /**
//     * 获取系统亮度
//     * @return
//     */
//    private int getSystemBrightness() {
//        int systemBrightness = 0;
//        try {
//            systemBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
//            Toast.makeText(this,systemBrightness + "",Toast.LENGTH_SHORT);
//        }catch (Settings.SettingNotFoundException e){
//            e.printStackTrace();
//        }
//        return systemBrightness;
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_VOLUME_UP:// 增大音量
//                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND
//                                | AudioManager.FLAG_SHOW_UI);
//
//                break;
//            case KeyEvent.KEYCODE_VOLUME_DOWN:// 减小音量
//                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND
//                                | AudioManager.FLAG_SHOW_UI);
//
//                break;
//            case KeyEvent.KEYCODE_HEADSETHOOK:
//
//                break;
//            default:
//                return super.onKeyDown(keyCode, event);
//        }
//        // 为true,则其它后台按键处理再也无法处理到该按键，为false,则其它后台按键处理可以继续处理该按键事件
//        return true;
//    }
//
//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        int screenWidth = v.getWidth();
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                startX = event.getX();
//                startY = event.getY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                float endY = event.getY();
//                float distanceY = startY - endY;
//                float endX = event.getX();
//                float distanceX = startX - endX;
//                if (startX < screenWidth / 2) {
//                    //左边
//                    //在这里处理音量
//                    //屏幕左半部分上滑，亮度变大，下滑，亮度变小
//                    final double FLING_MIN_DISTANCE = 50;
//                    final double FLING_MIN_VELOCITY = 100;
//                    if (distanceY > FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
//                        setVolume(1);
//                        showVolumeBar();
////                        Toast.makeText(this, "左边上滑", Toast.LENGTH_LONG).show();
//                    }
//                    if (distanceY < FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
//                        setVolume(-1);
//                        showVolumeBar();
////                        Toast.makeText(this, "左边下滑", Toast.LENGTH_LONG).show();
//                    }
//                } else {
//                    //屏幕右半部分上滑，亮度变大，下滑，亮度变小
//                    final double FLING_MIN_DISTANCE = 100;
//                    final double FLING_MIN_VELOCITY = 200;
//                    if (distanceY > FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
//                        setBrightness(10);
//                        showBrightnessBar();
//                    }
//                    if (distanceY < FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
//                        setBrightness(-10);
//                        showBrightnessBar();
//                    }
//                }
//                break;
//        }
//
//        return gestureDetector.onTouchEvent(event);
//    }
//
//    private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener(){
//
//        @Override
//        public boolean onSingleTapUp(MotionEvent e) {
//            return false;
//        }
//
//        @Override
//        public void onLongPress(MotionEvent e) {
//
//        }
//
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
//                               float velocityY) {
//            return true;
//        }
//
//        @Override
//        public void onShowPress(MotionEvent e) {
//        }
//
//        @Override
//        public boolean onDown(MotionEvent e) {
//            return false;
//        }
//
//        @Override
//        public boolean onDoubleTap(MotionEvent e) {
//            mIsPaused = !mIsPaused;
//
//            if(mIsPaused) {
//                if (mMediaPlayer != null) {
//                    mMediaPlayer.pause();
//                    mSurfaceView.setKeepScreenOn(false);
//                }
//
//                mVideoControllerPlayOrPause.setImageResource(R.drawable.video_controller_play);
//                showVideoController();
//            } else {
//                resume();
//                mVideoControllerPlayOrPause.setImageResource(R.drawable.video_controller_pause);
//            }
//
//            return true;
//        }
//
//        @Override
//        public boolean onDoubleTapEvent(MotionEvent e) {
//            return false;
//        }
//        /**
//         * 这个方法不同于onSingleTapUp，他是在GestureDetector确信用户在第一次触摸屏幕后，没有紧跟着第二次触摸屏幕，也就是不是“双击”的时候触发
//         * */
//        @Override
//        public boolean onSingleTapConfirmed(MotionEvent e) {
//            return false;
//        }
//
//        @Override
//        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            return true;
//        }
//    };
//
//    HomeListener.OnHomePressedListener mOnHomePressedListener = new HomeListener.OnHomePressedListener() {
//        @Override
//        public void onHomePressed() {
//            mIsPaused = !mIsPaused;
//            if(mIsPaused) {
//                if (mMediaPlayer != null) {
//                    mMediaPlayer.pause();
//                }
//
//                mVideoControllerPlayOrPause.setImageResource(R.drawable.video_controller_play);
//            }
//            homeExit = true;
//        }
//
//        @Override
//        public void onHomeLongPressed() {
//
//        }
//    };
//
//    ScreenObserver.ScreenStateListener mScreenListener = new ScreenObserver.ScreenStateListener(){
//
//        @Override
//        public void onScreenOn() {
//
//        }
//
//        @Override
//        public void onScreenOff() {
//            mIsPaused = !mIsPaused;
//            if(mIsPaused) {
//                if (mMediaPlayer != null) {
//                    mMediaPlayer.pause();
//                }
//
//                mVideoControllerPlayOrPause.setImageResource(R.drawable.video_controller_play);
//            }
//            isLockScreen = true;
//        }
//
//        @Override
//        public void onUserPresent() {
//        }
//    };
//
//    /**
//     * 电话状态监听.
//     * @author stephen
//     */
//    class OnePhoneStateListener extends PhoneStateListener {
//        @Override
//        public void onCallStateChanged(int state, String incomingNumber) {
//            Log.i(TAG, "[Listener]电话号码:"+incomingNumber);
//            switch(state){
//                case TelephonyManager.CALL_STATE_RINGING:
//                    Log.i(TAG, "[Listener]等待接电话:"+incomingNumber);
//                    mIsPaused = !mIsPaused;
//                    if(mIsPaused) {
//                        mMediaPlayer.pause();
//                        mVideoControllerPlayOrPause.setImageResource(R.drawable.video_controller_play);
//                    }
//                    break;
//                case TelephonyManager.CALL_STATE_IDLE:
//                    Log.i(TAG, "[Listener]电话挂断:"+incomingNumber);
//                    onCreate(null);
//                    mVideoControllerPlayOrPause.setImageResource(R.drawable.video_controller_pause);
//                    mIsPaused = false;
//                    break;
//                case TelephonyManager.CALL_STATE_OFFHOOK:
//                    Toast.makeText(VlcVideoActivity.this,"[Listener]通话中:"+incomingNumber,Toast.LENGTH_SHORT).show();
//                    Log.i(TAG, "[Listener]通话中:"+incomingNumber);
//                    break;
//            }
//            super.onCallStateChanged(state, incomingNumber);
//        }
//    }
//
//}
//
