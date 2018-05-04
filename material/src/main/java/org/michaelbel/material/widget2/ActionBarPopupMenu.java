package org.michaelbel.material.widget2;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;

import org.michaelbel.material.R;
import org.michaelbel.material.util2.Utils;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ActionBarPopupMenu extends PopupWindow {

    private static final String TAG = ActionBarPopupMenu.class.getSimpleName();

    private static final Field superListenerField;
    private static final boolean animationEnabled = Build.VERSION.SDK_INT >= 18;
    private static DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
    private AnimatorSet windowAnimatorSet;
    private Context context;

    static {
        Field f = null;

        try {
            f = PopupWindow.class.getDeclaredField("mOnScrollChangedListener");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, e.getMessage());
        }

        superListenerField = f;
    }

    private static final ViewTreeObserver.OnScrollChangedListener NOP = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            /* do nothing */
        }
    };

    private ViewTreeObserver.OnScrollChangedListener mSuperScrollListener;
    private ViewTreeObserver mViewTreeObserver;

    public interface OnDispatchKeyEventListener {
        void onDispatchKeyEvent(KeyEvent keyEvent);
    }

    public static class ActionBarPopupMenuLayout extends FrameLayout {

        private OnDispatchKeyEventListener mOnDispatchKeyEventListener;
        protected static Drawable backgroundDrawable;
        private float backScaleX = 1;
        private float backScaleY = 1;
        private int backAlpha = 255;
        private int lastStartedChild = 0;
        private boolean showedFromBottom;
        private HashMap<View, Integer> positions = new HashMap<>();

        private ScrollView scrollView;
        private LinearLayout linearLayout;

        public ActionBarPopupMenuLayout(Context context) {
            super(context);

            if (backgroundDrawable == null) {
                backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.popup_fixed);
            }

            setPadding(Utils.dp(getContext(), 8), Utils.dp(getContext(), 8), Utils.dp(getContext(), 8), Utils.dp(getContext(), 8));
            setWillNotDraw(false);

            try {
                scrollView = new ScrollView(context);
                scrollView.setVerticalScrollBarEnabled(false);
                addView(scrollView, LayoutHelper.makeFrame(getContext(), LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
            } catch (Throwable e) {
                Log.e(TAG, e.getMessage());
            }

            linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            if (scrollView != null) {
                scrollView.addView(linearLayout, new ScrollView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                addView(linearLayout, LayoutHelper.makeFrame(context, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
            }
        }

        public void setShowedFromBottom(boolean value) {
            showedFromBottom = value;
        }

        public void setDispatchKeyEventListener(OnDispatchKeyEventListener listener) {
            mOnDispatchKeyEventListener = listener;
        }

        public void setBackAlpha(int value) {
            backAlpha = value;
        }

        public int getBackAlpha() {
            return backAlpha;
        }

        public void setBackScaleX(float value) {
            backScaleX = value;
            invalidate();
        }

        public void setBackScaleY(float value) {
            backScaleY = value;
            if (animationEnabled) {
                int count = getItemsCount();
                int visibleCount = 0;
                for (int a = 0; a < count; a++) {
                    visibleCount += getItemAt(a).getVisibility() == VISIBLE ? 1 : 0;
                }
                int height = getMeasuredHeight() - Utils.dp(getContext(), 16);
                if (showedFromBottom) {
                    for (int a = lastStartedChild; a >= 0; a--) {
                        View child = getItemAt(a);
                        if (child.getVisibility() != VISIBLE) {
                            continue;
                        }
                        Integer position = positions.get(child);
                        if (position != null && height - (position * Utils.dp(getContext(), 48) + Utils.dp(getContext(), 32)) > value * height) {
                            break;
                        }
                        lastStartedChild = a - 1;
                        startChildAnimation(child);
                    }
                } else {
                    for (int a = lastStartedChild; a < count; a++) {
                        View child = getItemAt(a);
                        if (child.getVisibility() != VISIBLE) {
                            continue;
                        }
                        Integer position = positions.get(child);
                        if (position != null && (position + 1) * Utils.dp(getContext(), 48) - Utils.dp(getContext(), 24) > value * height) {
                            break;
                        }
                        lastStartedChild = a + 1;
                        startChildAnimation(child);
                    }
                }
            }
            invalidate();
        }

        private void startChildAnimation(View child) {
            if (animationEnabled) {
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(
                        ObjectAnimator.ofFloat(child, "alpha", 0.0f, 1.0f),
                        ObjectAnimator.ofFloat(child, "translationY", Utils.dp(getContext(), showedFromBottom ? 6 : -6), 0));
                animatorSet.setDuration(180);
                animatorSet.setInterpolator(decelerateInterpolator);
                animatorSet.start();
            }
        }

        @Override
        public void addView(View child) {
            linearLayout.addView(child);
        }

        public float getBackScaleX() {
            return backScaleX;
        }

        public float getBackScaleY() {
            return backScaleY;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (mOnDispatchKeyEventListener != null) {
                mOnDispatchKeyEventListener.onDispatchKeyEvent(event);
            }
            return super.dispatchKeyEvent(event);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (backgroundDrawable != null) {
                backgroundDrawable.setAlpha(backAlpha);
                if (showedFromBottom) {
                    backgroundDrawable.setBounds(0, (int) (getMeasuredHeight() * (1.0f - backScaleY)), (int) (getMeasuredWidth() * backScaleX), getMeasuredHeight());
                } else {
                    backgroundDrawable.setBounds(0, 0, (int) (getMeasuredWidth() * backScaleX), (int) (getMeasuredHeight() * backScaleY));
                }
                backgroundDrawable.draw(canvas);
            }
        }

        public int getItemsCount() {
            return linearLayout.getChildCount();
        }

        public View getItemAt(int index) {
            return linearLayout.getChildAt(index);
        }

        public void scrollToTop() {
            if (scrollView != null) {
                scrollView.scrollTo(0, 0);
            }
        }
    }

    public ActionBarPopupMenu(Context context) {
        super(context);
        initialize(context);
    }

    public ActionBarPopupMenu(Context context, int width, int height) {
        super(width, height);
        initialize(context);
    }

    public ActionBarPopupMenu(Context context, View contentView) {
        super(contentView);
        initialize(context);
    }

    public ActionBarPopupMenu(Context context, View contentView, int width, int height, boolean focusable) {
        super(contentView, width, height, focusable);
        initialize(context);
    }

    public ActionBarPopupMenu(Context context, View contentView, int width, int height) {
        super(contentView, width, height);
        initialize(context);
    }

    private void initialize(Context context) {
        this.context = context;

        if (superListenerField != null) {
            try {
                mSuperScrollListener = (ViewTreeObserver.OnScrollChangedListener) superListenerField.get(this);
                superListenerField.set(this, NOP);
            } catch (Exception e) {
                mSuperScrollListener = null;
            }
        }
    }

    private void unregisterListener() {
        if (mSuperScrollListener != null && mViewTreeObserver != null) {
            if (mViewTreeObserver.isAlive()) {
                mViewTreeObserver.removeOnScrollChangedListener(mSuperScrollListener);
            }
            mViewTreeObserver = null;
        }
    }

    private void registerListener(View anchor) {
        if (mSuperScrollListener != null) {
            ViewTreeObserver vto = (anchor.getWindowToken() != null) ? anchor.getViewTreeObserver() : null;
            if (vto != mViewTreeObserver) {
                if (mViewTreeObserver != null && mViewTreeObserver.isAlive()) {
                    mViewTreeObserver.removeOnScrollChangedListener(mSuperScrollListener);
                }
                if ((mViewTreeObserver = vto) != null) {
                    vto.addOnScrollChangedListener(mSuperScrollListener);
                }
            }
        }
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        try {
            super.showAsDropDown(anchor, xoff, yoff);
            registerListener(anchor);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void startAnimation() {
        if (animationEnabled) {
            if (windowAnimatorSet != null) {
                return;
            }

            ActionBarPopupMenuLayout content = (ActionBarPopupMenuLayout) getContentView();
            content.setTranslationY(0);
            content.setAlpha(1.0f);
            content.setPivotX(content.getMeasuredWidth());
            content.setPivotY(0);
            int count = content.getItemsCount();
            content.positions.clear();
            int visibleCount = 0;
            for (int a = 0; a < count; a++) {
                View child = content.getItemAt(a);
                if (child.getVisibility() != View.VISIBLE) {
                    continue;
                }

                content.positions.put(child, visibleCount);
                child.setAlpha(0.0f);
                visibleCount++;
            }

            if (content.showedFromBottom) {
                content.lastStartedChild = count - 1;
            } else {
                content.lastStartedChild = 0;
            }

            windowAnimatorSet = new AnimatorSet();
            windowAnimatorSet.playTogether(
                    ObjectAnimator.ofFloat(content, "backScaleY", 0.0f, 1.0f),
                    ObjectAnimator.ofInt(content, "backAlpha", 0, 255));
            windowAnimatorSet.setDuration(150 + 16 * visibleCount);
            windowAnimatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation) {
                    windowAnimatorSet = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    onAnimationEnd(animation);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            windowAnimatorSet.start();
        }
    }

    @Override
    public void update(View anchor, int xoff, int yoff, int width, int height) {
        super.update(anchor, xoff, yoff, width, height);
        registerListener(anchor);
    }

    @Override
    public void update(View anchor, int width, int height) {
        super.update(anchor, width, height);
        registerListener(anchor);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        unregisterListener();
    }

    @Override
    public void dismiss() {
        dismiss(true);
    }

    public void dismiss(boolean animated) {
        setFocusable(false);
        if (animationEnabled && animated) {
            if (windowAnimatorSet != null) {
                windowAnimatorSet.cancel();
            }
            ActionBarPopupMenuLayout content = (ActionBarPopupMenuLayout) getContentView();
            windowAnimatorSet = new AnimatorSet();
            windowAnimatorSet.playTogether(
                    ObjectAnimator.ofFloat(content, "translationY", Utils.dp(context, content.showedFromBottom ? 5 : -5)),
                    ObjectAnimator.ofFloat(content, "alpha", 0.0f));
            windowAnimatorSet.setDuration(150);
            windowAnimatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    windowAnimatorSet = null;
                    setFocusable(false);
                    try {
                        ActionBarPopupMenu.super.dismiss();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                    unregisterListener();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    onAnimationEnd(animation);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            windowAnimatorSet.start();
        } else {
            try {
                super.dismiss();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            unregisterListener();
        }
    }
}