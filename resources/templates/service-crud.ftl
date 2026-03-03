package ${packageName};

import java.util.List;
<#if idType == "UUID">
import java.util.UUID;
</#if>

import ${entityFqn};

public interface I${clazz.name}ServiceCrud {

    ${clazz.name} create(${clazz.name} entity);

    ${clazz.name} update(${clazz.name} entity);

    void delete(${idType} id);

    ${clazz.name} findById(${idType} id);

    List<${clazz.name}> findAll();
}