import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    val viewModel: CocktailViewModel = viewModel()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavigationGraph(navController, viewModel)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf("home", "search", "favorites")
    val icons = listOf("🏠", "🔍", "⭐")

    NavigationBar(containerColor = Color.LightGray) {
        val currentRoute by navController.currentBackStackEntryAsState()
        val selectedRoute = currentRoute?.destination?.route

        items.forEachIndexed { index, route ->
            NavigationBarItem(
                icon = { Text(icons[index], fontSize = 24.sp) },
                label = { Text(route.capitalize()) },
                selected = selectedRoute == route,
                onClick = { navController.navigate(route) }
            )
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, viewModel: CocktailViewModel) {
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen() }
        composable("search") { SearchScreen(viewModel) }
        composable("favorites") { FavoritesScreen(viewModel) }
    }
}

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Cocktail Bar 🍸", fontSize = 30.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun SearchScreen(viewModel: CocktailViewModel) {
    var searchText by remember { mutableStateOf(TextFieldValue()) }
    val cocktail = viewModel.cocktail

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        BasicTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.padding(8.dp).fillMaxWidth().border(1.dp, Color.Gray)) {
                    innerTextField()
                }
            }
        )
        Button(onClick = { viewModel.fetchCocktail(searchText.text) }) {
            Text("Cerca")
        }

        cocktail?.let {
            Text(text = "Nome: ${it.name}")
            Text(text = "Categoria: ${it.category}")
            Text(text = "Istruzioni: ${it.instructions}")
            Button(onClick = { viewModel.addFavorite(it) }) {
                Text("Salva nei Preferiti")
            }
        }
    }
}
@Composable
fun FavoritesScreen(viewModel: CocktailViewModel) {
    val favorites = viewModel.favorites.toList() // Convertiamo la lista in una List normale

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Cocktail Preferiti", fontSize = 24.sp)
        favorites.forEach { cocktail ->
            Text("- ${cocktail.name}")
        }
    }
}

/*AnimatedVisibility(visible = favorites.isNotEmpty()) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Cocktail Preferiti", fontSize = 24.sp)
        favorites.forEach { cocktail ->
            Text("- ${cocktail.name}")
        }
    }
}*/


class CocktailViewModel : androidx.lifecycle.ViewModel() {
    var cocktail by mutableStateOf<Cocktail?>(null)
    var favorites = mutableStateListOf<Cocktail>()

    fun fetchCocktail(name: String) {
        viewModelScope.launch {
            val response = URL("https://www.thecocktaildb.com/api/json/v1/1/search.php?s=$name").readText()
            val jsonObject = JSONObject(response)
            val drinks = jsonObject.optJSONArray("drinks")
            if (drinks != null && drinks.length() > 0) {
                val firstDrink = drinks.getJSONObject(0)
                cocktail = Cocktail(
                    name = firstDrink.getString("strDrink"),
                    category = firstDrink.getString("strCategory"),
                    instructions = firstDrink.getString("strInstructions")
                )
            }
        }
    }

    fun addFavorite(cocktail: Cocktail) {
        favorites.add(cocktail)
    }
}

data class Cocktail(val name: String, val category: String, val instructions: String)
