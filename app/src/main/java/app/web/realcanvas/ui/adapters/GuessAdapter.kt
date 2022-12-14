package app.web.realcanvas.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.web.realcanvas.R
import app.web.realcanvas.models.Message
import app.web.realcanvas.models.MessageType

class GuessAdapter(
    val messages: MutableList<Message>,
) : RecyclerView.Adapter<GuessAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGuess: TextView = view.findViewById(R.id.tv_guess)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_guess_item, parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]

        if (message.type == MessageType.GUESS_SUCCESS) {
            val text = "${message.userName} is correct"
            holder.tvGuess.setBackgroundResource(R.color.correct_guess)
            holder.tvGuess.textAlignment = View.TEXT_ALIGNMENT_CENTER
            holder.tvGuess.text = text
        } else {
            val text = "${message.userName}: ${message.message}"
//            holder.tvGuess.setBackgroundResource(R.color.correct_guess)
            holder.tvGuess.background = null
            holder.tvGuess.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            holder.tvGuess.text = text
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        messages.clear()
        notifyDataSetChanged()
    }
}