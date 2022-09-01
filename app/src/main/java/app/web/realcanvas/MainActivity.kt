package app.web.realcanvas

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import app.web.realcanvas.models.Screen
import app.web.realcanvas.viewmodels.GameViewModel
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

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

    override fun onBackPressed() {
        val currentFragID = navController.currentDestination?.id ?: return
        if (currentFragID == R.id.homeFragment) {
            super.onBackPressed()
            return
        }
        showLeaveDialog()
    }

    private fun showLeaveDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.exit_lobby))
            .setMessage(getString(R.string.exit_lobby_message))
            .setPositiveButton(android.R.string.ok) { _, _ -> disconnect() }
            .setNegativeButton(android.R.string.cancel, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun disconnect() {
        val currentFragID = navController.currentDestination?.id ?: return
        viewModel.disconnect()
        if (currentFragID == R.id.gameFragment) {
            navController.navigate(R.id.action_gameFragment_to_homeFragment)
        } else if (currentFragID == R.id.lobbyFragment) {
            navController.navigate(R.id.action_lobbyFragment_to_homeFragment)
        }
    }

    private fun observe() {
        viewModel.screen.observe(this) { goTo(it) }
        viewModel.toast.observe(this) {
            if (it != null)
                Snackbar.make(findViewById(R.id.content), it, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun goTo(screen: Screen) {
        val currentFragID = navController.currentDestination?.id ?: return
        when (currentFragID) {
            R.id.homeFragment -> {
                if (screen == Screen.LOBBY)
                    navController.navigate(R.id.action_homeFragment_to_lobbyFragment)
                else if (screen == Screen.GAME)
                    navController.navigate(R.id.action_homeFragment_to_gameFragment)
            }
            R.id.lobbyFragment -> {
                if (screen == Screen.GAME)
                    navController.navigate(R.id.action_lobbyFragment_to_gameFragment)
                else if (screen == Screen.HOME)
                    navController.navigate(R.id.action_lobbyFragment_to_homeFragment)
            }
            R.id.gameFragment -> {
                if (screen == Screen.LOBBY)
                    navController.navigate(R.id.action_gameFragment_to_lobbyFragment)
                else if (screen == Screen.HOME)
                    navController.navigate(R.id.action_gameFragment_to_homeFragment)
            }
        }
    }
}