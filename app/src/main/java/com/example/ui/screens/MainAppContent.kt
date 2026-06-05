package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.*
import com.example.viewmodel.AllServicesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class Screen {
    Home,
    Login,
    RegisterProfessional,
    AdminPanel,
    HiddenSettings,
    AboutApp,
    ProviderDetail,
    ChatRooms,
    UserHistory
}

fun parseHexColor(hex: String, defaultColor: Color): Color {
    if (hex.isBlank()) return defaultColor
    return try {
        val trimmed = hex.trim().removePrefix("#")
        if (trimmed.length == 6) {
            Color(android.graphics.Color.parseColor("#$trimmed"))
        } else if (trimmed.length == 8) {
            Color(android.graphics.Color.parseColor("#$trimmed"))
        } else {
            defaultColor
        }
    } catch (e: Exception) {
        defaultColor
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: AllServicesViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Configuration & Data
    val config by viewModel.appConfig.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val mainCategories by viewModel.mainCategories.collectAsStateWithLifecycle()
    val filteredProviders by viewModel.filteredProviders.collectAsStateWithLifecycle()
    val recommendedProviders by viewModel.recommendedProviders.collectAsStateWithLifecycle()
    val pendingProviders by viewModel.pendingProviders.collectAsStateWithLifecycle()
    val activeBanners by viewModel.activeBanners.collectAsStateWithLifecycle()
    val allBanners by viewModel.allBanners.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val activityLogs by viewModel.activityLogs.collectAsStateWithLifecycle()
    val deviceWhitelist by viewModel.deviceWhitelist.collectAsStateWithLifecycle()
    val cities by viewModel.cities.collectAsStateWithLifecycle()
    val previousRequests by viewModel.previousRequests.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val selectedMinRating by viewModel.selectedMinRating.collectAsStateWithLifecycle()
    val searchRadiusKm by viewModel.searchRadiusKm.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val userPoints by viewModel.userPoints.collectAsStateWithLifecycle()
    val backupMessage by viewModel.backupStatusMessage.collectAsStateWithLifecycle()

    // Local UI States
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    val screenHistory = remember { mutableStateListOf(Screen.Home) }
    var selectedProvider by remember { mutableStateOf<ServiceProvider?>(null) }
    var isArabic by remember { mutableStateOf(true) }

    // Owner secret clicks logic
    var homeClickCount by remember { mutableStateOf(0) }
    var lastClickTime by remember { mutableStateOf(0L) }
    var showOwnerPasswordDialog by remember { mutableStateOf(false) }
    var ownerPasswordInput by remember { mutableStateOf("") }

    // Floating Widgets Dialog States
    var showAssistantDialog by remember { mutableStateOf(false) }
    var showGeneralChatDialog by remember { mutableStateOf(false) }

    // Double back tap variables for exit
    var lastBackPressTime by remember { mutableStateOf(0L) }

    // Helper functions for programmatic navigation
    fun navigateTo(screen: Screen) {
        if (screen == Screen.Home) {
            screenHistory.clear()
            screenHistory.add(Screen.Home)
        } else {
            screenHistory.add(screen)
        }
        currentScreen = screen
    }

    fun navigateBack() {
        if (screenHistory.size > 1) {
            screenHistory.removeAt(screenHistory.size - 1)
            currentScreen = screenHistory.last()
        } else {
            currentScreen = Screen.Home
        }
    }

    val isDark = true // App works mostly in a luxurious dark palette as requested
    
    val primaryAccentColor = when (config.primaryTheme) {
        "COSMIC_SILVER" -> Color(0xFF94A3B8)
        "GOLDEN" -> Color(0xFFFFD700)
        "EMERALD" -> Color(0xFF10B981)
        "BLACK" -> Color(0xFFE2E8F0)
        "GREEN" -> Color(0xFF22C55E)
        "CUSTOM" -> parseHexColor(config.customPrimaryColor, Color(0xFF10B981))
        else -> Color(0xFF3B82F6)
    }

    val primaryBrush = when (config.primaryTheme) {
        "COSMIC_SILVER" -> Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8))) // Silver Slate
        "GOLDEN" -> Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFB8860B))) // Gold
        "EMERALD" -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857))) // Emerald
        "BLACK" -> Brush.linearGradient(listOf(Color(0xFF4A4A4A), Color(0xFF1A1A1A))) // Charcoal and black
        "GREEN" -> Brush.linearGradient(listOf(Color(0xFF22C55E), Color(0xFF15803D))) // Fresh Green
        "CUSTOM" -> Brush.linearGradient(listOf(primaryAccentColor, primaryAccentColor.copy(alpha = 0.7f)))
        else -> Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))) // Blueprint
    }

    val scaffoldBgColor = when (config.primaryTheme) {
        "COSMIC_SILVER" -> Color(0xFF0F172A) // Slate
        "GOLDEN" -> Color(0xFF1C1917) // Coal Stone
        "EMERALD" -> Color(0xFF062F24) // Deep Jade Forest
        "BLACK" -> Color(0xFF000000) // Pure black
        "GREEN" -> Color(0xFF022C22) // Emerald/forest background
        "CUSTOM" -> parseHexColor(config.customSecondaryColor, Color(0xFF062F24))
        else -> Color(0xFF0B0F19)
    }

    val normalSurfaceColor = when (config.primaryTheme) {
        "COSMIC_SILVER" -> Color(0xFF1E293B)
        "GOLDEN" -> Color(0xFF292524)
        "EMERALD" -> Color(0xFF0A4F3D)
        "BLACK" -> Color(0xFF121212) // Charcoal dark surface
        "GREEN" -> Color(0xFF064E3B) // Dark Green surface
        "CUSTOM" -> {
            val bg = parseHexColor(config.customSecondaryColor, Color(0xFF062F24))
            // Slightly lighter custom surface color
            try {
                // Return slightly offset background
                bg
            } catch(e: Exception) {
                Color(0xFF1F2937)
            }
        }
        else -> Color(0xFF1F2937)
    }

    val primaryTextColor = Color.White
    // Bold white typography is highly aligned with "الخط الافتراضي: أبيض ناصع عريض"
    val defaultAppFont = FontFamily.SansSerif

    // BackPress Interceptor (Single click home / double click exit)
    BackHandler {
        val now = System.currentTimeMillis()
        if (currentScreen == Screen.Home) {
            if (now - lastBackPressTime < 2000) {
                // Exit app
                android.os.Process.killProcess(android.os.Process.myPid())
            } else {
                lastBackPressTime = now
                Toast.makeText(context, if (isArabic) "اضغط مرة أخرى للخروج" else "Press again to exit", Toast.LENGTH_SHORT).show()
            }
        } else {
            navigateBack()
        }
    }

    // Owner portal click checker
    val triggerOwnerPortal: () -> Unit = {
        val now = System.currentTimeMillis()
        if (now - lastClickTime < 800) {
            homeClickCount++
        } else {
            homeClickCount = 1
        }
        lastClickTime = now
        if (homeClickCount >= 5) {
            homeClickCount = 0
            showOwnerPasswordDialog = true
        }
    }

    // Backup notification listener
    LaunchedEffect(backupMessage) {
        backupMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.dismissBackupStatus()
        }
    }

    // Main Scaffold Design with dynamic parameters
    Surface(
        color = scaffoldBgColor,
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                // Customized RTL Top Bar
                Surface(
                    color = normalSurfaceColor,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Title / Logo Area
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { triggerOwnerPortal() }
                                .padding(4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_app_logo),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = config.appName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = defaultAppFont,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Top bar actions orderable by Admin. Default: HOME,LOGIN,REGISTER,LANG,REFRESH
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val itemsList = config.topBarIconsOrder.split(",")
                            itemsList.forEach { item ->
                                when (item.trim().uppercase()) {
                                    "HOME" -> {
                                        IconButton(
                                            onClick = { navigateTo(Screen.Home) },
                                            modifier = Modifier.testTag("nav_home")
                                        ) {
                                            Icon(
                                                Icons.Default.Home,
                                                contentDescription = "الرئيسية",
                                                tint = if (currentScreen == Screen.Home) primaryAccentColor else Color.LightGray
                                            )
                                        }
                                    }
                                    "LOGIN" -> {
                                        IconButton(
                                            onClick = { navigateTo(Screen.Login) },
                                            modifier = Modifier.testTag("nav_login")
                                        ) {
                                            Icon(
                                                Icons.Default.Lock,
                                                contentDescription = "تسجيل الدخول",
                                                tint = if (currentScreen == Screen.Login || currentScreen == Screen.AdminPanel) primaryAccentColor else Color.LightGray
                                            )
                                        }
                                    }
                                    "REGISTER" -> {
                                        IconButton(
                                            onClick = { navigateTo(Screen.RegisterProfessional) },
                                            modifier = Modifier.testTag("nav_register")
                                        ) {
                                            Icon(
                                                Icons.Default.PersonAdd,
                                                contentDescription = "تسجيل مهني",
                                                tint = if (currentScreen == Screen.RegisterProfessional) primaryAccentColor else Color.LightGray
                                            )
                                        }
                                    }
                                    "LANG" -> {
                                        IconButton(
                                            onClick = {
                                                isArabic = !isArabic
                                                Toast.makeText(context, if (isArabic) "تم تغيير اللغة للعربية" else "Language changed to English", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.testTag("nav_language")
                                        ) {
                                            Icon(
                                                Icons.Default.Language,
                                                contentDescription = "اللغة",
                                                tint = Color.LightGray
                                            )
                                        }
                                    }
                                    "REFRESH" -> {
                                        IconButton(
                                            onClick = {
                                                viewModel.clearFilters()
                                                Toast.makeText(context, if (isArabic) "تم تحديث البيانات بنجاح" else "Data refreshed successfully", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.testTag("nav_refresh")
                                        ) {
                                            Icon(
                                                Icons.Default.Refresh,
                                                contentDescription = "تحديث",
                                                tint = Color.LightGray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Elegant Footer at the bottom
                if (config.showFooter) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .background(normalSurfaceColor)
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left Side: About App Icon (ℹ️)
                            IconButton(
                                onClick = { navigateTo(Screen.AboutApp) },
                                modifier = Modifier.size(34.dp).testTag("footer_about")
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "عن التطبيق",
                                    tint = primaryAccentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Center: Promotional text (e.g. MAW 777644670) 50% smaller
                            Text(
                                text = config.promotionalFooter,
                                color = Color.Gray,
                                fontSize = 8.sp, // Smaller by 50%
                                fontWeight = FontWeight.Light,
                                fontFamily = defaultAppFont,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )

                            // Right Side: Floating services trigger buttons (🤖 Assistant & 💬 Admin chat)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (config.chatVisible) {
                                    IconButton(
                                        onClick = { showGeneralChatDialog = !showGeneralChatDialog },
                                        modifier = Modifier.size(34.dp).testTag("footer_chat")
                                    ) {
                                        Icon(
                                            Icons.Default.ChatBubble,
                                            contentDescription = "الدعم المباشر",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                if (config.assistantVisible) {
                                    Button(
                                        onClick = { showAssistantDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp).testTag("footer_assistant")
                                    ) {
                                        Text(
                                            text = config.assistantIcon + " خدمات",
                                            fontSize = 9.sp,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Maintenance Banner override
                if (config.isMaintenanceMode && currentScreen != Screen.HiddenSettings && currentScreen != Screen.Login && currentScreen != Screen.AdminPanel) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Engineering,
                            contentDescription = "صيانة",
                            tint = primaryAccentColor,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "وضع الصيانة مفعل",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = defaultAppFont,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = config.maintenanceMessage,
                            color = Color.LightGray,
                            fontSize = 16.sp,
                            fontFamily = defaultAppFont,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Normal views transition
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                        },
                        label = "screen_trans"
                    ) { screen ->
                        when (screen) {
                            Screen.Home -> HomeScreenView(
                                viewModel = viewModel,
                                isArabic = isArabic,
                                primaryAccentColor = primaryAccentColor,
                                primaryBrush = primaryBrush,
                                normalSurfaceColor = normalSurfaceColor,
                                defaultAppFont = defaultAppFont,
                                onProviderClicked = { p ->
                                    selectedProvider = p
                                    navigateTo(Screen.ProviderDetail)
                                },
                                onShowHistory = { navigateTo(Screen.UserHistory) }
                            )
                            Screen.Login -> LoginScreenView(
                                viewModel = viewModel,
                                isArabic = isArabic,
                                primaryAccentColor = primaryAccentColor,
                                primaryBrush = primaryBrush,
                                normalSurfaceColor = normalSurfaceColor,
                                defaultAppFont = defaultAppFont,
                                onLoginSuccess = { navigateTo(Screen.AdminPanel) }
                            )
                            Screen.RegisterProfessional -> RegisterProfessionalView(
                                viewModel = viewModel,
                                isArabic = isArabic,
                                primaryAccentColor = primaryAccentColor,
                                primaryBrush = primaryBrush,
                                normalSurfaceColor = normalSurfaceColor,
                                defaultAppFont = defaultAppFont,
                                onSubmitted = {
                                    Toast.makeText(context, if (isArabic) "تم تقديم طلبك للمراجعة الفورية بنجاح!" else "Join request submitted successfully!", Toast.LENGTH_LONG).show()
                                    navigateTo(Screen.Home)
                                }
                            )
                            Screen.AdminPanel -> AdminPanelView(
                                viewModel = viewModel,
                                isArabic = isArabic,
                                primaryAccentColor = primaryAccentColor,
                                primaryBrush = primaryBrush,
                                normalSurfaceColor = normalSurfaceColor,
                                defaultAppFont = defaultAppFont,
                                onLogout = { navigateTo(Screen.Home) }
                            )
                            Screen.HiddenSettings -> HiddenSettingsView(
                                viewModel = viewModel,
                                isArabic = isArabic,
                                primaryAccentColor = primaryAccentColor,
                                primaryBrush = primaryBrush,
                                normalSurfaceColor = normalSurfaceColor,
                                defaultAppFont = defaultAppFont,
                                onExit = { navigateTo(Screen.Home) }
                            )
                            Screen.AboutApp -> AboutAppView(
                                viewModel = viewModel,
                                isArabic = isArabic,
                                primaryAccentColor = primaryAccentColor,
                                normalSurfaceColor = normalSurfaceColor,
                                defaultAppFont = defaultAppFont
                            )
                            Screen.ProviderDetail -> ProviderDetailView(
                                provider = selectedProvider!!,
                                viewModel = viewModel,
                                isArabic = isArabic,
                                primaryAccentColor = primaryAccentColor,
                                normalSurfaceColor = normalSurfaceColor,
                                defaultAppFont = defaultAppFont,
                                onBack = { navigateBack() }
                            )
                            Screen.ChatRooms -> Box(modifier = Modifier.fillMaxSize())
                            Screen.UserHistory -> UserHistoryView(
                                viewModel = viewModel,
                                isArabic = isArabic,
                                primaryAccentColor = primaryAccentColor,
                                normalSurfaceColor = normalSurfaceColor,
                                defaultAppFont = defaultAppFont,
                                onBack = { navigateBack() }
                            )
                        }
                    }
                }

                // Temporary snackbar notification banner for FCM simulated alerts
                if (notifications.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopCenter)
                            .shadow(12.dp, RoundedCornerShape(12.dp))
                            .background(Color(0xFF065F46))
                            .border(1.dp, Color(0xFF34D399), RoundedCornerShape(12.dp))
                            .clickable { viewModel.clearNotifications() }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = "إشعار",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "إشعار FCM فوري" else "Instant FCM Notification",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = notifications.first(),
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    fontFamily = defaultAppFont
                                )
                            }
                            IconButton(onClick = { viewModel.clearNotifications() }) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.LightGray)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Password dialog for Secret Back Office Owner portal
    if (showOwnerPasswordDialog) {
        Dialog(onDismissRequest = { showOwnerPasswordDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = normalSurfaceColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, primaryAccentColor, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = "Secret Shield",
                        tint = primaryAccentColor,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "البوابة السرية للمالك فقط",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = defaultAppFont
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = ownerPasswordInput,
                        onValueChange = { ownerPasswordInput = it },
                        label = { Text("كلمة المرور السرية", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryAccentColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("secret_password_input")
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showOwnerPasswordDialog = false }) {
                            Text("إلغاء", color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                if (ownerPasswordInput == "maher--736462") {
                                    showOwnerPasswordDialog = false
                                    ownerPasswordInput = ""
                                    navigateTo(Screen.HiddenSettings)
                                } else {
                                    Toast.makeText(context, "الرمز السري خاطئ!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                            modifier = Modifier.testTag("secret_login_button")
                        ) {
                            Text("ولوج آمن", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // Floating dialog for the AI Smart Assistant (🤖)
    if (showAssistantDialog) {
        Dialog(onDismissRequest = { showAssistantDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = normalSurfaceColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .border(2.dp, primaryAccentColor, RoundedCornerShape(20.dp))
            ) {
                val chatMessages by viewModel.assistantChat.collectAsStateWithLifecycle()
                val isTyping by viewModel.isAssistantTyping.collectAsStateWithLifecycle()
                var promptInput by remember { mutableStateOf("") }
                val lazyListState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(config.assistantIcon, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "المساعد التفاعلي للمهن",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "متصل بالذكاء الاصطناعي ومتاح أوفلاين",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(onClick = { showAssistantDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                        }
                    }

                    Divider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))

                    // Chat messages list
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(lazyListState)
                        ) {
                            chatMessages.forEach { msg ->
                                val isUser = msg.second
                                val text = msg.first
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 12.dp,
                                                    topEnd = 12.dp,
                                                    bottomStart = if (isUser) 12.dp else 2.dp,
                                                    bottomEnd = if (isUser) 2.dp else 12.dp
                                                )
                                            )
                                            .background(if (isUser) primaryAccentColor else Color(0xFF374151))
                                            .padding(10.dp)
                                            .widthIn(max = 220.dp)
                                    ) {
                                        Text(
                                            text = text,
                                            color = if (isUser) Color.Black else Color.White,
                                            fontSize = 12.sp,
                                            fontFamily = defaultAppFont
                                        )
                                    }
                                }
                            }

                            if (isTyping) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.Start)
                                ) {
                                    Text(
                                        "جاري التحليل واستخلاص الإجابة الفورية...",
                                        color = primaryAccentColor,
                                        fontSize = 11.sp,
                                        fontFamily = defaultAppFont
                                    )
                                }
                            }
                        }
                    }

                    // Suggestions row
                    Text(
                        text = "أسئلة مقترحة سريعة:",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val suggestionsAr = listOf("ماهي الأقسام", "كيف أتصل بمقدم خدمة", "ما هو رقم الدعم")
                        suggestionsAr.forEach { text ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(normalSurfaceColor.copy(alpha = 0.5f))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.askAssistant(text) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = text, color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            placeholder = { Text("اطرح أي سؤال عن الخدمات...", color = Color.LightGray, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryAccentColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("assistant_input")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (promptInput.isNotBlank()) {
                                    viewModel.askAssistant(promptInput)
                                    promptInput = ""
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(primaryAccentColor)
                                .size(44.dp).testTag("assistant_send")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // General chat rooms list dialog
    if (showGeneralChatDialog) {
        Dialog(onDismissRequest = { showGeneralChatDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = normalSurfaceColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .border(1.dp, primaryAccentColor, RoundedCornerShape(20.dp))
            ) {
                var activeConvoId by remember { mutableStateOf<String?>("777644670_admin") }
                val currentMessages by viewModel.currentChatMessages.collectAsStateWithLifecycle()
                var chatInput by remember { mutableStateOf("") }

                // Force ViewModel load active convo messages
                LaunchedEffect(activeConvoId) {
                    activeConvoId?.let { viewModel.startConversation(it) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ChatBubble, contentDescription = "دردشة", tint = primaryAccentColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("محادثة فورية مع المالك والأدمن", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showGeneralChatDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "غلق", tint = Color.LightGray)
                        }
                    }

                    Divider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))

                    // Simulated multiple conversations switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val activeRooms = listOf("777644670_admin", "visitor_support")
                        activeRooms.forEach { room ->
                            val isSel = activeConvoId == room
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) primaryAccentColor else Color.DarkGray)
                                    .clickable { activeConvoId = room }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (room.contains("777644670")) "محادثة ماهر" else "محادثة الزوار والدعم المباشر",
                                    color = if (isSel) Color.Black else Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Messages area
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            currentMessages.forEach { msg ->
                                val isMe = msg.senderRole == "USER" || msg.senderRole == "PROVIDER"
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                                        Text(msg.senderName, fontSize = 9.sp, color = Color.Gray)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isMe) Color(0xFF1F2937) else primaryAccentColor)
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                msg.messageText,
                                                color = if (isMe) Color.White else Color.Black,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Send bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("اكتب رسالتك هنا...", color = Color.LightGray, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryAccentColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("chat_room_text_input")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    activeConvoId?.let { convo ->
                                        viewModel.sendMessage(
                                            chatInput,
                                            convo,
                                            "الزبون الزائر",
                                            "USER"
                                        )
                                        chatInput = ""
                                    }
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(primaryAccentColor)
                                .size(44.dp).testTag("chat_room_send_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "إرسال", tint = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// VIEW: Home Dashboard Screen
// ------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenView(
    viewModel: AllServicesViewModel,
    isArabic: Boolean,
    primaryAccentColor: Color,
    primaryBrush: Brush,
    normalSurfaceColor: Color,
    defaultAppFont: FontFamily,
    onProviderClicked: (ServiceProvider) -> Unit,
    onShowHistory: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val selectedMinRating by viewModel.selectedMinRating.collectAsStateWithLifecycle()
    val searchRadiusKm by viewModel.searchRadiusKm.collectAsStateWithLifecycle()
    
    val mainCategories by viewModel.mainCategories.collectAsStateWithLifecycle()
    val filteredProviders by viewModel.filteredProviders.collectAsStateWithLifecycle()
    val recommendedProviders by viewModel.recommendedProviders.collectAsStateWithLifecycle()
    val activeBanners by viewModel.activeBanners.collectAsStateWithLifecycle()
    val cities by viewModel.cities.collectAsStateWithLifecycle()
    val userPoints by viewModel.userPoints.collectAsStateWithLifecycle()
    val config by viewModel.appConfig.collectAsStateWithLifecycle()

    var showAdvancedFilters by remember { mutableStateOf(false) }

    // Speech synthesis mic recording mockup properties
    var isVoiceRecording by remember { mutableStateOf(false) }

    // Autocomplete GPS address simulation helper
    var autoAddressInput by remember { mutableStateOf("") }
    var suggestedAddresses = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Welcome Message Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(normalSurfaceColor, Color.Transparent)
                    )
                )
                .padding(16.dp)
        ) {
                val textAlignment2 = when (config.welcomeMessageAlignment) {
                    "CENTER" -> Alignment.CenterHorizontally
                    "END" -> Alignment.End
                    else -> Alignment.Start
                }
                val textAlignValue2 = when (config.welcomeMessageAlignment) {
                    "CENTER" -> TextAlign.Center
                    "END" -> TextAlign.End
                    else -> TextAlign.Start
                }

                if (config.welcomeImageUri.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        androidx.compose.foundation.Image(
                            painter = coil.compose.rememberAsyncImagePainter(config.welcomeImageUri),
                            contentDescription = "صورة الترحيب المعتمدة",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = textAlignment2
                    ) {
                        Text(
                            text = if (isArabic) "كل الخدمات بين يديك" else "All Services In Your Hands",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = defaultAppFont
                        )
                        Text(
                            text = config.welcomeMessage,
                            color = Color.LightGray,
                            fontSize = config.welcomeMessageFontSize.sp,
                            fontFamily = defaultAppFont,
                            textAlign = textAlignValue2,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
        }

        // Active Bannered Ads slider
        if (activeBanners.isNotEmpty()) {
            Text(
                text = if (isArabic) "📌 إعلانات وبانرات مميزة" else "📌 Featured Ads",
                color = primaryAccentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeBanners) { banner ->
                    val isLarge = banner.size == "LARGE"
                    Card(
                        modifier = Modifier
                            .width(if (isLarge) 320.dp else 260.dp)
                            .height(110.dp)
                            .clickable {
                                Toast
                                    .makeText(
                                        context,
                                        "${if (isArabic) "التوجه لـ:" else "Redirecting to:"} ${banner.linkUrl}",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
                        border = BorderStroke(1.dp, primaryAccentColor.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.img_home_banner),
                                contentDescription = "Banner",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Overlay gradient for textual legibility
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Text(
                                    text = banner.title,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = defaultAppFont,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search & Advanced Filter Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text(if (isArabic) "ابحث عن سباك، كهربائي، دهان..." else "Search...", color = Color.Gray, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryAccentColor,
                            unfocusedBorderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("search_input_field")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Speech recognizer button mockup
                    IconButton(
                        onClick = {
                            isVoiceRecording = true
                            scope.launch {
                                delay(2200) // Simulated delay
                                isVoiceRecording = false
                                viewModel.updateSearchQuery("كهربائي ممتاز ماهر")
                                Toast.makeText(context, if (isArabic) "تم التقاط الصوت: كهربائي ممتاز ماهر" else "Voice captured: Electrician mathert", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(primaryAccentColor)
                            .size(44.dp).testTag("speech_voice_search_btn")
                    ) {
                        Icon(
                            if (isVoiceRecording) Icons.Default.MicNone else Icons.Default.Mic,
                            contentDescription = "بحث صوتي",
                            tint = Color.Black
                        )
                    }
                }

                if (isVoiceRecording) {
                    Text(
                        text = "🎤 جاري الاستماع عبر الذكاء الاصطناعي الصوتي المدمج...",
                        color = primaryAccentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showAdvancedFilters = !showAdvancedFilters },
                        colors = ButtonDefaults.textButtonColors(contentColor = primaryAccentColor)
                    ) {
                        Icon(if (showAdvancedFilters) Icons.Default.FilterListOff else Icons.Default.FilterList, contentDescription = "فلاتر")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showAdvancedFilters) "إغلاق التصفية المتقدمة" else "فلاتر وبحث جغرافي متطور", fontSize = 11.sp)
                    }

                    if (searchQuery.isNotEmpty() || selectedCity != null || selectedCategoryId != null || selectedMinRating != null || searchRadiusKm != null) {
                        TextButton(onClick = { viewModel.clearFilters() }) {
                            Text("تصفير الفلاتر 🔄", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }

                // Advanced filters details inside collapsible panel
                AnimatedVisibility(visible = showAdvancedFilters) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Category Selector Dropdown
                        Text("القسم الرئيسي التخصصي:", color = Color.White, fontSize = 11.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedCategoryId == null) primaryAccentColor else Color.DarkGray)
                                    .clickable { viewModel.selectCategory(null) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "الكل",
                                    color = if (selectedCategoryId == null) Color.Black else Color.White,
                                    fontSize = 11.sp
                                )
                            }
                            mainCategories.forEach { cat ->
                                val isSelected = selectedCategoryId == cat.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) primaryAccentColor else Color.DarkGray)
                                        .clickable { viewModel.selectCategory(cat.id) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        cat.nameAr,
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // City dropdown select list
                        Text("المدينة أو المحافظة السكنية:", color = Color.White, fontSize = 11.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedCity == null) primaryAccentColor else Color.DarkGray)
                                    .clickable { viewModel.selectCity(null) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "كل المدن",
                                    color = if (selectedCity == null) Color.Black else Color.White,
                                    fontSize = 11.sp
                                )
                            }
                            cities.forEach { city ->
                                val isSelected = selectedCity == city.nameAr
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) primaryAccentColor else Color.DarkGray)
                                        .clickable { viewModel.selectCity(city.nameAr) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        city.nameAr,
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Radius Search (محاكاة البحث بنصف القطر عبر الخريطة)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("تحديد دائرة نصف القطر (البحث الجغرافي):", color = Color.White, fontSize = 11.sp)
                                Text(
                                    if (searchRadiusKm == null) "الكل" else "${searchRadiusKm} كم كحد أقصى",
                                    color = primaryAccentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = (searchRadiusKm ?: 50).toFloat(),
                                onValueChange = { viewModel.selectRadius(it.toInt()) },
                                valueRange = 1f..50f,
                                colors = SliderDefaults.colors(
                                    thumbColor = primaryAccentColor,
                                    activeTrackColor = primaryAccentColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Rating filter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("الحد الأدنى لتقييم مقدم الخدمة:", color = Color.White, fontSize = 11.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val ratings = listOf(3.0f, 4.0f, 4.5f, 4.8f)
                                ratings.forEach { score ->
                                    val isSelected = selectedMinRating == score
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) primaryAccentColor else Color.DarkGray)
                                            .clickable { viewModel.selectMinRating(if (isSelected) null else score) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "⭐️$score+",
                                            color = if (isSelected) Color.Black else Color.White,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Map component simulator ( pins of workers by location radius)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "🗺️ محاكي خريطة الرادار للخدمات المتاحة",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = defaultAppFont
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = primaryAccentColor.copy(alpha = 0.15f), radius = size.minDimension / 1.5f)
                        drawCircle(color = primaryAccentColor.copy(alpha = 0.35f), radius = size.minDimension / 3.5f)
                    }
                    Text(
                        text = "جاري تتبع ورسم علامات الحضور المحيطة بك بدقة GPS المتحدثة تلقائياً...",
                        color = Color.LightGray,
                        fontSize = 9.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        // Categories List Horizontal Icons
        Text(
            text = if (isArabic) "🗂️ تصنيفات وأقسام المهن" else "🗂️ Categories",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = defaultAppFont,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(mainCategories) { cat ->
                val isSelected = selectedCategoryId == cat.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) primaryAccentColor else normalSurfaceColor)
                        .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectCategory(if (isSelected) null else cat.id) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        cat.nameAr,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = defaultAppFont
                    )
                }
            }
        }

        // Loyalty points and past request logs shortcuts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Loyalty Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        Toast
                            .makeText(
                                context,
                                "نقاط ولاء الزبون: تمكنك من استبدالها بجوائز وخصومات لدى الفنيين المعتمدين!",
                                Toast.LENGTH_LONG
                            )
                            .show()
                    },
                colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Stars, contentDescription = "Points", tint = Color(0xFFFBBF24))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("نقاط الولاء والمكافآت", color = Color.LightGray, fontSize = 9.sp)
                        Text("$userPoints نقطة ولاء", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Past History Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onShowHistory() },
                colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.History, contentDescription = "History", tint = primaryAccentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("الطلبات السابقة", color = Color.LightGray, fontSize = 9.sp)
                        Text("سجل تواصلك", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Service Providers Title with results count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isArabic) "🛠️ مقدمو الخدمات المعتمدون" else "🛠️ Service Providers",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = defaultAppFont
            )
            Text(
                text = "${filteredProviders.size} مهني متاح أونلاين",
                color = Color.LightGray,
                fontSize = 10.sp,
                fontFamily = defaultAppFont
            )
        }

        // Empty state check
        if (filteredProviders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.ManageSearch,
                    contentDescription = "No Results",
                    tint = Color.Gray,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "عذراً، لا يوجد مقدمو خدمة ببيانات التصفية الحالية.",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontFamily = defaultAppFont,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Providers cards loop
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filteredProviders.forEach { provider ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProviderClicked(provider) }
                            .testTag("provider_item_${provider.id}"),
                        colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Profile Avatar
                            Box(modifier = Modifier.size(54.dp)) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_app_logo),
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, primaryAccentColor, CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                // Subscription status crown / star overlay
                                if (provider.isSubscribed && provider.subscriptionStatus == "APPROVED") {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEAB308))
                                            .border(1.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👑", fontSize = 8.sp, color = Color.White)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = provider.fullName,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = defaultAppFont
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Verified status checker
                                    if (provider.isVerified) {
                                        Icon(
                                            Icons.Default.Verified,
                                            contentDescription = "موثق بالشارة الزرقاء",
                                            tint = Color(0xFF3B82F6),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.padding(top = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Categories locator text
                                    val catName = mainCategories.find { it.id == provider.mainCategoryId }?.nameAr ?: "خدمات عامة"
                                    Icon(Icons.Default.Build, contentDescription = null, tint = primaryAccentColor, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = catName,
                                        color = primaryAccentColor,
                                        fontSize = 10.sp,
                                        fontFamily = defaultAppFont
                                    )
                                }

                                Row(
                                    modifier = Modifier.padding(top = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${provider.area} - ${provider.address}",
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        fontFamily = defaultAppFont,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Rating and Pinned status badges on the right side
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = "Stars", tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format(Locale.US, "%.1f", provider.averageRating),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "(${provider.ratingCount} تقييم)",
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontFamily = defaultAppFont
                                )

                                if (provider.isPinned) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(primaryAccentColor.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "مبتثب بالصدارة ⭐",
                                            color = primaryAccentColor,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ------------------------------------------------------------------
// VIEW: Login Screen
// ------------------------------------------------------------------
@Composable
fun LoginScreenView(
    viewModel: AllServicesViewModel,
    isArabic: Boolean,
    primaryAccentColor: Color,
    primaryBrush: Brush,
    normalSurfaceColor: Color,
    defaultAppFont: FontFamily,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = "تسجيل الدخول",
            tint = primaryAccentColor,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "تسجيل دخول المدراء والمشرفين",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = defaultAppFont
        )
        Text(
            text = "لوحة التحكم وإدارة طلبات المراجعة الفورية",
            color = Color.LightGray,
            fontSize = 11.sp,
            fontFamily = defaultAppFont,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("اسم المستخدم", color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = primaryAccentColor,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("username_input_field")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور", color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = primaryAccentColor,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("password_input_field")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(checkedColor = primaryAccentColor)
                    )
                    Text("تذكر معلومات تسجيل الدخول وحفظها", color = Color.White, fontSize = 11.sp, fontFamily = defaultAppFont)
                }

                Button(
                    onClick = {
                        if (viewModel.login(username, password)) {
                            Toast.makeText(context, "أهلاً بك! تم التحقق والدخول بنجاح إلى لوحة الإشراف.", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, "عذراً، معلومات تسجيل الدخول للمدراء أو المشرفين خاطئة!", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("login_action_btn")
                ) {
                    Text("تسجيل الدخول الآمن للكونسول", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// VIEW: Create Professional registration request (👤)
// ------------------------------------------------------------------
@Composable
fun RegisterProfessionalView(
    viewModel: AllServicesViewModel,
    isArabic: Boolean,
    primaryAccentColor: Color,
    primaryBrush: Brush,
    normalSurfaceColor: Color,
    defaultAppFont: FontFamily,
    onSubmitted: () -> Unit
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val mainCategories = categories.filter { it.parentId == null }
    val config by viewModel.appConfig.collectAsStateWithLifecycle()

    val inputFieldBgValue = parseHexColor(config.inputFieldBgColor, normalSurfaceColor)
    val customTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = primaryAccentColor,
        unfocusedBorderColor = Color.Gray,
        focusedContainerColor = inputFieldBgValue,
        unfocusedContainerColor = inputFieldBgValue,
        disabledContainerColor = inputFieldBgValue,
        focusedPlaceholderColor = Color.Gray,
        unfocusedPlaceholderColor = Color.Gray,
        focusedLabelColor = Color.LightGray,
        unfocusedLabelColor = Color.LightGray
    )

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedSubCategoryId by remember { mutableStateOf<Int?>(null) }
    var address by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var gpsCoordinates by remember { mutableStateOf("") }

    // Photo and ID capture (with simulated upload options)
    var isProfileImageUploaded by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf("") }
    var isIdImageUploaded by remember { mutableStateOf(false) }
    var idImageUri by remember { mutableStateOf("") }

    val subCategories = categories.filter { it.parentId == selectedCategoryId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "استمارة تسجيل أصحاب المهن اليدوية والكوادر",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = defaultAppFont
        )
        Text(
            text = "أدخل بياناتك كاملة للحصول على شارة التوثيق وطلب المراجعة الفورية.",
            color = Color.LightGray,
            fontSize = 11.sp,
            fontFamily = defaultAppFont
        )

        Divider(color = Color.Gray)

        // Text Fields
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("الاسم الثلاثي الكامل (إجباري)", color = Color.LightGray) },
            colors = customTextFieldColors,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("register_fullname")
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("رقم الهاتف الفعال / واتساب (إجباري)", color = Color.LightGray) },
            colors = customTextFieldColors,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("register_phone")
        )

        // 1. SELECT MAIN CATEGORY
        Text("١. اختر القسم الرئيسي (إجباري):", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mainCategories) { cat ->
                val isSelected = selectedCategoryId == cat.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) primaryAccentColor else normalSurfaceColor)
                        .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { 
                            selectedCategoryId = cat.id 
                            selectedSubCategoryId = null // Reset subclass selector
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        cat.nameAr,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = defaultAppFont
                    )
                }
            }
        }

        // 2. SELECT SUBCATEGORY (Only visible if Main Category is selected!)
        if (selectedCategoryId != null) {
            Text("٢. اختر الخدمة / التخصص الفرعي (إجباري):", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            if (subCategories.isEmpty()) {
                Text("لا توجد خدمات فرعية حالياً في هذا القسم الرئيسي.", color = Color.Yellow, fontSize = 11.sp)
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(subCategories) { subCat ->
                        val isSelected = selectedSubCategoryId == subCat.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) primaryAccentColor else Color.DarkGray)
                                .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedSubCategoryId = subCat.id }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                subCat.nameAr,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = defaultAppFont
                            )
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("مكان وعنوان مركز/مكتب العمل الحالي (إجباري)", color = Color.LightGray) },
            colors = customTextFieldColors,
            modifier = Modifier.fillMaxWidth().testTag("register_address")
        )

        OutlinedTextField(
            value = area,
            onValueChange = { area = it },
            label = { Text("منطقة الدائرة السكنية الحالية (إجباري)", color = Color.LightGray) },
            colors = customTextFieldColors,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("register_area")
        )

        // GPS simulator with a helper autocomplete autofill button!
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = gpsCoordinates,
                onValueChange = { gpsCoordinates = it },
                label = { Text("إحداثيات وموقع الخريطة GPS (اختياري)", color = Color.LightGray) },
                colors = customTextFieldColors,
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("register_gps")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = {
                    gpsCoordinates = "15.3522,44.2018"
                    Toast.makeText(context, "تم جلب إحداثيات موقعك الحالي بنجاح!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("تحديد الموضع 📡", color = Color.White, fontSize = 10.sp)
            }
        }

        // Live Profile Image Preview if Uploaded
        if (isProfileImageUploaded && profileImageUri.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.Image(
                    painter = coil.compose.rememberAsyncImagePainter(profileImageUri),
                    contentDescription = "الصورة الشخصية المرفوعة",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, primaryAccentColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("صورتك الشخصية المعتمدة", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("جاهزة للإرسال ومعاينة الإدارة", color = Color.LightGray, fontSize = 9.sp)
                }
            }
        }

        // Camera / Gallery simulation capture cards
        Text("٣. إضافة المرفقات والصور الرسمية:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth()) {
            // Personal selfie card picker
            Card(
                modifier = Modifier
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
                border = BorderStroke(1.dp, if (isProfileImageUploaded) primaryAccentColor else Color.Gray)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (isProfileImageUploaded) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                        contentDescription = "Portrait Selfie Image",
                        tint = if (isProfileImageUploaded) primaryAccentColor else Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (isProfileImageUploaded) "تم تحديد السيلفي الشخصي" else "صورة سيلفي الشخصية (إجباري)",
                        fontSize = 9.sp,
                        fontFamily = defaultAppFont,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                isProfileImageUploaded = true
                                profileImageUri = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=250&auto=format&fit=crop"
                                Toast.makeText(context, "التقطت الصورة سيلفي عبر الكاميرا 📸", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f).height(24.dp)
                        ) {
                            Text("كاميرا 📸", fontSize = 8.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        Button(
                            onClick = {
                                isProfileImageUploaded = true
                                profileImageUri = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=250&auto=format&fit=crop"
                                Toast.makeText(context, "تم اختيار السيلفي من الاستوديو 🖼️", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f).height(24.dp)
                        ) {
                            Text("استوديو 🖼️", fontSize = 8.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // National ID card picker
            Card(
                modifier = Modifier
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
                border = BorderStroke(1.dp, if (isIdImageUploaded) primaryAccentColor else Color.Gray)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (isIdImageUploaded) Icons.Default.CheckCircle else Icons.Default.FileOpen,
                        contentDescription = "National Identity Image File Selection Picker",
                        tint = if (isIdImageUploaded) primaryAccentColor else Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (isIdImageUploaded) "بطاقة الهوية محفوظة" else "البطاقة الشخصية (اختياري)",
                        fontSize = 9.sp,
                        fontFamily = defaultAppFont,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                isIdImageUploaded = true
                                idImageUri = "https://images.unsplash.com/photo-1606136936357-ff33f4435ac5?q=80&w=250&auto=format&fit=crop"
                                Toast.makeText(context, "تم التقاط صورة بطاقة الهوية بالكاميرا 📸", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f).height(24.dp)
                        ) {
                            Text("كاميرا 📸", fontSize = 8.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        Button(
                            onClick = {
                                isIdImageUploaded = true
                                idImageUri = "https://images.unsplash.com/photo-1554774853-aae0a22c8aa4?q=80&w=250&auto=format&fit=crop"
                                Toast.makeText(context, "تم اختيار البطاقة الشخصية من الاستوديو 🖼️", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f).height(24.dp)
                        ) {
                            Text("استوديو 🖼️", fontSize = 8.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Action submit button
        Button(
            onClick = {
                val splitName = fullName.trim().split("\\s+".toRegex())
                if (fullName.isBlank() || phone.isBlank() || selectedCategoryId == null || selectedSubCategoryId == null || address.isBlank() || area.isBlank() || !isProfileImageUploaded) {
                    Toast.makeText(context, "الرجاء تعبئة كافة الحقول الإجبارية والتقاط الصورة الشخصية للتأكيد المسبق!", Toast.LENGTH_LONG).show()
                } else if (splitName.size < 3) {
                    Toast.makeText(context, "يجب إدخال الاسم ثلاثياً على الأقل لضمان قبول الطلب ومراجعته!", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.submitPendingProvider(
                        name = fullName,
                        phone = phone,
                        categoryId = selectedSubCategoryId!!, // Passes subclass selection to database
                        address = address,
                        area = area,
                        profileImage = if (profileImageUri.isNotBlank()) profileImageUri else "img_profile_placeholder.png",
                        idImage = if (idImageUri.isNotBlank()) idImageUri else "img_id_placeholder.png",
                        gps = gpsCoordinates
                    )
                    Toast.makeText(context, "تم تقديم طلبك بنجاح للفرز والمراجعة من المشرفين والمدير!", Toast.LENGTH_LONG).show()
                    onSubmitted()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_joining_btn")
        ) {
            Text("تقديم طلب الانضمام للمراجعة الفورية", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
        }
    }
}

// ------------------------------------------------------------------
// VIEW: Admin Control Panel Dashboard
// ------------------------------------------------------------------
@Composable
fun AdminPanelView(
    viewModel: AllServicesViewModel,
    isArabic: Boolean,
    primaryAccentColor: Color,
    primaryBrush: Brush,
    normalSurfaceColor: Color,
    defaultAppFont: FontFamily,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val pendingList by viewModel.pendingProviders.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val cities by viewModel.cities.collectAsStateWithLifecycle()
    val activityLogs by viewModel.activityLogs.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val allBanners by viewModel.allBanners.collectAsStateWithLifecycle()
    val supervisors by viewModel.supervisors.collectAsStateWithLifecycle()

    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val activeSupervisor by viewModel.currentSupervisorPermissions.collectAsStateWithLifecycle()
    val config by viewModel.appConfig.collectAsStateWithLifecycle()

    val inputFieldBgValue = parseHexColor(config.inputFieldBgColor, normalSurfaceColor)
    val customTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = primaryAccentColor,
        unfocusedBorderColor = Color.Gray,
        focusedContainerColor = inputFieldBgValue,
        unfocusedContainerColor = inputFieldBgValue,
        disabledContainerColor = inputFieldBgValue,
        focusedPlaceholderColor = Color.Gray,
        unfocusedPlaceholderColor = Color.Gray,
        focusedLabelColor = Color.LightGray,
        unfocusedLabelColor = Color.LightGray
    )

    // Permissions logic
    val canAcceptReject = if (currentUserRole == "OWNER") true else (activeSupervisor?.canAcceptRejectRequests == true)
    val canManageCategories = if (currentUserRole == "OWNER") true else (activeSupervisor?.canManageCategories == true)
    val canManageProviders = if (currentUserRole == "OWNER") true else (activeSupervisor?.canManageProviders == true)
    val canViewReports = if (currentUserRole == "OWNER") true else (activeSupervisor?.canViewReports == true)

    // Dynamic tabs listing
    val tabs = remember(currentUserRole, pendingList.size, reports.size, supervisors.size) {
        val list = mutableListOf(
            "طلبات الانضمام (${pendingList.size})",
            "إدارة وتصنيف الأقسام",
            "سجل النشاط",
            "بلاغات الزوار (${reports.size})",
            "إدارة البانرات واللافتات",
            "إضافة فني مباشر"
        )
        if (currentUserRole == "OWNER") {
            list.add("المشرفين والصلاحيات (${supervisors.size})")
            list.add("إعدادات رسالة الترحيب والخطوط")
        }
        list
    }

    // Edit Category dialog state helper
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Welcome and logout bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (currentUserRole == "OWNER") "مالك التطبيق (المدير العام)" else "حساب مشرف: ${activeSupervisor?.name ?: ""}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = defaultAppFont
                )
                Text(
                    text = if (currentUserRole == "OWNER") "صلاحية كونسول كاملة ومطلقة 👑" else "صلاحية إشرافية محددة ⚙️",
                    color = primaryAccentColor,
                    fontSize = 11.sp,
                    fontFamily = defaultAppFont
                )
            }
            IconButton(
                onClick = { 
                    viewModel.logout()
                    onLogout() 
                },
                modifier = Modifier.clip(CircleShape).background(Color.Red).size(36.dp).testTag("logout_dashboard_btn")
            ) {
                Icon(Icons.Default.Logout, contentDescription = "تسجيل الخروج", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs selectors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSel = selectedTab == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) primaryAccentColor else normalSurfaceColor)
                        .clickable { selectedTab = index }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = title,
                        color = if (isSel) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = defaultAppFont
                    )
                }
            }
        }

        Divider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))

        // Edit Category Dialog
        if (editingCategory != null) {
            var edtNameAr by remember { mutableStateOf(editingCategory!!.nameAr) }
            var edtNameEn by remember { mutableStateOf(editingCategory!!.nameEn) }
            var edtImageUrl by remember { mutableStateOf(editingCategory!!.imageUrl) }
            
            androidx.compose.ui.window.Dialog(onDismissRequest = { editingCategory = null }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("تعديل بيانات القسم", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
                        OutlinedTextField(
                            value = edtNameAr,
                            onValueChange = { edtNameAr = it },
                            label = { Text("الاسم بالعربية", color = Color.LightGray) }
                        )
                        OutlinedTextField(
                            value = edtNameEn,
                            onValueChange = { edtNameEn = it },
                            label = { Text("الاسم بالإنجليزية", color = Color.LightGray) }
                        )
                        OutlinedTextField(
                            value = edtImageUrl,
                            onValueChange = { edtImageUrl = it },
                            label = { Text("رابط أو مسار الصورة", color = Color.LightGray) }
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { editingCategory = null }) {
                                Text("إلغاء", color = Color.LightGray)
                            }
                            Button(
                                onClick = {
                                    if (edtNameAr.isNotBlank() && edtNameEn.isNotBlank()) {
                                        viewModel.updateCategory(editingCategory!!, edtNameAr, edtNameEn, edtImageUrl, editingCategory!!.parentId)
                                        editingCategory = null
                                        Toast.makeText(context, "تم تعديل القسم بنجاح في قاعدة البيانات!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor)
                            ) {
                                Text("حفظ التغييرات", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }

        // Active tab container
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: Pending requests review list with images and permission control
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (pendingList.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("لا توجد أي طلبات انضمام مهنية قيد المراجعة الفورية حالياً.", color = Color.Gray, fontSize = 12.sp, fontFamily = defaultAppFont)
                                }
                            }
                        }
                        items(pendingList) { pending ->
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("pending_request_${pending.id}"),
                                colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PendingActions, contentDescription = null, tint = primaryAccentColor)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(pending.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = defaultAppFont)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("رقم الهاتف: ${pending.phone}", color = Color.LightGray, fontSize = 11.sp, fontFamily = defaultAppFont)
                                    Text("المنطقة السكنية: ${pending.area}", color = Color.LightGray, fontSize = 11.sp, fontFamily = defaultAppFont)
                                    Text("العنوان الحالي: ${pending.address}", color = Color.LightGray, fontSize = 11.sp, fontFamily = defaultAppFont)
                                    Text("إحداثيات GPS: ${pending.gpsCoordinates}", color = Color.LightGray, fontSize = 11.sp, fontFamily = defaultAppFont)

                                    // Display actual selfies and ID cards submitted
                                    Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("الصورة الشخصية (إجباري)", color = Color.Gray, fontSize = 9.sp, fontFamily = defaultAppFont)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(75.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.DarkGray)
                                                    .border(1.dp, primaryAccentColor, RoundedCornerShape(8.dp))
                                                    .clickable { Toast.makeText(context, "معاينة كاملة للصورة الشخصية للمتقدم!", Toast.LENGTH_SHORT).show() }
                                            ) {
                                                androidx.compose.foundation.Image(
                                                    painter = coil.compose.rememberAsyncImagePainter(
                                                        model = if (pending.profilePhotoUrl.isNotBlank()) pending.profilePhotoUrl else "https://photos.example.com/avatar.png"
                                                    ),
                                                    contentDescription = "Portrait Selfie Image",
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("بطاقة الهوية (اختياري)", color = Color.Gray, fontSize = 9.sp, fontFamily = defaultAppFont)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(75.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.DarkGray)
                                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                                    .clickable { Toast.makeText(context, "معاينة كاملة لبطاقة هوية المتقدم!", Toast.LENGTH_SHORT).show() }
                                            ) {
                                                if (pending.idPhotoUrl.isNotBlank() && pending.idPhotoUrl != "img_id_placeholder.png") {
                                                    androidx.compose.foundation.Image(
                                                        painter = coil.compose.rememberAsyncImagePainter(model = pending.idPhotoUrl),
                                                        contentDescription = "National Identity Image",
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                        Text("غير مرفقة", color = Color.LightGray, fontSize = 9.sp, fontFamily = defaultAppFont)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Button(
                                            onClick = { 
                                                if (canAcceptReject) {
                                                    viewModel.rejectPendingProvider(pending.id, "المعلومات الشخصية غير واضحة")
                                                    Toast.makeText(context, "تم رفض الطلب بنجاح", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "ليس لليك صلاحية قبول أو رفض الطلبات!", Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("reject_btn")
                                        ) {
                                            Text("رفض الطلب ❌", color = Color.White, fontSize = 10.sp, fontFamily = defaultAppFont)
                                        }
                                        Button(
                                            onClick = { 
                                                if (canAcceptReject) {
                                                    viewModel.approvePendingProvider(pending.id)
                                                    Toast.makeText(context, "تم قبول وتوثيق الفني بنجاح", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "ليس لليك صلاحية قبول أو رفض الطلبات!", Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("approve_btn")
                                        ) {
                                            Text("قبول الطلب ✔️", color = Color.Black, fontSize = 10.sp, fontFamily = defaultAppFont)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // TAB 1: Categories management (Main & Secondary branch adding + changing names/images)
                    var nameArInput by remember { mutableStateOf("") }
                    var nameEnInput by remember { mutableStateOf("") }
                    var imageSourceInput by remember { mutableStateOf("") }
                    var selectedParentId by remember { mutableStateOf<Int?>(null) }
                    var currentAddType by remember { mutableStateOf(0) } // 0 = Main, 1 = Subcategory

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (!canManageCategories) {
                            Text("عذراً، هذا الحساب التابع للمشرف ليس لديه صلاحية إدارة أو تعديل الأقسام.", color = Color.Red, fontSize = 11.sp, fontFamily = defaultAppFont)
                        } else {
                            Text("نوع إدخال القسم الجديد:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { currentAddType = 0; selectedParentId = null },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (currentAddType == 0) primaryAccentColor else Color.DarkGray)
                                ) {
                                    Text("قسم رئيسي أساسي 📂", color = if (currentAddType == 0) Color.Black else Color.White, fontSize = 10.sp, fontFamily = defaultAppFont)
                                }
                                Button(
                                    onClick = { currentAddType = 1 },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (currentAddType == 1) primaryAccentColor else Color.DarkGray)
                                ) {
                                    Text("قسم فرعي تخصصي 🌿", color = if (currentAddType == 1) Color.Black else Color.White, fontSize = 10.sp, fontFamily = defaultAppFont)
                                }
                            }

                            if (currentAddType == 1) {
                                Text("اختر القسم الرئيسي الحاضن:", color = Color.White, fontSize = 11.sp, fontFamily = defaultAppFont)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(categories.filter { it.parentId == null }) { parent ->
                                        val isSel = selectedParentId == parent.id
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) primaryAccentColor else normalSurfaceColor)
                                                .clickable { selectedParentId = parent.id }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(parent.nameAr, fontSize = 10.sp, color = if (isSel) Color.Black else Color.White, fontFamily = defaultAppFont)
                                        }
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = nameArInput,
                                    onValueChange = { nameArInput = it },
                                    placeholder = { Text("الاسم بالعربية (إجباري)", color = Color.Gray, fontSize = 11.sp) },
                                    colors = customTextFieldColors,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = nameEnInput,
                                    onValueChange = { nameEnInput = it },
                                    placeholder = { Text("الاسم بالإنجليزية (إجباري)", color = Color.Gray, fontSize = 11.sp) },
                                    colors = customTextFieldColors,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            OutlinedTextField(
                                value = imageSourceInput,
                                onValueChange = { imageSourceInput = it },
                                placeholder = { Text("رابط أو مسار صورة القسم (اختياري - WebP, PNG)", color = Color.Gray, fontSize = 11.sp) },
                                colors = customTextFieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    if (nameArInput.isBlank() || nameEnInput.isBlank()) {
                                        Toast.makeText(context, "الرجاء كتاية الاسم بالعربي والانكليزي أولاً!", Toast.LENGTH_SHORT).show()
                                    } else if (currentAddType == 1 && selectedParentId == null) {
                                        Toast.makeText(context, "الرجاء تحديد القسم الرئيسي الحاضن للقسم الفرعي!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        if (currentAddType == 0) {
                                            viewModel.createMainCategory(nameArInput, nameEnInput, imageSourceInput)
                                        } else {
                                            viewModel.createSubCategory(nameArInput, nameEnInput, selectedParentId!!, imageSourceInput)
                                        }
                                        nameArInput = ""
                                        nameEnInput = ""
                                        imageSourceInput = ""
                                        Toast.makeText(context, "تمت إضافة التبويب وحفظ التعديلات بنجاح!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("حفظ وإضافة تصنيف بقاعدة البيانات 💾", color = Color.Black, fontSize = 12.sp, fontFamily = defaultAppFont)
                            }
                        }

                        Divider(color = Color.DarkGray)

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories) { cat ->
                                val parentName = if (cat.parentId != null) {
                                    val parent = categories.firstOrNull { it.id == cat.parentId }
                                    " (تحت: ${parent?.nameAr ?: ""})"
                                } else " (رئيسي)"

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(normalSurfaceColor, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${cat.nameAr} - ${cat.nameEn}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
                                        Text("النوع: $parentName", color = Color.Gray, fontSize = 10.sp, fontFamily = defaultAppFont)
                                    }
                                    if (canManageCategories) {
                                        Row {
                                            IconButton(onClick = { editingCategory = cat }) {
                                                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = primaryAccentColor)
                                            }
                                            IconButton(onClick = { 
                                                viewModel.deleteCategory(cat) 
                                                Toast.makeText(context, "تم حذف التصنيف بنجاح", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // TAB 2: Activity Logs
                    if (!canViewReports) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("عذراً، هذا الحساب الإشرافي غير مصرح له بقراءة سجلات النشاط والتحركات.", color = Color.Red, fontSize = 11.sp, fontFamily = defaultAppFont)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(activityLogs) { log ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = normalSurfaceColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("[ ${log.actor} ]", color = primaryAccentColor, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = defaultAppFont)
                                            Text(log.action, color = Color.White, fontSize = 11.sp, fontFamily = defaultAppFont)
                                        }
                                        val dateStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                                        Text(dateStr, color = Color.Gray, fontSize = 10.sp, fontFamily = defaultAppFont)
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // TAB 3: Reports
                    if (!canViewReports) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("عذراً، هذا الحساب الإشرافي ليس لديه إذن قراءة بلاغات وشكاوى الزوار.", color = Color.Red, fontSize = 11.sp, fontFamily = defaultAppFont)
                        }
                    } else {
                        Column {
                            Button(
                                onClick = {
                                    Toast.makeText(context, "تم استخراج ملف البلاغات الشكاوى بنجاح بتنسيق CSV للتسويات الورقية!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                            ) {
                                Text("تصدير تقرير البلاغات إلى CSV مفرزة بالتوقيت 📥", color = Color.White, fontSize = 11.sp, fontFamily = defaultAppFont)
                            }

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (reports.isEmpty()) {
                                    item { Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { Text("لا توجد أي بلاغات مسجلة ضد مقدمي الخدمات.", color = Color.Gray, fontSize = 12.sp, fontFamily = defaultAppFont) } }
                                }
                                items(reports) { r ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = normalSurfaceColor)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("البلاغ ضد مقدم الخدمة: ${r.providerName}", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
                                            Text("السبب: ${r.reportReason}", color = Color.White, fontSize = 12.sp, fontFamily = defaultAppFont)
                                            Text("اسم المرسل: ${r.reporterName}", color = Color.LightGray, fontSize = 10.sp, fontFamily = defaultAppFont)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                                TextButton(onClick = { 
                                                    viewModel.deleteReport(r.id) 
                                                    Toast.makeText(context, "تم تسوية البلاغ وحذفه بنجاح", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Text("تسوية البلاغ ومعالجته وتجاهله ✔️", color = primaryAccentColor, fontSize = 10.sp, fontFamily = defaultAppFont)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // TAB 4: Managing Banner Ads
                    var titleInput by remember { mutableStateOf("") }
                    var urlInput by remember { mutableStateOf("") }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("إنشاء بانر / لافتة دعائية جديدة لتظهر في الواجهة الرئيسية:", color = Color.White, fontSize = 11.sp, fontFamily = defaultAppFont)
                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            placeholder = { Text("عنوان البانر أو المحتوى النصي", color = Color.Gray, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        )
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            placeholder = { Text("رابط التوجيه عند النقر", color = Color.Gray, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        )
                        Button(
                            onClick = {
                                if (titleInput.isNotBlank() && urlInput.isNotBlank()) {
                                    viewModel.createBanner(titleInput, "img_banner.png", urlInput, "IMAGE", "MEDIUM", 6)
                                    titleInput = ""
                                    urlInput = ""
                                    Toast.makeText(context, "تم إعداد وعرض البانر بنجاح!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("أضف لافتة الآن", color = Color.Black, fontSize = 12.sp, fontFamily = defaultAppFont)
                        }

                        Divider(color = Color.DarkGray)

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(allBanners) { b ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(normalSurfaceColor, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(b.title, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f), fontFamily = defaultAppFont)
                                    IconButton(onClick = { viewModel.deleteBanner(b) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
                5 -> {
                    // TAB 5: Direct Add provider with subcategory supporting
                    var mName by remember { mutableStateOf("") }
                    var mPhone by remember { mutableStateOf("") }
                    var mAddress by remember { mutableStateOf("") }
                    var mArea by remember { mutableStateOf("") }
                    var mCatId by remember { mutableStateOf<Int?>(null) }
                    var mSubCatId by remember { mutableStateOf<Int?>(null) }

                    val parentCats = categories.filter { it.parentId == null }
                    val childCats = categories.filter { it.parentId == mCatId }

                    Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (!canManageProviders) {
                            Text("لحسابات المشرفين: ليس لديك صلاحية تعديل أو إضافة المزودين مباشرة.", color = Color.Red, fontSize = 11.sp, fontFamily = defaultAppFont)
                        } else {
                            Text("إدخال فني وتفعيله مباشرة بمجوعة المعتمدين:", color = Color.White, fontSize = 12.sp, fontFamily = defaultAppFont)
                            OutlinedTextField(
                                value = mName, onValueChange = { mName = it },
                                placeholder = { Text("الاسم الكامل", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            )
                            OutlinedTextField(
                                value = mPhone, onValueChange = { mPhone = it },
                                placeholder = { Text("رقم الهاتف الفعال", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            )
                            OutlinedTextField(
                                value = mAddress, onValueChange = { mAddress = it },
                                placeholder = { Text("العنوان والورشة", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            )
                            OutlinedTextField(
                                value = mArea, onValueChange = { mArea = it },
                                placeholder = { Text("المنطقة السكنية", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            )

                            Text("١. اختر التبويب الرئيسي:", color = Color.White, fontSize = 11.sp, fontFamily = defaultAppFont)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(parentCats) { cat ->
                                    val isSel = mCatId == cat.id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) primaryAccentColor else Color.DarkGray)
                                            .clickable { 
                                                mCatId = cat.id 
                                                mSubCatId = null
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(cat.nameAr, color = if (isSel) Color.Black else Color.White, fontSize = 10.sp, fontFamily = defaultAppFont)
                                    }
                                }
                            }

                            if (mCatId != null) {
                                Text("٢. اختر التبويب الفرعي / التخصص:", color = Color.White, fontSize = 11.sp, fontFamily = defaultAppFont)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(childCats) { sub ->
                                        val isSel = mSubCatId == sub.id
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) primaryAccentColor else Color.DarkGray)
                                                .clickable { mSubCatId = sub.id }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(sub.nameAr, color = if (isSel) Color.Black else Color.White, fontSize = 10.sp, fontFamily = defaultAppFont)
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (mName.isBlank() || mPhone.isBlank() || mSubCatId == null || mAddress.isBlank() || mArea.isBlank()) {
                                        Toast.makeText(context, "الرجاء كتاية كامل المعلومات واختيار الشعبة الفرعية المحددة!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.addProviderDirectly(mName, mPhone, mSubCatId!!, mAddress, mArea, "https://photos.example.com/avatar.png")
                                        mName = ""
                                        mPhone = ""
                                        mAddress = ""
                                        mArea = ""
                                        mCatId = null
                                        mSubCatId = null
                                        Toast.makeText(context, "تمت إضافة مقدم الخدمة بقوة وسرعة مباشرة!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("إضافة مقدم خدمة مباشر ✔️", color = Color.Black, fontFamily = defaultAppFont)
                            }
                        }
                    }
                }
                6 -> {
                    // TAB 6: Add, update or delete supervisors and configure their granular permissions!
                    if (currentUserRole == "OWNER") {
                        var svName by remember { mutableStateOf("") }
                        var svUser by remember { mutableStateOf("") }
                        var svPass by remember { mutableStateOf("") }

                        var permAcceptReject by remember { mutableStateOf(true) }
                        var permManageCat by remember { mutableStateOf(false) }
                        var permManageProv by remember { mutableStateOf(false) }
                        var permViewRep by remember { mutableStateOf(true) }

                        // State for modifying passwords inline
                        var editingSv by remember { mutableStateOf<Supervisor?>(null) }
                        var editingSvPass by remember { mutableStateOf("") }

                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("إضافة مشرف للنظام وتحديد صلاحياته:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
                            
                            OutlinedTextField(
                                value = svName,
                                onValueChange = { svName = it },
                                placeholder = { Text("اسم المشرف الثلاثي الكامل (مثال: ماهر محمد)", color = Color.Gray) },
                                colors = customTextFieldColors,
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = svUser,
                                    onValueChange = { svUser = it },
                                    placeholder = { Text("اسم مستخدم الدخول (مثال: maher12)", color = Color.Gray) },
                                    colors = customTextFieldColors,
                                    modifier = Modifier.weight(1f).height(48.dp)
                                )
                                OutlinedTextField(
                                    value = svPass,
                                    onValueChange = { svPass = it },
                                    placeholder = { Text("كلمة مرور المشرف الآمنة", color = Color.Gray) },
                                    colors = customTextFieldColors,
                                    modifier = Modifier.weight(1f).height(48.dp)
                                )
                            }

                            Text("تخصيص الصلاحيات لهذا المشرف (تفصيلي):", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = permAcceptReject, onCheckedChange = { permAcceptReject = it }, colors = CheckboxDefaults.colors(checkedColor = primaryAccentColor))
                                Text("صلاحية قبول / رفض طلبات انضمام الفنيين", color = Color.White, fontSize = 11.sp, fontFamily = defaultAppFont)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = permManageCat, onCheckedChange = { permManageCat = it }, colors = CheckboxDefaults.colors(checkedColor = primaryAccentColor))
                                Text("صلاحية إدارة وإضافة وتعديل الأقسام والخدمات", color = Color.White, fontSize = 11.sp, fontFamily = defaultAppFont)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = permManageProv, onCheckedChange = { permManageProv = it }, colors = CheckboxDefaults.colors(checkedColor = primaryAccentColor))
                                Text("صلاحية التحكم الميداني بالمهنيين الفعالين المشتركين", color = Color.White, fontSize = 11.sp, fontFamily = defaultAppFont)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = permViewRep, onCheckedChange = { permViewRep = it }, colors = CheckboxDefaults.colors(checkedColor = primaryAccentColor))
                                Text("صلاحية قراءة البلاغات وفحص سجلات نشاط كونسول الكلي", color = Color.White, fontSize = 11.sp, fontFamily = defaultAppFont)
                            }

                            Button(
                                onClick = {
                                    if (svName.isBlank() || svUser.isBlank() || svPass.isBlank()) {
                                        Toast.makeText(context, "الرجاء كتابة اسم ورقم مستخدم وكلمة مرور المشرف!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.createSupervisor(svUser, svName, svPass, permAcceptReject, permManageCat, permManageProv, permViewRep)
                                        svName = ""
                                        svUser = ""
                                        svPass = ""
                                        Toast.makeText(context, "تم بنجاح تشغيل وتفعيل حساب المشرف المعتمد!", Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Text("توليد وتفعيل المشرف الآن ✔️", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
                            }

                            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 10.dp))

                            Text("المشرفين النشطين بالمنصة وصلاحياتهم:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
                            if (supervisors.isEmpty()) {
                                Text("لا يوجد أي مشرف مساعد مسجل حالياً.", color = Color.LightGray, fontSize = 11.sp, fontFamily = defaultAppFont)
                            } else {
                                supervisors.forEach { sv ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = normalSurfaceColor)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column {
                                                    Text(sv.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
                                                    Text("اسم المستخدم: ${sv.username} | الرمز المعتمد: ${sv.password}", color = primaryAccentColor, fontSize = 10.sp, fontFamily = defaultAppFont)
                                                }
                                                Row {
                                                    IconButton(onClick = { 
                                                        editingSv = sv 
                                                        editingSvPass = sv.password
                                                    }) {
                                                        Icon(Icons.Default.Edit, contentDescription = "تعديل الرمز", tint = primaryAccentColor)
                                                    }
                                                    IconButton(onClick = { 
                                                        viewModel.deleteSupervisor(sv)
                                                        Toast.makeText(context, "تم عزل وإلغاء صلاحية المشرف بنجاح!", Toast.LENGTH_SHORT).show()
                                                    }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "حذف المشرف", tint = Color.Red)
                                                    }
                                                }
                                            }

                                            // Permissions label display
                                            Text(
                                                text = "الصلاحيات الممنوحة: " +
                                                        (if (sv.canAcceptRejectRequests) "قبول/الرفض • " else "") +
                                                        (if (sv.canManageCategories) "التصنيفات • " else "") +
                                                        (if (sv.canManageProviders) "إدارةالمهندسين • " else "") +
                                                        (if (sv.canViewReports) "بلاغات" else ""),
                                                color = Color.LightGray,
                                                fontSize = 10.sp,
                                                fontFamily = defaultAppFont
                                            )
                                        }
                                    }
                                }
                            }

                            // Supervisor Edit Dialog
                            if (editingSv != null) {
                                androidx.compose.ui.window.Dialog(onDismissRequest = { editingSv = null }) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
                                        modifier = Modifier.padding(16.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text("تحديث كلمة مرور صلاحيات المشرف", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
                                            OutlinedTextField(
                                                value = editingSvPass,
                                                onValueChange = { editingSvPass = it },
                                                placeholder = { Text("رمز المرور الآمن الجديد") }
                                            )
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                                TextButton(onClick = { editingSv = null }) {
                                                    Text("إلغاء", color = Color.LightGray)
                                                }
                                                Button(
                                                    onClick = {
                                                        if (editingSvPass.isNotBlank()) {
                                                            viewModel.updateSupervisor(editingSv!!.copy(password = editingSvPass))
                                                            editingSv = null
                                                            Toast.makeText(context, "تم حفظ كلمة السر الجديدة للمشرف بنجاح!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor)
                                                ) {
                                                    Text("حفظ", color = Color.Black)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 10.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = normalSurfaceColor.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, primaryAccentColor.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("🔄 مزامنة المشرفين المشتركة بين كل الأجهزة:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
                                    Text("تتيح لك هذه الميزة تصدير أو استيراد المشرفين وصلاحياتهم بضغطة زر لمزامنتها مع بقية هواتف الإدارة والمشرفين فوراً بشكل مشفر.", color = Color.LightGray, fontSize = 10.sp, fontFamily = defaultAppFont)
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        Button(
                                            onClick = {
                                                val syncCode = viewModel.exportSupervisorsToCode()
                                                if (syncCode.isNotBlank()) {
                                                    val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                    val clipData = android.content.ClipData.newPlainText("SupervisorsSyncCode", syncCode)
                                                    clipboardManager.setPrimaryClip(clipData)
                                                    Toast.makeText(context, "📋 تم توليد ونسخ كود المزامنة المشفر للحافظة! شاركه مع بقية الأجهزة الآن.", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "لا يوجد مشرفين حالياً لتصديرهم!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("تصدير كود المزامنة 📤", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
                                        }
                                    }

                                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))
                                    Text("📥 استيراد ومزامنة كود المشرفين من جهاز آخر:", color = Color.LightGray, fontSize = 10.sp, fontFamily = defaultAppFont)
                                    
                                    var pastedSyncCode by remember { mutableStateOf("") }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = pastedSyncCode,
                                            onValueChange = { pastedSyncCode = it },
                                            placeholder = { Text("ضع كود المزامنة الذي قمت بنسخه هنا...", color = Color.Gray, fontSize = 9.sp) },
                                            colors = customTextFieldColors,
                                            modifier = Modifier.weight(1f).height(44.dp)
                                        )
                                        Button(
                                            onClick = {
                                                if (pastedSyncCode.isBlank()) {
                                                    Toast.makeText(context, "الرجاء لصق كود المزامنة أولاً!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    val success = viewModel.importSupervisorsFromCode(pastedSyncCode)
                                                    if (success) {
                                                        Toast.makeText(context, "✔️ تمت مزامنة واستيراد المشرفين على هذا الجهاز بنجاح فوري!", Toast.LENGTH_LONG).show()
                                                        pastedSyncCode = ""
                                                    } else {
                                                        Toast.makeText(context, "❌ كود المزامنة الملصق غير صالح أو تالف! يرجى التأكد وإعادة المحاولة.", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor)
                                        ) {
                                            Text("مزامنة 🔄", color = Color.Black, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                7 -> {
                    // TAB 7: Custom Welcome settings control (image selection, alignments and font size editing)
                    if (currentUserRole == "OWNER") {
                        var welcomeText by remember { mutableStateOf(config.welcomeMessage) }
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("التحكم بشكل رسالة / غلاف الترحيب بالرئيسية:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
                            
                            Text("١. الاختيار من صور الهاتف لغلاف ترحيب مخصص بدلاً من النص:", color = Color.LightGray, fontSize = 11.sp, fontFamily = defaultAppFont)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        // Gallery Picker Simulation with custom beautiful illustrations!
                                        viewModel.updateWelcomeImage("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=600&auto=format&fit=crop")
                                        Toast.makeText(context, "تم بنجاح اختيار ورفع صورة الغلاف الترحيبي من الذاكرة!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("معرض صور الهاتف 🖼️", fontSize = 10.sp, color = Color.White)
                                }
                                Button(
                                    onClick = {
                                        // Camera capture banner mockup
                                        viewModel.updateWelcomeImage("https://images.unsplash.com/photo-1634017839464-5c339ebe3cb4?q=80&w=600&auto=format&fit=crop")
                                        Toast.makeText(context, "تم التقاط الغلاف الترحيبي مباشرة عبر كاميرا الهاتف 📸", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("التقاط صورة كاميرا 📸", fontSize = 10.sp, color = Color.White)
                                }
                            }

                            if (config.welcomeImageUri.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().background(Color.Yellow.copy(0.12f), RoundedCornerShape(8.dp)).padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("صورة الغلاف مفعّلة حالياً بالرئيسية!", color = Color.Yellow, fontSize = 10.sp, fontFamily = defaultAppFont)
                                    Button(
                                        onClick = { 
                                            viewModel.clearWelcomeImage()
                                            Toast.makeText(context, "تم حذف الصورة والعودة للرسالة النصية!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Text("إزالة الصورة ✖️", fontSize = 8.sp, color = Color.White)
                                    }
                                }
                            }

                            Divider(color = Color.DarkGray)

                            Text("٢. تعديل نص رسالة الترحيب اليدوية (في حال عدم وجود صورة مصاحبة):", color = Color.LightGray, fontSize = 11.sp, fontFamily = defaultAppFont)
                            OutlinedTextField(
                                value = welcomeText,
                                onValueChange = { welcomeText = it },
                                colors = customTextFieldColors,
                                modifier = Modifier.fillMaxWidth().height(80.dp)
                            )
                            Button(
                                onClick = {
                                    viewModel.updateWelcomeMessage(welcomeText)
                                    Toast.makeText(context, "تم تعديل نص المانشيت الترحيبي!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("حفظ نص الترحيب اليدوي", color = Color.Black, fontSize = 11.sp, fontFamily = defaultAppFont)
                            }

                            Divider(color = Color.DarkGray)

                            Text("٣. تحديد حجم خط الرسالة بالرئيسية:", color = Color.LightGray, fontSize = 11.sp, fontFamily = defaultAppFont)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                listOf(12, 14, 16, 18, 22).forEach { size ->
                                    val isSel = config.welcomeMessageFontSize == size
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) primaryAccentColor else Color.DarkGray)
                                            .clickable { viewModel.updateWelcomeFontSize(size) }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${size}sp", fontSize = 10.sp, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Divider(color = Color.DarkGray)

                            Text("٤. تحديد مكان وموضع محاذاة الرسالة بالرئيسية:", color = Color.LightGray, fontSize = 11.sp, fontFamily = defaultAppFont)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                mapOf("START" to "يمين / يسار حسب اللغة", "CENTER" to "في وسط كارد الترحيب", "END" to "في نهاية كارت الترحيب").forEach { (alignmentId, name) ->
                                    val isSel = config.welcomeMessageAlignment == alignmentId
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) primaryAccentColor else Color.DarkGray)
                                            .clickable { viewModel.updateWelcomeAlignment(alignmentId) }
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(name, fontSize = 9.sp, color = if (isSel) Color.Black else Color.White, textAlign = TextAlign.Center, fontFamily = defaultAppFont)
                                    }
                                }
                            }

                            Divider(color = Color.DarkGray)

                            Text("٥. تعديل خلفية حقول الكتابة والنص لتلافي مشاكل العرض:", color = Color.LightGray, fontSize = 11.sp, fontFamily = defaultAppFont)
                            var inputFieldHexInput by remember { mutableStateOf(config.inputFieldBgColor) }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = inputFieldHexInput,
                                    onValueChange = { inputFieldHexInput = it },
                                    colors = customTextFieldColors,
                                    placeholder = { Text("كود اللون بالهكس (مثال: #1E293B)", color = Color.Gray, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        if (inputFieldHexInput.isNotBlank()) {
                                            viewModel.updateInputFieldBgColor(inputFieldHexInput)
                                            Toast.makeText(context, "تم حفظ وتطبيق خلفية حقول الكتابة الجديدة في المنصة!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor)
                                ) {
                                    Text("تطبيق 🎨", color = Color.Black, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("٦. رابط / مسار صورة الغلاف لغلاف ترحيب مخصص بدلاً من النص:", color = Color.LightGray, fontSize = 11.sp, fontFamily = defaultAppFont)
                            var welcomeImgUrlInput by remember { mutableStateOf(config.welcomeImageUri) }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = welcomeImgUrlInput,
                                    onValueChange = { welcomeImgUrlInput = it },
                                    colors = customTextFieldColors,
                                    placeholder = { Text("مثال: https://images.unsplash.com/photo-...", color = Color.Gray, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        if (welcomeImgUrlInput.isNotBlank()) {
                                            viewModel.updateWelcomeImage(welcomeImgUrlInput)
                                            Toast.makeText(context, "تم حفظ وتحديث صورة الترحيب المخصصة بدلاً من الرسالة النصية!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor)
                                ) {
                                    Text("تطبيق 🖼️", color = Color.Black, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// VIEW: Secret Owner Portal Screen (Hidden Settings)
// ------------------------------------------------------------------
@Composable
fun HiddenSettingsView(
    viewModel: AllServicesViewModel,
    isArabic: Boolean,
    primaryAccentColor: Color,
    primaryBrush: Brush,
    normalSurfaceColor: Color,
    defaultAppFont: FontFamily,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val config by viewModel.appConfig.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val serviceProviders by viewModel.serviceProviders.collectAsStateWithLifecycle()

    var tempAppName by remember { mutableStateOf(config.appName) }
    var tempPhone by remember { mutableStateOf(config.supportPhone) }
    var tempEmail by remember { mutableStateOf(config.supportEmail) }
    var tempWhatsapp by remember { mutableStateOf(config.supportWhatsApp) }
    var tempFooter by remember { mutableStateOf(config.promotionalFooter) }
    var tempWelcome by remember { mutableStateOf(config.welcomeMessage) }
    var tempPass by remember { mutableStateOf(config.adminPassword) }
    var tempCustomPrimary by remember { mutableStateOf(config.customPrimaryColor) }
    var tempCustomSecondary by remember { mutableStateOf(config.customSecondaryColor) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "البوابة الخلفية السرية للمالك",
                    color = primaryAccentColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = defaultAppFont
                )
                Text("شروط وخصوصية تامة - لا يرى أحد هذه الإعدادات السرية", color = Color.Gray, fontSize = 11.sp, fontFamily = defaultAppFont)
            }
            IconButton(
                onClick = { onExit() },
                modifier = Modifier.clip(CircleShape).background(Color.DarkGray).size(36.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Exit", tint = Color.White)
            }
        }

        Divider(color = Color.Gray)

        // General settings modifiers
        OutlinedTextField(
            value = tempAppName,
            onValueChange = { tempAppName = it },
            label = { Text("تعديل اسم التطبيق الفعلي", color = Color.LightGray) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = primaryAccentColor),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = tempWelcome,
            onValueChange = { tempWelcome = it },
            label = { Text("تخصيص رسالة الترحيب الأم", color = Color.LightGray) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = primaryAccentColor),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = tempFooter,
            onValueChange = { tempFooter = it },
            label = { Text("التذييل الدعائي المتنقل (MAW 777644670)", color = Color.LightGray) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = primaryAccentColor),
            modifier = Modifier.fillMaxWidth()
        )

        Text("سجل دعم الزبائن والتطوير المباشر:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = tempPhone, onValueChange = { tempPhone = it },
                label = { Text("رقم الاتصال", color = Color.LightGray) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = primaryAccentColor),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = tempWhatsapp, onValueChange = { tempWhatsapp = it },
                label = { Text("رقم الواتساب", color = Color.LightGray) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = primaryAccentColor),
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = tempEmail,
            onValueChange = { tempEmail = it },
            label = { Text("الإيميل وقنوات المراسلة", color = Color.LightGray) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = primaryAccentColor),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = tempPass,
            onValueChange = { tempPass = it },
            label = { Text("تعديل كلمة مرور المشرف (WAM2026)", color = Color.LightGray) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = primaryAccentColor),
            modifier = Modifier.fillMaxWidth()
        )

        // Custom Visual theme choice interface
        Text("تغيير الألوان والهوية البصرية للتطبيق ديناميكياً:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val themes = listOf(
                "EMERALD" to "🟢 الزمردي",
                "GOLDEN" to "✨ الذهبي",
                "COSMIC_SILVER" to "🌌 الفضي",
                "BLACK" to "🖤 الأسود",
                "GREEN" to "💚 الأخضر",
                "CUSTOM" to "🎨 مخصصة"
            )
            themes.forEach { (key, display) ->
                val isSel = config.primaryTheme == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) primaryAccentColor else Color.DarkGray)
                        .clickable { viewModel.updateAppColors(key) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        display,
                        color = if (isSel) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Custom theme settings
        if (config.primaryTheme == "CUSTOM") {
            Card(
                colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("تعريف تدرج واختيار الألوان المخصصة (Hex) للكود:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tempCustomPrimary,
                            onValueChange = { tempCustomPrimary = it },
                            label = { Text("لون الهوية/المساعد (مثلاً #FF5722)", color = Color.LightGray, fontSize = 9.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = primaryAccentColor),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = tempCustomSecondary,
                            onValueChange = { tempCustomSecondary = it },
                            label = { Text("لون الخلفيات (مثلاً #1A1A1A)", color = Color.LightGray, fontSize = 9.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = primaryAccentColor),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (tempCustomPrimary.isNotBlank() && tempCustomSecondary.isNotBlank()) {
                                viewModel.updateCustomColors(tempCustomPrimary, tempCustomSecondary)
                                Toast.makeText(context, "تم تطبيق الهوية البصرية المخصصة للمستخدم بنجاح!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "الرجاء تعبئة الأكواد (#HEX) بشكل كامل أولاً!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("حفظ وتنشيط الهوية المخصصة 🎨", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Maintenance Mode Toggle Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(normalSurfaceColor, RoundedCornerShape(10.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("وضعية الصيانة الشاملة للتطبيق", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("تمنع رفع طلبات جديدة وتعرض ترحيب صيانة فقط", color = Color.Gray, fontSize = 10.sp)
            }
            Switch(
                checked = config.isMaintenanceMode,
                onCheckedChange = { viewModel.toggleMaintenanceMode(it, config.maintenanceMessage) },
                colors = SwitchDefaults.colors(checkedThumbColor = primaryAccentColor)
            )
        }

        // Data saving mode toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(normalSurfaceColor, RoundedCornerShape(10.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("وضع توفير باقة الإنترنت الفعال", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("يخفض جودة اللودر ويوقف الكاش الثقيل", color = Color.Gray, fontSize = 10.sp)
            }
            Switch(
                checked = config.dataSavingMode,
                onCheckedChange = { viewModel.toggleDataSavingMode(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = primaryAccentColor)
            )
        }

        // Action controls for Whitelist or FCM config channels
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("قنوات الإشعارات الفورية (FCM Channels):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                val channelsStr = config.fcmChannelsEnabled
                val reqEnabled = channelsStr.contains("JOIN_REQUESTS:true")
                val repEnabled = channelsStr.contains("REPORTS:true")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("إشعارات طلبات الانضمام الجديدة للأدمن", color = Color.LightGray, fontSize = 11.sp)
                    Switch(
                        checked = reqEnabled,
                        onCheckedChange = { viewModel.toggleFCMChannel("JOIN_REQUESTS", it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = primaryAccentColor)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("إشعارات شكاوى وبلاغات الزوار الفورية", color = Color.LightGray, fontSize = 11.sp)
                    Switch(
                        checked = repEnabled,
                        onCheckedChange = { viewModel.toggleFCMChannel("REPORTS", it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = primaryAccentColor)
                    )
                }
            }
        }

        // Database backups management panel as requested
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("إدارة النسخ الاحتياطي واستعادة البيانات:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("احمِ بيانات مقدمي الخدمات والأقسام واسترجعها بـ 0 ثانية.", color = Color.Gray, fontSize = 10.sp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.performManualBackup("ذاكرة الهاتف الكلية HD") },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("نسخة احتياطية 💾", color = Color.Black, fontSize = 10.sp)
                    }
                    Button(
                        onClick = { viewModel.restoreManualBackup() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("استيراد واستعادة 🔄", color = Color.White, fontSize = 10.sp)
                    }
                }

                Button(
                    onClick = { viewModel.purgeTemporaryLogs() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تفريغ الذاكرة المؤقتة وسجلات الرسائل القديمة 🗑️", color = Color.White, fontSize = 10.sp)
                }
            }
        }

        // Save modification changes button
        Button(
            onClick = {
                viewModel.updateAppName(tempAppName)
                viewModel.updateAppSupportInfo(tempPhone, tempEmail, tempWhatsapp)
                viewModel.updatePromotionalFooter(tempFooter)
                viewModel.updateWelcomeMessage(tempWelcome)
                viewModel.updateAdminPassword(tempPass)
                Toast.makeText(context, "تم حفظ الإعدادات السرية بنجاح على قاعدة البيانات!", Toast.LENGTH_SHORT).show()
                onExit()
            },
            colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_secret_settings")
        ) {
            Text("حفظ كل التغييرات والمزامنة الفورية", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

// ------------------------------------------------------------------
// VIEW: About Application Screen (ℹ️)
// ------------------------------------------------------------------
@Composable
fun AboutAppView(
    viewModel: AllServicesViewModel,
    isArabic: Boolean,
    primaryAccentColor: Color,
    normalSurfaceColor: Color,
    defaultAppFont: FontFamily
) {
    val context = LocalContext.current
    val config by viewModel.appConfig.collectAsStateWithLifecycle()
    val providers by viewModel.serviceProviders.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_app_logo),
            contentDescription = "Logo Logo",
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(20.dp))
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = config.appName,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = defaultAppFont
        )
        Text(
            text = "كل خدمات الصيانة والمهن بين يديك بلمسة واحدة",
            color = Color.LightGray,
            fontSize = 11.sp,
            fontFamily = defaultAppFont,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("قنوات الدعم والتواصل الفني للتطبيق:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = "phone", tint = primaryAccentColor)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("رقم الاتصال المباشر: ${config.supportPhone}", color = Color.White, fontSize = 12.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mail, contentDescription = "email", tint = primaryAccentColor)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("البريد الإلكتروني للشكاوى: ${config.supportEmail}", color = Color.White, fontSize = 12.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Chat, contentDescription = "whatsapp", tint = primaryAccentColor)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("خط الدعم واتساب: ${config.supportWhatsApp}", color = Color.White, fontSize = 12.sp)
                }

                Divider(color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))

                Text("إحصائيات وقواعد العمل الحالية:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("إصدار التطبيق الفضائي: V3.2.1-Gold", color = Color.LightGray, fontSize = 11.sp)
                Text("قاعدة البيانات المحلية: SQLite Offline Engine Enabled", color = Color.LightGray, fontSize = 11.sp)
                Text("عدد الفنيين المعتمدين والمزامنين حالياً: ${providers.size} مهني نشط", color = Color.LightGray, fontSize = 11.sp)
            }
        }
    }
}

// ------------------------------------------------------------------
// VIEW: Detailed Service Provider Screen (with ratings, points, report, chat)
// ------------------------------------------------------------------
@Composable
fun ProviderDetailView(
    provider: ServiceProvider,
    viewModel: AllServicesViewModel,
    isArabic: Boolean,
    primaryAccentColor: Color,
    normalSurfaceColor: Color,
    defaultAppFont: FontFamily,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val mainCategoryId = provider.mainCategoryId
    val categoryName = categories.find { it.id == mainCategoryId }?.nameAr ?: "خدمات عامة"

    var starsChoice by remember { mutableStateOf(5.0f) }
    var reviewTextInput by remember { mutableStateOf("") }

    var showMonthlySubscriptionDialog by remember { mutableStateOf(false) }
    var paymentReceiptInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Back toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { onBack() }, modifier = Modifier.clip(CircleShape).background(normalSurfaceColor)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(provider.fullName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = defaultAppFont)
            IconButton(
                onClick = {
                    viewModel.shareApp()
                    Toast.makeText(context, "تم نسخ رابط الملف الفني ورابط تحميل التطبيق بنجاح لمشاركته!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.clip(CircleShape).background(normalSurfaceColor)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
            }
        }

        // Banner Detail info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_logo),
                    contentDescription = "Portrait Avatar",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .border(3.dp, primaryAccentColor, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(provider.fullName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (provider.isVerified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Verified, contentDescription = "Verified Profile Badge", tint = Color(0xFF3B82F6))
                    }
                }
                Text(categoryName, color = primaryAccentColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Stars", tint = Color(0xFFFBBF24))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${String.format(Locale.US, "%.1f", provider.averageRating)} (${provider.ratingCount} تقييم للشهر الحالي)", color = Color.White, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.logContactToProvider(provider, categoryName)
                            Toast.makeText(context, "جاري طلب الاتصال المباشر على الهاتف: ${provider.phone}", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                        modifier = Modifier.weight(1f).testTag("call_provider_btn")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اتصل الآن", color = Color.Black)
                    }

                    // Button for Monthly Subscription for Providers
                    if (provider.phone == "777644670") {
                        Button(
                            onClick = { showMonthlySubscriptionDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("التاج الذهبي 👑", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Work address details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("تفاصيل السكن والعمل:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("المنطقة السكنية: ${provider.area}", color = Color.LightGray, fontSize = 12.sp)
                Text("الموقع والورشة الحالي: ${provider.address}", color = Color.LightGray, fontSize = 12.sp)
                if (provider.gpsCoordinates.isNotBlank()) {
                    Text("إحداثيات GPS المباشرة: ${provider.gpsCoordinates}", color = primaryAccentColor, fontSize = 11.sp)
                }
            }
        }

        // Active rating and comments form card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("اكتب تقييم لمقدم الخدمة:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val scores = listOf(1, 2, 3, 4, 5)
                    scores.forEach { score ->
                        val isPicked = starsChoice >= score
                        IconButton(onClick = { starsChoice = score.toFloat() }) {
                            Icon(
                                if (isPicked) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$score Stars",
                                tint = Color(0xFFFBBF24)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = reviewTextInput,
                    onValueChange = { reviewTextInput = it },
                    placeholder = { Text("اكتب مراجعتك عن الأمانة والسرعة والجودة هنا...", color = Color.LightGray, fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = primaryAccentColor),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (reviewTextInput.isNotBlank()) {
                            viewModel.addRatingAndReview(provider.id, starsChoice, reviewTextInput)
                            reviewTextInput = ""
                            Toast.makeText(context, "شكراً لك! تم إرسال مراجعتك وحفظ نجوم التقييم بنجاح.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إرسال التقييم والمراجعة الفورية", color = Color.Black, fontSize = 12.sp)
                }
            }
        }

        // Report Provider Button (البلاغات ومكافحة المحتوى)
        Button(
            onClick = {
                viewModel.submitReport(
                    provider.id,
                    provider.fullName,
                    "مقدم الخدمة طلب رسوماً إضافية غير المتفق عليها وبمستوى صيانة منخفض."
                )
                Toast.makeText(context, "تم تسجيل الشكوى والبلاغ فورياً، سيراجعها المشرف العام WAM2026!", Toast.LENGTH_LONG).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Report, contentDescription = "Report", tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("الإبلاغ عن المقدّم المخالف 🚨", color = Color.White, fontSize = 11.sp)
        }

        // Monthly Gold Premium subscription application modal popup
        if (showMonthlySubscriptionDialog) {
            Dialog(onDismissRequest = { showMonthlySubscriptionDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = normalSurfaceColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("الاشتراك الشهري الذهبي 👑", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "تمنحك الباقة شارة مميزة وتظهر بياناتك بشكل دائم في أعلى نتائج البحث لتلقي آلاف الاتصالات!",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        OutlinedTextField(
                            value = paymentReceiptInput,
                            onValueChange = { paymentReceiptInput = it },
                            label = { Text("أدخل رقم حوالة الكاش أو تذكرة الاشتراك", color = Color.LightGray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFFFD700)),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { showMonthlySubscriptionDialog = false }) {
                                Text("إلغاء", color = Color.Gray)
                            }
                            Button(
                                onClick = {
                                    if (paymentReceiptInput.isNotBlank()) {
                                        viewModel.submitSubscriptionRequest(provider.id, paymentReceiptInput)
                                        showMonthlySubscriptionDialog = false
                                        paymentReceiptInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                            ) {
                                Text("تأكيد الدفع والتحقق", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// VIEW: Previous Services History logs tracking
// ------------------------------------------------------------------
@Composable
fun UserHistoryView(
    viewModel: AllServicesViewModel,
    isArabic: Boolean,
    primaryAccentColor: Color,
    normalSurfaceColor: Color,
    defaultAppFont: FontFamily,
    onBack: () -> Unit
) {
    val previousRequests by viewModel.previousRequests.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBack() }, modifier = Modifier.clip(CircleShape).background(normalSurfaceColor)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("سجل تواصلك وطلبات الخدمة السابقة", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (previousRequests.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("لا يوجد سجل تواصل مسبق حتى الآن. اتصل بالفنيين من لوحة الاختيار لتتبعهم هنا!", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
            }
            items(previousRequests) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = normalSurfaceColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(req.providerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF065F46))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (req.status == "CONTACTED") "تم الاتصال بنجاح" else "مكتملة",
                                    color = Color.White,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Text("الخدمة والمهنة: ${req.categoryName}", color = primaryAccentColor, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                        Text("رقم التواصل: ${req.providerPhone}", color = Color.LightGray, fontSize = 11.sp)
                        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(req.timestamp))
                        Text("التاريخ والموقت: $dateStr", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}
