package app.web.realcanvas.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.web.realcanvas.R
import app.web.realcanvas.models.*
import app.web.realcanvas.ui.adapters.GuessAdapter
import app.web.realcanvas.util.hideKeyboard
import app.web.realcanvas.viewmodels.GameViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class GameFragment : Fragment() {
    private lateinit var gameViewModel: GameViewModel

    private lateinit var tvTimer: TextView

    private lateinit var viewNotDrawingAndChoosing: View
    private lateinit var tvPlayerChoosing: TextView

    private lateinit var viewDrawingAndChoosing: View
    private lateinit var tvChooseWord: TextView
    private lateinit var cardWord1: MaterialCardView
    private lateinit var tvWord1: TextView
    private lateinit var cardWord2: MaterialCardView
    private lateinit var tvWord2: TextView
    private lateinit var cardWord3: MaterialCardView
    private lateinit var tvWord3: TextView

    private lateinit var drawingCanvas: View
    private lateinit var btnUndo: ImageButton
    private lateinit var btnClear: ImageButton
    private lateinit var btnColorPicker: ImageButton
    private lateinit var paintView: PaintView
    private lateinit var buttonsForDrawing: ConstraintLayout
    private lateinit var llSelectedWord: LinearLayout

    private lateinit var etGuess: TextInputLayout
    private lateinit var rvGuesses: RecyclerView

    private var currentlySelectedWordIndex: Int? = null
    private lateinit var guessAdapter: GuessAdapter
    private val maxChars = 100

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

        gameViewModel.drawingData.observe(viewLifecycleOwner) {
            if (gameViewModel.currentPlayer?.isDrawing == false && it != null) {
                when (it.doWhatWhenDrawing) {
                    DoWhatWhenDrawing.ADD -> paintView.add(
                        it,
                        paintView.width,
                        paintView.height,
                    )
                    DoWhatWhenDrawing.CLEAR -> paintView.clear()
                    DoWhatWhenDrawing.UNDO -> paintView.undo()
                }
            }
        }

        gameViewModel.newMessage.observe(viewLifecycleOwner) {
            if (it != null) {
                guessAdapter.addMessage(it)
                if (guessAdapter.messages.isNotEmpty())
                    lifecycleScope.launch { rvGuesses.smoothScrollToPosition(guessAdapter.messages.size - 1) }
            }
        }
    }

    private fun sendMessage() {
        val msg = etGuess.editText?.text.toString().trim()
        if (msg.length > maxChars) {
            gameViewModel.showToast("Message too long")
            return
        }
        etGuess.editText?.text?.clear()
        if (msg.isEmpty()) return
        if (gameViewModel.currentPlayer == null) return
        gameViewModel.sendMessage(
            Message(
                gameViewModel.currentPlayer!!.userName,
                MessageType.DEFAULT,
                msg
            )
        )
        hideKeyboard(context, activity?.currentFocus)
    }

    private fun updateUiForCurrentPlayer() {
        if (gameViewModel.currentPlayer == null || gameViewModel.currentLobby.value == null) return

        tvTimer.text = gameViewModel.currentLobby.value!!.timer.toString()
        val isDrawing = gameViewModel.currentPlayer!!.isDrawing
        // todo: can be optimised with checks
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
            paintView.reset()
        }
    }

    private fun showCommonWhatWasTheWordCard() {
        val string = "Word was ${gameViewModel.currentlySelectedWord}"
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }

    private fun isDrawingAndChoosingUi() {
        etGuess.visibility = View.GONE
        drawingCanvas.visibility = View.GONE
        viewDrawingAndChoosing.visibility = View.VISIBLE
        tvWord1.text = gameViewModel.currentLobby.value!!.words[0]
        tvWord2.text = gameViewModel.currentLobby.value!!.words[1]
        tvWord3.text = gameViewModel.currentLobby.value!!.words[2]
    }

    private fun isNotDrawingAndChoosingUi() {
        etGuess.visibility = View.GONE
        viewDrawingAndChoosing.visibility = View.GONE
        drawingCanvas.visibility = View.GONE
        viewNotDrawingAndChoosing.visibility = View.VISIBLE
        val name =
            gameViewModel.currentLobby.value!!.players.values.filter { it.isDrawing }
                .getOrNull(0)?.userName
        val string = "$name is choosing a word"
        tvPlayerChoosing.text = string
    }

    private fun isNotDrawingAndGuessingUi() {
        etGuess.visibility = View.VISIBLE
        viewDrawingAndChoosing.visibility = View.GONE
        viewNotDrawingAndChoosing.visibility = View.GONE
        drawingCanvas.visibility = View.VISIBLE
        showSelectedWord(false)
        buttonsForDrawing.visibility = View.GONE
        paintView.setIsDrawing(false)
    }

    private fun isDrawingAndDrawingUi() {
        etGuess.visibility = View.GONE
        viewDrawingAndChoosing.visibility = View.GONE
        viewNotDrawingAndChoosing.visibility = View.GONE
        drawingCanvas.visibility = View.VISIBLE
        showSelectedWord(true)
        buttonsForDrawing.visibility = View.VISIBLE
        paintView.setIsDrawing(true)
    }

    private fun initUi(view: View) {
        tvTimer = view.findViewById(R.id.tv_timer)

        viewNotDrawingAndChoosing = view.findViewById(R.id.view_not_drawing_and_choosing)
        tvPlayerChoosing = view.findViewById(R.id.tv_player_choosing)

        viewDrawingAndChoosing = view.findViewById(R.id.view_drawing_and_choosing)
        tvChooseWord = view.findViewById(R.id.tv_choose_word)
        cardWord1 = view.findViewById(R.id.word1)
        cardWord1.setOnClickListener { updateSelectedWordCards(1) }
        tvWord1 = view.findViewById(R.id.tv_word1)
        cardWord2 = view.findViewById(R.id.word2)
        cardWord2.setOnClickListener { updateSelectedWordCards(2) }
        tvWord2 = view.findViewById(R.id.tv_word2)
        cardWord3 = view.findViewById(R.id.word3)
        cardWord3.setOnClickListener { updateSelectedWordCards(3) }
        tvWord3 = view.findViewById(R.id.tv_word3)

        drawingCanvas = view.findViewById(R.id.drawing_canvas)
        btnUndo = view.findViewById(R.id.btn_undo)
        btnUndo.setOnClickListener { paintView.undo(true) }
        btnClear = view.findViewById(R.id.btn_clear)
        btnClear.setOnClickListener { paintView.clear(true) }
        btnColorPicker = view.findViewById(R.id.btn_color_picker)
        buttonsForDrawing = view.findViewById(R.id.buttons_for_drawing)
        paintView = view.findViewById(R.id.paint_view)
        paintView.init(this)
        paintView.addOnLayoutChangeListener { _, left, top, right, bottom, leftWas, topWas, rightWas, bottomWas ->
            if (paintView.visibility != View.VISIBLE) return@addOnLayoutChangeListener
            val widthWas = rightWas - leftWas
            val heightWas = bottomWas - topWas
            val widthNow = right - left
            val heightNow = bottom - top
            paintView.onLayoutChange(widthWas, widthNow, heightWas, heightNow)
            if (guessAdapter.messages.isNotEmpty())
                lifecycleScope.launch { rvGuesses.smoothScrollToPosition(guessAdapter.messages.size - 1) }
        }
        llSelectedWord = view.findViewById(R.id.ll_selected_word)

        etGuess = view.findViewById(R.id.et_guess)
        etGuess.setEndIconOnClickListener { sendMessage() }
        view.findViewById<TextInputEditText>(R.id.edit_text_guess)
            .setOnEditorActionListener { _, id, _ ->
                if (id == EditorInfo.IME_ACTION_SEND) {
                    sendMessage()
                    true
                } else false
            }
        rvGuesses = view.findViewById(R.id.rv_guesses)
        guessAdapter = GuessAdapter(mutableListOf())
        rvGuesses.adapter = guessAdapter
        rvGuesses.layoutManager = LinearLayoutManager(context)
    }

    private fun showSelectedWord(shouldShowWord: Boolean) {
        llSelectedWord.removeAllViews()
        gameViewModel.currentlySelectedWord?.forEach { char ->
            val view =
                layoutInflater.inflate(R.layout.layout_alphabet, view?.parent as ViewGroup, false)
            val tv: TextView = view.findViewById(R.id.tv_alphabet)
            val dash: View = view.findViewById(R.id.dash)

            if (char == ' ') dash.visibility = View.GONE
            else dash.visibility = View.VISIBLE

            if (shouldShowWord) {
                tv.visibility = View.VISIBLE
                tv.text = "$char"
            } else {
                tv.visibility = View.GONE
            }
            llSelectedWord.addView(view)
        }
    }

    private fun updateSelectedWordCards(newVal: Int) {
        if (currentlySelectedWordIndex == null || currentlySelectedWordIndex != newVal) {
            currentlySelectedWordIndex = newVal
            when (currentlySelectedWordIndex) {
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
            gameViewModel.updateSelectedWord(currentlySelectedWordIndex!!)
        }
    }

    fun sendDrawingToOthers(list: List<DrawPoints>, doWhatWhenDrawing: DoWhatWhenDrawing) {
        gameViewModel.sendDrawingPath(
            list,
            doWhatWhenDrawing,
            paintView.width,
            paintView.height
        )
    }
}