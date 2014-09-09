package com.adgvcxz.path.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.*;
import com.adgvcxz.path.R;

/**
 * Created by scyoule on 14-9-5.
 */
public class PathGroup extends RelativeLayout {

    public static final int SHOW = 0;

    public static final int ANIM = 1;

    public static final int HIDE = 2;

    public static final int HIDE_COLOR_RED      = 0x9A;
    public static final int HIDE_COLOR_GREEN    = 0xFF;
    public static final int HIDE_COLOR_BLUE     = 0x9A;

    public static final int SHOW_COLOR_RED      = 0xFF;
    public static final int SHOW_COLOR_GREEN    = 0x82;
    public static final int SHOW_COLOR_BLUE     = 0x47;

    private int mMaxRadius;

    private Button mPathBtn;

    private int mCenterX, mCenterY;

    private Paint mPaint;

    private Animation mRotateAnim;

    private Animation mScaleAnim;

    private Animation mScaleAnim1;

    private int mCurrentRadius;

    private int mPerRadius;

    private int mStatus;

    private GridView mGridView;

    private int mNumber;

    private float mRed, mGreen, mBlue;

    private float mRedPer, mGreenPer, mBluePer;

    private OnItemClickListener mListener;

    public PathGroup(Context context) {
        super(context);
        mNumber = 3;
        init();
    }

    public PathGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PathGroup);
        init();
        mNumber = array.getInteger(R.styleable.PathGroup_numColumns, 3);
    }

    private void init() {
        mStatus = HIDE;
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mCenterX = (int) (mPathBtn.getX() + mPathBtn.getWidth() / 2);
                mCenterY = (int) (mPathBtn.getY() + mPathBtn.getHeight() / 2);
                mMaxRadius = mCenterY;
                mCurrentRadius = Math.min(mPathBtn.getWidth() / 2, mPathBtn.getHeight() / 2);
            }
        });
        setWillNotDraw(false);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mRed = HIDE_COLOR_RED;
        mGreen = HIDE_COLOR_GREEN;
        mBlue = HIDE_COLOR_BLUE;
        mPaint.setColor(Color.argb(0x99, (int) mRed, (int) mGreen, (int) mBlue));
        mRotateAnim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        mScaleAnim = AnimationUtils.loadAnimation(getContext(), R.anim.scale_show);
        mScaleAnim1 = AnimationUtils.loadAnimation(getContext(), R.anim.scale_hide);
        mScaleAnim1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mGridView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mGridView = (GridView) findViewById(R.id.gridView);
        mGridView.setNumColumns(mNumber);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mListener != null) {
                    mListener.onItemClick(i);
                }
            }
        });
        mPathBtn = (Button) findViewById(R.id.path_btn);
        mPathBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mPathBtn.startAnimation(mRotateAnim);
                if (mStatus == HIDE) {
                    startViewGroupAnimOpen();
                } else if (mStatus == SHOW){
                    stopViewGroupAnimClose();
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mCenterX, mCenterY, mCurrentRadius, mPaint);
    }

    private void startViewGroupAnimOpen() {
        mPerRadius = mMaxRadius / 50;
        mRedPer = (SHOW_COLOR_RED - HIDE_COLOR_RED) / 60;
        mGreenPer = (SHOW_COLOR_GREEN - HIDE_COLOR_GREEN) / 60;
        mBluePer = (SHOW_COLOR_BLUE - HIDE_COLOR_BLUE) / 60;
        mGridView.setVisibility(View.VISIBLE);
        LayoutAnimationController controller = new LayoutAnimationController(mScaleAnim, 0.2f);
        controller.setOrder(LayoutAnimationController.ORDER_REVERSE);
        mGridView.setLayoutAnimation(controller);
        new AnimThread().start();
    }

    public boolean getIsShow() {
        return mStatus == SHOW;
    }

    private void stopViewGroupAnimClose() {
        mPerRadius = -mMaxRadius / 50;
        mRedPer = (HIDE_COLOR_RED - SHOW_COLOR_RED) / (float) 60;
        mGreenPer = (HIDE_COLOR_GREEN - SHOW_COLOR_GREEN) / (float) 60;
        mBluePer = (HIDE_COLOR_BLUE - SHOW_COLOR_BLUE) / (float) 60;
        int count = mGridView.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mGridView.getChildAt(i);
            view.startAnimation(mScaleAnim1);
        }
        new AnimThread().start();
    }

    public void setAdapter(BaseAdapter adapter) {
        mGridView.setAdapter(adapter);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            invalidate();
        }
    };


    class AnimThread extends Thread {
        @Override
        public void run() {
            super.run();
            mStatus = ANIM;
            for (int i = 0; i < 60; i++) {
                mCurrentRadius += mPerRadius;
                mRed += mRedPer;
                mGreen += mGreenPer;
                mBlue += mBluePer;
                mPaint.setColor(Color.argb(0x99, (int) mRed, (int) mGreen, (int) mBlue));
                mHandler.sendEmptyMessage(0);
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mPerRadius > 0) {
                mStatus = SHOW;
                mRed = SHOW_COLOR_RED;
                mGreen = SHOW_COLOR_GREEN;
                mBlue = SHOW_COLOR_BLUE;
            } else {
                mStatus = HIDE;
                mRed = HIDE_COLOR_RED;
                mGreen = HIDE_COLOR_GREEN;
                mBlue = HIDE_COLOR_BLUE;
            }
            mHandler.sendEmptyMessage(0);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        public void onItemClick(int index);
    }
}
