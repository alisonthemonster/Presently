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
import com.presently.analytics.PresentlyAnalytics
import dagger.android.support.DaggerDialogFragment
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.CLICKED_RATE
import journal.gratitude.com.gratitudejournal.model.CLICKED_SHARE_MILESTONE
import kotlinx.android.synthetic.main.fragment_milestone_dialog.*
import javax.inject.Inject

class CelebrateDialogFragment : DaggerDialogFragment() {

    @Inject
    lateinit var analytics: PresentlyAnalytics

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_milestone_dialog, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val numEntries = arguments?.getInt(NUM_ENTRIES)
        num_entries.text = numEntries.toString()

        close.setOnClickListener {
            dismiss()
        }
        rate_presently.setOnClickListener {
            analytics.recordEvent(CLICKED_RATE)
            val appPackageName = context?.packageName
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (exception: android.content.ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
        }

        share_presently.setOnClickListener {
            analytics.recordEvent(CLICKED_SHARE_MILESTONE)

            val share = Intent(Intent.ACTION_SEND)
            share.type = "text/plain"
            val shareText = getString(R.string.share_milestone, numEntries)
            share.putExtra(Intent.EXTRA_TEXT, shareText)

            startActivity(Intent.createChooser(share, getString(R.string.share_your_gratitude)))
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setStyle(STYLE_NO_FRAME, R.style.MilestoneDialog)
        return dialog
    }

    override fun onDestroyView() {
        val dialog = dialog
        if (dialog != null && retainInstance) {
            dialog.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    companion object {
        const val NUM_ENTRIES = "NUM_ENTRIES"

        fun newInstance(numberEntries: Int): CelebrateDialogFragment {
            val dialog = CelebrateDialogFragment()
            dialog.retainInstance = true

            val args = Bundle()
            args.putInt(NUM_ENTRIES, numberEntries)
            dialog.arguments = args

            return dialog
        }
    }



}