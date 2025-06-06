package sn.svs.backoffice.config;

/**
 * Constantes utilisées dans toute l'application Maritime SVS
 */
public final class Constants {

    // Constantes de sécurité
    public static final String SYSTEM_USER = "system";
    public static final String ANONYMOUS_USER = "anonymoususer";
    public static final String DEFAULT_LANGUAGE = "fr";

    // Constantes JWT
    public static final String AUTHORITIES_KEY = "auth";
    public static final String JWT_ALGORITHM = "HS512";

    // Constantes de pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_FIELD = "id";
    public static final String DEFAULT_SORT_DIRECTION = "asc";

    // Constantes métier maritimes
    public static final String FACTURE_PREFIX = "FAC-";
    public static final String DEPENSE_PREFIX = "DEP-";
    public static final String COMPANY_CODE_PREFIX = "CMP-";
    public static final String SHIP_CODE_PREFIX = "SHP-";

    // Constantes devises
    public static final String DEVISE_XOF = "XOF";
    public static final String DEVISE_EUR = "EUR";
    public static final String DEVISE_USD = "USD";

    // Constantes de validation
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 254;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final int MAX_ADDRESS_LENGTH = 255;
    public static final int MAX_PHONE_LENGTH = 20;

    // Constantes maritimes spécifiques
    public static final int IMO_NUMBER_LENGTH = 7;
    public static final int MMSI_NUMBER_LENGTH = 9;
    public static final String SENEGAL_FLAG_CODE = "SN";
    public static final String DAKAR_PORT_CODE = "SNDK";
    public static final String DEFAULT_PORT_ATTACHE = "Dakar";

    // Constantes de statuts
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Constantes de formats de date
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIMEZONE_DAKAR = "Africa/Dakar";

    // Headers HTTP personnalisés
    public static final String HEADER_TOTAL_COUNT = "X-Total-Count";
    public static final String HEADER_MARITIME_ALERT = "X-Maritime-Alert";
    public static final String HEADER_API_VERSION = "X-API-Version";

    // Expressions régulières de validation
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String REGEX_PHONE_SENEGAL = "^(\\+221|221)?[0-9]{8,9}$";
    public static final String REGEX_RCCM = "^SN-DKR-[0-9]{4}-[A-Z]-[0-9]{5}$";
    public static final String REGEX_NINEA = "^[0-9]{9}$";
    public static final String REGEX_IMO = "^IMO[0-9]{7}$";
    public static final String REGEX_MMSI = "^[0-9]{9}$";

    // Messages d'erreur
    public static final String ERROR_VALIDATION = "Erreur de validation";
    public static final String ERROR_NOT_FOUND = "Ressource non trouvée";
    public static final String ERROR_DUPLICATE = "Ressource déjà existante";
    public static final String ERROR_UNAUTHORIZED = "Accès non autorisé";
    public static final String ERROR_FORBIDDEN = "Accès interdit";
    public static final String ERROR_INTERNAL = "Erreur interne du serveur";

    // Cache names
    public static final String CACHE_COMPANIES = "companies";
    public static final String CACHE_SHIPS = "ships";
    public static final String CACHE_OPERATIONS = "operations";
    public static final String CACHE_EXPENSE_CATEGORIES = "expenseCategories";

    private Constants() {
        // Classe utilitaire - constructeur privé
    }
}
