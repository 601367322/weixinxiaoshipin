package com.yixia.camera.demo.ui.record.views;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.yixia.camera.demo.common.CommonIntentExtra;
import com.yixia.camera.demo.log.Logger;
import com.yixia.camera.demo.po.POThemeSingle;
import com.yixia.camera.demo.preference.PreferenceKeys;
import com.yixia.camera.demo.preference.PreferenceUtils;
import com.yixia.camera.model.MediaObject;
import com.yixia.videoeditor.adapter.UtilityAdapter;

/**
 * MV主题预览
 * 
 * @author tangjun
 *
 */
public class ThemeSufaceView extends SurfaceView implements Callback {

	/** 循环 */
	private static final int HANDLER_MESSAGE_LOOP = 0;
	/** 进度检测 */
	private static final int HANDLER_MESSAGE_PROGRESS = 1;

	/** 画布 */
	private SurfaceHolder mSurfaceHolder;
	/** 视频存储对象 */
	private MediaObject mMediaObject;
	/** 当前主题 */
	private POThemeSingle mCurrentTheme;
	/** 视频播放完回调 */
	private OnComplateListener mOnComplateListener;
	/** 视频音乐 */
	private String mMusicPath;
	/** 输出路径 */
	private String mOutputPath;
	/** 输入 */
	private String mInputPath;
	/** MV滤镜路径，滤镜公共目录 */
	private String mMVPath, mFilterCommonPath, mFilterPath, mWatermarkPath, mWatermarkBlendmode;
	/** 片尾动画路径 */
	private String mEndPath;
	/** 矩阵参数 */
	private String mOpengMainmat;
	/** 特定贴图信息 */
	private String mPoster;
	/** 画布是否准备 */
	private boolean mSurfaceCreated;
	/** 是否正在播放 */
	private boolean mIsPlaying, mIsPause;
	/** 是否需要初始化Filter */
	private boolean mNeedInit;
	/** 是否导入图片 */
	private boolean mImportImage;
	/** 随机数因子 */
	private long mRandomFactor;
	/** 片尾的宽高 */
	private int mEndWidth, mEndHeight;
	/** 录制模式 */
	private int mRecordMode;
	/** 变速 */
	private float mSpeed = 1F;
	/** 主题静音、原声静音 */
	private boolean mThemeMute, mOrgiMute;

	public ThemeSufaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ThemeSufaceView(Context context) {
		super(context);
		init();
	}

	public void reset() {
		mFilterPath = "";
		mWatermarkPath = "";
		mWatermarkBlendmode = "";
		mSpeed = 1F;
		mThemeMute = false;
		//mOrgiMute = false;
		mPoster = "";
	}

	public void setPoster(String path) {
		this.mPoster = path;
	}

	public void setSpeed(float speed) {
		this.mSpeed = speed;
	}

	/** 设置主题静音 */
	public void setThemeMute(boolean mute) {
		this.mThemeMute = mute;
	}

	/** 设置原声静音 */
	public void setOrgiMute(boolean mute) {
		this.mOrgiMute = mute;
	}

	/** 初始化 */
	private void init() {
		getHolder().addCallback(this);
		getHolder().setFixedSize(480, 480);
		mRandomFactor = System.currentTimeMillis() / 1000;
	}

	public void setOnComplateListener(OnComplateListener l) {
		this.mOnComplateListener = l;
	}

	/** 从Intent获取视频相关的参数 */
	public void setIntent(Intent intent) {
		if (intent != null) {
			mImportImage = intent.getBooleanExtra(CommonIntentExtra.EXTRA_MEDIA_IMPORT_IMAGE, false);
			boolean mImportVideo = intent.getBooleanExtra(CommonIntentExtra.EXTRA_MEDIA_IMPORT_VIDEO, false);
//			mRecordMode = intent.getIntExtra("recordMode", -1);
//
//			//后置摄像头
//			if (mRecordMode == VideoRecorderActivity.RECORD_MODE_SYSTEM) {
//				int cameraId = intent.getIntExtra("cameraId", Camera.CameraInfo.CAMERA_FACING_BACK);
//				//系统录制需要翻转
//				if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
//					//后置摄像头
//					mOpengMainmat = "mainmat= * mat2(0.0, 0.75, -1.0, 0.0) + vec2(0.0, 1.0); ";
//				} else if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//					//前置摄像头
//					mOpengMainmat = "mainmat= * mat2(0.0, -0.75, -1.0, 0.0) + vec2(1.0, 1.0); ";
//				}
//			}
		}
	}

	/** 设置视频音乐 */
	public void setMusicPath(String path) {
		this.mMusicPath = path;
	}

	public String getMusicPath() {
		return mMusicPath;
	}

	/** 设置视频存储信息 */
	public void setMediaObject(MediaObject obj) {
		this.mMediaObject = obj;
	}

	/** 传递当前主题信息 */
	public void setTheme(POThemeSingle theme) {
		this.mCurrentTheme = theme;
	}

	/** 设置mp4路径 */
	public void setOutputPath(String output) {
		this.mOutputPath = output;
	}

	/** 设置输入源 */
	public void setInputPath(String input) {
		this.mInputPath = input;
	}

	/** MV滤镜路径 */
	public void setMVPath(String path) {
		this.mMVPath = path;
	}

	/** 设置高级编辑-滤镜 */
	public void setFilterPath(String path) {
		this.mFilterPath = path;
	}

	/** 设置高级编辑-水印 */
	public void setWatermarkPath(String path, String blendmode) {
		this.mWatermarkPath = path;
		this.mWatermarkBlendmode = blendmode;
	}

	/** MV滤镜公共路径 */
	public void setFilterCommomPath(String path) {
		this.mFilterCommonPath = path;
	}

	/** 设置片尾动画路径 */
	public void setVideoEndPath(String path) {
		this.mEndPath = path;
		this.mEndWidth = PreferenceUtils.getInt(PreferenceKeys.THEME_LOGO_AUTHOR_WIDTH, 0);
		this.mEndHeight = PreferenceUtils.getInt(PreferenceKeys.THEME_LOGO_AUTHOR_HEIGHT, 0);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mSurfaceCreated = true;
		mIsPlaying = true;
		mSurfaceHolder = holder;
		if (mNeedInit)
			initFilter();
		Logger.e("[ThemeSurfaceView]surfaceCreated...");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		mSurfaceHolder = holder;
		Logger.e("[ThemeSurfaceView]surfaceChanged...");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mSurfaceCreated = false;
		mIsPlaying = false;
		mSurfaceHolder = null;
		UtilityAdapter.FilterParserFree();
		Logger.e("[ThemeSurfaceView]surfaceDestroyed...");
	}

	/** 
	 * 开始转码
	 * 
	 * @return
	 */
	public boolean startEncoding() {
//		UtilityAdapter.FilterParserInit(getSetting(true), null);
		return true;
	}

	/** 初始化 */
	public void initFilter() {
		if (mSurfaceCreated && mSurfaceHolder != null) {
			mIsPlaying = true;
			mIsPause = false;
			mNeedInit = false;
			UtilityAdapter.FilterParserFree();
			//预览
//			UtilityAdapter.FilterParserInit(getSetting(false), mSurfaceHolder.getSurface());
		} else {
			mNeedInit = true;
		}
	}

	/** 继续播放 */
	public void start() {
		if (mSurfaceCreated) {
			mIsPlaying = true;
			mIsPause = false;
			UtilityAdapter.FilterParserPause(false);
		}
	}

	/** 暂停 */
	public void pause() {
		if (mSurfaceCreated) {
			mIsPlaying = false;
			mIsPause = true;
			UtilityAdapter.FilterParserPause(true);
		}
	}

	/** 是否正在播放 */
	public boolean isPlaying() {
		return mIsPlaying;
	}

	private Handler mVideoHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int progress;
			switch (msg.what) {
			case HANDLER_MESSAGE_LOOP:
				progress = UtilityAdapter.FilterParserInfo(UtilityAdapter.FILTERINFO_PROGRESS);
				if (progress == 100) {
					if (mOnComplateListener != null)
						mOnComplateListener.onComplate();
					//					initFilter();
					//					sendMessageDelayed(mVideoHandler.obtainMessage(HANDLER_MESSAGE_LOOP, msg.arg1, msg.arg2), msg.arg2);

				} else {
					clearMessages();
					mVideoHandler.sendEmptyMessage(HANDLER_MESSAGE_PROGRESS);
				}
				break;
			case HANDLER_MESSAGE_PROGRESS:
				progress = UtilityAdapter.FilterParserInfo(UtilityAdapter.FILTERINFO_PROGRESS);
				if (progress > 0) {
					int delayMillis = UtilityAdapter.FilterParserInfo(UtilityAdapter.FILTERINFO_TOTALMS);//进度
					int startTime = delayMillis - (int) (delayMillis * (progress / 100F));
					mVideoHandler.removeMessages(HANDLER_MESSAGE_LOOP);
					mVideoHandler.sendMessageDelayed(mVideoHandler.obtainMessage(HANDLER_MESSAGE_LOOP, 0, delayMillis), startTime);
				} else {
					sendEmptyMessageDelayed(HANDLER_MESSAGE_PROGRESS, 500);//没有取到进度，继续取
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	/** 暂停并且清除定时任务 */
	public void pauseClearDelayed() {
		pause();
		clearMessages();
	}

	/** 区域内循环播放 */
	public void loopDelayed() {
		if (mIsPause)
			start();
		else
			initFilter();
		clearMessages();
		mVideoHandler.sendEmptyMessage(HANDLER_MESSAGE_PROGRESS);
	}

	private void clearMessages() {
		if (mVideoHandler.hasMessages(HANDLER_MESSAGE_LOOP))
			mVideoHandler.removeMessages(HANDLER_MESSAGE_LOOP);
		if (mVideoHandler.hasMessages(HANDLER_MESSAGE_PROGRESS))
			mVideoHandler.removeMessages(HANDLER_MESSAGE_PROGRESS);
	}

	/** 释放底层库 */
	public void release() {
		mIsPause = false;
		mIsPlaying = false;
		UtilityAdapter.FilterParserFree();
	}



	public static interface OnComplateListener {
		/** 播放完成 */
		public void onComplate();
	}
}
