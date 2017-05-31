package com.vrvideo.in;


/**
 * Created by yangc on 2017/5/20.
 * E-Mail:yangchaojiang@outlook.com
 * Deprecated: 视频播放转回调
 */

public interface MyVrVideoEventLister {

    void onCompletion();//播放完成

/*    void onLoadSuccess();//加载成功*/

    void onLoadError(String errorMessage);//错误

    void onPlayVideo(String uuid);//开始播放
}
