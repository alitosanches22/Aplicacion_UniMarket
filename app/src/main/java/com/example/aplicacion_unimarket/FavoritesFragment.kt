package com.example.aplicacion_unimarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aplicacion_unimarket.databinding.FragmentFavoritesBinding
import com.example.aplicacion_unimarket.databinding.ItemProductBinding

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

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
        renderFavorites()
    }

    private fun renderFavorites() {
        val products = MarketplaceRepository.favoriteProducts()
        binding.favoritesContainer.removeAllViews()
        binding.emptyState.isVisible = products.isEmpty()

        products.forEach { product ->
            val itemBinding = ItemProductBinding.inflate(layoutInflater, binding.favoritesContainer, false)
            itemBinding.thumbnailText.text = product.category.displayName.take(3).uppercase()
            itemBinding.titleText.text = product.title
            itemBinding.priceText.text = "$${String.format("%.2f", product.price)}"
            itemBinding.descriptionText.text = product.description
            itemBinding.categoryText.text = product.category.displayName
            itemBinding.statusText.text = product.condition
            itemBinding.sellerText.text = product.sellerName
            itemBinding.favoriteButton.text = "Eliminar"
            itemBinding.favoriteButton.setOnClickListener {
                MarketplaceRepository.removeFavorite(product.id)
                renderFavorites()
            }
            itemBinding.root.setOnClickListener {
                findNavController().navigate(
                    R.id.action_favoritesFragment_to_productDetailFragment,
                    bundleOf(ProductDetailFragment.ARG_PRODUCT_ID to product.id)
                )
            }
            binding.favoritesContainer.addView(itemBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
