package com.campusshare.app

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

/**
 * Khung điều hướng BC1. Các màn hình nghiệp vụ sẽ thay thế nội dung mẫu
 * khi từng nhánh chức năng được tích hợp vào develop.
 */
class MainActivity : Activity() {
    private lateinit var sectionTitle: TextView
    private lateinit var sectionDescription: TextView
    private lateinit var navigationButtons: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sectionTitle = findViewById(R.id.sectionTitle)
        sectionDescription = findViewById(R.id.sectionDescription)
        navigationButtons = listOf(
            findViewById(R.id.navHome),
            findViewById(R.id.navSearch),
            findViewById(R.id.navPost),
            findViewById(R.id.navRequests),
            findViewById(R.id.navProfile)
        )

        Section.entries.forEachIndexed { index, section ->
            navigationButtons[index].setOnClickListener { showSection(section) }
        }
        showSection(Section.HOME)
    }

    private fun showSection(section: Section) {
        sectionTitle.setText(section.titleRes)
        sectionDescription.setText(section.descriptionRes)
        navigationButtons.forEachIndexed { index, button ->
            button.isEnabled = Section.entries[index] != section
        }
    }

    private enum class Section(val titleRes: Int, val descriptionRes: Int) {
        HOME(R.string.nav_home, R.string.home_description),
        SEARCH(R.string.nav_search, R.string.search_description),
        POST(R.string.nav_post, R.string.post_description),
        REQUESTS(R.string.nav_requests, R.string.requests_description),
        PROFILE(R.string.nav_profile, R.string.profile_description)
    }
}
