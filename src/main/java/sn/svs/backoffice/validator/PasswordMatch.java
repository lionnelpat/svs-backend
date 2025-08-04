package sn.svs.backoffice.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validateur personnalisé pour vérifier que les mots de passe correspondent
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchValidator.class)
@Documented
public @interface PasswordMatch {
    String message() default "Les mots de passe ne correspondent pas";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
