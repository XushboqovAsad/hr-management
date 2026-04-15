package uz.hrms.other.dto.lmsDtos;

import java.util.UUID;

record LmsTestOptionResponse(
        UUID id,
        Integer optionOrder,
        String optionText
) {
}
