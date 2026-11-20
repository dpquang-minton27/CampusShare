package com.example.baocaocuoiky.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.model.PostStatus
import com.example.baocaocuoiky.data.model.PostType
import com.example.baocaocuoiky.databinding.FragmentSearchBinding
import java.text.Normalizer
import java.util.Locale

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PostGridAdapter

    private var keyword: String = ""
    private var filterType: String? = null
    private var filterCategory: String? = null
    private var minCondition: Int = 0

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
            startActivity(android.content.Intent(requireContext(), com.example.baocaocuoiky.ui.detail.PostDetailActivity::class.java).apply {
                putExtra("EXTRA_POST_ID", post.postId)
            })
        }

        binding.rvSearchResults.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSearchResults.adapter = adapter

        binding.edtSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                keyword = s?.toString().orEmpty()
                binding.btnClearSearch.visibility = if (keyword.isNotEmpty()) View.VISIBLE else View.GONE
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        binding.btnClearSearch.setOnClickListener {
            binding.edtSearchQuery.setText("")
        }

        binding.chipGroupType.setOnCheckedStateChangeListener { _, checkedIds ->
            filterType = when (checkedIds.firstOrNull()) {
                R.id.chipTypeGive -> PostType.GIVE_AWAY
                R.id.chipTypeExchange -> PostType.EXCHANGE
                else -> null
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

        binding.sliderCondition.addOnChangeListener { _, value, _ ->
            minCondition = value.toInt()
            binding.tvConditionLabel.text = "Độ mới từ: $minCondition%"
            applyFilters()
        }
    }

    override fun onResume() {
        super.onResume()
        applyFilters()
    }

    private fun applyFilters() {
        val cleanQuery = normalizeText(keyword)
        val results = AppDataStore.posts
            .filter { it.postStatus == PostStatus.AVAILABLE }
            .filter { filterType == null || it.postType == filterType }
            .filter { filterCategory == null || it.categoryId == filterCategory }
            .filter { it.conditionPercent >= minCondition }
            .filter {
                cleanQuery.isEmpty() ||
                        normalizeText(it.title).contains(cleanQuery) ||
                        normalizeText(it.description).contains(cleanQuery)
            }
            .sortedByDescending { it.createdAt }

        adapter.updateData(results)
        binding.tvResultCount.text = "Tìm thấy ${results.size} kết quả"
        binding.tvEmptySearch.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun normalizeText(input: String): String {
        return Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .lowercase(Locale.ROOT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}