package com.adgvcxz.path.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.*;
import com.adgvcxz.path.R;

/**
 * Created by adgvcxz on 14-9-5.
 */
public class PathGroup extends RelativeLayout {

    public static final int SHOW = 0;

    public static final int SHOW_ANIM = 1;

    public static final int HIDE_ANIM = 2;

    public static final int HIDE = 3;

    private int mShowColor;

    private int mHideColor;

    private int mDragColor;

    private int mMaxRadius;

    private ImageView mPathBtn;

    private int mCenterX, mCenterY;

    private Paint mPaint;

    private Animation mRotateAnimShow, mRotateAnimHide;

    private Animation mScaleAnimShow;

    private Animation mScaleAnimHide;

    private int mCurrentRadius;

    private int mPerRadius;

    private int mStatus;

    private GridView mGridView;

    private int mNumber;

    private int mRightMargin, mBottomMargin;

    private OnItemClickListener mListener;

    private boolean mIsLongClick;

    private boolean mIsCanDrag;

    public PathGroup(Context context) {
        super(context);
        init();
        initView();
    }

    public PathGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PathGroup);
        mNumber = array.getInteger(R.styleable.PathGroup_numColumns, 3);
        mBottomMargin = array.getDimensionPixelSize(R.styleable.PathGroup_btnBottomMargin, mNumber);
        mRightMargin = array.getDimensionPixelSize(R.styleable.PathGroup_btnRightMargin, mBottomMargin);
        mShowColor = array.getColor(R.styleable.PathGroup_showColor, mShowColor);
        mHideColor = array.getColor(R.styleable.PathGroup_hideColor, mHideColor);
        mIsCanDrag = array.getBoolean(R.styleable.PathGroup_canMove, mIsCanDrag);
        mDragColor = array.getColor(R.styleable.PathGroup_dragColor, mDragColor);
        initView();
    }

    private void init() {
        mStatus = HIDE;
        float density = getResources().getDisplayMetrics().density;
        mNumber = 3;
        mBottomMargin = (int) (40 * density);
        mRightMargin = (int) (40 * density);
        mShowColor = Color.parseColor("#99FF8247");
        mHideColor = Color.parseColor("#999AFF9A");
        mDragColor = Color.parseColor("#990080FF");
        mIsCanDrag = true;
    }

    private void initView() {
        mGridView = new GridView(getContext());
        mGridView.setNumColumns(mNumber);
        mGridView.setGravity(Gravity.CENTER);
        mGridView.setVisibility(View.GONE);
        mGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        LayoutParams gridViewLP = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        gridViewLP.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(mGridView, gridViewLP);
        mPathBtn = new ImageView(getContext());
        mPathBtn.setBackgroundResource(R.drawable.plus);
        LayoutParams btnLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        btnLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        btnLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        btnLp.bottomMargin = mBottomMargin;
        btnLp.rightMargin = mRightMargin;
        addView(mPathBtn, btnLp);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mListener != null) {
                    mListener.onItemClick(i);
                }
            }
        });
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mCenterX = (int) (mPathBtn.getX() + mPathBtn.getWidth() / 2);
                mCenterY = (int) (mPathBtn.getY() + mPathBtn.getHeight() / 2);
                mCurrentRadius = Math.min(mPathBtn.getWidth() / 2, mPathBtn.getHeight() / 2);
                calculateMaxRadius();
            }
        });
        setWillNotDraw(false);
        mPaint = new Paint();
        mPaint.setColor(mHideColor);
        mRotateAnimShow = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_show);
        mRotateAnimHide = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_hide);
        mScaleAnimShow = AnimationUtils.loadAnimation(getContext(), R.anim.scale_show);
        mScaleAnimHide = AnimationUtils.loadAnimation(getContext(), R.anim.scale_hide);
        mScaleAnimHide.setAnimationListener(new Animation.AnimationListener() {
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
        initPathBtn();
    }

    private void initPathBtn() {
        mPathBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsLongClick) {
                    if (mStatus == HIDE) {
                        mStatus = SHOW_ANIM;
                        mPathBtn.startAnimation(mRotateAnimShow);
                        startViewGroupAnimOpen();
                    } else if (mStatus == SHOW) {
                        mStatus = HIDE_ANIM;
                        mPathBtn.startAnimation(mRotateAnimHide);
                        stopViewGroupAnimClose();
                    }
                } else {
                    mIsLongClick = false;
                }
            }
        });

        mPathBtn.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mIsCanDrag && mStatus == HIDE) {
                    mIsLongClick = true;
                    Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(100);
                    mPaint.setColor(mDragColor);
                }
                return false;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mCenterX, mCenterY, mCurrentRadius, mPaint);
    }
    float lastX, lastY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsLongClick = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsLongClick) {
                    int dx = (int) (ev.getX() - lastX);
                    int dy = (int) (ev.getY() - lastY);
                    LayoutParams lp = (LayoutParams) mPathBtn.getLayoutParams();
                    lp.rightMargin -= dx;
                    lp.bottomMargin -= dy;
                    if (lp.bottomMargin > getHeight() - mPathBtn.getHeight()) {
                        lp.bottomMargin = getHeight() - mPathBtn.getHeight();
                    }
                    if (lp.bottomMargin < 0) {
                        lp.bottomMargin =  0;
                    }
                    if (lp.rightMargin < 0) {
                        lp.rightMargin = 0;
                    }
                    if (lp.leftMargin > getWidth() - mPathBtn.getHeight()) {
                        lp.leftMargin = getWidth() - mPathBtn.getWidth();
                    }
                    mCenterX = getWidth() - lp.rightMargin - mPathBtn.getWidth() / 2;
                    mCenterY = getHeight() - lp.bottomMargin - mPathBtn.getHeight() / 2;
                    invalidate();
                    mPathBtn.setLayoutParams(lp);

                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsLongClick) {
                    mPaint.setColor(mHideColor);
                    calculateMaxRadius();
                    invalidate();
                }
                break;
        }
        lastX = ev.getX();
        lastY = ev.getY();
        return super.dispatchTouchEvent(ev);
    }

    private void startViewGroupAnimOpen() {
        mPerRadius = mMaxRadius / 50;
        mGridView.setVisibility(View.VISIBLE);
        LayoutAnimationController controller = new LayoutAnimationController(mScaleAnimShow, 0.2f);
        controller.setOrder(LayoutAnimationController.ORDER_REVERSE);
        mGridView.setLayoutAnimation(controller);
        new AnimThread().start();
    }

    public boolean getIsShow() {
        return mStatus == SHOW;
    }

    private void stopViewGroupAnimClose() {
        mPerRadius = -mMaxRadius / 50;
        int count = mGridView.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mGridView.getChildAt(i);
            view.startAnimation(mScaleAnimHide);
        }
        new AnimThread().start();
    }

    private void calculateMaxRadius() {

        int maxX = (int) Math.max(getWidth() - mPathBtn.getX(), mPathBtn.getX());
        int maxY = (int) Math.max(getHeight() - mPathBtn.getY(), mPathBtn.getY());
        mMaxRadius = Math.max(maxX, maxY) + mCurrentRadius;
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
            for (int i = 0; i < 60; i++) {
                mCurrentRadius += mPerRadius;
                if (i == 30) {
                    if (mStatus == HIDE_ANIM) {
                        mPaint.setColor(mHideColor);
                    } else if (mStatus == SHOW_ANIM) {
                        mPaint.setColor(mShowColor);
                    }
                }
                mHandler.sendEmptyMessage(0);
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mPerRadius > 0) {
                mStatus = SHOW;
                mPaint.setColor(mShowColor);
            } else {
                mStatus = HIDE;
                mPaint.setColor(mHideColor);
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
