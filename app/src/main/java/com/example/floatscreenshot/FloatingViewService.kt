package com.example.floatscreenshot

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast


internal class FloatingViewService: Service() {

    private var mWindowManager: WindowManager? = null
    private var mFloatingView: View? = null

    override fun onBind(intent: Intent?): IBinder? {
       return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        // Inflate the floating view layout we created
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        // Add the view to the window.

        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        // Specify the view position
        params.gravity =
            Gravity.TOP or Gravity.START //Initially view will be added to top-left corner
        params.x = 0
        params.y = 100
        // Add the view to the window
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mWindowManager?.addView(mFloatingView, params)

        // The root element of the collapsed view layout
        val collapsedView = mFloatingView?.findViewById<RelativeLayout>((R.id.collapse_view))
        // The root element of the expanded view layout
        val expandedView = mFloatingView?.findViewById<LinearLayout>(R.id.expanded_container);

        val closeIcon = mFloatingView?.findViewById<ImageView>((R.id.close_btn))
        closeIcon?.setOnClickListener {
            stopSelf()
        }

        val playIcon = mFloatingView?.findViewById<ImageView>((R.id.play_btn))
        playIcon?.setOnClickListener {
            Toast.makeText(it.context, "Playing the song.", Toast.LENGTH_SHORT).show();
        }

        val nextIcon = mFloatingView?.findViewById<ImageView>((R.id.next_btn))
        nextIcon?.setOnClickListener {
            Toast.makeText(it.context, "Next clicked!", Toast.LENGTH_SHORT).show();
        }

        val prevIcon = mFloatingView?.findViewById<ImageView>((R.id.prev_btn))
        prevIcon?.setOnClickListener {
            Toast.makeText(it.context, "Prev clicked!", Toast.LENGTH_SHORT).show();
        }

        val closeButton = mFloatingView?.findViewById<ImageView>((R.id.close_button))
        closeButton?.setOnClickListener {
            collapsedView?.visibility = View.VISIBLE;
            expandedView?.visibility = View.GONE;
        }

        // Open the application on thi button click
        val openButton = mFloatingView?.findViewById<ImageView>((R.id.open_button))
        openButton?.setOnClickListener {
            // Open the application  click.
            val intent = Intent(this@FloatingViewService, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            // close the service and remove view from the view hierarchy
            stopSelf()
        }


        val rootView = mFloatingView?.findViewById<RelativeLayout>(R.id.root_container)
        // Drag and move floating view using user's touch action.
        rootView?.setOnTouchListener(object : View.OnTouchListener {

            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {

                        //Info - remember the initial position.
                        initialX = params.x
                        initialY = params.y

                        //Info - get the touch location
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        //Info - Calculate the X and Y coordinates of the view.
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()

                        //Info - Update the layout with new X & Y coordinate
                        mWindowManager!!.updateViewLayout(mFloatingView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val xdiff = (event.rawX - initialTouchX).toInt()
                        val ydiff = (event.rawY - initialTouchY).toInt()

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (xdiff < 10 && ydiff < 10) {
                            if (isViewCollapsed()) {
                                //When user clicks on the image view of the collapsed layout,
                                //visibility of the collapsed layout will be changed to "View.GONE"
                                //and expanded view will become visible.
                                collapsedView!!.visibility = View.GONE
                                expandedView!!.visibility = View.VISIBLE
                            }
                        }
                    }
                }
                return false
            }
        })
    }

    /**
     * Detect if the floating view is collapsed or expanded.
     *
     * @return true if the floating view is collapsed.
     */
    private fun isViewCollapsed(): Boolean {
        return mFloatingView == null || mFloatingView?.findViewById<View>(R.id.collapse_view)?.visibility == View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mFloatingView != null) mWindowManager?.removeView(mFloatingView)
    }

}