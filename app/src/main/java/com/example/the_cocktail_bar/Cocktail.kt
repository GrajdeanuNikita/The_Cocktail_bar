package com.example.the_cocktail_bar
import org.json.JSONObject


data class CocktailR(
    val drink: List<Cocktail>?
)

data class Cocktail(
    val name: String,
    val imageUrl: String?,
    val instructions: String,
    val ingredients: List<String>?
){
    companion object {
        fun fromJson(json: JSONObject): Cocktail {
            val ingredients = mutableListOf<String>()
            for (i in 1..15) {
                val ingredient = json.optString("strIngredient$i", "")
                if (ingredient.isNotEmpty()) {
                    ingredients.add(ingredient)
                }
            }
            return Cocktail(
                name = json.getString("strDrink"),
                instructions = json.optString("strInstructions", "No instructions"),
                imageUrl = json.optString("strDrinkThumb", null),
                ingredients = ingredients
            )
        }
    }
}