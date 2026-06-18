package com.example.aplicacion_unimarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aplicacion_unimarket.databinding.FragmentProfileBinding
import com.example.aplicacion_unimarket.databinding.ItemMyProductBinding
import com.google.android.material.snackbar.Snackbar

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = MarketplaceRepository.currentUser
        binding.avatarText.text = user.firstName.first().uppercase()
        binding.nameText.text = user.fullName
        binding.emailText.text = user.email
        binding.careerText.text = user.career
        binding.phoneText.text = user.phone

        binding.favoritesButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_favoritesFragment)
        }
        binding.publishButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_publishProductFragment)
        }
        binding.editProfileButton.setOnClickListener {
            Snackbar.make(binding.root, "La edicion de perfil se conectara al backend.", Snackbar.LENGTH_SHORT).show()
        }
        binding.logoutButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        renderStats()
        renderMyProducts()
    }

    private fun renderStats() {
        val myProducts = MarketplaceRepository.myProducts()
        binding.myProductsCountText.text = myProducts.size.toString()
        binding.salesCountText.text = myProducts.count { it.sold }.toString()
        binding.favoritesCountText.text = MarketplaceRepository.favoriteProducts().size.toString()
    }

    private fun renderMyProducts() {
        binding.myProductsContainer.removeAllViews()
        MarketplaceRepository.myProducts().forEach { product ->
            val itemBinding = ItemMyProductBinding.inflate(layoutInflater, binding.myProductsContainer, false)
            itemBinding.titleText.text = product.title
            itemBinding.priceText.text = "$${String.format("%.2f", product.price)}"
            itemBinding.statusText.text = if (product.sold) "Vendido" else product.condition
            itemBinding.editButton.setOnClickListener {
                findNavController().navigate(
                    R.id.action_profileFragment_to_publishProductFragment,
                    bundleOf(ProductDetailFragment.ARG_PRODUCT_ID to product.id)
                )
            }
            itemBinding.soldButton.setOnClickListener {
                MarketplaceRepository.markSold(product.id)
                renderStats()
                renderMyProducts()
            }
            itemBinding.deleteButton.setOnClickListener {
                MarketplaceRepository.deleteProduct(product.id)
                renderStats()
                renderMyProducts()
            }
            itemBinding.root.setOnClickListener {
                findNavController().navigate(
                    R.id.action_profileFragment_to_productDetailFragment,
                    bundleOf(ProductDetailFragment.ARG_PRODUCT_ID to product.id)
                )
            }
            binding.myProductsContainer.addView(itemBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
