package cn.leo.audiovirtualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Leo on 2018/6/13.
 */

public class AudioVirtualizer extends View implements Visualizer.OnDataCaptureListener {
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int[] mWave;
    private int[] mModel;
    private Visualizer mVisualizer;
    private int mWidth;
    private int mHeight;
    private int mHeightStep;


    public AudioVirtualizer(Context context) {
        this(context, null);
    }

    public AudioVirtualizer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioVirtualizer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint.setStyle(Paint.Style.FILL);
    }

    //绑定媒体播放器
    public void bindMediaPlayer(MediaPlayer mediaPlayer) {
        initVisualizer(mediaPlayer.getAudioSessionId());
    }

    private void initVisualizer(int audioSession) {
        mVisualizer = new Visualizer(audioSession);
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
        mVisualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate() / 2,
                true, true);
        startVisualizer();
    }

    private void startVisualizer() {
        if (mVisualizer != null)
            mVisualizer.setEnabled(true);
    }

    private void stopVisualizer() {
        if (mVisualizer != null)
            mVisualizer.setEnabled(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startVisualizer();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopVisualizer();
    }


    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
        if (mWave == null) {
            mWave = new int[waveform.length / 2 + 1];
        }
        mWave[0] = Math.abs(waveform[1]);
        int j = 1;
        for (int i = 2; i < waveform.length / 2 + 1; ) {
            mWave[j] = (int) Math.hypot(waveform[i], waveform[i + 1]);
            i += 2;
            j++;
        }
    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        if (mModel == null) {
            mModel = new int[18];
        }
        mModel[0] = Math.abs(fft[1]);
        int j = 1;
        for (int i = 2; i < 36; ) {
            mModel[j] = (int) Math.hypot(fft[i], fft[i + 1]);
            i += 2;
            j++;
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();
            mHeightStep = mHeight / Byte.MAX_VALUE;
            int[] colors = new int[]{Color.RED, Color.GREEN};
            LinearGradient linearGradient = new LinearGradient(0, 0, mWidth,
                    mHeight, colors, null, Shader.TileMode.CLAMP);
            mPaint.setShader(linearGradient);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mModel == null) return;
        //drawWave(canvas);
        drawPillar(canvas);
    }

    private void drawWave(Canvas canvas) {
        float widthStep = mWidth / mWave.length;
        Path path = new Path();
        path.moveTo(0, mHeight);
        for (int i = 0; i < mWave.length; i++) {
            path.lineTo(i * widthStep, mHeight - mWave[i] * (mHeightStep / 4));
        }
        path.lineTo(mWidth, mHeight);
        path.close();
        canvas.drawPath(path, mPaint);
    }

    private void drawPillar(Canvas canvas) {
        float widthStep = mWidth / mModel.length;
        for (int i = 0; i < mModel.length; i++) {
            float left = i * widthStep;
            float top = mHeight - (mModel[i] * mHeightStep);
            float right = left + widthStep;
            float bottom = mHeight;
            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }
}
