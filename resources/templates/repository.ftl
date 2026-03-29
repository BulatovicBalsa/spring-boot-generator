package ${packageName};

<#if idType == "UUID">
import java.util.UUID;
</#if>

import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import ${entityPackage}.${clazz.name};

@Repository
public interface ${clazz.name}Repository extends JpaRepository<${clazz.name}, ${idType}> {

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

<#if clazz.pagination>
    Page<${clazz.name}> findAll(Pageable pageable);
</#if>
}