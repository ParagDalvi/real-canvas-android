package app.web.realcanvas.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import app.web.realcanvas.R
import app.web.realcanvas.models.Player
import app.web.realcanvas.viewmodels.GameViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textview.MaterialTextView


class LobbyFragment : Fragment() {
    private lateinit var gameViewModel: GameViewModel
    private lateinit var btnStart: Button
    private lateinit var btnCopy: ImageButton
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var tvLobbyCode: MaterialTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lobby, container, false)
        gameViewModel = ViewModelProvider(requireActivity())[GameViewModel::class.java]
        initUi(view)
        updateUiIfAdmin(gameViewModel.currentPlayer)
        observe()
        return view
    }

    private fun observe() {
        gameViewModel.currentLobby.observe(viewLifecycleOwner) {
            updateUiIfAdmin(gameViewModel.currentPlayer)
        }
        gameViewModel.newMessage.observe(viewLifecycleOwner) { showBadgeIfRequired() }
    }

    private fun showBadgeIfRequired() {
        val currentTab = tabLayout.selectedTabPosition
        if (currentTab != 1) {
            val badge = tabLayout.getTabAt(1)?.orCreateBadge
            badge?.backgroundColor = ContextCompat.getColor(requireContext(), R.color.white)
        }
    }

    private fun updateUiIfAdmin(currentPlayer: Player?) {
        if (currentPlayer == null) return

        if (currentPlayer.isAdmin) btnStart.visibility = View.VISIBLE
        else btnStart.visibility = View.GONE
    }

    private fun initUi(view: View) {
        tvLobbyCode = view.findViewById(R.id.tv_lobby_code)
        tvLobbyCode.text = getLobbyText()
        btnStart = view.findViewById(R.id.btn_start)
        btnStart.setOnClickListener { startGame() }
        btnCopy = view.findViewById(R.id.btn_copy)
        btnCopy.setOnClickListener { copy() }
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewpager)

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2

            override fun createFragment(position: Int): Fragment {
                return if (position == 0) PlayersListFragment() else MessageFragment()
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            when (pos) {
                0 -> {
                    tab.text = "Players"
                }
                1 -> {
                    tab.text = "Messages"
                }
            }
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 1) {
                    tab.removeBadge()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun copy() {
        if (gameViewModel.currentLobby.value == null) return
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", gameViewModel.currentLobby.value?.id)
        clipboard.setPrimaryClip(clip)
        gameViewModel.showToast("Copied!")
    }

    private fun getLobbyText(): String? {
        if (gameViewModel.currentLobby.value == null) return null
        return "Lobby code: ${gameViewModel.currentLobby.value!!.id}"
    }

    private fun startGame() {
//        if (gameViewModel.currentLobby?.players?.size!! < 2) {
//            Toast.makeText(context, "Need at least 2 players", Toast.LENGTH_SHORT).show()
//            return
//        }

        gameViewModel.startGame()
    }
}