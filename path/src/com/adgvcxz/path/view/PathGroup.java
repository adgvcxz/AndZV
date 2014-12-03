package com.adgvcxz.path.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.*;
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

    private int mMaxRadius, mMinRadius;

    private PathImage mPathBtn;

    private int mCenterX, mCenterY;

    private Paint mPaint;

    private Animation mRotateAnimShow, mRotateAnimHide;

    private Animation mScaleAnimShow;

    private Animation mScaleAnimHide;

    private int mCurrentRadius;

    private int mStatus;

    private GridView mGridView;

    private int mNumber;

    private int mRightMargin, mBottomMargin;

    private OnPathGroupListener mListener;

    private boolean mIsLongClick;

    private boolean mIsCanDrag;

    private boolean mItemClick;

    private CircleAnim mCircleAnim;

    private boolean mShowBackCircle;

    private int mIndex;

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
        mShowBackCircle = false;
        float density = getResources().getDisplayMetrics().density;
        mNumber = 3;
        mBottomMargin = (int) (40 * density);
        mRightMargin = (int) (40 * density);
        mShowColor = Color.parseColor("#99FF8247");
        mHideColor = Color.parseColor("#999AFF9A");
        mDragColor = Color.parseColor("#990080FF");
        mIsCanDrag = true;
        mCircleAnim = new CircleAnim();
        mCircleAnim.setInterpolator(new AccelerateInterpolator());
        mCircleAnim.setDuration(200);
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
        mPathBtn = new PathImage(getContext());
        mPathBtn.setShow(true);
        mPathBtn.setImageResource(R.drawable.plus);
        LayoutParams btnLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        btnLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        btnLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        addView(mPathBtn, btnLp);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mListener != null) {
                    mIndex = i;
                    mListener.onItemClick(i);
                }
            }
        });
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT > 16) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                mCenterX = (int) (mPathBtn.getX() + mPathBtn.getWidth() / 2);
                mCenterY = (int) (mPathBtn.getY() + mPathBtn.getHeight() / 2);
                mCurrentRadius = Math.min(mPathBtn.getWidth() / 2, mPathBtn.getHeight() / 2);
                mMinRadius = mCurrentRadius;
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
                    mItemClick = false;
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
                    mPathBtn.invalidate();
                }
                mItemClick = false;
                return false;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mShowBackCircle) {
            canvas.drawCircle(mCenterX, mCenterY, mCurrentRadius, mPaint);
        }
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
                        lp.bottomMargin = 0;
                    }
                    if (lp.rightMargin < 0) {
                        lp.rightMargin = 0;
                    }
                    if (lp.rightMargin > getWidth() - mPathBtn.getHeight()) {
                        lp.rightMargin = getWidth() - mPathBtn.getWidth();
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
                    mPathBtn.invalidate();
                    calculateMaxRadius();
                    invalidate();
                }
                break;
        }
        lastX = ev.getX();
        lastY = ev.getY();
        return super.dispatchTouchEvent(ev);
    }

    public void clickItem(int index) {
        mItemClick = true;
        close();
    }

    public void close() {
        mStatus = HIDE_ANIM;
        mPathBtn.startAnimation(mRotateAnimHide);
        stopViewGroupAnimClose();
    }

    private void startViewGroupAnimOpen() {
        mGridView.setVisibility(View.VISIBLE);
        LayoutAnimationController controller = new LayoutAnimationController(mScaleAnimShow, 0.2f);
        controller.setOrder(LayoutAnimationController.ORDER_REVERSE);
        mGridView.setLayoutAnimation(controller);
        startAnimation(mCircleAnim);
    }

    public boolean getIsShow() {
        return mStatus == SHOW;
    }

    private void stopViewGroupAnimClose() {
        int count = mGridView.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mGridView.getChildAt(i);
            view.startAnimation(mScaleAnimHide);
        }
        startAnimation(mCircleAnim);
    }

    private int calculateDistance(int x, int y, int dstX, int dstY) {
        return (int) Math.sqrt((x - dstX) * (x - dstX) + (y - dstY) * (y - dstY));
    }

    /**
     * 计算和四个角的距离最大值
     */
    private void calculateMaxRadius() {
        int x = (int) mPathBtn.getX();
        int y = (int) mPathBtn.getY();
        int radius1 = calculateDistance(x, y, 0, 0);
        int radius2 = calculateDistance(x, y, getWidth(), 0);
        int radius3 = calculateDistance(x, y, 0, getHeight());
        int radius4 = calculateDistance(x, y, getWidth(), getHeight());
        int result = Math.max(radius1, Math.max(radius2, Math.max(radius3, radius4)));
        mMaxRadius = result + mCurrentRadius + mPathBtn.getWidth();
    }

    public void setAdapter(BaseAdapter adapter) {
        mGridView.setAdapter(adapter);
    }


    public void showPathBtn() {
        ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(mPathBtn, "scaleX", 0f, 1f);
        ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(mPathBtn, "scaleY", 0f, 1f);
        ObjectAnimator animatorRotation = ObjectAnimator.ofFloat(mPathBtn, "rotation", 0f, 720f);
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setDuration(400);
        animationSet.playTogether(animatorScaleX, animatorScaleY, animatorRotation);
        animationSet.setInterpolator(new AnticipateOvershootInterpolator());
        animationSet.start();
    }

    public void hidePathBtn() {
        ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(mPathBtn, "scaleX", 1f, 0f);
        ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(mPathBtn, "scaleY", 1f, 0f);
        ObjectAnimator animatorRotation = ObjectAnimator.ofFloat(mPathBtn, "rotation", 720f, 0f);
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setDuration(400);
        animationSet.playTogether(animatorScaleX, animatorScaleY, animatorRotation);
        animationSet.setInterpolator(new AnticipateInterpolator());
        animationSet.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (oldh < h) {
            showPathBtn();
        } else {
            hidePathBtn();
        }
    }

    class CircleAnim extends Animation {

        public CircleAnim() {
            setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mShowBackCircle = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mStatus == SHOW_ANIM) {
                        mStatus = SHOW;
                        mPaint.setColor(mShowColor);
                        mPathBtn.invalidate();
                    } else {
                        mShowBackCircle = false;
                        mStatus = HIDE;
                        mPaint.setColor(mHideColor);
                        mPathBtn.invalidate();
                        if (mListener != null && mItemClick) {
                            mListener.onPathHide(mIndex);
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            switch (mStatus) {
                case HIDE_ANIM:
                    if (interpolatedTime > 0.5) {
                        mPaint.setColor(mHideColor);
                        mPathBtn.setShow(true);
                        mPathBtn.invalidate();
                    }
                    mCurrentRadius = (int) (mMaxRadius - (mMaxRadius - mMinRadius) * interpolatedTime);
                    break;
                case SHOW_ANIM:
                    if (interpolatedTime > 0.5) {
                        mPaint.setColor(mShowColor);
                        mPathBtn.setShow(false);
                        mPathBtn.invalidate();
                    }
                    mCurrentRadius = (int) (mMinRadius + (mMaxRadius - mMinRadius) * interpolatedTime);
                    break;
            }
            invalidate();
        }
    }

    public void setOnPathGroupListener(OnPathGroupListener listener) {
        mListener = listener;
    }

    public interface OnPathGroupListener {
        public void onItemClick(int index);

        public void onPathHide(int index);
    }


    class PathImage extends ImageView {

        private boolean showBtn;


        public PathImage(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (showBtn) {
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 2, mPaint);
            }
            super.onDraw(canvas);
        }

        public void setShow(boolean s) {
            showBtn = s;
        }

    }

}
