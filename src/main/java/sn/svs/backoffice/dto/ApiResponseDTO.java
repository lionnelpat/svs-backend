package sn.svs.backoffice.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour les réponses d'API standardisées
 * SVS - Dakar, Sénégal
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Réponse API standardisée")
public class ApiResponseDTO<T> {

    @Schema(description = "Indique si la requête a réussi", example = "true")
    private Boolean success;

    @Schema(description = "Message descriptif", example = "Opération réussie")
    private String message;

    @Schema(description = "Données de la réponse")
    private T data;

    @Schema(description = "Code d'erreur (si applicable)", example = "VALIDATION_ERROR")
    private String errorCode;

    @Schema(description = "Détails de l'erreur (si applicable)")
    private Object errorDetails;

    @Schema(description = "Horodatage de la réponse")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Méthodes statiques pour faciliter la création de réponses
    public static <T> ApiResponseDTO<T> success(T data) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponseDTO<T> error(String message) {
        return ApiResponseDTO.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponseDTO<T> error(String message, String errorCode) {
        return ApiResponseDTO.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }

    public static <T> ApiResponseDTO<T> error(String message, String errorCode, Object errorDetails) {
        return ApiResponseDTO.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .errorDetails(errorDetails)
                .build();
    }
}
