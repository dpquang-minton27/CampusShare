package com.example.baocaocuoiky.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.model.PostStatus
import com.example.baocaocuoiky.data.model.PostType
import com.example.baocaocuoiky.databinding.FragmentSearchBinding
import java.text.Normalizer
import java.util.Locale

/** Search & Filter (Figma node 16:10). Layout IDs from the existing Fragment remain stable. */
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PostGridAdapter

    private var keyword = ""
    private var filterType: String? = null
    private var filterCategory: String? = null
    private var minCondition = 0
    private var showCompleted = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PostGridAdapter(emptyList()) { post ->
            startActivity(
                android.content.Intent(
                    requireContext(),
                    com.example.baocaocuoiky.ui.detail.PostDetailActivity::class.java
                ).apply {
                    putExtra("EXTRA_POST_ID", post.postId)
                }
            )
        }
        binding.rvSearchResults.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSearchResults.adapter = adapter

        // Thiết lập mặc định khi mở tab Tìm kiếm
        binding.edtSearchQuery.setText("")

        // Mặc định chọn tất cả danh mục
        binding.chipGroupSearchCat.check(R.id.chipSearchCatAll)

        // Không giới hạn hình thức: GIVE_AWAY hoặc EXCHANGE
        binding.chipGroupType.clearCheck()

        // Chỉ hiển thị những bài đăng còn đồ
        binding.chipGroupStatus.check(R.id.chipStatusAvailable)

        // Không giới hạn phần trăm tình trạng sử dụng
        binding.sliderCondition.value = 0f
        binding.chipGroupCondition.clearCheck()
        binding.tvConditionLabel.text = "0%"

        // The design depicts a filtered example: "Giáo trình", GIVE_AWAY, AVAILABLE, 70%.
        // Initialize the logic from the actual XML controls so UI and results stay in sync.
        keyword = binding.edtSearchQuery.text?.toString().orEmpty()
        filterType = when (binding.chipGroupType.checkedChipId) {
            R.id.chipTypeGive -> PostType.GIVE_AWAY
            R.id.chipTypeExchange -> PostType.EXCHANGE
            else -> null
        }
        filterCategory = when (binding.chipGroupSearchCat.checkedChipId) {
            R.id.chipSearchCatBooks -> "books"
            R.id.chipSearchCatTech -> "tech"
            R.id.chipSearchCatDorm -> "dorm"
            R.id.chipSearchCatStudy -> "study"
            else -> null
        }
        showCompleted = binding.chipGroupStatus.checkedChipId == R.id.chipStatusCompleted
        minCondition = binding.sliderCondition.value.toInt()
        binding.tvConditionLabel.text = "$minCondition%"
        binding.btnClearSearch.visibility = if (keyword.isBlank()) View.GONE else View.VISIBLE

        binding.edtSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                keyword = s?.toString().orEmpty()
                binding.btnClearSearch.visibility =
                    if (keyword.isBlank()) View.GONE else View.VISIBLE
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        binding.btnClearSearch.setOnClickListener { binding.edtSearchQuery.setText("") }

        binding.btnToggleFilters.setOnClickListener {
            val expanding = binding.filterCard.visibility != View.VISIBLE
            binding.filterCard.visibility = if (expanding) View.VISIBLE else View.GONE
            binding.btnToggleFilters.contentDescription =
                if (expanding) "Ẩn bộ lọc" else "Hiện bộ lọc"
        }

        binding.chipGroupType.setOnCheckedStateChangeListener { _, checkedIds ->
            filterType = when (checkedIds.firstOrNull()) {
                R.id.chipTypeGive -> PostType.GIVE_AWAY
                R.id.chipTypeExchange -> PostType.EXCHANGE
                else -> null // Neither sharing type selected: show both.
            }
            applyFilters()
        }
        binding.chipGroupSearchCat.setOnCheckedStateChangeListener { _, checkedIds ->
            filterCategory = when (checkedIds.firstOrNull()) {
                R.id.chipSearchCatBooks -> "books"
                R.id.chipSearchCatTech -> "tech"
                R.id.chipSearchCatDorm -> "dorm"
                R.id.chipSearchCatStudy -> "study"
                else -> null
            }
            applyFilters()
        }
        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            showCompleted = checkedIds.firstOrNull() == R.id.chipStatusCompleted
            applyFilters()
        }

        binding.sliderCondition.addOnChangeListener { _, value, _ ->
            minCondition = value.toInt()
            binding.tvConditionLabel.text = "$minCondition%"
            updateConditionPreset()
            applyFilters()
        }
        binding.chipGroupCondition.setOnCheckedStateChangeListener { _, checkedIds ->
            val chosenValue = when (checkedIds.firstOrNull()) {
                R.id.chipCondition70 -> 70
                R.id.chipCondition80 -> 80
                R.id.chipCondition90 -> 90
                else -> null
            }
            if (chosenValue != null && chosenValue != minCondition) {
                binding.sliderCondition.value = chosenValue.toFloat()
            } else if (chosenValue == null) {
                // Tapping an already-selected preset should not leave it visually
                // off while the slider is still at the same exact preset value.
                updateConditionPreset()
            }
        }

        updateConditionPreset()
        applyFilters()
    }

    override fun onResume() {
        super.onResume()
        // Refresh the in-memory list after returning from a post detail screen.
        if (_binding != null && ::adapter.isInitialized) applyFilters()
    }

    private fun updateConditionPreset() {
        val preset = when (minCondition) {
            70 -> R.id.chipCondition70
            80 -> R.id.chipCondition80
            90 -> R.id.chipCondition90
            else -> View.NO_ID
        }
        if (preset == View.NO_ID) binding.chipGroupCondition.clearCheck()
        else if (binding.chipGroupCondition.checkedChipId != preset) {
            binding.chipGroupCondition.check(preset)
        }
    }

    private fun applyFilters() {
        val cleanQuery = normalizeText(keyword)
        val results = AppDataStore.posts.asSequence()
            .filter { post ->
                if (showCompleted) {
                    post.postStatus == PostStatus.TAKEN || post.postStatus == PostStatus.EXCHANGED
                } else {
                    post.postStatus == PostStatus.AVAILABLE
                }
            }
            .filter { filterType == null || it.postType == filterType }
            .filter { filterCategory == null || it.categoryId == filterCategory }
            .filter { it.conditionPercent >= minCondition }
            .filter {
                cleanQuery.isEmpty() ||
                    normalizeText(it.title).contains(cleanQuery) ||
                    normalizeText(it.description).contains(cleanQuery)
            }
            .sortedByDescending { it.createdAt }
            .toList()

        adapter.updateData(results)
        binding.tvResultCount.text = "Tìm thấy ${results.size} kết quả"
        binding.tvEmptySearch.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE

        // The initial badge is 3: category + GIVE_AWAY + condition >= 70%.
        // AVAILABLE is the default status and is not counted as an extra filter.
        val activeFilters =
            (if (filterCategory != null) 1 else 0) +
            (if (filterType != null) 1 else 0) +
            (if (minCondition > 0) 1 else 0) +
            (if (showCompleted) 1 else 0)
        binding.tvActiveFilterCount.text = activeFilters.toString()
        binding.tvActiveFilterCount.visibility =
            if (activeFilters == 0) View.GONE else View.VISIBLE
    }

    private fun normalizeText(input: String): String =
        Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .lowercase(Locale.ROOT)

    override fun onDestroyView() {
        binding.rvSearchResults.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
