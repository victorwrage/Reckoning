package com.zdv.reckoning.customView;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Info:
 * Created by xiaoyl
 * 创建时间:2017/6/1 10:02
 */

public class EllipsizeTextView extends TextView {
    public EllipsizeTextView(Context context) {
        super(context);
    }

    public EllipsizeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EllipsizeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EllipsizeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        if (focused)
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public void onWindowFocusChanged(boolean focused) {
        if (focused)
            super.onWindowFocusChanged(focused);
    }

    @Override
    public boolean isFocused() {
        return true;//一直返回true，假装这个控件一直获取着焦点
    }
}
