package com.example.aplicacion_unimarket

data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val career: String,
    val phone: String
) {
    val fullName: String
        get() = "$firstName $lastName"
}

enum class ProductCategory(val displayName: String) {
    BOOKS("Libros"),
    ELECTRONICS("Electronicos"),
    LAB("Laboratorio"),
    TUTORING("Tutorias"),
    OTHER("Otros");

    companion object {
        fun fromDisplayName(value: String): ProductCategory {
            return values().firstOrNull { it.displayName == value } ?: OTHER
        }
    }
}

data class Product(
    val id: String,
    var title: String,
    var description: String,
    var price: Double,
    var category: ProductCategory,
    var condition: String,
    var sellerName: String,
    var sellerCareer: String,
    var imageCount: Int,
    var hasVideo: Boolean,
    var sold: Boolean = false
)

data class ChatMessage(
    val sender: String,
    val body: String,
    val fromCurrentUser: Boolean
)

object MarketplaceRepository {
    val currentUser = User(
        firstName = "Alejandra",
        lastName = "Mendoza",
        email = "alejandra.mendoza@universidad.edu",
        career = "Ingenieria en Sistemas",
        phone = "099 555 2301"
    )

    private val products = mutableListOf(
        Product(
            id = "p1",
            title = "Calculo de Stewart 8va edicion",
            description = "Libro en buen estado, con ejercicios marcados y resumenes utiles para primer semestre.",
            price = 32.00,
            category = ProductCategory.BOOKS,
            condition = "Usado - bueno",
            sellerName = "Daniel Ruiz",
            sellerCareer = "Ingenieria Civil",
            imageCount = 3,
            hasVideo = false
        ),
        Product(
            id = "p2",
            title = "Calculadora cientifica Casio fx-991",
            description = "Funciona perfecto para estadistica, fisica y algebra. Incluye estuche.",
            price = 18.50,
            category = ProductCategory.ELECTRONICS,
            condition = "Usado - excelente",
            sellerName = "Mariana Lopez",
            sellerCareer = "Administracion",
            imageCount = 2,
            hasVideo = true
        ),
        Product(
            id = "p3",
            title = "Kit de laboratorio basico",
            description = "Gafas, bata talla M y guantes reutilizables para practicas de quimica.",
            price = 24.99,
            category = ProductCategory.LAB,
            condition = "Nuevo",
            sellerName = currentUser.fullName,
            sellerCareer = currentUser.career,
            imageCount = 4,
            hasVideo = true
        ),
        Product(
            id = "p4",
            title = "Tutorias de programacion Kotlin",
            description = "Sesiones por hora para tareas, proyectos Android y preparacion de examenes.",
            price = 10.00,
            category = ProductCategory.TUTORING,
            condition = "Disponible",
            sellerName = "Sofia Andrade",
            sellerCareer = "Software",
            imageCount = 1,
            hasVideo = false
        ),
        Product(
            id = "p5",
            title = "Mochila universitaria impermeable",
            description = "Tiene compartimento para laptop de 15 pulgadas y varios bolsillos interiores.",
            price = 21.00,
            category = ProductCategory.OTHER,
            condition = "Usado - bueno",
            sellerName = "Carlos Vera",
            sellerCareer = "Medicina",
            imageCount = 2,
            hasVideo = false
        )
    )

    private val favoriteProductIds = mutableSetOf("p1", "p2")
    private val conversations = mutableMapOf<String, MutableList<ChatMessage>>()

    fun searchProducts(query: String = "", category: ProductCategory? = null): List<Product> {
        val normalizedQuery = query.trim().lowercase()
        return products
            .filter { !it.sold }
            .filter { product ->
                category == null || product.category == category
            }
            .filter { product ->
                normalizedQuery.isBlank() ||
                    product.title.lowercase().contains(normalizedQuery) ||
                    product.description.lowercase().contains(normalizedQuery) ||
                    product.category.displayName.lowercase().contains(normalizedQuery)
            }
    }

    fun allProducts(): List<Product> = products.toList()

    fun myProducts(): List<Product> {
        return products.filter { it.sellerName == currentUser.fullName }
    }

    fun favoriteProducts(): List<Product> {
        return products.filter { it.id in favoriteProductIds }
    }

    fun findProduct(productId: String): Product? {
        return products.firstOrNull { it.id == productId }
    }

    fun isFavorite(productId: String): Boolean {
        return productId in favoriteProductIds
    }

    fun toggleFavorite(productId: String): Boolean {
        return if (favoriteProductIds.contains(productId)) {
            favoriteProductIds.remove(productId)
            false
        } else {
            favoriteProductIds.add(productId)
            true
        }
    }

    fun removeFavorite(productId: String) {
        favoriteProductIds.remove(productId)
    }

    fun addProduct(
        title: String,
        description: String,
        price: Double,
        category: ProductCategory,
        condition: String,
        imageCount: Int,
        hasVideo: Boolean
    ): Product {
        val product = Product(
            id = "p${System.currentTimeMillis()}",
            title = title,
            description = description,
            price = price,
            category = category,
            condition = condition,
            sellerName = currentUser.fullName,
            sellerCareer = currentUser.career,
            imageCount = imageCount.coerceAtLeast(1),
            hasVideo = hasVideo
        )
        products.add(0, product)
        return product
    }

    fun updateProduct(
        productId: String,
        title: String,
        description: String,
        price: Double,
        category: ProductCategory,
        condition: String,
        imageCount: Int,
        hasVideo: Boolean
    ) {
        findProduct(productId)?.apply {
            this.title = title
            this.description = description
            this.price = price
            this.category = category
            this.condition = condition
            this.imageCount = imageCount.coerceAtLeast(1)
            this.hasVideo = hasVideo
        }
    }

    fun markSold(productId: String) {
        findProduct(productId)?.sold = true
        favoriteProductIds.remove(productId)
    }

    fun deleteProduct(productId: String) {
        products.removeAll { it.id == productId }
        favoriteProductIds.remove(productId)
        conversations.remove(productId)
    }

    fun messagesFor(productId: String): MutableList<ChatMessage> {
        val product = findProduct(productId)
        return conversations.getOrPut(productId) {
            mutableListOf(
                ChatMessage(
                    sender = product?.sellerName ?: "Vendedor",
                    body = "Hola, gracias por escribir. El producto sigue disponible.",
                    fromCurrentUser = false
                )
            )
        }
    }

    fun sendMessage(productId: String, message: String) {
        val product = findProduct(productId)
        val conversation = messagesFor(productId)
        conversation.add(
            ChatMessage(
                sender = currentUser.firstName,
                body = message,
                fromCurrentUser = true
            )
        )
        conversation.add(
            ChatMessage(
                sender = product?.sellerName ?: "Vendedor",
                body = "Perfecto, coordinemos por este chat.",
                fromCurrentUser = false
            )
        )
    }
}
