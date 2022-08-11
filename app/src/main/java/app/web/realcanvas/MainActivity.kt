package app.web.realcanvas

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
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

        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        viewModel = ViewModelProvider(this)[GameViewModel::class.java]
        observe()
    }

    private fun observe() {
        viewModel.lobby.observe(this) { it?.gameState?.let { it1 -> goTo(it1) } }
        viewModel.toast.observe(this) {
            if (it != null) Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun goTo(gameState: GameState) {
        val currentFragID = navController.currentDestination?.id ?: return
        when (currentFragID) {
            R.id.homeFragment -> {
                if (gameState == GameState.LOBBY)
                    navController.navigate(R.id.action_homeFragment_to_lobbyFragment)
            }
            R.id.lobbyFragment -> {
                if (gameState == GameState.IN_GAME)
                    navController.navigate(R.id.action_lobbyFragment_to_gameFragment)
                else if (gameState == GameState.OUT) {
                    // disconnect
                    navController.navigate(R.id.action_lobbyFragment_to_homeFragment)
                }
            }
            R.id.gameFragment -> {
                if (gameState == GameState.LOBBY)
                    navController.navigate(R.id.action_gameFragment_to_lobbyFragment)
                else if (gameState == GameState.OUT) {
                    // disconnect
                    navController.navigate(R.id.action_gameFragment_to_homeFragment)
                }
            }
        }
    }
}