package org.basex.test.cs;

import org.basex.core.Session;
import org.basex.core.Context;
import org.basex.core.LocalSession;
import org.basex.core.Process;
import org.basex.core.Text;
import org.basex.core.proc.*;
import org.basex.core.Commands.*;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.io.NullOutput;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.basex.util.Token.*;

/**
 * This class tests the database commands.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class CmdTest {
  /** Database context. */
  protected static final Context CONTEXT = new Context();
  /** Test file. */
  private static final String FILE = "input.xml";
  /** Test name. */
  private static final String NAME = "input";
  /** Socket reference. */
  static Session session;

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    session = new LocalSession(CONTEXT);
  }

  /** Removes test databases and closes the database context. */
  @AfterClass
  public static void finish() {
    CONTEXT.close();
  }

  /**
   * Creates the database.
   */
  @After
  public final void setUp() {
    process(new DropDB(NAME));
  }

  /** Command test. */
  @Test
  public final void close() {
    ok(new Close());
    ok(new CreateDB(FILE));
    ok(new Close());
    no(new InfoDB());
  }

  /** Command Test. */
  @Test
  public final void copy() {
    no(new Copy("//title", "// li"));
    ok(new CreateDB(FILE));

    final Data data = CONTEXT.data();
    final int size = data != null ? data.meta.size : 0;

    ok(new Copy("//unknown", "//li"));
    ok(data, size);
    ok(new Copy("//title", "//li"));
    ok(data, size + 4);
    ok(new Copy("//  title", "//li", 1));
    no(new Copy("/", "//text()"));
  }

  /** Command Test. */
  @Test
  public final void createDB() {
    ok(new CreateDB(FILE));
    ok(new InfoDB());
    ok(new CreateDB(FILE, FILE));
    no(new CreateDB("abcde"));
    no(new CreateDB(""));
  }

  /** Command Test. */
  @Test
  public final void createFS() {
    ok(new CreateFS("bin", "fs"));
    ok(new DropDB("fs"));
  }

  /** Command Test. */
  @Test
  public final void createIndex() {
    for(final CmdIndex cmd : CmdIndex.values()) no(new CreateIndex(cmd));
    ok(new CreateDB(FILE));
    for(final CmdIndex cmd : CmdIndex.values()) ok(new CreateIndex(cmd));
  }

  /** Command Test. */
  @Test
  public final void createMAB() {
    // no test file available
    no(new CreateMAB("abcde", "abcde"));
  }

  /** Command Test. */
  @Test
  public final void cs() {
    no(new Cs("//li"));
    ok(new CreateDB(FILE));
    ok(new Cs("//  li"));
    ok(CONTEXT.current(), 2);
    ok(new Cs("."));
    ok(CONTEXT.current(), 2);
    ok(new Cs("/"));
    ok(CONTEXT.current(), 1);
  }

  /** Command Test. */
  @Test
  public final void delete() {
    no(new Delete("/"));
    ok(new CreateDB(FILE));
    ok(new Delete("// node()"));
    no(new Delete(""));
  }

  /** Command Test. */
  @Test
  public final void dropDB() {
    no(new DropDB(NAME));
    ok(new CreateDB(FILE));
    no(new DropDB(FILE));
    ok(new DropDB(NAME));
    no(new DropDB(NAME));
  }

  /** Command Test. */
  @Test
  public final void dropIndex() {
    for(final CmdIndex cmd : CmdIndex.values()) no(new DropIndex(cmd));
    ok(new CreateDB(FILE));
    for(final CmdIndex cmd : CmdIndex.values()) ok(new DropIndex(cmd));
  }

  /** Command Test. */
  @Test
  public final void export() {
    final IO io = IO.get("export.xml");
    no(new Export(io.path()));
    ok(new CreateDB(FILE));
    ok(new Export(io.path()));
    ok(io.exists());
    ok(io.delete());
  }

  /** Command Test. */
  @Test
  public final void find() {
    no(new Find("1"));
    ok(new CreateDB(FILE));
    ok(new Find("1"));
  }

  /** Command Test. */
  @Test
  public final void help() {
    ok(new Help(""));
    ok(new Help(null));
  }

  /** Command Test. */
  @Test
  public final void info() {
    ok(new Info());
  }

  /** Command Test. */
  @Test
  public final void infoDB() {
    no(new InfoDB());
    ok(new CreateDB(FILE));
    ok(new InfoDB());
  }

  /** Command Test. */
  @Test
  public final void infoIndex() {
    no(new InfoIndex());
    ok(new CreateDB(FILE));
    ok(new InfoIndex());
  }

  /** Command Test. */
  @Test
  public final void infoTable() {
    no(new InfoTable("1", "2"));
    ok(new CreateDB(FILE));
    ok(new InfoTable("1", "2"));
    ok(new InfoTable("1", null));
    ok(new InfoTable("// li", null));
  }

  /** Command Test. */
  @Test
  public final void insert() {
    no(new Insert(CmdUpdate.ELEMENT, "//title", "name"));
    ok(new CreateDB(FILE));

    final Data data = CONTEXT.data();
    final int size = data != null ? data.meta.size : 0;

    ok(new Insert(CmdUpdate.ELEMENT, "//unknown", "name"));
    ok(data, size);
    no(new Insert(CmdUpdate.ELEMENT, "//title", "in valid"));
    ok(data, size);
    ok(new Insert(CmdUpdate.ELEMENT, "//  title", "name"));
    ok(data, size + 1);

    ok(new Insert(CmdUpdate.TEXT, "//title", "abc"));
    ok(data, size + 2);
    // text is added to existing texts
    ok(new Insert(CmdUpdate.TEXT, "//li", "abc"));
    ok(data, size + 2);

    no(new Insert(CmdUpdate.ATTRIBUTE, "//title", "in valid", "new value"));
    ok(new Insert(CmdUpdate.ATTRIBUTE, "//title", "name", "new value"));
    ok(data, size + 3);

    ok(new Insert(CmdUpdate.COMMENT, "//title", "new value"));
    ok(data, size + 4);

    no(new Insert(CmdUpdate.PI, "//title", 1, "in valid", "new value"));
    ok(new Insert(CmdUpdate.PI, "//title", 1, "name", "new value"));
    ok(data, size + 5);

    no(new Insert(CmdUpdate.FRAGMENT, "//title", 1, "<xml"));
    ok(new Insert(CmdUpdate.FRAGMENT, "//title", 1, "<xml/>"));
    ok(data, size + 6);
    ok(new Insert("FRAGMENT", "//title", 1, "<xml/>"));
    ok(new Insert("fragment", "//title", 1, "<xml/>"));
  }

  /** Command Test. */
  @Test
  public final void list() {
    ok(new List());
    ok(new CreateDB(FILE));
    ok(new List());
  }

  /** Command Test. */
  @Test
  public final void open() {
    no(new Open(NAME));
    ok(new CreateDB(FILE));
    ok(new Open(NAME));
  }

  /** Command Test. */
  @Test
  public final void optimize() {
    no(new Optimize());
    ok(new CreateDB(FILE));
    ok(new Optimize());
  }

  /** Command Test. */
  @Test
  public final void run() {
    final IO io = IO.get("test.xq");
    no(new Run(io.path()));
    try {
      io.write(token("// li"));
    } catch(final Exception ex) {
      fail(ex.toString());
    }
    no(new Run(io.path()));
    ok(new CreateDB(FILE));
    ok(new Run(io.path()));
    io.delete();
  }

  /** Command Test. */
  @Test
  public final void set() {
    ok(new Set(CmdSet.INFO, Text.ON));
    ok(new Set(CmdSet.INFO, false));
    ok(new Set(CmdSet.CHOP, true));
    ok(new Set("runs", 1));
    no(new Set("runs", true));
  }

  /** Command Test. */
  @Test
  public final void update() {
    no(new Update(CmdUpdate.ELEMENT, "//title", "name"));
    ok(new CreateDB(FILE));

    ok(new Update(CmdUpdate.ELEMENT, "//unknown", "name"));
    no(new Update(CmdUpdate.ELEMENT, "//title", ""));
    no(new Update(CmdUpdate.ELEMENT, "//title", "in valid"));
    ok(new Update(CmdUpdate.ELEMENT, "//  title", "name"));

    ok(new Update(CmdUpdate.TEXT, "//title", "abc"));

    no(new Update(CmdUpdate.ATTRIBUTE, "//title", "", "new value"));
    no(new Update(CmdUpdate.ATTRIBUTE, "//title", "in valid", "new value"));
    ok(new Update(CmdUpdate.ATTRIBUTE, "//title", "name", "new value"));

    ok(new Update(CmdUpdate.COMMENT, "//title", "new value"));

    no(new Update(CmdUpdate.PI, "//title", "", "new value"));
    no(new Update(CmdUpdate.PI, "//title", "in valid", "new value"));
    ok(new Update(CmdUpdate.PI, "//title", "name", "new value"));
    ok(new Update("PI", "//title", "name", "new value"));
    ok(new Update("pi", "//  title", "name", "new value"));
  }

  /** Command Test. */
  @Test
  public final void xQuery() {
    no(new XQuery("/"));
    ok(new CreateDB(FILE));
    ok(new XQuery("/"));
    ok(new XQuery("1"));
    no(new XQuery("1+"));
  }

  /** Command Test. */
  @Test
  public final void xQueryMV() {
    // no test file available
    no(new XQueryMV("1", "1", "// li"));
  }

  /**
   * Assumes that the specified flag is successful.
   * @param flag flag
   */
  private static void ok(final boolean flag) {
    assertTrue(flag);
  }

  /**
   * Assumes that the database has the specified number of nodes.
   * @param data data reference
   * @param size expected size
   */
  private static void ok(final Data data, final int size) {
    if(data != null) assertEquals(data.meta.size, size);
  }

  /**
   * Assumes that the nodes have the specified number of nodes.
   * @param nodes context nodes
   * @param size expected size
   */
  private static void ok(final Nodes nodes, final int size) {
    if(nodes != null) assertEquals(nodes.size(), size);
  }

  /**
   * Assumes that this command is successful.
   * @param pr process reference
   */
  private void ok(final Process pr) {
    final String msg = process(pr);
    if(msg != null) fail(msg);
  }

  /**
   * Assumes that this command fails.
   * @param pr process reference
   */
  private void no(final Process pr) {
    ok(process(pr) != null);
  }

  /**
   * Runs the specified process.
   * @param pr process reference
   * @return success flag
   */
  private String process(final Process pr) {
    try {
      return session.execute(pr, new NullOutput()) ? null : session.info();
    } catch(final Exception ex) {
      return ex.toString();
    }
  }
}
