package valdez.albert.com.beep;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.SoundPool;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.Time;

public class BeepService extends Service {

    private Handler handler;
    private SoundPool soundPool = null;
    private int soundId;
    private AudioManager audioManager;
    private Time time;
    public BroadcastReceiver receiver;
    private AudioAttributes attributes;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Time
        time = new Time();

        // IntentFilter
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);

        // Broadcast Receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Determine the current time
                time.setToNow();

                if (time.minute == 29 || time.minute == 59) {
                    // Handler
                    handler = new Handler();

                    // Audio Attributes
                    attributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();

                    // Get AudioService that will be used in requestAudioFocus
                    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    // Sound Pool
                    soundPool = new SoundPool.Builder().setAudioAttributes(attributes).build();
                    soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                        @Override
                        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                            // Request AudioFocus first
                            int requestAudioFocusResult = audioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_NOTIFICATION, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

                            if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                                soundPool.play(soundId, 1, 1, 1,0,1);
                                handler.postDelayed(beepDelayerRunnable, 3000);
                            } else if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                                attributes = null;
                                soundPool.release();
                            }
                        }
                    });
                }

                if (time.minute == 0 || time.minute == 30) {
                    soundId = soundPool.load(getApplicationContext(), R.raw.drum, 1);
                }
            }
        };
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);

        handler.removeCallbacks(beepDelayerRunnable);
        handler = null;
        if (soundPool != null) {
            soundPool.release();
        }

        attributes = null;
        audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        soundId = 0;
    }

    public AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    soundPool.release();
                    handler.removeCallbacks(beepDelayerRunnable);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    soundPool.release();
                    handler.removeCallbacks(beepDelayerRunnable);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    float lowerVolume = (float) 0.4;
                    soundPool.setVolume(soundId, lowerVolume, lowerVolume);
                    break;
            }
        }
    };

    private Runnable beepDelayerRunnable = new Runnable() {
        @Override
        public void run() {
            attributes = null;
            audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
            soundPool.release();
            soundPool = null;
            soundId = 0;
            handler.removeCallbacks(this);
            handler = null;
        }
    };
}
