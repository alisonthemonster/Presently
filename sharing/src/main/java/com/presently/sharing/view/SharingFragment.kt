package com.presently.sharing.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.airbnb.mvrx.*
import com.airbnb.mvrx.mocking.MavericksViewMocks
import com.airbnb.mvrx.mocking.MockableMavericksView
import com.airbnb.mvrx.mocking.mockSingleViewModel
import com.presently.sharing.R
import com.presently.sharing.data.SharingArgs
import com.presently.sharing.data.SharingViewDesign
import com.presently.sharing.data.SharingViewState
import com.presently.sharing.data.designs
import com.presently.sharing.databinding.FragmentSharingBinding
import com.presently.ui.setStatusBarColorsForBackground
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class SharingFragment : Fragment(R.layout.fragment_sharing), MockableMavericksView {

    private val sharingViewModel: SharingViewModel by fragmentViewModel()

    private var _binding: FragmentSharingBinding? = null
    private val binding get() = _binding!!

    private val listener = object : OnDesignSelectedListener {
        override fun onDesignSelected(design: SharingViewDesign) {
            sharingViewModel.selectColorScheme(design)
        }
    }
    private val adapter = DesignListAdapter(listener)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSharingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.addData(designs)
        binding.designList.layoutManager = GridLayoutManager(context, 3)
        binding.designList.adapter = adapter

        binding.checkMark.setOnClickListener {
            sharingViewModel.clickFinish()
        }

        binding.backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.sharingContainer) { v, insets ->
            v.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top)
            insets
        }

        val window = requireActivity().window
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.toolbarColor, typedValue, true)
        setStatusBarColorsForBackground(window, typedValue.data)
        window.statusBarColor = typedValue.data
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharingViewModel.onCreate()
    }

    override fun invalidate() {
        withState(sharingViewModel) {
            binding.sharingPreview.setDesignBackgroundColor(it.viewDesign.backgroundColor)
            binding.sharingPreview.setHeaderTextColor(it.viewDesign.headerTextColor)
            binding.sharingPreview.setContentTextColor(it.viewDesign.bodyTextColor)
            binding.sharingPreview.setDate(it.dateString)
            binding.sharingPreview.setContent(it.content)

            if (it.clicksShare) {
                val bitmap = generateBitmap()
                shareBitmap(bitmap)
                sharingViewModel.sharingComplete()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun shareBitmap(bitmap: Bitmap) {
        lifecycleScope.launch {
            val uri = saveBitmap(bitmap)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share your gratitude"))
        }
    }

    private fun generateBitmap(): Bitmap {
        val view = binding.sharingPreview
        val width = view.width
        val height = view.height

        val measuredWidth = View.MeasureSpec.makeMeasureSpec(
            width,
            View.MeasureSpec.EXACTLY
        )
        val measuredHeight = View.MeasureSpec.makeMeasureSpec(
            height,
            View.MeasureSpec.EXACTLY
        )

        view.measure(measuredWidth, measuredHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    private suspend fun saveBitmap(bitmap: Bitmap): Uri = withContext(Dispatchers.IO) {
        val imagesFolder = File(requireContext().cacheDir, "presently_images")
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "presently-image.png")
        val fileOutputStream = file.outputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        FileProvider.getUriForFile(requireContext(), "${activity?.packageName}.fileprovider", file);
    }

    override fun provideMocks(): MavericksViewMocks<out MockableMavericksView, out Parcelable> = mockSingleViewModel(
        viewModelReference = SharingFragment::sharingViewModel,
        defaultState = SharingViewState(SharingArgs("Puppy snuggles", "September 29, 2021")),
        defaultArgs = SharingArgs("Puppy snuggles", "September 29, 2021")
    ) {
        state("Long entry") {
            copy(content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
        }
        state("Moonlight design") {
            copy(viewDesign = SharingViewDesign(
                "moonlight",
                R.color.moonlightMainTextAndButtonColor,
                R.color.moonlightMainTextAndButtonColor,
                R.color.moonlightBackgroundColor
            ))
        }
    }

    interface OnDesignSelectedListener {
        fun onDesignSelected(design: SharingViewDesign)
    }

    companion object {
        fun newInstance(date: String, content: String): SharingFragment {
            val fragment = SharingFragment()
            fragment.arguments = SharingArgs(content, date).asMavericksArgs()
            return fragment
        }
    }

}