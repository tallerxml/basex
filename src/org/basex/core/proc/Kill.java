package org.basex.core.proc;

import org.basex.core.Process;
import org.basex.core.User;
import org.basex.io.PrintOutput;
import org.basex.server.Sessions;

/**
 * Evaluates the 'kill' command and stops all current sessions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Kill extends Process {
  /**
   * Default constructor.
   */
  public Kill() {
    super(STANDARD | User.ADMIN);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    final Sessions ss = context.sessions;
    final int s = ss.size();
    while(ss.size() > 0) ss.get(0).exit();
    return info("% sessions killed.", s);
  }
}
