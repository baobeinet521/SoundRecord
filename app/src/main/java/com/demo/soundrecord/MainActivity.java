package com.demo.soundrecord;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static String TAG = "soundRecord";

    private Button mStartRecordBtn;
    private Button mStopRecordBtn;
    private Button mPlayRecordBtn;
    private Button mStopPalyBtn;

    private boolean isRecording = false;
    private AudioRecord mAudioRecord;
    private AudioTrack mAudioTrack;
    private int mBufferSizeInBytes = 16384;
    private int mSampleRateInHz = 16000;
    private int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStartRecordBtn = findViewById(R.id.start_record);
        mStopRecordBtn = findViewById(R.id.stop_record);
        mPlayRecordBtn = findViewById(R.id.play_record);
        mStopPalyBtn = findViewById(R.id.stop_play_record);


        mStartRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkPermission(MainActivity.this)){
                    requestPermission(MainActivity.this);

                }else{
                    Toast.makeText(MainActivity.this,"开始录音",Toast.LENGTH_LONG).show();
                    isRecording = true;
                    startRecord();
                }

            }
        });

        mStopRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkPermission(MainActivity.this)){
                    requestPermission(MainActivity.this);
                }else{
                    Toast.makeText(MainActivity.this,"停止录音",Toast.LENGTH_LONG).show();
                    stopRecord();
                }

            }
        });

        mPlayRecordBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if(!checkPermission(MainActivity.this)){
                    requestPermission(MainActivity.this);
                }else{
                    Toast.makeText(MainActivity.this,"播放录音",Toast.LENGTH_LONG).show();
                    playRecord();
                }

            }
        });
        mStopPalyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"停止播放录音====");
                Toast.makeText(MainActivity.this,"停止播放录音",Toast.LENGTH_LONG).show();
                stopPlay();
            }
        });
    }


    public void startRecord() {
        Log.i(TAG, "开始录音");
        //16K采集率
//        int frequency = 16000;
        //格式
//        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        //16Bit
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        //生成PCM文件
        String fileName = "ceshi.pcm";
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ICAPP" + File.separator + "SoundRecord" + File.separator;
        File destDir = new File(storePath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        mFilePath = storePath + fileName;

        Log.d(TAG, "startRecord: filePath     " + mFilePath);

        File mFile = new File(mFilePath);
        //如果存在，就先删除再创建
        if (mFile.exists()) {
            mFile.delete();
            Log.i(TAG, "删除文件");
        }
        try {
            mFile.createNewFile();
            Log.i(TAG, "创建文件");
        } catch (IOException e) {
            Log.i(TAG, "未能创建");
            throw new IllegalStateException("未能创建" + mFile.toString());
        }
        try {
            //输出流
            OutputStream os = new FileOutputStream(mFile);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            final DataOutputStream dos = new DataOutputStream(bos);

            int bufferSizeAudioRecord = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelConfig, audioEncoding);
            Log.i(TAG, "mSampleRateInHz=  " + mSampleRateInHz + "    bufferSize=    " + bufferSizeAudioRecord);
            int bufferSize = 16384;
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mSampleRateInHz, mChannelConfig, audioEncoding, bufferSize);
            mAudioRecord.startRecording();

            final byte[] buffer = new byte[16384];


            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (dos != null) {
                        while (isRecording) {
                            int read = mAudioRecord.read(buffer, 0, mBufferSizeInBytes);  //由AudioRecord读出来的存放到data
                            try {
                                dos.write(buffer);   //通过字符流写入到SDCAED
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
//                            mAudioRecord.stop();
                            // 彻底释放资源
//                            mAudioRecord.release();
                            dos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();


//            while (isRecording) {
//                int read = mAudioRecord.read(buffer, 0, mBufferSizeInBytes);  //由AudioRecord读出来的存放到data
//                if (read != AudioRecord.ERROR_INVALID_OPERATION) {
//                    try {
//                        dos.write(buffer);   //通过字符流写入到SDCAED
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }

//            mAudioRecord.stop();
//            // 彻底释放资源
//            mAudioRecord.release();
//            dos.close();

        } catch (Throwable t) {
            Log.e(TAG, "录音失败===   " + t.toString());
        }
    }

    public void stopRecord() {
        Log.d(TAG, "stopRecord: 停止录音");
        isRecording = false;
        mAudioRecord.stop();
        mAudioRecord.release();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void playRecord() {
        Log.d(TAG, "playRecord: 播放录音");
        mAudioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(mSampleRateInHz)
                        .setChannelMask(mChannelConfig)
                        .build(),
                mBufferSizeInBytes, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);   //创建AudioTrack对象

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mAudioTrack != null) {
                    mAudioTrack.play();   //开始播放，此时播放的数据为空
                    String parentFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ICAPP" + File.separator + "SoundRecord" + File.separator;

                    File pcmFile = new File(parentFilePath, "ceshi.pcm");      //创建需要播放的pcm 录音文件
                    String filePath = parentFilePath + "ceshi.pcm";
                    Log.d(TAG, "run: 播放录音文件目录====   " +filePath);
                    if (pcmFile != null) {
                        byte[] tempBuffer = new byte[mBufferSizeInBytes];
                        try {
                            FileInputStream fis = new FileInputStream(filePath);
                            if (fis.available() > 0) {  //获取test.pcm的大小
                                int read = 0;
                                while ((read = fis.read(tempBuffer)) != -1) {
                                    if (read == AudioTrack.ERROR_INVALID_OPERATION ||
                                            read == AudioTrack.ERROR_BAD_VALUE ||
                                            read == AudioTrack.ERROR
                                    ) {
                                        continue;
                                    } else {
                                        mAudioTrack.write(tempBuffer, 0, read);  //将读取的数据写入到AudioTrack里面
                                    }
                                }

                            }

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public void stopPlay(){
        if(mAudioTrack != null){
            mAudioTrack.stop();
        }

    }
    public static boolean checkPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity) {
        if (!(ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                !(ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                || !(ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
        || !(ContextCompat.checkSelfPermission(activity, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO,Manifest.permission.FOREGROUND_SERVICE}, 0);
        }
    }

    @Override
    protected void onStop() {
//        mAudioRecord.stop();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isBackground(MainActivity.this)){
            Log.d(TAG, "onPause: app 在前台运行");
        }else{
            Log.d(TAG, "onPause: app 在后台运行");

        }
        Intent intent = new Intent(this, NotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //android8.0以上通过startForegroundService启动service
            Log.d(TAG, "onPause: android8.0以上通过startForegroundService启动service");
            startForegroundService(intent);
        } else {
            Log.d(TAG, "onPause: android8.0以下通过startService启动service");
            startService(intent);
        }
    }

    // 判断程序是否在后台
    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }
}
