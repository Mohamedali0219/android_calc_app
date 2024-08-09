package com.example.calculatorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculatorapp.ui.theme.CalculatorAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorAppTheme {
                CalculatorApp()
            }
        }
    }
}

@Composable
fun CalculatorApp() {
    var displayText by remember { mutableStateOf("0") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = displayText,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .wrapContentWidth(Alignment.End)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val buttons = listOf(
                        listOf("%", "(", ")", "C"),
                        listOf("7", "8", "9", "/"),
                        listOf("4", "5", "6", "*"),
                        listOf("1", "2", "3", "-"),
                        listOf("0", ".", "=", "+")
                    )

                    for (row in buttons) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (button in row) {
                                CalculatorButton(
                                    text = button,
                                    modifier = Modifier.weight(1f),
                                    color = if (button in listOf("C", "/", "*", "-", "+", "=")) Color(0xFFFF9800) else Color.DarkGray
                                ) {
                                    displayText = handleButtonClick(button, displayText)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun CalculatorButton(text: String, modifier: Modifier = Modifier, color: Color, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .background(color, shape = RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

fun handleButtonClick(button: String, currentDisplay: String): String {
    return when (button) {
        "C" -> "0"
        "=" -> {
            try {
                evalExpression(currentDisplay)
            } catch (e: Exception) {
                "Error"
            }
        }
        else -> {
            if (currentDisplay == "0" && button != ".") {
                button
            } else {
                currentDisplay + button
            }
        }
    }
}

fun evalExpression(expression: String): String {
    // You can replace this simple evaluation with a more complex one if needed
    return try {
        val result = object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].toInt() else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.toInt()) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.toInt()) -> x += parseTerm() // addition
                        eat('-'.toInt()) -> x -= parseTerm() // subtraction
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.toInt()) -> x *= parseFactor() // multiplication
                        eat('/'.toInt()) -> x /= parseFactor() // division
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.toInt())) return parseFactor() // unary plus
                if (eat('-'.toInt())) return -parseFactor() // unary minus

                var x: Double
                val startPos = this.pos
                if (eat('('.toInt())) { // parentheses
                    x = parseExpression()
                    eat(')'.toInt())
                } else if ((ch >= '0'.toInt() && ch <= '9'.toInt()) || ch == '.'.toInt()) { // numbers
                    while (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) nextChar()
                    x = expression.substring(startPos, this.pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                return x
            }
        }.parse()

        if (result == result.toLong().toDouble()) {
            result.toLong().toString()
        } else {
            result.toString()
        }
    } catch (e: Exception) {
        "Error"
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CalculatorAppTheme {
        CalculatorApp()
    }
}
