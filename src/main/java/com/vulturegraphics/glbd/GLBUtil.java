package com.vulturegraphics.glbd;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;

/**
 * Main routine for glbd application.
 */
class GLBUtil {

    void usage() {
        System.out.println("Glen's book database, try \"add\" or \"find\"");
    }

    void add(String[] args) throws Exception {
        Logger log = LoggerFactory.getLogger(GLBUtil.class);
        OptionParser parser = new OptionParser();
        parser.accepts("alias-for", "This author's alias")
                .withRequiredArg().describedAs("A pattern that resolves to a single author");
        parser.accepts("title", "Add title")
                .withRequiredArg().describedAs("Full title of book to add for this author");
        parser.accepts("author", "Add author")
                .requiredIf("title", "alias-for")
                .withRequiredArg().describedAs("Author's name in the format LAST, FIRST");
        parser.acceptsAll(asList("?", "help"), "this help message").forHelp();

        OptionSet options;
        try {
            options = parser.parse(args);
            if (options.has("help")) {
                parser.printHelpOn(System.out);
                return;
            }
        } catch (OptionException ex) {
            System.out.println(ex.getMessage());
            parser.printHelpOn(System.out);
            return;
        }

        // Start a DB job queue for remaining work
        SQLiteQueue sqLiteQueue = DBase.getInstance().getQueue();

        // Here if parsing is complete and correct.
        // Figure out if the user is trying to add an author, possibly with an alias,
        // or a book entry (author + title).
        //   add --author "Doe, John"
        //   add --author "Doe, John" --alias "Public, John Q."
        //   add --author "Doe, John" --title "Nobody Remembers Me"
        // But not
        //   add --author "Doe, John" --alias "Public, John Q." --title "Nobody Remembers Me"

        if (options.has("author") && options.has("title")) {
            // The most common scenario: add a new book title by the given author.
            String name = (String) options.valueOf("author");
            List<Author> authors = sqLiteQueue.execute(
                    new SQLiteJob<List<Author>>() {
                        protected List<Author> job(SQLiteConnection conn) throws SQLiteException {
                            return Author.find(conn, name);
                        }
                    }
            ).complete();
            if (authors.size() < 1) {
                System.out.println("Author not in DB, add author first before adding title");
            }
            else if (authors.size() > 1) {
                System.out.println("Not supporting multiple author entry at this time");
            }
            else {
                String title = (String) options.valueOf("title");
                Author writer = authors.get(0);

                sqLiteQueue.execute(
                    new SQLiteJob<Void>() {
                        protected Void job(SQLiteConnection conn) throws SQLiteException {
                            conn.exec("BEGIN");
                            try {
                                Book b = Book.addTitle(conn, title);
                                writer.addBook(conn, b);
                            }
                            catch (SQLiteException se) {
                                conn.exec("ROLLBACK");
                                throw se;
                            }
                            conn.exec("COMMIT");
                            return null;
                        }
                    }
                ).complete();
            }
        }
        else if (options.has("author")) {
            String author_name = (String) options.valueOf("author");
            // Add a new author
            sqLiteQueue.execute(
                new SQLiteJob<Void>() {
                    protected Void job(SQLiteConnection conn) throws SQLiteException {
                        conn.exec("BEGIN");
                        try {
                            if (options.has("alias-for")) {
                                Author.addAuthor(conn,
                                                 author_name,
                                                 (String) options.valueOf("alias-for"));
                            } else {
                                Author.addAuthor(conn, author_name, null);
                            }
                        }
                        catch (SQLiteException se) {
                            conn.exec("ROLLBACK");
                            throw se;
                        }
                        conn.exec("COMMIT");
                        return null;
                    }
                }
             ).complete();
        }

        sqLiteQueue.stop(true).join();
    }


    void find(String[] args) throws Exception {
        Logger log = LoggerFactory.getLogger(GLBUtil.class);
        OptionParser parser = new OptionParser();
        parser.accepts("author", "Find author")
                .withRequiredArg().describedAs("Author pattern");
        parser.accepts("title", "Find title")
                .withRequiredArg().describedAs("Title pattern");
        parser.acceptsAll(asList("?", "help"), "this help message").forHelp();

        OptionSet options;
        try {
            options = parser.parse(args);
            if (options.has("help")) {
                parser.printHelpOn(System.out);
                return;
            }
        } catch (OptionException ex) {
            System.out.println(ex.getMessage());
            parser.printHelpOn(System.out);
            return;
        }

        // Start a DB job queue for remaining work
        SQLiteQueue sqLiteQueue = DBase.getInstance().getQueue();

        if (options.has("author")) {
            String pattern = (String) options.valueOf("author");
            List<Author> authors = sqLiteQueue.execute(
                    new SQLiteJob<List<Author>>() {
                        protected List<Author> job(SQLiteConnection conn) throws SQLiteException {
                            return Author.find(conn, pattern);
                        }
                    }
            ).complete();
            if (authors.size() == 0) {
                System.out.println("Author " + pattern + " not found.");
            } else {
                for (Author a : authors) {
                    System.out.println(a);
                    sqLiteQueue.execute(
                            new SQLiteJob<Void>() {
                                protected Void job(SQLiteConnection conn) throws SQLiteException {
                                    List<Book> books = a.bibliography(conn);
                                    for (Book b : books) {
                                        System.out.println("  " + b);
                                    }
                                    return null;
                                }
                            }
                    ).complete();
                }
            }
        }

        sqLiteQueue.stop(true).join();
    }

    /*
     * main - Setup application and run based on arguments
     */
    public static void main(String[] args) throws Exception {
        Logger log = LoggerFactory.getLogger(GLBUtil.class);

        GLBUtil glbutil = new GLBUtil();
        if (args.length < 1) {
            glbutil.usage();
            System.exit(0);
        }

        // Set logging for this application.

        // Logging policy is purposely not set for the library but
        // users may want to use the following line since the sqlite
        // classes do a substantial amount of logging.
        java.util.logging.Logger.getLogger("com.almworks.sqlite4java")
            .setLevel(java.util.logging.Level.SEVERE);

        // Process commands
        if (args.length > 1) {
            switch (args[0].toLowerCase()) {
                case "add":
                    glbutil.add(copyOfRange(args, 1, args.length));
                    break;
                case "find":
                    glbutil.find(copyOfRange(args, 1, args.length));
                    break;
                default:
                    glbutil.usage();
                    break;
            }
        } else {
            glbutil.usage();
        }

    }
}
