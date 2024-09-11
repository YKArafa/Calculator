package com.example.calc

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.calc.ui.theme.CalcTheme
import java.util.Stack
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly


class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalcTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    CalculatorLayout()

                }
            }
        }
    }
}


@Composable
fun CalculatorLayout() {
    // Use a mutable state to hold the expression
    var expression by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(128.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(text = expression, fontSize = 48.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(128.dp))

        // Pass the expression state and onUpdateExpression function to update it
        ButtonRow(expression, listOf("AC", "C", "%", "/")) { ne -> expression = ne }
        ButtonRow(expression, listOf("7", "8", "9", "*")) { ne -> expression = ne }
        ButtonRow(expression, listOf("4", "5", "6", "-")) { ne -> expression = ne }
        ButtonRow(expression, listOf("1", "2", "3", "+")) { ne -> expression = ne }
        ButtonRow(expression, listOf("0", "(", ")", "=")) { ne -> expression = ne }
    }
}

@Composable
fun ButtonRow(expression: String, buttonLabels: List<String>, update: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        buttonLabels.forEach { label ->
            Button(
                onClick = {
                    when (label) {
                        "AC"->  {if (expression != "0")update("0")}
                        "C" ->  {if (expression != "0") update(expression.removeRange(expression.length-1, expression.length))}
                        "=" ->  {if (expression != "0") update(calculate(expression).toString())}
                        "-" ->  {if (expression != "0" && expression[expression.length-1].isDigit())update("$expression $label ")}
                        "+" ->  {if (expression != "0" && expression[expression.length-1].isDigit())update("$expression $label ")}
                        "%" ->  {if (expression != "0" && expression[expression.length-1].isDigit())update("$expression $label ")}
                        "*" ->  {if (expression != "0" && expression[expression.length-1].isDigit())update("$expression $label ")}
                        "/" ->  {if (expression != "0" && expression[expression.length-1].isDigit())update("$expression $label ")}
                        "(" ->  {update("$expression $label ")}
                        ")" ->  {update("$expression $label ")}

                        else -> {
                            if (expression == "0") {
                                update(label)
                            } else {
                                update(expression + label)
                            }
                        }
                    }
                },colors = if (!label.isDigitsOnly() && label != "("&& label != ")" && label != "AC" && label != "C" && label != "%") {
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFC8B35),
                        contentColor = Color.White
                    )
                } else if(label == "AC" || label == "C" || label == "%") {
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFAAAAAA),
                        contentColor = Color.White
                    )
                }else  {
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF313131),
                        contentColor = Color.White
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),

            ) {

                Text(text = label, fontSize = 28.sp)
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
fun CalculatorLayoutPreview() {
    CalculatorLayout()
}

fun calculate(expression: String): Any {

    try {
        val s = expression.split(" ").filter { it.isNotEmpty() }

        val combined = mutableListOf<String>()
        var i = 0

        while (i < s.size) {
            var currentToken = s[i]

            while (i + 1 < s.size && s[i + 1].toDoubleOrNull() != null && currentToken.toDoubleOrNull() != null) {
                currentToken += s[i + 1]
                i++
            }

            combined.add(currentToken)
            i++
        }

        if (!isValidSyntax(combined)) {
            return "Syntax error"
        }

        val values = Stack<Double>()
        val ops = Stack<Char>()

        for (n in 0..combined.size-1) {
            when {
                combined[n].toDoubleOrNull() != null -> {
                    values.push(combined[n].toDouble())
                }

                combined[n] == "(" -> {
                    if(!combined[n-1].isDigitsOnly()){
                        ops.push('(')
                    }else{
                        ops.push('*')
                        ops.push('(')
                    }
                }

                combined[n] == ")" -> {
                    while (ops.isNotEmpty() && ops.peek() != '(') {
                        values.push(op(ops.pop(), values.pop(), values.pop()))
                    }
                    if (ops.isEmpty()) {
                        return "Syntax error"
                    }
                    ops.pop()
                }

                combined[n] in listOf("+", "-", "*", "/") -> {
                    while (ops.isNotEmpty() && Bodmas(combined[n][0], ops.peek())) {
                        values.push(op(ops.pop(), values.pop(), values.pop()))
                    }
                    ops.push(combined.get(n)[0])
                }

                else -> {
                    return "Syntax error"
                }
            }
        }

        while (ops.isNotEmpty()) {
            if (ops.peek() == '(' || ops.peek() == ')') {
                return "Syntax error"
            }
            values.push(op(ops.pop(), values.pop(), values.pop()))
        }

        if(values.peek() == values.peek().toInt().toDouble()){
            return values.pop().toInt()
        }else{
            return values.pop()
        }
    } catch (e: Exception) {
        return "Syntax error"
    }
}


fun isValidSyntax(s: List<String>): Boolean {
    var ls = true
    var open = 0

    for (n in s) {
        when {
            n.toDoubleOrNull() != null -> {
                ls = false
            }

            n == "(" -> {
                open++
                ls = true
            }

            n == ")" -> {
                open--
                if (open < 0) return false
                ls = false
            }

            n in listOf("+", "-", "*", "/") -> {
                if (ls) return false
                ls = true
            }

            else -> {
                return false
            }
        }
    }

    return !ls && open == 0
}

fun Bodmas(op1: Char, op2: Char): Boolean {
    if (op2 == '(' || op2 == ')') return false
    if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) return false
    return true
}

fun op(op: Char, b: Double, a: Double): Double {
    return when (op) {
        '+' -> a + b
        '-' -> a - b
        '*' -> a * b
        '/' -> {
            if (b == 0.0) throw ArithmeticException("Math Error")
            a / b
        }

        else -> throw UnsupportedOperationException("Unknown operator: $op")
    }
}


fun sumNumbers(vararg value: Double) = value.sum()




