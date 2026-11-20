package com.example.baocaocuoiky.ui.home

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.baocaocuoiky.MainActivity
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.model.PostStatus
import com.example.baocaocuoiky.databinding.FragmentHomeBinding
import com.example.baocaocuoiky.ui.detail.PostDetailActivity
import com.example.baocaocuoiky.ui.detail.RequestsActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PostGridAdapter
    private var selectedCategoryId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PostGridAdapter(emptyList()) { post ->
            startActivity(Intent(requireContext(), PostDetailActivity::class.java).apply {
                putExtra("EXTRA_POST_ID", post.postId)
            })
        }
        binding.rvHomePosts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvHomePosts.adapter = adapter

        binding.btnCreatePost.setOnClickListener {
            startActivity(Intent(requireContext(), CreatePostActivity::class.java))
        }
        binding.btnNotifications.setOnClickListener {
            startActivity(Intent(requireContext(), RequestsActivity::class.java).apply {
                putExtra("EXTRA_INITIAL_TAB", 1)
            })
        }
        binding.btnHeaderFavorites.setOnClickListener {
            (activity as? MainActivity)?.openFavoritesTab()
        }
        binding.cardSearchShortcut.setOnClickListener {
            (activity as? MainActivity)?.openSearchTab()
        }
        binding.btnFilterShortcut.setOnClickListener {
            (activity as? MainActivity)?.openSearchTab()
        }

        binding.catBooks.setOnClickListener { selectCategory("books") }
        binding.catTech.setOnClickListener { selectCategory("tech") }
        binding.catDorm.setOnClickListener { selectCategory("dorm") }
        binding.catPersonal.setOnClickListener { selectCategory("study") }
        binding.catAll.setOnClickListener { selectCategory(null) }

        selectCategory(null)
    }

    override fun onResume() {
        super.onResume()
        loadPosts()
    }

    private fun selectCategory(catId: String?) {
        selectedCategoryId = catId
        setCatActive(binding.bgCatBooks, binding.icCatBooks, binding.tvCatBooks, catId == "books")
        setCatActive(binding.bgCatTech, binding.icCatTech, binding.tvCatTech, catId == "tech")
        setCatActive(binding.bgCatDorm, binding.icCatDorm, binding.tvCatDorm, catId == "dorm")
        setCatActive(binding.bgCatPersonal, binding.icCatPersonal, binding.tvCatPersonal, catId == "study")
        setCatActive(binding.bgCatAll, binding.icCatAll, binding.tvCatAll, catId == null)
        loadPosts()
    }

    private fun setCatActive(bg: View, icon: ImageView, label: TextView, active: Boolean) {
        val blue = ContextCompat.getColor(requireContext(), R.color.primary_blue)
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val gray = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        bg.setBackgroundResource(if (active) R.drawable.bg_circle_blue_solid else R.drawable.bg_circle_white_border)
        icon.imageTintList = ColorStateList.valueOf(if (active) white else blue)
        label.setTextColor(if (active) blue else gray)
    }

    private fun loadPosts() {
        val list = AppDataStore.posts
            .filter { it.postStatus == PostStatus.AVAILABLE }
            .filter { selectedCategoryId == null || it.categoryId == selectedCategoryId }
            .sortedByDescending { it.createdAt }

        adapter.updateData(list)
        binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
