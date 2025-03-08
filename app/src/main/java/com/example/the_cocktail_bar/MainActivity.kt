package com.example.the_cocktail_bar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.draw.blur
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.material3.*


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
        bottomBar = { BarraNavigazione(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavigationGraph(navController, viewModel)
        }
    }
}

@Composable
fun BarraNavigazione(navController: NavHostController) {
    val items = listOf("home", "ricerca", "preferiti")
    val icons = listOf("🏠", "🔍", "⭐")
    //Icons.Filled.Star oppure Icons.Outlined.Star

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
        composable("home") { HomeScreen(navController) }
        composable("gioco") { Gioco(navController) }
        composable("ricerca") { Ricerca(viewModel) }
        composable("preferiti") { Preferiti(viewModel) }
    }
}

@Composable
fun HomeScreen(navController: NavController) {

    val tertiaryColor = colorResource(id = R.color.md_theme_tertiary_highContrast)


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Image(
            painter = painterResource(id = R.drawable.cock_tail),
            contentDescription = "Immagine cocktail bar",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.5f) // Riduce l'opacità per un effetto più soft
                .blur(2.dp) // Applica la sfocatura
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Text(
                text = "Cocktail Bar 🍸",
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { navController.navigate("gioco") }) {
                Text(text = "Gioca", color = Color.Black, fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun Gioco(navController: NavController) {
    var score by remember { mutableStateOf(0) }
    var cocktail by remember { mutableStateOf<Cocktail?>(null) }
    var selectedAnswer by remember { mutableStateOf("") }
    var feedbackColor by remember { mutableStateOf(Color.Transparent) }
    var isCorrectAnswer by remember { mutableStateOf(false) }

    // Funzione per ottenere un nuovo cocktail
    LaunchedEffect(true) {
        cocktail = fetchRandomCocktail()
        println("Cocktail caricato: ${cocktail?.name}")
    }
    val alcoholOptions = listOf("Vodka", "Rum", "Gin", "Whiskey", "Tequila", "Brandy", "Cognac", "Gin", "Scotch")


    // Dopo un breve delay, cambia la schermata
        LaunchedEffect(key1 = feedbackColor) {
            if (feedbackColor != Color.Transparent) {
                delay(1000)
                feedbackColor = Color.Transparent
                cocktail = fetchRandomCocktail()  // Carica un nuovo cocktail
                selectedAnswer = ""  // Reset della risposta selezionata
            }
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Gioco di Bevute ",
                fontSize = 30.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Mostra il cocktail
        cocktail?.let {
            Text(
                text = "Cocktail: ${it.name}",
                fontSize = 24.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mostra i bottoni con le risposte
            val options = (alcoholOptions + it.alcohol).shuffled().take(4)
            options.forEach { alcohol ->
                Button(
                    onClick = { selectedAnswer = alcohol
                             checkAnswer(selectedAnswer,cocktail){
                                 correct, color -> isCorrectAnswer= correct
                                 feedbackColor= color
                                 if(correct) score++
                             } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(text = alcohol, color = Color.White, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Feedback sulla risposta
             AnimatedVisibility(visible = feedbackColor != Color.Transparent) {
                Text(
                    text = if (isCorrectAnswer) "Corretto!" else "Sbagliato!",
                    fontSize = 24.sp,
                    color = feedbackColor
                )
            }

            // Punteggio
            Text(
                text = "Punteggio: $score",
                fontSize = 20.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Bottone di conferma


            Spacer(modifier = Modifier.height(20.dp))

            // Bottone per tornare indietro
            Button(
                onClick = { navController.popBackStack() },
            ) {
                Text("Indietro", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}



@Composable
fun Ricerca(viewModel: CocktailViewModel) {
    var searchText by remember { mutableStateOf(TextFieldValue()) }
    val cocktail = viewModel.cocktail
    val favorites = viewModel.favorites.toList()
    var searchResults: List<Cocktail> = listOf()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Campo di ricerca
        BasicTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray)
                ) {
                    innerTextField()
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.fetchCocktail(searchText.text) }) {
            Text("Cerca")
        }

        // Mostra i cocktail trovati
        searchResults.forEach { cocktail ->
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = "Nome: ${cocktail.name}", fontSize = 18.sp)
                Text(text = "Categoria: ${cocktail.category}", fontSize = 14.sp)
                Text(text = "Istruzioni: ${cocktail.instructions}", fontSize = 14.sp)
                Text(text = "Alcol: ${cocktail.alcohol}", fontSize = 14.sp)

                Button(onClick = { viewModel.addFavorite(cocktail) }) {
                    Text("Aggiungi ai Preferiti")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Sezione dei preferiti
        Text("Cocktail Preferiti", fontSize = 24.sp, modifier = Modifier.padding(top = 16.dp))

        favorites.forEach { cocktail ->
            Text("- ${cocktail.name}", fontSize = 18.sp)
        }
    }
    LaunchedEffect(viewModel.cocktail) {
        if (searchText.text.isNotEmpty()) {
            viewModel.fetchCocktailsByName(searchText.text) { results ->
                searchResults = results
            }
        }
    }
}

@Composable
fun Preferiti(viewModel: CocktailViewModel) {
    val favorites = viewModel.favorites.toList() // Convertiamo la lista in una List normale

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Cocktail Preferiti", fontSize = 24.sp)
        favorites.forEach { cocktail ->
            Text("- ${cocktail.name}")
        }
    }
}

class CocktailViewModel : androidx.lifecycle.ViewModel() {
    var cocktail by mutableStateOf<Cocktail?>(null)
    var favorites = mutableStateListOf<Cocktail>()
    private val _cocktailList = mutableStateListOf<Cocktail>()
    val cocktailList: List<Cocktail> get() = _cocktailList

    // Funzione per cercare cocktail per nome
    fun fetchCocktail(name: String) {
        viewModelScope.launch {
            val response = URL("https://www.thecocktaildb.com/api/json/v1/1/search.php?s=$name").readText()
            val jsonObject = JSONObject(response)
            val drinks = jsonObject.getJSONArray("drinks")
            if (drinks.length() > 0) {
                val drink = drinks.getJSONObject(0)
                cocktail = Cocktail(
                    name = drink.getString("strDrink"),
                    category = drink.getString("strCategory"),
                    instructions = drink.getString("strInstructions"),
                    alcohol = drink.getString("strAlcoholic")
                )
            }
        }
    } //usarew retrofit

    // Funzione per cercare cocktail per nome e restituire la lista
    fun fetchCocktailsByName(name: String, callback: (List<Cocktail>) -> Unit) {
        viewModelScope.launch {
            try {
                val response = URL("https://www.thecocktaildb.com/api/json/v1/1/search.php?s=$name").readText()
                val jsonObject = JSONObject(response)
                val drinks = jsonObject.optJSONArray("drinks")
                val cocktails = mutableListOf<Cocktail>()
                if (drinks != null && drinks.length() > 0) {
                    for (i in 0 until drinks.length()) {
                        val drink = drinks.getJSONObject(i)
                        cocktails.add(
                            Cocktail(
                                name = drink.getString("strDrink"),
                                category = drink.getString("strCategory"),
                                instructions = drink.getString("strInstructions"),
                                alcohol = drink.getString("strAlcoholic")
                            )
                        )
                    }
                }
                callback(cocktails) // Restituisce la lista dei cocktail
            } catch (e: Exception) {
                e.printStackTrace()
                callback(emptyList()) // Se c'è un errore, restituisce una lista vuota
            }
        }
    }

    // Funzione per aggiungere un cocktail ai preferiti
    fun addFavorite(cocktail: Cocktail) {
        favorites.add(cocktail)
    }
}


//--------GIOCO

data class CocktailResponse(
    val drinks: List<Cocktail>?
)

data class Cocktail(
    val name: String,
    val category: String,
    val instructions: String,
    val alcohol: String
)

suspend fun fetchRandomCocktail(): Cocktail? {
    return try {
        val response = URL("https://www.thecocktaildb.com/api/json/v1/1/random.php").readText()
        val jsonObject = JSONObject(response)
        val drinks = jsonObject.optJSONArray("drinks")
        if (drinks != null && drinks.length() > 0) {
            val firstDrink = drinks.getJSONObject(0)
            Cocktail(
                name = firstDrink.getString("strDrink"),
                category = firstDrink.getString("strCategory"),
                instructions = firstDrink.getString("strInstructions"),
                alcohol = firstDrink.getString("strAlcoholic")
            )
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun checkAnswer(
    selectedAnswer: String,
    cocktail: Cocktail?,
    onFeedbackChange: (Boolean, Color) -> Unit
) {
    if (selectedAnswer == cocktail?.alcohol) {
        onFeedbackChange(true, Color.Green)  // Risposta corretta
    } else {
        onFeedbackChange(false, Color.Red)  // Risposta sbagliata
    }
}