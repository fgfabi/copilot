package com.example.xiaomi14tapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xiaomi14tapp.ui.theme.Xiaomi14TAppTheme
import java.math.BigDecimal

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Xiaomi14TAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalculatorScreen()
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen() {
    var input by rememberSaveable { mutableStateOf("0") }

    val rows = listOf(
        listOf("C", "±", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "="),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101820))
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            input,
            fontSize = if (input.length <= 10) 52.sp else 36.sp,
            color = Color.White,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { buttonText ->
                    val isOperator = buttonText in setOf("+", "-", "×", "÷", "=")
                    val isZero = buttonText == "0"
                    val isEquals = buttonText == "="

                    Button(
                        onClick = { input = handleCalculatorInput(input, buttonText) },
                        modifier = Modifier
                            .weight(if (isZero) 2f else 1f)
                            .height(72.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isEquals -> MaterialTheme.colorScheme.primary
                                isOperator -> Color(0xFF3D5AFE)
                                else -> Color(0xFF1F2937)
                            }
                        )
                    ) {
                        Text(
                            buttonText,
                            fontSize = if (buttonText == "0") 24.sp else 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            if (row != rows.last()) {
                Box(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private fun handleCalculatorInput(currentInput: String, action: String): String {
    return when {
        action == "C" -> "0"
        action == "±" -> toggleSign(currentInput)
        action == "%" -> {
            val safeValue = currentInput.toDoubleOrNull() ?: 0.0
            formatResult(safeValue / 100.0)
        }
        action == "=" -> evaluateExpressionText(currentInput)
        action in setOf("+", "-", "×", "÷") -> appendOperator(currentInput, action)
        action == "." -> appendDecimal(currentInput)
        else -> appendNumber(currentInput, action)
    }
}

private fun appendNumber(currentInput: String, digit: String): String {
    if (currentInput == "0") return digit
    return currentInput + digit
}

private fun appendDecimal(currentInput: String): String {
    val lastOperatorIndex = currentInput.lastIndexOfAny(listOf("+", "-", "×", "÷"))
    val segment = if (lastOperatorIndex == -1) currentInput else currentInput.substring(lastOperatorIndex + 1)
    return if (segment.contains(".")) currentInput else currentInput + "."
}

private fun appendOperator(currentInput: String, operator: String): String {
    if (currentInput == "0") {
        return if (operator == "-") "-" else "0"
    }

    val trimmed = currentInput.trimEnd()
    val lastChar = trimmed.lastOrNull()
    return if (lastChar in setOf('+', '-', '×', '÷')) {
        trimmed.dropLast(1) + operator
    } else {
        trimmed + operator
    }
}

private fun toggleSign(currentInput: String): String {
    if (currentInput == "0") return currentInput
    return if (currentInput.startsWith("-")) currentInput.removePrefix("-") else "-$currentInput"
}

private fun evaluateExpressionText(expression: String): String {
    if (expression.isBlank()) return "0"
    return try {
        val normalized = expression.replace("×", "*").replace("÷", "/")
        val result = evaluateExpression(normalized)
        formatResult(result)
    } catch (_: Exception) {
        "Error"
    }
}

private fun evaluateExpression(expression: String): Double {
    val values = ArrayDeque<Double>()
    val operators = ArrayDeque<Char>()
    var currentNumber = ""

    for (index in expression.indices) {
        val char = expression[index]

        if (char.isDigit() || char == '.' || (char == '-' && (index == 0 || expression[index - 1] in "+-*/("))) {
            currentNumber += char
            continue
        }

        if (currentNumber.isNotEmpty()) {
            values.addLast(currentNumber.toDouble())
            currentNumber = ""
        }

        when (char) {
            '+', '-', '*', '/', '(' -> {
                while (operators.isNotEmpty() && operators.last() != '(' && precedence(operators.last()) >= precedence(char)) {
                    applyOperator(values, operators.removeLast())
                }
                operators.addLast(char)
            }
            ')' -> {
                while (operators.isNotEmpty() && operators.last() != '(') {
                    applyOperator(values, operators.removeLast())
                }
                if (operators.isNotEmpty()) {
                    operators.removeLast()
                }
            }
        }
    }

    if (currentNumber.isNotEmpty()) {
        values.addLast(currentNumber.toDouble())
    }

    while (operators.isNotEmpty()) {
        applyOperator(values, operators.removeLast())
    }

    if (values.isEmpty()) return 0.0
    return values.last()
}

private fun applyOperator(values: ArrayDeque<Double>, operator: Char) {
    val right = values.removeLast()
    val left = values.removeLast()
    val result = when (operator) {
        '+' -> left + right
        '-' -> left - right
        '*' -> left * right
        '/' -> left / right
        else -> left
    }
    values.addLast(result)
}

private fun precedence(operator: Char): Int = when (operator) {
    '+', '-' -> 1
    '*', '/' -> 2
    else -> 0
}

private fun formatResult(value: Double): String {
    if (!value.isFinite()) return "Error"
    val result = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
    return if (result == "-0") "0" else result
}

private fun String.lastIndexOfAny(values: List<String>): Int {
    var foundIndex = -1
    for (value in values) {
        val index = this.lastIndexOf(value)
        if (index > foundIndex) {
            foundIndex = index
        }
    }
    return foundIndex
}
