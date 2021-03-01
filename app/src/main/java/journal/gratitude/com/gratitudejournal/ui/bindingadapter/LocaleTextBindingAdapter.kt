package journal.gratitude.com.gratitudejournal.ui.bindingadapter

import android.app.Application
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.toFullString
import org.threeten.bp.LocalDate


@BindingAdapter("hintSupportingLocale")
fun updateHint(view: EditText, isToday: Boolean) {
    val prompt = when {
        isToday -> {
            view.resources?.getString(R.string.what_are_you_thankful_for)
        }
        view.text.isNullOrEmpty() -> {
            view.resources?.getStringArray(R.array.prompts)?.random() ?: ""
        }
        else -> {
            view.resources?.getString(R.string.what_were_you_thankful_for)
        }
    }

    view.setHint(prompt)
}

@BindingAdapter("inspirationSupportingLocale")
fun updateQuote(view: TextView, text: String?) {
    val quote = view.resources?.getStringArray(R.array.inspirations)?.random() ?: ""
    view.text = quote
}

@BindingAdapter("textSupportingLocale")
fun updateText(view: TextView, text: String?) {
    val prompt = view.resources?.getStringArray(R.array.prompts)?.random() ?: ""
    view.hint = prompt
}

@BindingAdapter("thankfulStringSupportingLocale")
fun updateThankfulString(textView: TextView, dateLiveData: MutableLiveData<LocalDate>?) {
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