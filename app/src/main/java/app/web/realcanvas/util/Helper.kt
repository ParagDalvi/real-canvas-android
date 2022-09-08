package app.web.realcanvas.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog

fun hideKeyboard(context: Context?, view: View?) {
    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view?.windowToken, 0)
}

fun showCustomDialog(
    context: Context,
    positivePress: () -> Unit,
    title: String,
    message: String
) {
    AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok) { _, _ -> positivePress() }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
}