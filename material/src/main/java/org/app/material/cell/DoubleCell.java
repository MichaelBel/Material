package org.app.material.cell;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.app.material.AndroidUtilities;

public class DoubleCell extends FrameLayout {

    private View listDivider;
    private ImageView iconView;
    private TextView titleTextView;
    private TextView valueTextView;

    public DoubleCell(Context context) {
        super(context);

        iconView = new ImageView(context);
        iconView.setVisibility(INVISIBLE);
        iconView.setScaleType(ImageView.ScaleType.CENTER);
        iconView.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), 0);
        //addView(iconView, LayoutHelper.makeFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.END : Gravity.START) | Gravity.CENTER_VERTICAL));

        titleTextView = new TextView(context);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        titleTextView.setLines(1);
        titleTextView.setMaxLines(1);
        titleTextView.setVisibility(INVISIBLE);
        titleTextView.setSingleLine(true);
        titleTextView.setEllipsize(TextUtils.TruncateAt.END);
        //titleTextView.setGravity((LocaleController.isRTL ? Gravity.END : Gravity.START) | Gravity.CENTER_VERTICAL);
        titleTextView.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(10), AndroidUtilities.dp(16), 0);
        //addView(titleTextView, LayoutHelper.makeFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.END : Gravity.START) | Gravity.TOP));

        valueTextView = new TextView(context);
        valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        valueTextView.setLines(1);
        valueTextView.setVisibility(INVISIBLE);
        valueTextView.setMaxLines(1);
        valueTextView.setSingleLine(true);
        //valueTextView.setGravity(DimenUtil.isRTL ? Gravity.END : Gravity.START);
        valueTextView.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(35), AndroidUtilities.dp(16), AndroidUtilities.dp(10));
        //addView(valueTextView, LayoutHelper.makeFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.END : Gravity.START) | Gravity.TOP));

        listDivider = new View(context);
        listDivider.setVisibility(INVISIBLE);
        //listDivider.setBackgroundColor(AppController.isTheme ? Colors.layoutLight : Colors.layoutDark);
        //addView(listDivider, LayoutHelper.makeFrame(LayoutHelper.WRAP_CONTENT, Colors.listDividerHeight, Gravity.BOTTOM));
    }

    public DoubleCell addIcon(int icon) {
        iconView.setVisibility(VISIBLE);
        iconView.setImageResource(icon);
        titleTextView.setPadding(AndroidUtilities.dp(72), AndroidUtilities.dp(10), AndroidUtilities.dp(72), 0);
        valueTextView.setPadding(AndroidUtilities.dp(72), AndroidUtilities.dp(35), AndroidUtilities.dp(72), AndroidUtilities.dp(10));
        return this;
    }

    public DoubleCell addTitle(String title) {
        titleTextView.setVisibility(VISIBLE);
        titleTextView.setText(title);
        return this;
    }

    public DoubleCell addValue(String value) {
        valueTextView.setVisibility(VISIBLE);
        valueTextView.setText(value);
        return this;
    }

    public DoubleCell addDivider() {
        listDivider.setVisibility(VISIBLE);
        return this;
    }
}