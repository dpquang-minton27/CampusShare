package com.example.baocaocuoiky

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.baocaocuoiky.databinding.ActivityMainBinding
import com.example.baocaocuoiky.ui.detail.FavoritesFragment
import com.example.baocaocuoiky.ui.home.HomeFragment
import com.example.baocaocuoiky.ui.home.SearchFragment
import com.example.baocaocuoiky.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tự động đẩy toàn bộ giao diện xuống bên dưới Status Bar (pin, wifi, đồng hồ...)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }

        // Mặc định mở Trang chủ khi vào app
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // Bắt sự kiện bấm chuyển 4 tab dưới đáy
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_search -> SearchFragment()
                R.id.nav_favorites -> FavoritesFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            replaceFragment(selectedFragment)
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun openSearchTab() {
        binding.bottomNavigation.selectedItemId = R.id.nav_search
    }

    fun openFavoritesTab() {
        binding.bottomNavigation.selectedItemId = R.id.nav_favorites
    }
}