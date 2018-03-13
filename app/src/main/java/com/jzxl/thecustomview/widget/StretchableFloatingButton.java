package com.jzxl.thecustomview.widget;import android.animation.Animator;import android.animation.AnimatorListenerAdapter;import android.animation.AnimatorSet;import android.animation.ValueAnimator;import android.content.Context;import android.content.res.TypedArray;import android.graphics.Bitmap;import android.graphics.BitmapFactory;import android.graphics.Canvas;import android.graphics.Color;import android.graphics.Paint;import android.graphics.RectF;import android.graphics.drawable.BitmapDrawable;import android.util.AttributeSet;import android.util.Log;import android.view.MotionEvent;import android.view.View;import android.view.ViewGroup;import android.view.animation.LinearInterpolator;import android.widget.TextView;import com.jzxl.thecustomview.R;/** * @author wangyongyong * @date * @Description */public class StretchableFloatingButton extends ViewGroup {    private static final String TAG = "StretchableFloatingButt";    private String text = "";     //文本    private int bacColor;       //控件背景色    private int circleleColor;  //小圆颜色    private int textColor;      //文本颜色    private float textSize;   //文本大小    private float speed;    //拉伸速度    private float degrees;  //旋转度数    private Bitmap openIcon, closeIcon;    private float iconWidth;  //图片宽度    private float y = 20;    //圆环宽度    private float y_x;      //圆环宽度比    private float width;    private float height;    private float center; // 圆的半径    private float x; // 矩形左边的x轴    private View child;     //textview    private int tWidth; // 文本宽度    private int tHeight; // 文本高度    private float tX;       //文本宽度变化值    private float tX_x;     //文本拉伸变化比    private float d_x;       //旋转比    private float rotateDegrees;    //旋转差值    private Paint paint;    private RectF mRectF;    public StretchableFloatingButton(Context context) {        this(context, null);    }    public StretchableFloatingButton(Context context, AttributeSet attrs) {        this(context, attrs, 0);    }    public StretchableFloatingButton(Context context, AttributeSet attrs, int defStyleAttr) {        super(context, attrs, defStyleAttr);        TypedArray type = context.obtainStyledAttributes(attrs, R.styleable.FloatingButton);        bacColor = type.getColor(R.styleable.FloatingButton_bac_color, Color.YELLOW);        circleleColor = type.getColor(R.styleable.FloatingButton_inner_circle_color, Color.BLACK);        textColor = type.getColor(R.styleable.FloatingButton_text_color, Color.BLACK);        textSize = type.getFloat(R.styleable.FloatingButton_text_size, 20);        text = type.getString(R.styleable.FloatingButton_text);        speed = type.getFloat(R.styleable.FloatingButton_speed, 80);        degrees = type.getFloat(R.styleable.FloatingButton_degrees, 90);        BitmapDrawable dra = (BitmapDrawable) type.getDrawable(R.styleable.FloatingButton_open_icon);        openIcon = dra != null ? dra.getBitmap() : BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);        BitmapDrawable dra2 = (BitmapDrawable) type.getDrawable(R.styleable.FloatingButton_close_icon);        closeIcon = dra2 != null ? dra2.getBitmap() : BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);        type.recycle();        init(context);    }    private void init(Context context) {        paint = new Paint(Paint.ANTI_ALIAS_FLAG);        paint.setColor(Color.WHITE);        TextView tv = new TextView(context);        tv.setText(text);        tv.setTextSize(16);        addView(tv);    }    @Override    protected void onLayout(boolean changed, int l, int t, int r, int b) {        child.layout((int) (center * 2 + 5), (int) (center - tHeight / 2), (int) tX, (int) (center + tHeight));    }    @Override    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {        if (width == 0) {            width = MeasureSpec.getSize(widthMeasureSpec);            height = MeasureSpec.getSize(heightMeasureSpec);            // 获取圆的半径            center = height / 2;            // 矩形左边的x轴            x = width - center;            // 初始化圆环宽度比            y_x = y / x;            // 获取TextView控件            child = this.getChildAt(0);            measureChild(child, widthMeasureSpec, heightMeasureSpec);            tWidth = child.getMeasuredWidth();            tHeight = child.getMeasuredHeight();            //初始化文本范围右下角 x坐标  +10设置间距            tX = tWidth + center * 2 + 10;            //初始文本伸缩比            tX_x = tX / (width - center * 2);            iconWidth = center - y - 5;            openIcon = MeasureUtil.zoomImg(openIcon, (int) iconWidth, (int) iconWidth);            closeIcon = MeasureUtil.zoomImg(closeIcon, (int) iconWidth, (int) iconWidth);            rotateDegrees = degrees;            //初始化旋转比            d_x = rotateDegrees / x;        }        super.onMeasure(widthMeasureSpec, heightMeasureSpec);    }    @Override    protected void dispatchDraw(Canvas canvas) {        paint.setColor(bacColor);        // 画左边圆        canvas.drawCircle(center, center, center, paint);        // 画矩形        mRectF = new RectF(center, 0, x, height);        canvas.drawRect(mRectF, paint);        // 画右圆        canvas.drawCircle(x, center, center, paint);        // 画小圆        paint.setColor(Color.BLACK);        canvas.drawCircle(center, center, center - y + 2, paint);        canvas.save();        canvas.rotate(-rotateDegrees, center, center);        Log.e(TAG, "dispatchDraw: " + (-degrees + rotateDegrees) + "-" + degrees + "-" + rotateDegrees);        if (rotateDegrees == 0) {  //旋转完变换图片            canvas.drawBitmap(closeIcon, center - iconWidth / 2, center - iconWidth / 2, paint);        } else {            canvas.drawBitmap(openIcon, center - iconWidth / 2, center - iconWidth / 2, paint);        }        canvas.restore();        super.dispatchDraw(canvas);    }    private ValueAnimator animator;    private ValueAnimator animator1;    private ValueAnimator animator2;    private AnimatorSet set;    private boolean mIsIncrease = true;    public void startScroll() {        startAnimator(mIsIncrease);    }    private void startAnimator(boolean isIncrease) {        if (set != null && set.isRunning()) {            return;        } else {            if (isIncrease) { // 如果展开，则收缩                animator = ValueAnimator.ofFloat(x, center);                animator.setInterpolator(new LinearInterpolator());                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {                    @Override                    public void onAnimationUpdate(ValueAnimator valueAnimator) {                        float mSweepAngle = (float) valueAnimator.getAnimatedValue();                        x = mSweepAngle;                        y = y_x * x;//                        tX = tX_x * x;                        mIsIncrease = false;                        requestLayout();                        invalidate();                    }                });                animator1 = ValueAnimator.ofFloat(width, 0);                animator1.setInterpolator(new LinearInterpolator());                animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {                    @Override                    public void onAnimationUpdate(ValueAnimator valueAnimator) {                        float mSweepAngle = (float) valueAnimator.getAnimatedValue();                        tX = tX_x * (mSweepAngle - center * 2);                    }                });                animator2 = ValueAnimator.ofFloat(x, 0);                animator2.setInterpolator(new LinearInterpolator());                animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {                    @Override                    public void onAnimationUpdate(ValueAnimator valueAnimator) {                        float mSweepAngle = (float) valueAnimator.getAnimatedValue();                        rotateDegrees = mSweepAngle * d_x;                        Log.e(TAG, "onAnimationUpdate: " + rotateDegrees);                    }                });            } else { // 如果收缩，则展开                animator = ValueAnimator.ofFloat(center, width - center);                animator.setInterpolator(new LinearInterpolator());                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {                    @Override                    public void onAnimationUpdate(ValueAnimator valueAnimator) {                        float mSweepAngle = (float) valueAnimator.getAnimatedValue();                        x = mSweepAngle;                        y = y_x * x;//                        tX = tX_x * x;                        mIsIncrease = true;                        rotateDegrees = d_x * x;                        requestLayout();                        invalidate();                    }                });                animator1 = ValueAnimator.ofFloat(0, width);                animator1.setInterpolator(new LinearInterpolator());                animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {                    @Override                    public void onAnimationUpdate(ValueAnimator valueAnimator) {                        float mSweepAngle = (float) valueAnimator.getAnimatedValue();                        tX = tX_x * (mSweepAngle - center * 2);                    }                });                animator2 = ValueAnimator.ofFloat(0, width - center);                animator2.setInterpolator(new LinearInterpolator());                animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {                    @Override                    public void onAnimationUpdate(ValueAnimator valueAnimator) {                        float mSweepAngle = (float) valueAnimator.getAnimatedValue();                        rotateDegrees = mSweepAngle * d_x;                        Log.e(TAG, "onAnimationUpdate:1 " + rotateDegrees);                    }                });            }            set = new AnimatorSet();            set.playTogether(animator, animator1, animator2);            set.setDuration(300);            set.setInterpolator(new LinearInterpolator());            set.start();            set.addListener(new AnimatorListenerAdapter() {                @Override                public void onAnimationEnd(Animator animation) {                    super.onAnimationEnd(animation);                    clearAnimation();                }            });        }    }    @Override    public boolean onTouchEvent(MotionEvent event) {        switch (event.getAction()) {            case MotionEvent.ACTION_DOWN:                break;            case MotionEvent.ACTION_UP:                if (!judgeCanClick(event.getX(), event.getY())) {                    return false;                }                break;        }        return super.onTouchEvent(event);    }    private boolean judgeCanClick(float x, float y) {        boolean canClick;        if (mIsIncrease) {            if (x < width && y < height) {                canClick = true;            } else {                canClick = false;            }        } else {            if (x < center * 2 && y < center * 2) {  //在圆内                canClick = true;            }else {                canClick = false;            }        }        return canClick;    }}