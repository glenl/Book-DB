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
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;

class GLBUtil {

    /**
     * main - Setup application and run based on arguments
     */
    public static void main(String[] args) throws Exception {
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

        // Set logging for this application.

        // Logging policy is purposely not set for the library but
        // users may want to use the following line since the sqlite
        // classes do a substantial amount of logging.
        java.util.logging.Logger.getLogger("com.almworks.sqlite4java")
                .setLevel(java.util.logging.Level.WARNING);

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
            if (options.has("title")) {
                String title = (String) options.valueOf("title");
                if (authors.size() > 1) {
                    System.out.println("Multiple authors not supported (yet)");
                } else {
                    Author a = authors.get(0);
                    Book b = sqLiteQueue.execute(
                            new SQLiteJob<Book>() {
                                protected Book job(SQLiteConnection conn) throws SQLiteException {
                                    return Book.addTitle(conn, title);
                                }
                            }
                    ).complete();
                    long volume_id = sqLiteQueue.execute(
                            new SQLiteJob<Long>() {
                                protected Long job(SQLiteConnection conn) throws SQLiteException {
                                    return a.addBook(conn, b);
                                }
                            }
                    ).complete();
                    log.info("New book added, id = " + volume_id);
                }
            } else {
                if (authors.size() == 0) {
                    System.out.println("No author match found for " + pattern);
                } else {
                    // No title, list author bibliography
                    for (Author author: authors) {
                        System.out.println(author);
                        List<Book> books = sqLiteQueue.execute(
                                new SQLiteJob<List<Book>>() {
                                    protected List<Book> job(SQLiteConnection conn) throws SQLiteException {
                                        return author.bibliography(conn);
                                    }
                                }
                        ).complete();
                        for (Book book: books) {
                            System.out.println("   " + book);
                        }
                    }
                }
            }
        }

        sqLiteQueue.stop(true).join();
    }

}

