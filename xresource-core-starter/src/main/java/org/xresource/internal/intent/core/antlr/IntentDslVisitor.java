// Generated from IntentDsl.g4 by ANTLR 4.13.1
package org.xresource.internal.intent.core.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link IntentDslParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface IntentDslVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#intent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntent(IntentDslParser.IntentContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#descriptionBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescriptionBlock(IntentDslParser.DescriptionBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#aliasBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAliasBlock(IntentDslParser.AliasBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#whereBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereBlock(IntentDslParser.WhereBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#paginationBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPaginationBlock(IntentDslParser.PaginationBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#limitBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitBlock(IntentDslParser.LimitBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#selectBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectBlock(IntentDslParser.SelectBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#selectList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectList(IntentDslParser.SelectListContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#joinBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinBlock(IntentDslParser.JoinBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#joinFilterBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinFilterBlock(IntentDslParser.JoinFilterBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#parameterBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterBlock(IntentDslParser.ParameterBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#paramEntry}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamEntry(IntentDslParser.ParamEntryContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#sortBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortBlock(IntentDslParser.SortBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#groupBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupBlock(IntentDslParser.GroupBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedName(IntentDslParser.QualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#paramSource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamSource(IntentDslParser.ParamSourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link IntentDslParser#bindingType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBindingType(IntentDslParser.BindingTypeContext ctx);
}