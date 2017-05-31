/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vrvideo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaCodec;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.Util;
import com.vrvideo.bean.DataUDpBean;
import com.vrvideo.config.Constant;
import com.vrvideo.in.MyVrVideoEventLister;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject.GVRVideoType;
import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class Minimal360Video extends GVRMain {
    private Vr360VideoActivity activity;
    private static final String TAG = "Minimal360Video";
    private Handler handler = new Handler();
    private long currentTimeMillis;
    private static final int USE_EXO_PLAYER = 1; //0IjkMediaPlayer  1系統 2  exoplay
    private MyMessageBroadcastReceiver myMessageBroadcastReceiver;
    private GVRVideoSceneObjectPlayer<?> videoSceneObjectPlayer;
    private Runnable runnable;
    private Runnable runnablePause;//暂停
    private MyVrVideoEventLister myVrVideoEventLister;//视频状态回调
    private ImageView exo_play_replay_layout;//暂停播放
    private Uri url;
    private String playGuid;//操作Id唯一

    Minimal360Video(Vr360VideoActivity activity, String url, String playGuid) {
        this.activity = activity;
        this.url = Uri.parse(url);
        this.playGuid = playGuid;
        init();
        initView();
        registerReceiver();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onInit(GVRContext gvrContext) {
        GVRScene scene = gvrContext.getMainScene();
        // create sphere / mesh
        GVRSphereSceneObject sphere = new GVRSphereSceneObject(gvrContext, 72, 144, false);
        GVRMesh mesh = sphere.getRenderData().getMesh();
        sphere.getTransform().setScale(200f, 200f, 200f);
        // create video scene
        GVRVideoSceneObject video = new GVRVideoSceneObject(gvrContext, mesh, videoSceneObjectPlayer, GVRVideoType.MONO);
        video.setName("video");
        // apply video to scene
        scene.addSceneObject(video);

    }

    private void initView() {
        ViewGroup mRenderableViewGroup = (ViewGroup) activity.findViewById(android.R.id.content).getRootView();
        exo_play_replay_layout = (ImageView) activity.getLayoutInflater().inflate(R.layout.simple_exo_play_replay, mRenderableViewGroup, false);
        mRenderableViewGroup.addView(exo_play_replay_layout);
        exo_play_replay_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isPlaying = isPlaying();
                state(isPlaying);
                isPlaying = isPlaying();
                if (isPlaying()) {
                    exo_play_replay_layout.setImageResource(R.drawable.ic_play_circle_outline_paue_48px);
                } else {
                    exo_play_replay_layout.setImageResource(R.drawable.ic_play_circle_outline_play_48px);
                }
                togglePauseViewpostDelayed(isPlaying);
            }
        });
        mRenderableViewGroup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d(TAG, "onTouch");
                    togglePauseView(isPlaying());
                }
                return false;
            }
        });
    }

    /***
     * /****
     * 暂停和播放view状态
     *
     * @param isPlaying  是否播放 false  true
     ***/
    private void togglePauseView(boolean isPlaying) {
        if (exo_play_replay_layout.getVisibility() == View.VISIBLE) {
            exo_play_replay_layout.setVisibility(View.GONE);
            return;
        } else {
            exo_play_replay_layout.setVisibility(View.VISIBLE);
        }
        if (isPlaying) {
            exo_play_replay_layout.setImageResource(R.drawable.ic_play_circle_outline_paue_48px);
        } else {
            exo_play_replay_layout.setImageResource(R.drawable.ic_play_circle_outline_play_48px);
        }
        togglePauseViewpostDelayed(isPlaying);
    }

    /**
     * 执行在暂停和view隐藏
     ***/
    private void togglePauseViewpostDelayed(final boolean isPlaying) {
        if (runnablePause != null) {
            exo_play_replay_layout.removeCallbacks(runnablePause);
        }
        runnablePause = new Runnable() {
            @Override
            public void run() {
                if (exo_play_replay_layout != null && isPlaying) {
                    exo_play_replay_layout.setVisibility(View.GONE);
                }
            }
        };
        exo_play_replay_layout.postDelayed(runnablePause, 3000);
    }

    /*****
     * 初始化状态
     * *****/
    private void init() {
        activity.getAppSettings().setShowLoadingIcon(false);
        activity.getAppSettings().setUseProtectedFramebuffer(true);
        activity.getAppSettings().setUseAndroidWearTouchpad(true);
        switch (USE_EXO_PLAYER) {
            case 0:
                videoSceneObjectPlayer = makeIjkMediaPlayer();
                break;
            case 1:
                videoSceneObjectPlayer = makeMediaPlayer();
                break;
            case 2:
                videoSceneObjectPlayer = makeExoPlayer();
                break;
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                Log.d("Minimal360VideoActivity", "asdsad");
                currentTimeMillis = System.currentTimeMillis();
                Log.d(TAG, "时间" + currentTimeMillis);
                switch (USE_EXO_PLAYER) {
                    case 0:
                        ((IjkMediaPlayer) videoSceneObjectPlayer.getPlayer()).seekTo(2000);
                        break;
                    case 1:
                        ((MediaPlayer) videoSceneObjectPlayer.getPlayer()).seekTo(2000);
                        break;
                    case 2:
                        ((ExoPlayer) videoSceneObjectPlayer.getPlayer()).seekTo(2000);
                        break;
                }

            }
        };
        // handler.postAtTime(runnable, 15000);
    }

    /*****
     * 状态切换
     * @param isPause 是否要暂停
     * ****/
    void state(boolean isPause) {
        if (null != videoSceneObjectPlayer) {
            final Object player = videoSceneObjectPlayer.getPlayer();
            switch (USE_EXO_PLAYER) {
                case 0:
                    IjkMediaPlayer ijkMediaPlayer = (IjkMediaPlayer) player;
                    if (isPause) {
                        ijkMediaPlayer.pause();
                    } else {
                        ijkMediaPlayer.start();
                    }
                    break;
                case 1:
                    MediaPlayer mediaPlayer = (MediaPlayer) player;
                    if (isPause) {
                        mediaPlayer.pause();
                    } else {
                        mediaPlayer.start();
                    }
                    break;
                case 2:
                    ExoPlayer exoPlayer = (ExoPlayer) player;
                    if (isPause) {
                        exoPlayer.setPlayWhenReady(false);
                    } else {
                        exoPlayer.setPlayWhenReady(true);
                    }
                    break;
            }
        }
    }

    /*****
     * 获取播放状态
     * ***/
    private boolean isPlaying() {
        if (null != videoSceneObjectPlayer) {
            final Object player = videoSceneObjectPlayer.getPlayer();
            switch (USE_EXO_PLAYER) {
                case 0:
                    IjkMediaPlayer ijkMediaPlayer = (IjkMediaPlayer) player;
                    return ijkMediaPlayer.isPlaying();
                case 1:
                    MediaPlayer mediaPlayer = (MediaPlayer) player;
                    return mediaPlayer.isPlaying();
                case 2:
                    ExoPlayer exoPlayer = (ExoPlayer) player;
                    return exoPlayer.getPlayWhenReady();
            }
        }
        return false;

    }

    /******
     * 注册广播事件
     ****/
    private void registerReceiver() {
        if (myMessageBroadcastReceiver != null) {
            activity.unregisterReceiver(myMessageBroadcastReceiver);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION);
        myMessageBroadcastReceiver = new MyMessageBroadcastReceiver();
        activity.registerReceiver(myMessageBroadcastReceiver, intentFilter);
        Log.d(TAG, "registerReceiver");
    }

    /***
     * Destroy the widget and free memory.
     * 销毁小部件和空闲内存。
     **/
    public void onDestroy() {
        if (myMessageBroadcastReceiver != null) {
            activity.unregisterReceiver(myMessageBroadcastReceiver);
        }
        videoSceneObjectPlayer.release();
        handler.removeCallbacks(runnable);
    }

    /****
     * IjkMediaPlayer 播放類
     * ******/
    private GVRVideoSceneObjectPlayer<IjkMediaPlayer> makeIjkMediaPlayer() {
        final IjkMediaPlayer mediaPlayer = new IjkMediaPlayer();
        try {
            mediaPlayer.setDataSource(activity, url);
            mediaPlayer._prepareAsync();
            mediaPlayer.setLooping(true);//是否循环播放
            mediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer iMediaPlayer) {
                    Log.d(TAG, "播放完成onCompletion:");
                    completion();
                }
            });
            mediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(IMediaPlayer iMediaPlayer) {
                    Log.d(TAG, "时间onSeekComplete:" + (System.currentTimeMillis() - currentTimeMillis));
                }
            });
            mediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer iMediaPlayer) {
                    Log.d(TAG, "时间onPrepared:" + (System.currentTimeMillis() - currentTimeMillis));
                }
            });
            mediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                    error("onError");
                    return false;
                }
            });
        } catch (IOException e) {
            error(e.getMessage());
            Log.e("Minimal360Video", "Assets were not loaded. Stopping application!");
            return null;
        }
        Log.d("Minimal360Video", "starting player.");
        return new GVRVideoSceneObjectPlayer<IjkMediaPlayer>() {
            @Override
            public IjkMediaPlayer getPlayer() {
                return mediaPlayer;
            }

            @Override
            public void setSurface(Surface surface) {
                mediaPlayer.setSurface(surface);
            }

            @Override
            public void release() {
                mediaPlayer.release();
            }

            @Override
            public boolean canReleaseSurfaceImmediately() {
                return true;
            }

            @Override
            public void pause() {
                mediaPlayer.pause();
            }

            @Override
            public void start() {
                mediaPlayer.start();
            }
        };
    }

    /****
     * 系統自帶 播放類
     * ******/
    private GVRVideoSceneObjectPlayer<MediaPlayer> makeMediaPlayer() {
        final MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            Log.d("Minimal360Video", "Assets was found.");
            //  mediaPlayer.setDataSource(activity, Uri.parse("http://120.25.246.21/vrMobile/travelVideo/zhejiang_xuanchuanpian.mp4"));
            mediaPlayer.setDataSource(activity, url);
            Log.d("Minimal360Video", "DataSource was set.");
            mediaPlayer.prepare();
            mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    Log.d(TAG, "时间onSeekComplete:" + (System.currentTimeMillis() - currentTimeMillis));
                }
            });
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.d(TAG, "时间onPrepared:" + (System.currentTimeMillis() - currentTimeMillis));
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d(TAG, "播放完成onCompletion:");
                    completion();
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    error("onError");
                    return false;
                }
            });
        } catch (IOException e) {
            error(e.getMessage());
            Log.e("Minimal360Video", "Assets were not loaded. Stopping application!");
            return null;
        }
        mediaPlayer.setLooping(false);//是否循环播放
        Log.d("Minimal360Video", "starting player.");
        return GVRVideoSceneObject.makePlayerInstance(mediaPlayer);
    }

    private MediaCodecAudioTrackRenderer audioRenderer;
    private MediaCodecVideoTrackRenderer videoRenderer;

    /****
     * google exoPlay
     * ******/
    private GVRVideoSceneObjectPlayer<ExoPlayer> makeExoPlayer() {
        final ExoPlayer player = ExoPlayer.Factory.newInstance(5);
        try {
            final DefaultUriDataSource dataSource = new DefaultUriDataSource(activity, Util.getUserAgent(activity, "123"));
            final ExtractorSampleSource sampleSource = new ExtractorSampleSource(url,
                    dataSource, new DefaultAllocator(128 * 1024), 128 * 1024 * 256);
            videoRenderer = new MediaCodecVideoTrackRenderer(activity, sampleSource,
                    MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,
                    MediaCodecSelector.DEFAULT);
        } catch (Exception e) {
            error(e.getMessage());
            return null;
        }
        // player.prepare(videoRenderer, audioRenderer);
        return new GVRVideoSceneObjectPlayer<ExoPlayer>() {
            @Override
            public ExoPlayer getPlayer() {
                return player;
            }

            @Override
            public void setSurface(final Surface surface) {
                player.addListener(new ExoPlayer.Listener() {
                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        Log.d(TAG, "onPlayerStateChanged:" + playbackState);
                        switch (playbackState) {
                            case ExoPlayer.STATE_BUFFERING://缓存区加载
                                Log.d(TAG, "时间STATE_BUFFERING:" + (System.currentTimeMillis() - currentTimeMillis));
                                break;
                            case ExoPlayer.STATE_ENDED://播放完成
                                completion();
                                player.seekTo(0);
                                break;
                            case ExoPlayer.STATE_IDLE:
                                Log.d(TAG, "时间STATE_IDLE:" + (System.currentTimeMillis() - currentTimeMillis));
                                break;
                            case ExoPlayer.STATE_PREPARING:
                                Log.d(TAG, "时间STATE_PREPARING:" + (System.currentTimeMillis() - currentTimeMillis));
                                break;
                            case ExoPlayer.STATE_READY://准备当前进度
                                Log.d(TAG, "时间STATE_READY:" + (System.currentTimeMillis() - currentTimeMillis));
                                break;
                            default:
                                break;
                        }
                    }

                    @Override
                    public void onPlayWhenReadyCommitted() {
                        surface.release();
                    }

                    @Override
                    public void onPlayerError(ExoPlaybackException error) {
                        error(error.getMessage());
                    }
                });

                player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
            }

            @Override
            public void release() {
                player.release();
            }

            @Override
            public boolean canReleaseSurfaceImmediately() {
                return false;
            }

            @Override
            public void pause() {
                player.setPlayWhenReady(false);
            }

            @Override
            public void start() {
                player.setPlayWhenReady(true);
            }
        };
    }

    /***
     * 播放视频
     * @param uuid  播放id
     * ***/
    private void playerVideo(String uuid) {
        playGuid = uuid;
        switch (USE_EXO_PLAYER) {
            case 0:
                IjkMediaPlayer ijkMediaPlayer = (IjkMediaPlayer) videoSceneObjectPlayer.getPlayer();
                ijkMediaPlayer.seekTo(0);
                ijkMediaPlayer.start();
                break;
            case 1:
                MediaPlayer mediaPlayer = (MediaPlayer) videoSceneObjectPlayer.getPlayer();
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                break;
            case 2:
                ExoPlayer exoPlayer = (ExoPlayer) videoSceneObjectPlayer.getPlayer();
                exoPlayer.prepare(videoRenderer, audioRenderer);
                exoPlayer.setPlayWhenReady(true);
                break;
        }
        if (myVrVideoEventLister != null) {
            myVrVideoEventLister.onPlayVideo(uuid);
        }
    }

    /*****
     * 结束视频
     * ****/
    private void stopVideo() {
        switch (USE_EXO_PLAYER) {
            case 0:
                IjkMediaPlayer ijkMediaPlayer = (IjkMediaPlayer) videoSceneObjectPlayer.getPlayer();
                ijkMediaPlayer.stop();
                break;
            case 1:
                MediaPlayer mediaPlayer = (MediaPlayer) videoSceneObjectPlayer.getPlayer();
                mediaPlayer.stop();
                break;
            case 2:
                ExoPlayer exoPlayer = (ExoPlayer) videoSceneObjectPlayer.getPlayer();
                exoPlayer.stop();
                break;
        }
    }

    /*****
     * 结束视频
     * ****/
    private void correctingPosition(String durationVideo) {
        switch (USE_EXO_PLAYER) {
            case 0:
                IjkMediaPlayer ijkMediaPlayer = (IjkMediaPlayer) videoSceneObjectPlayer.getPlayer();
                ijkMediaPlayer.seekTo(Long.parseLong(durationVideo));
                break;
            case 1:
                MediaPlayer mediaPlayer = (MediaPlayer) videoSceneObjectPlayer.getPlayer();
                mediaPlayer.seekTo(Integer.parseInt(durationVideo));
                break;
            case 2:
                ExoPlayer exoPlayer = (ExoPlayer) videoSceneObjectPlayer.getPlayer();
                exoPlayer.seekTo(Long.parseLong(durationVideo));
                break;
        }
    }

    public void setMyVrVideoEventLister(MyVrVideoEventLister myVrVideoEventLister) {
        this.myVrVideoEventLister = myVrVideoEventLister;
    }

    public String getPlayGuid() {


        return playGuid;
    }

    /****
     * 广播监听
     *****/
    class MyMessageBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Constant.ACTION)) {
                DataUDpBean baseBean = intent.getParcelableExtra("data");

                Log.d(TAG, "playerVideo");
                if (baseBean.getState() == null) return;
                if (baseBean.getState().equals(Constant.VALUE_0)) {//开始播放
                    //                   setPlayGuid(baseBean.getGuid());
//                        if (baseBean.getData().getPath() != null && !baseBean.getData().getPath().isEmpty()) {
//                            uri = Uri.parse(baseBean.getData().getPath());
//                        }
                    Log.d(TAG, "playerVideo" + baseBean.getGuid());
                    playerVideo(baseBean.getGuid());
                } else if (baseBean.getState().equals(Constant.VALUE_1)) {//结束
                    stopVideo();
                    Log.d(TAG, "correctingPosition");
                } else if (baseBean.getState().equals(Constant.VALUE_2)) {//暂停
                    state(true);
                    Log.d(TAG, "togglePause");
                } else if (baseBean.getState().equals(Constant.VALUE_3)) {//继续
                    state(false);
                    Log.d(TAG, "togglePausejixu");
                } else if (baseBean.getState().equals(Constant.VALUE_4)) {//跳帧
                    Log.d(TAG, "getCorrect");
                    correctingPosition(baseBean.getDate());
                }

            }
        }
    }

    /**
     * error信息
     ***/
    private void error(String getMessage) {
        if (myVrVideoEventLister != null) {
            myVrVideoEventLister.onLoadError(getMessage);
            myVrVideoEventLister.onCompletion();
        }
    }

    /**
     * 播放完成error信息
     ***/
    private void completion() {
        if (myVrVideoEventLister != null) {
            myVrVideoEventLister.onCompletion();
        }
    }
}
