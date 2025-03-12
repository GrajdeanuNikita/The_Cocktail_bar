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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.ViewModel
import com.example.the_cocktail_bar.network.RetrofitClient
import androidx.compose.ui.text.font.FontWeight
import coil.compose.rememberImagePainter



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
            //quello vero
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
    val coroutineScope = rememberCoroutineScope()

    // Carica un cocktail casuale
    LaunchedEffect(true) {
        val response = RetrofitClient.instance.getRandomCocktail()
        cocktail = response.drink?.first()
    }

    // Lista di alcolici per il gioco
    val alcoholOptions = listOf("Vodka", "Rum", "Gin", "Whiskey", "Tequila", "Brandy", "Cognac", "Gin", "Scotch")

    // Rimuovi il feedback dopo 1 secondo
    LaunchedEffect(key1 = feedbackColor) {
        if (feedbackColor != Color.Transparent) {
            delay(1000)
            feedbackColor = Color.Transparent
            selectedAnswer = ""
        }
    }

    val changeCocktail = {
        coroutineScope.launch {
            val response = RetrofitClient.instance.getRandomCocktail()
            cocktail = response.drink?.first()
            feedbackColor = Color.Transparent
            selectedAnswer = ""
        }
    }

    // Box principale che contiene il layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Padding per evitare che gli elementi tocchino i bordi
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titolo del gioco
            Text(
                text = "Gioco di Bevute",
                fontSize = 30.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Quando il cocktail è caricato, mostra l'immagine, nome e le opzioni
            cocktail?.let {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Mostra l'immagine del cocktail
                    it.imageUrl?.let { url ->
                        Image(painter = rememberImagePainter(url), contentDescription = null)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mostra il nome del cocktail
                    Text(
                        text = "Cocktail: ${it.name}",
                        fontSize = 24.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Crea le opzioni con ingredienti
                    val options = (alcoholOptions + it.ingredients.orEmpty()).shuffled().take(5)

                    options.forEach { option ->
                        Button(
                            onClick = {
                                selectedAnswer = option
                                // Controlla se la risposta è corretta
                                isCorrectAnswer = it.ingredients?.contains(option) == true
                                feedbackColor = if (isCorrectAnswer) Color.Green else Color.Red
                                if (isCorrectAnswer) score++ // Aumenta il punteggio se corretto
                            },
                            modifier = Modifier
                                .padding(8.dp)
                                .background(feedbackColor)
                        ) {
                            Text(option, color = Color.White, fontSize = 18.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Feedback sulla risposta dell'utente
            AnimatedVisibility(visible = feedbackColor != Color.Transparent) {
                Text(
                    text = if (isCorrectAnswer) "Corretto!" else "Sbagliato!",
                    fontSize = 24.sp,
                    color = feedbackColor
                )
            }

            // Mostra il punteggio attuale
            Text(
                text = "Punteggio: $score",
                fontSize = 20.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Pulsante per cambiare il cocktail
            Button(
                onClick = { changeCocktail() },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Cambia Cocktail", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Pulsante "Indietro" per tornare alla schermata precedente
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
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val searchResults by remember { mutableStateOf(viewModel.cocktailList) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        BasicTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
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

        Button(onClick = { viewModel.trovaCocktail(searchText.text) }) {
            Text("Cerca ")
        }

        searchResults.forEach { cocktail ->
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                // Nome del cocktail
                Text("Nome: ${cocktail.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                // Immagine del cocktail
                cocktail.imageUrl?.let {
                    Image(painter = rememberImagePainter(it), contentDescription = "Cocktail Image", modifier = Modifier.fillMaxWidth())
                }

                // Istruzioni
                Text("Istruzioni: ${cocktail.instructions}", fontSize = 14.sp)

                // Ingredienti
                cocktail.ingredients?.forEach { ingredient ->
                    Text("Ingrediente: $ingredient", fontSize = 14.sp)
                }
                Button(onClick = { viewModel.salvaCocktail(cocktail) }) {
                    Text("Salva nei preferiti")
                }
            }
        }
    }
}

@Composable
fun Preferiti(viewModel: CocktailViewModel) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Cocktail Preferiti", fontSize = 24.sp)
        if (viewModel.favorites.isEmpty()) {
            Text("Nessun cocktail preferito.", fontSize = 18.sp)
        } else {
            viewModel.favorites.forEach { cocktail ->
                Text("- ${cocktail.name}", fontSize = 18.sp)
            }
        }
    }
}

class CocktailViewModel : ViewModel() {
    var cocktailList: List<Cocktail> = listOf()
    var favorites = mutableStateListOf<Cocktail>()


    fun trovaCocktail(query: String) {
        viewModelScope.launch {
            val response = RetrofitClient.instance.searchCocktail(query)
            cocktailList = response.drink ?: listOf() }
    }

    fun salvaCocktail(cocktail: Cocktail) {
        if (!favorites.contains(cocktail)) {
            favorites.add(cocktail)  // Aggiungi il cocktail ai preferiti
        }
    }
}