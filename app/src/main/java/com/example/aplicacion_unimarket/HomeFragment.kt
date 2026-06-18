package com.example.aplicacion_unimarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aplicacion_unimarket.databinding.FragmentHomeBinding
import com.example.aplicacion_unimarket.databinding.ItemProductBinding
import com.google.android.material.chip.Chip

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var selectedCategory: ProductCategory? = null

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

        setupCategory(binding.chipAll, null)
        setupCategory(binding.chipBooks, ProductCategory.BOOKS)
        setupCategory(binding.chipElectronics, ProductCategory.ELECTRONICS)
        setupCategory(binding.chipLab, ProductCategory.LAB)
        setupCategory(binding.chipTutoring, ProductCategory.TUTORING)
        setupCategory(binding.chipOther, ProductCategory.OTHER)

        binding.searchInput.doOnTextChanged { _, _, _, _ -> renderProducts() }
        binding.publishProductButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_publishProductFragment)
        }
        binding.favoritesButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_favoritesFragment)
        }
        binding.profileButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }

        renderProducts()
    }

    private fun setupCategory(chip: Chip, category: ProductCategory?) {
        chip.setOnClickListener {
            selectedCategory = category
            renderProducts()
        }
    }

    private fun renderProducts() {
        val query = binding.searchInput.text?.toString().orEmpty()
        val products = MarketplaceRepository.searchProducts(query, selectedCategory)
        binding.productsContainer.removeAllViews()
        binding.emptyState.isVisible = products.isEmpty()

        products.forEach { product ->
            val itemBinding = ItemProductBinding.inflate(layoutInflater, binding.productsContainer, false)
            bindProduct(itemBinding, product)
            binding.productsContainer.addView(itemBinding.root)
        }
    }

    private fun bindProduct(itemBinding: ItemProductBinding, product: Product) {
        itemBinding.thumbnailText.text = product.category.displayName.take(3).uppercase()
        itemBinding.titleText.text = product.title
        itemBinding.priceText.text = "$${String.format("%.2f", product.price)}"
        itemBinding.descriptionText.text = product.description
        itemBinding.categoryText.text = product.category.displayName
        itemBinding.statusText.text = if (product.sold) "Vendido" else product.condition
        itemBinding.sellerText.text = product.sellerName
        itemBinding.favoriteButton.text =
            if (MarketplaceRepository.isFavorite(product.id)) "Guardado" else "Guardar"

        itemBinding.root.setOnClickListener {
            openDetail(product.id)
        }
        itemBinding.favoriteButton.setOnClickListener {
            MarketplaceRepository.toggleFavorite(product.id)
            renderProducts()
        }
    }

    private fun openDetail(productId: String) {
        findNavController().navigate(
            R.id.action_homeFragment_to_productDetailFragment,
            bundleOf(ProductDetailFragment.ARG_PRODUCT_ID to productId)
        )
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            renderProducts()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
