package app.web.realcanvas.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.web.realcanvas.R
import app.web.realcanvas.models.WhatsHappening
import app.web.realcanvas.viewmodels.GameViewModel
import com.google.android.material.card.MaterialCardView

class GameFragment : Fragment() {
    private lateinit var paintView: PaintView
    private lateinit var gameViewModel: GameViewModel
    private lateinit var tvTimer: TextView
    private lateinit var cardNoDrawChoosing: MaterialCardView
    private lateinit var tvPlayerChoosing: TextView
    private lateinit var viewNoDrawGuess: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)
        gameViewModel = ViewModelProvider(requireActivity())[GameViewModel::class.java]
        initUi(view)
        updateUiForCurrentPlayer()
        observe()
        return view
    }

    private fun observe() {
        gameViewModel.currentLobby.observe(viewLifecycleOwner) {
            updateUiForCurrentPlayer()
        }

        gameViewModel.drawingList.observe(viewLifecycleOwner) {
            if (gameViewModel.currentPlayer?.isDrawing == false) {
                paintView.updateDrawing(it)
            }
        }
    }

    private fun updateUiForCurrentPlayer() {
        if (gameViewModel.currentPlayer == null || gameViewModel.currentLobby.value == null) return

        val isDrawing = gameViewModel.currentPlayer!!.isDrawing
        if (isDrawing) {
            if (gameViewModel.currentLobby.value!!.whatsHappening == WhatsHappening.CHOOSING) {
                //todo: show dialog
            } else {
                isDrawingAndDrawingUi()
            }
        } else {
            if (gameViewModel.currentLobby.value!!.whatsHappening == WhatsHappening.CHOOSING) {
                isNotDrawingAndChoosingUi()
            } else {
                isNotDrawingAndDrawingUi()
            }
        }
    }

    private fun isNotDrawingAndChoosingUi() {
        viewNoDrawGuess.visibility = View.GONE

        cardNoDrawChoosing.visibility = View.VISIBLE
        val name =
            gameViewModel.currentLobby.value!!.players.values.filter { it.isDrawing }[0].userName
        val string = "$name is choosing a word"
        tvPlayerChoosing.text = string
        tvTimer.text = gameViewModel.currentLobby.value!!.timer.toString()
    }

    private fun isNotDrawingAndDrawingUi() {
        cardNoDrawChoosing.visibility = View.GONE
        cardNoDrawChoosing.visibility = View.VISIBLE
    }

    private fun isDrawingAndDrawingUi() {

    }

    private fun initUi(view: View) {
        tvTimer = view.findViewById(R.id.tv_timer)
        paintView = view.findViewById(R.id.paint_view)
        paintView.init(gameViewModel::sendDrawingPath)
        cardNoDrawChoosing = view.findViewById(R.id.card_no_draw_choosing)
        tvPlayerChoosing = view.findViewById(R.id.tv_player_choosing)
        viewNoDrawGuess = view.findViewById(R.id.view_no_draw_guessing)
    }
}