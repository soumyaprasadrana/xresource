// Generated from c:/Users/Soumya/Desktop/spring-opensource/xresource-core-starter/src/main/java/org/xresource/internal/intent/core/antlr/IntentDsl.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class IntentDslParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, T__34=35, T__35=36, T__36=37, T__37=38, 
		T__38=39, T__39=40, BOOLEAN=41, INT=42, STRING=43, IDENTIFIER=44, NEWLINE=45, 
		WS=46, COMMENT=47, INDENT=48, DEDENT=49;
	public static final int
		RULE_intent = 0, RULE_descriptionBlock = 1, RULE_aliasBlock = 2, RULE_whereBlock = 3, 
		RULE_paginationBlock = 4, RULE_limitBlock = 5, RULE_selectBlock = 6, RULE_selectList = 7, 
		RULE_joinBlock = 8, RULE_joinFilterBlock = 9, RULE_parameterBlock = 10, 
		RULE_paramEntry = 11, RULE_sortBlock = 12, RULE_groupBlock = 13, RULE_qualifiedName = 14, 
		RULE_paramSource = 15, RULE_bindingType = 16;
	private static String[] makeRuleNames() {
		return new String[] {
			"intent", "descriptionBlock", "aliasBlock", "whereBlock", "paginationBlock", 
			"limitBlock", "selectBlock", "selectList", "joinBlock", "joinFilterBlock", 
			"parameterBlock", "paramEntry", "sortBlock", "groupBlock", "qualifiedName", 
			"paramSource", "bindingType"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'Create'", "'Intent'", "'for'", "'resource'", "'as'", "'Description'", 
			"'With'", "'alias'", "'Where'", "'Paginated'", "'Limit'", "'Select'", 
			"','", "'Include'", "'Add'", "'filter'", "'having'", "'exact'", "'like'", 
			"'in'", "'greater_than'", "'less_than'", "'value'", "'from'", "'parameter'", 
			"'Parameters'", "'Param'", "'with'", "'datatype'", "'default'", "'source'", 
			"'using'", "'Sort'", "'by'", "'Group'", "'.'", "'static'", "'user_context'", 
			"'security_profile'", "'request'", null, null, null, null, null, "' '"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, "BOOLEAN", "INT", "STRING", "IDENTIFIER", 
			"NEWLINE", "WS", "COMMENT", "INDENT", "DEDENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "IntentDsl.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public IntentDslParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IntentContext extends ParserRuleContext {
		public Token resourceName;
		public Token intentName;
		public TerminalNode INDENT() { return getToken(IntentDslParser.INDENT, 0); }
		public List<TerminalNode> DEDENT() { return getTokens(IntentDslParser.DEDENT); }
		public TerminalNode DEDENT(int i) {
			return getToken(IntentDslParser.DEDENT, i);
		}
		public TerminalNode EOF() { return getToken(IntentDslParser.EOF, 0); }
		public List<TerminalNode> IDENTIFIER() { return getTokens(IntentDslParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(IntentDslParser.IDENTIFIER, i);
		}
		public List<DescriptionBlockContext> descriptionBlock() {
			return getRuleContexts(DescriptionBlockContext.class);
		}
		public DescriptionBlockContext descriptionBlock(int i) {
			return getRuleContext(DescriptionBlockContext.class,i);
		}
		public List<AliasBlockContext> aliasBlock() {
			return getRuleContexts(AliasBlockContext.class);
		}
		public AliasBlockContext aliasBlock(int i) {
			return getRuleContext(AliasBlockContext.class,i);
		}
		public List<WhereBlockContext> whereBlock() {
			return getRuleContexts(WhereBlockContext.class);
		}
		public WhereBlockContext whereBlock(int i) {
			return getRuleContext(WhereBlockContext.class,i);
		}
		public List<PaginationBlockContext> paginationBlock() {
			return getRuleContexts(PaginationBlockContext.class);
		}
		public PaginationBlockContext paginationBlock(int i) {
			return getRuleContext(PaginationBlockContext.class,i);
		}
		public List<LimitBlockContext> limitBlock() {
			return getRuleContexts(LimitBlockContext.class);
		}
		public LimitBlockContext limitBlock(int i) {
			return getRuleContext(LimitBlockContext.class,i);
		}
		public List<SelectBlockContext> selectBlock() {
			return getRuleContexts(SelectBlockContext.class);
		}
		public SelectBlockContext selectBlock(int i) {
			return getRuleContext(SelectBlockContext.class,i);
		}
		public List<JoinBlockContext> joinBlock() {
			return getRuleContexts(JoinBlockContext.class);
		}
		public JoinBlockContext joinBlock(int i) {
			return getRuleContext(JoinBlockContext.class,i);
		}
		public List<ParameterBlockContext> parameterBlock() {
			return getRuleContexts(ParameterBlockContext.class);
		}
		public ParameterBlockContext parameterBlock(int i) {
			return getRuleContext(ParameterBlockContext.class,i);
		}
		public List<SortBlockContext> sortBlock() {
			return getRuleContexts(SortBlockContext.class);
		}
		public SortBlockContext sortBlock(int i) {
			return getRuleContext(SortBlockContext.class,i);
		}
		public List<GroupBlockContext> groupBlock() {
			return getRuleContexts(GroupBlockContext.class);
		}
		public GroupBlockContext groupBlock(int i) {
			return getRuleContext(GroupBlockContext.class,i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(IntentDslParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(IntentDslParser.NEWLINE, i);
		}
		public IntentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intent; }
	}

	public final IntentContext intent() throws RecognitionException {
		IntentContext _localctx = new IntentContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_intent);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(34);
			match(T__0);
			setState(35);
			match(T__1);
			setState(36);
			match(T__2);
			setState(37);
			match(T__3);
			setState(38);
			((IntentContext)_localctx).resourceName = match(IDENTIFIER);
			setState(39);
			match(T__4);
			setState(40);
			((IntentContext)_localctx).intentName = match(IDENTIFIER);
			setState(41);
			match(INDENT);
			setState(53); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				setState(53);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case T__5:
					{
					setState(42);
					descriptionBlock();
					}
					break;
				case T__6:
					{
					setState(43);
					aliasBlock();
					}
					break;
				case T__8:
					{
					setState(44);
					whereBlock();
					}
					break;
				case T__9:
					{
					setState(45);
					paginationBlock();
					}
					break;
				case T__10:
					{
					setState(46);
					limitBlock();
					}
					break;
				case T__11:
					{
					setState(47);
					selectBlock();
					}
					break;
				case T__13:
					{
					setState(48);
					joinBlock();
					}
					break;
				case T__25:
					{
					setState(49);
					parameterBlock();
					}
					break;
				case T__32:
					{
					setState(50);
					sortBlock();
					}
					break;
				case T__34:
					{
					setState(51);
					groupBlock();
					}
					break;
				case NEWLINE:
					{
					setState(52);
					match(NEWLINE);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(55); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 35227388894912L) != 0) );
			setState(57);
			match(DEDENT);
			setState(61);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DEDENT) {
				{
				{
				setState(58);
				match(DEDENT);
				}
				}
				setState(63);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(64);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DescriptionBlockContext extends ParserRuleContext {
		public Token description;
		public TerminalNode NEWLINE() { return getToken(IntentDslParser.NEWLINE, 0); }
		public TerminalNode STRING() { return getToken(IntentDslParser.STRING, 0); }
		public DescriptionBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_descriptionBlock; }
	}

	public final DescriptionBlockContext descriptionBlock() throws RecognitionException {
		DescriptionBlockContext _localctx = new DescriptionBlockContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_descriptionBlock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			match(T__5);
			setState(67);
			((DescriptionBlockContext)_localctx).description = match(STRING);
			setState(68);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AliasBlockContext extends ParserRuleContext {
		public Token alias;
		public TerminalNode NEWLINE() { return getToken(IntentDslParser.NEWLINE, 0); }
		public TerminalNode IDENTIFIER() { return getToken(IntentDslParser.IDENTIFIER, 0); }
		public AliasBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aliasBlock; }
	}

	public final AliasBlockContext aliasBlock() throws RecognitionException {
		AliasBlockContext _localctx = new AliasBlockContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_aliasBlock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(70);
			match(T__6);
			setState(71);
			match(T__7);
			setState(72);
			((AliasBlockContext)_localctx).alias = match(IDENTIFIER);
			setState(73);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhereBlockContext extends ParserRuleContext {
		public Token condition;
		public TerminalNode NEWLINE() { return getToken(IntentDslParser.NEWLINE, 0); }
		public TerminalNode STRING() { return getToken(IntentDslParser.STRING, 0); }
		public WhereBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereBlock; }
	}

	public final WhereBlockContext whereBlock() throws RecognitionException {
		WhereBlockContext _localctx = new WhereBlockContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_whereBlock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(75);
			match(T__8);
			setState(76);
			((WhereBlockContext)_localctx).condition = match(STRING);
			setState(77);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PaginationBlockContext extends ParserRuleContext {
		public Token paginatedValue;
		public TerminalNode NEWLINE() { return getToken(IntentDslParser.NEWLINE, 0); }
		public TerminalNode BOOLEAN() { return getToken(IntentDslParser.BOOLEAN, 0); }
		public PaginationBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_paginationBlock; }
	}

	public final PaginationBlockContext paginationBlock() throws RecognitionException {
		PaginationBlockContext _localctx = new PaginationBlockContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_paginationBlock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			match(T__9);
			setState(80);
			((PaginationBlockContext)_localctx).paginatedValue = match(BOOLEAN);
			setState(81);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LimitBlockContext extends ParserRuleContext {
		public Token limitValue;
		public TerminalNode NEWLINE() { return getToken(IntentDslParser.NEWLINE, 0); }
		public TerminalNode INT() { return getToken(IntentDslParser.INT, 0); }
		public LimitBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_limitBlock; }
	}

	public final LimitBlockContext limitBlock() throws RecognitionException {
		LimitBlockContext _localctx = new LimitBlockContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_limitBlock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
			match(T__10);
			setState(84);
			((LimitBlockContext)_localctx).limitValue = match(INT);
			setState(85);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectBlockContext extends ParserRuleContext {
		public List<SelectListContext> selectList() {
			return getRuleContexts(SelectListContext.class);
		}
		public SelectListContext selectList(int i) {
			return getRuleContext(SelectListContext.class,i);
		}
		public TerminalNode NEWLINE() { return getToken(IntentDslParser.NEWLINE, 0); }
		public TerminalNode INDENT() { return getToken(IntentDslParser.INDENT, 0); }
		public SelectBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectBlock; }
	}

	public final SelectBlockContext selectBlock() throws RecognitionException {
		SelectBlockContext _localctx = new SelectBlockContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_selectBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			match(T__11);
			setState(88);
			selectList();
			setState(93);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__12) {
				{
				{
				setState(89);
				match(T__12);
				setState(90);
				selectList();
				}
				}
				setState(95);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(96);
			_la = _input.LA(1);
			if ( !(_la==NEWLINE || _la==INDENT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectListContext extends ParserRuleContext {
		public Token field;
		public Token alias;
		public List<TerminalNode> IDENTIFIER() { return getTokens(IntentDslParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(IntentDslParser.IDENTIFIER, i);
		}
		public SelectListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectList; }
	}

	public final SelectListContext selectList() throws RecognitionException {
		SelectListContext _localctx = new SelectListContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_selectList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(98);
			((SelectListContext)_localctx).field = match(IDENTIFIER);
			setState(101);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__4) {
				{
				setState(99);
				match(T__4);
				setState(100);
				((SelectListContext)_localctx).alias = match(IDENTIFIER);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class JoinBlockContext extends ParserRuleContext {
		public Token resourceName;
		public Token alias;
		public TerminalNode INDENT() { return getToken(IntentDslParser.INDENT, 0); }
		public TerminalNode DEDENT() { return getToken(IntentDslParser.DEDENT, 0); }
		public List<TerminalNode> IDENTIFIER() { return getTokens(IntentDslParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(IntentDslParser.IDENTIFIER, i);
		}
		public List<SelectBlockContext> selectBlock() {
			return getRuleContexts(SelectBlockContext.class);
		}
		public SelectBlockContext selectBlock(int i) {
			return getRuleContext(SelectBlockContext.class,i);
		}
		public List<JoinFilterBlockContext> joinFilterBlock() {
			return getRuleContexts(JoinFilterBlockContext.class);
		}
		public JoinFilterBlockContext joinFilterBlock(int i) {
			return getRuleContext(JoinFilterBlockContext.class,i);
		}
		public List<JoinBlockContext> joinBlock() {
			return getRuleContexts(JoinBlockContext.class);
		}
		public JoinBlockContext joinBlock(int i) {
			return getRuleContext(JoinBlockContext.class,i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(IntentDslParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(IntentDslParser.NEWLINE, i);
		}
		public JoinBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinBlock; }
	}

	public final JoinBlockContext joinBlock() throws RecognitionException {
		JoinBlockContext _localctx = new JoinBlockContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_joinBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			match(T__13);
			setState(104);
			((JoinBlockContext)_localctx).resourceName = match(IDENTIFIER);
			setState(107);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__4) {
				{
				setState(105);
				match(T__4);
				setState(106);
				((JoinBlockContext)_localctx).alias = match(IDENTIFIER);
				}
			}

			setState(109);
			match(INDENT);
			setState(116);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 35184372142080L) != 0)) {
				{
				setState(114);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case T__11:
					{
					setState(110);
					selectBlock();
					}
					break;
				case T__14:
					{
					setState(111);
					joinFilterBlock();
					}
					break;
				case T__13:
					{
					setState(112);
					joinBlock();
					}
					break;
				case NEWLINE:
					{
					setState(113);
					match(NEWLINE);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(118);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(119);
			match(DEDENT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class JoinFilterBlockContext extends ParserRuleContext {
		public Token field;
		public Token binding;
		public Token paramName;
		public List<TerminalNode> IDENTIFIER() { return getTokens(IntentDslParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(IntentDslParser.IDENTIFIER, i);
		}
		public JoinFilterBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinFilterBlock; }
	}

	public final JoinFilterBlockContext joinFilterBlock() throws RecognitionException {
		JoinFilterBlockContext _localctx = new JoinFilterBlockContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_joinFilterBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(121);
			match(T__14);
			setState(122);
			match(T__15);
			setState(123);
			match(T__2);
			setState(124);
			((JoinFilterBlockContext)_localctx).field = match(IDENTIFIER);
			setState(125);
			match(T__16);
			setState(126);
			((JoinFilterBlockContext)_localctx).binding = _input.LT(1);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 8126464L) != 0)) ) {
				((JoinFilterBlockContext)_localctx).binding = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(127);
			match(T__22);
			setState(128);
			match(T__23);
			setState(129);
			match(T__24);
			setState(130);
			((JoinFilterBlockContext)_localctx).paramName = match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParameterBlockContext extends ParserRuleContext {
		public TerminalNode INDENT() { return getToken(IntentDslParser.INDENT, 0); }
		public TerminalNode NEWLINE() { return getToken(IntentDslParser.NEWLINE, 0); }
		public TerminalNode DEDENT() { return getToken(IntentDslParser.DEDENT, 0); }
		public List<ParamEntryContext> paramEntry() {
			return getRuleContexts(ParamEntryContext.class);
		}
		public ParamEntryContext paramEntry(int i) {
			return getRuleContext(ParamEntryContext.class,i);
		}
		public ParameterBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterBlock; }
	}

	public final ParameterBlockContext parameterBlock() throws RecognitionException {
		ParameterBlockContext _localctx = new ParameterBlockContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_parameterBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(132);
			match(T__25);
			setState(133);
			match(INDENT);
			setState(135); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(134);
				paramEntry();
				}
				}
				setState(137); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==T__26 );
			setState(139);
			match(NEWLINE);
			setState(140);
			match(DEDENT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParamEntryContext extends ParserRuleContext {
		public Token name;
		public QualifiedNameContext type;
		public Token defaultValue;
		public ParamSourceContext source;
		public TerminalNode IDENTIFIER() { return getToken(IntentDslParser.IDENTIFIER, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public ParamSourceContext paramSource() {
			return getRuleContext(ParamSourceContext.class,0);
		}
		public BindingTypeContext bindingType() {
			return getRuleContext(BindingTypeContext.class,0);
		}
		public TerminalNode STRING() { return getToken(IntentDslParser.STRING, 0); }
		public ParamEntryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_paramEntry; }
	}

	public final ParamEntryContext paramEntry() throws RecognitionException {
		ParamEntryContext _localctx = new ParamEntryContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_paramEntry);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(142);
			match(T__26);
			setState(143);
			((ParamEntryContext)_localctx).name = match(IDENTIFIER);
			setState(144);
			match(T__27);
			setState(145);
			match(T__28);
			setState(146);
			((ParamEntryContext)_localctx).type = qualifiedName();
			setState(151);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__16) {
				{
				setState(147);
				match(T__16);
				setState(148);
				match(T__29);
				setState(149);
				match(T__22);
				setState(150);
				((ParamEntryContext)_localctx).defaultValue = match(STRING);
				}
			}

			setState(153);
			match(T__23);
			setState(154);
			match(T__30);
			setState(155);
			((ParamEntryContext)_localctx).source = paramSource();
			setState(158);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__31) {
				{
				setState(156);
				match(T__31);
				setState(157);
				bindingType();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SortBlockContext extends ParserRuleContext {
		public Token IDENTIFIER;
		public List<Token> sortFields = new ArrayList<Token>();
		public List<TerminalNode> IDENTIFIER() { return getTokens(IntentDslParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(IntentDslParser.IDENTIFIER, i);
		}
		public SortBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sortBlock; }
	}

	public final SortBlockContext sortBlock() throws RecognitionException {
		SortBlockContext _localctx = new SortBlockContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_sortBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(160);
			match(T__32);
			setState(161);
			match(T__33);
			setState(162);
			((SortBlockContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			((SortBlockContext)_localctx).sortFields.add(((SortBlockContext)_localctx).IDENTIFIER);
			setState(167);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__12) {
				{
				{
				setState(163);
				match(T__12);
				setState(164);
				((SortBlockContext)_localctx).IDENTIFIER = match(IDENTIFIER);
				((SortBlockContext)_localctx).sortFields.add(((SortBlockContext)_localctx).IDENTIFIER);
				}
				}
				setState(169);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GroupBlockContext extends ParserRuleContext {
		public Token IDENTIFIER;
		public List<Token> groupFields = new ArrayList<Token>();
		public List<TerminalNode> IDENTIFIER() { return getTokens(IntentDslParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(IntentDslParser.IDENTIFIER, i);
		}
		public GroupBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupBlock; }
	}

	public final GroupBlockContext groupBlock() throws RecognitionException {
		GroupBlockContext _localctx = new GroupBlockContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_groupBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(170);
			match(T__34);
			setState(171);
			match(T__33);
			setState(172);
			((GroupBlockContext)_localctx).IDENTIFIER = match(IDENTIFIER);
			((GroupBlockContext)_localctx).groupFields.add(((GroupBlockContext)_localctx).IDENTIFIER);
			setState(177);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__12) {
				{
				{
				setState(173);
				match(T__12);
				setState(174);
				((GroupBlockContext)_localctx).IDENTIFIER = match(IDENTIFIER);
				((GroupBlockContext)_localctx).groupFields.add(((GroupBlockContext)_localctx).IDENTIFIER);
				}
				}
				setState(179);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class QualifiedNameContext extends ParserRuleContext {
		public List<TerminalNode> IDENTIFIER() { return getTokens(IntentDslParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(IntentDslParser.IDENTIFIER, i);
		}
		public QualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedName; }
	}

	public final QualifiedNameContext qualifiedName() throws RecognitionException {
		QualifiedNameContext _localctx = new QualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_qualifiedName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(180);
			match(IDENTIFIER);
			setState(185);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__35) {
				{
				{
				setState(181);
				match(T__35);
				setState(182);
				match(IDENTIFIER);
				}
				}
				setState(187);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParamSourceContext extends ParserRuleContext {
		public ParamSourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_paramSource; }
	}

	public final ParamSourceContext paramSource() throws RecognitionException {
		ParamSourceContext _localctx = new ParamSourceContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_paramSource);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(188);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 2061584302080L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BindingTypeContext extends ParserRuleContext {
		public BindingTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bindingType; }
	}

	public final BindingTypeContext bindingType() throws RecognitionException {
		BindingTypeContext _localctx = new BindingTypeContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_bindingType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(190);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 8126464L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u00011\u00c1\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0004\u00006\b\u0000\u000b\u0000"+
		"\f\u00007\u0001\u0000\u0001\u0000\u0005\u0000<\b\u0000\n\u0000\f\u0000"+
		"?\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006\\\b\u0006"+
		"\n\u0006\f\u0006_\t\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0003\u0007f\b\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0003"+
		"\bl\b\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0005\bs\b\b\n\b\f\bv"+
		"\t\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0004\n\u0088"+
		"\b\n\u000b\n\f\n\u0089\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0003\u000b\u0098\b\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0003\u000b\u009f\b\u000b\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0005\f\u00a6\b\f\n\f\f\f\u00a9\t\f\u0001\r\u0001\r"+
		"\u0001\r\u0001\r\u0001\r\u0005\r\u00b0\b\r\n\r\f\r\u00b3\t\r\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0005\u000e\u00b8\b\u000e\n\u000e\f\u000e\u00bb"+
		"\t\u000e\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0000"+
		"\u0000\u0011\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016"+
		"\u0018\u001a\u001c\u001e \u0000\u0003\u0002\u0000--00\u0001\u0000\u0012"+
		"\u0016\u0001\u0000%(\u00c8\u0000\"\u0001\u0000\u0000\u0000\u0002B\u0001"+
		"\u0000\u0000\u0000\u0004F\u0001\u0000\u0000\u0000\u0006K\u0001\u0000\u0000"+
		"\u0000\bO\u0001\u0000\u0000\u0000\nS\u0001\u0000\u0000\u0000\fW\u0001"+
		"\u0000\u0000\u0000\u000eb\u0001\u0000\u0000\u0000\u0010g\u0001\u0000\u0000"+
		"\u0000\u0012y\u0001\u0000\u0000\u0000\u0014\u0084\u0001\u0000\u0000\u0000"+
		"\u0016\u008e\u0001\u0000\u0000\u0000\u0018\u00a0\u0001\u0000\u0000\u0000"+
		"\u001a\u00aa\u0001\u0000\u0000\u0000\u001c\u00b4\u0001\u0000\u0000\u0000"+
		"\u001e\u00bc\u0001\u0000\u0000\u0000 \u00be\u0001\u0000\u0000\u0000\""+
		"#\u0005\u0001\u0000\u0000#$\u0005\u0002\u0000\u0000$%\u0005\u0003\u0000"+
		"\u0000%&\u0005\u0004\u0000\u0000&\'\u0005,\u0000\u0000\'(\u0005\u0005"+
		"\u0000\u0000()\u0005,\u0000\u0000)5\u00050\u0000\u0000*6\u0003\u0002\u0001"+
		"\u0000+6\u0003\u0004\u0002\u0000,6\u0003\u0006\u0003\u0000-6\u0003\b\u0004"+
		"\u0000.6\u0003\n\u0005\u0000/6\u0003\f\u0006\u000006\u0003\u0010\b\u0000"+
		"16\u0003\u0014\n\u000026\u0003\u0018\f\u000036\u0003\u001a\r\u000046\u0005"+
		"-\u0000\u00005*\u0001\u0000\u0000\u00005+\u0001\u0000\u0000\u00005,\u0001"+
		"\u0000\u0000\u00005-\u0001\u0000\u0000\u00005.\u0001\u0000\u0000\u0000"+
		"5/\u0001\u0000\u0000\u000050\u0001\u0000\u0000\u000051\u0001\u0000\u0000"+
		"\u000052\u0001\u0000\u0000\u000053\u0001\u0000\u0000\u000054\u0001\u0000"+
		"\u0000\u000067\u0001\u0000\u0000\u000075\u0001\u0000\u0000\u000078\u0001"+
		"\u0000\u0000\u000089\u0001\u0000\u0000\u00009=\u00051\u0000\u0000:<\u0005"+
		"1\u0000\u0000;:\u0001\u0000\u0000\u0000<?\u0001\u0000\u0000\u0000=;\u0001"+
		"\u0000\u0000\u0000=>\u0001\u0000\u0000\u0000>@\u0001\u0000\u0000\u0000"+
		"?=\u0001\u0000\u0000\u0000@A\u0005\u0000\u0000\u0001A\u0001\u0001\u0000"+
		"\u0000\u0000BC\u0005\u0006\u0000\u0000CD\u0005+\u0000\u0000DE\u0005-\u0000"+
		"\u0000E\u0003\u0001\u0000\u0000\u0000FG\u0005\u0007\u0000\u0000GH\u0005"+
		"\b\u0000\u0000HI\u0005,\u0000\u0000IJ\u0005-\u0000\u0000J\u0005\u0001"+
		"\u0000\u0000\u0000KL\u0005\t\u0000\u0000LM\u0005+\u0000\u0000MN\u0005"+
		"-\u0000\u0000N\u0007\u0001\u0000\u0000\u0000OP\u0005\n\u0000\u0000PQ\u0005"+
		")\u0000\u0000QR\u0005-\u0000\u0000R\t\u0001\u0000\u0000\u0000ST\u0005"+
		"\u000b\u0000\u0000TU\u0005*\u0000\u0000UV\u0005-\u0000\u0000V\u000b\u0001"+
		"\u0000\u0000\u0000WX\u0005\f\u0000\u0000X]\u0003\u000e\u0007\u0000YZ\u0005"+
		"\r\u0000\u0000Z\\\u0003\u000e\u0007\u0000[Y\u0001\u0000\u0000\u0000\\"+
		"_\u0001\u0000\u0000\u0000][\u0001\u0000\u0000\u0000]^\u0001\u0000\u0000"+
		"\u0000^`\u0001\u0000\u0000\u0000_]\u0001\u0000\u0000\u0000`a\u0007\u0000"+
		"\u0000\u0000a\r\u0001\u0000\u0000\u0000be\u0005,\u0000\u0000cd\u0005\u0005"+
		"\u0000\u0000df\u0005,\u0000\u0000ec\u0001\u0000\u0000\u0000ef\u0001\u0000"+
		"\u0000\u0000f\u000f\u0001\u0000\u0000\u0000gh\u0005\u000e\u0000\u0000"+
		"hk\u0005,\u0000\u0000ij\u0005\u0005\u0000\u0000jl\u0005,\u0000\u0000k"+
		"i\u0001\u0000\u0000\u0000kl\u0001\u0000\u0000\u0000lm\u0001\u0000\u0000"+
		"\u0000mt\u00050\u0000\u0000ns\u0003\f\u0006\u0000os\u0003\u0012\t\u0000"+
		"ps\u0003\u0010\b\u0000qs\u0005-\u0000\u0000rn\u0001\u0000\u0000\u0000"+
		"ro\u0001\u0000\u0000\u0000rp\u0001\u0000\u0000\u0000rq\u0001\u0000\u0000"+
		"\u0000sv\u0001\u0000\u0000\u0000tr\u0001\u0000\u0000\u0000tu\u0001\u0000"+
		"\u0000\u0000uw\u0001\u0000\u0000\u0000vt\u0001\u0000\u0000\u0000wx\u0005"+
		"1\u0000\u0000x\u0011\u0001\u0000\u0000\u0000yz\u0005\u000f\u0000\u0000"+
		"z{\u0005\u0010\u0000\u0000{|\u0005\u0003\u0000\u0000|}\u0005,\u0000\u0000"+
		"}~\u0005\u0011\u0000\u0000~\u007f\u0007\u0001\u0000\u0000\u007f\u0080"+
		"\u0005\u0017\u0000\u0000\u0080\u0081\u0005\u0018\u0000\u0000\u0081\u0082"+
		"\u0005\u0019\u0000\u0000\u0082\u0083\u0005,\u0000\u0000\u0083\u0013\u0001"+
		"\u0000\u0000\u0000\u0084\u0085\u0005\u001a\u0000\u0000\u0085\u0087\u0005"+
		"0\u0000\u0000\u0086\u0088\u0003\u0016\u000b\u0000\u0087\u0086\u0001\u0000"+
		"\u0000\u0000\u0088\u0089\u0001\u0000\u0000\u0000\u0089\u0087\u0001\u0000"+
		"\u0000\u0000\u0089\u008a\u0001\u0000\u0000\u0000\u008a\u008b\u0001\u0000"+
		"\u0000\u0000\u008b\u008c\u0005-\u0000\u0000\u008c\u008d\u00051\u0000\u0000"+
		"\u008d\u0015\u0001\u0000\u0000\u0000\u008e\u008f\u0005\u001b\u0000\u0000"+
		"\u008f\u0090\u0005,\u0000\u0000\u0090\u0091\u0005\u001c\u0000\u0000\u0091"+
		"\u0092\u0005\u001d\u0000\u0000\u0092\u0097\u0003\u001c\u000e\u0000\u0093"+
		"\u0094\u0005\u0011\u0000\u0000\u0094\u0095\u0005\u001e\u0000\u0000\u0095"+
		"\u0096\u0005\u0017\u0000\u0000\u0096\u0098\u0005+\u0000\u0000\u0097\u0093"+
		"\u0001\u0000\u0000\u0000\u0097\u0098\u0001\u0000\u0000\u0000\u0098\u0099"+
		"\u0001\u0000\u0000\u0000\u0099\u009a\u0005\u0018\u0000\u0000\u009a\u009b"+
		"\u0005\u001f\u0000\u0000\u009b\u009e\u0003\u001e\u000f\u0000\u009c\u009d"+
		"\u0005 \u0000\u0000\u009d\u009f\u0003 \u0010\u0000\u009e\u009c\u0001\u0000"+
		"\u0000\u0000\u009e\u009f\u0001\u0000\u0000\u0000\u009f\u0017\u0001\u0000"+
		"\u0000\u0000\u00a0\u00a1\u0005!\u0000\u0000\u00a1\u00a2\u0005\"\u0000"+
		"\u0000\u00a2\u00a7\u0005,\u0000\u0000\u00a3\u00a4\u0005\r\u0000\u0000"+
		"\u00a4\u00a6\u0005,\u0000\u0000\u00a5\u00a3\u0001\u0000\u0000\u0000\u00a6"+
		"\u00a9\u0001\u0000\u0000\u0000\u00a7\u00a5\u0001\u0000\u0000\u0000\u00a7"+
		"\u00a8\u0001\u0000\u0000\u0000\u00a8\u0019\u0001\u0000\u0000\u0000\u00a9"+
		"\u00a7\u0001\u0000\u0000\u0000\u00aa\u00ab\u0005#\u0000\u0000\u00ab\u00ac"+
		"\u0005\"\u0000\u0000\u00ac\u00b1\u0005,\u0000\u0000\u00ad\u00ae\u0005"+
		"\r\u0000\u0000\u00ae\u00b0\u0005,\u0000\u0000\u00af\u00ad\u0001\u0000"+
		"\u0000\u0000\u00b0\u00b3\u0001\u0000\u0000\u0000\u00b1\u00af\u0001\u0000"+
		"\u0000\u0000\u00b1\u00b2\u0001\u0000\u0000\u0000\u00b2\u001b\u0001\u0000"+
		"\u0000\u0000\u00b3\u00b1\u0001\u0000\u0000\u0000\u00b4\u00b9\u0005,\u0000"+
		"\u0000\u00b5\u00b6\u0005$\u0000\u0000\u00b6\u00b8\u0005,\u0000\u0000\u00b7"+
		"\u00b5\u0001\u0000\u0000\u0000\u00b8\u00bb\u0001\u0000\u0000\u0000\u00b9"+
		"\u00b7\u0001\u0000\u0000\u0000\u00b9\u00ba\u0001\u0000\u0000\u0000\u00ba"+
		"\u001d\u0001\u0000\u0000\u0000\u00bb\u00b9\u0001\u0000\u0000\u0000\u00bc"+
		"\u00bd\u0007\u0002\u0000\u0000\u00bd\u001f\u0001\u0000\u0000\u0000\u00be"+
		"\u00bf\u0007\u0001\u0000\u0000\u00bf!\u0001\u0000\u0000\u0000\u000e57"+
		"=]ekrt\u0089\u0097\u009e\u00a7\u00b1\u00b9";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}