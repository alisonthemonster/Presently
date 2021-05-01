package journal.gratitude.com.gratitudejournal.ui.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AlertDialogLayout
import androidx.appcompat.widget.DialogTitle
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.preference.ListPreferenceDialogFragmentCompat
import journal.gratitude.com.gratitudejournal.R
import kotlinx.android.synthetic.main.fragment_theme.view.*
import kotlinx.android.synthetic.main.item_theme.*
import org.w3c.dom.Text
import java.lang.Exception

class CustomListPrefDialogFragCompat: ListPreferenceDialogFragmentCompat() {
    companion object {
        fun newInstance(key: String?): CustomListPrefDialogFragCompat? {
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
        val attrs: IntArray = IntArray(3)
        attrs[0] = R.attr.backgroundColor
        attrs[1] = R.attr.entryContentTextColor
        val typedArray = theme?.obtainStyledAttributes(attrs)

        val backgroundColor  = typedArray?.getColor(0, Color.WHITE)
        backgroundColor?.let { content?.setBackgroundColor(it) }
        val textColor = typedArray?.getColor(1, Color.WHITE)

        if(textColor != null) {
            /**
             * Must make sure the dialog is created before we can access views
             */
            dialog.setOnShowListener(DialogInterface.OnShowListener {
                setDialogTitleColor(textColor)
                setColorListItemColor(listView, textColor!!)

                /*
                   When items are scrolled in ListView, items are created and removed,
                   so we need to recolor some items
                 */
                listView.setOnScrollListener(object : AbsListView.OnScrollListener {
                    override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}
                    /*
                       Use this method instead of other to avoid having wait the end of scroll event
                     */
                    override fun onScroll(view: AbsListView?, firstVisibleItem: Int,
                                          visibleItemCount: Int, totalItemCount: Int) {
                        setColorListItemColor(listView, textColor!!)
                    }
                })

            })
        }

        return dialog
    }

    private fun setColorListItemColor(listView: ListView, color: Int) {
        for (i in 0 until listView.count) {
            try {
                listView.get(i).findViewById<TextView>(android.R.id.text1).setTextColor(color!!)
            } catch (e: IndexOutOfBoundsException) {
                // Exception is caused by accessing item not rendered in listview
            }
        }
    }

    private  fun setDialogTitleColor(color: Int) {
        var dialogTitle: TextView? = null
        val titleId = resources.getIdentifier("alertTitle", "id", context?.packageName)
        if (titleId > 0) {
            dialogTitle = dialog?.findViewById<TextView>(titleId)!!
            if (color != null) {
                dialogTitle?.setTextColor(color)
            }
        }
    }
}