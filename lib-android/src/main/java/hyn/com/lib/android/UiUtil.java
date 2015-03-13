package hyn.com.lib.android;

import android.os.Build;
import android.webkit.WebView;

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

}
