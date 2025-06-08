package org.xresource.acodemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xresource.acodemo.entity.Asset;
import org.xresource.core.intent.core.annotations.BindingType;
import org.xresource.core.intent.core.annotations.Intent;
import org.xresource.core.intent.core.annotations.IntentParameter;
import org.xresource.core.intent.core.annotations.Join;
import org.xresource.core.intent.core.annotations.JoinFilter;
import org.xresource.core.intent.core.annotations.ParamSource;
import org.xresource.core.intent.core.annotations.SelectAttribute;

@Repository
@Intent(name = "assetComplexQuery", description = "Get asset details with project, manager, city, region filters", where = "tag='TAG-2' AND City.name='City 4'", selectAttributes = {
                @SelectAttribute(field = "assetId"),
                @SelectAttribute(field = "tag"),
                @SelectAttribute(field = "description"),
                @SelectAttribute(field = "Project.title", aliasAs = "projectTitle"),
                @SelectAttribute(field = "Person.name", aliasAs = "managerName"),
                @SelectAttribute(field = "City.name", aliasAs = "cityName"),
                @SelectAttribute(field = "Region.name", aliasAs = "regionName"),
                @SelectAttribute(field = "AssetMaintenance.date", aliasAs = "lastMaintenance"),
                @SelectAttribute(alias = "maint_person", field = "name", aliasAs = "maintenanceOwner")
}, joins = {
                @Join(resource = "AssetUsage"),
                @Join(resource = "Project"),
                @Join(resource = "ProjectMember"),
                @Join(resource = "Person"),
                @Join(resource = "Address"),
                @Join(resource = "City"),
                @Join(resource = "Region", alias = "r", filters = {
                                @JoinFilter(field = "r.name", param = "regionName", binding = BindingType.EXACT)
                }),
                @Join(resource = "AssetMaintenance", autoChain = false),
                @Join(resource = "ProjectMember", alias = "pm_maint"),
                @Join(resource = "Person", alias = "maint_person")
}, parameters = {
                @IntentParameter(name = "regionName", type = String.class, defaultValue = "Region 5", source = ParamSource.STATIC)
}, sortBy = { "assetId" })

public interface AssetRepository extends JpaRepository<Asset, Long> {
}