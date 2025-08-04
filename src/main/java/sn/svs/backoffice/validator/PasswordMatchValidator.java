package sn.svs.backoffice.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import sn.svs.backoffice.dto.AuthDTO;



/**
 * Implémentation du validateur de correspondance des mots de passe
 */
class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        // Pas d'initialisation nécessaire
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj instanceof AuthDTO.ChangePasswordRequest) {
            return ((AuthDTO.ChangePasswordRequest) obj).isPasswordsMatching();
        }

        if (obj instanceof AuthDTO.ResetPasswordRequest) {
            return ((AuthDTO.ResetPasswordRequest) obj).isPasswordsMatching();
        }

        if (obj instanceof AuthDTO.RegisterRequest) {
            return ((AuthDTO.RegisterRequest) obj).isPasswordsMatching();
        }

        return true;
    }
}
