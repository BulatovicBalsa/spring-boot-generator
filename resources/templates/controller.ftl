package ${packageName};

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ${entityFqn};
import ${serviceFqn};

@RestController
@RequestMapping("${basePath}")
public class ${clazz.name}Controller {

    private final I${clazz.name}ServiceCrud service;

    public ${clazz.name}Controller(I${clazz.name}ServiceCrud service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<${clazz.name}> create(@RequestBody ${clazz.name} body) {
        ${clazz.name} created = service.create(body);
        return ResponseEntity.ok(created);
    }

    @PutMapping
    public ResponseEntity<${clazz.name}> update(@RequestBody ${clazz.name} body) {
        ${clazz.name} updated = service.update(body);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") ${idType} id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<${clazz.name}> findById(@PathVariable("id") ${idType} id) {
        ${clazz.name} found = service.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @GetMapping
    public ResponseEntity<List<${clazz.name}>> findAll() {
        List<${clazz.name}> all = service.findAll();
        return ResponseEntity.ok(all);
    }
}