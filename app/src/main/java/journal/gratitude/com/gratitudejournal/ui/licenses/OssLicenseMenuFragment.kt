package journal.gratitude.com.gratitudejournal.ui.licenses

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import journal.gratitude.com.gratitudejournal.R
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.util.*

class OssLicenseMenuFragment: Fragment() {
    data class License(val start: Int, val length: Int, val libName: String) {

    }
//    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
//    private val viewModel by viewModels<OssLicensesViewModel>{viewModelFactory}
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
            list.adapter = adapter
        })


        return view
    }

    private class LicenseMenuAdapter(context: Context, resource: Int, objects: List<OssLicensesActivity.License>) :
            ArrayAdapter<OssLicensesActivity.License>(context, resource, objects)
    {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            convertView?.setBackgroundColor(context.getColor(R.color.colorPrimary))
//            convertView?.findViewById<TextView>(android.R.id.text1) =
            return super.getView(position, convertView, parent)
        }


    }
}