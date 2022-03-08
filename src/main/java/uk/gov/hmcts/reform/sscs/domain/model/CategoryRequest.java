package uk.gov.hmcts.reform.sscs.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class CategoryRequest {

    String categoryId;

    String serviceId;

    String parentCategory;

    String parentKey;

    String key;

    String isChildRequired;
}
