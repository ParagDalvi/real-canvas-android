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
    private lateinit var cardIsNotDrawingAndChoosing: MaterialCardView
    private lateinit var cardIsDrawingAndChoosing: MaterialCardView
    private lateinit var cardCommonWhatWasTheWord: MaterialCardView
    private lateinit var tvPlayerChoosing: TextView
    private lateinit var tvCommonWordAns: TextView
    private lateinit var tvTimerOnPaintCanvas: TextView
    private lateinit var tvTimerWhenChoosing: TextView
    private lateinit var temp: TextView
    private lateinit var cardPaintingCanvas: MaterialCardView
    private lateinit var tvWord1: TextView
    private lateinit var cardWord1: MaterialCardView
    private lateinit var tvWord2: TextView
    private lateinit var cardWord2: MaterialCardView
    private lateinit var tvWord3: TextView
    private lateinit var cardWord3: MaterialCardView

    private var currentlySelectedWord = 1

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

        temp.text = gameViewModel.currentPlayer.toString()
        val isDrawing = gameViewModel.currentPlayer!!.isDrawing
        if (isDrawing) {
            if (gameViewModel.currentLobby.value!!.whatsHappening == WhatsHappening.CHOOSING) {
                isDrawingAndChoosingUi()
            } else {
                isDrawingAndDrawingUi()
            }
        } else {
            if (gameViewModel.currentLobby.value!!.whatsHappening == WhatsHappening.CHOOSING) {
                isNotDrawingAndChoosingUi()
            } else {
                isNotDrawingAndGuessingUi()
            }
        }

        if (gameViewModel.currentLobby.value!!.whatsHappening == WhatsHappening.DRAWING && gameViewModel.currentLobby.value!!.timer.toInt() == 1) {
            // drawing -> new player choosing
            showCommonWhatWasTheWordCard()
            paintView.setIsDrawing(false)
            paintView.reset()
        }

        if (gameViewModel.currentLobby.value!!.whatsHappening == WhatsHappening.CHOOSING && gameViewModel.currentLobby.value!!.timer.toInt() == 1) {
            // choosing -> drawing
            gameViewModel.updateSelectedWord(currentlySelectedWord)
        }
    }

    private fun showCommonWhatWasTheWordCard() {
        cardPaintingCanvas.visibility = View.GONE
        cardIsNotDrawingAndChoosing.visibility = View.GONE
        cardIsDrawingAndChoosing.visibility = View.GONE
        cardCommonWhatWasTheWord.visibility = View.VISIBLE
        val string = "Word was ${gameViewModel.currentLobby.value!!.selectedWord}"
        tvCommonWordAns.text = string
    }

    private fun isDrawingAndChoosingUi() {
        cardPaintingCanvas.visibility = View.GONE
        cardIsNotDrawingAndChoosing.visibility = View.GONE
        cardCommonWhatWasTheWord.visibility = View.GONE
        cardIsDrawingAndChoosing.visibility = View.VISIBLE
        tvWord1.text = gameViewModel.currentLobby.value!!.words[0]
        tvWord2.text = gameViewModel.currentLobby.value!!.words[1]
        tvWord3.text = gameViewModel.currentLobby.value!!.words[2]
        tvTimerWhenChoosing.text = gameViewModel.currentLobby.value!!.timer.toString()
    }

    private fun isNotDrawingAndChoosingUi() {
        cardIsDrawingAndChoosing.visibility = View.GONE
        cardPaintingCanvas.visibility = View.GONE
        cardCommonWhatWasTheWord.visibility = View.GONE
        cardIsNotDrawingAndChoosing.visibility = View.VISIBLE
        val name =
            gameViewModel.currentLobby.value!!.players.values.filter { it.isDrawing }[0].userName
        val string = "$name is choosing a word"
        tvPlayerChoosing.text = string
        tvTimer.text = gameViewModel.currentLobby.value!!.timer.toString()
    }

    private fun isNotDrawingAndGuessingUi() {
        cardIsDrawingAndChoosing.visibility = View.GONE
        cardIsNotDrawingAndChoosing.visibility = View.GONE
        cardCommonWhatWasTheWord.visibility = View.GONE
        cardPaintingCanvas.visibility = View.VISIBLE
        tvTimerOnPaintCanvas.text = gameViewModel.currentLobby.value!!.timer.toString()
        paintView.setIsDrawing(false)
    }

    private fun isDrawingAndDrawingUi() {
        cardIsDrawingAndChoosing.visibility = View.GONE
        cardIsNotDrawingAndChoosing.visibility = View.GONE
        cardCommonWhatWasTheWord.visibility = View.GONE
        cardPaintingCanvas.visibility = View.VISIBLE
        tvTimerOnPaintCanvas.text = gameViewModel.currentLobby.value!!.timer.toString()
        paintView.setIsDrawing(true)
    }

    private fun initUi(view: View) {
        tvTimer = view.findViewById(R.id.tv_timer_when_choosing)
        paintView = view.findViewById(R.id.paint_view)
        paintView.init(gameViewModel::sendDrawingPath)
        cardIsNotDrawingAndChoosing = view.findViewById(R.id.card_not_drawing_and_choosing)
        tvPlayerChoosing = view.findViewById(R.id.tv_player_choosing)
        cardPaintingCanvas = view.findViewById(R.id.painting_canvas)
        temp = view.findViewById(R.id.temp)
        cardIsDrawingAndChoosing = view.findViewById(R.id.card_drawing_and_choosing)
        cardCommonWhatWasTheWord = view.findViewById(R.id.card_common_what_was_the_word)
        tvCommonWordAns = view.findViewById(R.id.tv_common_word_ans)
        tvTimerOnPaintCanvas = view.findViewById(R.id.tv_timer_on_paint_canvas)
        tvTimerWhenChoosing = view.findViewById(R.id.tv_timer_is_choosing)
        cardWord1 = view.findViewById(R.id.word1)
        cardWord1.setOnClickListener { updateSelectedWordCards(1) }
        tvWord1 = view.findViewById(R.id.tv_word1)
        cardWord2 = view.findViewById(R.id.word2)
        cardWord2.setOnClickListener { updateSelectedWordCards(2) }
        tvWord2 = view.findViewById(R.id.tv_word2)
        cardWord3 = view.findViewById(R.id.word3)
        cardWord3.setOnClickListener { updateSelectedWordCards(3) }
        tvWord3 = view.findViewById(R.id.tv_word3)
    }

    private fun updateSelectedWordCards(newVal: Int) {
        currentlySelectedWord = newVal
        when (currentlySelectedWord) {
            1 -> {
                cardWord1.isChecked = true
                cardWord2.isChecked = false
                cardWord3.isChecked = false
            }
            2 -> {
                cardWord1.isChecked = false
                cardWord2.isChecked = true
                cardWord3.isChecked = false
            }
            3 -> {
                cardWord1.isChecked = false
                cardWord2.isChecked = false
                cardWord3.isChecked = true
            }
        }
    }
}