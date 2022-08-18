package app.web.realcanvas.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.web.realcanvas.R
import app.web.realcanvas.viewmodels.GameViewModel

class GameFragment : Fragment() {
    private lateinit var paintView: PaintView
    private lateinit var gameViewModel: GameViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)
        gameViewModel = ViewModelProvider(requireActivity())[GameViewModel::class.java]
        initUi(view)
        return view
    }

    private fun initUi(view: View) {
        paintView = view.findViewById(R.id.paint_view)
        paintView.init(gameViewModel::sendDrawingPath)
    }
}