package com.example.aplicacion_unimarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.aplicacion_unimarket.databinding.FragmentPublishProductBinding
import com.google.android.material.snackbar.Snackbar
import kotlin.math.max

class PublishProductFragment : Fragment() {

    private var _binding: FragmentPublishProductBinding? = null
    private val binding get() = _binding!!
    private var selectedImageCount = 1
    private var hasSelectedVideo = false
    private var editingProductId: String? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedImageCount = max(1, uris.size)
        updateMultimediaLabels()
    }

    private val videoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        hasSelectedVideo = uri != null
        updateMultimediaLabels()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPublishProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editingProductId = arguments?.getString(ProductDetailFragment.ARG_PRODUCT_ID)
        setupSpinners()
        setupMultimediaButtons()
        fillFormIfEditing()

        binding.publishButton.setOnClickListener {
            saveProduct()
        }
    }

    private fun setupSpinners() {
        val categories = ProductCategory.values().map { it.displayName }
        binding.categorySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        val conditions = listOf("Nuevo", "Usado - excelente", "Usado - bueno", "Usado - aceptable", "Disponible")
        binding.conditionSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            conditions
        )
    }

    private fun setupMultimediaButtons() {
        binding.addImagesButton.setOnClickListener {
            imagePicker.launch("image/*")
        }
        binding.addVideoButton.setOnClickListener {
            videoPicker.launch("video/*")
        }
        updateMultimediaLabels()
    }

    private fun fillFormIfEditing() {
        val product = editingProductId?.let { MarketplaceRepository.findProduct(it) } ?: return
        binding.screenTitleText.text = "Editar publicacion"
        binding.publishButton.text = "Guardar cambios"
        binding.titleInput.setText(product.title)
        binding.descriptionInput.setText(product.description)
        binding.priceInput.setText(String.format("%.2f", product.price))
        selectedImageCount = product.imageCount
        hasSelectedVideo = product.hasVideo

        val categoryPosition = ProductCategory.values().indexOf(product.category)
        if (categoryPosition >= 0) {
            binding.categorySpinner.setSelection(categoryPosition)
        }

        val conditionAdapter = binding.conditionSpinner.adapter
        for (index in 0 until conditionAdapter.count) {
            if (conditionAdapter.getItem(index) == product.condition) {
                binding.conditionSpinner.setSelection(index)
                break
            }
        }

        updateMultimediaLabels()
    }

    private fun saveProduct() {
        val title = binding.titleInput.text?.toString().orEmpty().trim()
        val description = binding.descriptionInput.text?.toString().orEmpty().trim()
        val price = binding.priceInput.text?.toString().orEmpty().toDoubleOrNull()

        if (title.isBlank() || description.isBlank() || price == null) {
            Snackbar.make(binding.root, "Completa titulo, descripcion y precio valido.", Snackbar.LENGTH_SHORT).show()
            return
        }

        val category = ProductCategory.fromDisplayName(binding.categorySpinner.selectedItem.toString())
        val condition = binding.conditionSpinner.selectedItem.toString()

        val productId = editingProductId
        if (productId == null) {
            MarketplaceRepository.addProduct(
                title = title,
                description = description,
                price = price,
                category = category,
                condition = condition,
                imageCount = selectedImageCount,
                hasVideo = hasSelectedVideo
            )
        } else {
            MarketplaceRepository.updateProduct(
                productId = productId,
                title = title,
                description = description,
                price = price,
                category = category,
                condition = condition,
                imageCount = selectedImageCount,
                hasVideo = hasSelectedVideo
            )
        }

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.homeFragment, false)
            .build()
        findNavController().navigate(R.id.action_publishProductFragment_to_homeFragment, null, navOptions)
    }

    private fun updateMultimediaLabels() {
        if (_binding == null) return
        binding.imagesCountText.text = "$selectedImageCount imagen(es) seleccionada(s)"
        binding.videoStatusText.text = if (hasSelectedVideo) "Video demostrativo agregado" else "Sin video demostrativo"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
