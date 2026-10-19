package za.ac.cput.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import za.ac.cput.domain.FundiProfile;
import za.ac.cput.service.FundiProfileService;

import java.util.List;

@RestController
@RequestMapping("/api/fundiprofiles")
@CrossOrigin
public class FundiProfileController
        extends BaseController<FundiProfile> {

    private final FundiProfileService service;

    public FundiProfileController(
            FundiProfileService service) {
        this.service = service;
    }

    @Override
    protected Object getService() {
        return service;
    }

    @Override
    protected FundiProfile createEntity(
            FundiProfile entity) {

        return service.create(entity);
    }

    @Override
    protected FundiProfile updateEntity(
            FundiProfile entity) {

        return service.update(entity);
    }

    @Override
    protected FundiProfile getEntity(
            Long id) {

        return service.getById(id);
    }

    @Override
    protected List<FundiProfile> getEntities() {
        return service.getAll();
    }

    @Override
    protected void deleteEntity(Long id) {
        service.delete(id);
    }

    /* =====================================================
       GET FUNDI PROFILE BY USER ID
       ===================================================== */

    @GetMapping("/user/{userId}")
    public ResponseEntity<FundiProfile> getByUserId(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                service.getByUserId(userId)
        );
    }

    /* =====================================================
       APPROVE FUNDI
       ===================================================== */

    @PutMapping("/{id}/approve")
    public ResponseEntity<FundiProfile> approveFundi(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.approveFundi(id)
        );
    }

    /* =====================================================
       REJECT FUNDI
       ===================================================== */

    @PutMapping("/{id}/reject")
    public ResponseEntity<FundiProfile> rejectFundi(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.rejectFundi(id)
        );
    }
}

