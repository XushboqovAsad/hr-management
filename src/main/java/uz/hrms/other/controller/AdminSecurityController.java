package uz.hrms.other.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Security Administration")
@SecurityRequirement(name = "bearerAuth")
public class AdminSecurityController {

    private final AuthorizationQueryService authorizationQueryService;

    AdminSecurityController(AuthorizationQueryService authorizationQueryService) {
        this.authorizationQueryService = authorizationQueryService;
    }

    @GetMapping("/roles")
    @Operation(summary = "List roles with permissions")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.hasPermission(authentication, 'ROLE', 'READ')")
    ResponseEntity<List<RoleResponse>> getRoles() {
        return ResponseEntity.ok(authorizationQueryService.getRoles());
    }

    @GetMapping("/permissions")
    @Operation(summary = "List permissions")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.hasPermission(authentication, 'ROLE', 'READ')")
    ResponseEntity<List<PermissionResponse>> getPermissions() {
        return ResponseEntity.ok(authorizationQueryService.getPermissions());
    }
}
