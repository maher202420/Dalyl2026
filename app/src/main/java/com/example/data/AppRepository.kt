package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDao) {

    // --- App Config / Settings ---
    val appConfig: Flow<AppConfig?> = dao.getAppConfigFlow()

    suspend fun getAppConfigDirect(): AppConfig? = dao.getAppConfig()

    suspend fun updateAppConfig(config: AppConfig) {
        dao.insertOrUpdateConfig(config)
    }

    // --- Categories ---
    val allCategories: Flow<List<Category>> = dao.getAllCategoriesFlow()
    val mainCategories: Flow<List<Category>> = dao.getMainCategoriesFlow()

    fun getSubcategories(parentId: Int): Flow<List<Category>> {
        return dao.getSubcategoriesFlow(parentId)
    }

    suspend fun insertCategory(category: Category) {
        dao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) {
        dao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        dao.deleteCategory(category)
        // Also delete subcategories under this main category if any
        if (category.parentId == null) {
            dao.deleteSubcategoriesByParent(category.id)
        }
    }

    // --- Service Providers ---
    val allServiceProviders: Flow<List<ServiceProvider>> = dao.getAllServiceProvidersFlow()
    val recommendedProviders: Flow<List<ServiceProvider>> = dao.getRecommendedProvidersFlow()
    val blockedProviders: Flow<List<ServiceProvider>> = dao.getBlockedProvidersFlow()

    fun getProvidersByCategory(categoryId: Int): Flow<List<ServiceProvider>> {
        return dao.getProvidersByCategoryFlow(categoryId)
    }

    suspend fun getProviderById(id: Int): ServiceProvider? {
        return dao.getProviderById(id)
    }

    suspend fun insertProvider(provider: ServiceProvider) {
        dao.insertProvider(provider)
    }

    suspend fun updateProvider(provider: ServiceProvider) {
        dao.updateProvider(provider)
    }

    suspend fun deleteProvider(provider: ServiceProvider) {
        dao.deleteProvider(provider)
    }

    // --- Pending Providers ---
    val allPendingProviders: Flow<List<PendingProvider>> = dao.getAllPendingProvidersFlow()

    suspend fun getPendingProviderById(id: Int): PendingProvider? {
        return dao.getPendingProviderById(id)
    }

    suspend fun insertPendingProvider(pending: PendingProvider) {
        dao.insertPendingProvider(pending)
    }

    suspend fun deletePendingProvider(pending: PendingProvider) {
        dao.deletePendingProvider(pending)
    }

    // --- Banners ---
    val activeBanners: Flow<List<BannerAd>> = dao.getActiveBannersFlow()
    val allBanners: Flow<List<BannerAd>> = dao.getAllBannersFlow()

    suspend fun insertBanner(banner: BannerAd) {
        dao.insertBanner(banner)
    }

    suspend fun updateBanner(banner: BannerAd) {
        dao.updateBanner(banner)
    }

    suspend fun deleteBanner(banner: BannerAd) {
        dao.deleteBanner(banner)
    }

    // --- Reports ---
    val allReports: Flow<List<Report>> = dao.getAllReportsFlow()

    suspend fun insertReport(report: Report) {
        dao.insertReport(report)
    }

    suspend fun deleteReport(reportId: Int) {
        dao.deleteReportById(reportId)
    }

    suspend fun clearAllReports() {
        dao.clearAllReports()
    }

    // --- Activity Logs ---
    val activityLogs: Flow<List<ActivityLog>> = dao.getActivityLogsFlow()

    suspend fun logActivity(actor: String, action: String) {
        dao.insertActivityLog(ActivityLog(actor = actor, action = action))
    }

    suspend fun clearOldLogs() {
        dao.clearOldActivityLogs()
    }

    // --- Messages / Chat ---
    val conversations: Flow<List<String>> = dao.getAllConversationsFlow()

    fun getMessagesForConversation(convoId: String): Flow<List<Message>> {
        return dao.getMessagesByConversationFlow(convoId)
    }

    suspend fun sendMessage(message: Message) {
        dao.insertMessage(message)
    }

    suspend fun clearMessages() {
        dao.clearOldMessages()
    }

    // --- Device Whitelist ---
    val deviceWhitelist: Flow<List<DeviceWhitelist>> = dao.getDeviceWhitelistFlow()

    suspend fun addToWhitelist(device: DeviceWhitelist) {
        dao.insertDevice(device)
    }

    suspend fun removeFromWhitelist(device: DeviceWhitelist) {
        dao.deleteDevice(device)
    }

    // --- Cities / Regions ---
    val allCities: Flow<List<City>> = dao.getAllCitiesFlow()

    suspend fun insertCity(city: City) {
        dao.insertCity(city)
    }

    suspend fun deleteCity(city: City) {
        dao.deleteCity(city)
    }

    // --- Previous Request ---
    val previousRequests: Flow<List<PreviousServiceRequest>> = dao.getPreviousRequestsFlow()

    suspend fun insertPreviousRequest(request: PreviousServiceRequest) {
        dao.insertPreviousRequest(request)
    }
}
