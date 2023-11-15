package com.example.kiosk_electric_charger.adview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TimeUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.kiosk_electric_charger.MainActivity;
import com.example.kiosk_electric_charger.SplashActivity;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class ImageVideoView extends LinearLayout {

    Context mContext;
//    CustomExoPlayerView customExoPlayerView;
    ImageView imageView;

    VideoView videoView;



    public ImageVideoView(Context context) {
        super(context);
        initView(context);
    }

    public ImageVideoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ImageVideoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        setBackgroundColor(Color.BLACK);

    }

    //url이 비디오 형식일 경우
    public void setUrlVideo(String url) {
        //앞의 진행되던 뷰 지우기
        removeAllViews();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //가운데 정렬
        params.gravity = Gravity.CENTER;
        videoView = new VideoView(mContext);
        videoView.setLayoutParams(params);
        addView(videoView);
        videoView.setVideoURI(Uri.fromFile(new File(url)));
        videoView.setOnPreparedListener(mp -> mp.start());



    }

    //url이 이미지 형식일 경우 imageview load
    public void setUrlImage(String url) {
        //앞의 진행되던 뷰 지우기
        removeAllViews();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //가운데 정렬
        params.gravity = Gravity.CENTER;
        imageView = new ImageView(mContext);
        imageView.setLayoutParams(params);
        addView(imageView);
        Glide.with(this).load(url).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(imageView);

    }

    //확장자가 비디오인지 이미지인지 확인
    public Boolean isVideo(String path) {
        String extension = getExtension(path);

        if (extension.equals("mp4") || extension.equals("MP4") || extension.equals("MOV") || extension.equals("mov") || extension.equals("AVI") || extension.equals("avi") ||
                extension.equals("MKV") || extension.equals("mkv") || extension.equals("WMV") || extension.equals("wmv") || extension.equals("TS") || extension.equals("ts") ||
                extension.equals("TP") || extension.equals("tp") || extension.equals("FLV") || extension.equals("flv") || extension.equals("3GP") || extension.equals("3gp") ||
                extension.equals("MPG") || extension.equals("mpg") || extension.equals("MPEG") || extension.equals("mpeg") || extension.equals("MPE") || extension.equals("mpe") ||
                extension.equals("ASF") || extension.equals("asf") || extension.equals("ASX") || extension.equals("asx") || extension.equals("DAT") || extension.equals("dat") ||
                extension.equals("RM") || extension.equals("rm")) {
            return true;
        } else {
            return false;
        }
    }

    //확장자 나누기
    private String getExtension(String url) {
        return url.substring(url.lastIndexOf(".") + 1, url.length());
    }

    //영상 길이 반환
    public long getVideoLengthToMili(String url){
        MediaPlayer mp = MediaPlayer.create(this.getContext(), Uri.fromFile(new File(url)));
        int duration = mp.getDuration();
        mp.release();
        return TimeUnit.MILLISECONDS.toMillis(duration);
    }

    //동영상 해제
    public void releasePlayer() {
//        if(customExoPlayerView != null){
//            customExoPlayerView.releasePlayer();
//        }
        if(videoView !=null){
            videoView.pause();
            videoView.stopPlayback();
        }

    }

    //동영상 멈춤
    public void StopPlayer() {
//        if(customExoPlayerView != null){
//            customExoPlayerView.releasePlayer();
//        }

        if(videoView !=null){
            videoView.pause();
        }
    }
}