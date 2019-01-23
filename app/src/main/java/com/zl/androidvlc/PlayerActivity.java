package com.zl.androidvlc;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zl.androidvlc.utils.SystemUtil;
import com.zl.androidvlc.utils.WindowUtils;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.core.view.MotionEventCompat;
import butterknife.BindView;

import static com.zl.androidvlc.utils.StringUtils.generateTime;

/**
 * 项目名称：AndroidVLC
 * 类描述：
 * 创建人：zhanglin
 * 创建时间：2019/1/16 16:11
 * 修改人：Administrator
 * 修改时间：2019/1/16 16:11
 * 修改备注：
 */
public class

PlayerActivity extends BaseActivity implements IVLCVout.OnNewVideoLayoutListener, View.OnClickListener {
    @BindView(R.id.iv_play)
    ImageView ivPlay;
    @BindView(R.id.tv_cur_time)
    TextView tvCurrentTime;
    @BindView(R.id.player_seek)
    SeekBar seekBarTime;
    @BindView(R.id.tv_separator)
    TextView tvSeparator;
    @BindView(R.id.tv_end_time)
    TextView tvTotalTime;
    @BindView(R.id.iv_media_quality)
    TextView ivMediaQuality;
    @BindView(R.id.iv_fullscreen)
    ImageView ivFullscreen;
    @BindView(R.id.ll_bottom_bar)
    LinearLayout llBottomBar;
    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;
    @BindView(R.id.iv_first_image)
    ImageView ivFirsrImage;


    private static final String TAG = "PlayerActivity";
    private static final String SAMPLE_URL = "/storage/emulated/0/videos/1.flv";

    //        private static final String VIDEO_URL = "http://www.zzguifan.com:8666/system/filehandle.aspx?62999f1f12ba78a2-4a4252135d81a2b2&f=5710223";
//    private static final String VIDEO_URL = "http://flv2.bn.netease.com/videolib3/1505/29/DCNOo7461/SD/DCNOo7461-mobile.mp4";
    private static final String VIDEO_URL = "http://xunleib.zuida360.com/1812/表象之下.BD1280高清中英双字版.mp4";

    @BindView(R.id.video_surface_frame)
    FrameLayout mVideoSurfaceFrame;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_video_title)
    TextView tvVideoTitle;
    @BindView(R.id.title_bar)
    LinearLayout titleBar;
    @BindView(R.id.tv_volume)
    TextView mTvVolume;
    @BindView(R.id.tv_brightness)
    TextView mTvBrightness;
    @BindView(R.id.tv_fast_forward)
    TextView mTvFastForward;
    @BindView(R.id.tv_fast_rewind)
    TextView mTvFastRewind;
    @BindView(R.id.fl_touch_layout)
    FrameLayout mFlTouchLayout;
    @BindView(R.id.pb_loading)
    ProgressBar mLoadingView;

    private LibVLC mLibVLC;
    private MediaPlayer mMediaPlayer;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_SEEK:
                    final int pos = setProgress();
                    if (!mIsSeeking && mIsShowBar && mMediaPlayer.isPlaying()) {
                        // 这里会重复发送MSG，已达到实时更新 Seek 的效果
                        msg = obtainMessage(MSG_UPDATE_SEEK);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;

            }
        }
    };
    private View.OnLayoutChangeListener mOnLayoutChangeListener = null;
    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_SCREEN = 1;
    private static final int SURFACE_FILL = 2;
    private static final int SURFACE_16_9 = 3;
    private static final int SURFACE_4_3 = 4;
    private static final int SURFACE_ORIGINAL = 5;
    private static int CURRENT_SIZE = SURFACE_BEST_FIT;

    private int mVideoHeight = 0;
    private int mVideoWidth = 0;
    private int mVideoVisibleHeight = 0;
    private int mVideoVisibleWidth = 0;
    private int mVideoSarNum = 0;
    private int mVideoSarDen = 0;

    private long totalTime = 0;

    private Uri uri;
    // 是否正在拖拽进度条
    private boolean mIsSeeking;
    // 进度条最大值
    private static final int MAX_VIDEO_SEEK = 1000;
    // 目标进度
    private long mTargetPosition = INVALID_VALUE;
    // 当前进度
    private int mCurPosition = INVALID_VALUE;
    // 无效变量
    private static final int INVALID_VALUE = -1;
    // 默认隐藏控制栏时间
    private static final int DEFAULT_HIDE_TIMEOUT = 5000;
    // 更新进度消息
    private static final int MSG_UPDATE_SEEK = 10086;
    // 音量控制
    private AudioManager mAudioManager;
    // 手势控制
    private GestureDetector mGestureDetector;
    // 最大音量
    private int mMaxVolume;
    // 当前音量
    private int mCurVolume = INVALID_VALUE;
    // 当前亮度
    private float mCurBrightness = INVALID_VALUE;

    // 进来还未播放
    private boolean mIsNeverPlay = true;
    // 是否显示控制栏
    private boolean mIsShowBar = true;

    @Override
    public int setLayoutView() {
        return R.layout.activity_player;
    }


    @Override
    public void initView() {
        seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private long curPosition;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return;
                }
                long duration = totalTime;
                // 计算目标位置
                mTargetPosition = (duration * progress) / MAX_VIDEO_SEEK;
                String desc;
                // 对比当前位置来显示快进或后退
                if (mTargetPosition > curPosition) {
                    desc = generateTime(mTargetPosition) + "/" + generateTime(duration);
                } else {
                    desc = generateTime(mTargetPosition) + "/" + generateTime(duration);
                }
                setFastForward(desc);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsSeeking = true;
                _showControlBar(3600000);
                mHandler.removeMessages(MSG_UPDATE_SEEK);
                curPosition = mMediaPlayer.getTime();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                hideTouchView();
                mIsSeeking = false;
                // 视频跳转
                mMediaPlayer.setTime(mTargetPosition);
                mTargetPosition = INVALID_VALUE;
                setProgress();
                _showControlBar(DEFAULT_HIDE_TIMEOUT);
            }


        });
        // 声音
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 亮度
        try {
            int e = Settings.System.getInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            float progress = 1.0F * (float) e / 255.0F;
            WindowManager.LayoutParams layout = this.getWindow().getAttributes();
            layout.screenBrightness = progress;
            this.getWindow().setAttributes(layout);
        } catch (Settings.SettingNotFoundException var7) {
            var7.printStackTrace();
        }

        mGestureDetector = new GestureDetector(this, mPlayerGestureListener);

        mVideoSurfaceFrame.setOnTouchListener(mPlayerTouchListener);
        ivPlay.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        ivFullscreen.setOnClickListener(this);
        final ArrayList<String> options = new ArrayList<>();
        options.add("--file-caching=10000");//文件缓存
        options.add("--network-caching=10000");//网络缓存

        options.add("--live-caching=10000");//直播缓存

        mLibVLC = new LibVLC(this, options);
        mMediaPlayer = new MediaPlayer(mLibVLC);

    }

    @Override
    protected void onStart() {
        super.onStart();

        IVLCVout ivlcVout = mMediaPlayer.getVLCVout();
        ivlcVout.setVideoView(surfaceView);
        ivlcVout.attachViews(this);
        uri = Uri.parse(VIDEO_URL);
//        uri = Uri.parse(SAMPLE_URL);
//        takePicture();//获取第一帧的图片
        tvVideoTitle.setText(uri.getLastPathSegment());
        Media media;
        if (VIDEO_URL.substring(0, 4).equalsIgnoreCase("http")) {
            media = new Media(mLibVLC, uri);
        } else {
            media = new Media(mLibVLC, SAMPLE_URL);
        }
        mMediaPlayer.setMedia(media);
        mMediaPlayer.play();

        if (mIsNeverPlay) {
            mIsNeverPlay = false;
            mIsShowBar = false;
            mLoadingView.setVisibility(View.VISIBLE);
        }
        if (mOnLayoutChangeListener == null) {
            mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
                private final Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        updateVideoSurfaces();
                    }
                };

                @Override
                public void onLayoutChange(View v, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                        mHandler.removeCallbacks(mRunnable);
                        mHandler.post(mRunnable);
                    }
                }
            };
        }
        mVideoSurfaceFrame.addOnLayoutChangeListener(mOnLayoutChangeListener);

        mMediaPlayer.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                try {
                    if (event.getTimeChanged() == 0 || totalTime == 0 || event.getTimeChanged() > totalTime) {
                        return;
                    }
                    if (event.type == MediaPlayer.Event.Buffering) {
                        Log.e("缓冲进度：", event.getBuffering() + "");
                        seekBarTime.setSecondaryProgress((int) event.getBuffering());//缓冲进度
                    }

                    seekBarTime.setProgress((int) event.getTimeChanged());//播放进度
                    tvCurrentTime.setText(SystemUtil.getMediaTime((int) event.getTimeChanged()));

                    //播放结束
                    if (mMediaPlayer.getPlayerState() == Media.State.Ended) {
                        mLoadingView.setVisibility(View.GONE);
                        stop();
                    }
                } catch (Exception e) {
                    Log.d("vlc-event", e.toString());
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mOnLayoutChangeListener != null) {
            mVideoSurfaceFrame.removeOnLayoutChangeListener(mOnLayoutChangeListener);
            mOnLayoutChangeListener = null;
        }

        mMediaPlayer.stop();

        mMediaPlayer.getVLCVout().detachViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mLibVLC.release();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //拦截音量键
        if (handleVolumeKey(keyCode)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void changeMediaPlayerLayout(int displayW, int displayH) {
        //* Change the video placement using the MediaPlayer API *//*
        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                mMediaPlayer.setAspectRatio(null);
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_FIT_SCREEN:
            case SURFACE_FILL: {
                Media.VideoTrack vtrack = mMediaPlayer.getCurrentVideoTrack();
                if (vtrack == null)
                    return;
                final boolean videoSwapped = vtrack.orientation == Media.VideoTrack.Orientation.LeftBottom
                        || vtrack.orientation == Media.VideoTrack.Orientation.RightTop;
                if (CURRENT_SIZE == SURFACE_FIT_SCREEN) {
                    int videoW = vtrack.width;
                    int videoH = vtrack.height;

                    if (videoSwapped) {
                        int swap = videoW;
                        videoW = videoH;
                        videoH = swap;
                    }
                    if (vtrack.sarNum != vtrack.sarDen)
                        videoW = videoW * vtrack.sarNum / vtrack.sarDen;

                    float ar = videoW / (float) videoH;
                    float dar = displayW / (float) displayH;

                    float scale;
                    if (dar >= ar)
                        scale = displayW / (float) videoW; //* horizontal *//*
                    else
                        scale = displayH / (float) videoH; //* vertical *//*
                    mMediaPlayer.setScale(scale);
                    mMediaPlayer.setAspectRatio(null);
                } else {
                    mMediaPlayer.setScale(0);
                    mMediaPlayer.setAspectRatio(!videoSwapped ? "" + displayW + ":" + displayH
                            : "" + displayH + ":" + displayW);
                }
                break;
            }
            case SURFACE_16_9:
                mMediaPlayer.setAspectRatio("16:9");
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_4_3:
                mMediaPlayer.setAspectRatio("4:3");
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_ORIGINAL:
                mMediaPlayer.setAspectRatio(null);
                mMediaPlayer.setScale(1);
                break;
        }
    }

    private void updateVideoSurfaces() {

        int sw = getWindow().getDecorView().getWidth();
        int sh = getWindow().getDecorView().getHeight();

        // sanity check
        if (sw * sh == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }

        mMediaPlayer.getVLCVout().setWindowSize(sw, sh);

        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        if (mVideoWidth * mVideoHeight == 0) {
            //* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API *//*
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            surfaceView.setLayoutParams(lp);
            lp = mVideoSurfaceFrame.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoSurfaceFrame.setLayoutParams(lp);
            changeMediaPlayerLayout(sw, sh);
            return;
        }

        if (lp.width == lp.height && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            //* We handle the placement of the video using Android View LayoutParams *//*
            mMediaPlayer.setAspectRatio(null);
            mMediaPlayer.setScale(0);
        }

        double dw = sw, dh = sh;
        final boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mVideoSarDen == mVideoSarNum) {
            //* No indication about the density, assuming 1:1 *//*
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            //* Use the specified aspect ratio *//*
            vw = mVideoVisibleWidth * (double) mVideoSarNum / mVideoSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_SCREEN:
                if (dar >= ar)
                    dh = dw / ar; //* horizontal *//*
                else
                    dw = dh * ar; //* vertical *//*
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        // set display size
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        surfaceView.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = mVideoSurfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        mVideoSurfaceFrame.setLayoutParams(lp);

        surfaceView.invalidate();
    }


    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth,
                                 int visibleHeight, int sarNum, int sarDen) {
        totalTime = mMediaPlayer.getLength();
        seekBarTime.setMax((int) totalTime);
        tvTotalTime.setText(SystemUtil.getMediaTime((int) totalTime));

        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mVideoSarNum = sarNum;
        mVideoSarDen = sarDen;
        updateVideoSurfaces();
        setSize(mVideoWidth, mVideoHeight);

    }

    /**
     * 获取某个时间点的帧图片
     */
    public void takePicture() {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        // 设置数据源，有多种重载，这里用本地文件的绝对路径
        mmr.setDataSource(SAMPLE_URL, new HashMap<>());
        Bitmap frameBitmap = mmr.getFrameAtTime();
        mmr.release();
        ivFirsrImage.setImageBitmap(frameBitmap);
    }


    @Override
    public void onClick(View v) {
        _refreshHideRunnable();
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_play:
                if (mMediaPlayer.isPlaying()) {
                    pause();
                } else {
                    play();
                }
                break;
            case R.id.iv_fullscreen:
                if (WindowUtils.getScreenOrientation(this) == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                break;
        }
    }

    /**
     * 设置surfaceView显示宽高
     *
     * @param width  宽度
     * @param height 高度
     */
    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        if (surfaceView == null)
            return;

        // get screen size
        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        // force surface buffer size
//        holder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        lp.width = w;
        lp.height = h;
        surfaceView.setLayoutParams(lp);
        surfaceView.invalidate();
    }

    /**
     * 隐藏视图Runnable
     */
    private Runnable mHideTouchViewRunnable = new Runnable() {
        @Override
        public void run() {
            hideTouchView();
        }
    };

    /**
     * 隐藏触摸视图
     */
    private void hideTouchView() {
        if (mFlTouchLayout.getVisibility() == View.VISIBLE) {
            mTvFastForward.setVisibility(View.GONE);
            mTvVolume.setVisibility(View.GONE);
            mTvBrightness.setVisibility(View.GONE);
            mFlTouchLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 隐藏视图Runnable
     */
    private Runnable mHideBarRunnable = new Runnable() {
        @Override
        public void run() {
            hideAllView(false);
        }
    };

    /**
     * 隐藏除视频外所有视图
     */
    private void hideAllView(boolean isTouchLock) {
//        mPlayerThumb.setVisibility(View.GONE);
        mFlTouchLayout.setVisibility(View.GONE);
        titleBar.setVisibility(View.GONE);
        llBottomBar.setVisibility(View.GONE);

    }

    View.OnTouchListener mPlayerTouchListener = new View.OnTouchListener() {
        // 触摸模式：正常、无效、缩放旋转
        private static final int NORMAL = 1;
        private static final int INVALID_POINTER = 2;
        // 触摸模式
        private int mode = NORMAL;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (MotionEventCompat.getActionMasked(event)) {
                case MotionEvent.ACTION_DOWN:
                    mode = NORMAL;
                    mHandler.removeCallbacks(mHideBarRunnable);
                    break;
            }
            // 触屏手势处理
            if (mode == NORMAL) {
                if (mGestureDetector.onTouchEvent(event)) {
                    return true;
                }
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_UP) {
                    endGesture();
                }
            }
            return false;
        }
    };

    private GestureDetector.OnGestureListener mPlayerGestureListener = new GestureDetector.SimpleOnGestureListener() {
        // 是否是按下的标识，默认为其他动作，true为按下标识，false为其他动作
        private boolean isDownTouch;
        // 是否声音控制,默认为亮度控制，true为声音控制，false为亮度控制
        private boolean isVolume;
        // 是否横向滑动，默认为纵向滑动，true为横向滑动，false为纵向滑动
        private boolean isLandscape;

        @Override
        public boolean onDown(MotionEvent e) {
            isDownTouch = true;
            Log.e("触发事件---", "onDown");
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // 短按，按下片刻后抬起，会触发这个手势，如果迅速抬起则不会
            Log.e("触发事件---", "onShowPress");
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // 释放，手指离开触时触发(长按、滚动、滑动时，不会触发这个手势)
            llBottomBar.setVisibility(View.VISIBLE);
            titleBar.setVisibility(View.VISIBLE);

            Log.e("触发事件---", "onSingleTapUp");
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //滑动，手指按下后滑动时触发
            Log.e("触发事件---", "onScroll");
            if (!mIsNeverPlay) {
                float mOldX = e1.getX(), mOldY = e1.getY();
                float deltaY = mOldY - e2.getY();
                float deltaX = mOldX - e2.getX();
                if (isDownTouch) {
                    // 判断左右或上下滑动
                    isLandscape = Math.abs(distanceX) >= Math.abs(distanceY);
                    // 判断是声音或亮度控制
                    isVolume = mOldX > getResources().getDisplayMetrics().widthPixels * 0.5f;
                    isDownTouch = false;
                }

                if (isLandscape) {
                    onProgressSlide(-deltaX / surfaceView.getWidth());
                } else {
                    float percent = deltaY / surfaceView.getHeight();
                    if (isVolume) {
                        onVolumeSlide(percent);
                    } else {
                        onBrightnessSlide(percent);
                    }
                }
            }

            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            //长按，触摸屏按下后既不抬起也不移动，过一段时间后触发
            Log.e("触发事件---", "onLongPress");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //手指在触摸屏上迅速移动，并松开的动作
            Log.e("触发事件---", "onFling");
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.e("触发事件---", "onDoubleTap");
            // 如果未进行播放则不执行双击操作
            if (mIsNeverPlay) {
                return true;
            }

            _refreshHideRunnable();
            if (mMediaPlayer.isPlaying()) {
                pause();
            } else {
                play();
            }
            return true;
        }
    };

    /**
     * 快进或者快退滑动改变进度，这里处理触摸滑动不是拉动 SeekBar
     *
     * @param percent 拖拽百分比
     */
    private void onProgressSlide(float percent) {
        int position = (int) mMediaPlayer.getTime();
        long duration = mMediaPlayer.getLength();
        // 单次拖拽最大时间差为100秒或播放时长的1/2
        long deltaMax = Math.min(100 * 1000, duration / 2);
        // 计算滑动时间
        long delta = (long) (deltaMax * percent);
        // 目标位置
        mTargetPosition = delta + position;
        if (mTargetPosition > duration) {
            mTargetPosition = duration;
        } else if (mTargetPosition <= 0) {
            mTargetPosition = 0;
        }

        String desc;
        // 对比当前位置来显示快进或后退
        if (mTargetPosition > position) {
            desc = generateTime(mTargetPosition) + "/" + generateTime(duration);
        } else {
            desc = generateTime(mTargetPosition) + "/" + generateTime(duration);
        }
        setFastForward(desc);
    }

    /**
     * 设置声音控制显示
     *
     * @param volume
     */
    private void setVolumeInfo(int volume) {
        if (mFlTouchLayout.getVisibility() == View.GONE) {
            mFlTouchLayout.setVisibility(View.VISIBLE);
        }
        if (mTvVolume.getVisibility() == View.GONE) {
            mTvVolume.setVisibility(View.VISIBLE);
        }
        mTvVolume.setText((volume * 100 / mMaxVolume) + "%");
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (mCurVolume == INVALID_VALUE) {
            mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mCurVolume < 0) {
                mCurVolume = 0;
            }
        }
        int index = (int) (percent * mMaxVolume) + mCurVolume;
        if (index > mMaxVolume) {
            index = mMaxVolume;
        } else if (index < 0) {
            index = 0;
        }
        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        // 变更进度条
        setVolumeInfo(index);
    }

    /**
     * 处理音量键，避免外部按音量键后导航栏和状态栏显示出来退不回去的状态
     *
     * @param keyCode
     * @return
     */
    public boolean handleVolumeKey(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            setVolume(true);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            setVolume(false);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 递增或递减音量，量度按最大音量的 1/15
     *
     * @param isIncrease 递增或递减
     */
    private void setVolume(boolean isIncrease) {
        int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (isIncrease) {
            curVolume += mMaxVolume / 15;
        } else {
            curVolume -= mMaxVolume / 15;
        }
        if (curVolume > mMaxVolume) {
            curVolume = mMaxVolume;
        } else if (curVolume < 0) {
            curVolume = 0;
        }
        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, 0);
        // 变更进度条
        setVolumeInfo(curVolume);
        mHandler.removeCallbacks(mHideTouchViewRunnable);
        mHandler.postDelayed(mHideTouchViewRunnable, 1000);
    }

    /**
     * 设置亮度控制显示
     *
     * @param brightness
     */
    private void setBrightnessInfo(float brightness) {
        if (mFlTouchLayout.getVisibility() == View.GONE) {
            mFlTouchLayout.setVisibility(View.VISIBLE);
        }
        if (mTvBrightness.getVisibility() == View.GONE) {
            mTvBrightness.setVisibility(View.VISIBLE);
        }
        mTvBrightness.setText(Math.ceil(brightness * 100) + "%");
    }

    /**
     * 滑动改变亮度大小
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (mCurBrightness < 0) {
            mCurBrightness = this.getWindow().getAttributes().screenBrightness;
            if (mCurBrightness < 0.0f) {
                mCurBrightness = 0.5f;
            } else if (mCurBrightness < 0.01f) {
                mCurBrightness = 0.01f;
            }
        }
        WindowManager.LayoutParams attributes = this.getWindow().getAttributes();
        attributes.screenBrightness = mCurBrightness + percent;
        if (attributes.screenBrightness > 1.0f) {
            attributes.screenBrightness = 1.0f;
        } else if (attributes.screenBrightness < 0.01f) {
            attributes.screenBrightness = 0.01f;
        }
        setBrightnessInfo(attributes.screenBrightness);
        this.getWindow().setAttributes(attributes);
    }

    /**
     * 更新进度条
     *
     * @return
     */
    private int setProgress() {
        if (mMediaPlayer == null || mIsSeeking) {
            return 0;
        }
        // 视频播放的当前进度
        int position = (int) mMediaPlayer.getTime();
        // 视频总的时长
        long duration = mMediaPlayer.getLength();
        if (duration > 0) {
            // 转换为 Seek 显示的进度值
            long pos = (long) MAX_VIDEO_SEEK * position / duration;
            seekBarTime.setProgress((int) pos);
        }
        // 获取缓冲的进度百分比，并显示在 Seek 的次进度
        int percent = MediaPlayer.Event.Buffering;

        seekBarTime.setSecondaryProgress(percent);
        // 更新播放时间
        tvCurrentTime.setText(generateTime(position));
        tvVideoTitle.setText(generateTime(duration));
        // 返回当前播放进度
        return position;
    }

    /**
     * 设置快进
     *
     * @param time
     */
    private void setFastForward(String time) {
        if (mFlTouchLayout.getVisibility() == View.GONE) {
            mFlTouchLayout.setVisibility(View.VISIBLE);
        }
        if (mTvFastForward.getVisibility() == View.GONE) {
            mTvFastForward.setVisibility(View.VISIBLE);
        }
        mTvFastForward.setText(time);
    }

    /**
     * 手势结束调用
     */
    private void endGesture() {
        if (mTargetPosition >= 0 && mTargetPosition != mMediaPlayer.getTime()) {
            // 更新视频播放进度
            mMediaPlayer.setTime(mTargetPosition);
            seekBarTime.setProgress((int) (mTargetPosition * MAX_VIDEO_SEEK / mMediaPlayer.getLength()));
            mTargetPosition = INVALID_VALUE;
        }
        // 隐藏触摸操作显示图像
        hideTouchView();
        _refreshHideRunnable();
        mCurVolume = INVALID_VALUE;
        mCurBrightness = INVALID_VALUE;
    }

    /**
     * 显示控制栏
     *
     * @param timeout 延迟隐藏时间
     */
    private void _showControlBar(int timeout) {
        if (!mIsShowBar) {
            setProgress();
            mIsShowBar = true;
        }
//        _setControlBarVisible(true);
        mHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
        // 先移除隐藏控制栏 Runnable，如果 timeout=0 则不做延迟隐藏操作
        mHandler.removeCallbacks(mHideBarRunnable);
        if (timeout != 0) {
            mHandler.postDelayed(mHideBarRunnable, timeout);
        }
    }


    /**
     * 刷新隐藏控制栏的操作
     */
    private void _refreshHideRunnable() {
        mHandler.removeCallbacks(mHideBarRunnable);
        mHandler.postDelayed(mHideBarRunnable, DEFAULT_HIDE_TIMEOUT);
    }

    private void play() {
        if (mIsNeverPlay) {
            mIsNeverPlay = false;
            mIsShowBar = false;
        }
        mMediaPlayer.play();
        ivPlay.setImageResource(R.mipmap.ic_video_pause);
    }

    private void pause() {
        mMediaPlayer.pause();
        ivPlay.setImageResource(R.mipmap.ic_video_play);
    }

    private void stop() {
        mIsNeverPlay = true;
        seekBarTime.setProgress(0);
        mMediaPlayer.setTime(0);
        tvTotalTime.setText(SystemUtil.getMediaTime((int) totalTime));
        mMediaPlayer.stop();
        ivPlay.setImageResource(R.mipmap.ic_video_play);

    }

}
