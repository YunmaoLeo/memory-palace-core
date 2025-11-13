package com.memorypalace.core.controller;

import com.memorypalace.core.model.AppUser;
import com.memorypalace.core.model.Folder;
import com.memorypalace.core.model.Project;
import com.memorypalace.core.repository.project.FolderRepository;
import com.memorypalace.core.repository.project.ProjectRepository;
import com.memorypalace.core.repository.user.UserRepository;
import com.memorypalace.core.security.TenantContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectsController {

    private final ProjectRepository projectRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public ProjectsController(ProjectRepository projectRepository, FolderRepository folderRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody Map<String, String> body) {
        var principal = TenantContext.get();
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        String name = body.getOrDefault("name", "").trim();
        if (name.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "name is required"));
        AppUser owner = userRepository.findById(principal.userId).orElse(null);
        if (owner == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        Project p = new Project();
        p.setTenant(owner.getTenant());
        p.setOwner(owner);
        p.setName(name);
        p = projectRepository.save(p);
        return ResponseEntity.ok(Map.of("id", p.getId(), "name", p.getName(), "createdAt", p.getCreatedAt()));
    }

    @GetMapping
    public ResponseEntity<?> listProjects() {
        var principal = TenantContext.get();
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        List<Project> list = projectRepository.findByTenant(principal.tenantId);
        return ResponseEntity.ok(list.stream().map(p -> Map.of(
            "id", p.getId(), "name", p.getName(), "createdAt", p.getCreatedAt()
        )).toList());
    }

    @PostMapping("/{projectId}/folders")
    public ResponseEntity<?> createFolder(@PathVariable("projectId") UUID projectId, @RequestBody Map<String, String> body) {
        var principal = TenantContext.get();
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        String name = body.getOrDefault("name", "").trim();
        if (name.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "name is required"));
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || !project.getTenant().getId().equals(principal.tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
        }
        Folder f = new Folder();
        f.setTenant(project.getTenant());
        f.setProject(project);
        f.setName(name);
        f = folderRepository.save(f);
        return ResponseEntity.ok(Map.of("id", f.getId(), "name", f.getName(), "projectId", projectId, "createdAt", f.getCreatedAt()));
    }

    @GetMapping("/{projectId}/folders")
    public ResponseEntity<?> listFolders(@PathVariable("projectId") UUID projectId) {
        var principal = TenantContext.get();
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        Project p = projectRepository.findById(projectId).orElse(null);
        if (p == null || !p.getTenant().getId().equals(principal.tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
        }
        List<Folder> list = folderRepository.findByProject(projectId, principal.tenantId);
        return ResponseEntity.ok(list.stream().map(f -> Map.of(
            "id", f.getId(), "name", f.getName(), "createdAt", f.getCreatedAt()
        )).toList());
    }
}


