package org.xresource.internal.intent.core.dsl;

import org.antlr.v4.runtime.Token;
import org.w3c.dom.*;
import org.xresource.internal.intent.core.antlr.IntentDslBaseVisitor;
import org.xresource.internal.intent.core.antlr.IntentDslParser;
import org.xresource.internal.intent.core.antlr.IntentDslParser.AliasBlockContext;
import org.xresource.internal.intent.core.antlr.IntentDslParser.DescriptionBlockContext;
import org.xresource.internal.intent.core.antlr.IntentDslParser.GroupBlockContext;
import org.xresource.internal.intent.core.antlr.IntentDslParser.IntentContext;
import org.xresource.internal.intent.core.antlr.IntentDslParser.JoinBlockContext;
import org.xresource.internal.intent.core.antlr.IntentDslParser.JoinFilterBlockContext;
import org.xresource.internal.intent.core.antlr.IntentDslParser.LimitBlockContext;
import org.xresource.internal.intent.core.antlr.IntentDslParser.PaginationBlockContext;
import org.xresource.internal.intent.core.antlr.IntentDslParser.ParamEntryContext;
import org.xresource.internal.intent.core.antlr.IntentDslParser.ParameterBlockContext;
import org.xresource.internal.intent.core.antlr.IntentDslParser.QualifiedNameContext;
import org.xresource.internal.intent.core.antlr.IntentDslParser.SelectBlockContext;
import org.xresource.internal.intent.core.antlr.IntentDslParser.SelectListContext;
import org.xresource.internal.intent.core.antlr.IntentDslParser.SortBlockContext;
import org.xresource.internal.intent.core.antlr.IntentDslParser.WhereBlockContext;

import javax.xml.parsers.DocumentBuilderFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Visitor implementation that converts parsed Intent DSL into an XML
 * {@link Document}, structured according to the Intent XSD schema.
 */
public class IntentDslVisitorToXml extends IntentDslBaseVisitor<Element> {

    private Document doc;
    private Element root;

    /**
     * Entry point: visits the Intent block and initializes the XML Document.
     *
     * @param ctx the Intent block context
     * @return the complete XML Document representing the parsed DSL
     */
    @Override
    public Element visitIntent(IntentContext ctx) {
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new RuntimeException("Error creating XML Document", e);
        }

        root = doc.createElement("Intent");
        root.setAttribute("name", ctx.intentName.getText());
        root.setAttribute("resource", ctx.resourceName.getText());

        List<DescriptionBlockContext> descriptionBlocks = ctx.descriptionBlock();
        if (!descriptionBlocks.isEmpty()) {
            visitDescriptionBlock(descriptionBlocks.get(0));
            if (descriptionBlocks.size() > 1) {
                System.err.println("Warning: Multiple Description blocks found; using the first one.");
            }
        }

        List<AliasBlockContext> aliasBlocks = ctx.aliasBlock();
        if (!aliasBlocks.isEmpty()) {
            visitAliasBlock(aliasBlocks.get(0));
            if (aliasBlocks.size() > 1) {
                System.err.println("Warning: Multiple Alias blocks found; using the first one.");
            }
        }

        List<WhereBlockContext> whereBlocks = ctx.whereBlock();
        if (!whereBlocks.isEmpty()) {
            visitWhereBlock(whereBlocks.get(0));
            if (whereBlocks.size() > 1) {
                System.err.println("Warning: Multiple Where blocks found; using the first one.");
            }
        }

        List<PaginationBlockContext> paginationBlocks = ctx.paginationBlock();
        if (!paginationBlocks.isEmpty()) {
            visitPaginationBlock(paginationBlocks.get(0));
            if (paginationBlocks.size() > 1) {
                System.err.println("Warning: Multiple Pagination blocks found; using the first one.");
            }
        }

        List<LimitBlockContext> limitBlocks = ctx.limitBlock();
        if (!limitBlocks.isEmpty()) {
            visitLimitBlock(limitBlocks.get(0));
            if (limitBlocks.size() > 1) {
                System.err.println("Warning: Multiple Limit blocks found; using the first one.");
            }
        }

        List<SelectBlockContext> selectBlocks = ctx.selectBlock();
        if (!selectBlocks.isEmpty()) {
            visitSelectBlock(selectBlocks.get(0));
            if (selectBlocks.size() > 1) {
                System.err.println("Warning: Multiple Select blocks found; using the first one.");
            }
        }

        for (ParameterBlockContext paramBlock : ctx.parameterBlock()) {
            root.appendChild(visitParameterBlock(paramBlock));
        }

        for (SortBlockContext sortBlock : ctx.sortBlock()) {
            root.appendChild(visitSortBlock(sortBlock));
        }

        for (GroupBlockContext groupBlock : ctx.groupBlock()) {
            root.appendChild(visitGroupBlock(groupBlock));
        }

        List<Element> topLevelJoinElements = new ArrayList<>();
        // Collect all top-level join blocks
        for (JoinBlockContext joinBlock : ctx.joinBlock()) {
            topLevelJoinElements.add(visitJoinBlock(joinBlock));
        }

        // If there are any join blocks, create the wrapper and append them
        if (!topLevelJoinElements.isEmpty()) {
            Element resourceDrillWrapper = doc.createElement("ResourceDrill");
            for (Element joinElement : topLevelJoinElements) {
                resourceDrillWrapper.appendChild(joinElement);
            }
            root.appendChild(resourceDrillWrapper);
        }

        doc.appendChild(root);
        return root;
    }

    @Override
    public Element visitDescriptionBlock(DescriptionBlockContext ctx) {
        root.setAttribute("description", stripQuotes(ctx.description.getText()));
        return null;
    }

    @Override
    public Element visitAliasBlock(AliasBlockContext ctx) {
        root.setAttribute("rootAlias", ctx.alias.getText());
        return null;
    }

    @Override
    public Element visitWhereBlock(WhereBlockContext ctx) {
        root.setAttribute("where", stripQuotes(ctx.condition.getText()));
        return null;
    }

    @Override
    public Element visitPaginationBlock(PaginationBlockContext ctx) {
        root.setAttribute("paginated", ctx.paginatedValue.getText());
        return null;
    }

    @Override
    public Element visitLimitBlock(LimitBlockContext ctx) {
        root.setAttribute("limit", ctx.limitValue.getText());
        return null;
    }

    @Override
    public Element visitSelectBlock(SelectBlockContext ctx) {
        for (SelectListContext sl : ctx.selectList()) {
            Element sel = doc.createElement("SelectAttribute");
            sel.setAttribute("field", sl.field.getText());
            if (sl.alias != null) {
                sel.setAttribute("alias", sl.alias.getText());
            }
            root.appendChild(sel);
        }
        return null;
    }

    @Override
    public Element visitParameterBlock(ParameterBlockContext ctx) {
        Element params = doc.createElement("parameters");
        for (ParamEntryContext p : ctx.paramEntry()) {
            params.appendChild(visitParamEntry(p));
        }
        return params;
    }

    @Override
    public Element visitParamEntry(ParamEntryContext ctx) {
        Element param = doc.createElement("IntentParameter");
        param.setAttribute("name", ctx.name.getText());
        param.setAttribute("type", getQualifiedName(ctx.type));

        String source = ctx.source.getText().toUpperCase(Locale.ROOT);
        param.setAttribute("source", source);

        if (source.equals("STATIC") && ctx.defaultValue == null) {
            throw new IllegalArgumentException(
                    "Static parameter '" + ctx.name.getText() + "' must have a default value.");
        }
        if (ctx.defaultValue != null) {
            param.setAttribute("defaultValue", stripQuotes(ctx.defaultValue.getText()));
        }
        if (ctx.bindingType() != null) {
            param.setAttribute("binding", ctx.bindingType().getText().toUpperCase());
        }

        return param;
    }

    @Override
    public Element visitSortBlock(SortBlockContext ctx) {
        Element sort = doc.createElement("sortBy");
        for (Token s : ctx.sortFields) {
            appendValue(sort, s.getText());
        }
        return sort;
    }

    @Override
    public Element visitGroupBlock(GroupBlockContext ctx) {
        Element group = doc.createElement("groupBy");
        for (Token g : ctx.groupFields) {
            appendValue(group, g.getText());
        }
        return group;
    }

    @Override
    public Element visitJoinBlock(JoinBlockContext ctx) {
        String resource = ctx.resourceName.getText();
        Element join = doc.createElement("XResource");
        join.setAttribute("name", resource);
        if (ctx.alias != null) {
            join.setAttribute("alias", ctx.alias.getText());
        }
        for (SelectBlockContext selectBlock : ctx.selectBlock()) {
            for (SelectListContext sl : selectBlock.selectList()) {
                Element sel = doc.createElement("SelectAttribute");
                sel.setAttribute("field", sl.field.getText());
                if (sl.alias != null) {
                    sel.setAttribute("aliasAs", sl.alias.getText());
                }
                join.appendChild(sel);
            }
        }
        for (JoinFilterBlockContext filterBlock : ctx.joinFilterBlock()) {
            join.appendChild(visitJoinFilterBlock(filterBlock));
        }
        for (JoinBlockContext nestedJoin : ctx.joinBlock()) {
            join.appendChild(visitJoinBlock(nestedJoin));
        }
        return join;
    }

    @Override
    public Element visitJoinFilterBlock(JoinFilterBlockContext ctx) {
        Element filter = doc.createElement("JoinFilter");
        filter.setAttribute("field", ctx.field.getText());
        filter.setAttribute("binding", ctx.binding.getText().toUpperCase());
        filter.setAttribute("param", ctx.paramName.getText());
        return filter;
    }

    // Helper to wrap value elements
    private void appendValue(Element parent, String value) {
        Element val = doc.createElement("value");
        val.setTextContent(value);
        parent.appendChild(val);
    }

    // Helper to convert qualified name
    private String getQualifiedName(QualifiedNameContext ctx) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ctx.IDENTIFIER().size(); i++) {
            if (i > 0) {
                sb.append('.');
            }
            sb.append(ctx.IDENTIFIER(i).getText());
        }
        return sb.toString();
    }

    // Helper to strip surrounding quotes from strings
    private String stripQuotes(String quoted) {
        if (quoted != null && quoted.length() >= 2 && quoted.startsWith("\"") && quoted.endsWith("\"")) {
            return quoted.substring(1, quoted.length() - 1);
        }
        return quoted;
    }

    // Getter for the XML Document
    public Document getDocument() {
        return doc;
    }
}