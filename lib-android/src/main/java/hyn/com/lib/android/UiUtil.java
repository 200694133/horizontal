package hyn.com.lib.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import java.io.ByteArrayOutputStream;

/**
 * Created by hanyanan on 2015/2/11.
 */
public class UiUtil {
    /**
     * 修正 WebView 在版本 11 以上的高危漏洞。
     *
     * @param webView 需要修复漏洞的 WebView 。
     */
    public static void fixWebViewBug(WebView webView)
    {
        if (null == webView)
        {
            return;
        }

        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.HONEYCOMB)
        {
            webView.removeJavascriptInterface("searchBoxJavaBridge_");
            webView.getSettings().setAllowFileAccess(false);
        }
    }

    // Get bitmap from specify view.
    public static Bitmap viewToBitmap1(View view) {
        // Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        // Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        // Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            // does not have background drawable, then draw white background on  the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        // return the bitmap
        return returnedBitmap;
    }
    //view to bitmap
    public static Bitmap viewToBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bm = view.getDrawingCache();
        return bm;
    }

//    // build bitmap from xml.将xml转为bitmap
//    public static Bitmap convertBitmapFromXML(Context context, int layoutId, Bitmap bm) {
//        View layout = LayoutInflater.from(context).inflate(layoutId, null);
////        View bitmapView = layout.findViewById(R.id.estatecartlist_item_bitmap);
////        TextView xml_text = (TextView) layout.findViewById(R.id.item_estatecart_tv_name);
////        ImageView image = (ImageView) layout.findViewById(R.id.item_estatecart_iv_main);
////        image.setImageBitmap(bm);
////        xml_text.setText(clusterSize);
//        //set the content to the view, TODO
//
//        layout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//        layout.layout(0, 0, layout.getMeasuredWidth(), layout.getMeasuredHeight());
//
//        final Bitmap clusterBitmap = Bitmap.createBitmap(layout.getMeasuredWidth(), layout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
//
//        Canvas canvas = new Canvas(clusterBitmap);
//        layout.draw(canvas);
//        return clusterBitmap;
//    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static byte[] bitmapToBytes(Bitmap bm){
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, bao);
        return bao.toByteArray();
    }

    public static Bitmap bytesToBitmap(byte[] b){
        if(b.length!=0){
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
    //缩放bitmap，可能会导致失真
    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newBmp;
    }

    //TODO
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;//什么颜色没关系
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
    //获得带倒影的图片
    public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
        final int reflectionGap = 4;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, h / 2, w, h / 2, matrix, false);

        Bitmap bitmapWithReflection = Bitmap.createBitmap(w, (h + h / 2), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapWithReflection);
        canvas.drawBitmap(bitmap, 0, 0, null);
        Paint defaultPaint = new Paint();
        canvas.drawRect(0, h, w, h + reflectionGap, defaultPaint);

        canvas.drawBitmap(reflectionImage, 0, h + reflectionGap, null);

        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
                bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
                0x00ffffff, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        // Set the Transfer mode to be porter duff and destination in
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        // Draw a rectangle using the paint with our linear gradient
        canvas.drawRect(0, h, w, bitmapWithReflection.getHeight() + reflectionGap, paint);

        return bitmapWithReflection;
    }
}
