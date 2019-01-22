# AndroidVLC  基于vlc的视频播放器
v1.0 实现视频正常播放，横竖屏切换；可以调节音量、亮度

如果不想编译的话,可直接下载AndroidDemo,Demo为AndroidStudio开发环境。若想自己编译可去官方下载源码。接下来简单介绍API的部分调用接口:(1、publicvoidplayMRL(Stringmrl)对应MediaPlayer的setDataSource(path),可以读取本地文件和流媒体文件,读取本地文件要加”file://”+path,读取流媒体就可以直接输入”http://“即可。(2、publicnativevoidplay()开始(3、p
如果不想编译的话,可直接下载 Android Demo,Demo为Android Studio开发环境。若想自己编译可去官方下载源码。




####接下来简单介绍API 的部分调用接口: 
```
(1、public void playMRL(String mrl) 
对应MediaPlayer的setDataSource(path),可以读取本地文件和流媒体文件,读取本地文件要加”file://”+path,读取流媒体就可以直接输入”http://“即可。


(2、 public native void play() 
开始


(3、 public native void pause() 
暂停


(4、public native void stop()
停止


(5、public native boolean isPlaying()
是否正在播放


(6、public native int getPlayerState(); 
获取播放的状态


(7、public native int getVolume()
获取音量。


(8、public native int setVolume(int volume)
设置音量。


(9、public native long getLength() 
获取视频的长度,以毫秒为单位。


(10、public native long getTime() 
返回视频当前时间,以毫秒为单位。


(11、public native long setTime(long time) 
设置视频当前时间,以毫秒为单位。


(12、public native float getPosition() 
设置视频当前位置。


(13、public native void setPosition(float pos) 
设置视频当前位置。


(14、public native void setRate(float rate) 
设置播放速度,1是正常速度,2是两倍速。


(15、public native boolean isSeekable() 
是否支持拖拽(判断是否为直播流的重要依据)。


(16、public void destroy()
销毁LibVLC实例。


(17、public byte[] getThumbnail(String mrl, int i_width, int i_height) 
获取视频的截图。


(18、public void setHardwareAcceleration(int hardwareAcceleration) 
设置硬解编码,参考LibVLC.HW_ACCELERATION_FULL等参数


(19、public void setNetworkCaching(int networkcaching) 
设置网络缓冲。


(20、public void setFrameSkip(boolean frameskip)
帧解码出错跳过。
```
