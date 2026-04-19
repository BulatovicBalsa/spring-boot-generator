package ${packageName};

import java.util.List;
<#if idType == "UUID">
import java.util.UUID;
</#if>

import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ${entityFqn};

public interface I${clazz.name}ServiceCrud {

    ${clazz.name} create(${clazz.name} entity);

    ${clazz.name} update(${clazz.name} entity);

    void delete(${idType} id);

    ${clazz.name} findById(${idType} id);

    List<${clazz.name}> findAll();

<#list clazz.properties as p>
<#if p.searchable && !p.relation && !p.embedded>
<#if p.unique>
    Optional<${clazz.name}> findBy${p.name?cap_first}(${typeUtil.toJava(p.type)} ${p.name});
<#else>
    List<${clazz.name}> findBy${p.name?cap_first}(${typeUtil.toJava(p.type)} ${p.name});
</#if>
</#if>

<#if p.rangeQuery && !p.relation && !p.embedded>
    List<${clazz.name}> findBy${p.name?cap_first}Between(${typeUtil.toJava(p.type)} start, ${typeUtil.toJava(p.type)} end);
</#if>
</#list>
}