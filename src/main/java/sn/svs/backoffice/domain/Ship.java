package sn.svs.backoffice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import sn.svs.backoffice.domain.ennumeration.ShipClassification;
import sn.svs.backoffice.domain.ennumeration.ShipFlag;
import sn.svs.backoffice.domain.ennumeration.ShipType;

/**
 * Entité représentant un navire
 * SVS - Dakar, Sénégal
 */
@Entity
@Table(name = "ships", indexes = {
        @Index(name = "idx_ship_nom", columnList = "nom"),
        @Index(name = "idx_ship_imo", columnList = "numero_imo"),
        @Index(name = "idx_ship_mmsi", columnList = "numero_mmsi"),
        @Index(name = "idx_ship_compagnie", columnList = "compagnie_id"),
        @Index(name = "idx_ship_type", columnList = "type_navire"),
        @Index(name = "idx_ship_pavillon", columnList = "pavillon"),
        @Index(name = "idx_ship_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@ToString(exclude = {"compagnie"})
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Ship extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ship_seq")
    @SequenceGenerator(name = "ship_seq", sequenceName = "ship_sequence", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Le nom du navire est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @NotBlank(message = "Le numéro IMO est obligatoire")
    @Size(min = 7, max = 10, message = "Le numéro IMO doit contenir entre 7 et 10 caractères")
    @Column(name = "numero_imo", nullable = false, length = 10, unique = true)
    private String numeroIMO;

    @NotNull(message = "Le pavillon est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "pavillon", nullable = false, length = 50)
    private ShipFlag pavillon;

    @NotNull(message = "Le type de navire est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "type_navire", nullable = false, length = 50)
    private ShipType typeNavire;

    @Min(value = 0, message = "Le nombre de passagers ne peut pas être négatif")
    @Column(name = "nombre_passagers")
    private Integer nombrePassagers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compagnie_id", nullable = false)
    private Company compagnie;

    @NotBlank(message = "Le port d'attache est obligatoire")
    @Size(min = 2, max = 100, message = "Le port d'attache doit contenir entre 2 et 100 caractères")
    @Column(name = "port_attache", nullable = false, length = 100)
    private String portAttache;

    @NotBlank(message = "Le numéro d'appel est obligatoire")
    @Size(min = 3, max = 20, message = "Le numéro d'appel doit contenir entre 3 et 20 caractères")
    @Column(name = "numero_appel", nullable = false, length = 20, unique = true)
    private String numeroAppel;

    @NotBlank(message = "Le numéro MMSI est obligatoire")
    @Size(min = 9, max = 9, message = "Le numéro MMSI doit contenir exactement 9 caractères")
    @Column(name = "numero_mmsi", nullable = false, length = 9, unique = true)
    private String numeroMMSI;

    @NotNull(message = "La classification est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "classification", nullable = false, length = 50)
    private ShipClassification classification;

    // Méthodes utilitaires
    public void activate() {
        this.setActive(true);
    }

    public void deactivate() {
        this.setActive(false);
    }

    /**
     * Méthode pour obtenir le nom complet du navire avec son IMO
     */
    public String getFullName() {
        return String.format("%s (IMO: %s)", this.nom, this.numeroIMO);
    }

    /**
     * Méthode pour vérifier si le navire peut transporter des passagers
     */
    public boolean canCarryPassengers() {
        return this.typeNavire == ShipType.PASSAGERS ||
                this.typeNavire == ShipType.CARGO ||
                (this.nombrePassagers != null && this.nombrePassagers > 0);
    }
}
