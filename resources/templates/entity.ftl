package ${packageName};

import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import ${dtoFqn};

<#if imports??>
<#list imports as i>
import ${i};
</#list>
</#if>

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "${nameUtil.toTableName(clazz.name)}")
public class ${clazz.name} {
<#if !hasId>
  <#if idStrategy?string == "LONG_IDENTITY">
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
  <#elseif idStrategy?string == "UUID">
    @Id
    @GeneratedValue
    private java.util.UUID id;
  </#if>

</#if>
<#list props as p>
  <#if p.id>
    <#if idStrategy?string == "LONG_IDENTITY">
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    <#elseif idStrategy?string == "UUID">
    @Id
    @GeneratedValue
    </#if>
  </#if>
  <#if p.relation>
    <#if p.relationKind.name() == "MANY_TO_ONE">
    <#if p.nullable?? && !p.nullable>
    @NotNull
    </#if>
    @ManyToOne(fetch = FetchType.LAZY)
    <#if p.nullable?? || p.unique??>
    @JoinColumn(<#if p.nullable??>nullable = ${p.nullable?c}</#if><#if p.unique??><#if p.nullable??>, </#if>unique = ${p.unique?c}</#if>)
    </#if>
    private ${p.targetClass} ${p.name};
    <#elseif p.relationKind.name() == "ONE_TO_MANY">
      <#if p.mappedBy?? && p.mappedBy?has_content>
    @OneToMany(mappedBy = "${p.mappedBy}")
      <#else>
    @OneToMany
      </#if>
    private Set<${p.targetClass}> ${p.name} = new HashSet<${p.targetClass}>();
    <#elseif p.relationKind.name() == "ONE_TO_ONE">
      <#if p.nullable?? && !p.nullable>
    @NotNull
      </#if>
      <#if p.mappedBy?? && p.mappedBy?has_content>
    @OneToOne(mappedBy = "${p.mappedBy}")
      <#else>
    @OneToOne(fetch = FetchType.LAZY)
    <#if p.nullable?? || p.unique??>
    @JoinColumn(<#if p.nullable??>nullable = ${p.nullable?c}</#if><#if p.unique??><#if p.nullable??>, </#if>unique = ${p.unique?c}</#if>)
    </#if>
      </#if>
    private ${p.targetClass} ${p.name};
    <#elseif p.relationKind.name() == "MANY_TO_MANY">
      <#if p.mappedBy?? && p.mappedBy?has_content>
    @ManyToMany(mappedBy = "${p.mappedBy}")
    private Set<${p.targetClass}> ${p.name} = new HashSet<${p.targetClass}>();
      <#else>
    @ManyToMany
    @JoinTable(
        name = "${nameUtil.toTableName(clazz.name)}_${nameUtil.toTableName(p.targetClass)}",
        joinColumns = @JoinColumn(name = "${nameUtil.toColumnName(clazz.name)}_id"),
        inverseJoinColumns = @JoinColumn(name = "${nameUtil.toColumnName(p.targetClass)}_id")
    )
    private Set<${p.targetClass}> ${p.name} = new HashSet<${p.targetClass}>();
    </#if>
  </#if>
  <#else>
    <#if p.nullable?? && !p.nullable>
    @NotNull
    </#if>
    <#if p.size?? && typeUtil.toJava(p.type) == "String">
    @Size(max = ${p.size})
    </#if>
    <#if p.minValue??>
    @DecimalMin("${p.minValue}")
    </#if>
    <#if p.maxValue??>
    @DecimalMax("${p.maxValue}")
    </#if>
    @Column(
        name = "${nameUtil.toColumnName(p.name)}"<#if p.nullable??>,
        nullable = ${p.nullable?c}</#if><#if p.unique??>,
        unique = ${p.unique?c}</#if><#if p.size?? && typeUtil.toJava(p.type) == "String">,
        length = ${p.size}</#if>
    )
    <#if p.enumeration>
    @Enumerated(EnumType.STRING)
    </#if>
    private ${typeUtil.toJava(p.type)} ${p.name};
  </#if>

</#list>

    public ${clazz.name}(${clazz.name}DTO dto) {
        this(dto, true);
    }

    public ${clazz.name}(${clazz.name}DTO dto, boolean includeRelations) {
<#if !hasId>
        this.id = dto.getId();
</#if>
<#list props as p>
  <#if p.relation>
    <#if p.collection>
        if (includeRelations && dto.get${p.name?cap_first}() != null) {
            this.${p.name} = new LinkedHashSet<${p.targetClass}>();
            for (${p.targetClass}DTO item : dto.get${p.name?cap_first}()) {
                this.${p.name}.add(new ${p.targetClass}(item, false));
            }
        }
    <#else>
        if (includeRelations && dto.get${p.name?cap_first}() != null) {
            this.${p.name} = new ${p.targetClass}(dto.get${p.name?cap_first}(), false);
        }
    </#if>
  <#else>
        this.${p.name} = dto.get${p.name?cap_first}();
  </#if>
</#list>
    }

    public void updateFromDto(${clazz.name}DTO dto) {
        updateFromDto(dto, true);
    }

    public void updateFromDto(${clazz.name}DTO dto, boolean includeRelations) {
<#list props as p>
  <#if p.relation>
    <#if !p.id>
      <#if p.collection>
        if (includeRelations && dto.get${p.name?cap_first}() != null) {
            this.${p.name} = new LinkedHashSet<${p.targetClass}>();
            for (${p.targetClass}DTO item : dto.get${p.name?cap_first}()) {
                this.${p.name}.add(new ${p.targetClass}(item, false));
            }
        }
      <#else>
        if (includeRelations) {
            this.${p.name} = dto.get${p.name?cap_first}() == null ? null : new ${p.targetClass}(dto.get${p.name?cap_first}(), false);
        }
      </#if>
    </#if>
  <#elseif !p.id>
        this.${p.name} = dto.get${p.name?cap_first}();
  </#if>
</#list>
    }
}
