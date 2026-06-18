package com.example.aplicacion_unimarket

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aplicacion_unimarket.databinding.FragmentProductDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private var productId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productId = requireArguments().getString(ARG_PRODUCT_ID).orEmpty()
        renderProduct()
    }

    private fun renderProduct() {
        val product = MarketplaceRepository.findProduct(productId)
        if (product == null) {
            Snackbar.make(binding.root, "La publicacion ya no existe.", Snackbar.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        binding.heroCategoryText.text = product.category.displayName
        binding.titleText.text = product.title
        binding.priceText.text = "$${String.format("%.2f", product.price)}"
        binding.descriptionText.text = product.description
        binding.categoryText.text = product.category.displayName
        binding.conditionText.text = if (product.sold) "Vendido" else product.condition
        binding.sellerText.text = "${product.sellerName} - ${product.sellerCareer}"
        binding.videoPanel.isVisible = product.hasVideo
        binding.videoEmptyText.isVisible = !product.hasVideo
        binding.favoriteButton.text =
            if (MarketplaceRepository.isFavorite(product.id)) "Quitar de favoritos" else "Agregar a favoritos"

        renderGallery(product.imageCount)

        binding.favoriteButton.setOnClickListener {
            MarketplaceRepository.toggleFavorite(product.id)
            renderProduct()
        }

        binding.contactSellerButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_productDetailFragment_to_chatFragment,
                bundleOf(ARG_PRODUCT_ID to product.id)
            )
        }

        val isOwner = product.sellerName == MarketplaceRepository.currentUser.fullName
        binding.ownerActionsGroup.isVisible = isOwner
        binding.editProductButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_productDetailFragment_to_publishProductFragment,
                bundleOf(ARG_PRODUCT_ID to product.id)
            )
        }
        binding.markSoldButton.setOnClickListener {
            MarketplaceRepository.markSold(product.id)
            renderProduct()
        }
        binding.deleteProductButton.setOnClickListener {
            confirmDelete(product.id)
        }
    }

    private fun renderGallery(imageCount: Int) {
        binding.galleryContainer.removeAllViews()
        repeat(imageCount.coerceAtLeast(1)) { index ->
            val item = TextView(requireContext()).apply {
                text = "Imagen ${index + 1}"
                gravity = Gravity.CENTER
                setTextColor(resources.getColor(R.color.unimarket_ink, null))
                setBackgroundResource(R.drawable.bg_gallery_item)
            }
            val params = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.gallery_item_width),
                resources.getDimensionPixelSize(R.dimen.gallery_item_height)
            ).apply {
                marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_sm)
            }
            binding.galleryContainer.addView(item, params)
        }
    }

    private fun confirmDelete(productId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar publicacion")
            .setMessage("Esta accion quitara el producto de la lista principal y favoritos.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                MarketplaceRepository.deleteProduct(productId)
                findNavController().navigate(R.id.action_productDetailFragment_to_homeFragment)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_PRODUCT_ID = "productId"
    }
}
