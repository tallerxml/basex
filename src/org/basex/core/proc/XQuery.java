package org.basex.core.proc;

import java.io.IOException;

import org.basex.core.Commands.Cmd;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'xquery' command and processes an XQuery request.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class XQuery extends AQuery {
  /**
   * Default constructor.
   * @param query query to process
   */
  public XQuery(final String query) {
    super(STANDARD, query);
  }

  @Override
  protected boolean exec(final PrintOutput out) throws IOException {
    return query(args[0]) && out(out);
  }

  @Override
  public String toString() {
    return Cmd.XQUERY + " " + args[0];
  }
}
