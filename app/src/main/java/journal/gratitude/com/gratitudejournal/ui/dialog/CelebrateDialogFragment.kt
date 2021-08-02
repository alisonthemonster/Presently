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
import com.presently.logging.AnalyticsLogger
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.databinding.FragmentMilestoneDialogBinding
import journal.gratitude.com.gratitudejournal.model.CLICKED_RATE
import journal.gratitude.com.gratitudejournal.model.CLICKED_SHARE_MILESTONE
import javax.inject.Inject

@AndroidEntryPoint
class CelebrateDialogFragment : DialogFragment() {

    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    private var _binding: FragmentMilestoneDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMilestoneDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val entryCount = requireNotNull(arguments?.getInt(NUM_ENTRIES))

        with(binding) {
            numEntries.text = entryCount.toString()

            close.setOnClickListener {
                dismiss()
            }
            ratePresently.setOnClickListener {
                analyticsLogger.recordEvent(CLICKED_RATE)
                val appPackageName = context?.packageName
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$appPackageName")
                        )
                    )
                } catch (exception: android.content.ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                        )
                    )
                }
            }

            sharePresently.setOnClickListener {
                analyticsLogger.recordEvent(CLICKED_SHARE_MILESTONE)

                val share = Intent(Intent.ACTION_SEND)
                share.type = "text/plain"
                val shareText = getString(R.string.share_milestone, entryCount)
                share.putExtra(Intent.EXTRA_TEXT, shareText)

                startActivity(Intent.createChooser(share, getString(R.string.share_your_gratitude)))
            }
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