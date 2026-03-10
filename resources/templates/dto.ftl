package ${packageName};

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
<#if idType == "UUID">
import java.util.UUID;
</#if>
import ${entityFqn};

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ${clazz.name}DTO {
<#if !hasId>
    private ${idType} id;
</#if>
<#list props as p>
  <#if !p.relation>
    private ${typeUtil.toJava(p.type)} ${p.name};
  </#if>
</#list>

    public ${clazz.name}DTO(${clazz.name} entity) {
<#if !hasId>
        this.id = entity.getId();
</#if>
<#list props as p>
  <#if !p.relation>
        this.${p.name} = entity.get${p.name?cap_first}();
  </#if>
</#list>
    }
}
