package journal.gratitude.com.gratitudejournal.ui.licenses

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import journal.gratitude.com.gratitudejournal.R
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.util.*

class OssLicenseMenuFragment: Fragment() {
    private lateinit var viewModel: OssLicensesViewModel
    private val TAG = "OssLicensesMenuActivity";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = OssLicensesViewModel(activity?.application!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_license_menu, container, false)
        val list = view.findViewById<ListView>(R.id.listView)
        viewModel.loadMenu()
        viewModel.menuLD.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val adapter = LicenseMenuAdapter(requireContext(), android.R.layout.simple_list_item_1, it.toMutableList())

            list.setOnItemClickListener(AdapterView.OnItemClickListener(function ={ parent, view, position, id ->
                run {
                    val license = adapter.getItem(position)
                    if(license != null)
                        activity?.supportFragmentManager?.beginTransaction()?.addToBackStack(null)?.replace(R.id.fragment, OssLicenseFragment(license))?.commit()
                }
            }))
            list.adapter = adapter
        })

        return view
    }

    private class LicenseMenuAdapter(context: Context, resource: Int, licenses: List<OssLicensesActivity.License>) :
            ArrayAdapter<OssLicensesActivity.License>(context, resource, licenses)
    {
        val licenses = licenses
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val textView = super.getView(position, convertView, parent) as TextView
            textView?.text =  licenses[position].libName
                return textView
        }
    }
}