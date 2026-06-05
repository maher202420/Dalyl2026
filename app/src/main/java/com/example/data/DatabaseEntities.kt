package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 1,
    val appName: String = "كل الخدمات بين يديك",
    val primaryTheme: String = "EMERALD", // COSMIC_SILVER, GOLDEN, EMERALD, CUSTOM
    val customPrimaryColor: String = "",
    val customSecondaryColor: String = "",
    val customFontType: String = "DEFAULT",
    val customFontColor: String = "#FFFFFF",
    val launcherIcon: String = "default",
    val promotionalFooter: String = "MAW 777644670",
    val welcomeMessage: String = "مرحباً بك في تطبيق كل الخدمات. متاحون لخدمتك على مدار الساعة!",
    val welcomeImageUri: String = "",
    val welcomeMessageFontSize: Int = 12,
    val welcomeMessageAlignment: String = "START", // START, CENTER, END
    val supportPhone: String = "777644670",
    val supportEmail: String = "support@servicesmaw.com",
    val supportWhatsApp: String = "777644670",
    val adminPassword: String = "maher736462",
    val isMaintenanceMode: Boolean = false,
    val maintenanceMessage: String = "التطبيق في وضع الصيانة حالياً للتحديث لتوفير أفضل تجربة لكم.",
    val isTwoFactorEnabled: Boolean = false,
    val maxRadiusSearch: Int = 10,
    val showFooter: Boolean = true,
    val assistantVisible: Boolean = true,
    val assistantIcon: String = "🤖",
    val assistantSize: Int = 48,
    val assistantX: Float = 0f,
    val assistantY: Float = 0f,
    val chatVisible: Boolean = true,
    val chatSize: Int = 48,
    val dataSavingMode: Boolean = false,
    val topBarIconsOrder: String = "HOME,LOGIN,REGISTER,LANG,REFRESH", // Order in CSV
    val fcmChannelsEnabled: String = "JOIN_REQUESTS:true,REPORTS:true,SUBSCRIPTIONS:true",
    val inputFieldBgColor: String = "#1E293B"
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameAr: String,
    val nameEn: String,
    val parentId: Int? = null, // null means Main category, non-null means subcategory
    val imageUrl: String = "",
    val sortOrder: Int = 0
)

@Entity(tableName = "service_providers")
data class ServiceProvider(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val phone: String,
    val mainCategoryId: Int, // Refers to Category.id
    val area: String,
    val address: String,
    val gpsCoordinates: String = "",
    val profilePhotoUrl: String = "",
    val idPhotoUrl: String = "",
    val isPinned: Boolean = false,
    val isRecommended: Boolean = false,
    val isVerified: Boolean = false,
    val isBlocked: Boolean = false,
    val averageRating: Float = 5.0f,
    val ratingCount: Int = 0,
    val points: Int = 0,
    val isSubscribed: Boolean = false, // Monthly subscription status
    val subscriptionStatus: String = "NONE", // NONE, PENDING, APPROVED, REJECTED
    val subscriptionPaymentDetails: String = "",
    val hasAdBanner: Boolean = false,
    val bannerImageUrl: String = "",
    val bannerLink: String = "",
    val bannerDurationDays: Int = 0,
    val bannerStartDate: Long = 0L,
    val registrationDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "pending_providers")
data class PendingProvider(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val phone: String,
    val mainCategoryId: Int,
    val area: String,
    val address: String,
    val gpsCoordinates: String = "",
    val profilePhotoUrl: String = "",
    val idPhotoUrl: String = "",
    val dateSubmitted: Long = System.currentTimeMillis(),
    val rejectionReason: String = ""
)

@Entity(tableName = "banner_ads")
data class BannerAd(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val imageUrl: String = "",
    val linkUrl: String = "",
    val type: String = "IMAGE", // IMAGE, VIDEO, TEXT
    val size: String = "MEDIUM", // SMALL, MEDIUM, LARGE
    val durationSeconds: Int = 5,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + 86400000 * 7, // Default 1 week
    val isVisible: Boolean = true
)

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reporterName: String = "زائر",
    val providerId: Int,
    val providerName: String,
    val reportReason: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val actor: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val conversationId: String, // e.g. "user_provider_5" or "visitor_admin"
    val senderName: String,
    val senderRole: String, // "USER", "PROVIDER", "ADMIN"
    val messageText: String,
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "device_whitelist")
data class DeviceWhitelist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deviceId: String,
    val deviceName: String,
    val isApproved: Boolean = true
)

@Entity(tableName = "cities")
data class City(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameAr: String,
    val nameEn: String
)

@Entity(tableName = "previous_service_requests")
data class PreviousServiceRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val providerId: Int,
    val providerName: String,
    val providerPhone: String,
    val categoryName: String,
    val status: String = "CONTACTED", // CONTACTED, COMPLETED, CANCELLED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "supervisors")
data class Supervisor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val name: String,
    val password: String,
    val canAcceptRejectRequests: Boolean = true,
    val canManageCategories: Boolean = false,
    val canManageProviders: Boolean = false,
    val canViewReports: Boolean = true
)

