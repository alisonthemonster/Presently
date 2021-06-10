package com.presently.sharing.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.presently.sharing.R
import com.presently.sharing.databinding.ViewOriginalBinding

class OriginalView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private var mBackgroundColor: Int
    private val background: View
    private var mHeaderTextColor: Int
    private val headerDate: TextView
    private val headerCopy: TextView
    private val upperDivider: View
    private val lowerDivider: View
    private val presentlyAppName: TextView
    private var mContentTextColor: Int
    private val content: TextView

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.OriginalView,
            0, 0
        ).apply {

            try {
                mBackgroundColor = getColor(R.styleable.OriginalView_backgroundColor, 1)
                mHeaderTextColor = getColor(R.styleable.OriginalView_headerTextColor, 1)
                mContentTextColor = getColor(R.styleable.OriginalView_bodyTextColor, 1)
            } finally {
                recycle()
            }
        }

        val view = View.inflate(context, R.layout.view_original, this)
        val binding = ViewOriginalBinding.bind(view)
        background = binding.sharingBackground
        headerCopy = binding.iWasGratefulFor
        headerDate = binding.dateString
        lowerDivider = binding.dividerLineBottom
        upperDivider = binding.dividerLine
        content = binding.content
        presentlyAppName = binding.presentlyAppName
    }

    fun setDesignBackgroundColor(backgroundColor: Int) {
        mBackgroundColor = backgroundColor
        background.setBackgroundColor(ContextCompat.getColor(this.context, mBackgroundColor))
        invalidate()
        requestLayout()
    }

    fun setHeaderTextColor(textColor: Int) {
        mHeaderTextColor = textColor
        upperDivider.setBackgroundColor(ContextCompat.getColor(this.context, mHeaderTextColor))
        lowerDivider.setBackgroundColor(ContextCompat.getColor(this.context, mHeaderTextColor))
        presentlyAppName.setTextColor(ContextCompat.getColor(this.context, mHeaderTextColor))
        headerDate.setTextColor(ContextCompat.getColor(this.context, mHeaderTextColor))
        headerCopy.setTextColor(ContextCompat.getColor(this.context, mHeaderTextColor))
        invalidate()
        requestLayout()
    }

    fun setContentTextColor(textColor: Int) {
        mContentTextColor = textColor
        content.setTextColor(ContextCompat.getColor(this.context, mContentTextColor))
    }

    fun setDate(dateString: String) {
        headerDate.text = dateString
    }

    fun setContent(contentString: String) {
        content.text = contentString
    }
}