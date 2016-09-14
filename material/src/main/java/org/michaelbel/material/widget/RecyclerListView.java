/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.michaelbel.material.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class RecyclerListView extends RecyclerView {

    private static final String TAG = RecyclerListView.class.getSimpleName();

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private RecyclerView.OnScrollListener onScrollListener;
    private OnInterceptTouchListener onInterceptTouchListener;
    private View emptyView;
    private Runnable selectChildRunnable;

    private GestureDetector mGestureDetector;
    private View currentChildView;
    private int currentChildPosition;
    private boolean interceptedByChild;
    private boolean wasPressed;
    private boolean disallowInterceptTouchEvents;
    private boolean instantClick;
    private Runnable clickRunnable;

    private static int[] attributes;
    private static boolean gotAttributes;
    private static volatile Handler applicationHandler = new Handler(Looper.getMainLooper());

    public RecyclerListView(Context context) {
        this(context, null);
    }

    public RecyclerListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs, defStyle);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyle) {
        try {
            if (!gotAttributes) {
                attributes = getResourceDeclareStyleableIntArray("com.android.internal", "View");
                gotAttributes = true;
            }

            TypedArray a = context.getTheme().obtainStyledAttributes(attributes);
            Method initializeScrollbars = android.view.View.class.getDeclaredMethod("initializeScrollbars", TypedArray.class);
            initializeScrollbars.invoke(this, a);
            a.recycle();
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        }

        super.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState != SCROLL_STATE_IDLE && currentChildView != null) {
                    if (selectChildRunnable != null) {
                        cancelRunOnUIThread(selectChildRunnable);
                        selectChildRunnable = null;
                    }

                    MotionEvent event = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
                    try {
                        mGestureDetector.onTouchEvent(event);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }

                    currentChildView.onTouchEvent(event);
                    event.recycle();
                    currentChildView.setPressed(false);
                    currentChildView = null;
                    interceptedByChild = false;
                }
                if (onScrollListener != null) {
                    onScrollListener.onScrollStateChanged(recyclerView, newState);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (onScrollListener != null) {
                    onScrollListener.onScrolled(recyclerView, dx, dy);
                }
            }
        });
        addOnItemTouchListener(new RecyclerListViewItemClickListener(context));
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemClick(View view, int position);
    }

    public interface OnInterceptTouchListener {
        boolean onInterceptTouchEvent(MotionEvent event);
    }

    private class RecyclerListViewItemClickListener implements RecyclerView.OnItemTouchListener {

        public RecyclerListViewItemClickListener(Context context) {
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    if (currentChildView != null && onItemClickListener != null) {
                        currentChildView.setPressed(true);
                        final View view = currentChildView;

                        if (instantClick) {
                            view.playSoundEffect(SoundEffectConstants.CLICK);
                            onItemClickListener.onItemClick(view, currentChildPosition);
                        }

                        runOnUIThread(clickRunnable = new Runnable() {
                            @Override
                            public void run() {
                                if (this == clickRunnable) {
                                    clickRunnable = null;
                                }

                                if (view != null) {
                                    view.setPressed(false);
                                    if (!instantClick) {
                                        view.playSoundEffect(SoundEffectConstants.CLICK);
                                        if (onItemClickListener != null) {
                                            onItemClickListener.onItemClick(view, currentChildPosition);
                                        }
                                    }
                                }
                            }
                        }, ViewConfiguration.getPressedStateDuration());

                        if (selectChildRunnable != null) {
                            cancelRunOnUIThread(selectChildRunnable);
                            selectChildRunnable = null;
                            currentChildView = null;
                            interceptedByChild = false;
                        }
                    }

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent event) {
                    if (currentChildView != null) {
                        if (onItemLongClickListener != null) {
                            if (onItemLongClickListener.onItemClick(currentChildView, currentChildPosition)) {
                                currentChildView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                            }
                        }
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent event) {
            int action = event.getActionMasked();
            boolean isScrollIdle = RecyclerListView.this.getScrollState() == RecyclerListView.SCROLL_STATE_IDLE;

            if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) && currentChildView == null && isScrollIdle) {
                currentChildView = view.findChildViewUnder(event.getX(), event.getY());
                if (currentChildView instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) currentChildView;
                    float x = event.getX() - currentChildView.getLeft();
                    float y = event.getY() - currentChildView.getTop();
                    final int count = viewGroup.getChildCount();
                    for (int i = count - 1; i >= 0; i--) {
                        final View child = viewGroup.getChildAt(i);
                        if (x >= child.getLeft() && x <= child.getRight() && y >= child.getTop() && y <= child.getBottom()) {
                            if (child.isClickable()) {
                                currentChildView = null;
                                break;
                            }
                        }
                    }
                }
                currentChildPosition = -1;
                if (currentChildView != null) {
                    currentChildPosition = view.getChildPosition(currentChildView);
                    MotionEvent childEvent = MotionEvent.obtain(0, 0, event.getActionMasked(), event.getX() - currentChildView.getLeft(), event.getY() - currentChildView.getTop(), 0);
                    if (currentChildView.onTouchEvent(childEvent)) {
                        interceptedByChild = true;
                    }
                    childEvent.recycle();
                }
            }

            if (currentChildView != null && !interceptedByChild) {
                try {
                    if (event != null) {
                        mGestureDetector.onTouchEvent(event);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                if (!interceptedByChild && currentChildView != null) {
                    selectChildRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (selectChildRunnable != null && currentChildView != null) {
                                currentChildView.setPressed(true);
                                selectChildRunnable = null;
                            }
                        }
                    };

                    runOnUIThread(selectChildRunnable, ViewConfiguration.getTapTimeout());
                }
            } else if (currentChildView != null && (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL || !isScrollIdle)) {
                if (selectChildRunnable != null) {
                    cancelRunOnUIThread(selectChildRunnable);
                    selectChildRunnable = null;
                }

                currentChildView.setPressed(false);
                currentChildView = null;
                interceptedByChild = false;
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent event) {}

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            cancelClickRunnables(true);
        }
    }

    public void cancelClickRunnables(boolean uncheck) {
        if (selectChildRunnable != null) {
            cancelRunOnUIThread(selectChildRunnable);
            selectChildRunnable = null;
        }

        if (currentChildView != null) {
            if (uncheck) {
                currentChildView.setPressed(false);
            }

            currentChildView = null;
        }

        if (clickRunnable != null) {
            cancelRunOnUIThread(clickRunnable);
            clickRunnable = null;
        }

        interceptedByChild = false;
    }

    private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };

    public int[] getResourceDeclareStyleableIntArray(String packageName, String name) {
        try {
            Field f = Class.forName(packageName + ".R$styleable").getField(name);
            if (f != null) {
                return (int[]) f.get(null);
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    @Override
    public void setVerticalScrollBarEnabled(boolean verticalScrollBarEnabled) {
        if (attributes != null) {
            super.setVerticalScrollBarEnabled(verticalScrollBarEnabled);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        onItemLongClickListener = listener;
    }

    public void setEmptyView(View view) {
        if (emptyView == view) {
            return;
        }

        emptyView = view;
        checkIfEmpty();
    }

    public View getEmptyView() {
        return emptyView;
    }

    public void invalidateViews() {
        int count = getChildCount();
        for (int a = 0; a < count; a++) {
            getChildAt(a).invalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (disallowInterceptTouchEvents) {
            requestDisallowInterceptTouchEvent(true);
        }

        return onInterceptTouchListener != null && onInterceptTouchListener.onInterceptTouchEvent(e) || super.onInterceptTouchEvent(e);
    }

    private void checkIfEmpty() {
        if (emptyView == null || getAdapter() == null) {
            return;
        }

        boolean emptyViewVisible = getAdapter().getItemCount() == 0;
        emptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
        setVisibility(emptyViewVisible ? INVISIBLE : VISIBLE);
    }

    @Override
    public void addOnScrollListener(OnScrollListener listener) {
        onScrollListener = listener;
    }

    public void setOnInterceptTouchListener(OnInterceptTouchListener listener) {
        onInterceptTouchListener = listener;
    }

    public void setInstantClick(boolean value) {
        instantClick = value;
    }

    public void setDisallowInterceptTouchEvents(boolean value) {
        disallowInterceptTouchEvents = value;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();

        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }

        super.setAdapter(adapter);

        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }

        checkIfEmpty();
    }

    @Override
    public void stopScroll() {
        try {
            super.stopScroll();
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            applicationHandler.post(runnable);
        } else {
            applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        applicationHandler.removeCallbacks(runnable);
    }
}