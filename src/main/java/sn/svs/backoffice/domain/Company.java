package sn.svs.backoffice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Entité représentant une compagnie maritime
 * SVS - Dakar, Sénégal
 */
@Entity
@Table(name = "companies", indexes = {
        @Index(name = "idx_company_nom", columnList = "nom"),
        @Index(name = "idx_company_email", columnList = "email"),
        @Index(name = "idx_company_rccm", columnList = "rccm"),
        @Index(name = "idx_company_ninea", columnList = "ninea"),
        @Index(name = "idx_company_active", columnList = "active"),
        @Index(name = "idx_company_pays", columnList = "pays")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@ToString(exclude = {"ships"})
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Company extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_seq")
    @SequenceGenerator(name = "company_seq", sequenceName = "company_sequence", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Le nom de la compagnie est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @NotBlank(message = "La raison sociale est obligatoire")
    @Size(min = 2, max = 150, message = "La raison sociale doit contenir entre 2 et 150 caractères")
    @Column(name = "raison_sociale", nullable = false, length = 150)
    private String raisonSociale;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    @Column(name = "adresse", nullable = false)
    private String adresse;

    @NotBlank(message = "La ville est obligatoire")
    @Size(min = 2, max = 100, message = "La ville doit contenir entre 2 et 100 caractères")
    @Column(name = "ville", nullable = false, length = 100)
    private String ville;

    @NotBlank(message = "Le pays est obligatoire")
    @Size(min = 2, max = 100, message = "Le pays doit contenir entre 2 et 100 caractères")
    @Column(name = "pays", nullable = false, length = 100)
    private String pays;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    @Column(name = "telephone", nullable = false, length = 20)
    private String telephone;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'format de l'email n'est pas valide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Size(max = 100, message = "Le contact principal ne peut pas dépasser 100 caractères")
    @Column(name = "contact_principal", length = 100)
    private String contactPrincipal;

    @Size(max = 20, message = "Le téléphone du contact ne peut pas dépasser 20 caractères")
    @Column(name = "telephone_contact", length = 20)
    private String telephoneContact;

    @Email(message = "Le format de l'email du contact n'est pas valide")
    @Size(max = 100, message = "L'email du contact ne peut pas dépasser 100 caractères")
    @Column(name = "email_contact", length = 100)
    private String emailContact;

    @Size(max = 50, message = "Le RCCM ne peut pas dépasser 50 caractères")
    @Column(name = "rccm", length = 50, unique = true)
    private String rccm;

    @Size(max = 20, message = "Le NINEA ne peut pas dépasser 20 caractères")
    @Column(name = "ninea", length = 20, unique = true)
    private String ninea;

    @Size(max = 255, message = "Le site web ne peut pas dépasser 255 caractères")
    @Column(name = "site_web")
    private String siteWeb;

    // Méthodes utilitaires
    public void activate() {
        this.setActive(true);
    }

    public void deactivate() {
        this.setActive(false);
    }

    @PrePersist
    protected void onCreate() {
        super.prePersist();
    }

    @PreUpdate
    protected void onUpdate() {
        super.preUpdate();
    }
}