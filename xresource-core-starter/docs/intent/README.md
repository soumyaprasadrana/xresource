# 📌 Intent Framework

The **Intent Framework** allows you to define **complex, reusable queries** in a declarative format. Intents are either predefined or dynamically generated query configurations that fetch structured, relational data from the API with minimal effort. They're especially useful for **resource drilling**, data projection, and filtering in a way that abstracts underlying persistence logic.

---

## 🔧 Core Concepts

### ✅ What is an Intent?

An `Intent` describes what data to fetch, how to join related resources, which attributes to select, and what filters or parameters to apply — **without writing actual JPQL**. The system takes care of query generation.

It supports:

- **Recursive resource joins**
- **Projection and aliasing**
- **Parameter binding**
- **Sorting and grouping**
- **Paginated and limited results**

---

## 🧬 XML-Based Intent Definition

All `Intent` definitions are currently backed by an XML schema. Here's a high-level sample of what an `Intent` looks like:

```xml
<Intent description="Get asset details with project, manager, city, region filters"
        name="assetComplexQuery1"
        resource="Asset"
        where="tag='TAG-2' AND City.name='City 4'">

    <SelectAttribute field="assetId"/>
    <SelectAttribute field="tag"/>
    <SelectAttribute field="description"/>

    <parameters>
        <IntentParameter name="regionName" type="String" source="STATIC" defaultValue="Region 5"/>
    </parameters>

    <sortBy>
        <value>assetId</value>
    </sortBy>

    <ResourceDrill>
        <XResource name="AssetUsage">
            <XResource name="Project">
                <SelectAttribute aliasAs="ProjectTitle" field="title"/>
                <XResource name="ProjectMember">
                    <XResource name="Person">
                        <SelectAttribute aliasAs="managerName" field="name"/>
                        <XResource name="Address">
                            <XResource name="City">
                                <SelectAttribute aliasAs="cityName" field="name"/>
                                <XResource name="Region">
                                    <SelectAttribute aliasAs="regionName" field="name"/>
                                    <JoinFilter field="name" param="regionName" binding="EXACT"/>
                                </XResource>
                            </XResource>
                        </XResource>
                    </XResource>
                </XResource>
            </XResource>
        </XResource>

        <XResource name="AssetMaintenance">
            <SelectAttribute aliasAs="lastMaintenance" field="date"/>
            <XResource name="ProjectMember">
                <XResource name="Person">
                    <SelectAttribute aliasAs="maintenanceOwner" field="name"/>
                </XResource>
            </XResource>
        </XResource>
    </ResourceDrill>
</Intent>
```

👉 This XML gets compiled into JPQL automatically using the Intent engine.

---

## 💡 Introducing IQL (Intent Query Language) - Experimental

To make `Intent` definitions more **developer-friendly**, we've introduced an experimental **DSL** called **IQL** (Intent Query Language), powered by ANTLR. IQL simplifies XML by providing a structured, readable syntax.

### 🔤 Example IQL for the same query above:

```
Create Intent for resource Asset as assetComplexQuery1
    Description "Get asset details with project, manager, city, region filters"
    Where "tag='TAG-2' AND City.name='City 4'"
    Select assetId, tag, description
    Parameters
        Param regionName with datatype String having default value "Region 5" from source static
    Sort by assetId

    Include AssetUsage
        Include Project
            Select title as ProjectTitle
            Include ProjectMember
                Include Person
                    Select name as managerName
                    Include Address
                        Include City
                            Select name as cityName
                            Include Region
                                Select name as regionName
                                Add filter for name having exact value from parameter regionName

    Include AssetMaintenance
        Select date as lastMaintenance
        Include ProjectMember
            Include Person
                Select name as maintenanceOwner
```

🔁 This IQL is compiled to XML, then into JPQL — fully automated!

---

## 🧪 Features at a Glance

| Feature                | Status          | Description                                                   |
| ---------------------- | --------------- | ------------------------------------------------------------- |
| XML-Based Querying     | ✅ Stable       | Define complex queries using annotated XML                    |
| Recursive Joins        | ✅ Stable       | Nest `XResource` blocks to traverse deep resource trees       |
| Parameter Binding      | ✅ Stable       | Define query-time parameters and bind them to filters         |
| Select + Aliasing      | ✅ Stable       | Choose only the fields you want, and rename them              |
| Sorting & Grouping     | ✅ Stable       | Easily group or sort result sets                              |
| Pagination & Limit     | ✅ Stable       | Control result set size and pages                             |
| IQL DSL via ANTLR      | ⚗️ Experimental | Human-readable DSL with indentation-based syntax              |
| Join Optimization      | 🔜 Planned      | Automatically pick optimal join strategies from metadata      |
| Subquery Splitting     | 🔜 Planned      | Break large queries into subqueries if response times degrade |
| Intent Actions (Write) | 🔜 Planned      | Use intent filters to **update** or **patch** target records  |

---

## 🔮 Roadmap

We plan to enhance the framework with:

- Intelligent **join path selection** based on graph metadata.
- **Query optimization** using caching and statistics.
- **Write support**, where an intent can also perform updates.
- Visual builders and schema-assisted IDE plugins for IQL authoring.

---

## 📂 Schema Reference

The core XML schema that drives `Intent` validation is [shown above](#xml-based-intent-definition). It includes:

- `Intent`
- `SelectAttribute`
- `XResource`
- `IntentParameter`
- `JoinFilter`
- `sortBy`, `groupBy`, etc.

---

## 🧩 Integration

- Java classpath: `org.xresource.core.intent.core.parser.*`
- DSL support via ANTLR grammar: `IntentDsl.g4`
- XML compiler: `XmlToIntentCompiler.compile(Element)`
- DSL compiler: `IntentDslCompiler.compile(String)`
