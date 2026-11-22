package id.ac.itb.k09.historygo.navigation

/** Jenis kegagalan saat meminta rute. Pesannya ditampilkan sesuai bahasa aktif (A01). */
enum class RouteError { NO_API_KEY, NOT_AUTHORIZED, QUOTA, SERVICE, NO_ROUTE, NO_INTERNET, UNKNOWN }

class RouteException(val error: RouteError, val httpCode: Int? = null) :
    Exception("Routes API gagal: $error" + (httpCode?.let { " (HTTP $it)" } ?: ""))
