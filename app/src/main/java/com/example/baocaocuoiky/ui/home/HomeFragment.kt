package com.example.baocaocuoiky.ui.home

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.PopupMenu
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
import java.text.Normalizer
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PostGridAdapter

    private var selectedCategoryId: String? = null
    // Chi thay doi sau khi nhan nut Tim (hoac phim Search tren ban phim).
    private var appliedQuery: String = ""

    companion object {
        private const val STATE_APPLIED_QUERY = "home_applied_query"
        private const val STATE_TYPED_QUERY = "home_typed_query"
        private const val STATE_CATEGORY_ID = "home_category_id"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appliedQuery = savedInstanceState?.getString(STATE_APPLIED_QUERY).orEmpty()
        selectedCategoryId = savedInstanceState?.getString(STATE_CATEGORY_ID)
        binding.edtHomeSearch.setText(
            savedInstanceState?.getString(STATE_TYPED_QUERY) ?: appliedQuery
        )

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

        // Tim truc tiep tren Home, khong chuyen Fragment.
        binding.btnHomeSearch.setOnClickListener { performHomeSearch() }
        binding.edtHomeSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performHomeSearch()
                true
            } else {
                false
            }
        }

        // Nut loc o Home chi loc danh muc Home, khong chuyen sang SearchFragment.
        binding.btnFilterShortcut.setOnClickListener { showHomeCategoryFilter() }

        binding.catBooks.setOnClickListener { selectCategory("books") }
        binding.catTech.setOnClickListener { selectCategory("tech") }
        binding.catDorm.setOnClickListener { selectCategory("dorm") }
        binding.catPersonal.setOnClickListener { selectCategory("study") }
        binding.catAll.setOnClickListener { selectCategory(null) }

        selectCategory(selectedCategoryId)
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null && ::adapter.isInitialized) loadPosts()
    }

    private fun performHomeSearch() {
        appliedQuery = binding.edtHomeSearch.text?.toString()?.trim().orEmpty()
        loadPosts()

        // Dong ban phim de nguoi dung xem ket qua ngay ben duoi.
        val keyboard = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.hideSoftInputFromWindow(binding.edtHomeSearch.windowToken, 0)
        binding.edtHomeSearch.clearFocus()
        binding.root.requestFocus()
    }

    private fun showHomeCategoryFilter() {
        val options = listOf(
            null to "Tất cả danh mục",
            "books" to "Giáo trình",
            "tech" to "Thiết bị",
            "dorm" to "Đồ KTX",
            "study" to "Đồ cá nhân"
        )
        PopupMenu(requireContext(), binding.btnFilterShortcut).apply {
            options.forEachIndexed { index, (_, label) ->
                menu.add(0, index + 1, index, label)
            }
            setOnMenuItemClickListener { item ->
                selectCategory(options[item.itemId - 1].first)
                true
            }
            show()
        }
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

        bg.setBackgroundResource(
            if (active) R.drawable.bg_circle_blue_solid else R.drawable.bg_circle_white_border
        )
        icon.imageTintList = ColorStateList.valueOf(if (active) white else blue)
        label.setTextColor(if (active) blue else gray)
    }

    private fun loadPosts() {
        val searchTerm = normalizeText(appliedQuery)
        val list = AppDataStore.posts
            .filter { it.postStatus == PostStatus.AVAILABLE }
            .filter { selectedCategoryId == null || it.categoryId == selectedCategoryId }
            .filter {
                searchTerm.isEmpty() ||
                    normalizeText(it.title).contains(searchTerm) ||
                    normalizeText(it.description).contains(searchTerm)
            }
            .sortedByDescending { it.createdAt }

        adapter.updateData(list)
        binding.tvHomeSectionTitle.text = if (searchTerm.isEmpty()) {
            "Tin mới đăng"
        } else {
            "Kết quả tìm kiếm (${list.size})"
        }
        binding.tvEmpty.text = if (searchTerm.isEmpty()) {
            "Chưa có món đồ nào trong danh mục này."
        } else {
            "Không tìm thấy món đồ phù hợp với \"$appliedQuery\"."
        }
        binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun normalizeText(input: String): String {
        return Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .lowercase(Locale.ROOT)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_APPLIED_QUERY, appliedQuery)
        outState.putString(STATE_CATEGORY_ID, selectedCategoryId)
        outState.putString(STATE_TYPED_QUERY, _binding?.edtHomeSearch?.text?.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
