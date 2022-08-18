package app.web.realcanvas.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.web.realcanvas.R
import app.web.realcanvas.models.Lobby
import app.web.realcanvas.models.WhatsHappening
import app.web.realcanvas.viewmodels.GameViewModel

class GameFragment : Fragment() {
    private lateinit var paintView: PaintView
    private lateinit var gameViewModel: GameViewModel
    private lateinit var tvTimer: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)
        gameViewModel = ViewModelProvider(requireActivity())[GameViewModel::class.java]
        initUi(view)
        setDrawingFlagForCurrentPlayer()
        observe()
        return view
    }

    private fun observe() {
        gameViewModel.update.observe(viewLifecycleOwner) {
            when (it) {
                Lobby.players -> handlePlayerChange()

                Lobby.timer -> handleTimerChange()

                Lobby.all -> {
                    handleTimerChange()
                    handlePlayerChange()
                }
            }
        }

        gameViewModel.drawingList.observe(viewLifecycleOwner) {
            if (gameViewModel.currentPlayer?.isDrawing == false) {
                paintView.updateDrawing(it)
            }
        }
    }

    private fun handleTimerChange() {
        tvTimer.text = gameViewModel.currentLobby?.timer.toString()
    }

    private fun handlePlayerChange() {
        setDrawingFlagForCurrentPlayer()
        if (gameViewModel.currentLobby?.whatsHappening == WhatsHappening.CHOOSING) {
            paintView.reset()
        }
    }

    private fun setDrawingFlagForCurrentPlayer() {
        if (gameViewModel.currentPlayer?.isDrawing == true) {
            paintView.setIsDrawing(true)
        } else {
            paintView.setIsDrawing(false)
        }
    }

    private fun initUi(view: View) {
        tvTimer = view.findViewById(R.id.tv_timer)
        paintView = view.findViewById(R.id.paint_view)
        paintView.init(gameViewModel::sendDrawingPath)
    }
}