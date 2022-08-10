package app.web.realcanvas

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import app.web.realcanvas.models.GameState
import app.web.realcanvas.viewmodels.GameViewModel

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var viewModel: GameViewModel
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        viewModel = ViewModelProvider(this)[GameViewModel::class.java]
        observe()
    }

    private fun observe() {
        viewModel.gameState.observe(this) { goTo(it) }
        viewModel.toast.observe(this) {
            if (it != null) Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun goTo(where: GameState) {
        when (where) {
            GameState.LOBBY -> {
                navController.navigate(R.id.action_homeFragment_to_lobbyFragment)
            }
            GameState.OUT -> {
                // todo: disconnect and remove data
                onBackPressed()
            }
            else -> {

            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}