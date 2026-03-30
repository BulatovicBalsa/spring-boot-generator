package ${packageName};

import java.util.Set;
import java.util.HashSet;
<#if idType == "UUID">
import java.util.UUID;
</#if>
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ${entityFqn};
<#if imports??>
<#list imports as importedDto>
import ${importedDto};
</#list>
</#if>
<#if entityImports??>
<#list entityImports as importedEntity>
import ${importedEntity};
</#list>
</#if>

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ${clazz.name}DTO {
<#if !hasId>
    private ${idType} id;
</#if>
<#list props as p>
  <#if !p.hidden?? || !p.hidden>
    <#if p.relation>
      <#if p.collection>
    private Set<${p.targetClass}DTO> ${p.name} = new HashSet<${p.targetClass}DTO>();
      <#else>
    private ${p.targetClass}DTO ${p.name};
      </#if>
    <#else>
      <#if p.name?lower_case?contains("password")>
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
      </#if>
    private ${typeUtil.toJava(p.type)} ${p.name};
    </#if>
  </#if>
</#list>

    public ${clazz.name}DTO(${clazz.name} entity) {
        this(entity, true);
    }

    public ${clazz.name}DTO(${clazz.name} entity, boolean includeRelations) {
<#if !hasId>
        this.id = entity.getId();
</#if>
<#list props as p>
  <#if !p.hidden?? || !p.hidden>
    <#if p.relation>
      <#if p.collection>
        if (includeRelations && entity.get${p.name?cap_first}() != null) {
            for (${p.targetClass} item : entity.get${p.name?cap_first}()) {
                this.${p.name}.add(new ${p.targetClass}DTO(item, false));
            }
        }
      <#else>
        if (includeRelations && entity.get${p.name?cap_first}() != null) {
            this.${p.name} = new ${p.targetClass}DTO(entity.get${p.name?cap_first}(), false);
        }
      </#if>
    <#else>
      <#if !p.name?lower_case?contains("password")>
        this.${p.name} = entity.get${p.name?cap_first}();
      </#if>
    </#if>
  </#if>
</#list>
    }
}
