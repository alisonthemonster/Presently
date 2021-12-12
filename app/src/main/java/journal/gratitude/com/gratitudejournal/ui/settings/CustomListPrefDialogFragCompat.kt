package journal.gratitude.com.gratitudejournal.ui.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.preference.ListPreferenceDialogFragmentCompat
import journal.gratitude.com.gratitudejournal.R

class CustomListPrefDialogFragCompat: ListPreferenceDialogFragmentCompat() {
    companion object {
        fun newInstance(key: String?): CustomListPrefDialogFragCompat {
            val fragment = CustomListPrefDialogFragCompat()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }

    @SuppressLint("ResourceType")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as AlertDialog
        val content = dialog.findViewById<View>(android.R.id.content)
        val listView = dialog.listView

        val theme = context?.theme
        val attrs = IntArray(3)
        attrs[0] = R.attr.timelineBackgroundColor
        attrs[1] = R.attr.entryBodyColor
        val typedArray = theme?.obtainStyledAttributes(attrs)

        val backgroundColor  = typedArray?.getColor(0, Color.WHITE)
        backgroundColor?.let { content?.setBackgroundColor(it) }
        val textColor = typedArray?.getColor(1, Color.WHITE)

        if(textColor != null) {
            /**
             * Must make sure the dialog is created before we can access views
             */
            dialog.setOnShowListener {
                setDialogTitleColor(textColor)
                setColorListItemColor(listView, textColor)

                /*
                   When items are scrolled in ListView, items are created and removed,
                   so we need to recolor some items
                 */
                listView.setOnScrollListener(object : AbsListView.OnScrollListener {
                    override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}

                    /*
                       Use this method instead of other to avoid having wait the end of scroll event
                     */
                    override fun onScroll(
                        view: AbsListView?, firstVisibleItem: Int,
                        visibleItemCount: Int, totalItemCount: Int
                    ) {
                        setColorListItemColor(listView, textColor)
                    }
                })

            }
        }

        return dialog
    }

    private fun setColorListItemColor(listView: ListView, color: Int) {
        for (i in 0 until listView.count) {
            try {
                listView[i].findViewById<TextView>(android.R.id.text1).setTextColor(color)
            } catch (e: IndexOutOfBoundsException) {
                // Exception is caused by accessing item not rendered in listview
            }
        }
    }

    private  fun setDialogTitleColor(color: Int) {
        val titleId = resources.getIdentifier("alertTitle", "id", context?.packageName)
        if (titleId > 0) {
            val dialogTitle: TextView? = dialog?.findViewById(titleId)
            dialogTitle?.setTextColor(color)
        }
    }
}