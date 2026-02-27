package ${packageName};

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;
import java.util.HashSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "${nameUtil.toSnakeCase(clazz.name)}")
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
    @ManyToOne
    private ${p.targetClass} ${p.name};
    <#elseif p.relationKind.name() == "ONE_TO_MANY">
      <#if p.mappedBy?? && p.mappedBy?has_content>
    @OneToMany(mappedBy = "${p.mappedBy}")
      <#else>
    @OneToMany
      </#if>
    private Set<${p.targetClass}> ${p.name} = new HashSet<${p.targetClass}>();
    <#elseif p.relationKind.name() == "ONE_TO_ONE">
      <#if p.mappedBy?? && p.mappedBy?has_content>
    @OneToOne(mappedBy = "${p.mappedBy}")
      <#else>
    @OneToOne
      </#if>
    private ${p.targetClass} ${p.name};
    <#elseif p.relationKind.name() == "MANY_TO_MANY">
      <#if p.mappedBy?? && p.mappedBy?has_content>
    @ManyToMany(mappedBy = "${p.mappedBy}")
      <#else>
    @ManyToMany
      </#if>
    private Set<${p.targetClass}> ${p.name} = new HashSet<${p.targetClass}>();
    </#if>
  <#else>
    @Column(name = "${nameUtil.toSnakeCase(p.name)}")
    private ${typeUtil.toJava(p.type)} ${p.name};
  </#if>

</#list>
}