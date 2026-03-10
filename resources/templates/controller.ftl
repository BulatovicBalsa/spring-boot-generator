package ${packageName};

import java.util.List;
import java.util.stream.Collectors;
<#if idType == "UUID">
import java.util.UUID;
</#if>

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import ${entityFqn};
import ${dtoFqn};
import ${serviceFqn};

@RestController
@RequestMapping("${basePath}")
public class ${clazz.name}Controller {

    private final I${clazz.name}ServiceCrud service;

    public ${clazz.name}Controller(I${clazz.name}ServiceCrud service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<${clazz.name}DTO> create(@RequestBody @Valid ${clazz.name}DTO body) {
        ${clazz.name} created = service.create(new ${clazz.name}(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(new ${clazz.name}DTO(created));
    }

    @PutMapping
    public ResponseEntity<${clazz.name}DTO> update(@RequestBody @Valid ${clazz.name}DTO body) {
        if (body.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        ${clazz.name} existing = service.findById(body.getId());
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.updateFromDto(body);
        ${clazz.name} updated = service.update(existing);
        return ResponseEntity.ok(new ${clazz.name}DTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ${idType} id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<${clazz.name}DTO> findById(@PathVariable ${idType} id) {
        ${clazz.name} found = service.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ${clazz.name}DTO(found));
    }

    @GetMapping
    public ResponseEntity<List<${clazz.name}DTO>> findAll() {
        List<${clazz.name}> all = service.findAll();
        return ResponseEntity.ok(
                all.stream()
                        .map(${clazz.name}DTO::new)
                        .collect(Collectors.toList())
        );
    }
}
