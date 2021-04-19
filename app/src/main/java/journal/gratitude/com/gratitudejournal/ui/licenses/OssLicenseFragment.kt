package journal.gratitude.com.gratitudejournal.ui.licenses

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import journal.gratitude.com.gratitudejournal.R
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.util.*

class OssLicenseFragment(val license: OssLicensesActivity.License): Fragment() {
    private val TAG = "OssLicenseFragment"
    private lateinit var viewModel: OssLicensesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = OssLicensesViewModel(activity?.application!!)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_license, container, false)
        val content = root.findViewById<TextView>(R.id.content)

        viewModel.loadLicense(license)
        viewModel.licenseLD.observe(viewLifecycleOwner, object : Observer<OssLicensesActivity.License?> {
            override fun onChanged(t: OssLicensesActivity.License?) {
                content.text = t?.licenseContent
            }
        })

        return root
    }
}