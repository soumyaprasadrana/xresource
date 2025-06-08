package org.xresource.internal.intent.core.xml;

import lombok.experimental.UtilityClass;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xresource.core.intent.core.annotations.BindingType;
import org.xresource.internal.intent.core.parser.model.IntentMeta;
import org.xresource.internal.intent.core.parser.model.JoinFilterMeta;
import org.xresource.internal.intent.core.parser.model.JoinMeta;
import org.xresource.internal.intent.core.parser.model.SelectAttributeMeta;
import org.xresource.internal.models.XResourceMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Optional;

@UtilityClass
public class XmlJoinParser {

    // Known non-join elements that are explicitly handled elsewhere
    private static final Set<String> NON_JOIN_ELEMENTS = Set.of(
            "SelectAttribute", "parameters", "sortBy", "groupBy",
            "IntentParameter", "JoinFilter", "value");

    /**
     * Entry point to parse all join structures from the root Intent XML element.
     * It looks for the optional <ResourceDrill> wrapper and collects all
     * JoinMeta objects into a single flattened list.
     *
     * @param intentElement The root <Intent> XML element.
     * @param intentMeta    The IntentMeta object to populate with select attributes
     *                      from joins.
     * @return A flattened list of all JoinMeta objects found, ordered by
     *         depth-first traversal.
     */
    public static List<JoinMeta> parseJoinsFromIntentElement(Element intentElement, IntentMeta intentMeta) {
        List<JoinMeta> allFlattenedJoins = new ArrayList<>();
        NodeList children = intentElement.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element el = (Element) node;
            if ("ResourceDrill".equals(el.getTagName())) {
                // If ResourceDrill wrapper exists, start collecting its children as top-level
                // joins
                // and recursively collect all nested joins into the same flattened list.
                collectJoinsRecursively(el, intentMeta, allFlattenedJoins);
                break; // Assuming only one ResourceDrill wrapper
            }
        }
        return allFlattenedJoins;
    }

    /**
     * Recursively traverses the XML tree, identifies join elements, and adds them
     * to a single, flattened list of JoinMeta objects.
     *
     * @param parentElement     The XML element that contains the join elements to
     *                          be parsed (e.g., <ResourceDrill> or a parent join
     *                          element).
     * @param intentMeta        The IntentMeta object to populate with select
     *                          attributes from joins (these are global).
     * @param allFlattenedJoins The list to which all parsed JoinMeta objects will
     *                          be added.
     */
    private static void collectJoinsRecursively(Element parentElement, IntentMeta intentMeta,
            List<JoinMeta> allFlattenedJoins) {
        NodeList children = parentElement.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element el = (Element) node;
            String tag = el.getTagName();

            // A join element is any element within <ResourceDrill> or a parent join that is
            // not a known non-join element.
            if (tag.equals("XResource")) {
                JoinMeta join = new JoinMeta();
                join.setResource(el.getAttribute("name"));
                join.setAlias(resolveJoinAlias(el, el.getAttribute("name")));
                // Optional attributes, retrieve if present, otherwise set to null or default
                Optional.ofNullable(el.getAttribute("on")).filter(s -> !s.isEmpty()).ifPresent(join::setOn);

                // autoChain defaults to false if not specified or invalid.
                if (parentElement.getTagName().equals("ResourceDrill")) {
                    join.setAutoChain(false);
                } else {
                    join.setAutoChain(true);
                }

                // Parse nested SelectAttributes for this join element and add to global intent
                // selects
                for (SelectAttributeMeta selectMeta : parseSelectAttributes(el, join.getAlias())) {
                    intentMeta.addSelectAttribute(selectMeta);
                }

                // Parse nested JoinFilters for this join element
                join.setFilters(parseJoinFilters(el, join.getAlias()));

                // Add this join to the main flattened list BEFORE recursing into its children.
                // This ensures depth-first order: parent then its immediate children.
                allFlattenedJoins.add(join);

                // Recursively collect nested joins within this join element
                // Pass 'el' as the new parent for the next level of recursion, and the same
                // flattened list.
                collectJoinsRecursively(el, intentMeta, allFlattenedJoins);
            }
        }
    }

    private static List<SelectAttributeMeta> parseSelectAttributes(Element parentElement, String parentSourceAlias) {
        List<SelectAttributeMeta> list = new ArrayList<>();
        NodeList children = parentElement.getChildNodes(); // Iterate direct children only

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && "SelectAttribute".equals(n.getNodeName())) {
                Element selEl = (Element) n;
                SelectAttributeMeta sel = new SelectAttributeMeta();
                sel.setField(selEl.getAttribute("field"));
                sel.setAlias(parentSourceAlias);
                Optional.ofNullable(selEl.getAttribute("aliasAs")).filter(s -> !s.isEmpty()).ifPresent(sel::setAliasAs);
                list.add(sel);
            }
        }
        return list;
    }

    private static List<JoinFilterMeta> parseJoinFilters(Element parentElement, String parentSourceAlias) {
        List<JoinFilterMeta> filtersList = new ArrayList<>();
        NodeList children = parentElement.getChildNodes(); // Iterate direct children only

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && "JoinFilter".equals(node.getNodeName())) {
                Element f = (Element) node;

                JoinFilterMeta filter = new JoinFilterMeta();
                String bindingAttr = f.getAttribute("binding");
                if (bindingAttr != null && !bindingAttr.isEmpty()) {
                    try {
                        filter.setBinding(BindingType.valueOf(bindingAttr));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid binding type encountered: " + bindingAttr + " for field "
                                + f.getAttribute("field"));
                        // Consider throwing a custom exception or setting a default/null binding
                    }
                }
                filter.setField(parentSourceAlias + "." + f.getAttribute("field"));
                filter.setParam(f.getAttribute("param"));
                filtersList.add(filter);
            }
        }
        return filtersList;
    }

    private static String resolveJoinAlias(Element joinElement, String tagName) {
        String alias = joinElement.getAttribute("alias");
        if (alias != null && !alias.isEmpty()) {
            return alias;
        } else {
            return tagName.substring(0, 1).toUpperCase() + "_" + (int) (Math.random() * 10000);
        }
    }
}