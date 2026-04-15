package uz.hrms.other.dto.lmsDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import uz.hrms.other.LmsTestAnswerRequest;

import java.util.List;

record LmsSubmitTestAttemptRequest(
        @Valid @NotEmpty List<LmsTestAnswerRequest> answers
) {
}
