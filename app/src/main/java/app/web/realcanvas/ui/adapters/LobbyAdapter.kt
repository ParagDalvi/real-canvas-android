package app.web.realcanvas.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.web.realcanvas.R
import app.web.realcanvas.models.Player
import app.web.realcanvas.util.showCustomDialog

class LobbyAdapter(
    private var players: List<Player>,
    private var currentPlayer: Player?,
    private val removePlayer: (Player) -> Unit,
) : RecyclerView.Adapter<LobbyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_name)
        val tvScore: TextView = view.findViewById(R.id.tv_score)
        val tvInitials: TextView = view.findViewById(R.id.tv_initial)
        val btnRemove: ImageButton = view.findViewById(R.id.btn_remove)
        val tvIsAdmin: TextView = view.findViewById(R.id.tv_admin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_lobby_item, parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]
        holder.tvName.text = player.userName

        holder.tvInitials.text = player.userName[0].toString().uppercase()

        holder.tvScore.text = player.score.toString()
        holder.tvScore.visibility = if (player.score == 0) View.GONE else View.VISIBLE

        if (currentPlayer?.isAdmin == true) {
            if (player.userName != currentPlayer?.userName) {
                holder.btnRemove.visibility = View.VISIBLE
                holder.btnRemove.setOnClickListener {
                    removePlayerDialog(
                        player,
                        holder.itemView.context
                    )
                }

                holder.tvIsAdmin.visibility = View.GONE
            }
        } else {
            holder.btnRemove.visibility = View.GONE

            holder.tvIsAdmin.visibility = if (player.isAdmin) View.VISIBLE else View.GONE
        }
    }

    private fun removePlayerDialog(player: Player, context: Context) {
        showCustomDialog(
            context,
            { removePlayer(player) },
            "Remove ${player.userName}?",
            "Are you sure you want to remove ${player.userName}?"
        )
    }

    override fun getItemCount(): Int = players.size

    @SuppressLint("NotifyDataSetChanged")
    fun updatePlayers(players: List<Player>, currentPlayer: Player?) {
        this.players = players
        this.currentPlayer = currentPlayer
        notifyDataSetChanged()
    }
}