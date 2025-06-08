grammar IntentDsl;

// 1. Declare INDENT and DEDENT tokens explicitly.
tokens {
	INDENT,
	DEDENT
}

// 2. Add necessary Java imports for the lexer's members.
@lexer::header {
    import com.yuvalshavit.antlr4.DenterHelper;
    import org.antlr.v4.runtime.Token;
    import org.antlr.v4.runtime.CharStream;
    import org.antlr.v4.runtime.misc.Pair;
}

// 3. Replace manual indentation logic with DenterHelper.
@lexer::members {
    // Instantiate the DenterHelper.
    // It requires the token IDs for your NEWLINE, INDENT, and DEDENT tokens.
    // Use the generated lexer/parser constants (e.g., IntentDslLexer.NEWLINE, IntentDslParser.INDENT).
    private final DenterHelper denter = new DenterHelper(
        IntentDslLexer.NEWLINE,    // The token ID for your NEWLINE rule in this grammar
        IntentDslParser.INDENT,    // The token ID for the synthesized INDENT token
        IntentDslParser.DEDENT     // The token ID for the synthesized DEDENT token
    ) {
        @Override
        public Token pullToken() {
            // This method is called by DenterHelper to get the next raw token from your lexer.
            // It effectively wraps the standard ANTLR lexer's nextToken() method.
            // Crucial: We need to ensure that skipped tokens (like WS and COMMENT) are not passed
            // to the DenterHelper if they are between a NL and the next meaningful token.
            // The DenterHelper usually handles this, but if problems persist,
            // we might need a loop here to pull until a non-skipped token.
            // For now, let's assume `-> skip` is enough.
            return IntentDslLexer.super.nextToken();
        }

        
    };

    @Override
    public Token nextToken() {
		Token tkn = denter.nextToken();
		//System.out.println(tkn.toString());
        // All token retrieval is delegated to the DenterHelper.
	        return tkn;
    }
}

// --- Parser Rules ---

// The main entry point for the DSL. Expects an 'intent' declaration followed by EOF.
intent:
	'Create' 'Intent' 'for' 'resource' resourceName = IDENTIFIER 'as' intentName = IDENTIFIER INDENT
		(
		// Expect a NEWLINE and then an INDENT for the block
		descriptionBlock
		| aliasBlock
		| whereBlock
		| paginationBlock
		| limitBlock
		| selectBlock
		| joinBlock
		| parameterBlock
		| sortBlock
		| groupBlock
		| NEWLINE
	)+ DEDENT // After all child blocks, expect a DEDENT
	(DEDENT)* EOF;

// Rules defining various blocks within an intent. Each block's content is implicitly delimited by
// indentation.
descriptionBlock: 'Description' description = STRING NEWLINE;
aliasBlock: 'With' 'alias' alias = IDENTIFIER NEWLINE;
whereBlock: 'Where' condition = STRING NEWLINE;
paginationBlock: 'Paginated' paginatedValue = BOOLEAN NEWLINE;
limitBlock: 'Limit' limitValue = INT NEWLINE;

selectBlock:
	'Select' selectList (',' selectList)* (NEWLINE | INDENT);

selectList: field = IDENTIFIER ('as' alias = IDENTIFIER)?;

joinBlock:
	'Include' resourceName = IDENTIFIER ('as' alias = IDENTIFIER)? INDENT (
		selectBlock
		| joinFilterBlock
		| joinBlock
		| NEWLINE
	)* DEDENT;
// Nested blocks like 'joinBlock' correctly expect their own INDENT/DEDENT.

joinFilterBlock:
	'Add' 'filter' 'for' field = IDENTIFIER 'having' binding = (
		'exact'
		| 'like'
		| 'in'
		| 'greater_than'
		| 'less_than'
	) 'value' 'from' 'parameter' paramName = IDENTIFIER;

parameterBlock: 'Parameters' INDENT paramEntry+ NEWLINE DEDENT;
paramEntry:
	'Param' name = IDENTIFIER 'with' 'datatype' type = qualifiedName (
		'having' 'default' 'value' defaultValue = STRING
	)? 'from' 'source' source = paramSource ('using' bindingType)?;

sortBlock:
	'Sort' 'by' sortFields += IDENTIFIER (
		',' sortFields += IDENTIFIER
	)*;
groupBlock:
	'Group' 'by' groupFields += IDENTIFIER (
		',' groupFields += IDENTIFIER
	)*;

qualifiedName: IDENTIFIER ('.' IDENTIFIER)*;
paramSource:
	'static'
	| 'user_context'
	| 'security_profile'
	| 'request';
bindingType:
	'exact'
	| 'like'
	| 'in'
	| 'greater_than'
	| 'less_than';

// --- Lexer Rules ---

// Basic token definitions
BOOLEAN: 'true' | 'false';
INT: [0-9]+;
STRING:
	'"' (~["\r\n] | '\\"')* '"'; // String literals, allowing escaped quotes
IDENTIFIER:
	[a-zA-Z_] [a-zA-Z0-9_]*; // Identifiers (e.g., variable names)

// 4. Critical: The NEWLINE rule. This rule now matches the newline followed by *spaces*. If your
// input uses tabs, change ' '* to '\t'* or ' |\t'*. Important: It must match the exact indentation
// character your input uses.
NEWLINE: ('\r'? '\n') -> channel(HIDDEN); // Ignore actual newlines
NL:
	NEWLINE_SPACES -> type(NEWLINE); // This is what DenterHelper sees

fragment NEWLINE_SPACES: ('\r'? '\n') [ \t]*;

// Whitespace within a line (not leading indentation) should be skipped.
WS: ' ' -> skip;
// Line comments are skipped.
COMMENT: '//' ~[\r\n]* -> skip;

// If you have special characters like parentheses, brackets, or braces, define them. They usually
// don't need actions with DenterHelper if the version doesn't support 'opened'.
/*
 OPAREN: '('; CPAREN: ')'; OBRACKET: '['; CBRACKET: ']'; OBRACE: '{'; CBRACE: '}';
 */