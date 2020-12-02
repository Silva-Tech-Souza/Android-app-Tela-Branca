package br.com.lucassouza.ntech.telabranca;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.MotionEvent;

public class TelaBrancaView extends View {



    // Enumeration for Mode
    public enum Mode {
        DRAW,
        TEXT,
        ERASER;
    }

    // Enumeration for Drawer
    public enum Drawer {
        PEN,
        LINE,
        RECTANGLE,
        CIRCLE,
        ELLIPSE,
        QUADRATIC_BEZIER,
        QUBIC_BEZIER;
    }

    private Context context = null;
    private Canvas canvas   = null;
    private Bitmap bitmap   = null;

    private List<Path>  pathLists  = new ArrayList<Path>();
    private List<Paint> paintLists = new ArrayList<Paint>();

    // for Eraser
    private int baseColor = Color.WHITE;

    // for Undo, Redo
    private int historyPointer = 0;

    // Flags
    private Mode mode      = Mode.DRAW;
    private Drawer drawer  = Drawer.PEN;
    private boolean isDown = false;

    // for Paint
    private Paint.Style paintStyle = Paint.Style.STROKE;
    public int paintStrokeColor   = Color.BLACK;
    public int paintFillColor     = Color.BLACK;
    public float paintStrokeWidth = 3F;
    private int opacity            = 255;
    private float blur             = 0F;
    private Paint.Cap lineCap      = Paint.Cap.ROUND;

    // for Text
    private String text           = "";
    private Typeface fontFamily   = Typeface.DEFAULT;
    private float fontSize        = 32F;
    private Paint.Align textAlign = Paint.Align.RIGHT;  // fixed
    private Paint textPaint       = new Paint();
    private float textX           = 0F;
    private float textY           = 0F;

    // for Drawer
    private float startX   = 0F;
    private float startY   = 0F;
    private float controlX = 0F;
    private float controlY = 0F;

    public TelaBrancaView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setup(context);
    }


    public TelaBrancaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setup(context);
    }


    public TelaBrancaView(Context context) {
        super(context);
        this.setup(context);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void setup(Context context) {
        this.context = context;


        this.pathLists.add(new Path());
        this.paintLists.add(this.createPaint());
        this.historyPointer++;

        this.textPaint.setARGB(0, 255, 255, 255);
    }


    private Paint createPaint() {
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setStyle(this.paintStyle);
        paint.setStrokeWidth(this.paintStrokeWidth);
        paint.setStrokeCap(this.lineCap);
        paint.setStrokeJoin(Paint.Join.ROUND);  // fixed

            // Otherwise
            paint.setColor(this.paintStrokeColor);
            //paint.setShadowLayer(this.blur, 0F, 0F, this.paintStrokeColor);
            paint.setAlpha(this.opacity);
            paint.setPathEffect(new CornerPathEffect(10));



        return paint;
    }

    private Path createPath(MotionEvent event) {
        Path path = new Path();

        // Save for ACTION_MOVE
        this.startX = event.getX();
        this.startY = event.getY();


        path.moveTo(this.startX, this.startY);

        return path;
    }


    private void updateHistory(Path path) {
        if (this.historyPointer == this.pathLists.size()) {
            this.pathLists.add(path);
            this.paintLists.add(this.createPaint());
            this.historyPointer++;
        } else {
            // On the way of Undo or Redo
            this.pathLists.set(this.historyPointer, path);
            this.paintLists.set(this.historyPointer, this.createPaint());
            this.historyPointer++;

            for (int i = this.historyPointer, size = this.paintLists.size(); i < size; i++) {
                this.pathLists.remove(this.historyPointer);
                this.paintLists.remove(this.historyPointer);
            }
        }
    }


    private Path getCurrentPath() {
        return this.pathLists.get(this.historyPointer - 1);
    }


    private void drawText(Canvas canvas) {
        if (this.text.length() <= 0) {
            return;
        }

        if (this.mode == Mode.TEXT) {
            this.textX = this.startX;
            this.textY = this.startY;

            this.textPaint = this.createPaint();
        }

        float textX = this.textX;
        float textY = this.textY;

        Paint paintForMeasureText = new Paint();

        // Line break automatically
        float textLength   = paintForMeasureText.measureText(this.text);
        float lengthOfChar = textLength / (float)this.text.length();
        float restWidth    = this.canvas.getWidth() - textX;  // text-align : right
        int numChars       = (lengthOfChar <= 0) ? 1 : (int)Math.floor((double)(restWidth / lengthOfChar));  // The number of characters at 1 line
        int modNumChars    = (numChars < 1) ? 1 : numChars;
        float y            = textY;

        for (int i = 0, len = this.text.length(); i < len; i += modNumChars) {
            String substring = "";

            if ((i + modNumChars) < len) {
                substring = this.text.substring(i, (i + modNumChars));
            } else {
                substring = this.text.substring(i, len);
            }

            y += this.fontSize;

            canvas.drawText(substring, textX, y, this.textPaint);
        }
    }


    private void onActionDown(MotionEvent event) {

        switch (this.mode) {
            case DRAW   :
            case ERASER :
                if ((this.drawer != Drawer.QUADRATIC_BEZIER) && (this.drawer != Drawer.QUBIC_BEZIER)) {

                    this.updateHistory(this.createPath(event));
                    this.isDown = true;
                } else {
                    // Bezier
                    if ((this.startX == 0F) && (this.startY == 0F)) {
                        // The 1st tap
                        this.updateHistory(this.createPath(event));
                    } else {
                        // The 2nd tap
                        this.controlX = event.getX();
                        this.controlY = event.getY();

                        this.isDown = true;
                    }
                }

                break;
            case TEXT   :
                this.startX = event.getX();
                this.startY = event.getY();

                break;
            default :
                break;
        }
    }

    private void onActionMove(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch (this.mode) {
            case DRAW   :
            case ERASER :

                if ((this.drawer != Drawer.QUADRATIC_BEZIER) && (this.drawer != Drawer.QUBIC_BEZIER)) {
                    if (!isDown) {
                        return;
                    }

                    Path path = this.getCurrentPath();

                    switch (this.drawer) {
                        case PEN :
                            path.lineTo(x, y);
                            break;
                        case LINE :
                            path.reset();
                            path.moveTo(this.startX, this.startY);
                            path.lineTo(x, y);
                            break;
                        case RECTANGLE :
                            path.reset();
                            path.addRect(this.startX, this.startY, x, y, Path.Direction.CCW);
                            break;
                        case CIRCLE :
                            double distanceX = Math.abs((double)(this.startX - x));
                            double distanceY = Math.abs((double)(this.startX - y));
                            double radius    = Math.sqrt(Math.pow(distanceX, 2.0) + Math.pow(distanceY, 2.0));

                            path.reset();
                            path.addCircle(this.startX, this.startY, (float)radius, Path.Direction.CCW);
                            break;
                        case ELLIPSE :
                            RectF rect = new RectF(this.startX, this.startY, x, y);

                            path.reset();
                            path.addOval(rect, Path.Direction.CCW);
                            break;
                        default :
                            break;
                    }
                } else {
                    if (!isDown) {
                        return;
                    }

                    Path path = this.getCurrentPath();

                    path.reset();
                    path.moveTo(this.startX, this.startY);
                    path.quadTo(this.controlX, this.controlY, x, y);
                }

                break;
            case TEXT :
                this.startX = x;
                this.startY = y;

                break;
            default :
                break;
        }
    }

    private void onActionUp(MotionEvent event) {

        if (isDown) {
            this.startX = 0F;
            this.startY = 0F;
            this.isDown = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Before "drawPath"
        canvas.drawColor(this.baseColor);

        if (this.bitmap != null) {
            canvas.drawBitmap(this.bitmap, 0F, 0F, new Paint());
        }

        for (int i = 0; i < this.historyPointer; i++) {

            Path path   = this.pathLists.get(i);
            Paint paint = this.paintLists.get(i);

            canvas.drawPath(path, paint);


        }

        this.drawText(canvas);

        this.canvas = canvas;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {



        Log.d("pointer count","pointer count: "+event.getPointerCount());

        if(event.getPointerCount() == 1) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    this.onActionDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    this.onActionMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                    this.onActionUp(event);
                    break;
                default:
                    break;
            }
        }

        // Re draw
        this.invalidate();

        return true;
    }


    public void setMode(Mode mode) {
        this.mode = mode;
    }


    public void setDrawer(Drawer drawer) {
        this.drawer = drawer;
    }


    public boolean undo() {
        if (this.historyPointer > 1) {
            this.historyPointer--;
            this.invalidate();

            return true;
        } else {
            return false;
        }
    }


    public void clear() {
        Path path = new Path();
        path.moveTo(0F, 0F);
        path.addRect(0F, 0F, this.getWidth(), getHeight(), Path.Direction.CCW);
        path.close();

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        this.historyPointer = 0;
        this.paintLists = new ArrayList<Paint>();
        this.pathLists = new ArrayList<Path>();



        this.text = "";

        // Clear
        this.invalidate();

    }


    public void setPaintStyle(Paint.Style style) {
        this.paintStyle = style;
    }


    public void setOpacity(int opacity) {
        if ((opacity >= 0) && (opacity <= 255)) {
            this.opacity = opacity;
        } else {
            this.opacity= 255;
        }
    }


    public void setBlur(float blur) {
        if (blur >= 0) {
            this.blur = blur;
        } else {
            this.blur = 0F;
        }
    }

    public Bitmap getBitmap() {
        this.setDrawingCacheEnabled(false);
        this.setDrawingCacheEnabled(true);

        return Bitmap.createBitmap(this.getDrawingCache());
    }



}