package journal.gratitude.com.gratitudejournal.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.analytics.FirebaseAnalytics
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.CLICKED_RATE
import kotlinx.android.synthetic.main.dialog_fragment.*

class CelebrateDialog : DialogFragment() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)

        val numEntries = arguments?.getInt("NUM_ENTRIES")
        num_entries.text = numEntries.toString()

        rate_presently.setOnClickListener {
            firebaseAnalytics.logEvent(CLICKED_RATE, null)
            val appPackageName = context?.packageName
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (exception: android.content.ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.MilestoneDialog)
        return dialog
    }

    override fun onDestroyView() {
        val dialog = dialog
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && retainInstance) {
            dialog.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    companion object {
        fun newInstance(numberEntries: Int): CelebrateDialog {
            val dialog = CelebrateDialog()
            dialog.retainInstance = true

            // Supply num input as an argument.
            val args = Bundle()
            args.putInt("NUM_ENTRIES", numberEntries)
            dialog.arguments = args

            return dialog
        }
    }



}