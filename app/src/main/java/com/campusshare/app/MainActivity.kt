package com.campusshare.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.campusshare.app.databinding.ActivityMainBinding
import com.campusshare.app.data.CampusShareData
import com.campusshare.app.ui.auth.AuthActivity
import com.campusshare.app.ui.auth.ProfileActivity

class MainActivity : Activity() {

    private var stopObserving: (() -> Unit)? = null

    private lateinit var binding: ActivityMainBinding
    private lateinit var navigationButtons: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (CampusShareData.authRepository.currentUser == null) {
            openLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigationButtons = listOf(
            binding.navHome,
            binding.navSearch,
            binding.navPost,
            binding.navRequests,
            binding.navProfile
        )

        Section.entries.forEachIndexed { index, section ->
            navigationButtons[index].setOnClickListener {
                showSection(section)
            }
        }

        showSection(Section.HOME)
    }

    override fun onResume() {
        super.onResume()

        if (isFinishing) return

        if (CampusShareData.authRepository.currentUser == null) {
            openLogin()
            return
        }

        stopObserving?.invoke()

        stopObserving =
            CampusShareData.authRepository.observeAuthState {
                val user = CampusShareData.authRepository.currentUser

                if (user == null) {
                    openLogin()
                } else {
                    binding.sessionUser.text =
                        getString(
                            R.string.signed_in_as,
                            user.fullName
                        )
                }
            }
    }

    override fun onPause() {
        stopObserving?.invoke()
        stopObserving = null

        super.onPause()
    }

    private fun openLogin() {
        val intent = Intent(this, AuthActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        }

        startActivity(intent)
        finish()
    }

    private fun showSection(section: Section) {
        if (section == Section.PROFILE) {
            startActivity(
                Intent(this, ProfileActivity::class.java)
            )
            return
        }

        binding.sectionTitle.setText(section.titleRes)
        binding.sectionDescription.setText(section.descriptionRes)

        navigationButtons.forEachIndexed { index, button ->
            button.isEnabled = Section.entries[index] != section
        }
    }

    private enum class Section(
        val titleRes: Int,
        val descriptionRes: Int
    ) {
        HOME(
            R.string.nav_home,
            R.string.home_description
        ),
        SEARCH(
            R.string.nav_search,
            R.string.search_description
        ),
        POST(
            R.string.nav_post,
            R.string.post_description
        ),
        REQUESTS(
            R.string.nav_requests,
            R.string.requests_description
        ),
        PROFILE(
            R.string.nav_profile,
            R.string.profile_description
        )
    }
}
