package app.web.realcanvas

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import app.web.realcanvas.models.GameState
import app.web.realcanvas.viewmodels.GameViewModel

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var viewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[GameViewModel::class.java]
        observe()
    }

    private fun observe() {
        viewModel.gameState.observe(this) {
            goTo(it)
        }
    }

    private fun goTo(where: GameState) {
        when (where) {
            GameState.LOBBY -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.action_homeFragment_to_lobbyFragment)
            }
            GameState.OUT -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.action_lobbyFragment_to_homeFragment)
            }
            else -> {
                Log.d(TAG, "goTo: Could not find where to go, so 'out'")
                findNavController(R.id.nav_host_fragment).navigate(R.id.action_lobbyFragment_to_homeFragment)
            }
        }
    }
}