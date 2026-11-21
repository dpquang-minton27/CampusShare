package com.example.baocaocuoiky.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baocaocuoiky.MainActivity
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.databinding.FragmentFavoritesBinding

/** Figma 16:656: vertical favorite cards. The bottom navigation belongs to MainActivity. */
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = requireNotNull(_binding)
    private lateinit var adapter: FavoritePostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FavoritePostAdapter(
            onOpenDetail = { post -> openDetail(post.postId, false) },
            onRequest = { post -> openDetail(post.postId, true) }
        )
        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorites.adapter = adapter
        binding.btnFavBack.setOnClickListener {
            (activity as? MainActivity)?.openHomeTab()
                ?: requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) loadFavorites()
    }

    private fun loadFavorites() {
        val favoriteIds = AppDataStore.favoritePostIds
        val savedPosts = AppDataStore.posts.filter { it.postId in favoriteIds }
        adapter.submitList(savedPosts.toList())
        binding.tvEmptyFav.visibility = if (savedPosts.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openDetail(postId: String, requestImmediately: Boolean) {
        val intent = Intent(requireContext(), PostDetailActivity::class.java).apply {
            putExtra("EXTRA_POST_ID", postId)
            putExtra(PostDetailActivity.EXTRA_OPEN_REQUEST_DIALOG, requestImmediately)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        binding.rvFavorites.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
