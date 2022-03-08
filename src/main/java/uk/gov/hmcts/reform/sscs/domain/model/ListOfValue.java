package uk.gov.hmcts.reform.sscs.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class ListOfValue {

    @Id
    @Column(name = "ctid")
    @JsonIgnore
    private String id;
    private String key;
    private String value;
}
