package journal.gratitude.com.gratitudejournal.ui.bindingadapter

import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.toFullString
import org.threeten.bp.LocalDate


@BindingAdapter("hintSupportingLocale")
fun updateHint(view: EditText,text: String?) {
    val prompt = view.resources?.getStringArray(R.array.prompts)?.random() ?: ""
    view.setHint(prompt)
}

@BindingAdapter("textSupportingLocale")
fun updateText(view: TextView, text: String?) {
    val prompt = view.resources?.getStringArray(R.array.prompts)?.random() ?: ""
    view.hint = prompt
}

@BindingAdapter("thankFulStringSupportingLocale")
fun updateThankFulString(textView: TextView, dateLiveData: MutableLiveData<LocalDate>?) {
    val date = dateLiveData?.value ?: textView.setText("")
    if (date == LocalDate.now()) {
        textView.text = textView.resources.getString(R.string.iam)
    } else {
        textView.text = textView.resources.getString(R.string.iwas)
    }
}

@BindingAdapter("dateStringSupportingLocale")
fun updateDateString(view: TextView, dateLiveData: MutableLiveData<LocalDate>?) {
    val date: LocalDate = dateLiveData?.value ?: LocalDate.now()
    val today = LocalDate.now()
    when (date) {
        today -> view.text = view.resources.getString(R.string.today)
        today.minusDays(1) -> view.text = view.resources.getString(R.string.yesterday)
        else -> view.text = date.toFullString()
    }
}