package journal.gratitude.com.gratitudejournal.ui.licenses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import journal.gratitude.com.gratitudejournal.R
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.util.*

class OssLicensesViewModel(application: Application): AndroidViewModel(application) {
    private val menuMLD: MutableLiveData<List<OssLicensesActivity.License>> = MutableLiveData()
    val menuLD: LiveData<List<OssLicensesActivity.License>> = menuMLD

    private val licenseMLD: MutableLiveData<OssLicensesActivity.License> = MutableLiveData()
    val licenseLD = licenseMLD

    private lateinit var licensesInputStream: InputStreamReader

    init {
//        licensesInputStream = InputStreamReader(getApplication<Application>().resources.openRawResource(R.raw.third_party_licenses))
    }
    fun loadMenu() {
        val inStream = getApplication<Application>().resources.openRawResource(R.raw.third_party_license_metadata)
        val scanner = Scanner(inStream)
        var start = 0
        var end = 0
        val menuValues = mutableListOf<OssLicensesActivity.License>()
        viewModelScope.launch {
            val libs = mutableListOf<OssLicensesActivity.License>()
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                var firstDelimIndex = line.indexOf(":")
                val startString = line.subSequence(0, firstDelimIndex).toString()
                var secondDelimIndex = line.indexOf(" ")
                val lengthString = line.subSequence(firstDelimIndex + 1, secondDelimIndex).toString()
                val library = line.subSequence(secondDelimIndex, line.length).toString()
                libs.add(OssLicensesActivity.License(Integer.parseInt(startString), Integer.parseInt(lengthString), libName = library))
            }
            menuMLD.postValue(libs)
        }
    }

    fun loadLicense(license: OssLicensesActivity.License) {
        var charArray :CharArray = CharArray(license.length)

        licensesInputStream = InputStreamReader(getApplication<Application>().resources.openRawResource(R.raw.third_party_licenses))
        licensesInputStream.skip((license.start - 1).toLong())
        licensesInputStream.read(charArray, 0, license.length)
        license.licenseContent = charArray.concatToString()
        licenseMLD.postValue(license)
    }

}