package kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.res.Resources
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kv.apps.taskmanager.R
import java.time.LocalDate
import java.util.Calendar

@SuppressLint("DiscouragedApi")
@Composable
fun rememberCustomDatePicker(
    initialDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    @ColorRes headerColor: Int = R.color.main_app_color,
    @ColorRes headerTextColor: Int = R.color.black,
    @ColorRes backgroundColor: Int = R.color.background_color,
    @ColorRes dayTextColor: Int = R.color.white,
    @ColorRes buttonColor: Int = R.color.main_app_color
): () -> Unit {
    val context = LocalContext.current

    return {
        val calendar = Calendar.getInstance()
        initialDate?.let {
            calendar.set(it.year, it.monthValue - 1, it.dayOfMonth)
        }

        val datePickerDialog = DatePickerDialog(
            context,
            R.style.CustomDatePickerTheme,
            { _, year, month, dayOfMonth ->
                onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { dismiss() }
            setOnCancelListener { dismiss() }

            show()

            window?.decorView?.post {
                try {
                    getButton(DatePickerDialog.BUTTON_POSITIVE)?.setTextColor(
                        ContextCompat.getColor(context, buttonColor)
                    )
                    getButton(DatePickerDialog.BUTTON_NEGATIVE)?.setTextColor(
                        ContextCompat.getColor(context, buttonColor)
                    )

                    window?.setBackgroundDrawableResource(backgroundColor)

                    val header = findDatePickerHeader(this)
                    header?.let {
                        it.setPadding(
                            it.paddingLeft,
                            it.paddingTop,
                            it.paddingRight,
                            it.paddingBottom
                        )

                        it.setBackgroundColor(
                            ContextCompat.getColor(context, headerColor)
                        )

                        val textViews = ArrayList<TextView>()
                        findTextViewsRecursive(it, textViews)
                        textViews.forEach { tv ->
                            tv.setTextColor(ContextCompat.getColor(context, headerTextColor))
                            tv.layoutParams = tv.layoutParams?.apply {
                                width = ViewGroup.LayoutParams.WRAP_CONTENT
                            }
                        }

                        if (textViews.isEmpty()) {
                            val ids = listOf(
                                Resources.getSystem().getIdentifier("date_picker_header_year", "id", "android"),
                                Resources.getSystem().getIdentifier("date_picker_header_date", "id", "android"),
                                Resources.getSystem().getIdentifier("date_picker_header_month", "id", "android")
                            )

                            ids.forEach { id ->
                                if (id != 0) {
                                    findViewById<TextView>(id)?.apply {
                                        setTextColor(ContextCompat.getColor(context, headerTextColor))
                                        layoutParams = layoutParams?.apply {
                                            width = ViewGroup.LayoutParams.WRAP_CONTENT
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val datePicker = findViewById<View>(Resources.getSystem().getIdentifier("datePicker", "id", "android"))
                    datePicker?.layoutParams = datePicker.layoutParams?.apply {
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                    }

                } catch (e: Exception) {
                    Log.e("CustomDatePicker", "Error styling DatePicker", e)
                }
            }
        }
    }
}

@SuppressLint("DiscouragedApi")
private fun findDatePickerHeader(dialog: DatePickerDialog): View? {
    return try {
        val resources = Resources.getSystem()
        val headerId = resources.getIdentifier("date_picker_header", "id", "android")
        if (headerId != 0) dialog.findViewById(headerId) else null
    } catch (_: Exception) {
        null
    }
}

private fun findTextViewsRecursive(view: View, output: ArrayList<TextView>) {
    if (view is TextView) {
        output.add(view)
    } else if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            findTextViewsRecursive(view.getChildAt(i), output)
        }
    }
}