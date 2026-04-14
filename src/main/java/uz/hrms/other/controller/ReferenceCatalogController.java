package uz.hrms.other.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.auth.CurrentUser;
import uz.hrms.other.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reference")
@Validated
public class ReferenceCatalogController {

    private final ReferenceCatalogService referenceCatalogService;
    private final AccessPolicy accessPolicy;

    ReferenceCatalogController(ReferenceCatalogService referenceCatalogService, AccessPolicy accessPolicy) {
        this.referenceCatalogService = referenceCatalogService;
        this.accessPolicy = accessPolicy;
    }

    @GetMapping("/catalogs")
    List<ReferenceCatalogDefinitionResponse> definitions(Authentication authentication) {
        ensureRead(authentication);
        return referenceCatalogService.definitions();
    }

    @GetMapping("/catalogs/{catalog}")
    List<ReferenceCatalogItemResponse> list(Authentication authentication, @PathVariable String catalog) {
        ensureRead(authentication);
        return referenceCatalogService.list(ReferenceCatalogKey.fromPath(catalog));
    }

    @PostMapping("/catalogs/{catalog}")
    @ResponseStatus(HttpStatus.CREATED)
    ReferenceCatalogItemResponse create(Authentication authentication, @PathVariable String catalog, @Valid @RequestBody ReferenceCatalogUpsertRequest request) {
        ensureWrite(authentication);
        return referenceCatalogService.create(ReferenceCatalogKey.fromPath(catalog), request, currentUser(authentication));
    }

    @PutMapping("/catalogs/{catalog}/{id}")
    ReferenceCatalogItemResponse update(Authentication authentication, @PathVariable String catalog, @PathVariable UUID id, @Valid @RequestBody ReferenceCatalogUpsertRequest request) {
        ensureWrite(authentication);
        return referenceCatalogService.update(ReferenceCatalogKey.fromPath(catalog), id, request, currentUser(authentication));
    }

    private void ensureRead(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "ROLE", "READ")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureWrite(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "ROLE", "WRITE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private CurrentUser currentUser(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CurrentUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return (CurrentUser) principal;
    }
}