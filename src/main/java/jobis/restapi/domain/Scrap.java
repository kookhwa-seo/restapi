package jobis.restapi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Scrap {
    @Id
    private String userId;

    private Integer totalPayment;

    private Integer totalUseAmount;
}
