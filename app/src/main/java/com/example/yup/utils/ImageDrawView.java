package com.example.yup.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ImageDrawView extends androidx.appcompat.widget.AppCompatImageView {

    List<List<Point>> scaledBoxes = new ArrayList<>();
    float ScaleX = 1;
    float ScaleY = 1;
    Context context;
    Bitmap originalBm;
    List<List<Point>> originalBoxes = new ArrayList<>();
    List<String> labels;
    List<Float> scores;
    Bitmap curBitmap;
    ImageDrawView imageDrawView;


    public ImageDrawView(@NonNull Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
        this.context = context;
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // Loop through the scaled boxes and check if in any of those box
                DisplayMetrics displayMetrics = new DisplayMetrics();
                ((Activity) context).getWindowManager().getDefaultDisplay()
                        .getMetrics(displayMetrics);
                int[] viewPosition = new int[2];
                view.getLocationOnScreen(viewPosition);
                Point absTouchPos = new Point(motionEvent.getX(), motionEvent.getY());
                for (int i=0; i<scaledBoxes.size(); ++i)
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                        if (isInside(scaledBoxes.get(i), absTouchPos)) {
                            showMenu(labels.get(i), scaledBoxes.get(i), imageDrawView);
                            return true;
                        }
                return false;
            }
        });
        imageDrawView = this;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        curBitmap = bm;
        super.setImageBitmap(bm);
    }
    public Bitmap getCurBitmap() {return curBitmap;}

    public void setScores(List<Float> scores) {
        this.scores = scores;
    }

    public void drawBitmapWithBoundingBoxes(Bitmap bm, List<List<Point>> boxes, Paint paint) {
        this.scaledBoxes.clear();
        this.originalBoxes = boxes;
        Bitmap bm1 = bm.copy(bm.getConfig(),true);
        originalBm = bm.copy(bm.getConfig(),true);
        Canvas canvas = new Canvas(bm1);
        for (int i=0; i<boxes.size(); ++i) {
            List<Point> box = boxes.get(i);
            Path path = new Path();
            path.moveTo(box.get(0).x, box.get(0).y);
            for (int j = 1; j < box.size(); j++) {
                path.lineTo(box.get(j).x, box.get(j).y);
            }
            path.lineTo(box.get(0).x, box.get(0).y);
            canvas.drawPath(path, paint);
        }
//        height = this.getHeight();
//        if (this.getWidth() != bm1.getWidth())
//            ScaleX = (float)1.0*this.getWidth()/bm1.getWidth();
//        if (this.getHeight() != bm1.getHeight())
//            ScaleY = (float)1.0*this.getHeight()/bm1.getHeight();
//        if (ScaleX == 0)
//            ScaleX = 1;
//        if (ScaleY == 0) {
//            ScaleY = 1;
//            height = bm1.getHeight();
//        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);
        float dpWidth = (displayMetrics.widthPixels / displayMetrics.density);
        int pxWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpWidth, displayMetrics);

        ScaleX = (float)1.0*pxWidth/bm1.getWidth();
        ScaleY = ScaleX;

        this.setImageBitmap(scaleBitmapWidth(bm1, (int)(ScaleX * bm1.getWidth())));

        for (int i = 0;i<boxes.size();++i) {
            List<Point> box = boxes.get(i);
            List<Point> scaledBox = new ArrayList<>();
            for (int j=0; j<box.size(); ++j)
                scaledBox.add(new Point((float)ScaleX*box.get(j).x,(float)ScaleY*box.get(j).y));
            scaledBoxes.add(scaledBox);
        }
    }
    public static Bitmap scaleBitmapWidth(Bitmap bitmap, int width) {
        return scaleBitmap(bitmap, width, (width * bitmap.getHeight())
                / bitmap.getWidth());
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int width, int height) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, width, height,
                false);
        return newBitmap;
    }
    static boolean onSegment(Point p, Point q, Point r)
    {
        if (q.x <= Math.max(p.x, r.x) &&
                q.x >= Math.min(p.x, r.x) &&
                q.y <= Math.max(p.y, r.y) &&
                q.y >= Math.min(p.y, r.y))
        {
            return true;
        }
        return false;
    }

    // To find orientation of ordered triplet (p, q, r).
    // The function returns following values
    // 0 --> p, q and r are collinear
    // 1 --> Clockwise
    // 2 --> Counterclockwise
    static int orientation(Point p, Point q, Point r)
    {
        int val = ((int) q.y - (int) p.y) * ((int) r.x - (int) q.x)
                - ((int) q.x - (int) p.x) * ((int) r.y - (int) q.y);

        if (val == 0)
        {
            return 0; // collinear
        }
        return (val > 0) ? 1 : 2; // clock or counter clockwise
    }

    // The function that returns true if
    // line segment 'p1q1' and 'p2q2' intersect.
    static boolean doIntersect(Point p1, Point q1,
                               Point p2, Point q2)
    {
        // Find the four orientations needed for
        // general and special cases
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        // General case
        if (o1 != o2 && o3 != o4)
        {
            return true;
        }

        // Special Cases
        // p1, q1 and p2 are collinear and
        // p2 lies on segment p1q1
        if (o1 == 0 && onSegment(p1, p2, q1))
        {
            return true;
        }

        // p1, q1 and p2 are collinear and
        // q2 lies on segment p1q1
        if (o2 == 0 && onSegment(p1, q2, q1))
        {
            return true;
        }

        // p2, q2 and p1 are collinear and
        // p1 lies on segment p2q2
        if (o3 == 0 && onSegment(p2, p1, q2))
        {
            return true;
        }

        // p2, q2 and q1 are collinear and
        // q1 lies on segment p2q2
        if (o4 == 0 && onSegment(p2, q1, q2))
        {
            return true;
        }

        // Doesn't fall in any of the above cases
        return false;
    }

    // Returns true if the point p lies
    // inside the polygon[] with n vertices
    static Point findMin(List<Point> points) {
        Point min = points.get(0);
        for (Point point : points) {
            if (point.x < min.x) {
                min.x = point.x;
            }
            if (point.y < min.y) {
                min.y = point.y;
            }
        }
        return min;
    }
    static boolean isInside(List<Point> polygon, Point p)
    {
        Point min = findMin(polygon);
        if (p.x < min.x || p.y < min.y) {
            return false;
        }
        int n = polygon.size();
        // There must be at least 3 vertices in polygon[]
        if (n < 3)
        {
            return false;
        }

        // Create a point for line segment from p to infinite
        Point extreme = new Point(9999, p.y);

        // Count intersections of the above line
        // with sides of polygon
        int count = 0, i = 0;
        do
        {
            int next = (i + 1) % n;

            // Check if the line segment from 'p' to
            // 'extreme' intersects with the line
            // segment from 'polygon[i]' to 'polygon[next]'
            if (doIntersect(polygon.get(i), polygon.get(next), p, extreme))
            {
                // If the point 'p' is collinear with line
                // segment 'i-next', then check if it lies
                // on segment. If it lies, return true, otherwise false
                if (orientation(polygon.get(i), p, polygon.get(next)) == 0)
                {
                    return onSegment(polygon.get(i), p,
                            polygon.get(next));
                }

                count++;
            }
            i = next;
        } while (i != 0);

        // Return true if count is odd, false otherwise
        return (count % 2 == 1); // Same as (count%2 == 1)
    }

    private void showMenu(String label, List<Point> box, ImageDrawView parent) {
        ResultDialog resultDialog = new ResultDialog(context, label, box, parent);
        resultDialog.show();
    }

    public Bitmap getFullSizeBitmap(Paint paint) {
        Bitmap bm = originalBm.copy(originalBm.getConfig(),true);
        Canvas canvas = new Canvas(bm);
        for (int i=0; i<originalBoxes.size(); ++i) {
            List<Point> box = originalBoxes.get(i);
            Path path = new Path();
            path.moveTo(box.get(0).x, box.get(0).y);
            for (int j = 1; j < box.size(); j++) {
                path.lineTo(box.get(j).x, box.get(j).y);
            }
            path.lineTo(box.get(0).x, box.get(0).y);
            canvas.drawPath(path, paint);
        }
        return bm;
    }
}
