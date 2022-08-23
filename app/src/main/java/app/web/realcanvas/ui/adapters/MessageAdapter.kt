package app.web.realcanvas.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.web.realcanvas.R
import app.web.realcanvas.models.Message

class MessageAdapter(
    val messages: MutableList<Message>,
    private val currentUserName: String?
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tv_message)
        val tvInitials: TextView = view.findViewById(R.id.tv_initial)
        val tvUsername: TextView = view.findViewById(R.id.tv_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_message_item, parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        if (currentUserName != null && currentUserName != message.userName) {
            holder.tvInitials.visibility = View.VISIBLE
            holder.tvMessage.text = message.message
            holder.tvInitials.text = message.userName[0].uppercase()
            holder.tvUsername.text = message.userName

            holder.tvMessage.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            holder.tvUsername.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        } else {
            holder.tvInitials.visibility = View.GONE
            holder.tvMessage.text = message.message
            holder.tvUsername.text = holder.tvUsername.context.getString(R.string.you)

            holder.tvMessage.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            holder.tvUsername.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}