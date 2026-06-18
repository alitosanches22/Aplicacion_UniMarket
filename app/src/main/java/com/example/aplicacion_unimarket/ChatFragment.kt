package com.example.aplicacion_unimarket

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.aplicacion_unimarket.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private var productId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productId = requireArguments().getString(ProductDetailFragment.ARG_PRODUCT_ID).orEmpty()

        val product = MarketplaceRepository.findProduct(productId)
        binding.chatTitleText.text = product?.sellerName ?: "Vendedor"
        binding.productContextText.text = product?.title ?: "Producto"

        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text?.toString().orEmpty().trim()
            if (message.isBlank()) return@setOnClickListener
            MarketplaceRepository.sendMessage(productId, message)
            binding.messageInput.text?.clear()
            renderMessages()
        }

        renderMessages()
    }

    private fun renderMessages() {
        binding.messagesContainer.removeAllViews()
        MarketplaceRepository.messagesFor(productId).forEach { message ->
            val messageView = TextView(requireContext()).apply {
                text = "${message.sender}\n${message.body}"
                setTextColor(resources.getColor(R.color.unimarket_ink, null))
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.spacing_md),
                    resources.getDimensionPixelSize(R.dimen.spacing_sm),
                    resources.getDimensionPixelSize(R.dimen.spacing_md),
                    resources.getDimensionPixelSize(R.dimen.spacing_sm)
                )
                setBackgroundResource(
                    if (message.fromCurrentUser) R.drawable.bg_message_me else R.drawable.bg_message_other
                )
            }
            val params = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.chat_bubble_max_width),
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = if (message.fromCurrentUser) Gravity.END else Gravity.START
                bottomMargin = resources.getDimensionPixelSize(R.dimen.spacing_sm)
            }
            binding.messagesContainer.addView(messageView, params)
        }
        binding.messagesScroll.post {
            binding.messagesScroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
