package kv.apps.taskmanager.presentation.shared.uiComposables


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.res.Resources
import android.util.Log
import android.view.View
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

            create()

            window?.setBackgroundDrawableResource(backgroundColor)

            getButton(DatePickerDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(context, buttonColor)
            )
            getButton(DatePickerDialog.BUTTON_NEGATIVE)?.setTextColor(
                ContextCompat.getColor(context, buttonColor)
            )

            try {
                val headerId = Resources.getSystem().getIdentifier(
                    "date_picker_header", "id", "android")
                findViewById<View>(headerId)?.setBackgroundColor(
                    ContextCompat.getColor(context, headerColor)
                )

                listOf(
                    "date_picker_header_year",
                    "date_picker_header_date",
                    "date_picker_header_month",
                    "date_picker_header_text"
                ).forEach { idName ->
                    val id = Resources.getSystem().getIdentifier(idName, "id", "android")
                    findViewById<TextView>(id)?.setTextColor(
                        ContextCompat.getColor(context, headerTextColor)
                    )
                }

                val calendarViewId = Resources.getSystem().getIdentifier(
                    "android:id/month_grid", "id", "android")
                findViewById<View>(calendarViewId)?.setBackgroundColor(
                    ContextCompat.getColor(context, backgroundColor)
                )

                val dayId = Resources.getSystem().getIdentifier(
                    "android:id/day", "id", "android")
                findViewById<TextView>(dayId)?.setTextColor(
                    ContextCompat.getColor(context, dayTextColor)
                )

            } catch (e: Exception) {
                Log.e("CustomDatePicker", "Error styling DatePicker", e)
            }
        }

        datePickerDialog.show()
    }
}