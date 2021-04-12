package journal.gratitude.com.gratitudejournal.ui.licenses

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import journal.gratitude.com.gratitudejournal.R
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.util.*

class OssLicensesActivity: AppCompatActivity() {
    data class License(val start: Int, val length: Int, val libName: String, var licenseContent: String = "") {

    }
    private val TAG = "OssLicensesMenuActivity";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       setContentView(R.layout.activity_licenses)
        supportFragmentManager.beginTransaction().replace(R.id.fragment, OssLicenseMenuFragment()).commit()
    }

    fun loadData() {
        val inStream = resources.openRawResource(R.raw.third_party_license_metadata)
        val scanner = Scanner(inStream)
        var start = 0
        var end = 0
        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            var firstDelimIndex = line.indexOf(":")
            val startString = line.subSequence(0, firstDelimIndex)
            var secondDelimIndex = line.indexOf(" ")
            val lengthString = line.subSequence(firstDelimIndex + 1, secondDelimIndex)
            val library = line.subSequence(secondDelimIndex, line.length)
        }
    }
}