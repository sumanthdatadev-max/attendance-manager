package com.attendancemanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendancemanager.app.data.entity.Fee
import com.attendancemanager.app.data.entity.PaymentMethod
import com.attendancemanager.app.ui.viewmodel.FeeViewModel

@Composable
fun FeeScreen(viewModel: FeeViewModel) {
    val feeState by viewModel.feeState.collectAsState()
    val totalDue by viewModel.totalDue.collectAsState()
    val totalCollected by viewModel.totalCollected.collectAsState()
    val cashCollected by viewModel.cashCollected.collectAsState()
    val upiCollected by viewModel.upiCollected.collectAsState()
    val fullyPaidCount by viewModel.fullyPaidCount.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val feeDetailsWithNames by viewModel.feeDetailsWithNames.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Month Selector
        Text(
            "Month: ${feeState.selectedYearMonth}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Summary Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard("Total Due", "₹$totalDue", Color(0xFFFF6B6B))
            SummaryCard("Collected", "₹$totalCollected", Color(0xFF51CF66))
            SummaryCard("Paid", "$fullyPaidCount", Color(0xFF4ECDC4))
            SummaryCard("Pending", "$pendingCount", Color(0xFFFFE66D))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Payment Method Breakdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentMethodCard("💵 Cash", "₹$cashCollected", Color(0xFF90EE90))
            PaymentMethodCard("📱 UPI", "₹$upiCollected", Color(0xFF87CEEB))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fee Details Header
        Text(
            "Fee Details",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Fee List
        val filteredFees = if (searchQuery.isEmpty()) {
            feeDetailsWithNames
        } else {
            feeDetailsWithNames.filter { feeWithName ->
                feeWithName.memberName.contains(searchQuery, ignoreCase = true)
            }
        }

        if (filteredFees.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (searchQuery.isEmpty()) "No fees data available" else "No members found matching '$searchQuery'",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredFees) { feeWithName ->
                    FeeCard(
                        fee = feeWithName.fee,
                        memberName = feeWithName.memberName
                    ) { paymentAmount, paymentMethod, upiId ->
                        viewModel.recordPayment(feeWithName.fee.memberId, paymentAmount, paymentMethod, upiId)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search by member name...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun SummaryCard(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun PaymentMethodCard(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun FeeCard(
    fee: Fee,
    memberName: String,
    onPaymentClick: (Int, PaymentMethod, String?) -> Unit
) {
    var showPaymentDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPaymentDialog = true },
        colors = CardDefaults.cardColors(
            containerColor = when {
                fee.isPaid -> Color(0xFFF0F9FF)
                fee.pendingAmount > 0 -> Color(0xFFFEF2F2)
                else -> Color.White
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(memberName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Total: ₹${fee.totalAmount}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Paid: ₹${fee.paidAmount}",
                        fontSize = 12.sp,
                        color = Color(0xFF51CF66),
                        fontWeight = FontWeight.SemiBold
                    )
                    if (fee.pendingAmount > 0) {
                        Text(
                            "Pending: ₹${fee.pendingAmount}",
                            fontSize = 12.sp,
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            "✓ Paid",
                            fontSize = 12.sp,
                            color = Color(0xFF51CF66),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Show payment method badge
            if (fee.paymentMethod != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val methodLabel = when (fee.paymentMethod) {
                        PaymentMethod.CASH -> "💵 Cash"
                        PaymentMethod.UPI -> "📱 UPI"
                        else -> ""
                    }
                    Badge(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        containerColor = when (fee.paymentMethod) {
                            PaymentMethod.CASH -> Color(0xFF90EE90).copy(alpha = 0.3f)
                            PaymentMethod.UPI -> Color(0xFF87CEEB).copy(alpha = 0.3f)
                            else -> Color.LightGray
                        }
                    ) {
                        Text(methodLabel, fontSize = 11.sp, modifier = Modifier.padding(4.dp))
                    }
                    
                    if (fee.upiTransactionId != null) {
                        Text(
                            "TXN: ${fee.upiTransactionId}",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            if (fee.pendingAmount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = fee.paidAmount.toFloat() / fee.totalAmount.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF51CF66),
                    trackColor = Color(0xFFE0E0E0)
                )
            }
        }
    }

    if (showPaymentDialog) {
        FeePaymentDialog(
            fee = fee,
            memberName = memberName,
            onDismiss = { showPaymentDialog = false },
            onPaymentConfirm = { amount, method, upiId ->
                onPaymentClick(amount, method, upiId)
                showPaymentDialog = false
            }
        )
    }
}

@Composable
fun FeePaymentDialog(
    fee: Fee,
    memberName: String,
    onDismiss: () -> Unit,
    onPaymentConfirm: (Int, PaymentMethod, String?) -> Unit
) {
    var paymentAmount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var upiTransactionId by remember { mutableStateOf("") }

    // Auto-calculate pending amount
    val calculatedPending = remember(paymentAmount) {
        val paid = paymentAmount.toIntOrNull() ?: 0
        (fee.pendingAmount - paid).coerceAtLeast(0)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment - $memberName") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Total: ₹${fee.totalAmount}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Already Paid: ₹${fee.paidAmount}", fontSize = 12.sp)
                Text(
                    "Outstanding: ₹${fee.pendingAmount}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B6B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Divider()

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Amount Input
                TextField(
                    value = paymentAmount,
                    onValueChange = { paymentAmount = it },
                    label = { Text("Enter Paid Fee (₹)") },
                    placeholder = { Text("Enter amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Auto-calculated Pending
                if (paymentAmount.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Pending After Payment:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                "₹$calculatedPending",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (calculatedPending > 0) Color(0xFFFF6B6B) else Color(0xFF51CF66)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method Selection
                Text("Payment Method:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedMethod == PaymentMethod.CASH,
                            onClick = { selectedMethod = PaymentMethod.CASH }
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMethod == PaymentMethod.CASH,
                        onClick = { selectedMethod = PaymentMethod.CASH }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("💵 Cash", fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedMethod == PaymentMethod.UPI,
                            onClick = { selectedMethod = PaymentMethod.UPI }
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMethod == PaymentMethod.UPI,
                        onClick = { selectedMethod = PaymentMethod.UPI }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📱 UPI", fontSize = 14.sp)
                }

                // UPI Transaction ID (only show if UPI is selected)
                if (selectedMethod == PaymentMethod.UPI) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = upiTransactionId,
                        onValueChange = { upiTransactionId = it },
                        label = { Text("UPI Transaction ID") },
                        placeholder = { Text("e.g., UPI1234567890") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    paymentAmount.toIntOrNull()?.let { amount ->
                        if (amount > 0 && amount <= fee.pendingAmount && selectedMethod != null) {
                            onPaymentConfirm(
                                amount,
                                selectedMethod!!,
                                if (selectedMethod == PaymentMethod.UPI) upiTransactionId.takeIf { it.isNotEmpty() } else null
                            )
                        }
                    }
                },
                enabled = paymentAmount.isNotEmpty() && (paymentAmount.toIntOrNull() ?: 0) > 0 && selectedMethod != null
            ) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
