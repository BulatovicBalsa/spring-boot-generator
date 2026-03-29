package ${packageName};

import java.util.List;
<#if idType == "UUID">
import java.util.UUID;
</#if>

import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ${entityFqn};
import ${repoFqn};
<#if interfaceFqn??>
import ${interfaceFqn};
</#if>

@Service
@Transactional
public class ${clazz.name}ServiceCrudImpl implements I${clazz.name}ServiceCrud {

    private final ${clazz.name}Repository repository;

    public ${clazz.name}ServiceCrudImpl(${clazz.name}Repository repository) {
        this.repository = repository;
    }

    @Override
    public ${clazz.name} create(${clazz.name} entity) {
        return repository.save(entity);
    }

    @Override
    public ${clazz.name} update(${clazz.name} entity) {
        return repository.save(entity);
    }

    @Override
    public void delete(${idType} id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ${clazz.name} findById(${idType} id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<${clazz.name}> findAll() {
        return repository.findAll();
    }

<#list clazz.properties as p>
<#if p.searchable && !p.relation && !p.embedded>
    @Override
    @Transactional(readOnly = true)
<#if p.unique>
    public Optional<${clazz.name}> findBy${p.name?cap_first}(${typeUtil.toJava(p.type)} ${p.name}) {
    return repository.findBy${p.name?cap_first}(${p.name});
    }
<#else>
    public List<${clazz.name}> findBy${p.name?cap_first}(${typeUtil.toJava(p.type)} ${p.name}) {
    return repository.findBy${p.name?cap_first}(${p.name});
    }
</#if>
</#if>

<#if p.rangeQuery && !p.relation && !p.embedded>
    @Override
    @Transactional(readOnly = true)
    public List<${clazz.name}> findBy${p.name?cap_first}Between(${typeUtil.toJava(p.type)} start, ${typeUtil.toJava(p.type)} end) {
    return repository.findBy${p.name?cap_first}Between(start, end);
    }
</#if>
</#list>
}