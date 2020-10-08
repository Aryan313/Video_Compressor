package com.ShriRamVC.video;

import android.content.Context;
import android.os.Environment;
import android.util.Log;


import com.ShriRamVC.utils.ProgressCalculator;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;


public class VideoCompressor {

    public static final int SUCCESS = 1;
    public static final int FAILED = 2;
    public static final int NONE = 3;
    public static final int RUNNING = 4;

    private final Context context;
    private final ProgressCalculator mProgressCalculator;
    private boolean isFinished;
    private int status = NONE;
    private String errorMessage = "Compression Failed!";

    public VideoCompressor(Context context) {
        this.context = context;
        mProgressCalculator = new ProgressCalculator();
    }

    public void startCompressing(String inputPath, CompressionListener listener) {
        if (inputPath == null || inputPath.isEmpty()) {
            status = NONE;
            if (listener != null) {
                listener.compressionFinished(NONE, false, null);
            }
            return;
        }

        String outputPath = "";
        outputPath = getAppDir() + "/video_compress.mp4";
        String[] commandParams = new String[26];
        commandParams[0] = "";
        commandParams[1] = "-threads";
        commandParams[2] = "0";
        commandParams[3] = "-y";
        commandParams[4] = "-i";
        commandParams[5] = inputPath;
        commandParams[6] = "-strict";
        commandParams[7] = "experimental";
        commandParams[8] = "160x120";
        commandParams[9] = "-aspect";
        commandParams[10] = "4:3";
        commandParams[11] = "-r";
        commandParams[12] = "30";
        commandParams[13] = "-ab";
        commandParams[14] = "48000";
        commandParams[15] = "-ac";
        commandParams[16] = "2";
        commandParams[17] = "-ar";
        commandParams[18] = "22050";
        commandParams[19] = "-b";
        commandParams[20] = "2097k";
        commandParams[21] = "-vcodec";
        commandParams[22] = "mpeg4";
        commandParams[23] = outputPath;


        compressVideo(commandParams, outputPath, listener);

    }

    public String getAppDir() {
        String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        outputPath += "/" + "vvvvv";
        File file = new File(outputPath);
        if (!file.exists()) {
            file.mkdir();
        }
        outputPath += "/" + "videocompress";
        file = new File(outputPath);
        if (!file.exists()) {
            file.mkdir();
        }
        return outputPath;
    }

    private void compressVideo(String[] command, final String outputFilePath, final CompressionListener listener) {
        try {

            FFmpeg.getInstance(context).execute(command, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    status = SUCCESS;
                }

                @Override
                public void onProgress(String message) {
                    status = RUNNING;
                    Log.e("VideoCronProgress", message);
                    int progress = mProgressCalculator.calcProgress(message);
                    Log.e("VideoCronProgress == ", progress + "..");
                    if (progress != 0 && progress <= 100) {
                        if (progress >= 99) {
                            progress = 100;
                        }
                        listener.onProgress(progress);
                    }
                }

                @Override
                public void onFailure(String message) {
                    status = FAILED;
                    Log.e("VideoCompressor", message);
                    if (listener != null) {
                        listener.onFailure("Error : " + message);
                    }
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {
                    Log.e("VideoCronProgress", "finnished");
                    isFinished = true;
                    if (listener != null) {
                        listener.compressionFinished(status, true, outputFilePath);
                    }
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            status = FAILED;
            errorMessage = e.getMessage();
            if (listener != null) {
                listener.onFailure("Error : " + e.getMessage());
            }
        }
    }

    public interface CompressionListener {
        void compressionFinished(int status, boolean isVideo, String fileOutputPath);

        void onFailure(String message);

        void onProgress(int progress);
    }

    public boolean isDone() {
        return status == SUCCESS || status == NONE;
    }

}
