package org.xresource.internal.intent.core.dsl;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Element;
import org.xresource.internal.intent.core.antlr.IntentDslLexer;
import org.xresource.internal.intent.core.antlr.IntentDslParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class IntentDslCompiler {
  /**
   * Compiler for Intent DSL strings into XML DOM structure.
   *
   * <p>
   * This method parses a DSL string representing an Intent definition
   * and transforms it into a traversible {@link org.w3c.dom.Element} (DOM node).
   * If the DSL contains syntax errors, the compilation will be aborted and an
   * exception will be thrown with detailed error messages.
   *
   * @param dslInput the raw Intent DSL string input
   * @return the DOM {@link Element} representing the compiled XML structure
   * @throws Exception if any syntax errors are found or parsing fails
   */
  public Element compile(String dslInput) throws Exception {
    CharStream charStream = CharStreams.fromReader(new StringReader(dslInput));
    IntentDslLexer lexer = new IntentDslLexer(charStream);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    IntentDslParser parser = new IntentDslParser(tokens);

    // Custom error listener to collect syntax errors
    SyntaxErrorCollector errorListener = new SyntaxErrorCollector();
    parser.removeErrorListeners(); // remove default console errors
    parser.addErrorListener(errorListener);

    ParseTree tree = parser.intent();

    // Throw exception if syntax errors were found
    if (!errorListener.getErrors().isEmpty()) {
      throw new RuntimeException("Intent DSL syntax errors:\n" + String.join("\n", errorListener.getErrors()));
    }

    // Visit tree to build DOM
    IntentDslVisitorToXml visitor = new IntentDslVisitorToXml();
    return visitor.visit(tree);
  }

  /**
   * Internal custom error listener to accumulate parser errors.
   */
  private static class SyntaxErrorCollector extends BaseErrorListener {
    private final List<String> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
        int line, int charPositionInLine,
        String msg, RecognitionException e) {
      errors.add("Line " + line + ":" + charPositionInLine + " " + msg);
    }

    public List<String> getErrors() {
      return errors;
    }
  }
}
