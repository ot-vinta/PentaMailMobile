package ru.otvinta.pentamail

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import android.text.method.Touch.onTouchEvent



class RecyclerItemClickListener(context: Context, recyclerView: RecyclerView, listener: OnItemClickListener) : RecyclerView.OnItemTouchListener{
    private var mListener: OnItemClickListener = listener

    public interface OnItemClickListener{
        fun onItemClick(view : View, position : Int)

        fun onLongItemClick(view : View, position : Int)
    }

    var mGestureDetector: GestureDetector

    init {
        mGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val child = recyclerView.findChildViewUnder(e.x, e.y)
                if (child != null && mListener != null) {
                    mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child))
                }
            }
        })
    }

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView = view.findChildViewUnder(e.x, e.y)
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView))
            return true
        }
        return false
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
}