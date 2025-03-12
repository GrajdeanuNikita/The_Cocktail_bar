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
import androidx.lifecycle.ViewModel
import com.example.the_cocktail_bar.network.RetrofitClient
import androidx.compose.ui.text.font.FontWeight
import coil.compose.rememberImagePainter
import com.example.the_cocktail_bar.ui.theme.DeepBlueBlack
import com.example.the_cocktail_bar.ui.theme.MidnightBlue
import com.example.the_cocktail_bar.ui.theme.OxfordBlue
import com.example.the_cocktail_bar.ui.theme.Blue300

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

    NavigationBar(
        containerColor = DeepBlueBlack
    ) {
        val currentRoute by navController.currentBackStackEntryAsState()
        val selectedRoute = currentRoute?.destination?.route

        items.forEachIndexed { index, route ->
            NavigationBarItem(
                icon = {
                    Text(
                        text = icons[index],
                        fontSize = 24.sp,
                        color = Color.White
                    )
                },
                label = {
                    Text(
                        text = route.capitalize(),
                        color = Color.White
                    )
                },
                selected = selectedRoute == route,
                onClick = { navController.navigate(route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.White,
                    indicatorColor = Color.Gray
                )
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



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlueBlack)
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
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { navController.navigate("gioco") }) {
                Text(text = "Gioca", color = OxfordBlue, fontSize = 20.sp)
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

    // Carica un cocktail casuale inizialmente appena si apre la schermata
    LaunchedEffect(Unit) {
        val response = RetrofitClient.instance.getRandomCocktail()
        cocktail = response.drink?.first()
    }

    // Lista di alcolici casuali per il bottone
    val alcoholOptions = listOf("Vodka", "Rum", "Gin", "Whiskey", "Tequila", "Brandy", "Cognac", "Gin", "Scotch")


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


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlueBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Gioco di Bevute",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            cocktail?.let {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    it.imageUrl?.let { url ->
                        Image(painter = rememberImagePainter(url), contentDescription = null)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Cocktail: ${it.name}",
                        fontSize = 24.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Creazione delle opzioni con ingredienti di una giusta e le altre 4 prese dalla lista di su
                    val options = (alcoholOptions + it.ingredients.orEmpty()).shuffled().take(5)

                    options.forEach { option ->
                        Button(
                            onClick = {
                                selectedAnswer = option
                                isCorrectAnswer = it.ingredients?.contains(option) == true
                                feedbackColor = if (isCorrectAnswer) Color.Green else Color.Red
                                if (isCorrectAnswer) score++ // Aumenta il punteggio se corretto(poi in caso fare
                                //dei piccoli eventi quando si arriva a tot punti)

                            },
                            colors= ButtonDefaults.buttonColors(
                                containerColor= MidnightBlue,
                                contentColor = Color.White
                            ),
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

            AnimatedVisibility(visible = feedbackColor != Color.Transparent) {
                Text(
                    text = if (isCorrectAnswer) "Corretto!" else "Sbagliato!",
                    fontSize = 24.sp,
                    color = feedbackColor
                )
            }

            Text(
                text = "Punteggio: $score",
                fontSize = 20.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { changeCocktail() },
                colors = ButtonDefaults.buttonColors(containerColor = MidnightBlue),
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Cambia Cocktail", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))


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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Blue300)
            .padding(16.dp),
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ricerca Cocktail",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.width(15.dp))
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
                Text("Nome: ${cocktail.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                cocktail.imageUrl?.let {
                    Image(painter = rememberImagePainter(it), contentDescription = "Cocktail Image", modifier = Modifier.fillMaxWidth())
                }
                Text("Istruzioni: ${cocktail.instructions}", fontSize = 14.sp)

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

//gestione degli api di random e ricerca
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
            favorites.add(cocktail)
        }
    }
}