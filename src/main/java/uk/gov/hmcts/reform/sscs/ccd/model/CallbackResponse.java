package uk.gov.hmcts.reform.sscs.ccd.model;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseData;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class CallbackResponse<T extends CaseData> {

    @NonNull
    private T data;
    private final Set<String> errors = new LinkedHashSet<>();
    private final Set<String> warnings = new LinkedHashSet<>();

    public CallbackResponse(@NotNull T data) {
        this.data = data;
    }

    public void setData(@NotNull T data) {
        this.data = data;
    }

    @NotNull
    public T getData() {
        return data;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public void addErrors(Collection<String> errors) {
        this.errors.addAll(errors);
    }

    public void addWarning(String error) {
        this.warnings.add(error);
    }

    public void addWarnings(Collection<String> errors) {
        this.warnings.addAll(errors);
    }

    public Set<String> getErrors() {
        return ImmutableSet.copyOf(errors);
    }

    public Set<String> getWarnings() {
        return ImmutableSet.copyOf(warnings);
    }
}
