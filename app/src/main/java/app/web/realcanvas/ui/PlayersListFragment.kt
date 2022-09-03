package app.web.realcanvas.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.web.realcanvas.R
import app.web.realcanvas.ui.adapters.LobbyAdapter
import app.web.realcanvas.viewmodels.GameViewModel

class PlayersListFragment : Fragment() {
    private lateinit var gameViewModel: GameViewModel
    private lateinit var rvLobby: RecyclerView
    private lateinit var lobbyAdapter: LobbyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_players_list, container, false)
        gameViewModel = ViewModelProvider(requireActivity())[GameViewModel::class.java]
        initUi(view)
        observe()
        return view
    }

    private fun observe() {
        gameViewModel.currentLobby.observe(viewLifecycleOwner) {
            if (it != null) {
                val list = gameViewModel.currentLobby.value!!.players.values
                    .sortedBy { player -> player.score }
                lobbyAdapter.updatePlayers(
                    list,
                    // purposely not using gameviewmodel.currentPlayer, as it is updated after setting live data
                    gameViewModel.currentLobby.value?.players?.get(gameViewModel.currentPlayer?.userName)
                )
            }
        }
    }

    private fun initUi(view: View) {
        rvLobby = view.findViewById(R.id.rv_lobby)
        lobbyAdapter = LobbyAdapter(listOf(), gameViewModel.currentPlayer)
        rvLobby.adapter = lobbyAdapter
        rvLobby.layoutManager = LinearLayoutManager(context)
        rvLobby.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }
}