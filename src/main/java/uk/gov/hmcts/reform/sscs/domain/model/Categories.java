package uk.gov.hmcts.reform.sscs.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Categories {

    @JsonProperty("list_of_values")
    private List<Category> listOfCategory;
}
