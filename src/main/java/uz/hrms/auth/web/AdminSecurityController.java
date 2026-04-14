package uz.hrms.auth.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.hrms.auth.service.AuthorizationQueryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Security Administration")
@SecurityRequirement(name = "bearerAuth")
public class AdminSecurityController {

    private final AuthorizationQueryService authorizationQueryService;

    public AdminSecurityController(AuthorizationQueryService authorizationQueryService) {
        this.authorizationQueryService = authorizationQueryService;
    }

    @GetMapping("/roles")
    @PreAuthorize("@accessPolicy.hasPermission(authentication, 'ROLE', 'READ')")
    @Operation(summary = "List roles with permissions")
    public ResponseEntity<List<AdminSecurityDtos.RoleResponse>> getRoles() {
        return ResponseEntity.ok(authorizationQueryService.getRoles());
    }

    @GetMapping("/permissions")
    @PreAuthorize("@accessPolicy.hasPermission(authentication, 'ROLE', 'READ')")
    @Operation(summary = "List permissions")
    public ResponseEntity<List<AdminSecurityDtos.PermissionResponse>> getPermissions() {
        return ResponseEntity.ok(authorizationQueryService.getPermissions());
    }
}
