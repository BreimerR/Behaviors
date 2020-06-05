package com.gmail.brymher.behaviors;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

/*
 * Copyright (C) 2015 The Android Open Source Project
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
open class ViewOffsetBehavior<V : View> : CoordinatorLayout.Behavior<V> {

    private var mViewOffsetHelper: ViewOffsetHelper? = null

    private var mTempTopBottomOffset = 0;
    private var mTempLeftRightOffset = 0;

    constructor() : super()

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        // First let lay the child out
        layoutChild(parent, child, layoutDirection);

        if (mViewOffsetHelper == null) {
            mViewOffsetHelper = ViewOffsetHelper(child);
        }
        mViewOffsetHelper?.onViewLayout();

        if (mTempTopBottomOffset != 0) {
            mViewOffsetHelper?.topAndBottomOffset = mTempTopBottomOffset;
            mTempTopBottomOffset = 0;
        }
        if (mTempLeftRightOffset != 0) {
            mViewOffsetHelper?.leftAndRightOffset = mTempLeftRightOffset;
            mTempLeftRightOffset = 0;
        }

        return true;
    }

    protected open fun layoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int) {
        // Let the parent lay it out by default
        parent.onLayoutChild(child, layoutDirection);
    }

    fun setTopAndBottomOffset(offset: Int): Boolean {

        return if (mViewOffsetHelper == null) {
            mTempTopBottomOffset = offset
            false
        } else mViewOffsetHelper!!.setTopAndBottomOffset(offset)

    }

    fun setLeftAndRightOffset(offset: Int): Boolean {
        return if (mViewOffsetHelper == null) {
            mTempLeftRightOffset = offset;
            false
        } else mViewOffsetHelper!!.setLeftAndRightOffset(offset)
    }

    fun getTopAndBottomOffset(): Int {
        return mViewOffsetHelper?.topAndBottomOffset ?: 0
    }

    fun getLeftAndRightOffset(): Int {
        return mViewOffsetHelper?.leftAndRightOffset ?: 0;
    }
}
