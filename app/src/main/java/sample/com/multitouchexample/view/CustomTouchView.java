package sample.com.multitouchexample.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import sample.com.multitouchexample.instances.TouchManager;
import sample.com.multitouchexample.instances.Vector2D;

public class CustomTouchView extends View implements OnTouchListener {
    private static final boolean DEBUG = false;
    private final float squareAngle = (float) Math.PI / 2.0f;
    private Bitmap bitmap;
    private int width;
    private int height;
    private Matrix transform = new Matrix();

    private Vector2D position = new Vector2D();
    private float scale = 1;
    private float angle = 0;

    private TouchManager touchManager = new TouchManager(2);
    private boolean isInitialized = false;
    private boolean rotateEnable = false;

    // Debug helpers to draw lines between the two touch points
    private Vector2D vca = null;
    private Vector2D vcb = null;
    private Vector2D vpa = null;
    private Vector2D vpb = null;
    private GestureDetector gestureDetector;
    private AnimationAsyncTask animationAsyncTask;

    private float bitmapLeft;
    private float bitmapTop;
    private float bitmapRight;
    private float bitmapBottom;
    private boolean isBringToFront;

    private Paint debugPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    public CustomTouchView(Context context, Bitmap bitmap) {
        super(context);
        
        this.bitmap = bitmap;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();

        setOnTouchListener(this);
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void initialized(float scale, float left, float top, float width, float height, float angle) {
        position.set((left + width / 2) * scale, (top + height / 2) * scale);
        float realWidth = width * scale;
        float realHeight = height * scale;

        float bitmapScale = realWidth / (float) bitmap.getWidth();
        if ((float) bitmap.getHeight() * bitmapScale < realHeight) {
            bitmapScale = realHeight / (float) bitmap.getHeight();
        }

        this.scale = bitmapScale;
        this.angle = getRadiansFromDegrees(angle);
        isInitialized = true;
        invalidate();
    }

    public void setPosition(float left, float top, float right, float bottom, float angle, boolean restore) {
        this.angle = getRadiansFromDegrees(angle);
        this.scale = (right - left) / (float) bitmap.getWidth();
        if (Math.round(Math.abs(Math.sin(angle))) == 1 && !restore) {
            this.scale = (right - left) / (float) bitmap.getHeight();
        }

        position = new Vector2D((right + left) / 2, (bottom + top) / 2);
        isInitialized = true;
        invalidate();
    }

    public void restore(float centerX, float centerY, float scale, float angle) {
        position.set(centerX, centerY);
        this.scale = scale;
        this.angle = angle;
        invalidate();
    }

    private static float getDegreesFromRadians(float angle) {
        return (float) (angle * 180.0 / Math.PI);
    }

    public static float getRadiansFromDegrees(float degrees) {
        return (float) (degrees * Math.PI / 180.0f);
    }

    private boolean touchInView(float x, float y) {
        return x < bitmapRight && x > bitmapLeft
                && y < bitmapBottom && y > bitmapTop;
    }

    private void startAnimation(float fromAngle, float toAngle) {

        if (fromAngle == toAngle) {
            return;
        }

        float fromAngleDegree = getDegreesFromRadians(fromAngle);
        float toAngleDegree = getDegreesFromRadians(toAngle);


        if (animationAsyncTask != null && animationAsyncTask.isRunning()) {
            return;
        }

        animationAsyncTask = new AnimationAsyncTask(fromAngleDegree, toAngleDegree);
        animationAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class AnimationAsyncTask extends AsyncTask<Void, Float, Float> {
        private float fromAngleDegree;
        private float endAngleDegree;
        private boolean running = true;

        AnimationAsyncTask(float fromAngleDegree, float endAngleDegree) {
            this.fromAngleDegree = fromAngleDegree;
            this.endAngleDegree = endAngleDegree;
        }

        boolean isRunning() {
            return running;
        }

        void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            running = false;
        }

        @Override
        protected Float doInBackground(Void[] params) {
            float pad;
            if (fromAngleDegree < endAngleDegree) {
                pad = (endAngleDegree - fromAngleDegree) / 6;

                float currentAngleDegree = fromAngleDegree;
                while (running && currentAngleDegree < endAngleDegree) {
                    // update
                    publishProgress(currentAngleDegree);

                    try {
                        Thread.sleep(3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    currentAngleDegree += pad;
                }
            } else {
                float currentAngleDegree = fromAngleDegree;
                pad = (fromAngleDegree - endAngleDegree) / 6;

                while (running && currentAngleDegree > endAngleDegree) {
                    // update
                    publishProgress(currentAngleDegree);

                    try {
                        Thread.sleep(3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    currentAngleDegree -= pad;
                }
            }

            return endAngleDegree;
        }


        @Override
        protected void onProgressUpdate(Float... values) {
            super.onProgressUpdate(values);

            angle = getRadiansFromDegrees(values[0]);
            invalidate();
        }

        @Override
        protected void onPostExecute(Float result) {
            super.onPostExecute(result);
            setRunning(false);

            if (result != null) {
                angle = getRadiansFromDegrees(result);
                invalidate();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setScreenSize(int width, int height) {
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        render(canvas);
    }

    private void render(Canvas canvas) {
        if (!isInitialized) {
            int w = getWidth();
            int h = getHeight();
            position.set(w / 2, h / 2);
            isInitialized = true;
        }

        transform.reset();
        transform.postTranslate(-width / 2.0f, -height / 2.0f);
        transform.postRotate(getDegreesFromRadians(angle));
        transform.postScale(scale, scale);
        transform.postTranslate(position.getX(), position.getY());

        float width = bitmap.getWidth() * scale;
        float height = bitmap.getHeight() * scale;

        if (Math.round(Math.abs(Math.sin(angle))) == 1) {
            width = bitmap.getHeight() * scale;
            height = bitmap.getWidth() * scale;
        }

        // position in parent
        bitmapLeft = position.getX() - width / 2;
        bitmapTop = position.getY() - height / 2;
        bitmapRight = position.getX() + width / 2;
        bitmapBottom = position.getY() + height / 2;
        // draw object

        canvas.drawBitmap(bitmap, transform, debugPaint);

        if (DEBUG) {
            try {
                debugPaint.setColor(0xFF007F00);
                canvas.drawCircle(vca.getX(), vca.getY(), 64, debugPaint);
                debugPaint.setColor(0xFF7F0000);
                canvas.drawCircle(vcb.getX(), vcb.getY(), 64, debugPaint);

                debugPaint.setColor(0xFFFF0000);
                canvas.drawLine(vpa.getX(), vpa.getY(), vpb.getX(), vpb.getY(), debugPaint);
                debugPaint.setColor(0xFF00FF00);
                canvas.drawLine(vca.getX(), vca.getY(), vcb.getX(), vcb.getY(), debugPaint);
            } catch (NullPointerException e) {
                // Just being lazy here...
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int actionCode = event.getAction() & MotionEvent.ACTION_MASK;

        if (actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_POINTER_DOWN) {
            if (!touchInView(event.getX(), event.getY())) {
                return false;
            } else if (isBringToFront) {
                this.bringToFront();
            }
        }

        vca = null;
        vcb = null;
        vpa = null;
        vpb = null;

        try {
            touchManager.update(event);

            if (touchManager.getPressCount() == 1) {
                vca = touchManager.getPoint(0);
                vpa = touchManager.getPreviousPoint(0);
                position.add(touchManager.moveDelta(0));
            } else {
                if (touchManager.getPressCount() == 2) {
                    vca = touchManager.getPoint(0);
                    vpa = touchManager.getPreviousPoint(0);
                    vcb = touchManager.getPoint(1);
                    vpb = touchManager.getPreviousPoint(1);

                    Vector2D current = touchManager.getVector(0, 1);
                    Vector2D previous = touchManager.getPreviousVector(0, 1);
                    float currentDistance = current.getLength();
                    float previousDistance = previous.getLength();

                    if (previousDistance != 0) {
                        scale *= currentDistance / previousDistance;
                    }

                    if (rotateEnable) {
                        angle -= Vector2D.getSignedAngleBetween(current, previous);
                    }
                }
            }

            if (rotateEnable && touchManager.getPressCount() > 0
                    && (actionCode == MotionEvent.ACTION_POINTER_UP || actionCode == MotionEvent.ACTION_UP)) {

                float decimal = angle % squareAngle;
                int result = (int) (angle / squareAngle);
                int value = decimal > 0 ? 1 : -1;

                if (Math.abs(decimal) > (squareAngle / 2.0f)) {
                    startAnimation(angle, (Math.abs(result) + 1) * value * squareAngle);
                } else {
                    startAnimation(angle, (Math.abs(result)) * value * squareAngle);
                }

            }

            invalidate();
        } catch (Throwable t) {
            // So lazy...
        }

        return gestureDetector.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (rotateEnable) {
                startAnimation(angle, angle + squareAngle);
            }
            return true;
        }
    }

    public void setBitmap(Bitmap bitmap) {
        try {
            this.bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.bitmap = bitmap;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        invalidate();

    }

    public float getBitmapLeft() {
        return bitmapLeft;
    }

    public void setBitmapLeft(float bitmapLeft) {
        this.bitmapLeft = bitmapLeft;
    }

    public float getBitmapTop() {
        return bitmapTop;
    }

    public void setBitmapTop(float bitmapTop) {
        this.bitmapTop = bitmapTop;
    }

    public float getBitmapRight() {
        return bitmapRight;
    }

    public int getBitmapWidth() {
        return bitmap.getWidth();
    }

    public int getBitmapHeight() {
        return bitmap.getHeight();
    }

    public void setBitmapRight(float bitmapRight) {
        this.bitmapRight = bitmapRight;
    }

    public float getBitmapBottom() {
        return bitmapBottom;
    }

    public void setBitmapBottom(float bitmapBottom) {
        this.bitmapBottom = bitmapBottom;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getAngle() {
        return getDegreesFromRadians(angle);
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public void setRotateEnable(boolean rotateEnable) {
        this.rotateEnable = rotateEnable;
    }

    public float getRealBitmapLeft() {
        return bitmapLeft;
    }

    public float getRealBitmapTop() {
        return bitmapTop;
    }

    public float getRealBitmapRight() {
        return bitmapRight;
    }

    public float getRealBitmapBottom() {
        return bitmapBottom;
    }

    public boolean isBringToFront() {
        return isBringToFront;
    }

    public void setBringToFront(boolean bringToFront) {
        isBringToFront = bringToFront;
    }

    public void destroy() {
        try {
            bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
