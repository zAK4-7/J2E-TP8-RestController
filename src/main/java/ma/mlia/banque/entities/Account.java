package ma.mlia.banque.entities;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Bank account persisted in the H2 database.
 * {@code @JacksonXmlRootElement} is what makes the XML output possible.
 */
@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "account")
@Schema(description = "Bank account")
public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Generated identifier", example = "1")
    private Long id;

    @Schema(description = "Current balance", example = "1500.0")
    private double balance;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Creation date", example = "2024-11-01")
    private Date creationDate;

    // Stored as a string so the database stays readable
    @Enumerated(EnumType.STRING)
    @Schema(description = "Account type", example = "CURRENT")
    private AccountType type;
}
