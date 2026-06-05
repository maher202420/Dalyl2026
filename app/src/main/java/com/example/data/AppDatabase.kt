package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface AppDao {
    // --- App Config / Settings ---
    @Query("SELECT * FROM app_config WHERE id = 1")
    fun getAppConfigFlow(): Flow<AppConfig?>

    @Query("SELECT * FROM app_config WHERE id = 1")
    suspend fun getAppConfig(): AppConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: AppConfig)

    // --- Categories ---
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun getAllCategoriesFlow(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE parentId IS NULL ORDER BY sortOrder ASC")
    fun getMainCategoriesFlow(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY sortOrder ASC")
    fun getSubcategoriesFlow(parentId: Int): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("DELETE FROM categories WHERE parentId = :parentId")
    suspend fun deleteSubcategoriesByParent(parentId: Int)

    // --- Service Providers ---
    @Query("SELECT * FROM service_providers WHERE isBlocked = 0 ORDER BY isPinned DESC, averageRating DESC, id DESC")
    fun getAllServiceProvidersFlow(): Flow<List<ServiceProvider>>

    @Query("SELECT * FROM service_providers WHERE isBlocked = 0 AND mainCategoryId = :categoryId ORDER BY isPinned DESC, averageRating DESC, id DESC")
    fun getProvidersByCategoryFlow(categoryId: Int): Flow<List<ServiceProvider>>

    @Query("SELECT * FROM service_providers WHERE isRecommended = 1 AND isBlocked = 0 ORDER BY averageRating DESC")
    fun getRecommendedProvidersFlow(): Flow<List<ServiceProvider>>

    @Query("SELECT * FROM service_providers WHERE id = :id")
    suspend fun getProviderById(id: Int): ServiceProvider?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ServiceProvider)

    @Update
    suspend fun updateProvider(provider: ServiceProvider)

    @Delete
    suspend fun deleteProvider(provider: ServiceProvider)

    @Query("SELECT * FROM service_providers WHERE isBlocked = 1")
    fun getBlockedProvidersFlow(): Flow<List<ServiceProvider>>

    // --- Pending Providers ---
    @Query("SELECT * FROM pending_providers ORDER BY dateSubmitted DESC")
    fun getAllPendingProvidersFlow(): Flow<List<PendingProvider>>

    @Query("SELECT * FROM pending_providers WHERE id = :id")
    suspend fun getPendingProviderById(id: Int): PendingProvider?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingProvider(pending: PendingProvider)

    @Delete
    suspend fun deletePendingProvider(pending: PendingProvider)

    // --- Banners ---
    @Query("SELECT * FROM banner_ads WHERE isVisible = 1 ORDER BY startDate DESC")
    fun getActiveBannersFlow(): Flow<List<BannerAd>>

    @Query("SELECT * FROM banner_ads ORDER BY startDate DESC")
    fun getAllBannersFlow(): Flow<List<BannerAd>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: BannerAd)

    @Update
    suspend fun updateBanner(banner: BannerAd)

    @Delete
    suspend fun deleteBanner(banner: BannerAd)

    // --- Reports ---
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReportsFlow(): Flow<List<Report>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReportById(id: Int)

    @Query("DELETE FROM reports")
    suspend fun clearAllReports()

    // --- Activity Logs ---
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getActivityLogsFlow(): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLog)

    @Query("DELETE FROM activity_logs")
    suspend fun clearOldActivityLogs()

    // --- Messages ---
    @Query("SELECT * FROM messages WHERE conversationId = :convoId ORDER BY timestamp ASC")
    fun getMessagesByConversationFlow(convoId: String): Flow<List<Message>>

    @Query("SELECT DISTINCT conversationId FROM messages ORDER BY timestamp DESC")
    fun getAllConversationsFlow(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("DELETE FROM messages")
    suspend fun clearOldMessages()

    // --- Device Whitelist ---
    @Query("SELECT * FROM device_whitelist")
    fun getDeviceWhitelistFlow(): Flow<List<DeviceWhitelist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceWhitelist)

    @Delete
    suspend fun deleteDevice(device: DeviceWhitelist)

    // --- Cities / Regions ---
    @Query("SELECT * FROM cities ORDER BY nameAr ASC")
    fun getAllCitiesFlow(): Flow<List<City>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: City)

    @Delete
    suspend fun deleteCity(city: City)

    // --- Previous Service Requests ---
    @Query("SELECT * FROM previous_service_requests ORDER BY timestamp DESC")
    fun getPreviousRequestsFlow(): Flow<List<PreviousServiceRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreviousRequest(request: PreviousServiceRequest)

    // --- Supervisors ---
    @Query("SELECT * FROM supervisors ORDER BY id DESC")
    fun getAllSupervisorsFlow(): Flow<List<Supervisor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupervisor(supervisor: Supervisor)

    @Update
    suspend fun updateSupervisor(supervisor: Supervisor)

    @Delete
    suspend fun deleteSupervisor(supervisor: Supervisor)
}

@Database(
    entities = [
        AppConfig::class,
        Category::class,
        ServiceProvider::class,
        PendingProvider::class,
        BannerAd::class,
        Report::class,
        ActivityLog::class,
        Message::class,
        DeviceWhitelist::class,
        City::class,
        PreviousServiceRequest::class,
        Supervisor::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "all_services_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.dao())
                }
            }
        }

        suspend fun populateDatabase(dao: AppDao) {
            // Seed default application settings
            dao.insertOrUpdateConfig(AppConfig())

            // Seed some beautiful categories (Main and Sub)
            val plumbingId = 1
            val electId = 2
            val paintId = 3
            val cleanId = 4
            val acId = 5
            val techId = 6

            dao.insertCategory(Category(id = plumbingId, nameAr = "سباكة وصحي", nameEn = "Plumbing", sortOrder = 1))
            dao.insertCategory(Category(id = electId, nameAr = "كهرباء وإنارة", nameEn = "Electricity", sortOrder = 2))
            dao.insertCategory(Category(id = paintId, nameAr = "طلاء ودهانات", nameEn = "Painting", sortOrder = 3))
            dao.insertCategory(Category(id = cleanId, nameAr = "تنظيف منازل", nameEn = "Home Cleaning", sortOrder = 4))
            dao.insertCategory(Category(id = acId, nameAr = "صيانة تكييف تبريد", nameEn = "AC Maintenance", sortOrder = 5))
            dao.insertCategory(Category(id = techId, nameAr = "برمجة وبستنة", nameEn = "Tech & Gardening", sortOrder = 6))

            // Subcategories for Plumbing
            dao.insertCategory(Category(nameAr = "تركيب مغاسل وخلاطات", nameEn = "Sink & Faucet Fitting", parentId = plumbingId, sortOrder = 10))
            dao.insertCategory(Category(nameAr = "تسليك مجاري شبكات", nameEn = "Drainage Clearance", parentId = plumbingId, sortOrder = 11))
            dao.insertCategory(Category(nameAr = "صيانة الخزانات والمضخات", nameEn = "Tanks & Pumps Maintenance", parentId = plumbingId, sortOrder = 12))

            // Subcategories for Electricity
            dao.insertCategory(Category(nameAr = "تركيب نجف وثريات", nameEn = "Chandelier Setup", parentId = electId, sortOrder = 20))
            dao.insertCategory(Category(nameAr = "صيانة لوحات الكهرباء والعدادات", nameEn = "Breaker Panel & Meter Repair", parentId = electId, sortOrder = 21))
            dao.insertCategory(Category(nameAr = "تمديد أسلاك وشبكات تيار", nameEn = "Wiring Extensions", parentId = electId, sortOrder = 22))

            // Subcategories for Painting
            dao.insertCategory(Category(nameAr = "دهان جدران ومطابخ ديركو", nameEn = "Wall & Kitchen Painting", parentId = paintId, sortOrder = 30))
            dao.insertCategory(Category(nameAr = "تصاميم ورق حائط وتعتيق", nameEn = "Wallpaper & Antique Art", parentId = paintId, sortOrder = 31))

            // Subcategories for Tech
            dao.insertCategory(Category(nameAr = "برمجة هواتف وحواسيب", nameEn = "Mobile & PC Programming", parentId = techId, sortOrder = 40))
            dao.insertCategory(Category(nameAr = "تنسيق مزارع وزهور", nameEn = "Gardening & Landscaping", parentId = techId, sortOrder = 41))

            // Seed default cities
            dao.insertCity(City(nameAr = "صنعاء", nameEn = "Sanaa"))
            dao.insertCity(City(nameAr = "عدن", nameEn = "Aden"))
            dao.insertCity(City(nameAr = "تعز", nameEn = "Taiz"))
            dao.insertCity(City(nameAr = "الحديدة", nameEn = "Hodeidah"))
            dao.insertCity(City(nameAr = "حضرموت", nameEn = "Hadramout"))

            // Seed actual service providers
            dao.insertProvider(
                ServiceProvider(
                    fullName = "ماهر محمد طاهر",
                    phone = "777644670",
                    mainCategoryId = electId,
                    area = "الحصبة",
                    address = "شارع مازدا، خلف البريد",
                    gpsCoordinates = "15.3694,44.1912",
                    isPinned = true,
                    isRecommended = true,
                    isVerified = true,
                    averageRating = 4.9f,
                    ratingCount = 14,
                    isSubscribed = true,
                    subscriptionStatus = "APPROVED",
                    points = 120
                )
            )

            dao.insertProvider(
                ServiceProvider(
                    fullName = "أبو أحمد السباك",
                    phone = "771234567",
                    mainCategoryId = plumbingId,
                    area = "التحرير",
                    address = "بجوار مكتبة الجيل الجديد",
                    gpsCoordinates = "15.3522,44.2018",
                    isPinned = false,
                    isRecommended = true,
                    isVerified = false,
                    averageRating = 4.6f,
                    ratingCount = 8,
                    points = 40
                )
            )

            dao.insertProvider(
                ServiceProvider(
                    fullName = "ياسين لطلاء الجدران الراقية",
                    phone = "733998877",
                    mainCategoryId = paintId,
                    area = "جولة سبأ",
                    address = "شارع القيادة، تقاطع الزبيري",
                    gpsCoordinates = "15.3592,44.2091",
                    isPinned = true,
                    isRecommended = false,
                    isVerified = true,
                    averageRating = 4.8f,
                    ratingCount = 18,
                    points = 250
                )
            )

            dao.insertProvider(
                ServiceProvider(
                    fullName = "م. خالد لصيانة التكييف المركزي",
                    phone = "711223344",
                    mainCategoryId = acId,
                    area = "حدة",
                    address = "عمارة حدة هيلز، شارع حدة الرئيسي",
                    gpsCoordinates = "15.3123,44.1824",
                    isPinned = false,
                    isRecommended = true,
                    isVerified = true,
                    averageRating = 5.0f,
                    ratingCount = 22,
                    isSubscribed = true,
                    subscriptionStatus = "APPROVED",
                    points = 320
                )
            )

            // Seed default Banner Ads
            dao.insertBanner(
                BannerAd(
                    title = "مهرجان الصيف: خصومات تصل لـ 20% على خدمات تنظيف المنازل بالكامل!",
                    imageUrl = "https://images.unsplash.com/photo-1581578731548-c64695cc6952?q=80&w=1470&auto=format&fit=crop",
                    linkUrl = "https://www.google.com",
                    type = "IMAGE",
                    size = "MEDIUM",
                    durationSeconds = 6,
                    startDate = System.currentTimeMillis()
                )
            )

            dao.insertBanner(
                BannerAd(
                    title = "احصل الآن على شارة التحقق الزرقاء وقدم خدماتك لملايين العملاء مجاناً!",
                    imageUrl = "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?q=80&w=1470&auto=format&fit=crop",
                    linkUrl = "https://www.google.com",
                    type = "IMAGE",
                    size = "LARGE",
                    durationSeconds = 8,
                    startDate = System.currentTimeMillis()
                )
            )

            // Seed placeholder whitelist
            dao.insertDevice(DeviceWhitelist(deviceId = "ADMIN_SECURE_PHONE", deviceName = "سامسونج جالاكسي S24 ألترا"))

            // Seed demo messages
            dao.insertMessage(
                Message(
                    conversationId = "777644670_admin",
                    senderName = "ماهر محمد طاهر",
                    senderRole = "PROVIDER",
                    messageText = "مرحباً يا مدير، لقد قدمت بالطلب وأتمنى الموافقة والتوثيق للخدمات."
                )
            )
            dao.insertMessage(
                Message(
                    conversationId = "777644670_admin",
                    senderName = "الأدمن WAM2026",
                    senderRole = "ADMIN",
                    messageText = "مرحباً بك يا ماهر. تم استلام طلبك ومراجعته وتوثيق حسابك بالشارة الزرقاء وتثبيته كخدمة موصى بها."
                )
            )
        }
    }
}
