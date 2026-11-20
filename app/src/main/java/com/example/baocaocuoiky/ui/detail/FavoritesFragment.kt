package com.example.baocaocuoiky.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.databinding.FragmentFavoritesBinding
import com.example.baocaocuoiky.ui.home.PostGridAdapter

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PostGridAdapter

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

        adapter = PostGridAdapter(emptyList()) { post ->
            startActivity(Intent(requireContext(), PostDetailActivity::class.java).apply {
                putExtra("EXTRA_POST_ID", post.postId)
            })
        }

        binding.rvFavorites.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvFavorites.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun loadFavorites() {
        val favPosts = AppDataStore.posts.filter { AppDataStore.favoritePostIds.contains(it.postId) }
        adapter.updateData(favPosts)
        binding.tvFavCount.text = "Bạn đã lưu ${favPosts.size} món đồ"
        binding.tvEmptyFav.visibility = if (favPosts.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}