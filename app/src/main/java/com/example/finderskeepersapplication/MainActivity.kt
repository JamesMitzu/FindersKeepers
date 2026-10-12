package com.example.finderskeepersapplication

import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.finderskeepersapplication.ui.screens.Dashboard
import com.example.finderskeepersapplication.ui.theme.FindersKeepersApplicationTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.finderskeepersapplication.ui.theme.FindersGreen
import com.example.finderskeepersapplication.ui.theme.KeepersOrange

private object LandingPageRoutes {
    const val landingPage1 = "landingPage1"
    const val landingPage2 = "landingPage2"
    const val landingPage3 = "landingPage3"
    const val finishLandingPage = "finishLandingPage"
}

@Composable
fun AppLandingPageNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = LandingPageRoutes.landingPage1) {
        composable(LandingPageRoutes.landingPage1) {
            LandingPage1(navController = navController)
        }
        composable(LandingPageRoutes.landingPage2) {
            LandingPage2(navController = navController)
        }
        composable(LandingPageRoutes.landingPage3) {
            LandingPage3(navController = navController)
        }
        composable(LandingPageRoutes.finishLandingPage) {
            FinishLandingPage(navController = navController)
        }
    }
}

private const val PREFS_NAME = "finders_keepers_prefs"
private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)) {
            startActivity(Intent(this, Dashboard::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            FindersKeepersApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppLandingPageNavigation()
                    }
                }
            }
        }
    }
}

// Landing Page 1
@Composable
fun LandingPage1(navController: NavController, modifier: Modifier = Modifier) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp, vertical = 20.dp)
    ) {
        val (logo, title, description, dots, button) = createRefs()

        Image(
            painter = painterResource(id = R.drawable.navigate_logo),
            contentDescription = "Landing Page Logo",
            modifier = Modifier
                .size(150.dp)
                .constrainAs(logo) {
                    linkTo(parent.top, title.top, bias = 0.6f)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = "Remember where everything goes.",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            lineHeight = 34.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .constrainAs(title) {
                    bottom.linkTo(description.top, margin = 40.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = "FindersKeepers is your memory for things — where you put them, where they live, and where to find them.",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier
                .constrainAs(description) {
                    bottom.linkTo(dots.top, margin = 100.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Row(
            modifier = Modifier.constrainAs(dots) {
                bottom.linkTo(button.top, margin = 32.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF3C7565)))
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.LightGray))
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.LightGray))
        }

        Button(
            onClick = {
                navController.navigate(LandingPageRoutes.landingPage2)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF509A85),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .constrainAs(button) {
                    bottom.linkTo(parent.bottom, margin = 20.dp)
                },
            content = {
                Text(text = "Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        )
    }
}

// Landing Page 2
@Composable
fun LandingPage2(navController: NavController, modifier: Modifier = Modifier) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp, vertical = 20.dp)
    ) {
        val (logo, title, description, dots, button) = createRefs()

        Image(
            painter = painterResource(id = R.drawable.mic_logo),
            contentDescription = "Landing Page Logo",
            modifier = Modifier
                .size(150.dp)
                .constrainAs(logo) {
                    linkTo(parent.top, title.top, bias = 0.548f)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = "Just say it out loud.",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            lineHeight = 34.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .constrainAs(title) {
                    bottom.linkTo(description.top, margin = 40.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = "Tell FindersKeepers what you’re putting away and where. No typing — just your voice.",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier
                .constrainAs(description) {
                    bottom.linkTo(dots.top, margin = 100.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Row(
            modifier = Modifier.constrainAs(dots) {
                bottom.linkTo(button.top, margin = 32.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.LightGray))
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF3C7565)))
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.LightGray))
        }

        Button(
            onClick = {
                navController.navigate(LandingPageRoutes.landingPage3)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF509A85),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .constrainAs(button) {
                    bottom.linkTo(parent.bottom, margin = 20.dp)
                },
            content = {
                Text(text = "Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        )
    }
}

// Landing Page 3
@Composable
fun LandingPage3(navController: NavController, modifier: Modifier = Modifier) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp, vertical = 20.dp)
    ) {
        val (logo, title, description, dots, button) = createRefs()

        Image(
            painter = painterResource(id = R.drawable.text_logo),
            contentDescription = "Landing Page Logo",
            modifier = Modifier
                .size(150.dp)
                .constrainAs(logo) {
                    linkTo(parent.top, title.top, bias = 0.548f)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = "Fill it in seconds.",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            lineHeight = 34.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .constrainAs(title) {
                    bottom.linkTo(description.top, margin = 40.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = "Ask FindersKeepers where you put something. It’ll tell you exactly where to look — every time.",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier
                .constrainAs(description) {
                    bottom.linkTo(dots.top, margin = 100.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Row(
            modifier = Modifier.constrainAs(dots) {
                bottom.linkTo(button.top, margin = 32.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.LightGray))
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.LightGray))
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF3C7565)))
        }

        Button(
            onClick = {
                navController.navigate(LandingPageRoutes.finishLandingPage)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF509A85),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .constrainAs(button) {
                    bottom.linkTo(parent.bottom, margin = 20.dp)
                },
            content = {
                Text(text = "Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        )
    }
}

// Last Landing Page
@Composable
fun FinishLandingPage(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp, vertical = 20.dp)
    ) {
        val (content, button) = createRefs()

        Column(
            modifier = Modifier
                .constrainAs(content) {
                    centerTo(parent)
                    translationY = (-100).dp
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.finderskeepers_logo),
                contentDescription = "Landing Page Logo",
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Finders",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = FindersGreen
                )

                Text(
                    text = "Keepers",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = KeepersOrange
                )
            }
        }

        Button(
            onClick = {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(KEY_ONBOARDING_COMPLETE, true)
                    .apply()
                val intent = Intent(context, Dashboard::class.java)
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = FindersGreen,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .constrainAs(button) {
                    bottom.linkTo(parent.bottom, margin = 20.dp)
                }
        ) {
            Text(
                text = "Finish",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}