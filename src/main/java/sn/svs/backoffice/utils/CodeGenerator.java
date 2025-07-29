package sn.svs.backoffice.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;
import java.util.function.Supplier;


public class CodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(CodeGenerator.class);

    private static final int MAX_ATTEMPTS = 100;


    // ✅ Constructeur privé pour empêcher l'instanciation
    private CodeGenerator() {
        throw new UnsupportedOperationException("Cette classe est utilitaire et ne doit pas être instanciée");
    }

    /**
     * Génère un code unique au format PREFIX-001, PREFIX-002, etc.
     *
     * @param prefix        le préfixe (ex: OPE, CAT-DEP)
     * @param existsChecker méthode de vérification d'existence (ex: repo::existsByCode)
     * @param lastCodeFetcher méthode qui retourne le dernier code généré (ex: repo::findLastCode)
     * @return le code généré
     */
    public static String generate(String prefix,
                                  Predicate<String> existsChecker,
                                  Supplier<String> lastCodeFetcher) {

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String code = generateNextCode(prefix, lastCodeFetcher.get());
            if (!existsChecker.test(code)) {
                return code;
            }
        }

        log.error("❌ Impossible de générer un code unique après {} tentatives pour le préfixe '{}'", MAX_ATTEMPTS, prefix);
        throw new IllegalStateException("Échec de génération de code unique pour le préfixe " + prefix);
    }

    private static String generateNextCode(String prefix, String lastCode) {
        int nextNumber = 1;

        if (lastCode != null && lastCode.startsWith(prefix)) {
            String numberPart = lastCode.substring(prefix.length());
            if (numberPart.matches("-?\\d+")) {
                nextNumber = Integer.parseInt(numberPart.replace("-", "")) + 1;
            }
        }

        return String.format("%s-%03d", prefix, nextNumber);
    }
}

